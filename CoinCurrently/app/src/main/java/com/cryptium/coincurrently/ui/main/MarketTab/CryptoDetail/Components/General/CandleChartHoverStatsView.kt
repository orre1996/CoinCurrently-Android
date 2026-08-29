package com.cryptium.coincurrently.ui.main.MarketTab.CryptoDetail.Components.General

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.cryptium.coincurrently.ui.main.Compose.onSurfaceDimmer
import com.cryptium.coincurrently.ui.main.Compose.primaryBackgroundNoRipple
import com.cryptium.coincurrently.ui.main.Compose.CappedFontScale
import com.cryptium.coincurrently.ui.main.Compose.DynamicTypeSizeCap
import com.cryptium.coincurrently.ui.main.Extensions.satoshiBold

@Composable
fun CandleChartHoverStatsView(
    currentCandleChart: List<String>?,
    xPosition: Dp,
    isDragging: Boolean,
    hoverCandlePriceTextWidth: MutableState<Dp>,
    horizontalEdgePadding: Dp = 16.dp,
    modifier: Modifier = Modifier,
) {
    val localDensity = LocalDensity.current
    val valueOpacity: Float by animateFloatAsState(
        if (isDragging) {
            1f
        } else {
            0f
        }, label = ""
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .zIndex(2f)
            .alpha(valueOpacity)
            .wrapContentHeight(Alignment.CenterVertically)
            .offset(xPosition)
            .padding(horizontal = horizontalEdgePadding)
            .onGloballyPositioned { coordinates ->
                hoverCandlePriceTextWidth.value = with(localDensity) { coordinates.size.width.toDp() }
            }
            .primaryBackgroundNoRipple(
                fillWidth = false,
                paddingValues = PaddingValues(
                    vertical = 4.dp,
                    horizontal = 8.dp
                )
            )
    ) {
        CappedFontScale(DynamicTypeSizeCap.Large) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Open",
                        style = satoshiBold(12),
                        color = MaterialTheme.colors.onSurfaceDimmer
                    )

                    Text(
                        text = currentCandleChart?.getOrNull(1) ?: "",
                        style = satoshiBold(12),
                        color = MaterialTheme.colors.onSurface
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Close",
                        style = satoshiBold(12),
                        color = MaterialTheme.colors.onSurfaceDimmer
                    )

                    Text(
                        text = currentCandleChart?.getOrNull(3) ?: "",
                        style = satoshiBold(12),
                        color = MaterialTheme.colors.onSurface
                    )
                }
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalAlignment = Alignment.End
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "High",
                        style = satoshiBold(12),
                        color = MaterialTheme.colors.onSurfaceDimmer
                    )

                    Text(
                        text = currentCandleChart?.getOrNull(2) ?: "",
                        style = satoshiBold(12),
                        color = MaterialTheme.colors.onSurface
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Low",
                        style = satoshiBold(12),
                        color = MaterialTheme.colors.onSurfaceDimmer
                    )

                    Text(
                        text = currentCandleChart?.getOrNull(4) ?: "",
                        style = satoshiBold(12),
                        color = MaterialTheme.colors.onSurface
                    )
                }
            }
        }

        Text(
            text = currentCandleChart?.getOrNull(0) ?: "",
            style = satoshiBold(12),
            color = MaterialTheme.colors.onSurfaceDimmer
        )
        }
    }
}