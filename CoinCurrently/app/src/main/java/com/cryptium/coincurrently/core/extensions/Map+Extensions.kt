package com.cryptium.coincurrently.core.extensions
import com.cryptium.coincurrently.core.persistence.UserPreferences

fun Map<String, Double>.getSelectedCurrency(currency: String? = null): Double? {
    return this.get(currency?.lowercase() ?: (UserPreferences.getSelectedCurrency().code.lowercase()))
}

fun Map<String, String>.getSelectedCurrency(currency: String? = null): String? {
    return this.get(currency?.lowercase() ?: (UserPreferences.getSelectedCurrency().code.lowercase()))
}