package com.cryptium.coincurrently.features.Home.Carousel
import android.annotation.SuppressLint
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.DropdownMenu
import androidx.compose.material.Icon
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.invisibleToUser
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.onLongClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.cryptium.coincurrently.sharedui.components.AccessibilityLayout
import com.cryptium.coincurrently.sharedui.components.AdaptiveStack
import com.cryptium.coincurrently.sharedui.components.CachedUrlImage
import com.cryptium.coincurrently.R
import com.cryptium.coincurrently.sharedui.modifiers.MinimumTextSize
import com.cryptium.coincurrently.sharedui.modifiers.scaledImageSize
import com.cryptium.coincurrently.sharedui.theme.onSurfaceContainerDim
import com.cryptium.coincurrently.sharedui.theme.onSurfaceDim
import com.cryptium.coincurrently.sharedui.theme.onSurfaceDimmer
import com.cryptium.coincurrently.sharedui.modifiers.shimmeringModifier
import com.cryptium.coincurrently.core.extensions.changePercentDecimalPoints
import com.cryptium.coincurrently.core.extensions.directionAccessible
import com.cryptium.coincurrently.core.localization.Localization
import com.cryptium.coincurrently.core.extensions.formatCurrency
import com.cryptium.coincurrently.core.extensions.numberAccessible
import com.cryptium.coincurrently.core.extensions.satoshiBold
import com.cryptium.coincurrently.sharedui.components.PriceChangePercentView
import com.cryptium.coincurrently.sharedui.models.FormattingThreshold
import com.cryptium.coincurrently.sharedui.models.PriceChange
import com.cryptium.coincurrently.sharedui.loading.ChangeSize
import com.cryptium.coincurrently.core.networking.ResponseModels.Toplist
import com.cryptium.coincurrently.core.networking.ResponseModels.getPreferredChange
import com.cryptium.coincurrently.core.networking.ResponseModels.getPriceChange

@SuppressLint("UnrememberedMutableInteractionSource")
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ToplistCard(
    coin: Toplist?,
    isFavorited: Boolean,
    selectedChangeOption: PriceChange,
    isFirstElement: Boolean = false,
    listIndex: Int = 0,
    listCount: Int = 0,
    navigateToCryptoDetail: () -> Unit,
    toggleFavorite: () -> Unit,
) {
    val isDropDownExpanded = remember { mutableStateOf(false) }
    val isHorizontal = AccessibilityLayout.isHorizontal
    val change = coin?.getPriceChange(selectedChangeOption = selectedChangeOption)
    val safeChange = if (change?.isInfinite() == true) 0.0 else change
    val decimals = safeChange?.changePercentDecimalPoints ?: 2
    val accessibleChange = safeChange?.numberAccessible(maxPlaces = decimals) ?: ""
    val direction = change?.directionAccessible ?: ""
    val accessibilityLabelText = listOf(
        Localization.format(
            "Currency %@ of %@, %@",
            (listIndex + 1).toString(),
            listCount.toString(),
            coin?.name ?: "",
        ),
        Localization.format(
            "Price: %@",
            coin?.current_price?.formatCurrency(threshold = FormattingThreshold.NONE, accessible = true) ?: "",
        ),
        Localization.format(
            "%@ price change: %@ %@ percent",
            Localization.string(selectedChangeOption.accessibilityTimeframe),
            if (direction.isEmpty()) "" else Localization.string(direction),
            accessibleChange,
        ),
        Localization.format(
            "Market cap: %@",
            (coin?.market_cap ?: 0.0).formatCurrency(accessible = true),
        ),
    ).joinToString(separator = " - ")

    Box {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .semantics(mergeDescendants = true) {
                    contentDescription = accessibilityLabelText
                    onClick(
                        label = Localization.format(
                            "Double tap for %@ details. Long press for more options",
                            coin?.name ?: "",
                        ),
                        action = {
                        navigateToCryptoDetail.invoke()
                        true
                    })
                    onLongClick(label = "More options", action = {
                        isDropDownExpanded.value = true
                        true
                    })
                }
                .combinedClickable(
                    interactionSource = MutableInteractionSource(),
                    indication = ripple(),
                    onClick = {
                        navigateToCryptoDetail.invoke()
                    },
                    onLongClick = {
                        isDropDownExpanded.value = true
                    }
                )
                .padding(
                    top = if(isFirstElement) 0.dp else 10.dp,
                    start = 16.dp,
                    end = 16.dp,
                    bottom = 10.dp,
                )
        ) {
            MarketCapRankView(
                coin = coin,
                modifier = Modifier.semantics { invisibleToUser() }
            )

            AdaptiveStack(
                isHorizontal = isHorizontal,
                spacing = 8.dp,
                horizontalAlignment = Alignment.Start,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f),
            ) {
                CoinImageView(
                    imageUrl = coin?.image,
                    modifier = Modifier.semantics { invisibleToUser() }
                )

                CoinStatsView(
                    symbol = coin?.symbol,
                    marketCap = coin?.market_cap,
                    modifier = if (isHorizontal) {
                        Modifier
                            .weight(0.7f)
                            .padding(end = 8.dp)
                            .semantics { invisibleToUser() }
                    } else {
                        sectionModifier()
                            .semantics { invisibleToUser() }
                    },
                )

                PriceView(
                    currentPrice = coin?.current_price,
                    isHorizontal = isHorizontal,
                    modifier = (if (isHorizontal) {
                        Modifier.weight(1f)
                    } else {
                        Modifier.fillMaxWidth()
                    }).semantics { invisibleToUser() },
                )
            }

            PriceChangeView(
                change = coin?.getPriceChange(selectedChangeOption = selectedChangeOption),
                isLoading = coin == null,
                modifier = Modifier.semantics { invisibleToUser() }
            )
        }

        CoinDropdownMenu(
            isFavorited = isFavorited,
            isDropDownExpanded = isDropDownExpanded,
            toggleFavorite = {
                toggleFavorite.invoke()
            }
        )
    }
}

