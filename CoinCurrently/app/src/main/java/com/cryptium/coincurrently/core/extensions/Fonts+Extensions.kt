package com.cryptium.coincurrently.core.extensions
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.sp
import com.cryptium.coincurrently.R

val satoshiBlack = FontFamily(Font(R.font.satoshi_black))
val satoshiBold = FontFamily(Font(R.font.satoshi_bold))
val satoshiMedium = FontFamily(Font(R.font.satoshi_medium))
val satoshiRegular = FontFamily(Font(R.font.satoshi_regular))
val satoshiLight = FontFamily(Font(R.font.satoshi_light))
val satoshiItalic = FontFamily(Font(R.font.satoshi_italic))

fun satoshiBlack(size: Int): TextStyle {
    return TextStyle(
        fontFamily = satoshiBlack,
        fontSize = size.sp,
        platformStyle = PlatformTextStyle(
            includeFontPadding = false
        )
    )
}

fun satoshiBold(size: Int): TextStyle {
    return TextStyle(
        fontFamily = satoshiBold,
        fontSize = size.sp,
        platformStyle = PlatformTextStyle(
            includeFontPadding = false
        )
    )
}

fun satoshiMedium(size: Int): TextStyle {
    return TextStyle(
        fontFamily = satoshiMedium,
        fontSize = size.sp,
        platformStyle = PlatformTextStyle(
            includeFontPadding = false
        )
    )
}

fun satoshiRegular(size: Int): TextStyle {
    return TextStyle(
        fontFamily = satoshiRegular,
        fontSize = size.sp,
        platformStyle = PlatformTextStyle(
            includeFontPadding = false
        )
    )
}

fun satoshiLight(size: Int): TextStyle {
    return TextStyle(
        fontFamily = satoshiLight,
        fontSize = size.sp,
        platformStyle = PlatformTextStyle(
            includeFontPadding = false
        )
    )
}

fun satoshiItalic(size: Int): TextStyle {
    return TextStyle(
        fontFamily = satoshiItalic,
        fontSize = size.sp,
        platformStyle = PlatformTextStyle(
            includeFontPadding = false
        )
    )
}

val poppinsLight = FontFamily(Font(R.font.poppins_light))
val poppinsRegular = FontFamily(Font(R.font.poppins_regular))
val poppinsSemibold = FontFamily(Font(R.font.poppins_semibold))

fun poppinsLight(size: Int): TextStyle {
    return TextStyle(
        fontFamily = poppinsLight,
        fontSize = size.sp,
        platformStyle = PlatformTextStyle(
            includeFontPadding = false
        )
    )
}

fun poppinsRegular(size: Int): TextStyle {
    return TextStyle(
        fontFamily = poppinsRegular,
        fontSize = size.sp,
        platformStyle = PlatformTextStyle(
            includeFontPadding = false
        )
    )
}

fun poppinsSemibold(size: Int): TextStyle {
    return TextStyle(
        fontFamily = poppinsSemibold,
        fontSize = size.sp,
        platformStyle = PlatformTextStyle(
            includeFontPadding = false
        )
    )
}
