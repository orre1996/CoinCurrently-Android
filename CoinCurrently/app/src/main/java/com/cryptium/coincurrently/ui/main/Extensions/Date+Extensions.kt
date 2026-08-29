package com.cryptium.coincurrently.ui.main.Extensions

import com.cryptium.coincurrently.AppLocale
import com.cryptium.coincurrently.ui.main.Repositories.StoreKitManager
import networking.ResponseModels.TimeCharts
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.temporal.ChronoUnit
import java.util.Calendar
import java.util.Date
import java.util.GregorianCalendar
import java.util.TimeZone
import java.util.concurrent.TimeUnit

private val portfolioExportDateFormatter =
    SimpleDateFormat("yyyy-MM-dd HH:mm:ss", AppLocale.enUsPosix).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

private val coinTrackerExportDateFormatter =
    SimpleDateFormat("MM/dd/yyyy HH:mm:ss", AppLocale.enUsPosix).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

fun Date.toPortfolioExportDateTimeUTC(): String =
    portfolioExportDateFormatter.format(this)

fun Date.toCoinTrackerExportDateTimeUTC(): String =
    coinTrackerExportDateFormatter.format(this)

val Date.daysAgo: String
    get() {
        val now = Date()
        val diffMillis = now.time - this.time
        val diffDays = TimeUnit.MILLISECONDS.toDays(diffMillis).toInt().coerceAtLeast(0)
        val formattedDiff = NumberFormat.getInstance(AppLocale.enUsPosix).format(diffDays)
        return if (diffDays == 1) "$formattedDiff day" else "$formattedDiff days"
    }

fun Date.isAtMidnight(): Boolean {
    val calendar = Calendar.getInstance().apply {
        time = this@isAtMidnight
    }

    return calendar.get(Calendar.HOUR_OF_DAY) == 0 && calendar.get(Calendar.MINUTE) == 0
}

fun Date.formatDate(style: DateFormat): String? {
    val formatter = SimpleDateFormat(style.pattern, AppLocale.enUsPosix)

    return try {
        return formatter.format(this)
    } catch(_: Exception) {
        null
    }
}

fun Date.alertTimeAgoDisplay(): String {
    val now = Date()
    val diffInMillis = now.time - this.time

    val minutesAgo = TimeUnit.MILLISECONDS.toMinutes(diffInMillis)
    val hoursAgo = TimeUnit.MILLISECONDS.toHours(diffInMillis)
    val daysAgo = TimeUnit.MILLISECONDS.toDays(diffInMillis)

    return when {
        minutesAgo < 1 -> "just now"
        hoursAgo < 1 -> {
            if (minutesAgo == 1L) "$minutesAgo minute ago" else "$minutesAgo minutes ago"
        }
        daysAgo < 1 -> {
            if (hoursAgo == 1L) "$hoursAgo hour ago" else "$hoursAgo hours ago"
        }
        else -> {
            val formatter = SimpleDateFormat("MMMM d, yyyy", AppLocale.enUsPosix)
            formatter.format(this)
        }
    }
}



fun Date.inTime(days: Int = 0, hours: Int = 0, minutes: Int = 0): Date {
    val calendar = Calendar.getInstance(AppLocale.enUsPosix)
    calendar.add(Calendar.DAY_OF_YEAR, days)
    calendar.add(Calendar.HOUR, hours)
    calendar.add(Calendar.MINUTE, minutes)
    return calendar.time
}

fun Date.isMoreThanOneMonthOld(): Boolean {
    val currentDate = LocalDate.now()
    val dateInstant = this.toInstant()
    val dateLocal = dateInstant.atZone(ZoneId.systemDefault()).toLocalDate()

    val monthsBetween = ChronoUnit.MONTHS.between(dateLocal, currentDate)

    return monthsBetween >= 1
}


fun Date.isMoreThanTwoDays(): Boolean {
    val currentDate = LocalDate.now()
    val dateInstant = this.toInstant()
    val dateLocal = dateInstant.atZone(ZoneId.systemDefault()).toLocalDate()

    val daysBetween = ChronoUnit.DAYS.between(dateLocal, currentDate)

    return daysBetween >= 2
}


fun Date?.isMoreThanDefaultCache(): Boolean {
    if (this == null) {
        return false
    }

    val now = Date()
    val diffInMillis = now.time - this.time
    val thresholdMinutes = if (StoreKitManager.currentSubscription.value != null) 3 else 15

    return diffInMillis > thresholdMinutes * 60 * 1000
}

fun Date.roundToNextWholeHour(): Date {
    val closestHour = if (Date().minutes >= 30) 60 - Date().minutes else -Date().minutes

    val c = GregorianCalendar()
    c.time = this
    c.add(Calendar.HOUR, if (closestHour <= 0) 0 else 1)
    c.set(Calendar.MINUTE, 0)
    c.set(Calendar.SECOND, 0)
    return c.time
}

