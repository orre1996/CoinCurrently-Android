package com.cryptium.coincurrently.ui.main.MarketTab.CryptoDetail.Components.Overview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cryptium.coincurrently.AppLocale
import com.cryptium.coincurrently.R
import com.cryptium.coincurrently.ui.main.Compose.AccessibilityLayout
import com.cryptium.coincurrently.ui.main.Compose.tabPageTalkBackTraversal
import com.cryptium.coincurrently.ui.main.Compose.contentScreenWidth
import com.cryptium.coincurrently.ui.main.Compose.AdaptiveStack
import com.cryptium.coincurrently.ui.main.Compose.Separator
import com.cryptium.coincurrently.ui.main.Compose.onSurfaceContainerDim
import com.cryptium.coincurrently.ui.main.Compose.onSurfaceDim
import com.cryptium.coincurrently.ui.main.Compose.onSurfaceDimmer
import com.cryptium.coincurrently.ui.main.Compose.primaryBackgroundNoRipple
import com.cryptium.coincurrently.ui.main.Compose.shimmeringModifier
import com.cryptium.coincurrently.ui.main.Compose.surfaceContainerLowest
import com.cryptium.coincurrently.ui.main.Compose.trendDown
import com.cryptium.coincurrently.ui.main.Compose.trendUp
import com.cryptium.coincurrently.ui.main.Extensions.dpToPx
import com.cryptium.coincurrently.ui.main.Extensions.formatCurrency
import com.cryptium.coincurrently.ui.main.Extensions.formatGroupedWholeNumber
import com.cryptium.coincurrently.ui.main.Extensions.getSelectedCurrency
import com.cryptium.coincurrently.ui.main.Extensions.normalize
import com.cryptium.coincurrently.ui.main.Extensions.numberAccessible
import com.cryptium.coincurrently.ui.main.Extensions.pxToDp
import com.cryptium.coincurrently.ui.main.Extensions.satoshiBold
import com.cryptium.coincurrently.ui.main.General.Components.Chart.hideFromTalkBackWhenEnabled
import com.cryptium.coincurrently.ui.main.General.Loading.ChangeSize
import com.cryptium.coincurrently.ui.main.General.Enums.FormattingThreshold
import networking.ResponseModels.CryptoDetailInfoModel
import networking.ResponseModels.CurrencySelectionModel
import networking.ResponseModels.TimeCharts
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import kotlin.math.abs
import kotlin.math.min

@Composable
fun CryptoDetailStatisticsView(
    cryptoDetailInfoModel: CryptoDetailInfoModel?,
    timeChart: TimeCharts,
    prices: List<Double>?,
    totalMarketCap: Double?,
) {
    val tabPage = Modifier.tabPageTalkBackTraversal()

    Column(
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        HeaderView(tabPage = tabPage)

        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .primaryBackgroundNoRipple()
        ) {
            RangeView(
                prices = prices,
                timeChart = timeChart,
                tabPage = tabPage,
            )

            Separator()

            MarketCapView(
                cryptoDetailInfoModel = cryptoDetailInfoModel,
                tabPage = tabPage,
            )

            Separator()

            DominanceView(
                cryptoDetailInfoModel = cryptoDetailInfoModel,
                totalMarketCap = totalMarketCap,
                tabPage = tabPage,
            )

            Separator()

            if (cryptoDetailInfoModel?.market_data?.market_cap == null || cryptoDetailInfoModel.market_data?.max_supply != null) {
                SupplyView(
                    cryptoDetailInfoModel = cryptoDetailInfoModel,
                    tabPage = tabPage,
                )
            } else {
                SupplyNoMaxView(
                    cryptoDetailInfoModel = cryptoDetailInfoModel,
                    tabPage = tabPage,
                )
            }
        }
    }
}

