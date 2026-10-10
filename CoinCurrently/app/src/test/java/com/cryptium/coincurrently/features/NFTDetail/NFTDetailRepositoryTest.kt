package com.cryptium.coincurrently.features.NFTDetail

import com.cryptium.coincurrently.core.networking.CacheUrls
import com.cryptium.coincurrently.core.networking.MainRepository
import com.cryptium.coincurrently.core.networking.ResponseModels.NFTDetail
import com.cryptium.coincurrently.core.networking.ResponseModels.NFTTicker
import com.cryptium.coincurrently.core.networking.ResponseModels.NFTTickerResponse
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

class NFTDetailRepositoryTest {

    @get:Rule
    val mainDispatcher = MainDispatcherRule()

    @Before
    fun setUp() {
        mockkObject(MainRepository)
        RetrofitBuilder.clearResponseCache()
        NFTDetailRepository.nftDetail = null
        NFTDetailRepository.nftPrices = null
        NFTDetailRepository.nftTickers = null
    }

    @Test
    fun getNftDetailInfo_returnsNullWithoutId() = runTest {
        assertNull(NFTDetailRepository.getNftDetailInfo(null))
    }

    @Test
    fun getNftDetailInfo_returnsCacheAndRemembersIt() = runTest {
        val cached = NFTDetail(id = "punks", name = "Punks")
        seedCache(CacheUrls.nftDetail("punks"), cached)
        coEvery { MainRepository.getNftDetail(any()) } returns Result.failure(IllegalStateException("down"))

        val result = NFTDetailRepository.getNftDetailInfo("punks")

        assertEquals("Punks", result?.name)
        assertEquals(cached, NFTDetailRepository.nftDetail?.get("punks"))
        coVerify(exactly = 0) { MainRepository.getNftDetail(any()) }
    }

    @Test
    fun getNftDetailInfo_storesNetworkDetail() = runTest {
        val fetched = NFTDetail(id = "bayc", name = "BAYC")
        coEvery { MainRepository.getNftDetail("bayc") } returns Result.success(fetched)

        assertEquals(fetched, NFTDetailRepository.getNftDetailInfo("bayc"))
        assertEquals(fetched, NFTDetailRepository.nftDetail?.get("bayc"))
    }

    @Test
    fun getNftPriceVolume_returnsNullWhenChartJsonIsMissing() = runTest {
        coEvery { MainRepository.getNftPriceVolume(any(), any()) } returns Result.failure(IllegalStateException("down"))

        assertNull(NFTDetailRepository.getNftPriceVolume("punks", "7"))
    }

    @Test
    fun getNftPriceVolume_decodesCachedChartJson() = runTest {
        seedCache(CacheUrls.nftPriceVolume("punks", "7"), """{"floor_price_usd":[[1.0,2.0]]}""")

        val result = NFTDetailRepository.getNftPriceVolume("punks", "7")

        assertEquals(2.0, result?.floor_price_usd?.first()?.price)
        assertEquals(2.0, NFTDetailRepository.nftPrices?.get("punks-7")?.floor_price_usd?.first()?.price)
    }

    @Test
    fun getNFTTickers_storesTickerList() = runTest {
        val tickers = listOf(NFTTicker(name = "Blur"))
        coEvery { MainRepository.getNFTTickers("punks") } returns Result.success(NFTTickerResponse(tickers))

        assertEquals(tickers, NFTDetailRepository.getNFTTickers("punks"))
        assertEquals(tickers, NFTDetailRepository.nftTickers?.get("punks"))
    }

    @Test
    fun loadCachedDataForNft_ignoresBlankIds() {
        NFTDetailRepository.loadCachedDataForNft("", "7")

        assertNull(NFTDetailRepository.nftDetail)
    }
}
