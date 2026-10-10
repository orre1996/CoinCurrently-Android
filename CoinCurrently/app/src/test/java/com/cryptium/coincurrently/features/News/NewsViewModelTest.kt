package com.cryptium.coincurrently.features.News

import com.cryptium.coincurrently.core.networking.NewsCategory
import com.cryptium.coincurrently.core.networking.ResponseModels.CoinsList
import com.cryptium.coincurrently.core.networking.ResponseModels.NewNewsItem
import com.cryptium.coincurrently.core.networking.ResponseModels.NewNewsModel
import com.cryptium.coincurrently.features.GlobalMarket.GlobalMarketRepository
import com.cryptium.coincurrently.testsupport.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockkObject
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class NewsViewModelTest {

    @get:Rule
    val mainDispatcher = MainDispatcherRule()

    @Before
    fun setUp() {
        mockkObject(NewsRepository, GlobalMarketRepository)
        every { NewsRepository.newsCategory } returns NewsCategory.General()
        every { NewsRepository.getCachedNewsItems(any(), any()) } returns emptyList()
        every { NewsRepository.cachedNews } returns mutableMapOf()
    }

    @Test
    fun getNews_filtersSentimentCategories() = runTest {
        val items = listOf(
            NewNewsItem(title = "up", sentiment = "Positive"),
            NewNewsItem(title = "down", sentiment = "Negative"),
        )
        coEvery { NewsRepository.getNewNews(any(), any(), any()) } returns NewNewsModel(data = items)
        every { NewsRepository.updateNewsType(any()) } returns Unit
        val viewModel = NewsViewModel()

        viewModel.getNews(NewsCategory.Positive())

        assertEquals(listOf("up"), viewModel.news?.map { it.title })
        assertTrue(viewModel.newsCategory is NewsCategory.Positive)
    }

    @Test
    fun getNews_clearsTheListWhenTheCategoryChanges() = runTest {
        coEvery { NewsRepository.getNewNews(any(), any(), any()) } returns null
        every { NewsRepository.updateNewsType(any()) } returns Unit
        val viewModel = NewsViewModel()
        viewModel.news = listOf(NewNewsItem(title = "old"))

        viewModel.getNews(NewsCategory.Videos())

        assertEquals(null, viewModel.news)
    }

    @Test
    fun getAllCoins_marksTheLoadCompleteEvenWhenTheListIsEmpty() = runTest {
        coEvery { GlobalMarketRepository.getAllCoins() } returns null
        val viewModel = NewsViewModel()

        viewModel.getAllCoins()

        assertTrue(viewModel.hasLoadedAllCoinsOnce)
        assertEquals(emptyList<CoinsList>(), viewModel.coinsList)
    }
}
