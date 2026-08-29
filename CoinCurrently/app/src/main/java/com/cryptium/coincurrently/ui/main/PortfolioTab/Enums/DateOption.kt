package com.cryptium.coincurrently.ui.main.PortfolioTab.Enums

import com.cryptium.coincurrently.AppLocale
import java.text.SimpleDateFormat
import java.util.Date

sealed class DateOption {

    object All : DateOption()
    object LastWeek : DateOption()
    object LastMonth : DateOption()
    object LastThreeMonths : DateOption()
    object LastSixMonths : DateOption()
    object LastYear : DateOption()
    object YearToDate : DateOption()

    data class Custom(
        val startDate: Date?,
        val endDate: Date?
    ) : DateOption()

    companion object {
        val allCases: List<DateOption> = listOf(
            All,
            LastWeek,
            LastMonth,
            LastThreeMonths,
            LastSixMonths,
            LastYear,
            YearToDate,
            Custom(Date(), Date())
        )
    }
}

val DateOption.startDate: Date?
    get() = when (this) {
        is DateOption.Custom -> startDate
        else -> null
    }

val DateOption.endDate: Date?
    get() = when (this) {
        is DateOption.Custom -> endDate
        else -> null
    }

val DateOption.type: DateOption
    get() = when (this) {
        is DateOption.Custom ->
            DateOption.Custom(
                startDate = Date(0),               // distantPast
                endDate = Date(Long.MAX_VALUE)     // distantFuture
            )
        else -> this
    }

val DateOption.title: String
    get() = when (this) {
        DateOption.All -> "All"
        DateOption.LastWeek -> "Last week"
        DateOption.LastMonth -> "Last month"
        DateOption.LastThreeMonths -> "Last 3 months"
        DateOption.LastSixMonths -> "Last 6 months"
        DateOption.LastYear -> "Last year"
        DateOption.YearToDate -> "Year to date"
        is DateOption.Custom -> "Custom"
    }

val DateOption.rangeTitle: String
    get() = when (this) {
        DateOption.All -> "Date"

        is DateOption.Custom -> {
            val start = startDate?.formatAbbreviated() ?: ""
            val end = endDate?.formatAbbreviated() ?: ""
            "$start - $end"
        }

        else -> title
    }

fun Date.formatAbbreviated(): String {
    val formatter = SimpleDateFormat("MMM d, yyyy", AppLocale.enUsPosix)
    return formatter.format(this)
}