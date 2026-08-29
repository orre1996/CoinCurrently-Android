package com.cryptium.coincurrently.ui.main.MarketTab.Derivatives.Views

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.height
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.invisibleToUser
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.verticalScroll
import com.cryptium.coincurrently.ui.main.Compose.AccessibilityLayout
import com.cryptium.coincurrently.ui.main.Compose.AdaptiveStack
import com.cryptium.coincurrently.ui.main.Compose.CachedUrlImage
import com.cryptium.coincurrently.R
import com.cryptium.coincurrently.ui.main.Compose.CloseButton
import com.cryptium.coincurrently.ui.main.Compose.DragHandler
import com.cryptium.coincurrently.ui.main.Compose.SecondaryButton
import com.cryptium.coincurrently.ui.main.Compose.Separator
import com.cryptium.coincurrently.ui.main.Compose.onSurfaceDim
import com.cryptium.coincurrently.ui.main.Compose.outlineVariant

import com.cryptium.coincurrently.ui.main.Compose.surfaceContainerLow
import com.cryptium.coincurrently.ui.main.Extensions.currencyAccessible
import com.cryptium.coincurrently.ui.main.Extensions.formatCurrency
import com.cryptium.coincurrently.ui.main.Extensions.formattedRemovingTrailingZeros
import com.cryptium.coincurrently.ui.main.Extensions.numberAccessible
import com.cryptium.coincurrently.ui.main.Extensions.satoshiBold
import com.cryptium.coincurrently.ui.main.Extensions.symbolAccessible
import com.cryptium.coincurrently.ui.main.Extensions.timeAgoDisplay
import com.cryptium.coincurrently.ui.main.Extensions.timeUntilExpired
import com.cryptium.coincurrently.ui.main.General.Components.NewPriceChangePercentView
import com.cryptium.coincurrently.ui.main.General.Enums.FormattingThreshold
import com.cryptium.coincurrently.ui.main.General.GlobalFunctions
import com.cryptium.coincurrently.ui.main.General.Loading.ChangeSize
import com.cryptium.coincurrently.ui.main.analytics.AnalyticsManager
import com.cryptium.coincurrently.ui.main.analytics.ScreenLabel
import com.cryptium.coincurrently.ui.main.analytics.WebViewOpened
import kotlinx.coroutines.launch
import network.chaintech.kmp_date_time_picker.utils.capitalize
import networking.ResponseModels.CurrencySelectionModel
import com.cryptium.coincurrently.ui.main.Compose.ModalBottomSheetSystemNavigationBar
import com.cryptium.coincurrently.ui.main.Compose.rememberDragHandlerHeight
import com.cryptium.coincurrently.ui.main.Compose.rememberModalSheetMaxBodyHeight
import com.cryptium.coincurrently.ui.main.Compose.ModalSheetBody
import com.cryptium.coincurrently.ui.main.Compose.ModalSheetColumnLayout
import networking.ResponseModels.DerivativeDetailModel

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun DerivativeDetailSheet(
    derivativeDetail: DerivativeDetailModel,
    multiplier: Double?,
    imageUrl: String?,
    hasToplist: Boolean,
    exchangeName: String,
    close: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val maxBodyHeight = rememberModalSheetMaxBodyHeight(rememberDragHandlerHeight(topPadding = 16.dp, bottomPadding = 12.dp))

    ModalBottomSheet(
        onDismissRequest = {
            coroutineScope.launch {
                close.invoke()
            }
        },
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        containerColor = MaterialTheme.colors.surfaceContainerLow,
        tonalElevation = 16.dp,
        contentWindowInsets = { WindowInsets(0,0 ,0 ,0) },
        dragHandle = {
            DragHandler(
                modifier = Modifier
                    .padding(
                        top = 16.dp,
                        bottom = 12.dp
                    )
                    .clearAndSetSemantics {
                        invisibleToUser()
                    }
            )
        }
    ) {
        ModalBottomSheetSystemNavigationBar()
        ModalSheetBody(
            context = context,
            maxBodyHeight = maxBodyHeight,
        ) {
            val isHorizontal = AccessibilityLayout.isHorizontal
            val isFuture = derivativeDetail.contract_type == "Future"

            ModalSheetColumnLayout(
                maxBodyHeight = maxBodyHeight,
                header = {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                        ) {
                            HeaderView(
                                symbol = derivativeDetail.symbol,
                                imageUrl = imageUrl,
                                hasToplist = hasToplist,
                            )

                            Spacer(modifier = Modifier.weight(1f))

                            CloseButton(
                                onClick = {
                                    coroutineScope.launch {
                                        sheetState.hide()
                                        close.invoke()
                                    }
                                }
                            )
                        }

                        SheetSeparator(
                            padding = PaddingValues(
                                top = 24.dp,
                                start = 16.dp,
                                end = 16.dp
                            )
                        )
                    }
                },
                footer = {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        SheetSeparator(
                            padding = PaddingValues(
                                horizontal = 16.dp
                            )
                        )

                        ActionButtons(
                            tradeUrl = derivativeDetail.trade_url
                        )
                    }
                },
                scrollContent = { scrollModifier ->
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = scrollModifier
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp),
                    ) {
                Spacer(modifier = Modifier.height(if(isHorizontal) 8.dp else 0.dp))

                StatPairRow(
                    isHorizontal = isHorizontal,
                    first = { modifier ->
                        PriceView(
                            priceUsd = derivativeDetail.converted_last?.usd?.toDoubleOrNull(),
                            multiplier = multiplier,
                            modifier = modifier,
                        )
                    },
                    second = { modifier ->
                        ChangeView(
                            change = derivativeDetail.h24_percentage_change,
                            modifier = modifier,
                        )
                    },
                )

                StatPairRow(
                    isHorizontal = isHorizontal,
                    first = { modifier ->
                        OpenInterestView(
                            openInterest = derivativeDetail.open_interest_usd,
                            multiplier = multiplier,
                            modifier = modifier,
                        )
                    },
                    second = { modifier ->
                        VolumeView(
                            volume = derivativeDetail.converted_volume?.usd?.toDoubleOrNull(),
                            multiplier = multiplier,
                            modifier = modifier,
                        )
                    },
                )

                StatPairRow(
                    isHorizontal = isHorizontal,
                    first = { modifier ->
                        FundingRateView(
                            fundingRate = derivativeDetail.funding_rate,
                            modifier = modifier,
                        )
                    },
                    second = { modifier ->
                        IndexPriceView(
                            index = derivativeDetail.index,
                            target = derivativeDetail.target,
                            modifier = modifier,
                        )
                    },
                )

                StatPairRow(
                    isHorizontal = isHorizontal,
                    first = { modifier ->
                        IndexBasisView(
                            indexBasisPercentage = derivativeDetail.index_basis_percentage,
                            modifier = modifier,
                        )
                    },
                    second = { modifier ->
                        BidSpreadView(
                            bidSpread = derivativeDetail.bid_ask_spread,
                            modifier = modifier,
                        )
                    },
                )

                StatPairRow(
                    isHorizontal = isHorizontal,
                    first = { modifier ->
                        ContractTypeView(
                            contractType = derivativeDetail.contract_type?.capitalize(),
                            modifier = modifier,
                        )
                    },
                    second = { modifier ->
                        ExchangeView(
                            exchange = exchangeName,
                            modifier = modifier,
                        )
                    },
                )

                StatPairRowOptionalSecond(
                    isHorizontal = isHorizontal,
                    showSecond = isFuture,
                    first = { modifier ->
                        LastTradedView(
                            lastTraded = derivativeDetail.last_traded,
                            modifier = modifier,
                        )
                    },
                    second = { modifier ->
                        ExpiryView(
                            expiredAt = derivativeDetail.expired_at,
                            modifier = modifier,
                        )
                    },
                )

                Spacer(modifier = Modifier.height(if(isHorizontal) 8.dp else 0.dp))
                    }
                },
            )
        }
    }
}

