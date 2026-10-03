package com.cryptium.coincurrently.features.Market.Categories.Views
import com.cryptium.coincurrently.sharedui.modifiers.imageSize

import com.cryptium.coincurrently.app.NavigationScreens
import android.os.Handler
import android.os.Looper
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
import com.cryptium.coincurrently.sharedui.theme.*
import com.cryptium.coincurrently.sharedui.modifiers.*
import com.cryptium.coincurrently.sharedui.components.AccessibilityLayout
import com.cryptium.coincurrently.sharedui.buttons.ScrollToTopButton
import com.cryptium.coincurrently.sharedui.buttons.SecondaryButton
import com.cryptium.coincurrently.app.NavigationViewModel
import com.cryptium.coincurrently.sharedui.components.ObserveCurrencyChanges
import com.cryptium.coincurrently.sharedui.components.OnLifecycleEvent
import com.cryptium.coincurrently.core.analytics.AnalyticsManager
import com.cryptium.coincurrently.core.localization.Localization
import com.cryptium.coincurrently.core.analytics.Navigate
import com.cryptium.coincurrently.core.analytics.ScreenLabel
import com.cryptium.coincurrently.core.analytics.ScrollToTopPressed
import com.cryptium.coincurrently.core.analytics.SheetLabel
import com.cryptium.coincurrently.core.analytics.SheetOpened
import com.cryptium.coincurrently.core.analytics.SortOptionSelected
import com.cryptium.coincurrently.sharedui.models.SortOption
import com.cryptium.coincurrently.features.Market.Categories.ViewModels.CategoriesViewModel
import com.cryptium.coincurrently.features.CoinDetail.Sheets.SelectCoinSheet
import com.cryptium.coincurrently.core.extensions.isTablet
import com.cryptium.coincurrently.features.Market.Toplist.Sheets.ToplistSortBySheet
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.cryptium.coincurrently.core.networking.ResponseModels.CryptoCategory
import com.cryptium.coincurrently.core.networking.ResponseModels.toCryptoDetail
import kotlin.time.Duration.Companion.seconds
import com.cryptium.coincurrently.app.SearchCase
import com.cryptium.coincurrently.app.SearchScreenCase

@Composable
fun CategoriesScreen(
    currentPage: Int,
    scrollState: LazyListState,
    navigationViewModel: NavigationViewModel
) {
    val viewModel: CategoriesViewModel = viewModel()
    val configuration = LocalConfiguration.current
    val isTablet = LocalContext.current.isTablet()
    val screenHeight = configuration.screenHeightDp.dp

    val coroutineScope = rememberCoroutineScope()
    var isRefreshing by remember { mutableStateOf(false) }

    val isSearchShowing = remember { mutableStateOf(false) }
    val isSortShowing = remember { mutableStateOf(false) }

    val firstVisibleIndex by remember {
        derivedStateOf { scrollState.firstVisibleItemIndex }
    }
    val talkBackEnabled = AccessibilityLayout.isTalkBackEnabled

    LaunchedEffect(currentPage) {
        if (currentPage == 3) {
            launch {
                viewModel.getCategories(
                    newSortOption = viewModel.selectedSortOption
                )
            }
            launch {
                viewModel.getBitcoin()
            }
        }
    }

    ObserveCurrencyChanges(navigationViewModel) {
        coroutineScope {
            launch {
                viewModel.getCategories(newSortOption = viewModel.selectedSortOption)
            }
            launch {
                viewModel.getBitcoin()
            }
        }
    }

    OnLifecycleEvent { _, event ->
        when (event) {
            Lifecycle.Event.ON_RESUME -> {
                if (currentPage == 3) {
                    coroutineScope.launch {
                        coroutineScope {
                            launch {
                                viewModel.getCategories(
                                    newSortOption = viewModel.selectedSortOption
                                )
                            }
                            launch {
                                viewModel.getBitcoin()
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
                        viewModel.getCategories(
                            newSortOption = viewModel.selectedSortOption,
                        )
                        isRefreshing = false
                    }

                    launch {
                       viewModel.getBitcoin()
                    }

                    delay(10.seconds)
                    isRefreshing = false
                }

                isRefreshing = true
            }
        ) {
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
                        isSortShowing = isSortShowing
                    )
                }

                item {
                    CategoriesHeaderView(
                        modifier = Modifier
                            .padding(bottom = 6.dp)
                    )
                }

                if (viewModel.categories.isEmpty()) {
                    TempList(isTablet)
                } else {
                    RealList(
                        viewModel.categories,
                        viewModel.multiplier,
                        openCategoryDetail = {
                            val dest = NavigationScreens.CategoryDetail(
                                it
                            )
                            AnalyticsManager.log(
                                Navigate(
                                    fromScreen = ScreenLabel.Sectors,
                                    toScreen = ScreenLabel.CategoryDetail,
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
                fromScreen = ScreenLabel.Sectors,
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
                options = SortOption.categoriesOptions,
                selectedOption = viewModel.selectedSortOption,
                newOptionSelected = { option ->
                    AnalyticsManager.log(
                        SortOptionSelected(fromScreen = ScreenLabel.Sectors, option = option),
                    )
                    coroutineScope.launch {
                        viewModel.getCategories(
                            newSortOption = option
                        )
                    }
                },
                close = {
                    isSortShowing.value = false
                }
            )
        }

        ScrollToTopButton(
            visible = !talkBackEnabled && firstVisibleIndex >= 15,
            onClick = {
                AnalyticsManager.log(ScrollToTopPressed(fromScreen = ScreenLabel.Sectors))
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
    isSortShowing: MutableState<Boolean>
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
            contentDescription = Localization.string("Search for sectors"),
            colorFilter = ColorFilter.tint(MaterialTheme.colors.onSurfaceDimmer),
            modifier = Modifier
                .clip(CircleShape)
                .clickable {
                    AnalyticsManager.log(
                        SheetOpened(fromScreen = ScreenLabel.Sectors, sheet = SheetLabel.CoinSearch),
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
                    SheetOpened(fromScreen = ScreenLabel.Sectors, sheet = SheetLabel.SortBy),
                )
                isSortShowing.value = true
            }
        )
    }
}

private fun LazyListScope.TempList(isTablet: Boolean) {
    val count = if (isTablet) 30 else 15
    items(count) { index ->
        CategoryListItem(
            category = null,
            multiplier = null,
            listIndex = index,
            listCount = count,
            openCategoryDetail = { }
        )
    }
}

private fun LazyListScope.RealList(
    categories: List<CryptoCategory>,
    multiplier: Double?,
    openCategoryDetail: (CryptoCategory) -> Unit
) {

    items(categories) {
        val index = categories.indexOf(it)
        CategoryListItem(
            category = it,
            multiplier = multiplier,
            listIndex = index,
            listCount = categories.size,
            openCategoryDetail = {
                openCategoryDetail.invoke(it)
            }
        )
    }
}