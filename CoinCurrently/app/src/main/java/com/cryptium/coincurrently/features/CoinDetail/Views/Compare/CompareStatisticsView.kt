package com.cryptium.coincurrently.features.CoinDetail.Views.Compare
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.invisibleToUser
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.cryptium.coincurrently.core.localization.Localization
import com.cryptium.coincurrently.sharedui.components.AccessibilityLayout
import com.cryptium.coincurrently.sharedui.components.AdaptiveStack
import com.cryptium.coincurrently.sharedui.components.Separator
import com.cryptium.coincurrently.sharedui.theme.blue50
import com.cryptium.coincurrently.sharedui.theme.onSurfaceDim
import com.cryptium.coincurrently.sharedui.theme.onSurfaceDimmer
import com.cryptium.coincurrently.sharedui.theme.primary50
import com.cryptium.coincurrently.sharedui.theme.primaryBackgroundNoRipple
import com.cryptium.coincurrently.sharedui.modifiers.shimmeringModifier
import com.cryptium.coincurrently.core.extensions.formatCurrency
import com.cryptium.coincurrently.core.extensions.formatGroupedWholeNumber
import com.cryptium.coincurrently.core.extensions.getDifferenceInPercent
import com.cryptium.coincurrently.core.extensions.getSelectedCurrency
import com.cryptium.coincurrently.core.extensions.satoshiBold
import com.cryptium.coincurrently.sharedui.components.CustomProgressBarView
import com.cryptium.coincurrently.sharedui.charts.hideFromTalkBackWhenEnabled
import com.cryptium.coincurrently.sharedui.components.PriceChangePercentView
import com.cryptium.coincurrently.sharedui.models.FormattingThreshold
import com.cryptium.coincurrently.sharedui.loading.ChangeSize
import com.cryptium.coincurrently.core.networking.ResponseModels.CryptoDetailInfoModel
import com.cryptium.coincurrently.core.networking.ResponseModels.TimeCharts

@Composable
fun CompareStatisticsView(
    mainCryptoInfo: CryptoDetailInfoModel?,
    compareCryptoInfo: CryptoDetailInfoModel?,
    mainPrices: List<Double>?,
    comparePrices: List<Double>?,
    selectedTimeframe: TimeCharts,
    modifier: Modifier = Modifier,
) {
    Column(
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
            .padding(
                top = 32.dp,
                start = 16.dp,
                bottom = 32.dp,
                end = 16.dp
            )
    ) {
        HeaderView()

        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .primaryBackgroundNoRipple()
        ) {
            PriceView(
                mainCryptoInfo = mainCryptoInfo,
                compareCryptoInfo = compareCryptoInfo
            )

            Separator()

            PriceChangeView(
                mainCryptoInfo = mainCryptoInfo,
                compareCryptoInfo = compareCryptoInfo,
                mainPrices = mainPrices,
                comparePrices = comparePrices,
                selectedTimeframe = selectedTimeframe,
            )

            Separator()

            MarketCapView(
                mainCryptoInfo = mainCryptoInfo,
                compareCryptoInfo = compareCryptoInfo
            )

            Separator()

            VolumeView(
                mainCryptoInfo = mainCryptoInfo,
                compareCryptoInfo = compareCryptoInfo
            )

            Separator()

            SupplyView(
                mainCryptoInfo = mainCryptoInfo,
                compareCryptoInfo = compareCryptoInfo
            )
        }
    }
}

@Composable
private fun HeaderView() {
    Text(
        text = Localization.string("Statistics"),
        style = satoshiBold(22),
        color = MaterialTheme.colors.onSurfaceDim,
        modifier = Modifier.semantics { heading() },
    )
}

