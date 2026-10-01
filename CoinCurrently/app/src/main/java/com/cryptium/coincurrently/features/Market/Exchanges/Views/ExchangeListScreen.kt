package com.cryptium.coincurrently.features.Market.Exchanges.Views
import com.cryptium.coincurrently.sharedui.modifiers.imageSize

import com.cryptium.coincurrently.app.NavigationScreens
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.runtime.derivedStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cryptium.coincurrently.R
import com.cryptium.coincurrently.sharedui.buttons.ScrollToTopButton
import com.cryptium.coincurrently.sharedui.buttons.SecondaryButton
import com.cryptium.coincurrently.sharedui.components.AccessibilityLayout
import com.cryptium.coincurrently.sharedui.theme.onSurfaceDimmer
import com.cryptium.coincurrently.sharedui.theme.surfaceContainerHigh
import com.cryptium.coincurrently.core.extensions.isTablet
import com.cryptium.coincurrently.sharedui.models.SortOption
import com.cryptium.coincurrently.app.NavigationViewModel
import com.cryptium.coincurrently.sharedui.components.ObserveCurrencyChanges
import com.cryptium.coincurrently.sharedui.components.OnLifecycleEvent
import com.cryptium.coincurrently.features.CoinDetail.Sheets.SelectCoinSheet
import com.cryptium.coincurrently.core.analytics.AnalyticsManager
import com.cryptium.coincurrently.core.localization.Localization
import com.cryptium.coincurrently.core.analytics.Navigate
import com.cryptium.coincurrently.core.analytics.ScreenLabel
import com.cryptium.coincurrently.core.analytics.ScrollToTopPressed
import com.cryptium.coincurrently.core.analytics.SheetLabel
import com.cryptium.coincurrently.core.analytics.SheetOpened
import com.cryptium.coincurrently.core.analytics.SortOptionSelected
import com.cryptium.coincurrently.features.Market.Exchanges.ViewModels.ExchangesViewModel
import com.cryptium.coincurrently.features.Market.Toplist.Sheets.ToplistSortBySheet
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.cryptium.coincurrently.core.networking.ResponseModels.CryptoDetailInfoModel
import com.cryptium.coincurrently.core.networking.ResponseModels.ExchangeList
import com.cryptium.coincurrently.core.networking.ResponseModels.toCryptoDetail
import com.cryptium.coincurrently.core.networking.ResponseModels.toExchange
import kotlin.time.Duration.Companion.seconds
import com.cryptium.coincurrently.app.SearchCase
import com.cryptium.coincurrently.app.SearchScreenCase

