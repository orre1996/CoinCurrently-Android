package com.cryptium.coincurrently.sharedui.charts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cryptium.coincurrently.sharedui.theme.outline
import com.cryptium.coincurrently.sharedui.theme.primaryTextColor
import com.cryptium.coincurrently.core.extensions.dpToPx
import com.cryptium.coincurrently.sharedui.charts.HorizontalDottedLine
import com.cryptium.coincurrently.sharedui.charts.VerticalDottedLine

@Composable
fun CandleChartHover(
    isDragging: Boolean,
    xPos: Float,
    yPos: Dp,
    paddings: PaddingValues? = null,
    topPadding: Dp,
) {
    val color = MaterialTheme.colors.primaryTextColor
    val valueOpacity: Float by animateFloatAsState(
        if (isDragging) {
            1f
        } else {
            0f
        }, label = ""
    )

    val yPos = yPos.dpToPx()

    Box(
        modifier = Modifier
        .padding(paddings ?: PaddingValues())
        .fillMaxSize()
        .alpha(valueOpacity)
    ) {
        VerticalDottedLine(
            xPosition = xPos,
            yOffset = -topPadding,
            color = MaterialTheme.colors.outline.copy(0.25f),
        )

        HorizontalDottedLine(
            yPosition = yPos,
            yOffset = -topPadding,
            color = MaterialTheme.colors.outline.copy(0.25f),
        )
    }
}