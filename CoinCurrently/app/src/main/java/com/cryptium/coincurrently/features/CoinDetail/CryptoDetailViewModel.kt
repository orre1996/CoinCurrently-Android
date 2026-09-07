@file:Suppress("BooleanMethodIsAlwaysInverted", "BooleanMethodIsAlwaysInverted",
    "BooleanMethodIsAlwaysInverted", "BooleanMethodIsAlwaysInverted",
    "BooleanMethodIsAlwaysInverted", "BooleanMethodIsAlwaysInverted",
    "BooleanMethodIsAlwaysInverted", "BooleanMethodIsAlwaysInverted",
    "BooleanMethodIsAlwaysInverted", "BooleanMethodIsAlwaysInverted",
    "BooleanMethodIsAlwaysInverted"
)

package com.cryptium.coincurrently.features.CoinDetail
import com.cryptium.coincurrently.core.persistence.UserPreferences
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import androidx.compose.ui.unit.min
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import com.cryptium.coincurrently.app.MainActivity
import com.cryptium.coincurrently.sharedui.components.CustomImageView
import com.cryptium.coincurrently.core.extensions.DateFormat
import com.cryptium.coincurrently.core.extensions.convertStringToDate
import com.cryptium.coincurrently.core.extensions.formatCurrency
import com.cryptium.coincurrently.core.extensions.getActivity
import com.cryptium.coincurrently.core.extensions.getDifferenceInPercent
import com.cryptium.coincurrently.core.extensions.groupedTransactions
import com.cryptium.coincurrently.features.Portfolio.Models.PortfolioSwapTransaction
import com.cryptium.coincurrently.sharedui.models.SortOption
import com.cryptium.coincurrently.features.Market.Toplist.Repositories.ToplistRepository
import com.cryptium.coincurrently.features.News.NewsRepository
import com.cryptium.coincurrently.features.Portfolio.PortfolioRepository
import com.cryptium.coincurrently.features.PriceAlerts.PriceAlertsRepository
import com.cryptium.coincurrently.core.purchases.BillingManager
import com.cryptium.coincurrently.core.extensions.pieContaining
import com.cryptium.coincurrently.sharedui.charts.HoverHelper
import com.cryptium.coincurrently.sharedui.components.renderComposableToBitmap
import com.cryptium.coincurrently.sharedui.components.saveBitmapToCache
import com.cryptium.coincurrently.sharedui.components.shareBitmap
import com.cryptium.coincurrently.core.networking.ResponseModels.GlobalMarketData
import com.cryptium.coincurrently.core.networking.ResponseModels.GlobalMarketModel
import com.cryptium.coincurrently.features.GlobalMarket.GlobalMarketRepository
import com.cryptium.coincurrently.core.analytics.ChartCandleModeChanged
import com.cryptium.coincurrently.core.analytics.FavoriteAdded
import com.cryptium.coincurrently.core.analytics.FavoriteRemoved
import com.cryptium.coincurrently.core.analytics.AnalyticsManager
import com.cryptium.coincurrently.core.analytics.ScreenLabel
import com.cryptium.coincurrently.core.persistence.StorageKeys
import com.cryptium.coincurrently.core.persistence.StorageManager
import com.cryptium.coincurrently.sharedui.models.CachedData
import com.cryptium.coincurrently.features.CoinDetail.Views.About.CryptoDetailAboutShareChart
import com.cryptium.coincurrently.core.models.ChartTypes
import com.cryptium.coincurrently.features.Portfolio.DataClasses.PortfolioEvent
import com.cryptium.coincurrently.features.Portfolio.DataClasses.PortfolioModel
import com.cryptium.coincurrently.features.Portfolio.Enums.DateOption
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import com.cryptium.coincurrently.core.networking.NewsCategory
import com.cryptium.coincurrently.core.networking.ResponseModels.*
import java.util.*
import kotlin.math.min
import com.cryptium.coincurrently.core.persistence.CurrencyRepository
import com.cryptium.coincurrently.features.Paywall.getCurrency
import com.cryptium.coincurrently.features.PriceAlerts.PriceAlertCreateResult

