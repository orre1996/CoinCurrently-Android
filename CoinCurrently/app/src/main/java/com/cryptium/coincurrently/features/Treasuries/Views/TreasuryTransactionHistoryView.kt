package com.cryptium.coincurrently.features.Treasuries.Views
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.invisibleToUser
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.cryptium.coincurrently.sharedui.components.CachedUrlImage
import com.cryptium.coincurrently.sharedui.theme.onSurfaceDim
import com.cryptium.coincurrently.sharedui.modifiers.shimmeringModifier
import com.cryptium.coincurrently.core.extensions.accessibleLongDateDescription
import com.cryptium.coincurrently.core.extensions.changeColor
import com.cryptium.coincurrently.core.extensions.formatCurrency
import com.cryptium.coincurrently.core.extensions.formatDate
import com.cryptium.coincurrently.core.extensions.satoshiBold
import com.cryptium.coincurrently.core.extensions.satoshiMedium
import com.cryptium.coincurrently.sharedui.models.FormattingThreshold
import com.cryptium.coincurrently.sharedui.buttons.SecondaryButton
import com.cryptium.coincurrently.R
import com.cryptium.coincurrently.sharedui.modifiers.MinimumTextSize
import com.cryptium.coincurrently.core.extensions.DateFormat
import com.cryptium.coincurrently.core.networking.ResponseModels.CurrencySelectionModel
import com.cryptium.coincurrently.core.networking.ResponseModels.Toplist
import com.cryptium.coincurrently.core.networking.ResponseModels.TreasuryTransaction
import com.cryptium.coincurrently.core.networking.ResponseModels.TreasuryTransactionType
import com.cryptium.coincurrently.core.persistence.allCurrencies

@Composable
fun TreasuryTransactionHistoryView(
    holdings: List<Toplist>,
    selectedHoldingCoinId: String?,
    transactions: List<TreasuryTransaction>?,
    showHoldingFilter: () -> Unit,
    filterButtonTitle: String,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
    ) {
        SecondaryButton(
            text = filterButtonTitle,
            accessibilityLabel = "Holding, currently selected: $filterButtonTitle",
            icon = R.drawable.chevron_down,
            onClick = showHoldingFilter,
            modifier = Modifier.padding(bottom = 24.dp),
        )

        TreasuryTransactionHistoryHeaderView()

        if (transactions != null) {
            transactions.forEach { transaction ->
                TreasuryTransactionItemView(
                    transaction = transaction,
                    coin = coinFor(holdings, transaction.coin_id),
                )
            }
        } else {
            repeat(6) {
                TreasuryTransactionItemView(
                    transaction = null,
                    coin = null,
                )
            }
        }
    }
}

private fun coinFor(holdings: List<Toplist>, coinId: String?): Toplist? {
    if (coinId == null) return null
    return holdings.firstOrNull { it.id == coinId }
}

@Composable
fun TreasuryTransactionHistoryHeaderView() {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 6.dp)
            .clearAndSetSemantics { invisibleToUser() },
    ) {
        MinimumTextSize(
            text = "Dec 31, 2025",
            font = satoshiBold(16),
            alignment = Alignment.CenterStart
        ) {
            Text(
                text = "Date",
                style = satoshiMedium(14),
                color = MaterialTheme.colors.onSurfaceDim,
            )
        }

        Text(
            text = "Coin",
            style = satoshiMedium(14),
            color = MaterialTheme.colors.onSurfaceDim,
        )

        Text(
            text = "Balance",
            style = satoshiMedium(14),
            color = MaterialTheme.colors.onSurfaceDim,
            textAlign = TextAlign.End,
            modifier = Modifier
                .weight(1f),
        )
    }
}

