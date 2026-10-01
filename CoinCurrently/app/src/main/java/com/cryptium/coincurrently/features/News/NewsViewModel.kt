package com.cryptium.coincurrently.features.News
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.*
import com.cryptium.coincurrently.core.persistence.StorageKeys
import com.cryptium.coincurrently.core.persistence.StorageManager
import com.cryptium.coincurrently.sharedui.models.CachedData
import com.cryptium.coincurrently.features.GlobalMarket.GlobalMarketRepository
import com.cryptium.coincurrently.features.News.NewsRepository
import com.cryptium.coincurrently.core.networking.NewsCategory
import com.cryptium.coincurrently.core.networking.ResponseModels.CoinsList
import com.cryptium.coincurrently.core.networking.ResponseModels.NewNewsItem
import com.cryptium.coincurrently.core.networking.ResponseModels.NewNewsModel

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

        newsRepository.getCachedNewsItems(newsCategory).takeIf { it.isNotEmpty() }?.let {
            news = it
            setNewsFiltered()
        }

        newsRepository.cachedNews?.get(NewsCategory.Podcasts().type)?.let {
            podcastNews = it.data.data
        }

        newsRepository.cachedNews?.get(NewsCategory.Videos().type)?.let {
            videoNews = it.data.data
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
        newsRepository.getNewNews(25, NewsCategory.Videos())?.let {
            videoNews = it.data
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