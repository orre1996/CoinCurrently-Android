package com.cryptium.coincurrently.ui.main.CustomViews

import android.content.Context
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.cryptium.coincurrently.AppLocale
import com.cryptium.coincurrently.R
import com.cryptium.coincurrently.ui.main.Compose.mainGray
import com.cryptium.coincurrently.ui.main.Compose.mainGreen
import com.cryptium.coincurrently.ui.main.Compose.mainRed
import com.cryptium.coincurrently.ui.main.Compose.onSurfaceDim
import com.cryptium.coincurrently.ui.main.Compose.trendDown
import com.cryptium.coincurrently.ui.main.Compose.trendUp
import com.cryptium.coincurrently.ui.main.General.GlobalFunctions
import com.cryptium.coincurrently.ui.main.Widgets.widgetOnSurfaceDim
import com.cryptium.coincurrently.ui.main.Widgets.widgetTrendUp
import com.cryptium.coincurrently.ui.main.Widgets.widgetTrendDown
import networking.ResponseModels.CurrencySelectionModel
import java.math.BigDecimal
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.NumberFormat
import kotlin.math.pow
import kotlin.math.round

fun Long.getFormattedCurrencyString(withSymbol: Boolean = true, context: Context, currencySelectionModel: CurrencySelectionModel? = null, maximumDigits: Int = 7): String? {
    val currency = currencySelectionModel ?: (GlobalFunctions.getSelectedCurrency(context) ?: CurrencySelectionModel())

    val locale = currency.formattingLocale
    val symbol = DecimalFormatSymbols(locale)
    symbol.currencySymbol = currency.symbol

    val format = NumberFormat.getCurrencyInstance(locale) as DecimalFormat
    format.decimalFormatSymbols = symbol
    format.minimumFractionDigits = 2
    format.maximumFractionDigits = if (kotlin.math.abs(this) < 1) maximumDigits else 2

    return if ( !withSymbol ) {
        format.format(this).replace(currency.symbol, "").trim()
    } else {
        format.format(this)
    }
}

fun Number.getFormattedCurrencyString(withSymbol: Boolean = true, context: Context? = null, currencySelectionModel: CurrencySelectionModel? = null, maximumDigits: Int = 7, minimumDigits: Int = 2): String? {
    val currency = currencySelectionModel ?: (GlobalFunctions.getSelectedCurrency(context))

    val locale = currency.formattingLocale
    val symbol = DecimalFormatSymbols(locale)
    symbol.currencySymbol = currency.symbol

    val format = NumberFormat.getCurrencyInstance(locale) as DecimalFormat
    format.decimalFormatSymbols = symbol
    format.minimumFractionDigits = if(this == BigDecimal("0.0")) 2 else minimumDigits
    format.maximumFractionDigits = if(this == BigDecimal("0.0")) 2 else if (kotlin.math.abs(this.toDouble()) < 1) maximumDigits else 2

    return if (!withSymbol) {
        format.format(this).replace(currency.symbol, "").trim()
    } else {
        format.format(this)
    }
}

fun Double.getFormattedStringOneDecimal(): String {
    val format = NumberFormat.getNumberInstance(AppLocale.enUsPosix) as DecimalFormat
    format.minimumFractionDigits = 1
    format.maximumFractionDigits = 7

    return format.format(this)
}

fun Double.getFormattedStringCalculator(): String {
    val symbols = DecimalFormatSymbols(AppLocale.enUsPosix)
    symbols.groupingSeparator = ' '
    symbols.decimalSeparator = '.'

    val format = DecimalFormat("#,###.##", symbols)
    format.minimumFractionDigits = 0
    format.maximumFractionDigits = 7

    return format.format(this)
}

fun Double.getFormattedStringTwoDecimal(): String {
    val format = NumberFormat.getNumberInstance(AppLocale.enUsPosix) as DecimalFormat
    format.minimumFractionDigits = 2
    format.maximumFractionDigits = 7
    return format.format(this)
}