@Composable
private fun PriceView(
    mainCryptoInfo: CryptoDetailInfoModel?,
    compareCryptoInfo: CryptoDetailInfoModel?,
) {
    val isHorizontal = AccessibilityLayout.isHorizontal
    val mainPrice = mainCryptoInfo?.market_data?.current_price?.getSelectedCurrency()
    val comparePrice = compareCryptoInfo?.market_data?.current_price?.getSelectedCurrency()
    val childModifier = Modifier.semantics { invisibleToUser() }

    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.semantics(mergeDescendants = true) {},
    ) {
        Text(
            text = Localization.string("Price"),
            style = satoshiBold(12),
            color = MaterialTheme.colors.onSurfaceDim
        )

        AdaptiveStack(
            isHorizontal = isHorizontal,
            spacing = if (isHorizontal) 16.dp else 8.dp,
            horizontalAlignment = Alignment.Start,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = sectionModifier()
                    .clearAndSetSemantics {
                        contentDescription = accessibleComparePriceLabel(
                            name = mainCryptoInfo?.name,
                            value = mainPrice,
                        )
                    },
            ) {
                CompareRectangleView(
                    color = MaterialTheme.colors.primary50
                )

                Text(
                    text = mainPrice?.formatCurrency(threshold = FormattingThreshold.NONE) ?: "000000000",
                    style = satoshiBold(16),
                    color = MaterialTheme.colors.onSurface,
                    modifier = Modifier
                        .shimmeringModifier(mainPrice == null)
                        .then(childModifier)
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = sectionModifier()
                    .clearAndSetSemantics {
                        contentDescription = accessibleComparePriceLabel(
                            name = compareCryptoInfo?.name,
                            value = comparePrice,
                        )
                    },
            ) {
                CompareRectangleView(
                    color = MaterialTheme.colors.blue50
                )

                Text(
                    text = comparePrice?.formatCurrency(threshold = FormattingThreshold.NONE) ?: "000000000",
                    style = satoshiBold(16),
                    color = MaterialTheme.colors.onSurface,
                    modifier = Modifier
                        .shimmeringModifier(comparePrice == null)
                        .then(childModifier)
                )
            }
        }
    }
}

@Composable
private fun PriceChangeView(
    mainCryptoInfo: CryptoDetailInfoModel?,
    compareCryptoInfo: CryptoDetailInfoModel?,
    mainPrices: List<Double>?,
    comparePrices: List<Double>?,
    selectedTimeframe: TimeCharts,
) {
    val isHorizontal = AccessibilityLayout.isHorizontal
    val mainPriceChange = getDifferenceInPercent(mainPrices?.lastOrNull() ?: 0.0, mainPrices?.firstOrNull() ?: 0.0)
    val comparePriceChange = getDifferenceInPercent(comparePrices?.lastOrNull() ?: 0.0, comparePrices?.firstOrNull() ?: 0.0)
    val childModifier = Modifier.semantics { invisibleToUser() }

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.semantics(mergeDescendants = true) {},
    ) {
        Text(
            text = Localization.string("Price Change"),
            style = satoshiBold(12),
            color = MaterialTheme.colors.onSurfaceDim
        )

        AdaptiveStack(
            isHorizontal = isHorizontal,
            spacing = if (isHorizontal) 16.dp else 8.dp,
            horizontalAlignment = Alignment.Start,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = sectionModifier()
                    .clearAndSetSemantics {
                        contentDescription = accessibleComparePriceChangeLabel(
                            name = mainCryptoInfo?.name,
                            change = mainPriceChange,
                            selectedTimeframe = selectedTimeframe,
                        )
                    },
            ) {
                CompareRectangleView(
                    color = MaterialTheme.colors.primary50
                )

                PriceChangePercentView(
                    change = mainPriceChange,
                    size = ChangeSize.MEDIUM,
                    isDownloading = mainPrices == null,
                    modifier = childModifier,
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = sectionModifier()
                    .clearAndSetSemantics {
                        contentDescription = accessibleComparePriceChangeLabel(
                            name = compareCryptoInfo?.name,
                            change = comparePriceChange,
                            selectedTimeframe = selectedTimeframe,
                        )
                    },
            ) {
                CompareRectangleView(
                    color = MaterialTheme.colors.blue50
                )

                PriceChangePercentView(
                    change = comparePriceChange,
                    size = ChangeSize.MEDIUM,
                    isDownloading = mainPrices == null,
                    modifier = childModifier,
                )
            }
        }
    }
}

