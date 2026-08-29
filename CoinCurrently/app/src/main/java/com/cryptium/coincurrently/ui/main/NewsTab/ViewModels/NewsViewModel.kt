package com.cryptium.coincurrently.ui.main.NewsTab.ViewModels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.*
import com.cryptium.coincurrently.ui.main.Managers.StorageKeys
import com.cryptium.coincurrently.ui.main.Managers.StorageManager
import com.cryptium.coincurrently.ui.main.Models.CachedData
import com.cryptium.coincurrently.ui.main.HomeTab.GlobalMarket.Repositories.GlobalMarketRepository
import com.cryptium.coincurrently.ui.main.Managers.FirebaseManager
import com.cryptium.coincurrently.ui.main.Managers.SecretMessageHelper
import com.cryptium.coincurrently.ui.main.Repositories.NewsRepository
import kotlinx.coroutines.launch
import networking.NewsCategory
import networking.ResponseModels.CoinsList
import networking.ResponseModels.NewNewsItem
import networking.ResponseModels.NewNewsModel

class NewsViewModel: ViewModel() {

    private val newsRepository by lazy { NewsRepository }
    private val globalMarketRepository by lazy { GlobalMarketRepository }

    var news: List<NewNewsItem>? by mutableStateOf(null)
    var videoNews: List<NewNewsItem>? by mutableStateOf(null)
    var podcastNews: List<NewNewsItem>? by mutableStateOf(null)
    var coinsList: List<CoinsList> by mutableStateOf(listOf())

    var hasLoadedAllCoinsOnce: Boolean = false

    var newsCategory: NewsCategory by mutableStateOf(NewsCategory.General())

    var selectedVideo: NewNewsItem? by mutableStateOf(null)

    init {
        newsCategory = newsRepository.newsCategory

        newsRepository.cachedNews?.get(newsCategory.type)?.let {
            news = it.data.data
            setNewsFiltered()
        }

        newsRepository.cachedNews?.get(NewsCategory.Podcasts().type)?.let {
            podcastNews = it.data.data
        }

        viewModelScope.launch {
            FirebaseManager.ensureRemoteConfigFetched()
            newsRepository.cachedNews?.get(NewsCategory.Videos().type)?.let {
                videoNews = SecretMessageHelper.withSecretNews(
                    it.data.data.orEmpty(),
                    SecretMessageHelper.PLACEMENT_NEWS_VIDEOS,
                    8,
                )
            }
        }
    }

    suspend fun getNews(category: NewsCategory) {
        if (newsCategory != category) {
            news = null
        }

        newsCategory = category
        newsRepository.updateNewsType(category)

        val count = when(category) {
            is NewsCategory.General -> {
                100
            } is NewsCategory.Neutral -> {
                100
            } is NewsCategory.Positive -> {
                100
            } is NewsCategory.Negative -> {
                100
            } else -> {
                25
            }
        }

        newsRepository.getNewNews(count, category = category)?.let {
            news = it.data
            setNewsFiltered()
        }
    }

    suspend fun getVideoNews() {
        FirebaseManager.ensureRemoteConfigFetched()
        newsRepository.getNewNews(25, NewsCategory.Videos())?.let {
            videoNews = SecretMessageHelper.withSecretNews(
                it.data.orEmpty(),
                SecretMessageHelper.PLACEMENT_NEWS_VIDEOS,
                8,
            )
        }
    }

    suspend fun getPodcastNews() {
        newsRepository.getNewNews(25, NewsCategory.Podcasts())?.let {
            podcastNews = it.data
        }
    }

    suspend fun getAllCoins() {
        globalMarketRepository.getAllCoins()?.let {
            coinsList = it
        }
        hasLoadedAllCoinsOnce = true
    }

    private fun setNewsFiltered() {
        news = when (newsCategory) {
            is NewsCategory.Positive -> {
                news?.filter { it.sentiment == "Positive" }
            }
            is NewsCategory.Neutral -> {
                news?.filter { it.sentiment == "Neutral" }
            }
            is NewsCategory.Negative -> {
                news?.filter { it.sentiment == "Negative" }
            }
            else -> {
                news
            }
        }
    }
}