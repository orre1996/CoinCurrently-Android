package com.cryptium.coincurrently.sharedui.charts

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import androidx.compose.ui.unit.min
import com.cryptium.coincurrently.core.extensions.formatCurrency
import com.cryptium.coincurrently.core.extensions.getDifferenceInPercent
import com.cryptium.coincurrently.core.extensions.is24HourClock
import com.cryptium.coincurrently.core.networking.ResponseModels.TimeCharts
import com.cryptium.coincurrently.sharedui.components.Point
import com.cryptium.coincurrently.sharedui.components.getPoint
import com.cryptium.coincurrently.sharedui.components.getPriceXPosition
import com.cryptium.coincurrently.sharedui.components.lineChartIndexFromPlotX
import com.cryptium.coincurrently.sharedui.models.FormattingThreshold

data class CompareChartHoverUi(
    val dragIndex: Int,
    val hoverPrice: String,
    val comparedHoverPrice: String,
    val hoverTime: String,
    val hoverTimePosition: Dp,
    val dragPoint: Point,
    val compareDragPoint: Point,
    val mainDifferencePercent: Double,
    val compareDifferencePercent: Double,
)

@Composable
fun rememberCompareChartHoverUi(
    dragPosition: Dp,
    chartSize: DpSize,
    hoverPriceTextWidth: Dp,
    timeChart: TimeCharts?,
    prices: List<Double>,
    comparePrices: List<Double>,
    timeStamps: List<Double>,
    compareTimeStamps: List<Double>,
    formatPrice: (Double) -> String = {
        it.formatCurrency(threshold = FormattingThreshold.MILLION)
    },
): CompareChartHoverUi {
    val hoverX = max(0.dp, min(dragPosition, chartSize.width))
    val drawnPrices = if (prices.size > 1) prices.dropLast(2) else prices
    val drawnComparePrices = if (comparePrices.size > 1) comparePrices.dropLast(2) else comparePrices
    val drawnTimeStamps = if (prices.size > 1) timeStamps.dropLast(2) else timeStamps
    val drawnCompareTimeStamps = if (compareTimeStamps.size > 1) {
        compareTimeStamps.dropLast(2)
    } else {
        compareTimeStamps
    }
    val drawCount = drawnPrices.size
    val dragIndex = if (drawCount <= 1) {
        0
    } else {
        lineChartIndexFromPlotX(hoverX, chartSize.width, drawCount)
    }

    fun formattedOrNa(value: Double?): String {
        return if (value == null || value == -1.0) "N/A" else formatPrice(value)
    }

    val is24Hour = LocalContext.current.is24HourClock
    val hoverTime = run {
        if (drawnTimeStamps.isEmpty()) {
            ""
        } else {
            val lowestTimeStamp = if (
                (drawnTimeStamps.filter { it != -1.0 }.minOrNull() ?: Double.POSITIVE_INFINITY) <
                    (drawnCompareTimeStamps.filter { it != -1.0 }.minOrNull() ?: Double.POSITIVE_INFINITY)
            ) {
                drawnTimeStamps
            } else {
                drawnCompareTimeStamps
            }
            if (lowestTimeStamp.isEmpty()) {
                ""
            } else {
                formatHoverTimestamp(
                    lowestTimeStamp.getOrNull(dragIndex),
                    timeChart,
                    is24Hour,
                    HoverTimeFallback.FromTimeChart,
                )
            }
        }
    }

    val dragPoint = getPoint(
        index = dragIndex,
        rect = chartSize,
        prices = prices,
        dropLastTwo = true,
    )
    val compareDragPoint = getPoint(
        index = dragIndex,
        rect = chartSize,
        prices = comparePrices,
        dropLastTwo = true,
    )
    val hoverTimePosition = getPriceXPosition(
        dragPoint.x,
        hoverPriceTextWidth,
        chartSize.width,
    ) - hoverPriceTextWidth / 2

    val mainFirst = prices.firstOrNull { it != -1.0 } ?: 0.0
    val mainSecond = drawnPrices.getOrNull(dragIndex) ?: 0.0
    val compareFirst = comparePrices.firstOrNull { it != -1.0 } ?: 0.0
    val compareSecond = drawnComparePrices.getOrNull(dragIndex) ?: 0.0

    return CompareChartHoverUi(
        dragIndex = dragIndex,
        hoverPrice = formattedOrNa(drawnPrices.getOrNull(dragIndex)),
        comparedHoverPrice = formattedOrNa(drawnComparePrices.getOrNull(dragIndex)),
        hoverTime = hoverTime,
        hoverTimePosition = hoverTimePosition,
        dragPoint = dragPoint,
        compareDragPoint = compareDragPoint,
        mainDifferencePercent = getDifferenceInPercent(mainSecond, mainFirst) ?: 0.0,
        compareDifferencePercent = getDifferenceInPercent(compareSecond, compareFirst) ?: 0.0,
    )
}
