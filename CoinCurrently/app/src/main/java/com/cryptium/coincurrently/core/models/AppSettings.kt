package com.cryptium.coincurrently.core.models

enum class PreferredTimeframe(value: Int) {
    DAILY(0),
    WEEKLY(1),
    MONTHLY(2);

    val title: String
        get() = when (this) {
            DAILY -> "Daily"
            WEEKLY -> "Weekly"
            MONTHLY -> "Monthly"
        }

    val changeTitle: String
        get() = when (this) {
            DAILY -> "24h:"
            WEEKLY -> "7d:"
            MONTHLY -> "30d:"
        }

    val changeLabel: String
        get() = when (this) {
            DAILY -> "24h"
            WEEKLY -> "7d"
            MONTHLY -> "30d"
        }

    val daysRaw: String
        get() = when (this) {
            DAILY -> "1"
            WEEKLY -> "7"
            MONTHLY -> "30"
        }

    fun chartIndex(fromCryptoDetail: Boolean = false): Int = when (this) {
        DAILY -> if (fromCryptoDetail) 4 else 0
        WEEKLY -> if (fromCryptoDetail) 5 else 1
        MONTHLY -> if (fromCryptoDetail) 6 else 2
    }
}

enum class AppColorScheme(val displayName: String) {
    LIGHT("Light Mode"),
    DARK("Dark Mode"),
    SYSTEM("System");

    fun resolvedColorScheme(isSystemDark: Boolean): Boolean =
        when (this) {
            LIGHT -> false
            DARK -> true
            SYSTEM -> isSystemDark
        }

    val preferredScheme: Boolean?
        get() = when (this) {
            SYSTEM -> null
            LIGHT -> false
            DARK -> true
        }
}

enum class SelectedStartTab {
    LastUsed,
    Home,
    Portfolio,
    Market,
    News,
    More;

    fun getName(): String {
        return when (this) {
            LastUsed -> "Last used"
            Home -> "Home"
            Portfolio -> "Portfolio"
            Market -> "Market"
            News -> "News"
            else -> "Settings"
        }
    }
}