@Composable
private fun RangeView(
    prices: List<Double>?,
    timeChart: TimeCharts,
    tabPage: Modifier,
) {
    val isHorizontal = AccessibilityLayout.isHorizontal
    val lowAccessible = accessibleRangePrice(prices?.minOrNull())
    val highAccessible = accessibleRangePrice(prices?.maxOrNull())
    val rangeAccessibilityLabel = "${timeChart.rangeTitle}. From $lowAccessible to $highAccessible"

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = tabPage.semantics(mergeDescendants = true) {
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
                text = prices?.minOrNull()?.formatCurrency(threshold = FormattingThreshold.NONE) ?: "00000000000",
                style = satoshiBold(16),
                color = MaterialTheme.colors.onSurface,
                modifier = Modifier
                    .shimmeringModifier(prices.isNullOrEmpty())
                    .then(sectionModifier())
                    .semantics { invisibleToUser() }
            )

            Text(
                text = prices?.maxOrNull()?.formatCurrency(threshold = FormattingThreshold.NONE) ?: "00000000000",
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
    prices: List<Double>?,
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
private fun HeaderView(tabPage: Modifier) {
    Text(
        text = "Statistics",
        style = satoshiBold(22),
        color = MaterialTheme.colors.onSurfaceDim,
        modifier = tabPage.semantics { heading() },
    )
}

@Composable
private fun MarketCapView(
    cryptoDetailInfoModel: CryptoDetailInfoModel?,
    tabPage: Modifier,
) {
    val isHorizontal = AccessibilityLayout.isHorizontal
    val marketCap = cryptoDetailInfoModel?.market_data?.market_cap?.getSelectedCurrency()

    AdaptiveStack(
        isHorizontal = isHorizontal,
        spacing = 16.dp,
        horizontalAlignment = Alignment.Start,
        verticalAlignment = Alignment.Top,
        modifier = Modifier.fillMaxWidth(),
    ) {
        StatItem(
            label = "Market Cap Rank",
            value = "#${cryptoDetailInfoModel?.market_cap_rank ?: 0}",
            isLoading = cryptoDetailInfoModel?.market_cap_rank == null,
            accessibilityLabel = if (cryptoDetailInfoModel?.market_cap_rank == null) {
                "Market Cap Rank, No rank"
            } else {
                "Market Cap Rank, ${cryptoDetailInfoModel.market_cap_rank}"
            },
            modifier = sectionModifier(),
            tabPage = tabPage,
        )

        if (!isHorizontal) {
            Separator()
        }

        StatItem(
            label = "Market Cap",
            value = marketCap?.formatCurrency(
                threshold = FormattingThreshold.NONE,
                maxDecimals = if (marketCap >= 1_000_000_000) 0 else null
            ) ?: "00000000000",
            isLoading = cryptoDetailInfoModel?.market_data?.market_cap == null,
            accessibilityLabel = shortCurrencyAccessibilityLabel("Market Cap", marketCap),
            modifier = sectionModifier(),
            tabPage = tabPage,
        )
    }
}

@Composable
private fun DominanceView(
    cryptoDetailInfoModel: CryptoDetailInfoModel?,
    totalMarketCap: Double?,
    tabPage: Modifier,
) {
    val isHorizontal = AccessibilityLayout.isHorizontal
    val marketCap = cryptoDetailInfoModel?.market_data?.market_cap?.getSelectedCurrency() ?: 0.0
    val dominance = ((marketCap / (totalMarketCap ?: 0.0)) * 100)
    val volume = cryptoDetailInfoModel?.market_data?.total_volume?.getSelectedCurrency()
    val isDominanceLoading = cryptoDetailInfoModel?.market_cap_rank == null || totalMarketCap == null

    AdaptiveStack(
        isHorizontal = isHorizontal,
        spacing = 16.dp,
        horizontalAlignment = Alignment.Start,
        verticalAlignment = Alignment.Top,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalAlignment = Alignment.Start,
            modifier = sectionModifier()
                .then(tabPage)
                .semantics(mergeDescendants = true) {
                    contentDescription = if (isDominanceLoading) {
                        "Market Dominance"
                    } else {
                        "Market Dominance, ${dominance.numberAccessible(maxPlaces = 4)} percent"
                    }
                },
        ) {
            Text(
                text = "Market Dominance",
                style = satoshiBold(12),
                color = MaterialTheme.colors.onSurfaceContainerDim
            )

            if (isDominanceLoading) {
                Text(
                    text = "0.00%",
                    style = satoshiBold(16),
                    color = MaterialTheme.colors.onSurface,
                    modifier = Modifier
                        .shimmeringModifier(true)
                )
            } else {
                Text(
                    text = compressedPriceText(value = dominance),
                    style = satoshiBold(16),
                    color = MaterialTheme.colors.onSurface
                )
            }
        }

        if (!isHorizontal) {
            Separator()
        }

        StatItem(
            label = "24 Hour Volume",
            value = volume?.formatCurrency(
                threshold = FormattingThreshold.NONE,
                maxDecimals = if (volume >= 1_000_000_000) 0 else null
            ) ?: "00000000000",
            isLoading = cryptoDetailInfoModel?.market_data?.total_volume == null,
            accessibilityLabel = shortCurrencyAccessibilityLabel("24 Hour Volume", volume),
            modifier = sectionModifier(),
            tabPage = tabPage,
        )
    }
}

fun compressedPriceText(
    value: Double
): AnnotatedString {
    val absValue = abs(value)

    val locale = AppLocale.enUsPosix
    val symbols = DecimalFormatSymbols(locale)
    val decimalSeparator = symbols.decimalSeparator

    val formatter = DecimalFormat(
        "0.${"0".repeat(4)}",
        symbols
    )

    val formatted = formatter.format(absValue)

    val sepIndex = formatted.indexOf(decimalSeparator)
    if (sepIndex == -1 || absValue == 0.0) {
        return buildAnnotatedString {
            append(formatted)
            append("%")
        }
    }

    val decimals = formatted.substring(sepIndex + 1)
    val zeroCount = decimals.takeWhile { it == '0' }.length
    val significant = decimals.drop(zeroCount).take(4)

    if (zeroCount < 2) {
        val fallback = when {
            absValue < 0.02 -> String.format(locale, "%.7f", absValue)
            absValue < 1.0 -> String.format(locale, "%.4f", absValue)
            else -> String.format(locale, "%.2f", absValue)
        }

        return buildAnnotatedString {
            append(fallback)
            append("%")
        }
    }

    return buildAnnotatedString {
        append("0$decimalSeparator")
        append("0")

        withStyle(
            style = SpanStyle(
                baselineShift = BaselineShift(-0.3f),
                fontFamily = FontFamily(Font(R.font.satoshi_bold)),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        ) {
            append(zeroCount.toString())
        }

        append(significant)
        append("%")
    }
}

@Composable
private fun SupplyView(
    cryptoDetailInfoModel: CryptoDetailInfoModel?,
    tabPage: Modifier,
) {
    val isHorizontal = AccessibilityLayout.isHorizontal
    val screenWidth = contentScreenWidth()

    val progress = (cryptoDetailInfoModel?.market_data?.circulating_supply ?: 1.0) /
        (cryptoDetailInfoModel?.market_data?.max_supply ?: 1.0)

    val localDensity = LocalDensity.current
    var allTimeRangeBarWidth by remember { mutableStateOf(0.dp) }

    val circulatingSupply =
        (cryptoDetailInfoModel?.market_data?.circulating_supply ?: 0.0).formatGroupedWholeNumber()
    val maxSupplyAccessibility = if (cryptoDetailInfoModel?.market_data?.max_supply == null) {
        "Max supply: unlimited"
    } else {
        "Max Supply: ${(cryptoDetailInfoModel.market_data?.max_supply ?: 0.0).formatGroupedWholeNumber()}"
    }
    val supplyAccessibilityLabel = "Supply Circulation. Circulating Supply: $circulatingSupply. $maxSupplyAccessibility"

    Column(
        modifier = tabPage.semantics(mergeDescendants = true) {
            contentDescription = supplyAccessibilityLabel
        },
    ) {
        Text(
            text = "Supply Circulation",
            style = satoshiBold(12),
            color = MaterialTheme.colors.onSurfaceContainerDim,
            modifier = Modifier.semantics { invisibleToUser() },
        )

        Box(
            modifier = Modifier
                .padding(vertical = 12.dp)
                .hideFromTalkBackWhenEnabled()
        ) {
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colors.surfaceContainerLowest)
                    .fillMaxWidth()
                    .height(8.dp)
                    .onGloballyPositioned { coordinates ->
                        allTimeRangeBarWidth = with(localDensity) { coordinates.size.width.toDp() }
                    }
            ) {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .fillMaxHeight()
                        .widthIn(
                            min = (progress * allTimeRangeBarWidth.dpToPx()).pxToDp(),
                            max = screenWidth
                        )
                        .background(MaterialTheme.colors.primary)
                )
            }
        }

        AdaptiveStack(
            isHorizontal = isHorizontal,
            spacing = if (isHorizontal) 0.dp else 16.dp,
            horizontalAlignment = Alignment.Start,
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalAlignment = Alignment.Start,
                modifier = if (isHorizontal) Modifier else sectionModifier(),
            ) {
                Text(
                    text = circulatingSupply,
                    style = satoshiBold(16),
                    color = MaterialTheme.colors.onSurface,
                    modifier = Modifier
                        .shimmeringModifier(cryptoDetailInfoModel?.market_data?.atl == null)
                        .semantics { invisibleToUser() }
                )

                Text(
                    text = "Circulating Supply",
                    style = satoshiBold(12),
                    color = MaterialTheme.colors.onSurfaceDimmer,
                    modifier = Modifier
                        .shimmeringModifier(cryptoDetailInfoModel?.market_data?.circulating_supply == null)
                        .semantics { invisibleToUser() }
                )
            }

            if (isHorizontal) {
                Spacer(modifier = Modifier.weight(1f))
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalAlignment = if (isHorizontal) Alignment.End else Alignment.Start,
                modifier = if (isHorizontal) Modifier else sectionModifier(),
            ) {
                Text(
                    text = if (cryptoDetailInfoModel?.market_data?.max_supply == null) {
                        "∞"
                    } else {
                        (cryptoDetailInfoModel.market_data?.max_supply ?: 0.0).formatGroupedWholeNumber()
                    },
                    style = satoshiBold(16),
                    color = MaterialTheme.colors.onSurface,
                    modifier = Modifier
                        .shimmeringModifier(cryptoDetailInfoModel?.market_data?.ath == null)
                        .semantics { invisibleToUser() }
                )

                Text(
                    text = "Max Supply",
                    style = satoshiBold(12),
                    color = MaterialTheme.colors.onSurfaceDimmer,
                    modifier = Modifier
                        .shimmeringModifier(cryptoDetailInfoModel?.market_data?.max_supply == null)
                        .semantics { invisibleToUser() }
                )
            }
        }
    }
}

