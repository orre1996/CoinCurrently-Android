package com.cryptium.coincurrently.core.extensions
import androidx.compose.ui.unit.sp
import androidx.glance.text.FontWeight
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider

/**
 * Glance equivalents of the satoshi TextStyles (see Fonts+Extensions.kt).
 * Widgets run in RemoteViews and cannot use custom fonts; these use system font
 * with matching weight/style and the same fontSize so layout stays consistent.
 */
fun satoshiBoldGlance(size: Int, color: ColorProvider): TextStyle = TextStyle(
    fontSize = size.sp,
    fontWeight = FontWeight.Bold,
    color = color
)

fun satoshiMediumGlance(size: Int, color: ColorProvider): TextStyle = TextStyle(
    fontSize = size.sp,
    fontWeight = FontWeight.Medium,
    color = color
)

fun satoshiRegularGlance(size: Int, color: ColorProvider): TextStyle = TextStyle(
    fontSize = size.sp,
    fontWeight = FontWeight.Normal,
    color = color
)
