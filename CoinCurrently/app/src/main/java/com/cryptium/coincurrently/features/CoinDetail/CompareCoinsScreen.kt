package com.cryptium.coincurrently.features.CoinDetail
import com.cryptium.coincurrently.sharedui.modifiers.imageSize

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.invisibleToUser
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import androidx.compose.ui.unit.min
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cryptium.coincurrently.R
import com.cryptium.coincurrently.sharedui.components.AccessibilityLayout
import com.cryptium.coincurrently.sharedui.components.AdaptiveStack
import com.cryptium.coincurrently.sharedui.components.BackPressHandler
import com.cryptium.coincurrently.sharedui.components.Separator
import com.cryptium.coincurrently.sharedui.theme.blue50
import com.cryptium.coincurrently.sharedui.theme.onSurfaceDim
import com.cryptium.coincurrently.sharedui.theme.onSurfaceDimmer
import com.cryptium.coincurrently.sharedui.theme.primary50
import com.cryptium.coincurrently.sharedui.modifiers.shimmeringModifier
import com.cryptium.coincurrently.features.CoinDetail.Sheets.SentimentInfoSheet
import com.cryptium.coincurrently.core.extensions.dpToPx
import com.cryptium.coincurrently.core.extensions.getSelectedCurrency
import com.cryptium.coincurrently.core.extensions.pxToDp
import com.cryptium.coincurrently.core.extensions.satoshiBold
import com.cryptium.coincurrently.sharedui.charts.ChartBackground
import com.cryptium.coincurrently.sharedui.charts.ChartTimeStamps
import com.cryptium.coincurrently.sharedui.charts.ChartTimeframeButtonsView
import com.cryptium.coincurrently.sharedui.charts.LineChart
import com.cryptium.coincurrently.sharedui.charts.ChartAccessibilityOverlay
import com.cryptium.coincurrently.sharedui.charts.chartAccessibilityOverlay
import com.cryptium.coincurrently.sharedui.charts.chartTalkBackSection
import com.cryptium.coincurrently.sharedui.charts.hideFromTalkBackWhenEnabled
import com.cryptium.coincurrently.sharedui.charts.rememberCompareChartHoverUi
import com.cryptium.coincurrently.core.localization.Localization
import com.cryptium.coincurrently.sharedui.navigation.CustomTopAppBar
import com.cryptium.coincurrently.sharedui.components.ImageCase
import com.cryptium.coincurrently.sharedui.loading.ActivityIndicator
import com.cryptium.coincurrently.app.NavigationViewModel
import com.cryptium.coincurrently.sharedui.components.OnLifecycleEvent
import com.cryptium.coincurrently.core.analytics.ChangeCompareCoin
import com.cryptium.coincurrently.core.analytics.CompareCoinChangeSelection
import com.cryptium.coincurrently.core.analytics.AnalyticsManager
import com.cryptium.coincurrently.core.analytics.ScreenLabel
import com.cryptium.coincurrently.core.analytics.SheetLabel
import com.cryptium.coincurrently.core.analytics.SheetOpened
import com.cryptium.coincurrently.features.CoinDetail.Views.Compare.CompareChartHoverView
import com.cryptium.coincurrently.features.CoinDetail.Views.Compare.CompareHistoricDataView
import com.cryptium.coincurrently.features.CoinDetail.Views.Compare.CompareRectangleView
import com.cryptium.coincurrently.features.CoinDetail.Views.Compare.CompareSentimentView
import com.cryptium.coincurrently.features.CoinDetail.Views.Compare.CompareStatisticsView
import com.cryptium.coincurrently.features.CoinDetail.Views.Compare.CompareTouchChartStats
import com.cryptium.coincurrently.features.CoinDetail.Views.General.ChartTypeButtonsView
import com.cryptium.coincurrently.features.CoinDetail.Views.General.CryptoDetailSummaryView
import com.cryptium.coincurrently.core.models.ChartTypes
import com.cryptium.coincurrently.features.CoinDetail.Sheets.SelectCoinSheet
import com.cryptium.coincurrently.features.CoinDetail.CompareCoinsViewModel
import com.cryptium.coincurrently.features.CoinDetail.CompareCoinsViewModelFactory
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.cryptium.coincurrently.core.networking.ResponseModels.CryptoDetailInfoModel
import com.cryptium.coincurrently.core.networking.ResponseModels.PriceVolumeModel
import com.cryptium.coincurrently.core.networking.ResponseModels.TimeCharts
import kotlin.math.floor
import com.cryptium.coincurrently.app.SearchCase
import com.cryptium.coincurrently.app.SearchScreenCase

