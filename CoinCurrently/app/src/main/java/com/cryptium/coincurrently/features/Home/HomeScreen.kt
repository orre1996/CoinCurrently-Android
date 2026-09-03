package com.cryptium.coincurrently.features.Home
import android.util.Log
import android.app.Application
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment.Companion.Bottom
import androidx.compose.ui.Alignment.Companion.Top
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.activity.compose.BackHandler
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cryptium.coincurrently.app.NavigationScreens
import com.cryptium.coincurrently.sharedui.components.AccessibilityLayout
import com.cryptium.coincurrently.sharedui.buttons.PrimaryButton
import com.cryptium.coincurrently.sharedui.modifiers.CappedFontScale
import com.cryptium.coincurrently.sharedui.modifiers.DynamicTypeSizeCap
import com.cryptium.coincurrently.sharedui.components.conditionalStickyHeader
import com.cryptium.coincurrently.core.extensions.dpToPx
import com.cryptium.coincurrently.core.extensions.getActivity
import com.cryptium.coincurrently.sharedui.components.ObserveCurrencyChanges
import com.cryptium.coincurrently.sharedui.components.OnLifecycleEvent
import com.cryptium.coincurrently.app.NavigationViewModel
import com.cryptium.coincurrently.app.title
import com.cryptium.coincurrently.sharedui.models.PriceChange
import com.cryptium.coincurrently.features.Home.Views.PremiumView
import com.cryptium.coincurrently.features.Home.Carousel.ToplistCategoryView
import com.cryptium.coincurrently.core.models.CarouselFilter
import com.cryptium.coincurrently.features.Home.Views.MarketStatsView
import com.cryptium.coincurrently.features.Home.Carousel.ToplistFilterCarouselView
import com.cryptium.coincurrently.features.Home.Carousel.ToplistHeaderView
import com.cryptium.coincurrently.features.Home.HomeViewModel
import com.cryptium.coincurrently.features.Home.HomeViewModelFactory
import com.cryptium.coincurrently.features.Home.HomeViewModelImp
import com.cryptium.coincurrently.features.PriceAlerts.PriceAlertsRepository
import com.cryptium.coincurrently.core.analytics.BuyProAction
import com.cryptium.coincurrently.core.analytics.AnalyticsManager
import com.cryptium.coincurrently.core.analytics.HomeFilterSelected
import com.cryptium.coincurrently.core.analytics.Navigate
import com.cryptium.coincurrently.core.analytics.ScreenLabel
import com.cryptium.coincurrently.core.analytics.TabBarDoubleTapped
import com.cryptium.coincurrently.core.analytics.ViewAll
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.cryptium.coincurrently.core.networking.ResponseModels.PriceVolumeModel
import com.cryptium.coincurrently.core.networking.ResponseModels.Toplist
import com.cryptium.coincurrently.core.networking.ResponseModels.toCryptoDetail
import kotlin.math.absoluteValue
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    navigationViewModel: NavigationViewModel,
    modifier: Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val viewModel = viewModel<HomeViewModelImp>(
        factory = HomeViewModelFactory(context.applicationContext as Application)
    )

    var doubleBackToExitPressedOnce by remember { mutableStateOf(false) }
    var isRefreshing by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val homeLazyListState = rememberLazyListState(initialFirstVisibleItemIndex = 0)

    val initialHomeFilterIndex = remember {
        CarouselFilter.homeFilters.indexOf(viewModel.selectedFilter).coerceAtLeast(0)
    }
    val filterListState = rememberLazyListState(initialFirstVisibleItemIndex = initialHomeFilterIndex)
    val filterOffset = -64.dp.dpToPx().toInt()
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = initialHomeFilterIndex)
    val flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)
    val useStickyHeaders = AccessibilityLayout.useStickyHeaders
    val talkBackEnabled = AccessibilityLayout.isTalkBackEnabled
    val selectedPageIndex by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val viewportCenter = layoutInfo.viewportSize.width / 2
            layoutInfo.visibleItemsInfo
                .minByOrNull { (it.offset + it.size / 2 - viewportCenter).absoluteValue }
                ?.index ?: listState.firstVisibleItemIndex
        }
    }

    SideEffect {
        Log.i("GlobalMarket", "HomeScreen composed (SideEffect)")
    }

    LaunchedEffect(Unit) {
        viewModel.initObservers(lifecycleOwner)
        viewModel.loadPremiumOfferProducts(context)

        launch {
            Log.i("GlobalMarket", "HomeScreen LaunchedEffect requesting getGlobalMarket")
            viewModel.getGlobalMarket()
            Log.i("GlobalMarket", "HomeScreen after getGlobalMarket uiStateNull=${viewModel.globalMarket == null}")
        }

        launch {
            viewModel.getFearGreed()
        }

        launch {
            viewModel.getGasFees()
        }

        launch {
            viewModel.downloadData()
        }
    }

    var hasScrolledToDefaultHomeFilter by remember { mutableStateOf(false) }
    LaunchedEffect(viewModel.hasResolvedDefaultHomeFilter) {
        if (!viewModel.hasResolvedDefaultHomeFilter) return@LaunchedEffect
        val index = CarouselFilter.homeFilters.indexOf(viewModel.selectedFilter).coerceAtLeast(0)
        if (index > 0) {
            listState.scrollToItem(index)
            filterListState.scrollToItem(index, filterOffset)
        }
        hasScrolledToDefaultHomeFilter = true
    }

    ObserveCurrencyChanges(navigationViewModel) {
        viewModel.refreshAllHomeData()
    }

    OnLifecycleEvent { _, event ->
        when (event) {
            Lifecycle.Event.ON_RESUME -> {
                coroutineScope.launch {
                    launch {
                        viewModel.getGlobalMarket()
                    }
                    launch {
                        viewModel.getFearGreed()
                    }
                    launch {
                        viewModel.getGasFees()
                    }
                    launch {
                        viewModel.downloadData()
                    }
                }
            }

            else -> {}
        }
    }

    LaunchedEffect(selectedPageIndex) {
        if (talkBackEnabled) return@LaunchedEffect
        if (!hasScrolledToDefaultHomeFilter) return@LaunchedEffect

        val filter = CarouselFilter.homeFilters.get(selectedPageIndex)
        if (filter == viewModel.selectedFilter) return@LaunchedEffect

        viewModel.selectedFilter = filter
        AnalyticsManager.log(
            HomeFilterSelected(fromScreen = ScreenLabel.Home, filter = filter),
        )
        filterListState.animateScrollToItem(selectedPageIndex, filterOffset)
    }

    var hasLoadedInitialFilter by remember { mutableStateOf(false) }
    LaunchedEffect(viewModel.selectedFilter) {
        if (!hasLoadedInitialFilter) {
            hasLoadedInitialFilter = true
            return@LaunchedEffect
        }
        viewModel.downloadData()
    }

    var hasInitializedTalkBackFilter by remember { mutableStateOf(false) }
    LaunchedEffect(viewModel.selectedFilter, talkBackEnabled) {
        if (!talkBackEnabled) {
            hasInitializedTalkBackFilter = false
            return@LaunchedEffect
        }
        if (!hasInitializedTalkBackFilter) {
            hasInitializedTalkBackFilter = true
            return@LaunchedEffect
        }
        viewModel.downloadData()
    }

    val promotionTitle = viewModel.promotionTitle
    val promotionMessage = viewModel.promotionMessage
    val promotionMessageAccessible = viewModel.promotionMessageAccessible
    val offerTimeLeft = viewModel.offerTimeLeft
    val showPromotion = !promotionTitle.isNullOrEmpty() && !promotionMessage.isNullOrEmpty()

    // Observe promotion fields explicitly — mutableStateOf on the ViewModel may not
    // trigger recomposition when updated from async billing/remote-config callbacks.
    var promotionVisible by remember { mutableStateOf(showPromotion) }
    var displayedPromotionTitle by remember { mutableStateOf(promotionTitle) }
    var displayedPromotionMessage by remember { mutableStateOf(promotionMessage) }
    var displayedPromotionMessageAccessible by remember { mutableStateOf(promotionMessageAccessible) }
    var displayedOfferTimeLeft by remember { mutableStateOf(offerTimeLeft) }

    LaunchedEffect(viewModel) {
        snapshotFlow {
            PromotionSnapshot(
                title = viewModel.promotionTitle,
                message = viewModel.promotionMessage,
                accessibleMessage = viewModel.promotionMessageAccessible,
                timeLeft = viewModel.offerTimeLeft,
            )
        }.collect { snapshot ->
            displayedPromotionTitle = snapshot.title
            displayedPromotionMessage = snapshot.message
            displayedPromotionMessageAccessible = snapshot.accessibleMessage
            displayedOfferTimeLeft = snapshot.timeLeft
            promotionVisible = !snapshot.title.isNullOrEmpty() && !snapshot.message.isNullOrEmpty()
        }
    }

    BackHandler(onBack = {
        val offset =
            homeLazyListState.firstVisibleItemIndex + homeLazyListState.firstVisibleItemScrollOffset

        if (offset > if (promotionVisible) 0 else 1) {
            AnalyticsManager.log(TabBarDoubleTapped(ScreenLabel.Home))
            coroutineScope.launch {
                homeLazyListState.animateScrollToItem(0)
            }
        } else {
            if (!doubleBackToExitPressedOnce) {
                Toast.makeText(context, "Press back again to exit the app", Toast.LENGTH_SHORT)
                    .show()
                doubleBackToExitPressedOnce = true
                Handler(Looper.getMainLooper()).postDelayed({
                    doubleBackToExitPressedOnce = false
                }, 2500L)
            } else {
                context.getActivity()?.finishAffinity()
            }
        }
    })

    Box {
        CappedFontScale(DynamicTypeSizeCap.Accessibility2) {
            SwipeRefresh(
                state = rememberSwipeRefreshState(isRefreshing = isRefreshing),
                onRefresh = {
                    isRefreshing = true

                    coroutineScope.launch {
                        launch {
                            viewModel.getGlobalMarket()
                        }

                        launch {
                            viewModel.getFearGreed()
                        }

                        launch {
                            viewModel.getGasFees()
                        }

                        launch {
                            viewModel.downloadData()
                            isRefreshing = false
                        }

                        delay(10.seconds)
                        isRefreshing = false
                    }
                },
                modifier = modifier
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    AnimatedVisibility(
                        visible = promotionVisible,
                        modifier = Modifier.wrapContentHeight(),
                        enter = slideInVertically(initialOffsetY = { it }) + expandVertically(
                            expandFrom = Bottom,
                        ),
                        exit = slideOutVertically(targetOffsetY = { it }) + shrinkVertically(
                            shrinkTowards = Bottom,
                        ),
                    ) {
                        PremiumView(
                            title = displayedPromotionTitle ?: "",
                            message = displayedPromotionMessage ?: "",
                            accessibleMessage = displayedPromotionMessageAccessible,
                            timeLeft = displayedOfferTimeLeft ?: "",
                            viewPressed = {
                                navigationViewModel.openBuyPro(
                                    ScreenLabel.Home,
                                    BuyProAction.HomePromotion,
                                )
                            },
                            closeAction = {
                                viewModel.premiumViewClosed(it)
                            },
                        )
                    }

                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        state = homeLazyListState,
                        verticalArrangement = Arrangement.spacedBy(0.dp),
                    ) {
                    /*item {
                        Row {
                            PrimaryButton(
                                "Trigger price alert"
                            ) {
                                GlobalScope.launch {
                                    PriceAlertsRepository.triggerAlert()
                                }
                            }
                            PrimaryButton(
                                "Trigger big mover"
                            ) {
                                GlobalScope.launch {
                                    PriceAlertsRepository.triggerBigMover()
                                }
                            }
                        }
                    }*/

                    item {
                        MarketStatsView(
                            globalMarket = viewModel.globalMarket,
                            gasFees = viewModel.gasFees,
                            fearGreed = viewModel.fearGreedModel,
                            navigateToGlobalMarket = {
                                AnalyticsManager.log(
                                    Navigate(
                                        fromScreen = ScreenLabel.Home,
                                        toScreen = ScreenLabel.GlobalMarket
                                    ),
                                )
                                navigationViewModel.navigate(NavigationScreens.GlobalMarket)
                            },
                            navigateToGasFees = {
                                AnalyticsManager.log(
                                    Navigate(
                                        fromScreen = ScreenLabel.Home,
                                        toScreen = ScreenLabel.GasFees
                                    ),
                                )
                                navigationViewModel.navigate(NavigationScreens.GasFees)
                            },
                            navigateToFearGreed = {
                                AnalyticsManager.log(
                                    Navigate(
                                        fromScreen = ScreenLabel.Home,
                                        toScreen = ScreenLabel.FearGreed
                                    ),
                                )
                                navigationViewModel.navigate(NavigationScreens.FearGreed)
                            }
                        )
                    }

                    conditionalStickyHeader(
                        useSticky = useStickyHeaders,
                        talkBackEnabled = talkBackEnabled,
                    ) {
                        val hideToplistHeader: Boolean =
                            viewModel.selectedFilter == CarouselFilter.Favorites && viewModel.favorites?.isEmpty() == true
                        val headerAlpha: Float by animateFloatAsState(
                            targetValue = if (hideToplistHeader) 0f else 1f,
                            animationSpec = tween(durationMillis = 220),
                            label = "toplistHeaderAlpha"
                        )

                        Column(
                            modifier = Modifier
                                .background(MaterialTheme.colors.surface)
                        ) {
                            ToplistFilterCarouselView(
                                filters = CarouselFilter.homeFilters,
                                selectedFilter = viewModel.selectedFilter,
                                scrollState = filterListState,
                                paddingValues = PaddingValues(top = 24.dp, bottom = 16.dp),
                                onFilterSelected = { filter ->
                                    viewModel.selectedFilter = filter
                                    AnalyticsManager.log(
                                        HomeFilterSelected(
                                            fromScreen = ScreenLabel.Home,
                                            filter = filter,
                                        ),
                                    )
                                    val index = CarouselFilter.homeFilters.indexOf(filter)

                                    coroutineScope.launch {
                                        if (!talkBackEnabled) {
                                            listState.animateScrollToItem(index)
                                        }
                                        filterListState.animateScrollToItem(index, filterOffset)
                                    }
                                },
                            )

                            ToplistHeaderView(
                                selectedChangeOption = PriceChange.SevenDays,
                                modifier = Modifier
                                    .alpha(headerAlpha)
                                    .padding(bottom = 16.dp)
                            )
                        }
                    }

                    item {
                        if (talkBackEnabled) {
                            HomeToplistCategory(
                                filter = viewModel.selectedFilter,
                                viewModel = viewModel,
                                context = context,
                                navigationViewModel = navigationViewModel,
                            )
                        } else {
                            LazyRow(
                                state = listState,
                                flingBehavior = flingBehavior,
                            ) {
                                items(CarouselFilter.homeFilters) { filter ->
                                    HomeToplistCategory(
                                        filter = filter,
                                        viewModel = viewModel,
                                        context = context,
                                        navigationViewModel = navigationViewModel,
                                        modifier = Modifier.fillParentMaxWidth(),
                                    )
                                }
                            }
                        }
                    }
                }
                }
            }
        }
    }
}

