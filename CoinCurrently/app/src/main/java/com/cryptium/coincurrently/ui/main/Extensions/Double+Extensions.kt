package com.cryptium.coincurrently.ui.main.Extensions

import android.content.Context
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.glance.unit.ColorProvider
import com.cryptium.coincurrently.AppLocale
import com.cryptium.coincurrently.R
import com.cryptium.coincurrently.ui.main.Compose.WidgetColorValues
import com.cryptium.coincurrently.ui.main.Compose.onSurfaceContainerDim
import com.cryptium.coincurrently.ui.main.Compose.onSurfaceDim
import com.cryptium.coincurrently.ui.main.Compose.trendDown
import com.cryptium.coincurrently.ui.main.Compose.trendUp
import com.cryptium.coincurrently.ui.main.CustomViews.getFormattedCurrencyString
import com.cryptium.coincurrently.ui.main.CustomViews.roundTo
import com.cryptium.coincurrently.ui.main.General.Enums.FormattingThreshold
import com.cryptium.coincurrently.ui.main.General.GlobalFunctions
import com.cryptium.coincurrently.ui.main.HomeTab.GlobalMarket.DataClasses.Currency
import networking.ResponseModels.CurrencySelectionModel
import java.lang.Integer.min
import java.math.BigDecimal
import java.math.BigInteger
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.NumberFormat
import java.util.Locale
import kotlin.compareTo
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.round
import kotlin.math.roundToLong

fun Double.getChangeInValue(currentValue: Double): Double {
    val changeInPercent = (this / 100) + 1
    val changeInValue = currentValue / changeInPercent

    return currentValue - changeInValue
}

@Composable
fun Double.changeImage(): Painter {
    return when {
        this > 0 -> painterResource(id = R.drawable.trend_up)
        this < 0 -> painterResource(id = R.drawable.trend_down)
        else -> painterResource(id = R.drawable.trend_still)
    }
}

@Composable
fun Double.changeColor(): Color {
    return when {
        this > 0 -> MaterialTheme.colors.trendUp
        this < 0 -> MaterialTheme.colors.trendDown
        else -> MaterialTheme.colors.onSurfaceDim
    }
}

@Composable
fun Double.changeColorWidget(): ColorProvider {
    return when {
        this < 0 -> WidgetColorValues.trendDown()
        this > 0 -> WidgetColorValues.trendUp()
        else -> WidgetColorValues.onSurfaceDim()
    }
}

@Composable
fun Double.changeSymbolWidget(): Int {
    return when {
        this > 0 -> return R.drawable.trend_up
        this < 0 -> return R.drawable.trend_down
        else -> R.drawable.trend_still
    }
}

@Composable
fun Double.changeWithSymbol(): String {
    return when {
        isInfinite() -> {
            val symbols = android.icu.text.DecimalFormatSymbols.getInstance(AppLocale.enUsPosix)
            if (this > 0) "${symbols.plusSignString}∞%" else "${symbols.minusSignString}∞%"
        }
        this > 9999.0 -> ">9999%"
        this < -9999.0 -> "<9999%"
        this < 0 -> "${"%.2f".format(this)}%"
        else -> "+${"%.2f".format(this)}%"
    }
}

val Double.changeValueDecimalPoints: Int
    get() =
        if(abs(this) < 1) 4 else 2

fun Double.formattedRemovingTrailingZeros(
    locale: Locale = AppLocale.enUsPosix,
    maximumDigits: Int = 2,
    useGrouping: Boolean = true,
): String {
    val value = this
    val formatter = (NumberFormat.getNumberInstance(locale) as DecimalFormat).apply {
        minimumFractionDigits = 0
        maximumFractionDigits = if (abs(value) < 1) 7 else maximumDigits
        isGroupingUsed = useGrouping
    }
    return try {
        formatter.format(this)
    } catch (e: Exception) {
        "0"
    }
}

fun Double.formatGroupedWholeNumber(
    locale: Locale = GlobalFunctions.getSelectedCurrency().formattingLocale,
): String {
    return abs(roundTo(0)).formattedRemovingTrailingZeros(
        locale = locale,
        maximumDigits = 0,
        useGrouping = true,
    )
}

