package com.cryptium.coincurrently.features.ExchangeDetail.Views
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.invisibleToUser
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.cryptium.coincurrently.sharedui.components.AccessibilityLayout
import com.cryptium.coincurrently.sharedui.components.AdaptiveStack
import com.cryptium.coincurrently.sharedui.components.Separator
import com.cryptium.coincurrently.sharedui.components.SeparatorVertical
import com.cryptium.coincurrently.sharedui.theme.onSurfaceDim
import com.cryptium.coincurrently.sharedui.theme.primaryBackgroundNoRipple
import com.cryptium.coincurrently.sharedui.modifiers.shimmeringModifier
import com.cryptium.coincurrently.core.extensions.satoshiBold
import com.cryptium.coincurrently.core.localization.Localization
import com.cryptium.coincurrently.core.extensions.trustColor
import com.cryptium.coincurrently.core.networking.ResponseModels.ExchangeResponse

@Composable
fun ExchangeDetailSummaryView(
    exchangeDetail: ExchangeResponse?
) {
    val isHorizontal = AccessibilityLayout.isHorizontal

    Box(
        modifier = Modifier
            .padding(16.dp)
            .primaryBackgroundNoRipple(
                cornerRadius = 12.dp
            )
    ) {
        AdaptiveStack(
            isHorizontal = isHorizontal,
            spacing = if (isHorizontal) 32.dp else 16.dp,
            horizontalAlignment = Alignment.Start,
            verticalAlignment = Alignment.Top,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            StatItem(
                label = "Coins",
                value = "${exchangeDetail?.coins ?: 0}",
                isLoading = exchangeDetail?.coins == null,
                modifier = sectionModifier(),
            )

            if (!isHorizontal) {
                Separator()
            }

            StatItem(
                label = "Pairs",
                value = "${exchangeDetail?.pairs ?: 0}",
                isLoading = exchangeDetail?.pairs == null,
                modifier = sectionModifier(),
            )

            if (!isHorizontal) {
                Separator()
            }

            TrustScoreItem(
                trustScore = exchangeDetail?.trust_score,
                isLoading = exchangeDetail?.trust_score == null,
                modifier = sectionModifier(),
            )
        }

        if (isHorizontal) {
            Row(
                modifier = Modifier
                    .matchParentSize()
                    .padding(vertical = 12.dp)
            ) {
                Spacer(modifier = Modifier.weight(1f))

                Box(modifier = Modifier.semantics { invisibleToUser() }) {
                    SeparatorVertical(width = 2.dp)
                }

                Spacer(modifier = Modifier.weight(1f))

                Box(modifier = Modifier.semantics { invisibleToUser() }) {
                    SeparatorVertical(width = 2.dp)
                }

                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String,
    isLoading: Boolean,
    modifier: Modifier = Modifier,
) {
    val localizedLabel = Localization.string(label)
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = Alignment.Start,
        modifier = modifier
            .semantics(mergeDescendants = true) {
                contentDescription = "$localizedLabel: $value"
            },
    ) {
        Text(
            text = localizedLabel,
            style = satoshiBold(12),
            color = MaterialTheme.colors.onSurfaceDim,
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

@Composable
private fun TrustScoreItem(
    trustScore: Int?,
    isLoading: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = Alignment.Start,
        modifier = modifier
            .semantics(mergeDescendants = true) {
                contentDescription = "${Localization.string("Trust score: ")}${Localization.format("%d of 10", trustScore ?: 0)}"
            },
    ) {
        Text(
            text = Localization.string("Trust Score"),
            style = satoshiBold(12),
            color = MaterialTheme.colors.onSurfaceDim,
            modifier = Modifier.semantics { invisibleToUser() },
        )

        Text(
            text = "${trustScore ?: 0}/10",
            style = satoshiBold(16),
            color = trustScore?.trustColor ?: MaterialTheme.colors.onSurfaceDim,
            modifier = Modifier
                .shimmeringModifier(isLoading)
                .semantics { invisibleToUser() }
        )
    }
}
