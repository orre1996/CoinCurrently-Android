package com.cryptium.coincurrently.features.Market.Categories.ViewModels

import com.cryptium.coincurrently.core.networking.ResponseModels.CryptoCategory
import com.cryptium.coincurrently.core.persistence.CurrencyRepository
import com.cryptium.coincurrently.features.Market.Categories.Repositories.CategoriesRepository
import com.cryptium.coincurrently.sharedui.models.SortOption
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

class CategoriesViewModelTest {

    @get:Rule
    val mainDispatcher = MainDispatcherRule()

    @Before
    fun setUp() {
        mockkObject(CategoriesRepository, CurrencyRepository)
        every { CategoriesRepository.categories } returns mutableMapOf()
        every { CurrencyRepository.bitcoinData } returns null
    }

    @Test
    fun getCategories_replacesTheListForTheRequestedSort() = runTest {
        val fetched = listOf(CryptoCategory(id = "defi"))
        coEvery { CategoriesRepository.getCategories(SortOption.NameAsc) } returns fetched
        val viewModel = CategoriesViewModel()

        viewModel.getCategories(SortOption.NameAsc)

        assertEquals(fetched, viewModel.categories)
        assertEquals(SortOption.NameAsc, viewModel.selectedSortOption)
    }

    @Test
    fun getCategories_keepsThePreviousListWhenTheFetchFails() = runTest {
        val viewModel = CategoriesViewModel()
        viewModel.categories = listOf(CryptoCategory(id = "meme"))
        coEvery { CategoriesRepository.getCategories(any()) } returns null

        viewModel.getCategories(viewModel.selectedSortOption)

        assertEquals("meme", viewModel.categories.first().id)
        assertTrue(viewModel.multiplier == null)
    }
}
