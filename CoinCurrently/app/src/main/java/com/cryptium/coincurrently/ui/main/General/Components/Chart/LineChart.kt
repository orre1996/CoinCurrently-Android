package com.cryptium.coincurrently.ui.main.General.Components.Chart

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.cryptium.coincurrently.ui.main.Compose.onSurfaceDim
import com.cryptium.coincurrently.ui.main.Extensions.normalizeCustom
import kotlin.math.max

private const val CHART_LINE_WIDTH_DP = 2f

private enum class ChartGradientMode {
    Single,
    SingleHover,
    MultiHover,
}

private fun pulsingDotDiameter(lineWidthDp: Float = CHART_LINE_WIDTH_DP): Float {
    return max(3.5f, lineWidthDp * 3.5f)
}

private fun clampPosition(position: Float): Float = position.coerceIn(0f, 1f)

private fun resolveGradientMode(
    isDragging: Boolean,
    dragPositionCount: Int,
): ChartGradientMode {
    if (!isDragging) return ChartGradientMode.Single
    return when (dragPositionCount) {
        2 -> ChartGradientMode.MultiHover
        1 -> ChartGradientMode.SingleHover
        else -> ChartGradientMode.Single
    }
}

private fun lineGradientBrush(
    width: Float,
    color: Color,
    dimColor: Color,
    firstPosition: Float,
    lastPosition: Float,
    mode: ChartGradientMode,
): Brush {
    val first = clampPosition(firstPosition)
    val last = clampPosition(lastPosition)
    val fadedLineColor = color.copy(alpha = 0.15f)
    val dimLineColor = dimColor.copy(alpha = 0.2f)

    return when (mode) {
        ChartGradientMode.Single -> Brush.horizontalGradient(
            colorStops = arrayOf(0f to color, 1f to color),
            startX = 0f,
            endX = width,
        )

        ChartGradientMode.SingleHover -> Brush.horizontalGradient(
            colorStops = arrayOf(
                0f to color,
                first to color,
                last to fadedLineColor,
                1f to fadedLineColor,
            ),
            startX = 0f,
            endX = width,
        )

        ChartGradientMode.MultiHover -> Brush.horizontalGradient(
            colorStops = arrayOf(
                0f to dimLineColor,
                first to dimLineColor,
                first to color,
                last to color,
                last to dimLineColor,
                1f to dimLineColor,
            ),
            startX = 0f,
            endX = width,
        )
    }
}

private fun fillGradientBrush(
    width: Float,
    color: Color,
    dimColor: Color,
    firstPosition: Float,
    lastPosition: Float,
    mode: ChartGradientMode,
): Brush {
    val first = clampPosition(firstPosition)
    val last = clampPosition(lastPosition)
    val dimFillColor = dimColor.copy(alpha = 0.25f)

    return if (mode == ChartGradientMode.MultiHover) {
        Brush.horizontalGradient(
            colorStops = arrayOf(
                0f to dimFillColor,
                first to dimFillColor,
                first to color,
                last to color,
                last to dimFillColor,
                1f to dimFillColor,
            ),
            startX = 0f,
            endX = width,
        )
    } else {
        Brush.horizontalGradient(
            colorStops = arrayOf(0f to color, 1f to color),
            startX = 0f,
            endX = width,
        )
    }
}

private fun DrawScope.drawFillWithVerticalMask(path: Path, brush: Brush) {
    drawIntoCanvas { canvas ->
        val checkpoint = canvas.nativeCanvas.saveLayer(null, null)
        drawPath(path, brush, style = Fill)
        drawRect(
            topLeft = Offset.Zero,
            size = size,
            brush = Brush.verticalGradient(
                colorStops = arrayOf(
                    0f to Color.White.copy(alpha = 0.3f),
                    1f to Color.White.copy(alpha = 0.05f),
                ),
            ),
            blendMode = BlendMode.DstIn,
        )
        canvas.nativeCanvas.restoreToCount(checkpoint)
    }
}

