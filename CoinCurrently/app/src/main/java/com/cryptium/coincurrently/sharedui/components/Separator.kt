package com.cryptium.coincurrently.sharedui.components
import androidx.compose.foundation.layout.*
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.cryptium.coincurrently.sharedui.theme.outlineVariant

@Composable
fun Separator(
    padding: PaddingValues = PaddingValues(0.dp),
    forceRedraw: Boolean = false,
    alpha: Float = 1f
) {
    Divider(
        color = MaterialTheme.colors.outlineVariant,
        modifier = Modifier
            .padding(padding)
            .height(1.dp)
            .fillMaxWidth()
            .zIndex(if(forceRedraw) 1f else 1f)
            .alpha(alpha)
    )
}

@Composable
fun SeparatorVertical(
    padding: PaddingValues = PaddingValues(0.dp),
    height: Modifier = Modifier,
    forceRedraw: Boolean = false,
    alpha: Float = 1f,
    width: Dp = 1.dp,
    color: Color = MaterialTheme.colors.outlineVariant
) {
    Divider(
        color = color,
        modifier = height
            .padding(padding)
            .width(width)
            .fillMaxHeight()
            .zIndex(if(forceRedraw) 1f else 1f)
            .alpha(alpha)
    )
}
