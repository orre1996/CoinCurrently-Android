package com.cryptium.coincurrently.ui.main.MarketTab.Toplist.Views

import com.cryptium.coincurrently.ui.main.Compose.imageSize

import NavigationScreens
import android.annotation.SuppressLint
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
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.invisibleToUser
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cryptium.coincurrently.R
import com.cryptium.coincurrently.ui.main.Compose.AccessibilityLayout
import com.cryptium.coincurrently.ui.main.Compose.ScrollToTopButton
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import com.cryptium.coincurrently.ui.main.Compose.SecondaryButton
import com.cryptium.coincurrently.ui.main.Compose.onSurfaceDim
import com.cryptium.coincurrently.ui.main.Compose.onSurfaceDimmer
import com.cryptium.coincurrently.ui.main.Compose.shimmeringModifier
import com.cryptium.coincurrently.ui.main.Compose.surfaceContainerHigh
import com.cryptium.coincurrently.ui.main.Extensions.changeColor
import com.cryptium.coincurrently.ui.main.Extensions.changePercentAccessible
import com.cryptium.coincurrently.ui.main.Extensions.changeWithSymbol
import com.cryptium.coincurrently.ui.main.Extensions.directionAccessible
import com.cryptium.coincurrently.ui.main.Extensions.formatCurrency
import com.cryptium.coincurrently.ui.main.Extensions.getChangeInValue
import com.cryptium.coincurrently.ui.main.Extensions.getDifferenceInPercent
import com.cryptium.coincurrently.ui.main.Extensions.isTablet
import com.cryptium.coincurrently.ui.main.Extensions.satoshiBold
import com.cryptium.coincurrently.ui.main.General.Components.NewPriceChangeValueView
import com.cryptium.coincurrently.ui.main.General.Enums.FormattingThreshold
import com.cryptium.coincurrently.ui.main.General.Enums.PriceChange
import com.cryptium.coincurrently.ui.main.General.Enums.SortOption
import com.cryptium.coincurrently.ui.main.General.Loading.ChangeSize
import com.cryptium.coincurrently.ui.main.General.NavigationViewModel
import com.cryptium.coincurrently.ui.main.General.WebView
import com.cryptium.coincurrently.ui.main.Managers.SecretMessageHelper
import com.cryptium.coincurrently.ui.main.General.ObserveCurrencyChanges
import com.cryptium.coincurrently.ui.main.General.OnLifecycleEvent
import com.cryptium.coincurrently.ui.main.HomeTab.GlobalMarket.Components.GlobalmarketSummaryView
import com.cryptium.coincurrently.ui.main.analytics.BuyProAction
import com.cryptium.coincurrently.ui.main.analytics.AnalyticsManager
import com.cryptium.coincurrently.ui.main.analytics.Navigate
import com.cryptium.coincurrently.ui.main.analytics.PriceChangeOptionSelected
import com.cryptium.coincurrently.ui.main.analytics.ScreenLabel
import com.cryptium.coincurrently.ui.main.analytics.ScrollToTopPressed
import com.cryptium.coincurrently.ui.main.analytics.SheetLabel
import com.cryptium.coincurrently.ui.main.analytics.SheetOpened
import com.cryptium.coincurrently.ui.main.analytics.SortOptionSelected
import com.cryptium.coincurrently.ui.main.MarketTab.Toplist.ViewModels.ToplistViewModel
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.cryptium.coincurrently.ui.main.HomeTab.Home.Components.ToplistCard
import com.cryptium.coincurrently.ui.main.HomeTab.Home.Components.ToplistHeaderView
import com.cryptium.coincurrently.ui.main.Managers.StorageKeys
import com.cryptium.coincurrently.ui.main.Managers.StorageManager
import com.cryptium.coincurrently.ui.main.MarketTab.CryptoDetail.Popups.SelectCoinSheet
import com.cryptium.coincurrently.ui.main.MarketTab.Toplist.Popups.PriceChangeSheet
import com.cryptium.coincurrently.ui.main.MarketTab.Toplist.Popups.ToplistSortBySheet
import com.cryptium.coincurrently.ui.main.MarketTab.Toplist.ViewModels.ToplistViewModelFactory
import com.cryptium.coincurrently.ui.main.PortfolioTab.ViewModels.NewTransactionViewModelFactory
import networking.ResponseModels.CryptoCategory
import networking.ResponseModels.CurrencySelectionModel
import networking.ResponseModels.TimeCharts
import networking.ResponseModels.Toplist
import networking.ResponseModels.toCryptoDetail
import kotlin.time.Duration.Companion.seconds

