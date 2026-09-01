package com.cryptium.coincurrently.sharedui.components
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cryptium.coincurrently.sharedui.theme.surfaceContainerLowest
import com.cryptium.coincurrently.core.extensions.pxToDp

@Composable
fun CustomProgressBarView(
    progress: Double?,
    offset: Double = 0.0,
    foregroundColor: Color,
    backgroundColor: Color = MaterialTheme.colors.surfaceContainerLowest,
    height: Dp = 8.dp,
    capsuleShaped: Boolean = true,
    downloading: Boolean = false,
    modifier: Modifier = Modifier
) {
    var progressBarWidth by remember { mutableIntStateOf(1) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .background(backgroundColor, shape = if (capsuleShaped) CircleShape else RectangleShape)
            .onSizeChanged { size ->
                progressBarWidth = size.width
            }
    ) {
        Box(
            modifier = Modifier
                .padding(start = (offset * progressBarWidth).pxToDp())
                .width(((progress ?: 0.0) * progressBarWidth).pxToDp().coerceAtMost(progressBarWidth.pxToDp()))
                .fillMaxHeight()
                .background(foregroundColor, shape = if (capsuleShaped) CircleShape else RectangleShape)
        )
    }
}
