package com.cryptium.coincurrently.features.Market.NFT.Views
import com.cryptium.coincurrently.sharedui.modifiers.imageSize

import com.cryptium.coincurrently.app.NavigationScreens
import com.cryptium.coincurrently.app.SearchCase
import com.cryptium.coincurrently.app.SearchScreenCase
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds
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
import com.cryptium.coincurrently.features.Market.NFT.ViewModels.NftToplistViewModel
import com.cryptium.coincurrently.features.Market.NFT.Sheets.NFTsPlatformsSheet
import com.cryptium.coincurrently.features.Market.Toplist.Sheets.ToplistSortBySheet
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.cryptium.coincurrently.core.networking.ResponseModels.NFTPlatform
import com.cryptium.coincurrently.core.networking.ResponseModels.NFTToplist
import com.cryptium.coincurrently.core.networking.ResponseModels.toNftToplist

@Composable
fun NftToplistScreen(
    currentPage: Int,
    scrollState: LazyListState,
    navigationViewModel: NavigationViewModel,
) {
    val viewModel: NftToplistViewModel = viewModel()

    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val isTablet = LocalContext.current.isTablet()
    val uiScope = rememberCoroutineScope()

    var isRefreshing by remember { mutableStateOf(false) }

    val isSearchShowing = remember { mutableStateOf(false) }
    val isSortShowing = remember { mutableStateOf(false) }
    val isPlatformsShowing = remember { mutableStateOf(false) }

    val firstVisibleIndex by remember {
        derivedStateOf { scrollState.firstVisibleItemIndex }
    }
    val talkBackEnabled = AccessibilityLayout.isTalkBackEnabled

    LaunchedEffect(currentPage) {
        if (currentPage == 1) {
            launch {
                viewModel.getAllCoins()
            }
            launch {
                viewModel.getBitcoin()
            }
            launch {
                viewModel.getNFTPlatforms()
            }
            launch {
                viewModel.getNftToplist(
                    newPlatform = viewModel.selectedPlatform,
                    newSortOption = viewModel.selectedSortOption
                )
            }
        }
    }

    ObserveCurrencyChanges(navigationViewModel) {
        coroutineScope {
            launch { viewModel.getBitcoin() }
            launch {
                viewModel.getNftToplist(
                    newPlatform = viewModel.selectedPlatform,
                    newSortOption = viewModel.selectedSortOption,
                )
            }
        }
    }

    OnLifecycleEvent { _, event ->
        when (event) {
            Lifecycle.Event.ON_RESUME -> {
                if (currentPage == 1) {
                    uiScope.launch {
                        kotlinx.coroutines.coroutineScope {
                            launch { viewModel.getAllCoins() }
                            launch { viewModel.getBitcoin() }
                            launch { viewModel.getNFTPlatforms() }
                            launch {
                                viewModel.getNftToplist(
                                    newPlatform = viewModel.selectedPlatform,
                                    newSortOption = viewModel.selectedSortOption
                                )
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
                uiScope.launch {
                    try {
                        coroutineScope {
                            launch { viewModel.getAllCoins() }
                            launch { viewModel.getBitcoin() }
                            launch {
                                viewModel.getNftToplist(
                                    newPlatform = viewModel.selectedPlatform,
                                    newSortOption = viewModel.selectedSortOption
                                )
                            }
                        }
                        delay(10.seconds)
                    } finally {
                        isRefreshing = false
                    }
                }
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
                        selectedPlatform = viewModel.selectedPlatform,
                        hasPlatforms = viewModel.nftPlatforms != null,
                        isSearchShowing = isSearchShowing,
                        isSortShowing = isSortShowing,
                        isPlatformsShowing = isPlatformsShowing
                    )
                }

                item {
                    NFTHeaderView(
                        modifier = Modifier
                            .padding(bottom = 6.dp)
                    )
                }

                if (viewModel.nftToplist.isEmpty()) {
                    TempList(isTablet = isTablet)
                } else {
                    RealList(
                        viewModel.nftToplist,
                        viewModel,
                        nftPressed = {
                            val dest = NavigationScreens.NFTDetail(
                                it
                            )
                            AnalyticsManager.log(
                                Navigate(
                                    fromScreen = ScreenLabel.Nfts,
                                    toScreen = ScreenLabel.NftDetail,
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
                    list = SearchCase.NFTs
                ),
                fromScreen = ScreenLabel.Nfts,
                highlightId = null,
                hideId = null,
                isSearch = true,
                selectedCoin = { coin ->
                    val coin = coin ?: return@SelectCoinSheet

                    val dest = NavigationScreens.NFTDetail(
                        coin.toNftToplist()
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
                options = SortOption.nftOptions,
                selectedOption = viewModel.selectedSortOption,
                newOptionSelected = { option ->
                    AnalyticsManager.log(
                        SortOptionSelected(fromScreen = ScreenLabel.Nfts, option = option),
                    )
                    uiScope.launch {
                        viewModel.getNftToplist(
                            newPlatform = viewModel.selectedPlatform,
                            newSortOption = option
                        )
                    }
                },
                close = {
                    isSortShowing.value = false
                }
            )
        }

        if (isPlatformsShowing.value) {
            NFTsPlatformsSheet(
                allPlatforms = viewModel.nftPlatforms ?: listOf(),
                selectedPlatform = viewModel.selectedPlatform,
                newOptionSelected = { option ->
                    uiScope.launch {
                        viewModel.getNftToplist(
                            newPlatform = option,
                            newSortOption = viewModel.selectedSortOption
                        )
                    }
                },
                close = {
                    isPlatformsShowing.value = false
                }
            )
        }

        ScrollToTopButton(
            visible = !talkBackEnabled && firstVisibleIndex >= 15,
            onClick = {
                AnalyticsManager.log(ScrollToTopPressed(fromScreen = ScreenLabel.Nfts))
                uiScope.launch {
                    scrollState.animateScrollToItem(0)
                }
            }
        )
    }
}


@Composable
private fun FilterButtons(
    selectedSortOption: SortOption,
    selectedPlatform: NFTPlatform,
    hasPlatforms: Boolean,
    isSearchShowing: MutableState<Boolean>,
    isSortShowing: MutableState<Boolean>,
    isPlatformsShowing: MutableState<Boolean>
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
            contentDescription = Localization.string("Search for NFTs"),
            colorFilter = ColorFilter.tint(MaterialTheme.colors.onSurfaceDimmer),
            modifier = Modifier
                .clip(CircleShape)
                .clickable {
                    AnalyticsManager.log(
                        SheetOpened(fromScreen = ScreenLabel.Nfts, sheet = SheetLabel.CoinSearch),
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
                    SheetOpened(fromScreen = ScreenLabel.Nfts, sheet = SheetLabel.SortBy),
                )
                isSortShowing.value = true
            }
        )


        SecondaryButton(
            text = if (selectedPlatform.id == "all") {
                Localization.string("Chain")
            } else {
                selectedPlatform.name ?: Localization.string("All Chains")
            },
            accessibilityLabel = Localization.format(
                "Chain, currently selected: %@",
                if (selectedPlatform.id == "all") {
                    Localization.string("All Chains")
                } else {
                    selectedPlatform.accessibilityTitle
                },
            ),
            icon = R.drawable.chevron_down,
            isEnabled = hasPlatforms,
            onClick = {
                AnalyticsManager.log(
                    SheetOpened(fromScreen = ScreenLabel.Nfts, sheet = SheetLabel.Platform),
                )
                isPlatformsShowing.value = true
            }
        )
    }
}

private fun LazyListScope.TempList(isTablet: Boolean) {
    val count = if (isTablet) 40 else 10
    items(count) { index ->
        NftToplistCell(
            nft = null,
            chainSymbol = null,
            priceMultiplier = null,
            listIndex = index,
            listCount = count,
            nftPressed = { })
    }
}

private fun LazyListScope.RealList(
    toplist: List<NFTToplist>,
    viewModel: NftToplistViewModel,
    nftPressed: (NFTToplist) -> Unit
) {
    items(toplist) {
        val index = toplist.indexOf(it)
        NftToplistCell(
            nft = it,
            chainSymbol = viewModel.getChainSymbol(it.native_currency),
            priceMultiplier = viewModel.multiplier,
            listIndex = index,
            listCount = toplist.size,
            nftPressed = {
                nftPressed.invoke(it)
            }
        )
    }
}
