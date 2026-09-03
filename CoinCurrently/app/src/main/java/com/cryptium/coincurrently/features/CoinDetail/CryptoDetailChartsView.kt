package com.cryptium.coincurrently.features.CoinDetail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.material.MaterialTheme
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.layout.onGloballyPositioned
import kotlin.math.floor
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.BoxScope
import com.cryptium.coincurrently.sharedui.charts.ChartHoverView
import com.cryptium.coincurrently.sharedui.charts.CandleChart
import com.cryptium.coincurrently.sharedui.charts.CandleChartHover
import com.cryptium.coincurrently.sharedui.charts.LineChart
import com.cryptium.coincurrently.sharedui.charts.hideFromTalkBackWhenEnabled
import com.cryptium.coincurrently.sharedui.components.Point
import com.cryptium.coincurrently.core.networking.ResponseModels.Candle
import androidx.compose.runtime.MutableState
import com.cryptium.coincurrently.sharedui.theme.*

@Composable
internal fun MainChart(
    prices: List<Double>,
    chartColor: Color?,
    dragIndex: Int?,
    isDragging: Boolean,
    dragPositions: List<Point>,
    chartSize: MutableState<DpSize>,
    biggestValue: DpSize,
    hideFromTalkBack: Boolean = false,
    showLivePriceIndicator: Boolean = false,
    showHoverOverlay: Boolean = true,
    statsAreaHeight: Dp = 0.dp,
    fullHeightHoverLine: Boolean = false,
) {
    val localDensity = LocalDensity.current
    val chartModifier = Modifier
        .onGloballyPositioned { coordinates ->
            val sizePx = coordinates.size
            val width = floor(sizePx.width.toFloat())
            val height = floor(sizePx.height.toFloat())

            chartSize.value = with(localDensity) {
                DpSize(
                    width.toDp(),
                    height.toDp()
                )
            }
        }
        .then(
            if (hideFromTalkBack) {
                Modifier.hideFromTalkBackWhenEnabled()
            } else {
                Modifier
            }
        )

    LineChart(
        prices = prices,
        color = chartColor ?: MaterialTheme.colors.onSurfaceDim,
        dragIndex = dragIndex,
        showFill = true,
        isDragging = isDragging,
        showLivePriceIndicator = showLivePriceIndicator,
        dragPositionXs = if (isDragging) {
            dragPositions.map { with(localDensity) { it.x.toPx() } }
        } else {
            emptyList()
        },
        paddings = PaddingValues(
            top = biggestValue.height / 2,
            bottom = biggestValue.height / 2,
            end = biggestValue.width
        ),
        modifier = chartModifier
    )

    if (showHoverOverlay) {
        Box(
            modifier = if (hideFromTalkBack) {
                Modifier.hideFromTalkBackWhenEnabled()
            } else {
                Modifier
            }
        ) {
            ChartHoverView(
                positions = dragPositions,
                isDragging = isDragging,
                color = chartColor ?: MaterialTheme.colors.onSurfaceDim,
                topPadding = biggestValue.height / 2,
                statsAreaHeight = statsAreaHeight,
                fullHeightLine = fullHeightHoverLine,
            )
        }
    }
}

@Composable
internal fun CandleChart(
    candleSticks: List<Candle>,
    isDragging: Boolean,
    hoverXPosition: Float,
    pointCandle: Dp,
    chartSize: MutableState<DpSize>,
    biggestValue: DpSize,
    hideFromTalkBack: Boolean = false,
    showHoverOverlay: Boolean = true,
) {
    val localDensity = LocalDensity.current
    val chartModifier = Modifier
        .padding(
            PaddingValues(
                top = biggestValue.height / 2,
                bottom = biggestValue.height / 2,
                end = biggestValue.width,
            ),
        )
        .onGloballyPositioned { coordinates ->
            val sizePx = coordinates.size
            chartSize.value = with(localDensity) {
                DpSize(
                    sizePx.width.toDp(),
                    sizePx.height.toDp()
                )
            }
        }
        .then(
            if (hideFromTalkBack) {
                Modifier.hideFromTalkBackWhenEnabled()
            } else {
                Modifier
            }
        )

    com.cryptium.coincurrently.sharedui.charts.CandleChart(
        candleSticks,
        modifier = chartModifier
    )

    if (showHoverOverlay) {
        Box(
            modifier = if (hideFromTalkBack) {
                Modifier.hideFromTalkBackWhenEnabled()
            } else {
                Modifier
            }
        ) {
            CandleChartHover(
                isDragging = isDragging,
                xPos = hoverXPosition,
                yPos = pointCandle,
                paddings = PaddingValues(
                    top = biggestValue.height / 2,
                    bottom = biggestValue.height / 2,
                    end = biggestValue.width,
                ),
                topPadding = biggestValue.height / 2,
            )
        }
    }
}

