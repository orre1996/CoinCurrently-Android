package com.cryptium.coincurrently.features.Treasuries.Views
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.draw.alpha
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cryptium.coincurrently.R
import com.cryptium.coincurrently.sharedui.components.AccessibilityLayout
import com.cryptium.coincurrently.sharedui.buttons.ScrollToTopButton
import com.cryptium.coincurrently.sharedui.buttons.SecondaryButton
import com.cryptium.coincurrently.core.extensions.isTablet
import com.cryptium.coincurrently.sharedui.sheets.EmptyContent
import com.cryptium.coincurrently.app.NavigationScreens
import com.cryptium.coincurrently.features.Treasuries.TreasuryEntityFilter
import com.cryptium.coincurrently.app.NavigationViewModel
import com.cryptium.coincurrently.sharedui.components.ObserveCurrencyChanges
import com.cryptium.coincurrently.sharedui.components.OnLifecycleEvent
import com.cryptium.coincurrently.features.Treasuries.Sheets.TreasuryCoinSheet
import com.cryptium.coincurrently.features.Treasuries.Sheets.TreasuryEntityFilterSheet
import com.cryptium.coincurrently.features.Treasuries.ViewModels.TreasuriesViewModel
import com.cryptium.coincurrently.core.analytics.AnalyticsManager
import com.cryptium.coincurrently.core.analytics.Navigate
import com.cryptium.coincurrently.core.analytics.ScreenLabel
import com.cryptium.coincurrently.core.analytics.ScrollToTopPressed
import com.cryptium.coincurrently.core.analytics.SheetLabel
import com.cryptium.coincurrently.core.analytics.SheetOpened
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

@Composable
fun TreasuriesListFragment(
    currentPage: Int,
    scrollState: LazyListState,
    navigationViewModel: NavigationViewModel,
) {
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val isTablet = LocalContext.current.isTablet()
    val coroutineScope = rememberCoroutineScope()

    val viewModel: TreasuriesViewModel = viewModel()
    var isRefreshing by remember { mutableStateOf(false) }
    var isEntityFilterShowing by remember { mutableStateOf(false) }
    var isCoinShowing by remember { mutableStateOf(false) }

    val firstVisibleIndex by remember {
        derivedStateOf { scrollState.firstVisibleItemIndex }
    }
    val talkBackEnabled = AccessibilityLayout.isTalkBackEnabled

    LaunchedEffect(Unit) {
        viewModel.setStartData()
    }

    LaunchedEffect(currentPage) {
        if (currentPage == 2) {
            coroutineScope.launch {
                coroutineScope {
                    launch { viewModel.getTreasuries() }
                    launch { viewModel.getBitcoin() }
                }
            }
        }
    }

    ObserveCurrencyChanges(navigationViewModel) {
        coroutineScope {
            launch { viewModel.getTreasuries() }
            launch { viewModel.getBitcoin() }
        }
    }

    OnLifecycleEvent { _, event ->
        when (event) {
            Lifecycle.Event.ON_RESUME -> {
                if (currentPage == 2) {
                    coroutineScope.launch {
                        coroutineScope {
                            launch { viewModel.getTreasuries() }
                            launch { viewModel.getBitcoin() }
                        }
                    }
                }
            }
            else -> Unit
        }
    }

    Box(contentAlignment = Alignment.BottomEnd) {
        SwipeRefresh(
            state = rememberSwipeRefreshState(isRefreshing = isRefreshing),
            onRefresh = {
                isRefreshing = true
                coroutineScope.launch {
                    coroutineScope {
                        launch { viewModel.getTreasuries() }
                        launch { viewModel.getBitcoin() }
                    }
                    delay(10.seconds)
                    isRefreshing = false
                }
            },
        ) {
            LazyColumn(
                state = scrollState,
                contentPadding = PaddingValues(bottom = 16.dp),
                modifier = Modifier.height(screenHeight),
            ) {
                item {
                    TreasuriesSummaryView(
                        totalHoldings = viewModel.filteredTotalHoldings,
                        marketCapDominance = viewModel.filteredMarketCapDominance,
                        coinSymbol = viewModel.selectedCoin?.symbol,
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                }

                item {
                    FilterButtons(
                        selectedEntityFilter = viewModel.selectedEntityFilter,
                        selectedCoinName = viewModel.selectedCoin?.name
                            ?: viewModel.selectedCoinId.replaceFirstChar { it.uppercase() },
                        hasCoins = viewModel.availableCoins.isNotEmpty(),
                        onEntityFilterClick = {
                            AnalyticsManager.log(
                                SheetOpened(fromScreen = ScreenLabel.Treasuries, sheet = SheetLabel.TreasuryEntityFilter),
                            )
                            isEntityFilterShowing = true
                        },
                        onCoinClick = {
                            AnalyticsManager.log(
                                SheetOpened(fromScreen = ScreenLabel.Treasuries, sheet = SheetLabel.CoinSearch),
                            )
                            isCoinShowing = true
                        },
                    )
                }

                item {
                    TreasuriesHeaderView(
                        modifier = Modifier
                            .alpha(if (viewModel.holders.isEmpty() && viewModel.treasury != null) 0f else 1f)
                            .padding(bottom = 6.dp),
                    )
                }

                when {
                    viewModel.treasury == null -> tempList(isTablet = isTablet, viewModel = viewModel)
                    viewModel.holders.isEmpty() -> emptyList(viewModel = viewModel)
                    else -> realList(
                        viewModel = viewModel,
                        navigationViewModel = navigationViewModel,
                        coroutineScope = coroutineScope,
                    )
                }
            }
        }

        if (isEntityFilterShowing) {
            TreasuryEntityFilterSheet(
                selectedFilter = viewModel.selectedEntityFilter,
                newFilterSelected = { filter ->
                    viewModel.setEntityFilter(filter)
                },
                close = { isEntityFilterShowing = false },
            )
        }

        if (isCoinShowing) {
            TreasuryCoinSheet(
                coins = viewModel.availableCoins,
                selectedCoinId = viewModel.selectedCoinId,
                newCoinSelected = { coin ->
                    coroutineScope.launch {
                        viewModel.setSelectedCoin(coin)
                    }
                },
                close = { isCoinShowing = false },
            )
        }

        ScrollToTopButton(
            visible = !talkBackEnabled && firstVisibleIndex >= 15,
            onClick = {
                AnalyticsManager.log(ScrollToTopPressed(fromScreen = ScreenLabel.Treasuries))
                coroutineScope.launch {
                    scrollState.animateScrollToItem(0)
                }
            },
        )
    }
}

@Composable
private fun FilterButtons(
    selectedEntityFilter: TreasuryEntityFilter,
    selectedCoinName: String,
    hasCoins: Boolean,
    onEntityFilterClick: () -> Unit,
    onCoinClick: () -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .horizontalScroll(rememberScrollState())
            .padding(top = 24.dp, start = 16.dp, end = 16.dp, bottom = 8.dp),
    ) {
        SecondaryButton(
            text = selectedEntityFilter.title,
            accessibilityLabel = "Type, currently selected: ${selectedEntityFilter.accessibilityTitle}",
            icon = R.drawable.chevron_down,
            onClick = onEntityFilterClick,
        )

        SecondaryButton(
            text = selectedCoinName,
            accessibilityLabel = "Coin, currently selected: $selectedCoinName",
            icon = R.drawable.chevron_down,
            isEnabled = hasCoins,
            onClick = onCoinClick,
        )
    }
}

