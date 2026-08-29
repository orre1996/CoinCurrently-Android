package com.cryptium.coincurrently.ui.main.MarketTab.CryptoDetail.Components.Overview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.invisibleToUser
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cryptium.coincurrently.AppLocale
import com.cryptium.coincurrently.ui.main.Compose.AccessibilityLayout
import com.cryptium.coincurrently.ui.main.Compose.contentScreenWidth
import com.cryptium.coincurrently.ui.main.Compose.AdaptiveStack
import com.cryptium.coincurrently.ui.main.Compose.Separator
import com.cryptium.coincurrently.ui.main.Compose.onSurfaceContainerDim
import com.cryptium.coincurrently.ui.main.Compose.onSurfaceDim
import com.cryptium.coincurrently.ui.main.Compose.primaryBackgroundNoRipple
import com.cryptium.coincurrently.ui.main.Compose.shimmeringModifier
import com.cryptium.coincurrently.ui.main.Compose.surfaceContainerLowest
import com.cryptium.coincurrently.ui.main.Compose.trendDown
import com.cryptium.coincurrently.ui.main.Compose.trendUp
import com.cryptium.coincurrently.ui.main.Extensions.changeDecimalPoints
import com.cryptium.coincurrently.ui.main.Extensions.dpToPx
import com.cryptium.coincurrently.ui.main.Extensions.formatCurrency
import com.cryptium.coincurrently.ui.main.Extensions.getDifferenceInPercent
import com.cryptium.coincurrently.ui.main.Extensions.normalize
import com.cryptium.coincurrently.ui.main.Extensions.pxToDp
import com.cryptium.coincurrently.ui.main.Extensions.roundTo
import com.cryptium.coincurrently.ui.main.Extensions.satoshiBold
import com.cryptium.coincurrently.ui.main.General.Components.Chart.hideFromTalkBackWhenEnabled
import com.cryptium.coincurrently.ui.main.General.Enums.FormattingThreshold
import networking.ResponseModels.CurrencySelectionModel
import networking.ResponseModels.NFTDetail
import networking.ResponseModels.TimeCharts
import java.text.NumberFormat
import kotlin.math.abs
import kotlin.math.min

private fun NumberFormat.formatFiniteOrDash(value: Double?): String =
    value?.takeIf { it.isFinite() }?.let { format(it) } ?: "-"

@Composable
fun NFTDetailStatisticsView(
    nftDetailInfo: NFTDetail?,
    timeChart: TimeCharts,
    prices: List<Double>?,
    chartCurrency: CurrencySelectionModel,
    isFiatSelected: Boolean,
    multiplier: Double?,
    modifier: Modifier = Modifier,
) {
    Column(
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier,
    ) {
        HeaderView()

        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .primaryBackgroundNoRipple()
        ) {
            RangeView(
                timeChart = timeChart,
                prices = prices,
                chartCurrency = chartCurrency,
                isFiatSelected = isFiatSelected,
                nftDetailInfo = nftDetailInfo,
            )

            Separator()

            MarketCapView(
                nftDetailInfo = nftDetailInfo,
                isFiatSelected = isFiatSelected,
                multiplier = multiplier,
                chartCurrency = chartCurrency
            )

            Separator()

            DailyStatsView(
                nftDetailInfo = nftDetailInfo,
                isFiatSelected = isFiatSelected,
                multiplier = multiplier,
                chartCurrency = chartCurrency
            )

            Separator()

            SupplyView(
                nftDetailInfo = nftDetailInfo,
            )
        }
    }
}

