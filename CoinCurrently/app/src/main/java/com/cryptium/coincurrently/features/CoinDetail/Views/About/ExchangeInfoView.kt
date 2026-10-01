package com.cryptium.coincurrently.features.CoinDetail.Views.About
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
import com.cryptium.coincurrently.sharedui.components.AccessibilityLayout
import com.cryptium.coincurrently.sharedui.components.AdaptiveStack
import com.cryptium.coincurrently.sharedui.components.Separator
import com.cryptium.coincurrently.sharedui.theme.onSurfaceDim
import com.cryptium.coincurrently.sharedui.theme.primaryBackgroundNoRipple
import com.cryptium.coincurrently.core.extensions.satoshiBold
import com.cryptium.coincurrently.core.localization.Localization

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
            text = Localization.string("Exchange Info"),
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

    val localizedLabel = Localization.string(label)
    val displayedValue = if (label == "Exchange Type") Localization.string(value) else value
    val labelAccessibility = if (label == "Exchange Type") {
        Localization.string("Exchange type: ")
    } else {
        "$localizedLabel: "
    }

    Column(
        modifier = modifier
            .then(tabPage)
            .semantics(mergeDescendants = true) {
                contentDescription = "$labelAccessibility$displayedValue"
            },
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = localizedLabel,
            style = satoshiBold(12),
            color = MaterialTheme.colors.onSurfaceDim,
            modifier = Modifier.semantics { invisibleToUser() },
        )
        Text(
            text = displayedValue,
            style = satoshiBold(16),
            color = MaterialTheme.colors.onSurface,
            modifier = Modifier.semantics { invisibleToUser() },
        )
    }
}
