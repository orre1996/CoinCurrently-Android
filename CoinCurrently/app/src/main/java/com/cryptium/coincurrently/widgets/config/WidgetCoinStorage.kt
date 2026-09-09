package com.cryptium.coincurrently.widgets.config

import android.content.Context
import com.cryptium.coincurrently.core.persistence.UserPreferences

object WidgetCoinStorage {
    val hardcodedFallbackIds: List<String> = listOf(
        "bitcoin",
        "ethereum",
        "solana",
        "tether",
        "ripple",
        "usd-coin",
        "binancecoin",
        "cardano",
    )

    fun getSavedCoinIds(context: Context? = null): List<String> {
        return UserPreferences.getWidgetCurrency(context).mapNotNull { it.id?.takeIf { id -> id.isNotEmpty() } }
    }
}
