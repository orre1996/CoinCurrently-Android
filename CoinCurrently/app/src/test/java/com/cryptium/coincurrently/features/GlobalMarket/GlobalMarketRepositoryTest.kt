package com.cryptium.coincurrently.features.GlobalMarket

import com.cryptium.coincurrently.core.networking.CacheUrls
import com.cryptium.coincurrently.core.networking.MainRepository
import com.cryptium.coincurrently.core.networking.ResponseModels.CoinsList
import com.cryptium.coincurrently.core.networking.ResponseModels.GlobalMarketChartData
import com.cryptium.coincurrently.core.networking.ResponseModels.GlobalMarketChartModel
import com.cryptium.coincurrently.core.networking.ResponseModels.GlobalMarketData
import com.cryptium.coincurrently.core.networking.ResponseModels.GlobalMarketModel
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

class GlobalMarketRepositoryTest {

    @get:Rule
    val mainDispatcher = MainDispatcherRule()

    @Before
    fun setUp() {
        mockkObject(MainRepository)
        RetrofitBuilder.clearResponseCache()
        coEvery { MainRepository.getCoinsList() } returns Result.success(arrayListOf())
        GlobalMarketRepository.globalChart = null
    }

    @Test
    fun getAllCoins_returnsCacheWithoutNetwork() = runTest {
        val cached = arrayListOf(CoinsList(id = "bitcoin", symbol = "btc", name = "Bitcoin"))
        seedCache(CacheUrls.coinsList(), cached)
        coEvery { MainRepository.getCoinsList() } returns Result.failure(IllegalStateException("down"))

        val result = GlobalMarketRepository.getAllCoins()

        assertEquals("bitcoin", result?.first()?.id)
        assertEquals(cached, GlobalMarketRepository.coinsList)
    }

    @Test
    fun getAllCoins_storesNetworkList() = runTest {
        val fetched = arrayListOf(CoinsList(id = "ethereum"))
        coEvery { MainRepository.getCoinsList() } returns Result.success(fetched)

        assertEquals(fetched, GlobalMarketRepository.getAllCoins())
    }

    @Test
    fun getGlobalChart_keepsEveryPointForShortRanges() = runTest {
        val chart = chart(points = 4)
        seedCache(CacheUrls.globalChart("7"), chart)
        coEvery { MainRepository.getGlobalChart(any()) } returns Result.failure(IllegalStateException("down"))

        val result = GlobalMarketRepository.getGlobalChart("7")

        assertEquals(4, result?.market_cap?.size)
        coVerify(exactly = 0) { MainRepository.getGlobalChart(any()) }
    }

    @Test
    fun getGlobalChart_downsamplesMaxRangeButKeepsExtremes() = runTest {
        val caps = ArrayList<ArrayList<Double?>>()
        repeat(800) { index ->
            caps.add(arrayListOf(index.toDouble(), index.toDouble()))
        }
        caps[3] = arrayListOf(3.0, 10_000.0)
        seedCache(
            CacheUrls.globalChart("max"),
            GlobalMarketChartModel(
                market_cap_chart = GlobalMarketChartData(
                    market_cap = caps,
                    volume = arrayListOf(arrayListOf(1.0, 2.0)),
                ),
            ),
        )

        val result = GlobalMarketRepository.getGlobalChart("max")

        assertEquals(401, result?.market_cap?.size)
        org.junit.Assert.assertTrue(result?.market_cap?.any { it.lastOrNull() == 10_000.0 } == true)
    }

    @Test
    fun getGlobalMarket_buildsTopPercentagesAndOtherSlice() = runTest {
        val model = GlobalMarketModel(
            data = GlobalMarketData(
                market_cap_percentage = mapOf("eth" to 18.0, "btc" to 52.0, "sol" to 3.0),
                active_cryptocurrencies = 12000,
            ),
        )
        coEvery { MainRepository.getGlobalMarket() } returns Result.success(model)

        val result = GlobalMarketRepository.getGlobalMarket()

        assertEquals(listOf("BTC", "ETH", "SOL", "OTHER"), result?.percentages?.map { it.symbol })
        assertEquals(12000, result?.active_cryptocurrencies)
        assertEquals(result, GlobalMarketRepository.globalMarket)
    }

    @Test
    fun getGlobalMarket_returnsNullWhenPayloadHasNoData() = runTest {
        coEvery { MainRepository.getGlobalMarket() } returns Result.success(GlobalMarketModel(data = null))

        assertNull(GlobalMarketRepository.getGlobalMarket())
    }

    private fun chart(points: Int): GlobalMarketChartModel {
        val caps = ArrayList<ArrayList<Double?>>()
        repeat(points) { index -> caps.add(arrayListOf(index.toDouble(), index.toDouble() + 1)) }
        return GlobalMarketChartModel(market_cap_chart = GlobalMarketChartData(market_cap = caps, volume = caps))
    }
}
