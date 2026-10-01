package com.cryptium.coincurrently.features.News
import com.cryptium.coincurrently.core.localization.Localization
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.activity.compose.BackHandler
import androidx.compose.ui.Alignment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cryptium.coincurrently.sharedui.theme.*
import com.cryptium.coincurrently.core.extensions.getActivity
import com.cryptium.coincurrently.core.localization.AppLanguageState
import com.cryptium.coincurrently.app.NavigationViewModel
import com.cryptium.coincurrently.sharedui.components.OnLifecycleEvent
import com.cryptium.coincurrently.core.analytics.ScreenLabel
import com.cryptium.coincurrently.sharedui.sheets.WebViewSheet
import com.cryptium.coincurrently.core.analytics.AnalyticsManager
import com.cryptium.coincurrently.core.analytics.WebViewOpened
import com.cryptium.coincurrently.core.analytics.TabBarDoubleTapped
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.cryptium.coincurrently.core.networking.ResponseModels.NewNewsItem
import kotlin.time.Duration.Companion.seconds

@Composable
fun NewsScreen(
    navigationViewModel: NavigationViewModel,
    modifier: Modifier
) {
    val viewModel: NewsViewModel = viewModel()
    val context = LocalContext.current

    val coroutineScope = rememberCoroutineScope()
    var doubleBackToExitPressedOnce by remember { mutableStateOf(false) }

    val newsScrollState = rememberLazyListState()

    var selectedUrl: String? by remember { mutableStateOf(null) }

    val isRefreshing = remember { mutableStateOf(false) }

    LaunchedEffect(AppLanguageState.preference) {
        viewModel.getNews()
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
            BackHandler(onBack = {
                val offset = newsScrollState.firstVisibleItemIndex + newsScrollState.firstVisibleItemScrollOffset

                if (offset > 0) {
                    AnalyticsManager.log(TabBarDoubleTapped(ScreenLabel.News))
                    coroutineScope.launch {
                        newsScrollState.animateScrollToItem(0)
                    }
                } else {
                    if (!doubleBackToExitPressedOnce) {
                        Toast.makeText(context, Localization.string("Press back again to exit the app"), Toast.LENGTH_SHORT).show()
                        doubleBackToExitPressedOnce = true
                        Handler(Looper.getMainLooper()).postDelayed({
                            doubleBackToExitPressedOnce = false
                        }, 2500L)
                    } else {
                        context.getActivity()?.finishAffinity()
                    }
                }
            })

            NewsSection(
                news = viewModel.news,
                scrollState = newsScrollState,
                isRefreshing = isRefreshing.value,
                newsCardPressed = onNewsUrlPressed,
                getNews = {
                    coroutineScope.launch {
                        viewModel.getNews()
                    }
                },
                refresh = {
                    coroutineScope.launch {
                        isRefreshing.value = true

                        launch {
                            viewModel.getNews()
                            isRefreshing.value = false
                        }

                        delay(10.seconds)
                        isRefreshing.value = false
                    }
                },
            )
        }

        selectedUrl?.let {
            WebViewSheet(it) {
                selectedUrl = null
            }
        }
    }
}

@Composable
private fun NewsSection(
    news: List<NewNewsItem>?,
    scrollState: LazyListState,
    isRefreshing: Boolean,
    newsCardPressed: (String) -> Unit,
    getNews: () -> Unit,
    refresh: () -> Unit,
) {
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp

    LaunchedEffect(Unit) {
        getNews.invoke()
    }

    OnLifecycleEvent { _, event ->
        when (event) {
            Lifecycle.Event.ON_RESUME -> {
                getNews.invoke()
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
                top =  16.dp,
                bottom = 32.dp
            ),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(32.dp),
            modifier = Modifier
                .height(screenHeight)
        ) {
            val totalCount = news?.size ?: 10

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
                        newsType = NewsMediaType.Article,
                        callback = {
                            newsCardPressed(it)
                        }
                    )
                }
            }
        }
    }
}
