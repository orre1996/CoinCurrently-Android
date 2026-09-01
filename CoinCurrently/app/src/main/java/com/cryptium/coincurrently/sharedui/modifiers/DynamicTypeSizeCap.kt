package com.cryptium.coincurrently.sharedui.modifiers
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import kotlin.math.min

/**
 * iOS Dynamic Type size caps mapped to Android font scale factors.
 * Ratios use the system body style at default (17pt) as the baseline.
 */
enum class DynamicTypeSizeCap(val fontScaleFactor: Float) {
    /** Default size; matches iOS tab bar and navigation bar chrome. */
    Large(1f),
    XxLarge(21f / 17f),
    XxxLarge(23f / 17f),
    Accessibility1(28f / 17f),
    Accessibility2(33f / 17f),
    Accessibility3(40f / 17f),
}

internal fun cappedFontScale(currentFontScale: Float, maxFontScale: Float): Float =
    min(currentFontScale, maxFontScale)

internal fun Density.withMaxFontScale(maxFontScale: Float): Density =
    Density(
        density = density,
        fontScale = cappedFontScale(fontScale, maxFontScale),
    )

@Composable
fun CappedFontScale(
    maxSize: DynamicTypeSizeCap,
    content: @Composable () -> Unit,
) {
    val density = LocalDensity.current
    val cappedDensity = remember(density.density, density.fontScale, maxSize) {
        density.withMaxFontScale(maxSize.fontScaleFactor)
    }
    CompositionLocalProvider(LocalDensity provides cappedDensity) {
        content()
    }
}
