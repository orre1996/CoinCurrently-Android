package com.cryptium.coincurrently.ui.main.PortfolioTab.Compose

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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.invisibleToUser
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cryptium.coincurrently.ui.main.Compose.AccessibilityLayout
import com.cryptium.coincurrently.ui.main.Compose.AdaptiveStack
import com.cryptium.coincurrently.ui.main.Compose.CachedUrlImage
import com.cryptium.coincurrently.ui.main.Compose.MinimumTextSize
import com.cryptium.coincurrently.ui.main.Compose.onSurfaceDimmer
import com.cryptium.coincurrently.ui.main.Compose.shimmeringModifier
import com.cryptium.coincurrently.ui.main.Extensions.formatCurrency
import com.cryptium.coincurrently.ui.main.Extensions.getAmount
import com.cryptium.coincurrently.ui.main.Extensions.getProfitPercent
import com.cryptium.coincurrently.ui.main.Extensions.getProfitValue
import com.cryptium.coincurrently.ui.main.Extensions.satoshiBold
import com.cryptium.coincurrently.ui.main.Extensions.stars
import com.cryptium.coincurrently.ui.main.General.Components.NewPriceChangePercentView
import com.cryptium.coincurrently.ui.main.General.Components.NewPriceChangeValueView
import com.cryptium.coincurrently.ui.main.General.Enums.FormattingThreshold
import com.cryptium.coincurrently.ui.main.General.GlobalFunctions
import com.cryptium.coincurrently.ui.main.General.Loading.ChangeSize
import com.cryptium.coincurrently.ui.main.PortfolioTab.DataClasses.NewTransaction
import networking.ResponseModels.CurrencySelectionModel
import networking.ResponseModels.Pie

@Composable
fun PortfolioCoinView(
    pie: Pie?,
    transactions: List<NewTransaction>?,
    prices: Map<String, Double>?,
    hideData: Boolean,
    index: Int? = null,
    totalCount: Int? = null,
    navigateToCoin: () -> Unit
) {
    val isHorizontal = AccessibilityLayout.isHorizontal
    val currency = GlobalFunctions.getSelectedCurrency()
    val currentAmount = transactions?.getAmount()
    val currentPrice = prices?.get(currency.code.lowercase())
        ?: pie?.data?.sparkline_in_7d?.price?.lastOrNull()
        ?: 0.0
    val isDownloading = pie?.data == null
    val coinName = pie?.data?.name ?: pie?.data?.symbol ?: ""
    val holdingsValue = (currentAmount ?: 0.0) * currentPrice
    val profitValue = transactions?.getProfitValue(prices = prices)
    val profitPercent = transactions?.getProfitPercent(prices = prices)

    val accessibilityLabelText = buildString {
        if (index != null && totalCount != null) {
            append("Asset ${index + 1} of $totalCount. ")
        }
        if (hideData) {
            append("$coinName, holdings hidden. ")
        } else {
            val amountLabel = accessibleCoinAmountLabel(
                amount = currentAmount ?: 0.0,
                coinName = coinName,
            )
            append("$coinName. holdings: $amountLabel. ${holdingsValue.formatCurrency(accessible = true)}. ")
        }
        append(profitLossAccessibilityLabel(profitValue, profitPercent, hideData))
    }

    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
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
            CachedUrlImage(
                model = pie?.data?.image,
                size = 28.dp,
                isDownloading = pie?.data == null,
                respectMissingContentShimmer = false,
                modifier = Modifier.semantics { invisibleToUser() },
            )

            CoinInfoView(
                pie = pie,
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

        ProfitLossView(
            transactions = transactions,
            prices = prices,
            hideData = hideData,
            isDownloading = prices.isNullOrEmpty(),
            modifier = Modifier
                .padding(start = 16.dp)
                .semantics { invisibleToUser() },
        )
    }
}

@Composable
private fun CoinInfoView(
    pie: Pie?,
    hideData: Boolean,
    currentAmount: Double?,
    currentPrice: Double,
    isDownloading: Boolean,
    isHorizontal: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier,
    ) {
        Text(
            text = pie?.data?.symbol?.uppercase() ?: "0000",
            style = satoshiBold(16),
            color = MaterialTheme.colors.onSurface,
            modifier = Modifier
                .shimmeringModifier(pie?.data?.symbol == null)
        )

        Text(
            text = pie?.data?.name ?: "0000000",
            style = satoshiBold(14),
            color = MaterialTheme.colors.onSurfaceDimmer,
            modifier = Modifier
                .shimmeringModifier(pie?.data?.name == null)
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
    currentPrice: Double,
    isDownloading: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = Alignment.End,
        modifier = modifier,
    ) {
        Text(
            text = if (hideData) stars else currentAmount?.formatCurrency(currency = CurrencySelectionModel.empty()) ?: "00000",
            style = satoshiBold(16),
            color = MaterialTheme.colors.onSurface,
            textAlign = TextAlign.End,
            modifier = Modifier
                .shimmeringModifier(isDownloading)
        )

        Text(
            text = if (hideData) stars else ((currentAmount ?: 0.0) * currentPrice).formatCurrency(threshold = FormattingThreshold.MILLION),
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
    currentPrice: Double,
    isDownloading: Boolean,
) {
    Text(
        text = if (hideData) stars else currentAmount?.formatCurrency(currency = CurrencySelectionModel.empty()) ?: "00000",
        style = satoshiBold(12),
        color = MaterialTheme.colors.onSurfaceDimmer,
        textAlign = TextAlign.Start,
        modifier = Modifier
            .shimmeringModifier(isDownloading)
    )

    Text(
        text = if (hideData) stars else ((currentAmount ?: 0.0) * currentPrice).formatCurrency(threshold = FormattingThreshold.MILLION),
        style = satoshiBold(12),
        color = MaterialTheme.colors.onSurfaceDimmer,
        textAlign = TextAlign.Start,
        modifier = Modifier
            .shimmeringModifier(isDownloading)
    )
}

@Composable
private fun ProfitLossView(
    transactions: List<NewTransaction>?,
    prices: Map<String, Double>?,
    hideData: Boolean,
    isDownloading: Boolean,
    modifier: Modifier = Modifier,
) {
    MinimumTextSize(
        text = "Profit/Loss",
        font = satoshiBold(12),
        extraSize = 16.dp,
        alignment = Alignment.CenterEnd,
        modifier = modifier,
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalAlignment = Alignment.End,
        ) {
            NewPriceChangeValueView(
                change = transactions?.getProfitValue(prices = prices),
                size = ChangeSize.MEDIUM,
                hideData = hideData,
                showDirection = false,
                isDownloading = isDownloading,
            )

            NewPriceChangePercentView(
                change = transactions?.getProfitPercent(prices = prices),
                size = ChangeSize.SMALL,
                isDownloading = isDownloading,
                modifier = Modifier
                    .alpha(0.75f)
            )
        }
    }
}
