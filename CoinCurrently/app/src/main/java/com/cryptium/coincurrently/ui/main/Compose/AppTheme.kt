package com.cryptium.coincurrently.ui.main.Compose

import android.annotation.SuppressLint
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.runtime.CompositionLocalProvider
import com.cryptium.coincurrently.ui.main.General.GlobalFunctions

val LocalColorBlindFilter = compositionLocalOf { false }

private const val NarrowWidthThresholdDp = 380
private const val NarrowWidthFontScaleFactor = 0.9f

@SuppressLint("ConflictingOnColor")
private val DarkColorPalette = darkColors(
    primary = Color(0xFFEA9A00),
    primaryVariant = Color(0xFFC87F00),
    secondary = Color(0xFF03DAC6),
    secondaryVariant = Color(0xFF03DAC6),
    background = Color(0xFF111111),
    surface = Color(0xFF111111),
    error = Color(0xFFCF6679),
    onPrimary = Color(0xFF1B0E00),
    onSecondary = Color.Black,
    onBackground = Color(0xFFFCF8F8),
    onSurface = Color(0xFFFCF8F8),
    onError = Color.Black,
)

@SuppressLint("ConflictingOnColor")
private val LightColorPalette = lightColors(
    primary = Color(0xFF8F5F00),
    primaryVariant = Color(0xFFC87F00),
    secondary = Color(0xFF018786),
    secondaryVariant = Color(0xFF018786),
    background = Color(0xFFE5E2E1),
    surface = Color(0xFFE5E2E1),
    error = Color(0xFFB00020),
    onPrimary = Color(0xFFFFFFFF),
    onSecondary = Color.White,
    onBackground = Color(0xFF1C1B1B),
    onSurface = Color(0xFF1C1B1B),
    onError = Color.White,
)

@Composable
fun AppTheme(
    darkTheme: Boolean,
    colorBlindFilter: Boolean = GlobalFunctions.getColorBlind(),
    content: @Composable () -> Unit,
) {
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val narrowWidth = configuration.screenWidthDp <= NarrowWidthThresholdDp
    val adjustedDensity = remember(density.density, density.fontScale, narrowWidth) {
        val scaledFontScale = density.fontScale *
            if (narrowWidth) NarrowWidthFontScaleFactor else 1f
        Density(
            density = density.density,
            fontScale = cappedFontScale(
                scaledFontScale,
                DynamicTypeSizeCap.Accessibility3.fontScaleFactor,
            ),
        )
    }
    val accessibilityLayout = remember(adjustedDensity.fontScale, configuration) {
        accessibilityLayoutState(
            fontScale = adjustedDensity.fontScale,
            configuration = configuration,
        )
    }
    CompositionLocalProvider(
        LocalDensity provides adjustedDensity,
        LocalAccessibilityLayout provides accessibilityLayout,
        LocalColorBlindFilter provides colorBlindFilter,
    ) {
        MaterialTheme(
            colors = if (darkTheme) DarkColorPalette else LightColorPalette,
            content = content
        )
    }
}