@Composable
private fun HomeToplistCategory(
    filter: CarouselFilter,
    viewModel: HomeViewModelImp,
    context: Context,
    navigationViewModel: NavigationViewModel,
    modifier: Modifier = Modifier,
) {
    ToplistCategoryView(
        toplist = viewModel.getList(filter),
        favoriteList = viewModel.favorites,
        showViewAll = filter.shouldShowViewAll,
        carouselFilter = filter,
        modifier = modifier,
        toggleFavorite = {
            viewModel.favouriteButtonPressed(
                item = it,
                context = context
            ) {
                navigationViewModel.openBuyPro(
                    ScreenLabel.Home,
                    BuyProAction.Favorite
                )
            }
        },
        navigateToCryptoDetail = { coin ->
            val dest = NavigationScreens.CryptoDetail(
                cryptoDetailInfoModel = coin.toCryptoDetail(),
            )
            AnalyticsManager.log(
                Navigate(
                    fromScreen = ScreenLabel.Home,
                    toScreen = ScreenLabel.CryptoDetail,
                    coinName = coin.name ?: "",
                ),
            )
            navigationViewModel.navigate(dest)
        },
        viewAllPressed = {
            viewModel.seeAllPressed()
            AnalyticsManager.log(
                ViewAll(
                    fromScreen = ScreenLabel.Home,
                    filter = filter
                )
            )
            navigationViewModel.goToMarketSubTab(0)
        }
    )
}

private data class PromotionSnapshot(
    val title: String?,
    val message: String?,
    val accessibleMessage: String?,
    val timeLeft: String?,
)