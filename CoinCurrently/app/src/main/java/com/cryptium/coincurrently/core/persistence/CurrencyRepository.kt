package com.cryptium.coincurrently.core.persistence
import com.cryptium.coincurrently.core.persistence.UserPreferences
import android.content.Context
import android.util.Log
import com.cryptium.coincurrently.core.extensions.unwrapOrNull
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.cryptium.coincurrently.core.extensions.getActivity
import com.cryptium.coincurrently.core.extensions.updateCurrecyWidgets
import com.cryptium.coincurrently.core.analytics.FirebaseManager
import com.microsoft.signalr.HubConnection
import com.microsoft.signalr.HubConnectionBuilder
import com.microsoft.signalr.HubConnectionState
import com.microsoft.signalr.TransportEnum
import io.reactivex.rxjava3.core.Single
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import com.cryptium.coincurrently.core.networking.CacheUrls
import com.cryptium.coincurrently.core.networking.MainRepository
import com.cryptium.coincurrently.core.networking.ResponseModels.CGSimplePriceUpdate
import com.cryptium.coincurrently.core.networking.ResponseModels.Candle
import com.cryptium.coincurrently.core.networking.ResponseModels.CryptoDetailInfoModel
import com.cryptium.coincurrently.core.networking.ResponseModels.PriceVolumeModel
import com.cryptium.coincurrently.core.networking.ResponseModels.TimeCharts
import com.cryptium.coincurrently.core.networking.ResponseModels.Toplist
import com.cryptium.coincurrently.core.networking.ResponseModels.parseCandles
import com.cryptium.coincurrently.core.networking.RetrofitBuilder
import com.cryptium.coincurrently.core.networking.ResponseModels.serverBaseUrl
import kotlin.math.max

object CurrencyRepository {
    private val gson = Gson()

    private val cryptoDetailInfoType = object : TypeToken<CryptoDetailInfoModel>() {}.type
    private val priceVolumeType = object : TypeToken<PriceVolumeModel>() {}.type

    var cryptoInfo: CryptoDetailInfoModel? = null
    var cryptoDetail: MutableMap<String, CryptoDetailInfoModel>? = null
    var cryptoPrices: MutableMap<String, PriceVolumeModel>? = null
    var cryptoCandles: MutableMap<String, List<Candle>>? = null

    var widgetCurrencies: List<Toplist> = UserPreferences.getWidgetCurrency()
    var bitcoinData: CryptoDetailInfoModel? = null

    private var hubConnection: HubConnection? = null
    private var onSignalRMessage: ((CGSimplePriceUpdate) -> Unit)? = null
    private var subscribedCoinIds: List<String> = emptyList()
    private var isSignalRReconnecting = false
    private var signalRReconnectJob: Job? = null
    private val signalRMutex = Mutex()
    private val signalRScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    init {
        try {
            @Suppress("UNCHECKED_CAST")
            bitcoinData = RetrofitBuilder.getCached(CacheUrls.cryptoDetail("bitcoin", true), cryptoDetailInfoType, includeExpired = true)?.data as? CryptoDetailInfoModel
                ?: RetrofitBuilder.getCached(CacheUrls.cryptoDetail("bitcoin", false), cryptoDetailInfoType, includeExpired = true)?.data as? CryptoDetailInfoModel
        } catch (_: Exception) {
            // StorageManager/RetrofitBuilder may not be ready yet; cache will populate on first fetch
        }
    }

    /**
     * Hydrates [cryptoDetail], [cryptoPrices], and [cryptoCandles] from persisted Retrofit cache
     * so detail screens can show last data before network. Mirrors disk/cache keys used by [getCryptoDetail],
     * [getPriceVolume], and [getCandleStick].
     */
    fun loadCachedDataForCoin(id: String, chart: TimeCharts, includeExpired: Boolean = true) {
        if (id.isEmpty()) return

        @Suppress("UNCHECKED_CAST")
        val detail: CryptoDetailInfoModel? =
            (RetrofitBuilder.getCached(CacheUrls.cryptoDetail(id, true), cryptoDetailInfoType, includeExpired)?.data as? CryptoDetailInfoModel)
                ?: (RetrofitBuilder.getCached(CacheUrls.cryptoDetail(id, false), cryptoDetailInfoType, includeExpired)?.data as? CryptoDetailInfoModel)

        detail?.let {
            cryptoDetail = (cryptoDetail ?: mutableMapOf()).also { m -> m[id] = it }
            cryptoInfo = it
        }

        val currencyCode = UserPreferences.getSelectedCurrency(null).code
        @Suppress("UNCHECKED_CAST")
        val pv: PriceVolumeModel? =
            (RetrofitBuilder.getCached(CacheUrls.priceVolume(id, currencyCode, chart.days, chart.interval, true), priceVolumeType, includeExpired)?.data as? PriceVolumeModel)
                ?: (RetrofitBuilder.getCached(CacheUrls.priceVolume(id, currencyCode, chart.days, chart.interval, false), priceVolumeType, includeExpired)?.data as? PriceVolumeModel)

        pv?.let { model ->
            cryptoPrices = (cryptoPrices ?: mutableMapOf()).also { m -> m["${id}-${chart.days}"] = model }
        }

        var candleCached = RetrofitBuilder.getCached<String>(CacheUrls.candles(id, currencyCode, chart.days, true), includeExpired)
        if (candleCached == null) {
            candleCached = RetrofitBuilder.getCached<String>(CacheUrls.candles(id, currencyCode, chart.days, false), includeExpired)
        }
        candleCached?.let { cached ->
            parseCandles(cached.data)?.candle?.let { list ->
                cryptoCandles = (cryptoCandles ?: mutableMapOf()).also { m -> m["${id}-${chart.days}"] = list }
            }
        }
    }

