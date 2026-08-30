package com.cryptium.coincurrently.ui.main.MarketTab.CryptoDetail.Screens

import com.cryptium.coincurrently.ui.main.Compose.imageSize

import AddressImport
import NavigationScreens
import SearchCase
import SearchScreenCase
import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.with
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.zIndex
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import androidx.compose.ui.unit.min
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cryptium.coincurrently.R
import com.cryptium.coincurrently.AppLocale
import com.cryptium.coincurrently.ui.main.Compose.*
import com.cryptium.coincurrently.ui.main.CryptoDetail.Compose.Popups.SentimentInfoSheet
import com.cryptium.coincurrently.ui.main.CryptoDetail.Compose.Portfolio.Models.NewSwapTransaction
import com.cryptium.coincurrently.ui.main.CryptoDetail.Compose.PriceChart.CandleChart
import com.cryptium.coincurrently.ui.main.CryptoDetail.Compose.PriceChart.CandleChartHover
import com.cryptium.coincurrently.ui.main.MarketTab.CryptoDetail.Components.News.DetailNews
import com.cryptium.coincurrently.ui.main.Extensions.getActivity
import com.cryptium.coincurrently.ui.main.Extensions.dpToPx
import com.cryptium.coincurrently.ui.main.Extensions.is24HourClock
import com.cryptium.coincurrently.ui.main.Extensions.getSelectedCurrency
import com.cryptium.coincurrently.ui.main.Extensions.isTablet
import com.cryptium.coincurrently.ui.main.Extensions.pieContaining
import com.cryptium.coincurrently.ui.main.MarketTab.CryptoDetail.ViewModel.CryptoDetailViewModel
import com.cryptium.coincurrently.ui.main.MarketTab.CryptoDetail.ViewModel.CryptoDetailViewModelFactory
import com.cryptium.coincurrently.ui.main.Extensions.pxToDp
import com.cryptium.coincurrently.ui.main.Extensions.satoshiBold
import com.cryptium.coincurrently.ui.main.General.ChartBackground
import com.cryptium.coincurrently.ui.main.General.ChartTimeStamps
import com.cryptium.coincurrently.ui.main.General.ChartTimeframeButtonsView
import com.cryptium.coincurrently.ui.main.General.Components.Chart.ChartAccessibilityOverlay
import com.cryptium.coincurrently.ui.main.General.Components.Chart.chartAccessibilityOverlay
import com.cryptium.coincurrently.ui.main.General.ChartHoverStats
import com.cryptium.coincurrently.ui.main.General.Components.Chart.ChartHoverView
import com.cryptium.coincurrently.ui.main.General.Components.CustomTopAppBar
import com.cryptium.coincurrently.ui.main.General.Components.ImageCase
import com.cryptium.coincurrently.ui.main.General.Components.Loading.ActivityIndicator
import com.cryptium.coincurrently.ui.main.General.CryptoDetailShareView
import com.cryptium.coincurrently.ui.main.General.Enums.ChangeDirection
import com.cryptium.coincurrently.ui.main.General.Enums.SortOption
import com.cryptium.coincurrently.ui.main.General.GlobalFunctions
import com.cryptium.coincurrently.ui.main.General.NavigationViewModel
import com.cryptium.coincurrently.ui.main.General.OnLifecycleEvent
import com.cryptium.coincurrently.ui.main.General.Components.Chart.MultiTouchView
import com.cryptium.coincurrently.ui.main.General.Components.Chart.delayTouches
import com.cryptium.coincurrently.ui.main.General.Components.Chart.hideFromTalkBackWhenEnabled
import com.cryptium.coincurrently.ui.main.General.Components.Chart.LineChart
import com.cryptium.coincurrently.ui.main.General.Utilities.Point
import com.cryptium.coincurrently.ui.main.General.WebView
import com.cryptium.coincurrently.ui.main.Repositories.PriceAlertCreateResult
import com.cryptium.coincurrently.ui.main.Repositories.StoreKitManager
import networking.ResponseModels.CryptoDetailInfoModel
import com.cryptium.coincurrently.ui.main.MarketTab.CryptoDetail.Components.General.ChartButtonsView
import com.cryptium.coincurrently.ui.main.MarketTab.CryptoDetail.Enums.ChartTypes
import com.cryptium.coincurrently.ui.main.MarketTab.CryptoDetail.Components.About.CryptoDetailAboutView
import com.cryptium.coincurrently.ui.main.MarketTab.CryptoDetail.Components.About.toDetailAboutData
import com.cryptium.coincurrently.ui.main.MarketTab.CryptoDetail.Components.General.CandleChartHoverStatsView
import com.cryptium.coincurrently.ui.main.MarketTab.CryptoDetail.Components.General.ScrollableTabSelector
import com.cryptium.coincurrently.ui.main.MarketTab.CryptoDetail.Components.General.CryptoDetailSummaryView
import com.cryptium.coincurrently.ui.main.MarketTab.CryptoDetail.Components.Markets.CryptoDetailMarketsView
import com.cryptium.coincurrently.ui.main.MarketTab.CryptoDetail.Components.Markets.toDetailMarketsData
import com.cryptium.coincurrently.ui.main.MarketTab.CryptoDetail.Components.Overview.CryptoDetailOverviewView
import com.cryptium.coincurrently.ui.main.MarketTab.CryptoDetail.Components.Portfolio.CryptoDetailPortfolioView
import com.cryptium.coincurrently.ui.main.MarketTab.CryptoDetail.Popups.EditWidgetSheet
import com.cryptium.coincurrently.ui.main.MarketTab.CryptoDetail.Popups.SelectCoinSheet
import com.cryptium.coincurrently.ui.main.MarketTab.Toplist.Popups.ToplistSortBySheet
import com.cryptium.coincurrently.ui.main.PopupViews.SnackBarType
import com.cryptium.coincurrently.ui.main.analytics.BuyProAction
import com.cryptium.coincurrently.ui.main.analytics.ChangeCompareCoin
import com.cryptium.coincurrently.ui.main.analytics.CompareCoinChangeSelection
import com.cryptium.coincurrently.ui.main.analytics.CurrencySelected
import com.cryptium.coincurrently.ui.main.analytics.AnalyticsManager
import com.cryptium.coincurrently.ui.main.analytics.Navigate
import com.cryptium.coincurrently.ui.main.analytics.PortfolioTransactionDeleted
import com.cryptium.coincurrently.ui.main.analytics.PortfolioTransactionDeletedKind
import com.cryptium.coincurrently.ui.main.analytics.PriceAlertAdded
import com.cryptium.coincurrently.ui.main.analytics.PriceAlertDeleted
import com.cryptium.coincurrently.ui.main.analytics.PriceAlertInputType
import com.cryptium.coincurrently.ui.main.analytics.ScreenLabel
import com.cryptium.coincurrently.ui.main.analytics.ScrollToTopPressed
import com.cryptium.coincurrently.ui.main.analytics.SheetLabel
import com.cryptium.coincurrently.ui.main.analytics.SheetOpened
import com.cryptium.coincurrently.ui.main.analytics.SortOptionSelected
import com.cryptium.coincurrently.ui.main.analytics.TabChanged
import com.cryptium.coincurrently.ui.main.analytics.WidgetAdded
import com.cryptium.coincurrently.ui.main.analytics.WidgetReplaceUndone
import com.cryptium.coincurrently.ui.main.analytics.WidgetReplaced
import com.cryptium.coincurrently.ui.main.PortfolioTab.DataClasses.NewTransaction
import com.cryptium.coincurrently.ui.main.PortfolioTab.DataClasses.PortfolioEvent
import com.cryptium.coincurrently.ui.main.PortfolioTab.Enums.NewTransactionType
import com.cryptium.coincurrently.ui.main.PortfolioTab.Popups.SelectTransactionTypeSheet
import com.cryptium.coincurrently.ui.main.PortfolioTab.Popups.SwapTransactionDetailSheet
import com.cryptium.coincurrently.ui.main.PortfolioTab.Popups.TransactionDetailSheet
import com.cryptium.coincurrently.ui.main.SettingsTab.Popups.CurrencyOptionsSheet
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import networking.ResponseModels.Candle
import networking.ResponseModels.CurrencySelectionModel
import networking.ResponseModels.TimeCharts
import networking.ResponseModels.toCryptoDetail
import networking.ResponseModels.toToplist
import kotlin.math.floor

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@SuppressLint("UnrememberedMutableState")
@Composable
fun CryptoDetailScreen(
    cryptoDetailInfoModel: CryptoDetailInfoModel,
    navigationViewModel: NavigationViewModel,
    modifier: Modifier
) {
    val viewModel: CryptoDetailViewModel = viewModel(
        factory = CryptoDetailViewModelFactory(
            cryptoDetailInfoModel
        )
    )

    val context = LocalContext.current
    val isTablet = context.isTablet()
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val talkBackEnabled = AccessibilityLayout.isTalkBackEnabled

    val tabs = listOf("Overview", "About", "Portfolio", "Markets", "News")
    val scrollState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val useStickyHeaders = AccessibilityLayout.useStickyHeaders

    var isCoinsViewShowing by remember { mutableStateOf(false) }
    var isSelectingMainCoin by remember { mutableStateOf(false) }

    var isEditWidgetShowing by remember { mutableStateOf(false) }

    var selectedUrl: String? by remember { mutableStateOf(null) }

    val showCurrencySelector = remember { mutableStateOf(false) }
    var selectedCurrency by remember { mutableStateOf(GlobalFunctions.getSelectedCurrency()) }

    val biggestValue = remember { mutableStateOf(DpSize.Zero) }

    val isDropDownExpanded = remember { mutableStateOf(false) }

    val currentSubscription by StoreKitManager.currentSubscription.observeAsState()

    val isMarketsSortShowing = remember { mutableStateOf(false) }
    val showSentimentInfo = remember { mutableStateOf(false) }
    var selectedTransaction: PortfolioEvent? by remember { mutableStateOf(null) }

    var showAddAlert by remember { mutableStateOf(false) }
    var isAddingAlert by remember { mutableStateOf(false) }
    val isAlertByPriceSelected = remember { mutableStateOf(true) }
    val isRecurringAlertOn = remember { mutableStateOf(false) }
    val priceInput = remember { mutableDoubleStateOf(0.0) }
    val percentInput = remember { mutableDoubleStateOf(10.0) }
    var selectedAlertCurrency by remember { mutableStateOf(GlobalFunctions.getSelectedCurrency()) }
    val selectedDirection = remember { mutableStateOf(ChangeDirection.Up) }
    var isSelectingAlertCurrency by remember { mutableStateOf(false) }
    var showAlertError by remember { mutableStateOf(false) }

    val showAddTransactionSheet = remember { mutableStateOf(false ) }

    val firstVisibleIndex by remember {
        derivedStateOf { scrollState.firstVisibleItemIndex }
    }

    LaunchedEffect(Unit) {
        com.cryptium.coincurrently.ui.main.SettingsTab.Achievements.Managers.AchievementManager.increment(
            com.cryptium.coincurrently.ui.main.SettingsTab.Achievements.Models.AchievementCounterKey.cryptoDetailsViewed
        )
        coroutineScope {
            launch { viewModel.getPriceAlerts() }
            launch { viewModel.getGlobalMarketData() }
            launch {
                viewModel.refresh(
                    context,
                    showReload = viewModel.mainReloading,
                    showRefreshIndicator = false,
                    getNews = false
                )
            }
        }
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
                    coroutineScope {
                        launch { viewModel.getPriceAlerts() }
                        launch { viewModel.getGlobalMarketData() }
                        launch {
                            viewModel.refresh(
                                context,
                                showReload = viewModel.mainReloading,
                                showRefreshIndicator = false,
                                getNews = false
                            )
                        }
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

    LaunchedEffect(viewModel.currentPage) {
        if (viewModel.currentPage == 2) {
            viewModel.getPortfolio()
        } else if (viewModel.currentPage == 4) {
            viewModel.getCryptoNews()
        }
    }

    OnLifecycleEvent { _, event ->
        when (event) {
            Lifecycle.Event.ON_RESUME -> {
                coroutineScope.launch {
                    if (viewModel.currentPage == 2) {
                        viewModel.getPortfolio()
                    } else if (viewModel.currentPage == 4) {
                        viewModel.getCryptoNews()
                    }
                }
            }
            else -> { }
        }
    }

    val pendingPortfolioTransactionId = navigationViewModel.pendingPortfolioTransactionId
    LaunchedEffect(pendingPortfolioTransactionId) {
        val idStr = pendingPortfolioTransactionId ?: return@LaunchedEffect
        val previousTransactionCount = navigationViewModel.pendingPortfolioPreviousTransactionCount
        navigationViewModel.pendingPortfolioTransactionId = null
        navigationViewModel.pendingPortfolioPreviousTransactionCount = -1

        launch {
            viewModel.getPortfolio()
        }

        val (event, coin) = viewModel.findTransaction(idStr)
        val symbol = coin?.symbol?.uppercase(AppLocale.enUsPosix) ?: "Coin"

        when (event) {
            is NewTransaction -> {
                navigationViewModel.showSnackbar(
                    context = context,
                    type = SnackBarType.SUCCESS,
                    message = "$symbol transaction added to your portfolio!",
                    buttonTitle = "View Details",
                    callback = {
                        AnalyticsManager.log(
                            SheetOpened(ScreenLabel.CryptoDetail, SheetLabel.PortfolioTransactionDetails),
                        )
                        selectedTransaction = event
                    }
                )
            }

            is NewSwapTransaction -> {
                navigationViewModel.showSnackbar(
                    context = context,
                    type = SnackBarType.SUCCESS,
                    message = "$symbol transaction added to your portfolio!",
                    buttonTitle = "View Details",
                    callback = {
                        AnalyticsManager.log(
                            SheetOpened(ScreenLabel.CryptoDetail, SheetLabel.PortfolioTransactionDetails),
                        )
                        selectedTransaction = event
                    }
                )
            }

            is AddressImport -> {
                val wallet = event
                val topCoin = coin ?: return@LaunchedEffect
                val addr = wallet.address.uppercase(AppLocale.enUsPosix)
                val shortAddr =
                    if (addr.length >= 10) "${addr.take(5)}...${addr.takeLast(5)}" else addr
                navigationViewModel.showSnackbar(
                    context = context,
                    type = SnackBarType.SUCCESS,
                    message = "$shortAddr ($symbol) imported to your portfolio!",
                    buttonTitle = "View Details",
                    callback = {
                        val dest = NavigationScreens.WalletDetails(
                            wallet = wallet,
                            coin = topCoin
                        )
                        AnalyticsManager.log(
                            Navigate(
                                fromScreen = ScreenLabel.CryptoDetail,
                                toScreen = ScreenLabel.WalletDetails,
                                coinName = topCoin.name ?: "",
                            ),
                        )
                        navigationViewModel.navigate(dest)
                    }
                )
            }

            else -> { }
        }

        if (previousTransactionCount >= 0) {
            kotlinx.coroutines.delay(150)
            com.cryptium.coincurrently.ui.main.SettingsTab.Achievements.Managers.AchievementManager
                .checkPortfolioTransactionUnlock(previousTransactionCount)
        }
    }

    BackHandler(onBack = {
        navigationViewModel.navigateBack()
    })

    SwipeRefresh(
        state = rememberSwipeRefreshState(isRefreshing = viewModel.isRefreshing),
        onRefresh = {
            coroutineScope.launch {
                viewModel.refresh(
                    context,
                    showReload = false,
                    getNews = viewModel.currentPage == 4
                )
            }
            if (viewModel.currentPage == 2) {
                coroutineScope.launch {
                    viewModel.getPortfolio()
                }
            }
        }) {
        Box(
            contentAlignment = Alignment.BottomEnd,
        ) {
            Column(
                modifier = modifier
            ) {
                CustomTopAppBar(
                    title = viewModel.cryptoInfo?.name ?: "",
                    navigateBack = {
                        navigationViewModel.navigateBack()
                    },
                    actions = listOf({
                        FavoriteButton(
                            isFavorited = viewModel.inFavoritesList,
                            coinName = viewModel.cryptoInfo?.name ?: "coin",
                            toggleFavorite = {
                                coroutineScope.launch {
                                    viewModel.favoriteButtonPressed(context) {
                                        navigationViewModel.openBuyPro(ScreenLabel.CryptoDetail, BuyProAction.Favorite)
                                    }
                                }
                            }
                        )

                        MoreButton(
                            isDropDownExpanded = isDropDownExpanded,
                            coinName = viewModel.cryptoInfo?.name ?: "coin",
                            isAlreadyAddedToWidgets = viewModel.widgetCurrencies.firstOrNull { it.id == viewModel.cryptoInfo?.id } != null,
                            widgetsCount = viewModel.widgetCurrencies.count(),
                            addToWidget = {
                                AnalyticsManager.log(
                                    WidgetAdded(
                                        fromScreen = ScreenLabel.CryptoDetail,
                                        coinSymbol = viewModel.cryptoInfo?.symbol?.uppercase(AppLocale.enUsPosix) ?: "",
                                    ),
                                )
                                viewModel.addCurrencyToWidgets(context)
                                navigationViewModel.showSnackbar(
                                    context = context,
                                    type = SnackBarType.SUCCESS,
                                    message = "${viewModel.cryptoInfo?.symbol?.uppercase() ?: "Coin"} added to widgets.",
                                    buttonTitle = "Manage",
                                    callback = {
                                        val dest = NavigationScreens.Widgets
                                        AnalyticsManager.log(
                                            Navigate(
                                                fromScreen = ScreenLabel.CryptoDetail,
                                                toScreen = ScreenLabel.Widgets,
                                                coinName = viewModel.cryptoInfo?.name ?: "",
                                            ),
                                        )
                                        navigationViewModel.deepLink(dest)
                                    }
                                )
                            },
                            editWidgets = {
                                AnalyticsManager.log(
                                    SheetOpened(ScreenLabel.CryptoDetail, SheetLabel.EditWidgets),
                                )
                                isEditWidgetShowing = true
                            },
                            deepLinkToWidgets = {
                                val dest = NavigationScreens.Widgets
                                AnalyticsManager.log(
                                    Navigate(
                                        fromScreen = ScreenLabel.CryptoDetail,
                                        toScreen = ScreenLabel.Widgets,
                                        coinName = viewModel.cryptoInfo?.name ?: "",
                                    ),
                                )
                                navigationViewModel.deepLink(dest)
                            },
                            compareCoins = {
                                AnalyticsManager.log(
                                    SheetOpened(ScreenLabel.CryptoDetail, SheetLabel.CoinSearch),
                                )
                                isSelectingMainCoin = false
                                isCoinsViewShowing = true
                            },
                            addPriceAlert = {
                                if (!viewModel.checkIfUserEnabledPush(context) ) {
                                    val dest = NavigationScreens.PriceAlerts
                                    AnalyticsManager.log(
                                        Navigate(
                                            fromScreen = ScreenLabel.CryptoDetail,
                                            toScreen = ScreenLabel.PriceAlerts,
                                            coinName = viewModel.cryptoInfo?.name ?: "",
                                        ),
                                    )
                                    navigationViewModel.deepLink(to = dest)
                                } else if (viewModel.allPriceAlertsCount >= 2 && currentSubscription == null) {
                                    navigationViewModel.openBuyPro(ScreenLabel.CryptoDetail, BuyProAction.PriceAlert)
                                } else {
                                    AnalyticsManager.log(
                                        SheetOpened(ScreenLabel.CryptoDetail, SheetLabel.NewPriceAlert),
                                    )
                                    isAlertByPriceSelected.value = true
                                    priceInput.doubleValue = 0.0
                                    percentInput.doubleValue = 10.0
                                    isRecurringAlertOn.value = false
                                    selectedDirection.value = ChangeDirection.Up
                                    isAddingAlert = false
                                    showAddAlert = true
                                }
                            },
                            shareDetailPage = {
                                context.getActivity()?.let { activity ->
                                    CryptoDetailShareView(
                                        activity = activity,
                                        context = context,
                                        viewModel = viewModel
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
                            name = viewModel.cryptoInfo?.name,
                            currentPrice = viewModel.cryptoInfo?.market_data?.current_price?.getSelectedCurrency(),
                            imageUrl = viewModel.cryptoInfo?.image?.small,
                            imageBitmap = viewModel.cryptoDetailSmallImageBitmap,
                            prices = viewModel.onlyPrices,
                            selectedTimeframe = viewModel.selectedTimeFrame,
                            shortPrice = false,
                            isWebSocketEnabled = viewModel.isWebSocketEnabled,
                            changeCoinAccessibilityHint = "Double tap to change the coin",
                            changeCoin = {
                                if (viewModel.cryptoInfo?.id != null) {
                                    AnalyticsManager.log(
                                        SheetOpened(ScreenLabel.CryptoDetail, SheetLabel.CoinSearch),
                                    )
                                    isSelectingMainCoin = true
                                    isCoinsViewShowing = true
                                }
                            },
                            modifier = Modifier
                                .padding(horizontal = 16.dp)
                        )
                    }

                    item {
                        ChartButtonsView(
                            fromScreen = ScreenLabel.CryptoDetail,
                            selectedGraphType = viewModel.currentChartType.value,
                            isCandleChartEnabled = viewModel.isCandleChartEnabled,
                            isInCompareMode = false,
                            imageCase = ImageCase.Fixed(if(viewModel.isCandleChartEnabled) R.drawable.linechart else R.drawable.candlestick),
                            chartTypeAction = {
                                viewModel.setChartType(it)
                            },
                            toggleCandleChart = {
                                coroutineScope.launch {
                                    viewModel.toggleChart()
                                }
                            }
                        )
                    }

                    item {
                        val chartHeight = (screenHeight.dpToPx() * 0.35).pxToDp()

                        Column(
                            modifier = Modifier
                                .padding(horizontal = 16.dp)
                                .delayTouches()
                                .chartAccessibilityOverlay(
                                    timeStamps = if (viewModel.candleChartEnabled) emptyList() else viewModel.timeStamps,
                                    prices = viewModel.prices,
                                    timeChart = viewModel.selectedTimeFrame,
                                    trailingPadding = biggestValue.value.width,
                                )
                        ) {
                            Box(Modifier.fillMaxWidth()) {
                                CandleChartHoverStatsView(
                                    currentCandleChart = viewModel.getCandleHoverStats,
                                    xPosition = viewModel.hoverCandleTimePosition,
                                    isDragging = viewModel.isDragging && viewModel.isCandleChartEnabled,
                                    hoverCandlePriceTextWidth = viewModel.hoverCandlePriceTextWidth,
                                    horizontalEdgePadding = 0.dp,
                                    modifier = Modifier.alpha(
                                        if (viewModel.isCandleChartEnabled) 1f else 0f,
                                    ),
                                )

                                ChartHoverStats(
                                    isMultiHover = viewModel.hoverHelper.hoverPrices.size == 2,
                                    hoverTimes = viewModel.hoverHelper.hoverTimes,
                                    differenceValue = viewModel.differenceValue,
                                    differencePercent = viewModel.differencePercent,
                                    multiHoverColor = viewModel.multiHoverGraphColor,
                                    hoverPrice = viewModel.hoverPrice,
                                    hoverPriceColor = MaterialTheme.colors.onSurface,
                                    hoverTime = viewModel.hoverTime,
                                    hoverTimePosition = viewModel.hoverTimePosition,
                                    isDragging = viewModel.isDragging && !viewModel.candleChartEnabled,
                                    hoverPriceTextWidth = viewModel.hoverPriceTextWidth,
                                    horizontalEdgePadding = 0.dp,
                                    modifier = Modifier.alpha(
                                        if (viewModel.isCandleChartEnabled) 0f else 1f,
                                    ),
                                )
                            }

                            Box(
                                Modifier
                                    .fillMaxWidth()
                                    .height(chartHeight)
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
                                } else if (viewModel.isCandleChartEnabled) {
                                    CandleChart(
                                        candleSticks = viewModel.candleSticks,
                                        isDragging = viewModel.isDragging,
                                        hoverXPosition = viewModel.candleXPosition.dpToPx(),
                                        pointCandle = viewModel.pointCandle,
                                        chartSize = viewModel.chartSize,
                                        biggestValue = biggestValue.value,
                                        hideFromTalkBack = talkBackEnabled,
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
                                        showLivePriceIndicator = viewModel.isWebSocketEnabled && !viewModel.isCandleChartEnabled && viewModel.currentChartType.value == ChartTypes.PRICE,
                                    )
                                }

                                ChartAccessibilityOverlay(
                                    timeStamps = if (viewModel.candleChartEnabled) emptyList() else viewModel.timeStamps,
                                    prices = viewModel.prices,
                                    timeChart = viewModel.selectedTimeFrame,
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
                            timeStamps = if(viewModel.candleChartEnabled) viewModel.candleTimestamps else viewModel.timeStamps,
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
                            TimeCharts.getButtons(from = TimeCharts.anHour, to = TimeCharts.max),
                            viewModel.selectedTimeFrame,
                            fromScreen = ScreenLabel.CryptoDetail,
                        ) {
                            if (!viewModel.isDragging) {
                                coroutineScope.launch {
                                    viewModel.refresh(
                                        context,
                                        showReload = true,
                                        showRefreshIndicator = false,
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
                            selectedTabIndex = viewModel.currentPage,
                            onTabSelected = {
                                if (it != viewModel.currentPage) {
                                    val tabIndex = it.coerceIn(0, tabs.lastIndex)
                                    AnalyticsManager.log(
                                        TabChanged(
                                            fromScreen = ScreenLabel.CryptoDetail,
                                            selectedTabName = tabs[tabIndex],
                                        )
                                    )
                                }
                                viewModel.currentPage = it

                                coroutineScope.launch {
                                    scrollState.animateScrollToItem(
                                        if (talkBackEnabled) 8 else 7
                                    )
                                }
                            }
                        )
                    }

                    when (viewModel.currentPage) {
                        0 -> tabPageItem {
                            CryptoDetailOverviewView(
                                cryptoDetailInfoModel = viewModel.cryptoInfo,
                                prices = if(viewModel.isCandleChartEnabled) viewModel.candlePrices else viewModel.prices,
                                totalMarketCap = viewModel.globalMarketData?.total_market_cap?.getSelectedCurrency(),
                                timeCharts = viewModel.selectedTimeFrame,
                                alerts = viewModel.alerts,
                                selectedCurrency = selectedCurrency,
                                showCurrencySelector = showCurrencySelector,
                                showSentimentInfo = showSentimentInfo,
                                addNewAlert = {
                                    if (!viewModel.checkIfUserEnabledPush(context)) {
                                        val dest = NavigationScreens.PriceAlerts
                                        AnalyticsManager.log(
                                            Navigate(
                                                fromScreen = ScreenLabel.CryptoDetail,
                                                toScreen = ScreenLabel.PriceAlerts,
                                                coinName = viewModel.cryptoInfo?.name ?: "",
                                            ),
                                        )
                                        navigationViewModel.deepLink(to = dest)
                                    } else if (viewModel.allPriceAlertsCount >= 2 && currentSubscription == null) {
                                        navigationViewModel.openBuyPro(ScreenLabel.CryptoDetail, BuyProAction.PriceAlert)
                                    } else {
                                        AnalyticsManager.log(
                                            SheetOpened(ScreenLabel.CryptoDetail, SheetLabel.NewPriceAlert),
                                        )
                                        isAlertByPriceSelected.value = true
                                        priceInput.doubleValue = 0.0
                                        percentInput.doubleValue = 10.0
                                        isRecurringAlertOn.value = false
                                        selectedDirection.value = ChangeDirection.Up
                                        isAddingAlert = false
                                        showAddAlert = true
                                    }
                                },
                                deleteAlert = {
                                    coroutineScope.launch {
                                        val coinName = viewModel.cryptoInfo?.name ?: ""
                                        navigationViewModel.showSnackbar(
                                            context = context,
                                            type = SnackBarType.SUCCESS,
                                            message = "Price alert deleted"
                                        )
                                        viewModel.deleteAlert(it)
                                        AnalyticsManager.log(
                                            PriceAlertDeleted(ScreenLabel.CryptoDetail, coinName),
                                        )
                                    }
                                }
                            )
                        }
                        1 -> tabPageItem {
                            CryptoDetailAboutView(
                                data = viewModel.toDetailAboutData(),
                                fromScreen = ScreenLabel.CryptoDetail,
                                openUrl = {
                                    selectedUrl = it
                                }
                            )
                        }
                        2 -> {
                            CryptoDetailPortfolioView(
                                viewModel = viewModel,
                                navigationViewModel = navigationViewModel,
                                showAddTransactionSheet = showAddTransactionSheet,
                                hasPro = currentSubscription != null,
                                isTablet = isTablet,
                                showTransactionDetails = {
                                    when (it) {
                                        is AddressImport -> {
                                            val coin = viewModel.portfolio?.pieContaining(it)?.data ?: return@CryptoDetailPortfolioView

                                            val dest = NavigationScreens.WalletDetails(
                                                wallet = it,
                                                coin = coin
                                            )
                                            AnalyticsManager.log(
                                                Navigate(
                                                    fromScreen = ScreenLabel.CryptoDetail,
                                                    toScreen = ScreenLabel.WalletDetails,
                                                    coinName = coin.name ?: "",
                                                ),
                                            )
                                            navigationViewModel.navigate(dest)
                                        }
                                        else -> {
                                            AnalyticsManager.log(
                                                SheetOpened(ScreenLabel.CryptoDetail, SheetLabel.PortfolioTransactionDetails),
                                            )
                                            selectedTransaction = it
                                        }
                                    }
                                }
                            )
                        }
                        3 -> {
                            val remoteConfigReady = viewModel.remoteConfigReady
                            val (config, items) = viewModel.toDetailMarketsData()
                            CryptoDetailMarketsView(
                                config = config,
                                items = items,
                                selectedSortOption = viewModel.selectedSortOption,
                                isSortShowing = isMarketsSortShowing,
                                errorMessage = "coin",
                                fromScreen = ScreenLabel.CryptoDetail,
                                openUrl = {
                                    selectedUrl = it
                                },
                                isTablet = isTablet
                            )
                        }
                        4 -> DetailNews(
                            news = viewModel.newsData,
                            title = "coin",
                            showError = viewModel.showNewsError,
                            isTablet = isTablet,
                            fromScreen = ScreenLabel.CryptoDetail,
                            openUrl = {
                                selectedUrl = it
                            }
                        )
                    }
                }
            }

            ScrollToTopButton(
                visible = !talkBackEnabled && firstVisibleIndex >= 22 && (viewModel.currentPage == 3 || viewModel.currentPage == 4),
                respectNavigationBars = true,
                onClick = {
                    val tabIndex = viewModel.currentPage.coerceIn(0, tabs.lastIndex)
                    AnalyticsManager.log(
                        ScrollToTopPressed(
                            fromScreen = ScreenLabel.CryptoDetail,
                            selectedTabName = tabs[tabIndex],
                        ),
                    )
                    coroutineScope.launch {
                        scrollState.animateScrollToItem(7)
                    }
                }
            )

            /*if (isAddToWidgetShowing) {
                AddToWidgetSheet(
                    widgetsCount = viewModel.widgetCurrencies.count(),
                    deepLinkToWidget = {
                        navigationViewModel.deepLink(NavigationScreens.Widgets)
                        isAddToWidgetShowing = false
                    },
                    close = {
                        isAddToWidgetShowing = false
                    }
                )
            }*/

            if (isEditWidgetShowing) {
                EditWidgetSheet(
                    widgetCoins = viewModel.widgetCurrencies,
                    currentCoinName = viewModel.cryptoInfo?.name,
                    replaceCoin = { toplist ->
                        viewModel.replaceWidgetCurrency(toplist, context = context)
                        isEditWidgetShowing = false

                        val mainSymbol = viewModel.cryptoInfo?.symbol?.uppercase() ?: ""
                        val replacedSymbol = toplist.symbol?.uppercase() ?: ""

                        AnalyticsManager.log(
                            WidgetReplaced(
                                fromScreen = ScreenLabel.CryptoDetail,
                                fromSymbol = mainSymbol,
                                toSymbol = replacedSymbol,
                            ),
                        )

                        navigationViewModel.showSnackbar(
                            context = context,
                            type = SnackBarType.SUCCESS,
                            message = "Replaced $mainSymbol with $replacedSymbol in widgets.",
                            buttonTitle = "Undo",
                            callback = {
                                val toplistItem = viewModel.cryptoInfo?.toToplist() ?: return@showSnackbar

                                AnalyticsManager.log(
                                    WidgetReplaceUndone(
                                        fromScreen = ScreenLabel.CryptoDetail,
                                        fromSymbol = replacedSymbol,
                                        toSymbol = mainSymbol,
                                    ),
                                )

                                viewModel.replaceWidgetCurrency(coin = toplistItem, with = toplist, context)
                            }
                        )
                    },
                    close = {
                        isEditWidgetShowing = false
                    }
                )
            }

            if (isCoinsViewShowing) {
                SelectCoinSheet(
                    searchScreenCase = SearchScreenCase.Search(
                        list = SearchCase.OnlyCoins
                    ),
                    fromScreen = ScreenLabel.CryptoDetail,
                    highlightId = viewModel.cryptoInfo?.id,
                    hideId = null,
                    selectedCoin = { coin ->
                        val id = coin?.id ?: return@SelectCoinSheet

                        if (isSelectingMainCoin) {
                            AnalyticsManager.log(
                                ChangeCompareCoin(
                                    fromScreen = ScreenLabel.CryptoDetail,
                                    selection = CompareCoinChangeSelection.Main,
                                    coinName = coin.name ?: "",
                                ),
                            )
                            coroutineScope.launch {
                                viewModel.newMainCoin(context, id)
                            }
                        } else {
                            val dest = NavigationScreens.CompareCoins(
                                viewModel.cryptoInfo,
                                viewModel.priceVolumeModel,
                                id
                            )
                            AnalyticsManager.log(
                                Navigate(
                                    fromScreen = ScreenLabel.CryptoDetail,
                                    toScreen = ScreenLabel.CompareCoins,
                                            coinName = viewModel.cryptoInfo?.name ?: "",
                                ),
                            )
                            navigationViewModel.navigate(dest)
                        }
                    },
                    close = {
                        isCoinsViewShowing = false
                    }
                )
            }

            if (showCurrencySelector.value) {
                CurrencyOptionsSheet(
                    selectedOption = selectedCurrency,
                    newOptionSelected = {
                        selectedCurrency = it
                        AnalyticsManager.log(
                            CurrencySelected(
                                fromScreen = ScreenLabel.CryptoDetail,
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
                    options = SortOption.exchangeDetailOptions,
                    selectedOption = viewModel.selectedSortOption,
                    newOptionSelected = { option ->
                        AnalyticsManager.log(
                            SortOptionSelected(ScreenLabel.CryptoDetail, option),
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

            if (showSentimentInfo.value) {
                SentimentInfoSheet(
                    close = {
                        showSentimentInfo.value = false
                    }
                )
            }

            if (showAddAlert) {
                NewPriceAlertSheet(
                    sheetState = sheetState,
                    cryptoDetailModel = viewModel.cryptoInfo,
                    selectedCurrency = selectedAlertCurrency,
                    isLoading = isAddingAlert,
                    showError = showAlertError,
                    isAlertByPriceSelected = isAlertByPriceSelected,
                    priceInput = priceInput,
                    percentInput = percentInput,
                    isRecurringAlertOn = isRecurringAlertOn,
                    selectedDirection = selectedDirection,
                    selectNewCurrency = {
                        AnalyticsManager.log(
                            SheetOpened(ScreenLabel.CryptoDetail, SheetLabel.CurrencySelector),
                        )
                        showAddAlert = false
                        isSelectingAlertCurrency = true
                    },
                    addNewAlert = {
                        isAddingAlert = true

                        coroutineScope.launch {
                            when (viewModel.addPriceAlert(it)) {
                                PriceAlertCreateResult.Success -> {
                                    AnalyticsManager.log(
                                        PriceAlertAdded(
                                            fromScreen = ScreenLabel.CryptoDetail,
                                            coinName = viewModel.cryptoInfo?.name ?: "",
                                            isRecurring = isRecurringAlertOn.value,
                                            inputType = if (isAlertByPriceSelected.value) {
                                                PriceAlertInputType.Price
                                            } else {
                                                PriceAlertInputType.Percent
                                            },
                                        ),
                                    )
                                    navigationViewModel.showSnackbar(
                                        context = context,
                                        type = SnackBarType.SUCCESS,
                                        message = "Price alert added"
                                    )
                                    sheetState.hide()
                                    showAddAlert = false
                                }
                                PriceAlertCreateResult.FcmTokenUnavailable -> {
                                    navigationViewModel.showSnackbar(
                                        context = context,
                                        type = SnackBarType.FAILURE,
                                        message = "Something went wrong, restart app and try again"
                                    )
                                }
                                PriceAlertCreateResult.Failed -> {
                                    showAlertError = !showAlertError
                                }
                            }

                            isAddingAlert = false
                        }
                    },
                    close = {
                        coroutineScope.launch {
                            sheetState.hide()
                            showAddAlert = false
                        }
                    }
                )
            }

            if (isSelectingAlertCurrency) {
                CurrencyOptionsSheet(
                    selectedOption = selectedAlertCurrency,
                    newOptionSelected = {
                        selectedAlertCurrency = it
                        AnalyticsManager.log(
                            CurrencySelected(
                                fromScreen = ScreenLabel.CryptoDetail,
                                currency = selectedAlertCurrency.currency,
                            ),
                        )
                    },
                    close = {
                        isSelectingAlertCurrency = false
                        showAddAlert = true
                    }
                )
            }

            if (showAddTransactionSheet.value) {
                SelectTransactionTypeSheet(
                    transactionSelected = {
                        showAddTransactionSheet.value = false

                        it?.let {
                            when {
                                (it == NewTransactionType.Swap) -> {
                                    val dest = NavigationScreens.NewPortfolioSwapTransaction(
                                        transactionType = NewTransactionType.Swap,
                                        currencyDataModel = viewModel.cryptoInfo,
                                        transaction = null,
                                        popToRoot = false
                                    )
                                    AnalyticsManager.log(
                                        Navigate(
                                            fromScreen = ScreenLabel.CryptoDetail,
                                            toScreen = ScreenLabel.NewSwapTransaction,
                                            coinName = viewModel.cryptoInfo?.name ?: "",
                                        ),
                                    )
                                    navigationViewModel.navigate(dest)
                                }

                                else -> {
                                    val dest = NavigationScreens.NewPortfolioTransaction(
                                        transactionType = it,
                                        currencyDataModel = viewModel.cryptoInfo,
                                        transaction = null,
                                        popToRoot = false
                                    )
                                    AnalyticsManager.log(
                                        Navigate(
                                            fromScreen = ScreenLabel.CryptoDetail,
                                            toScreen = ScreenLabel.NewPortfolioTransaction,
                                            coinName = viewModel.cryptoInfo?.name ?: "",
                                        ),
                                    )
                                    navigationViewModel.navigate(dest)
                                }
                            }
                        }
                    }
                )
            }

            selectedTransaction?.let {
                when (val event = it) {
                    is NewTransaction -> {
                        val coin = viewModel.portfolio?.pieContaining(event)?.data

                        TransactionDetailSheet(
                            coin = coin,
                            transaction = event,
                            editTransaction = {
                                selectedTransaction = null
                                val dest = NavigationScreens.NewPortfolioTransaction(
                                    transactionType = it.transactionType,
                                    currencyDataModel = viewModel.portfolio?.pieContaining(it)?.data?.toCryptoDetail(),
                                    transaction = it,
                                    popToRoot = true
                                )
                                AnalyticsManager.log(
                                    Navigate(
                                        fromScreen = ScreenLabel.CryptoDetail,
                                        toScreen = ScreenLabel.NewPortfolioTransaction,
                                        coinName = coin?.name,
                                    ),
                                )
                                navigationViewModel.navigate(dest)
                            },
                            hideData = viewModel.hidePortfolioData,
                            deleteTransaction = {
                                selectedTransaction = null
                                AnalyticsManager.log(
                                    PortfolioTransactionDeleted(
                                        fromScreen = ScreenLabel.CryptoDetail,
                                        coinName = coin?.name ?: "",
                                        kind = PortfolioTransactionDeletedKind.Transaction,
                                    ),
                                )
                                viewModel.deleteTransaction(
                                    transaction = it
                                )
                            },
                            close = {
                                selectedTransaction = null
                            }
                        )
                    }

                    is NewSwapTransaction -> {
                        val swapCoin = viewModel.portfolio?.pieContaining(event)?.data
                        SwapTransactionDetailSheet(
                            coin = swapCoin,
                            transaction = event,
                            editTransaction = {
                                selectedTransaction = null
                                val dest = NavigationScreens.NewPortfolioSwapTransaction(
                                    transactionType = NewTransactionType.Swap,
                                    currencyDataModel = CryptoDetailInfoModel(
                                        id = it.fromCurrencyId,
                                        symbol = it.fromCurrencySymbol
                                    ),
                                    transaction = it,
                                    popToRoot = true
                                )
                                AnalyticsManager.log(
                                    Navigate(
                                        fromScreen = ScreenLabel.CryptoDetail,
                                        toScreen = ScreenLabel.NewSwapTransaction,
                                        coinName = swapCoin?.name,
                                    ),
                                )
                                navigationViewModel.navigate(dest)
                            },
                            hideData = viewModel.hidePortfolioData,
                            deleteTransaction = {
                                selectedTransaction = null
                                AnalyticsManager.log(
                                    PortfolioTransactionDeleted(
                                        fromScreen = ScreenLabel.CryptoDetail,
                                        coinName = swapCoin?.name ?: "",
                                        kind = PortfolioTransactionDeletedKind.Swap,
                                    ),
                                )
                                viewModel.deleteTransaction(
                                    transaction = it
                                )
                            },
                            close = {
                                selectedTransaction = null
                            }
                        )
                    }

                    else -> { }
                }
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
private fun FavoriteButton(
    isFavorited: Boolean,
    coinName: String,
    toggleFavorite: () -> Unit
) {
    Image(
        painter = painterResource(if (isFavorited) R.drawable.favorite_filled else R.drawable.favorite_outline),
        colorFilter = ColorFilter.tint(if (isFavorited) MaterialTheme.colors.primary else MaterialTheme.colors.onSurface),
        contentDescription = if (isFavorited) {
            "Remove $coinName from favorites"
        } else {
            "Add $coinName to favorites"
        },
        modifier = Modifier
            .padding(8.dp)
            .imageSize(24.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
            ) {
                toggleFavorite.invoke()
            }
    )
}

@Composable
private fun MoreButton(
    isDropDownExpanded: MutableState<Boolean>,
    coinName: String,
    isAlreadyAddedToWidgets: Boolean,
    widgetsCount: Int,
    addToWidget: () -> Unit,
    editWidgets: () -> Unit,
    deepLinkToWidgets: () -> Unit,
    compareCoins: () -> Unit,
    addPriceAlert: () -> Unit,
    shareDetailPage: () -> Unit
) {
    IconButton(onClick = {
        isDropDownExpanded.value = true
    }) {
        Box(
            modifier = Modifier.semantics {
                contentDescription = "More options"
            }
        ) {
            Image(
                painter = painterResource(id = R.drawable.more),
                contentDescription = null,
                colorFilter = ColorFilter.tint(
                    MaterialTheme.colors.onSurface
                ),
            )

            DropdownMenu(
                expanded = isDropDownExpanded.value,
                onDismissRequest = {
                    isDropDownExpanded.value = false
                }
            ) {
                DropdownMenuItem(
                    content = {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                painter = painterResource(if (isAlreadyAddedToWidgets) R.drawable.edit else R.drawable.add),
                                contentDescription = null,
                                colorFilter = ColorFilter.tint(MaterialTheme.colors.onSurfaceDim),
                                modifier = Modifier
                                    .imageSize(20.dp)
                            )

                            Text(
                                text = if (isAlreadyAddedToWidgets) "Manage Widgets" else "Add to Widgets",
                                style = satoshiBold(16),
                                color = MaterialTheme.colors.onSurfaceDim
                            )
                        }
                    },
                    onClick = {
                        isDropDownExpanded.value = false
                        if (isAlreadyAddedToWidgets) {
                            deepLinkToWidgets.invoke()
                        } else if (widgetsCount == 8) {
                            editWidgets.invoke()
                        } else {
                            addToWidget.invoke()
                        }
                    }
                )

                Separator()

                DropdownMenuItem(
                    content = {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                painter = painterResource(R.drawable.converter),
                                contentDescription = null,
                                colorFilter = ColorFilter.tint(MaterialTheme.colors.onSurfaceDim),
                                modifier = Modifier
                                    .imageSize(20.dp)
                            )

                            Text(
                                text = "Compare",
                                style = satoshiBold(16),
                                color = MaterialTheme.colors.onSurfaceDim
                            )
                        }
                    },
                    onClick = {
                        isDropDownExpanded.value = false
                        compareCoins.invoke()
                    }
                )

                Separator()

                DropdownMenuItem(
                    modifier = Modifier.semantics(mergeDescendants = true) {
                        contentDescription = "Add new $coinName price alert"
                    },
                    content = {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                painter = painterResource(R.drawable.alert),
                                contentDescription = null,
                                colorFilter = ColorFilter.tint(MaterialTheme.colors.onSurfaceDim),
                                modifier = Modifier
                                    .imageSize(20.dp)
                            )

                            Text(
                                text = "Add Alert",
                                style = satoshiBold(16),
                                color = MaterialTheme.colors.onSurfaceDim
                            )
                        }
                    },
                    onClick = {
                        isDropDownExpanded.value = false
                        addPriceAlert.invoke()
                    }
                )

                Separator()

                DropdownMenuItem(
                    content = {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                painter = painterResource(R.drawable.share),
                                contentDescription = null,
                                colorFilter = ColorFilter.tint(MaterialTheme.colors.onSurfaceDim),
                                modifier = Modifier
                                    .imageSize(20.dp)
                            )

                            Text(
                                text = "Share",
                                style = satoshiBold(16),
                                color = MaterialTheme.colors.onSurfaceDim
                            )
                        }
                    },
                    onClick = {
                        isDropDownExpanded.value = false

                        shareDetailPage.invoke()
                    }
                )
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
    showLivePriceIndicator: Boolean = false,
    showHoverOverlay: Boolean = true,
    statsAreaHeight: Dp = 0.dp,
    fullHeightHoverLine: Boolean = false,
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
        showLivePriceIndicator = showLivePriceIndicator,
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

    if (showHoverOverlay) {
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
                statsAreaHeight = statsAreaHeight,
                fullHeightLine = fullHeightHoverLine,
            )
        }
    }
}

@Composable
private fun CandleChart(
    candleSticks: List<Candle>,
    isDragging: Boolean,
    hoverXPosition: Float,
    pointCandle: Dp,
    chartSize: MutableState<DpSize>,
    biggestValue: DpSize,
    hideFromTalkBack: Boolean = false,
    showHoverOverlay: Boolean = true,
) {
    val localDensity = LocalDensity.current
    val chartModifier = Modifier
        .padding(
            PaddingValues(
                top = biggestValue.height / 2,
                bottom = biggestValue.height / 2,
                end = biggestValue.width,
            ),
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
        .then(
            if (hideFromTalkBack) {
                Modifier.hideFromTalkBackWhenEnabled()
            } else {
                Modifier
            }
        )

    CandleChart(
        candleSticks,
        modifier = chartModifier
    )

    if (showHoverOverlay) {
        Box(
            modifier = if (hideFromTalkBack) {
                Modifier.hideFromTalkBackWhenEnabled()
            } else {
                Modifier
            }
        ) {
            CandleChartHover(
                isDragging = isDragging,
                xPos = hoverXPosition,
                yPos = pointCandle,
                paddings = PaddingValues(
                    top = biggestValue.height / 2,
                    bottom = biggestValue.height / 2,
                    end = biggestValue.width,
                ),
                topPadding = biggestValue.height / 2,
            )
        }
    }
}