@Composable
private fun StatPairRow(
    isHorizontal: Boolean,
    first: @Composable (Modifier) -> Unit,
    second: @Composable (Modifier) -> Unit,
) {
    AdaptiveStack(
        isHorizontal = isHorizontal,
        spacing = 16.dp,
        horizontalAlignment = Alignment.Start,
        verticalAlignment = Alignment.Top,
        modifier = Modifier.fillMaxWidth(),
    ) {
        first(sectionModifier())

        if (!isHorizontal) {
            SheetSeparator()
        }

        second(sectionModifier())
    }

    if (!isHorizontal) {
        SheetSeparator()
    }
}

@Composable
private fun ColumnScope.StatPairRowOptionalSecond(
    isHorizontal: Boolean,
    showSecond: Boolean,
    first: @Composable (Modifier) -> Unit,
    second: @Composable (Modifier) -> Unit,
) {
    AdaptiveStack(
        isHorizontal = isHorizontal,
        spacing = if (isHorizontal) 16.dp else 8.dp,
        horizontalAlignment = Alignment.Start,
        verticalAlignment = Alignment.Top,
        modifier = Modifier.fillMaxWidth(),
    ) {
        first(sectionModifier())

        if (!isHorizontal && showSecond) {
            SheetSeparator()
        }

        if (showSecond) {
            second(sectionModifier())
        }
    }
}

