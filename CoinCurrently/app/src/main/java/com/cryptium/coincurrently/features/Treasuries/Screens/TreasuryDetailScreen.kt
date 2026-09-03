package com.cryptium.coincurrently.features.Treasuries.Screens
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
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
import com.cryptium.coincurrently.sharedui.components.AccessibilityLayout
import com.cryptium.coincurrently.sharedui.buttons.ScrollToTopButton
import com.cryptium.coincurrently.sharedui.theme.onSurfaceDim
import com.cryptium.coincurrently.core.extensions.dpToPx
import com.cryptium.coincurrently.core.extensions.is24HourClock
import com.cryptium.coincurrently.core.extensions.pxToDp
import com.cryptium.coincurrently.sharedui.charts.ChartBackground
import com.cryptium.coincurrently.sharedui.charts.ChartHoverStats
import com.cryptium.coincurrently.sharedui.charts.rememberLineChartHoverUi
import com.cryptium.coincurrently.sharedui.models.FormattingThreshold
import com.cryptium.coincurrently.core.extensions.formatCurrency
import com.cryptium.coincurrently.sharedui.charts.ChartTimeStamps
import com.cryptium.coincurrently.sharedui.charts.ChartTimeframeButtonsView
import com.cryptium.coincurrently.sharedui.charts.ChartAccessibilityOverlay
import com.cryptium.coincurrently.sharedui.charts.ChartHoverView
import com.cryptium.coincurrently.sharedui.charts.LineChart
import com.cryptium.coincurrently.sharedui.charts.MultiTouchView
import com.cryptium.coincurrently.sharedui.charts.chartAccessibilityOverlay
import com.cryptium.coincurrently.sharedui.charts.delayTouches
import com.cryptium.coincurrently.sharedui.charts.hideFromTalkBackWhenEnabled
import com.cryptium.coincurrently.sharedui.navigation.CustomTopAppBar
import com.cryptium.coincurrently.sharedui.loading.ActivityIndicator
import com.cryptium.coincurrently.app.NavigationViewModel
import com.cryptium.coincurrently.sharedui.components.OnLifecycleEvent
import com.cryptium.coincurrently.sharedui.components.Point
import com.cryptium.coincurrently.features.CoinDetail.Views.General.CryptoDetailSummaryView
import com.cryptium.coincurrently.features.Treasuries.Sheets.TreasuryHoldingFilterSheet
import com.cryptium.coincurrently.features.Treasuries.ViewModels.TreasuryDetailViewModel
import com.cryptium.coincurrently.features.Treasuries.ViewModels.TreasuryDetailViewModelFactory
import com.cryptium.coincurrently.features.Treasuries.Views.TreasuryTransactionHistoryView
import com.cryptium.coincurrently.core.analytics.AnalyticsManager
import com.cryptium.coincurrently.core.analytics.ScreenLabel
import com.cryptium.coincurrently.core.analytics.ScrollToTopPressed
import com.cryptium.coincurrently.core.analytics.SheetLabel
import com.cryptium.coincurrently.core.analytics.SheetOpened
import kotlinx.coroutines.launch
import com.cryptium.coincurrently.core.networking.ResponseModels.PublicTreasuryHolder
import com.cryptium.coincurrently.core.networking.ResponseModels.TimeCharts
import kotlin.math.floor

