package com.cryptium.coincurrently.ui.main.MarketTab.NFT.Views

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.cryptium.coincurrently.ui.main.Compose.AccessibilityLayout
import com.cryptium.coincurrently.ui.main.Compose.AdaptiveStack
import com.cryptium.coincurrently.ui.main.Compose.CachedUrlImage
import com.cryptium.coincurrently.ui.main.Compose.MinimumTextSize
import com.cryptium.coincurrently.ui.main.Compose.onSurfaceDim
import com.cryptium.coincurrently.ui.main.Compose.shimmeringModifier
import com.cryptium.coincurrently.ui.main.Extensions.changePercentDecimalPoints
import com.cryptium.coincurrently.ui.main.Extensions.directionAccessible
import com.cryptium.coincurrently.ui.main.Extensions.formatCurrency
import com.cryptium.coincurrently.ui.main.Extensions.numberAccessible
import com.cryptium.coincurrently.ui.main.Extensions.satoshiBold
import com.cryptium.coincurrently.ui.main.General.Components.NewPriceChangePercentView
import com.cryptium.coincurrently.ui.main.General.Enums.FormattingThreshold
import networking.ResponseModels.CurrencySelectionModel
import networking.ResponseModels.NFTToplist

@Composable
fun NftToplistCell(
    nft: NFTToplist?,
    chainSymbol: String?,
    priceMultiplier: Double?,
    listIndex: Int? = null,
    listCount: Int? = null,
    nftPressed: (NFTToplist) -> Unit
) {
    val isHorizontal = AccessibilityLayout.isHorizontal
    val change = nft?.floor_price_in_usd_24h_percentage_change
    val safeChange = if (change?.isInfinite() == true) 0.0 else change
    val decimals = safeChange?.changePercentDecimalPoints ?: 2
    val accessibleChange = safeChange?.numberAccessible(maxPlaces = decimals) ?: ""
    val nativeFloorPrice = nft?.floor_price?.native_currency
    val convertedFloorPrice = (nft?.floor_price?.usd ?: 0.0) * (priceMultiplier ?: 0.0)
    val accessibilityLabelText = buildString {
        if (listIndex != null && listCount != null) {
            append("NFT ${listIndex + 1} of $listCount. ${nft?.name ?: ""}. ")
        } else {
            append("${nft?.name ?: ""}. ")
        }
        nativeFloorPrice?.let {
            val currency = nativeFloorPriceCurrency(nft, chainSymbol)
            append("Floor price: ${it.formatCurrency(currency = currency, accessible = true)}. ")
        }
        if (priceMultiplier != null) {
            append("Converted floor price: ${convertedFloorPrice.formatCurrency(threshold = FormattingThreshold.NONE, accessible = true)}. ")
        }
        append("24 hour price change: ${change?.directionAccessible ?: ""} $accessibleChange percent")
    }

    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .semantics(mergeDescendants = true) {
                contentDescription = accessibilityLabelText
                onClick(label = "view ${nft?.name ?: ""} details", action = {
                    nft?.let { nftPressed(it) }
                    true
                })
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                nft?.let {
                    nftPressed(it)
                }
            }
            .padding(
                vertical = 10.dp,
                horizontal = 16.dp
            )
    ) {
        AdaptiveStack(
            isHorizontal = isHorizontal,
            spacing = 8.dp,
            horizontalAlignment = Alignment.Start,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .weight(1f)
                .semantics { invisibleToUser() }
        ) {
            NFTImageView(
                imageString = nft?.image?.small
            )

            NFTNameView(
                name = nft?.name,
                modifier = sectionModifier()
            )

            NFTPriceView(
                floorPriceNative = nft?.floor_price?.native_currency,
                floorPriceUSD = nft?.floor_price?.usd,
                symbol = chainSymbol,
                priceMultiplier = priceMultiplier,
                isHorizontal = isHorizontal,
                modifier = sectionModifier(),
            )
        }

        PriceChangeView(
            change = nft?.floor_price_in_usd_24h_percentage_change,
            isLoading = nft == null,
            modifier = Modifier
                .padding(start = 8.dp)
                .semantics { invisibleToUser() }
        )
    }
}

private fun nativeFloorPriceCurrency(
    nft: NFTToplist?,
    chainSymbol: String?,
): CurrencySelectionModel {
    val symbol = nft?.native_currency_symbol?.trim()?.uppercase()
        ?: chainSymbol?.trim()?.uppercase()
        ?: ""

    if (symbol.isEmpty()) {
        return CurrencySelectionModel(
            currency = "",
            code = "",
            symbol = "",
            suffix = true,
        )
    }

    val spelledSymbol = symbol.toCharArray().joinToString(" ") { it.toString() }
    return CurrencySelectionModel(
        currency = spelledSymbol,
        code = symbol,
        symbol = symbol,
        suffix = true,
        readAccessibilityAsSymbol = true,
    )
}

@Composable
private fun NFTImageView(imageString: String?) {
    CachedUrlImage(
        model = imageString,
        size = 28.dp,
    )
}

@Composable
private fun NFTNameView(
    name: String?,
    modifier: Modifier
) {
    Text(
        text = name ?: "00000000",
        style = satoshiBold(16),
        color = MaterialTheme.colors.onSurface,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier
            .shimmeringModifier(name == null)
    )
}

@Composable
private fun NFTPriceView(
    floorPriceNative: Double?,
    floorPriceUSD: Double?,
    symbol: String?,
    priceMultiplier: Double?,
    isHorizontal: Boolean,
    modifier: Modifier = Modifier,
) {
    val currency = CurrencySelectionModel(
        currency = " $symbol",
        code = " $symbol",
        symbol = " $symbol",
        suffix = true
    )
    val isLoadingNative = floorPriceNative == null
    val isLoadingConverted = floorPriceNative == null || priceMultiplier == null

    Column(
        horizontalAlignment = if (isHorizontal) Alignment.End else Alignment.Start,
        modifier = modifier
    ) {
        Text(
            text = floorPriceNative?.formatCurrency(currency = currency) ?: "0000",
            style = satoshiBold(16),
            color = MaterialTheme.colors.onSurface,
            textAlign = if (isHorizontal) TextAlign.End else TextAlign.Start,
            modifier = Modifier
                .shimmeringModifier(isLoadingNative)
        )

        Text(
            text = ((floorPriceUSD ?: 0.0).times(priceMultiplier ?: 0.0)).formatCurrency(
                threshold = FormattingThreshold.NONE
            ),
            style = satoshiBold(12),
            color = MaterialTheme.colors.onSurfaceDim,
            textAlign = if (isHorizontal) TextAlign.End else TextAlign.Start,
            modifier = Modifier
                .shimmeringModifier(isLoadingConverted)
        )
    }
}

@Composable
private fun PriceChangeView(
    change: Double?,
    isLoading: Boolean,
    modifier: Modifier = Modifier,
) {
    MinimumTextSize(
        text = "999.9%",
        font = satoshiBold(16),
        extraSize = 20.dp,
        alignment = Alignment.CenterEnd,
        modifier = modifier,
    ) {
        NewPriceChangePercentView(
            change = change,
            accessibleTimeframe = "24 hour",
            isDownloading = isLoading,
            modifier = Modifier.semantics { invisibleToUser() }
        )
    }
}
