package com.cryptium.coincurrently.ui.main.CryptoDetail.Compose.PriceChart

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.cryptium.coincurrently.ui.main.Compose.onSurfaceDim
import com.cryptium.coincurrently.ui.main.Compose.trendDown
import com.cryptium.coincurrently.ui.main.Compose.trendUp
import com.cryptium.coincurrently.ui.main.Extensions.normalizeCustom
import networking.ResponseModels.Candle

@Composable
fun CandleChart(
    candles: List<Candle>?,
    modifier: Modifier
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(0.dp),
        modifier = modifier
    ) {
        candles?.forEach {
            val normalizedCandle = getNormalisedCandles(candles, it)
            val color = getCandleColor(it)

            Canvas(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f)
            ) {
                val candleStickStem = Path().apply {
                    moveTo(0.5f * size.width, (1f - (normalizedCandle.getOrNull(2) ?: 0f)) * size.height)
                    lineTo(0.5f * size.width, (1f - (normalizedCandle.getOrNull(3) ?: 0f)) * size.height)
                }

                drawPath(
                    path = candleStickStem,
                    color = color,
                    style = Stroke(
                        width = 1.dp.toPx(),
                        cap = StrokeCap.Butt
                    )
                )

                val candleStickBody = Path().apply {
                    moveTo(0.5f * size.width, (1f - (normalizedCandle.getOrNull(1) ?: 0f)) * size.height)
                    lineTo(0.5f * size.width, (1f - (normalizedCandle.getOrNull(4) ?: 0f)) * size.height)
                }

                drawPath(
                    path = candleStickBody,
                    color = color,
                    style = Stroke(
                        width = size.width - 2.dp.toPx(),
                        cap = StrokeCap.Butt
                    )
                )
            }
        }
    }
}

@Composable
private fun getCandleColor(candle: Candle): Color {
    return if ((candle.open ?: 0.0) == (candle.close ?: 0.0)) {
        MaterialTheme.colors.onSurfaceDim
    } else if ((candle.open ?: 0.0) > (candle.close ?: 0.0)) {
        MaterialTheme.colors.trendUp
    } else {
        MaterialTheme.colors.trendDown
    }
}

private fun getNormalisedCandles(candles: List<Candle>?, candle: Candle?): List<Float> {
    val max = candles?.maxBy { it.high ?: 0.0 }?.high ?: 0.0
    val min = candles?.minBy { it.low ?: 0.0 }?.low ?: 0.0

    val timestamp = candle?.timestamp ?: 0.0
    val open = candle?.open ?: 0.0
    val high = candle?.high ?: 0.0
    val low = candle?.low ?: 0.0
    val close = candle?.close ?: 0.0

    if (open == 1.0 && high == 1.0 && low == 1.0 && close == 1.0) {
        return listOf(0.5f, 0.5f, 0.5f, 0.5f, 0.502f)
    }

    val normalizedCandles: ArrayList<Float> = ArrayList(listOf(
        timestamp,
        open,
        high,
        low,
        close
    ).normalizeCustom(min, max))

    if (normalizedCandles.getOrNull(1) == normalizedCandles.getOrNull(4)) {
        normalizedCandles[4] = (normalizedCandles.getOrNull(4) ?: 0f) + 0.002f
    }

    return normalizedCandles
}