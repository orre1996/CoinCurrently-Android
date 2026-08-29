package com.cryptium.coincurrently.ui.main.General.Components.Loading

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cryptium.coincurrently.ui.main.Compose.conditional

@Composable
fun ActivityIndicator(
    size: Dp = 32.dp,
    fill: Boolean = true,
    color: Color = MaterialTheme.colors.onSurface,
    modifier: Modifier = Modifier
) {
    CircularProgressIndicator(
        color = color,
        modifier = modifier
            .conditional(fill) {
                fillMaxSize()
            }
            .wrapContentSize(Center)
            .size(size)
    )
}