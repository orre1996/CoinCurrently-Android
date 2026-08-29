package com.cryptium.coincurrently.ui.main.MarketTab.CryptoDetail.Components.News

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Text
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cryptium.coincurrently.ui.main.Compose.tabPageTalkBackTraversal
import com.cryptium.coincurrently.ui.main.Compose.onSurfaceDim
import com.cryptium.coincurrently.ui.main.Extensions.satoshiBold
import com.cryptium.coincurrently.ui.main.Extensions.satoshiRegular
import com.cryptium.coincurrently.ui.main.General.Components.EmptyContent
import com.cryptium.coincurrently.ui.main.NewsTab.Compose.NewsMediaType
import com.cryptium.coincurrently.ui.main.NewsTab.Compose.SmallNewsItem
import com.cryptium.coincurrently.ui.main.analytics.AnalyticsManager
import com.cryptium.coincurrently.ui.main.analytics.NewsOpened
import com.cryptium.coincurrently.ui.main.analytics.ScreenLabel
import networking.ResponseModels.NewNewsItem

fun LazyListScope.DetailNews(
    news: List<NewNewsItem>?,
    title: String,
    showError: Boolean,
    isTablet: Boolean,
    fromScreen: ScreenLabel,
    openUrl: (String) -> Unit
) {
    if (showError) {
        item {
            EmptyContent(
                title = "No news yet",
                subTitle = "When news about this $title appears, it’ll show up here.",
                modifier = Modifier
                    .tabPageTalkBackTraversal(groupChildren = true)
                    .padding(
                        vertical = 72.dp,
                        horizontal = 32.dp
                    )
            )
        }
    } else {
        news?.let { newsList ->
            RealList(newsList, fromScreen, openUrl)
        } ?: run {
            TempList(isTablet = isTablet)
        }
    }
}

private fun LazyListScope.RealList(
    news: List<NewNewsItem>,
    fromScreen: ScreenLabel,
    openUrl: (String) -> Unit
) {
    items(news.size) { index ->
        val newsItem = news[index]
        Box(
            modifier = Modifier
                .tabPageTalkBackTraversal(groupChildren = true)
                .padding(
                start = 16.dp,
                end = 16.dp,
                top = 32.dp,
                bottom = 8.dp
            )
        ) {
            SmallNewsItem(
                newsItem = newsItem,
                index = index + 1,
                totalCount = news.size,
                newsType = NewsMediaType.Article,
                callback = {
                    AnalyticsManager.log(NewsOpened(fromScreen = fromScreen))
                    openUrl(it)
                }
            )
        }
    }
}

private fun LazyListScope.TempList(isTablet: Boolean) {
    val cardCount = if (isTablet) 20 else 10
    items(cardCount) { index ->
        Box(
            modifier = Modifier
                .tabPageTalkBackTraversal(groupChildren = true)
                .padding(
                start = 16.dp,
                end = 16.dp,
                top = 32.dp,
                bottom = 8.dp
            )
        ) {
            SmallNewsItem(
                newsItem = null,
                index = index + 1,
                totalCount = cardCount,
                callback = { },
            )
        }
    }
}