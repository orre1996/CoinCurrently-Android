package com.cryptium.coincurrently.sharedui.models
enum class PriceChange(val rawValue: String) {
    OneHour("1h"),
    OneDay("24h"),
    SevenDays("7d"),
    OneMonth("30d");

    val subtitle: String
        get() = when (this) {
            OneHour -> "Price change in the last hour"
            OneDay -> "Price change in the last 24 hours"
            SevenDays -> "Price change in the last 7 days"
            OneMonth -> "Price change in the last 30 days"
        }

    val accessibilityTimeframe: String
        get() = when (this) {
            OneHour -> "1 hour"
            OneDay -> "24 hour"
            SevenDays -> "7 days"
            OneMonth -> "30 days"
        }
}
