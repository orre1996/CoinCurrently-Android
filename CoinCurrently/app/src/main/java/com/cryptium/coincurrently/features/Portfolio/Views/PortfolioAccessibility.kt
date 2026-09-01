package com.cryptium.coincurrently.features.Portfolio.Views
import com.cryptium.coincurrently.core.networking.ResponseModels.AddressImport
import com.cryptium.coincurrently.core.extensions.changePercentDecimalPoints
import com.cryptium.coincurrently.core.extensions.directionAccessible
import com.cryptium.coincurrently.core.extensions.formatCurrency
import com.cryptium.coincurrently.core.extensions.numberAccessible
import com.cryptium.coincurrently.core.extensions.symbolAccessible
import com.cryptium.coincurrently.core.networking.ResponseModels.CurrencySelectionModel
import kotlin.math.abs

fun accessibilityAddressDescription(wallet: AddressImport): String {
    val formattedAddress = wallet.address.uppercase()
    val firstFour = formattedAddress.take(4)
    val lastFour = formattedAddress.takeLast(4)
    return "first 4 in address $firstFour, last 4 in address $lastFour"
}

fun portfolioChangeAccessibilityLabel(
    title: String,
    value: Double?,
    percent: Double?,
    hideData: Boolean,
    isEmpty: Boolean = false,
): String {
    val changePercent = if (isEmpty) 0.0 else (percent ?: 0.0)
    val percentText = if (changePercent.isInfinite()) {
        null
    } else {
        val decimals = changePercent.changePercentDecimalPoints
        val accessiblePercent = abs(changePercent).numberAccessible(maxPlaces = decimals)
        val direction = changePercent.directionAccessible.lowercase()
        "$direction $accessiblePercent percent"
    }

    if (hideData) {
        return if (percentText != null) "$title, data hidden, $percentText" else "$title, data hidden"
    }

    val changeValue = if (isEmpty) 0.0 else (value ?: 0.0)
    val valueDirection = changeValue.directionAccessible.lowercase()
    val valueText = abs(changeValue).formatCurrency(accessible = true)

    return if (percentText != null) {
        "$title. $valueDirection $valueText, $percentText"
    } else {
        "$title. $valueDirection $valueText"
    }
}

fun accessibleCoinAmountLabel(amount: Double, coinName: String): String {
    val amountText = amount.formatCurrency(currency = CurrencySelectionModel.empty(), accessible = true)
    val label = coinName.lowercase()
    if (label.isEmpty()) return amountText
    return "$amountText $label"
}

fun accessibleCoinAmountWithSymbol(amount: Double, symbol: String?): String {
    val absAmount = abs(amount)
    val decimals = if (absAmount < 1) 7 else 2
    val amountText = absAmount.numberAccessible(maxPlaces = decimals)
    val symbolText = symbol?.trim()?.uppercase()?.symbolAccessible.orEmpty()
    if (symbolText.isEmpty()) return amountText
    return "$amountText $symbolText"
}

fun profitLossAccessibilityLabel(
    profitValue: Double?,
    profitPercent: Double?,
    hideData: Boolean,
): String {
    return portfolioChangeAccessibilityLabel(
        title = "Profit loss",
        value = profitValue,
        percent = profitPercent,
        hideData = hideData,
    )
}

fun walletPriceChangeAccessibilityLabel(change: Double?): String {
    val safeChange = if (change?.isInfinite() == true) 0.0 else (change ?: 0.0)
    val decimals = safeChange.changePercentDecimalPoints
    val accessiblePercent = abs(safeChange).numberAccessible(maxPlaces = decimals)
    val direction = safeChange.directionAccessible.lowercase()
    return "7 days price change: $direction $accessiblePercent percent"
}
