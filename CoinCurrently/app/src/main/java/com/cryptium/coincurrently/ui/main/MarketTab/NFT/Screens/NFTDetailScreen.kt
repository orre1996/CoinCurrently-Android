package com.cryptium.coincurrently.ui.main.MarketTab.NFT.Screens

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import androidx.compose.ui.unit.min
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cryptium.coincurrently.R
import com.cryptium.coincurrently.ui.main.Compose.*
import com.cryptium.coincurrently.ui.main.Extensions.dpToPx
import com.cryptium.coincurrently.ui.main.Extensions.getActivity
import com.cryptium.coincurrently.ui.main.Extensions.is24HourClock
import com.cryptium.coincurrently.ui.main.Extensions.isTablet
import com.cryptium.coincurrently.ui.main.Extensions.pxToDp
import com.cryptium.coincurrently.ui.main.General.ChartBackground
import com.cryptium.coincurrently.ui.main.General.ChartHoverStats
import com.cryptium.coincurrently.ui.main.General.ChartTimeStamps
import com.cryptium.coincurrently.ui.main.General.ChartTimeframeButtonsView
import com.cryptium.coincurrently.ui.main.General.Components.Chart.ChartAccessibilityOverlay
import com.cryptium.coincurrently.ui.main.General.Components.Chart.chartAccessibilityOverlay
import com.cryptium.coincurrently.ui.main.General.Components.Chart.ChartHoverView
import com.cryptium.coincurrently.ui.main.General.Components.Chart.MultiTouchView
import com.cryptium.coincurrently.ui.main.General.Components.Chart.delayTouches
import com.cryptium.coincurrently.ui.main.General.Components.Chart.hideFromTalkBackWhenEnabled
import com.cryptium.coincurrently.ui.main.General.Components.Chart.LineChart
import com.cryptium.coincurrently.ui.main.General.Components.CustomTopAppBar
import com.cryptium.coincurrently.ui.main.General.Components.ImageCase
import com.cryptium.coincurrently.ui.main.General.Components.Loading.ActivityIndicator
import com.cryptium.coincurrently.ui.main.General.Enums.SortOption
import com.cryptium.coincurrently.ui.main.General.GlobalFunctions
import com.cryptium.coincurrently.ui.main.General.NFTDetailShareView
import com.cryptium.coincurrently.ui.main.analytics.AppShareActivityCompleted
import com.cryptium.coincurrently.ui.main.analytics.CurrencySelected
import com.cryptium.coincurrently.ui.main.analytics.AnalyticsManager
import com.cryptium.coincurrently.ui.main.analytics.ScreenLabel
import com.cryptium.coincurrently.ui.main.analytics.ScrollToTopPressed
import com.cryptium.coincurrently.ui.main.analytics.SortOptionSelected
import com.cryptium.coincurrently.ui.main.analytics.TabSwitched
import com.cryptium.coincurrently.ui.main.General.NavigationViewModel
import com.cryptium.coincurrently.ui.main.General.OnLifecycleEvent
import com.cryptium.coincurrently.ui.main.General.Utilities.Point
import com.cryptium.coincurrently.ui.main.General.WebView
import com.cryptium.coincurrently.ui.main.MarketTab.CryptoDetail.Components.General.ChartButtonsView
import com.cryptium.coincurrently.ui.main.MarketTab.CryptoDetail.Components.General.ScrollableTabSelector
import com.cryptium.coincurrently.ui.main.MarketTab.CryptoDetail.Components.General.CryptoDetailSummaryView
import com.cryptium.coincurrently.ui.main.MarketTab.CryptoDetail.Components.About.CryptoDetailAboutView
import com.cryptium.coincurrently.ui.main.MarketTab.CryptoDetail.Components.About.toDetailAboutData
import com.cryptium.coincurrently.ui.main.MarketTab.CryptoDetail.Components.Overview.NFTDetailOverviewView
import com.cryptium.coincurrently.ui.main.MarketTab.CryptoDetail.Components.Markets.CryptoDetailMarketsView
import com.cryptium.coincurrently.ui.main.MarketTab.CryptoDetail.Components.Markets.toDetailMarketsData
import com.cryptium.coincurrently.ui.main.MarketTab.CryptoDetail.Components.News.DetailNews
import com.cryptium.coincurrently.ui.main.MarketTab.Toplist.Popups.ToplistSortBySheet
import com.cryptium.coincurrently.ui.main.MarketTab.NFT.ViewModels.NFTDetailViewModel
import com.cryptium.coincurrently.ui.main.MarketTab.NFT.ViewModels.NFTDetailViewModelFactory
import com.cryptium.coincurrently.ui.main.SettingsTab.Popups.CurrencyOptionsSheet
import kotlinx.coroutines.launch
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import networking.ResponseModels.*
import kotlin.collections.lastOrNull
import kotlin.math.floor