    suspend fun getCryptoDetail(id: String?, isFreeUrl: Boolean = true): CryptoDetailInfoModel? {
        val id = id ?: return null
        val url = CacheUrls.cryptoDetail(id, isFreeUrl)

        @Suppress("UNCHECKED_CAST")
        (RetrofitBuilder.getCached(url, cryptoDetailInfoType)?.data as? CryptoDetailInfoModel)?.let { data ->
            cryptoInfo = data
            cryptoDetail = (cryptoDetail ?: mutableMapOf()).also { it[id] = data }
            return data
        }

        Log.w("Network call ", "Crypto detail")

        val cryptoDetailResult = MainRepository.getCryptoDetailInfo(id, isFreeUrl).unwrapOrNull()

        cryptoDetailResult?.let {
            cryptoDetail = (cryptoDetail ?: mutableMapOf()).also { it[id] = cryptoDetailResult }
            cryptoInfo = it
        } ?: run {
            if (isFreeUrl) {
                return getCryptoDetail(id, false)
            }
        }

        return cryptoDetailResult
    }

    suspend fun getPriceVolume(id: String?, chart: TimeCharts, context: Context? = null, isFreeUrl: Boolean = true): PriceVolumeModel? {
        val id = id ?: return null
        val currencyCode = UserPreferences.getSelectedCurrency(context).code
        val url = CacheUrls.priceVolume(id, currencyCode, chart.days, chart.interval, isFreeUrl)

        @Suppress("UNCHECKED_CAST")
        (RetrofitBuilder.getCached(url, priceVolumeType)?.data as? PriceVolumeModel)?.let { data ->
            cryptoPrices = (cryptoPrices ?: mutableMapOf()).also { it["$id-${chart.days}"] = data }
            return data
        }

        Log.w("Network call ", "Price volume")

        val priceVolumeModel = MainRepository.getPriceVolume(id, currencyCode, chart.days, chart.interval, isFreeUrl).unwrapOrNull()

        priceVolumeModel?.let {
            cryptoPrices = (cryptoPrices ?: mutableMapOf()).also { m -> m["$id-${chart.days}"] = it }
        } ?: run {
            if (isFreeUrl) {
                return getPriceVolume(id, chart, context, false)
            }
        }

        return priceVolumeModel
    }

    suspend fun getCandleStick(id: String?, days: String, isFreeUrl: Boolean = true): List<Candle>? {
        val id = id ?: return null
        val currencyCode = UserPreferences.getSelectedCurrency(null).code.lowercase()
        val url = CacheUrls.candles(id, currencyCode, days, isFreeUrl)

        RetrofitBuilder.getCached<String>(url)?.let { cached ->
            var dataPoints = parseCandles(cached.data)?.candle
            val divider = 1
            dataPoints = filterCandles(
                dataPoints,
                dataPoints?.maxBy { it.high ?: 0.0 }?.high ?: 0.0,
                dataPoints?.minBy { it.low ?: 0.0 }?.low ?: 0.0,
                divider
            )
            dataPoints?.let {
                cryptoCandles = (cryptoCandles ?: mutableMapOf()).also { m -> m["$id-$days"] = it }
                return it
            }
        }

        Log.w("Network call ", "Candles")

        val candles: String? = MainRepository.getCandleChart(id, currencyCode, days, isFreeUrl).unwrapOrNull()
        var dataPoints = parseCandles(candles)?.candle

        val divider = 1
        dataPoints = filterCandles(
            dataPoints,
            dataPoints?.maxBy { it.high ?: 0.0 }?.high ?: 0.0,
            dataPoints?.minBy { it.low ?: 0.0 }?.low ?: 0.0,
            divider
        )

        dataPoints?.let {
            cryptoCandles = (cryptoCandles ?: mutableMapOf()).also { m -> m["$id-$days"] = it }
        } ?: run {
            if (isFreeUrl) {
                return getCandleStick(id, days, false)
            }
        }

        return dataPoints
    }