@Composable
fun ToplistFragment(
    currentPage: Int,
    category: CryptoCategory?,
    scrollState: LazyListState,
    navigationViewModel: NavigationViewModel
) {
    val configuration = LocalConfiguration.current
    val isTablet = LocalContext.current.isTablet()

    val viewModel: ToplistViewModel = viewModel(
        factory = ToplistViewModelFactory(
            category = category
        )
    )

    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    var isRefreshing by remember { mutableStateOf(false) }
    val screenHeight = configuration.screenHeightDp.dp

    val isSearchShowing = remember { mutableStateOf(false) }
    val isSortShowing = remember { mutableStateOf(false) }
    val isPriceChangeShowing = remember { mutableStateOf(false) }
    var selectedUrl by remember { mutableStateOf<String?>(null) }

    val firstVisibleIndex by remember {
        derivedStateOf { scrollState.firstVisibleItemIndex }
    }
    val talkBackEnabled = AccessibilityLayout.isTalkBackEnabled

    val marketSubTabRequestId = navigationViewModel.marketSubTabRequestId.intValue

    LaunchedEffect(currentPage, marketSubTabRequestId) {
        if (currentPage == 0) {
            val sort = StorageManager.get<SortOption>(StorageKeys.TOPLIST_SORT_OPTION) ?: SortOption.MktCapDesc
            val priceChange = StorageManager.get<PriceChange>(StorageKeys.TOPLIST_PRICE_CHANGE_OPTION) ?: PriceChange.SevenDays

            viewModel.getToplist(
                newSortOption = sort,
                newPriceChangeOption = priceChange,
            )
        }
    }

    ObserveCurrencyChanges(navigationViewModel) {
        viewModel.getToplist(
            newSortOption = viewModel.selectedSortOption,
            newPriceChangeOption = viewModel.selectedPriceChangeOption,
        )
    }

    OnLifecycleEvent { _, event ->
        when (event) {
            Lifecycle.Event.ON_RESUME -> {
                if (currentPage == 0) {
                    val sort = StorageManager.get<SortOption>(StorageKeys.TOPLIST_SORT_OPTION) ?: SortOption.MktCapDesc
                    val priceChange = StorageManager.get<PriceChange>(StorageKeys.TOPLIST_PRICE_CHANGE_OPTION) ?: PriceChange.SevenDays
                    coroutineScope.launch {
                        viewModel.getToplist(
                            newSortOption = sort,
                            newPriceChangeOption = priceChange,
                        )
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
                        viewModel.getToplist(
                            newSortOption = viewModel.selectedSortOption,
                            newPriceChangeOption = viewModel.selectedPriceChangeOption
                        )
                        isRefreshing = false
                    }

                    delay(10.seconds)
                    isRefreshing = false
                }
            }
        ) {
            LazyColumn(
                state = scrollState,
                contentPadding = PaddingValues(bottom = 16.dp),
                modifier = Modifier
                    .height(screenHeight)
            ) {
                category?.let {
                    item {

                        GlobalmarketSummaryView(
                            marketCap = it.market_cap,
                            changePercent = it.market_cap_change_24h,
                            selectedTimeframe = TimeCharts.aDay
                        )
                    }
                }

                item {
                    FilterButtons(
                        isPlatform = category != null,
                        selectedSortOption = viewModel.selectedSortOption,
                        selectedChangeOption = viewModel.selectedPriceChangeOption,
                        isSearchShowing = isSearchShowing,
                        isSortShowing = isSortShowing,
                        isPriceChangeShowing = isPriceChangeShowing
                    )
                }

                item {
                    ToplistHeaderView(
                        selectedChangeOption = viewModel.selectedPriceChangeOption,
                        modifier = Modifier
                            .padding(bottom = 6.dp)
                    )
                }

                if (viewModel.toplist.isEmpty()) {
                    TempList(
                        selectedChangeOption = viewModel.selectedPriceChangeOption,
                        isTablet
                    )
                } else {
                    RealList(
                        viewModel.toplist,
                        viewModel.favoritesList,
                        selectedChangeOption = viewModel.selectedPriceChangeOption,
                        toggleFavorite = {
                            viewModel.favouriteButtonPressed(
                                toplistItem = it,
                                context = context
                            ) {
                                navigationViewModel.openBuyPro(ScreenLabel.Toplist, BuyProAction.Favorite)
                            }
                        },
                        currencyPressed = { coin ->
                            if (!SecretMessageHelper.handleSecretToplistClick(coin) { selectedUrl = it }) {
                                val fromScreen = if (category?.id.isNullOrBlank()) {
                                    ScreenLabel.Toplist
                                } else {
                                    ScreenLabel.CategoryDetail
                                }
                                val dest = NavigationScreens.CryptoDetail(
                                    coin.toCryptoDetail(),
                                )
                                AnalyticsManager.log(
                                    Navigate(
                                        fromScreen = fromScreen,
                                        toScreen = ScreenLabel.CryptoDetail,
                                        coinName = coin.name ?: "",
                                    ),
                                )
                                navigationViewModel.navigate(dest)
                            }
                        }
                    )
                }
            }
        }

        if (isSearchShowing.value) {
            SelectCoinSheet(
                searchScreenCase = SearchScreenCase.Search(
                    list = SearchCase.Coins
                ),
                fromScreen = ScreenLabel.Toplist,
                highlightId = null,
                hideId = null,
                isSearch = true,
                onOpenUrl = { selectedUrl = it },
                selectedCoin = { coin ->
                    val coin = coin ?: return@SelectCoinSheet

                    val dest = NavigationScreens.CryptoDetail(
                        coin.toCryptoDetail()
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
                options = if(category == null) SortOption.toplistOptions else SortOption.sectorOptions,
                selectedOption = viewModel.selectedSortOption,
                newOptionSelected = { option ->
                    AnalyticsManager.log(
                        SortOptionSelected(fromScreen = ScreenLabel.Toplist, option = option),
                    )
                    coroutineScope.launch {
                        viewModel.getToplist(
                            newSortOption = option,
                            newPriceChangeOption = viewModel.selectedPriceChangeOption
                        )
                    }
                },
                close = {
                    isSortShowing.value = false
                }
            )
        }

        if (isPriceChangeShowing.value) {
            PriceChangeSheet(
                selectedOption = viewModel.selectedPriceChangeOption,
                newOptionSelected = { option ->
                    AnalyticsManager.log(
                        PriceChangeOptionSelected(fromScreen = ScreenLabel.Toplist, option = option),
                    )
                    coroutineScope.launch {
                        if (viewModel.selectedSortOption == SortOption.PriceChangeDesc || viewModel.selectedSortOption == SortOption.PriceChangeAsc) {
                            viewModel.getToplist(
                                newSortOption = viewModel.selectedSortOption,
                                newPriceChangeOption = option
                            )
                        } else {
                            viewModel.setNewPriceChangeOption(
                                newPriceChangeOption = option
                            )
                        }
                    }
                },
                close = {
                    isPriceChangeShowing.value = false
                }
            )
        }

        ScrollToTopButton(
            visible = !talkBackEnabled && firstVisibleIndex >= 15,
            onClick = {
                AnalyticsManager.log(ScrollToTopPressed(fromScreen = ScreenLabel.Toplist))
                coroutineScope.launch {
                    scrollState.animateScrollToItem(0)
                }
            }
        )

        selectedUrl?.let {
            WebView(it) {
                selectedUrl = null
            }
        }
    }
}


@Composable
private fun FilterButtons(
    isPlatform: Boolean,
    selectedSortOption: SortOption,
    selectedChangeOption: PriceChange,
    isSearchShowing: MutableState<Boolean>,
    isSortShowing: MutableState<Boolean>,
    isPriceChangeShowing: MutableState<Boolean>
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .horizontalScroll(rememberScrollState())
            .padding(
                top = if(isPlatform) 24.dp else 32.dp,
                start = 16.dp,
                end = 16.dp,
                bottom = 8.dp
            )
    ) {
        Image(
            painter = painterResource(R.drawable.search),
            contentDescription = "Search for coins",
            colorFilter = ColorFilter.tint(MaterialTheme.colors.onSurfaceDimmer),
            modifier = Modifier
                .clip(CircleShape)
                .clickable {
                    AnalyticsManager.log(
                        SheetOpened(fromScreen = ScreenLabel.Toplist, sheet = SheetLabel.CoinSearch),
                    )
                    isSearchShowing.value = true
                }
                .background(MaterialTheme.colors.surfaceContainerHigh)
                .padding(12.dp)
                .imageSize(24.dp)
        )

        SecondaryButton(
            text = selectedSortOption.title,
            accessibilityLabel = "Sort. currently selected: ${selectedSortOption.accessibilityTitle}",
            icon = R.drawable.sort,
            onClick = {
                AnalyticsManager.log(
                    SheetOpened(fromScreen = ScreenLabel.Toplist, sheet = SheetLabel.SortBy),
                )
                isSortShowing.value = true
            }
        )

        SecondaryButton(
            text = selectedChangeOption.rawValue,
            accessibilityLabel = "Timeframe. currently selected: ${selectedChangeOption.accessibilityTimeframe}",
            icon = R.drawable.percent,
            onClick = {
                AnalyticsManager.log(
                    SheetOpened(fromScreen = ScreenLabel.Toplist, sheet = SheetLabel.PriceChange),
                )
                isPriceChangeShowing.value = true
            }
        )
    }
}

@SuppressLint("UnrememberedMutableState")
private fun LazyListScope.TempList(
    selectedChangeOption: PriceChange,
    isTablet: Boolean
) {
    items(if (isTablet) 30 else 15) { index ->
        ToplistCard(
            coin = null,
            isFavorited = false,
            selectedChangeOption = selectedChangeOption,
            listIndex = index,
            listCount = if (isTablet) 30 else 15,
            navigateToCryptoDetail = { },
            toggleFavorite = { },
        )
    }
}

@SuppressLint("UnrememberedMutableState")
private fun LazyListScope.RealList(
    list: List<Toplist>,
    favoriteList: List<Toplist>,
    selectedChangeOption: PriceChange,
    toggleFavorite: (Toplist) -> Unit,
    currencyPressed: (Toplist) -> Unit
) {
    items(list) { coin ->
        val isFavorited = favoriteList.any { it.id == coin.id }
        val index = list.indexOf(coin)

        ToplistCard(
            coin = coin,
            isFavorited = isFavorited,
            selectedChangeOption = selectedChangeOption,
            listIndex = index,
            listCount = list.size,
            navigateToCryptoDetail = {
                currencyPressed.invoke(coin)
            },
            toggleFavorite = {
                toggleFavorite.invoke(coin)
            },
        )
    }
}