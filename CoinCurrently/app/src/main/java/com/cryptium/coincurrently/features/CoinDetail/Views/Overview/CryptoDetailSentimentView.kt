package com.cryptium.coincurrently.features.CoinDetail.Views.Overview
import com.cryptium.coincurrently.sharedui.modifiers.imageSize

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.invisibleToUser
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.cryptium.coincurrently.R
import com.cryptium.coincurrently.core.localization.Localization
import com.cryptium.coincurrently.sharedui.components.contentScreenWidth
import com.cryptium.coincurrently.sharedui.components.tabPageTalkBackTraversal
import com.cryptium.coincurrently.sharedui.theme.onSurfaceContainerDim
import com.cryptium.coincurrently.sharedui.theme.onSurfaceDim
import com.cryptium.coincurrently.sharedui.theme.onSurfaceDimmer
import com.cryptium.coincurrently.sharedui.theme.primaryBackgroundNoRipple
import com.cryptium.coincurrently.sharedui.modifiers.shimmeringModifier
import com.cryptium.coincurrently.sharedui.theme.trendDown
import com.cryptium.coincurrently.sharedui.theme.trendUp
import com.cryptium.coincurrently.core.extensions.dpToPx
import com.cryptium.coincurrently.core.extensions.pxToDp
import com.cryptium.coincurrently.core.extensions.satoshiBold
import com.cryptium.coincurrently.sharedui.charts.hideFromTalkBackWhenEnabled
import com.cryptium.coincurrently.core.analytics.AnalyticsManager
import com.cryptium.coincurrently.core.analytics.ScreenLabel
import com.cryptium.coincurrently.core.analytics.SheetLabel
import com.cryptium.coincurrently.core.analytics.SheetOpened
import kotlin.math.roundToInt

@Composable
fun CryptoDetailSentimentView(
    sentimentVotesUpPercentage: Double?,
    sentimentVotesDownPercentage: Double?,
    showSentimentInfo: MutableState<Boolean>,
    fromScreen: ScreenLabel = ScreenLabel.CryptoDetail,
) {
    val tabPage = Modifier.tabPageTalkBackTraversal()

    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        HeaderView(
            showSentimentInfo = showSentimentInfo,
            fromScreen = fromScreen,
            tabPage = tabPage,
        )

        VotesView(
            sentimentVotesUpPercentage = sentimentVotesUpPercentage,
            sentimentVotesDownPercentage = sentimentVotesDownPercentage,
            tabPage = tabPage,
        )
    }
}

@Composable
private fun HeaderView(
    showSentimentInfo: MutableState<Boolean>,
    fromScreen: ScreenLabel,
    tabPage: Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = Localization.string("Community Sentiment"),
            style = satoshiBold(22),
            color = MaterialTheme.colors.onSurfaceDim,
            modifier = tabPage.semantics { heading() },
        )

        Spacer(modifier = Modifier.weight(1f))

        Image(
            painter = painterResource(id = R.drawable.info),
            contentDescription = Localization.string("Community sentiment info"),
            colorFilter = ColorFilter.tint(MaterialTheme.colors.onSurfaceDimmer),
            modifier = tabPage
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
    sentimentVotesUpPercentage: Double?,
    sentimentVotesDownPercentage: Double?,
    tabPage: Modifier,
) {
    Column(
        modifier = tabPage
            .primaryBackgroundNoRipple()
            .semantics(mergeDescendants = true) {
                val bullish = (sentimentVotesUpPercentage ?: 0.0).roundToInt()
                val bearish = (sentimentVotesDownPercentage ?: 0.0).roundToInt()
                contentDescription = "${Localization.string("Sentiment Votes")}. ${Localization.string("Bullish")} $bullish percent. ${Localization.string("Bearish")} $bearish percent."
            },
    ) {
        Text(
            text = Localization.string("Sentiment Votes"),
            style = satoshiBold(12),
            color = MaterialTheme.colors.onSurfaceContainerDim,
            modifier = Modifier.semantics { invisibleToUser() },
        )

        SentimentMeter(
            sentimentVotesUpPercentage = sentimentVotesUpPercentage,
            sentimentVotesDownPercentage = sentimentVotesDownPercentage
        )

        SentimentMeterTitles(
            sentimentVotesUpPercentage = sentimentVotesUpPercentage,
            sentimentVotesDownPercentage = sentimentVotesDownPercentage
        )
    }
}

@Composable
private fun SentimentMeter(
    sentimentVotesUpPercentage: Double?,
    sentimentVotesDownPercentage: Double?
) {
    val screenWidth = contentScreenWidth()

    val localDensity = LocalDensity.current
    var fearMeterWidth by remember { mutableStateOf(0.dp) }

    Box(
        modifier = Modifier
            .padding(vertical = 12.dp)
            .clip(CircleShape)
            .hideFromTalkBackWhenEnabled()
            .onGloballyPositioned { coordinates ->
                fearMeterWidth = with(localDensity) { coordinates.size.width.toDp() }
            }
            .shimmeringModifier(sentimentVotesUpPercentage == null || sentimentVotesDownPercentage == null)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp)
                .background(MaterialTheme.colors.onSurfaceDim)

        )

        if (sentimentVotesUpPercentage != null && sentimentVotesDownPercentage != null) {
            Row {
                Box(
                    modifier = Modifier
                        .widthIn(min = (fearMeterWidth.dpToPx().times(sentimentVotesUpPercentage / 100.0)).pxToDp(), max = screenWidth)
                        .height(24.dp)
                        .background(MaterialTheme.colors.trendUp)
                )

                Box(
                    modifier = Modifier
                        .widthIn(min = (fearMeterWidth.dpToPx().times(sentimentVotesDownPercentage / 100.0)).pxToDp(), max = screenWidth)
                        .height(24.dp)
                        .background(MaterialTheme.colors.trendDown)
                )
            }
        }
    }
}

@Composable
private fun SentimentMeterTitles(
    sentimentVotesUpPercentage: Double?,
    sentimentVotesDownPercentage: Double?
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "${(sentimentVotesUpPercentage ?: 0.0).roundToInt()}%",
                style = satoshiBold(16),
                color = MaterialTheme.colors.trendUp,
                modifier = Modifier
                    .shimmeringModifier(sentimentVotesUpPercentage == null)
                    .semantics { invisibleToUser() }
            )

            Text(
                text = Localization.string("Bullish"),
                style = satoshiBold(12),
                color = MaterialTheme.colors.onSurfaceDim,
                modifier = Modifier.semantics { invisibleToUser() },
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = "${(sentimentVotesDownPercentage ?: 0.0).roundToInt()}%",
                style = satoshiBold(16),
                color = MaterialTheme.colors.trendDown,
                modifier = Modifier
                    .shimmeringModifier(sentimentVotesDownPercentage == null)
                    .semantics { invisibleToUser() }
            )

            Text(
                text = Localization.string("Bearish"),
                style = satoshiBold(12),
                color = MaterialTheme.colors.onSurfaceDim,
                modifier = Modifier.semantics { invisibleToUser() },
            )
        }
    }
}