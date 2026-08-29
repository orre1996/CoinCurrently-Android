package com.cryptium.coincurrently.ui.main.MarketTab.CryptoDetail.Screens

import com.cryptium.coincurrently.ui.main.Compose.imageSize

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
import com.cryptium.coincurrently.ui.main.Compose.AccessibilityLayout
import com.cryptium.coincurrently.ui.main.Compose.AdaptiveStack
import com.cryptium.coincurrently.ui.main.Compose.BackPressHandler
import com.cryptium.coincurrently.ui.main.Compose.Separator
import com.cryptium.coincurrently.ui.main.Compose.blue50
import com.cryptium.coincurrently.ui.main.Compose.onSurfaceDim
import com.cryptium.coincurrently.ui.main.Compose.onSurfaceDimmer
import com.cryptium.coincurrently.ui.main.Compose.primary50
import com.cryptium.coincurrently.ui.main.Compose.shimmeringModifier
import com.cryptium.coincurrently.ui.main.CryptoDetail.Compose.Popups.SentimentInfoSheet
import com.cryptium.coincurrently.ui.main.Extensions.dpToPx
import com.cryptium.coincurrently.ui.main.Extensions.getSelectedCurrency
import com.cryptium.coincurrently.ui.main.Extensions.pxToDp
import com.cryptium.coincurrently.ui.main.Extensions.satoshiBold
import com.cryptium.coincurrently.ui.main.General.ChartBackground
import com.cryptium.coincurrently.ui.main.General.ChartTimeStamps
import com.cryptium.coincurrently.ui.main.General.ChartTimeframeButtonsView
import com.cryptium.coincurrently.ui.main.General.Components.Chart.LineChart
import com.cryptium.coincurrently.ui.main.General.Components.Chart.ChartAccessibilityOverlay
import com.cryptium.coincurrently.ui.main.General.Components.Chart.chartAccessibilityOverlay
import com.cryptium.coincurrently.ui.main.General.Components.Chart.chartTalkBackSection
import com.cryptium.coincurrently.ui.main.General.Components.Chart.hideFromTalkBackWhenEnabled
import com.cryptium.coincurrently.ui.main.General.Components.CustomTopAppBar
import com.cryptium.coincurrently.ui.main.General.Components.ImageCase
import com.cryptium.coincurrently.ui.main.General.Components.Loading.ActivityIndicator
import com.cryptium.coincurrently.ui.main.General.NavigationViewModel
import com.cryptium.coincurrently.ui.main.General.OnLifecycleEvent
import com.cryptium.coincurrently.ui.main.analytics.ChangeCompareCoin
import com.cryptium.coincurrently.ui.main.analytics.CompareCoinChangeSelection
import com.cryptium.coincurrently.ui.main.analytics.AnalyticsManager
import com.cryptium.coincurrently.ui.main.analytics.ScreenLabel
import com.cryptium.coincurrently.ui.main.analytics.SheetLabel
import com.cryptium.coincurrently.ui.main.analytics.SheetOpened
import com.cryptium.coincurrently.ui.main.MarketTab.CryptoDetail.Components.Compare.CompareChartHoverView
import com.cryptium.coincurrently.ui.main.MarketTab.CryptoDetail.Components.Compare.CompareHistoricDataView
import com.cryptium.coincurrently.ui.main.MarketTab.CryptoDetail.Components.Compare.CompareRectangleView
import com.cryptium.coincurrently.ui.main.MarketTab.CryptoDetail.Components.Compare.CompareSentimentView
import com.cryptium.coincurrently.ui.main.MarketTab.CryptoDetail.Components.Compare.CompareStatisticsView
import com.cryptium.coincurrently.ui.main.MarketTab.CryptoDetail.Components.Compare.CompareTouchChartStats
import com.cryptium.coincurrently.ui.main.MarketTab.CryptoDetail.Components.General.ChartButtonsView
import com.cryptium.coincurrently.ui.main.MarketTab.CryptoDetail.Components.General.CryptoDetailSummaryView
import com.cryptium.coincurrently.ui.main.MarketTab.CryptoDetail.Enums.ChartTypes
import com.cryptium.coincurrently.ui.main.MarketTab.CryptoDetail.Popups.SelectCoinSheet
import com.cryptium.coincurrently.ui.main.MarketTab.CryptoDetail.ViewModel.CompareCoinsViewModel
import com.cryptium.coincurrently.ui.main.MarketTab.CryptoDetail.ViewModel.CompareCoinsViewModelFactory
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import networking.ResponseModels.CryptoDetailInfoModel
import networking.ResponseModels.PriceVolumeModel
import networking.ResponseModels.TimeCharts
import kotlin.math.floor

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

    BackHandler(onBack = {
        navigationViewModel.navigateBack()
    })

    LaunchedEffect(Unit) {
        com.cryptium.coincurrently.ui.main.SettingsTab.Achievements.Managers.AchievementManager.increment(
            com.cryptium.coincurrently.ui.main.SettingsTab.Achievements.Models.AchievementCounterKey.compareSessions
        )
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
                    title = "Compare",
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

                    ChartButtonsView(
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
                        mainHoverPrice = viewModel.hoverPrice,
                        mainSymbol = viewModel.mainCryptoInfo?.symbol,
                        mainPercentChange = viewModel.mainDifferencePercent,
                        compareHoverPrice = viewModel.comparedHoverPrice,
                        compareSymbol = viewModel.compareCryptoInfo?.symbol,
                        comparePercentChange = viewModel.compareDifferencePercent,
                        hoverTime = viewModel.hoverTime,
                        xPositionTime = viewModel.hoverTimePosition,
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
                                        dragIndex = if (viewModel.isDragging) viewModel.dragIndex else null,
                                        chartSize = viewModel.chartSize,
                                        biggestValue = biggestValue.value,
                                        isDragging = viewModel.isDragging,
                                        showLivePriceIndicator = viewModel.isWebSocketEnabled && viewModel.currentChartType.value == ChartTypes.PRICE,
                                    )

                                    CompareChart(
                                        prices = viewModel.getComparePrices(),
                                        chartColor = MaterialTheme.colors.blue50,
                                        dragIndex = if (viewModel.isDragging) viewModel.dragIndex else null,
                                        biggestValue = biggestValue.value,
                                        isDragging = viewModel.isDragging,
                                        showLivePriceIndicator = viewModel.isWebSocketEnabled && viewModel.currentChartType.value == ChartTypes.PRICE,
                                    )

                                    CompareChartHoverView(
                                        mainPosition = viewModel.dragPoint,
                                        hideMainMark = viewModel.getPrices(dropLastTwo = true).getOrNull(viewModel.dragIndex) == -1.0,
                                        comparePosition = viewModel.compareDragPoint,
                                        hideCompareMark = viewModel.getComparePrices(dropLastTwo = true).getOrNull(viewModel.dragIndex) == -1.0,
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

@Composable
private fun ChangeCoinButton(
    callback: () -> Unit
) {
    IconButton(
        onClick = {
            callback.invoke()
        },
        modifier = Modifier.semantics {
            contentDescription = "Change compare coin"
        },
    ) {
        Image(
            painter = painterResource( R.drawable.change_coin),
            colorFilter = ColorFilter.tint(MaterialTheme.colors.onSurface),
            contentDescription = null,
            modifier = Modifier
                .padding(8.dp)
                .imageSize(24.dp)
        )
    }
}

@Composable
private fun CoinSymbolsView(
    mainSymbol: String?,
    compareSymbol: String?
) {
    val talkBackEnabled = AccessibilityLayout.isTalkBackEnabled
    val hideFromTalkBack = if (talkBackEnabled) {
        Modifier.clearAndSetSemantics { invisibleToUser() }
    } else {
        Modifier
    }

    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = 16.dp,
                top = 32.dp,
                bottom = 32.dp,
                end = 16.dp
            )
            .then(hideFromTalkBack)
    ) {
        CompareRectangleView(
            color = MaterialTheme.colors.primary50
        )

        Text(
            text = mainSymbol?.uppercase() ?: "0000",
            style = satoshiBold(16),
            color = MaterialTheme.colors.onSurfaceDimmer,
            modifier = Modifier
                .padding(end = 24.dp)
                .shimmeringModifier(mainSymbol == null)
                .then(hideFromTalkBack)
        )

        CompareRectangleView(
            color = MaterialTheme.colors.blue50
        )

        Text(
            text = compareSymbol?.uppercase() ?: "0000",
            style = satoshiBold(16),
            color = MaterialTheme.colors.onSurfaceDimmer,
            modifier = Modifier
                .padding(end = 24.dp)
                .shimmeringModifier(compareSymbol == null)
                .then(hideFromTalkBack)
        )
    }
}

