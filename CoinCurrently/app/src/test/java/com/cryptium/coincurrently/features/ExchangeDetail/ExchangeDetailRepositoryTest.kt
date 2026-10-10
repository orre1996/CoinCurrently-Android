package com.cryptium.coincurrently.features.ExchangeDetail

import com.cryptium.coincurrently.core.networking.CacheUrls
import com.cryptium.coincurrently.core.networking.MainRepository
import com.cryptium.coincurrently.core.networking.ResponseModels.ExchangeResponse
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

class ExchangeDetailRepositoryTest {

    @get:Rule
    val mainDispatcher = MainDispatcherRule()

    @Before
    fun setUp() {
        mockkObject(MainRepository)
        RetrofitBuilder.clearResponseCache()
        ExchangeDetailRepository.exchangeDetail = null
        ExchangeDetailRepository.exchangePrices = null
    }

    @Test
    fun getExchange_returnsNullWithoutId() = runTest {
        assertNull(ExchangeDetailRepository.getExchange(null))
    }

    @Test
    fun getExchange_stampsCachedResponseWithRequestedId() = runTest {
        seedCache(CacheUrls.exchangeDetail("binance"), ExchangeResponse(name = "Binance"))
        coEvery { MainRepository.getExchangeDetailInfo(any()) } returns Result.failure(IllegalStateException("down"))

        val result = ExchangeDetailRepository.getExchange("binance")

        assertEquals("binance", result?.id)
        assertEquals("Binance", result?.name)
        assertEquals("binance", ExchangeDetailRepository.exchangeDetail?.get("binance")?.id)
        coVerify(exactly = 0) { MainRepository.getExchangeDetailInfo(any()) }
    }

    @Test
    fun getExchange_storesNetworkResponse() = runTest {
        coEvery { MainRepository.getExchangeDetailInfo("kraken") } returns Result.success(ExchangeResponse(name = "Kraken"))

        val result = ExchangeDetailRepository.getExchange("kraken")

        assertEquals("kraken", result?.id)
        assertEquals("Kraken", ExchangeDetailRepository.exchangeDetail?.get("kraken")?.name)
    }

    @Test
    fun getExchangePrices_decodesCachedVolumeJson() = runTest {
        seedCache(CacheUrls.exchangeVolume("binance", "7"), """[[10.0,"2.5"]]""")

        val result = ExchangeDetailRepository.getExchangePrices("binance", "7")

        assertEquals(10.0, result?.first()?.timestamp)
        assertEquals(2.5, result?.first()?.price)
        assertEquals(2.5, ExchangeDetailRepository.exchangePrices?.get("binance-7")?.first()?.price)
    }

    @Test
    fun getExchangePrices_returnsNullWhenVolumeCallFails() = runTest {
        coEvery { MainRepository.getExchangeVolume(any(), any()) } returns Result.failure(IllegalStateException("down"))

        assertNull(ExchangeDetailRepository.getExchangePrices("binance", "1"))
    }

    @Test
    fun loadCachedDataForExchange_ignoresBlankIds() {
        ExchangeDetailRepository.loadCachedDataForExchange("", "7")

        assertNull(ExchangeDetailRepository.exchangeDetail)
    }
}
