package com.cryptium.coincurrently.features.Market.Toplist.Views
import com.cryptium.coincurrently.sharedui.modifiers.imageSize

import com.cryptium.coincurrently.app.NavigationScreens
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
import com.cryptium.coincurrently.sharedui.components.AccessibilityLayout
import com.cryptium.coincurrently.sharedui.buttons.ScrollToTopButton
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import com.cryptium.coincurrently.sharedui.buttons.SecondaryButton
import com.cryptium.coincurrently.sharedui.theme.onSurfaceDim
import com.cryptium.coincurrently.sharedui.theme.onSurfaceDimmer
import com.cryptium.coincurrently.sharedui.modifiers.shimmeringModifier
import com.cryptium.coincurrently.sharedui.theme.surfaceContainerHigh
import com.cryptium.coincurrently.core.extensions.changeColor
import com.cryptium.coincurrently.core.extensions.changePercentAccessible
import com.cryptium.coincurrently.core.extensions.changeWithSymbol
import com.cryptium.coincurrently.core.extensions.directionAccessible
import com.cryptium.coincurrently.core.extensions.formatCurrency
import com.cryptium.coincurrently.core.extensions.getChangeInValue
import com.cryptium.coincurrently.core.extensions.getDifferenceInPercent
import com.cryptium.coincurrently.core.extensions.isTablet
import com.cryptium.coincurrently.core.extensions.satoshiBold
import com.cryptium.coincurrently.sharedui.components.PriceChangeValueView
import com.cryptium.coincurrently.sharedui.models.FormattingThreshold
import com.cryptium.coincurrently.sharedui.models.PriceChange
import com.cryptium.coincurrently.sharedui.models.SortOption
import com.cryptium.coincurrently.sharedui.loading.ChangeSize
import com.cryptium.coincurrently.app.NavigationViewModel
import com.cryptium.coincurrently.sharedui.components.ObserveCurrencyChanges
import com.cryptium.coincurrently.sharedui.components.OnLifecycleEvent
import com.cryptium.coincurrently.features.GlobalMarket.Views.GlobalmarketSummaryView
import com.cryptium.coincurrently.core.analytics.AnalyticsManager
import com.cryptium.coincurrently.core.localization.Localization
import com.cryptium.coincurrently.core.analytics.Navigate
import com.cryptium.coincurrently.core.analytics.PriceChangeOptionSelected
import com.cryptium.coincurrently.core.analytics.ScreenLabel
import com.cryptium.coincurrently.core.analytics.ScrollToTopPressed
import com.cryptium.coincurrently.core.analytics.SheetLabel
import com.cryptium.coincurrently.core.analytics.SheetOpened
import com.cryptium.coincurrently.core.analytics.SortOptionSelected
import com.cryptium.coincurrently.features.Market.Toplist.ViewModels.ToplistViewModel
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.cryptium.coincurrently.features.Home.Carousel.ToplistCard
import com.cryptium.coincurrently.features.Home.Carousel.ToplistHeaderView
import com.cryptium.coincurrently.core.persistence.StorageKeys
import com.cryptium.coincurrently.core.persistence.StorageManager
import com.cryptium.coincurrently.features.CoinDetail.Sheets.SelectCoinSheet
import com.cryptium.coincurrently.features.Market.Toplist.Sheets.PriceChangeSheet
import com.cryptium.coincurrently.features.Market.Toplist.Sheets.ToplistSortBySheet
import com.cryptium.coincurrently.features.Market.Toplist.ViewModels.ToplistViewModelFactory
import com.cryptium.coincurrently.features.Portfolio.ViewModels.AddTransactionViewModelFactory
import com.cryptium.coincurrently.core.networking.ResponseModels.CryptoCategory
import com.cryptium.coincurrently.core.networking.ResponseModels.CurrencySelectionModel
import com.cryptium.coincurrently.core.networking.ResponseModels.TimeCharts
import com.cryptium.coincurrently.core.networking.ResponseModels.Toplist
import com.cryptium.coincurrently.core.networking.ResponseModels.toCryptoDetail
import kotlin.time.Duration.Companion.seconds
import com.cryptium.coincurrently.app.SearchCase
import com.cryptium.coincurrently.app.SearchScreenCase

@Composable
fun ToplistScreen(
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
                                context = context,
                            )
                        },
                        currencyPressed = { coin ->
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
            contentDescription = Localization.string("Search for coins"),
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
            text = Localization.string(selectedSortOption.title),
            accessibilityLabel = Localization.format(
                "Sort, currently selected: %@",
                Localization.string(selectedSortOption.accessibilityTitle),
            ),
            icon = R.drawable.sort,
            onClick = {
                AnalyticsManager.log(
                    SheetOpened(fromScreen = ScreenLabel.Toplist, sheet = SheetLabel.SortBy),
                )
                isSortShowing.value = true
            }
        )

        SecondaryButton(
            text = Localization.string(selectedChangeOption.rawValue),
            accessibilityLabel = Localization.format(
                "Timeframe, currently selected: %@",
                Localization.string(selectedChangeOption.accessibilityTimeframe),
            ),
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