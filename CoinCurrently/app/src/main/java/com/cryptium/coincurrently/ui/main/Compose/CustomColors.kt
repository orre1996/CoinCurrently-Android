package com.cryptium.coincurrently.ui.main.Compose

import androidx.compose.material.Colors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.glance.unit.ColorProvider
import com.cryptium.coincurrently.ui.main.General.GlobalFunctions

@Composable
private fun Colors.semanticColor(
    standardLight: Long,
    standardDark: Long,
    accessibleLight: Long,
    accessibleDark: Long,
): Color {
    val accessible = LocalColorBlindFilter.current
    val hex = when {
        accessible && isLight -> accessibleLight
        accessible && !isLight -> accessibleDark
        isLight -> standardLight
        else -> standardDark
    }
    return Color(hex)
}

@get:Composable
val Colors.surface: Color
    get() = if (isLight) Color(0xFFE5E2E1) else Color(0xFFF6FEFF)

@get:Composable
val Colors.onSurface: Color
    get() = if (isLight) Color(0xFF1C1B1B) else Color(0xFFFCF8F8)


@get:Composable
val Colors.onSurfaceContainer: Color
    get() = if (isLight) Color(0xFF1C1B1B) else Color(0xFFF6FEFF)

@get:Composable
val Colors.inverseOnContainer: Color
    get() = if (isLight) Color(0xFFF4F0EF) else Color(0xFF1C1B1B)

@get:Composable
val Colors.surfaceContainer: Color
    get() = if (isLight) Color(0xFFEBE8E7) else Color(0xFF262625)

@get:Composable
val Colors.onSurfaceDim: Color
    get() = if (isLight) Color(0xFF484646) else Color(0xFFC9C6C5)

@get:Composable
val Colors.onSurfaceDimmer: Color
    get() = if (isLight) Color(0xFF6C6A6A) else Color(0xFF929090)

@get:Composable
val Colors.onSurfaceDimmest: Color
    get() = if (isLight) Color(0xFF929090) else Color(0xFF605E5E)

@get:Composable
val Colors.primaryContainer: Color
    get() = if (isLight) Color(0xFFFFEEDD) else Color(0xFF372100)

@get:Composable
val Colors.overlayColor: Color
    get() = if (isLight) Color(0x66000000) else Color(0x99000000)

@get:Composable
val Colors.primary90: Color
    get() = if (isLight) Color(0xFF9A6500) else Color(0xFFFFDDB5)

@get:Composable
val Colors.primary50: Color
    get() = if (isLight) Color(0xFFA46B00) else Color(0xFFA46B00)

@get:Composable
val Colors.primary20: Color
    get() = if (isLight) Color(0xFF452B00) else Color(0xFF452B00)


@get:Composable
val Colors.secondary15: Color
    get() = if (isLight) Color(0xFFFFEEDA) else Color(0xFF31230E)

@get:Composable
val Colors.blue50: Color
    get() = if (isLight) Color(0xFF006AC2) else Color(0xFF006AC2)

@get:Composable
val Colors.onSurfaceContainerDim: Color
    get() = if (isLight) Color(0xFF6C6A6A) else Color(0xFFADAAAA)

@get:Composable
val Colors.onSurfaceContainerDimmer: Color
    get() = if (isLight) Color(0xFF6C6A6A) else Color(0xFF929090)

@get:Composable
val Colors.surfaceContainerHigh: Color
    get() = if (isLight) Color(0xFFEDEAE9) else Color(0xFF313030)

@get:Composable
val Colors.surfaceContainerHighest: Color
    get() = if (isLight) Color(0xFFFCF8F8) else Color(0xFF484646)

@get:Composable
val Colors.surfaceContainerLow: Color
    get() = if (isLight) Color(0xFFDEDAD9) else Color(0xFF1C1B1B)

@get:Composable
val Colors.surfaceContainerLowest: Color
    get() = if (isLight) Color(0xFFC9C6C5) else Color(0xFF111111)

