package com.cryptium.coincurrently.ui.main.MarketTab.CryptoDetail.Components.Compare

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import com.cryptium.coincurrently.ui.main.Compose.AccessibilityLayout
import com.cryptium.coincurrently.ui.main.Compose.AdaptiveStack
import com.cryptium.coincurrently.ui.main.Compose.Separator
import com.cryptium.coincurrently.ui.main.Compose.blue50
import com.cryptium.coincurrently.ui.main.Compose.onSurfaceContainerDim
import com.cryptium.coincurrently.ui.main.Compose.onSurfaceDim
import com.cryptium.coincurrently.ui.main.Compose.onSurfaceDimmer
import com.cryptium.coincurrently.ui.main.Compose.primary50
import com.cryptium.coincurrently.ui.main.Compose.primaryBackgroundNoRipple
import com.cryptium.coincurrently.ui.main.Compose.shimmeringModifier
import com.cryptium.coincurrently.ui.main.Extensions.DateFormat
import com.cryptium.coincurrently.ui.main.Extensions.convertStringToDate
import com.cryptium.coincurrently.ui.main.Extensions.daysAgo
import com.cryptium.coincurrently.ui.main.Extensions.formatCurrency
import com.cryptium.coincurrently.ui.main.Extensions.formatDate
import com.cryptium.coincurrently.ui.main.Extensions.getSelectedCurrency
import com.cryptium.coincurrently.ui.main.Extensions.satoshiBold
import com.cryptium.coincurrently.ui.main.General.Components.CustomProgressBarView
import com.cryptium.coincurrently.ui.main.General.Components.Chart.hideFromTalkBackWhenEnabled
import com.cryptium.coincurrently.ui.main.General.Enums.FormattingThreshold
import networking.ResponseModels.CryptoDetailInfoModel

