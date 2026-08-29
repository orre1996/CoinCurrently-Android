package com.cryptium.coincurrently.ui.main.MarketTab.CryptoDetail.Components.Compare

import com.cryptium.coincurrently.ui.main.Extensions.changePercentDecimalPoints
import com.cryptium.coincurrently.ui.main.Extensions.directionAccessible
import com.cryptium.coincurrently.ui.main.Extensions.formatCurrency
import com.cryptium.coincurrently.ui.main.Extensions.formatGroupedWholeNumber
import com.cryptium.coincurrently.ui.main.Extensions.numberAccessible
import com.cryptium.coincurrently.ui.main.General.Enums.FormattingThreshold
import networking.ResponseModels.CurrencySelectionModel
import networking.ResponseModels.TimeCharts
import kotlin.math.abs
import kotlin.math.roundToInt

fun accessibleComparePriceLabel(name: String?, value: Double?): String {
    val priceText = value?.formatCurrency(threshold = FormattingThreshold.NONE, accessible = true) ?: ""
    return "${name ?: ""}: $priceText"
}

fun accessibleCompareVolumeLabel(name: String?, value: Double?): String {
    val volumeText = value?.formatCurrency(accessible = true) ?: ""
    return "${name ?: ""}: $volumeText"
}

fun accessibleCompareMarketCapLabel(name: String?, value: Double?, rank: Int?): String {
    val marketCap = value?.formatCurrency(accessible = true) ?: ""
    val rankLabel = rank?.let { "rank $it" } ?: "no rank"
    return "${name ?: ""}: $marketCap, $rankLabel"
}

fun accessibleComparePriceChangeLabel(
    name: String?,
    change: Double?,
    selectedTimeframe: TimeCharts,
): String {
    if (change == null) {
        return "${name ?: ""}: ${selectedTimeframe.accessibilityTimeframe} price change: not available"
    }

    val safeChange = if (change.isInfinite()) 0.0 else change
    val accessibleChange = abs(safeChange).numberAccessible(maxPlaces = safeChange.changePercentDecimalPoints)
    val direction = safeChange.directionAccessible.lowercase()
    return "${name ?: ""}: ${selectedTimeframe.accessibilityTimeframe} price change: $direction $accessibleChange percent"
}

fun accessibleCompareSupplyLabel(name: String?, value: Double?): String {
    val supplyText = value?.formatGroupedWholeNumber() ?: ""
    return "${name ?: ""}: $supplyText"
}

fun accessibleRangeEndpointLabel(prefix: String, price: Double?, date: String?): String {
    val priceText = price?.formatCurrency(accessible = true) ?: ""
    val dateText = date ?: "unknown date"
    return "$prefix $priceText on $dateText"
}

fun accessibleDaysAgoLabel(name: String?, daysAgo: String?): String {
    return "${name ?: ""}: ${daysAgo ?: "unknown"}"
}

fun accessibleGenesisDateLabel(name: String?, date: String?): String {
    return "${name ?: ""}: ${date ?: "unknown"}"
}

fun accessibleBullishLabel(name: String?, percent: Double?): String {
    val formattedPercent = (percent ?: 0.0).roundToInt()
    return "${name ?: ""}: $formattedPercent percent bullish"
}

fun accessibleAllTimeRangeLabel(name: String?): String {
    return "${name ?: ""} All-Time Range"
}
