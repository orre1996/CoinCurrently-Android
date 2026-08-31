package com.cryptium.coincurrently.ui.main.MarketTab.Derivatives.Views

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Text
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.invisibleToUser
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cryptium.coincurrently.R
import com.cryptium.coincurrently.ui.main.Compose.*
import com.cryptium.coincurrently.ui.main.Extensions.formatCurrency
import com.cryptium.coincurrently.ui.main.Extensions.isTablet
import com.cryptium.coincurrently.ui.main.Extensions.getSelectedCurrency
import com.cryptium.coincurrently.ui.main.Extensions.satoshiBold
import com.cryptium.coincurrently.ui.main.Extensions.satoshiMedium
import com.cryptium.coincurrently.ui.main.General.Components.CustomTopAppBar
import com.cryptium.coincurrently.ui.main.General.Enums.FormattingThreshold
import com.cryptium.coincurrently.ui.main.General.Enums.SortOption
import com.cryptium.coincurrently.ui.main.General.NavigationViewModel
import com.cryptium.coincurrently.ui.main.General.OnLifecycleEvent
import com.cryptium.coincurrently.ui.main.MarketTab.CryptoDetail.Components.General.ScrollableTabSelector
import com.cryptium.coincurrently.ui.main.MarketTab.Derivatives.ViewModels.DerivativeDetailsViewModel
import com.cryptium.coincurrently.ui.main.MarketTab.Derivatives.ViewModels.DerivativeDetailsViewModelFactory
import com.cryptium.coincurrently.ui.main.MarketTab.Toplist.Popups.ToplistSortBySheet
import com.cryptium.coincurrently.ui.main.analytics.AnalyticsManager
import com.cryptium.coincurrently.ui.main.analytics.ScreenLabel
import com.cryptium.coincurrently.ui.main.analytics.ScrollToTopPressed
import com.cryptium.coincurrently.ui.main.analytics.SheetLabel
import com.cryptium.coincurrently.ui.main.analytics.SheetOpened
import com.cryptium.coincurrently.ui.main.analytics.SortOptionSelected
import com.cryptium.coincurrently.ui.main.analytics.TabChanged
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds
import networking.ResponseModels.*

