package com.cryptium.coincurrently.features.Home.Trending

import com.cryptium.coincurrently.app.SearchCase
import com.cryptium.coincurrently.core.networking.ResponseModels.CoinItem
import com.cryptium.coincurrently.core.networking.ResponseModels.TrendingCoin
import com.cryptium.coincurrently.core.networking.ResponseModels.TrendingItems
import com.cryptium.coincurrently.core.networking.ResponseModels.TrendingNFT
import com.cryptium.coincurrently.testsupport.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockkObject
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class TrendingViewModelTest {

    @get:Rule
    val mainDispatcher = MainDispatcherRule()

    @Before
    fun setUp() {
        mockkObject(TrendingRepository)
        every { TrendingRepository.trendingList } returns null
    }

    @Test
    fun getTrendingItems_mapsEachSearchCase() {
        val viewModel = TrendingViewModel()
        viewModel.trendingList = TrendingItems(
            coins = listOf(TrendingCoin(CoinItem(id = "bitcoin", name = "Bitcoin"))),
            nfts = listOf(TrendingNFT(id = "punks", name = "Punks")),
        )

        assertEquals(listOf("bitcoin"), viewModel.getTrendingItems(SearchCase.Coins)?.map { it.id })
        assertEquals(listOf("punks"), viewModel.getTrendingItems(SearchCase.NFTs)?.map { it.id })
        assertTrue(viewModel.getTrendingItems(SearchCase.Exchanges)?.isEmpty() == true)
    }

    @Test
    fun getTrendingCoins_storesTheRepositoryList() = runTest {
        val items = TrendingItems(coins = listOf(TrendingCoin(CoinItem(id = "sol"))))
        coEvery { TrendingRepository.getTrendingList() } returns items
        val viewModel = TrendingViewModel()

        viewModel.getTrendingCoins()

        assertEquals(items, viewModel.trendingList)
    }
}