@Composable
private fun SheetSeparator(
    padding: PaddingValues = PaddingValues(0.dp),
) {
    Box(
        modifier = Modifier.clearAndSetSemantics {
            invisibleToUser()
        }
    ) {
        Separator(padding = padding)
    }
}

@Composable
private fun StatView(
    title: String,
    titleAccessible: String? = null,
    value: String,
    valueAccessible: String? = null,
    modifier: Modifier = Modifier,
) {
    val accessibleTitle = titleAccessible ?: title
    val accessibilityLabel = valueAccessible?.let { "$accessibleTitle: $it" }

    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier.then(
            if (accessibilityLabel != null) {
                Modifier.semantics(mergeDescendants = true) {
                    contentDescription = accessibilityLabel
                }
            } else {
                Modifier.semantics(mergeDescendants = true) {}
            }
        )
    ) {
        Text(
            text = title,
            style = satoshiBold(12),
            color = MaterialTheme.colors.onSurfaceDim,
            modifier = if (valueAccessible != null) {
                Modifier.semantics { invisibleToUser() }
            } else {
                Modifier
            }
        )

        Text(
            text = value,
            style = satoshiBold(16),
            color = MaterialTheme.colors.onSurface,
            modifier = if (valueAccessible != null) {
                Modifier.semantics { invisibleToUser() }
            } else {
                Modifier
            }
        )
    }
}

@Composable
private fun HeaderView(
    symbol: String?,
    imageUrl: String?,
    hasToplist: Boolean,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (hasToplist && imageUrl == null) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colors.outlineVariant,
                        shape = CircleShape
                    )
                    .semantics { invisibleToUser() }
            )
        } else {
            CachedUrlImage(
                model = imageUrl,
                size = 24.dp,
                modifier = Modifier.semantics { invisibleToUser() },
            )
        }

        Text(
            text = symbol ?: "",
            style = satoshiBold(20),
            color = MaterialTheme.colors.onSurfaceDim,
            modifier = Modifier.semantics(mergeDescendants = true) {
                heading()
                contentDescription = symbol?.symbolAccessible ?: ""
            }
        )
    }
}

@Composable
private fun PriceView(
    priceUsd: Double?,
    multiplier: Double?,
    modifier: Modifier
) {
    val lastPrice = (priceUsd ?: 0.0) * (multiplier ?: 0.0)

    StatView(
        title = "Price",
        value = lastPrice.formatCurrency(threshold = FormattingThreshold.NONE),
        valueAccessible = lastPrice.currencyAccessible(),
        modifier = modifier,
    )
}

@Composable
private fun ChangeView(
    change: Double?,
    modifier: Modifier
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier.semantics(mergeDescendants = true) {}
    ) {
        Text(
            text = "24h Price Change",
            style = satoshiBold(12),
            color = MaterialTheme.colors.onSurfaceDim,
            modifier = Modifier.semantics { invisibleToUser() }
        )

        NewPriceChangePercentView(
            change = change,
            size = ChangeSize.MEDIUM,
            accessibleTimeframe = "24 hour",
            isDownloading = false
        )
    }
}

@Composable
private fun OpenInterestView(
    openInterest: Double?,
    multiplier: Double?,
    modifier: Modifier
) {
    val interest = (openInterest ?: 0.0) * (multiplier ?: 0.0)

    StatView(
        title = "Open Interest",
        value = interest.formatCurrency(),
        valueAccessible = interest.formatCurrency(accessible = true),
        modifier = modifier,
    )
}

@Composable
private fun VolumeView(
    volume: Double?,
    multiplier: Double?,
    modifier: Modifier
) {
    val convertedVolume = (volume ?: 0.0) * (multiplier ?: 0.0)

    StatView(
        title = "24h Volume",
        titleAccessible = "24 hour volume",
        value = convertedVolume.formatCurrency(),
        valueAccessible = convertedVolume.formatCurrency(accessible = true),
        modifier = modifier,
    )
}

@Composable
private fun FundingRateView(
    fundingRate: Double?,
    modifier: Modifier
) {
    val rate = fundingRate ?: 0.0
    val formattedRate = rate.let {
        java.text.DecimalFormat("0.00").apply {
            minimumFractionDigits = 2
            maximumFractionDigits = 7
        }.format(it)
    }

    StatView(
        title = "Funding Rate",
        value = "$formattedRate%",
        valueAccessible = "${rate.numberAccessible(maxPlaces = 7)} percent",
        modifier = modifier,
    )
}