@Composable
fun DerivativeDetailScreen(
    navigationViewModel: NavigationViewModel,
    modifier: Modifier,
    derivative: Derivative
) {
    val viewModel: DerivativeDetailsViewModel = viewModel(
        factory = DerivativeDetailsViewModelFactory(derivative)
    )

    var selectedTicker: DerivativeDetailModel? by remember { mutableStateOf(null) }
    var currentPage by remember { mutableStateOf(0) }
    var isRefreshing by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberLazyListState()

    val isSortShowing = remember { mutableStateOf(false) }

    val isTablet = LocalContext.current.isTablet()
    val talkBackEnabled = AccessibilityLayout.isTalkBackEnabled

    val tabs = listOf("Perpetuals", "Futures")

    val selectTicker: (DerivativeDetailModel) -> Unit = { ticker ->
        AnalyticsManager.log(
            SheetOpened(
                fromScreen = ScreenLabel.DerivativeDetail,
                sheet = SheetLabel.DerivativeTickerDetail
            )
        )
        selectedTicker = ticker
    }

    val firstVisibleIndex by remember {
        derivedStateOf { scrollState.firstVisibleItemIndex }
    }

    LaunchedEffect(Unit) {
        coroutineScope {
            launch { viewModel.getBitcoin() }
            launch { viewModel.getDerivatives() }
        }
    }

    OnLifecycleEvent { _, event ->
        when (event) {
            Lifecycle.Event.ON_RESUME -> {
                coroutineScope.launch {
                    kotlinx.coroutines.coroutineScope {
                        launch { viewModel.getBitcoin() }
                        launch { viewModel.getDerivatives() }
                    }
                }
            }
            else -> { }
        }
    }

    BackHandler(onBack = {
        navigationViewModel.navigateBack()
    })

    Box(
        contentAlignment = Alignment.BottomEnd
    ) {
        Column(
            modifier = modifier
        ) {
            CustomTopAppBar(
                title = viewModel.derivative?.displayName ?: "",
                navigateBack = {
                    navigationViewModel.navigateBack()
                }
            )

            SwipeRefresh(
                state = rememberSwipeRefreshState(isRefreshing = isRefreshing),
                onRefresh = {
                    isRefreshing = true
                    coroutineScope.launch {
                        try {
                            coroutineScope {
                                launch { viewModel.getDerivatives() }
                                launch { viewModel.getBitcoin() }
                            }
                            delay(10.seconds)
                        } finally {
                            isRefreshing = false
                        }
                    }
                },
            ) {
                val configuration = LocalConfiguration.current
                val screenHeight = configuration.screenHeightDp.dp
                val useStickyHeaders = AccessibilityLayout.useStickyHeaders

                LazyColumn(
                    state = scrollState,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(
                        top = 16.dp,
                        bottom = 32.dp
                    ),
                    modifier = Modifier
                        .height(screenHeight)
                ) {
                    item {
                        TradingVolumeView(
                            volumeBtc = viewModel.derivative?.trade_volume_24h_btc,
                            bitcoin = viewModel.bitcoin
                        )
                    }

                    item {
                        SummaryView(
                            derivative = viewModel.derivative,
                            bitcoin = viewModel.bitcoin
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
                                        TabChanged(
                                            fromScreen = ScreenLabel.DerivativeDetail,
                                            selectedTabName = tabs[it]
                                        )
                                    )
                                }
                                currentPage = it

                                coroutineScope.launch {
                                    scrollState.animateScrollToItem(
                                        if (talkBackEnabled) 3 else 2
                                    )
                                }
                            }
                        )
                    }

                    item {
                        SecondaryButton(
                            text = viewModel.selectedSortOption.title,
                            accessibilityLabel = "Sort. currently selected: ${viewModel.selectedSortOption.accessibilityTitle}",
                            icon = R.drawable.sort,
                            onClick = {
                                AnalyticsManager.log(
                                    SheetOpened(
                                        fromScreen = ScreenLabel.DerivativeDetail,
                                        sheet = SheetLabel.SortBy
                                    )
                                )
                                isSortShowing.value = true
                            },
                            modifier = Modifier
                                .tabPageTalkBackTraversal()
                                .padding(
                                    vertical = 8.dp,
                                    horizontal = 16.dp
                                )
                        )
                    }

                    item {
                        Box(modifier = Modifier.tabPageTalkBackTraversal()) {
                            SectionHeader(
                                currentPage = currentPage
                            )
                        }
                    }

                    if (viewModel.perpetualList.isEmpty() && viewModel.futuresList.isEmpty()) {
                        TempList(
                            currentPage = currentPage,
                            isTablet = isTablet
                        )
                    } else if (currentPage == 0) {
                        PerpetualsList(
                            perpetuals = viewModel.perpetualList,
                            multiplier = viewModel.multiplier,
                            toplist = viewModel.toplist,
                            derivativeSelected = selectTicker
                        )
                    } else if (currentPage == 1) {
                        FuturesList(
                            perpetuals = viewModel.futuresList,
                            multiplier = viewModel.multiplier,
                            toplist = viewModel.toplist,
                            derivativeSelected = selectTicker
                        )
                    }
                }
            }
        }

        ScrollToTopButton(
            visible = !talkBackEnabled && firstVisibleIndex >= 15,
            respectNavigationBars = true,
            onClick = {
                AnalyticsManager.log(
                    ScrollToTopPressed(
                        fromScreen = ScreenLabel.DerivativeDetail,
                        selectedTabName = tabs[currentPage]
                    )
                )
                coroutineScope.launch {
                    scrollState.animateScrollToItem(2)
                }
            }
        )

        if (isSortShowing.value) {
            ToplistSortBySheet(
                SortOption.derivativeDetailOptions,
                selectedOption = viewModel.selectedSortOption,
                newOptionSelected = {
                    AnalyticsManager.log(
                        SortOptionSelected(
                            fromScreen = ScreenLabel.DerivativeDetail,
                            option = viewModel.selectedSortOption
                        )
                    )
                    viewModel.setSortOption(it)
                },
                close = {
                    isSortShowing.value = false
                }
            )
        }

        selectedTicker?.let { derivative ->
            DerivativeDetailSheet(
                derivativeDetail = derivative,
                multiplier = viewModel.multiplier,
                imageUrl = viewModel.toplist?.firstOrNull{ it.symbol?.lowercase() == derivative.base?.lowercase()}?.image,
                hasToplist = viewModel.toplist != null,
                exchangeName = viewModel.derivative?.displayName ?: "",
                close = {
                    selectedTicker = null
                }
            )
        }
    }
}

