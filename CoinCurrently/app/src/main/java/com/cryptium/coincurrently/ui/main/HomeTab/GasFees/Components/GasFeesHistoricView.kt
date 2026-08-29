package com.cryptium.coincurrently.ui.main.HomeTab.GasFees.Components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.cryptium.coincurrently.ui.main.Compose.AccessibilityLayout
import com.cryptium.coincurrently.ui.main.Compose.MinimumTextSize
import com.cryptium.coincurrently.ui.main.Compose.extremeFear
import com.cryptium.coincurrently.ui.main.Compose.extremeGreed
import com.cryptium.coincurrently.ui.main.Compose.fear
import com.cryptium.coincurrently.ui.main.Compose.greed
import com.cryptium.coincurrently.ui.main.Compose.neutral
import com.cryptium.coincurrently.ui.main.Compose.onSurfaceDimmer
import com.cryptium.coincurrently.ui.main.Compose.primaryBackgroundNoRipple
import com.cryptium.coincurrently.ui.main.Compose.shimmeringModifier
import com.cryptium.coincurrently.ui.main.Extensions.formatCurrency
import com.cryptium.coincurrently.ui.main.Extensions.roundTo
import com.cryptium.coincurrently.ui.main.Extensions.satoshiBold
import com.cryptium.coincurrently.ui.main.General.Enums.FormattingThreshold
import networking.ResponseModels.CurrencySelectionModel

@Composable
fun GasFeesHistoricView(
    timeframe: String,
    indexValue: String?,
    status: String?,
    modifier: Modifier = Modifier,
) {
    val isHorizontal = AccessibilityLayout.isHorizontal

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(0.dp),
        modifier = modifier
            .primaryBackgroundNoRipple(
                fillWidth = isHorizontal,
                paddingValues = PaddingValues(
                    horizontal = 8.dp,
                    vertical = 12.dp
                )
            )
    ) {
        val roundedChange = indexValue?.toDoubleOrNull()?.roundTo(3) ?: 0.0

        MinimumTextSize(
            text = "Current Average",
            font = satoshiBold(12),
            alignment = Alignment.TopCenter
        ) {
            Text(
                text = timeframe,
                style = satoshiBold(12),
                color = MaterialTheme.colors.onSurfaceDimmer,
                modifier = Modifier
                    .padding(bottom = 4.dp)
            )
        }

        Text(
            text = roundedChange.formatCurrency(
                currency = CurrencySelectionModel.empty(),
                minDecimals = 3,
                maxDecimals = 3
            ),
            style = satoshiBold(18),
            color = MaterialTheme.colors.onSurface,
            modifier = Modifier
                .shimmeringModifier(indexValue == null)
        )

        MinimumTextSize(
            text = "Very High",
            font = satoshiBold(12),
            alignment = Alignment.TopCenter
        ) {
            Text(
                text = status ?: "000000",
                style = satoshiBold(12),
                color = getGasIndexColor(indexValue ?: "0"),
                modifier = Modifier
                    .shimmeringModifier(indexValue == null),
            )
        }
    }
}

@Composable
fun getGasIndexColor(indexValue: String): Color {
    val doubleValue = indexValue.toDoubleOrNull() ?: 0.0
    return when {
        doubleValue < 3.0 -> MaterialTheme.colors.extremeGreed
        doubleValue < 10.0 -> MaterialTheme.colors.greed
        doubleValue < 30.0 -> MaterialTheme.colors.neutral
        doubleValue < 80.0 -> MaterialTheme.colors.fear
        else -> MaterialTheme.colors.extremeFear
    }
}

fun getGasIndexString(indexValue: String): String {
    val doubleValue = indexValue.toDoubleOrNull() ?: 0.0
    return when {
        doubleValue < 3.0 -> "Very Low"
        doubleValue < 10.0 -> "Low"
        doubleValue < 30.0 -> "Neutral"
        doubleValue < 80.0 -> "High"
        else -> "Very High"
    }
}