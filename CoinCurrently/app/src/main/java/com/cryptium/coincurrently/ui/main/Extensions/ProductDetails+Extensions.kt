package com.cryptium.coincurrently.ui.main.Extensions

import com.android.billingclient.api.ProductDetails
import com.cryptium.coincurrently.ui.main.Extensions.currencyAccessible
import com.cryptium.coincurrently.ui.main.Extensions.formatCurrency
import com.cryptium.coincurrently.ui.main.SettingsTab.Compose.allCurrencies
import networking.ResponseModels.CurrencySelectionModel

fun ProductDetails.getFormattedPrice(): String {
    val price = this.getIntroductoryOffer()?.priceAmountMicros ?: 0
    val currency = this.getIntroductoryOffer()?.priceCurrencyCode ?: "USD"
    val currencyModel = allCurrencies.firstOrNull { it.code.lowercase() == currency.lowercase() }
    return (price.toDouble() / 1000000).formatCurrency(currencySelectionModel = currencyModel)
}

fun ProductDetails.getAccessiblePrice(): String {
    val offer = this.getIntroductoryOffer() ?: return ""
    val currencyModel = allCurrencies.firstOrNull {
        it.code.equals(offer.priceCurrencyCode, ignoreCase = true)
    } ?: CurrencySelectionModel()
    return (offer.priceAmountMicros.toDouble() / 1_000_000).currencyAccessible(currency = currencyModel)
}