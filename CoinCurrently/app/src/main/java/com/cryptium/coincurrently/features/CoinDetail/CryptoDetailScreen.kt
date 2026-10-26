package com.cryptium.coincurrently.features.CoinDetail
import com.cryptium.coincurrently.core.persistence.UserPreferences
import com.cryptium.coincurrently.sharedui.modifiers.imageSize

import com.cryptium.coincurrently.core.networking.ResponseModels.AddressImport
import com.cryptium.coincurrently.app.NavigationScreens
import com.cryptium.coincurrently.app.SearchCase
import com.cryptium.coincurrently.app.SearchScreenCase
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
import com.cryptium.coincurrently.core.config.AppLocale
import com.cryptium.coincurrently.sharedui.theme.*
import com.cryptium.coincurrently.sharedui.modifiers.*
import com.cryptium.coincurrently.sharedui.components.AccessibilityLayout
import com.cryptium.coincurrently.sharedui.components.Separator
import com.cryptium.coincurrently.sharedui.components.conditionalStickyHeader
import com.cryptium.coincurrently.sharedui.components.tabPageItem
import com.cryptium.coincurrently.sharedui.buttons.ScrollToTopButton
import com.cryptium.coincurrently.sharedui.charts.ChartHoverStats
import com.cryptium.coincurrently.sharedui.charts.rememberLineChartHoverUi
import com.cryptium.coincurrently.sharedui.charts.rememberCandleHoverUi
import com.cryptium.coincurrently.sharedui.models.FormattingThreshold
import com.cryptium.coincurrently.core.extensions.formatCurrency
import com.cryptium.coincurrently.features.CoinDetail.Sheets.SentimentInfoSheet
import com.cryptium.coincurrently.features.Portfolio.Models.PortfolioSwapTransaction
import com.cryptium.coincurrently.sharedui.charts.CandleChart
import com.cryptium.coincurrently.sharedui.charts.CandleChartHover
import com.cryptium.coincurrently.core.extensions.getActivity
import com.cryptium.coincurrently.core.extensions.dpToPx
import com.cryptium.coincurrently.core.extensions.is24HourClock
import com.cryptium.coincurrently.core.localization.AppLanguageState
import com.cryptium.coincurrently.core.localization.Localization
import com.cryptium.coincurrently.core.extensions.getSelectedCurrency
import com.cryptium.coincurrently.core.extensions.isTablet
import com.cryptium.coincurrently.core.extensions.pieContaining
import com.cryptium.coincurrently.features.CoinDetail.CryptoDetailViewModel
import com.cryptium.coincurrently.features.CoinDetail.CryptoDetailViewModelFactory
import com.cryptium.coincurrently.core.extensions.pxToDp
import com.cryptium.coincurrently.core.extensions.satoshiBold
import com.cryptium.coincurrently.sharedui.charts.ChartBackground
import com.cryptium.coincurrently.sharedui.charts.ChartTimeStamps
import com.cryptium.coincurrently.sharedui.charts.ChartTimeframeButtonsView
import com.cryptium.coincurrently.sharedui.charts.ChartAccessibilityOverlay
import com.cryptium.coincurrently.sharedui.charts.chartAccessibilityOverlay
import com.cryptium.coincurrently.sharedui.charts.ChartHoverView
import com.cryptium.coincurrently.sharedui.navigation.CustomTopAppBar
import com.cryptium.coincurrently.sharedui.components.ImageCase
import com.cryptium.coincurrently.sharedui.loading.ActivityIndicator
import com.cryptium.coincurrently.features.CoinDetail.Share.CryptoDetailShareCard
import com.cryptium.coincurrently.sharedui.models.ChangeDirection
import com.cryptium.coincurrently.sharedui.models.SortOption
import com.cryptium.coincurrently.app.NavigationViewModel
import com.cryptium.coincurrently.sharedui.components.OnLifecycleEvent
import com.cryptium.coincurrently.sharedui.charts.MultiTouchView
import com.cryptium.coincurrently.sharedui.charts.delayTouches
import com.cryptium.coincurrently.sharedui.charts.hideFromTalkBackWhenEnabled
import com.cryptium.coincurrently.sharedui.charts.LineChart
import com.cryptium.coincurrently.sharedui.components.Point
import com.cryptium.coincurrently.sharedui.sheets.WebViewSheet
import com.cryptium.coincurrently.features.PriceAlerts.PriceAlertCreateResult
import com.cryptium.coincurrently.core.networking.ResponseModels.CryptoDetailInfoModel
import com.cryptium.coincurrently.features.CoinDetail.Views.General.ChartTypeButtonsView
import com.cryptium.coincurrently.core.models.ChartTypes
import com.cryptium.coincurrently.features.CoinDetail.Views.About.CryptoDetailAboutView
import com.cryptium.coincurrently.features.CoinDetail.Views.About.toDetailAboutData
import com.cryptium.coincurrently.features.CoinDetail.Views.General.CandleChartHoverStatsView
import com.cryptium.coincurrently.features.CoinDetail.Views.General.ScrollableTabSelector
import com.cryptium.coincurrently.features.CoinDetail.Views.General.CryptoDetailSummaryView
import com.cryptium.coincurrently.features.CoinDetail.Views.Markets.CryptoDetailMarketsView
import com.cryptium.coincurrently.features.CoinDetail.Views.Markets.toDetailMarketsData
import com.cryptium.coincurrently.features.CoinDetail.Views.Overview.CryptoDetailOverviewView
import com.cryptium.coincurrently.features.CoinDetail.Views.Portfolio.CryptoDetailPortfolioView
import com.cryptium.coincurrently.features.CoinDetail.Sheets.SelectCoinSheet
import com.cryptium.coincurrently.features.Market.Toplist.Sheets.ToplistSortBySheet
import com.cryptium.coincurrently.sharedui.components.SnackBarType
import com.cryptium.coincurrently.core.analytics.ChangeCompareCoin
import com.cryptium.coincurrently.core.analytics.CompareCoinChangeSelection
import com.cryptium.coincurrently.core.analytics.CurrencySelected
import com.cryptium.coincurrently.core.analytics.AnalyticsManager
import com.cryptium.coincurrently.core.analytics.Navigate
import com.cryptium.coincurrently.core.analytics.PortfolioTransactionDeleted
import com.cryptium.coincurrently.core.analytics.PortfolioTransactionDeletedKind
import com.cryptium.coincurrently.core.analytics.PriceAlertAdded
import com.cryptium.coincurrently.core.analytics.PriceAlertDeleted
import com.cryptium.coincurrently.core.analytics.PriceAlertInputType
import com.cryptium.coincurrently.core.analytics.ScreenLabel
import com.cryptium.coincurrently.core.analytics.ScrollToTopPressed
import com.cryptium.coincurrently.core.analytics.SheetLabel
import com.cryptium.coincurrently.core.analytics.SheetOpened
import com.cryptium.coincurrently.core.analytics.SortOptionSelected
import com.cryptium.coincurrently.core.analytics.TabChanged
import com.cryptium.coincurrently.features.Portfolio.DataClasses.PortfolioTransaction
import com.cryptium.coincurrently.features.Portfolio.DataClasses.PortfolioEvent
import com.cryptium.coincurrently.features.Portfolio.Enums.PortfolioTransactionType
import com.cryptium.coincurrently.features.Portfolio.Sheets.SelectTransactionTypeSheet
import com.cryptium.coincurrently.features.Portfolio.Sheets.SwapTransactionDetailSheet
import com.cryptium.coincurrently.features.Portfolio.Sheets.TransactionDetailSheet
import com.cryptium.coincurrently.features.Settings.CurrencySelector.CurrencyOptionsSheet
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.cryptium.coincurrently.core.networking.ResponseModels.Candle
import com.cryptium.coincurrently.core.networking.ResponseModels.CurrencySelectionModel
import com.cryptium.coincurrently.core.networking.ResponseModels.TimeCharts
import com.cryptium.coincurrently.core.networking.ResponseModels.toCryptoDetail
import com.cryptium.coincurrently.core.networking.ResponseModels.toToplist
import kotlin.math.floor
import com.cryptium.coincurrently.features.PriceAlerts.CreatePriceAlertSheet

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

    val hoverUi = rememberLineChartHoverUi(
        hoverHelper = viewModel.hoverHelper,
        isDragging = viewModel.isDragging,
        dragPosition = viewModel.dragPosition.value,
        prices = viewModel.prices,
        timeStamps = viewModel.timeStamps,
        chartSize = viewModel.chartSize.value,
        hoverPriceTextWidth = viewModel.hoverPriceTextWidth.value,
        timeChart = viewModel.selectedTimeFrame,
        changePercent = viewModel.changePercent,
        formatPrice = { it.formatCurrency(threshold = FormattingThreshold.MILLION) },
    )
    val candleHoverUi = rememberCandleHoverUi(
        dragPosition = viewModel.dragPosition.value,
        chartSize = viewModel.chartSize.value,
        candleSticks = viewModel.candleSticks,
        hoverCandlePriceTextWidth = viewModel.hoverCandlePriceTextWidth.value,
        timeChart = viewModel.selectedTimeFrame,
        candleMiddlePrices = viewModel.candleMiddlePrices,
        highestCandleValue = viewModel.highestCandleValue,
        lowestCandleValue = viewModel.lowestCandleValue,
        candleTimestamps = viewModel.candleTimestamps,
    )

    val context = LocalContext.current
    val isTablet = context.isTablet()
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val talkBackEnabled = AccessibilityLayout.isTalkBackEnabled

    val tabs = listOf("Overview", "About", "Portfolio", "Markets")
    val scrollState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val useStickyHeaders = AccessibilityLayout.useStickyHeaders

    var isCoinsViewShowing by remember { mutableStateOf(false) }
    var isSelectingMainCoin by remember { mutableStateOf(false) }

    var selectedUrl: String? by remember { mutableStateOf(null) }

    val showCurrencySelector = remember { mutableStateOf(false) }
    var selectedCurrency by remember { mutableStateOf(UserPreferences.getSelectedCurrency()) }

    val biggestValue = remember { mutableStateOf(DpSize.Zero) }

    val isDropDownExpanded = remember { mutableStateOf(false) }


    val isMarketsSortShowing = remember { mutableStateOf(false) }
    val showSentimentInfo = remember { mutableStateOf(false) }
    var selectedTransaction: PortfolioEvent? by remember { mutableStateOf(null) }

    var showAddAlert by remember { mutableStateOf(false) }
    var isAddingAlert by remember { mutableStateOf(false) }
    val isAlertByPriceSelected = remember { mutableStateOf(true) }
    val isRecurringAlertOn = remember { mutableStateOf(false) }
    val priceInput = remember { mutableDoubleStateOf(0.0) }
    val percentInput = remember { mutableDoubleStateOf(10.0) }
    var selectedAlertCurrency by remember { mutableStateOf(UserPreferences.getSelectedCurrency()) }
    val selectedDirection = remember { mutableStateOf(ChangeDirection.Up) }
    var isSelectingAlertCurrency by remember { mutableStateOf(false) }
    var showAlertError by remember { mutableStateOf(false) }

    val showAddTransactionSheet = remember { mutableStateOf(false ) }

    val firstVisibleIndex by remember {
        derivedStateOf { scrollState.firstVisibleItemIndex }
    }

    LaunchedEffect(Unit) {
        coroutineScope {
            launch { viewModel.getPriceAlerts() }
            launch { viewModel.getGlobalMarketData() }
            launch {
                viewModel.refresh(
                    context,
                    showReload = viewModel.mainReloading,
                    showRefreshIndicator = false,
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

    LaunchedEffect(viewModel.currentPage, AppLanguageState.preference) {
        if (viewModel.currentPage == 2) {
            viewModel.getPortfolio()
        }
    }

    OnLifecycleEvent { _, event ->
        when (event) {
            Lifecycle.Event.ON_RESUME -> {
                coroutineScope.launch {
                    if (viewModel.currentPage == 2) {
                        viewModel.getPortfolio()
                    }
                }
            }
            else -> { }
        }
    }

    val pendingPortfolioTransactionId = navigationViewModel.pendingPortfolioTransactionId
    LaunchedEffect(pendingPortfolioTransactionId) {
        val idStr = pendingPortfolioTransactionId ?: return@LaunchedEffect
        navigationViewModel.pendingPortfolioTransactionId = null

        launch {
            viewModel.getPortfolio()
        }

        val (event, coin) = viewModel.findTransaction(idStr)
        val symbol = coin?.symbol?.uppercase(AppLocale.enUsPosix) ?: Localization.string("Coin")

        when (event) {
            is PortfolioTransaction -> {
                navigationViewModel.showSnackbar(
                    context = context,
                    type = SnackBarType.SUCCESS,
                    message = Localization.format("%@ transaction added to your portfolio!", symbol),
                    buttonTitle = Localization.string("View Details"),
                    callback = {
                        AnalyticsManager.log(
                            SheetOpened(ScreenLabel.CryptoDetail, SheetLabel.PortfolioTransactionDetails),
                        )
                        selectedTransaction = event
                    }
                )
            }

            is PortfolioSwapTransaction -> {
                navigationViewModel.showSnackbar(
                    context = context,
                    type = SnackBarType.SUCCESS,
                    message = Localization.format("%@ transaction added to your portfolio!", symbol),
                    buttonTitle = Localization.string("View Details"),
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
                    message = Localization.format(
                        "%1$@ (%2$@) imported to your portfolio!",
                        shortAddr,
                        symbol,
                    ),
                    buttonTitle = Localization.string("View Details"),
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
                                viewModel.favoriteButtonPressed(context)
                            }
                        )

                        MoreButton(
                            isDropDownExpanded = isDropDownExpanded,
                            coinName = viewModel.cryptoInfo?.name ?: "coin",
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
                                    CryptoDetailShareCard(
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
                        ChartTypeButtonsView(
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
                                    currentCandleChart = candleHoverUi.stats,
                                    xPosition = candleHoverUi.timePosition,
                                    isDragging = viewModel.isDragging && viewModel.isCandleChartEnabled,
                                    hoverCandlePriceTextWidth = viewModel.hoverCandlePriceTextWidth,
                                    horizontalEdgePadding = 0.dp,
                                    modifier = Modifier.alpha(
                                        if (viewModel.isCandleChartEnabled) 1f else 0f,
                                    ),
                                )

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
                                        hoverXPosition = candleHoverUi.xPosition.dpToPx(),
                                        pointCandle = candleHoverUi.pointY,
                                        chartSize = viewModel.chartSize,
                                        biggestValue = biggestValue.value,
                                        hideFromTalkBack = talkBackEnabled,
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
                                            message = Localization.string("Price alert deleted.")
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
                    }
                }
            }

            ScrollToTopButton(
                visible = !talkBackEnabled && firstVisibleIndex >= 22 && viewModel.currentPage == 3,
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
                CreatePriceAlertSheet(
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
                                        message = Localization.string("Price alert added.")
                                    )
                                    sheetState.hide()
                                    showAddAlert = false
                                }
                                PriceAlertCreateResult.FcmTokenUnavailable -> {
                                    navigationViewModel.showSnackbar(
                                        context = context,
                                        type = SnackBarType.FAILURE,
                                        message = Localization.string("Something went wrong. Restart the app and try again.")
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
                                (it == PortfolioTransactionType.Swap) -> {
                                    val dest = NavigationScreens.AddPortfolioSwapTransaction(
                                        transactionType = PortfolioTransactionType.Swap,
                                        currencyDataModel = viewModel.cryptoInfo,
                                        transaction = null,
                                        popToRoot = false
                                    )
                                    AnalyticsManager.log(
                                        Navigate(
                                            fromScreen = ScreenLabel.CryptoDetail,
                                            toScreen = ScreenLabel.PortfolioSwapTransaction,
                                            coinName = viewModel.cryptoInfo?.name ?: "",
                                        ),
                                    )
                                    navigationViewModel.navigate(dest)
                                }

                                else -> {
                                    val dest = NavigationScreens.AddPortfolioTransaction(
                                        transactionType = it,
                                        currencyDataModel = viewModel.cryptoInfo,
                                        transaction = null,
                                        popToRoot = false
                                    )
                                    AnalyticsManager.log(
                                        Navigate(
                                            fromScreen = ScreenLabel.CryptoDetail,
                                            toScreen = ScreenLabel.AddPortfolioTransaction,
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
                    is PortfolioTransaction -> {
                        val coin = viewModel.portfolio?.pieContaining(event)?.data

                        TransactionDetailSheet(
                            coin = coin,
                            transaction = event,
                            editTransaction = {
                                selectedTransaction = null
                                val dest = NavigationScreens.AddPortfolioTransaction(
                                    transactionType = it.transactionType,
                                    currencyDataModel = viewModel.portfolio?.pieContaining(it)?.data?.toCryptoDetail(),
                                    transaction = it,
                                    popToRoot = true
                                )
                                AnalyticsManager.log(
                                    Navigate(
                                        fromScreen = ScreenLabel.CryptoDetail,
                                        toScreen = ScreenLabel.AddPortfolioTransaction,
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

                    is PortfolioSwapTransaction -> {
                        val swapCoin = viewModel.portfolio?.pieContaining(event)?.data
                        SwapTransactionDetailSheet(
                            coin = swapCoin,
                            transaction = event,
                            editTransaction = {
                                selectedTransaction = null
                                val dest = NavigationScreens.AddPortfolioSwapTransaction(
                                    transactionType = PortfolioTransactionType.Swap,
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
                                        toScreen = ScreenLabel.PortfolioSwapTransaction,
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
                WebViewSheet(it) {
                    selectedUrl = null
                }
            }
        }
    }
}