@Composable
private fun MarketCapView(
    mainCryptoInfo: CryptoDetailInfoModel?,
    compareCryptoInfo: CryptoDetailInfoModel?,
) {
    val isHorizontal = AccessibilityLayout.isHorizontal
    val mainMarketCap = mainCryptoInfo?.market_data?.market_cap?.getSelectedCurrency()
    val mainRank = mainCryptoInfo?.market_cap_rank
    val compareMarketCap = compareCryptoInfo?.market_data?.market_cap?.getSelectedCurrency()
    val compareRank = compareCryptoInfo?.market_cap_rank
    val childModifier = Modifier.semantics { invisibleToUser() }

    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.semantics(mergeDescendants = true) {},
    ) {
        Text(
            text = Localization.string("Market Cap"),
            style = satoshiBold(12),
            color = MaterialTheme.colors.onSurfaceDim
        )

        CustomProgressBarView(
            progress = mainMarketCap?.div(compareMarketCap ?: 0.0),
            foregroundColor = MaterialTheme.colors.primary50,
            backgroundColor = Color.Transparent,
            downloading = mainMarketCap == null || compareMarketCap == null,
            modifier = Modifier
                .padding(top = 8.dp)
                .hideFromTalkBackWhenEnabled()
        )

        CustomProgressBarView(
            progress = compareMarketCap?.div(mainMarketCap ?: 0.0),
            foregroundColor = MaterialTheme.colors.blue50,
            backgroundColor = Color.Transparent,
            downloading = mainMarketCap == null || compareMarketCap == null,
            modifier = Modifier
                .padding(bottom = 8.dp)
                .hideFromTalkBackWhenEnabled()
        )

        AdaptiveStack(
            isHorizontal = isHorizontal,
            spacing = if (isHorizontal) 16.dp else 8.dp,
            horizontalAlignment = Alignment.Start,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = sectionModifier()
                    .clearAndSetSemantics {
                        contentDescription = accessibleCompareMarketCapLabel(
                            name = mainCryptoInfo?.name,
                            value = mainMarketCap,
                            rank = mainRank,
                        )
                    },
            ) {
                CompareRectangleView(
                    color = MaterialTheme.colors.primary50
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = childModifier,
                ) {
                    Text(
                        text = mainMarketCap?.formatCurrency() ?: "000000000",
                        style = satoshiBold(16),
                        color = MaterialTheme.colors.onSurface,
                        modifier = Modifier
                            .shimmeringModifier(mainMarketCap == null)
                    )

                    Text(
                        text = "#${mainRank ?: 0}",
                        style = satoshiBold(16),
                        color = MaterialTheme.colors.onSurfaceDimmer,
                        modifier = Modifier
                            .shimmeringModifier(mainRank == null)
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = sectionModifier()
                    .clearAndSetSemantics {
                        contentDescription = accessibleCompareMarketCapLabel(
                            name = compareCryptoInfo?.name,
                            value = compareMarketCap,
                            rank = compareRank,
                        )
                    },
            ) {
                CompareRectangleView(
                    color = MaterialTheme.colors.blue50
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = childModifier,
                ) {
                    Text(
                        text = compareMarketCap?.formatCurrency() ?: "000000000",
                        style = satoshiBold(16),
                        color = MaterialTheme.colors.onSurface,
                        modifier = Modifier
                            .shimmeringModifier(compareMarketCap == null)
                    )

                    Text(
                        text = "#${compareRank ?: 0}",
                        style = satoshiBold(16),
                        color = MaterialTheme.colors.onSurfaceDimmer,
                        modifier = Modifier
                            .shimmeringModifier(compareRank == null)
                    )
                }
            }
        }
    }
}

