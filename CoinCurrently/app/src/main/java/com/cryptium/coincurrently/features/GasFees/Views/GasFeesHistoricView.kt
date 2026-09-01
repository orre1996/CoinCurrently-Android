package com.cryptium.coincurrently.features.GasFees.Views
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
import com.cryptium.coincurrently.sharedui.components.AccessibilityLayout
import com.cryptium.coincurrently.sharedui.modifiers.MinimumTextSize
import com.cryptium.coincurrently.sharedui.theme.extremeFear
import com.cryptium.coincurrently.sharedui.theme.extremeGreed
import com.cryptium.coincurrently.sharedui.theme.fear
import com.cryptium.coincurrently.sharedui.theme.greed
import com.cryptium.coincurrently.sharedui.theme.neutral
import com.cryptium.coincurrently.sharedui.theme.onSurfaceDimmer
import com.cryptium.coincurrently.sharedui.theme.primaryBackgroundNoRipple
import com.cryptium.coincurrently.sharedui.modifiers.shimmeringModifier
import com.cryptium.coincurrently.core.extensions.formatCurrency
import com.cryptium.coincurrently.core.extensions.roundTo
import com.cryptium.coincurrently.core.extensions.satoshiBold
import com.cryptium.coincurrently.sharedui.models.FormattingThreshold
import com.cryptium.coincurrently.core.networking.ResponseModels.CurrencySelectionModel

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