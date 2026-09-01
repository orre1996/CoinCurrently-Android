package com.cryptium.coincurrently.features.CoinDetail.Views.Compare
import com.cryptium.coincurrently.core.extensions.changePercentDecimalPoints
import com.cryptium.coincurrently.core.extensions.directionAccessible
import com.cryptium.coincurrently.core.extensions.formatCurrency
import com.cryptium.coincurrently.core.extensions.formatGroupedWholeNumber
import com.cryptium.coincurrently.core.extensions.numberAccessible
import com.cryptium.coincurrently.sharedui.models.FormattingThreshold
import com.cryptium.coincurrently.core.networking.ResponseModels.CurrencySelectionModel
import com.cryptium.coincurrently.core.networking.ResponseModels.TimeCharts
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