val Double.timeUntilExpired: String
    get() {
        val expirationDate = Instant.ofEpochSecond(this.toLong()).atZone(ZoneId.systemDefault())
        val now = Instant.now().atZone(ZoneId.systemDefault())
        if (expirationDate <= now) return "-"

        val months = ChronoUnit.MONTHS.between(now, expirationDate)
        val days = ChronoUnit.DAYS.between(now, expirationDate)
        val hours = Duration.between(now, expirationDate).toHours()

        return when {
            months > 0 -> if (months == 1L) "1 month" else "$months months"
            days > 0 -> if (days == 1L) "1 day" else "$days days"
            hours >= 0 -> if (hours <= 1) "1 hour" else "$hours hours"
            else -> "Less than 1 hour"
        }
    }

val Double.timeAgoDisplay: String
    get() {
        val self = Instant.ofEpochSecond(this.toLong()).atZone(ZoneId.systemDefault())
        val now = Instant.now().atZone(ZoneId.systemDefault())
        val minuteAgo = now.minusMinutes(1)
        val hourAgo = now.minusHours(1)
        val dayAgo = now.minusDays(1)
        val weekAgo = now.minusWeeks(1)

        return when {
            self.isAfter(minuteAgo) -> {
                val diff = ChronoUnit.SECONDS.between(self, now)
                if (diff == 1L) "1 second ago" else "$diff seconds ago"
            }
            self.isAfter(hourAgo) -> {
                val diff = ChronoUnit.MINUTES.between(self, now)
                if (diff == 1L) "1 minute ago" else "$diff minutes ago"
            }
            self.isAfter(dayAgo) -> {
                val diff = ChronoUnit.HOURS.between(self, now)
                if (diff == 1L) "1 hour ago" else "$diff hours ago"
            }
            self.isAfter(weekAgo) -> {
                val diff = ChronoUnit.DAYS.between(self, now)
                if (diff == 1L) "1 day ago" else "$diff days ago"
            }
            else -> {
                val diff = ChronoUnit.WEEKS.between(self, now)
                if (diff == 1L) "1 week ago" else "$diff weeks ago"
            }
        }
    }

val Double.changeDecimalPoints: Int
    get() = when {
        abs(this) >= 1000 -> 0
        abs(this) >= 100 -> 1
        else -> 2
    }

/** Formats a percent change for display, e.g. `12.34%` or `1.00K%` when ≥ 1,000. */
fun Double.formatChangePercent(): String {
    if (isInfinite()) {
        return if (this > 0) "∞%" else "-∞%"
    }

    val absValue = abs(this)
    val threshold = FormattingThreshold.entries
        .firstOrNull { absValue >= it.rawValue && it != FormattingThreshold.NONE }
        ?: FormattingThreshold.NONE
    val scaled = absValue / threshold.rawValue
    val decimals = when {
        threshold != FormattingThreshold.NONE -> 2
        absValue >= 100 -> 1
        else -> 2
    }
    val formatter = NumberFormat.getNumberInstance(AppLocale.enUsPosix).apply {
        maximumFractionDigits = decimals
        minimumFractionDigits = decimals
        isGroupingUsed = true
    }
    return "${formatter.format(scaled.roundTo(decimals))}${threshold.symbol}%"
}

fun Double.roundTo(places: Int): Double {
    try {
        val divisor: Double = 10.0.pow(places)
        val scaled: Double = this * divisor
        if (!scaled.isFinite()) {
            return this
        }
        return round(scaled) / divisor
    } catch (e: Exception) {
        return 0.0
    }
}

/** Rounds to the given decimal places; returns 0 if the absolute value is below 10^(-decimals). */
fun Double.roundOrZero(decimals: Int): Double {
    val rounded = this.roundTo(decimals)
    val threshold = 10.0.pow(-decimals)
    return if (abs(rounded) < threshold) 0.0 else rounded
}


val Double.changePercentDecimalPoints: Int
    get() = when {
        abs(this) >= 1000 -> 0
        abs(this) >= 100 -> 1
        else -> 2
    }

