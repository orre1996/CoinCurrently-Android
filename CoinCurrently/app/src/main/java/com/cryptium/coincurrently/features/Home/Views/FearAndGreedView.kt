package com.cryptium.coincurrently.features.Home.Views
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.cryptium.coincurrently.sharedui.theme.extremeFear
import com.cryptium.coincurrently.sharedui.theme.extremeGreed
import com.cryptium.coincurrently.sharedui.theme.fear
import com.cryptium.coincurrently.sharedui.theme.greed
import com.cryptium.coincurrently.sharedui.theme.neutral

@Composable
fun getFearGreedColorCompose(value: Int): Color {
    return if (value < 20) {
        MaterialTheme.colors.extremeFear
    } else if (value < 40) {
        MaterialTheme.colors.fear
    } else if (value < 60) {
        MaterialTheme.colors.neutral
    } else if (value < 80) {
        MaterialTheme.colors.greed
    } else {
        MaterialTheme.colors.extremeGreed
    }
}

fun getFearGreedClarification(value: Int): String {
    return if (value < 20) {
        "Extreme Fear"
    } else if (value < 40) {
        "Fear"
    } else if (value < 60) {
        "Neutral"
    } else if (value < 80) {
        "Greed"
    } else {
        "Extreme Greed"
    }
}
