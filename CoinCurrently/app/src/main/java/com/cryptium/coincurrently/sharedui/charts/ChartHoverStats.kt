package com.cryptium.coincurrently.sharedui.charts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.cryptium.coincurrently.sharedui.charts.hideFromTalkBackWhenEnabled

@Composable
fun ChartHoverStats(
    isMultiHover: Boolean,
    hoverTimes: List<String>,
    differenceValue: String,
    differencePercent: String,
    multiHoverColor: Color,
    hoverPrice: String,
    hoverPriceColor: Color?,
    hoverTime: String,
    hoverTimePosition: Dp,
    isDragging: Boolean,
    hoverPriceTextWidth: MutableState<Dp>,
    modifier: Modifier = Modifier,
    hideFromTalkBack: Boolean = true,
    horizontalEdgePadding: Dp = 16.dp,
) {
    Box(
        modifier = modifier
            .zIndex(2f)
            .fillMaxWidth()
            .then(
                if (hideFromTalkBack) {
                    Modifier.hideFromTalkBackWhenEnabled()
                } else {
                    Modifier
                },
            ),
    ) {
        if (isMultiHover) {
            NewMultiHoverTextView(
                hoverTimes = hoverTimes,
                differenceValue = differenceValue,
                differencePercent = differencePercent,
                color = multiHoverColor,
                xPosition = hoverTimePosition,
                isDragging = isDragging,
                hoverPriceTextWidth = hoverPriceTextWidth,
                horizontalEdgePadding = horizontalEdgePadding,
            )
        } else {
            SingleTouchChartStats(
                hoverPrice = hoverPrice,
                priceColor = hoverPriceColor,
                hoverTime = hoverTime,
                xPositionTime = hoverTimePosition,
                isDragging = isDragging,
                hoverPriceTextWidth = hoverPriceTextWidth,
                horizontalEdgePadding = horizontalEdgePadding,
            )
        }
    }
}
