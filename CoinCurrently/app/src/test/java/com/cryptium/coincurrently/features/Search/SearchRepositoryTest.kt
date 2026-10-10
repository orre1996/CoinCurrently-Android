package com.cryptium.coincurrently.features.Search

import com.cryptium.coincurrently.core.networking.CacheUrls
import com.cryptium.coincurrently.core.networking.MainRepository
import com.cryptium.coincurrently.core.networking.ResponseModels.SearchModel
import com.cryptium.coincurrently.core.networking.ResponseModels.SearchResult
import com.cryptium.coincurrently.core.networking.RetrofitBuilder
import com.cryptium.coincurrently.testsupport.MainDispatcherRule
import com.cryptium.coincurrently.testsupport.seedCache
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockkObject
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SearchRepositoryTest {

    @get:Rule
    val mainDispatcher = MainDispatcherRule()

    @Before
    fun setUp() {
        mockkObject(MainRepository)
        RetrofitBuilder.clearResponseCache()
        SearchRepository.searchHistory = mutableMapOf()
    }

    @Test
    fun getSearchResult_returnsCachedModelWithoutNetwork() = runTest {
        val cached = SearchModel(coins = listOf(SearchResult(id = "bitcoin")))
        seedCache(CacheUrls.search("bit"), cached)
        coEvery { MainRepository.getSearchResults(any()) } returns Result.success(SearchModel())

        val result = SearchRepository.getSearchResult("bit")

        assertEquals("bitcoin", result?.coins?.first()?.id)
        coVerify(exactly = 0) { MainRepository.getSearchResults(any()) }
    }

    @Test
    fun getSearchResult_fetchesWhenCacheMisses() = runTest {
        val fetched = SearchModel(exchanges = listOf(SearchResult(id = "binance")))
        coEvery { MainRepository.getSearchResults("bin") } returns Result.success(fetched)

        assertEquals(fetched, SearchRepository.getSearchResult("bin"))
    }

    @Test
    fun getSearchResult_returnsNullWhenNetworkFails() = runTest {
        coEvery { MainRepository.getSearchResults("x") } returns Result.failure(IllegalStateException("down"))

        assertNull(SearchRepository.getSearchResult("x"))
    }

    @Test
    fun appendSearchHistory_prependsDedupesAndKeepsSeven() {
        repeat(8) { index ->
            SearchRepository.appendSearchHistory("coins", SearchResult(id = "coin-$index"))
        }
        SearchRepository.appendSearchHistory("coins", SearchResult(id = "coin-3", name = "updated"))

        val ids = SearchRepository.searchHistory["coins"].orEmpty().map { it.id }

        assertEquals(listOf("coin-3", "coin-7", "coin-6", "coin-5", "coin-4", "coin-2", "coin-1"), ids)
        assertEquals("updated", SearchRepository.searchHistory["coins"]?.first()?.name)
    }
}
