package com.cryptium.coincurrently.ui.main.General.Components.Chart

import androidx.compose.foundation.layout.Box
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.cryptium.coincurrently.ui.main.Compose.outline
import com.cryptium.coincurrently.ui.main.Extensions.dpToPx
import com.cryptium.coincurrently.ui.main.General.Utilities.Point

@Composable
fun ChartHoverView(
    positions: List<Point>,
    color: Color,
    isDragging: Boolean,
    topPadding: Dp = 0.dp,
    statsAreaHeight: Dp = 0.dp,
    fullHeightLine: Boolean = false,
) {
    val lineTopOffset = if (fullHeightLine) {
        0.dp
    } else {
        -(topPadding + statsAreaHeight)
    }
    val lineTopInset = if (fullHeightLine) {
        statsAreaHeight
    } else {
        0.dp
    }
    val lineBottomInset = if (fullHeightLine) {
        topPadding
    } else {
        0.dp
    }

    Box(
        modifier = Modifier
            .zIndex(0f)
            .alpha(
            if (isDragging) {
                1f
            } else {
                0f
            },
        ),
    ) {
        positions.forEach { position ->
            VerticalDottedLine(
                xPosition = position.x.dpToPx(),
                yOffset = lineTopOffset,
                lineTopInset = lineTopInset,
                lineBottomInset = lineBottomInset,
                color = MaterialTheme.colors.outline.copy(0.25f),
            )
        }

        positions.forEach { position ->
            HoverCircle(
                position = position,
                color = color,
                topPadding = topPadding,
                statsAreaHeight = statsAreaHeight,
            )
        }
    }
}
