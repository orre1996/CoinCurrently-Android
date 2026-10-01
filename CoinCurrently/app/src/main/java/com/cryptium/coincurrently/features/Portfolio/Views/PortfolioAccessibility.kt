package com.cryptium.coincurrently.features.Portfolio.Views
import com.cryptium.coincurrently.core.localization.Localization
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
    return Localization.format(
        "first 4 in address %1\$@, last 4 in address %2\$@",
        firstFour,
        lastFour,
    )
}

fun portfolioChangeAccessibilityLabel(
    title: String,
    value: Double?,
    percent: Double?,
    hideData: Boolean,
    isEmpty: Boolean = false,
): String {
    val changePercent = if (isEmpty) 0.0 else (percent ?: 0.0)
    val safeChange = if (changePercent.isInfinite()) 0.0 else changePercent
    val decimals = safeChange.changePercentDecimalPoints
    val accessiblePercent = abs(safeChange).numberAccessible(maxPlaces = decimals)
    val direction = Localization.string(safeChange.directionAccessible)
    val percentText = Localization.format("%1\$@ %2\$@ percent", direction, accessiblePercent)

    if (hideData) {
        return Localization.format("%1\$@, %2\$@", title, percentText)
    }

    val changeValue = if (isEmpty) 0.0 else (value ?: 0.0)
    val valueDirection = Localization.string(changeValue.directionAccessible)
    val valueText = abs(changeValue).formatCurrency(accessible = true)
    return Localization.format("%1\$@, %2\$@ %3\$@, %4\$@", title, valueDirection, valueText, percentText)
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
    val safeChange = if (profitPercent == null || profitPercent.isInfinite()) 0.0 else profitPercent
    val decimals = safeChange.changePercentDecimalPoints
    val accessiblePercent = abs(safeChange).numberAccessible(maxPlaces = decimals)
    val percentText = Localization.format(
        "%1\$@ %2\$@ percent",
        Localization.string(safeChange.directionAccessible),
        accessiblePercent,
    )

    if (hideData) {
        return Localization.format("Profit/Loss, %@", percentText)
    }

    val changeValue = profitValue ?: 0.0
    val valueText = abs(changeValue).formatCurrency(accessible = true)
    return Localization.format(
        "Profit/Loss, %1\$@ %2\$@, %3\$@",
        Localization.string(changeValue.directionAccessible),
        valueText,
        percentText,
    )
}

fun walletPriceChangeAccessibilityLabel(change: Double?): String {
    val safeChange = if (change?.isInfinite() == true) 0.0 else (change ?: 0.0)
    val decimals = safeChange.changePercentDecimalPoints
    val accessiblePercent = abs(safeChange).numberAccessible(maxPlaces = decimals)
    return Localization.format(
        "%@ price change: %@ %@ percent",
        Localization.string("7 days"),
        Localization.string(safeChange.directionAccessible),
        accessiblePercent,
    )
}
