package com.cryptium.coincurrently.ui.main.General.Components.Chart

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.focusable
import androidx.compose.runtime.remember
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.invisibleToUser
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.zIndex
import com.cryptium.coincurrently.ui.main.Compose.AccessibilityLayout
import com.cryptium.coincurrently.ui.main.Extensions.accessibleChartHoverDate
import com.cryptium.coincurrently.ui.main.Extensions.chartAxisTimestampIndex
import com.cryptium.coincurrently.ui.main.Extensions.currencyAccessible
import com.cryptium.coincurrently.ui.main.Extensions.directionAccessible
import com.cryptium.coincurrently.ui.main.Extensions.formatCurrency
import com.cryptium.coincurrently.ui.main.Extensions.getSelectedCurrency
import com.cryptium.coincurrently.ui.main.Extensions.is24HourClock
import com.cryptium.coincurrently.ui.main.Extensions.numberAccessible
import com.cryptium.coincurrently.ui.main.General.GlobalFunctions
import networking.ResponseModels.CurrencySelectionModel
import networking.ResponseModels.TimeCharts
import java.util.Date

internal const val CHART_TIMEFRAME_TALKBACK_TRAVERSAL_INDEX = 0f
internal const val CHART_ACCESSIBILITY_OVERLAY_TALKBACK_TRAVERSAL_INDEX = 1f
internal const val DEFERRED_AFTER_CHART_OVERLAY_TALKBACK_TRAVERSAL_INDEX = 2f
internal const val TAB_PAGE_CONTENT_TALKBACK_TRAVERSAL_INDEX = 3f
internal const val ACTION_BUTTON_TALKBACK_TRAVERSAL_INDEX = 4f

@Composable
fun Modifier.deferTalkBackAfterChartAccessibility(): Modifier {
    val talkBackEnabled = AccessibilityLayout.isTalkBackEnabled
    return if (talkBackEnabled) {
        this.semantics {
            traversalIndex = DEFERRED_AFTER_CHART_OVERLAY_TALKBACK_TRAVERSAL_INDEX
        }
    } else {
        this
    }
}

@Composable
fun Modifier.deferTalkBackAfterTabContent(): Modifier {
    val talkBackEnabled = AccessibilityLayout.isTalkBackEnabled
    return if (talkBackEnabled) {
        this.semantics {
            traversalIndex = ACTION_BUTTON_TALKBACK_TRAVERSAL_INDEX
        }
    } else {
        this
    }
}

@Composable
fun Modifier.chartTalkBackSection(): Modifier {
    val talkBackEnabled = AccessibilityLayout.isTalkBackEnabled
    return if (talkBackEnabled) {
        this.semantics { isTraversalGroup = true }
    } else {
        this
    }
}

@Composable
fun Modifier.chartAccessibilityOverlay(
    timeStamps: List<Double>,
    prices: List<Double>,
    timeChart: TimeCharts,
    trailingPadding: Dp = Dp.Unspecified,
    comparePrices: List<Double>? = null,
    mainSeriesName: String? = null,
    compareSeriesName: String? = null,
    dontShowTime: Boolean = false,
    useExchangeDateFormat: Boolean = false,
    currency: CurrencySelectionModel = GlobalFunctions.getSelectedCurrency(),
    priceFormatter: ((Double) -> String)? = null,
    showPercentChangeFromStart: Boolean = false,
): Modifier {
    val talkBackEnabled = AccessibilityLayout.isTalkBackEnabled
    if (!talkBackEnabled || timeStamps.isEmpty()) return this

    return this.then(
        Modifier.semantics {
            isTraversalGroup = true
            traversalIndex = CHART_ACCESSIBILITY_OVERLAY_TALKBACK_TRAVERSAL_INDEX
        }
    )
}

@Composable
fun ChartAccessibilityOverlay(
    timeStamps: List<Double>,
    prices: List<Double>,
    timeChart: TimeCharts,
    trailingPadding: Dp = Dp.Unspecified,
    comparePrices: List<Double>? = null,
    mainSeriesName: String? = null,
    compareSeriesName: String? = null,
    dontShowTime: Boolean = false,
    useExchangeDateFormat: Boolean = false,
    currency: CurrencySelectionModel = GlobalFunctions.getSelectedCurrency(),
    priceFormatter: ((Double) -> String)? = null,
    showPercentChangeFromStart: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val talkBackEnabled = AccessibilityLayout.isTalkBackEnabled
    if (!talkBackEnabled) return

    val sectionCount = minOf(9, timeStamps.size)
    if (sectionCount <= 0) return

    val context = LocalContext.current
    val is24HourClock = context.is24HourClock

    Row(
        modifier = modifier
            .fillMaxSize()
            .zIndex(1f)
            .then(
                if (trailingPadding != Dp.Unspecified) {
                    Modifier.padding(end = trailingPadding)
                } else {
                    Modifier
                }
            )
    ) {
        for (sectionIndex in 1..sectionCount) {
            val label = accessibilityLabel(
                sectionIndex = sectionIndex,
                sectionCount = sectionCount,
                timeStamps = timeStamps,
                prices = prices,
                timeChart = timeChart,
                comparePrices = comparePrices,
                mainSeriesName = mainSeriesName,
                compareSeriesName = compareSeriesName,
                dontShowTime = dontShowTime,
                useExchangeDateFormat = useExchangeDateFormat,
                currency = currency,
                priceFormatter = priceFormatter,
                showPercentChangeFromStart = showPercentChangeFromStart,
                is24HourClock = is24HourClock,
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .focusable()
                    .clearAndSetSemantics {
                        contentDescription = label
                        traversalIndex = (sectionIndex - 1).toFloat()
                    }
            )
        }
    }
}

