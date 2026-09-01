package com.cryptium.coincurrently.features.FearGreed
import androidx.activity.compose.BackHandler
import com.cryptium.coincurrently.core.networking.ResponseModels.TimeCharts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import androidx.compose.ui.unit.min
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cryptium.coincurrently.R
import com.cryptium.coincurrently.sharedui.theme.*
import com.cryptium.coincurrently.sharedui.modifiers.*
import com.cryptium.coincurrently.sharedui.components.AccessibilityLayout
import com.cryptium.coincurrently.sharedui.charts.differencePercent
import com.cryptium.coincurrently.sharedui.charts.dragPositions
import com.cryptium.coincurrently.sharedui.charts.multiHoverGraphColor
import com.cryptium.coincurrently.core.extensions.dpToPx
import com.cryptium.coincurrently.core.extensions.is24HourClock
import com.cryptium.coincurrently.core.extensions.numberAccessible
import com.cryptium.coincurrently.core.extensions.pxToDp
import com.cryptium.coincurrently.core.extensions.satoshiMedium
import com.cryptium.coincurrently.sharedui.charts.ChartAccessibilityOverlay
import com.cryptium.coincurrently.sharedui.charts.chartAccessibilityOverlay
import com.cryptium.coincurrently.sharedui.charts.ChartHoverView
import com.cryptium.coincurrently.sharedui.charts.MultiTouchView
import com.cryptium.coincurrently.sharedui.charts.delayTouches
import com.cryptium.coincurrently.sharedui.charts.hideFromTalkBackWhenEnabled
import com.cryptium.coincurrently.sharedui.charts.LineChart
import com.cryptium.coincurrently.sharedui.charts.ChartBackground
import com.cryptium.coincurrently.sharedui.charts.ChartHoverStats
import com.cryptium.coincurrently.sharedui.charts.ChartTimeframeButtonsView
import com.cryptium.coincurrently.sharedui.charts.ChartTimeStamps
import com.cryptium.coincurrently.sharedui.navigation.CustomTopAppBar
import com.cryptium.coincurrently.sharedui.loading.ActivityIndicator
import com.cryptium.coincurrently.app.NavigationViewModel
import com.cryptium.coincurrently.sharedui.components.OnLifecycleEvent
import com.cryptium.coincurrently.core.analytics.ScreenLabel
import com.cryptium.coincurrently.features.Home.Views.getFearGreedClarification
import com.cryptium.coincurrently.features.FearGreed.Views.FearGreedHistoricView
import com.cryptium.coincurrently.features.FearGreed.Sheets.FearGreedHelpSheet
import com.cryptium.coincurrently.features.FearGreed.FearAndGreedViewModel
import com.cryptium.coincurrently.features.FearGreed.FearAndGreedViewModelFactory
import com.cryptium.coincurrently.features.Home.Views.FearGreedMeter
import kotlinx.coroutines.launch
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.cryptium.coincurrently.core.networking.ResponseModels.FearGreedItem

