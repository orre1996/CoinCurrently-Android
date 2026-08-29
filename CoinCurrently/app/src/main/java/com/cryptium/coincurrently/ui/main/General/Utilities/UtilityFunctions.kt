package com.cryptium.coincurrently.ui.main.General.Utilities

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import com.cryptium.coincurrently.ui.main.Extensions.normalize
import kotlin.math.floor
import kotlin.math.min

data class Point(
    val x: Dp,
    val y: Dp
)

fun getPriceXPosition(position: Dp, textSize: Dp, readerWidthSize: Dp): Dp {
    if (position < textSize / 2) {
        return textSize / 2
    }

    if (position + textSize / 2 > readerWidthSize) {
        return readerWidthSize - textSize / 2
    }

    return position
}

@Composable
fun getPoint(
    index: Int,
    rect: DpSize,
    prices: List<Double>,
    dropLastTwo: Boolean = false
): Point {
    val negativeValues = prices.filter { it == -1.0 }.size
    val normalizedPrices = prices.filter { it != -1.0 }.normalize()
    val point = normalizedPrices.getOrNull(index - negativeValues) ?: 0f
    val x = rect.width * index.toFloat() / (prices.size - (if(dropLastTwo) 2 else 0) - 1).toFloat()
    val y = (1 - point) * rect.height

    Log.d("test rect ", rect.width.toString())
    Log.d("test index ", index.toString())
    Log.d("test count ", prices.count().toString())
    return Point(
        x = x,
        y = y
    )
}

/**
 * Line charts place point i at x = plotWidth * i / (pointCount - 1). This is the inverse mapping.
 */
fun lineChartIndexFromPlotX(xInPlot: Dp, plotWidth: Dp, pointCount: Int): Int {
    if (pointCount <= 0 || plotWidth.value <= 0f) return 0
    if (pointCount == 1) return 0
    val clampedX = xInPlot.coerceIn(0.dp, plotWidth)
    val maxIndex = pointCount
    return min(maxIndex, (maxIndex * (clampedX / plotWidth).toFloat()).toInt())
}

/** Candlestick row uses equal-width bins (weight 1/n each). */
fun candleChartIndexFromPlotX(xInPlot: Dp, plotWidth: Dp, candleCount: Int): Int {
    if (candleCount <= 1) return 0
    if (plotWidth.value <= 0f) return 0
    val idx = floor((xInPlot / plotWidth * candleCount.toFloat()).toDouble()).toInt()
    return idx.coerceIn(0, candleCount - 1)
}

/** Horizontal center of candle bin [index] for equal-width layout. */
fun candleBinCenterX(candleIndex: Int, plotWidth: Dp, candleCount: Int): Dp {
    if (candleCount <= 0) return 0.dp
    val i = candleIndex.coerceIn(0, candleCount - 1)
    return plotWidth * ((i + 0.5f) / candleCount)
}