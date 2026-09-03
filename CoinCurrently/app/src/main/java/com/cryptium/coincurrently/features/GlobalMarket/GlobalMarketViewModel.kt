package com.cryptium.coincurrently.features.GlobalMarket
import android.util.Log
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.referentialEqualityPolicy
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.cryptium.coincurrently.core.extensions.formatCurrency
import com.cryptium.coincurrently.core.extensions.getDifferenceInPercent
import com.cryptium.coincurrently.sharedui.charts.ChartHoverController
import com.cryptium.coincurrently.core.networking.ResponseModels.GlobalMarketChartData
import com.cryptium.coincurrently.core.networking.ResponseModels.GlobalMarketData
import com.cryptium.coincurrently.features.GlobalMarket.GlobalMarketRepository
import com.cryptium.coincurrently.core.persistence.StorageKeys
import com.cryptium.coincurrently.core.persistence.StorageManager
import com.cryptium.coincurrently.core.models.ChartTypes
import com.cryptium.coincurrently.core.networking.ResponseModels.CurrencySelectionModel
import com.cryptium.coincurrently.core.networking.ResponseModels.TimeCharts

class GlobalMarketViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return GlobalMarketViewModelImp() as T
    }
}

interface GlobalMarketViewModel {
    var globalMarket: GlobalMarketData?
    var globalMarketChart: GlobalMarketChartData?
    var selectedTimeFrame: TimeCharts
    var selectedChartType: ChartTypes
    var prices: List<Double>
    var timeStamps: List<Double>
    var changePercent: Double?
    var mainReloading: Boolean
    var isRefreshing: Boolean
    var isDragging: Boolean
    var showMainPriceError: Boolean
    val chartHover: ChartHoverController
    var chartSize: androidx.compose.runtime.MutableState<DpSize>
    val hoverPriceTextWidth: androidx.compose.runtime.MutableState<Dp>

    val mainGraphStats: List<String?>

    suspend fun setStartData()
    suspend fun getGlobalMarket()
    suspend fun refresh(newDays: String? = null, newTimeChart: TimeCharts? = null)
    suspend fun getColors(context: Context)
    fun setChartType(type: ChartTypes)
    fun updateHoverStats(touchXs: List<Dp>, is24HourClock: Boolean)

    val latestChartMarketCap: Double?
    val latestChartVolume: Double?
    val marketCapPrices: List<Double>
    val volumePrices: List<Double>
    val marketCapChangePercent: Double?
}

class GlobalMarketViewModelImp : ViewModel(), GlobalMarketViewModel {
    private val globalMarketRepository by lazy { GlobalMarketRepository }

    override var globalMarket: GlobalMarketData? by mutableStateOf(null, referentialEqualityPolicy())
    override var globalMarketChart: GlobalMarketChartData? by mutableStateOf(null, referentialEqualityPolicy())
    override var selectedTimeFrame by mutableStateOf(
        TimeCharts.getStartTimeFrameBetween(TimeCharts.aWeek, TimeCharts.max)
    )
    private val initialChartType = (
        StorageManager.get<ChartTypes>(StorageKeys.SELECTED_CHART_TYPE) ?: ChartTypes.MKTCAP
    ).let {
        if (it == ChartTypes.PRICE) ChartTypes.MKTCAP else it
    }
    override var selectedChartType by mutableStateOf(initialChartType)
    override var prices by mutableStateOf(listOf<Double>())
    override var timeStamps by mutableStateOf(listOf<Double>())
    override var changePercent by mutableStateOf<Double?>(null)
    override var mainReloading by mutableStateOf(true)
    override var isRefreshing by mutableStateOf(false)
    override var isDragging by mutableStateOf(false)
    override var showMainPriceError by mutableStateOf(false)
    override val chartHover = ChartHoverController()
    override var chartSize = mutableStateOf(DpSize.Zero)
    override val hoverPriceTextWidth = mutableStateOf(0.dp)

    init {
        globalMarket = globalMarketRepository.globalMarket

        globalMarketRepository.loadCachedDataForGlobalChart(selectedTimeFrame.days)

        globalMarketRepository.globalChart?.get(selectedTimeFrame.days)?.let { chartData ->
            globalMarketChart = chartData
            setPrices()
            setTimestamps()
            showMainPriceError = prices.isEmpty()
            mainReloading = false
        }
    }

    override val mainGraphStats: List<String?>
        get() = (0 until 6).map { getPriceInChart(prices, it) }

    override val latestChartMarketCap: Double?
        get() = lastChartValue(globalMarketChart?.market_cap)

    override val latestChartVolume: Double?
        get() = lastChartValue(globalMarketChart?.volume)

    override val marketCapPrices: List<Double>
        get() = chartSeries(globalMarketChart?.market_cap)

