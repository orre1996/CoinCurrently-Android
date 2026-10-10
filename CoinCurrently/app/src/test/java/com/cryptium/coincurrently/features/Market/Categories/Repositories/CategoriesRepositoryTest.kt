package com.cryptium.coincurrently.features.Market.Categories.Repositories

import com.cryptium.coincurrently.core.networking.CacheUrls
import com.cryptium.coincurrently.core.networking.MainRepository
import com.cryptium.coincurrently.core.networking.ResponseModels.CryptoCategory
import com.cryptium.coincurrently.core.networking.RetrofitBuilder
import com.cryptium.coincurrently.sharedui.models.SortOption
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

class CategoriesRepositoryTest {

    @get:Rule
    val mainDispatcher = MainDispatcherRule()

    private val sort = SortOption.categoriesOptions.first()

    @Before
    fun setUp() {
        mockkObject(MainRepository)
        RetrofitBuilder.clearResponseCache()
    }

    @Test
    fun getCategories_returnsNonEmptyCacheWithoutNetwork() = runTest {
        val cached = arrayListOf(CryptoCategory(id = "defi", name = "DeFi"))
        seedCache(CacheUrls.categories(sort), cached)
        coEvery { MainRepository.getAllCategories(any()) } returns Result.success(arrayListOf())

        val result = CategoriesRepository.getCategories(sort)

        assertEquals("defi", result?.first()?.id)
        coVerify(exactly = 0) { MainRepository.getAllCategories(any()) }
    }

    @Test
    fun getCategories_storesNetworkResultUnderApiKey() = runTest {
        val fetched = arrayListOf(CryptoCategory(id = "meme"))
        coEvery { MainRepository.getAllCategories(sort) } returns Result.success(fetched)

        assertEquals(fetched, CategoriesRepository.getCategories(sort))
        assertEquals("meme", CategoriesRepository.categories[sort.categoriesApiValue]?.data?.first()?.id)
    }

    @Test
    fun getCategories_returnsNullWhenNetworkFails() = runTest {
        coEvery { MainRepository.getAllCategories(sort) } returns Result.failure(IllegalStateException("down"))

        assertNull(CategoriesRepository.getCategories(sort))
    }
}
