package com.cryptium.coincurrently.features.Portfolio.Views
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.invisibleToUser
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cryptium.coincurrently.sharedui.components.AccessibilityLayout
import com.cryptium.coincurrently.sharedui.components.AdaptiveStack
import com.cryptium.coincurrently.sharedui.components.CachedUrlImage
import com.cryptium.coincurrently.sharedui.theme.onSurfaceDimmer
import com.cryptium.coincurrently.sharedui.modifiers.shimmeringModifier
import com.cryptium.coincurrently.features.Portfolio.Models.PortfolioSwapTransaction
import com.cryptium.coincurrently.core.extensions.DateFormat
import com.cryptium.coincurrently.core.extensions.addedAccessibilityDescription
import com.cryptium.coincurrently.core.extensions.formatCurrency
import com.cryptium.coincurrently.core.extensions.formatDate
import com.cryptium.coincurrently.core.extensions.is24HourClock
import com.cryptium.coincurrently.core.extensions.satoshiBold
import com.cryptium.coincurrently.core.extensions.stars
import com.cryptium.coincurrently.sharedui.models.FormattingThreshold
import com.cryptium.coincurrently.core.networking.ResponseModels.CurrencySelectionModel
import com.cryptium.coincurrently.core.networking.ResponseModels.Pie
import com.cryptium.coincurrently.core.networking.ResponseModels.Toplist

@Composable
fun PortfolioSwapView(
    coin: Toplist?,
    transaction: PortfolioSwapTransaction?,
    portfolio: List<Pie>? = null,
    hideData: Boolean,
    index: Int? = null,
    totalCount: Int? = null,
    modifier: Modifier = Modifier,
    showTransactionDetails: () -> Unit
) {
    val context = LocalContext.current
    val isHorizontal = AccessibilityLayout.isHorizontal

    fun coinName(id: String, symbol: String): String {
        if (coin?.id == id) {
            coin.name?.takeIf { it.isNotEmpty() }?.let { return it }
        }
        portfolio?.firstOrNull { it.data?.id == id }?.data?.name?.takeIf { it.isNotEmpty() }?.let {
            return it
        }
        return symbol
    }

    val fromName = coinName(
        id = transaction?.fromCurrencyId ?: "",
        symbol = transaction?.fromCurrencySymbol ?: "",
    )
    val toName = coinName(
        id = transaction?.toCurrencyId ?: "",
        symbol = transaction?.toCurrencySymbol ?: "",
    )

    val accessibilityLabelText = buildString {
        if (index != null && totalCount != null) {
            append("Transaction ${index + 1} of $totalCount. ")
        }
        append("Swapped $fromName for $toName. ")
        if (hideData) {
            append("Amount hidden. ")
        } else {
            val receivedLabel = accessibleCoinAmountWithSymbol(
                amount = transaction?.toAmount ?: 0.0,
                symbol = transaction?.toCurrencySymbol,
            )
            append("Received $receivedLabel. ")
            val sentLabel = accessibleCoinAmountWithSymbol(
                amount = transaction?.fromAmount ?: 0.0,
                symbol = transaction?.fromCurrencySymbol,
            )
            append("Sent $sentLabel. ")
        }
        transaction?.date?.let {
            append(it.addedAccessibilityDescription)
        }
    }

    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
            .semantics(mergeDescendants = true) {
                contentDescription = accessibilityLabelText
                onClick(label = "view transaction details", action = {
                    showTransactionDetails.invoke()
                    true
                })
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
            ) {
                showTransactionDetails.invoke()
            }
    ) {
        CachedUrlImage(
            model = coin?.image,
            size = 28.dp,
            modifier = Modifier.semantics { invisibleToUser() },
        )

        AdaptiveStack(
            isHorizontal = isHorizontal,
            spacing = 4.dp,
            horizontalAlignment = Alignment.Start,
            verticalAlignment = Alignment.Top,
            modifier = Modifier.weight(1f),
        ) {
            SwapInfoView(
                transaction = transaction,
                isDownloading = transaction == null,
                modifier = if (isHorizontal) {
                    Modifier.fillMaxWidth().semantics { invisibleToUser() }
                } else {
                    sectionModifier().semantics { invisibleToUser() }
                },
            )

            if (!isHorizontal) {
                SwapValuesSubtitleView(
                    transaction = transaction,
                    hideData = hideData,
                    isDownloading = transaction == null,
                    modifier = Modifier.semantics { invisibleToUser() },
                )
            }
        }

        if (isHorizontal) {
            SwapValuesView(
                transaction = transaction,
                hideData = hideData,
                isDownloading = transaction == null,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .semantics { invisibleToUser() },
            )
        }
    }
}

