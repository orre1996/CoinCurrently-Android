package com.cryptium.coincurrently.features.Home.Views
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import com.cryptium.coincurrently.sharedui.theme.extremeFear
import com.cryptium.coincurrently.sharedui.theme.extremeGreed
import com.cryptium.coincurrently.sharedui.theme.fear
import com.cryptium.coincurrently.sharedui.theme.greed
import com.cryptium.coincurrently.sharedui.theme.neutral

@Composable
fun FearGreedMeter(
    height: Dp,
    modifier: Modifier = Modifier
) {
    val colors = listOf(
        MaterialTheme.colors.extremeFear,
        MaterialTheme.colors.fear,
        MaterialTheme.colors.neutral,
        MaterialTheme.colors.greed,
        MaterialTheme.colors.extremeGreed
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(CircleShape)
    ) {
        colors.forEach {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(height)
                    .background(it)
            )
        }
    }
}