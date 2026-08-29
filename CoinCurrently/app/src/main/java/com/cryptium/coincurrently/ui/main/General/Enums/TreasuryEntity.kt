package com.cryptium.coincurrently.ui.main.General.Enums

enum class TreasuryEntity(val rawValue: String) {
    Companies("companies"),
    Governments("governments");

    val apiEntityType: String
        get() = when (this) {
            Companies -> "company"
            Governments -> "government"
        }

    val displayName: String
        get() = when (this) {
            Companies -> "Company"
            Governments -> "Government"
        }
}

enum class TreasuryEntityFilter {
    All,
    Companies,
    Governments;

    val title: String
        get() = when (this) {
            All -> "All"
            Companies -> "Company"
            Governments -> "Government"
        }

    val accessibilityTitle: String
        get() = when (this) {
            All -> "All holders"
            Companies -> "Companies only"
            Governments -> "Governments only"
        }
}