@Composable
private fun SwapInfoView(
    transaction: PortfolioSwapTransaction?,
    isDownloading: Boolean,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier,
    ) {
        Text(
            text = "Swapped ${transaction?.fromCurrencySymbol?.uppercase() ?: "0000"} for ${transaction?.toCurrencySymbol?.uppercase() ?: "0000"}",
            style = satoshiBold(16),
            color = MaterialTheme.colors.onSurface,
            modifier = Modifier
                .shimmeringModifier(isDownloading)
        )

        Text(
            text = transaction?.date?.formatDate(if (context.is24HourClock) DateFormat.HHmm else DateFormat.hhmma) ?: "000000",
            style = satoshiBold(12),
            color = MaterialTheme.colors.onSurfaceDimmer,
            modifier = Modifier
                .shimmeringModifier(isDownloading)
        )
    }
}

@Composable
private fun SwapValuesView(
    transaction: PortfolioSwapTransaction?,
    hideData: Boolean,
    isDownloading: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = Alignment.End,
        modifier = modifier,
    ) {
        Text(
            text = if (hideData) stars else "+${transaction?.toAmount?.formatCurrency(threshold = FormattingThreshold.MILLION, currency = CurrencySelectionModel.empty()) ?: "000000"}${transaction?.toCurrencySymbol?.uppercase() ?: "0000"}",
            style = satoshiBold(16),
            textAlign = TextAlign.End,
            color = MaterialTheme.colors.onSurface,
            modifier = Modifier
                .shimmeringModifier(isDownloading)
        )

        Text(
            text = if (hideData) stars else "-${transaction?.fromAmount?.formatCurrency(threshold = FormattingThreshold.MILLION, currency = CurrencySelectionModel.empty()) ?: "000000"}${transaction?.fromCurrencySymbol?.uppercase() ?: "0000"}",
            style = satoshiBold(12),
            textAlign = TextAlign.End,
            color = MaterialTheme.colors.onSurfaceDimmer,
            modifier = Modifier
                .shimmeringModifier(isDownloading)
        )
    }
}

@Composable
private fun SwapValuesSubtitleView(
    transaction: PortfolioSwapTransaction?,
    hideData: Boolean,
    isDownloading: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = Alignment.Start,
        modifier = modifier,
    ) {
        Text(
            text = if (hideData) stars else "+${transaction?.toAmount?.formatCurrency(threshold = FormattingThreshold.MILLION, currency = CurrencySelectionModel.empty()) ?: "000000"}${transaction?.toCurrencySymbol?.uppercase() ?: "0000"}",
            style = satoshiBold(12),
            textAlign = TextAlign.Start,
            color = MaterialTheme.colors.onSurfaceDimmer,
            modifier = Modifier
                .shimmeringModifier(isDownloading)
        )

        Text(
            text = if (hideData) stars else "-${transaction?.fromAmount?.formatCurrency(threshold = FormattingThreshold.MILLION, currency = CurrencySelectionModel.empty()) ?: "000000"}${transaction?.fromCurrencySymbol?.uppercase() ?: "0000"}",
            style = satoshiBold(12),
            textAlign = TextAlign.Start,
            color = MaterialTheme.colors.onSurfaceDimmer,
            modifier = Modifier
                .shimmeringModifier(isDownloading)
        )
    }
}
