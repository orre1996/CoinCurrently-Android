package com.cryptium.coincurrently.features.News
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.compose.ui.Alignment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cryptium.coincurrently.sharedui.theme.*
import com.cryptium.coincurrently.sharedui.modifiers.*
import com.cryptium.coincurrently.sharedui.components.Separator
import com.cryptium.coincurrently.core.extensions.getActivity
import com.cryptium.coincurrently.app.NavigationViewModel
import com.cryptium.coincurrently.sharedui.components.OnLifecycleEvent
import com.cryptium.coincurrently.core.analytics.BuyProAction
import com.cryptium.coincurrently.core.analytics.ScreenLabel
import com.cryptium.coincurrently.sharedui.sheets.WebView
import com.cryptium.coincurrently.features.News.NewsViewModel
import com.cryptium.coincurrently.features.Home.Carousel.ToplistFilterCarouselView
import com.cryptium.coincurrently.features.Home.Carousel.CarouselFilter
import com.cryptium.coincurrently.features.CoinDetail.Views.General.CryptoDetailPageSelector
import com.cryptium.coincurrently.features.CoinDetail.Sheets.SelectCoinSheet
import com.cryptium.coincurrently.core.purchases.StoreKitManager
import com.cryptium.coincurrently.core.analytics.BuyProOpened
import com.cryptium.coincurrently.core.analytics.AnalyticsManager
import com.cryptium.coincurrently.core.analytics.NewsFilterSelected
import com.cryptium.coincurrently.core.analytics.NewsTabSelected
import com.cryptium.coincurrently.core.analytics.WebViewOpened
import com.cryptium.coincurrently.core.analytics.SheetLabel
import com.cryptium.coincurrently.core.analytics.SheetOpened
import com.cryptium.coincurrently.core.analytics.TabBarDoubleTapped
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.cryptium.coincurrently.core.networking.NewsCategory
import com.cryptium.coincurrently.core.networking.ResponseModels.NewNewsItem
import kotlin.time.Duration.Companion.seconds
import com.cryptium.coincurrently.app.SearchCase
import com.cryptium.coincurrently.app.SearchScreenCase

