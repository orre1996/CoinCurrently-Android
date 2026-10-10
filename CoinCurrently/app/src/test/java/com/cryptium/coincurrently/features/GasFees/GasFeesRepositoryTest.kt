package com.cryptium.coincurrently.features.GasFees

import com.cryptium.coincurrently.core.networking.CacheUrls
import com.cryptium.coincurrently.core.networking.MainRepository
import com.cryptium.coincurrently.core.networking.ResponseModels.GasFeesModel
import com.cryptium.coincurrently.core.networking.ResponseModels.GasResult
import com.cryptium.coincurrently.core.networking.RetrofitBuilder
import com.cryptium.coincurrently.testsupport.MainDispatcherRule
import com.cryptium.coincurrently.testsupport.seedCache
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockkObject
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class GasFeesRepositoryTest {

    @get:Rule
    val mainDispatcher = MainDispatcherRule()

    @Before
    fun setUp() {
        mockkObject(MainRepository)
        RetrofitBuilder.clearResponseCache()
        GasFeesRepository.cachedGasFees = null
        GasFeesRepository.cachedGasFeesPrices = null
    }

    @After
    fun tearDown() {
        GasFeesRepository.cachedGasFees = null
        GasFeesRepository.cachedGasFeesPrices = null
    }

    @Test
    fun getGasFees_returnsFreshCacheWithoutCallingNetwork() = runTest {
        val cached = gasFees("12")
        seedCache(CacheUrls.gasFees(false), cached)
        coEvery { MainRepository.getGasFees(any()) } returns Result.success(gasFees("99"))

        val result = GasFeesRepository.getGasFees()

        assertEquals("12", result?.gasFeeData?.first()?.proposeGasPrice)
        assertEquals(cached, GasFeesRepository.cachedGasFees)
        coVerify(exactly = 0) { MainRepository.getGasFees(any()) }
    }

    @Test
    fun getGasFees_fetchesAndStoresResultWhenCacheIsEmpty() = runTest {
        val fetched = gasFees("40")
        coEvery { MainRepository.getGasFees(false) } returns Result.success(fetched)

        val result = GasFeesRepository.getGasFees()

        assertEquals(fetched, result)
        assertEquals(fetched, GasFeesRepository.cachedGasFees)
        coVerify(exactly = 1) { MainRepository.getGasFees(false) }
    }

    @Test
    fun getGasFees_returnsNullWhenNetworkFails() = runTest {
        coEvery { MainRepository.getGasFees(false) } returns Result.failure(IllegalStateException("down"))

        assertNull(GasFeesRepository.getGasFees())
        assertNull(GasFeesRepository.cachedGasFees)
    }

    @Test
    fun getGasFeesPrices_usesHistoryCacheKeyAndNetworkFlag() = runTest {
        val fetched = gasFees("7")
        coEvery { MainRepository.getGasFees(true) } returns Result.success(fetched)

        val result = GasFeesRepository.getGasFeesPrices()

        assertEquals(fetched, result)
        assertEquals(fetched, GasFeesRepository.cachedGasFeesPrices)
        coVerify(exactly = 1) { MainRepository.getGasFees(true) }
    }

    private fun gasFees(propose: String) = GasFeesModel(
        gasFeeData = listOf(
            GasResult(
                lastBlock = "1",
                sK_Date = "2026-01-01T00:00:00.000Z",
                safeGasPrice = "1",
                proposeGasPrice = propose,
                fastGasPrice = "50",
                suggestBaseFee = "1",
                gasUsedRatio = "0.5",
            ),
        ),
    )
}