val Double.changePercentAccessible: String
    get() {
        val absValue = abs(this)
        val threshold = FormattingThreshold.entries
            .firstOrNull { absValue >= it.rawValue && it != FormattingThreshold.NONE }
            ?: FormattingThreshold.NONE
        if (threshold == FormattingThreshold.NONE) {
            return absValue.numberAccessible(maxPlaces = changePercentDecimalPoints)
        }
        val shortened = (absValue / threshold.rawValue).roundTo(2)
        return "${shortened.numberAccessible(maxPlaces = 2)} ${threshold.spellOutSuffix}"
    }

val Double.directionAccessible: String
    get() = if (this >= 0) "Up" else "Down"

fun Double.numberAccessible(maxPlaces: Int = 7): String {
    val places = if (abs(this) < 1) maxPlaces else 2
    return abs(roundTo(places)).formattedRemovingTrailingZeros(
        maximumDigits = places,
        useGrouping = false,
    )
}

fun Double.currencyAccessible(
    currency: CurrencySelectionModel = GlobalFunctions.getSelectedCurrency(),
    maxDecimals: Int? = null,
): String {
    return accessibleCurrencyAmount(currency = currency, maxDecimals = maxDecimals)
}

private fun Double.accessibleCurrencyAmount(
    currency: CurrencySelectionModel,
    maxDecimals: Int?,
): String {
    val absAmount = abs(this)
    val decimals = accessibleCurrencyDecimalPlaces(
        currency = currency,
        maxDecimals = maxDecimals,
        absAmount = absAmount,
    )

    if (currency.readAccessibilityAsSymbol) {
        val amountText = absAmount.formattedRemovingTrailingZeros(
            locale = currency.formattingLocale,
            maximumDigits = decimals,
            useGrouping = true,
        )
        return "$amountText ${currency.currency.trim()}"
    }

    val currencyName = currency.voiceOverCurrencyName

    if (absAmount < 1) {
        val amountText = accessibleSubDollarAmount(absAmount, decimals)
        val unit = accessibleCurrencyUnit(amount = absAmount.roundTo(decimals), pluralName = currencyName)
        return "$amountText $unit"
    }

    val scaled = BigDecimal.valueOf(absAmount).setScale(2, RoundingMode.HALF_UP)
    val wholePart = scaled.toBigInteger()
    val cents = scaled.remainder(BigDecimal.ONE).movePointRight(2).toInt()
    val wholeText = numberSpelledOut(wholePart.toLong())
    val wholeUnit = accessibleCurrencyUnit(wholePart, currencyName)

    return when {
        cents == 0 -> "$wholeText $wholeUnit"
        else -> {
            val centsText = numberSpelledOut(cents.toLong())
            val centUnit = if (cents == 1) "cent" else "cents"
            "$wholeText $wholeUnit, $centsText $centUnit"
        }
    }
}

private fun accessibleCurrencyDecimalPlaces(
    currency: CurrencySelectionModel,
    maxDecimals: Int?,
    absAmount: Double,
): Int {
    maxDecimals?.let { return it }

    val currencyFractionDigits = try {
        java.util.Currency.getInstance(currency.code).defaultFractionDigits
    } catch (_: Exception) {
        null
    }

    return when {
        currencyFractionDigits == 0 -> 0
        absAmount < 1 -> 7
        else -> currencyFractionDigits?.coerceIn(0, 7) ?: 2
    }
}

private fun accessibleSubDollarAmount(amount: Double, decimals: Int): String {
    val scaled = BigDecimal.valueOf(amount)
        .setScale(decimals, RoundingMode.HALF_UP)
        .stripTrailingZeros()
    val plain = scaled.toPlainString()
    val parts = plain.split(".")
    val wholePart = parts[0].toLongOrNull() ?: 0L
    val wholeText = numberSpelledOut(wholePart)

    if (parts.size == 1) return wholeText

    val fractionDigits = parts[1].map { digit ->
        numberSpelledOut(digit.digitToInt().toLong())
    }.joinToString(" ")

    return "$wholeText point $fractionDigits"
}

