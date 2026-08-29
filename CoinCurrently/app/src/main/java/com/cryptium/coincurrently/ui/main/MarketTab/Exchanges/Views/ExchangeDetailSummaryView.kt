package com.cryptium.coincurrently.ui.main.MarketTab.Exchanges.Views

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
import com.cryptium.coincurrently.ui.main.Compose.AccessibilityLayout
import com.cryptium.coincurrently.ui.main.Compose.AdaptiveStack
import com.cryptium.coincurrently.ui.main.Compose.Separator
import com.cryptium.coincurrently.ui.main.Compose.SeparatorVertical
import com.cryptium.coincurrently.ui.main.Compose.onSurfaceDim
import com.cryptium.coincurrently.ui.main.Compose.primaryBackgroundNoRipple
import com.cryptium.coincurrently.ui.main.Compose.shimmeringModifier
import com.cryptium.coincurrently.ui.main.Extensions.satoshiBold
import com.cryptium.coincurrently.ui.main.Extensions.trustColor
import networking.ResponseModels.ExchangeResponse

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
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = Alignment.Start,
        modifier = modifier
            .semantics(mergeDescendants = true) {
                contentDescription = "$label: $value"
            },
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
                contentDescription = "Trust score: ${trustScore ?: 0} of 10"
            },
    ) {
        Text(
            text = "Trust Score",
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
