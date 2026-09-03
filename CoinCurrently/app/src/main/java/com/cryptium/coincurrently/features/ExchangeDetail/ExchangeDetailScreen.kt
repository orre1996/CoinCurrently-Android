package com.cryptium.coincurrently.features.ExchangeDetail
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Text
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import androidx.compose.ui.unit.min
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cryptium.coincurrently.sharedui.theme.*
import com.cryptium.coincurrently.sharedui.modifiers.*
import com.cryptium.coincurrently.sharedui.components.AccessibilityLayout
import com.cryptium.coincurrently.sharedui.components.conditionalStickyHeader
import com.cryptium.coincurrently.sharedui.components.tabPageItem
import com.cryptium.coincurrently.sharedui.buttons.ScrollToTopButton
import com.cryptium.coincurrently.sharedui.charts.ChartHoverStats
import com.cryptium.coincurrently.sharedui.charts.rememberLineChartHoverUi
import com.cryptium.coincurrently.sharedui.models.FormattingThreshold
import com.cryptium.coincurrently.core.extensions.formatCurrency
import com.cryptium.coincurrently.features.CoinDetail.Views.News.DetailNews
import com.cryptium.coincurrently.core.extensions.dpToPx
import com.cryptium.coincurrently.core.extensions.is24HourClock
import com.cryptium.coincurrently.core.extensions.isTablet
import com.cryptium.coincurrently.core.extensions.pxToDp
import com.cryptium.coincurrently.sharedui.charts.ChartBackground
import com.cryptium.coincurrently.sharedui.charts.ChartTimeStamps
import com.cryptium.coincurrently.sharedui.charts.ChartTimeframeButtonsView
import com.cryptium.coincurrently.sharedui.charts.ChartAccessibilityOverlay
import com.cryptium.coincurrently.sharedui.charts.chartAccessibilityOverlay
import com.cryptium.coincurrently.sharedui.charts.ChartHoverView
import com.cryptium.coincurrently.sharedui.charts.MultiTouchView
import com.cryptium.coincurrently.sharedui.charts.delayTouches
import com.cryptium.coincurrently.sharedui.charts.hideFromTalkBackWhenEnabled
import com.cryptium.coincurrently.sharedui.charts.LineChart
import com.cryptium.coincurrently.sharedui.navigation.CustomTopAppBar
import com.cryptium.coincurrently.sharedui.loading.ActivityIndicator
import com.cryptium.coincurrently.sharedui.models.SortOption
import com.cryptium.coincurrently.app.NavigationViewModel
import com.cryptium.coincurrently.sharedui.components.OnLifecycleEvent
import com.cryptium.coincurrently.sharedui.components.Point
import com.cryptium.coincurrently.sharedui.sheets.WebView
import com.cryptium.coincurrently.features.CoinDetail.Views.About.CryptoDetailAboutView
import com.cryptium.coincurrently.features.CoinDetail.Views.About.toDetailAboutData
import com.cryptium.coincurrently.features.CoinDetail.Views.Markets.CryptoDetailMarketsView
import com.cryptium.coincurrently.features.CoinDetail.Views.Markets.toDetailMarketsData
import com.cryptium.coincurrently.features.CoinDetail.Views.General.ScrollableTabSelector
import com.cryptium.coincurrently.features.CoinDetail.Views.General.CryptoDetailSummaryView
import com.cryptium.coincurrently.features.ExchangeDetail.ExchangeDetailViewModel
import com.cryptium.coincurrently.features.ExchangeDetail.ExchangeDetailViewModelFactory
import com.cryptium.coincurrently.features.ExchangeDetail.Views.ExchangeDetailSummaryView
import com.cryptium.coincurrently.features.Market.Toplist.Sheets.ToplistSortBySheet
import com.cryptium.coincurrently.core.analytics.AnalyticsManager
import com.cryptium.coincurrently.core.analytics.ScreenLabel
import com.cryptium.coincurrently.core.analytics.ScrollToTopPressed
import com.cryptium.coincurrently.core.analytics.SortOptionSelected
import com.cryptium.coincurrently.core.analytics.TabChanged
import kotlinx.coroutines.launch
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.cryptium.coincurrently.core.networking.ResponseModels.*
import kotlin.math.floor

