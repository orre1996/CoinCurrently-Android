package com.cryptium.coincurrently.features.Portfolio.Views
import com.cryptium.coincurrently.core.persistence.UserPreferences
import com.cryptium.coincurrently.sharedui.modifiers.imageSize

import com.cryptium.coincurrently.core.networking.ResponseModels.AddressImport
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.invisibleToUser
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cryptium.coincurrently.R
import com.cryptium.coincurrently.sharedui.components.AccessibilityLayout
import com.cryptium.coincurrently.sharedui.components.AdaptiveStack
import com.cryptium.coincurrently.sharedui.theme.onSurfaceDimmer
import com.cryptium.coincurrently.sharedui.theme.outlineVariant
import com.cryptium.coincurrently.sharedui.modifiers.shimmeringModifier
import com.cryptium.coincurrently.sharedui.theme.surfaceContainerLow
import com.cryptium.coincurrently.core.extensions.addedAccessibilityDescription
import com.cryptium.coincurrently.core.localization.Localization
import com.cryptium.coincurrently.core.extensions.formatCurrency
import com.cryptium.coincurrently.core.extensions.satoshiBold
import com.cryptium.coincurrently.core.extensions.stars
import com.cryptium.coincurrently.sharedui.models.FormattingThreshold
import com.cryptium.coincurrently.core.networking.ResponseModels.Toplist

@Composable
fun PortfolioImportTransaction(
    coin: Toplist?,
    walletImport: AddressImport?,
    prices: MutableMap<String, Map<String, Double>>?,
    hideData: Boolean,
    index: Int? = null,
    totalCount: Int? = null,
    modifier: Modifier = Modifier,
    showTransactionDetails: () -> Unit
) {
    val isHorizontal = AccessibilityLayout.isHorizontal
    val currency = UserPreferences.getSelectedCurrency()
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
    val walletName = walletImport?.walletName ?: ""
    val coinName = coin?.name ?: coin?.symbol ?: ""
    val value = walletImport?.totalValue(mainId = coin?.id, prices = prices)

    val accessibilityLabelText = buildString {
        if (index != null && totalCount != null) {
            append("${Localization.format("Transaction %1\$d of %2\$d", index + 1, totalCount)}. ")
        }
        append("${Localization.format("Imported %@", walletName)}. ")
        if (coinName.isNotEmpty() && walletImport != null) {
            append("${Localization.format("%1$@ Mainnet, %2$@", coinName, accessibilityAddressDescription(walletImport))}. ")
        }
        if (hideData) {
            append("${Localization.string("Value hidden")}. ")
        } else if (value != null) {
            append(
                "${
                    Localization.format(
                        "Value %@",
                        value.formatCurrency(
                            threshold = FormattingThreshold.MILLION,
                            currency = currency,
                            accessible = true,
                        ),
                    )
                }. "
            )
        }
        walletImport?.date?.let {
            append(it.addedAccessibilityDescription)
        }
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
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
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.walletimport),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                colorFilter = ColorFilter.tint(
                    MaterialTheme.colors.onSurfaceDimmer
                ),
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colors.surfaceContainerLow,
                        shape = CircleShape
                    )
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colors.outlineVariant,
                        shape = CircleShape
                    )
                    .padding(6.dp)
                    .imageSize(16.dp)
                    .semantics { invisibleToUser() }
            )

            AdaptiveStack(
                isHorizontal = isHorizontal,
                spacing = 4.dp,
                horizontalAlignment = Alignment.Start,
                verticalAlignment = Alignment.Top,
                modifier = Modifier.weight(1f),
            ) {
                ImportInfoView(
                    coin = coin,
                    walletImport = walletImport,
                    isDownloading = isDownloading,
                    modifier = if (isHorizontal) {
                        Modifier.fillMaxWidth().semantics { invisibleToUser() }
                    } else {
                        sectionModifier().semantics { invisibleToUser() }
                    },
                )

                if (!isHorizontal) {
                    Text(
                        text = formattedValue,
                        style = satoshiBold(12),
                        color = MaterialTheme.colors.onSurfaceDimmer,
                        textAlign = TextAlign.Start,
                        modifier = Modifier
                            .shimmeringModifier(isDownloading)
                            .semantics { invisibleToUser() }
                    )
                }
            }

            if (isHorizontal) {
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
}

@Composable
private fun ImportInfoView(
    coin: Toplist?,
    walletImport: AddressImport?,
    isDownloading: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = Alignment.Start,
        modifier = modifier,
    ) {
        Text(
            text = Localization.format("Imported %@", walletImport?.walletName ?: ""),
            style = satoshiBold(16),
            color = MaterialTheme.colors.onSurface,
            modifier = Modifier
                .shimmeringModifier(isDownloading)
        )

        Text(
            text = Localization.format(
                "%1$@ Mainnet · %2$@",
                coin?.symbol?.uppercase() ?: "",
                "${walletImport?.address?.uppercase()?.take(4)}...${walletImport?.address?.uppercase()?.takeLast(4)}",
            ),
            style = satoshiBold(12),
            color = MaterialTheme.colors.onSurfaceDimmer,
            modifier = Modifier
                .shimmeringModifier(isDownloading)
        )
    }
}
