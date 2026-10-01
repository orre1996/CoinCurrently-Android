package com.cryptium.coincurrently.features.Market.Treasuries.Views
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.dp
import com.cryptium.coincurrently.sharedui.components.AccessibilityLayout
import com.cryptium.coincurrently.sharedui.theme.onSurfaceDim
import com.cryptium.coincurrently.core.extensions.formatCurrency
import com.cryptium.coincurrently.core.localization.Localization
import com.cryptium.coincurrently.core.extensions.numberAccessible
import com.cryptium.coincurrently.core.extensions.roundTo
import com.cryptium.coincurrently.core.extensions.satoshiBold
import com.cryptium.coincurrently.sharedui.models.FormattingThreshold
import com.cryptium.coincurrently.sharedui.modifiers.shimmeringModifier
import com.cryptium.coincurrently.core.networking.ResponseModels.CurrencySelectionModel
import com.cryptium.coincurrently.core.persistence.allCurrencies

@Composable
fun TreasuriesSummaryView(
    totalHoldings: Double?,
    marketCapDominance: Double?,
    coinSymbol: String?,
    modifier: Modifier = Modifier,
) {
    val isHorizontal = !AccessibilityLayout.isHorizontal
    val holdingsCurrency = holdingsCurrency(coinSymbol)
    val accessibilityHoldingsCurrency = accessibleHoldingsCurrency(coinSymbol)
    val formattedTotalHoldings = (totalHoldings ?: 1_272_129.0).formatCurrency(
        threshold = if (isHorizontal) FormattingThreshold.MILLION else FormattingThreshold.NONE,
        currency = holdingsCurrency,
    )
    val roundedDominance = (marketCapDominance ?: 0.0).roundTo(2)
    val formattedDominance = "%.2f".format(roundedDominance)
    val summaryAccessibilityLabel = if (totalHoldings == null && marketCapDominance == null) {
        Localization.string("Loading total holdings")
    } else {
        Localization.format(
            "Total Holdings: %@. Market Cap Dominance: %@ percent",
            (totalHoldings ?: 0.0).formatCurrency(
                threshold = if (isHorizontal) FormattingThreshold.MILLION else FormattingThreshold.NONE,
                currency = accessibilityHoldingsCurrency,
                accessible = true,
            ),
            roundedDominance.numberAccessible(maxPlaces = 2),
        )
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier
            .fillMaxWidth()
            .clearAndSetSemantics {
                contentDescription = summaryAccessibilityLabel
            }
            .padding(top = 16.dp)
    ) {
        Text(
            text = Localization.string("Total Holdings"),
            style = satoshiBold(18),
            color = MaterialTheme.colors.onSurfaceDim,
        )

        Text(
            text = formattedTotalHoldings,
            style = satoshiBold(36),
            color = MaterialTheme.colors.onSurface,
            modifier = Modifier.shimmeringModifier(totalHoldings == null),
        )

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = Localization.string("Market Cap Dominance"),
                style = satoshiBold(12),
                color = MaterialTheme.colors.onSurfaceDim,
            )

            Text(
                text = "$formattedDominance%",
                style = satoshiBold(12),
                color = MaterialTheme.colors.onSurface,
                modifier = Modifier.shimmeringModifier(marketCapDominance == null),
            )
        }
    }
}

private fun holdingsCurrency(coinSymbol: String?): CurrencySelectionModel {
    return CurrencySelectionModel(
        currency = "",
        code = coinSymbol?.uppercase() ?: "BTC",
        symbol = coinSymbol?.uppercase() ?: "BTC",
        suffix = true,
    )
}

private fun accessibleHoldingsCurrency(coinSymbol: String?): CurrencySelectionModel {
    val symbol = coinSymbol?.uppercase()
    if (!symbol.isNullOrEmpty()) {
        allCurrencies.firstOrNull { it.code == symbol }?.let { return it }
        return CurrencySelectionModel(
            currency = symbol.map { it.toString() }.joinToString(" "),
            code = symbol,
            symbol = symbol,
            suffix = true,
            readAccessibilityAsSymbol = true,
        )
    }

    return CurrencySelectionModel(
        currency = "",
        code = "BTC",
        symbol = "BTC",
        suffix = true,
    )
}
