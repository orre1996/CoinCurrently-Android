package com.cryptium.coincurrently.ui.main.HomeTab.GlobalMarket.Screens

import android.os.Handler
import android.os.Looper
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.invisibleToUser
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cryptium.coincurrently.ui.main.Compose.*
import com.cryptium.coincurrently.ui.main.Extensions.dpToPx
import com.cryptium.coincurrently.ui.main.Extensions.is24HourClock
import com.cryptium.coincurrently.ui.main.Extensions.pxToDp
import com.cryptium.coincurrently.ui.main.Extensions.satoshiBold
import com.cryptium.coincurrently.ui.main.General.ChartBackground
import com.cryptium.coincurrently.ui.main.General.ChartHoverStats
import com.cryptium.coincurrently.ui.main.General.ChartTimeStamps
import com.cryptium.coincurrently.ui.main.General.ChartTimeframeButtonsView
import com.cryptium.coincurrently.ui.main.General.Components.Chart.ChartAccessibilityOverlay
import com.cryptium.coincurrently.ui.main.General.Components.Chart.ChartHoverView
import com.cryptium.coincurrently.ui.main.General.Components.Chart.LineChart
import com.cryptium.coincurrently.ui.main.General.Components.Chart.MultiTouchView
import com.cryptium.coincurrently.ui.main.General.Components.Chart.chartAccessibilityOverlay
import com.cryptium.coincurrently.ui.main.General.Components.Chart.delayTouches
import com.cryptium.coincurrently.ui.main.General.Components.Chart.hideFromTalkBackWhenEnabled
import com.cryptium.coincurrently.ui.main.General.Components.CustomTopAppBar
import com.cryptium.coincurrently.ui.main.General.Components.Loading.ActivityIndicator
import com.cryptium.coincurrently.ui.main.General.NavigationViewModel
import com.cryptium.coincurrently.ui.main.General.OnLifecycleEvent
import com.cryptium.coincurrently.ui.main.General.Utilities.Point
import com.cryptium.coincurrently.ui.main.HomeTab.GlobalMarket.Components.GlobalmarketSummaryView
import com.cryptium.coincurrently.ui.main.HomeTab.GlobalMarket.Components.MarketShareView
import com.cryptium.coincurrently.ui.main.General.Components.ImageCase
import com.cryptium.coincurrently.ui.main.HomeTab.GlobalMarket.ViewModels.GlobalMarketViewModel
import com.cryptium.coincurrently.ui.main.HomeTab.GlobalMarket.ViewModels.GlobalMarketViewModelFactory
import com.cryptium.coincurrently.ui.main.MarketTab.CryptoDetail.Components.General.ChartButtonsView
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.cryptium.coincurrently.ui.main.HomeTab.GlobalMarket.DataClasses.Percentages
import com.cryptium.coincurrently.ui.main.analytics.ScreenLabel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import networking.ResponseModels.TimeCharts
import kotlin.math.floor
import kotlin.math.max
import kotlin.time.Duration.Companion.seconds