@OptIn(ExperimentalPagerApi::class)
@Composable
fun NewsScreen(
    navigationViewModel: NavigationViewModel,
    modifier: Modifier
) {
    val viewModel: NewsViewModel = viewModel()
    val context = LocalContext.current

    val coroutineScope = rememberCoroutineScope()
    var doubleBackToExitPressedOnce by remember { mutableStateOf(false) }
    val tabs = listOf("Articles", "Videos", "Podcasts")
    val pagerState = rememberPagerState()

    val newsScrollState = rememberLazyListState()
    val videosScrollState = rememberLazyListState()
    val podcastsScrollState = rememberLazyListState()
    val filterListState = rememberLazyListState()

    val isMenuShowing = remember { mutableStateOf(false) }
    var isCoinsMenuShowing by remember { mutableStateOf(false) }
    var selectedUrl: String? by remember { mutableStateOf(null) }

    val isArticlesRefreshing = remember { mutableStateOf(false) }
    val isVideosRefreshing = remember { mutableStateOf(false) }
    val isPodcastsRefreshing = remember { mutableStateOf(false) }

    val currentSubscription by StoreKitManager.currentSubscription.observeAsState()

    LaunchedEffect(Unit) {
        if (!viewModel.hasLoadedAllCoinsOnce) {
            viewModel.getAllCoins()
        }
    }

    OnLifecycleEvent { _, event ->
        when (event) {
            Lifecycle.Event.ON_RESUME -> {
                coroutineScope.launch {
                    if (!viewModel.hasLoadedAllCoinsOnce) {
                        viewModel.getAllCoins()
                    }
                }
            }
            else -> { }
        }
    }

    LaunchedEffect(pagerState.currentPage) {
        val page = pagerState.currentPage
        if (page in tabs.indices) {
            AnalyticsManager.log(
                NewsTabSelected(
                    fromScreen = ScreenLabel.News,
                    tab = tabs[page],
                ),
            )
        }
    }

    val onNewsUrlPressed: (String) -> Unit = { url ->
        com.cryptium.coincurrently.features.Achievements.Managers.AchievementManager.increment(
            com.cryptium.coincurrently.features.Achievements.Models.AchievementCounterKey.newsArticlesOpened
        )
        AnalyticsManager.log(
            WebViewOpened(fromScreen = ScreenLabel.News, url = url),
        )
        selectedUrl = url
    }

    Box {
        Column(
            modifier = modifier
                .padding(top =  16.dp)
        ) {
            Column {
                CryptoDetailPageSelector(
                    tabs = tabs,
                    pagerState = pagerState,
                    modifier = Modifier
                )

                Separator()

                BackHandler(onBack = {
                    val scrollState = when (pagerState.currentPage) {
                        0 -> { newsScrollState }
                        1 -> { videosScrollState }
                        else -> { podcastsScrollState }
                    }

                    val offset = scrollState.firstVisibleItemIndex + scrollState.firstVisibleItemScrollOffset

                    if (isMenuShowing.value) {
                        isMenuShowing.value = false
                    } else if (isCoinsMenuShowing) {
                        isCoinsMenuShowing = false
                    } else if (offset > 0) {
                        AnalyticsManager.log(TabBarDoubleTapped(ScreenLabel.News))
                        coroutineScope.launch {
                            scrollState.animateScrollToItem(0)
                        }
                    } else {
                        if (!doubleBackToExitPressedOnce) {
                            Toast.makeText(context, "Press back again to exit the app", Toast.LENGTH_SHORT).show()
                            doubleBackToExitPressedOnce = true
                            Handler(Looper.getMainLooper()).postDelayed({
                                doubleBackToExitPressedOnce = false
                            }, 2500L)
                        } else {
                            context.getActivity()?.finishAffinity()
                        }
                    }
                })

                HorizontalPager(
                    count = tabs.size,
                    state = pagerState,
                ) { tabIndex ->
                    when (tabIndex) {
                        0 -> {
                            NewsSection(
                                news = viewModel.news,
                                scrollState = newsScrollState,
                                filterListState = filterListState,
                                selectedFilter = viewModel.newsCategory.toCarouselFilter,
                                hasPro = currentSubscription != null,
                                isRefreshing = isArticlesRefreshing.value,
                                page = 0,
                                currentPage = pagerState.currentPage,
                                newsCardPressed = onNewsUrlPressed,
                                getNews = {
                                    coroutineScope.launch {
                                        viewModel.getNews(viewModel.newsCategory)
                                    }
                                },
                                refresh = {
                                    coroutineScope.launch {
                                        isArticlesRefreshing.value = true

                                        launch {
                                            viewModel.getNews(viewModel.newsCategory)
                                            isArticlesRefreshing.value = false
                                        }

                                        delay(10.seconds)
                                        isArticlesRefreshing.value = false
                                    }
                                },
                                newFilterSelected = { filter ->
                                    coroutineScope.launch {
                                        when {
                                            filter.requiresPro && currentSubscription == null -> {
                                                navigationViewModel.openBuyPro(ScreenLabel.News, BuyProAction.NewsFilter)
                                                AnalyticsManager.log(
                                                    BuyProOpened(
                                                        fromScreen = ScreenLabel.News,
                                                        action = BuyProAction.NewsFilter,
                                                    ),
                                                )
                                            }
                                            filter is CarouselFilter.Coins -> {
                                                AnalyticsManager.log(
                                                    SheetOpened(
                                                        fromScreen = ScreenLabel.News,
                                                        sheet = SheetLabel.CoinSearch,
                                                    ),
                                                )
                                                isCoinsMenuShowing = true
                                            }
                                            else -> {
                                                AnalyticsManager.log(
                                                    NewsFilterSelected(
                                                        fromScreen = ScreenLabel.News,
                                                        filter = filter,
                                                    ),
                                                )
                                                viewModel.getNews(filter.toNewsCategory)
                                            }
                                        }
                                    }
                                }
                            )
                        }

                        1 -> {
                            NewsSection(
                                news = viewModel.videoNews,
                                scrollState = videosScrollState,
                                filterListState = filterListState,
                                selectedFilter = viewModel.newsCategory.toCarouselFilter,
                                hasPro = currentSubscription != null,
                                isRefreshing = isVideosRefreshing.value,
                                page = 1,
                                currentPage = pagerState.currentPage,
                                newsCardPressed = onNewsUrlPressed,
                                getNews = {
                                    coroutineScope.launch {
                                        viewModel.getVideoNews()
                                    }
                                },
                                refresh = {
                                    coroutineScope.launch {
                                        isVideosRefreshing.value = true

                                        launch {
                                            viewModel.getVideoNews()
                                            isVideosRefreshing.value = false
                                        }

                                        delay(10.seconds)
                                        isVideosRefreshing.value = false
                                    }
                                },
                                newFilterSelected = { _ -> }
                            )
                        }

                        2 -> {
                            NewsSection(
                                news = viewModel.podcastNews,
                                scrollState = podcastsScrollState,
                                filterListState = filterListState,
                                selectedFilter = viewModel.newsCategory.toCarouselFilter,
                                hasPro = currentSubscription != null,
                                isRefreshing = isPodcastsRefreshing.value,
                                page = 2,
                                currentPage = pagerState.currentPage,
                                newsCardPressed = onNewsUrlPressed,
                                getNews = {
                                    coroutineScope.launch {
                                        viewModel.getPodcastNews()
                                    }
                                },
                                refresh = {
                                    coroutineScope.launch {
                                        isPodcastsRefreshing.value = true

                                        launch {
                                            viewModel.getPodcastNews()
                                            isPodcastsRefreshing.value = false
                                        }

                                        delay(10.seconds)
                                        isVideosRefreshing.value = false
                                    }
                                },
                                newFilterSelected = { _ -> }
                            )
                        }
                    }
                }
            }
        }

        if (isCoinsMenuShowing) {
            SelectCoinSheet(
                searchScreenCase = SearchScreenCase.Search(
                    list = SearchCase.OnlyCoins
                ),
                fromScreen = ScreenLabel.News,
                highlightId = viewModel.newsCategory.selectedId,
                hideId = null,
                selectedCoin = { coin ->
                    val symbol = coin?.symbol ?: return@SelectCoinSheet

                    coroutineScope.launch {
                        viewModel.getNews(NewsCategory.Coin(symbol.uppercase()))
                    }
                },
                close = {
                    isCoinsMenuShowing = false
                }
            )
        }

        selectedUrl?.let {
            WebView(it) {
                selectedUrl = null
            }
        }
    }
}

