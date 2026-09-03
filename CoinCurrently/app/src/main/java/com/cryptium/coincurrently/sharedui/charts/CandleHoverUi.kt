package com.cryptium.coincurrently.sharedui.charts

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import androidx.compose.ui.unit.min
import com.cryptium.coincurrently.core.extensions.formatCurrency
import com.cryptium.coincurrently.core.extensions.is24HourClock
import com.cryptium.coincurrently.core.networking.ResponseModels.Candle
import com.cryptium.coincurrently.core.networking.ResponseModels.TimeCharts
import com.cryptium.coincurrently.sharedui.components.candleBinCenterX
import com.cryptium.coincurrently.sharedui.components.candleChartIndexFromPlotX
import com.cryptium.coincurrently.sharedui.components.getPoint
import com.cryptium.coincurrently.sharedui.components.getPriceXPosition
import com.cryptium.coincurrently.sharedui.models.FormattingThreshold

data class CandleHoverUi(
    val stats: List<String>?,
    val timePosition: Dp,
    val dragIndex: Int,
    val xPosition: Dp,
    val pointY: Dp,
)

@Composable
fun rememberCandleHoverUi(
    dragPosition: Dp,
    chartSize: DpSize,
    candleSticks: List<Candle>,
    hoverCandlePriceTextWidth: Dp,
    timeChart: TimeCharts?,
    candleMiddlePrices: List<Double>,
    highestCandleValue: Double?,
    lowestCandleValue: Double?,
    candleTimestamps: List<Double>,
): CandleHoverUi {
    val n = candleSticks.size
    val xPosition = max(0.dp, min(chartSize.width, dragPosition))
    val dragIndex = if (n <= 1) 0 else candleChartIndexFromPlotX(xPosition, chartSize.width, n)
    val clampedIndex = dragIndex.coerceIn(0, (n - 1).coerceAtLeast(0))
    val is24Hour = LocalContext.current.is24HourClock
    val candle = candleSticks.getOrNull(clampedIndex)
    val stats = if (candle == null) {
        null
    } else {
        val timestamp = candleTimestamps.getOrNull(clampedIndex)
        listOf(
            formatHoverTimestamp(timestamp, timeChart, is24Hour, HoverTimeFallback.FromTimeChart),
            candle.open?.formatCurrency(threshold = FormattingThreshold.NONE).orEmpty(),
            candle.high?.formatCurrency(threshold = FormattingThreshold.NONE).orEmpty(),
            candle.low?.formatCurrency(threshold = FormattingThreshold.NONE).orEmpty(),
            candle.close?.formatCurrency(threshold = FormattingThreshold.NONE).orEmpty(),
        )
    }
    val binX = if (n <= 0) 0.dp else candleBinCenterX(clampedIndex, chartSize.width, n)
    val timePosition = getPriceXPosition(binX, hoverCandlePriceTextWidth, chartSize.width) -
        hoverCandlePriceTextWidth / 2

    var prices = candleMiddlePrices
    val highest = highestCandleValue
    val lowest = lowestCandleValue
    val pointY = if (highest == null || lowest == null) {
        0.5.dp
    } else {
        prices = prices + highest + lowest
        getPoint(
            index = clampedIndex,
            rect = chartSize,
            prices = prices,
            dropLastTwo = true,
        ).y
    }

    return CandleHoverUi(
        stats = stats,
        timePosition = timePosition,
        dragIndex = clampedIndex,
        xPosition = xPosition,
        pointY = pointY,
    )
}
