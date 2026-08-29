package com.cryptium.coincurrently.ui.main.MarketTab.CryptoDetail.Components.Compare

import androidx.compose.foundation.layout.Box
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cryptium.coincurrently.ui.main.Compose.blue50
import com.cryptium.coincurrently.ui.main.Compose.outline
import com.cryptium.coincurrently.ui.main.Compose.primary50
import com.cryptium.coincurrently.ui.main.Extensions.dpToPx
import com.cryptium.coincurrently.ui.main.General.Components.Chart.HoverCircle
import com.cryptium.coincurrently.ui.main.General.Components.Chart.VerticalDottedLine
import com.cryptium.coincurrently.ui.main.General.Components.Chart.hideFromTalkBackWhenEnabled
import com.cryptium.coincurrently.ui.main.General.Utilities.Point

@Composable
fun CompareChartHoverView(
    mainPosition: Point,
    hideMainMark: Boolean,
    comparePosition: Point,
    hideCompareMark: Boolean,
    isDragging: Boolean,
    topPadding: Dp = 0.dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .hideFromTalkBackWhenEnabled()
            .alpha(
                if (isDragging) {
                    1f
                } else {
                    0f
                }
            )
    ) {
        val xPosition = mainPosition.x.dpToPx()

        VerticalDottedLine(
            xPosition = xPosition,
            yOffset = -topPadding,
            color = MaterialTheme.colors.outline.copy(0.5f)
        )

        if (!hideMainMark) {
            HoverCircle(
                position = mainPosition,
                color = MaterialTheme.colors.primary50,
                topPadding = topPadding
            )
        }

        if (!hideCompareMark) {
            HoverCircle(
                position = Point(mainPosition.x, comparePosition.y),
                color = MaterialTheme.colors.blue50,
                topPadding = topPadding
            )
        }
    }
}