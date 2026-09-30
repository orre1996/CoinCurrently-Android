package com.cryptium.coincurrently.features.GlobalMarket.Views
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.invisibleToUser
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.cryptium.coincurrently.sharedui.components.AccessibilityLayout
import com.cryptium.coincurrently.sharedui.theme.onSurfaceDim
import com.cryptium.coincurrently.sharedui.modifiers.shimmeringModifier
import com.cryptium.coincurrently.core.extensions.changeColor
import com.cryptium.coincurrently.core.localization.Localization
import com.cryptium.coincurrently.core.extensions.changeWithSymbol
import com.cryptium.coincurrently.core.extensions.formatCurrency
import com.cryptium.coincurrently.core.extensions.getChangeInValue
import com.cryptium.coincurrently.core.extensions.satoshiBold
import com.cryptium.coincurrently.sharedui.components.PriceChangeValueView
import com.cryptium.coincurrently.sharedui.models.FormattingThreshold
import com.cryptium.coincurrently.sharedui.loading.ChangeSize
import com.cryptium.coincurrently.core.networking.ResponseModels.TimeCharts


@Composable
fun GlobalmarketSummaryView(
    marketCap: Double?,
    changePercent: Double?,
    selectedTimeframe: TimeCharts
) {
    val talkBackEnabled = AccessibilityLayout.isTalkBackEnabled
    val isHorizontal = AccessibilityLayout.isHorizontal
    val formattingThreshold = if (isHorizontal) FormattingThreshold.NONE else FormattingThreshold.MILLION
    val marketCapAccessible = (marketCap ?: 0.0).formatCurrency(
        threshold = formattingThreshold,
        maxDecimals = if ((marketCap ?: 0.0) >= 1_000_000_000) 0 else 2,
        accessible = true,
    )

    val safeChange = if (changePercent?.isInfinite() == true) 0.0 else changePercent
    val changeDirection = safeChange?.let { if (it >= 0) "Up" else "Down" } ?: ""
    val accessibleChange = safeChange?.let { "%.2f".format(it) } ?: ""
    val accessibilityLabel = Localization.format(
        "Market Cap, market cap: %@. %@ market cap change: %@ %@ percent",
        marketCapAccessible,
        Localization.string(selectedTimeframe.accessibilityTimeframe),
        if (changeDirection.isEmpty()) "" else Localization.string(changeDirection),
        accessibleChange,
    )
    val childModifier = if (talkBackEnabled) {
        Modifier.semantics { invisibleToUser() }
    } else {
        Modifier
    }

    Column(
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
            .padding(16.dp)
            .then(
                if (talkBackEnabled) {
                    Modifier.clearAndSetSemantics {
                        contentDescription = accessibilityLabel
                    }
                } else {
                    Modifier
                }
            )
    ) {
        Text(
            text = Localization.string("Market Cap"),
            style = satoshiBold(18),
            color = MaterialTheme.colors.onSurfaceDim,
            modifier = childModifier,
        )

        Text(
            text = marketCap?.formatCurrency(
                threshold = formattingThreshold,
                maxDecimals = if ((marketCap ?: 0.0) >= 1_000_000_000) 0 else 2,
            ) ?: "00000000",
            style = satoshiBold(36),
            color = MaterialTheme.colors.onSurfaceDim,
            modifier = childModifier
                .shimmeringModifier(marketCap == null)
        )

        ChangeView(
            currentPrice = marketCap,
            change = safeChange,
            selectedTimeframe = selectedTimeframe,
            talkBackEnabled = talkBackEnabled,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ChangeView(
    currentPrice: Double?,
    change: Double?,
    selectedTimeframe: TimeCharts,
    talkBackEnabled: Boolean = false,
) {
    val roundedChange = change ?: 0.0
    val formattedChange = "%.2f".format(roundedChange).changeWithSymbol

    val changeValue = roundedChange.getChangeInValue(currentValue = currentPrice ?: 0.0)
    val childModifier = if (talkBackEnabled) {
        Modifier.semantics { invisibleToUser() }
    } else {
        Modifier
    }

    FlowRow(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = childModifier,
    ) {
        PriceChangeValueView(
            change = changeValue,
            size = ChangeSize.EXTRA_SMALL,
            showDirection = true,
            isDownloading = currentPrice == null || change == null,
            modifier = childModifier
        )

        Text(
            text = "($formattedChange%)",
            style = satoshiBold(12),
            color = roundedChange.changeColor(),
            modifier = childModifier
                .shimmeringModifier(change == null)
        )

        Text(
            text = Localization.string(selectedTimeframe.timeAgoText),
            style = satoshiBold(12),
            color = MaterialTheme.colors.onSurfaceDim,
            modifier = childModifier,
        )
    }
}