@Composable
fun CompareHistoricDataView(
    mainCryptoInfo: CryptoDetailInfoModel?,
    compareCryptoInfo: CryptoDetailInfoModel?,
    modifier: Modifier = Modifier,
) {
    Column(
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
            .padding(
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
            AllTimeRangeView(
                cryptoInfo = mainCryptoInfo,
                progressColor = MaterialTheme.colors.primary50,
                isDownloading = mainCryptoInfo?.symbol == null,
            )

            Separator()

            AllTimeRangeView(
                cryptoInfo = compareCryptoInfo,
                progressColor = MaterialTheme.colors.blue50,
                isDownloading = compareCryptoInfo?.symbol == null,
            )

            Separator()

            AllTimeHighView(
                mainCryptoInfo = mainCryptoInfo,
                compareCryptoInfo = compareCryptoInfo
            )

            Separator()

            AllTimeLowView(
                mainCryptoInfo = mainCryptoInfo,
                compareCryptoInfo = compareCryptoInfo
            )

            Separator()

            GenesisDateView(
                mainCryptoInfo = mainCryptoInfo,
                compareCryptoInfo = compareCryptoInfo
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
private fun AllTimeRangeView(
    cryptoInfo: CryptoDetailInfoModel?,
    progressColor: Color,
    isDownloading: Boolean,
) {
    val isHorizontal = AccessibilityLayout.isHorizontal
    val childModifier = Modifier.semantics { invisibleToUser() }
    val progress = (cryptoInfo?.market_data?.current_price?.getSelectedCurrency() ?: 1.0) /
        (cryptoInfo?.market_data?.ath?.getSelectedCurrency() ?: 0.0)

    val atlDate = cryptoInfo?.market_data?.atl_date?.getSelectedCurrency()
        ?.convertStringToDate(DateFormat.yyyyMMddTHHmmssSSSZ)
    val formattedAtlDate = atlDate?.formatDate(style = DateFormat.MMMddyyyy)
    val atlPrice = cryptoInfo?.market_data?.atl?.getSelectedCurrency()

    val athDate = cryptoInfo?.market_data?.ath_date?.getSelectedCurrency()
        ?.convertStringToDate(DateFormat.yyyyMMddTHHmmssSSSZ)
    val formattedAthDate = athDate?.formatDate(style = DateFormat.MMMddyyyy)
    val athPrice = cryptoInfo?.market_data?.ath?.getSelectedCurrency()

    val atlAccessible = atlPrice?.formatCurrency(
        threshold = FormattingThreshold.NONE,
        accessible = true,
    ) ?: ""
    val athAccessible = athPrice?.formatCurrency(
        threshold = FormattingThreshold.NONE,
        accessible = true,
    ) ?: ""
    val rangeAccessibilityLabel =
        "${accessibleAllTimeRangeLabel(cryptoInfo?.name)}. From $atlAccessible on ${formattedAtlDate ?: ""}, to $athAccessible on ${formattedAthDate ?: ""}"

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.semantics(mergeDescendants = true) {
            contentDescription = rangeAccessibilityLabel
        },
    ) {
        Text(
            text = "All-Time Range (${cryptoInfo?.symbol?.uppercase() ?: ""})",
            style = satoshiBold(12),
            color = MaterialTheme.colors.onSurfaceContainerDim,
            modifier = Modifier
                .shimmeringModifier(isDownloading)
                .semantics { invisibleToUser() },
        )

        Box(
            modifier = Modifier
                .padding(vertical = 12.dp)
                .hideFromTalkBackWhenEnabled()
        ) {
            CustomProgressBarView(
                progress = progress,
                foregroundColor = progressColor,
                downloading = cryptoInfo == null,
            )
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
                    text = atlPrice?.formatCurrency(threshold = FormattingThreshold.NONE) ?: "",
                    style = satoshiBold(16),
                    color = MaterialTheme.colors.onSurface,
                    modifier = Modifier
                        .shimmeringModifier(cryptoInfo?.market_data?.atl == null)
                        .then(childModifier)
                )

                Text(
                    text = formattedAtlDate ?: "0000000",
                    style = satoshiBold(12),
                    color = MaterialTheme.colors.onSurfaceDimmer,
                    modifier = Modifier
                        .shimmeringModifier(formattedAtlDate == null)
                        .then(childModifier)
                )
            }

            if (isHorizontal) {
                Spacer(
                    modifier = Modifier
                        .weight(1f)
                        .hideFromTalkBackWhenEnabled()
                )
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalAlignment = if (isHorizontal) Alignment.End else Alignment.Start,
                modifier = if (isHorizontal) Modifier else sectionModifier(),
            ) {
                Text(
                    text = athPrice?.formatCurrency(threshold = FormattingThreshold.NONE) ?: "",
                    style = satoshiBold(16),
                    color = MaterialTheme.colors.onSurface,
                    modifier = Modifier
                        .shimmeringModifier(cryptoInfo?.market_data?.ath == null)
                        .then(childModifier)
                )

                Text(
                    text = formattedAthDate ?: "0000000",
                    style = satoshiBold(12),
                    color = MaterialTheme.colors.onSurfaceDimmer,
                    modifier = Modifier
                        .shimmeringModifier(formattedAthDate == null)
                        .then(childModifier)
                )
            }
        }
    }
}

@Composable
private fun AllTimeHighView(
    mainCryptoInfo: CryptoDetailInfoModel?,
    compareCryptoInfo: CryptoDetailInfoModel?,
) {
    CompareDualValueSection(
        title = "Since All-Time High",
        mainAccessibilityLabel = accessibleDaysAgoLabel(
            name = mainCryptoInfo?.name,
            daysAgo = mainCryptoInfo?.market_data?.ath_date?.getSelectedCurrency()
                ?.convertStringToDate(DateFormat.yyyyMMddTHHmmssSSSZ)?.daysAgo,
        ),
        compareAccessibilityLabel = accessibleDaysAgoLabel(
            name = compareCryptoInfo?.name,
            daysAgo = compareCryptoInfo?.market_data?.ath_date?.getSelectedCurrency()
                ?.convertStringToDate(DateFormat.yyyyMMddTHHmmssSSSZ)?.daysAgo,
        ),
        mainValue = mainCryptoInfo?.market_data?.ath_date?.getSelectedCurrency()
            ?.convertStringToDate(DateFormat.yyyyMMddTHHmmssSSSZ)?.daysAgo ?: "Unknown",
        compareValue = compareCryptoInfo?.market_data?.ath_date?.getSelectedCurrency()
            ?.convertStringToDate(DateFormat.yyyyMMddTHHmmssSSSZ)?.daysAgo ?: "Unknown",
        isMainLoading = mainCryptoInfo?.market_data?.ath_date == null,
        isCompareLoading = compareCryptoInfo?.market_data?.ath_date == null,
    )
}

@Composable
private fun AllTimeLowView(
    mainCryptoInfo: CryptoDetailInfoModel?,
    compareCryptoInfo: CryptoDetailInfoModel?,
) {
    CompareDualValueSection(
        title = "Since All-Time Low",
        mainAccessibilityLabel = accessibleDaysAgoLabel(
            name = mainCryptoInfo?.name,
            daysAgo = mainCryptoInfo?.market_data?.atl_date?.getSelectedCurrency()
                ?.convertStringToDate(DateFormat.yyyyMMddTHHmmssSSSZ)?.daysAgo,
        ),
        compareAccessibilityLabel = accessibleDaysAgoLabel(
            name = compareCryptoInfo?.name,
            daysAgo = compareCryptoInfo?.market_data?.atl_date?.getSelectedCurrency()
                ?.convertStringToDate(DateFormat.yyyyMMddTHHmmssSSSZ)?.daysAgo,
        ),
        mainValue = mainCryptoInfo?.market_data?.atl_date?.getSelectedCurrency()
            ?.convertStringToDate(DateFormat.yyyyMMddTHHmmssSSSZ)?.daysAgo ?: "Unknown",
        compareValue = compareCryptoInfo?.market_data?.atl_date?.getSelectedCurrency()
            ?.convertStringToDate(DateFormat.yyyyMMddTHHmmssSSSZ)?.daysAgo ?: "Unknown",
        isMainLoading = mainCryptoInfo?.market_data?.atl_date == null,
        isCompareLoading = compareCryptoInfo?.market_data?.atl_date == null,
    )
}

@Composable
private fun GenesisDateView(
    mainCryptoInfo: CryptoDetailInfoModel?,
    compareCryptoInfo: CryptoDetailInfoModel?,
) {
    val mainGenesisDate = mainCryptoInfo?.genesis_date?.convertStringToDate(DateFormat.yyyyMMdd, gmtTimeZone = false)
    val mainFormattedGenesisDate = mainGenesisDate?.formatDate(DateFormat.MMMddyyyy)

    val compareGenesisDate = compareCryptoInfo?.genesis_date?.convertStringToDate(DateFormat.yyyyMMdd, gmtTimeZone = false)
    val compareFormattedGenesisDate = compareGenesisDate?.formatDate(DateFormat.MMMddyyyy)

    CompareDualValueSection(
        title = "Genesis Date",
        mainAccessibilityLabel = accessibleGenesisDateLabel(
            name = mainCryptoInfo?.name,
            date = mainFormattedGenesisDate,
        ),
        compareAccessibilityLabel = accessibleGenesisDateLabel(
            name = compareCryptoInfo?.name,
            date = compareFormattedGenesisDate,
        ),
        mainValue = mainFormattedGenesisDate ?: "Unknown",
        compareValue = compareFormattedGenesisDate ?: "Unknown",
        isMainLoading = mainCryptoInfo?.market_data?.atl_date == null,
        isCompareLoading = compareCryptoInfo?.market_data?.atl_date == null,
    )
}

@Composable
private fun CompareDualValueSection(
    title: String,
    mainAccessibilityLabel: String,
    compareAccessibilityLabel: String,
    mainValue: String,
    compareValue: String,
    isMainLoading: Boolean,
    isCompareLoading: Boolean,
) {
    val isHorizontal = AccessibilityLayout.isHorizontal
    val childModifier = Modifier.semantics { invisibleToUser() }

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.semantics(mergeDescendants = true) {},
    ) {
        Text(
            text = title,
            style = satoshiBold(12),
            color = MaterialTheme.colors.onSurfaceContainerDim
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
                        contentDescription = mainAccessibilityLabel
                    },
            ) {
                CompareRectangleView(
                    color = MaterialTheme.colors.primary50
                )

                Text(
                    text = mainValue,
                    style = satoshiBold(16),
                    color = MaterialTheme.colors.onSurface,
                    modifier = Modifier
                        .shimmeringModifier(isMainLoading)
                        .then(childModifier)
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = sectionModifier()
                    .clearAndSetSemantics {
                        contentDescription = compareAccessibilityLabel
                    },
            ) {
                CompareRectangleView(
                    color = MaterialTheme.colors.blue50
                )

                Text(
                    text = compareValue,
                    style = satoshiBold(16),
                    color = MaterialTheme.colors.onSurface,
                    modifier = Modifier
                        .shimmeringModifier(isCompareLoading)
                        .then(childModifier)
                )
            }
        }
    }
}
