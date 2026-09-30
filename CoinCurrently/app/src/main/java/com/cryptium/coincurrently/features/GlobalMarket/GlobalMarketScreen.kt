package com.cryptium.coincurrently.features.GlobalMarket
import com.cryptium.coincurrently.core.localization.Localization
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
import com.cryptium.coincurrently.sharedui.theme.*
import com.cryptium.coincurrently.sharedui.modifiers.*
import com.cryptium.coincurrently.sharedui.components.AccessibilityLayout
import com.cryptium.coincurrently.sharedui.components.contentScreenWidthPx
import com.cryptium.coincurrently.sharedui.charts.ChartBackground
import com.cryptium.coincurrently.sharedui.charts.ChartHoverStats
import com.cryptium.coincurrently.sharedui.charts.rememberLineChartHoverUi
import com.cryptium.coincurrently.sharedui.models.FormattingThreshold
import com.cryptium.coincurrently.core.extensions.formatCurrency
import com.cryptium.coincurrently.core.extensions.dpToPx
import com.cryptium.coincurrently.core.extensions.is24HourClock
import com.cryptium.coincurrently.core.extensions.pxToDp
import com.cryptium.coincurrently.core.extensions.satoshiBold
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
import com.cryptium.coincurrently.features.GlobalMarket.Views.GlobalmarketSummaryView
import com.cryptium.coincurrently.features.GlobalMarket.Views.MarketShareView
import com.cryptium.coincurrently.sharedui.components.ImageCase
import com.cryptium.coincurrently.features.GlobalMarket.GlobalMarketViewModel
import com.cryptium.coincurrently.features.GlobalMarket.GlobalMarketViewModelFactory
import com.cryptium.coincurrently.features.CoinDetail.Views.General.ChartTypeButtonsView
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.cryptium.coincurrently.core.networking.ResponseModels.Percentages
import com.cryptium.coincurrently.core.analytics.ScreenLabel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.cryptium.coincurrently.core.networking.ResponseModels.TimeCharts
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
            title = Localization.string("Market Cap"),
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

                ChartTypeButtonsView(
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
    val localizedTitle = Localization.string(title)
    val localizedAccessibleTitle = Localization.string(accessibleTitle)
    val accessibilityLabel = valueAccessible?.let {
        Localization.format("%@: %@", localizedAccessibleTitle, it)
    }

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
            localizedTitle,
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