private fun LazyListScope.tempList(
    isTablet: Boolean,
    viewModel: TreasuriesViewModel,
) {
    val count = if (isTablet) 30 else 15
    items(count) {
        TreasuriesItemView(
            data = null,
            coinSymbol = viewModel.selectedCoin?.symbol,
            priceMultiplier = viewModel.multiplier,
            onTreasuryClick = { },
        )
    }
}

private fun LazyListScope.emptyList(viewModel: TreasuriesViewModel) {
    item {
        EmptyContent(
            title = emptyTitle(viewModel),
            subTitle = emptySubTitle(viewModel),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 40.dp),
        )
    }
}

private fun LazyListScope.realList(
    viewModel: TreasuriesViewModel,
    navigationViewModel: NavigationViewModel,
    coroutineScope: CoroutineScope,
) {
    itemsIndexed(viewModel.holders, key = { index, item ->
        "${item.entity.name}-${item.holder.name}-$index"
    }) { index, treasury ->
        TreasuriesItemView(
            data = treasury.holder,
            coinSymbol = viewModel.selectedCoin?.symbol,
            priceMultiplier = viewModel.multiplier,
            listIndex = index,
            listCount = viewModel.holders.size,
            onTreasuryClick = {
                coroutineScope.launch {
                    val resolvedHolder = viewModel.resolveHolderWithEntityId(
                        treasury.holder,
                        treasury.entity,
                    )
                    val dest = NavigationScreens.TreasuryDetail(
                        holder = resolvedHolder,
                        coinId = treasury.coinId,
                    )
                    AnalyticsManager.log(
                        Navigate(
                            fromScreen = ScreenLabel.Treasuries,
                            toScreen = ScreenLabel.TreasuryDetail,
                            treasuryName = resolvedHolder.name,
                        ),
                    )
                    navigationViewModel.navigate(dest)
                }
            },
        )
    }
}

private fun emptyTitle(viewModel: TreasuriesViewModel): String {
    return when (viewModel.selectedEntityFilter) {
        TreasuryEntityFilter.All -> "No treasuries yet"
        TreasuryEntityFilter.Companies -> "No company treasuries yet"
        TreasuryEntityFilter.Governments -> "No government treasuries yet"
    }
}

private fun emptySubTitle(viewModel: TreasuriesViewModel): String {
    val coinName = viewModel.selectedCoin?.name
        ?: viewModel.selectedCoinId.replaceFirstChar { it.uppercase() }

    return when (viewModel.selectedEntityFilter) {
        TreasuryEntityFilter.All -> "No one currently holds $coinName."
        TreasuryEntityFilter.Companies -> "No companies currently hold $coinName."
        TreasuryEntityFilter.Governments -> "No governments currently hold $coinName."
    }
}
