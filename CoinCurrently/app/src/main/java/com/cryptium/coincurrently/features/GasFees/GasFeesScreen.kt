package com.cryptium.coincurrently.features.GasFees
import com.cryptium.coincurrently.core.localization.Localization
import com.cryptium.coincurrently.sharedui.modifiers.imageSize

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Alignment.Companion.CenterStart
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.rotate
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.invisibleToUser
import androidx.compose.ui.semantics.semantics
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cryptium.coincurrently.R
import com.cryptium.coincurrently.sharedui.components.AccessibilityLayout
import com.cryptium.coincurrently.sharedui.components.BackPressHandler
import com.cryptium.coincurrently.sharedui.theme.onSurfaceContainerDimmer
import com.cryptium.coincurrently.sharedui.theme.onSurfaceDim
import com.cryptium.coincurrently.sharedui.theme.onSurfaceDimmer
import com.cryptium.coincurrently.sharedui.theme.outline
import com.cryptium.coincurrently.sharedui.theme.surfaceContainerHighest
import com.cryptium.coincurrently.core.extensions.dpToPx
import com.cryptium.coincurrently.core.extensions.is24HourClock
import com.cryptium.coincurrently.core.extensions.numberAccessible
import com.cryptium.coincurrently.core.extensions.pxToDp
import com.cryptium.coincurrently.core.extensions.satoshiBold
import com.cryptium.coincurrently.core.extensions.satoshiMedium
import com.cryptium.coincurrently.sharedui.charts.ChartBackground
import com.cryptium.coincurrently.sharedui.charts.ChartHoverStats
import com.cryptium.coincurrently.sharedui.charts.rememberLineChartHoverUi
import com.cryptium.coincurrently.sharedui.charts.HoverTimeFallback
import com.cryptium.coincurrently.sharedui.charts.decimalDifferenceValue
import com.cryptium.coincurrently.core.extensions.roundTo
import com.cryptium.coincurrently.sharedui.charts.ChartTimeStamps
import com.cryptium.coincurrently.sharedui.charts.ChartAccessibilityOverlay
import com.cryptium.coincurrently.sharedui.charts.chartAccessibilityOverlay
import com.cryptium.coincurrently.sharedui.charts.ChartHoverView
import com.cryptium.coincurrently.sharedui.charts.MultiTouchView
import com.cryptium.coincurrently.sharedui.charts.delayTouches
import com.cryptium.coincurrently.sharedui.charts.hideFromTalkBackWhenEnabled
import com.cryptium.coincurrently.sharedui.charts.LineChart
import com.cryptium.coincurrently.sharedui.navigation.CustomTopAppBar
import com.cryptium.coincurrently.sharedui.loading.ActivityIndicator
import com.cryptium.coincurrently.app.NavigationViewModel
import com.cryptium.coincurrently.sharedui.components.OnLifecycleEvent
import com.cryptium.coincurrently.features.GasFees.Views.EstimateGasFeeCost
import com.cryptium.coincurrently.features.GasFees.Views.GasFeesHistoricView
import com.cryptium.coincurrently.features.GasFees.Views.getGasIndexColor
import com.cryptium.coincurrently.features.GasFees.Views.getGasIndexString
import com.cryptium.coincurrently.features.GasFees.GasUnits
import com.cryptium.coincurrently.features.GasFees.Sheets.GasCostHelpSheet
import com.cryptium.coincurrently.features.GasFees.Sheets.GasFeesHelpSheet
import com.cryptium.coincurrently.features.GasFees.GasFeesViewModel
import com.cryptium.coincurrently.features.GasFees.GasFeesViewModelFactory
import com.cryptium.coincurrently.features.Home.Views.FearGreedMeter
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.cryptium.coincurrently.core.networking.ResponseModels.TimeCharts