@Composable
fun CompareCoinsScreen(
    cryptoInfo: CryptoDetailInfoModel?,
    priceVolumeModel: PriceVolumeModel?,
    compareId: String,
    navigationViewModel: NavigationViewModel,
    modifier: Modifier
) {
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp

    val biggestValue = remember { mutableStateOf(DpSize.Zero) }
    var isCoinsViewShowing by remember { mutableStateOf(false) }
    var isSelectingMainCoin by remember { mutableStateOf(false) }
    val showSentimentInfo = remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    val viewModel: CompareCoinsViewModel = viewModel(
        factory = CompareCoinsViewModelFactory(
            cryptoInfo = cryptoInfo,
            priceVolumeModel = priceVolumeModel,
            compareId = compareId
        )
    )

    val hoverUi = rememberCompareChartHoverUi(
        dragPosition = viewModel.dragPosition.value,
        chartSize = viewModel.chartSize.value,
        hoverPriceTextWidth = viewModel.hoverPriceTextWidth.value,
        timeChart = viewModel.selectedTimeFrame,
        prices = viewModel.getPrices(),
        comparePrices = viewModel.getComparePrices(),
        timeStamps = viewModel.getTimestamps(),
        compareTimeStamps = viewModel.getCompareTimestamps(),
    )

    BackHandler(onBack = {
        navigationViewModel.navigateBack()
    })

    LaunchedEffect(Unit) {
        viewModel.setStartData()
        coroutineScope {
            launch { viewModel.refresh(showRefreshIndicator = false) }
            launch { viewModel.getToplist() }
        }
        kotlinx.coroutines.yield()
        viewModel.syncLivePriceUpdates()
    }

    DisposableEffect(Unit) {
        onDispose {
            coroutineScope.launch {
                viewModel.stopLivePriceUpdates()
            }
        }
    }

    OnLifecycleEvent { _, event ->
        when (event) {
            Lifecycle.Event.ON_RESUME -> {
                coroutineScope.launch {
                    kotlinx.coroutines.coroutineScope {
                        launch { viewModel.refresh(showRefreshIndicator = false) }
                        launch { viewModel.getToplist() }
                    }
                }
            }
            Lifecycle.Event.ON_PAUSE -> {
                coroutineScope.launch {
                    viewModel.stopLivePriceUpdates()
                }
            }
            else -> { }
        }
    }

    SwipeRefresh(
        state = rememberSwipeRefreshState(isRefreshing = viewModel.isRefreshing),
        onRefresh = {
            coroutineScope.launch {
                viewModel.refresh(
                    showReload = false,
                )
            }
        }) {
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Column(
                modifier = modifier
            ) {
                CustomTopAppBar(
                    title = Localization.string("Compare"),
                    navigateBack = {
                        navigationViewModel.navigateBack()
                    },
                    actions = listOf({
                        ChangeCoinButton(
                            callback = {
                                logCompareCoinsCoinSearchOpened(
                                    selection = CompareCoinChangeSelection.Compare,
                                    coinName = viewModel.compareCryptoInfo?.name ?: "",
                                )
                                isSelectingMainCoin = false
                                isCoinsViewShowing = true
                            }
                        )
                    })
                )

                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .padding(
                            top = 16.dp,
                            bottom = 32.dp
                        )
                ) {
                    val isHorizontal = AccessibilityLayout.isHorizontal

                    AdaptiveStack(
                        isHorizontal = isHorizontal,
                        spacing = 16.dp,
                        horizontalAlignment = Alignment.Start,
                        verticalAlignment = Alignment.Top,
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                    ) {
                        CryptoDetailSummaryView(
                            name = viewModel.mainCryptoInfo?.name,
                            currentPrice = viewModel.mainCryptoInfo?.market_data?.current_price?.getSelectedCurrency(),
                            prices = viewModel.onlyPrices,
                            imageUrl = viewModel.mainCryptoInfo?.image?.small,
                            selectedTimeframe = viewModel.selectedTimeFrame,
                            shortPrice = true,
                            isWebSocketEnabled = viewModel.isWebSocketEnabled,
                            changeCoinAccessibilityHint = "Double tap to change the coin",
                            changeCoin = {
                                if (viewModel.mainCryptoInfo?.id != null) {
                                    logCompareCoinsCoinSearchOpened(
                                        selection = CompareCoinChangeSelection.Main,
                                        coinName = viewModel.mainCryptoInfo?.name ?: "",
                                    )
                                    isSelectingMainCoin = true
                                    isCoinsViewShowing = true
                                }
                            },
                            modifier = sectionModifier(),
                        )

                        CryptoDetailSummaryView(
                            name = viewModel.compareCryptoInfo?.name,
                            currentPrice = viewModel.compareCryptoInfo?.market_data?.current_price?.getSelectedCurrency(),
                            prices = viewModel.onlyComparePrices,
                            imageUrl = viewModel.compareCryptoInfo?.image?.small,
                            selectedTimeframe = viewModel.selectedTimeFrame,
                            shortPrice = true,
                            isWebSocketEnabled = viewModel.isWebSocketEnabled,
                            changeCoinAccessibilityHint = "Double tap to change the coin",
                            changeCoin = {
                                if (viewModel.compareCryptoInfo?.id != null) {
                                    logCompareCoinsCoinSearchOpened(
                                        selection = CompareCoinChangeSelection.Compare,
                                        coinName = viewModel.compareCryptoInfo?.name ?: "",
                                    )
                                    isSelectingMainCoin = false
                                    isCoinsViewShowing = true
                                }
                            },
                            modifier = sectionModifier(),
                        )
                    }

                    ChartTypeButtonsView(
                        fromScreen = ScreenLabel.CompareCoins,
                        selectedGraphType = viewModel.currentChartType.value,
                        isCandleChartEnabled = false,
                        isInCompareMode = true,
                        imageCase = ImageCase.Fixed(R.drawable.candlestick),
                        toggleCandleChart = { },
                        chartTypeAction = {
                            viewModel.setChartType(it)
                        }
                    )

                    CompareTouchChartStats(
                        mainHoverPrice = hoverUi.hoverPrice,
                        mainSymbol = viewModel.mainCryptoInfo?.symbol,
                        mainPercentChange = hoverUi.mainDifferencePercent,
                        compareHoverPrice = hoverUi.comparedHoverPrice,
                        compareSymbol = viewModel.compareCryptoInfo?.symbol,
                        comparePercentChange = hoverUi.compareDifferencePercent,
                        hoverTime = hoverUi.hoverTime,
                        xPositionTime = hoverUi.hoverTimePosition,
                        isDragging = viewModel.isDragging,
                        hoverPriceTextWidth = viewModel.hoverPriceTextWidth
                    )

                    Column(modifier = Modifier.chartTalkBackSection()) {
                        Box(
                            modifier = Modifier
                                .height((screenHeight.dpToPx() * 0.35).pxToDp())
                                .chartAccessibilityOverlay(
                                    timeStamps = viewModel.timeStamps,
                                    prices = viewModel.getPrices(),
                                    timeChart = viewModel.selectedTimeFrame,
                                    trailingPadding = biggestValue.value.width,
                                    comparePrices = viewModel.getComparePrices(),
                                    mainSeriesName = viewModel.mainCryptoInfo?.name,
                                    compareSeriesName = viewModel.compareCryptoInfo?.name,
                                    showPercentChangeFromStart = true,
                                )
                                .pointerInput(Unit) {
                                    detectDragGesturesAfterLongPress(onDragStart = {
                                        if (!viewModel.isRefreshing) {
                                            viewModel.isDragging = true

                                            val minClamp = min(
                                                it.x.toDp() - 16.dp,
                                                viewModel.chartSize.value.width - 0.001.dp
                                            )
                                            viewModel.dragPosition.value = max(0.dp, minClamp)
                                        }
                                    }, onDrag = { change, _ ->
                                        if (!viewModel.isRefreshing) {
                                            val minClamp = min(
                                                change.position.x.toDp() - 16.dp,
                                                viewModel.chartSize.value.width - 0.001.dp
                                            )
                                            viewModel.dragPosition.value = max(0.dp, minClamp)
                                        }
                                    }, onDragEnd = {
                                        viewModel.isDragging = false
                                    }, onDragCancel = {
                                        viewModel.isDragging = false
                                    })
                                }
                                .padding(horizontal = 16.dp)
                        ) {
                            Box(modifier = Modifier.hideFromTalkBackWhenEnabled()) {
                                ChartBackground(
                                    values = viewModel.graphStats,
                                    biggestValue = biggestValue
                                )
                            }

                            if (viewModel.mainReloading || viewModel.compareReloading) {
                                ActivityIndicator(
                                    fill = true,
                                    modifier = Modifier.hideFromTalkBackWhenEnabled(),
                                )
                            } else {
                                Box(modifier = Modifier.hideFromTalkBackWhenEnabled()) {
                                    MainChart(
                                        prices = viewModel.getPrices(),
                                        chartColor = MaterialTheme.colors.primary50,
                                        dragIndex = if (viewModel.isDragging) hoverUi.dragIndex else null,
                                        chartSize = viewModel.chartSize,
                                        biggestValue = biggestValue.value,
                                        isDragging = viewModel.isDragging,
                                        showLivePriceIndicator = viewModel.isWebSocketEnabled && viewModel.currentChartType.value == ChartTypes.PRICE,
                                    )

                                    CompareChart(
                                        prices = viewModel.getComparePrices(),
                                        chartColor = MaterialTheme.colors.blue50,
                                        dragIndex = if (viewModel.isDragging) hoverUi.dragIndex else null,
                                        biggestValue = biggestValue.value,
                                        isDragging = viewModel.isDragging,
                                        showLivePriceIndicator = viewModel.isWebSocketEnabled && viewModel.currentChartType.value == ChartTypes.PRICE,
                                    )

                                    CompareChartHoverView(
                                        mainPosition = hoverUi.dragPoint,
                                        hideMainMark = viewModel.getPrices(dropLastTwo = true).getOrNull(hoverUi.dragIndex) == -1.0,
                                        comparePosition = hoverUi.compareDragPoint,
                                        hideCompareMark = viewModel.getComparePrices(dropLastTwo = true).getOrNull(hoverUi.dragIndex) == -1.0,
                                        isDragging = viewModel.isDragging,
                                        topPadding = biggestValue.value.height / 2,
                                    )
                                }
                            }

                            ChartAccessibilityOverlay(
                                timeStamps = viewModel.timeStamps,
                                prices = viewModel.getPrices(),
                                timeChart = viewModel.selectedTimeFrame,
                                trailingPadding = biggestValue.value.width,
                                comparePrices = viewModel.getComparePrices(),
                                mainSeriesName = viewModel.mainCryptoInfo?.name,
                                compareSeriesName = viewModel.compareCryptoInfo?.name,
                                showPercentChangeFromStart = true,
                            )
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
                                .hideFromTalkBackWhenEnabled()
                        )

                        ChartTimeframeButtonsView(
                            TimeCharts.getButtons(from = TimeCharts.anHour, to = TimeCharts.max),
                            viewModel.selectedTimeFrame,
                            fromScreen = ScreenLabel.CompareCoins,
                        ) {
                            if (!viewModel.isDragging) {
                                coroutineScope.launch {
                                    viewModel.refresh(
                                        showReload = true,
                                        showRefreshIndicator = false,
                                        newDays = it.days,
                                        newTimeChart = it.timeChart
                                    )
                                }
                            }
                        }
                    }

                    CoinSymbolsView(
                        mainSymbol = viewModel.mainCryptoInfo?.symbol,
                        compareSymbol = viewModel.compareCryptoInfo?.symbol
                    )

                    Separator()

                    CompareStatisticsView(
                        mainCryptoInfo = viewModel.mainCryptoInfo,
                        compareCryptoInfo = viewModel.compareCryptoInfo,
                        mainPrices = viewModel.getPrices(dropLastTwo = true).filter { it != -1.0 },
                        comparePrices = viewModel.getComparePrices(dropLastTwo = true).filter { it != -1.0 },
                        selectedTimeframe = viewModel.selectedTimeFrame,
                        modifier = Modifier.semantics { traversalIndex = 2f },
                    )

                    CompareHistoricDataView(
                        mainCryptoInfo = viewModel.mainCryptoInfo,
                        compareCryptoInfo = viewModel.compareCryptoInfo,
                        modifier = Modifier.semantics { traversalIndex = 2f },
                    )

                    CompareSentimentView(
                        mainCryptoInfo = viewModel.mainCryptoInfo,
                        compareCryptoInfo = viewModel.compareCryptoInfo,
                        showSentimentInfo = showSentimentInfo,
                        modifier = Modifier.semantics { traversalIndex = 2f },
                    )
                }
            }

            if (isCoinsViewShowing) {
                SelectCoinSheet(
                    searchScreenCase = SearchScreenCase.Search(
                        list = SearchCase.OnlyCoins
                    ),
                    fromScreen = ScreenLabel.CompareCoins,
                    highlightId = if(isSelectingMainCoin) viewModel.mainCryptoInfo?.id else viewModel.compareCryptoInfo?.id,
                    hideId = if(isSelectingMainCoin) viewModel.compareCryptoInfo?.id else viewModel.mainCryptoInfo?.id,
                    selectedCoin = { coin ->
                        val id = coin?.id ?: return@SelectCoinSheet

                        coroutineScope.launch {
                            viewModel.refresh(
                                forceMainId = if (isSelectingMainCoin) id else null,
                                forceCompareId = if (isSelectingMainCoin) null else id,
                                showRefreshIndicator = false
                            )
                        }
                    },
                    close = {
                        isCoinsViewShowing = false
                    }
                )
            }

            if (showSentimentInfo.value) {
                SentimentInfoSheet(
                    close = {
                        showSentimentInfo.value = false
                    }
                )
            }
        }
    }
}

private fun logCompareCoinsCoinSearchOpened(
    selection: CompareCoinChangeSelection,
    coinName: String,
) {
    AnalyticsManager.log(
        ChangeCompareCoin(
            fromScreen = ScreenLabel.CompareCoins,
            selection = selection,
            coinName = coinName,
        ),
    )
    AnalyticsManager.log(
        SheetOpened(fromScreen = ScreenLabel.CompareCoins, sheet = SheetLabel.CoinSearch),
    )
}
