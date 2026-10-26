package com.cryptium.coincurrently.features.CoinDetail.Views.Compare
import com.cryptium.coincurrently.sharedui.modifiers.imageSize

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.cryptium.coincurrently.R
import com.cryptium.coincurrently.core.localization.Localization
import com.cryptium.coincurrently.sharedui.theme.blue50
import com.cryptium.coincurrently.sharedui.theme.onSurfaceDim
import com.cryptium.coincurrently.sharedui.theme.onSurfaceDimmer
import com.cryptium.coincurrently.sharedui.theme.primary50
import com.cryptium.coincurrently.sharedui.theme.primaryBackgroundNoRipple
import com.cryptium.coincurrently.sharedui.modifiers.CappedFontScale
import com.cryptium.coincurrently.sharedui.modifiers.DynamicTypeSizeCap
import com.cryptium.coincurrently.core.extensions.satoshiBold
import com.cryptium.coincurrently.sharedui.charts.hideFromTalkBackWhenEnabled
import com.cryptium.coincurrently.sharedui.components.PriceChangePercentView
import com.cryptium.coincurrently.sharedui.loading.ChangeSize

@Composable
fun CompareTouchChartStats(
    mainHoverPrice: String,
    mainSymbol: String?,
    mainPercentChange: Double,
    compareHoverPrice: String,
    compareSymbol: String?,
    comparePercentChange: Double,
    hoverTime: String,
    xPositionTime: Dp,
    isDragging: Boolean,
    hoverPriceTextWidth: MutableState<Dp>
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
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
            .zIndex(2f)
            .alpha(valueOpacity)
            .then(
                if (!isDragging) {
                    Modifier.hideFromTalkBackWhenEnabled()
                } else {
                    Modifier
                }
            )
            .wrapContentHeight(Alignment.CenterVertically)
            .offset(xPositionTime)
            .padding(horizontal = 16.dp)
            .onGloballyPositioned { coordinates ->
                hoverPriceTextWidth.value = with(localDensity) { coordinates.size.width.toDp() }
            }
            .primaryBackgroundNoRipple(
                fillWidth = false,
                paddingValues = PaddingValues(
                    vertical = 4.dp,
                    horizontal = 8.dp
                )
            )
    ) {
        CappedFontScale(DynamicTypeSizeCap.XxxLarge) {
        Column(
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                CompareRectangleView(color = MaterialTheme.colors.primary50)

                Text(
                    text = mainSymbol?.uppercase() ?: "",
                    style = satoshiBold(12),
                    color = MaterialTheme.colors.onSurfaceDimmer
                )

                Text(
                    text = if (mainHoverPrice == "N/A") Localization.string("N/A") else mainHoverPrice,
                    style = satoshiBold(12),
                    color = MaterialTheme.colors.onSurface
                )

                if (mainHoverPrice == "N/A") {
                    NAView()
                } else {
                    PriceChangePercentView(
                        change = mainPercentChange,
                        size = ChangeSize.SMALL,
                        isDownloading = false
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                CompareRectangleView(color = MaterialTheme.colors.blue50)

                Text(
                    text = compareSymbol?.uppercase() ?: "",
                    style = satoshiBold(12),
                    color = MaterialTheme.colors.onSurfaceDimmer
                )

                Text(
                    text = if (compareHoverPrice == "N/A") Localization.string("N/A") else compareHoverPrice,
                    style = satoshiBold(12),
                    color = MaterialTheme.colors.onSurface
                )

                if (compareHoverPrice == "N/A") {
                    NAView()
                } else {
                    PriceChangePercentView(
                        change = comparePercentChange,
                        size = ChangeSize.SMALL,
                        isDownloading = false
                    )
                }
            }
        }

        Text(
            text = hoverTime,
            style = satoshiBold(12),
            color = MaterialTheme.colors.onSurfaceDimmer
        )
        }
    }
}

@Composable
private fun NAView() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Image(
            painter = painterResource(R.drawable.trend_still),
            contentDescription = null,
            colorFilter = ColorFilter.tint(MaterialTheme.colors.onSurfaceDim),
            modifier = Modifier
                .imageSize(14.dp)
        )

        Text(
            text = Localization.string("N/A"),
            style = satoshiBold(14),
            color = MaterialTheme.colors.onSurfaceDim
        )
    }
}