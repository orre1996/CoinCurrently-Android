package com.cryptium.coincurrently.features.CoinDetail.Views.Overview
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.invisibleToUser
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.cryptium.coincurrently.core.localization.Localization
import com.cryptium.coincurrently.core.extensions.localizedAbbreviatedDate
import com.cryptium.coincurrently.sharedui.components.AccessibilityLayout
import com.cryptium.coincurrently.sharedui.components.tabPageTalkBackTraversal
import com.cryptium.coincurrently.sharedui.components.contentScreenWidth
import com.cryptium.coincurrently.sharedui.components.AdaptiveStack
import com.cryptium.coincurrently.sharedui.components.Separator
import com.cryptium.coincurrently.sharedui.theme.onSurfaceContainerDim
import com.cryptium.coincurrently.sharedui.theme.onSurfaceDim
import com.cryptium.coincurrently.sharedui.theme.onSurfaceDimmer
import com.cryptium.coincurrently.sharedui.modifiers.shimmeringModifier
import com.cryptium.coincurrently.sharedui.theme.surfaceContainerLow
import com.cryptium.coincurrently.sharedui.theme.surfaceContainerLowest
import com.cryptium.coincurrently.core.extensions.DateFormat
import com.cryptium.coincurrently.core.extensions.convertStringToDate
import com.cryptium.coincurrently.core.extensions.daysAgo
import com.cryptium.coincurrently.core.extensions.dpToPx
import com.cryptium.coincurrently.core.extensions.formatCurrency
import com.cryptium.coincurrently.core.extensions.getSelectedCurrency
import com.cryptium.coincurrently.core.extensions.pxToDp
import com.cryptium.coincurrently.core.extensions.satoshiBold
import com.cryptium.coincurrently.sharedui.charts.hideFromTalkBackWhenEnabled
import com.cryptium.coincurrently.sharedui.components.PriceChangePercentView
import com.cryptium.coincurrently.sharedui.models.FormattingThreshold
import com.cryptium.coincurrently.sharedui.loading.ChangeSize
import com.cryptium.coincurrently.core.networking.ResponseModels.CryptoDetailInfoModel

@Composable
fun CryptoDetailHistoricDataView(
    cryptoDetailInfoModel: CryptoDetailInfoModel?,
) {
    val tabPage = Modifier.tabPageTalkBackTraversal()

    Column(
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        HeaderView(tabPage = tabPage)

        HistoricDataCard {
            ChangeView(
                cryptoDetailInfoModel = cryptoDetailInfoModel,
            )

            HistoricDataSeparator()

            AllTimeRangeView(
                cryptoDetailInfoModel = cryptoDetailInfoModel,
                tabPage = tabPage,
            )

            HistoricDataSeparator()

            AllTimeLowView(
                cryptoDetailInfoModel = cryptoDetailInfoModel,
                tabPage = tabPage,
            )

            HistoricDataSeparator()

            AllTimeHighView(
                cryptoDetailInfoModel = cryptoDetailInfoModel,
                tabPage = tabPage,
            )

            HistoricDataSeparator()

            GenesisDateView(
                cryptoDetailInfoModel = cryptoDetailInfoModel,
                tabPage = tabPage,
            )
        }
    }
}

@Composable
private fun HistoricDataCard(
    content: @Composable ColumnScope.() -> Unit,
) {
    val talkBackEnabled = AccessibilityLayout.isTalkBackEnabled

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colors.surfaceContainerLow)
    ) {
        if (!talkBackEnabled) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .pointerInput(Unit) {
                        detectTapGestures(onTap = {})
                    }
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            content = content,
        )
    }
}

@Composable
private fun HistoricDataSeparator() {
    Box(modifier = Modifier.hideFromTalkBackWhenEnabled()) {
        Separator()
    }
}

@Composable
private fun HeaderView(tabPage: Modifier) {
    Text(
        text = Localization.string("Historical Data"),
        style = satoshiBold(22),
        color = MaterialTheme.colors.onSurfaceDim,
        modifier = tabPage.semantics { heading() },
    )
}

@Composable
private fun ChangeView(
    cryptoDetailInfoModel: CryptoDetailInfoModel?,
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
                change = cryptoDetailInfoModel?.market_data?.price_change_percentage_24h,
                modifier = sectionModifier(),
            )

            if (!isHorizontal) {
                Separator()
            }

            HistoricDataChangeView(
                timeframe = "7 Day",
                change = cryptoDetailInfoModel?.market_data?.price_change_percentage_7d,
                modifier = sectionModifier(),
            )

            if (!isHorizontal) {
                Separator()
            }

            HistoricDataChangeView(
                timeframe = "14 Day",
                change = cryptoDetailInfoModel?.market_data?.price_change_percentage_14d,
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
                change = cryptoDetailInfoModel?.market_data?.price_change_percentage_30d,
                modifier = sectionModifier(),
            )

            if (!isHorizontal) {
                Separator()
            }

            HistoricDataChangeView(
                timeframe = "60 Day",
                change = cryptoDetailInfoModel?.market_data?.price_change_percentage_60d,
                modifier = sectionModifier(),
            )

            if (!isHorizontal) {
                Separator()
            }

            HistoricDataChangeView(
                timeframe = "1 Year",
                change = cryptoDetailInfoModel?.market_data?.price_change_percentage_1y,
                modifier = sectionModifier(),
            )
        }
    }
}