@Composable
fun FearAndGreedScreen(
    navigationViewModel: NavigationViewModel,
    modifier: Modifier
) {
    val localDensity = LocalDensity.current
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val viewModel: FearAndGreedViewModel = viewModel(
        factory = FearAndGreedViewModelFactory()
    )

    var isRefreshing by remember { mutableStateOf(false) }
    var isHelpShowing by remember { mutableStateOf(false) }
    val biggestValue = remember { mutableStateOf(DpSize.Zero) }
    val swipeScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        com.cryptium.coincurrently.features.Achievements.Managers.AchievementManager.increment(
            com.cryptium.coincurrently.features.Achievements.Models.AchievementCounterKey.fearGreedVisits
        )
        viewModel.getFearGreed()
    }

    OnLifecycleEvent { _, event ->
        when (event) {
            Lifecycle.Event.ON_RESUME -> {
                swipeScope.launch {
                    viewModel.getFearGreed()
                }
            }
            else -> { }
        }
    }

    BackHandler(onBack = {
        navigationViewModel.navigateBack()
    })

    Column(
        modifier = modifier
    ) {
        CustomTopAppBar(
            "Fear & Greed",
            navigateBack = {
                navigationViewModel.navigateBack()
            },
            actions = listOf({
                InfoButton(
                    showHelp = {
                        isHelpShowing = true
                    }
                )
            })
        )

        SwipeRefresh(
            state = rememberSwipeRefreshState(isRefreshing = isRefreshing),
            onRefresh = {
                isRefreshing = true
                swipeScope.launch {
                    try {
                        viewModel.getFearGreed()
                    } finally {
                        isRefreshing = false
                    }
                }
            }) {
            Box {
                Column(
                    horizontalAlignment = Alignment.Start,
                    modifier = Modifier
                        .verticalScroll(rememberScrollState(), enabled = !viewModel.isDragging)
                        .onGloballyPositioned { coordinates ->
                            val sizePx = coordinates.size
                            viewModel.viewSize.value = with(localDensity) {
                                DpSize(
                                    sizePx.width.toDp(),
                                    sizePx.height.toDp()
                                )
                            }
                        }
                ) {
                    HistoricView(
                        historicTimeframes = viewModel.historicTimeframes,
                        data = viewModel.fearGreedModel?.data,
                    )

                    LocalFearGreedMeter(
                        viewModel.fearGreedModel?.data?.firstOrNull()
                    )

                    ChartHoverStats(
                        isMultiHover = viewModel.multiHoverTimes.size == 2,
                        hoverTimes = viewModel.multiHoverTimes,
                        differenceValue = viewModel.differenceValue,
                        differencePercent = viewModel.differencePercent,
                        multiHoverColor = viewModel.multiHoverGraphColor,
                        hoverPrice = viewModel.hoverPrice,
                        hoverPriceColor = viewModel.hoverColor,
                        hoverTime = viewModel.hoverTime,
                        hoverTimePosition = viewModel.hoverTimePosition,
                        isDragging = viewModel.isDragging,
                        hoverPriceTextWidth = viewModel.hoverPriceTextWidth,
                    )

                    Box(
                        modifier = Modifier
                            .height((screenHeight.dpToPx() * 0.35).pxToDp())
                            .clipToBounds()
                            .delayTouches()
                            .chartAccessibilityOverlay(
                                timeStamps = viewModel.timeStamps,
                                prices = viewModel.prices.dropLast(2),
                                timeChart = viewModel.selectedChartTimeFrame,
                                trailingPadding = biggestValue.value.width,
                                dontShowTime = true,
                                priceFormatter = { it.numberAccessible(maxPlaces = 0) },
                            )
                            .padding(horizontal = 16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .hideFromTalkBackWhenEnabled()
                        ) {
                            ChartBackground(
                                values = (0 until 6).map { "${it * 20}" },
                                biggestValue = biggestValue
                            )

                            viewModel.fearGreedModel?.let {
                                LineChart(
                                    prices = viewModel.prices,
                                    color = viewModel.graphColor,
                                    dragIndex = if (viewModel.isDragging) viewModel.dragIndex else null,
                                    dropCount = 2,
                                    isDragging = viewModel.isDragging,
                                    dragPositionXs = if (viewModel.isDragging) {
                                        viewModel.dragPositions.map { with(localDensity) { it.x.toPx() } }
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
                                )

                                ChartHoverView(
                                    positions = viewModel.dragPositions,
                                    isDragging = viewModel.isDragging,
                                    color = viewModel.graphColor,
                                    topPadding = biggestValue.value.height / 2,
                                )
                            } ?: run {
                                ActivityIndicator()
                            }
                        }

                        ChartAccessibilityOverlay(
                            timeStamps = viewModel.timeStamps,
                            prices = viewModel.prices.dropLast(2),
                            timeChart = viewModel.selectedChartTimeFrame,
                            trailingPadding = biggestValue.value.width,
                            dontShowTime = true,
                            priceFormatter = { it.numberAccessible(maxPlaces = 0) },
                        )

                        MultiTouchView(
                            modifier = Modifier.matchParentSize(),
                            onTouchesChanged = { touchXs ->
                                if (!isRefreshing) {
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
                        TimeCharts.getButtons(from = TimeCharts.aWeek, to = TimeCharts.max),
                        viewModel.selectedChartTimeFrame,
                        fromScreen = ScreenLabel.FearGreed,
                    ) {
                        if (!viewModel.isDragging) {
                            viewModel.selectedChartTimeFrame = it.timeChart
                            viewModel.setPrices()
                        }
                    }
                }

                if (isHelpShowing) {
                    FearGreedHelpSheet {
                        isHelpShowing = false
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoButton(
    showHelp: () -> Unit
) {
    IconButton(onClick = {
        showHelp.invoke()
    }) {
        Image(
            painter = painterResource(id = R.drawable.info),
            contentDescription = "Help understand the fear and greed index",
            colorFilter = ColorFilter.tint(
                MaterialTheme.colors.onSurface
            ),
        )
    }
}

@Composable
private fun HistoricView(
    historicTimeframes: List<Pair<Int, String>>,
    data: List<FearGreedItem>?,
) {
    val isHorizontal = AccessibilityLayout.isHorizontal

    val longestStatus = historicTimeframes.mapIndexed { index, value ->
        val currentStep = historicTimeframes.get(index).first
        val currentValue = data?.get(currentStep)?.value
        getFearGreedClarification(currentValue?.toIntOrNull() ?: 0)
    }.min()

    val itemCount = historicTimeframes.size.coerceAtLeast(1)
    val cardSpacing = 12.dp * (itemCount - 1).coerceAtLeast(0)

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = if (isHorizontal) 16.dp else 0.dp),
    ) {
        val horizontalContentPadding = if (isHorizontal) 0.dp else 32.dp
        val equalCardWidth = (
            (maxWidth - horizontalContentPadding - cardSpacing).coerceAtLeast(0.dp) / itemCount
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(
                start = if (isHorizontal) 0.dp else 16.dp,
                end = if (isHorizontal) 0.dp else 16.dp,
                top = 16.dp,
                bottom = 12.dp,
            ),
            modifier = Modifier.fillMaxWidth(),
        ) {
            itemsIndexed(historicTimeframes) { index, (step, timeframe) ->
                val currentValue = data?.getOrNull(step)?.value
                val currentStatus = getFearGreedClarification(currentValue?.toIntOrNull() ?: 0)
                val currentValueAccessible = getHistoricTextAccessible(
                    index = index,
                    value = currentValue ?: "0",
                    timeframe = timeframe,
                )

                FearGreedHistoricView(
                    timeframe = timeframe,
                    indexValue = currentValue,
                    longestStatus = longestStatus,
                    status = currentStatus,
                    modifier = if (isHorizontal) {
                        Modifier
                            .width(equalCardWidth)
                            .clearAndSetSemantics {
                                contentDescription = "$currentValueAccessible. $currentStatus"
                            }
                    } else {
                        Modifier
                            .widthIn(min = equalCardWidth)
                            .clearAndSetSemantics {
                                contentDescription = "$currentValueAccessible. $currentStatus"
                            }
                    },
                )
            }
        }
    }
}

@Composable
private fun LocalFearGreedMeter(value: FearGreedItem?) {
    val localDensity = LocalDensity.current
    var fearMeterWidth by remember { mutableStateOf(0.dp) }

    val percent = (value?.value?.toDouble() ?: 0.0) / 100.0
    val thumbSize = 32.dp
    val thumbOuterSize = thumbSize + 4.dp

    Column(
        modifier = Modifier
            .padding(vertical = 16.dp)
    ) {
        Box(
            contentAlignment = Alignment.CenterStart,
        ) {
            FearGreedMeter(
                height = 24.dp,
                modifier = Modifier
                    .onGloballyPositioned { coordinates ->
                        fearMeterWidth = with(localDensity) { coordinates.size.width.toDp() }
                    }
                    .padding(horizontal = 16.dp)
            )

            Box(
                modifier = Modifier
                    .offset(x = fearMeterWidth * percent.toFloat() - thumbOuterSize / 2)
                    .size(thumbOuterSize)
                    .clip(CircleShape)
                    .background(MaterialTheme.colors.outline)
                    .padding(2.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colors.surfaceContainerHighest)
            )
        }

        FearGreedMeterHeader()
    }
}

@Composable
private fun FearGreedMeterHeader() {
    Row(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .hideFromTalkBackWhenEnabled()
    ) {
        Text(
            "Fear",
            style = satoshiMedium(14),
            color = MaterialTheme.colors.onSurfaceContainerDimmer,
            modifier = Modifier.hideFromTalkBackWhenEnabled(),
        )

        Spacer(modifier = Modifier.weight(1f))

        Text(
            "Greed",
            style = satoshiMedium(14),
            color = MaterialTheme.colors.onSurfaceContainerDimmer,
            modifier = Modifier.hideFromTalkBackWhenEnabled(),
        )
    }
}

private fun getHistoricTextAccessible(
    index: Int,
    value: String,
    timeframe: String,
): String {
    return if (index == 0) {
        "The fear and greed index is currently $value out of 100"
    } else {
        "The fear and greed index was $value out of 100 $timeframe"
    }
}
