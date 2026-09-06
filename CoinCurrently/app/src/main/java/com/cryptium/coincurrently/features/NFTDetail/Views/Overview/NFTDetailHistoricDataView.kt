package com.cryptium.coincurrently.features.NFTDetail.Views.Overview
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.invisibleToUser
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.cryptium.coincurrently.sharedui.components.AccessibilityLayout
import com.cryptium.coincurrently.sharedui.components.AdaptiveStack
import com.cryptium.coincurrently.sharedui.components.Separator
import com.cryptium.coincurrently.sharedui.theme.onSurfaceContainerDim
import com.cryptium.coincurrently.sharedui.theme.onSurfaceDim
import com.cryptium.coincurrently.sharedui.theme.primaryBackgroundNoRipple
import com.cryptium.coincurrently.core.extensions.DateFormat
import com.cryptium.coincurrently.core.extensions.convertStringToDate
import com.cryptium.coincurrently.core.extensions.daysAgo
import com.cryptium.coincurrently.core.extensions.satoshiBold
import com.cryptium.coincurrently.sharedui.components.PriceChangePercentView
import com.cryptium.coincurrently.sharedui.loading.ChangeSize
import com.cryptium.coincurrently.core.networking.ResponseModels.NFTDetail
import com.cryptium.coincurrently.features.CoinDetail.Views.Overview.HistoricDataChangeView

@Composable
fun NFTDetailHistoricDataView(
    nftDetail: NFTDetail?,
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
            ChangeView(
                nftDetail = nftDetail
            )

            Separator()

            AllTimeHighView(
                nftDetail = nftDetail
            )
        }
    }
}

@Composable
private fun HeaderView() {
    Text(
        text = "Historical Data",
        style = satoshiBold(22),
        color = MaterialTheme.colors.onSurfaceDim,
        modifier = Modifier.semantics { heading() },
    )
}

@Composable
private fun ChangeView(
    nftDetail: NFTDetail?
) {
    val isHorizontal = AccessibilityLayout.isHorizontal

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        AdaptiveStack(
            isHorizontal = isHorizontal,
            spacing = 16.dp,
            horizontalAlignment = Alignment.Start,
        ) {
            HistoricDataChangeView(
                timeframe = "24 Hour",
                change = nftDetail?.floor_price_24h_percentage_change?.usd,
                modifier = sectionModifier(),
            )

            if (!isHorizontal) {
                Separator()
            }

            HistoricDataChangeView(
                timeframe = "7 Day",
                change = nftDetail?.floor_price_7d_percentage_change?.usd,
                modifier = sectionModifier(),
            )

            if (!isHorizontal) {
                Separator()
            }

            HistoricDataChangeView(
                timeframe = "14 Day",
                change = nftDetail?.floor_price_14d_percentage_change?.usd,
                modifier = sectionModifier(),
            )
        }

        Separator()

        AdaptiveStack(
            isHorizontal = isHorizontal,
            spacing = 16.dp,
            horizontalAlignment = Alignment.Start,
        ) {
            HistoricDataChangeView(
                timeframe = "30 Day",
                change = nftDetail?.floor_price_30d_percentage_change?.usd,
                modifier = sectionModifier(),
            )

            if (!isHorizontal) {
                Separator()
            }

            HistoricDataChangeView(
                timeframe = "60 Day",
                change = nftDetail?.floor_price_60d_percentage_change?.usd,
                modifier = sectionModifier(),
            )

            if (!isHorizontal) {
                Separator()
            }

            HistoricDataChangeView(
                timeframe = "1 Year",
                change = nftDetail?.floor_price_1y_percentage_change?.usd,
                modifier = sectionModifier(),
            )
        }
    }
}

@Composable
private fun AllTimeHighView(
    nftDetail: NFTDetail?
) {
    val isHorizontal = AccessibilityLayout.isHorizontal
    val daysSinceAth = nftDetail?.ath_date?.usd
        ?.convertStringToDate(DateFormat.yyyyMMddTHHmmssSSSZ)
        ?.daysAgo
        ?: "00000000"

    AdaptiveStack(
        isHorizontal = isHorizontal,
        spacing = 16.dp,
        horizontalAlignment = Alignment.Start,
        verticalAlignment = Alignment.Top,
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = sectionModifier(),
        ) {
            Text(
                text = "All-Time High %",
                style = satoshiBold(12),
                color = MaterialTheme.colors.onSurfaceContainerDim,
                modifier = Modifier.semantics { invisibleToUser() },
            )

            PriceChangePercentView(
                change = nftDetail?.ath_change_percentage?.usd,
                accessibleTimeframe = "Change since all time high:",
                size = ChangeSize.MEDIUM,
                isDownloading = nftDetail == null
            )
        }

        if (!isHorizontal) {
            Separator()
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = sectionModifier()
                .semantics(mergeDescendants = true) {
                    contentDescription = "Days since all time high: $daysSinceAth"
                },
        ) {
            Text(
                text = "Since All-Time High",
                style = satoshiBold(12),
                color = MaterialTheme.colors.onSurfaceContainerDim,
                modifier = Modifier.semantics { invisibleToUser() },
            )

            Text(
                text = daysSinceAth,
                style = satoshiBold(16),
                color = MaterialTheme.colors.onSurface,
                modifier = Modifier.semantics { invisibleToUser() },
            )
        }
    }
}
