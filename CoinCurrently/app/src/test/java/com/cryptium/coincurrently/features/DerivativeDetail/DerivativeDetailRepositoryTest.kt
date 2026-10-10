package com.cryptium.coincurrently.features.DerivativeDetail

import com.cryptium.coincurrently.core.networking.CacheUrls
import com.cryptium.coincurrently.core.networking.MainRepository
import com.cryptium.coincurrently.core.networking.ResponseModels.Derivative
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

class DerivativeDetailRepositoryTest {

    @get:Rule
    val mainDispatcher = MainDispatcherRule()

    @Before
    fun setUp() {
        mockkObject(MainRepository)
        RetrofitBuilder.clearResponseCache()
    }

    @Test
    fun getDerivativeDetail_returnsNullForMissingId() = runTest {
        assertNull(DerivativeDetailRepository.getDerivativeDetail(null))
        coVerify(exactly = 0) { MainRepository.getDerivativeDetail(any()) }
    }

    @Test
    fun getDerivativeDetail_copiesRequestedIdOntoCachedDerivative() = runTest {
        seedCache(CacheUrls.derivativeDetail("binance_futures"), Derivative(name = "Binance"))
        coEvery { MainRepository.getDerivativeDetail(any()) } returns Result.success(Derivative())

        val result = DerivativeDetailRepository.getDerivativeDetail("binance_futures")

        assertEquals("binance_futures", result?.id)
        assertEquals("Binance", result?.name)
        coVerify(exactly = 0) { MainRepository.getDerivativeDetail(any()) }
    }

    @Test
    fun getDerivativeDetail_fetchesAndStampsIdWhenCacheMisses() = runTest {
        coEvery { MainRepository.getDerivativeDetail("okx") } returns Result.success(Derivative(name = "OKX"))

        val result = DerivativeDetailRepository.getDerivativeDetail("okx")

        assertEquals("okx", result?.id)
        assertEquals("OKX", result?.name)
    }
}
