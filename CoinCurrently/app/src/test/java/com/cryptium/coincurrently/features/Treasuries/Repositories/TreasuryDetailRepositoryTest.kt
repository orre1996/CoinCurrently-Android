package com.cryptium.coincurrently.features.Treasuries.Repositories

import com.cryptium.coincurrently.core.networking.CacheUrls
import com.cryptium.coincurrently.core.networking.MainRepository
import com.cryptium.coincurrently.core.networking.ResponseModels.TreasuryDetailEntity
import com.cryptium.coincurrently.core.networking.ResponseModels.TreasuryHoldingChartModel
import com.cryptium.coincurrently.core.networking.ResponseModels.TreasuryTransactionHistoryModel
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

class TreasuryDetailRepositoryTest {

    @get:Rule
    val mainDispatcher = MainDispatcherRule()

    @Before
    fun setUp() {
        mockkObject(MainRepository)
        RetrofitBuilder.clearResponseCache()
        TreasuryDetailRepository.cachedTreasuryDetails.clear()
        TreasuryDetailRepository.cachedHoldingCharts.clear()
        TreasuryDetailRepository.cachedTransactionHistory.clear()
    }

    @Test
    fun getTreasuryDetail_returnsCacheWithoutNetwork() = runTest {
        val cached = TreasuryDetailEntity(id = "microstrategy", name = "Strategy")
        seedCache(CacheUrls.treasuryDetail("microstrategy"), cached)
        coEvery { MainRepository.getTreasuryDetail(any()) } returns Result.failure(IllegalStateException("down"))

        val result = TreasuryDetailRepository.getTreasuryDetail("microstrategy")

        assertEquals("Strategy", result?.name)
        assertEquals(cached, TreasuryDetailRepository.getCachedTreasuryDetail("microstrategy"))
        coVerify(exactly = 0) { MainRepository.getTreasuryDetail(any()) }
    }

    @Test
    fun getHoldingChart_storesNetworkChart() = runTest {
        val chart = TreasuryHoldingChartModel(holdings = listOf(listOf(1.0, 2.0)))
        coEvery { MainRepository.getTreasuryHoldingChart("microstrategy", "bitcoin", "30") } returns Result.success(chart)

        val result = TreasuryDetailRepository.getHoldingChart("microstrategy", "bitcoin", "30")

        assertEquals(chart, result)
        assertEquals(chart, TreasuryDetailRepository.getCachedHoldingChart("microstrategy", "bitcoin", "30"))
    }

    @Test
    fun getTransactionHistory_returnsNullWhenNetworkFails() = runTest {
        coEvery { MainRepository.getTreasuryTransactionHistory(any(), any(), any(), any()) } returns
            Result.failure(IllegalStateException("down"))

        assertNull(TreasuryDetailRepository.getTransactionHistory("microstrategy", "bitcoin"))
    }

    @Test
    fun ensureCacheLoaded_hydratesDetailChartAndHistory() = runTest {
        val detail = TreasuryDetailEntity(id = "usa")
        val chart = TreasuryHoldingChartModel(prices = listOf(listOf(1.0, 2.0)))
        val history = TreasuryTransactionHistoryModel(transactions = emptyList())
        seedCache(CacheUrls.treasuryDetail("usa"), detail)
        seedCache(CacheUrls.treasuryHoldingChart("usa", "bitcoin", "7"), chart)
        seedCache(CacheUrls.treasuryTransactionHistory("usa"), history)

        TreasuryDetailRepository.ensureCacheLoaded("usa", "bitcoin", "7")

        assertEquals(detail, TreasuryDetailRepository.getCachedTreasuryDetail("usa"))
        assertEquals(chart, TreasuryDetailRepository.getCachedHoldingChart("usa", "bitcoin", "7"))
        assertEquals(history, TreasuryDetailRepository.getCachedTransactionHistory("usa", null))
    }
}
