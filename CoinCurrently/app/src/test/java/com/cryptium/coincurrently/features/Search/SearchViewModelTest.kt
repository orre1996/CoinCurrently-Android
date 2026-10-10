package com.cryptium.coincurrently.features.Search

import com.cryptium.coincurrently.app.SearchCase
import com.cryptium.coincurrently.core.networking.ResponseModels.SearchModel
import com.cryptium.coincurrently.core.networking.ResponseModels.SearchResult
import com.cryptium.coincurrently.testsupport.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockkObject
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SearchViewModelTest {

    @get:Rule
    val mainDispatcher = MainDispatcherRule()

    @Before
    fun setUp() {
        mockkObject(SearchRepository)
        every { SearchRepository.searchHistory } returns mutableMapOf()
    }

    @Test
    fun getSearchResult_clearsResultsForAnEmptyQuery() = runTest {
        val viewModel = SearchViewModel()
        viewModel.searchResult = SearchModel(coins = listOf(SearchResult(id = "bitcoin")))

        viewModel.getSearchResult("")

        assertEquals("", viewModel.searchInput.value)
        assertNull(viewModel.searchResult)
        assertFalse(viewModel.currentlySearching)
        assertFalse(viewModel.showSearchError)
    }

    @Test
    fun getSearchResult_showsAnErrorWhenNothingIsReturned() = runTest {
        coEvery { SearchRepository.getSearchResult("missing") } returns null
        val viewModel = SearchViewModel()

        viewModel.getSearchResult("missing")

        assertTrue(viewModel.showSearchError)
        assertNull(viewModel.searchResult)
        assertFalse(viewModel.currentlySearching)
    }

    @Test
    fun appendSearchHistory_refreshesTheVisibleHistory() {
        val item = SearchResult(id = "bitcoin")
        every { SearchRepository.appendSearchHistory(any(), any()) } answers {
            every { SearchRepository.searchHistory } returns mutableMapOf("Coins" to listOf(item))
        }
        val viewModel = SearchViewModel()

        viewModel.appendSearchHistory(SearchCase.Coins, item)

        assertEquals(listOf("bitcoin"), viewModel.searchHistory["Coins"]?.map { it.id })
    }
}
