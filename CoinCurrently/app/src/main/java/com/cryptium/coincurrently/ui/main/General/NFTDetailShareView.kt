package com.cryptium.coincurrently.ui.main.General

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.cryptium.coincurrently.ui.main.Compose.AppTheme
import com.cryptium.coincurrently.ui.main.Compose.onSurfaceDim
import com.cryptium.coincurrently.ui.main.CryptoDetail.Compose.PriceChart.CandleChart
import com.cryptium.coincurrently.ui.main.CryptoDetail.Compose.PriceChart.CandleChartHover
import com.cryptium.coincurrently.ui.main.Extensions.dpToPx
import com.cryptium.coincurrently.ui.main.Extensions.getSelectedCurrency
import com.cryptium.coincurrently.ui.main.General.Components.Chart.HoverLineView
import com.cryptium.coincurrently.ui.main.General.Components.Chart.LineChart
import com.cryptium.coincurrently.ui.main.General.Components.Loading.ActivityIndicator
import com.cryptium.coincurrently.ui.main.General.Utilities.Point
import com.cryptium.coincurrently.ui.main.MarketTab.CryptoDetail.Components.General.CryptoDetailSummaryView
import com.cryptium.coincurrently.ui.main.MarketTab.NFT.ViewModels.NFTDetailViewModel
import networking.ResponseModels.Candle
import kotlin.math.floor

@Composable
fun NFTDetailShareView(
    viewModel: NFTDetailViewModel
) {
    val biggestValue = remember { mutableStateOf(DpSize.Zero) }

    Box(
        modifier = Modifier
            .background(MaterialTheme.colors.surface)
            .padding(
                top = 16.dp,
                start = 16.dp,
                end = 16.dp
            ),
    ) {
        Column {
            CryptoDetailSummaryView(
                name = viewModel.nftDetailInfo?.name,
                currentPrice = viewModel.onlyPrices.lastOrNull(),
                imageUrl = viewModel.nftDetailInfo?.image?.small,
                imageBitmap = viewModel.nftDetailSmallImageBitmap,
                prices = viewModel.onlyPrices,
                selectedTimeframe = viewModel.selectedTimeFrame,
                shortPrice = false,
                changeCoin = { },
                modifier = Modifier
            )

            Box(
                modifier = Modifier
                    .padding(top = 16.dp)
                    .height(250.dp)
            ) {
                ChartBackground(
                    values = viewModel.mainGraphStats,
                    biggestValue = biggestValue
                )

                if (viewModel.mainReloading) {
                    ActivityIndicator()
                } else {
                    MainChart(
                        prices = viewModel.onlyPrices,
                        chartColor = viewModel.mainChartColor,
                        dragIndex = null,
                        isDragging = false,
                        dragPoint = Point(0.dp, 0.dp),
                        chartSize = viewModel.chartSize,
                        biggestValue = biggestValue.value,
                    )
                }
            }

            ChartTimeStamps(
                currentTimeChart = viewModel.selectedTimeFrame,
                timeStamps =  viewModel.timeStamps,
                modifier = Modifier
                    .offset(y = -biggestValue.value.height / 2)
                    .padding(
                        start = 16.dp,
                        end = 16.dp + biggestValue.value.width,
                        bottom = biggestValue.value.height / 2,
                    )
            )
        }
    }
}

@Composable
private fun MainChart(
    prices: List<Double>,
    chartColor: Color?,
    dragIndex: Int?,
    isDragging: Boolean,
    dragPoint: Point,
    chartSize: MutableState<DpSize>,
    biggestValue: DpSize,
) {
    val localDensity = LocalDensity.current

    LineChart(
        prices = prices,
        color = chartColor ?: MaterialTheme.colors.onSurfaceDim,
        dragIndex = dragIndex,
        showFill = true,
        paddings = PaddingValues(
            top = biggestValue.height / 2,
            bottom = biggestValue.height / 2,
            end = biggestValue.width
        ),
        modifier = Modifier
            .onGloballyPositioned { coordinates ->
                val sizePx = coordinates.size
                val width = floor(sizePx.width.toFloat())
                val height = floor(sizePx.height.toFloat())

                chartSize.value = with(localDensity) {
                    DpSize(
                        width.toDp(),
                        height.toDp()
                    )
                }
            }
    )

    HoverLineView(
        position = dragPoint,
        isDragging = isDragging,
        color = chartColor ?: MaterialTheme.colors.onSurfaceDim,
        topPadding = biggestValue.height / 2
    )
}

fun NFTDetailShareView(
    activity: Activity,
    context: Context,
    viewModel: NFTDetailViewModel,
    shareChooserLauncher: ActivityResultLauncher<Intent>,
) {
    val darkTheme =
        (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
            Configuration.UI_MODE_NIGHT_YES

    renderComposableToBitmap(
        activity = activity,
        composable = {
            AppTheme(darkTheme = darkTheme) {
                NFTDetailShareView(
                    viewModel = viewModel
                )
            }
        },
        onBitmapReady = { bitmap ->
            val label = viewModel.nftDetailInfo?.name?.trim()?.takeIf { it.isNotEmpty() } ?: "NFTs"
            shareBitmap(
                context,
                bitmap,
                "NFTDetail_share",
                chooserLauncher = shareChooserLauncher,
                trackedShareLabel = label,
                trackedShareOthersWord = "NFTs",
            )
        }
    )
}
