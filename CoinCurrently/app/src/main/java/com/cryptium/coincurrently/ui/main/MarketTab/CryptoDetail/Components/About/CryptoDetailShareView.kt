package com.cryptium.coincurrently.ui.main.MarketTab.CryptoDetail.Components.About

import android.graphics.Picture
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.cryptium.coincurrently.R
import com.cryptium.coincurrently.ui.main.Compose.onSurfaceDim
import com.cryptium.coincurrently.ui.main.Extensions.getSelectedCurrency
import com.cryptium.coincurrently.ui.main.General.ChartBackground
import com.cryptium.coincurrently.ui.main.General.ChartTimeStamps
import com.cryptium.coincurrently.ui.main.General.Components.Chart.LineChart
import com.cryptium.coincurrently.ui.main.General.Components.ImageCase
import com.cryptium.coincurrently.ui.main.General.Components.Loading.ActivityIndicator
import com.cryptium.coincurrently.ui.main.General.SingleTouchChartStats
import com.cryptium.coincurrently.ui.main.analytics.ScreenLabel
import com.cryptium.coincurrently.ui.main.MarketTab.CryptoDetail.Components.General.ChartButtonsView
import com.cryptium.coincurrently.ui.main.MarketTab.CryptoDetail.Components.General.CryptoDetailSummaryView
import com.cryptium.coincurrently.ui.main.MarketTab.CryptoDetail.ViewModel.CryptoDetailViewModel
import kotlinx.coroutines.launch
import networking.ResponseModels.Candle

@Composable
fun CryptoDetailShareView(
    viewModel: CryptoDetailViewModel
) {
    val biggestValue = remember { mutableStateOf(DpSize.Zero) }
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .background(MaterialTheme.colors.surface)
            .verticalScroll(rememberScrollState())
            .padding(bottom = 32.dp)
    ) {
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

        ChartButtonsView(
            fromScreen = ScreenLabel.CryptoDetail,
            selectedGraphType = viewModel.currentChartType.value,
            isCandleChartEnabled = viewModel.isCandleChartEnabled,
            isInCompareMode = false,
            imageCase = ImageCase.Fixed(R.drawable.candlestick),
            chartTypeAction = {
                viewModel.setChartType(it)
            },
            toggleCandleChart = {
                coroutineScope.launch {
                    viewModel.toggleChart()
                }
            }
        )

        if (!viewModel.candleChartEnabled) {
            SingleTouchChartStats(
                hoverPrice = viewModel.hoverPrice,
                priceColor = MaterialTheme.colors.onSurface,
                hoverTime = viewModel.hoverTime,
                xPositionTime = viewModel.hoverTimePosition,
                isDragging = viewModel.isDragging,
                viewModel.hoverPriceTextWidth
            )
        }

        Box(
            modifier = Modifier
                .size(width = 380.dp, height = 370.dp)
                .padding(horizontal = 16.dp)
        ) {
            ChartBackground(
                values = viewModel.mainGraphStats,
                biggestValue = biggestValue
            )

            if (!viewModel.mainReloading) {
                if (viewModel.isCandleChartEnabled) {
                    CandleChart(
                        candleSticks = viewModel.candleSticks,
                        chartSize = viewModel.chartSize,
                        biggestValue = biggestValue.value,
                    )
                } else {
                    MainChart(
                        prices = viewModel.prices,
                        chartColor = viewModel.mainChartColor,
                        dragIndex = if (viewModel.isDragging) viewModel.dragIndex else null,
                        chartSize = viewModel.chartSize,
                        isInCompareMode = false,
                        biggestValue = biggestValue.value
                    )
                }
            } else {
                ActivityIndicator()
            }
        }

        ChartTimeStamps(
            viewModel.selectedTimeFrame,
            viewModel.timeStamps,
            modifier = Modifier
                .offset(y = -biggestValue.value.height / 2)
                .padding(
                    start = 16.dp,
                    end = 16.dp + biggestValue.value.width,
                    bottom = 16.dp - biggestValue.value.height / 2,
                )
        )
    }
}


@Composable
private fun MainChart(
    prices: List<Double>,
    chartColor: Color?,
    dragIndex: Int?,
    isInCompareMode: Boolean,
    chartSize: MutableState<DpSize>,
    biggestValue: DpSize
) {
    val localDensity = LocalDensity.current

    LineChart(
        prices = prices,
        color = chartColor ?: MaterialTheme.colors.onSurfaceDim,
        dragIndex = dragIndex,
        showFill = !isInCompareMode,
        paddings = PaddingValues(
            top = biggestValue.height / 2,
            bottom = biggestValue.height / 2,
            end = biggestValue.width
        ),
        modifier = Modifier
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
}

@Composable
private fun CandleChart(
    candleSticks: List<Candle>,
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
}