package com.cryptium.coincurrently.features.Market.Exchanges.Repositories

import com.cryptium.coincurrently.core.networking.CacheUrls
import com.cryptium.coincurrently.core.networking.MainRepository
import com.cryptium.coincurrently.core.networking.ResponseModels.ExchangeList
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

class ExchangesRepositoryTest {

    @get:Rule
    val mainDispatcher = MainDispatcherRule()

    @Before
    fun setUp() {
        mockkObject(MainRepository)
        RetrofitBuilder.clearResponseCache()
    }

    @Test
    fun getExchanges_returnsNonEmptyCacheWithoutNetwork() = runTest {
        seedCache(CacheUrls.exchanges(), arrayListOf(exchange("binance")))
        coEvery { MainRepository.getExchangesToplist() } returns Result.success(arrayListOf())

        val result = ExchangesRepository.getExchanges()

        assertEquals("binance", result?.first()?.id)
        assertEquals("binance", ExchangesRepository.exchanges?.first()?.id)
        coVerify(exactly = 0) { MainRepository.getExchangesToplist() }
    }

    @Test
    fun getExchanges_storesNetworkResult() = runTest {
        val fetched = arrayListOf(exchange("coinbase"))
        coEvery { MainRepository.getExchangesToplist() } returns Result.success(fetched)

        assertEquals(fetched, ExchangesRepository.getExchanges())
        assertEquals("coinbase", ExchangesRepository.exchanges?.first()?.id)
    }

    @Test
    fun getExchanges_returnsNullWhenNetworkFails() = runTest {
        coEvery { MainRepository.getExchangesToplist() } returns Result.failure(IllegalStateException("down"))

        assertNull(ExchangesRepository.getExchanges())
    }

    private fun exchange(id: String) = ExchangeList(
        id = id,
        name = id,
        year_established = null,
        country = null,
        url = null,
        image = null,
        has_trading_incentive = null,
        trust_score = null,
        trust_score_rank = null,
        trade_volume_24h_btc = null,
        trade_volume_24h_btc_normalized = null,
    )
}
