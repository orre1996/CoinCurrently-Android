package com.cryptium.coincurrently.core.models

enum class PreferredTimeframe {
    DAILY,
    WEEKLY,
    MONTHLY;

    val title: String
        get() = when (this) {
            DAILY -> "Daily"
            WEEKLY -> "Weekly"
            MONTHLY -> "Monthly"
        }

    val changeLabel: String
        get() = when (this) {
            DAILY -> "24h"
            WEEKLY -> "7d"
            MONTHLY -> "30d"
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
    More;

    fun getName(): String {
        return when (this) {
            LastUsed -> "Last used"
            Home -> "Home"
            Portfolio -> "Portfolio"
            Market -> "Market"
            else -> "Settings"
        }
    }
}
