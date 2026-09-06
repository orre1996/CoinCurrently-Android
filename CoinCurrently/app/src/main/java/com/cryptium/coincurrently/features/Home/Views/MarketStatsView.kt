package com.cryptium.coincurrently.features.Home.Views
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.invisibleToUser
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cryptium.coincurrently.sharedui.components.AccessibilityLayout
import com.cryptium.coincurrently.sharedui.components.AdaptiveStack
import com.cryptium.coincurrently.sharedui.components.Separator
import com.cryptium.coincurrently.sharedui.components.SeparatorVertical
import com.cryptium.coincurrently.sharedui.theme.onSurfaceContainer
import com.cryptium.coincurrently.sharedui.theme.onSurfaceContainerDim
import com.cryptium.coincurrently.sharedui.theme.outline
import com.cryptium.coincurrently.sharedui.theme.primaryBackgroundNoRipple
import com.cryptium.coincurrently.sharedui.modifiers.shimmeringModifier
import com.cryptium.coincurrently.sharedui.theme.surfaceContainer
import com.cryptium.coincurrently.sharedui.theme.surfaceContainerHighest
import com.cryptium.coincurrently.sharedui.charts.getFormattedCurrencyString
import com.cryptium.coincurrently.core.extensions.changePercentDecimalPoints
import com.cryptium.coincurrently.core.extensions.directionAccessible
import com.cryptium.coincurrently.core.extensions.formatCurrency
import com.cryptium.coincurrently.core.extensions.getSelectedCurrency
import com.cryptium.coincurrently.core.extensions.numberAccessible
import com.cryptium.coincurrently.core.extensions.satoshiBold
import com.cryptium.coincurrently.core.extensions.satoshiMedium
import com.cryptium.coincurrently.sharedui.components.PriceChangePercentView
import com.cryptium.coincurrently.sharedui.loading.ChangeSize
import com.cryptium.coincurrently.core.networking.ResponseModels.FearGreed
import com.cryptium.coincurrently.core.networking.ResponseModels.GasFeesModel
import com.cryptium.coincurrently.core.networking.ResponseModels.GlobalMarketData

@Composable
fun MarketStatsView(
    globalMarket: GlobalMarketData?,
    gasFees: GasFeesModel?,
    fearGreed: FearGreed?,
    navigateToGlobalMarket: () -> Unit,
    navigateToGasFees: () -> Unit,
    navigateToFearGreed: () -> Unit
) {
    val isHorizontal = AccessibilityLayout.isHorizontal

    AdaptiveStack(
        isHorizontal = isHorizontal,
        spacing = if (isHorizontal) 0.dp else 0.dp,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalAlignment = Alignment.Top,
        modifier = Modifier
            .padding(
                top = 16.dp,
                start = 16.dp,
                end = 16.dp,
                bottom = 16.dp
            )
            .primaryBackgroundNoRipple(
                paddingValues = PaddingValues(0.dp),
            )
            .then(
                if (isHorizontal) {
                    Modifier.height(intrinsicSize = IntrinsicSize.Min)
                } else {
                    Modifier
                }
            )
    ) {
        MarketCapView(
            globalMarket,
            navigateToGlobalMarket = navigateToGlobalMarket,
            modifier = sectionModifier()
        )

        if (isHorizontal) {
            SeparatorVertical(padding = PaddingValues(vertical = 12.dp))
        } else {
            Separator(padding = PaddingValues(horizontal = 16.dp))
        }

        GasFeesView(
            gasFees,
            navigateToGasFees = navigateToGasFees,
            modifier = sectionModifier()
        )

        if (isHorizontal) {
            SeparatorVertical(padding = PaddingValues(vertical = 12.dp))
        } else {
            Separator(padding = PaddingValues(horizontal = 16.dp))
        }

        FearGreedView(
            fearGreed,
            navigateToFearGreed = navigateToFearGreed,
            modifier = sectionModifier()
        )
    }
}