@get:Composable
val Colors.outline: Color
    get() = if (isLight) Color(0xFF929090) else Color(0xFF797776)

@get:Composable
val Colors.outlineVariant: Color
    get() = if (isLight) Color(0xFFC9C6C5) else Color(0xFF313030)

@get:Composable
val Colors.extremeFear: Color
    get() = semanticColor(
        standardLight = 0xFFC43435,
        standardDark = 0xFFF95A56,
        accessibleLight = 0xFF882255,
        accessibleDark = 0xFFC77CFF,
    )

@get:Composable
val Colors.fear: Color
    get() = semanticColor(
        standardLight = 0xFFC96800,
        standardDark = 0xFFFB9145,
        accessibleLight = 0xFFD55E00,
        accessibleDark = 0xFFFF9F4A,
    )

@get:Composable
val Colors.neutral: Color
    get() = semanticColor(
        standardLight = 0xFF9A8200,
        standardDark = 0xFFFFDF2E,
        accessibleLight = 0xFF757575,
        accessibleDark = 0xFFB8B8B8,
    )

@get:Composable
val Colors.greed: Color
    get() = semanticColor(
        standardLight = 0xFF4A8C00,
        standardDark = 0xFFA8DF5F,
        accessibleLight = 0xFF009E99,
        accessibleDark = 0xFF4ECDC4,
    )

@get:Composable
val Colors.extremeGreed: Color
    get() = semanticColor(
        standardLight = 0xFF007F52,
        standardDark = 0xFF42E099,
        accessibleLight = 0xFF0072B2,
        accessibleDark = 0xFF56B4E9,
    )

@get:Composable
val Colors.trendUp: Color
    get() = semanticColor(
        standardLight = 0xFF007F52,
        standardDark = 0xFF42E099,
        accessibleLight = 0xFF0072B2,
        accessibleDark = 0xFF56B4E9,
    )

@get:Composable
val Colors.trendDown: Color
    get() = semanticColor(
        standardLight = 0xFFB3272A,
        standardDark = 0xFFF95A56,
        accessibleLight = 0xFFD55E00,
        accessibleDark = 0xFFE69F00,
    )

@get:Composable
val Colors.inverseSurface: Color
    get() = if (isLight) Color(0xFF1C1B1B) else Color(0xFFE5E2E1)

@get:Composable
val Colors.primaryTextColor: Color
    get() = if (isLight) Color(0xFF222A4F) else Color(0xFFEAF3FF)

@get:Composable
val Colors.primaryTextColorInverse: Color
    get() = if (isLight) Color(0xFFFFFFFF) else Color(0xFF36427A)

@get:Composable
val Colors.subtitle: Color
    get() = if (isLight) Color(0xB2222A4F) else Color(0xB2EAF3FF)

@get:Composable
val Colors.cardBackground: Color
    get() = if (isLight) Color(0xFFCED2EC) else Color(0xFF323774)

@get:Composable
val Colors.tabbarAccent: Color
    get() = if (isLight) Color(0xFF2459AC) else Color(0xFF7CA9ED)

@get:Composable
val Colors.cardBorder: Color
    get() = if (isLight) Color(0xFF2459AC) else Color(0xFF7CA9ED)

@get:Composable
val Colors.mainGreen: Color
    get() = semanticColor(
        standardLight = 0xFF2E6737,
        standardDark = 0xFF47BE45,
        accessibleLight = 0xFF0072B2,
        accessibleDark = 0xFF56B4E9,
    )

@get:Composable
val Colors.mainYellow: Color
    get() = if (isLight) Color(0xFF605E00) else Color(0xFFE2E21E)

@get:Composable
val Colors.mainRed: Color
    get() = semanticColor(
        standardLight = 0xFFA2352F,
        standardDark = 0xFFF88684,
        accessibleLight = 0xFFD55E00,
        accessibleDark = 0xFFE69F00,
    )

@get:Composable
val Colors.mainGray: Color
    get() = if (isLight) Color(0xFF7B849D) else Color(0xFF9BA2A5)