@Composable
fun TreasuryDetailScreen(
    navigationViewModel: NavigationViewModel,
    modifier: Modifier,
    holder: PublicTreasuryHolder,
    coinId: String,
) {
    val viewModel: TreasuryDetailViewModel = viewModel(
        factory = TreasuryDetailViewModelFactory(
            holder = holder,
            coinId = coinId,
        ),
    )

    val hoverUi = rememberLineChartHoverUi(
        chartHover = viewModel.chartHover,
        prices = viewModel.prices,
        timeStamps = viewModel.timeStamps,
        chartSize = viewModel.chartSize.value,
        hoverPriceTextWidth = viewModel.hoverPriceTextWidth.value,
        timeChart = viewModel.selectedTimeFrame,
        changePercent = viewModel.changePercent,
        formatPrice = {
            it.formatCurrency(
                threshold = FormattingThreshold.MILLION,
                currency = viewModel.selectedCurrency,
            )
        },
    )

    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val context = LocalContext.current
    val talkBackEnabled = AccessibilityLayout.isTalkBackEnabled
    val coroutineScope = rememberCoroutineScope()
    val biggestValue = remember { mutableStateOf(DpSize.Zero) }
    val scrollState = rememberLazyListState()
    var isHoldingFilterShowing by remember { mutableStateOf(false) }

    val firstVisibleIndex by remember {
        derivedStateOf { scrollState.firstVisibleItemIndex }
    }

    LaunchedEffect(Unit) {
        com.cryptium.coincurrently.features.Achievements.Managers.AchievementManager.increment(
            com.cryptium.coincurrently.features.Achievements.Models.AchievementCounterKey.treasuryVisits
        )
        viewModel.setStartData()
        viewModel.refresh(showReload = false)
    }

    OnLifecycleEvent { _, event ->
        when (event) {
            Lifecycle.Event.ON_RESUME -> {
                coroutineScope.launch {
                    viewModel.refresh(showReload = false)
                }
            }
            else -> Unit
        }
    }

    BackHandler(onBack = {
        navigationViewModel.navigateBack()
    })

    Box(
        modifier = modifier,
        contentAlignment = Alignment.BottomEnd,
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            CustomTopAppBar(
                title = viewModel.holder.name ?: "",
                navigateBack = {
                    navigationViewModel.navigateBack()
                },
            )

            LazyColumn(
                state = scrollState,
                userScrollEnabled = !viewModel.isDragging,
                contentPadding = PaddingValues(bottom = 32.dp),
            ) {
                item {
                    CryptoDetailSummaryView(
                        name = viewModel.holder.name,
                        currentPrice = viewModel.onlyPrices.lastOrNull(),
                        imageUrl = null,
                        countryCode = viewModel.treasuryDetail?.country ?: viewModel.holder.country,
                        prices = viewModel.onlyPrices,
                        selectedTimeframe = viewModel.selectedTimeFrame,
                        shortPrice = false,
                        currency = viewModel.selectedCurrency,
                        changeCoin = { },
                        modifier = Modifier.padding(
                            16.dp
                        ),
                    )
                }

                item {
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
                }

                item {
                    Box(
                        modifier = Modifier
                            .height((screenHeight.dpToPx() * 0.35).pxToDp())
                            .clipToBounds()
                            .delayTouches()
                            .padding(horizontal = 16.dp)
                            .chartAccessibilityOverlay(
                                timeStamps = viewModel.timeStamps,
                                prices = viewModel.prices,
                                timeChart = viewModel.selectedTimeFrame,
                                currency = viewModel.selectedCurrency,
                                trailingPadding = biggestValue.value.width,
                            ),
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
                            TreasuryDetailChart(
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
                            currency = viewModel.selectedCurrency,
                            trailingPadding = biggestValue.value.width,
                        )

                        MultiTouchView(
                            modifier = Modifier.matchParentSize(),
                            onTouchesChanged = { touchXs ->
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
                            },
                        )
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
                            .hideFromTalkBackWhenEnabled(),
                    )
                }

                item {
                    ChartTimeframeButtonsView(
                        buttons = TimeCharts.treasuryDetailButtons(),
                        selectedTimeCharts = viewModel.selectedTimeFrame,
                        fromScreen = ScreenLabel.TreasuryDetail,
                    ) { button ->
                        if (!viewModel.isDragging) {
                            coroutineScope.launch {
                                viewModel.refresh(
                                    newDays = button.days,
                                    newTimeChart = button.timeChart,
                                )
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(32.dp))
                }

                item {
                    TreasuryTransactionHistoryView(
                        holdings = viewModel.availableHoldingCoins,
                        selectedHoldingCoinId = viewModel.selectedTransactionHoldingCoinId,
                        transactions = viewModel.transactionHistory?.transactions,
                        showHoldingFilter = {
                            AnalyticsManager.log(
                                SheetOpened(
                                    fromScreen = ScreenLabel.TreasuryDetail,
                                    sheet = SheetLabel.TreasuryHoldingFilter,
                                ),
                            )
                            isHoldingFilterShowing = true
                        },
                        filterButtonTitle = viewModel.transactionFilterButtonTitle,
                    )
                }
            }
        }

        ScrollToTopButton(
            visible = !talkBackEnabled && firstVisibleIndex >= 8,
            respectNavigationBars = true,
            onClick = {
                AnalyticsManager.log(ScrollToTopPressed(fromScreen = ScreenLabel.TreasuryDetail))
                coroutineScope.launch {
                    scrollState.animateScrollToItem(0)
                }
            },
        )

        if (isHoldingFilterShowing) {
            TreasuryHoldingFilterSheet(
                holdings = viewModel.availableHoldingCoins,
                selectedCoinId = viewModel.selectedTransactionHoldingCoinId,
                newHoldingSelected = { coin ->
                    coroutineScope.launch {
                        viewModel.setTransactionHoldingFilter(coin)
                    }
                },
                close = { isHoldingFilterShowing = false },
            )
        }
    }
}

@Composable
private fun TreasuryDetailChart(
    prices: List<Double>,
    chartColor: androidx.compose.ui.graphics.Color?,
    dragIndex: Int?,
    isDragging: Boolean,
    dragPositions: List<Point>,
    chartSize: androidx.compose.runtime.MutableState<DpSize>,
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
                    height.toDp(),
                )
            }
        }
        .then(
            if (hideFromTalkBack) {
                Modifier.hideFromTalkBackWhenEnabled()
            } else {
                Modifier
            },
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
            end = biggestValue.width,
        ),
        modifier = chartModifier,
    )

    Box(
        modifier = if (hideFromTalkBack) {
            Modifier.hideFromTalkBackWhenEnabled()
        } else {
            Modifier
        },
    ) {
        ChartHoverView(
            positions = dragPositions,
            isDragging = isDragging,
            color = chartColor ?: MaterialTheme.colors.onSurfaceDim,
            topPadding = biggestValue.height / 2,
        )
    }
}