@Composable
private fun NewsSection(
    news: List<NewNewsItem>?,
    scrollState: LazyListState,
    filterListState: LazyListState,
    selectedFilter: CarouselFilter,
    hasPro: Boolean,
    isRefreshing: Boolean,
    page: Int,
    currentPage: Int,
    newsCardPressed: (String) -> Unit,
    getNews: () -> Unit,
    refresh: () -> Unit,
    newFilterSelected: (CarouselFilter) -> Unit
) {
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp

    LaunchedEffect(currentPage) {
        if (currentPage == page) {
            getNews.invoke()
        }
    }

    OnLifecycleEvent { _, event ->
        when (event) {
            Lifecycle.Event.ON_RESUME -> {
                if (currentPage == page) {
                    getNews.invoke()
                }
            }
            else -> { }
        }
    }

    SwipeRefresh(
        state = rememberSwipeRefreshState(isRefreshing = isRefreshing),
        onRefresh = {
            refresh.invoke()
        }
    ) {
        LazyColumn(
            state = scrollState,
            contentPadding = PaddingValues(
                top =  32.dp,
                bottom = 32.dp
            ),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(32.dp),
            modifier = Modifier
                .height(screenHeight)
        ) {
            if (page == 0) {
                item {
                    ToplistFilterCarouselView(
                        filters = CarouselFilter.newsFilters,
                        selectedFilter = selectedFilter,
                        scrollState = filterListState,
                        paddingValues = PaddingValues(0.dp),
                        hasPro = hasPro,
                        onFilterSelected = {
                            newFilterSelected.invoke(it)
                        }
                    )
                }
            }

            val totalCount = news?.size ?: 10
            val newsType = when (page) {
                0 -> NewsMediaType.Article
                1 -> NewsMediaType.Video
                else -> NewsMediaType.Podcast
            }

            items(totalCount) { index ->
                val newsItem = news?.get(index)

                Box(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                ) {
                    SmallNewsItem(
                        newsItem = newsItem,
                        index = index + 1,
                        totalCount = totalCount,
                        newsType = newsType,
                        callback = {
                            newsCardPressed(it)
                        }
                    )
                }
            }
        }
    }
}