@Composable
private fun MainChart(
    prices: List<Double>,
    chartColor: Color?,
    dragIndex: Int?,
    chartSize: MutableState<DpSize>,
    biggestValue: DpSize,
    isDragging: Boolean = false,
    showLivePriceIndicator: Boolean = false,
) {
    val localDensity = LocalDensity.current

    LineChart(
        prices = prices,
        color = chartColor ?: MaterialTheme.colors.onSurfaceDim,
        dragIndex = dragIndex,
        showFill = false,
        dropCount = 2,
        isDragging = isDragging,
        showLivePriceIndicator = showLivePriceIndicator,
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
}

@Composable
private fun CompareChart(
    prices: List<Double>,
    chartColor: Color?,
    dragIndex: Int?,
    biggestValue: DpSize,
    isDragging: Boolean = false,
    showLivePriceIndicator: Boolean = false,
) {
    LineChart(
        prices = prices,
        color = chartColor ?: MaterialTheme.colors.onSurfaceDim,
        dragIndex = dragIndex,
        showFill = false,
        dropCount = 2,
        isDragging = isDragging,
        showLivePriceIndicator = showLivePriceIndicator,
        paddings = PaddingValues(
            top = biggestValue.height / 2,
            bottom = biggestValue.height / 2,
            end = biggestValue.width
        ),
    )
}