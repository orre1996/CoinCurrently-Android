package com.cryptium.coincurrently.ui.main.MarketTab.CryptoDetail.Components.Compare

import com.cryptium.coincurrently.ui.main.Compose.imageSize

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
import com.cryptium.coincurrently.ui.main.Compose.blue50
import com.cryptium.coincurrently.ui.main.Compose.onSurfaceDim
import com.cryptium.coincurrently.ui.main.Compose.onSurfaceDimmer
import com.cryptium.coincurrently.ui.main.Compose.primary50
import com.cryptium.coincurrently.ui.main.Compose.primaryBackgroundNoRipple
import com.cryptium.coincurrently.ui.main.Compose.CappedFontScale
import com.cryptium.coincurrently.ui.main.Compose.DynamicTypeSizeCap
import com.cryptium.coincurrently.ui.main.Extensions.satoshiBold
import com.cryptium.coincurrently.ui.main.General.Components.Chart.hideFromTalkBackWhenEnabled
import com.cryptium.coincurrently.ui.main.General.Components.NewPriceChangePercentView
import com.cryptium.coincurrently.ui.main.General.Loading.ChangeSize
import networking.ResponseModels.getSentimentColor
import networking.ResponseModels.getSentimentSign

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
                    text = mainHoverPrice,
                    style = satoshiBold(12),
                    color = MaterialTheme.colors.onSurface
                )

                if (mainHoverPrice == "N/A") {
                    NAView()
                } else {
                    NewPriceChangePercentView(
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
                    text = compareHoverPrice,
                    style = satoshiBold(12),
                    color = MaterialTheme.colors.onSurface
                )

                if (compareHoverPrice == "N/A") {
                    NAView()
                } else {
                    NewPriceChangePercentView(
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
            text = "N/A",
            style = satoshiBold(14),
            color = MaterialTheme.colors.onSurfaceDim
        )
    }
}