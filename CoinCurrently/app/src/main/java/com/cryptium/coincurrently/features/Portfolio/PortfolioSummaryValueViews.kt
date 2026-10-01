package com.cryptium.coincurrently.features.Portfolio

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.invisibleToUser
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.cryptium.coincurrently.R
import com.cryptium.coincurrently.core.localization.Localization
import com.cryptium.coincurrently.core.extensions.*
import com.cryptium.coincurrently.features.Portfolio.Views.portfolioChangeAccessibilityLabel
import com.cryptium.coincurrently.sharedui.components.*
import com.cryptium.coincurrently.sharedui.modifiers.*
import com.cryptium.coincurrently.sharedui.models.*
import com.cryptium.coincurrently.sharedui.theme.*
import com.cryptium.coincurrently.sharedui.loading.ChangeSize
import com.cryptium.coincurrently.sharedui.loading.PriceChangeDirectionView

@Composable
internal fun TotalValueView(
    totalValue: Double?,
    hideData: Boolean,
    isPieChartShowing: Boolean,
    togglePieChart: () -> Unit,
    toggleHideData: () -> Unit,
) {
    val formattedTotalValue =
        totalValue?.formatCurrency(threshold = FormattingThreshold.BILLION) ?: "000000000"
    val portfolioValueLabel = Localization.format(
        "Portfolio value: %@",
        if (hideData) stars else formattedTotalValue,
    )
    val hideDataLabel = Localization.string(if (hideData) "Show portfolio data" else "Hide portfolio data")
    val pieChartToggleLabel = Localization.string(
        if (isPieChartShowing) "Switch to line chart" else "Switch to pie chart",
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        Text(
            text = Localization.string("Portfolio Value"),
            style = satoshiBold(18),
            color = MaterialTheme.colors.onSurfaceDim,
            modifier = Modifier.semantics { invisibleToUser() }
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = if (hideData) stars else formattedTotalValue,
                style = satoshiBold(36),
                color = MaterialTheme.colors.onSurface,
                modifier = Modifier
                    .shimmeringModifier(totalValue == null)
                    .semantics {
                        contentDescription = portfolioValueLabel
                    }
            )

            Image(
                painter = painterResource(if (hideData) R.drawable.unhide else R.drawable.hide),
                contentDescription = hideDataLabel,
                colorFilter = ColorFilter.tint(MaterialTheme.colors.onSurfaceDimmer),
                modifier = Modifier
                    .padding(12.dp)
                    .imageSize(24.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                    ) {
                        toggleHideData.invoke()
                    }
            )

            Spacer(modifier = Modifier.weight(1f))

            Image(
                painter = painterResource(if (isPieChartShowing) R.drawable.linechart else R.drawable.portfolio),
                contentDescription = pieChartToggleLabel,
                colorFilter = ColorFilter.tint(MaterialTheme.colors.onSurface),
                modifier = Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colors.surfaceContainerHigh)
                    .padding(12.dp)
                    .imageSize(24.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                    ) {
                        togglePieChart.invoke()
                    }
            )
        }
    }
}

@Composable
internal fun ValueChangeView(
    changePercent: Double?,
    changeValue: Double?,
    isEmpty: Boolean,
    hideData: Boolean,
    modifier: Modifier,
) {
    val roundedPercent = changePercent?.roundTo(2)
    val formattedPercent = "%.2f".format(roundedPercent ?: 0.0).changeWithSymbol
    val accessibilityLabel = portfolioChangeAccessibilityLabel(
        title = Localization.string("7 days value change"),
        value = changeValue,
        percent = changePercent,
        hideData = hideData,
        isEmpty = isEmpty,
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier.semantics(mergeDescendants = true) {
            contentDescription = accessibilityLabel
        }
    ) {
        Text(
            text = Localization.string("Value Change (7d)"),
            style = satoshiBold(12),
            color = MaterialTheme.colors.onSurfaceDim,
            modifier = Modifier.semantics { invisibleToUser() }
        )

        FlowRow(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            itemVerticalAlignment = Alignment.CenterVertically
        ) {
            if (hideData) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.semantics { invisibleToUser() }
                ) {
                    PriceChangeDirectionView(
                        change = 0.0,
                        size = ChangeSize.SMALL,
                        isDownloading = changePercent == null
                    )

                    Text(
                        text = stars,
                        style = satoshiBold(14),
                        color = MaterialTheme.colors.onSurfaceDim,
                        modifier = Modifier.semantics { invisibleToUser() }
                    )
                }
            } else {
                PriceChangeValueView(
                    change = changeValue,
                    size = ChangeSize.SMALL,
                    isDownloading = changeValue == null
                )
            }

            Text(
                text = "(${if (changePercent?.isInfinite() ?: true || isEmpty) "-" else formattedPercent+"%"})",
                style = satoshiBold(14),
                color = roundedPercent?.changeColor() ?: MaterialTheme.colors.onSurfaceDim,
                modifier = Modifier
                    .shimmeringModifier(changePercent == null)
                    .semantics { invisibleToUser() }
            )
        }
    }
}

@Composable
internal fun ProfitLossView(
    changePercent: Double?,
    changeValue: Double?,
    hideData: Boolean,
    isEmpty: Boolean,
    modifier: Modifier,
) {
    val roundedPercent = changePercent?.roundTo(2)
    val formattedPercent = "%.2f".format(roundedPercent ?: 0.0).changeWithSymbol
    val accessibilityLabel = portfolioChangeAccessibilityLabel(
        title = Localization.string("Profit/Loss"),
        value = changeValue,
        percent = changePercent,
        hideData = hideData,
        isEmpty = isEmpty,
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier.semantics(mergeDescendants = true) {
            contentDescription = accessibilityLabel
        }
    ) {
        Text(
            text = Localization.string("Profit/Loss"),
            style = satoshiBold(12),
            color = MaterialTheme.colors.onSurfaceDim,
            modifier = Modifier.semantics { invisibleToUser() }
        )

        FlowRow(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            itemVerticalAlignment = Alignment.CenterVertically
        ) {
            if (hideData) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.semantics { invisibleToUser() }
                ) {
                    PriceChangeDirectionView(
                        change = 0.0,
                        size = ChangeSize.SMALL,
                        isDownloading = changeValue == null
                    )

                    Text(
                        text = stars,
                        style = satoshiBold(14),
                        color = MaterialTheme.colors.onSurfaceDim,
                        modifier = Modifier.semantics { invisibleToUser() }
                    )
                }
            } else {
                PriceChangeValueView(
                    change = changeValue,
                    size = ChangeSize.SMALL,
                    hideData = hideData,
                    isDownloading = changeValue == null
                )
            }

            Text(
                text = "(${if (changePercent?.isInfinite() ?: true || isEmpty) "-" else formattedPercent+"%"})",
                style = satoshiBold(14),
                color = roundedPercent?.changeColor() ?: MaterialTheme.colors.onSurfaceDim,
                modifier = Modifier
                    .shimmeringModifier(changePercent == null)
                    .semantics { invisibleToUser() }
            )
        }
    }
}