@Composable
fun NftDetailScreen(
    navigationViewModel: NavigationViewModel,
    modifier: Modifier,
    nftToplist: NFTToplist
) {
    val viewModel: NFTDetailViewModel = viewModel(
        factory = NFTDetailViewModelFactory(
            nftToplist = nftToplist
        )
    )

    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val context = LocalContext.current
    val isTablet = context.isTablet()
    val talkBackEnabled = AccessibilityLayout.isTalkBackEnabled
    val fixedSelectedCurrency = GlobalFunctions.getSelectedCurrency()

    val biggestValue = remember { mutableStateOf(DpSize.Zero) }

    val tabs = listOf("Overview", "About", "Markets", "News")
    var currentPage by remember { mutableStateOf(0) }
    val scrollState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val useStickyHeaders = AccessibilityLayout.useStickyHeaders

    var selectedUrl: String? by remember { mutableStateOf(null) }

    val showCurrencySelector = remember { mutableStateOf(false) }
    var selectedCurrency by remember { mutableStateOf(GlobalFunctions.getSelectedCurrency()) }

    val isMarketsSortShowing = remember { mutableStateOf(false) }

    val firstVisibleIndex by remember {
        derivedStateOf { scrollState.firstVisibleItemIndex }
    }

    val nftShareChooserLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        val activityType = result.data?.component?.className ?: "none"
        AnalyticsManager.log(
            AppShareActivityCompleted(
                fromScreen = ScreenLabel.NftDetail,
                activityType = activityType,
                completed = result.resultCode == Activity.RESULT_OK,
            ),
        )
    }

    LaunchedEffect(Unit) {
        com.cryptium.coincurrently.ui.main.SettingsTab.Achievements.Managers.AchievementManager.increment(
            com.cryptium.coincurrently.ui.main.SettingsTab.Achievements.Models.AchievementCounterKey.nftDetailsViewed
        )
        viewModel.refresh(context)
    }

    OnLifecycleEvent { _, event ->
        when (event) {
            Lifecycle.Event.ON_RESUME -> {
                coroutineScope.launch {
                    viewModel.refresh(context)
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
            coroutineScope.launch {
                viewModel.isRefreshing = true
                try {
                    viewModel.refresh(
                        context,
                        fetchNews = currentPage == 3  // News tab index
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
                    title = viewModel.nftDetailInfo?.name ?: "",
                    navigateBack = {
                        navigationViewModel.navigateBack()
                    },
                    actions = listOf({
                        ShareButton(
                            share = {
                                context.getActivity()?.let { activity ->
                                    NFTDetailShareView(
                                        activity = activity,
                                        context = context,
                                        viewModel = viewModel,
                                        shareChooserLauncher = nftShareChooserLauncher,
                                    )
                                }
                            }
                        )
                    })
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
                            name = viewModel.nftDetailInfo?.name,
                            currentPrice = viewModel.onlyPrices.lastOrNull(),
                            imageUrl = viewModel.nftDetailInfo?.image?.small,
                            imageBitmap = viewModel.nftDetailSmallImageBitmap,
                            prices = viewModel.prices,
                            selectedTimeframe = viewModel.selectedTimeFrame,
                            shortPrice = false,
                            currency = viewModel.selectedCurrency,
                            changeCoin = { },
                            modifier = Modifier
                                .padding(horizontal = 16.dp)
                        )
                    }

                    item {
                        ChartButtonsView(
                            fromScreen = ScreenLabel.NftDetail,
                            selectedGraphType = viewModel.currentChartType.value,
                            isCandleChartEnabled = !viewModel.isChartFiat,
                            isInCompareMode = false,
                            isAlwaysSelectable = true,
                            imageCase = if(viewModel.isChartFiat) ImageCase.Image(viewModel.cryptoDetailModel?.image?.small) else ImageCase.Text(
                                fixedSelectedCurrency.symbol),
                            chartTypeAction = {
                                viewModel.setChartType(it)
                            },
                            toggleCandleChart = {
                                viewModel.toggleCurrencyType()
                            }
                        )
                    }

                    item {
                        Column {
                            ChartHoverStats(
                                isMultiHover = viewModel.chartHover.hoverHelper.hoverPrices.size == 2,
                                hoverTimes = viewModel.chartHover.hoverHelper.hoverTimes,
                                differenceValue = viewModel.differenceValue,
                                differencePercent = viewModel.differencePercent,
                                multiHoverColor = viewModel.multiHoverGraphColor,
                                hoverPrice = viewModel.hoverPrice,
                                hoverPriceColor = MaterialTheme.colors.onSurface,
                                hoverTime = viewModel.hoverTime,
                                hoverTimePosition = viewModel.hoverTimePosition,
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
                                        currency = viewModel.selectedCurrency,
                                        trailingPadding = biggestValue.value.width,
                                    )
                            ) {
                                Box(modifier = Modifier.hideFromTalkBackWhenEnabled()) {
                                    ChartBackground(
                                        values = viewModel.mainGraphStats,
                                        biggestValue = biggestValue
                                    )
                                }

                                if (viewModel.mainReloading) {
                                    ActivityIndicator(
                                        modifier = Modifier.hideFromTalkBackWhenEnabled(),
                                    )
                                } else {
                                    MainChart(
                                        prices = viewModel.prices,
                                        chartColor = viewModel.mainChartColor,
                                        dragIndex = if (viewModel.isDragging) viewModel.dragIndex else null,
                                        isDragging = viewModel.isDragging,
                                        dragPositions = viewModel.dragPositions,
                                        chartSize = viewModel.chartSize,
                                        biggestValue = biggestValue.value,
                                        hideFromTalkBack = talkBackEnabled,
                                    )
                                }

                                ChartAccessibilityOverlay(
                                    timeStamps = viewModel.timeStamps,
                                    prices = viewModel.prices,
                                    timeChart = viewModel.selectedTimeFrame,
                                    currency = viewModel.selectedCurrency,
                                    trailingPadding = biggestValue.value.width,
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
                            TimeCharts.getButtons(from = TimeCharts.anHour, to = TimeCharts.aYear),
                            viewModel.selectedTimeFrame,
                            fromScreen = ScreenLabel.NftDetail,
                        ) {
                            if (!viewModel.isDragging) {
                                coroutineScope.launch {
                                    viewModel.refresh(
                                        context,
                                        newDays = it.days,
                                        newTimeChart = it.timeChart
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Spacer(
                            modifier = Modifier
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
                                        TabSwitched.detailSegment(
                                            fromScreen = ScreenLabel.NftDetail,
                                            selectedTabName = tabs[it],
                                        ),
                                    )
                                }
                                currentPage = it

                                coroutineScope.launch {
                                    scrollState.animateScrollToItem(
                                        if (talkBackEnabled) 8 else 7
                                    )
                                }
                            }
                        )
                    }

                    when (currentPage) {
                        0 -> tabPageItem {
                            NFTDetailOverviewView(
                                nftDetailModel = viewModel.nftDetailInfo,
                                currentCurrency = viewModel.cryptoDetailModel,
                                prices = viewModel.onlyPrices,
                                timeChart = viewModel.selectedTimeFrame,
                                chartCurrency = viewModel.selectedCurrency,
                                isFiatSelected = viewModel.isChartFiat,
                                multiplier = viewModel.getPriceMultiplier(),
                                selectedCurrency = selectedCurrency,
                                showCurrencySelector = showCurrencySelector
                            )
                        }

                        1 -> tabPageItem {
                            CryptoDetailAboutView(
                                data = viewModel.toDetailAboutData(),
                                fromScreen = ScreenLabel.NftDetail,
                                openUrl = {
                                    selectedUrl = it
                                }
                            )
                        }

                        2 -> {
                            val (config, items) = viewModel.toDetailMarketsData()
                            CryptoDetailMarketsView(
                                config = config,
                                items = items,
                                selectedSortOption = viewModel.selectedSortOption,
                                isSortShowing = isMarketsSortShowing,
                                errorMessage = "NFT collection",
                                fromScreen = ScreenLabel.NftDetail,
                                openUrl = {
                                    selectedUrl = it
                                },
                                isTablet = isTablet
                            )
                        }

                        3 -> DetailNews(
                            news = viewModel.newsData,
                            title = "NFT collection",
                            showError = viewModel.showNewsError,
                            isTablet = isTablet,
                            fromScreen = ScreenLabel.NftDetail,
                            openUrl = {
                                selectedUrl = it
                            }
                        )
                    }
                }
            }

            ScrollToTopButton(
                visible = !talkBackEnabled && firstVisibleIndex >= 22 && currentPage == 3,
                respectNavigationBars = true,
                onClick = {
                    AnalyticsManager.log(
                        ScrollToTopPressed(
                            fromScreen = ScreenLabel.NftDetail,
                            selectedTabName = tabs[currentPage],
                        ),
                    )
                    coroutineScope.launch {
                        scrollState.animateScrollToItem(7)
                    }
                }
            )

            if (showCurrencySelector.value) {
                CurrencyOptionsSheet(
                    selectedOption = selectedCurrency,
                    newOptionSelected = {
                        selectedCurrency = it
                        AnalyticsManager.log(
                            CurrencySelected(
                                fromScreen = ScreenLabel.NftDetail,
                                currency = it.currency,
                            ),
                        )
                    },
                    close = {
                        showCurrencySelector.value = false
                    }
                )
            }

            if (isMarketsSortShowing.value) {
                ToplistSortBySheet(
                    options = SortOption.nftDetailOptions,
                    selectedOption = viewModel.selectedSortOption,
                    newOptionSelected = { option ->
                        AnalyticsManager.log(
                            SortOptionSelected(
                                fromScreen = ScreenLabel.NftDetail,
                                option = option,
                            ),
                        )
                        coroutineScope.launch {
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
private fun ShareButton(
    share: () -> Unit
) {
    IconButton(onClick = {
        share.invoke()
    }) {
        Image(
            painter = painterResource(id = R.drawable.share),
            contentDescription = "Share",
            colorFilter = ColorFilter.tint(
                MaterialTheme.colors.onSurface
            ),
        )
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
        prices = if (prices.all { it == prices.first() }) listOf() else prices,
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
            topPadding = biggestValue.height / 2
        )
    }
}
