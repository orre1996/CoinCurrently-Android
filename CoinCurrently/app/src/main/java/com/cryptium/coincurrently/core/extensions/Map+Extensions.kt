package com.cryptium.coincurrently.core.extensions
import com.cryptium.coincurrently.sharedui.components.GlobalFunctions

fun Map<String, Double>.getSelectedCurrency(currency: String? = null): Double? {
    return this.get(currency?.lowercase() ?: (GlobalFunctions.getSelectedCurrency().code.lowercase()))
}

fun Map<String, String>.getSelectedCurrency(currency: String? = null): String? {
    return this.get(currency?.lowercase() ?: (GlobalFunctions.getSelectedCurrency().code.lowercase()))
}