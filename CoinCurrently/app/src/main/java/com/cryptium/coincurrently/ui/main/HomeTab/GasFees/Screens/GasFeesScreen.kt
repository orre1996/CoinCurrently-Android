package com.cryptium.coincurrently.ui.main.HomeTab.GasFees.Screens

import com.cryptium.coincurrently.ui.main.Compose.imageSize

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
import com.cryptium.coincurrently.ui.main.Compose.AccessibilityLayout
import com.cryptium.coincurrently.ui.main.Compose.BackPressHandler
import com.cryptium.coincurrently.ui.main.Compose.onSurfaceContainerDimmer
import com.cryptium.coincurrently.ui.main.Compose.onSurfaceDim
import com.cryptium.coincurrently.ui.main.Compose.onSurfaceDimmer
import com.cryptium.coincurrently.ui.main.Compose.outline
import com.cryptium.coincurrently.ui.main.Compose.surfaceContainerHighest
import com.cryptium.coincurrently.ui.main.Extensions.dpToPx
import com.cryptium.coincurrently.ui.main.Extensions.is24HourClock
import com.cryptium.coincurrently.ui.main.Extensions.numberAccessible
import com.cryptium.coincurrently.ui.main.Extensions.pxToDp
import com.cryptium.coincurrently.ui.main.Extensions.satoshiBold
import com.cryptium.coincurrently.ui.main.Extensions.satoshiMedium
import com.cryptium.coincurrently.ui.main.General.ChartBackground
import com.cryptium.coincurrently.ui.main.General.ChartHoverStats
import com.cryptium.coincurrently.ui.main.General.ChartTimeStamps
import com.cryptium.coincurrently.ui.main.General.Components.Chart.ChartAccessibilityOverlay
import com.cryptium.coincurrently.ui.main.General.Components.Chart.chartAccessibilityOverlay
import com.cryptium.coincurrently.ui.main.General.Components.Chart.ChartHoverView
import com.cryptium.coincurrently.ui.main.General.Components.Chart.MultiTouchView
import com.cryptium.coincurrently.ui.main.General.Components.Chart.delayTouches
import com.cryptium.coincurrently.ui.main.General.Components.Chart.hideFromTalkBackWhenEnabled
import com.cryptium.coincurrently.ui.main.General.Components.Chart.LineChart
import com.cryptium.coincurrently.ui.main.General.Components.CustomTopAppBar
import com.cryptium.coincurrently.ui.main.General.Components.Loading.ActivityIndicator
import com.cryptium.coincurrently.ui.main.General.NavigationViewModel
import com.cryptium.coincurrently.ui.main.General.OnLifecycleEvent
import com.cryptium.coincurrently.ui.main.HomeTab.GasFees.Components.EstimateGasFeeCost
import com.cryptium.coincurrently.ui.main.HomeTab.GasFees.Components.GasFeesHistoricView
import com.cryptium.coincurrently.ui.main.HomeTab.GasFees.Components.getGasIndexString
import com.cryptium.coincurrently.ui.main.HomeTab.GasFees.Enums.GasUnits
import com.cryptium.coincurrently.ui.main.HomeTab.GasFees.Popups.GasCostHelpSheet
import com.cryptium.coincurrently.ui.main.HomeTab.GasFees.Popups.GasFeesHelpSheet
import com.cryptium.coincurrently.ui.main.HomeTab.GasFees.ViewModels.GasFeesViewModel
import com.cryptium.coincurrently.ui.main.HomeTab.GasFees.ViewModels.GasFeesViewModelFactory
import com.cryptium.coincurrently.ui.main.HomeTab.Home.Components.FearGreedMeter
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import networking.ResponseModels.TimeCharts

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
    val swipeScope = rememberCoroutineScope()

    val biggestValue = remember { mutableStateOf(DpSize.Zero) }

    var showInfoSheet by remember { mutableStateOf(false) }
    var showCostInfoSheet by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        com.cryptium.coincurrently.ui.main.SettingsTab.Achievements.Managers.AchievementManager.increment(
            com.cryptium.coincurrently.ui.main.SettingsTab.Achievements.Models.AchievementCounterKey.gasFeesVisits
        )
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
            title = "ETH Gas Fee",
            accessibilityTitle = "Ethereum gas fee",
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
                            .padding(horizontal = 16.dp)
                            .chartAccessibilityOverlay(
                                timeStamps = viewModel.timeStamps.value,
                                prices = viewModel.prices.value,
                                timeChart = TimeCharts.aWeek,
                                trailingPadding = biggestValue.value.width,
                                priceFormatter = { "${it.numberAccessible(maxPlaces = 2)} Gwei" },
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
                                    positions = viewModel.dragPositions,
                                    isDragging = viewModel.isDragging,
                                    color = viewModel.graphColor,
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
                            priceFormatter = { "${it.numberAccessible(maxPlaces = 2)} Gwei" },
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
            contentDescription = "Help understand how ethereum gas fees work",
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
                        contentDescription = "$currentValueAccessible. $currentStatus"
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
            "Low",
            style = satoshiMedium(14),
            color = MaterialTheme.colors.onSurfaceContainerDimmer,
            modifier = Modifier.hideFromTalkBackWhenEnabled(),
        )

        Spacer(modifier = Modifier.weight(1f))

        Text(
            "High",
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
            text = "Historical Data",
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
                text = "Estimated Cost",
                style = satoshiBold(22),
                color = MaterialTheme.colors.onSurfaceDim,
                modifier = Modifier.semantics { heading() },
            )

            Spacer(modifier = Modifier.weight(1f))

            Image(
                painter = painterResource(id = R.drawable.info),
                contentDescription = "Estimated Cost info",
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