package com.cryptium.coincurrently.features.Portfolio.Views
import com.cryptium.coincurrently.core.persistence.UserPreferences
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
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
import com.cryptium.coincurrently.core.extensions.DateFormat
import com.cryptium.coincurrently.core.localization.Localization
import com.cryptium.coincurrently.core.extensions.addedAccessibilityDescription
import com.cryptium.coincurrently.core.extensions.formatCurrency
import com.cryptium.coincurrently.core.extensions.formatDate
import com.cryptium.coincurrently.core.extensions.is24HourClock
import com.cryptium.coincurrently.core.extensions.numberAccessible
import com.cryptium.coincurrently.core.extensions.satoshiBold
import com.cryptium.coincurrently.core.extensions.stars
import com.cryptium.coincurrently.core.extensions.symbolAccessible
import kotlin.math.abs
import com.cryptium.coincurrently.sharedui.models.FormattingThreshold
import com.cryptium.coincurrently.features.Portfolio.DataClasses.PortfolioTransaction
import com.cryptium.coincurrently.features.Portfolio.DataClasses.isAddOrInTransferTransaction
import com.cryptium.coincurrently.core.persistence.allCurrencies
import com.cryptium.coincurrently.core.networking.ResponseModels.CurrencySelectionModel
import com.cryptium.coincurrently.core.networking.ResponseModels.Toplist

@Composable
fun PortfolioTransactionView(
    coin: Toplist?,
    transaction: PortfolioTransaction?,
    prices: Map<String, Double>?,
    hideData: Boolean,
    index: Int? = null,
    totalCount: Int? = null,
    modifier: Modifier = Modifier,
    showTransactionDetails: () -> Unit
) {
    val context = LocalContext.current
    val isHorizontal = AccessibilityLayout.isHorizontal
    val currency = allCurrencies.firstOrNull { it.code.lowercase() == transaction?.currency?.lowercase() }
        ?: UserPreferences.getSelectedCurrency()

    val currentPrice: Double? = if (transaction?.currency == currency.code) {
        coin?.sparkline_in_7d?.price?.lastOrNull()
    } else {
        prices?.get(transaction?.currency?.lowercase() ?: "usd")
    }

    val accessibilityLabelText = buildString {
        if (index != null && totalCount != null) {
            append("${Localization.format("Transaction %1\$d of %2\$d", index + 1, totalCount)}. ")
        }
        val typeText = transaction?.transactionType?.transactionText ?: ""
        val coinName = coin?.name ?: coin?.symbol ?: ""
        val summary = "$typeText $coinName".trim()
        if (summary.isNotEmpty()) {
            append("$summary. ")
        }
        if (hideData) {
            append("${Localization.string("Amount hidden")}. ${Localization.string("Value hidden")}. ")
        } else {
            val prefix = Localization.string(
                if (transaction?.isAddOrInTransferTransaction == true) "Plus" else "Minus",
            )
            val amount = abs(transaction?.amount ?: 0.0)
            val decimals = if (amount < 1) 7 else 2
            append(
                "${
                    Localization.format(
                        "%1$@ %2$@ %3$@",
                        prefix,
                        amount.numberAccessible(maxPlaces = decimals),
                        coin?.symbol.accessibleSymbol(),
                    )
                }. ",
            )
            val value = (transaction?.amount ?: 0.0) * (currentPrice ?: 0.0)
            append("${Localization.format("Value %@", value.formatCurrency(currency = currency, accessible = true))}. ")
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
                    transaction?.let { showTransactionDetails.invoke() }
                    true
                })
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
            ) {
                transaction?.let {
                    showTransactionDetails.invoke()
                }
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
            TransactionInfoView(
                coin = coin,
                transaction = transaction,
                isDownloading = transaction == null,
                modifier = if (isHorizontal) {
                    Modifier.fillMaxWidth().semantics { invisibleToUser() }
                } else {
                    sectionModifier().semantics { invisibleToUser() }
                },
            )

            if (!isHorizontal) {
                TransactionValuesSubtitleView(
                    coin = coin,
                    transaction = transaction,
                    currentPrice = currentPrice,
                    currency = currency,
                    hideData = hideData,
                    isDownloading = transaction == null,
                    modifier = Modifier.semantics { invisibleToUser() },
                )
            }
        }

        if (isHorizontal) {
            TransactionValuesView(
                coin = coin,
                transaction = transaction,
                currentPrice = currentPrice,
                currency = currency,
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
private fun TransactionInfoView(
    coin: Toplist?,
    transaction: PortfolioTransaction?,
    isDownloading: Boolean,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier,
    ) {
        Text(
            text = "${transaction?.transactionType?.transactionText ?: "00000000"} ${coin?.symbol?.uppercase() ?: "0000"}",
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
private fun TransactionValuesView(
    coin: Toplist?,
    transaction: PortfolioTransaction?,
    currentPrice: Double?,
    currency: CurrencySelectionModel,
    hideData: Boolean,
    isDownloading: Boolean,
    modifier: Modifier = Modifier,
) {
    val price = ((transaction?.amount ?: 0.0) * (currentPrice ?: 0.0)).formatCurrency(
        threshold = FormattingThreshold.MILLION,
        currency = currency
    )
    val sign = transaction?.transactionType?.getTransactionPrefixSign ?: ""
    val amount = transaction?.amount?.formatCurrency(
        threshold = FormattingThreshold.MILLION,
        currency = CurrencySelectionModel.empty()
    ) ?: "000000"

    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = Alignment.End,
        modifier = modifier,
    ) {
        Text(
            text = if (hideData) stars else price,
            style = satoshiBold(16),
            color = MaterialTheme.colors.onSurface,
            textAlign = TextAlign.End,
            modifier = Modifier
                .shimmeringModifier(isDownloading)
        )

        Text(
            text = if (hideData) stars else "$sign$amount${coin?.symbol?.uppercase() ?: ""}",
            style = satoshiBold(12),
            color = MaterialTheme.colors.onSurfaceDimmer,
            textAlign = TextAlign.End,
            modifier = Modifier
                .shimmeringModifier(isDownloading)
        )
    }
}

@Composable
private fun TransactionValuesSubtitleView(
    coin: Toplist?,
    transaction: PortfolioTransaction?,
    currentPrice: Double?,
    currency: CurrencySelectionModel,
    hideData: Boolean,
    isDownloading: Boolean,
    modifier: Modifier = Modifier,
) {
    val price = ((transaction?.amount ?: 0.0) * (currentPrice ?: 0.0)).formatCurrency(
        threshold = FormattingThreshold.MILLION,
        currency = currency
    )
    val sign = transaction?.transactionType?.getTransactionPrefixSign ?: ""
    val amount = transaction?.amount?.formatCurrency(
        threshold = FormattingThreshold.MILLION,
        currency = CurrencySelectionModel.empty()
    ) ?: "000000"

    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = Alignment.Start,
        modifier = modifier,
    ) {
        Text(
            text = if (hideData) stars else price,
            style = satoshiBold(12),
            color = MaterialTheme.colors.onSurfaceDimmer,
            textAlign = TextAlign.Start,
            modifier = Modifier
                .shimmeringModifier(isDownloading)
        )

        Text(
            text = if (hideData) stars else "$sign$amount${coin?.symbol?.uppercase() ?: ""}",
            style = satoshiBold(12),
            color = MaterialTheme.colors.onSurfaceDimmer,
            textAlign = TextAlign.Start,
            modifier = Modifier
                .shimmeringModifier(isDownloading)
        )
    }
}

private fun String?.accessibleSymbol(): String {
    return this?.trim()?.uppercase()?.symbolAccessible.orEmpty()
}
