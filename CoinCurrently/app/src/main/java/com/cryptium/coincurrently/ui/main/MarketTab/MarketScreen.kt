package com.cryptium.coincurrently.ui.main.MarketTab

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.cryptium.coincurrently.ui.main.Compose.*
import com.cryptium.coincurrently.ui.main.Extensions.getActivity
import com.cryptium.coincurrently.ui.main.General.NavigationViewModel
import com.cryptium.coincurrently.ui.main.analytics.AnalyticsManager
import com.cryptium.coincurrently.ui.main.analytics.ScreenLabel
import com.cryptium.coincurrently.ui.main.analytics.TabBarDoubleTapped
import com.cryptium.coincurrently.ui.main.MarketTab.Categories.Views.CategoriesFragment
import com.cryptium.coincurrently.ui.main.MarketTab.CryptoDetail.Components.General.CryptoDetailPageSelector
import com.cryptium.coincurrently.ui.main.MarketTab.Derivatives.Views.DerivativesListFragment
import com.cryptium.coincurrently.ui.main.MarketTab.Exchanges.Views.ExchangeListFragment
import com.cryptium.coincurrently.ui.main.MarketTab.NFT.Views.NftToplistFragment
import com.cryptium.coincurrently.ui.main.MarketTab.Toplist.Views.ToplistFragment
import com.cryptium.coincurrently.ui.main.MarketTab.Treasuries.Views.TreasuriesListFragment
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.launch

private val marketTabs = listOf(
    "Coins",
    "NFTs",
    "Treasuries",
    "Sectors",
    "Exchanges",
    "Derivatives",
)

private const val treasuriesPageIndex = 2
private const val sectorsPageIndex = 3
private const val exchangesPageIndex = 4
private const val derivativesPageIndex = 5

@SuppressLint("UnrememberedMutableState")
@OptIn(ExperimentalPagerApi::class)
@Composable
fun MarketScreen(
    navigationViewModel: NavigationViewModel,
    modifier: Modifier
) {
    val context = LocalContext.current

    val tabs = marketTabs
    val savedMarketPage = navigationViewModel.marketSelectedPage.intValue.coerceIn(0, tabs.lastIndex)
    val pagerState = rememberPagerState(initialPage = savedMarketPage)
    val toplistScrollState = rememberLazyListState()
    val nftsScrollState = rememberLazyListState()
    val treasuriesScrollState = rememberLazyListState()
    val categoriesScrollState = rememberLazyListState()
    val exchangesScrollState = rememberLazyListState()
    val derivativesScrollState = rememberLazyListState()

    var doubleBackToExitPressedOnce by remember { mutableStateOf(false) }
    var selectedTabIndex by remember { mutableIntStateOf(savedMarketPage) }
    val coroutineScope = rememberCoroutineScope()
    val talkBackEnabled = AccessibilityLayout.isTalkBackEnabled
    val currentPage = if (talkBackEnabled) selectedTabIndex else pagerState.currentPage

    LaunchedEffect(pagerState.currentPage) {
        if (!talkBackEnabled) {
            selectedTabIndex = pagerState.currentPage
            navigationViewModel.marketSelectedPage.intValue = pagerState.currentPage
        }
    }

    val marketSubTabRequestId = navigationViewModel.marketSubTabRequestId.intValue
    LaunchedEffect(marketSubTabRequestId) {
        if (marketSubTabRequestId > 0) {
            val page = navigationViewModel.marketSelectedPage.intValue
            if (talkBackEnabled) {
                selectedTabIndex = page
            } else {
                pagerState.scrollToPage(page)
            }
            navigationViewModel.marketSubTabRequestId.intValue = 0
        }
    }

    Column(
        modifier = modifier
    ) {
        CappedFontScale(DynamicTypeSizeCap.Accessibility2) {
        Box {
            Column (
                modifier = Modifier
                    .padding(top = 16.dp)
            ){
                CryptoDetailPageSelector(
                    tabs = tabs,
                    pagerState = pagerState,
                    modifier = Modifier,
                    selectedPageIndex = currentPage,
                    onPageSelected = if (talkBackEnabled) {
                        { page ->
                            selectedTabIndex = page
                            navigationViewModel.marketSelectedPage.intValue = page
                        }
                    } else {
                        null
                    },
                )

                Separator()

                BackHandler(onBack = {
                    val scrollState = marketScrollState(
                        currentPage = currentPage,
                        toplistScrollState = toplistScrollState,
                        nftsScrollState = nftsScrollState,
                        treasuriesScrollState = treasuriesScrollState,
                        categoriesScrollState = categoriesScrollState,
                        exchangesScrollState = exchangesScrollState,
                        derivativesScrollState = derivativesScrollState,
                    )

                    val offset = scrollState.firstVisibleItemIndex + scrollState.firstVisibleItemScrollOffset

                    if (offset > 0) {
                        val doubleTapScreen = when (currentPage) {
                            0 -> ScreenLabel.Toplist
                            1 -> ScreenLabel.Nfts
                            treasuriesPageIndex -> ScreenLabel.Treasuries
                            sectorsPageIndex -> ScreenLabel.Sectors
                            exchangesPageIndex -> ScreenLabel.Exchanges
                            else -> ScreenLabel.Derivatives
                        }
                        AnalyticsManager.log(TabBarDoubleTapped(doubleTapScreen))
                        coroutineScope.launch {
                            scrollState.animateScrollToItem(0)
                        }
                    } else {
                        if (!doubleBackToExitPressedOnce) {
                            Toast.makeText(
                                context,
                                "Press back again to exit the app",
                                Toast.LENGTH_SHORT
                            ).show()
                            doubleBackToExitPressedOnce = true
                            Handler(Looper.getMainLooper()).postDelayed({
                                doubleBackToExitPressedOnce = false
                            }, 2500L)
                        } else {
                            context.getActivity()?.finishAffinity()
                        }
                    }
                })

                if (talkBackEnabled) {
                    MarketTabContent(
                        tabIndex = selectedTabIndex,
                        currentPage = currentPage,
                        toplistScrollState = toplistScrollState,
                        nftsScrollState = nftsScrollState,
                        treasuriesScrollState = treasuriesScrollState,
                        categoriesScrollState = categoriesScrollState,
                        exchangesScrollState = exchangesScrollState,
                        derivativesScrollState = derivativesScrollState,
                        navigationViewModel = navigationViewModel,
                    )
                } else {
                    HorizontalPager(
                        count = tabs.size,
                        state = pagerState,
                        verticalAlignment = Alignment.Top
                    ) { tabIndex ->
                        MarketTabContent(
                            tabIndex = tabIndex,
                            currentPage = currentPage,
                            toplistScrollState = toplistScrollState,
                            nftsScrollState = nftsScrollState,
                            treasuriesScrollState = treasuriesScrollState,
                            categoriesScrollState = categoriesScrollState,
                            exchangesScrollState = exchangesScrollState,
                            derivativesScrollState = derivativesScrollState,
                            navigationViewModel = navigationViewModel,
                        )
                    }
                }
            }
        }
        }
    }
}

