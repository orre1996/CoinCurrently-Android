package com.cryptium.coincurrently.ui.main.Compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun rememberDragHandlerHeight(
    topPadding: Dp = 16.dp,
    bottomPadding: Dp = 12.dp,
): Dp {
    val density = LocalDensity.current
    val measuredHeightPx = remember(topPadding, bottomPadding) { mutableIntStateOf(0) }
    val measureWidthPx = contentScreenWidthPx()

    Box(modifier = Modifier.size(0.dp)) {
        SubcomposeLayout { _ ->
            val placeable = subcompose("dragHandler") {
                DragHandler(
                    modifier = Modifier.padding(
                        top = topPadding,
                        bottom = bottomPadding,
                    ),
                )
            }.first().measure(
                Constraints(
                    minWidth = measureWidthPx,
                    maxWidth = measureWidthPx,
                    minHeight = 0,
                    maxHeight = Constraints.Infinity,
                ),
            )
            measuredHeightPx.intValue = placeable.height
            layout(0, 0) {}
        }
    }

    return if (measuredHeightPx.intValue > 0) {
        with(density) { measuredHeightPx.intValue.toDp() }
    } else {
        topPadding + 4.dp + bottomPadding
    }
}

@Composable
fun DragHandler(
    modifier: Modifier = Modifier
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(width = 32.dp, height = 4.dp)
                .clip(RoundedCornerShape(percent = 50))
                .background(MaterialTheme.colors.outline)
        )
    }
}