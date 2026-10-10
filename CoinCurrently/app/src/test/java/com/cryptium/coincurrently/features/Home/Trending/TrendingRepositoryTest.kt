package com.cryptium.coincurrently.features.Home.Trending

import com.cryptium.coincurrently.core.networking.CacheUrls
import com.cryptium.coincurrently.core.networking.MainRepository
import com.cryptium.coincurrently.core.networking.ResponseModels.CoinItem
import com.cryptium.coincurrently.core.networking.ResponseModels.Toplist
import com.cryptium.coincurrently.core.networking.ResponseModels.TrendingCoin
import com.cryptium.coincurrently.core.networking.ResponseModels.TrendingItems
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

class TrendingRepositoryTest {

    @get:Rule
    val mainDispatcher = MainDispatcherRule()

    @Before
    fun setUp() {
        mockkObject(MainRepository)
        RetrofitBuilder.clearResponseCache()
        TrendingRepository.trendingList = null
        TrendingRepository.cachedTrendingCoins = null
    }

    @Test
    fun orderedCachedTrendingCoins_followsTrendingIdOrder() {
        TrendingRepository.trendingList = TrendingItems(
            coins = listOf(
                TrendingCoin(CoinItem(id = "eth")),
                TrendingCoin(CoinItem(id = "btc")),
            ),
        )
        TrendingRepository.cachedTrendingCoins = arrayListOf(Toplist(id = "btc"), Toplist(id = "eth"), Toplist(id = "sol"))

        assertEquals(listOf("eth", "btc"), TrendingRepository.orderedCachedTrendingCoins()?.map { it.id })
    }

    @Test
    fun getTrendingList_returnsCacheWithoutNetwork() = runTest {
        val cached = TrendingItems(coins = listOf(TrendingCoin(CoinItem(id = "sol"))))
        seedCache(CacheUrls.trending(), cached)
        coEvery { MainRepository.getTrendingCoins() } returns Result.failure(IllegalStateException("down"))

        assertEquals("sol", TrendingRepository.getTrendingList()?.coins?.first()?.item?.id)
        coVerify(exactly = 0) { MainRepository.getTrendingCoins() }
    }

    @Test
    fun getTrendingList_storesNetworkItems() = runTest {
        val fetched = TrendingItems(coins = listOf(TrendingCoin(CoinItem(id = "doge"))))
        coEvery { MainRepository.getTrendingCoins() } returns Result.success(fetched)

        assertEquals(fetched, TrendingRepository.getTrendingList())
        assertEquals(fetched, TrendingRepository.trendingList)
    }

    @Test
    fun getTrendingCoins_returnsNullWhenTrendingIdsAreMissing() = runTest {
        coEvery { MainRepository.getTrendingCoins() } returns Result.success(TrendingItems(coins = emptyList()))

        assertNull(TrendingRepository.getTrendingCoins())
    }

    @Test
    fun updateFromToplist_replacesMatchingCachedCoins() {
        TrendingRepository.cachedTrendingCoins = arrayListOf(Toplist(id = "btc", current_price = 1.0))

        TrendingRepository.updateFromToplist(arrayListOf(Toplist(id = "btc", current_price = 2.0)))

        assertEquals(2.0, TrendingRepository.cachedTrendingCoins?.first()?.current_price)
    }
}