@Composable
fun GlobalMarketScreen(
    navigationViewModel: NavigationViewModel,
    modifier: Modifier
) {
    val context = LocalContext.current
    val uiCoroutineScope = rememberCoroutineScope()
    val talkBackEnabled = AccessibilityLayout.isTalkBackEnabled
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp

    var isRefreshing by remember { mutableStateOf(false) }
    val biggestValue = remember { mutableStateOf(DpSize.Zero) }

    val viewModel: GlobalMarketViewModel = viewModel(
        factory = GlobalMarketViewModelFactory()
    )

    LaunchedEffect(Unit) {
        viewModel.setStartData()
        viewModel.getGlobalMarket()
        viewModel.refresh()
        viewModel.getColors(context)
    }

    OnLifecycleEvent { _, event ->
        when (event) {
            Lifecycle.Event.ON_RESUME -> {
                uiCoroutineScope.launch {
                    viewModel.getGlobalMarket()
                    viewModel.refresh()
                    viewModel.getColors(context)
                }
            }

            else -> {}
        }
    }

    BackHandler(onBack = {
        navigationViewModel.navigateBack()
    })

    Column(
        modifier = modifier
    ) {
        CustomTopAppBar(
            title = "Market Cap",
            navigateBack = {
                navigationViewModel.navigateBack()
            }
        )

        SwipeRefresh(
            state = rememberSwipeRefreshState(isRefreshing = isRefreshing),
            onRefresh = {
                isRefreshing = true

                uiCoroutineScope.launch {
                    launch {
                        viewModel.getGlobalMarket()
                        viewModel.refresh()
                        viewModel.getColors(context)

                        isRefreshing = false
                    }

                    delay(10.seconds)
                    isRefreshing = false
                }
            },
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 32.dp)
            ) {
                GlobalmarketSummaryView(
                    marketCap = viewModel.latestChartMarketCap,
                    changePercent = viewModel.marketCapChangePercent,
                    selectedTimeframe = viewModel.selectedTimeFrame
                )

                ChartButtonsView(
                    fromScreen = ScreenLabel.GlobalMarket,
                    selectedGraphType = viewModel.selectedChartType,
                    isCandleChartEnabled = false,
                    isInCompareMode = true,
                    imageCase = ImageCase.Nothing,
                    chartTypeAction = {
                        viewModel.setChartType(it)
                    },
                    toggleCandleChart = {},
                )

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
                        trailingPadding = biggestValue.value.width,
                        useExchangeDateFormat = true,
                    )

                    MultiTouchView(
                        modifier = Modifier.matchParentSize(),
                        onTouchesChanged = { touchXs ->
                            if (!viewModel.isRefreshing) {
                                viewModel.updateHoverStats(
                                    touchXs = touchXs.map { touchX ->
                                        androidx.compose.ui.unit.max(
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

                ChartTimeframeButtonsView(
                    buttons = TimeCharts.getButtons(from = TimeCharts.aDay, to = TimeCharts.max),
                    selectedTimeCharts = viewModel.selectedTimeFrame,
                    fromScreen = ScreenLabel.GlobalMarket,
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

                viewModel.globalMarket?.percentages?.let {
                    MarketShareMeter(
                        it
                    )
                } ?: run {
                    TempMarketShareMeter()
                }

                viewModel.globalMarket?.percentages?.let {
                    RealCoins(
                        it
                    )
                } ?: run {
                    TempCoins()
                }

                GlobalMarketStatsSection(
                    activeCryptocurrencies = viewModel.globalMarket?.active_cryptocurrencies?.toString(),
                    markets = viewModel.globalMarket?.markets?.toString(),
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

@Composable
private fun GlobalMarketStatsSection(
    activeCryptocurrencies: String?,
    markets: String?,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.Top,
        modifier = Modifier
            .padding(top = 4.dp)
            .padding(horizontal = 16.dp)
            .primaryBackgroundNoRipple()
            .fillMaxWidth()
    ) {
        InfoView(
            title = "Active Currencies",
            value = activeCryptocurrencies,
            modifier = Modifier
                .weight(1f)
        )

        InfoView(
            title = "Markets",
            value = markets,
            modifier = Modifier
                .weight(1f)
        )
    }
}

@Composable
private fun MarketShareMeter(
    percentages: List<Percentages>
) {
    val screenWidth = contentScreenWidthPx()

    Row(
        modifier = Modifier
            .height(136.dp)
            .padding(horizontal = 16.dp)
            .padding(top = 32.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clearAndSetSemantics {
                invisibleToUser()
            }
    ) {
        percentages.forEach { percent ->
            val width = max(0.0, (percent.percentage ?: 0.0) * (screenWidth - 32))
            val colorInt = percent.color
            val currentColor: Color = when {
                percent.symbol?.uppercase() == "OTHER" -> MaterialTheme.colors.primary20
                colorInt != null -> Color(colorInt)
                else -> MaterialTheme.colors.surfaceContainerLow
            }

            Box(
                modifier = Modifier
                    .width(width.pxToDp())
                    .fillMaxHeight()
                    .background(currentColor)
            )
        }
    }
}

@Composable
private fun TempMarketShareMeter() {
    Row(
        modifier = Modifier
            .height(136.dp)
            .padding(top = 32.dp)
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxSize()
                .shimmeringModifier(true)
        )
    }
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
private fun RealCoins(percentages: List<Percentages>) {
    FlowRow(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
            .padding(
                horizontal = 16.dp,
                vertical = 12.dp
            )
    ) {
        percentages.forEachIndexed { index, percent ->
            MarketShareView(
                percent = percent,
                listIndex = index,
                listCount = percentages.size,
            )
        }
    }
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
private fun TempCoins() {
    FlowRow(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
            .padding(
                horizontal = 16.dp,
                vertical = 12.dp
            )
    ) {
        (0 until 6).forEach { index ->
            MarketShareView(
                percent = null,
                listIndex = index,
                listCount = 6,
            )
        }
    }
}

@Composable
private fun InfoView(
    title: String,
    titleAccessible: String? = null,
    value: String?,
    valueAccessible: String? = null,
    modifier: Modifier
) {
    val accessibleTitle = titleAccessible ?: title
    val accessibilityLabel = valueAccessible?.let { "$accessibleTitle: $it" }

    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = Alignment.Start,
        modifier = modifier.then(
            if (accessibilityLabel != null) {
                Modifier.semantics(mergeDescendants = true) {
                    contentDescription = accessibilityLabel
                }
            } else {
                Modifier.semantics(mergeDescendants = true) {}
            }
        )
    ) {
        Text(
            title,
            style = satoshiBold(12),
            color = MaterialTheme.colors.onSurfaceContainerDim,
            modifier = if (valueAccessible != null) {
                Modifier.semantics { invisibleToUser() }
            } else {
                Modifier
            }
        )

        Text(
            value ?: "000000000000",
            style = satoshiBold(16),
            color = MaterialTheme.colors.onSurface,
            modifier = Modifier
                .shimmeringModifier(value == null)
                .then(
                    if (valueAccessible != null) {
                        Modifier.semantics { invisibleToUser() }
                    } else {
                        Modifier
                    }
                )
        )
    }
}


