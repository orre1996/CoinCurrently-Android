package com.cryptium.coincurrently.core.extensions
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.sp
import com.cryptium.coincurrently.R

val satoshiBold = FontFamily(Font(R.font.satoshi_bold))
val satoshiMedium = FontFamily(Font(R.font.satoshi_medium))
val satoshiRegular = FontFamily(Font(R.font.satoshi_regular))

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

val poppinsRegular = FontFamily(Font(R.font.poppins_regular))
val poppinsSemibold = FontFamily(Font(R.font.poppins_semibold))

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