@Composable
private fun TradingVolumeView(
    volumeBtc: String?,
    bitcoin: CryptoDetailInfoModel?
) {
    val volume = volumeBtc?.toDoubleOrNull() ?: 0.0
    val bitcoinPrice = bitcoin?.market_data?.current_price?.getSelectedCurrency() ?: 0.0
    val addedVolume = volume * bitcoinPrice
    val valueAccessible = addedVolume.formatCurrency(
        threshold = FormattingThreshold.NONE,
        maxDecimals = if (addedVolume < 1_000_000) 2 else 0,
        accessible = true,
    )

    Column(
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .semantics(mergeDescendants = true) {
                contentDescription = "24 hour volume: $valueAccessible"
            }
    ) {
        Text(
            text = "24h Volume",
            style = satoshiBold(18),
            color = MaterialTheme.colors.onSurfaceDimmer,
            modifier = Modifier.semantics { invisibleToUser() }
        )

        Text(
            text = addedVolume.formatCurrency(
                threshold = FormattingThreshold.NONE,
                maxDecimals = if (addedVolume < 1_000_000) 2 else 0
            ),
            style = satoshiBold(36),
            color = MaterialTheme.colors.onSurface,
            modifier = Modifier
                .shimmeringModifier(volumeBtc == null || bitcoin == null)
                .semantics { invisibleToUser() }
        )
    }
}

@Composable
private fun SummaryView(
    derivative: Derivative?,
    bitcoin: CryptoDetailInfoModel?
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .height(IntrinsicSize.Min)
            .padding(
                start = 16.dp,
                top = 8.dp,
                end = 16.dp,
                bottom = 8.dp
            )
            .primaryBackgroundNoRipple()
    ) {
        val interest = derivative?.open_interest_btc ?: 0.0
        val bitcoinPrice = bitcoin?.market_data?.current_price?.getSelectedCurrency() ?: 0.0
        val openInterestAccessible = (interest * bitcoinPrice).formatCurrency(accessible = true)

        Column(
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier
                .weight(1f)
                .semantics(mergeDescendants = true) {
                    contentDescription = "24 hour open interest: $openInterestAccessible"
                }
        ) {
            Text(
                text = "24h Open Interest",
                style = satoshiBold(12),
                color = MaterialTheme.colors.onSurfaceDim,
                modifier = Modifier.semantics { invisibleToUser() }
            )

            Text(
                text = (interest * bitcoinPrice).formatCurrency(),
                style = satoshiBold(16),
                color = MaterialTheme.colors.onSurface,
                modifier = Modifier
                    .shimmeringModifier(derivative == null || bitcoin == null)
                    .semantics { invisibleToUser() }
            )
        }

        SeparatorVertical(
            width = 2.dp
        )

        Column(
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier
                .weight(1f)
                .semantics(mergeDescendants = true) {}
        ) {
            Text(
                text = "Pairs",
                style = satoshiBold(12),
                color = MaterialTheme.colors.onSurfaceDim
            )

            Text(
                text = "${((derivative?.number_of_perpetual_pairs ?: 0) + (derivative?.number_of_futures_pairs?: 0))}",
                style = satoshiBold(16),
                color = MaterialTheme.colors.onSurface,
                modifier = Modifier
                    .shimmeringModifier(derivative == null)
            )
        }
    }
}

