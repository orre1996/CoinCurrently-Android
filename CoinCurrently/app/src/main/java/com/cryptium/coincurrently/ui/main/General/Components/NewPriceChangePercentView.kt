package com.cryptium.coincurrently.ui.main.General.Components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.unit.dp
import com.cryptium.coincurrently.ui.main.Compose.AccessibilityLayout
import com.cryptium.coincurrently.ui.main.General.Components.Chart.TAB_PAGE_CONTENT_TALKBACK_TRAVERSAL_INDEX
import com.cryptium.coincurrently.ui.main.Compose.onSurfaceContainerDim
import com.cryptium.coincurrently.ui.main.Compose.shimmeringModifier
import com.cryptium.coincurrently.ui.main.Extensions.changeColor
import com.cryptium.coincurrently.ui.main.Extensions.changeDecimalPoints
import com.cryptium.coincurrently.ui.main.Extensions.changePercentAccessible
import com.cryptium.coincurrently.ui.main.Extensions.directionAccessible
import com.cryptium.coincurrently.ui.main.Extensions.formatChangePercent
import com.cryptium.coincurrently.ui.main.Extensions.roundTo
import com.cryptium.coincurrently.ui.main.Extensions.satoshiBold
import com.cryptium.coincurrently.ui.main.General.Loading.ChangeSize
import com.cryptium.coincurrently.ui.main.General.Loading.NewShimmerImageDirection

@Composable
fun NewPriceChangePercentView(
    change: Double?,
    size: ChangeSize = ChangeSize.MEDIUM,
    accessibleTimeframe: String = "",
    isDownloading: Boolean,
    modifier: Modifier = Modifier,
    tabPageTalkBack: Boolean = false,
) {
    val talkBackEnabled = AccessibilityLayout.isTalkBackEnabled
    val decimals = change?.changeDecimalPoints ?: 2
    val roundedChange = change?.roundTo(decimals)
    val safeChange = if (change?.isInfinite() == true) 0.0 else change
    val direction = safeChange?.directionAccessible ?: ""
    val accessibleChange = safeChange?.changePercentAccessible ?: ""
    val accessibilityLabel = if (accessibleTimeframe.isEmpty()) {
        "Price change: $direction $accessibleChange percent"
    } else {
        "$accessibleTimeframe price change. $direction $accessibleChange percent"
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier.clearAndSetSemantics {
            contentDescription = accessibilityLabel
            if (tabPageTalkBack && talkBackEnabled) {
                traversalIndex = TAB_PAGE_CONTENT_TALKBACK_TRAVERSAL_INDEX
            }
        }
    ) {
        NewShimmerImageDirection(
            change = roundedChange,
            size = size,
            isDownloading = isDownloading
        )

        Text(
            text = (change ?: 0.0).formatChangePercent(),
            style = satoshiBold(size.fontSize),
            color = roundedChange?.changeColor() ?: MaterialTheme.colors.onSurfaceContainerDim,
            maxLines = 1,
            modifier = Modifier
                .shimmeringModifier(isDownloading)
        )
    }
}