@Composable
private fun VolumeView(
    mainCryptoInfo: CryptoDetailInfoModel?,
    compareCryptoInfo: CryptoDetailInfoModel?,
) {
    val isHorizontal = AccessibilityLayout.isHorizontal
    val mainVolume = mainCryptoInfo?.market_data?.total_volume?.getSelectedCurrency()
    val compareVolume = compareCryptoInfo?.market_data?.total_volume?.getSelectedCurrency()
    val childModifier = Modifier.semantics { invisibleToUser() }

    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.semantics(mergeDescendants = true) {},
    ) {
        Text(
            text = Localization.string("24 Hour Volume"),
            style = satoshiBold(12),
            color = MaterialTheme.colors.onSurfaceDim
        )

        CustomProgressBarView(
            progress = mainVolume?.div(compareVolume ?: 0.0),
            foregroundColor = MaterialTheme.colors.primary50,
            backgroundColor = Color.Transparent,
            downloading = mainVolume == null || compareVolume == null,
            modifier = Modifier
                .padding(top = 8.dp)
                .hideFromTalkBackWhenEnabled()
        )

        CustomProgressBarView(
            progress = compareVolume?.div(mainVolume ?: 0.0),
            foregroundColor = MaterialTheme.colors.blue50,
            backgroundColor = Color.Transparent,
            downloading = mainVolume == null || compareVolume == null,
            modifier = Modifier
                .padding(bottom = 8.dp)
                .hideFromTalkBackWhenEnabled()
        )

        AdaptiveStack(
            isHorizontal = isHorizontal,
            spacing = if (isHorizontal) 16.dp else 8.dp,
            horizontalAlignment = Alignment.Start,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = sectionModifier()
                    .clearAndSetSemantics {
                        contentDescription = accessibleCompareVolumeLabel(
                            name = mainCryptoInfo?.name,
                            value = mainVolume,
                        )
                    },
            ) {
                CompareRectangleView(
                    color = MaterialTheme.colors.primary50
                )

                Text(
                    text = mainVolume?.formatCurrency() ?: "000000000",
                    style = satoshiBold(16),
                    color = MaterialTheme.colors.onSurface,
                    modifier = Modifier
                        .shimmeringModifier(mainVolume == null)
                        .then(childModifier)
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = sectionModifier()
                    .clearAndSetSemantics {
                        contentDescription = accessibleCompareVolumeLabel(
                            name = compareCryptoInfo?.name,
                            value = compareVolume,
                        )
                    },
            ) {
                CompareRectangleView(
                    color = MaterialTheme.colors.blue50
                )

                Text(
                    text = compareVolume?.formatCurrency() ?: "000000000",
                    style = satoshiBold(16),
                    color = MaterialTheme.colors.onSurface,
                    modifier = Modifier
                        .shimmeringModifier(compareVolume == null)
                        .then(childModifier)
                )
            }
        }
    }
}

@Composable
private fun SupplyView(
    mainCryptoInfo: CryptoDetailInfoModel?,
    compareCryptoInfo: CryptoDetailInfoModel?,
) {
    val isHorizontal = AccessibilityLayout.isHorizontal
    val mainSupply = mainCryptoInfo?.market_data?.circulating_supply
    val compareSupply = compareCryptoInfo?.market_data?.circulating_supply
    val childModifier = Modifier.semantics { invisibleToUser() }

    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.semantics(mergeDescendants = true) {},
    ) {
        Text(
            text = Localization.string("Circulating Supply"),
            style = satoshiBold(12),
            color = MaterialTheme.colors.onSurfaceDim
        )

        CustomProgressBarView(
            progress = mainSupply?.div(compareSupply ?: 0.0),
            foregroundColor = MaterialTheme.colors.primary50,
            backgroundColor = Color.Transparent,
            downloading = mainSupply == null || compareSupply == null,
            modifier = Modifier
                .padding(top = 8.dp)
                .hideFromTalkBackWhenEnabled()
        )

        CustomProgressBarView(
            progress = compareSupply?.div(mainSupply ?: 0.0),
            foregroundColor = MaterialTheme.colors.blue50,
            backgroundColor = Color.Transparent,
            downloading = mainSupply == null || compareSupply == null,
            modifier = Modifier
                .padding(bottom = 8.dp)
                .hideFromTalkBackWhenEnabled()
        )

        AdaptiveStack(
            isHorizontal = isHorizontal,
            spacing = if (isHorizontal) 16.dp else 8.dp,
            horizontalAlignment = Alignment.Start,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = sectionModifier()
                    .clearAndSetSemantics {
                        contentDescription = accessibleCompareSupplyLabel(
                            name = mainCryptoInfo?.name,
                            value = mainSupply,
                        )
                    },
            ) {
                CompareRectangleView(
                    color = MaterialTheme.colors.primary50
                )

                Text(
                    text = mainSupply?.formatGroupedWholeNumber() ?: "000000000",
                    style = satoshiBold(16),
                    color = MaterialTheme.colors.onSurface,
                    modifier = Modifier
                        .shimmeringModifier(mainSupply == null)
                        .then(childModifier)
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = sectionModifier()
                    .clearAndSetSemantics {
                        contentDescription = accessibleCompareSupplyLabel(
                            name = compareCryptoInfo?.name,
                            value = compareSupply,
                        )
                    },
            ) {
                CompareRectangleView(
                    color = MaterialTheme.colors.blue50
                )

                Text(
                    text = compareSupply?.formatGroupedWholeNumber() ?: "000000000",
                    style = satoshiBold(16),
                    color = MaterialTheme.colors.onSurface,
                    modifier = Modifier
                        .shimmeringModifier(compareSupply == null)
                        .then(childModifier)
                )
            }
        }
    }
}
