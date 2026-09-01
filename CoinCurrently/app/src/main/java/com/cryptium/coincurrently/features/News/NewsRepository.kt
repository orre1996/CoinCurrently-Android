package com.cryptium.coincurrently.features.News
import com.cryptium.coincurrently.core.extensions.unwrapOrNull
import com.cryptium.coincurrently.core.persistence.StorageKeys
import com.google.gson.reflect.TypeToken
import com.cryptium.coincurrently.core.persistence.StorageManager
import com.cryptium.coincurrently.sharedui.models.CachedData
import com.cryptium.coincurrently.core.networking.ApiHelper
import com.cryptium.coincurrently.core.networking.CacheUrls
import com.cryptium.coincurrently.core.networking.MainRepository
import com.cryptium.coincurrently.core.networking.NewsCategory
import com.cryptium.coincurrently.core.networking.ResponseModels.NewNewsModel
import com.cryptium.coincurrently.core.networking.ResponseModels.NewNewsItem
import com.cryptium.coincurrently.core.networking.RetrofitBuilder
import java.util.Date

class NewsRepository {
    companion object {
        private val mainRepository by lazy { MainRepository(ApiHelper(RetrofitBuilder.apiService)) }

        private val newNewsModelType = object : TypeToken<NewNewsModel>() {}.type

        var cachedNews: MutableMap<String, CachedData<NewNewsModel>> = mutableMapOf()
            private set
        var newsCategory: NewsCategory = StorageManager.get<NewsCategory>(StorageKeys.NEWS_CATEGORY) ?: NewsCategory.General()

        init {
            try {
                listOf(
                    NewsCategory.General(), NewsCategory.Technical_Analysis(), NewsCategory.Positive(),
                    NewsCategory.Neutral(), NewsCategory.Negative(), NewsCategory.Podcasts(),
                    NewsCategory.Videos(), NewsCategory.Exchanges(), NewsCategory.NFT()
                ).forEach { cat ->
                    val url = newsUrl(cat)
                    RetrofitBuilder.getCached(url, newNewsModelType, includeExpired = true)?.let { cached ->
                        (cached.data as? NewNewsModel)?.let { data ->
                            cachedNews = (cachedNews + (cat.type to CachedData(data, cached.date))).toMutableMap()
                        }
                    }
                }
            } catch (_: Exception) {
                // StorageManager/RetrofitBuilder may not be ready yet; cache will populate on first fetch
            }
        }

        private fun newsUrl(category: NewsCategory, count: Int = 100): String = when (category) {
            is NewsCategory.General -> CacheUrls.newsGeneral(count)
            is NewsCategory.Technical_Analysis -> CacheUrls.newsTAnalysis()
            is NewsCategory.Positive -> CacheUrls.newsGeneral(25)
            is NewsCategory.Neutral, is NewsCategory.Negative -> CacheUrls.newsGeneral(count)
            is NewsCategory.Podcasts -> CacheUrls.newsPodcasts()
            is NewsCategory.Videos -> CacheUrls.newsVideos()
            is NewsCategory.Exchange -> CacheUrls.newsExchange(category.id)
            is NewsCategory.Exchanges -> CacheUrls.newsExchanges()
            is NewsCategory.NFT -> CacheUrls.newsNft()
            is NewsCategory.Coin -> CacheUrls.newsCoin(category.id)
        }

        suspend fun getNewNews(count: Int = 100, category: NewsCategory): NewNewsModel? {
            val url = newsUrl(category, count)
            RetrofitBuilder.getCached(url, newNewsModelType)?.let { cached ->
                (cached.data as? NewNewsModel)?.let { data ->
                    if (!data.data.isNullOrEmpty()) {
                        cachedNews = (cachedNews + (category.type to CachedData(data, cached.date))).toMutableMap()
                        return data
                    }
                }
            }

            val news = mainRepository.getNewNews(count, category).unwrapOrNull()
            news?.let {
                cachedNews = (cachedNews + (category.type to CachedData(it, Date()))).toMutableMap()
            }
            return news
        }

        fun updateNewsType(category: NewsCategory) {
            StorageManager.set(StorageKeys.NEWS_CATEGORY, category)
            newsCategory = category
        }

        fun getCachedNewsItems(category: NewsCategory): List<NewNewsItem> {
            return cachedNews[category.type]?.data?.data.orEmpty()
        }
    }
}
