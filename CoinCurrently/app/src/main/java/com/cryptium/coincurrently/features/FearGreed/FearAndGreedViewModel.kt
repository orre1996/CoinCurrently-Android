package com.cryptium.coincurrently.features.FearGreed
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.cryptium.coincurrently.sharedui.charts.ChartHoverController
import com.cryptium.coincurrently.features.FearGreed.FearGreedRepository
import com.cryptium.coincurrently.core.networking.ResponseModels.FearGreed
import com.cryptium.coincurrently.core.networking.ResponseModels.TimeCharts

class FearAndGreedViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return FearAndGreedViewModelImp() as T
    }
}

interface FearAndGreedViewModel {
    var fearGreedModel: FearGreed?
    var selectedChartTimeFrame: TimeCharts
    var isDragging: Boolean
    var prices: List<Double>
    var timeStamps: List<Double>
    var dragPosition: MutableState<Dp>
    var chartSize: MutableState<DpSize>
    var viewSize: MutableState<DpSize>
    val hoverPriceTextWidth: MutableState<Dp>
    val historicTimeframes: List<Pair<Int, String>>
    val chartHover: ChartHoverController

    fun updateHoverStats(touchXs: List<Dp>, is24HourClock: Boolean)
    suspend fun getFearGreed(callback: (() -> Unit)? = null)
    fun setPrices()
}

class FearAndGreedViewModelImp: ViewModel(), FearAndGreedViewModel {
    private val fearAndGreedRepository by lazy { FearGreedRepository }

    override var fearGreedModel: FearGreed? by mutableStateOf(null)

    override var selectedChartTimeFrame by mutableStateOf(
        TimeCharts.getStartTimeFrameBetween(TimeCharts.aWeek, TimeCharts.max),
    )
    override var isDragging by mutableStateOf(false)
    override var prices by mutableStateOf(listOf<Double>())
    override var timeStamps by mutableStateOf(listOf<Double>())
    override var dragPosition = mutableStateOf(0.dp)
    override var chartSize = mutableStateOf(DpSize.Zero)
    override var viewSize = mutableStateOf(DpSize.Zero)
    override val hoverPriceTextWidth = mutableStateOf(0.dp)
    override val chartHover = ChartHoverController()

    override val historicTimeframes = listOf(
        Pair(0, "Now"),
        Pair(1, "Yesterday"),
        Pair(7, "Last week"),
        Pair(30, "Last month")
    )

    init {
        fearGreedModel = fearAndGreedRepository.fearGreed
    }

    override fun updateHoverStats(touchXs: List<Dp>, is24HourClock: Boolean) {
        chartHover.updateHoverStats(
            touchXs = touchXs,
            chartWidth = chartSize.value.width,
            prices = prices.dropLast(2),
            dates = timeStamps,
            timeChart = selectedChartTimeFrame,
            is24HourClock = is24HourClock,
            isBlocked = fearGreedModel == null,
            hoverPrices = prices.dropLast(2),
            dontShowTime = true,
        )
        isDragging = chartHover.isDragging
        dragPosition.value = chartHover.dragPosition.value
    }

    override suspend fun getFearGreed(callback: (() -> Unit)?) {
        fearAndGreedRepository.getFearGreedModel()?.let {
            fearGreedModel = it
            setPrices()
            callback?.invoke()
        }
    }

    override fun setPrices() {
        val data = fearGreedModel?.data.orEmpty()
        val limit = when (selectedChartTimeFrame) {
            TimeCharts.aWeek       -> 7
            TimeCharts.aMonth      -> 30
            TimeCharts.threeMonths -> 90
            TimeCharts.aYear       -> 360
            TimeCharts.max         -> data.size
            else                   -> 0
        }

        val subset = data.take(limit).asReversed()
        val priceArray = subset.map { it.value?.toDouble() ?: 0.0 } + listOf(0.0, 100.0)
        val timeStampsArray = subset.map { (it.timestamp?.toDouble() ?: 0.0) * 1000 }

        prices = priceArray
        timeStamps = timeStampsArray
    }
}
