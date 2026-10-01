package com.cryptium.coincurrently.features.Market.Derivatives.Views
import com.cryptium.coincurrently.app.NavigationScreens
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cryptium.coincurrently.R
import com.cryptium.coincurrently.sharedui.buttons.ScrollToTopButton
import com.cryptium.coincurrently.sharedui.buttons.SecondaryButton
import com.cryptium.coincurrently.sharedui.components.AccessibilityLayout
import com.cryptium.coincurrently.core.extensions.isTablet
import com.cryptium.coincurrently.sharedui.models.SortOption
import com.cryptium.coincurrently.app.NavigationViewModel
import com.cryptium.coincurrently.sharedui.components.ObserveCurrencyChanges
import com.cryptium.coincurrently.sharedui.components.OnLifecycleEvent
import com.cryptium.coincurrently.features.Market.Derivatives.ViewModels.DerivativesViewModel
import com.cryptium.coincurrently.core.analytics.AnalyticsManager
import com.cryptium.coincurrently.core.localization.Localization
import com.cryptium.coincurrently.core.analytics.Navigate
import com.cryptium.coincurrently.core.analytics.ScreenLabel
import com.cryptium.coincurrently.core.analytics.ScrollToTopPressed
import com.cryptium.coincurrently.core.analytics.SheetLabel
import com.cryptium.coincurrently.core.analytics.SheetOpened
import com.cryptium.coincurrently.core.analytics.SortOptionSelected
import com.cryptium.coincurrently.features.Market.Toplist.Sheets.ToplistSortBySheet
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.cryptium.coincurrently.core.networking.ResponseModels.CryptoDetailInfoModel
import com.cryptium.coincurrently.core.networking.ResponseModels.Derivative
import kotlin.time.Duration.Companion.seconds

@Composable
fun DerivativesListScreen(
    currentPage: Int,
    scrollState: LazyListState,
    navigationViewModel: NavigationViewModel
) {
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val isTablet = LocalContext.current.isTablet()

    val coroutineScope = rememberCoroutineScope()

    val viewModel: DerivativesViewModel = viewModel()
    var isRefreshing by remember { mutableStateOf(false) }

    val isSortShowing = remember { mutableStateOf(false) }

    val firstVisibleIndex by remember {
        derivedStateOf { scrollState.firstVisibleItemIndex }
    }
    val talkBackEnabled = AccessibilityLayout.isTalkBackEnabled

    LaunchedEffect(currentPage) {
        if (currentPage == 5) {
            launch {
                viewModel.getDerivatives(
                    newSortOption = viewModel.selectedSortOption
                )
            }
            launch {
                viewModel.getBitcoin()
            }
        }
    }

    ObserveCurrencyChanges(navigationViewModel) {
        kotlinx.coroutines.coroutineScope {
            launch {
                viewModel.getDerivatives(newSortOption = viewModel.selectedSortOption)
            }
            launch { viewModel.getBitcoin() }
        }
    }

    OnLifecycleEvent { _, event ->
        when (event) {
            Lifecycle.Event.ON_RESUME -> {
                if (currentPage == 5) {
                    coroutineScope.launch {
                        kotlinx.coroutines.coroutineScope {
                            launch {
                                viewModel.getDerivatives(
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
                        viewModel.getDerivatives(
                            newSortOption = viewModel.selectedSortOption
                        )
                        isRefreshing = false
                    }

                    launch {
                        viewModel.getBitcoin()
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
                item {
                    FilterButtons(
                        selectedSortOption = viewModel.selectedSortOption,
                        isSortShowing = isSortShowing,
                    )
                }

                item {
                    DerivativesHeaderView(
                        modifier = Modifier
                            .padding(bottom = 6.dp)
                    )
                }

                if (viewModel.allDerivatives.isEmpty()) {
                    TempList(isTablet = isTablet)
                } else {
                    RealList(
                        viewModel.allDerivatives,
                        viewModel.bitcoin,
                        derivativePressed = { derivative ->

                            val dest = NavigationScreens.DerivativeDetail(
                                derivative
                            )
                            AnalyticsManager.log(
                                Navigate(
                                    fromScreen = ScreenLabel.Derivatives,
                                    toScreen = ScreenLabel.DerivativeDetail,
                                ),
                            )
                            navigationViewModel.navigate(dest)
                        }
                    )
                }
            }
        }

        if (isSortShowing.value) {
            ToplistSortBySheet(
                options = SortOption.derivativesOptions,
                selectedOption = viewModel.selectedSortOption,
                newOptionSelected = { option ->
                    AnalyticsManager.log(
                        SortOptionSelected(fromScreen = ScreenLabel.Derivatives, option = option),
                    )
                    coroutineScope.launch {
                        viewModel.getDerivatives(
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
                AnalyticsManager.log(ScrollToTopPressed(fromScreen = ScreenLabel.Derivatives))
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
        SecondaryButton(
            text = Localization.string(selectedSortOption.title),
            accessibilityLabel = Localization.format(
                "Sort, currently selected: %@",
                Localization.string(selectedSortOption.accessibilityTitle),
            ),
            icon = R.drawable.sort,
            onClick = {
                AnalyticsManager.log(
                    SheetOpened(fromScreen = ScreenLabel.Derivatives, sheet = SheetLabel.SortBy),
                )
                isSortShowing.value = true
            }
        )
    }
}


private fun LazyListScope.TempList(isTablet: Boolean) {
    val count = if (isTablet) 30 else 15
    items(count) { index ->
        DerivativeListItem(
            derivative = null,
            bitcoin = null,
            listIndex = index,
            listCount = count,
            openDerivativeDetail = { }
        )
    }
}

private fun LazyListScope.RealList(
    derivatives: List<Derivative>,
    bitcoin: CryptoDetailInfoModel?,
    derivativePressed: (Derivative) -> Unit
) {
    items(derivatives) {
        val index = derivatives.indexOf(it)

        DerivativeListItem(
            derivative = it,
            bitcoin = bitcoin,
            listIndex = index,
            listCount = derivatives.size,
            openDerivativeDetail = {
                derivativePressed.invoke(it)
            }
        )
    }
}