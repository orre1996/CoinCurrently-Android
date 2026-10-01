package com.cryptium.coincurrently.features.CoinDetail.Views.Compare
import com.cryptium.coincurrently.core.localization.Localization
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
    return Localization.format("%1\$@: %2\$@", name ?: "", priceText)
}

fun accessibleCompareVolumeLabel(name: String?, value: Double?): String {
    val volumeText = value?.formatCurrency(accessible = true) ?: ""
    return Localization.format("%1\$@: %2\$@", name ?: "", volumeText)
}

fun accessibleCompareMarketCapLabel(name: String?, value: Double?, rank: Int?): String {
    val marketCap = value?.formatCurrency(accessible = true) ?: ""
    val rankLabel = rank?.let { Localization.format("rank %d", it) } ?: Localization.string("No rank")
    return Localization.format("%1$@: %2$@, %3$@", name ?: "", marketCap, rankLabel)
}

fun accessibleComparePriceChangeLabel(
    name: String?,
    change: Double?,
    selectedTimeframe: TimeCharts,
): String {
    val timeframe = Localization.string(selectedTimeframe.accessibilityTimeframe)
    if (change == null) {
        return Localization.format(
            "%1$@: %2$@ price change: %3$@",
            name ?: "",
            timeframe,
            Localization.string("Not available"),
        )
    }

    val safeChange = if (change.isInfinite()) 0.0 else change
    val accessibleChange = abs(safeChange).numberAccessible(maxPlaces = safeChange.changePercentDecimalPoints)
    return Localization.format(
        "%1$@: %2$@ price change: %3$@ %4$@ percent",
        name ?: "",
        timeframe,
        Localization.string(safeChange.directionAccessible),
        accessibleChange,
    )
}

fun accessibleCompareSupplyLabel(name: String?, value: Double?): String {
    val supplyText = value?.formatGroupedWholeNumber() ?: ""
    return Localization.format("%1\$@: %2\$@", name ?: "", supplyText)
}

fun accessibleDaysAgoLabel(name: String?, daysAgo: String?): String {
    return Localization.format("%1$@: %2$@", name ?: "", daysAgo ?: Localization.string("Unknown"))
}

fun accessibleGenesisDateLabel(name: String?, date: String?): String {
    return Localization.format("%1$@: %2$@", name ?: "", date ?: Localization.string("Unknown"))
}

fun accessibleBullishLabel(name: String?, percent: Double?): String {
    val formattedPercent = (percent ?: 0.0).roundToInt().toString()
    return Localization.format("%1\$@: %2\$@ percent bullish", name ?: "", formattedPercent)
}

fun accessibleAllTimeRangeLabel(name: String?): String {
    return Localization.format("%@ All-Time Range", name ?: "")
}