@Composable
private fun SectionHeader(
    currentPage: Int
) {
    val isHorizontal = AccessibilityLayout.isHorizontal

    Row(
        horizontalArrangement = Arrangement.spacedBy(24.dp),
        modifier = Modifier
            .clearAndSetSemantics {
                invisibleToUser()
            }
            .padding(
                start = 16.dp,
                bottom = 0.dp,
                end = 16.dp
            )
    ) {
        Text(
            text = if (currentPage == 0) "Symbol" else "Contract",
            style = satoshiMedium(14),
            color = MaterialTheme.colors.onSurfaceDim,
            modifier = Modifier
                .weight(1f, fill = isHorizontal)
        )

        if (isHorizontal) {
            Text(
                text = "Price",
                style = satoshiMedium(14),
                color = MaterialTheme.colors.onSurfaceDim,
                maxLines = 1
            )

            Text(
                text = "Open Interest",
                style = satoshiMedium(14),
                color = MaterialTheme.colors.onSurfaceDim,
                maxLines = 1,
                textAlign = TextAlign.End
            )
        } else {
            Text(
                text = "Price / Open Interest",
                style = satoshiMedium(14),
                color = MaterialTheme.colors.onSurfaceDim,
                textAlign = TextAlign.End,
                modifier = Modifier
                    .weight(1f)
            )
        }
    }
}


private fun LazyListScope.TempList(
    currentPage: Int,
    isTablet: Boolean
) {
    val count = if (isTablet) 30 else 15
    items(count) { index ->
        Box(modifier = Modifier.tabPageTalkBackTraversal()) {
            DerivativeDetailItem(
                derivativeDetail = null,
                multiplier = null,
                isFutures = currentPage == 0,
                listIndex = index,
                listCount = count,
                imageUrl = null,
                hasToplist = false,
                derivativeSelected = { }
            )
        }
    }
}

private fun LazyListScope.PerpetualsList(
    perpetuals: List<DerivativeDetailModel>,
    multiplier: Double?,
    toplist: List<Toplist>?,
    derivativeSelected: (DerivativeDetailModel) -> Unit
) {
    itemsIndexed(perpetuals) { index, derivative ->
        Box(modifier = Modifier.tabPageTalkBackTraversal()) {
            DerivativeDetailItem(
                derivativeDetail = derivative,
                multiplier = multiplier,
                isFutures = false,
                listIndex = index,
                listCount = perpetuals.size,
                imageUrl = toplist?.firstOrNull { it.symbol?.lowercase() == derivative.base?.lowercase() }?.image,
                hasToplist = toplist != null,
                derivativeSelected = {
                    derivativeSelected.invoke(derivative)
                }
            )
        }
    }
}

private fun LazyListScope.FuturesList(
    perpetuals: List<DerivativeDetailModel>,
    multiplier: Double?,
    toplist: List<Toplist>?,
    derivativeSelected: (DerivativeDetailModel) -> Unit
) {
    itemsIndexed(perpetuals) { index, derivative ->
        Box(modifier = Modifier.tabPageTalkBackTraversal()) {
            DerivativeDetailItem(
                derivativeDetail = derivative,
                multiplier = multiplier,
                isFutures = true,
                listIndex = index,
                listCount = perpetuals.size,
                imageUrl = toplist?.firstOrNull { it.symbol?.lowercase() == derivative.base?.lowercase() }?.image,
                hasToplist = toplist != null,
                derivativeSelected = {
                    derivativeSelected.invoke(derivative)
                }
            )
        }
    }
}