private fun numberSpelledOut(value: Long): String {
    if (value == 0L) return "zero"
    if (value < 0) return "negative ${numberSpelledOut(-value)}"

    val units = arrayOf(
        "zero", "one", "two", "three", "four", "five", "six", "seven", "eight", "nine",
        "ten", "eleven", "twelve", "thirteen", "fourteen", "fifteen", "sixteen",
        "seventeen", "eighteen", "nineteen",
    )
    val tens = arrayOf("", "", "twenty", "thirty", "forty", "fifty", "sixty", "seventy", "eighty", "ninety")
    val scales = arrayOf("", "thousand", "million", "billion", "trillion", "quadrillion")

    fun spellUnder100(n: Int): String = when {
        n < 20 -> units[n]
        n < 100 -> {
            val ten = tens[n / 10]
            val unit = n % 10
            if (unit == 0) ten else "$ten-${units[unit]}"
        }
        else -> error("Unexpected number: $n")
    }

    fun spellUnder1000(n: Int): String = when {
        n < 100 -> spellUnder100(n)
        else -> {
            val hundred = n / 100
            val remainder = n % 100
            if (remainder == 0) "${units[hundred]} hundred"
            else "${units[hundred]} hundred ${spellUnder100(remainder)}"
        }
    }

    var remaining = value
    val parts = mutableListOf<String>()
    var scaleIndex = 0

    while (remaining > 0) {
        val chunk = (remaining % 1000).toInt()
        if (chunk != 0) {
            val chunkText = spellUnder1000(chunk)
            val scale = scales[scaleIndex]
            parts.add(0, if (scale.isEmpty()) chunkText else "$chunkText $scale")
        }
        remaining /= 1000
        scaleIndex++
        if (scaleIndex >= scales.size && remaining > 0) {
            return value.toString()
        }
    }

    return parts.joinToString(" ")
}

private fun accessibleCurrencyUnit(amount: BigInteger, pluralName: String): String {
    if (amount == BigInteger.ONE && pluralName.lowercase().endsWith("s")) {
        return pluralName.dropLast(1)
    }
    return pluralName
}

private fun accessibleCurrencyUnit(amount: Double, pluralName: String): String {
    if (amount == 1.0 && pluralName.lowercase().endsWith("s")) {
        return pluralName.dropLast(1)
    }
    return pluralName
}

fun Double.formatCurrency(
    threshold: FormattingThreshold? = null,
    currency: CurrencySelectionModel = GlobalFunctions.getSelectedCurrency(),
    minDecimals: Int? = null,
    maxDecimals: Int? = null,
    accessible: Boolean = false,
): String {
    val resolvedThreshold = when {
        threshold == FormattingThreshold.NONE || (threshold != null && abs(this) < threshold.rawValue) ->
            FormattingThreshold.NONE
        else ->
            FormattingThreshold.entries.firstOrNull { abs(this) >= it.rawValue } ?: FormattingThreshold.NONE
    }

    return formattedString(
        amount = this,
        threshold = resolvedThreshold,
        currency = currency,
        minDecimals = minDecimals,
        maxDecimals = maxDecimals,
        accessible = accessible,
    ).replace("  ", " ")
}

/** Formats a currency amount for editable input fields (no currency symbol). */
fun Double.formatCurrencyInput(
    currency: CurrencySelectionModel = GlobalFunctions.getSelectedCurrency(),
): String {
    val decimals = if (abs(this) < 1) 7 else 2
    val rounded = roundTo(decimals)
    val formatter = NumberFormat.getNumberInstance(currency.formattingLocale).apply {
        minimumFractionDigits = 2
        maximumFractionDigits = decimals
        isGroupingUsed = true
    }
    return formatter.format(rounded)
}