@Composable
private fun RangeView(
    timeChart: TimeCharts,
    prices: List<Double>?,
    chartCurrency: CurrencySelectionModel,
    isFiatSelected: Boolean,
    nftDetailInfo: NFTDetail?,
) {
    val isHorizontal = AccessibilityLayout.isHorizontal
    val rangeCurrency = accessibleRangeCurrency(
        isFiatSelected = isFiatSelected,
        chartCurrency = chartCurrency,
        nftDetailInfo = nftDetailInfo,
    )
    val lowAccessible = accessibleRangePrice(prices?.minOrNull(), rangeCurrency)
    val highAccessible = accessibleRangePrice(prices?.maxOrNull(), rangeCurrency)
    val rangeAccessibilityLabel = "${timeChart.rangeTitle}. From $lowAccessible to $highAccessible"

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.semantics(mergeDescendants = true) {
            contentDescription = rangeAccessibilityLabel
        },
    ) {
        Text(
            text = timeChart.rangeTitle,
            style = satoshiBold(12),
            color = MaterialTheme.colors.onSurfaceContainerDim,
            modifier = Modifier.semantics { invisibleToUser() },
        )

        Box(modifier = Modifier.hideFromTalkBackWhenEnabled()) {
            RangeProgressBar(
                prices = prices
            )
        }

        AdaptiveStack(
            isHorizontal = isHorizontal,
            spacing = 16.dp,
            horizontalAlignment = Alignment.Start,
        ) {
            Text(
                text = prices?.minOrNull()?.formatCurrency(
                    threshold = FormattingThreshold.NONE,
                    currency = chartCurrency
                ) ?: "00000000000",
                style = satoshiBold(16),
                color = MaterialTheme.colors.onSurface,
                modifier = Modifier
                    .shimmeringModifier(prices.isNullOrEmpty())
                    .then(sectionModifier())
                    .semantics { invisibleToUser() }
            )

            Text(
                text = prices?.maxOrNull()?.formatCurrency(
                    threshold = FormattingThreshold.NONE,
                    currency = chartCurrency
                ) ?: "00000000000",
                style = satoshiBold(16),
                color = MaterialTheme.colors.onSurface,
                textAlign = if (isHorizontal) TextAlign.End else TextAlign.Start,
                modifier = Modifier
                    .shimmeringModifier(prices.isNullOrEmpty())
                    .then(sectionModifier())
                    .semantics { invisibleToUser() }
            )
        }
    }
}

@Composable
private fun RangeProgressBar(
    prices: List<Double>?
) {
    val screenWidth = contentScreenWidth()
    val localDensity = LocalDensity.current

    val lowestPrice = prices?.minOrNull() ?: 0.0
    val highestPrice = prices?.maxOrNull() ?: 0.0
    val currentPrice = prices?.lastOrNull() ?: 0.0
    val openPrice = prices?.firstOrNull() ?: 0.0
    val currentPriceNormalized = listOf(openPrice, lowestPrice, highestPrice, currentPrice).normalize()

    val rangeWidth = abs((currentPriceNormalized.firstOrNull() ?: 0f) - (currentPriceNormalized.lastOrNull() ?: 0f))
    val startPoint = min(currentPriceNormalized.firstOrNull() ?: 0f, currentPriceNormalized.lastOrNull() ?: 0f)
    val rangeColor = if (currentPrice >= openPrice) MaterialTheme.colors.trendUp else MaterialTheme.colors.trendDown

    var rangeProgressBarWidth by remember { mutableStateOf(0.dp) }

    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(MaterialTheme.colors.surfaceContainerLowest)
            .fillMaxWidth()
            .height(8.dp)
            .onGloballyPositioned { coordinates ->
                rangeProgressBarWidth = with(localDensity) { coordinates.size.width.toDp() }
            }
    ) {
        Box(
            modifier = Modifier
                .padding(start = (startPoint * rangeProgressBarWidth.dpToPx()).pxToDp())
                .clip(CircleShape)
                .fillMaxHeight()
                .widthIn(
                    min = (rangeWidth * rangeProgressBarWidth.dpToPx()).pxToDp(),
                    max = screenWidth
                )
                .background(rangeColor)
        )
    }
}

@Composable
private fun HeaderView() {
    Text(
        text = "Statistics",
        style = satoshiBold(22),
        color = MaterialTheme.colors.onSurfaceDim,
        modifier = Modifier.semantics { heading() },
    )
}