@Composable
fun TreasuryTransactionItemView(
    transaction: TreasuryTransaction?,
    coin: Toplist?,
) {
    val holdingsCurrency = holdingsCurrency(coin, transaction)
    val accessibilityHoldingsCurrency = accessibleHoldingsCurrency(coin, transaction)
    val formattedDate = transaction?.date?.let { timestamp ->
        java.util.Date(timestamp.toLong()).formatDate(DateFormat.MMMdyyyy)
    } ?: "000000"
    val accessibilityDate = transaction?.date?.let { timestamp ->
        java.util.Date(timestamp.toLong()).accessibleLongDateDescription()
    } ?: formattedDate
    val coinName = coin?.name ?: transaction?.coin_id?.replaceFirstChar { it.uppercase() } ?: "00000"
    val balanceText = (transaction?.holding_balance ?: 843_738.0).formatCurrency(
        threshold = FormattingThreshold.NONE,
        currency = holdingsCurrency,
    )
    val netChange = transaction?.holding_net_change ?: 24_869.0
    val netChangeFormatted = kotlin.math.abs(netChange).formatCurrency(
        threshold = FormattingThreshold.NONE,
        currency = holdingsCurrency,
    )
    val netChangeSign = when {
        netChange > 0 -> "+"
        netChange < 0 -> "-"
        else -> ""
    }
    val netChangeText = "$netChangeSign$netChangeFormatted"
    val netChangeColor = netChange.changeColor()

    val accessibilityLabel = if (transaction == null) {
        "Loading transaction"
    } else {
        buildList {
            add(accessibilityDate)
            transaction.type?.let { type ->
                add(if (type == TreasuryTransactionType.buy) "Buy" else "Sell")
            }
            add(coinName)
            add(
                "Balance: ${(transaction.holding_balance ?: 0.0).formatCurrency(
                    threshold = FormattingThreshold.NONE,
                    currency = accessibilityHoldingsCurrency,
                    accessible = true,
                )}"
            )
            val change = transaction.holding_net_change ?: 0.0
            val changeAmount = kotlin.math.abs(change).formatCurrency(
                threshold = FormattingThreshold.NONE,
                currency = accessibilityHoldingsCurrency,
                accessible = true,
            )
            add(
                when {
                    change > 0 -> "Change up $changeAmount"
                    change < 0 -> "Change down $changeAmount"
                    else -> "Change $changeAmount"
                }
            )
        }.joinToString(". ")
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp)
            .semantics(mergeDescendants = true) {
                contentDescription = accessibilityLabel
            },
    ) {
        MinimumTextSize(
            text = "Dec 31, 2025",
            font = satoshiBold(16),
            alignment = Alignment.CenterStart
        ) {
            Text(
                text = formattedDate,
                style = satoshiBold(14),
                color = MaterialTheme.colors.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .shimmeringModifier(transaction == null)
                    .semantics { invisibleToUser() },
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier
                .padding(end = 12.dp)
                .semantics { invisibleToUser() },
        ) {
            CachedUrlImage(
                model = coin?.image,
                size = 20.dp,
                isDownloading = transaction != null && coin == null,
            )

            Text(
                text = coinName,
                style = satoshiBold(16),
                color = MaterialTheme.colors.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.shimmeringModifier(transaction == null),
            )
        }

        Column(
            horizontalAlignment = Alignment.End,
            modifier = Modifier
                .weight(1f)
                .semantics { invisibleToUser() },
        ) {
            Text(
                text = balanceText,
                style = satoshiBold(16),
                color = MaterialTheme.colors.onSurface,
                textAlign = TextAlign.End,
                modifier = Modifier.shimmeringModifier(transaction == null),
            )

            Text(
                text = netChangeText,
                style = satoshiBold(12),
                color = netChangeColor,
                textAlign = TextAlign.End,
                modifier = Modifier.shimmeringModifier(transaction == null),
            )
        }
    }
}

private fun holdingsCurrency(
    coin: Toplist?,
    transaction: TreasuryTransaction?,
): CurrencySelectionModel {
    val symbol = coin?.symbol?.uppercase()
        ?: transaction?.coin_id?.uppercase()
        ?: ""

    return CurrencySelectionModel(
        currency = "",
        code = symbol,
        symbol = symbol,
        suffix = true,
    )
}

private fun accessibleHoldingsCurrency(
    coin: Toplist?,
    transaction: TreasuryTransaction?,
): CurrencySelectionModel {
    val symbol = coin?.symbol?.uppercase()
        ?: transaction?.coin_id?.uppercase()

    if (!symbol.isNullOrEmpty()) {
        allCurrencies.firstOrNull { it.code == symbol }?.let { return it }
        return CurrencySelectionModel(
            currency = symbol.map { it.toString() }.joinToString(" "),
            code = symbol,
            symbol = symbol,
            suffix = true,
            readAccessibilityAsSymbol = true,
        )
    }

    return holdingsCurrency(coin, transaction)
}
