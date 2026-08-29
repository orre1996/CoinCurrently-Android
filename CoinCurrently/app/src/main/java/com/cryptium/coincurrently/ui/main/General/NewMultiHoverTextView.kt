package com.cryptium.coincurrently.ui.main.General

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.cryptium.coincurrently.ui.main.Compose.CappedFontScale
import com.cryptium.coincurrently.ui.main.Compose.DynamicTypeSizeCap
import com.cryptium.coincurrently.ui.main.Compose.onSurfaceDimmer
import com.cryptium.coincurrently.ui.main.Compose.primaryBackgroundNoRipple
import com.cryptium.coincurrently.ui.main.Extensions.satoshiBold

@Composable
fun NewMultiHoverTextView(
    hoverTimes: List<String>,
    differenceValue: String,
    differencePercent: String,
    color: Color,
    xPosition: Dp,
    isDragging: Boolean,
    hoverPriceTextWidth: MutableState<Dp>,
    horizontalEdgePadding: Dp = 16.dp,
) {
    val localDensity = LocalDensity.current
    val valueOpacity: Float by animateFloatAsState(
        if (isDragging) 1f else 0f,
        label = "multiHoverOpacity",
    )

    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .zIndex(3f)
            .alpha(valueOpacity)
            .wrapContentHeight(Alignment.CenterVertically)
            .offset(x = xPosition)
            .padding(horizontal = horizontalEdgePadding)
            .onGloballyPositioned { coordinates ->
                hoverPriceTextWidth.value = with(localDensity) { coordinates.size.width.toDp() }
            }
            .primaryBackgroundNoRipple(
                fillWidth = false,
                paddingValues = PaddingValues(
                    vertical = 4.dp,
                    horizontal = 8.dp,
                ),
            ),
    ) {
        CappedFontScale(DynamicTypeSizeCap.XxxLarge) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                MultiHoverColumn(
                    value = differenceValue,
                    time = hoverTimes.firstOrNull().orEmpty(),
                    color = color,
                )

                Text(
                    text = "-",
                    style = satoshiBold(16),
                    color = MaterialTheme.colors.onSurface,
                )

                MultiHoverColumn(
                    value = differencePercent,
                    time = hoverTimes.getOrNull(1).orEmpty(),
                    color = color,
                )
            }
        }
    }
}

@Composable
private fun MultiHoverColumn(
    value: String,
    time: String,
    color: Color,
) {
    androidx.compose.foundation.layout.Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = value,
            style = satoshiBold(16),
            color = color,
        )

        Text(
            text = time,
            style = satoshiBold(12),
            color = MaterialTheme.colors.onSurfaceDimmer,
        )
    }
}
