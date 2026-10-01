package com.cryptium.coincurrently.features.CoinDetail.Views.Compare
import com.cryptium.coincurrently.sharedui.modifiers.imageSize

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.invisibleToUser
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.cryptium.coincurrently.R
import com.cryptium.coincurrently.core.localization.Localization
import com.cryptium.coincurrently.sharedui.components.AccessibilityLayout
import com.cryptium.coincurrently.sharedui.components.AdaptiveStack
import com.cryptium.coincurrently.sharedui.theme.blue50
import com.cryptium.coincurrently.sharedui.theme.onSurfaceContainerDim
import com.cryptium.coincurrently.sharedui.theme.onSurfaceDim
import com.cryptium.coincurrently.sharedui.theme.onSurfaceDimmer
import com.cryptium.coincurrently.sharedui.theme.primary50
import com.cryptium.coincurrently.sharedui.theme.primaryBackgroundNoRipple
import com.cryptium.coincurrently.sharedui.modifiers.shimmeringModifier
import com.cryptium.coincurrently.core.extensions.satoshiBold
import com.cryptium.coincurrently.sharedui.components.CustomProgressBarView
import com.cryptium.coincurrently.sharedui.charts.hideFromTalkBackWhenEnabled
import com.cryptium.coincurrently.core.analytics.AnalyticsManager
import com.cryptium.coincurrently.core.analytics.ScreenLabel
import com.cryptium.coincurrently.core.analytics.SheetLabel
import com.cryptium.coincurrently.core.analytics.SheetOpened
import com.cryptium.coincurrently.core.networking.ResponseModels.CryptoDetailInfoModel

@Composable
fun CompareSentimentView(
    mainCryptoInfo: CryptoDetailInfoModel?,
    compareCryptoInfo: CryptoDetailInfoModel?,
    showSentimentInfo: MutableState<Boolean>,
    fromScreen: ScreenLabel = ScreenLabel.CompareCoins,
    modifier: Modifier = Modifier,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier
            .padding(
                horizontal = 16.dp
            )
    ) {
        HeaderView(
            showSentimentInfo = showSentimentInfo,
            fromScreen = fromScreen,
        )

        VotesView(
            mainCryptoInfo = mainCryptoInfo,
            compareCryptoInfo = compareCryptoInfo,
        )
    }
}

@Composable
private fun HeaderView(
    showSentimentInfo: MutableState<Boolean>,
    fromScreen: ScreenLabel,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = Localization.string("Community Sentiment"),
            style = satoshiBold(22),
            color = MaterialTheme.colors.onSurfaceDim,
            modifier = Modifier.semantics { heading() },
        )

        Spacer(modifier = Modifier.weight(1f))

        Image(
            painter = painterResource(id = R.drawable.info),
            contentDescription = Localization.string("Community sentiment info"),
            colorFilter = ColorFilter.tint(MaterialTheme.colors.onSurfaceDimmer),
            modifier = Modifier
                .padding(12.dp)
                .imageSize(24.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    AnalyticsManager.log(
                        SheetOpened(fromScreen = fromScreen, sheet = SheetLabel.CommunitySentimentHelp),
                    )
                    showSentimentInfo.value = true
                }
        )
    }
}

@Composable
private fun VotesView(
    mainCryptoInfo: CryptoDetailInfoModel?,
    compareCryptoInfo: CryptoDetailInfoModel?,
) {
    val mainUpVotes = mainCryptoInfo?.sentiment_votes_up_percentage
    val compareUpVotes = compareCryptoInfo?.sentiment_votes_up_percentage

    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
            .primaryBackgroundNoRipple()
            .semantics(mergeDescendants = true) {},
    ) {
        Text(
            text = Localization.string("Sentiment Votes"),
            style = satoshiBold(12),
            color = MaterialTheme.colors.onSurfaceContainerDim
        )

        CustomProgressBarView(
            progress = mainUpVotes?.div(100),
            foregroundColor = MaterialTheme.colors.primary50,
            downloading = mainUpVotes == null,
            modifier = Modifier
                .padding(top = 12.dp)
                .hideFromTalkBackWhenEnabled()
        )

        CustomProgressBarView(
            progress = compareUpVotes?.div(100),
            foregroundColor = MaterialTheme.colors.blue50,
            downloading = compareUpVotes == null,
            modifier = Modifier
                .padding(bottom = 12.dp)
                .hideFromTalkBackWhenEnabled()
        )

        SentimentMeterTitles(
            mainCryptoInfo = mainCryptoInfo,
            compareCryptoInfo = compareCryptoInfo
        )
    }
}

@Composable
private fun SentimentMeterTitles(
    mainCryptoInfo: CryptoDetailInfoModel?,
    compareCryptoInfo: CryptoDetailInfoModel?,
) {
    val isHorizontal = AccessibilityLayout.isHorizontal
    val childModifier = Modifier.semantics { invisibleToUser() }

    AdaptiveStack(
        isHorizontal = isHorizontal,
        spacing = if (isHorizontal) 16.dp else 8.dp,
        horizontalAlignment = Alignment.Start,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = sectionModifier()
                .clearAndSetSemantics {
                    contentDescription = accessibleBullishLabel(
                        name = mainCryptoInfo?.name,
                        percent = mainCryptoInfo?.sentiment_votes_up_percentage,
                    )
                },
        ) {
            CompareRectangleView(
                color = MaterialTheme.colors.primary50,
                modifier = Modifier
                    .padding(end = 4.dp)
            )

            Text(
                text = "${String.format("%.0f", mainCryptoInfo?.sentiment_votes_up_percentage ?: 0.0)}%",
                style = satoshiBold(16),
                color = MaterialTheme.colors.onSurface,
                modifier = Modifier
                    .shimmeringModifier(mainCryptoInfo?.sentiment_votes_up_percentage == null)
                    .then(childModifier)
            )

            Text(
                text = Localization.string("Bullish"),
                style = satoshiBold(16),
                color = MaterialTheme.colors.onSurfaceDimmer,
                modifier = Modifier
                    .weight(1f)
                    .then(childModifier)
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = sectionModifier()
                .clearAndSetSemantics {
                    contentDescription = accessibleBullishLabel(
                        name = compareCryptoInfo?.name,
                        percent = compareCryptoInfo?.sentiment_votes_up_percentage,
                    )
                },
        ) {
            CompareRectangleView(
                color = MaterialTheme.colors.blue50,
                modifier = Modifier
                    .padding(end = 4.dp)
            )

            Text(
                text = "${String.format("%.0f", compareCryptoInfo?.sentiment_votes_up_percentage ?: 0.0)}%",
                style = satoshiBold(16),
                color = MaterialTheme.colors.onSurface,
                modifier = Modifier
                    .shimmeringModifier(compareCryptoInfo?.sentiment_votes_up_percentage == null)
                    .then(childModifier)
            )

            Text(
                text = Localization.string("Bullish"),
                style = satoshiBold(16),
                color = MaterialTheme.colors.onSurfaceDimmer,
                modifier = childModifier,
            )
        }
    }
}