@Composable
fun LineChart(
    modifier: Modifier = Modifier,
    prices: List<Double>,
    color: Color,
    dragIndex: Int?,
    showFill: Boolean = true,
    paddings: PaddingValues? = null,
    dropCount: Int = 0,
    isEmptyAtBottom: Boolean = false,
    isDragging: Boolean = false,
    showLivePriceIndicator: Boolean = false,
    dragPositionXs: List<Float> = emptyList(),
    dash: FloatArray = floatArrayOf(),
    normalizationMin: Double? = null,
    normalizationMax: Double? = null,
) {
    val dimColor = MaterialTheme.colors.onSurfaceDim

    val realPrices = remember(prices, dropCount, isEmptyAtBottom, normalizationMin, normalizationMax) {
        if (prices.isEmpty()) {
            if (isEmptyAtBottom) listOf(0f, 0f) else listOf(0.5f, 0.5f)
        } else {
            var allSame = true
            var min = normalizationMin ?: Double.MAX_VALUE
            var max = normalizationMax ?: Double.MIN_VALUE
            // Ignore leading -1 padding from compare charts (common on Max).
            val first = prices.firstOrNull { it != -1.0 } ?: prices.first()

            if (normalizationMin == null || normalizationMax == null) {
                for (price in prices) {
                    if (price != -1.0) {
                        if (price != first) allSame = false
                        if (price < min) min = price
                        if (price > max) max = price
                    }
                }
            } else {
                for (price in prices) {
                    if (price != -1.0 && price != first) {
                        allSame = false
                    }
                }
            }

            if (allSame || min == Double.MAX_VALUE) {
                if (isEmptyAtBottom) listOf(0f, 0f) else listOf(0.5f, 0.5f)
            } else {
                prices.normalizeCustom(max = max, min = min).dropLast(dropCount)
            }
        }
    }

    val firstValidIndex = remember(prices) {
        prices.indexOfFirst { it != -1.0 }.coerceAtLeast(0)
    }

    val lineStrokePath = remember { Path() }
    val fillStrokePath = remember { Path() }

    val lastPoint = remember(realPrices) {
        if (realPrices.size <= 1) {
            null
        } else {
            val lastIndex = realPrices.indexOfLast { it != -1f }
            if (lastIndex < 0) {
                null
            } else {
                val x = lastIndex.toFloat() / (realPrices.size - 1).coerceAtLeast(1)
                val y = 1f - realPrices[lastIndex]
                Pair(x, y)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .padding(paddings ?: PaddingValues())
                .clipToBounds()
        ) {
            Canvas(modifier = modifier.fillMaxSize()) {
                val priceCount = realPrices.size
                if (priceCount < 2) return@Canvas

                val widthStep = size.width / (priceCount - 1)
                val canvasHeight = size.height
                val strokeWidth = 2.dp.toPx()
                val dashPathEffect = if (dash.isEmpty()) {
                    null
                } else {
                    PathEffect.dashPathEffect(
                        FloatArray(dash.size) { index -> dash[index].dp.toPx() },
                        0f,
                    )
                }
                val useHoverGradient = dragPositionXs.isNotEmpty() && isDragging
                val gradientMode = resolveGradientMode(isDragging, dragPositionXs.size)
                val firstPosition = (dragPositionXs.firstOrNull() ?: 0f) / size.width
                val lastPosition = (dragPositionXs.lastOrNull() ?: 0f) / size.width
                val lineRange = when {
                    useHoverGradient -> priceCount
                    dragIndex == null || !showFill -> priceCount
                    else -> dragIndex + 1
                }

                lineStrokePath.reset()
                var isFirst = true
                for (i in firstValidIndex until lineRange) {
                    val currentX = widthStep * i
                    val currentY = (1f - (realPrices.getOrNull(i) ?: 0f)) * canvasHeight

                    if (isFirst) {
                        lineStrokePath.moveTo(currentX, currentY)
                        isFirst = false
                    } else {
                        lineStrokePath.lineTo(currentX, currentY)
                    }
                }

                val lineBrush = lineGradientBrush(
                    width = size.width,
                    color = color,
                    dimColor = dimColor,
                    firstPosition = firstPosition,
                    lastPosition = lastPosition,
                    mode = if (useHoverGradient) gradientMode else ChartGradientMode.Single,
                )

                drawPath(
                    path = lineStrokePath,
                    brush = lineBrush,
                    style = Stroke(
                        width = strokeWidth,
                        cap = if (dashPathEffect != null) StrokeCap.Round else StrokeCap.Butt,
                        join = StrokeJoin.Round,
                        pathEffect = dashPathEffect,
                    ),
                )

                if (showFill) {
                    fillStrokePath.reset()

                    for (i in 0 until priceCount) {
                        val currentX = widthStep * i
                        val currentY = (1f - realPrices[i]) * canvasHeight

                        if (i == 0) {
                            fillStrokePath.moveTo(currentX, currentY)
                        } else {
                            fillStrokePath.lineTo(currentX, currentY)
                        }
                    }

                    fillStrokePath.lineTo(size.width, canvasHeight)
                    fillStrokePath.lineTo(0f, canvasHeight)
                    fillStrokePath.close()

                    val                     fillBrush = fillGradientBrush(
                        width = size.width,
                        color = color,
                        dimColor = dimColor,
                        firstPosition = firstPosition,
                        lastPosition = lastPosition,
                        mode = if (useHoverGradient && showFill) {
                            gradientMode
                        } else {
                            ChartGradientMode.Single
                        },
                    )

                    drawFillWithVerticalMask(fillStrokePath, fillBrush)
                }
            }
        }

        if (showLivePriceIndicator && !isDragging) {
            lastPoint?.let { (x, y) ->
                PulsingDot(
                    color = color,
                    modifier = Modifier.matchParentSize(),
                    normalizedX = x,
                    normalizedY = y,
                    paddings = paddings ?: PaddingValues(),
                    diameter = pulsingDotDiameter(),
                )
            }
        }
    }
}

@Composable
private fun PulsingDot(
    color: Color,
    modifier: Modifier = Modifier,
    normalizedX: Float,
    normalizedY: Float,
    paddings: PaddingValues,
    diameter: Float,
) {
    val transition = rememberInfiniteTransition(label = "livePricePulse")
    val pulseScale = transition.animateFloat(
        initialValue = 1f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "pulseScale",
    )
    val pulseAlpha = transition.animateFloat(
        initialValue = 0.75f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "pulseAlpha",
    )

    Canvas(
        modifier = modifier.graphicsLayer { clip = false },
    ) {
        val plotLeft = paddings.calculateLeftPadding(layoutDirection).toPx()
        val plotTop = paddings.calculateTopPadding().toPx()
        val plotRight = size.width - paddings.calculateRightPadding(layoutDirection).toPx()
        val plotBottom = size.height - paddings.calculateBottomPadding().toPx()
        val plotWidth = (plotRight - plotLeft).coerceAtLeast(1f)
        val plotHeight = (plotBottom - plotTop).coerceAtLeast(1f)

        val x = plotLeft + normalizedX * plotWidth
        val y = plotTop + normalizedY * plotHeight
        val radius = diameter.dp.toPx() / 2f

        drawCircle(
            color = color.copy(alpha = pulseAlpha.value),
            radius = radius * pulseScale.value,
            center = Offset(x, y),
        )
        drawCircle(
            color = color,
            radius = radius,
            center = Offset(x, y),
        )
    }
}
