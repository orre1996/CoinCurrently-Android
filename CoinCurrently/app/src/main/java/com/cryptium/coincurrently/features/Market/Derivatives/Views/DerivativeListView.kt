package com.cryptium.coincurrently.features.Market.Derivatives.Views
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
import androidx.compose.ui.unit.dp
import com.cryptium.coincurrently.sharedui.components.AccessibilityLayout
import com.cryptium.coincurrently.sharedui.components.AdaptiveStack
import com.cryptium.coincurrently.sharedui.components.CachedUrlImage
import com.cryptium.coincurrently.sharedui.modifiers.MinimumTextSize
import com.cryptium.coincurrently.sharedui.theme.onSurfaceDimmer
import com.cryptium.coincurrently.sharedui.modifiers.shimmeringModifier
import com.cryptium.coincurrently.core.extensions.formatCurrency
import com.cryptium.coincurrently.core.extensions.getSelectedCurrency
import com.cryptium.coincurrently.core.extensions.satoshiBold
import com.cryptium.coincurrently.core.extensions.satoshiMedium
import com.cryptium.coincurrently.core.networking.ResponseModels.CryptoDetailInfoModel
import com.cryptium.coincurrently.core.networking.ResponseModels.Derivative

@Composable
fun DerivativeListItem(
    derivative: Derivative?,
    bitcoin: CryptoDetailInfoModel?,
    listIndex: Int? = null,
    listCount: Int? = null,
    openDerivativeDetail: (Derivative) -> Unit
) {
    val isHorizontal = AccessibilityLayout.isHorizontal
    val bitcoinPrice = bitcoin?.market_data?.current_price?.getSelectedCurrency()
    val isLoadingOpenInterest = derivative == null || bitcoinPrice == null
    val totalInterest = (derivative?.open_interest_btc ?: 0.0) * (bitcoinPrice ?: 0.0)
    val formattedOpenInterest = totalInterest.formatCurrency()
    val totalVolume = (derivative?.trade_volume_24h_btc?.toDouble() ?: 0.0) * (bitcoinPrice ?: 0.0)
    val formattedVolume = totalVolume.formatCurrency()
    val isLoadingVolume = derivative == null || bitcoinPrice == null
    val accessibilityLabelText = buildString {
        if (listIndex != null && listCount != null) {
            append("Derivative ${listIndex + 1} of $listCount, ${derivative?.name ?: ""}. ")
        } else {
            append("${derivative?.name ?: ""}. ")
        }
        append("Open interest: ${totalInterest.formatCurrency(accessible = true)}. ")
        append("24 hour volume: ${totalVolume.formatCurrency(accessible = true)}")
    }

    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .semantics(mergeDescendants = true) {
                contentDescription = accessibilityLabelText
                onClick(label = "view ${derivative?.name ?: ""} details", action = {
                    derivative?.let { openDerivativeDetail(it) }
                    true
                })
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                derivative?.let {
                    openDerivativeDetail(it)
                }
            }
            .padding(
                vertical = 10.dp,
                horizontal = 16.dp
            )
    ) {
        AdaptiveStack(
            isHorizontal = isHorizontal,
            spacing = if (isHorizontal) 8.dp else 0.dp,
            horizontalAlignment = Alignment.Start,
            verticalAlignment = Alignment.Top,
            modifier = Modifier
                .weight(1f)
                .semantics { invisibleToUser() },
        ) {
            DerivativeImageView(derivative?.image)

            AdaptiveStack(
                isHorizontal = isHorizontal,
                spacing = 0.dp,
                horizontalAlignment = Alignment.Start,
                verticalAlignment = Alignment.Top,
            ) {
                DerivativeNameView(
                    name = derivative?.name
                )

                if (!isHorizontal) {
                    Text(
                        text = if (isLoadingOpenInterest) "0000000000" else formattedOpenInterest,
                        style = satoshiBold(12),
                        color = MaterialTheme.colors.onSurfaceDimmer,
                        modifier = Modifier
                            .shimmeringModifier(isLoadingOpenInterest)
                    )
                }
            }
        }

        AdaptiveStack(
            isHorizontal = isHorizontal,
            spacing = 16.dp,
            horizontalAlignment = Alignment.Start,
            verticalAlignment = Alignment.Top,
            modifier = Modifier.semantics { invisibleToUser() },
        ) {
            if (isHorizontal) {
                OpenInterestView(
                    formattedOpenInterest = formattedOpenInterest,
                    isLoading = isLoadingOpenInterest,
                )
            }

            VolumeView(
                formattedVolume = formattedVolume,
                isLoading = isLoadingVolume,
            )
        }
    }
}

@Composable
private fun DerivativeImageView(
    imageString: String?
) {
    CachedUrlImage(
        model = imageString,
        size = 28.dp,
    )
}

@Composable
private fun DerivativeNameView(
    name: String?
) {
    Text(
        text = name ?: "000000000",
        style = satoshiBold(16),
        color = MaterialTheme.colors.onSurface,
        modifier = Modifier
            .shimmeringModifier(name == null)
    )
}

@Composable
private fun OpenInterestView(
    formattedOpenInterest: String,
    isLoading: Boolean,
) {
    MinimumTextSize(
        text = "24h Volume",
        font = satoshiMedium(14),
        alignment = Alignment.CenterEnd
    ) {
        Text(
            text = if (isLoading) "0000000000" else formattedOpenInterest,
            style = satoshiBold(16),
            color = MaterialTheme.colors.onSurface,
            modifier = Modifier
                .shimmeringModifier(isLoading)
        )
    }
}

@Composable
private fun VolumeView(
    formattedVolume: String,
    isLoading: Boolean,
) {
    MinimumTextSize(
        text = "24h Volume",
        font = satoshiMedium(14),
        alignment = Alignment.CenterEnd
    ) {
        Text(
            text = if (isLoading) "000000000" else formattedVolume,
            style = satoshiBold(16),
            color = MaterialTheme.colors.onSurface,
            modifier = Modifier
                .shimmeringModifier(isLoading)
        )
    }
}
