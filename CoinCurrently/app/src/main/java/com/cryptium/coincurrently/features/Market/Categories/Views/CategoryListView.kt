package com.cryptium.coincurrently.features.Market.Categories.Views
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
import androidx.compose.ui.unit.dp
import com.cryptium.coincurrently.sharedui.components.AccessibilityLayout
import com.cryptium.coincurrently.sharedui.components.CachedUrlImage
import com.cryptium.coincurrently.sharedui.modifiers.MinimumTextSize
import com.cryptium.coincurrently.sharedui.theme.onSurfaceDimmer
import com.cryptium.coincurrently.sharedui.modifiers.shimmeringModifier
import com.cryptium.coincurrently.core.extensions.changePercentDecimalPoints
import com.cryptium.coincurrently.core.extensions.directionAccessible
import com.cryptium.coincurrently.core.extensions.formatCurrency
import com.cryptium.coincurrently.core.extensions.numberAccessible
import com.cryptium.coincurrently.core.extensions.satoshiBold
import com.cryptium.coincurrently.sharedui.components.PriceChangePercentView
import com.cryptium.coincurrently.core.networking.ResponseModels.CryptoCategory

@Composable
fun CategoryListItem(
    category: CryptoCategory?,
    multiplier: Double?,
    listIndex: Int? = null,
    listCount: Int? = null,
    openCategoryDetail: (CryptoCategory) -> Unit
) {
    val isHorizontal = AccessibilityLayout.isHorizontal
    val marketCap = (category?.market_cap ?: 0.0) * (multiplier ?: 0.0)
    val change = category?.market_cap_change_24h
    val safeChange = if (change?.isInfinite() == true) 0.0 else change
    val decimals = safeChange?.changePercentDecimalPoints ?: 2
    val accessibleChange = safeChange?.numberAccessible(maxPlaces = decimals) ?: ""
    val accessibilityLabelText = buildString {
        if (listIndex != null && listCount != null) {
            append("Category ${listIndex + 1} of $listCount. ${category?.name ?: ""}. ")
        } else {
            append("${category?.name ?: ""}. ")
        }
        append("Market cap: ${marketCap.formatCurrency(accessible = true)}. ")
        append("24 hour price change: ${change?.directionAccessible ?: ""} $accessibleChange percent")
    }

    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .semantics(mergeDescendants = true) {
                contentDescription = accessibilityLabelText
                onClick(label = "view ${category?.name ?: ""} details", action = {
                    category?.let { openCategoryDetail(it) }
                    true
                })
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                category?.let {
                    openCategoryDetail(it)
                }
            }
            .padding(
                vertical = 10.dp,
                horizontal = 16.dp
            )
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.Start,
            modifier = Modifier
                .weight(1f)
                .semantics { invisibleToUser() }
        ) {
            NameView(
                currentCategory = category
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                category?.top_3_coins?.let {
                    RealImagesView(it)
                } ?: run {
                    TempImagesView()
                }
            }

            if (!isHorizontal) {
                MarketCapSubtitleView(
                    currentCategory = category,
                    multiplier = multiplier,
                )
            }
        }

        if (isHorizontal) {
            MarketCapView(
                currentCategory = category,
                multiplier = multiplier,
                modifier = Modifier.semantics { invisibleToUser() }
            )
        }

        PriceChangeView(
            currentCategory = category,
            multiplier = multiplier,
            modifier = Modifier.semantics { invisibleToUser() }
        )
    }
}

@Composable
private fun TempImagesView() {
    (0 until 3).forEach { _ ->
        CachedUrlImage(
            model = null,
            size = 16.dp,
        )
    }
}

@Composable
private fun RealImagesView(images: List<String>) {
    images.take(3).forEach {
        CachedUrlImage(
            model = it,
            size = 16.dp,
        )
    }
}

@Composable
private fun NameView(
    currentCategory: CryptoCategory?
) {
    Text(
        text = currentCategory?.name ?: "00000000",
        style = satoshiBold(16),
        color = MaterialTheme.colors.onSurface,
        textAlign = TextAlign.Start,
        modifier = Modifier
            .shimmeringModifier(currentCategory == null)
    )
}

@Composable
private fun MarketCapView(
    currentCategory: CryptoCategory?,
    multiplier: Double?,
    modifier: Modifier = Modifier,
) {
    val marketCap = (currentCategory?.market_cap ?: 0.0) * (multiplier ?: 0.0)
    val isLoading = currentCategory == null || multiplier == null
    val text = marketCap.formatCurrency()

    MinimumTextSize(
        text = "Market Cap",
        font = satoshiBold(16),
        alignment = Alignment.CenterEnd
    ) {
        Text(
            text = if (isLoading) "000000000000" else text,
            style = satoshiBold(16),
            color = MaterialTheme.colors.onSurface,
            textAlign = TextAlign.End,
            modifier = modifier
                .shimmeringModifier(isLoading)
        )
    }
}

@Composable
private fun MarketCapSubtitleView(
    currentCategory: CryptoCategory?,
    multiplier: Double?,
) {
    val marketCap = (currentCategory?.market_cap ?: 0.0) * (multiplier ?: 0.0)
    val isLoading = currentCategory == null || multiplier == null
    val text = marketCap.formatCurrency()

    Text(
        text = if (isLoading) "000000000000" else text,
        style = satoshiBold(12),
        color = MaterialTheme.colors.onSurfaceDimmer,
        textAlign = TextAlign.Start,
        modifier = Modifier
            .shimmeringModifier(isLoading)
    )
}

@Composable
private fun PriceChangeView(
    currentCategory: CryptoCategory?,
    multiplier: Double?,
    modifier: Modifier = Modifier,
) {
    val isLoading = currentCategory == null || multiplier == null

    MinimumTextSize(
        text = "999.9%",
        font = satoshiBold(16),
        extraSize = 20.dp,
        alignment = Alignment.CenterEnd,
        modifier = modifier,
    ) {
        PriceChangePercentView(
            change = currentCategory?.market_cap_change_24h,
            accessibleTimeframe = "24 hour",
            isDownloading = isLoading,
            modifier = Modifier.semantics { invisibleToUser() }
        )
    }
}
