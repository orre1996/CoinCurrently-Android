package com.cryptium.coincurrently.core.extensions
import kotlinx.datetime.toJavaLocalTime
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.util.Date

fun LocalDate.toDate(
    hour: Int,
    minute: Int,
    second: Int = 0,
    zone: ZoneId = ZoneId.systemDefault()
): Date {
    return Date.from(
        this.atTime(LocalTime.of(hour, minute, second))
            .atZone(zone)
            .toInstant()
    )
}

fun kotlinx.datetime.LocalTime.toDate(date: Date): Date {
    val javaTime = this.toJavaLocalTime()
    val javaDate = date.toLocalDate()

    return Date.from(
        javaDate
            .atTime(javaTime)
            .atZone(ZoneId.systemDefault())
            .toInstant()
    )
}