fun Date.tenYearsAgo(): Date {
    val cal = Calendar.getInstance()
    cal.time = this
    cal.add(Calendar.YEAR, -10)
    return cal.time
}

fun Date.toLocalDate(): LocalDate {
    return this.toInstant()
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
}

fun Date.withTime(
    hour: Int,
    minute: Int,
    zone: ZoneId = ZoneId.systemDefault()
): Date {
    return Date.from(
        this.toInstant()
            .atZone(zone)
            .toLocalDate()
            .atTime(hour, minute)
            .atZone(zone)
            .toInstant()
    )
}

val Date.hour: Int
    get() = this.toInstant()
        .atZone(ZoneId.systemDefault())
        .hour

val Date.minute: Int
    get() = this.toInstant()
        .atZone(ZoneId.systemDefault())
        .minute

fun Date.accessibleChartHoverDate(
    timeChart: TimeCharts,
    dontShowTime: Boolean = false,
    useExchangeFormat: Boolean = false,
    is24HourClock: Boolean,
): String {
    if (useExchangeFormat) {
        return accessibleChartExchangeHoverDate(timeChart, is24HourClock)
    }

    return when (timeChart) {
        TimeCharts.aWeek -> {
            if (dontShowTime) {
                formatDate(DateFormat.MMMMddyyyy) ?: ""
            } else {
                val day = formatDate(DateFormat.MMMMdd) ?: ""
                "$day at ${accessibleChartHoverTime(is24HourClock)}"
            }
        }
        TimeCharts.aMonth, TimeCharts.threeMonths, TimeCharts.aYear, TimeCharts.max ->
            formatDate(DateFormat.MMMMddyyyy) ?: ""
        else -> {
            val day = formatDate(DateFormat.MMMMdd) ?: ""
            "$day at ${accessibleChartHoverTime(is24HourClock)}"
        }
    }
}

private fun Date.accessibleChartExchangeHoverDate(
    timeChart: TimeCharts,
    is24HourClock: Boolean,
): String {
    return when (timeChart) {
        TimeCharts.aDay, TimeCharts.aWeek -> {
            val day = formatDate(DateFormat.MMMMdd) ?: ""
            "$day at ${accessibleChartHoverTime(is24HourClock)}"
        }
        TimeCharts.aMonth, TimeCharts.aYear, TimeCharts.max ->
            formatDate(DateFormat.MMMMddyyyy) ?: ""
        else -> {
            val day = formatDate(DateFormat.MMMdd) ?: ""
            "$day at ${accessibleChartHoverTime(is24HourClock)}"
        }
    }
}

private fun Date.accessibleChartHoverTime(is24HourClock: Boolean): String {
    val calendar = Calendar.getInstance().apply { time = this@accessibleChartHoverTime }
    val hour = calendar.get(Calendar.HOUR_OF_DAY)
    val minute = calendar.get(Calendar.MINUTE)

    if (is24HourClock) {
        if (minute == 0) {
            if (hour == 0) return "midnight"
            return "${hour.toDouble().numberAccessible(maxPlaces = 0)} o'clock"
        }
        if (hour == 0) {
            return "${minute.toDouble().numberAccessible(maxPlaces = 0)} past midnight"
        }
        return "${hour.toDouble().numberAccessible(maxPlaces = 0)} ${accessibleSpokenMinute(minute)}"
    }

    val hour12 = hour % 12
    val displayHour = if (hour12 == 0) 12 else hour12
    val period = if (hour < 12) "am" else "pm"

    if (minute == 0) {
        return "${displayHour.toDouble().numberAccessible(maxPlaces = 0)} $period"
    }

    return "${displayHour.toDouble().numberAccessible(maxPlaces = 0)} ${accessibleSpokenMinute(minute)} $period"
}

private fun accessibleSpokenMinute(minute: Int): String {
    if (minute < 10) {
        return "oh ${minute.toDouble().numberAccessible(maxPlaces = 0)}"
    }
    return minute.toDouble().numberAccessible(maxPlaces = 0)
}

val Date.addedAccessibilityDescription: String
    get() {
        val formatter = SimpleDateFormat("EEEE, MMMM d, yyyy 'at' hh:mm a", AppLocale.enUsPosix)
        return "Added ${formatter.format(this)}"
    }

fun Date.accessibleLongDateDescription(): String {
    val formatter = SimpleDateFormat("EEEE, MMMM d, yyyy", AppLocale.enUsPosix)
    return formatter.format(this)
}

fun Date.accessibleTimeDescription(is24HourClock: Boolean): String =
    accessibleChartHoverTime(is24HourClock)

fun chartAxisTimestampIndex(labelIndex: Int, labelCount: Int, dataCount: Int): Int {
    if (dataCount <= 0) return 0
    if (dataCount == 1) return 0
    val fraction = (labelIndex - 0.5) / labelCount.toDouble()
    val clamped = fraction.coerceIn(0.0, 1.0)
    val maxIndex = dataCount
    return (maxIndex * clamped).toInt().coerceAtMost(maxIndex)
}