package com.cryptium.coincurrently.ui.main.HomeTab.FearGreed.Components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cryptium.coincurrently.ui.main.Compose.AccessibilityLayout
import com.cryptium.coincurrently.ui.main.Compose.MinimumTextSize
import com.cryptium.coincurrently.ui.main.Compose.onSurfaceDimmer
import com.cryptium.coincurrently.ui.main.Compose.primaryBackgroundNoRipple
import com.cryptium.coincurrently.ui.main.Compose.shimmeringModifier
import com.cryptium.coincurrently.ui.main.Extensions.satoshiBold
import com.cryptium.coincurrently.ui.main.HomeTab.Compose.getFearGreedColorCompose

@Composable
fun FearGreedHistoricView(
    timeframe: String,
    indexValue: String?,
    longestStatus: String?,
    status: String?,
    modifier: Modifier = Modifier,
) {
    val isHorizontal = AccessibilityLayout.isHorizontal
    val statusSeparator = if (isHorizontal) " " else "\n"
    val longestStatusPlaceholder = longestStatus?.replace(" ", statusSeparator) ?: ""
    val statusText = status?.replace(" ", statusSeparator) ?: "000000"

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .primaryBackgroundNoRipple(
                fillWidth = isHorizontal,
                paddingValues = PaddingValues(
                    start = 8.dp,
                    top = 12.dp,
                    end = 8.dp,
                    bottom = 12.dp,
                ),
            ),
    ) {
        MinimumTextSize(
            text = "Last Month",
            font = satoshiBold(12),
            alignment = Alignment.TopCenter,
            modifier = Modifier
                .padding(bottom = 4.dp),
        ) {
            Text(
                text = timeframe,
                style = satoshiBold(12),
                color = MaterialTheme.colors.onSurfaceDimmer,
                textAlign = TextAlign.Center,
            )
        }

        Text(
            text = indexValue ?: "000",
            style = satoshiBold(18),
            color = MaterialTheme.colors.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .shimmeringModifier(indexValue == null),
        )

        MinimumTextSize(
            text = longestStatusPlaceholder,
            font = satoshiBold(12),
            alignment = Alignment.TopCenter,
            maxLines = if (isHorizontal) 2 else 1,
        ) {
            Text(
                text = statusText,
                style = satoshiBold(12),
                color = getFearGreedColorCompose(indexValue?.toIntOrNull() ?: 0),
                textAlign = TextAlign.Center,
                maxLines = if (isHorizontal) 2 else 1,
                modifier = Modifier
                    .shimmeringModifier(indexValue == null),
            )
        }
    }
}