@Composable
fun Modifier.delayTouches(): Modifier {
    val talkBackEnabled = AccessibilityLayout.isTalkBackEnabled
    if (talkBackEnabled) return this

    return this.clickable(
        indication = null,
        interactionSource = remember { MutableInteractionSource() },
        onClick = {},
    )
}

@Composable
fun Modifier.hideFromTalkBackWhenEnabled(): Modifier {
    val talkBackEnabled = AccessibilityLayout.isTalkBackEnabled
    return if (talkBackEnabled) {
        this.semantics(mergeDescendants = true) { invisibleToUser() }
    } else {
        this
    }
}

private fun accessibilityLabel(
    sectionIndex: Int,
    sectionCount: Int,
    timeStamps: List<Double>,
    prices: List<Double>,
    timeChart: TimeCharts,
    comparePrices: List<Double>?,
    mainSeriesName: String?,
    compareSeriesName: String?,
    dontShowTime: Boolean,
    useExchangeDateFormat: Boolean,
    currency: CurrencySelectionModel,
    priceFormatter: ((Double) -> String)?,
    showPercentChangeFromStart: Boolean,
    is24HourClock: Boolean,
): String {
    val dataIndex = chartAxisTimestampIndex(
        labelIndex = sectionIndex,
        labelCount = sectionCount,
        dataCount = timeStamps.size,
    )

    val date = formattedDate(
        dataIndex = dataIndex,
        timeStamps = timeStamps,
        timeChart = timeChart,
        dontShowTime = dontShowTime,
        useExchangeDateFormat = useExchangeDateFormat,
        is24HourClock = is24HourClock,
    )
    val priceText = formattedPrice(
        dataIndex = dataIndex,
        prices = prices,
        currency = currency,
        priceFormatter = priceFormatter,
        showPercentChangeFromStart = showPercentChangeFromStart,
    )

    if (comparePrices != null) {
        val comparePriceText = formattedPrice(
            dataIndex = dataIndex,
            prices = comparePrices,
            currency = currency,
            priceFormatter = priceFormatter,
            showPercentChangeFromStart = showPercentChangeFromStart,
        )
        val mainLabel = labeledPrice(mainSeriesName, priceText)
        val compareLabel = labeledPrice(compareSeriesName, comparePriceText)
        return "$date, $mainLabel, $compareLabel"
    }

    return "$date, $priceText"
}

private fun labeledPrice(name: String?, price: String): String {
    if (name.isNullOrEmpty()) return price
    return "$name: $price"
}

private fun formattedDate(
    dataIndex: Int,
    timeStamps: List<Double>,
    timeChart: TimeCharts,
    dontShowTime: Boolean,
    useExchangeDateFormat: Boolean,
    is24HourClock: Boolean,
): String {
    val timestamp = timeStamps.getOrNull(dataIndex) ?: 0.0
    val date = Date(timestamp.toLong())
    return date.accessibleChartHoverDate(
        timeChart = timeChart,
        dontShowTime = dontShowTime,
        useExchangeFormat = useExchangeDateFormat,
        is24HourClock = is24HourClock,
    )
}

private fun formattedPrice(
    dataIndex: Int,
    prices: List<Double>,
    currency: CurrencySelectionModel,
    priceFormatter: ((Double) -> String)?,
    showPercentChangeFromStart: Boolean,
): String {
    val price = prices.getOrNull(dataIndex)
    if (price == null || price == -1.0) return "N/A"

    var priceText = priceFormatter?.invoke(price)
        ?: if (currency.readAccessibilityAsSymbol) {
            price.currencyAccessible(currency = currency)
        } else {
            price.formatCurrency(currency = currency, accessible = true)
        }

    if (showPercentChangeFromStart) {
        val firstPrice = prices.firstOrNull { it != -1.0 }
        if (firstPrice != null) {
            val change = ((price - firstPrice) / firstPrice) * 100
            val safeChange = if (change.isInfinite()) 0.0 else change
            val accessibleChange = safeChange.numberAccessible(maxPlaces = 2)
            val direction = safeChange.directionAccessible.lowercase()
            priceText += ", $direction $accessibleChange percent"
        }
    }

    return priceText
}