private fun formattedString(
    amount: Double,
    threshold: FormattingThreshold,
    currency: CurrencySelectionModel,
    minDecimals: Int? = null,
    maxDecimals: Int? = null,
    accessible: Boolean = false,
): String {
    val formattedValue = amount / threshold.rawValue
    val decimals = maxDecimals ?: if (abs(amount) < 1) 7 else 2

    if (accessible) {
        val currencyName = currency.voiceOverCurrencyName
        if (threshold == FormattingThreshold.NONE) {
            return amount.accessibleCurrencyAmount(currency = currency, maxDecimals = maxDecimals)
        }
        val roundedFormattedValue = formattedValue.roundTo(decimals)
        val formattedAmount = roundedFormattedValue.numberAccessible(maxPlaces = decimals)
        return "$formattedAmount ${threshold.spellOutSuffix} $currencyName"
    }

    if (currency.symbol.isEmpty() && currency.code.isEmpty()) {
        val numberFormat = NumberFormat.getNumberInstance(currency.formattingLocale).apply {
            minimumFractionDigits = minDecimals ?: 2
            maximumFractionDigits = decimals
            isGroupingUsed = true
        }
        val formattedAmount = numberFormat.format(formattedValue).trim()
        return "$formattedAmount${threshold.symbol}"
    }

    val symbolPrefix = if (currency.suffix) "" else currency.symbol
    val symbolSuffix = if (currency.suffix) {
        val space = if (threshold.symbol.isNotEmpty()) " " else ""
        "$space${currency.symbol}"
    } else ""

    val locale = currency.formattingLocale
    val symbols = DecimalFormatSymbols(locale).apply {
        currencySymbol = symbolPrefix
    }

    val numberFormat = (NumberFormat.getCurrencyInstance(locale) as DecimalFormat).apply {
        this.currency =  try {
            java.util.Currency.getInstance(currency.code)
        } catch(e: Exception) {
            java.util.Currency.getInstance("USD")
        }

        minimumFractionDigits = minDecimals ?: 2
        maximumFractionDigits = decimals
        decimalFormatSymbols = symbols
    }

    val formattedAmount = numberFormat.format(formattedValue).trim()
    return if (symbolSuffix.isEmpty()) {
        "$formattedAmount${threshold.symbol}"
    } else {
        "$formattedAmount${threshold.symbol} $symbolSuffix"
    }
}







fun Number.formatCurrency(short: Boolean = false, withSymbol: Boolean = true, context: Context? = null, currencySelectionModel: CurrencySelectionModel? = null, fromPriceChange: Boolean = false, roundToClosest: Boolean = false, fromWidgets: Boolean = false): String {
    val shortings = arrayListOf("K", "M", "B", "T", "Q")
    val currency = currencySelectionModel ?: GlobalFunctions.getSelectedCurrency(context)
    val currencySuffix = if (currency?.suffix == true && withSymbol) " ${currency.symbol}" else ""

    var price = BigDecimal(BigDecimal(this.toDouble()).toPlainString()).setScale(7, RoundingMode.FLOOR)
    var minimumDigits = 2

    // Värden över 2.147 miljarder blir fucked om de är double, därav toBigDecimal
    if (roundToClosest) {
        price = if (price > 10000000000.0.toBigDecimal()) {
            roundUp(price,  1000000000.0.toBigDecimal())
        } else if (price > 1000000000.0.toBigDecimal()) {
            roundUp(price, 100000000.0.toBigDecimal())
        } else if (price > 1000000.0.toBigDecimal()) {
            roundUp(price, 1000000.0.toBigDecimal())
        } else if (price > 100000.0.toBigDecimal()) {
            roundUp(price, 10000.0.toBigDecimal())
        } else if (price > 10000.0.toBigDecimal()) {
            roundUp(price, 100.0.toBigDecimal())
        } else if (price > 1000.0.toBigDecimal()) {
            roundUp(price, 10.0.toBigDecimal())
        } else if (price > 50.0.toBigDecimal()) {
            roundUp(price, 1.0.toBigDecimal())
        } else if (price > 10.0.toBigDecimal()) {
            ((price.toDouble() * 5).roundToLong() / 5.0).toBigDecimal()
        } else if (price > 6.toBigDecimal()) {
            ((price.toDouble() * 10).roundToLong() / 10.0).toBigDecimal()
        } else if (price > 1.1.toBigDecimal()) {
            ((price.toDouble() * 50).roundToLong() / 50.0).toBigDecimal()
        } else if (price > 0.1.toBigDecimal()) {
            minimumDigits = 3
            ((price.toDouble() * 100) / 100.0).roundTo(3).toBigDecimal()
        } else if (price > 0.01.toBigDecimal()) {
            minimumDigits = 3
            ((price.toDouble() * 1000) / 1000.0).roundTo(3).toBigDecimal()
        } else {
            minimumDigits = 7
            ((price.toDouble() * 100000) / 100000.0).roundTo(7).toBigDecimal()
        }
    }

    var maxDigits = 7

    if (fromPriceChange) {

        if (kotlin.math.abs(price.toDouble()) < 1) {
            maxDigits = 4
        }

        if ((kotlin.math.abs(price.toDouble()) < 0.0000001)) {
           return (0.0).getFormattedCurrencyString(!(currency?.suffix ?: false), context, currency, maxDigits)+currencySuffix
        } else if ((kotlin.math.abs(price.toDouble()) < 0.0001)) {
           return (price * 1000.0.toBigDecimal()).getFormattedCurrencyString(!(currency?.suffix ?: false), context, currency, maxDigits)?.trim() + "m" + currencySuffix
        }
    }

    if (!short) {
        val fromWidgets = if(price >= BigDecimal(0.0) && fromWidgets) "+" else ""
        return "$fromWidgets${(price).getFormattedCurrencyString(!(currency?.suffix ?: false) && withSymbol, context, minimumDigits = minimumDigits, currencySelectionModel = currency)}$currencySuffix"
    }

    val long = kotlin.math.abs(price.toLong())
    val priceString = long.toString()
    val splitPrice = priceString.split(".").joinToString { it }
    val priceLength = (splitPrice.length - 1) / 3
    val suffix = if (kotlin.math.abs(price.toDouble()) > 999) shortings[min(priceLength - 1, 4)] else ""
    val division = 10.toDouble().pow(priceLength.toDouble() * 3)

    return "${(BigDecimal(price.toPlainString()).setScale(maxDigits, RoundingMode.FLOOR) / division.toBigDecimal()).toDouble().getFormattedCurrencyString(!(currency?.suffix ?: false) && withSymbol, context, currency, maxDigits)}$suffix$currencySuffix"
}