@Composable
fun ExchangeListScreen(
    currentPage: Int,
    scrollState: LazyListState,
    navigationViewModel: NavigationViewModel
) {
    val coroutineScope = rememberCoroutineScope()
    val isTablet = LocalContext.current.isTablet()

    val viewModel: ExchangesViewModel = viewModel()
    var isRefreshing by remember { mutableStateOf(false) }


    val isSearchShowing = remember { mutableStateOf(false) }
    val isSortShowing = remember { mutableStateOf(false) }

    val firstVisibleIndex by remember {
        derivedStateOf { scrollState.firstVisibleItemIndex }
    }
    val talkBackEnabled = AccessibilityLayout.isTalkBackEnabled

    LaunchedEffect(currentPage) {
        if (currentPage == 4) {
            launch {
                viewModel.getExchanges()
            }
            launch {
                viewModel.getBitcoinData()
            }
        }
    }

    ObserveCurrencyChanges(navigationViewModel) {
        kotlinx.coroutines.coroutineScope {
            launch { viewModel.getExchanges() }
            launch { viewModel.getBitcoinData() }
        }
    }

    OnLifecycleEvent { _, event ->
        when (event) {
            Lifecycle.Event.ON_RESUME -> {
                if (currentPage == 4) {
                    coroutineScope.launch {
                        kotlinx.coroutines.coroutineScope {
                            launch {
                                viewModel.getExchanges()
                            }
                            launch {
                                viewModel.getBitcoinData()
                            }
                        }
                    }
                }
            }
            else -> { }
        }
    }

    Box(
        contentAlignment = Alignment.BottomEnd
    ) {
        SwipeRefresh(
            state = rememberSwipeRefreshState(isRefreshing = isRefreshing),
            onRefresh = {
                isRefreshing = true

                coroutineScope.launch {
                    launch {
                        viewModel.getExchanges()
                        isRefreshing = false
                    }
                    launch {
                        viewModel.getBitcoinData()
                    }

                    delay(10.seconds)
                    isRefreshing = false
                }
            }
        ) {
            val configuration = LocalConfiguration.current
            val screenHeight = configuration.screenHeightDp.dp

            LazyColumn(
                state = scrollState,
                contentPadding = PaddingValues(bottom = 16.dp),
                modifier = Modifier
                    .height(screenHeight)
            ) {
                item {
                    FilterButtons(
                        selectedSortOption = viewModel.selectedSortOption,
                        isSearchShowing = isSearchShowing,
                        isSortShowing = isSortShowing,
                    )
                }

                item {
                    ExchangesHeaderView(
                        modifier = Modifier
                            .padding(bottom = 6.dp)
                    )
                }

                if(viewModel.exchanges.isEmpty()) {
                    TempList(
                        isTablet
                    )
                } else {
                    RealList(
                        viewModel.exchanges,
                        viewModel.bitcoin,
                        exchangePressed = { exchange ->
                            val dest = NavigationScreens.ExchangeDetail(
                                exchange.toExchange
                            )
                            AnalyticsManager.log(
                                Navigate(
                                    fromScreen = ScreenLabel.Exchanges,
                                    toScreen = ScreenLabel.ExchangeDetail,
                                ),
                            )
                            navigationViewModel.navigate(dest)
                        }
                    )
                }
            }
        }

        if (isSearchShowing.value) {
            SelectCoinSheet(
                searchScreenCase = SearchScreenCase.Search(
                    list = SearchCase.Exchanges
                ),
                fromScreen = ScreenLabel.Exchanges,
                highlightId = null,
                hideId = null,
                selectedCoin = { coin ->
                    val coin = coin ?: return@SelectCoinSheet

                    val dest = NavigationScreens.ExchangeDetail(
                        coin.toExchange()
                    )
                    navigationViewModel.navigate(dest)
                },
                close = {
                    isSearchShowing.value = false
                }
            )
        }

        if (isSortShowing.value) {
            ToplistSortBySheet(
                options = SortOption.exchangesOptions,
                selectedOption = viewModel.selectedSortOption,
                newOptionSelected = { option ->
                    AnalyticsManager.log(
                        SortOptionSelected(fromScreen = ScreenLabel.Exchanges, option = option),
                    )
                    viewModel.setNewSortOption(option)
                },
                close = {
                    isSortShowing.value = false
                }
            )
        }

        ScrollToTopButton(
            visible = !talkBackEnabled && firstVisibleIndex >= 15,
            onClick = {
                AnalyticsManager.log(ScrollToTopPressed(fromScreen = ScreenLabel.Exchanges))
                coroutineScope.launch {
                    scrollState.animateScrollToItem(0)
                }
            }
        )
    }
}

@Composable
private fun FilterButtons(
    selectedSortOption: SortOption,
    isSearchShowing: MutableState<Boolean>,
    isSortShowing: MutableState<Boolean>,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .horizontalScroll(rememberScrollState())
            .padding(
                top = 32.dp,
                start = 16.dp,
                end = 16.dp,
                bottom = 8.dp
            )
    ) {
        Image(
            painter = painterResource(R.drawable.search),
            contentDescription = Localization.string("Search for exchanges"),
            colorFilter = ColorFilter.tint(MaterialTheme.colors.onSurfaceDimmer),
            modifier = Modifier
                .clip(CircleShape)
                .clickable {
                    AnalyticsManager.log(
                        SheetOpened(fromScreen = ScreenLabel.Exchanges, sheet = SheetLabel.CoinSearch),
                    )
                    isSearchShowing.value = true
                }
                .background(MaterialTheme.colors.surfaceContainerHigh)
                .padding(12.dp)
                .imageSize(24.dp)
        )

        SecondaryButton(
            text = Localization.string(selectedSortOption.title),
            accessibilityLabel = Localization.format(
                "Sort, currently selected: %@",
                Localization.string(selectedSortOption.accessibilityTitle),
            ),
            icon = R.drawable.sort,
            onClick = {
                AnalyticsManager.log(
                    SheetOpened(fromScreen = ScreenLabel.Exchanges, sheet = SheetLabel.SortBy),
                )
                isSortShowing.value = true
            }
        )
    }
}

private fun LazyListScope.TempList(isTablet: Boolean) {
    val count = if (isTablet) 30 else 15
    items(count) { index ->
        ExchangeListView(
            exchange = null,
            bitcoin = null,
            listIndex = index,
            listCount = count,
            exchangePressed = { }
        )
    }
}

private fun LazyListScope.RealList(
    exchanges: List<ExchangeList>,
    bitcoin: CryptoDetailInfoModel?,
    exchangePressed: (ExchangeList) -> Unit
) {
    items(exchanges) {
        val index = exchanges.indexOf(it)
        ExchangeListView(
            exchange = it,
            bitcoin = bitcoin,
            listIndex = index,
            listCount = exchanges.size,
            exchangePressed = exchangePressed
        )
    }
}