@get:Composable
val Colors.overlay: Color
    get() = if (isLight) Color(0x66000000) else Color(0x99000000)

@get:Composable
val Colors.buttonTextColor: Color
    get() = if (isLight) Color(0xFFFFFFFF) else Color(0xFF171B3D)

@get:Composable
val Colors.buttonBackgroundColor: Color
    get() = if (isLight) Color(0xFF437DC8) else Color(0xFFA2BFF6)

@get:Composable
val Colors.pageSelectorSelectedBG: Color
    get() = if (isLight) Color(0xFFE1EEFF) else Color(0xFF171717)

@get:Composable
val Colors.transparent: Color
    get() = Color(0x00000000)

@get:Composable
val Colors.skeletonBackground: Color
    get() = if (isLight) Color(0x403A3A43) else Color(0x40E6E9FF)

@get:Composable
val Colors.popUpColor: Color
    get() = if (isLight) Color(0xFF323774) else Color(0xFFCDCEE1)

/**
 * Same color values as the Compose extensions above, as ARGB Int for use outside Compose
 * (e.g. Glance widgets). Pass isDark = true when the system is in dark mode.
 */
object WidgetColorValues {
    private fun semanticColorProvider(
        standardDay: Long,
        standardNight: Long,
        accessibleDay: Long,
        accessibleNight: Long,
    ): ColorProvider {
        val accessible = GlobalFunctions.getColorBlind()
        return if (accessible) {
            androidx.glance.color.ColorProvider(
                day = Color(accessibleDay),
                night = Color(accessibleNight),
            )
        } else {
            androidx.glance.color.ColorProvider(
                day = Color(standardDay),
                night = Color(standardNight),
            )
        }
    }

    /** Glance ColorProvider for surface (day/night). Matches Colors.surface. */
    fun surface(): ColorProvider = androidx.glance.color.ColorProvider(
        day = Color(0xFFE5E2E1),
        night = Color(0xFFF6FEFF),
    )

    fun onSurface(): ColorProvider = androidx.glance.color.ColorProvider(
        day = Color(0xFF1C1B1B),
        night = Color(0xFFFCF8F8),
    )

    fun onSurfaceDim(): ColorProvider = androidx.glance.color.ColorProvider(
        day = Color(0xFF484646),
        night = Color(0xFFC9C6C5)
    )

    /** onSurfaceDim with 0.5 opacity. */
    fun onSurfaceDimTransparent(): ColorProvider = androidx.glance.color.ColorProvider(
        day = Color(0x80484646),
        night = Color(0x80C9C6C5)
    )

    /** Semi-transparent overlay (e.g. for buy pro overlay). Matches Colors.overlayColor. */
    fun overlayScrim(): ColorProvider = androidx.glance.color.ColorProvider(
        day = Color(0x66000000),
        night = Color(0x99000000),
    )

    /** Sentiment badge background (tertiary-style, ~80% opacity). */
    fun sentimentBadgeBackground(): ColorProvider = androidx.glance.color.ColorProvider(
        day = Color(0xCC525252),
        night = Color(0xCC525252),
    )

    fun trendUp(): ColorProvider = semanticColorProvider(
        standardDay = 0xFF007F52,
        standardNight = 0xFF42E099,
        accessibleDay = 0xFF0072B2,
        accessibleNight = 0xFF56B4E9,
    )

    fun trendDown(): ColorProvider = semanticColorProvider(
        standardDay = 0xFFB3272A,
        standardNight = 0xFFF95A56,
        accessibleDay = 0xFFD55E00,
        accessibleNight = 0xFFE69F00,
    )

    /** Gas fee widget: Standard/Neutral (yellow). */
    fun gasNeutral(): ColorProvider = semanticColorProvider(
        standardDay = 0xFF9A8200,
        standardNight = 0xFFFFDF2E,
        accessibleDay = 0xFF757575,
        accessibleNight = 0xFFB8B8B8,
    )

}