@Composable
private fun MarketCapView(
    nftDetailInfo: NFTDetail?,
    isFiatSelected: Boolean,
    multiplier: Double?,
    chartCurrency: CurrencySelectionModel,
) {
    val isHorizontal = AccessibilityLayout.isHorizontal
    val marketCap = nftDetailInfo?.market_cap?.getSelectedPrice(isFiatSelected, multiplier)

    AdaptiveStack(
        isHorizontal = isHorizontal,
        spacing = 16.dp,
        horizontalAlignment = Alignment.Start,
        verticalAlignment = Alignment.Top,
        modifier = Modifier.fillMaxWidth(),
    ) {
        StatItem(
            label = "Market Cap Rank",
            value = if (nftDetailInfo?.market_cap_rank == null) {
                "-"
            } else {
                "#${nftDetailInfo.market_cap_rank}"
            },
            isLoading = nftDetailInfo == null,
            accessibilityLabel = if (nftDetailInfo?.market_cap_rank == null) {
                "Market Cap Rank. No rank"
            } else {
                "Market Cap Rank. ${nftDetailInfo.market_cap_rank}"
            },
            modifier = sectionModifier(),
        )

        if (!isHorizontal) {
            Separator()
        }

        StatItem(
            label = "Market Cap",
            value = marketCap?.formatCurrency(
                threshold = FormattingThreshold.NONE,
                currency = chartCurrency
            ) ?: "00000000000",
            isLoading = nftDetailInfo?.market_cap == null,
            accessibilityLabel = shortCurrencyAccessibilityLabel(
                title = "Market Cap",
                value = marketCap,
                currency = chartCurrency,
            ),
            modifier = sectionModifier(),
        )
    }
}

@Composable
private fun DailyStatsView(
    nftDetailInfo: NFTDetail?,
    isFiatSelected: Boolean,
    multiplier: Double?,
    chartCurrency: CurrencySelectionModel,
) {
    val isHorizontal = AccessibilityLayout.isHorizontal
    val volume = nftDetailInfo?.volume_24h?.getSelectedPrice(isFiatSelected, multiplier)
    val dailySales = if (nftDetailInfo?.one_day_sales == null) {
        "-"
    } else {
        nftDetailInfo.one_day_sales.toString()
    }

    AdaptiveStack(
        isHorizontal = isHorizontal,
        spacing = 16.dp,
        horizontalAlignment = Alignment.Start,
        verticalAlignment = Alignment.Top,
        modifier = Modifier.fillMaxWidth(),
    ) {
        StatItem(
            label = "24 Hour Sales",
            value = dailySales,
            isLoading = nftDetailInfo == null,
            accessibilityLabel = "24 hour sales. ${nftDetailInfo?.one_day_sales ?: 0}",
            modifier = sectionModifier(),
        )

        if (!isHorizontal) {
            Separator()
        }

        StatItem(
            label = "24 Hour Volume",
            value = volume?.formatCurrency(
                threshold = FormattingThreshold.NONE,
                currency = chartCurrency
            ) ?: "00000000000",
            isLoading = nftDetailInfo?.volume_24h == null,
            accessibilityLabel = shortCurrencyAccessibilityLabel(
                title = "24 Hour Volume",
                value = volume,
                currency = chartCurrency,
            ),
            modifier = sectionModifier(),
        )
    }
}

