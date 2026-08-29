package com.cryptium.coincurrently.ui.main.MarketTab.CryptoDetail.Components.Markets

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.invisibleToUser
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cryptium.coincurrently.R
import com.cryptium.coincurrently.ui.main.Compose.AccessibilityLayout
import com.cryptium.coincurrently.ui.main.Compose.tabPageTalkBackTraversal
import com.cryptium.coincurrently.ui.main.Compose.AdaptiveStack
import com.cryptium.coincurrently.ui.main.Compose.CachedUrlImage
import com.cryptium.coincurrently.ui.main.Compose.MinimumTextSize
import com.cryptium.coincurrently.ui.main.Compose.SecondaryButton
import com.cryptium.coincurrently.ui.main.Compose.onSurfaceDim
import com.cryptium.coincurrently.ui.main.Compose.onSurfaceDimmer
import com.cryptium.coincurrently.ui.main.Compose.outlineVariant
import com.cryptium.coincurrently.ui.main.Compose.shimmeringModifier
import com.cryptium.coincurrently.ui.main.Extensions.satoshiBold
import com.cryptium.coincurrently.ui.main.Extensions.satoshiMedium
import com.cryptium.coincurrently.ui.main.General.Components.EmptyContent
import com.cryptium.coincurrently.ui.main.General.Enums.SortOption
import com.cryptium.coincurrently.ui.main.analytics.AnalyticsManager
import com.cryptium.coincurrently.ui.main.analytics.ScreenLabel
import com.cryptium.coincurrently.ui.main.analytics.SheetOpened
import com.cryptium.coincurrently.ui.main.analytics.SheetLabel
import com.cryptium.coincurrently.ui.main.analytics.WebViewOpened

/**
 * Generic Markets view for detail screens (Crypto, NFT, Exchange).
 * Use with DetailMarketsConfig and the appropriate toDetailMarketsData() extension.
 */