class CryptoDetailViewModelFactory(
    val cryptoDetail: CryptoDetailInfoModel?,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CryptoDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CryptoDetailViewModel(cryptoDetail) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

@SuppressLint("MutableCollectionMutableState")
class CryptoDetailViewModel(
    cryptoDetail: CryptoDetailInfoModel?,
): ViewModel() {
    private val toplistRepository by lazy { ToplistRepository }
    private val currencyRepository by lazy { CurrencyRepository }
    private val newsRepository by lazy { NewsRepository }
    private val portfolioRepository by lazy { PortfolioRepository }
    private val priceAlertsRepository by lazy { PriceAlertsRepository }
    private val globalMarketRepository by lazy { GlobalMarketRepository }

    var globalMarketData: GlobalMarketData? = null

    // MainCoin
    private var currencyId: String
    var currentPage by mutableStateOf(0)

    // Main coin
    var cryptoInfo: CryptoDetailInfoModel? by mutableStateOf(null)

    var cryptoDetailSmallImageBitmap: Bitmap? by mutableStateOf(null)

    var priceVolumeModel: PriceVolumeModel? by mutableStateOf(null)
    var prices: List<Double> by mutableStateOf(listOf())
    var onlyPrices: List<Double> by mutableStateOf(listOf())
    var volumes: List<Double> by mutableStateOf(listOf())
    var timeStamps: List<Double> by mutableStateOf(listOf())
    var mainReloading: Boolean by mutableStateOf(true)
    var inFavoritesList: Boolean by mutableStateOf(false)

   var dragPosition = mutableStateOf(0.dp)
   var chartSize = mutableStateOf(DpSize.Zero)
   val hoverPriceTextWidth = mutableStateOf(0.dp)
   val hoverCandlePriceTextWidth = mutableStateOf(0.dp)
   val hoverHelper = HoverHelper()

    // Candle sticks
    var candleSticks: List<Candle> by mutableStateOf(listOf())

    // Price alerts
    var alerts: List<AlertCoin>? by mutableStateOf(null)

    // News
    var newsData: List<NewNewsItem>? by mutableStateOf(null)
    var showNewsError: Boolean by mutableStateOf(false)

    // Portfolio
    var pie: Pie? by mutableStateOf(null)
    var portfolio: List<Pie>? by mutableStateOf(null)
    var portfolioAllCurrencies: MutableMap<String, Map<String, Double>>? by mutableStateOf(null)

    var hidePortfolioData: Boolean by mutableStateOf(false)
    var groupedTransactions: Map<Date, List<PortfolioEvent>>? by mutableStateOf(null)

    // General
    var isRefreshing: Boolean by mutableStateOf(false)
    var currentChartType = mutableStateOf(StorageManager.get<ChartTypes>(StorageKeys.SELECTED_CHART_TYPE) ?: ChartTypes.PRICE)
    var candleChartEnabled by mutableStateOf(StorageManager.get<Boolean>(StorageKeys.IS_CANDLE_CHART_ENABLED) ?: false)
    var currentDays by mutableStateOf(
        TimeCharts.getStartTimeFrameBetween(TimeCharts.anHour, TimeCharts.max).days,
    )
    var selectedTimeFrame by mutableStateOf(
        TimeCharts.getStartTimeFrameBetween(TimeCharts.anHour, TimeCharts.max),
    )
    var widgetCurrencies: List<Toplist> by mutableStateOf(listOf())
    var isDragging by mutableStateOf(false)

    var selectedSortOption by mutableStateOf(SortOption.VolumeDesc)

    private var hasProSubscription = BillingManager.currentSubscription.value != null

    val isWebSocketEnabled: Boolean
        get() = UserPreferences.isWebSocketEnabled(hasPro = hasProSubscription)

    init {
        this.currencyId = cryptoDetail?.id ?: ""

        currencyRepository.loadCachedDataForCoin(currencyId, selectedTimeFrame)

        currencyRepository.widgetCurrencies.let {
            widgetCurrencies = it
        }

        currencyRepository.cryptoDetail?.get(currencyId)?.let {
            cryptoInfo = it
        } ?: run {
            this.cryptoInfo = cryptoDetail
        }

        inFavoritesList = UserPreferences.getFavorites().orEmpty().any { it == (cryptoInfo?.id ?: currencyId) }

        currencyRepository.cryptoPrices?.get("$currencyId-$currentDays")?.let { cached ->
            priceVolumeModel = cached
            mainReloading = isCandleChartEnabled
        }

        currencyRepository.cryptoCandles?.get("$currencyId-$currentDays")?.let {
            candleSticks = it
        }

        globalMarketRepository.globalMarket?.let {
            globalMarketData = it
        }

        if (isCandleChartEnabled && candleSticks.isNotEmpty()) {
            setCandleSticks()
            mainReloading = false
        }

        priceVolumeModel?.let {
            setPrices()
            setOnlyPrices()
            setVolumes()
            setTimestamps()
        }

        newsRepository.cachedNews?.get(NewsCategory.Coin(cryptoInfo?.id ?: "").type)?.let {
            newsData = it.data.data
        }

        selectedSortOption = StorageManager.get<SortOption>(StorageKeys.CRYPTO_DETAIL_MARKETS_SORT_OPTION) ?: SortOption.VolumeDesc
    }

    suspend fun getGlobalMarketData() {
        globalMarketRepository.getGlobalMarket()?.let {
            globalMarketData = it
        }
    }

    val allPriceAlertsCount: Int get() {
        return priceAlertsRepository.priceAlerts?.count() ?: 0
    }

    val portfolioModels: List<PortfolioModel>? get() {
        return portfolioRepository.portfolioModels
    }

    val isCandleChartEnabled: Boolean get() {
        return candleChartEnabled
    }

    val changePercent: Double? get() {
        if (isCandleChartEnabled) {
            val first = candleSticks.firstOrNull()?.open ?: return null
            val last = candleSticks.lastOrNull()?.close ?: return null

            return getDifferenceInPercent(last, first)
        } else {
            val first = prices.firstOrNull() ?: return null
            val last = prices.lastOrNull() ?: return null

            return getDifferenceInPercent(last, first)
        }
    }

    val mainGraphStats: List<String?> get() {
        return (0 until 6).map { if(isCandleChartEnabled) getChartCandlePrice(it) else getPriceInChart(prices, it) }
    }

    val candlePrices: List<Double> get() {
        if (candleSticks.isEmpty()) return emptyList()
        val first = candleSticks.firstOrNull()?.open ?: return emptyList()
        val last = candleSticks.lastOrNull()?.close ?: return emptyList()
        val highs = candleSticks.mapNotNull({ it.high })
        val lows = candleSticks.mapNotNull({ it.low })
        val max = highs.maxOrNull() ?: 0.0
        val min = lows.minOrNull() ?: 0.0
        return listOf(first, max, min, last)
    }

    val highestCandleValue: Double? get() {
        return candleSticks.mapNotNull({ it.high }).maxOrNull()
    }

    val lowestCandleValue: Double? get() {
        return candleSticks.mapNotNull({ it.low }).minOrNull()
    }

    val candleTimestamps: List<Double> get() {
        return candleSticks.mapNotNull({ it.timestamp })
    }

    val candleMiddlePrices: List<Double> get() {
        val prices: ArrayList<Double> = arrayListOf()

        candleSticks.forEach({
            val high = it.high ?: 0.0
            val low = it.low ?: 0.0
            prices.add((low + high) / 2)
        })

        return prices
    }

    suspend fun getCryptoNews(forceSymbol: String? = null) {
        val id: String = forceSymbol?.uppercase() ?: cryptoInfo?.symbol?.uppercase() ?: return

        showNewsError = false

        val news = newsRepository.getNewNews(25, NewsCategory.Coin(id))
        if (news?.data.isNullOrEmpty()) {
            showNewsError = true
        }

        news?.let {
            newsData = it.data.orEmpty()
        } ?: run {
            showNewsError = true
        }
    }

    suspend fun refresh(
        context: Context,
        showReload: Boolean = true,
        showRefreshIndicator: Boolean = true,
        newDays: String? = null,
        newTimeChart: TimeCharts? = null,
        forceId: String? = null,
        getNews: Boolean = true,
        forceSymbol: String? = null
    ) {
        val appContext = context.applicationContext
        coroutineScope {
            isRefreshing = showRefreshIndicator
            mainReloading = showReload

            hasProSubscription = BillingManager.currentSubscription.value != null

            val days = newDays ?: currentDays
            val chart = newTimeChart ?: selectedTimeFrame
            val mainId = forceId ?: currencyId

            val cryptoInfo = async { getCryptoInfo(appContext, mainId) }
            val news = async { if (getNews) getCryptoNews(forceSymbol) }
            val candleChart = async {
                if (isCandleChartEnabled) {
                    getCandleChart(mainId, days)
                }
            }
            val priceVolumeModel = async {
                if (!isCandleChartEnabled || !showReload) {
                    getMainCurrencyPrices(mainId, chart)
                }
            }

            cryptoInfo.await()
            news.await()
            candleChart.await()
            priceVolumeModel.await()

            currentDays = days
            selectedTimeFrame = chart

            setPrices()
            setOnlyPrices()
            setVolumes()
            setTimestamps()

            if (isCandleChartEnabled) {
                setCandleSticks()
            }

            mainReloading = false
            isRefreshing = false

            if (!mainId.isNullOrEmpty()) {
                startWebSocket(mainId)
            }
        }
    }

    suspend fun stopLivePriceUpdates() {
        stopWebSocket()
    }

    private suspend fun startWebSocket(coinId: String) {
        if (!isWebSocketEnabled || coinId.isEmpty()) {
            stopWebSocket()
            return
        }

        val connected = currencyRepository.connectSignalR { update ->
            viewModelScope.launch(Dispatchers.Main.immediate) {
                applyWebSocketUpdate(update)
            }
        }

        if (connected) {
            currencyRepository.getCurrency(coinId)
        }
    }

    private suspend fun stopWebSocket() {
        currencyRepository.disconnectSignalR()
    }

    private fun applyWebSocketUpdate(update: CGSimplePriceUpdate) {
        val coinId = cryptoInfo?.id ?: return
        if (update.coinId?.lowercase() != coinId.lowercase()) {
            return
        }

        val existing = cryptoInfo ?: CryptoDetailInfoModel(id = coinId)
        cryptoInfo = update.apply(existing)

        if (candleChartEnabled || currentChartType.value != ChartTypes.PRICE) {
            return
        }

        val livePrice = update.price ?: return
        if (prices.isEmpty()) {
            return
        }

        val selectedCurrency = UserPreferences.getSelectedCurrency(null).code.lowercase()
        if (update.vsCurrency?.lowercase() != selectedCurrency) {
            return
        }

        prices = prices.toMutableList().also { it[it.lastIndex] = livePrice }
        if (onlyPrices.isNotEmpty()) {
            onlyPrices = onlyPrices.toMutableList().also { it[it.lastIndex] = livePrice }
        }
    }

    fun addCurrencyToWidgets(context: Context) {
        val toplistItem = cryptoInfo?.toToplist() ?: return
        val previousCount = widgetCurrencies.size.toDouble()

        widgetCurrencies = widgetCurrencies + toplistItem
        currencyRepository.changeWidgetsOrder(widgetCurrencies, context)

        com.cryptium.coincurrently.features.Achievements.Managers.AchievementManager.checkMetricUnlock(
            metric = com.cryptium.coincurrently.features.Achievements.Models.AchievementMetric.widgetCoinsCount,
            previousValue = previousCount,
            newValue = widgetCurrencies.size.toDouble(),
        )
    }

    fun replaceWidgetCurrency(coin: Toplist, with: Toplist? = null, context: Context) {
        val newItem = with ?: cryptoInfo?.toToplist() ?: return
        val index = widgetCurrencies.indexOfFirst { it.id == coin.id }

        if (index !in widgetCurrencies.indices) return

        val updatedList = widgetCurrencies.toMutableList().apply {
            this[index] = newItem
        }

        widgetCurrencies = updatedList

        currencyRepository.changeWidgetsOrder(updatedList, context)
    }

    suspend fun toggleChart() {
        mainReloading = true
        candleChartEnabled = !candleChartEnabled
        com.cryptium.coincurrently.features.Achievements.Managers.AchievementManager.increment(
            com.cryptium.coincurrently.features.Achievements.Models.AchievementCounterKey.candleChartToggles
        )
        AnalyticsManager.log(
            ChartCandleModeChanged(
                fromScreen = ScreenLabel.CryptoDetail,
                isCandleChart = candleChartEnabled,
            ),
        )
        StorageManager.set(StorageKeys.IS_CANDLE_CHART_ENABLED, candleChartEnabled)

        if (candleChartEnabled) {
            getCandleChart(cryptoInfo?.id ?: "", currentDays)
        } else {
            getMainCurrencyPrices(cryptoInfo?.id ?: "", selectedTimeFrame)
        }

        setPrices()
        setOnlyPrices()
        setVolumes()
        setTimestamps()

        if (isCandleChartEnabled) {
            setCandleSticks()
        }

        mainReloading = false
    }

    private suspend fun getCryptoInfo(context: Context, id: String?) {
        currencyRepository.getCryptoDetail(id)?.let { info ->
            cryptoInfo = info
            currencyId = info.id ?: ""
            inFavoritesList = UserPreferences.getFavorites().orEmpty().any { coin -> coin == info.id }
            cryptoDetailSmallImageBitmap = fetchCryptoSmallIcon(context, info.image?.small)
        }
    }

    /** Bitmap is copied inside [CustomImageView.setImageWithCompletion] before Glide recycles pooled data. */
    private suspend fun fetchCryptoSmallIcon(context: Context, url: String?): Bitmap? {
        if (url.isNullOrBlank()) return null
        val appContext = context.applicationContext
        return suspendCancellableCoroutine { continuation ->
            val imageView = CustomImageView(appContext)
            val mainHandler = Handler(Looper.getMainLooper())
            imageView.setImageWithCompletion(url, context) { _, detachedCopy ->
                mainHandler.post {
                    if (continuation.isActive) {
                        continuation.resume(detachedCopy)
                    } else {
                        detachedCopy?.takeUnless { it.isRecycled }?.recycle()
                    }
                }
            }
        }
    }

    private suspend fun getMainCurrencyPrices(id: String?, chart: TimeCharts) {
        val priceVolumeModel = currencyRepository.getPriceVolume(id, chart)
        this.priceVolumeModel = currencyRepository.filterPriceVolume(priceVolumeModel, (priceVolumeModel?.prices?.size ?: 0) / 200)
    }

    suspend fun newMainCoin(context: Context, id: String) {
        coroutineScope {
            stopWebSocket()
            mainReloading = true
            cryptoInfo = null
            cryptoDetailSmallImageBitmap = null
            priceVolumeModel = null

            setPrices()
            setOnlyPrices()
            setVolumes()
            setTimestamps()

            val cryptoInfo = async { getCryptoInfo(context, id) }
            val candleChart = async { if (isCandleChartEnabled) { getCandleChart(id, currentDays) } }
            val priceVolumeModel = async { getMainCurrencyPrices(id, selectedTimeFrame) }

            cryptoInfo.await()
            candleChart.await()
            priceVolumeModel.await()

            setPrices()
            setOnlyPrices()
            setVolumes()
            setTimestamps()

            mainReloading = false
            startWebSocket(id)
        }
    }

    fun favoriteButtonPressed(
        context: Context,
        callback: () -> Unit
    ) {
        val toplistItem = cryptoInfo?.toToplist() ?: return

        if (BillingManager.currentSubscription.value == null && !inFavoritesList && UserPreferences.getFavorites().orEmpty().size >= 2) {
            callback.invoke()
            return
        }

        val wasFavorite = inFavoritesList
        if (wasFavorite) {
            AnalyticsManager.log(
                FavoriteRemoved(
                    coinName = toplistItem.name ?: "",
                    fromScreen = ScreenLabel.CryptoDetail,
                ),
            )
        } else {
            AnalyticsManager.log(
                FavoriteAdded(
                    coinName = toplistItem.name ?: "",
                    fromScreen = ScreenLabel.CryptoDetail,
                ),
            )
        }

        inFavoritesList = !inFavoritesList

        toplistRepository.favouriteButtonPressed(toplistItem)
        MainActivity.showAppReview(context)
    }

    fun setChartType(type: ChartTypes) {
        currentChartType.value = type
        StorageManager.set(StorageKeys.SELECTED_CHART_TYPE, currentChartType.value)

        setPrices()
    }

    private fun getPriceInChart(prices: List<Double>, at: Int): String? {
        val min = prices.minOfOrNull { it } ?: return null
        val max = prices.maxOfOrNull { it } ?: return null
        val priceSteps = (max - min) / 5
        val value = min + (priceSteps * at.toDouble())

        return value.formatCurrency(currency = CurrencySelectionModel.empty())
    }

    // Candle chart

    private suspend fun getCandleChart(id: String?, days: String) {
        currencyRepository.getCandleStick(id, days)?.let {
            candleSticks = it
        }
    }

    private fun getChartCandlePrice(at: Int): String? {
        val min = lowestCandleValue ?: return null
        val max = highestCandleValue ?: return null
        val priceSteps = (max - min) / 5
        val value = min + (priceSteps * at.toDouble())
        
        return value.formatCurrency(currency = CurrencySelectionModel.empty())
    }

    // Price alerts

    fun checkIfUserEnabledPush(context: Context): Boolean {
        return NotificationManagerCompat.from(context).areNotificationsEnabled()
    }

    suspend fun addPriceAlert(alertCoin: PostAlertCoin): PriceAlertCreateResult {
        return when (val result = priceAlertsRepository.createAlert(alertCoin)) {
            PriceAlertCreateResult.Success -> {
                alerts = priceAlertsRepository.priceAlerts
                    ?.filter { it.coinId == cryptoInfo?.id?.lowercase() }
                    ?.sortedByDescending { it.createdDate.convertStringToDate(DateFormat.yyyyMMddTHHmmssSSSZ) }
                PriceAlertCreateResult.Success
            }
            else -> result
        }
    }

    suspend fun getPriceAlerts() {
        priceAlertsRepository.getAlerts()?.let {
            alerts = it
                .filter { it.coinId == cryptoInfo?.id?.lowercase() }
                .sortedByDescending { it.createdDate.convertStringToDate(DateFormat.yyyyMMddTHHmmssSSSZ) }
        } ?: run {
            alerts = listOf()
        }
    }

    suspend fun deleteAlert(alert: AlertCoin) {
        alerts = alerts?.filter { it.id != alert.id }

        priceAlertsRepository.deleteAlert(alert.id)?.let {
            alerts = it
                .filter { it.coinId == cryptoInfo?.id?.lowercase() }
                .sortedByDescending { it.createdDate.convertStringToDate(DateFormat.yyyyMMddTHHmmssSSSZ) }
        }
    }

    // Portfolio

    suspend fun getPortfolio() {
        viewModelScope.launch {
            val portfolioDeferred = async {
                portfolioRepository.getPortfolio()
            }
            val currenciesDeferred = async {
                portfolioRepository.getPortfolioAllCurrencies()
            }

            awaitAll(portfolioDeferred, currenciesDeferred)

            portfolio = portfolioDeferred.await()?.data?.data
            portfolioAllCurrencies = currenciesDeferred.await()
            pie = portfolio?.firstOrNull { it.data?.id == cryptoInfo?.id }
            setGroupTransactions()
        }
    }

    suspend fun findTransaction(transactionId: String): Pair<PortfolioEvent?, Toplist?> {
        return portfolioRepository.findPortfolioEventByTransactionId(transactionId)
    }

    fun setGroupTransactions() {
        if (portfolio == null) {
            groupedTransactions = mapOf()
            return
        }
        
        val coinSlices: List<Toplist> = portfolio
            ?.filter { it.data?.id == cryptoInfo?.id }
            ?.mapNotNull { it.data } ?: listOf()
        val result: MutableMap<Date, MutableList<PortfolioEvent>> =
            if (coinSlices.isEmpty()) mutableMapOf() else portfolio
                ?.groupedTransactions(
                    types = listOf(),
                    dateOption = DateOption.All,
                    coinNames = coinSlices
                )
                ?.mapValues { (_, v) -> v.toMutableList() }
                ?.toMutableMap() ?: mutableMapOf()
        val targetId: String? = cryptoInfo?.id
        if (targetId != null) {
            val calendar: Calendar = Calendar.getInstance()
            val seenIds: MutableSet<UUID> = result.values
                .flatMap { ev -> ev.map { it.id } }
                .toMutableSet()
            val fromSwaps: List<PortfolioSwapTransaction> = portfolio
                ?.onEach { it.sanitizeGsonNullLists() }
                ?.flatMap { it.swaps }
                ?.filter { it.fromCurrencyId == targetId } ?: listOf()
            for (swap in fromSwaps) {
                if (seenIds.contains(swap.id)) {
                    continue
                }
                seenIds.add(swap.id)
                calendar.time = swap.date
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val day: Date = calendar.time
                result.getOrPut(day) { mutableListOf() }.add(swap)
            }
            result.values.forEach { it.sortByDescending { it.date } }
        }
        groupedTransactions = result
    }

    fun deleteTransaction(transaction: PortfolioEvent) {
        viewModelScope.launch {
            val id = portfolio?.pieContaining(transaction)?.data?.id ?: ""

            portfolio = portfolioRepository.deleteTransaction(
                id = id,
                uuid = transaction.id
            )?.data?.data

            pie = portfolio?.firstOrNull { it.data?.id == cryptoInfo?.id }

            setGroupTransactions()
        }
    }

    fun toggleHideData() {
        hidePortfolioData = !hidePortfolioData
        UserPreferences.setHidePortfolioData(hidePortfolioData)
    }

    // MARK: - Private main functions

    private fun setOnlyPrices() {
        val prefix = getPricesRangePrefix(priceVolumeModel)

        onlyPrices = priceVolumeModel?.prices?.takeLast(prefix)?.map { it.lastOrNull() ?: 0.0 }?.filter({ it != 0.0 }) ?: listOf()
    }

    private fun setPrices() {
        val prices: List<Double>
        val prefix = getPricesRangePrefix(priceVolumeModel)

        if (currentChartType.value == ChartTypes.PRICE || candleChartEnabled) {
            prices = priceVolumeModel?.prices?.takeLast(prefix)?.map({ it.lastOrNull() ?: 0.0 }) ?: arrayListOf()
        } else if (currentChartType.value == ChartTypes.VOLUME) {
            prices = priceVolumeModel?.total_volumes?.takeLast(prefix)?.map({ it.lastOrNull() ?: 0.0 }) ?: arrayListOf()
        } else {
            prices = priceVolumeModel?.market_caps?.takeLast(prefix)?.map({ it.lastOrNull() ?: 0.0 }) ?: arrayListOf()
        }

        this.prices = prices.map { it }.filter({ it != 0.0 })
    }

    private fun setTimestamps() {
        val timeStamps: List<Double>
        val prefix = getPricesRangePrefix(priceVolumeModel)

        if (currentChartType.value == ChartTypes.PRICE || candleChartEnabled) {
            timeStamps = priceVolumeModel?.prices?.takeLast(prefix)?.map({ it.firstOrNull() ?: 0.0 }) ?: arrayListOf()
        } else if (currentChartType.value == ChartTypes.VOLUME) {
            timeStamps = priceVolumeModel?.total_volumes?.takeLast(prefix)?.map({ it.firstOrNull() ?: 0.0 }) ?: arrayListOf()
        } else {
            timeStamps = priceVolumeModel?.market_caps?.takeLast(prefix)?.map({ it.firstOrNull() ?: 0.0 }) ?: arrayListOf()
        }

        this.timeStamps = timeStamps.map { it }.filter({ it != 0.0 })
    }

    private fun setVolumes() {
        val prefix = getPricesRangePrefix(priceVolumeModel)

        volumes = priceVolumeModel?.total_volumes?.takeLast(prefix)?.map({ (it.lastOrNull() ?: 0.0) })?.filter({ it != 0.0 }) ?: listOf()
    }

    // Private candle functions

    private fun setCandleSticks() {
        val prefix = when (selectedTimeFrame) {
            TimeCharts.anHour -> { (candleSticks.size / 24) }
            else -> candleSticks.size
        }

        this.candleSticks = candleSticks.takeLast(prefix)
    }

    private fun getPricesRangePrefix(priceVolume: PriceVolumeModel?): Int {
        return when (selectedTimeFrame) {
            TimeCharts.anHour -> { ((priceVolume?.prices?.size ?: 0) / 24) }
            else -> (priceVolume?.prices?.size ?: 0)
        }
    }

    fun updateHoverStats(touchXs: List<Dp>, is24HourClock: Boolean) {
        if (mainReloading) return

        if ((timeStamps.isEmpty() && candleTimestamps.isEmpty()) || touchXs.isEmpty()) {
            isDragging = false
            return
        }

        isDragging = true

        val chartWidth = chartSize.value.width
        val adjustedTouchXs = if (isCandleChartEnabled) {
            listOf(touchXs.firstOrNull() ?: 0.dp)
        } else {
            touchXs
        }.map { touchX ->
            max(0.dp, min(touchX, chartWidth - 0.001.dp))
        }

        dragPosition.value = adjustedTouchXs.firstOrNull() ?: 0.dp

        if (isCandleChartEnabled) {
            hoverHelper.getHoverPriceCandles(
                chartWidth = chartWidth,
                touchX = adjustedTouchXs.firstOrNull() ?: 0.dp,
                candles = candleSticks,
                timeChart = selectedTimeFrame,
                is24HourClock = is24HourClock,
                isDragging = isDragging,
            )
        } else {
            hoverHelper.getHoverPrice(
                chartWidth = chartWidth,
                touchXs = adjustedTouchXs,
                prices = prices,
                dates = timeStamps,
                timeChart = selectedTimeFrame,
                is24HourClock = is24HourClock,
                isDragging = isDragging,
            )
        }
    }

    // Private portfolio functions

    fun testShare(context: Context) {
        val activity = context.getActivity() ?: return

        renderComposableToBitmap(
            activity = activity,
            composable = {
                CryptoDetailAboutShareChart(this)
            },
            onBitmapReady = { bitmap ->
                saveBitmapToCache(context, bitmap, cryptoInfo?.name ?: "coin")
                val sym = cryptoInfo?.symbol?.trim()?.uppercase()
                val label = if (!sym.isNullOrEmpty()) "\$$sym" else "\$COIN"
                shareBitmap(context, bitmap, cryptoInfo?.name ?: "coin", trackedShareLabel = label)
            }
        )
    }

    fun setNewSortOption(newSortOption: SortOption) {
        selectedSortOption = newSortOption
        StorageManager.set(StorageKeys.CRYPTO_DETAIL_MARKETS_SORT_OPTION, newSortOption)
    }
}
