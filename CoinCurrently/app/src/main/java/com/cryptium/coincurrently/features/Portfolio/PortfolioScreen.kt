package com.cryptium.coincurrently.features.Portfolio
import com.cryptium.coincurrently.core.localization.Localization
import com.cryptium.coincurrently.core.persistence.UserPreferences
import com.cryptium.coincurrently.sharedui.theme.*
import com.cryptium.coincurrently.sharedui.modifiers.*
import com.cryptium.coincurrently.sharedui.components.AccessibilityLayout
import com.cryptium.coincurrently.sharedui.components.AdaptiveStack
import com.cryptium.coincurrently.sharedui.components.conditionalStickyHeader
import com.cryptium.coincurrently.sharedui.buttons.PrimaryButton
import com.cryptium.coincurrently.sharedui.modifiers.imageSize
import com.cryptium.coincurrently.core.extensions.roundTo
import com.cryptium.coincurrently.core.extensions.changeWithSymbol

import com.cryptium.coincurrently.core.networking.ResponseModels.AddressImport
import com.cryptium.coincurrently.app.NavigationScreens
import com.cryptium.coincurrently.app.SearchScreenCase
import com.cryptium.coincurrently.app.SearchCase
import android.app.Activity
import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.material.MaterialTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import androidx.compose.ui.unit.min
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.invisibleToUser
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.traversalIndex
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cryptium.coincurrently.core.config.AppLocale
import com.cryptium.coincurrently.features.Portfolio.Models.PortfolioSwapTransaction
import com.cryptium.coincurrently.core.extensions.*
import com.cryptium.coincurrently.sharedui.charts.ChartBackground
import com.cryptium.coincurrently.sharedui.charts.ChartHoverStats
import com.cryptium.coincurrently.sharedui.charts.rememberLineChartHoverUi
import com.cryptium.coincurrently.sharedui.charts.HoverTimeFallback
import com.cryptium.coincurrently.sharedui.charts.ChartTimeStamps
import com.cryptium.coincurrently.sharedui.charts.ChartAccessibilityOverlay
import com.cryptium.coincurrently.sharedui.charts.ChartHoverView
import com.cryptium.coincurrently.sharedui.charts.MultiTouchView
import com.cryptium.coincurrently.sharedui.charts.delayTouches
import com.cryptium.coincurrently.sharedui.charts.LineChart
import com.cryptium.coincurrently.sharedui.charts.CHART_ACCESSIBILITY_OVERLAY_TALKBACK_TRAVERSAL_INDEX
import com.cryptium.coincurrently.sharedui.charts.DEFERRED_AFTER_CHART_OVERLAY_TALKBACK_TRAVERSAL_INDEX
import com.cryptium.coincurrently.sharedui.charts.chartAccessibilityOverlay
import com.cryptium.coincurrently.sharedui.charts.chartTalkBackSection
import com.cryptium.coincurrently.sharedui.charts.deferTalkBackAfterTabContent
import com.cryptium.coincurrently.sharedui.charts.hideFromTalkBackWhenEnabled
import com.cryptium.coincurrently.sharedui.loading.ActivityIndicator
import com.cryptium.coincurrently.sharedui.models.FormattingThreshold
import com.cryptium.coincurrently.sharedui.loading.ChangeSize
import com.cryptium.coincurrently.app.NavigationViewModel
import com.cryptium.coincurrently.sharedui.components.ObserveCurrencyChanges
import com.cryptium.coincurrently.sharedui.components.OnLifecycleEvent
import com.cryptium.coincurrently.core.analytics.BuyProAction
import com.cryptium.coincurrently.core.analytics.ClearFilters
import com.cryptium.coincurrently.core.analytics.AnalyticsManager
import com.cryptium.coincurrently.core.analytics.Navigate
import com.cryptium.coincurrently.core.analytics.PortfolioDataHide
import com.cryptium.coincurrently.core.analytics.PortfolioDataShow
import com.cryptium.coincurrently.core.analytics.PortfolioTransactionDeleted
import com.cryptium.coincurrently.core.analytics.PortfolioTransactionDeletedKind
import com.cryptium.coincurrently.core.analytics.ScreenLabel
import com.cryptium.coincurrently.core.analytics.SheetLabel
import com.cryptium.coincurrently.core.analytics.SheetOpened
import com.cryptium.coincurrently.core.analytics.TabBarDoubleTapped
import com.cryptium.coincurrently.core.analytics.TabChanged
import com.cryptium.coincurrently.features.CoinDetail.Views.General.ScrollableTabSelector
import com.cryptium.coincurrently.features.CoinDetail.Views.Compare.CompareRectangleView
import com.cryptium.coincurrently.features.CoinDetail.Sheets.SelectCoinSheet
import com.cryptium.coincurrently.features.Portfolio.Views.MyAssetsView
import com.cryptium.coincurrently.features.Portfolio.Views.PortfolioPieChartView
import com.cryptium.coincurrently.features.Portfolio.Views.TransactionsView
import com.cryptium.coincurrently.features.Portfolio.Views.WalletsView
import com.cryptium.coincurrently.features.Portfolio.DataClasses.PortfolioTransaction
import com.cryptium.coincurrently.features.Portfolio.DataClasses.PortfolioEvent
import com.cryptium.coincurrently.features.Portfolio.Enums.DateOption
import com.cryptium.coincurrently.features.Portfolio.Enums.PortfolioTransactionType
import com.cryptium.coincurrently.features.Portfolio.Enums.endDate
import com.cryptium.coincurrently.features.Portfolio.Enums.startDate
import com.cryptium.coincurrently.features.Portfolio.Sheets.DateFieldOption
import com.cryptium.coincurrently.features.Portfolio.Sheets.DateFilterSheet
import com.cryptium.coincurrently.features.Portfolio.Sheets.DateRangePickerSheet
import com.cryptium.coincurrently.features.Portfolio.Sheets.SelectCoinsSheet
import com.cryptium.coincurrently.features.Portfolio.Sheets.SelectDateSheet
import com.cryptium.coincurrently.features.Portfolio.Enums.PortfolioExportFormat
import com.cryptium.coincurrently.features.Portfolio.Sheets.SelectExportFormatSheet
import com.cryptium.coincurrently.features.Portfolio.Sheets.SelectTransactionTypeSheet
import com.cryptium.coincurrently.features.Portfolio.Sheets.SelectTypesSheet
import com.cryptium.coincurrently.features.Portfolio.Views.portfolioChangeAccessibilityLabel
import com.cryptium.coincurrently.features.Portfolio.Sheets.SwapTransactionDetailSheet
import com.cryptium.coincurrently.features.Portfolio.Sheets.TransactionDetailSheet
import com.cryptium.coincurrently.features.Portfolio.ViewModels.PortfolioViewModel
import com.cryptium.coincurrently.sharedui.components.SnackBarType
import com.cryptium.coincurrently.core.purchases.BillingManager
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import com.cryptium.coincurrently.core.networking.ResponseModels.*
import kotlinx.coroutines.delay
import java.util.Date
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class,
    ExperimentalPagerApi::class
)
@SuppressLint("SimpleDateFormat")
@Composable
fun PortfolioScreen(
    navigationViewModel: NavigationViewModel,
    modifier: Modifier
) {
    val context = LocalContext.current
    val viewModel: PortfolioViewModel = viewModel()
    val hoverUi = rememberLineChartHoverUi(
        chartHover = viewModel.chartHover,
        prices = viewModel.prices,
        timeStamps = viewModel.timeStamps,
        chartSize = viewModel.chartSize.value,
        hoverPriceTextWidth = viewModel.hoverPriceTextWidth.value,
        timeChart = viewModel.selectedChartTimeFrame,
        changePercent = viewModel.changePercent,
        formatPrice = {
            if (viewModel.hideData) stars else it.formatCurrency(threshold = FormattingThreshold.MILLION)
        },
        dropLastCount = viewModel.chartDropCount,
        timeFallback = HoverTimeFallback.LocalClock,
        emptyPrice = if (viewModel.hideData) stars else "N/A",
        transformDragPoint = { point ->
            if ((viewModel.portfolio?.isEmpty() != false) || viewModel.prices.all { it == 0.0 }) {
                point.copy(y = viewModel.chartSize.value.height)
            } else {
                point
            }
        },
    )

    val currentSubscription by BillingManager.currentSubscription.observeAsState()
    var doubleBackToExitPressedOnce by remember { mutableStateOf(false) }
    var isRefreshing by remember { mutableStateOf(false) }
    val useStickyHeaders = AccessibilityLayout.useStickyHeaders
    val talkBackEnabled = AccessibilityLayout.isTalkBackEnabled

    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberLazyListState()

    val localDensity = LocalDensity.current
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp

    val biggestValue = remember { mutableStateOf(DpSize.Zero) }
    val isActionMenuExpanded = remember { mutableStateOf(false) }
    var isTransactionTypeSheetShowing by remember { mutableStateOf(false) }
    var isExportFormatSheetShowing by remember { mutableStateOf(false) }
    var isBenchmarkCoinSelectorShowing by remember { mutableStateOf(false) }
    var exportSheetFreeExportsRemaining by remember { mutableStateOf<Int?>(null) }
    var pendingExportFormat by remember { mutableStateOf<PortfolioExportFormat?>(null) }

    val shareLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            viewModel.recordPortfolioExport()
            com.cryptium.coincurrently.features.Achievements.Managers.AchievementManager.increment(
                com.cryptium.coincurrently.features.Achievements.Models.AchievementCounterKey.portfolioExports,
            )
        }
    }

    var selectedTransaction: PortfolioEvent? by remember { mutableStateOf(null) }

    var isSelectingTypeFilter: Boolean by remember { mutableStateOf(false) }
    var isSelectingCoinsFilter: Boolean by remember { mutableStateOf(false) }
    var isSelectingDateFilter: Boolean by remember { mutableStateOf(false) }
    var isSelectingDates: Boolean by remember { mutableStateOf(false) }
    var temporaryCustomDate: DateOption by remember { mutableStateOf(DateOption.Custom(null, null)) }

    var isPickingStartDate: Boolean by remember { mutableStateOf(false) }
    var isDatePickerShowing: Boolean by remember { mutableStateOf(false) }
    var isPieChartShowing by remember { mutableStateOf(false) }
    var didRestorePieChartPreference by remember { mutableStateOf(false) }
    var selectedPieSliceId by remember { mutableStateOf<String?>(null) }
    var lineChartHeight by remember { mutableStateOf(0.dp) }
    val chartPlotHeight = (screenHeight.dpToPx() * 0.35).pxToDp()

    val tabs = listOf("Assets", "Transactions", "Wallets")

    var previousPortfolioTabIndex by remember { mutableIntStateOf(-1) }
    LaunchedEffect(Unit) {
        snapshotFlow { viewModel.currentPage }
            .collectLatest { page ->
                if (page in tabs.indices &&
                    previousPortfolioTabIndex >= 0 &&
                    previousPortfolioTabIndex != page
                ) {
                    AnalyticsManager.log(
                        TabChanged(ScreenLabel.Portfolio, tabs[page]),
                    )
                }
                if (page in tabs.indices) {
                    previousPortfolioTabIndex = page
                }
            }
    }

    LaunchedEffect(Unit) {
        viewModel.initializeFromCache()
        viewModel.getPortfolioAndCurrencies(context)
        viewModel.refreshImportWallets()
    }

    LaunchedEffect(Unit) {
        if (!didRestorePieChartPreference && lineChartHeight > 0.dp) {
            delay(0.1.seconds)
            isPieChartShowing = UserPreferences.getPortfolioPieChartShowing()
            didRestorePieChartPreference = true
        }
    }

    ObserveCurrencyChanges(navigationViewModel) {
        viewModel.getPortfolioAndCurrencies(context)
    }

    val onPortfolioTabSelected: (Int) -> Unit = { tabIndex ->
        viewModel.currentPage = tabIndex

        coroutineScope.launch {
            scrollState.animateScrollToItem(2)
        }
    }

    OnLifecycleEvent { _, event ->
        when (event) {
            Lifecycle.Event.ON_RESUME -> {
                coroutineScope.launch {
                    viewModel.getPortfolioAndCurrencies(context)
                    viewModel.refreshImportWallets()
                }
            }
            else -> { }
        }
    }

    LaunchedEffect(viewModel.typeFilters.value) {
        viewModel.setGroupTransactions()
    }

    LaunchedEffect(viewModel.dateFilter.value) {
        viewModel.setGroupTransactions()
    }

    LaunchedEffect(viewModel.coinFilters.value) {
        viewModel.setGroupTransactions()
    }

    val pendingPortfolioTransactionId = navigationViewModel.pendingPortfolioTransactionId
    val pendingPortfolioOpenTransactionId = navigationViewModel.pendingPortfolioOpenTransactionId

    LaunchedEffect(pendingPortfolioOpenTransactionId) {
        val idStr = pendingPortfolioOpenTransactionId ?: return@LaunchedEffect
        navigationViewModel.pendingPortfolioOpenTransactionId = null

        val (event, _) = viewModel.findTransaction(idStr)
        if (event != null) {
            AnalyticsManager.log(
                SheetOpened(ScreenLabel.Portfolio, SheetLabel.PortfolioTransactionDetails),
            )
            selectedTransaction = event
        }
    }

    LaunchedEffect(pendingPortfolioTransactionId) {
        val idStr = pendingPortfolioTransactionId ?: return@LaunchedEffect
        val previousTransactionCount = navigationViewModel.pendingPortfolioPreviousTransactionCount
        navigationViewModel.pendingPortfolioTransactionId = null
        navigationViewModel.pendingPortfolioPreviousTransactionCount = -1

        var (event, coin) = viewModel.findTransaction(idStr)
        if (event == null) {
            kotlinx.coroutines.delay(100)
            val retry = viewModel.findTransaction(idStr)
            event = retry.first
            coin = retry.second
        }

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
                            SheetOpened(ScreenLabel.Portfolio, SheetLabel.PortfolioTransactionDetails),
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
                            SheetOpened(ScreenLabel.Portfolio, SheetLabel.PortfolioTransactionDetails),
                        )
                        selectedTransaction = event
                    }
                )
            }

            is AddressImport -> {
                val wallet = event
                val topCoin = coin ?: return@LaunchedEffect
                val addr = wallet.address.uppercase(AppLocale.enUsPosix)
                val shortAddr = if (addr.length >= 10) "${addr.take(5)}...${addr.takeLast(5)}" else addr

                navigationViewModel.showSnackbar(
                    context = context,
                    type = SnackBarType.SUCCESS,
                    message = Localization.format(
                        "%1\$@ (%2\$@) imported to your portfolio!",
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
                                fromScreen = ScreenLabel.Portfolio,
                                toScreen = ScreenLabel.WalletDetails,
                                coinName = topCoin.name ?: "",
                            ),
                        )
                        navigationViewModel.navigate(dest)
                    }
                )
            }

            else -> {
                navigationViewModel.showSnackbar(
                    context = context,
                    type = SnackBarType.SUCCESS,
                    message = Localization.format("%@ transaction added to your portfolio!", symbol),
                )
            }
        }

        if (previousTransactionCount >= 0) {
            com.cryptium.coincurrently.features.Achievements.Managers.AchievementManager
                .checkPortfolioTransactionUnlock(previousTransactionCount)
        }
    }

    BackHandler(onBack = {
        val offset = scrollState.firstVisibleItemIndex + scrollState.firstVisibleItemScrollOffset

        if (offset > 0) {
            AnalyticsManager.log(TabBarDoubleTapped(ScreenLabel.Portfolio))
            coroutineScope.launch {
                scrollState.animateScrollToItem(0)
            }
        } else {
            if (!doubleBackToExitPressedOnce) {
                Toast.makeText(context, "Press back again to exit the app", Toast.LENGTH_SHORT)
                    .show()
                doubleBackToExitPressedOnce = true
                Handler(Looper.getMainLooper()).postDelayed({
                    doubleBackToExitPressedOnce = false
                }, 2500L)
            } else {
                context.getActivity()?.finishAffinity()
            }
        }
    })

    Box(
        contentAlignment = Alignment.BottomEnd,
        modifier = if (talkBackEnabled) {
            Modifier.semantics { isTraversalGroup = true }
        } else {
            Modifier
        }
    ) {
        SwipeRefresh(
            state = rememberSwipeRefreshState(isRefreshing = isRefreshing),
            modifier = modifier.then(
                if (talkBackEnabled) {
                    Modifier.semantics { traversalIndex = 0f }
                } else {
                    Modifier
                }
            ),
            onRefresh = {
                isRefreshing = true

                viewModel.refreshImportWallets()
                viewModel.getPortfolioAndCurrencies(context) {
                    isRefreshing = false
                }

                Handler(Looper.getMainLooper()).postDelayed({
                    isRefreshing = false
                }, 10_000L)
            }
        ) {
            LazyColumn(
                state = scrollState,
                userScrollEnabled = !viewModel.isDragging,
                contentPadding = PaddingValues(
                    top = 16.dp,
                    bottom = 56.dp
                ),
                modifier = Modifier
                    .fillMaxSize()
            ) {
                item {
                    TotalValueView(
                        totalValue = viewModel.totalValue,
                        hideData = viewModel.hideData,
                        isPieChartShowing = isPieChartShowing,
                        togglePieChart = {
                            selectedPieSliceId = null
                            isPieChartShowing = !isPieChartShowing
                            UserPreferences.setPortfolioPieChartShowing(isPieChartShowing)
                        },
                        toggleHideData = {
                            viewModel.toggleHideData()
                            if (viewModel.hideData) {
                                AnalyticsManager.log(PortfolioDataHide(ScreenLabel.Portfolio))
                            } else {
                                AnalyticsManager.log(PortfolioDataShow(ScreenLabel.Portfolio))
                            }
                        }
                    )
                }

                item {
                    Column(
                        modifier = if (talkBackEnabled) {
                            Modifier.semantics { isTraversalGroup = true }
                        } else {
                            Modifier
                        }
                    ) {
                        val isHorizontal = AccessibilityLayout.isHorizontal
                        val valueChangeTalkBackModifier = if (talkBackEnabled) {
                            Modifier.semantics { traversalIndex = 0f }
                        } else {
                            Modifier
                        }
                        val profitLossTalkBackModifier = if (talkBackEnabled) {
                            Modifier.semantics { traversalIndex = 0.5f }
                        } else {
                            Modifier
                        }

                        AdaptiveStack(
                            isHorizontal = isHorizontal,
                            spacing = if (isHorizontal) 16.dp else 8.dp,
                            horizontalAlignment = Alignment.Start,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .padding(
                                    PaddingValues(
                                        top = 12.dp,
                                        start = 16.dp,
                                        end = 16.dp,
                                        bottom = 16.dp
                                    )
                                )
                        ) {
                            ValueChangeView(
                                changePercent = viewModel.changePercent,
                                changeValue = viewModel.changeValue,
                                hideData = viewModel.hideData,
                                isEmpty = viewModel.portfolio.isNullOrEmpty(),
                                modifier = sectionModifier().then(valueChangeTalkBackModifier),
                            )

                            ProfitLossView(
                                changePercent = viewModel.profitLossPercent,
                                changeValue = viewModel.profitLossValue,
                                hideData = viewModel.hideData,
                                isEmpty = viewModel.portfolio.isNullOrEmpty(),
                                modifier = sectionModifier().then(profitLossTalkBackModifier),
                            )
                        }

                        Column(
                            modifier = Modifier
                                .chartTalkBackSection()
                                .then(
                                    if (talkBackEnabled) {
                                        Modifier.semantics {
                                            traversalIndex =
                                                CHART_ACCESSIBILITY_OVERLAY_TALKBACK_TRAVERSAL_INDEX
                                        }
                                    } else {
                                        Modifier
                                    }
                                )
                        ) {
                            AnimatedContent(
                                targetState = isPieChartShowing,
                                transitionSpec = {
                                    if (targetState) {
                                        slideInHorizontally(
                                            initialOffsetX = { fullWidth -> fullWidth },
                                            animationSpec = tween(400),
                                        ) togetherWith slideOutHorizontally(
                                            targetOffsetX = { fullWidth -> -fullWidth },
                                            animationSpec = tween(400),
                                        )
                                    } else {
                                        slideInHorizontally(
                                            initialOffsetX = { fullWidth -> -fullWidth },
                                            animationSpec = tween(400),
                                        ) togetherWith slideOutHorizontally(
                                            targetOffsetX = { fullWidth -> fullWidth },
                                            animationSpec = tween(400),
                                        )
                                    }
                                },
                                label = "portfolioChartToggle",
                            ) { showingPieChart ->
                                if (showingPieChart) {
                                    PortfolioPieChartView(
                                        slices = viewModel.portfolioPieSlices,
                                        selectedSliceId = selectedPieSliceId,
                                        onSelectedSliceIdChange = { selectedPieSliceId = it },
                                        isLoading = viewModel.isDownloading,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(lineChartHeight.coerceAtLeast(1.dp))
                                            .padding(
                                                horizontal = 16.dp,
                                                vertical = 48.dp,
                                            ),
                                    )
                                } else {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .onGloballyPositioned { coordinates ->
                                                lineChartHeight = with(localDensity) {
                                                    coordinates.size.height.toDp()
                                                }
                                            },
                                    ) {
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
                                                    .height(chartPlotHeight)
                                                .delayTouches()
                                                .chartAccessibilityOverlay(
                                                    timeStamps = if (viewModel.portfolio.isNullOrEmpty()) {
                                                        emptyList()
                                                    } else {
                                                        viewModel.getTimeStamps(dropLastTwo = viewModel.chartDropCount > 0)
                                                    },
                                                    prices = if (viewModel.portfolio.isNullOrEmpty()) {
                                                        emptyList()
                                                    } else {
                                                        viewModel.getPrices(dropLastTwo = viewModel.chartDropCount > 0)
                                                    },
                                                    timeChart = viewModel.selectedChartTimeFrame,
                                                    trailingPadding = biggestValue.value.width,
                                                    comparePrices = if (viewModel.benchmarkPrices.isEmpty()) {
                                                        null
                                                    } else {
                                                        viewModel.getBenchmarkPrices(dropLastTwo = viewModel.chartDropCount > 0)
                                                    },
                                                    mainSeriesName = if (viewModel.benchmarkCoin == null) {
                                                        null
                                                    } else {
                                                        Localization.string("Portfolio")
                                                    },
                                                    compareSeriesName = viewModel.benchmarkCoin?.name,
                                                    priceFormatter = { price ->
                                                        if (viewModel.hideData) stars else price.formatCurrency(accessible = true)
                                                    },
                                                )
                                                .padding(horizontal = 16.dp)
                                        ) {
                                            Box(modifier = Modifier.hideFromTalkBackWhenEnabled()) {
                                                ChartBackground(
                                                    values = viewModel.graphStats,
                                                    biggestValue = biggestValue
                                                )
                                            }

                                            if (viewModel.isDownloading) {
                                                ActivityIndicator(
                                                    modifier = Modifier.hideFromTalkBackWhenEnabled(),
                                                )
                                            } else {
                                                LineChart(
                                                    prices = viewModel.prices,
                                                    color = hoverUi.chartColor,
                                                    dragIndex = if (viewModel.isDragging) hoverUi.dragIndex else null,
                                                    isEmptyAtBottom = true,
                                                    isDragging = viewModel.isDragging,
                                                    dropCount = viewModel.chartDropCount,
                                                    dragPositionXs = if (viewModel.isDragging) {
                                                        hoverUi.dragPositions.map { with(localDensity) { it.x.toPx() } }
                                                    } else {
                                                        emptyList()
                                                    },
                                                    paddings = PaddingValues(
                                                        top = biggestValue.value.height / 2,
                                                        bottom = biggestValue.value.height / 2,
                                                        end = biggestValue.value.width
                                                    ),
                                                    modifier = Modifier
                                                        .onGloballyPositioned { coordinates ->
                                                            val sizePx = coordinates.size
                                                            viewModel.chartSize.value = with(localDensity) {
                                                                DpSize(
                                                                    sizePx.width.toDp(),
                                                                    sizePx.height.toDp()
                                                                )
                                                            }
                                                        }
                                                        .hideFromTalkBackWhenEnabled()
                                                )

                                                if (viewModel.benchmarkPrices.isNotEmpty()) {
                                                    LineChart(
                                                        prices = viewModel.benchmarkPrices,
                                                        color = MaterialTheme.colors.primary,
                                                        dragIndex = if (viewModel.isDragging) hoverUi.dragIndex else null,
                                                        showFill = false,
                                                        isEmptyAtBottom = true,
                                                        isDragging = viewModel.isDragging,
                                                        dropCount = viewModel.chartDropCount,
                                                        dash = floatArrayOf(8f, 5f),
                                                        dragPositionXs = if (viewModel.isDragging) {
                                                            hoverUi.dragPositions.map { with(localDensity) { it.x.toPx() } }
                                                        } else {
                                                            emptyList()
                                                        },
                                                        paddings = PaddingValues(
                                                            top = biggestValue.value.height / 2,
                                                            bottom = biggestValue.value.height / 2,
                                                            end = biggestValue.value.width
                                                        ),
                                                        modifier = Modifier.hideFromTalkBackWhenEnabled(),
                                                    )
                                                }

                                                Box(modifier = Modifier.hideFromTalkBackWhenEnabled()) {
                                                    ChartHoverView(
                                                        positions = hoverUi.dragPositions,
                                                        isDragging = viewModel.isDragging,
                                                        color = hoverUi.chartColor,
                                                        topPadding = biggestValue.value.height / 2,
                                                    )
                                                }
                                            }

                                            ChartAccessibilityOverlay(
                                                timeStamps = if (viewModel.portfolio.isNullOrEmpty()) {
                                                    emptyList()
                                                } else {
                                                    viewModel.getTimeStamps(dropLastTwo = viewModel.chartDropCount > 0)
                                                },
                                                prices = if (viewModel.portfolio.isNullOrEmpty()) {
                                                    emptyList()
                                                } else {
                                                    viewModel.getPrices(dropLastTwo = viewModel.chartDropCount > 0)
                                                },
                                                timeChart = viewModel.selectedChartTimeFrame,
                                                trailingPadding = biggestValue.value.width,
                                                comparePrices = if (viewModel.benchmarkPrices.isEmpty()) {
                                                    null
                                                } else {
                                                    viewModel.getBenchmarkPrices(dropLastTwo = viewModel.chartDropCount > 0)
                                                },
                                                mainSeriesName = if (viewModel.benchmarkCoin == null) {
                                                    null
                                                } else {
                                                    Localization.string("Portfolio")
                                                },
                                                compareSeriesName = viewModel.benchmarkCoin?.name,
                                                priceFormatter = { price ->
                                                    if (viewModel.hideData) stars else price.formatCurrency(accessible = true)
                                                },
                                            )

                                            MultiTouchView(
                                                modifier = Modifier.matchParentSize(),
                                                onTouchesChanged = { touchXs ->
                                                    viewModel.portfolio?.let {
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

                                        ChartTimeStamps(
                                            viewModel.selectedChartTimeFrame,
                                            viewModel.getTimeStamps(dropLastTwo = viewModel.chartDropCount > 0),
                                            modifier = Modifier
                                                .layout { measurable, constraints ->
                                                    val placeable = measurable.measure(constraints)
                                                    val overlap = (biggestValue.value.height / 2).roundToPx()
                                                    layout(
                                                        placeable.width,
                                                        (placeable.height - overlap).coerceAtLeast(0),
                                                    ) {
                                                        placeable.placeRelative(0, -overlap)
                                                    }
                                                }
                                                .padding(
                                                    start = 16.dp,
                                                    end = 16.dp + biggestValue.value.width,
                                                    bottom = (if (viewModel.benchmarkCoin == null) 32 else 16).dp + biggestValue.value.height / 2,
                                                )
                                                .hideFromTalkBackWhenEnabled()
                                        )

                                        viewModel.benchmarkCoin?.let { benchmarkCoin ->
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(bottom = 32.dp),
                                            ) {
                                                CompareRectangleView(color = hoverUi.chartColor)

                                                Text(
                                                    text = Localization.string("Portfolio"),
                                                    style = satoshiBold(16),
                                                    color = MaterialTheme.colors.onSurfaceDimmer,
                                                    modifier = Modifier.padding(end = 24.dp),
                                                )

                                                CompareRectangleView(color = MaterialTheme.colors.primary)

                                                Text(
                                                    text = benchmarkCoin.symbol?.uppercase(AppLocale.enUsPosix) ?: "",
                                                    style = satoshiBold(16),
                                                    color = MaterialTheme.colors.onSurfaceDimmer,
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        if (talkBackEnabled) {
                            Box(
                                modifier = Modifier.semantics {
                                    traversalIndex =
                                        DEFERRED_AFTER_CHART_OVERLAY_TALKBACK_TRAVERSAL_INDEX
                                }
                            ) {
                                ScrollableTabSelector(
                                    tabs = tabs,
                                    selectedTabIndex = viewModel.currentPage,
                                    onTabSelected = onPortfolioTabSelected,
                                )
                            }
                        }
                    }
                }

                if (!talkBackEnabled) {
                    conditionalStickyHeader(
                        useSticky = useStickyHeaders,
                        talkBackEnabled = talkBackEnabled,
                    ) {
                        ScrollableTabSelector(
                            tabs = tabs,
                            selectedTabIndex = viewModel.currentPage,
                            onTabSelected = onPortfolioTabSelected,
                        )
                    }
                }

                if (viewModel.currentPage == 0) {
                    MyAssetsView(
                        portfolio = viewModel.portfolio,
                        allPrices = viewModel.portfolioAllCurrencies,
                        hideData = viewModel.hideData,
                        isTablet = context.isTablet(),
                        navigateToCoin = {
                            val dest = NavigationScreens.CryptoDetail(
                                cryptoDetailInfoModel = it.toCryptoDetail(),
                            )
                            AnalyticsManager.log(
                                Navigate(
                                    fromScreen = ScreenLabel.Portfolio,
                                    toScreen = ScreenLabel.CryptoDetail,
                                    coinName = it.name ?: "",
                                ),
                            )
                            navigationViewModel.navigate(dest)
                        }
                    )
                } else if (viewModel.currentPage == 1) {
                    TransactionsView(
                        portfolio = viewModel.portfolio,
                        groupedTransactions = viewModel.groupedTransactions,
                        prices = viewModel.portfolioAllCurrencies,
                        hideData = viewModel.hideData,
                        typeFilters = viewModel.typeFilters.value,
                        dateFilter = viewModel.dateFilter.value,
                        coinFilters = viewModel.coinFilters.value,
                        typeFilterPressed = {
                            AnalyticsManager.log(
                                SheetOpened(ScreenLabel.Portfolio, SheetLabel.PortfolioTypeFilter),
                            )
                            isSelectingTypeFilter = true
                        },
                        dateFilterPressed = {
                            AnalyticsManager.log(
                                SheetOpened(ScreenLabel.Portfolio, SheetLabel.PortfolioDateFilter),
                            )
                            isSelectingDateFilter = true
                        },
                        coinFilterPressed = {
                            AnalyticsManager.log(
                                SheetOpened(ScreenLabel.Portfolio, SheetLabel.PortfolioCoinFilter),
                            )
                            isSelectingCoinsFilter = true
                        },
                        clearFilters = {
                            AnalyticsManager.log(ClearFilters(ScreenLabel.Portfolio))
                            viewModel.typeFilters.value = arrayListOf()
                            viewModel.coinFilters.value = arrayListOf()
                            viewModel.dateFilter.value = DateOption.All
                            temporaryCustomDate = DateOption.Custom(null, null)
                        },
                        showTransactionDetails = {
                            when (it) {
                                is AddressImport -> {
                                    val coin = viewModel.portfolio?.pieContaining(it)?.data ?: return@TransactionsView

                                    val dest = NavigationScreens.WalletDetails(
                                        wallet = it,
                                        coin = coin
                                    )
                                    AnalyticsManager.log(
                                        Navigate(
                                            fromScreen = ScreenLabel.Portfolio,
                                            toScreen = ScreenLabel.WalletDetails,
                                            coinName = coin.name ?: "",
                                        ),
                                    )
                                    navigationViewModel.navigate(dest)
                                }
                                else -> {
                                    AnalyticsManager.log(
                                        SheetOpened(ScreenLabel.Portfolio, SheetLabel.PortfolioTransactionDetails),
                                    )
                                    selectedTransaction = it
                                }
                            }
                        }
                    )
                } else if (viewModel.currentPage == 2) {
                    WalletsView(
                        portfolio = viewModel.portfolio,
                        groupedTransactions = viewModel.groupedTransactions,
                        prices = viewModel.portfolioAllCurrencies,
                        hideData = viewModel.hideData,
                        showTransactionDetails = {
                            val wallet = it as? AddressImport ?: return@WalletsView
                            val coin = viewModel.portfolio?.pieContaining(it)?.data ?: return@WalletsView

                            val dest = NavigationScreens.WalletDetails(
                                wallet = wallet,
                                coin = coin
                            )
                            AnalyticsManager.log(
                                Navigate(
                                    fromScreen = ScreenLabel.Portfolio,
                                    toScreen = ScreenLabel.WalletDetails,
                                    coinName = coin.name ?: "",
                                ),
                            )
                            navigationViewModel.navigate(dest)
                        }
                    )
                }
            }
        }

        ActionButtons(
            isActionMenuExpanded = isActionMenuExpanded,
            hasTransactions = (viewModel.portfolio?.allTransactionsCount ?: 0) > 0,
            benchmarkCoin = viewModel.benchmarkCoin,
            addTransaction = {
                if ((viewModel.portfolio?.allTransactionsCount ?: 0) >= 3 && currentSubscription == null) {
                    navigationViewModel.openBuyPro(ScreenLabel.Portfolio, BuyProAction.AddTransaction)
                } else {
                    AnalyticsManager.log(
                        SheetOpened(ScreenLabel.Portfolio, SheetLabel.PortfolioAddTransactionType),
                    )
                    isTransactionTypeSheetShowing = true
                }
            },
            importWallet = {
                if (currentSubscription == null) {
                    navigationViewModel.openBuyPro(ScreenLabel.Portfolio, BuyProAction.ImportWallet)
                } else {
                    val dest = NavigationScreens.ImportWalletTransaction(
                        popToRoot = true
                    )
                    AnalyticsManager.log(
                        Navigate(
                            fromScreen = ScreenLabel.Portfolio,
                            toScreen = ScreenLabel.ImportWalletTransaction,
                        ),
                    )
                    navigationViewModel.navigate(dest)
                }
            },
            exportPortfolio = {
                if ((viewModel.portfolio?.allTransactionsCount ?: 0) > 0) {
                    if (viewModel.canExportPortfolio()) {
                        exportSheetFreeExportsRemaining = if (currentSubscription != null) {
                            null
                        } else {
                            viewModel.remainingFreePortfolioExports
                        }
                        AnalyticsManager.log(
                            SheetOpened(ScreenLabel.Portfolio, SheetLabel.PortfolioExportFormat),
                        )
                        isExportFormatSheetShowing = true
                    } else {
                        navigationViewModel.openBuyPro(ScreenLabel.Portfolio, BuyProAction.ExportPortfolio)
                    }
                }
            },
            selectBenchmark = {
                AnalyticsManager.log(
                    SheetOpened(ScreenLabel.Portfolio, SheetLabel.CoinSearch),
                )
                isBenchmarkCoinSelectorShowing = true
            },
            removeBenchmark = {
                viewModel.clearBenchmark()
            },
            modifier = modifier.deferTalkBackAfterTabContent()
        )

        if (isBenchmarkCoinSelectorShowing) {
            SelectCoinSheet(
                searchScreenCase = SearchScreenCase.Search(
                    list = SearchCase.OnlyCoins,
                ),
                fromScreen = ScreenLabel.Portfolio,
                highlightId = viewModel.benchmarkCoin?.id,
                hideId = null,
                selectedCoin = { coin ->
                    coin?.id?.let { viewModel.setBenchmarkCoin(it) }
                },
                close = {
                    isBenchmarkCoinSelectorShowing = false
                },
            )
        }

        if (isExportFormatSheetShowing) {
            SelectExportFormatSheet(
                freeExportsRemaining = exportSheetFreeExportsRemaining,
                formatSelected = { format ->
                    isExportFormatSheetShowing = false
                    pendingExportFormat = format
                },
            )
        }

        LaunchedEffect(pendingExportFormat) {
            val format = pendingExportFormat ?: return@LaunchedEffect
            pendingExportFormat = null
            viewModel.exportPortfolio(
                context = context,
                format = format,
                launcher = shareLauncher,
            )
        }

        if (isTransactionTypeSheetShowing) {
            SelectTransactionTypeSheet(
                transactionSelected = {
                    isTransactionTypeSheetShowing = false

                    it?.let { transactionType ->
                        val dest = NavigationScreens.Search(
                            screenCase = SearchScreenCase.Transaction(
                                transactionType = transactionType
                            ),
                        )
                        AnalyticsManager.log(
                            Navigate(
                                fromScreen = ScreenLabel.Portfolio,
                                toScreen = ScreenLabel.Search,
                            ),
                        )
                        navigationViewModel.navigate(dest)
                    }
                }
            )
        }

        if (isSelectingTypeFilter) {
            SelectTypesSheet(
                fromScreen = ScreenLabel.Portfolio,
                transactionTypes = viewModel.typeFilters,
                close = {
                    isSelectingTypeFilter = false
                }
            )
        }

        if (isSelectingDateFilter) {
            SelectDateSheet(
                fromScreen = ScreenLabel.Portfolio,
                selectedOption = viewModel.dateFilter,
                selectCustomDate = {
                    isSelectingDateFilter = false
                    isSelectingDates = true
                }, close = {
                    isSelectingDateFilter = false
                }
            )
        }

        if (isSelectingDates) {
            DateFilterSheet(
                fromScreen = ScreenLabel.Portfolio,
                startDate = temporaryCustomDate.startDate,
                endDate = temporaryCustomDate.endDate,
                selectDate = {
                    isPickingStartDate = it == DateFieldOption.start
                    isDatePickerShowing = true
                },
                clearDate = {
                    temporaryCustomDate = DateOption.Custom(null, null)
                    viewModel.dateFilter.value = DateOption.All
                },
                applyDate = {
                    viewModel.dateFilter.value = it
                    isSelectingDates = false
                },
                close = {
                    isSelectingDates = false
                }
            )
        }

        if (isDatePickerShowing) {
            DateRangePickerSheet(
                fromScreen = ScreenLabel.Portfolio,
                startDate = if(isPickingStartDate) temporaryCustomDate.startDate ?: Date() else  temporaryCustomDate.endDate ?: Date(),
                close = {
                    isDatePickerShowing = false
                    isSelectingDates = true
                },
                applyDate = {
                    var start = temporaryCustomDate.startDate
                    var end = temporaryCustomDate.endDate

                    if (isPickingStartDate) {
                        start = it
                    } else {
                        end = it
                    }

                    // 🔒 Enforce valid order
                    if (start != null && end != null && start.after(end)) {
                        if (isPickingStartDate) {
                            end = start
                        } else {
                            start = end
                        }
                    }

                    temporaryCustomDate = DateOption.Custom(
                        startDate = start,
                        endDate = end
                    )
                }
            )
        }

        if (isSelectingCoinsFilter) {
            SelectCoinsSheet(
                fromScreen = ScreenLabel.Portfolio,
                coins = viewModel.coinFilterList,
                selectedCoins = viewModel.coinFilters,
                close = {
                    isSelectingCoinsFilter = false
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
                                    fromScreen = ScreenLabel.Portfolio,
                                    toScreen = ScreenLabel.AddPortfolioTransaction,
                                    coinName = coin?.name ?: "",
                                ),
                            )
                            navigationViewModel.navigate(dest)
                        },
                        hideData = viewModel.hideData,
                        deleteTransaction = { tx ->
                            val coinName = viewModel.portfolio?.pieContaining(tx)?.data?.name ?: ""
                            selectedTransaction = null
                            AnalyticsManager.log(
                                PortfolioTransactionDeleted(
                                    fromScreen = ScreenLabel.Portfolio,
                                    coinName = coinName,
                                    kind = PortfolioTransactionDeletedKind.Transaction,
                                ),
                            )
                            viewModel.deleteTransaction(transaction = tx)
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
                                    fromScreen = ScreenLabel.Portfolio,
                                    toScreen = ScreenLabel.PortfolioSwapTransaction,
                                    coinName = swapCoin?.name ?: "",
                                ),
                            )
                            navigationViewModel.navigate(dest)
                        },
                        hideData = viewModel.hideData,
                        deleteTransaction = { tx ->
                            val coinName = viewModel.portfolio?.pieContaining(tx)?.data?.name ?: ""
                            selectedTransaction = null
                            AnalyticsManager.log(
                                PortfolioTransactionDeleted(
                                    fromScreen = ScreenLabel.Portfolio,
                                    coinName = coinName,
                                    kind = PortfolioTransactionDeletedKind.Swap,
                                ),
                            )
                            viewModel.deleteTransaction(transaction = tx)
                        },
                        close = {
                            selectedTransaction = null
                        }
                    )
                }

                else -> { }
            }
        }
    }
}
