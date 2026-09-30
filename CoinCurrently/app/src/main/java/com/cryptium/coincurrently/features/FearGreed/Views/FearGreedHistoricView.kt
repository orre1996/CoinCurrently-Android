package com.cryptium.coincurrently.features.FearGreed.Views
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
import com.cryptium.coincurrently.sharedui.components.AccessibilityLayout
import com.cryptium.coincurrently.sharedui.modifiers.MinimumTextSize
import com.cryptium.coincurrently.sharedui.theme.onSurfaceDimmer
import com.cryptium.coincurrently.sharedui.theme.primaryBackgroundNoRipple
import com.cryptium.coincurrently.sharedui.modifiers.shimmeringModifier
import com.cryptium.coincurrently.core.extensions.satoshiBold
import com.cryptium.coincurrently.core.localization.Localization
import com.cryptium.coincurrently.features.Home.Views.getFearGreedColorCompose

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
    val longestStatusPlaceholder = wrappedStatus(longestStatus, statusSeparator)
    val statusText = if (status == null) "000000" else wrappedStatus(status, statusSeparator)

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
            text = Localization.string("Last month"),
            maxLines = 2,
            font = satoshiBold(12),
            alignment = Alignment.TopCenter,
            modifier = Modifier
                .padding(bottom = 4.dp),
        ) {
            Text(
                text = Localization.string(timeframe),
                maxLines = 2,
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

private fun wrappedStatus(status: String?, separator: String): String {
    if (status.isNullOrEmpty()) return ""
    return Localization.string(status).replace(" ", separator)
}
