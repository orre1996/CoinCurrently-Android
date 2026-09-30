package com.cryptium.coincurrently.sharedui.components
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
import com.cryptium.coincurrently.core.localization.Localization
import com.cryptium.coincurrently.sharedui.components.AccessibilityLayout
import com.cryptium.coincurrently.sharedui.charts.TAB_PAGE_CONTENT_TALKBACK_TRAVERSAL_INDEX
import com.cryptium.coincurrently.sharedui.theme.onSurfaceContainerDim
import com.cryptium.coincurrently.sharedui.modifiers.shimmeringModifier
import com.cryptium.coincurrently.core.extensions.changeColor
import com.cryptium.coincurrently.core.extensions.changeDecimalPoints
import com.cryptium.coincurrently.core.extensions.changePercentAccessible
import com.cryptium.coincurrently.core.extensions.directionAccessible
import com.cryptium.coincurrently.core.extensions.formatChangePercent
import com.cryptium.coincurrently.core.extensions.roundTo
import com.cryptium.coincurrently.core.extensions.satoshiBold
import com.cryptium.coincurrently.sharedui.loading.ChangeSize
import com.cryptium.coincurrently.sharedui.loading.PriceChangeDirectionView

@Composable
fun PriceChangePercentView(
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
    val localizedDirection = if (direction.isEmpty()) "" else Localization.string(direction)
    val accessibleChange = safeChange?.changePercentAccessible ?: ""
    val accessibilityLabel = if (accessibleTimeframe.isEmpty()) {
        Localization.format("Price change: %@ %@ percent", localizedDirection, accessibleChange)
    } else {
        Localization.format(
            "%@ price change: %@ %@ percent",
            Localization.string(accessibleTimeframe),
            localizedDirection,
            accessibleChange,
        )
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
        PriceChangeDirectionView(
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