@Composable
fun ExchangeDetailScreen(
    navigationViewModel: NavigationViewModel,
    modifier: Modifier,
    exchange: ExchangeResponse
) {
    val viewModel: ExchangeDetailViewModel = viewModel(
        factory = ExchangeDetailViewModelFactory(
            exchange = exchange
        )
    )

    val hoverUi = rememberLineChartHoverUi(
        chartHover = viewModel.chartHover,
        prices = viewModel.prices,
        timeStamps = viewModel.timeStamps,
        chartSize = viewModel.chartSize.value,
        hoverPriceTextWidth = viewModel.hoverPriceTextWidth.value,
        timeChart = viewModel.selectedTimeFrame,
        changePercent = viewModel.changePercent,
        formatPrice = { it.formatCurrency(threshold = FormattingThreshold.MILLION) },
    )

    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val context = LocalContext.current
    val isTablet = context.isTablet()
    val talkBackEnabled = AccessibilityLayout.isTalkBackEnabled

    val biggestValue = remember { mutableStateOf(DpSize.Zero) }

    val tabs = listOf("Markets", "About", "News")
    var currentPage by remember { mutableStateOf(0) }

    val uiCoroutineScope = rememberCoroutineScope()
    val scrollState = rememberLazyListState()
    val useStickyHeaders = AccessibilityLayout.useStickyHeaders

    var selectedUrl: String? by remember { mutableStateOf(null) }

    val isMarketsSortShowing = remember { mutableStateOf(false) }

    val firstVisibleIndex by remember {
        derivedStateOf { scrollState.firstVisibleItemIndex }
    }

    LaunchedEffect(Unit) {
        com.cryptium.coincurrently.features.Achievements.Managers.AchievementManager.increment(
            com.cryptium.coincurrently.features.Achievements.Models.AchievementCounterKey.exchangeDetailsViewed
        )
        launch { viewModel.getToplist() }
        launch { viewModel.refresh() }
    }

    OnLifecycleEvent { _, event ->
        when (event) {
            Lifecycle.Event.ON_RESUME -> {
                uiCoroutineScope.launch {
                    launch { viewModel.getToplist() }
                    launch { viewModel.refresh() }
                }
            }
            else -> { }
        }
    }

    BackHandler(onBack = {
        navigationViewModel.navigateBack()
    })

    SwipeRefresh(
        state = rememberSwipeRefreshState(isRefreshing = viewModel.isRefreshing),
        onRefresh = {
            uiCoroutineScope.launch {
                viewModel.isRefreshing = true
                try {
                    viewModel.refresh(
                        getNews = currentPage == 1
                    )
                } finally {
                    viewModel.isRefreshing = false
                }
            }
        }) {
        Box(
            contentAlignment = Alignment.BottomEnd
        ) {
            Column(
                modifier = modifier
            ) {
                CustomTopAppBar(
                    title = viewModel.exchangeInfo?.name ?: "",
                    navigateBack = {
                        navigationViewModel.navigateBack()
                    }
                )

                LazyColumn(
                    state = scrollState,
                    userScrollEnabled = !viewModel.isDragging,
                    contentPadding = PaddingValues(
                        top = 16.dp,
                        bottom = 32.dp
                    )
                ) {
                    item {
                        CryptoDetailSummaryView(
                            name = viewModel.exchangeInfo?.name,
                            currentPrice = viewModel.prices.lastOrNull(),
                            imageUrl = viewModel.exchangeInfo?.image,
                            prices = viewModel.prices,
                            selectedTimeframe = viewModel.selectedTimeFrame,
                            shortPrice = false,
                            valueAccessibilityLabel = "24 hour volume",
                            changeMetricLabel = "volume",
                            changeCoin = { },
                            modifier = Modifier
                                .padding(
                                    start = 16.dp,
                                    bottom = 8.dp,
                                    end = 16.dp
                                )
                        )
                    }

                    item {
                        ExchangeDetailSummaryView(
                            exchangeDetail = viewModel.exchangeInfo
                        )
                    }

                    item {
                        Column {
                            ChartHoverStats(
                                isMultiHover = hoverUi.isMultiHover,
                                hoverTimes = hoverUi.hoverTimes,
                                differenceValue = hoverUi.differenceValue,
                                differencePercent = hoverUi.differencePercent,
                                multiHoverColor = hoverUi.multiHoverGraphColor,
                                hoverPrice = hoverUi.hoverPrice,
                                hoverPriceColor = hoverUi.hoverColor,
                                hoverTime = hoverUi.hoverTime,
                                hoverTimePosition = hoverUi.hoverTimePosition,
                                isDragging = viewModel.isDragging,
                                hoverPriceTextWidth = viewModel.hoverPriceTextWidth,
                            )

                            Box(
                                modifier = Modifier
                                    .height((screenHeight.dpToPx() * 0.35).pxToDp())
                                    .delayTouches()
                                    .padding(horizontal = 16.dp)
                                    .chartAccessibilityOverlay(
                                        timeStamps = viewModel.timeStamps,
                                        prices = viewModel.prices,
                                        timeChart = viewModel.selectedTimeFrame,
                                        trailingPadding = biggestValue.value.width,
                                        useExchangeDateFormat = true,
                                    )
                            ) {
                                Box(modifier = Modifier.hideFromTalkBackWhenEnabled()) {
                                    ChartBackground(
                                        values = viewModel.mainGraphStats,
                                        biggestValue = biggestValue,
                                    )
                                }

                                if (viewModel.mainReloading) {
                                    ActivityIndicator(
                                        modifier = Modifier.hideFromTalkBackWhenEnabled(),
                                    )
                                } else {
                                    MainChart(
                                        prices = viewModel.prices,
                                        chartColor = hoverUi.chartColor,
                                        dragIndex = if (viewModel.isDragging) hoverUi.dragIndex else null,
                                        isDragging = viewModel.isDragging,
                                        dragPositions = hoverUi.dragPositions,
                                        chartSize = viewModel.chartSize,
                                        biggestValue = biggestValue.value,
                                        hideFromTalkBack = talkBackEnabled,
                                    )
                                }

                                ChartAccessibilityOverlay(
                                    timeStamps = viewModel.timeStamps,
                                    prices = viewModel.prices,
                                    timeChart = viewModel.selectedTimeFrame,
                                    trailingPadding = biggestValue.value.width,
                                    useExchangeDateFormat = true,
                                )

                                MultiTouchView(
                                    modifier = Modifier.matchParentSize(),
                                    onTouchesChanged = { touchXs ->
                                        if (!viewModel.isRefreshing) {
                                            viewModel.updateHoverStats(
                                                touchXs = touchXs.map { touchX ->
                                                    max(
                                                        0.dp,
                                                        min(
                                                            touchX,
                                                            viewModel.chartSize.value.width - 0.001.dp,
                                                        ),
                                                    )
                                                },
                                                is24HourClock = context.is24HourClock,
                                            )
                                        }
                                    },
                                )
                            }
                        }
                    }

                    item {
                        ChartTimeStamps(
                            currentTimeChart = viewModel.selectedTimeFrame,
                            timeStamps = viewModel.timeStamps,
                            modifier = Modifier
                                .offset(y = -biggestValue.value.height / 2)
                                .padding(
                                    start = 16.dp,
                                    end = 16.dp + biggestValue.value.width,
                                    bottom = 16.dp - biggestValue.value.height / 2,
                                )
                                .hideFromTalkBackWhenEnabled()
                        )
                    }

                    item {
                        ChartTimeframeButtonsView(
                            TimeCharts.getButtons(from = TimeCharts.aDay, to = TimeCharts.aYear),
                            viewModel.selectedTimeFrame,
                            fromScreen = ScreenLabel.ExchangeDetail,
                        ) {
                            if (!viewModel.isDragging) {
                                uiCoroutineScope.launch {
                                    viewModel.refresh(
                                        newDays = it.days,
                                        newTimeChart = it.timeChart
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier
                            .height(32.dp)
                        )
                    }

                    conditionalStickyHeader(
                        useSticky = useStickyHeaders,
                        talkBackEnabled = talkBackEnabled,
                    ) {
                        ScrollableTabSelector(
                            tabs = tabs,
                            selectedTabIndex = currentPage,
                            onTabSelected = {
                                if (it != currentPage) {
                                    AnalyticsManager.log(
                                        TabChanged(
                                            fromScreen = ScreenLabel.ExchangeDetail,
                                            selectedTabName = tabs[it]
                                        )
                                    )
                                }
                                currentPage = it

                                uiCoroutineScope.launch {
                                    scrollState.animateScrollToItem(
                                        if (talkBackEnabled) 8 else 7
                                    )
                                }
                            }
                        )
                    }

                    when (currentPage) {
                        0 -> {
                            val (config, items) = viewModel.toDetailMarketsData()
                            CryptoDetailMarketsView(
                                config = config,
                                items = items,
                                selectedSortOption = viewModel.selectedSortOption,
                                isSortShowing = isMarketsSortShowing,
                                errorMessage = "exchange",
                                fromScreen = ScreenLabel.ExchangeDetail,
                                openUrl = {
                                    selectedUrl = it
                                },
                                isTablet = isTablet
                            )
                        }

                        1 -> tabPageItem {
                            CryptoDetailAboutView(
                                data = viewModel.toDetailAboutData(),
                                fromScreen = ScreenLabel.ExchangeDetail,
                                openUrl = {
                                    selectedUrl = it
                                }
                            )
                        }

                        2 -> DetailNews(
                            news = viewModel.newsData,
                            title = "exchange",
                            showError = viewModel.showNewsError,
                            isTablet = isTablet,
                            fromScreen = ScreenLabel.ExchangeDetail,
                            openUrl = {
                                selectedUrl = it
                            }
                        )
                    }
                }
            }

            ScrollToTopButton(
                visible = !talkBackEnabled && firstVisibleIndex >= 22 && (currentPage == 0 || currentPage == 2),
                respectNavigationBars = true,
                onClick = {
                    AnalyticsManager.log(
                        ScrollToTopPressed(
                            fromScreen = ScreenLabel.ExchangeDetail,
                            selectedTabName = tabs[currentPage]
                        )
                    )
                    uiCoroutineScope.launch {
                        scrollState.animateScrollToItem(7)
                    }
                }
            )

            if (isMarketsSortShowing.value) {
                ToplistSortBySheet(
                    options = SortOption.exchangeDetailOptions,
                    selectedOption = viewModel.selectedSortOption,
                    newOptionSelected = { option ->
                        AnalyticsManager.log(
                            SortOptionSelected(
                                fromScreen = ScreenLabel.ExchangeDetail,
                                option = option
                            )
                        )
                        uiCoroutineScope.launch {
                            viewModel.setNewSortOption(
                                newSortOption = option
                            )
                        }
                    },
                    close = {
                        isMarketsSortShowing.value = false
                    }
                )
            }

            selectedUrl?.let {
                WebView(it) {
                    selectedUrl = null
                }
            }
        }
    }
}

@Composable
private fun MainChart(
    prices: List<Double>,
    chartColor: Color?,
    dragIndex: Int?,
    isDragging: Boolean,
    dragPositions: List<Point>,
    chartSize: MutableState<DpSize>,
    biggestValue: DpSize,
    hideFromTalkBack: Boolean = false,
) {
    val localDensity = LocalDensity.current
    val chartModifier = Modifier
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
        .then(
            if (hideFromTalkBack) {
                Modifier.hideFromTalkBackWhenEnabled()
            } else {
                Modifier
            }
        )

    LineChart(
        prices = prices,
        color = chartColor ?: MaterialTheme.colors.onSurfaceDim,
        dragIndex = dragIndex,
        showFill = true,
        isDragging = isDragging,
        dragPositionXs = if (isDragging) {
            dragPositions.map { with(localDensity) { it.x.toPx() } }
        } else {
            emptyList()
        },
        paddings = PaddingValues(
            top = biggestValue.height / 2,
            bottom = biggestValue.height / 2,
            end = biggestValue.width
        ),
        modifier = chartModifier
    )

    Box(
        modifier = if (hideFromTalkBack) {
            Modifier.hideFromTalkBackWhenEnabled()
        } else {
            Modifier
        }
    ) {
        ChartHoverView(
            positions = dragPositions,
            isDragging = isDragging,
            color = chartColor ?: MaterialTheme.colors.onSurfaceDim,
            topPadding = biggestValue.height / 2,
        )
    }
}