@Composable
private fun SupplyNoMaxView(
    cryptoDetailInfoModel: CryptoDetailInfoModel?,
    tabPage: Modifier,
) {
    val isHorizontal = AccessibilityLayout.isHorizontal
    val circulatingSupply =
        (cryptoDetailInfoModel?.market_data?.circulating_supply ?: 0.0).formatGroupedWholeNumber()

    AdaptiveStack(
        isHorizontal = isHorizontal,
        spacing = 16.dp,
        horizontalAlignment = Alignment.Start,
        verticalAlignment = Alignment.Top,
        modifier = Modifier.fillMaxWidth(),
    ) {
        StatItem(
            label = "Circulating Supply",
            value = circulatingSupply,
            isLoading = cryptoDetailInfoModel?.market_data?.circulating_supply == null,
            accessibilityLabel = "Circulating Supply: $circulatingSupply",
            modifier = sectionModifier(),
            tabPage = tabPage,
        )

        if (!isHorizontal) {
            Separator()
        }

        StatItem(
            label = "Max Supply",
            value = "∞",
            isLoading = cryptoDetailInfoModel?.market_data == null,
            accessibilityLabel = "Max supply: unlimited",
            modifier = sectionModifier(),
            tabPage = tabPage,
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
    tabPage: Modifier = Modifier,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = Alignment.Start,
        modifier = modifier
            .then(tabPage)
            .semantics(mergeDescendants = true) {
                contentDescription = accessibilityLabel ?: "$label: $value"
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
                .semantics { invisibleToUser() },
        )
    }
}

private fun shortCurrencyAccessibilityLabel(
    title: String,
    value: Double?,
): String {
    val formattedValue = value?.formatCurrency(
        threshold = FormattingThreshold.NONE,
        accessible = true,
    ) ?: ""
    return "$title, $formattedValue"
}

private fun accessibleRangePrice(value: Double?): String {
    return value?.formatCurrency(
        threshold = FormattingThreshold.NONE,
        accessible = true,
    ) ?: ""
}