@Composable
private fun MarketCapView(
    globalMarket: GlobalMarketData?,
    navigateToGlobalMarket: () -> Unit,
    modifier: Modifier
) {
    val marketCap = globalMarket?.total_market_cap?.getSelectedCurrency()
    val marketCapAccessible = marketCap?.formatCurrency(accessible = true)
    val change = globalMarket?.market_cap_change_percentage_24h_usd
    val changeDirection = change?.directionAccessible ?: ""
    val accessibleChange = change?.numberAccessible(maxPlaces = change?.changePercentDecimalPoints ?: 2) ?: ""

    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier
            .semantics(mergeDescendants = true) {
                contentDescription =
                    "The global market cap is $marketCapAccessible, - 24 hour price change: $changeDirection $accessibleChange percent"
                onClick(label = "view more global market stats", action = {
                    navigateToGlobalMarket.invoke()
                    true
                })
            }
            .clickable {
                navigateToGlobalMarket.invoke()
            }
            .padding(16.dp)
    ) {
        Text(
            text = "Market Cap",
            style = satoshiMedium(14),
            color = MaterialTheme.colors.onSurfaceContainerDim,
            modifier = Modifier.semantics { invisibleToUser() },
        )

        Text(
            text = marketCap?.formatCurrency() ?: "00000",
            style = satoshiBold(24),
            color = MaterialTheme.colors.onSurfaceContainer,
            modifier = Modifier
                .shimmeringModifier(globalMarket == null)
                .semantics { invisibleToUser() },
        )

        PriceChangePercentView(
            change = change,
            size = ChangeSize.SMALL,
            isDownloading = globalMarket == null,
            modifier = Modifier.semantics { invisibleToUser() },
        )
    }
}

@Composable
private fun GasFeesView(
    gasFees: GasFeesModel?,
    navigateToGasFees: () -> Unit,
    modifier: Modifier
) {
    val gasFee = gasFees?.gasFeeData?.firstOrNull()?.proposeGasPrice?.toDoubleOrNull() ?: 0.0
    val accessibleGasFee = gasFee.numberAccessible(maxPlaces = 2)

    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier
            .semantics(mergeDescendants = true) {
                contentDescription =
                    "The current average ethereum gas fee is $accessibleGasFee Gwei."
                onClick(label = "view more gas fee stats", action = {
                    navigateToGasFees.invoke()
                    true
                })
            }
            .clickable {
                navigateToGasFees.invoke()
            }
            .padding(16.dp)
    ) {
        Text(
            text = "ETH Gas Fee",
            style = satoshiMedium(14),
            color = MaterialTheme.colors.onSurfaceContainerDim,
            modifier = Modifier.semantics { invisibleToUser() },
        )

        Text(
            text = gasFee.getFormattedCurrencyString(false, minimumDigits = 2, maximumDigits = 2) ?: "0000000",
            style = satoshiBold(24),
            color = MaterialTheme.colors.onSurfaceContainer,
            modifier = Modifier
                .shimmeringModifier(gasFees == null)
                .semantics { invisibleToUser() },
        )

        Text(
            text = "Gwei",
            style = satoshiBold(12),
            color = MaterialTheme.colors.onSurfaceContainerDim,
            modifier = Modifier.semantics { invisibleToUser() },
        )
    }
}

@Composable
private fun FearGreedView(
    fearGreed: FearGreed?,
    navigateToFearGreed: () -> Unit,
    modifier: Modifier
) {
    val firstValue = fearGreed?.data?.firstOrNull()?.value
    val classification = fearGreed?.data?.firstOrNull()?.value_classification ?: ""

    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier
            .semantics(mergeDescendants = true) {
                contentDescription =
                    "The current fear and greed index is ${firstValue ?: ""}. $classification"
                onClick(label = "view more fear and greed stats", action = {
                    navigateToFearGreed.invoke()
                    true
                })
            }
            .clickable {
                navigateToFearGreed.invoke()
            }
            .padding(16.dp)
    ) {
        Text(
            text = "Fear & Greed",
            style = satoshiMedium(14),
            color = MaterialTheme.colors.onSurfaceContainerDim,
            modifier = Modifier.semantics { invisibleToUser() },
        )

        Text(
            text = firstValue ?: "00",
            style = satoshiBold(24),
            color = MaterialTheme.colors.onSurfaceContainer,
            modifier = Modifier
                .shimmeringModifier(fearGreed == null)
                .semantics { invisibleToUser() },
        )

        FearGreedMeter(
            currentValue = firstValue,
            modifier = Modifier.semantics { invisibleToUser() },
        )
    }
}

@Composable
private fun FearGreedMeter(
    currentValue: String?,
    modifier: Modifier = Modifier,
) {
    val localDensity = LocalDensity.current
    val circleSize: Dp = 12.dp
    val percent = ((currentValue ?: "0").toDouble() / 100).toFloat()
    var fearMeterWidth by remember { mutableStateOf(0.dp) }

    Box(
        contentAlignment = Alignment.CenterStart,
        modifier = modifier
            .padding(top = 4.dp)
            .onGloballyPositioned { coordinates ->
                fearMeterWidth = with(localDensity) { coordinates.size.width.toDp() }
            }
    ) {
        FearGreedMeter(height = 8.dp)

        Box(
            modifier = Modifier
                .offset(x = (fearMeterWidth.times(percent)) - circleSize / 2)
                .size(circleSize)
                .clip(CircleShape)
                .background(MaterialTheme.colors.outline)
                .padding(2.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colors.surfaceContainerHighest)
        )
    }
}