package com.cryptium.coincurrently.features.CoinDetail
import com.cryptium.coincurrently.core.persistence.UserPreferences
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.cryptium.coincurrently.core.extensions.getDifferenceInDecimal
import com.cryptium.coincurrently.core.extensions.getDifferenceInPercent
import com.cryptium.coincurrently.sharedui.models.PriceChange
import com.cryptium.coincurrently.sharedui.models.SortOption
import com.cryptium.coincurrently.core.persistence.StorageKeys
import com.cryptium.coincurrently.core.persistence.StorageManager
import com.cryptium.coincurrently.core.models.ChartTypes
import com.cryptium.coincurrently.core.persistence.CurrencyRepository
import com.cryptium.coincurrently.features.Market.Toplist.Repositories.ToplistRepository
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import com.cryptium.coincurrently.core.networking.ResponseModels.CGSimplePriceUpdate
import com.cryptium.coincurrently.core.networking.ResponseModels.CryptoDetailInfoModel
import com.cryptium.coincurrently.core.networking.ResponseModels.PriceVolumeModel
import com.cryptium.coincurrently.core.networking.ResponseModels.TimeCharts
import com.cryptium.coincurrently.core.networking.ResponseModels.Toplist
import kotlin.math.abs

class CompareCoinsViewModelFactory(
    val cryptoInfo: CryptoDetailInfoModel?,
    val priceVolumeModel: PriceVolumeModel?,
    val compareId: String,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CompareCoinsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CompareCoinsViewModel(
                cryptoInfo,
                priceVolumeModel,
                compareId,
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class CompareCoinsViewModel(
    cryptoInfo: CryptoDetailInfoModel?,
    priceVolumeModel: PriceVolumeModel?,
    compareId: String,
): ViewModel() {
    companion object {
        // Matches iOS `getPriceModelInternal(isFromCompare: true)` divider.
        private const val COMPARE_CHART_DIVIDER = 2
        // Matches iOS `Date.moreThanADay` (ms).
        private const val MORE_THAN_A_DAY_MS = 86_400_000.0
    }

    private val currencyRepository by lazy { CurrencyRepository }
    private val toplistRepository by lazy { ToplistRepository }

    // Main coin
    var mainCryptoInfo: CryptoDetailInfoModel? by mutableStateOf(cryptoInfo)
    var mainPriceVolumeModel: PriceVolumeModel? by mutableStateOf(priceVolumeModel)
    var prices: List<Double> by mutableStateOf(listOf())
    var onlyPrices: List<Double> by mutableStateOf(listOf())
    var volumes: List<Double> by mutableStateOf(listOf())
    var timeStamps: List<Double> by mutableStateOf(listOf())
    var mainReloading: Boolean by mutableStateOf(false)

    // Compare coin
    var compareCryptoInfo: CryptoDetailInfoModel? by mutableStateOf(CryptoDetailInfoModel(compareId))
    var comparePriceVolumeModel: PriceVolumeModel? by mutableStateOf(null)
    var comparePrices: List<Double> by mutableStateOf(listOf())
    var onlyComparePrices: List<Double> by mutableStateOf(listOf())
    var compareVolumes: List<Double> by mutableStateOf(listOf())
    var compareTimeStamps: List<Double> by mutableStateOf(listOf())
    var compareReloading: Boolean by mutableStateOf(false)

    // General

    var comparableCoins: List<Toplist>? by mutableStateOf(null)
    var currentChartType = mutableStateOf(StorageManager.get<ChartTypes>(StorageKeys.SELECTED_CHART_TYPE) ?: ChartTypes.PRICE)
    var isRefreshing: Boolean by mutableStateOf(false)
    var currentDays by mutableStateOf(
        TimeCharts.getStartTimeFrameBetween(TimeCharts.anHour, TimeCharts.max).days,
    )
    var selectedTimeFrame by mutableStateOf(
        TimeCharts.getStartTimeFrameBetween(TimeCharts.anHour, TimeCharts.max),
    )

    var isDragging by mutableStateOf(false)
    var dragPosition = mutableStateOf(0.dp)
    var chartSize = mutableStateOf(DpSize.Zero)
    val hoverPriceTextWidth = mutableStateOf(0.dp)

    val isWebSocketEnabled: Boolean
        get() = UserPreferences.isWebSocketEnabled()

    init {
        val coins: ArrayList<Toplist> = run {
            val cacheKey = "${SortOption.MktCapDesc.getOptionKey(PriceChange.SevenDays)}-${null}"
            if(toplistRepository.toplist?.get(cacheKey)?.data != null) {
                toplistRepository.toplist?.get(cacheKey)?.data ?: arrayListOf()
            } else {
                toplistRepository.smallToplist ?: arrayListOf()
            }
        }

        comparableCoins = coins.sortedBy { it.market_cap_rank ?: Int.MAX_VALUE }

        // Match iOS compare: always downsample with divider 2 (not the heavier Max divider).
        mainPriceVolumeModel = currencyRepository.filterPriceVolume(priceVolumeModel, COMPARE_CHART_DIVIDER)

        setPrices()
        setOnlyComparePrices()
        setTimestamps()
        setVolumes()

        currencyRepository.cryptoDetail?.get(compareId)?.let {
            compareCryptoInfo = it
        }

        currencyRepository.cryptoPrices?.get("$compareId-$currentDays")?.let {
            comparePriceVolumeModel = currencyRepository.filterPriceVolume(it, COMPARE_CHART_DIVIDER)
            setComparePrices()
            setOnlyComparePrices()
            setCompareTimestamps()
            setCompareVolumes()
        }

        addExtraElementsToTimeline()
    }

    suspend fun getToplist() {
        toplistRepository.getToplist(
            sortOption = SortOption.MktCapDesc,
            priceChange = PriceChange.SevenDays,
            platformId = null
        )?.let {
            comparableCoins = it
        }
    }

    fun setChartType(type: ChartTypes) {
        currentChartType.value = type
        StorageManager.set(StorageKeys.SELECTED_CHART_TYPE, currentChartType.value)

        addExtraElementsToTimeline()
    }

    fun getPrices(dropLastTwo: Boolean = false): List<Double> {
        return if (dropLastTwo && prices.size > 1) {
            prices.dropLast(2)
        } else {
            prices
        }
    }

    fun getTimestamps(dropLastTwo: Boolean = false): List<Double> {
        return if (dropLastTwo && prices.size > 1) {
            timeStamps.dropLast(2)
        } else {
            timeStamps
        }
    }

    fun getComparePrices(dropLastTwo: Boolean = false): List<Double> {
        return if (dropLastTwo && comparePrices.size > 1) {
            comparePrices.dropLast(2)
        } else {
            comparePrices
        }
    }

    fun getCompareTimestamps(dropLastTwo: Boolean = false): List<Double> {
        return if (dropLastTwo && compareTimeStamps.size > 1) {
            compareTimeStamps.map { it }.dropLast(2)
        } else {
            compareTimeStamps
        }
    }

    private suspend fun getCryptoInfo(id: String?) {
        currencyRepository.getCryptoDetail(id)?.let {
            mainCryptoInfo = it
        }
    }

    private suspend fun getMainCurrencyPrices(id: String?, chart: TimeCharts) {
        val priceVolumeModel = currencyRepository.getPriceVolume(id, chart)
        mainPriceVolumeModel = currencyRepository.filterPriceVolume(priceVolumeModel, COMPARE_CHART_DIVIDER)
    }

    private suspend fun getCompareCryptoInfo(id: String?) {
        currencyRepository.getCryptoDetail(id)?.let {
            compareCryptoInfo = it
        }
    }

    private suspend fun getComparePriceVolumeModel(id: String?, chart: TimeCharts) {
        val priceVolumeModel = currencyRepository.getPriceVolume(id, chart)
        comparePriceVolumeModel = currencyRepository.filterPriceVolume(priceVolumeModel, COMPARE_CHART_DIVIDER)
    }

    suspend fun refresh(
        showReload: Boolean = true,
        showRefreshIndicator: Boolean = true,
        newDays: String? = null,
        newTimeChart: TimeCharts? = null,
        forceMainId: String? = null,
        forceCompareId: String? = null
    ) {
        coroutineScope {
            isRefreshing = showRefreshIndicator
            mainReloading = showReload
            compareReloading = showReload

            val days = newDays ?: currentDays
            val chart = newTimeChart ?: selectedTimeFrame
            val mainId = forceMainId ?: mainCryptoInfo?.id
            val compareId = forceCompareId ?: compareCryptoInfo?.id

            val cryptoInfo = async { getCryptoInfo(mainId) }

            val priceVolumeModel = async {
                getMainCurrencyPrices(mainId, chart)
            }

            val compareCryptoInfo = async {
                getCompareCryptoInfo(compareId)
            }

            val comparePriceVolumeModel = async {
                getComparePriceVolumeModel(compareId, chart)
            }

            cryptoInfo.await()
            priceVolumeModel.await()
            compareCryptoInfo.await()
            comparePriceVolumeModel.await()

            currentDays = days
            selectedTimeFrame = chart

            setPrices()
            setOnlyPrices()
            setVolumes()
            setTimestamps()

            addExtraElementsToTimeline()

            mainReloading = false
            compareReloading = false
            isRefreshing = false

            startWebSocket(mainId = mainId, compareId = compareId)
        }
    }

    suspend fun stopLivePriceUpdates() {
        stopWebSocket()
    }

    suspend fun syncLivePriceUpdates() {
        startWebSocket(mainId = mainCryptoInfo?.id, compareId = compareCryptoInfo?.id)
    }

    private suspend fun startWebSocket(mainId: String?, compareId: String?) {
        val coinIds = listOfNotNull(mainId, compareId)
            .map { it.lowercase() }
            .filter { it.isNotEmpty() }

        if (!isWebSocketEnabled || coinIds.isEmpty()) {
            stopWebSocket()
            return
        }

        val connected = currencyRepository.connectSignalR { update ->
            viewModelScope.launch(Dispatchers.Main.immediate) {
                applyWebSocketUpdate(update)
            }
        }

        if (connected) {
            currencyRepository.getCurrencies(coinIds)
        }
    }

    private suspend fun stopWebSocket() {
        currencyRepository.disconnectSignalR()
    }

    private fun applyWebSocketUpdate(update: CGSimplePriceUpdate) {
        val updateCoinId = update.coinId?.lowercase() ?: return

        val selectedCurrency = UserPreferences.getSelectedCurrency(null).code.lowercase()
        if (update.vsCurrency?.lowercase() != selectedCurrency) {
            return
        }

        if (updateCoinId == mainCryptoInfo?.id?.lowercase()) {
            val existing = mainCryptoInfo ?: CryptoDetailInfoModel(id = updateCoinId)
            mainCryptoInfo = update.apply(existing)

            if (currentChartType.value == ChartTypes.PRICE) {
                update.price?.let { livePrice ->
                    updateLastLivePrice(
                        livePrice = livePrice,
                        onOnlyPricesUpdate = { onlyPrices = it },
                        onChartPricesUpdate = { prices = it },
                        currentOnlyPrices = onlyPrices,
                        currentChartPrices = prices,
                    )
                }
            }
        } else if (updateCoinId == compareCryptoInfo?.id?.lowercase()) {
            val existing = compareCryptoInfo ?: CryptoDetailInfoModel(id = updateCoinId)
            compareCryptoInfo = update.apply(existing)

            if (currentChartType.value == ChartTypes.PRICE) {
                update.price?.let { livePrice ->
                    updateLastLivePrice(
                        livePrice = livePrice,
                        onOnlyPricesUpdate = { onlyComparePrices = it },
                        onChartPricesUpdate = { comparePrices = it },
                        currentOnlyPrices = onlyComparePrices,
                        currentChartPrices = comparePrices,
                    )
                }
            }
        }
    }

    private fun updateLastLivePrice(
        livePrice: Double,
        currentOnlyPrices: List<Double>,
        currentChartPrices: List<Double>,
        onOnlyPricesUpdate: (List<Double>) -> Unit,
        onChartPricesUpdate: (List<Double>) -> Unit,
    ) {
        if (currentOnlyPrices.isEmpty()) {
            return
        }

        onOnlyPricesUpdate(currentOnlyPrices.toMutableList().also { it[it.lastIndex] = livePrice })

        if (currentChartPrices.isEmpty()) {
            return
        }

        val updatedChartPrices = currentChartPrices.toMutableList()
        if (updatedChartPrices.size > 2) {
            updatedChartPrices[updatedChartPrices.lastIndex - 2] = livePrice
        } else {
            updatedChartPrices[updatedChartPrices.lastIndex] = livePrice
        }
        onChartPricesUpdate(updatedChartPrices)
    }

    val graphStats: List<String?>
        get() {
            val mainPrices = getPrices(dropLastTwo = true).filter { it != -1.0 }
            val comparePrices = getComparePrices(dropLastTwo = true).filter { it != -1.0 }

            val firstMainPrice = mainPrices.firstOrNull()
            val maxMainPrice = mainPrices.maxOrNull()
            val minMainPrice = mainPrices.minOrNull()

            val firstComparePrice = comparePrices.firstOrNull()
            val maxComparePrice = comparePrices.maxOrNull()
            val minComparePrice = comparePrices.minOrNull()

            if (firstMainPrice == null || maxMainPrice == null || minMainPrice == null ||
                firstComparePrice == null || maxComparePrice == null || minComparePrice == null
            ) {
                return (0 until 6).map {
                    null
                }
            }

            val mainTopPercent = getDifferenceInPercent(maxMainPrice, firstMainPrice) ?: 0.0
            val mainBottomPercent = getDifferenceInPercent(minMainPrice, firstMainPrice) ?: 0.0

            val compareTopPercent = getDifferenceInPercent(maxComparePrice, firstComparePrice) ?: 0.0
            val compareBottomPercent = getDifferenceInPercent(minComparePrice, firstComparePrice) ?: 0.0

            val chartMax = maxOf(mainTopPercent, compareTopPercent)
            val chartMin = minOf(mainBottomPercent, compareBottomPercent)

            val step = (chartMax - chartMin) / 5.0

            return (0 until 6).map { i ->
                val change = chartMin + (step * i)
                "${"%.2f".format(change)}%"
            }
        }

    private fun addExtraElementsToTimeline() {
        if (mainPriceVolumeModel == null || comparePriceVolumeModel == null) {
            return
        }

        setPrices()
        setOnlyPrices()
        setVolumes()
        setTimestamps()

        setComparePrices()
        setOnlyComparePrices()
        setCompareVolumes()
        setCompareTimestamps()

        val firstMain = prices.firstOrNull() ?: 0.0
        val mainMax = prices.maxOrNull() ?: 0.0
        val mainMin = prices.minOrNull() ?: 0.0
        val firstTimestamp = timeStamps.firstOrNull() ?: 0.0

        val firstCompare = comparePrices.firstOrNull() ?: 0.0
        val compareMax = comparePrices.maxOrNull() ?: 0.0
        val compareMin = comparePrices.minOrNull() ?: 0.0
        val compareTimestamps = compareTimeStamps.firstOrNull() ?: 0.0

        val compareMaxPercent = getDifferenceInDecimal(compareMax, firstCompare) ?: 0.0
        val compareMinPercent = getDifferenceInDecimal(compareMin, firstCompare) ?: 0.0

        prices = prices + listOf(firstMain * compareMaxPercent, firstMain * compareMinPercent)

        // iOS compares Date intervals against `Date.moreThanADay` (1 day), not 1_000_000 ms (~16 min).
        // On Max, start dates often differ by years; using the wrong threshold mis-aligns phantom points.
        if (abs(compareTimestamps - firstTimestamp) > MORE_THAN_A_DAY_MS) {
            timeStamps = timeStamps + listOf(compareTimestamps, compareTimestamps)
        } else {
            timeStamps = timeStamps + listOf(firstTimestamp, firstTimestamp)
        }

        val mainMaxPercent = getDifferenceInDecimal(mainMax, firstMain) ?: 0.0
        val mainMinPercent = getDifferenceInDecimal(mainMin, firstMain) ?: 0.0

        comparePrices = comparePrices + listOf(firstCompare * mainMaxPercent, firstCompare * mainMinPercent)

        if (abs(firstTimestamp - compareTimestamps) > MORE_THAN_A_DAY_MS) {
            compareTimeStamps = compareTimeStamps + listOf(firstTimestamp, firstTimestamp)
        } else {
            compareTimeStamps = compareTimeStamps + listOf(compareTimestamps, compareTimestamps)
        }

        val delta = abs(prices.size - comparePrices.size)

        val tempPrices = prices.toMutableList()
        val tempTimeStamps = timeStamps.toMutableList()
        val tempVolumes = volumes.toMutableList()

        val tempComparePrices = comparePrices.toMutableList()
        val tempCompareTimeStamps = compareTimeStamps.toMutableList()
        val tempCompareVolumes = compareVolumes.toMutableList()

        if (prices.size > comparePrices.size) {
            repeat(delta) {
                tempComparePrices.add(0, -1.0)
                tempCompareTimeStamps.add(0, -1.0)
                tempCompareVolumes.add(0, -1.0)
            }
        } else if (comparePrices.size > prices.size) {
            repeat(delta) {
                tempPrices.add(0, -1.0)
                tempTimeStamps.add(0, -1.0)
                tempVolumes.add(0, -1.0)
            }
        }

        prices = tempPrices
        timeStamps = tempTimeStamps
        volumes = tempVolumes

        comparePrices = tempComparePrices
        compareTimeStamps = tempCompareTimeStamps
        compareVolumes = tempCompareVolumes
    }

    private fun setOnlyPrices() {
        val prefix = getPricesRangePrefix(mainPriceVolumeModel)

        onlyPrices = mainPriceVolumeModel?.prices?.takeLast(prefix)?.map { it.lastOrNull() ?: 0.0 }?.filter({ it != 0.0 }) ?: listOf()
    }

    private fun setPrices() {
        val prices: List<Double>
        val prefix = getPricesRangePrefix(mainPriceVolumeModel)

        if (currentChartType.value == ChartTypes.PRICE) {
            prices = mainPriceVolumeModel?.prices?.takeLast(prefix)?.map({ it.lastOrNull() ?: 0.0 }) ?: arrayListOf()
        } else if (currentChartType.value == ChartTypes.VOLUME) {
            prices = mainPriceVolumeModel?.total_volumes?.takeLast(prefix)?.map({ it.lastOrNull() ?: 0.0 }) ?: arrayListOf()
        } else {
            prices = mainPriceVolumeModel?.market_caps?.takeLast(prefix)?.map({ it.lastOrNull() ?: 0.0 }) ?: arrayListOf()
        }

        this.prices = prices.map { it }.filter { it != 0.0 }
    }

    private fun setTimestamps() {
        val timeStamps: List<Double>
        val prefix = getPricesRangePrefix(mainPriceVolumeModel)

        if (currentChartType.value == ChartTypes.PRICE) {
            timeStamps = mainPriceVolumeModel?.prices?.takeLast(prefix)?.map({ it.firstOrNull() ?: 0.0 }) ?: arrayListOf()
        } else if (currentChartType.value == ChartTypes.VOLUME) {
            timeStamps = mainPriceVolumeModel?.total_volumes?.takeLast(prefix)?.map({ it.firstOrNull() ?: 0.0 }) ?: arrayListOf()
        } else {
            timeStamps = mainPriceVolumeModel?.market_caps?.takeLast(prefix)?.map({ it.firstOrNull() ?: 0.0 }) ?: arrayListOf()
        }

        this.timeStamps = timeStamps.map { it }.filter { it != 0.0 }
    }

    private fun setVolumes() {
        val prefix = getPricesRangePrefix(mainPriceVolumeModel)

        volumes = mainPriceVolumeModel?.total_volumes?.takeLast(prefix)?.map({ (it.lastOrNull() ?: 0.0) })?.filter { it != 0.0 } ?: listOf()
    }

    private fun setOnlyComparePrices() {
        val prefix = getPricesRangePrefix(comparePriceVolumeModel)

        onlyComparePrices = comparePriceVolumeModel?.prices?.takeLast(prefix)?.map({ (it.lastOrNull() ?: 0.0) })?.filter { it != 0.0 } ?: listOf()
    }

    private fun setComparePrices() {
        val prices: List<Double>
        val prefix = getPricesRangePrefix(comparePriceVolumeModel)

        if (currentChartType.value == ChartTypes.PRICE) {
            prices = comparePriceVolumeModel?.prices?.takeLast(prefix)?.map({ it.lastOrNull() ?: 0.0 }) ?: arrayListOf()
        } else if (currentChartType.value == ChartTypes.VOLUME) {
            prices = comparePriceVolumeModel?.total_volumes?.takeLast(prefix)?.map({ it.lastOrNull() ?: 0.0 }) ?: arrayListOf()
        } else {
            prices = comparePriceVolumeModel?.market_caps?.takeLast(prefix)?.map({ it.lastOrNull() ?: 0.0 }) ?: arrayListOf()
        }

        this.comparePrices = prices.map { it }.filter { it != 0.0 }
    }

    private fun setCompareTimestamps() {
        val timeStamps: List<Double>
        val prefix = getPricesRangePrefix(comparePriceVolumeModel)

        if (currentChartType.value == ChartTypes.PRICE) {
            timeStamps = comparePriceVolumeModel?.prices?.takeLast(prefix)?.map({ it.firstOrNull() ?: 0.0 }) ?: arrayListOf()
        } else if (currentChartType.value == ChartTypes.VOLUME) {
            timeStamps = comparePriceVolumeModel?.total_volumes?.takeLast(prefix)?.map({ it.firstOrNull() ?: 0.0 }) ?: arrayListOf()
        } else {
            timeStamps = comparePriceVolumeModel?.market_caps?.takeLast(prefix)?.map({ it.firstOrNull() ?: 0.0 }) ?: arrayListOf()
        }

        this.compareTimeStamps = timeStamps.map { it }.filter { it != 0.0 }
    }

    private fun setCompareVolumes() {
        val prefix = getPricesRangePrefix(comparePriceVolumeModel)

        compareVolumes = comparePriceVolumeModel?.total_volumes?.takeLast(prefix)?.map({ (it.lastOrNull() ?: 0.0) })?.filter { it != 0.0 } ?: listOf()
    }

    private fun getPricesRangePrefix(priceVolume: PriceVolumeModel?): Int {
        return when (selectedTimeFrame) {
            TimeCharts.anHour -> { ((priceVolume?.prices?.size ?: 0) / 24) }
            else -> (priceVolume?.prices?.size ?: 0)
        }
    }
}