@Composable
private fun AllTimeRangeView(
    cryptoDetailInfoModel: CryptoDetailInfoModel?,
    tabPage: Modifier,
) {
    val isHorizontal = AccessibilityLayout.isHorizontal
    val screenWidth = contentScreenWidth()

    val progress = (cryptoDetailInfoModel?.market_data?.current_price?.getSelectedCurrency() ?: 1.0) /
        (cryptoDetailInfoModel?.market_data?.ath?.getSelectedCurrency() ?: 1.0)

    val atlDate = cryptoDetailInfoModel?.market_data?.atl_date?.getSelectedCurrency()
        ?.convertStringToDate(DateFormat.yyyyMMddTHHmmssSSSZ)
    val formattedAtlDate = atlDate?.localizedAbbreviatedDate

    val athDate = cryptoDetailInfoModel?.market_data?.ath_date?.getSelectedCurrency()
        ?.convertStringToDate(DateFormat.yyyyMMddTHHmmssSSSZ)
    val formattedAthDate = athDate?.localizedAbbreviatedDate
    val rangeAccessibilityLabel = accessibleAllTimeRangeLabel(
        atl = cryptoDetailInfoModel?.market_data?.atl?.getSelectedCurrency(),
        ath = cryptoDetailInfoModel?.market_data?.ath?.getSelectedCurrency(),
        atlDate = formattedAtlDate,
        athDate = formattedAthDate,
    )

    val localDensity = LocalDensity.current
    var allTimeRangeBarWidth by remember { mutableStateOf(0.dp) }

    Column(
        modifier = tabPage.semantics(mergeDescendants = true) {
            contentDescription = rangeAccessibilityLabel
        },
    ) {
        Text(
            text = Localization.string("All-Time Range"),
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
                    text = cryptoDetailInfoModel?.market_data?.atl?.getSelectedCurrency()
                        ?.formatCurrency(threshold = FormattingThreshold.NONE) ?: "",
                    style = satoshiBold(16),
                    color = MaterialTheme.colors.onSurface,
                    modifier = Modifier
                        .shimmeringModifier(cryptoDetailInfoModel?.market_data?.atl == null)
                        .semantics { invisibleToUser() }
                )

                Text(
                    text = formattedAtlDate ?: "0000000",
                    style = satoshiBold(12),
                    color = MaterialTheme.colors.onSurfaceDimmer,
                    modifier = Modifier
                        .shimmeringModifier(formattedAtlDate == null)
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
                    text = cryptoDetailInfoModel?.market_data?.ath?.getSelectedCurrency()
                        ?.formatCurrency(threshold = FormattingThreshold.NONE) ?: "",
                    style = satoshiBold(16),
                    color = MaterialTheme.colors.onSurface,
                    modifier = Modifier
                        .shimmeringModifier(cryptoDetailInfoModel?.market_data?.ath == null)
                        .semantics { invisibleToUser() }
                )

                Text(
                    text = formattedAthDate ?: "0000000",
                    style = satoshiBold(12),
                    color = MaterialTheme.colors.onSurfaceDimmer,
                    modifier = Modifier
                        .shimmeringModifier(formattedAthDate == null)
                        .semantics { invisibleToUser() }
                )
            }
        }
    }
}

@Composable
private fun AllTimeLowView(
    cryptoDetailInfoModel: CryptoDetailInfoModel?,
    tabPage: Modifier,
) {
    val isHorizontal = AccessibilityLayout.isHorizontal

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
                text = Localization.string("All-Time Low %"),
                style = satoshiBold(12),
                color = MaterialTheme.colors.onSurfaceContainerDim,
                modifier = Modifier.semantics { invisibleToUser() },
            )

            PriceChangePercentView(
                change = cryptoDetailInfoModel?.market_data?.atl_change_percentage?.getSelectedCurrency(),
                accessibleTimeframe = "Change since all time low:",
                size = ChangeSize.MEDIUM,
                isDownloading = cryptoDetailInfoModel?.market_data?.atl_change_percentage == null,
                tabPageTalkBack = true,
            )
        }

        if (!isHorizontal) {
            HistoricDataSeparator()
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = sectionModifier()
                .then(tabPage)
                .semantics(mergeDescendants = true) {
                    contentDescription = "${Localization.string("Days since all time low: ")}${
                        cryptoDetailInfoModel?.market_data?.atl_date?.getSelectedCurrency()
                            ?.convertStringToDate(DateFormat.yyyyMMddTHHmmssSSSZ)?.daysAgo ?: ""
                    }"
                },
        ) {
            Text(
                text = Localization.string("Since All-Time Low"),
                style = satoshiBold(12),
                color = MaterialTheme.colors.onSurfaceContainerDim,
                modifier = Modifier.semantics { invisibleToUser() },
            )

            Text(
                text = cryptoDetailInfoModel?.market_data?.atl_date?.getSelectedCurrency()
                    ?.convertStringToDate(DateFormat.yyyyMMddTHHmmssSSSZ)?.daysAgo ?: "00000000",
                style = satoshiBold(16),
                color = MaterialTheme.colors.onSurface,
                modifier = Modifier.semantics { invisibleToUser() },
            )
        }
    }
}

