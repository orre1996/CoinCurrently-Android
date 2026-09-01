package com.cryptium.coincurrently.core.extensions
import com.cryptium.coincurrently.core.config.AppLocale
import com.cryptium.coincurrently.sharedui.components.GlobalFunctions
import com.cryptium.coincurrently.sharedui.components.PreferredTimeframe
import android.icu.text.DecimalFormatSymbols as IcuDecimalFormatSymbols
import android.icu.text.NumberFormat as IcuNumberFormat
import java.text.SimpleDateFormat
import java.util.*

private const val INFINITY_CHARACTER = '\u221E'

/** Unicode minus (U+2212); some formatters emit this instead of ASCII hyphen-minus (U+002D). */
private const val UNICODE_MINUS_SIGN = '\u2212'

/** True if [this] parses as non-finite infinity (JDK or default-locale ICU), or is already ∞. */
private fun String.looksLikeFormattedInfinity(): Boolean {
    val trimmed = trim()
    if (trimmed.singleOrNull() == INFINITY_CHARACTER) return true
    val normalized = trimmed.replace(",", ".").replace(" ", "")
    normalized.toDoubleOrNull()?.takeIf { it.isInfinite() }?.let { return true }
    return try {
        val parsed =
            IcuNumberFormat.getInstance(AppLocale.enUsPosix).parse(trimmed.replace(" ", ""))
        parsed?.toDouble()?.isInfinite() == true
    } catch (_: Exception) {
        false
    }
}

const val stars = "∗∗∗∗∗∗"

enum class DateFormat(val pattern: String) {
    MMMddyyyy("MMM dd, yyyy"),
    MMMdyyyy("MMM d, yyyy"),
    MMMdd("MMM d"),
    MMMMdd("MMMM d"),
    MMMMddyyyy("MMMM d, yyyy"),
    MMMddyyyyhhmma("MMM dd, yyyy 'at' hh:mm a"),
    MMMddyyyyHHmm("MMM dd, yyyy 'at' HH:mm"),
    yyyyMMdd("yyyy-MM-dd"),
    EdMMMyyyyHHmmssZ("E, d MMM yyyy HH:mm:ss Z"),
    yyyyMMddTHHmmssSSSZ("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"),
    HHmm("HH:mm"),
    hhmma("hh:mm a")
}

fun String.isNumeric(): Boolean {
    return this.toDoubleOrNull() != null
}

val String.withoutSymbol: String
    get() {
    return this
        .replace("-", "")
        .replace("+", "")
}

val String.symbolAccessible: String
    get() = map { "$it " }.joinToString("").trimEnd()

val String.changeWithSymbol: String
    get() {
        val symbols = IcuDecimalFormatSymbols.getInstance(AppLocale.enUsPosix)
        val localizedPlusSign = symbols.plusSignString
        val localizedMinusSign = symbols.minusSignString
        val escapedLocalizedPlusSign = Regex.escape(localizedPlusSign)
        val escapedLocalizedMinusSign = Regex.escape(localizedMinusSign)
        val escapedUnicodeMinus = Regex.escape(UNICODE_MINUS_SIGN.toString())
        val minusPattern = "$escapedLocalizedMinusSign|-|$escapedUnicodeMinus"
        val hasNegativeSign = Regex(minusPattern).containsMatchIn(this)
        val cleaned = replace(
            Regex("$escapedLocalizedPlusSign|$escapedLocalizedMinusSign|\\+|-|$escapedUnicodeMinus"),
            ""
        )
        val infinityChar = INFINITY_CHARACTER.toString()
        val body = if (cleaned.looksLikeFormattedInfinity()) infinityChar else cleaned
        return if (hasNegativeSign) {
            "$localizedMinusSign$body"
        } else {
            "$localizedPlusSign$body"
        }
    }


fun String.getDate(): Date? {
    return try {
        val format = SimpleDateFormat("E, d MMM yyyy HH:mm:ss Z", AppLocale.enUsPosix)
        format.parse(this)
    } catch (e: Exception) {
        Date()
    }
}

fun String.convertStringToDate(dateFormat: DateFormat, gmtTimeZone: Boolean = true): Date? {
    val dateFormatter = SimpleDateFormat(dateFormat.pattern, AppLocale.enUsPosix)
    if (gmtTimeZone) {
        dateFormatter.timeZone = TimeZone.getTimeZone("GMT")
    }
    return try {
        dateFormatter.parse(this)
    } catch (e: Exception) {
        null
    }
}

fun String.getEmptyStringLength(length: Int): String {
    var array = ""
    for (i in 0 until length) {
        array += "0"
    }

    return array
}

fun String.MMMMddyyyy(): String? {
    val sdf = SimpleDateFormat("yyyy-MM-dd", AppLocale.enUsPosix)
    val convertedDate: Date?
    val formattedDate: String?

    try {
        convertedDate = sdf.parse(this)
        formattedDate = SimpleDateFormat("MMMM dd, yyyy", AppLocale.enUsPosix).format(convertedDate)
    } catch (e: Exception) {
        return "Unknown"
    }

    return formattedDate
}

object TimeframeLabels {
    val preferredWinnersTimeframe: String
        get() = when (GlobalFunctions.getPreferredTimeframe()) {
            PreferredTimeframe.DAILY -> "Daily Winners"
            PreferredTimeframe.WEEKLY -> "Weekly Winners"
            PreferredTimeframe.MONTHLY -> "Monthly Winners"
        }

    val preferredLosersTimeframe: String
        get() = when (GlobalFunctions.getPreferredTimeframe()) {
            PreferredTimeframe.DAILY -> "Daily Losers"
            PreferredTimeframe.WEEKLY -> "Weekly Losers"
            PreferredTimeframe.MONTHLY -> "Monthly Losers"
        }
}