package com.cryptium.coincurrently.widgets.receivers

import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import com.cryptium.coincurrently.core.persistence.UserPreferences

fun isWidgetDarkMode(context: Context): Boolean {
    return (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_YES) != 0
}

private fun widgetColor(context: Context, darkHex: String, lightHex: String): Int {
    return Color.parseColor(if (isWidgetDarkMode(context)) darkHex else lightHex)
}

private fun widgetSemanticColor(
    context: Context,
    standardDarkHex: String,
    standardLightHex: String,
    accessibleDarkHex: String,
    accessibleLightHex: String,
): Int {
    val isDark = isWidgetDarkMode(context)
    val accessible = UserPreferences.getColorBlind()
    return when {
        accessible && isDark -> Color.parseColor(accessibleDarkHex)
        accessible && !isDark -> Color.parseColor(accessibleLightHex)
        isDark -> Color.parseColor(standardDarkHex)
        else -> Color.parseColor(standardLightHex)
    }
}

fun widgetOnSurface(context: Context): Int = widgetColor(context, "#FCF8F8", "#1C1B1B")

fun widgetOnSurfaceDim(context: Context): Int = widgetColor(context, "#C9C6C5", "#484646")

fun widgetOnSurfaceDimmer(context: Context): Int = widgetColor(context, "#929090", "#6C6A6A")

fun widgetOnPrimary(context: Context): Int = widgetColor(context, "#1B0E00", "#FFFFFF")

fun widgetTrendUp(context: Context): Int = widgetSemanticColor(
    context,
    standardDarkHex = "#42E099",
    standardLightHex = "#007F52",
    accessibleDarkHex = "#56B4E9",
    accessibleLightHex = "#0072B2",
)

fun widgetTrendDown(context: Context): Int = widgetSemanticColor(
    context,
    standardDarkHex = "#F95A56",
    standardLightHex = "#B3272A",
    accessibleDarkHex = "#E69F00",
    accessibleLightHex = "#D55E00",
)

fun widgetExtremeFear(context: Context): Int = widgetSemanticColor(
    context,
    standardDarkHex = "#F95A56",
    standardLightHex = "#C43435",
    accessibleDarkHex = "#C77CFF",
    accessibleLightHex = "#882255",
)

fun widgetFear(context: Context): Int = widgetSemanticColor(
    context,
    standardDarkHex = "#FB9145",
    standardLightHex = "#C96800",
    accessibleDarkHex = "#FF9F4A",
    accessibleLightHex = "#D55E00",
)

fun widgetNeutral(context: Context): Int = widgetSemanticColor(
    context,
    standardDarkHex = "#FFDF2E",
    standardLightHex = "#9A8200",
    accessibleDarkHex = "#B8B8B8",
    accessibleLightHex = "#757575",
)

fun widgetGreed(context: Context): Int = widgetSemanticColor(
    context,
    standardDarkHex = "#A8DF5F",
    standardLightHex = "#4A8C00",
    accessibleDarkHex = "#4ECDC4",
    accessibleLightHex = "#009E99",
)

fun widgetExtremeGreed(context: Context): Int = widgetTrendUp(context)

fun widgetOutline(context: Context): Int = widgetColor(context, "#797776", "#929090")

fun widgetOutlineVariant(context: Context): Int = widgetColor(context, "#313030", "#C9C6C5")

fun widgetSurfaceContainerHighest(context: Context): Int = widgetColor(context, "#484646", "#FCF8F8")
