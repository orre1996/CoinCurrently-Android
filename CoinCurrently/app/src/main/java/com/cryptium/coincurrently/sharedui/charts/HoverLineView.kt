package com.cryptium.coincurrently.sharedui.charts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cryptium.coincurrently.sharedui.theme.outline
import com.cryptium.coincurrently.core.extensions.dpToPx
import com.cryptium.coincurrently.sharedui.components.Point

@Composable
fun HoverLineView(
    position: Point,
    color: Color,
    isDragging: Boolean,
    topPadding: Dp = 0.dp
) {
    Box(
        modifier = Modifier
            .alpha(
                if (isDragging) {
                    1f
                } else {
                    0f
                }
            )
    ) {
        val xPosition = position.x.dpToPx()

        VerticalDottedLine(
            xPosition = xPosition,
            yOffset = -topPadding,
            color = MaterialTheme.colors.outline.copy(0.25f),
        )

        HoverCircle(
            position = position,
            color = color,
            topPadding = topPadding,
        )
    }
}

@Composable
fun VerticalDottedLine(
    xPosition: Float,
    yOffset: Dp,
    color: Color,
    lineTopInset: Dp = 0.dp,
    lineBottomInset: Dp = 0.dp,
) {
    Canvas(
        modifier = Modifier
            .fillMaxHeight()
            .padding(top = lineTopInset, bottom = lineBottomInset)
            .offset(y = yOffset)
    ) {
        val pathEffect = PathEffect.dashPathEffect(
            intervals = floatArrayOf(4.5.dp.toPx(), 5.dp.toPx()),
            phase = 0f
        )

        drawLine(
            color = color.copy(0.4f),
            start = Offset(xPosition, 0f),
            end = Offset(xPosition, size.height),
            pathEffect = pathEffect,
            strokeWidth = 2.dp.toPx()
        )
    }
}

@Composable
fun HorizontalDottedLine(
    yPosition: Float,
    yOffset: Dp,
    color: Color
) {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .offset(y = yOffset)
    ) {
        val pathEffect = PathEffect.dashPathEffect(
            intervals = floatArrayOf(4.5.dp.toPx(), 5.dp.toPx()),
            phase = 0f
        )

        drawLine(
            color = color.copy(0.4f),
            start = Offset(0f, yPosition),
            end = Offset(size.width, yPosition),
            pathEffect = pathEffect,
            strokeWidth = 2.dp.toPx()
        )
    }
}

@Composable
fun HoverCircle(
    position: Point,
    color: Color,
    topPadding: Dp,
    statsAreaHeight: Dp = 0.dp,
) {
    Canvas(
        modifier = Modifier
            .offset(x = position.x, y = statsAreaHeight + position.y + topPadding)
    ) {
        drawCircle(
            color = color.copy(0.25f),
            34.dp.value
        )

        drawCircle(
            color = color,
            10.dp.value
        )
    }
}