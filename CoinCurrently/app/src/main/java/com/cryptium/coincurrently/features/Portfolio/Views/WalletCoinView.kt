package com.cryptium.coincurrently.features.Portfolio.Views
import com.cryptium.coincurrently.core.networking.ResponseModels.TokenInfo
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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
import com.cryptium.coincurrently.sharedui.modifiers.MinimumTextSize
import com.cryptium.coincurrently.sharedui.theme.onSurfaceDimmer
import com.cryptium.coincurrently.sharedui.modifiers.scaledImageSize
import com.cryptium.coincurrently.sharedui.modifiers.shimmeringModifier
import com.cryptium.coincurrently.core.extensions.formatCurrency
import com.cryptium.coincurrently.core.extensions.satoshiBold
import com.cryptium.coincurrently.core.extensions.stars
import com.cryptium.coincurrently.sharedui.components.NewPriceChangePercentView
import com.cryptium.coincurrently.sharedui.models.FormattingThreshold
import com.cryptium.coincurrently.sharedui.loading.ChangeSize
import com.cryptium.coincurrently.core.networking.ResponseModels.CurrencySelectionModel
import com.cryptium.coincurrently.core.networking.ResponseModels.Toplist

@Composable
fun WalletCoinView(
    toplist: Toplist?,
    token: TokenInfo?,
    hideData: Boolean,
    isDownloading: Boolean,
    index: Int? = null,
    totalCount: Int? = null,
    modifier: Modifier = Modifier,
    navigateToCoin: () -> Unit
) {
    val isHorizontal = AccessibilityLayout.isHorizontal
    val currentAmount = token?.balance?.toDoubleOrNull()
    val currentPrice = toplist?.current_price
    val coinName = toplist?.name ?: token?.tokenSymbol ?: ""
    val holdingsValue = (currentAmount ?: 0.0) * (currentPrice ?: 0.0)

    val accessibilityLabelText = buildString {
        if (index != null && totalCount != null) {
            append("Asset ${index + 1} of $totalCount. ")
        }
        if (hideData) {
            append("$coinName, holdings hidden. ")
        } else {
            val amountLabel = accessibleCoinAmountLabel(
                amount = currentAmount ?: 0.0,
                coinName = toplist?.name ?: toplist?.symbol ?: token?.tokenSymbol ?: "",
            )
            append("$coinName, holdings $amountLabel, ${holdingsValue.formatCurrency(accessible = true)}. ")
        }
        append(walletPriceChangeAccessibilityLabel(toplist?.price_change_percentage_7d_in_currency))
    }

    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .semantics(mergeDescendants = true) {
                contentDescription = accessibilityLabelText
                onClick(label = "view $coinName details", action = {
                    navigateToCoin.invoke()
                    true
                })
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                navigateToCoin.invoke()
            }
    ) {
        AdaptiveStack(
            isHorizontal = isHorizontal,
            spacing = 8.dp,
            horizontalAlignment = Alignment.Start,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f),
        ) {
            if (isDownloading || toplist?.image != null) {
                CachedUrlImage(
                    model = toplist?.image,
                    size = 28.dp,
                    isDownloading = isDownloading,
                    modifier = Modifier.semantics { invisibleToUser() },
                )
            }

            CoinInfoView(
                toplist = toplist,
                token = token,
                hideData = hideData,
                currentAmount = currentAmount,
                currentPrice = currentPrice,
                isDownloading = isDownloading,
                isHorizontal = isHorizontal,
                modifier = if (isHorizontal) {
                    Modifier.semantics { invisibleToUser() }
                } else {
                    sectionModifier().semantics { invisibleToUser() }
                },
            )
        }

        if (isHorizontal) {
            HoldingsView(
                hideData = hideData,
                currentAmount = currentAmount,
                currentPrice = currentPrice,
                isDownloading = isDownloading,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(start = 8.dp)
                    .semantics { invisibleToUser() },
            )
        }

        PriceChangeView(
            change = toplist?.price_change_percentage_7d_in_currency,
            isDownloading = isDownloading,
            modifier = Modifier
                .padding(start = 16.dp)
                .semantics { invisibleToUser() },
        )
    }
}