fun LazyListScope.CryptoDetailMarketsView(
    config: DetailMarketsConfig,
    items: List<DetailMarketsItem>?,
    selectedSortOption: SortOption,
    isSortShowing: MutableState<Boolean>,
    errorMessage: String,
    fromScreen: ScreenLabel,
    openUrl: (String) -> Unit,
    isTablet: Boolean
) {
    val openTradeUrlWithAnalytics: (String) -> Unit = { url ->
        AnalyticsManager.log(WebViewOpened(fromScreen = fromScreen, url = url))
        openUrl(url)
    }
    val showVolume = items?.any { it.volume != null } == true
    val useExchangeStyle = !config.useFaviconFormat
    val headerLabel = config.headers.firstOrNull().orEmpty()

    if (items == null || items.isNotEmpty()) {
        item {
            Box(
                modifier = Modifier
                    .tabPageTalkBackTraversal(groupChildren = true)
                    .padding(
                    start = 16.dp,
                    end = 16.dp,
                    top = 32.dp
                )
            ) {
                SecondaryButton(
                    text = selectedSortOption.title,
                    accessibilityLabel = "Sort. currently selected. ${selectedSortOption.accessibilityTitle}",
                    icon = R.drawable.sort,
                    onClick = {
                        AnalyticsManager.log(
                            SheetOpened(
                                fromScreen = fromScreen,
                                sheet = SheetLabel.SortBy,
                            ),
                        )
                        isSortShowing.value = true
                    }
                )
            }
        }

        item {
            Box(
                modifier = Modifier
                    .clearAndSetSemantics { invisibleToUser() }
                    .padding(
                    start = 16.dp,
                    top = 24.dp,
                    end = 16.dp,
                    bottom = 6.dp
                )
            ) {
                DetailMarketsHeaderView(
                    headerLabel = headerLabel,
                    showVolume = showVolume,
                )
            }
        }
    }

    when {
        items == null -> TempList(
            config = config,
            showVolume = showVolume,
            useExchangeStyle = useExchangeStyle,
            isTablet = isTablet,
        )

        items.isEmpty() -> {
            item {
                EmptyContent(
                    title = "No markets yet",
                    subTitle = "When this $errorMessage is added to a market, it'll show up here.",
                    modifier = Modifier
                        .tabPageTalkBackTraversal(groupChildren = true)
                        .padding(
                            vertical = 72.dp,
                            horizontal = 32.dp
                        )
                )
            }
        }

        else -> {
            itemsIndexed(items) { index, item ->
                Box(modifier = Modifier.tabPageTalkBackTraversal(groupChildren = true).padding(horizontal = 16.dp)) {
                    DetailMarketsRowView(
                        item = item,
                        config = config,
                        showVolume = showVolume,
                        useExchangeStyle = useExchangeStyle,
                        isLastRow = item == items.lastOrNull(),
                        listIndex = index,
                        listCount = items.size,
                        openUrl = openTradeUrlWithAnalytics
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailMarketsHeaderView(
    headerLabel: String,
    showVolume: Boolean,
) {
    val isHorizontal = AccessibilityLayout.isHorizontal

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.clearAndSetSemantics { invisibleToUser() },
    ) {
        Text(
            text = if (isHorizontal) headerLabel else "$headerLabel / Price",
            style = satoshiMedium(14),
            color = MaterialTheme.colors.onSurfaceDim,
            modifier = Modifier
                .weight(1f)
        )

        if (isHorizontal) {
            Text(
                text = "Price",
                style = satoshiMedium(14),
                color = MaterialTheme.colors.onSurfaceDim,
                maxLines = 1,
            )

            if (showVolume) {
                Text(
                    text = "24h Volume",
                    style = satoshiMedium(14),
                    color = MaterialTheme.colors.onSurfaceDim,
                    maxLines = 1,
                )
            }
        } else if (showVolume) {
            Text(
                text = "24h Volume",
                style = satoshiMedium(14),
                color = MaterialTheme.colors.onSurfaceDim,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun DetailMarketsRowView(
    item: DetailMarketsItem,
    config: DetailMarketsConfig,
    showVolume: Boolean,
    useExchangeStyle: Boolean,
    isLastRow: Boolean,
    listIndex: Int? = null,
    listCount: Int? = null,
    openUrl: (String) -> Unit
) {
    val isHorizontal = AccessibilityLayout.isHorizontal
    val headerLabel = config.headers.firstOrNull().orEmpty()
    val accessibilityLabel = buildString {
        if (listIndex != null && listCount != null) {
            append("$headerLabel ${listIndex + 1} of $listCount. ")
        }
        append(item.name ?: "")
        item.priceAccessible?.let { append(". Last price: $it") }
        if (showVolume) {
            item.volumeAccessible?.let { append(". 24 hour volume: $it") }
        }
    }

    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .clearAndSetSemantics {
                contentDescription = accessibilityLabel
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
            ) {
                item.tradeUrl?.let { openUrl(it) }
            }
            .heightIn(min = 48.dp)
            .padding(
                top = 10.dp,
                bottom = if (isLastRow) 0.dp else 10.dp
            )
    ) {
        Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .weight(1f)
                .semantics { invisibleToUser() }
        ) {
            MarketsImageView(
                imageUrl = item.imageUrl,
                sizeDp = config.imageSizeDp,
                useFaviconFormat = config.useFaviconFormat,
                useExchangeStyle = useExchangeStyle,
                hasItem = item.name != null,
            )

            AdaptiveStack(
                isHorizontal = isHorizontal,
                spacing = 0.dp,
                horizontalAlignment = Alignment.Start,
                verticalAlignment = Alignment.Top,
            ) {
        Text(
            text = item.name ?: "000000",
            style = satoshiBold(16),
            color = MaterialTheme.colors.onSurface,
            modifier = Modifier
                .shimmeringModifier(item.name == null)
                .semantics { invisibleToUser() }
        )

        if (!isHorizontal) {
            Text(
                text = item.price ?: "000000000",
                style = satoshiBold(12),
                color = MaterialTheme.colors.onSurfaceDimmer,
                modifier = Modifier
                    .shimmeringModifier(item.price == null)
                    .semantics { invisibleToUser() }
            )
        }
            }
        }

        if (isHorizontal) {
            Text(
                text = item.price ?: "000000000",
                style = satoshiBold(16),
                color = MaterialTheme.colors.onSurface,
                textAlign = TextAlign.End,
                modifier = Modifier
                    .shimmeringModifier(item.price == null)
                    .semantics { invisibleToUser() }
            )

            if (showVolume) {
                VolumeView(
                    volume = item.volume,
                )
            }
        } else if (showVolume) {
            VolumeView(
                volume = item.volume,
            )
        }
    }
}

@Composable
private fun VolumeView(
    volume: String?,
) {
    MinimumTextSize(
        text = "24h Volume",
        font = satoshiMedium(14),
        alignment = Alignment.CenterEnd
    ) {
        Text(
            text = volume ?: "000000000",
            style = satoshiBold(16),
            color = MaterialTheme.colors.onSurface,
            textAlign = TextAlign.End,
            modifier = Modifier
                .shimmeringModifier(volume == null)
                .semantics { invisibleToUser() }
        )
    }
}

@Composable
private fun MarketsImageView(
    imageUrl: String?,
    sizeDp: Int,
    useFaviconFormat: Boolean,
    useExchangeStyle: Boolean,
    hasItem: Boolean,
) {
    val size = sizeDp.dp

    if (useExchangeStyle && hasItem && imageUrl == null) {
        Box(
            modifier = Modifier
                .size(size)
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colors.outlineVariant,
                    shape = CircleShape
                )
        )
    } else {
        val imageModel = if (useFaviconFormat) {
            "https://www.google.com/s2/favicons?domain=${imageUrl ?: ""}"
        } else {
            imageUrl ?: ""
        }

        CachedUrlImage(
            model = imageModel,
            size = size,
            isDownloading = imageUrl == null,
            modifier = Modifier.semantics { invisibleToUser() },
        )
    }
}

private fun LazyListScope.TempList(
    config: DetailMarketsConfig,
    showVolume: Boolean,
    useExchangeStyle: Boolean,
    isTablet: Boolean,
) {
    val rowCount = if (isTablet) 40 else 20
    items(rowCount) { index ->
        Box(modifier = Modifier.tabPageTalkBackTraversal(groupChildren = true).padding(horizontal = 16.dp)) {
            DetailMarketsRowView(
                item = DetailMarketsItem(
                    imageUrl = null,
                    name = null,
                    price = null,
                    volume = null,
                    tradeUrl = null
                ),
                config = config,
                showVolume = showVolume,
                useExchangeStyle = useExchangeStyle,
                isLastRow = index == rowCount - 1,
                openUrl = { }
            )
        }
    }
}
