package com.cryptium.coincurrently.features.FearGreed
import android.util.Log
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import androidx.compose.ui.unit.min
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.cryptium.coincurrently.core.config.AppLocale
import com.cryptium.coincurrently.sharedui.theme.extremeFear
import com.cryptium.coincurrently.sharedui.theme.extremeGreed
import com.cryptium.coincurrently.sharedui.theme.fear
import com.cryptium.coincurrently.sharedui.theme.greed
import com.cryptium.coincurrently.sharedui.theme.neutral
import com.cryptium.coincurrently.sharedui.charts.ChartHoverController
import com.cryptium.coincurrently.sharedui.components.Point
import com.cryptium.coincurrently.sharedui.charts.currentHoverXPosition
import com.cryptium.coincurrently.sharedui.charts.decimalDifferenceValue
import com.cryptium.coincurrently.sharedui.charts.differencePercent
import com.cryptium.coincurrently.sharedui.charts.dragPositions
import com.cryptium.coincurrently.sharedui.components.getPoint
import com.cryptium.coincurrently.sharedui.components.getPriceXPosition
import com.cryptium.coincurrently.sharedui.charts.hoverDragIndex
import com.cryptium.coincurrently.sharedui.charts.indexDifferenceValue
import com.cryptium.coincurrently.sharedui.charts.multiHoverGraphColor
import com.cryptium.coincurrently.features.FearGreed.FearGreedRepository
import kotlin.math.roundToInt
import com.cryptium.coincurrently.core.networking.ResponseModels.FearGreed
import com.cryptium.coincurrently.core.networking.ResponseModels.TimeCharts
import java.text.SimpleDateFormat
import java.util.Date

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

    @get:Composable val hoverPrice: String
    @get:Composable val hoverTimePosition: Dp
    @get:Composable val dragIndex: Int
    @get:Composable val dragPoint: Point
    @get:Composable val hoverTime: String
    @get:Composable val graphColor: Color
    @get:Composable val hoverColor: Color
    @get:Composable val multiHoverGraphColor: Color
    @get:Composable val differenceValue: String
    @get:Composable val differencePercent: String
    @get:Composable val dragPositions: List<Point>
    @get:Composable val multiHoverTimes: List<String>

    @Composable fun getXPosition(textWidth: Dp): Dp
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
    val chartHover = ChartHoverController()

    override val historicTimeframes = listOf(
        Pair(0, "Now"),
        Pair(1, "Yesterday"),
        Pair(7, "Last week"),
        Pair(30, "Last month")
    )

    init {
        fearGreedModel = fearAndGreedRepository.fearGreed
    }

    override val dragIndex: Int @Composable get() {
        val drawCount = (prices.size - 2).coerceAtLeast(0)
        if (drawCount <= 1) return 0
        return chartHover.hoverDragIndex(chartSize.value.width, drawCount)
    }

    override val hoverPrice: String @Composable get() {
        return chartHover.hoverHelper.hoverPrices.firstOrNull()?.toInt()?.toString()
            ?: prices.getOrNull(dragIndex)?.toInt()?.toString()
            ?: "0"
    }

    override val hoverTimePosition: Dp @Composable get() {
        return getXPosition(hoverPriceTextWidth.value) - hoverPriceTextWidth.value / 2
    }

    override val dragPoint: Point @Composable get() {
        return getPoint(
            index = dragIndex,
            rect = chartSize.value,
            prices = prices,
            dropLastTwo = true
        )
    }

    override val graphColor: Color @Composable get() {
        val value = prices.dropLast(2).lastOrNull()?.toInt() ?: 0
        return getFearGreedColor(value)
    }

    override val hoverColor: Color @Composable get() {
        val value = prices.dropLast(2).getOrNull(dragIndex)?.toInt() ?: 0
        return getFearGreedColor(value)
    }

    override val hoverTime: String @Composable get() {
        return chartHover.hoverHelper.hoverTimes.firstOrNull() ?: run {
            val formatter = when (selectedChartTimeFrame) {
                TimeCharts.max -> "MMM YYYY"
                else -> "dd MMM YYYY"
            }
            val currentTimestamp = timeStamps.getOrNull(dragIndex)?.toLong() ?: 0
            SimpleDateFormat(formatter, AppLocale.enUsPosix).format(Date(currentTimestamp))
        }
    }

    override val multiHoverGraphColor: Color @Composable get() {
        val default = graphColor
        return chartHover.multiHoverGraphColor(default)
    }

    override val differenceValue: String @Composable get() = chartHover.indexDifferenceValue()

    override val differencePercent: String @Composable get() = chartHover.differencePercent()

    override val dragPositions: List<Point> @Composable get() {
        return chartHover.dragPositions(chartSize.value, prices, dropLastCount = 2)
    }

    override val multiHoverTimes: List<String> @Composable get() = chartHover.hoverHelper.hoverTimes

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

    @Composable
    override fun getXPosition(textWidth: Dp): Dp {
        val xPosition = getPoint(
            index = dragIndex,
            rect = chartSize.value,
            prices = prices,
            dropLastTwo = true
        ).x

        return getPriceXPosition(xPosition, textWidth, chartSize.value.width)
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

    @Composable
    private fun getFearGreedColor(value: Int): Color {
        return when {
            value < 20 -> MaterialTheme.colors.extremeFear
            value < 40 -> MaterialTheme.colors.fear
            value < 60 -> MaterialTheme.colors.neutral
            value < 80 -> MaterialTheme.colors.greed
            else -> MaterialTheme.colors.extremeGreed
        }
    }

}
