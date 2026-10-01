package com.cryptium.coincurrently.features.News

import com.cryptium.coincurrently.core.extensions.unwrapOrNull
import com.cryptium.coincurrently.core.networking.MainRepository
import com.cryptium.coincurrently.core.networking.ResponseModels.NewNewsItem

object NewsRepository {
    suspend fun getNews(coinId: String? = null): List<NewNewsItem>? {
        val news = MainRepository.getCoinGeckoNews(coinId?.takeIf { it.isNotEmpty() }).unwrapOrNull()
            ?: return null
        return news.data.orEmpty()
    }
}