fun roundUp(number: BigDecimal, near: BigDecimal): BigDecimal {
    if(number % near == 0.0.toBigDecimal()) return number

    return ((number.toDouble() / near.toDouble()).roundToLong() * near.toLong()).toDouble().toBigDecimal()
}

fun Double.formatCurrencyNoCurrency(short: Boolean = false, context: Context? = null, currencySelectionModel: CurrencySelectionModel? = null): String {
    val shortings = arrayListOf("K", "M", "B", "T", "Q")

    if (!short) {
        return "${(this).getFormattedCurrencyString(false, context, currencySelectionModel)}"
    }

    val long = kotlin.math.abs(this.toLong())
    val priceString = long.toString()
    val splitPrice = priceString.split(".").joinToString { it }
    val priceLength = (splitPrice.length - 1) / 3
    val suffix = if (kotlin.math.abs(this) > 999) shortings[min(priceLength - 1, 4)] else ""
    val division = 10.toDouble().pow(priceLength.toDouble() * 3)
    return "${(this / division).getFormattedCurrencyString(false, context, currencySelectionModel)}$suffix"
}

fun Double.formatCurrencyFromBillion(withSymbol: Boolean = true, context: Context? = null, currencySelectionModel: CurrencySelectionModel? = null): String {
    val shortings = arrayListOf("K", "M", "B", "T", "Q")
    val currency = currencySelectionModel ?: GlobalFunctions.getSelectedCurrency(context)
    val currencySuffix = if (currency?.suffix == true && withSymbol) " ${currency.symbol ?: "$"}" else ""

    val long = kotlin.math.abs(this.toLong())
    val priceString = long.toString()
    val splitPrice = priceString.split(".").joinToString { it }

    if (splitPrice.length < 10) {
        return "${(this).getFormattedCurrencyString(!(currency?.suffix ?: false) && withSymbol, context, currency)}$currencySuffix"
    }

    val priceLength = (splitPrice.length - 1) / 3
    val suffix = if (kotlin.math.abs(this) > 999) shortings[min(priceLength - 1, 4)] else ""
    val division = 10.toDouble().pow(priceLength.toDouble() * 3)
    return "${(this / division).getFormattedCurrencyString(!(currency?.suffix ?: false) && withSymbol, context, currency)}$suffix$currencySuffix"
}

