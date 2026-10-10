package com.cryptium.coincurrently.features.Market.Derivatives.Repositories

import com.cryptium.coincurrently.core.networking.CacheUrls
import com.cryptium.coincurrently.core.networking.MainRepository
import com.cryptium.coincurrently.core.networking.ResponseModels.Derivative
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

class DerivativesRepositoryTest {

    @get:Rule
    val mainDispatcher = MainDispatcherRule()

    private val sort = SortOption.derivativesOptions.first()

    @Before
    fun setUp() {
        mockkObject(MainRepository)
        RetrofitBuilder.clearResponseCache()
    }

    @Test
    fun getDerivatives_returnsNonEmptyCacheWithoutNetwork() = runTest {
        val cached = arrayListOf(Derivative(id = "binance"))
        seedCache(CacheUrls.derivatives(sort), cached)
        coEvery { MainRepository.getAllDerivatives(any()) } returns Result.success(emptyList())

        val result = DerivativesRepository.getDerivatives(sort)

        assertEquals("binance", result?.first()?.id)
        assertEquals("binance", DerivativesRepository.derivatives[sort.name]?.data?.first()?.id)
        coVerify(exactly = 0) { MainRepository.getAllDerivatives(any()) }
    }

    @Test
    fun getDerivatives_storesNetworkList() = runTest {
        val fetched = listOf(Derivative(id = "bybit"))
        coEvery { MainRepository.getAllDerivatives(sort) } returns Result.success(fetched)

        assertEquals(fetched, DerivativesRepository.getDerivatives(sort))
        assertEquals("bybit", DerivativesRepository.derivatives[sort.name]?.data?.first()?.id)
    }

    @Test
    fun getDerivatives_returnsNullWhenNetworkFails() = runTest {
        coEvery { MainRepository.getAllDerivatives(sort) } returns Result.failure(IllegalStateException("down"))

        assertNull(DerivativesRepository.getDerivatives(sort))
    }
}
