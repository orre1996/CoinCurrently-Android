package com.cryptium.coincurrently.features.Market.Exchanges.Views
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.invisibleToUser
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cryptium.coincurrently.sharedui.components.AccessibilityLayout
import com.cryptium.coincurrently.sharedui.components.AdaptiveStack
import com.cryptium.coincurrently.sharedui.components.CachedUrlImage
import com.cryptium.coincurrently.sharedui.theme.onSurfaceDim
import com.cryptium.coincurrently.sharedui.theme.onSurfaceDimmer
import com.cryptium.coincurrently.sharedui.modifiers.shimmeringModifier
import com.cryptium.coincurrently.core.extensions.formatCurrency
import com.cryptium.coincurrently.core.extensions.getSelectedCurrency
import com.cryptium.coincurrently.core.extensions.satoshiBold
import com.cryptium.coincurrently.core.extensions.trustColor
import com.cryptium.coincurrently.sharedui.models.FormattingThreshold
import com.cryptium.coincurrently.core.networking.ResponseModels.CryptoDetailInfoModel
import com.cryptium.coincurrently.core.networking.ResponseModels.ExchangeList

@Composable
fun ExchangeListView(
    exchange: ExchangeList?,
    bitcoin: CryptoDetailInfoModel?,
    listIndex: Int? = null,
    listCount: Int? = null,
    exchangePressed: (ExchangeList) -> Unit
) {
    val isHorizontal = AccessibilityLayout.isHorizontal
    val bitcoinPrice = bitcoin?.market_data?.current_price?.getSelectedCurrency()
    val isLoadingVolume = exchange == null || bitcoinPrice == null
    val totalVolume = (exchange?.trade_volume_24h_btc ?: 0.0) * (bitcoinPrice ?: 0.0)
    val formattedVolume = totalVolume.formatCurrency(
        threshold = if (isHorizontal) FormattingThreshold.NONE else FormattingThreshold.MILLION
    )
    val accessibilityLabelText = buildString {
        if (listIndex != null && listCount != null) {
            append("Exchange ${listIndex + 1} of $listCount, ${exchange?.name ?: ""}. ")
        } else {
            append("${exchange?.name ?: ""}. ")
        }
        append("24 hour volume: ${totalVolume.formatCurrency(threshold = FormattingThreshold.NONE, accessible = true)}. ")
        exchange?.trust_score?.let {
            append("Trust score: $it of 10")
        }
    }

    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .semantics(mergeDescendants = true) {
                contentDescription = accessibilityLabelText
                onClick(label = "view ${exchange?.name ?: ""} details", action = {
                    exchange?.let { exchangePressed(it) }
                    true
                })
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                exchange?.let {
                    exchangePressed(it)
                }
            }
            .padding(
                vertical = 10.dp,
                horizontal = 16.dp
            )
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .weight(1f)
                .semantics { invisibleToUser() }
        ) {
            ExchangeImageView(
                imageString = exchange?.image
            )

            AdaptiveStack(
                isHorizontal = isHorizontal,
                spacing = 0.dp,
                horizontalAlignment = Alignment.Start,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ExchangeNameView(
                    name = exchange?.name,
                )

                if (!isHorizontal) {
                    Text(
                        text = if (isLoadingVolume) "00000000000" else formattedVolume,
                        style = satoshiBold(12),
                        color = MaterialTheme.colors.onSurfaceDimmer,
                        modifier = Modifier
                            .shimmeringModifier(isLoadingVolume)
                    )
                }
            }
        }

        if (isHorizontal) {
            ExchangeVolumeView(
                formattedVolume = formattedVolume,
                isLoading = isLoadingVolume,
                modifier = Modifier.semantics { invisibleToUser() }
            )
        }

        TrustScoreView(
            trustScore = exchange?.trust_score,
            isLoading = exchange == null,
            modifier = Modifier.semantics { invisibleToUser() }
        )
    }
}

@Composable
private fun ExchangeImageView(
    imageString: String?
) {
    CachedUrlImage(
        model = imageString,
        size = 28.dp,
    )
}

@Composable
private fun ExchangeNameView(
    name: String?
) {
    Text(
        text = name ?: "00000",
        style = satoshiBold(16),
        color = MaterialTheme.colors.onSurface,
        modifier = Modifier
            .shimmeringModifier(name == null)
    )
}

@Composable
private fun ExchangeVolumeView(
    formattedVolume: String,
    isLoading: Boolean,
    modifier: Modifier = Modifier,
) {
    Text(
        text = if (isLoading) "00000000000" else formattedVolume,
        style = satoshiBold(16),
        color = MaterialTheme.colors.onSurface,
        textAlign = TextAlign.End,
        modifier = modifier
            .shimmeringModifier(isLoading)
    )
}

@Composable
private fun TrustScoreView(
    trustScore: Int?,
    isLoading: Boolean,
    modifier: Modifier = Modifier,
) {
    Text(
        text = "${trustScore ?: 0}/10",
        style = satoshiBold(16),
        color = trustScore?.trustColor ?: MaterialTheme.colors.onSurfaceDim,
        modifier = modifier
            .shimmeringModifier(isLoading)
    )
}
