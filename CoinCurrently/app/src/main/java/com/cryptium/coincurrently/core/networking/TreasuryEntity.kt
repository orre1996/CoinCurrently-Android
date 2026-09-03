package com.cryptium.coincurrently.core.networking

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
