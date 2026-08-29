package com.cryptium.coincurrently.ui.main.MarketTab.CryptoDetail.Components.About

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
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
import com.cryptium.coincurrently.ui.main.Compose.AccessibilityLayout
import com.cryptium.coincurrently.ui.main.Compose.AdaptiveStack
import com.cryptium.coincurrently.ui.main.Compose.Separator
import com.cryptium.coincurrently.ui.main.Compose.onSurfaceDim
import com.cryptium.coincurrently.ui.main.Compose.primaryBackgroundNoRipple
import com.cryptium.coincurrently.ui.main.Extensions.satoshiBold

/**
 * Exchange Info section showing Founded, Headquarters, and Exchange Type.
 * Only shown for exchange detail screens.
 */
@Composable
fun ExchangeInfoView(
    data: ExchangeInfoData,
    tabPage: Modifier = Modifier,
    modifier: Modifier = Modifier,
) {
    val hasFounded = data.founded != null
    val hasHeadquarters = !data.headquarters.isNullOrEmpty()
    val hasExchangeType = data.exchangeType != null
    val hasFirstRow = hasFounded || hasHeadquarters
    val hasSecondRow = hasExchangeType
    val hasAnyData = hasFirstRow || hasSecondRow

    if (!hasAnyData) return

    val isHorizontal = AccessibilityLayout.isHorizontal

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier
    ) {
        Text(
            text = "Exchange Info",
            style = satoshiBold(22),
            color = MaterialTheme.colors.onSurfaceDim,
            modifier = tabPage.semantics { heading() },
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .primaryBackgroundNoRipple(
                    cornerRadius = 12.dp
                )
        ) {
            if (hasFirstRow) {
                AdaptiveStack(
                    isHorizontal = isHorizontal,
                    spacing = if (isHorizontal) 16.dp else 12.dp,
                    horizontalAlignment = Alignment.Start,
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    if (hasFounded) {
                        ExchangeInfoItem(
                            label = "Founded",
                            value = data.founded,
                            modifier = sectionModifier(),
                            tabPage = tabPage,
                        )
                    }

                    if (!isHorizontal && hasFounded && hasHeadquarters) {
                        Separator()
                    }

                    if (hasHeadquarters) {
                        ExchangeInfoItem(
                            label = "Headquarters",
                            value = data.headquarters,
                            modifier = sectionModifier(),
                            tabPage = tabPage,
                        )
                    }
                }
            }

            if (hasSecondRow) {
                if (hasFirstRow) {
                    Separator()
                }

                ExchangeInfoItem(
                    label = "Exchange Type",
                    value = data.exchangeType,
                    modifier = Modifier.fillMaxWidth(),
                    tabPage = tabPage,
                )
            }
        }
    }
}

@Composable
private fun ExchangeInfoItem(
    label: String,
    value: String?,
    modifier: Modifier = Modifier,
    tabPage: Modifier = Modifier,
) {
    if (value == null) return

    Column(
        modifier = modifier
            .then(tabPage)
            .semantics(mergeDescendants = true) {
                contentDescription = "$label: $value"
            },
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = label,
            style = satoshiBold(12),
            color = MaterialTheme.colors.onSurfaceDim,
            modifier = Modifier.semantics { invisibleToUser() },
        )
        Text(
            text = value,
            style = satoshiBold(16),
            color = MaterialTheme.colors.onSurface,
            modifier = Modifier.semantics { invisibleToUser() },
        )
    }
}
