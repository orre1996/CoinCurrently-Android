package com.cryptium.coincurrently.widgets.receivers
import com.cryptium.coincurrently.core.config.AppLocale
import android.content.Context
import com.cryptium.coincurrently.core.extensions.is24HourClock
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.concurrent.TimeUnit

fun Date.getDateTimeAgo(): String {
    val now = Date()

    val seconds = TimeUnit.MILLISECONDS.toSeconds(now.time - (this.time))
    val minutes = TimeUnit.MILLISECONDS.toMinutes(now.time - (this.time))
    val hours = TimeUnit.MILLISECONDS.toHours(now.time - (this.time))
    val days = TimeUnit.MILLISECONDS.toDays(now.time - (this.time))
    val weeks = days / 7

    if (seconds < 60) {
        val plural = if (seconds != 1L) "s" else ""
        return "$seconds second$plural ago"
    } else if (minutes < 60) {
        val plural = if (minutes != 1L) "s" else ""
        return "$minutes minute$plural ago"
    } else if (hours < 24) {
        val plural = if (hours != 1L) "s" else ""
        return "$hours hour$plural ago"
    } else if (days < 7) {
        val plural = if (days != 1L) "s" else ""
        return "$days day$plural ago"
    } else {
        val plural = if (weeks != 1L) "s" else ""
        return "$weeks week$plural ago"
    }/* else if (days/30 < 12){
        val plural = "s"
        return "${days/30} month$plural ago"
    } else {
        val plural = if (days/30/12 != 1L) "s" else ""
        return "${days/30/12} year$plural ago"
    }*/
}

fun LocalDate.EEEMMMMdyyyyStyle(): String {
    val formatter = "EEE, MMMM d, yyyy"
    val dateFormat = DateTimeFormatter.ofPattern(formatter, AppLocale.enUsPosix)
    return this.format(dateFormat)
}

fun LocalTime.getHourAndMinute(context: Context): String {
    val formatter = if(context.is24HourClock) "HH:mm" else "hh:mm a"
    val dateFormat = DateTimeFormatter.ofPattern(formatter, AppLocale.enUsPosix)
    return this.format(dateFormat)
}
