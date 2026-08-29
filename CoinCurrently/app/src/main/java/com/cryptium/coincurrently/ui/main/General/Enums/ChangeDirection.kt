package com.cryptium.coincurrently.ui.main.General.Enums

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.cryptium.coincurrently.ui.main.Compose.trendDown
import com.cryptium.coincurrently.ui.main.Compose.trendUp

enum class ChangeDirection(val value: String) {
    Up("up"),
    Down("down");

    val symbol: String
        get() = when (this) {
            Up -> "+%"
            Down -> "-%"
        }

    val color: Color @Composable
        get() = when (this) {
            Up -> MaterialTheme.colors.trendUp
            Down -> MaterialTheme.colors.trendDown
        }

    fun operation(): (Double, Double) -> Double {
        return if (this == Up) { a, b -> a + b } else { a, b -> a - b }
    }
}