    suspend fun getBitcoin(isFreeUrl: Boolean = true): CryptoDetailInfoModel? {
        val url = CacheUrls.cryptoDetail("bitcoin", isFreeUrl)

        @Suppress("UNCHECKED_CAST")
        (RetrofitBuilder.getCached(url, cryptoDetailInfoType)?.data as? CryptoDetailInfoModel)?.let { data ->
            bitcoinData = data
            return data
        }

        Log.w("Network call ", "Bitcoin")

        val bitcoin = MainRepository.getCryptoDetailInfo("bitcoin", isFreeUrl).unwrapOrNull()

        bitcoin?.let {
            bitcoinData = it
        } ?: run {
            if (isFreeUrl) {
                return getBitcoin(false)
            }
        }

        return bitcoin
    }

    fun changeWidgetsOrder(currencies: List<Toplist>, context: Context) {
        if (currencies != widgetCurrencies) {
            widgetCurrencies = currencies
            UserPreferences.setWidgetCurrency(currencies)
            context.getActivity()?.updateCurrecyWidgets()
        }
    }

    fun deleteWidget(coin: Toplist, context: Context) {
        val oldList = widgetCurrencies.toMutableList()
        oldList.removeIf { it.id == coin.id }
        widgetCurrencies = oldList
        UserPreferences.setWidgetCurrency(widgetCurrencies)
        context.getActivity()?.updateCurrecyWidgets()
    }

    suspend fun getMultipleCurrencies(ids: String, currencyCode: String): List<Toplist>? {
        if(ids.isEmpty()) {
            return null
        }

        Log.w("Network call ", "Multiple coins")

        return MainRepository.getMultipleCurrencies(ids, currencyCode).unwrapOrNull()
    }

    fun filterPriceVolume(priceVolumeModel: PriceVolumeModel?, divider: Int): PriceVolumeModel? {
        val divider = max(1, divider)

        priceVolumeModel?.prices = filterDataPoints(
            priceVolumeModel?.prices,
            priceVolumeModel?.prices?.map({ it.lastOrNull() ?: 0.0 })?.maxOfOrNull({ it }) ?: 0.0,
            priceVolumeModel?.prices?.map({ it.lastOrNull() ?: 0.0 })?.minOfOrNull({ it }) ?: 0.0,
            divider
        ) ?: arrayListOf()

        priceVolumeModel?.total_volumes = filterDataPoints(
            priceVolumeModel?.total_volumes,
            priceVolumeModel?.total_volumes?.map({ it.lastOrNull() ?: 0.0 })?.maxOfOrNull({ it }) ?: 0.0,
            priceVolumeModel?.total_volumes?.map({ it.lastOrNull() ?: 0.0 })?.minOfOrNull({ it }) ?: 0.0,
            divider
        ) ?: arrayListOf()

        priceVolumeModel?.market_caps = filterDataPoints(
            priceVolumeModel?.market_caps,
            priceVolumeModel?.market_caps?.map({ it.lastOrNull() ?: 0.0 })?.maxOfOrNull({ it }) ?: 0.0,
            priceVolumeModel?.market_caps?.map({ it.lastOrNull() ?: 0.0 })?.minOfOrNull({ it }) ?: 0.0,
            divider
        ) ?: arrayListOf()

        return priceVolumeModel
    }

    private fun filterDataPoints(dataPoints: ArrayList<ArrayList<Double?>>?, maxElement: Double?, minElement: Double?, divider: Int): ArrayList<ArrayList<Double?>>? {
        val array = dataPoints?.mapIndexedNotNull { index, element ->
            if(index % divider == 0 || element.lastOrNull() == maxElement || element.lastOrNull() == minElement) element else null
        }

        return ArrayList(array?.map { ArrayList(it) } ?: arrayListOf())
    }

    private fun filterCandles(dataPoints: List<Candle>?, maxElement: Double?, minElement: Double?, divider: Int): List<Candle>? {
        return dataPoints?.mapIndexedNotNull { index, element ->
            if(index % divider == 0 || element.high == maxElement || element.low == minElement) element else null
        }
    }

