package com.cryptium.coincurrently.widgets.receivers
import com.cryptium.coincurrently.core.localization.Localization
import java.util.Date
import java.util.concurrent.TimeUnit

fun Date.getDateTimeAgo(useSystemLanguage: Boolean = false): String {
    val now = Date()

    val seconds = TimeUnit.MILLISECONDS.toSeconds(now.time - this.time).coerceAtLeast(0)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(now.time - this.time)
    val hours = TimeUnit.MILLISECONDS.toHours(now.time - this.time)
    val days = TimeUnit.MILLISECONDS.toDays(now.time - this.time)
    val weeks = days / 7

    return when {
        seconds < 60 -> formatAgo(
            useSystemLanguage,
            if (seconds == 1L) "%d second ago" else "%d seconds ago",
            seconds,
        )
        minutes < 60 -> formatAgo(
            useSystemLanguage,
            if (minutes == 1L) "%d minute ago" else "%d minutes ago",
            minutes,
        )
        hours < 24 -> formatAgo(
            useSystemLanguage,
            if (hours == 1L) "%d hour ago" else "%d hours ago",
            hours,
        )
        days < 7 -> formatAgo(
            useSystemLanguage,
            if (days == 1L) "%d day ago" else "%d days ago",
            days,
        )
        else -> formatAgo(
            useSystemLanguage,
            if (weeks == 1L) "%d week ago" else "%d weeks ago",
            weeks,
        )
    }
}

private fun formatAgo(useSystemLanguage: Boolean, key: String, amount: Long): String {
    return if (useSystemLanguage) {
        Localization.systemFormat(key, amount)
    } else {
        Localization.format(key, amount)
    }
}
