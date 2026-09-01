package com.cryptium.coincurrently.core.extensions
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import com.cryptium.coincurrently.sharedui.theme.extremeFear
import com.cryptium.coincurrently.sharedui.theme.extremeGreed
import com.cryptium.coincurrently.sharedui.theme.fear
import com.cryptium.coincurrently.sharedui.theme.greed
import com.cryptium.coincurrently.sharedui.theme.neutral
import com.cryptium.coincurrently.sharedui.theme.onSurfaceDim
import com.cryptium.coincurrently.features.Portfolio.Models.TransferType
import com.cryptium.coincurrently.app.NavigationScreens

val Int.trustColor: Color @Composable
    get() = when (this) {
        10, 9 -> MaterialTheme.colors.extremeGreed
        7, 8 -> MaterialTheme.colors.greed
        5, 6 -> MaterialTheme.colors.neutral
        3, 4 -> MaterialTheme.colors.fear
        1, 2 -> MaterialTheme.colors.extremeFear
        else -> MaterialTheme.colors.onSurfaceDim
    }

val Int.selectedTabbarItem
    get() = run {
        when (this) {
            0 -> NavigationScreens.Home
            1 -> NavigationScreens.Portfolio
            2 -> NavigationScreens.Market
            3 -> NavigationScreens.News
            else -> NavigationScreens.Settings
        }
    }

val Int.getTransferType: TransferType
    get() {
        return when (this) {
            0 -> { TransferType.OUTTYPE }
            else ->{ TransferType.INTYPE }

        }
    }

@Composable
fun Dp.dpToPx() = with(LocalDensity.current) { this@dpToPx.toPx() }

@Composable
fun Int.pxToDp() = with(LocalDensity.current) { this@pxToDp.toDp() }
