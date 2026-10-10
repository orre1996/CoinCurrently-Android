package com.cryptium.coincurrently.features.News

import com.cryptium.coincurrently.core.networking.CacheUrls
import com.cryptium.coincurrently.core.networking.MainRepository
import com.cryptium.coincurrently.core.networking.NewsCategory
import com.cryptium.coincurrently.core.networking.ResponseModels.NewNewsItem
import com.cryptium.coincurrently.core.networking.ResponseModels.NewNewsModel
import com.cryptium.coincurrently.core.networking.RetrofitBuilder
import com.cryptium.coincurrently.testsupport.MainDispatcherRule
import com.cryptium.coincurrently.testsupport.seedCache
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockkObject
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class NewsRepositoryTest {

    @get:Rule
    val mainDispatcher = MainDispatcherRule()

    @Before
    fun setUp() {
        mockkObject(MainRepository)
        RetrofitBuilder.clearResponseCache()
    }

    @Test
    fun getNewNews_generalUsesCoinGeckoAndRemembersItems() = runTest {
        val model = NewNewsModel(data = listOf(NewNewsItem(title = "Market open")))
        coEvery { MainRepository.getCoinGeckoNews(null) } returns Result.success(model)

        val result = NewsRepository.getNewNews(category = NewsCategory.General())

        assertEquals("Market open", result?.data?.first()?.title)
        assertEquals("Market open", NewsRepository.getCachedNewsItems(NewsCategory.General()).first().title)
        coVerify(exactly = 0) { MainRepository.getNewNews(any(), any()) }
    }

    @Test
    fun getNewNews_coinCategoryPassesCoinIdToCoinGecko() = runTest {
        coEvery { MainRepository.getCoinGeckoNews("bitcoin") } returns Result.success(
            NewNewsModel(data = listOf(NewNewsItem(title = "BTC"))),
        )

        val result = NewsRepository.getNewNews(category = NewsCategory.Coin("bitcoin"))

        assertEquals("BTC", result?.data?.first()?.title)
    }

    @Test
    fun getNewNews_videosPreferFreshCache() = runTest {
        seedCache(CacheUrls.newsVideos(), NewNewsModel(data = listOf(NewNewsItem(title = "Clip"))))
        coEvery { MainRepository.getNewNews(any(), any()) } returns Result.failure(IllegalStateException("down"))

        val result = NewsRepository.getNewNews(25, NewsCategory.Videos())

        assertEquals("Clip", result?.data?.first()?.title)
        coVerify(exactly = 0) { MainRepository.getNewNews(any(), any()) }
    }

    @Test
    fun getNewNews_returnsNullWhenCategoryFeedFails() = runTest {
        coEvery { MainRepository.getNewNews(any(), any()) } returns Result.failure(IllegalStateException("down"))

        assertNull(NewsRepository.getNewNews(25, NewsCategory.Podcasts()))
    }

    @Test
    fun getNewNews_fallsBackToCachedCoinGeckoItemsWhenRefreshIsEmpty() = runTest {
        coEvery { MainRepository.getCoinGeckoNews("ethereum") } returns Result.success(
            NewNewsModel(data = listOf(NewNewsItem(title = "First"))),
        )
        NewsRepository.getNewNews(category = NewsCategory.Coin("ethereum"))
        coEvery { MainRepository.getCoinGeckoNews("ethereum") } returns Result.success(NewNewsModel(data = emptyList()))

        val result = NewsRepository.getNewNews(category = NewsCategory.Coin("ethereum"))

        assertEquals("First", result?.data?.first()?.title)
    }

    @Test
    fun updateNewsType_replacesSelectedCategory() {
        NewsRepository.updateNewsType(NewsCategory.Negative())

        assertTrue(NewsRepository.newsCategory is NewsCategory.Negative)
    }
}