fun Double.formatCurrencyFromThousand(withSymbol: Boolean = true, context: Context? = null, currencySelectionModel: CurrencySelectionModel? = null): String {
    val shortings = arrayListOf("K", "M", "B", "T", "Q")
    val currency = currencySelectionModel ?: GlobalFunctions.getSelectedCurrency(context)
    val currencySuffix = if (currency?.suffix == true && withSymbol) " ${currency.symbol}" else ""

    val long = kotlin.math.abs(this.toLong())
    val priceString = long.toString()
    val splitPrice = priceString.split(".").joinToString { it }

    val priceLength = (splitPrice.length - 1) / 3
    val suffix = if (kotlin.math.abs(this) > 999) shortings[min(priceLength - 1, 4)] else ""
    val division = 10.toDouble().pow(priceLength.toDouble() * 3)
    return "${(this / division).getFormattedCurrencyString(!(currency?.suffix ?: false) && withSymbol, context, currency)}$suffix$currencySuffix"
}

fun Double.formatCurrencyFromTrillion(withSymbol: Boolean = true, context: Context? = null, currencySelectionModel: CurrencySelectionModel? = null): String {
    val shortings = arrayListOf("K", "M", "B", "T", "Q")
    val currency = currencySelectionModel ?: GlobalFunctions.getSelectedCurrency(context)
    val currencySuffix = if (currency?.suffix == true && withSymbol) " ${currency.symbol}" else ""

    val long = kotlin.math.abs(this.toLong())
    val priceString = long.toString()
    val splitPrice = priceString.split(".").joinToString { it }

    if (splitPrice.length < 13) {
        return "${(this).getFormattedCurrencyString(!(currency?.suffix ?: false) && withSymbol, context, currency)}$currencySuffix"
    }

    val priceLength = (splitPrice.length - 1) / 3
    val suffix = if (kotlin.math.abs(this) > 999) shortings[min(priceLength - 1, 4)] else ""
    val division = 10.toDouble().pow(priceLength.toDouble() * 3)
    return "${(this / division).getFormattedCurrencyString(!(currency?.suffix ?: false) && withSymbol, context, currency)}$suffix$currencySuffix"
}


fun Double.formatCurrencyFromHundred(withSymbol: Boolean = true, context: Context? = null, fromPortfolio: Boolean = false, currencySelectionModel: CurrencySelectionModel? = null, fromPriceChange: Boolean = false): String {
    val shortings = arrayListOf("K", "M", "B", "T", "Q")
    val currency = currencySelectionModel ?: GlobalFunctions.getSelectedCurrency(context)
    val currencySuffix = if (currency?.suffix == true && withSymbol) " ${currency.symbol}" else ""
    val prefix = if (fromPortfolio && this > 0) "" else ""

    val long = kotlin.math.abs(this.toLong())
    val priceString = long.toString()
    val splitPrice = priceString.split(".").joinToString { it }

    var maxDigits = 7

    if (fromPriceChange) {

        if (kotlin.math.abs(this) < 1) {
            maxDigits = 4
        }

        if ((kotlin.math.abs(this) < 0.0000001)) {
            return (0.0).getFormattedCurrencyString(!(currency?.suffix ?: false), context, currency, maxDigits)+currencySuffix
        } else if ((kotlin.math.abs(this) < 0.0001)) {
            return (this * 1000).getFormattedCurrencyString(!(currency?.suffix ?: false), context, currency, maxDigits)?.trim() + "m" + currencySuffix
        }

    }

    if (kotlin.math.abs(this) < 100000) {
        return "$prefix${(this).getFormattedCurrencyString(!(currency?.suffix ?: false) && withSymbol, context, maximumDigits = maxDigits)}$currencySuffix"
    }

    val priceLength = (splitPrice.replace("-","").length - 1) / 3
    val suffix = if (kotlin.math.abs(this) > 999) shortings[min(priceLength - 1, 4)] else ""
    val division = 10.toDouble().pow(priceLength.toDouble() * 3)
    return "$prefix${(this / division).getFormattedCurrencyString(!(currency?.suffix ?: false) && withSymbol, context, maximumDigits = maxDigits)}$suffix$currencySuffix"
}