fun Double.getFormattedCalculator(): String {
    val format = NumberFormat.getNumberInstance(AppLocale.enUsPosix) as DecimalFormat
    format.minimumFractionDigits = 4
    format.maximumFractionDigits = if(this < 0.1) 7 else 4
    return format.format(this)
}

fun Double.getChangeSymbolResource(): Int {
    val roundedDouble = this.roundTo(2)

    return when {
        roundedDouble > 0 -> return R.drawable.trend_up
        roundedDouble < 0 -> return R.drawable.trend_down
        else -> R.drawable.trend_still
    }
}

@Composable
fun Double.getChangeColorCompose(): Color {
    val roundedDouble = this.roundTo(7)

    return when {
        roundedDouble > 0 -> MaterialTheme.colors.trendUp
        roundedDouble < 0 -> MaterialTheme.colors.trendDown
        else -> MaterialTheme.colors.onSurfaceDim
    }
}

fun Double.getWidgetChangeColor(context: Context): Int {
    val roundedDouble = this.roundTo(2)

    return when {
        roundedDouble > 0 -> return widgetTrendUp(context)
        roundedDouble < 0 -> return widgetTrendDown(context)
        else -> widgetOnSurfaceDim(context)
    }
}

fun Double.getChangeWithSymbol(withSymbol: Boolean = false, force: Boolean = false,): String {
    var rounded = this.roundTo(2)

    if (rounded == -0.0) {
        rounded = 0.0
    }

    val change = when {
        rounded > 9999.0 -> ">9999%"
        rounded < -9999.0 -> "<9999%"
        rounded < 0 -> "${String.format("%.2f", rounded).replace(if (withSymbol || force) "" else "-", "")}%"
        else -> "${if(withSymbol || force) "+" else ""}${String.format("%.2f", rounded)}%"
    }

    return if(withSymbol || force) change else change.replace("-", "").replace("+", "")
}

fun Double.getChangeWithoutSymbol(): String {
    val change = when {
        this < 0 -> "${String.format("%.2f", this)}%"
        else -> "${String.format("%.2f", this)}%"
    }

    return change.replace("-", "").replace("+", "")
}

fun Double.roundTo(decimals: Int): Double {
    return String.format("%.${decimals}f", this).replace(",", ".").toDouble()
}

fun Double.roundToNew(decimals: Int): String {
    return String.format("%.${decimals}f", this).replace(",", ".")
}

fun Double.roundToDouble(decimals: Int): Double {
    val divisor = 10.0.pow(decimals)
    return round(this * divisor) / decimals
}

fun Double.getChangeWithSymbolPortfolio(): String {
    var value = this

    if (value == -0.0) {
        value = 0.0
    }

    return when {
        value > 9999.0 -> ">9999%"
        value < -9999.0 -> "<9999%"
        else -> { "${String.format("%.2f", value).replace("-", "")}%" }
    }
}

@Composable
fun Double.getPercentChangeColorPortfolio(): Color {
    if (kotlin.math.abs(this) < 0.0000001) {
        return MaterialTheme.colors.mainGray
    }

    return when {
        this > 0 -> MaterialTheme.colors.mainGreen
        this < 0 -> MaterialTheme.colors.mainRed
        else -> MaterialTheme.colors.mainGray
    }
}

fun Double.getPercentChangeDrawablePortfolio(): Int {
    if (kotlin.math.abs(this) < 0.0000001) {
        return R.drawable.trend_still
    }

    return try {
        when {
            this > 0 -> return R.drawable.trend_up
            this < 0 -> return R.drawable.trend_down
            else -> return R.drawable.trend_still
        }
    } catch (e: Exception) {
        R.drawable.trend_still
    }
}

fun Double.getWidgetPercentChangeColorPortfolio(context: Context): Int {
    if (kotlin.math.abs(this) < 0.0000001) {
        return widgetOnSurfaceDim(context)
    }

    return try {
        when {
            this > 0 -> return widgetTrendUp(context)
            this < 0 -> return widgetTrendDown(context)
            else -> widgetOnSurfaceDim(context)
        }
    } catch (e: Exception) {
        widgetOnSurfaceDim(context)
    }
}