private fun indexPriceCurrency(target: String?): CurrencySelectionModel {
    val symbol = target?.trim()?.uppercase().orEmpty()
    if (symbol.isEmpty()) {
        return CurrencySelectionModel(
            currency = "",
            code = "",
            symbol = "",
            suffix = true,
            readAccessibilityAsSymbol = true,
        )
    }

    val selectedCurrency = GlobalFunctions.getSelectedCurrency()
    val spelledSymbol = symbol.toCharArray().joinToString(" ") { it.toString() }
    return CurrencySelectionModel(
        currency = spelledSymbol,
        code = symbol,
        symbol = symbol,
        language = selectedCurrency.language,
        country = selectedCurrency.country,
        suffix = true,
        readAccessibilityAsSymbol = true,
    )
}

@Composable
private fun IndexPriceView(
    index: Double?,
    target: String?,
    modifier: Modifier
) {
    val currency = indexPriceCurrency(target)
    val indexValue = index ?: 0.0

    StatView(
        title = "Index Price",
        value = indexValue.formatCurrency(
            threshold = FormattingThreshold.NONE,
            currency = currency
        ),
        valueAccessible = indexValue.currencyAccessible(currency = currency),
        modifier = modifier,
    )
}

@Composable
private fun IndexBasisView(
    indexBasisPercentage: Double?,
    modifier: Modifier
) {
    val basis = indexBasisPercentage ?: 0.0
    val formattedBasis = indexBasisPercentage?.formattedRemovingTrailingZeros() ?: "0"

    StatView(
        title = "Index Basis %",
        titleAccessible = "Index basis percent",
        value = "$formattedBasis%",
        valueAccessible = "${basis.numberAccessible()} percent",
        modifier = modifier,
    )
}

@Composable
private fun BidSpreadView(
    bidSpread: Double?,
    modifier: Modifier
) {
    val spread = bidSpread
    val formattedSpread = spread?.let { rate ->
        java.text.DecimalFormat("0.00").apply {
            minimumFractionDigits = 2
            maximumFractionDigits = 7
        }.format(rate)
    } ?: "-"

    StatView(
        title = "Bid Ask Spread",
        value = "$formattedSpread%",
        valueAccessible = spread?.let { "${it.numberAccessible(maxPlaces = 7)} percent" } ?: "Not available",
        modifier = modifier,
    )
}

@Composable
private fun LastTradedView(
    lastTraded: Double?,
    modifier: Modifier
) {
    val lastTradedText = lastTraded?.timeAgoDisplay ?: "-"

    StatView(
        title = "Last Traded",
        value = lastTradedText,
        valueAccessible = if (lastTradedText == "-") "Not available" else lastTradedText,
        modifier = modifier,
    )
}


@Composable
private fun ContractTypeView(
    contractType: String?,
    modifier: Modifier
) {
    StatView(
        title = "Contract Type",
        value = contractType ?: "",
        modifier = modifier,
    )
}

@Composable
private fun ExchangeView(
    exchange: String,
    modifier: Modifier
) {
    StatView(
        title = "Exchange",
        value = exchange,
        modifier = modifier,
    )
}

@Composable
private fun ExpiryView(
    expiredAt: Double?,
    modifier: Modifier
) {
    val expiresIn = expiredAt?.timeUntilExpired ?: "-"
    val accessibleExpiry = when (expiresIn) {
        "Expired" -> "expired"
        "-" -> "Not available"
        else -> "expires in $expiresIn"
    }

    StatView(
        title = "Expires",
        value = expiresIn,
        valueAccessible = accessibleExpiry,
        modifier = modifier,
    )
}



@Composable
private fun ActionButtons(
    tradeUrl: String?
) {
    val context = LocalContext.current

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .padding(
                top = 24.dp,
                bottom = 24.dp,
                start = 16.dp,
                end = 16.dp,
            )
    ) {
        SecondaryButton(
            text = "View Exchange",
            accessibilityLabel = "View exchange",
            icon = R.drawable.external,
            onClick = {
                tradeUrl?.let { urlString ->
                    AnalyticsManager.log(
                        WebViewOpened(fromScreen = ScreenLabel.DerivativeDetail, url = urlString),
                    )
                    context.startActivity(
                        Intent(Intent.ACTION_VIEW, Uri.parse(urlString))
                    )
                }
            },
            modifier = Modifier
                .weight(1f)
        )
    }
}