@Composable
private fun MarketTabContent(
    tabIndex: Int,
    currentPage: Int,
    toplistScrollState: LazyListState,
    nftsScrollState: LazyListState,
    treasuriesScrollState: LazyListState,
    categoriesScrollState: LazyListState,
    exchangesScrollState: LazyListState,
    derivativesScrollState: LazyListState,
    navigationViewModel: NavigationViewModel,
) {
    when (tabIndex) {
        0 -> {
            ToplistFragment(
                currentPage = currentPage,
                category = null,
                scrollState = toplistScrollState,
                navigationViewModel = navigationViewModel
            )
        }

        1 -> {
            NftToplistFragment(
                currentPage = currentPage,
                scrollState = nftsScrollState,
                navigationViewModel = navigationViewModel
            )
        }

        treasuriesPageIndex -> {
            TreasuriesListFragment(
                currentPage = currentPage,
                scrollState = treasuriesScrollState,
                navigationViewModel = navigationViewModel,
            )
        }

        sectorsPageIndex -> {
            CategoriesFragment(
                currentPage = currentPage,
                scrollState = categoriesScrollState,
                navigationViewModel = navigationViewModel
            )
        }

        exchangesPageIndex -> {
            ExchangeListFragment(
                currentPage = currentPage,
                scrollState = exchangesScrollState,
                navigationViewModel = navigationViewModel
            )
        }

        derivativesPageIndex -> {
            DerivativesListFragment(
                currentPage = currentPage,
                scrollState = derivativesScrollState,
                navigationViewModel = navigationViewModel
            )
        }
    }
}

private fun marketScrollState(
    currentPage: Int,
    toplistScrollState: LazyListState,
    nftsScrollState: LazyListState,
    treasuriesScrollState: LazyListState,
    categoriesScrollState: LazyListState,
    exchangesScrollState: LazyListState,
    derivativesScrollState: LazyListState,
): LazyListState {
    return when (currentPage) {
        0 -> toplistScrollState
        1 -> nftsScrollState
        treasuriesPageIndex -> treasuriesScrollState
        sectorsPageIndex -> categoriesScrollState
        exchangesPageIndex -> exchangesScrollState
        else -> derivativesScrollState
    }
}
