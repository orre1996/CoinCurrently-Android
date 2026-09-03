package com.cryptium.coincurrently.features.Treasuries

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