@Composable
fun GasFeesScreen(
    navigationViewModel: NavigationViewModel,
    modifier: Modifier
) {
    val localDensity = LocalDensity.current
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    var isRefreshing by remember { mutableStateOf(false) }
    val viewModel: GasFeesViewModel = viewModel(
        factory = GasFeesViewModelFactory()
    )
    val hoverUi = rememberLineChartHoverUi(
        chartHover = viewModel.chartHover,
        prices = viewModel.prices.value,
        timeStamps = viewModel.timeStamps.value,
        chartSize = viewModel.chartSize.value,
        hoverPriceTextWidth = viewModel.hoverPriceTextWidth.value,
        timeChart = TimeCharts.aWeek,
        changePercent = null,
        formatPrice = { "%.2f".format(it.roundTo(2)) },
        formatDifferenceValue = { decimalDifferenceValue() },
        dropLastCount = 2,
        chartColor = getGasIndexColor(
            (viewModel.prices.value.dropLast(2).lastOrNull() ?: 0).toString()
        ),
        timeFallback = HoverTimeFallback.DateOnly,
        emptyPrice = "0.00",
    )
    val hoverPriceColor = getGasIndexColor(
        (viewModel.prices.value.dropLast(2).getOrNull(hoverUi.dragIndex)?.toInt() ?: 0).toString()
    )
    val swipeScope = rememberCoroutineScope()

    val biggestValue = remember { mutableStateOf(DpSize.Zero) }

    var showInfoSheet by remember { mutableStateOf(false) }
    var showCostInfoSheet by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        coroutineScope {
            launch { viewModel.getGasFees() }
            launch { viewModel.getGasFeePrices() }
            launch { viewModel.getEthereumPrice() }
        }
    }

    OnLifecycleEvent { _, event ->
        when (event) {
            Lifecycle.Event.ON_RESUME -> {
                swipeScope.launch {
                    coroutineScope {
                        launch { viewModel.getGasFees() }
                        launch { viewModel.getGasFeePrices() }
                        launch { viewModel.getEthereumPrice() }
                    }
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
            title = Localization.string("ETH Gas Fee"),
            accessibilityTitle = Localization.string("Ethereum gas fee"),
            navigateBack = {
                navigationViewModel.navigateBack()
            },
            actions = listOf({
                InfoButton(
                    showHelp = {
                        showInfoSheet = true
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
                        coroutineScope {
                            launch { viewModel.getGasFees() }
                            launch { viewModel.getGasFeePrices() }
                            launch { viewModel.getEthereumPrice() }
                        }
                    } finally {
                        isRefreshing = false
                    }
                }
            }) {
            Box {
                Column(
                    modifier = Modifier
                        .padding(top = 16.dp)
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
                    CurrentView(
                        currentStats = viewModel.currentTimeFrames,
                        getCurrentTextAccessible = viewModel::getCurrentTextAccessible,
                    )

                    GasFeesMeter(
                        percent = viewModel.normalizedPosition
                    )

                    ChartHoverStats(
                        isMultiHover = hoverUi.isMultiHover,
                        hoverTimes = hoverUi.hoverTimes,
                        differenceValue = hoverUi.differenceValue,
                        differencePercent = hoverUi.differencePercent,
                        multiHoverColor = hoverUi.multiHoverGraphColor,
                        hoverPrice = hoverUi.hoverPrice,
                        hoverPriceColor = hoverPriceColor,
                        hoverTime = hoverUi.hoverTime,
                        hoverTimePosition = hoverUi.hoverTimePosition,
                        isDragging = viewModel.isDragging,
                        hoverPriceTextWidth = viewModel.hoverPriceTextWidth,
                    )

                    Box(
                        modifier = Modifier
                            .height((screenHeight.dpToPx() * 0.35).pxToDp())
                            .clipToBounds()
                            .delayTouches()
                            .padding(horizontal = 16.dp)
                            .chartAccessibilityOverlay(
                                timeStamps = viewModel.timeStamps.value,
                                prices = viewModel.prices.value,
                                timeChart = TimeCharts.aWeek,
                                trailingPadding = biggestValue.value.width,
                                priceFormatter = { "${it.numberAccessible(maxPlaces = 2)} ${Localization.string("Gwei")}" },
                            )
                    ) {
                        Box(modifier = Modifier.hideFromTalkBackWhenEnabled()) {
                            ChartBackground(
                                values = (0 until 6).map { "${it * 2}" },
                                biggestValue = biggestValue
                            )
                        }

                        viewModel.gasFeePrices?.let {
                            LineChart(
                                prices = viewModel.prices.value,
                                color = hoverUi.chartColor,
                                dragIndex = if (viewModel.isDragging) hoverUi.dragIndex else null,
                                dropCount = 2,
                                isDragging = viewModel.isDragging,
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
                                    .hideFromTalkBackWhenEnabled()
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

                            Box(modifier = Modifier.hideFromTalkBackWhenEnabled()) {
                                ChartHoverView(
                                    positions = hoverUi.dragPositions,
                                    isDragging = viewModel.isDragging,
                                    color = hoverUi.chartColor,
                                    topPadding = biggestValue.value.height / 2,
                                )
                            }
                        } ?: run {
                            ActivityIndicator(
                                modifier = Modifier.hideFromTalkBackWhenEnabled(),
                            )
                        }

                        ChartAccessibilityOverlay(
                            timeStamps = viewModel.timeStamps.value,
                            prices = viewModel.prices.value,
                            timeChart = TimeCharts.aWeek,
                            trailingPadding = biggestValue.value.width,
                            priceFormatter = { "${it.numberAccessible(maxPlaces = 2)} ${Localization.string("Gwei")}" },
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
                        TimeCharts.aWeek,
                        viewModel.timeStamps.value,
                        modifier = Modifier
                            .offset(y = -biggestValue.value.height / 2)
                            .padding(
                                start = 16.dp,
                                end = 16.dp + biggestValue.value.width,
                                bottom = 16.dp - biggestValue.value.height / 2,
                            )
                            .hideFromTalkBackWhenEnabled()
                    )

                    HistoricData(
                        prices = viewModel.allPrices.value
                    )

                    EstimateCostsView(
                        swapCost = viewModel.getGasPrice(GasUnits.Swap),
                        nftCost = viewModel.getGasPrice(GasUnits.Nft),
                        bridgingCost = viewModel.getGasPrice(GasUnits.Bridging),
                        borrowingCost = viewModel.getGasPrice(GasUnits.Borrowing),
                        accessibleSwapCost = viewModel.getGasPrice(GasUnits.Swap, accessible = true),
                        accessibleNftCost = viewModel.getGasPrice(GasUnits.Nft, accessible = true),
                        accessibleBridgingCost = viewModel.getGasPrice(GasUnits.Bridging, accessible = true),
                        accessibleBorrowingCost = viewModel.getGasPrice(GasUnits.Borrowing, accessible = true),
                        openHelpSheet = {
                            showCostInfoSheet = true
                        }
                    )
                }

                if (showInfoSheet) {
                    GasFeesHelpSheet {
                        showInfoSheet = false
                    }
                }

                if (showCostInfoSheet) {
                    GasCostHelpSheet {
                        showCostInfoSheet = false
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
            contentDescription = Localization.string("Help understand how ethereum gas fees work"),
            colorFilter = ColorFilter.tint(
                MaterialTheme.colors.onSurface
            ),
        )
    }
}


@Composable
private fun CurrentView(
    currentStats: List<Pair<String?, String>>,
    getCurrentTextAccessible: (Int, String) -> String,
) {
    val isHorizontal = AccessibilityLayout.isHorizontal
    val itemCount = currentStats.count().coerceAtLeast(1)
    val cardSpacing = 16.dp * (itemCount - 1).coerceAtLeast(0)

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
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(
                start = if (isHorizontal) 0.dp else 16.dp,
                end = if (isHorizontal) 0.dp else 16.dp,
            ),
            modifier = Modifier.fillMaxWidth(),
        ) {
            itemsIndexed(currentStats) { index, item ->
                val currentValueAccessible = getCurrentTextAccessible(index, item.first ?: "0")
                val currentStatus = getGasIndexString(item.first ?: "0")

                GasFeesHistoricView(
                    timeframe = item.second,
                    indexValue = item.first,
                    status = currentStatus,
                    modifier = (
                        if (isHorizontal) {
                            Modifier.width(equalCardWidth)
                        } else {
                            Modifier.widthIn(min = equalCardWidth)
                        }
                    ).semantics(mergeDescendants = true) {
                        contentDescription = Localization.format(
                            "%@. %@",
                            currentValueAccessible,
                            Localization.string(currentStatus),
                        )
                    },
                )
            }
        }
    }
}

@Composable
private fun GasFeesMeter(
    percent: Double
) {
    val localDensity = LocalDensity.current
    var gasMeterWidth by remember { mutableStateOf(0.dp) }
    val circleSize = 32.dp

    Column(
        modifier = Modifier
            .padding(
                top = 32.dp,
                bottom = 16.dp
            )
            .hideFromTalkBackWhenEnabled()
    ) {
        Box(
            contentAlignment = CenterStart,
        ) {
            Box(
                contentAlignment = Center,
                modifier = Modifier
                    .height(circleSize)
            ) {
                FearGreedMeter(
                    height = 24.dp,
                    modifier = Modifier
                        .rotate(180f)
                        .onGloballyPositioned { coordinates ->
                            gasMeterWidth = with(localDensity) { coordinates.size.width.toDp() }
                        }
                        .padding(horizontal = 16.dp)
                )
            }

            Box(
                modifier = Modifier
                    .offset(x = ((gasMeterWidth.dpToPx() * percent) - ((circleSize + 4.dp).dpToPx() * percent)).pxToDp())
                    .size(circleSize + 4.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colors.outline)
                    .padding(2.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colors.surfaceContainerHighest)
            )
        }

        GasFeesMeterHeader()
    }
}

@Composable
private fun GasFeesMeterHeader() {
    Row(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .hideFromTalkBackWhenEnabled()
    ) {
        Text(
            Localization.string("Low"),
            style = satoshiMedium(14),
            color = MaterialTheme.colors.onSurfaceContainerDimmer,
            modifier = Modifier.hideFromTalkBackWhenEnabled(),
        )

        Spacer(modifier = Modifier.weight(1f))

        Text(
            Localization.string("High"),
            style = satoshiMedium(14),
            color = MaterialTheme.colors.onSurfaceContainerDimmer,
            modifier = Modifier.hideFromTalkBackWhenEnabled(),
        )
    }
}


@Composable
private fun HistoricData(
    prices: List<Double>
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .padding(
                start = 16.dp,
                end = 16.dp,
                top = 16.dp,
                bottom = 0.dp
            )
    ) {
        Text(
            text = Localization.string("Historical Data"),
            style = satoshiBold(22),
            color = MaterialTheme.colors.onSurfaceDim,
            modifier = Modifier.semantics { heading() },
        )

        EstimateGasFeeCost(
            prices = prices
        )
    }
}


@Composable
private fun EstimateCostsView(
    swapCost: String?,
    nftCost: String?,
    bridgingCost: String?,
    borrowingCost: String?,
    accessibleSwapCost: String?,
    accessibleNftCost: String?,
    accessibleBridgingCost: String?,
    accessibleBorrowingCost: String?,
    openHelpSheet: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .padding(
                start = 16.dp,
                end = 16.dp,
                top = 32.dp,
                bottom = 32.dp
            )
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = Localization.string("Estimated Cost"),
                style = satoshiBold(22),
                color = MaterialTheme.colors.onSurfaceDim,
                modifier = Modifier.semantics { heading() },
            )

            Spacer(modifier = Modifier.weight(1f))

            Image(
                painter = painterResource(id = R.drawable.info),
                contentDescription = Localization.string("Estimated Cost info"),
                colorFilter = ColorFilter.tint(MaterialTheme.colors.onSurfaceDimmer),
                modifier = Modifier
                    .padding(12.dp)
                    .imageSize(24.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        openHelpSheet.invoke()
                    }
            )
        }

        EstimateGasFeeCost(
            swapCost = swapCost,
            nftCost = nftCost,
            bridgingCost = bridgingCost,
            borrowingCost = borrowingCost,
            accessibleSwapCost = accessibleSwapCost,
            accessibleNftCost = accessibleNftCost,
            accessibleBridgingCost = accessibleBridgingCost,
            accessibleBorrowingCost = accessibleBorrowingCost,
        )
    }
}