    suspend fun connectSignalR(onMessage: (CGSimplePriceUpdate) -> Unit): Boolean = signalRMutex.withLock {
        connectSignalRLocked(onMessage)
    }

    private suspend fun connectSignalRLocked(onMessage: (CGSimplePriceUpdate) -> Unit): Boolean {
        onSignalRMessage = onMessage

        if (hubConnection?.connectionState == HubConnectionState.CONNECTED) {
            return true
        }

        val token = withContext(Dispatchers.IO) { FirebaseManager.getFirebaseToken() }
        if (token.isEmpty()) {
            Log.w("SignalR", "Connection failed: could not generate auth token")
            return false
        }

        return withContext(Dispatchers.IO) {
            try {
                val connection = HubConnectionBuilder.create("$serverBaseUrl/premium/connect")
                    .withAccessTokenProvider(Single.defer { Single.just(FirebaseManager.getFirebaseToken()) })
                    .withTransport(TransportEnum.LONG_POLLING)
                    .build()

                connection.on("weiner", { message: String ->
                    processSignalRMessage(message)
                }, String::class.java)

                connection.onClosed {
                    signalRScope.launch {
                        handleSignalRClosed()
                    }
                }

                connection.start().blockingAwait()
                hubConnection = connection
                true
            } catch (error: Exception) {
                Log.w("SignalR", "Connection failed: ${error.message}")
                hubConnection = null
                false
            }
        }
    }

    suspend fun getCurrency(coinId: String): Boolean {
        return getCurrencies(listOf(coinId))
    }

    suspend fun getCurrencies(coinIds: List<String>): Boolean {
        subscribedCoinIds = coinIds
            .map { it.lowercase() }
            .filter { it.isNotEmpty() }

        if (subscribedCoinIds.isEmpty()) {
            return false
        }

        val selectedCurrency = UserPreferences.getSelectedCurrency(null).code.lowercase()
        val result = MainRepository.subCoin(
            coinIds = subscribedCoinIds,
            currencies = listOf(selectedCurrency),
        )

        return result.isSuccess
    }

    suspend fun disconnectSignalR() {
        signalRMutex.withLock {
            tearDownSignalRConnectionLocked(clearSubscription = true)
        }
    }

    private suspend fun tearDownSignalRConnectionLocked(clearSubscription: Boolean) {
        signalRReconnectJob?.cancel()
        signalRReconnectJob = null

        withContext(Dispatchers.IO) {
            try {
                hubConnection?.stop()?.blockingAwait()
            } catch (_: Exception) {
            }
        }
        hubConnection = null

        if (clearSubscription) {
            onSignalRMessage = null
            subscribedCoinIds = emptyList()
            isSignalRReconnecting = false
        }
    }

    private fun scheduleSignalRReconnect() {
        if (subscribedCoinIds.isEmpty() || onSignalRMessage == null) {
            return
        }

        signalRReconnectJob?.cancel()
        signalRReconnectJob = signalRScope.launch {
            delay(2_000)
            reconnectSignalRAfterFailure()
        }
    }

    private suspend fun reconnectSignalRAfterFailure() {
        if (isSignalRReconnecting) return
        val callback = onSignalRMessage ?: return
        if (subscribedCoinIds.isEmpty()) return

        isSignalRReconnecting = true
        try {
            Log.w("SignalR", "Long poll failed, reconnecting...")
            signalRMutex.withLock {
                tearDownSignalRConnectionLocked(clearSubscription = false)
                val connected = connectSignalRLocked(callback)
                if (connected) {
                    getCurrencies(subscribedCoinIds)
                }
            }
        } finally {
            isSignalRReconnecting = false
            signalRReconnectJob = null
        }
    }

    private suspend fun handleSignalRClosed() {
        if (subscribedCoinIds.isEmpty() || onSignalRMessage == null || isSignalRReconnecting) {
            return
        }
        scheduleSignalRReconnect()
    }

    private fun processSignalRMessage(message: String) {
        val update = try {
            gson.fromJson(message, CGSimplePriceUpdate::class.java)
        } catch (_: Exception) {
            null
        } ?: return

        val coinId = update.coinId?.lowercase() ?: return
        val existing = cryptoDetail?.get(coinId) ?: CryptoDetailInfoModel(id = coinId)
        val updated = update.apply(existing)

        cryptoDetail = (cryptoDetail ?: mutableMapOf()).also { it[coinId] = updated }
        if (cryptoInfo?.id?.lowercase() == coinId) {
            cryptoInfo = updated
        }
        if (coinId == "bitcoin") {
            bitcoinData = updated
        }

        onSignalRMessage?.invoke(update)
    }
}
