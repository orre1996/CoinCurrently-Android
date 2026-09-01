package com.cryptium.coincurrently.sharedui.charts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import androidx.compose.ui.unit.min
import com.cryptium.coincurrently.sharedui.charts.getChangeColorCompose
import com.cryptium.coincurrently.sharedui.charts.getChangeWithSymbol
import com.cryptium.coincurrently.core.extensions.roundTo
import com.cryptium.coincurrently.core.extensions.formatCurrency
import com.cryptium.coincurrently.core.extensions.getDifferenceInPercent
import com.cryptium.coincurrently.sharedui.models.FormattingThreshold
import com.cryptium.coincurrently.core.networking.ResponseModels.TimeCharts
import com.cryptium.coincurrently.sharedui.components.Point
import com.cryptium.coincurrently.sharedui.components.getPoint
import com.cryptium.coincurrently.sharedui.components.lineChartIndexFromPlotX

class ChartHoverController {
    val hoverHelper = HoverHelper()
    var isDragging by mutableStateOf(false)
    var dragPosition = mutableStateOf(0.dp)

    fun updateHoverStats(
        touchXs: List<Dp>,
        chartWidth: Dp,
        prices: List<Double>,
        dates: List<Double>,
        timeChart: TimeCharts?,
        is24HourClock: Boolean,
        isBlocked: Boolean = false,
        hoverPrices: List<Double> = prices,
        dontShowTime: Boolean = false,
    ) {
        if (isBlocked) return

        if (dates.isEmpty() || touchXs.isEmpty()) {
            clearHover()
            return
        }

        isDragging = true

        val adjustedTouchXs = touchXs.map { touchX ->
            max(0.dp, min(touchX, chartWidth - 0.001.dp))
        }
        dragPosition.value = adjustedTouchXs.firstOrNull() ?: 0.dp

        hoverHelper.getHoverPrice(
            chartWidth = chartWidth,
            touchXs = adjustedTouchXs,
            prices = hoverPrices,
            dates = dates,
            timeChart = timeChart,
            is24HourClock = is24HourClock,
            isDragging = isDragging,
            dontShowTime = dontShowTime,
        )
    }

    fun clearHover() {
        isDragging = false
    }

    fun resetHoverValues() {
        hoverHelper.resetValues()
    }
}

@Composable
fun ChartHoverController.currentHoverXPosition(chartWidth: Dp): Dp {
    if (chartWidth.value <= 0f) return 0.dp

    val hoverPoints = hoverHelper.offset
    if (hoverPoints.isEmpty()) {
        return max(0.dp, min(dragPosition.value, chartWidth))
    }

    return if (hoverHelper.hoverPrices.size == 2) {
        val average = hoverPoints.sumOf { it.value.toDouble() } / hoverPoints.size
        max(0.dp, min(average.toFloat().dp, chartWidth))
    } else {
        max(0.dp, min(hoverPoints.first(), chartWidth))
    }
}

@Composable
fun ChartHoverController.hoverDragIndex(chartWidth: Dp, pointCount: Int): Int {
    if (pointCount <= 1 || chartWidth.value <= 0f) return 0
    return lineChartIndexFromPlotX(currentHoverXPosition(chartWidth), chartWidth, pointCount)
}

@Composable
fun ChartHoverController.dragPositions(
    chartSize: DpSize,
    prices: List<Double>,
    dropLastCount: Int = 0,
): List<Point> {
    val drawCount = (prices.size - dropLastCount).coerceAtLeast(1)
    return hoverHelper.offset.map { touchX ->
        val index = lineChartIndexFromPlotX(touchX, chartSize.width, drawCount)
        getPoint(
            index = index,
            rect = chartSize,
            prices = prices,
            dropLastTwo = dropLastCount >= 2,
        )
    }
}

@Composable
fun ChartHoverController.multiHoverGraphColor(defaultColor: Color): Color {
    if (hoverHelper.hoverPrices.size != 2 || !isDragging) return defaultColor

    val first = hoverHelper.hoverPrices.getOrNull(0) ?: 0.0
    val second = hoverHelper.hoverPrices.getOrNull(1) ?: 0.0
    val change = (second - first).roundTo(7)

    if (change == 0.0) {
        return getDifferenceInPercent(second, first)?.roundTo(7)?.getChangeColorCompose() ?: defaultColor
    }

    return change.getChangeColorCompose()
}

fun ChartHoverController.currencyDifferenceValue(): String {
    val first = hoverHelper.hoverPrices.getOrNull(0) ?: 0.0
    val second = hoverHelper.hoverPrices.getOrNull(1) ?: 0.0
    val percent = getDifferenceInPercent(second, first)?.roundTo(2) ?: 0.0
    val sign = if (percent < 0) "" else "+"
    val difference = (second - first).formatCurrency(threshold = FormattingThreshold.MILLION)
    return "$sign$difference"
}

fun ChartHoverController.differencePercent(): String {
    val first = hoverHelper.hoverPrices.getOrNull(0) ?: 0.0
    val second = hoverHelper.hoverPrices.getOrNull(1) ?: 0.0
    val difference = getDifferenceInPercent(second, first) ?: 0.0
    return difference.getChangeWithSymbol(withSymbol = true, force = true)
}

fun ChartHoverController.indexDifferenceValue(): String {
    val first = hoverHelper.hoverPrices.getOrNull(0) ?: 0.0
    val second = hoverHelper.hoverPrices.getOrNull(1) ?: 0.0
    val percent = getDifferenceInPercent(second, first)?.roundTo(2) ?: 0.0
    val sign = if (percent < 0) "" else "+"
    val difference = (second - first).toInt()
    return "$sign$difference"
}

fun ChartHoverController.decimalDifferenceValue(decimals: Int = 2): String {
    val first = hoverHelper.hoverPrices.getOrNull(0) ?: 0.0
    val second = hoverHelper.hoverPrices.getOrNull(1) ?: 0.0
    val percent = getDifferenceInPercent(second, first)?.roundTo(2) ?: 0.0
    val sign = if (percent < 0) "" else "+"
    val difference = (second - first).roundTo(decimals)
    return "$sign${"%.${decimals}f".format(difference)}"
}
