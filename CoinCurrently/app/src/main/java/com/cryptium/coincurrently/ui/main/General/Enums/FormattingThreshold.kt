package com.cryptium.coincurrently.ui.main.General.Enums

enum class FormattingThreshold(val rawValue: Double, val symbol: String) {
    TRILLION(1_000_000_000_000.0, "T"),
    BILLION(1_000_000_000.0, "B"),
    MILLION(1_000_000.0, "M"),
    THOUSAND(1_000.0, "K"),
    NONE(1.0, "");

    val spellOutSuffix: String
        get() = when (symbol) {
            "T" -> "trillion"
            "B" -> "billion"
            "M" -> "million"
            "K" -> "thousand"
            else -> ""
        }

    companion object {
        val valuesDescending = values().sortedByDescending { it.rawValue }
    }
}