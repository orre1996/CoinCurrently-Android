package com.cryptium.coincurrently.ui.main.General

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
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
import com.cryptium.coincurrently.ui.main.CryptoDetail.Compose.PriceChart.CandleChartHover
import com.cryptium.coincurrently.ui.main.Extensions.dpToPx
import com.cryptium.coincurrently.ui.main.Extensions.getSelectedCurrency
import com.cryptium.coincurrently.ui.main.General.Components.Chart.HoverLineView
import com.cryptium.coincurrently.ui.main.General.Components.Chart.LineChart
import com.cryptium.coincurrently.ui.main.General.Components.Loading.ActivityIndicator
import com.cryptium.coincurrently.ui.main.General.Utilities.Point
import com.cryptium.coincurrently.ui.main.MarketTab.CryptoDetail.Components.General.CryptoDetailSummaryView
import com.cryptium.coincurrently.ui.main.MarketTab.CryptoDetail.ViewModel.CryptoDetailViewModel
import networking.ResponseModels.Candle
import kotlin.math.floor

@Composable
fun CryptoDetailShareView(
    viewModel: CryptoDetailViewModel
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
                name = viewModel.cryptoInfo?.name,
                currentPrice = viewModel.cryptoInfo?.market_data?.current_price?.getSelectedCurrency(),
                imageUrl = viewModel.cryptoInfo?.image?.small,
                imageBitmap = viewModel.cryptoDetailSmallImageBitmap,
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
                    if (viewModel.isCandleChartEnabled) {
                        CandleChart(
                            candleSticks = viewModel.candleSticks,
                            isDragging = viewModel.isDragging,
                            hoverXPosition = viewModel.candleXPosition.dpToPx(),
                            pointCandle = viewModel.pointCandle,
                            chartSize = viewModel.chartSize,
                            biggestValue = biggestValue.value,
                        )
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
            }

            ChartTimeStamps(
                currentTimeChart = viewModel.selectedTimeFrame,
                timeStamps = if (viewModel.isCandleChartEnabled) viewModel.candleTimestamps else viewModel.timeStamps,
                modifier = Modifier
                    .offset(y = -biggestValue.value.height / 2)
                    .padding(
                        start = 16.dp,
                        end = 16.dp + biggestValue.value.width,
                        bottom =  biggestValue.value.height / 2,
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

@Composable
private fun CandleChart(
    candleSticks: List<Candle>,
    isDragging: Boolean,
    hoverXPosition: Float,
    pointCandle: Dp,
    chartSize: MutableState<DpSize>,
    biggestValue: DpSize
) {
    val localDensity = LocalDensity.current

    com.cryptium.coincurrently.ui.main.CryptoDetail.Compose.PriceChart.CandleChart(
        candleSticks,
        modifier = Modifier
            .padding(
                PaddingValues(
                    top = biggestValue.height / 2,
                    bottom = biggestValue.height / 2,
                    end = biggestValue.width
                )
            )
            .onGloballyPositioned { coordinates ->
                val sizePx = coordinates.size
                chartSize.value = with(localDensity) {
                    DpSize(
                        sizePx.width.toDp(),
                        sizePx.height.toDp()
                    )
                }
            }
    )

    CandleChartHover(
        isDragging = isDragging,
        xPos = hoverXPosition,
        yPos = pointCandle,
        paddings = PaddingValues(
            top = biggestValue.height / 2,
            bottom = biggestValue.height / 2,
            end = biggestValue.width
        ),
        topPadding =  biggestValue.height / 2
    )
}

fun CryptoDetailShareView(
    activity: Activity,
    context: Context,
    viewModel: CryptoDetailViewModel
) {
    val darkTheme =
        (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
            Configuration.UI_MODE_NIGHT_YES

    renderComposableToBitmap(
        activity = activity,
        composable = {
            AppTheme(darkTheme = darkTheme) {
                CryptoDetailShareView(
                    viewModel = viewModel
                )
            }
        },
        onBitmapReady = { bitmap ->
            val sym = viewModel.cryptoInfo?.symbol?.trim()?.uppercase()
            val label = if (!sym.isNullOrEmpty()) "\$$sym" else "\$COIN"
            shareBitmap(context, bitmap, "CryptoDetail_share", trackedShareLabel = label)
        }
    )
}