@Composable
private fun SupplyView(
    nftDetailInfo: NFTDetail?,
) {
    val isHorizontal = AccessibilityLayout.isHorizontal
    val formatter = NumberFormat.getNumberInstance(AppLocale.enUsPosix).apply {
        maximumFractionDigits = 0
        minimumFractionDigits = 0
        isGroupingUsed = true
    }

    val totalSupply = nftDetailInfo?.total_supply?.takeIf { it.isFinite() && it != 0.0 }
    val uniqueOwners = nftDetailInfo?.number_of_unique_addresses?.takeIf { it.isFinite() }
    val percent = totalSupply?.let { supply ->
        getDifferenceInPercent(
            between = supply - (uniqueOwners ?: 0.0),
            and = supply
        )?.takeIf { it.isFinite() }
    }

    val decimals = percent?.changeDecimalPoints ?: 0
    val roundedChange = percent?.roundTo(places = decimals)?.takeIf { it.isFinite() }

    val tempFormatter = NumberFormat.getNumberInstance(AppLocale.enUsPosix).apply {
        maximumFractionDigits = decimals
        minimumFractionDigits = decimals
        isGroupingUsed = true
    }

    val formattedUniqueOwners = tempFormatter.formatFiniteOrDash(uniqueOwners)
    val formattedPercentOwners = roundedChange?.let { tempFormatter.format(abs(it)) }
    val ownersLabel = if (formattedPercentOwners != null) {
        "$formattedUniqueOwners ($formattedPercentOwners%)"
    } else {
        formattedUniqueOwners
    }

    AdaptiveStack(
        isHorizontal = isHorizontal,
        spacing = 16.dp,
        horizontalAlignment = Alignment.Start,
        verticalAlignment = Alignment.Top,
        modifier = Modifier.fillMaxWidth(),
    ) {
        StatItem(
            label = "Unique Owners",
            value = ownersLabel,
            isLoading = nftDetailInfo?.number_of_unique_addresses == null,
            modifier = sectionModifier(),
        )

        if (!isHorizontal) {
            Separator()
        }

        StatItem(
            label = "Total Supply",
            value = nftDetailInfo?.total_supply
                ?.takeIf { it.isFinite() }
                ?.let { formatter.format(it) }
                ?: "Unknown",
            isLoading = nftDetailInfo?.total_supply == null,
            modifier = sectionModifier(),
        )
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String,
    isLoading: Boolean,
    modifier: Modifier = Modifier,
    accessibilityLabel: String? = null,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = Alignment.Start,
        modifier = modifier
            .semantics(mergeDescendants = true) {
                contentDescription = accessibilityLabel ?: "$label. $value"
            },
    ) {
        Text(
            text = label,
            style = satoshiBold(12),
            color = MaterialTheme.colors.onSurfaceContainerDim,
            modifier = Modifier.semantics { invisibleToUser() },
        )

        Text(
            text = value,
            style = satoshiBold(16),
            color = MaterialTheme.colors.onSurface,
            modifier = Modifier
                .shimmeringModifier(isLoading)
                .semantics { invisibleToUser() }
        )
    }
}

private fun shortCurrencyAccessibilityLabel(
    title: String,
    value: Double?,
    currency: CurrencySelectionModel,
): String {
    val formattedValue = value?.formatCurrency(
        threshold = FormattingThreshold.NONE,
        currency = currency,
        accessible = true,
    ) ?: ""
    return "$title, $formattedValue"
}

private fun accessibleRangePrice(
    value: Double?,
    currency: CurrencySelectionModel,
): String {
    return value?.formatCurrency(
        threshold = FormattingThreshold.NONE,
        currency = currency,
        accessible = true,
    ) ?: ""
}

private fun accessibleRangeCurrency(
    isFiatSelected: Boolean,
    chartCurrency: CurrencySelectionModel,
    nftDetailInfo: NFTDetail?,
): CurrencySelectionModel {
    if (isFiatSelected) return chartCurrency

    val symbol = nftDetailInfo?.native_currency_symbol?.trim().orEmpty()
    if (symbol.isEmpty()) return chartCurrency

    val spelledSymbol = symbol.uppercase().toCharArray().joinToString(" ") { it.toString() }
    return CurrencySelectionModel(
        currency = spelledSymbol,
        code = symbol,
        symbol = symbol,
        suffix = true,
        readAccessibilityAsSymbol = true,
    )
}
