package com.cryptium.coincurrently.ui.main.HomeTab.GasFees.Components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import com.cryptium.coincurrently.ui.main.Compose.AccessibilityLayout
import com.cryptium.coincurrently.ui.main.Compose.AdaptiveStack
import com.cryptium.coincurrently.ui.main.Compose.Separator
import com.cryptium.coincurrently.ui.main.Compose.onSurfaceDim
import com.cryptium.coincurrently.ui.main.Compose.primaryBackgroundNoRipple
import com.cryptium.coincurrently.ui.main.Compose.shimmeringModifier
import com.cryptium.coincurrently.ui.main.Extensions.formatCurrency
import com.cryptium.coincurrently.ui.main.Extensions.numberAccessible
import com.cryptium.coincurrently.ui.main.Extensions.satoshiBold
import networking.ResponseModels.CurrencySelectionModel

@Composable
fun EstimateGasFeeCost(
    prices: List<Double>
) {
    val isHorizontal = AccessibilityLayout.isHorizontal

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .primaryBackgroundNoRipple()
    ) {
        AdaptiveStack(
            isHorizontal = isHorizontal,
            spacing = 16.dp,
            horizontalAlignment = Alignment.Start,
            verticalAlignment = Alignment.Top,
        ) {
            GasFeeCostView(
                title = "Today",
                value = prices.getOrNull(0),
                modifier = sectionModifier(),
            )

            if (!isHorizontal) {
                Separator()
            }

            GasFeeCostView(
                title = "Yesterday",
                value = prices.getOrNull(1),
                modifier = sectionModifier(),
            )
        }

        Separator()

        AdaptiveStack(
            isHorizontal = isHorizontal,
            spacing = 16.dp,
            horizontalAlignment = Alignment.Start,
            verticalAlignment = Alignment.Top,
        ) {
            GasFeeCostView(
                title = "Last Week",
                value = prices.getOrNull(6),
                modifier = sectionModifier(),
            )

            if (!isHorizontal) {
                Separator()
            }

            GasFeeCostView(
                title = "Last Month",
                value = prices.getOrNull(30),
                modifier = sectionModifier(),
            )
        }
    }
}

@Composable
private fun GasFeeCostView(
    title: String,
    value: Double?,
    modifier: Modifier
) {
    val currentStatus = getGasIndexString(indexValue = value.toString())
    val formattedValue = value?.formatCurrency(
        currency = CurrencySelectionModel.empty(),
        minDecimals = 3,
        maxDecimals = 3,
    ) ?: "00000"
    val accessibleValue = value?.numberAccessible(maxPlaces = 2) ?: "0"
    val accessibleDescription = historicAccessibilityLabel(
        title = title,
        accessibleValue = accessibleValue,
        status = currentStatus,
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(0.dp),
        modifier = modifier
            .clearAndSetSemantics {
                contentDescription = accessibleDescription
            }
    ) {
        Text(
            text = title,
            style = satoshiBold(12),
            color = MaterialTheme.colors.onSurfaceDim,
            modifier = Modifier
                .padding(bottom = 4.dp)
                .semantics { invisibleToUser() },
        )

        Text(
            text = "$formattedValue Gwei",
            style = satoshiBold(16),
            color = MaterialTheme.colors.onSurface,
            modifier = Modifier
                .shimmeringModifier(value == null)
                .semantics { invisibleToUser() },
        )

        Text(
            text = currentStatus,
            style = satoshiBold(12),
            color = getGasIndexColor(indexValue = value.toString()),
            modifier = Modifier
                .shimmeringModifier(value == null)
                .semantics { invisibleToUser() },
        )
    }
}

private fun historicAccessibilityLabel(
    title: String,
    accessibleValue: String,
    status: String,
): String {
    return if (title == "Today") {
        "The current average ethereum gas fee is $accessibleValue Gwei. $status"
    } else {
        "The average ethereum gas fee was $accessibleValue Gwei $title. $status"
    }
}