@Composable
private fun MarketCapRankView(
    coin: Toplist?,
    modifier: Modifier = Modifier,
) {
    MinimumTextSize(
        text = "9999",
        font = satoshiBold(16),
    ) {
        Text(
            text = if (coin == null) "-" else "${coin.market_cap_rank ?: "-"}",
            style = satoshiBold(16),
            color = MaterialTheme.colors.onSurfaceDim,
            modifier = Modifier
                .then(modifier)
                .shimmeringModifier(coin == null)
        )
    }
}

@Composable
private fun CoinImageView(
    imageUrl: String?,
    modifier: Modifier = Modifier,
) {
    CachedUrlImage(
        model = imageUrl,
        size = 28.dp,
        modifier = modifier,
    )
}

@Composable
private fun CoinStatsView(
    symbol: String?,
    marketCap: Double?,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
    ) {
        Text(
            text = symbol?.uppercase() ?: "0000",
            style = satoshiBold(16),
            color = MaterialTheme.colors.onSurface,
            modifier = Modifier
                .shimmeringModifier(symbol == null)
        )

        Text(
            text = marketCap?.formatCurrency() ?: "00000000",
            style = satoshiBold(12),
            color = MaterialTheme.colors.onSurfaceDimmer,
            modifier = Modifier
                .shimmeringModifier(marketCap == null)
        )
    }
}

@Composable
private fun PriceView(
    currentPrice: Double?,
    isHorizontal: Boolean,
    modifier: Modifier,
) {
    Text(
        text = currentPrice?.formatCurrency(threshold = FormattingThreshold.NONE) ?: "00000000",
        style = satoshiBold(16),
        color = MaterialTheme.colors.onSurface,
        textAlign = if (isHorizontal) TextAlign.End else TextAlign.Start,
        modifier = modifier
            .shimmeringModifier(currentPrice == null),
    )
}

@Composable
private fun PriceChangeView(
    change: Double?,
    isLoading: Boolean,
    modifier: Modifier = Modifier,
) {
    MinimumTextSize(
        text = "999.9%",
        font = satoshiBold(ChangeSize.MEDIUM.fontSize),
        extraSize = scaledImageSize(ChangeSize.MEDIUM.triangleSize) + 4.dp,
        alignment = Alignment.CenterEnd,
        modifier = modifier,
    ) {
        PriceChangePercentView(
            change = change,
            size = ChangeSize.MEDIUM,
            isDownloading = isLoading
        )
    }
}

@Composable
private fun CoinDropdownMenu(
    isFavorited: Boolean,
    isDropDownExpanded: MutableState<Boolean>,
    toggleFavorite: () -> Unit
) {
    DropdownMenu(
        expanded = isDropDownExpanded.value,
        onDismissRequest = {
            isDropDownExpanded.value = false
        },
         offset = DpOffset(x = 80.dp, y = 0.dp),
    ) {
        DropdownMenuItem(
            onClick = {
                isDropDownExpanded.value = false
                toggleFavorite()
            },
            text = {
                Text(
                    Localization.string(
                        if (isFavorited) {
                            "Remove from favorites"
                        } else {
                            "Add to favorites"
                        },
                    ),
                    style = satoshiBold(16)
                )
            },
            trailingIcon = {
                Icon(
                    painter = painterResource(
                        id = if (isFavorited) {
                            R.drawable.favorite_filled
                        } else {
                            R.drawable.favorite_outline
                        }
                    ),
                    contentDescription = null
                )
            }
        )
    }
}