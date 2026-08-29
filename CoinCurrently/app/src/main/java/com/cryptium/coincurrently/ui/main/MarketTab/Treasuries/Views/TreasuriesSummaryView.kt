package com.cryptium.coincurrently.ui.main.MarketTab.Treasuries.Views

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
import com.cryptium.coincurrently.ui.main.Compose.AccessibilityLayout
import com.cryptium.coincurrently.ui.main.Compose.onSurfaceDim
import com.cryptium.coincurrently.ui.main.Extensions.formatCurrency
import com.cryptium.coincurrently.ui.main.Extensions.numberAccessible
import com.cryptium.coincurrently.ui.main.Extensions.roundTo
import com.cryptium.coincurrently.ui.main.Extensions.satoshiBold
import com.cryptium.coincurrently.ui.main.General.Enums.FormattingThreshold
import com.cryptium.coincurrently.ui.main.Compose.shimmeringModifier
import networking.ResponseModels.CurrencySelectionModel

@Composable
fun TreasuriesSummaryView(
    totalHoldings: Double?,
    marketCapDominance: Double?,
    coinSymbol: String?,
    modifier: Modifier = Modifier,
) {
    val isHorizontal = !AccessibilityLayout.isHorizontal
    val holdingsCurrency = holdingsCurrency(coinSymbol)
    val formattedTotalHoldings = (totalHoldings ?: 1_272_129.0).formatCurrency(
        threshold = if (isHorizontal) FormattingThreshold.MILLION else FormattingThreshold.NONE,
        currency = holdingsCurrency,
    )
    val roundedDominance = (marketCapDominance ?: 0.0).roundTo(2)
    val formattedDominance = "%.2f".format(roundedDominance)
    val summaryAccessibilityLabel = buildString {
        append(
            "Total Holdings: ${
                (totalHoldings ?: 0.0).formatCurrency(
                    threshold = if (isHorizontal) FormattingThreshold.MILLION else FormattingThreshold.NONE,
                    currency = holdingsCurrency,
                    accessible = true,
                )
            }. "
        )
        append("Market Cap Dominance: ${roundedDominance.numberAccessible(maxPlaces = 2)} percent")
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
            text = "Total Holdings",
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
                text = "Market Cap Dominance",
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