fun Double.formatCurrencyFromMillion(withSymbol: Boolean = true, context: Context? = null, fromPortfolio: Boolean = false, currencySelectionModel: CurrencySelectionModel? = null): String {
    val shortings = arrayListOf("K", "M", "B", "T", "Q")

    val currency = currencySelectionModel ?: GlobalFunctions.getSelectedCurrency(context)
    val currencySuffix = if (currency?.suffix == true && withSymbol) " ${currency.symbol}" else ""
    val prefix = if (fromPortfolio && this > 0) "+" else ""

    val long = this.toLong()
    val priceString = long.toString()
    val splitPrice = priceString.split(".").joinToString { it }

    if (kotlin.math.abs(this) < 1000000) {
        return "$prefix${(this).getFormattedCurrencyString(!(currency?.suffix ?: false) && withSymbol, context, currency)}$currencySuffix"
    }

    val priceLength = (splitPrice.length - 1) / 3
    val suffix = if (kotlin.math.abs(this) > 999) shortings[min(priceLength - 1, 4)] else ""
    val division = 10.toDouble().pow(priceLength.toDouble() * 3)
    return "$prefix${(this / division).getFormattedCurrencyString(!(currency?.suffix ?: false) && withSymbol, context, currency)}$suffix$currencySuffix"
}

fun Currency.getSelectedCurrency(context: Context? = null, currency: String? = null): Double? {
    when (currency ?: (GlobalFunctions.getSelectedCurrency(context).code)) {
        "AED" -> { return this.aed }
        "AUD" -> { return this.aud }
        "BTC" -> { return this.btc }
        "BRL" -> { return this.brl }
        "BCH" -> { return this.bch }
        "BITS" -> { return this.bits }
        "BNB" -> { return this.bnb }
        "CAD" -> { return this.cad }
        "CHF" -> { return this.chf }
        "CLP" -> { return this.clp }
        "CNY" -> { return this.cny }
        "CZK" -> { return this.czk }
        "DKK" -> { return this.dkk }
        "DOT" -> { return this.dot }
        "ETH" -> { return this.eth }
        "EUR" -> { return this.eur }
        "EOS" -> { return this.eos }
        "GBP" -> { return this.gbp }
        "HKD" -> { return this.hkd }
        "HUF" -> { return this.huf }
        "IDR" -> { return this.idr }
        "ILS" -> { return this.ils }
        "INR" -> { return this.inr }
        "JPY" -> { return this.jpy }
        "LTC" -> { return this.ltc }
        "KRW" -> { return this.krw }
        "LKR" -> { return this.lkr }
        "LINK" -> { return this.link }
        "MXN" -> { return this.mxn }
        "MYR" -> { return this.myr }
        "NOK" -> { return this.nok }
        "NZD" -> { return this.nzd }
        "PHP" -> { return this.php }
        "PKR" -> { return this.pkr }
        "PLN" -> { return this.pln }
        "XRP" -> { return this.xrp }
        "RUB" -> { return this.rub }
        "SEK" -> { return this.sek }
        "SATS" -> { return this.sats }
        "SGD" -> { return this.sgd }
        "THB" -> { return this.thb }
        "TRY" -> { return this.`try` }
        "UAH" -> { return this.uah }
        "USD" -> { return this.usd }
        "VND" -> { return this.vnd }
        "ZAR" -> { return this.zar }
        "XLM" -> { return this.xlm }
        "YFI" -> { return this.yfi }
        else -> { return this.usd }
    }
}

fun getDifferenceInPercent(between: Double, and: Double): Double? {
    val delta = between - and
    val difference = (100 * (delta / (and)))

    return if(difference.isNaN()) 0.0 else difference
}

fun getDifferenceInDecimal(a: Double, b: Double): Double? {
    val delta = a - b
    val difference = 1 + (delta / b)

    return if(difference.isNaN()) null else difference
}

@Composable
fun Double.pxToDp() = with(LocalDensity.current) { this@pxToDp.toFloat().toDp() }