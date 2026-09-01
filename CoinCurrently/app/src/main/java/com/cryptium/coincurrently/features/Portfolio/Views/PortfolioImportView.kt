package com.cryptium.coincurrently.features.Portfolio.Views
import com.cryptium.coincurrently.core.networking.ResponseModels.AddressImport
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.invisibleToUser
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cryptium.coincurrently.sharedui.components.CachedUrlImage
import com.cryptium.coincurrently.sharedui.theme.onSurfaceDim
import com.cryptium.coincurrently.sharedui.modifiers.shimmeringModifier
import com.cryptium.coincurrently.core.extensions.formatCurrency
import com.cryptium.coincurrently.core.extensions.satoshiBold
import com.cryptium.coincurrently.core.extensions.stars
import com.cryptium.coincurrently.sharedui.models.FormattingThreshold
import com.cryptium.coincurrently.sharedui.components.GlobalFunctions
import com.cryptium.coincurrently.core.networking.ResponseModels.Toplist

@Composable
fun PortfolioImportView(
    coin: Toplist?,
    walletImport: AddressImport?,
    prices: MutableMap<String, Map<String, Double>>?,
    hideData: Boolean,
    index: Int? = null,
    totalCount: Int? = null,
    showTransactionDetails: () -> Unit
) {
    val currency = GlobalFunctions.getSelectedCurrency()
    val formattedValue = if (hideData) {
        stars
    } else {
        walletImport?.totalValue(
            mainId = coin?.id,
            prices = prices
        )?.formatCurrency(
            threshold = FormattingThreshold.MILLION
        ) ?: ""
    }
    val isDownloading = coin == null
    val walletName = walletImport?.walletName ?: "Imported"
    val coinName = coin?.name ?: coin?.symbol ?: ""
    val value = walletImport?.totalValue(mainId = coin?.id, prices = prices)

    val accessibilityLabelText = buildString {
        if (index != null && totalCount != null) {
            append("Wallet ${index + 1} of $totalCount. ")
        }
        append("$walletName. ")
        if (coinName.isNotEmpty() && walletImport != null) {
            append("$coinName Mainnet, ${accessibilityAddressDescription(walletImport)}. ")
        }
        if (hideData) {
            append("Value hidden")
        } else if (value != null) {
            append(
                "Value ${
                    value.formatCurrency(
                        threshold = FormattingThreshold.MILLION,
                        currency = currency,
                        accessible = true,
                    )
                }"
            )
        }
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
            .semantics(mergeDescendants = true) {
                contentDescription = accessibilityLabelText
                onClick(label = "view wallet details", action = {
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
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CachedUrlImage(
                model = coin?.image,
                size = 24.dp,
                modifier = Modifier.semantics { invisibleToUser() },
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalAlignment = Alignment.Start,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .semantics { invisibleToUser() },
            ) {
                Text(
                    text = walletName,
                    style = satoshiBold(16),
                    color = MaterialTheme.colors.onSurface,
                    modifier = Modifier
                        .shimmeringModifier(isDownloading)
                )

                Text(
                    text = "${coin?.symbol?.uppercase() ?: ""} Mainnet · ${walletImport?.address?.uppercase()?.take(4)}...${walletImport?.address?.uppercase()?.takeLast(4)}",
                    style = satoshiBold(12),
                    color = MaterialTheme.colors.onSurfaceDim,
                    modifier = Modifier
                        .shimmeringModifier(isDownloading)
                )
            }

            Text(
                text = formattedValue,
                style = satoshiBold(16),
                color = MaterialTheme.colors.onSurface,
                textAlign = TextAlign.End,
                modifier = Modifier
                    .shimmeringModifier(isDownloading)
                    .weight(1f)
                    .fillMaxWidth()
                    .semantics { invisibleToUser() },
            )
        }
    }
}