@Composable
private fun AllTimeHighView(
    cryptoDetailInfoModel: CryptoDetailInfoModel?,
    tabPage: Modifier,
) {
    val isHorizontal = AccessibilityLayout.isHorizontal

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
                text = Localization.string("All-Time High %"),
                style = satoshiBold(12),
                color = MaterialTheme.colors.onSurfaceContainerDim,
                modifier = Modifier.semantics { invisibleToUser() },
            )

            PriceChangePercentView(
                change = cryptoDetailInfoModel?.market_data?.ath_change_percentage?.getSelectedCurrency(),
                accessibleTimeframe = "Change since all time high: ",
                size = ChangeSize.MEDIUM,
                isDownloading = cryptoDetailInfoModel?.market_data?.ath_change_percentage == null,
                tabPageTalkBack = true,
            )
        }

        if (!isHorizontal) {
            HistoricDataSeparator()
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = sectionModifier()
                .then(tabPage)
                .semantics(mergeDescendants = true) {
                    contentDescription = "${Localization.string("Days since all time high: ")}${
                        cryptoDetailInfoModel?.market_data?.ath_date?.getSelectedCurrency()
                            ?.convertStringToDate(DateFormat.yyyyMMddTHHmmssSSSZ)?.daysAgo ?: ""
                    }"
                },
        ) {
            Text(
                text = Localization.string("Since All-Time High"),
                style = satoshiBold(12),
                color = MaterialTheme.colors.onSurfaceContainerDim,
                modifier = Modifier.semantics { invisibleToUser() },
            )

            Text(
                text = cryptoDetailInfoModel?.market_data?.ath_date?.getSelectedCurrency()
                    ?.convertStringToDate(DateFormat.yyyyMMddTHHmmssSSSZ)?.daysAgo ?: "00000000",
                style = satoshiBold(16),
                color = MaterialTheme.colors.onSurface,
                modifier = Modifier.semantics { invisibleToUser() },
            )
        }
    }
}

@Composable
private fun GenesisDateView(
    cryptoDetailInfoModel: CryptoDetailInfoModel?,
    tabPage: Modifier,
) {
    val genesisDate = cryptoDetailInfoModel?.genesis_date?.convertStringToDate(
        DateFormat.yyyyMMdd,
        gmtTimeZone = false
    )
    val formattedGenesisDate = genesisDate?.localizedAbbreviatedDate

    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = tabPage.semantics(mergeDescendants = true) {
            contentDescription = Localization.format(
                "%@, %@",
                Localization.string("Genesis Date"),
                formattedGenesisDate ?: Localization.string("Unknown"),
            )
        },
    ) {
        Text(
            text = Localization.string("Genesis Date"),
            style = satoshiBold(12),
            color = MaterialTheme.colors.onSurfaceContainerDim,
            modifier = Modifier.semantics { invisibleToUser() },
        )

        Text(
            text = formattedGenesisDate ?: Localization.string("Unknown"),
            style = satoshiBold(16),
            color = MaterialTheme.colors.onSurface,
            modifier = Modifier
                .shimmeringModifier(cryptoDetailInfoModel == null)
                .semantics { invisibleToUser() },
        )
    }
}

private fun accessibleAllTimeRangePrice(value: Double?): String {
    return value?.formatCurrency(
        threshold = FormattingThreshold.NONE,
        accessible = true,
    ) ?: ""
}

private fun accessibleAllTimeRangeLabel(
    atl: Double?,
    ath: Double?,
    atlDate: String?,
    athDate: String?,
): String {
    val atlAccessible = accessibleAllTimeRangePrice(atl)
    val athAccessible = accessibleAllTimeRangePrice(ath)
    return "${Localization.string("All-Time Range")}. ${
        Localization.format("From %@", atlAccessible)
    } ${Localization.format("on %@", atlDate ?: "")}, ${
        Localization.format("to %@", athAccessible)
    } ${Localization.format("on %@", athDate ?: "")}"
}