    override val volumePrices: List<Double>
        get() = chartSeries(globalMarketChart?.volume)

    override val marketCapChangePercent: Double?
        get() = seriesChangePercent(marketCapPrices)

    override suspend fun setStartData() {
        globalMarketRepository.loadCachedDataForGlobalChart(selectedTimeFrame.days)

        globalMarketRepository.globalMarket?.let {
            globalMarket = it
        }

        globalMarketRepository.globalChart?.get(selectedTimeFrame.days)?.let { chartData ->
            globalMarketChart = chartData
            setPrices()
            setTimestamps()
            showMainPriceError = prices.isEmpty()
        }
    }

    override suspend fun getGlobalMarket() {
        Log.i("GlobalMarket", "GlobalMarketViewModel.getGlobalMarket() called")
        val data = globalMarketRepository.getGlobalMarket()
        Log.d(
            "GlobalMarket",
            "GlobalMarketViewModel.getGlobalMarket() result null=${data == null} " +
                "active=${data?.active_cryptocurrencies} pct=${data?.percentages?.size}"
        )
        if (data != null) {
            globalMarket = data
            Log.i("GlobalMarket", "GlobalMarketViewModel state updated")
        } else {
            Log.w("GlobalMarket", "GlobalMarketViewModel keeping previous globalMarket=${globalMarket != null}")
        }
    }

    override suspend fun refresh(newDays: String?, newTimeChart: TimeCharts?) {
        mainReloading = true

        val chart = newTimeChart ?: selectedTimeFrame
        selectedTimeFrame = chart

        globalMarketRepository.getGlobalChart(newDays ?: chart.days)?.let { chartData ->
            globalMarketChart = chartData
            setPrices()
            setTimestamps()
            showMainPriceError = false
        } ?: run {
            showMainPriceError = true
        }

        mainReloading = false
    }

    override suspend fun getColors(context: Context) {
        globalMarketRepository.getColors(context) { updated ->
            globalMarket = updated.copy(percentages = updated.percentages?.toList())
        }?.let { result ->
            globalMarket = result.copy(percentages = result.percentages?.toList())
        }
    }

    override fun setChartType(type: ChartTypes) {
        selectedChartType = when (type) {
            ChartTypes.PRICE -> ChartTypes.MKTCAP
            else -> type
        }
        StorageManager.set(StorageKeys.SELECTED_CHART_TYPE, selectedChartType)

        setPrices()
        setTimestamps()
        showMainPriceError = prices.isEmpty()
    }

    override fun updateHoverStats(touchXs: List<Dp>, is24HourClock: Boolean) {
        chartHover.updateHoverStats(
            touchXs = touchXs,
            chartWidth = chartSize.value.width,
            prices = prices,
            dates = timeStamps,
            timeChart = selectedTimeFrame,
            is24HourClock = is24HourClock,
            isBlocked = mainReloading,
        )
        isDragging = chartHover.isDragging
    }

    private fun setPrices() {
        val chartData = chartDataForSelectedType()

        prices = chartSeries(chartData)

        changePercent = seriesChangePercent(prices)
    }

    private fun setTimestamps() {
        val chartData = chartDataForSelectedType()

        timeStamps = chartData
            ?.mapNotNull { it.firstOrNull() }
            ?.filter { it != 0.0 }
            ?: emptyList()
    }

    private fun chartDataForSelectedType(): ArrayList<ArrayList<Double?>>? {
        return when (selectedChartType) {
            ChartTypes.VOLUME -> globalMarketChart?.volume
            else -> globalMarketChart?.market_cap
        }
    }

    private fun chartSeries(chartData: ArrayList<ArrayList<Double?>>?): List<Double> {
        return chartData
            ?.mapNotNull { it.lastOrNull() }
            ?.filter { it != 0.0 }
            ?: emptyList()
    }

    private fun seriesChangePercent(prices: List<Double>): Double? {
        return if (prices.size >= 2) {
            getDifferenceInPercent(prices.last(), prices.first())
        } else {
            null
        }
    }

    private fun lastChartValue(chartData: ArrayList<ArrayList<Double?>>?): Double? {
        return chartData
            ?.mapNotNull { it.lastOrNull() }
            ?.filter { it != 0.0 }
            ?.lastOrNull()
    }

    private fun getPriceInChart(prices: List<Double>, at: Int): String? {
        if (showMainPriceError) return null

        val minPrice = prices.minOrNull() ?: return null
        val maxPrice = prices.maxOrNull() ?: return null
        val step = (maxPrice - minPrice) / 5
        val value = minPrice + (step * at.toDouble())

        return value.formatCurrency(currency = CurrencySelectionModel.empty())
    }
}