@Composable
private fun CoinInfoView(
    toplist: Toplist?,
    token: TokenInfo?,
    hideData: Boolean,
    currentAmount: Double?,
    currentPrice: Double?,
    isDownloading: Boolean,
    isHorizontal: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier,
    ) {
        Text(
            text = toplist?.symbol?.uppercase() ?: token?.tokenSymbol?.uppercase() ?: "",
            style = satoshiBold(16),
            color = MaterialTheme.colors.onSurface,
            modifier = Modifier
                .shimmeringModifier(isDownloading)
        )

        Text(
            text = toplist?.name ?: token?.tokenSymbol?.uppercase() ?: "",
            style = satoshiBold(14),
            color = MaterialTheme.colors.onSurfaceDimmer,
            modifier = Modifier
                .shimmeringModifier(isDownloading)
        )

        if (!isHorizontal) {
            HoldingsSubtitleView(
                hideData = hideData,
                currentAmount = currentAmount,
                currentPrice = currentPrice,
                isDownloading = isDownloading,
            )
        }
    }
}

@Composable
private fun HoldingsView(
    hideData: Boolean,
    currentAmount: Double?,
    currentPrice: Double?,
    isDownloading: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = Alignment.End,
        modifier = modifier,
    ) {
        Text(
            text = formattedAmount(
                hideData = hideData,
                currentAmount = currentAmount,
                isDownloading = isDownloading,
            ),
            style = satoshiBold(16),
            color = MaterialTheme.colors.onSurface,
            textAlign = TextAlign.End,
            modifier = Modifier
                .shimmeringModifier(isDownloading)
        )

        Text(
            text = formattedValue(
                hideData = hideData,
                currentAmount = currentAmount,
                currentPrice = currentPrice,
                isDownloading = isDownloading,
            ),
            style = satoshiBold(14),
            color = MaterialTheme.colors.onSurfaceDimmer,
            textAlign = TextAlign.End,
            modifier = Modifier
                .shimmeringModifier(isDownloading)
        )
    }
}

@Composable
private fun HoldingsSubtitleView(
    hideData: Boolean,
    currentAmount: Double?,
    currentPrice: Double?,
    isDownloading: Boolean,
) {
    Text(
        text = formattedAmount(
            hideData = hideData,
            currentAmount = currentAmount,
            isDownloading = isDownloading,
        ),
        style = satoshiBold(12),
        color = MaterialTheme.colors.onSurfaceDimmer,
        textAlign = TextAlign.Start,
        modifier = Modifier
            .shimmeringModifier(isDownloading)
    )

    Text(
        text = formattedValue(
            hideData = hideData,
            currentAmount = currentAmount,
            currentPrice = currentPrice,
            isDownloading = isDownloading,
        ),
        style = satoshiBold(12),
        color = MaterialTheme.colors.onSurfaceDimmer,
        textAlign = TextAlign.Start,
        modifier = Modifier
            .shimmeringModifier(isDownloading)
    )
}

@Composable
private fun PriceChangeView(
    change: Double?,
    isDownloading: Boolean,
    modifier: Modifier = Modifier,
) {
    MinimumTextSize(
        text = "999.9%",
        font = satoshiBold(16),
        extraSize = scaledImageSize(ChangeSize.MEDIUM.triangleSize) + 4.dp,
        alignment = Alignment.CenterEnd,
        modifier = modifier,
    ) {
        NewPriceChangePercentView(
            change = change,
            isDownloading = isDownloading,
        )
    }
}

private fun formattedAmount(
    hideData: Boolean,
    currentAmount: Double?,
    isDownloading: Boolean,
): String {
    if (isDownloading) {
        return "00000"
    }

    if (hideData) {
        return stars
    }

    return currentAmount?.formatCurrency(currency = CurrencySelectionModel.empty()) ?: "Missing Data"
}

private fun formattedValue(
    hideData: Boolean,
    currentAmount: Double?,
    currentPrice: Double?,
    isDownloading: Boolean,
): String {
    if (hideData) {
        return stars
    }

    if (currentPrice == null) {
        return "Missing Data"
    }

    return ((currentAmount ?: 0.0) * currentPrice).formatCurrency(threshold = FormattingThreshold.MILLION)
}
