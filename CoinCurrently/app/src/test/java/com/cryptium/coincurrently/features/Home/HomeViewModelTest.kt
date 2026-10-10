package com.cryptium.coincurrently.features.Home

import android.app.Application
import android.content.Context
import com.cryptium.coincurrently.core.extensions.isTablet
import com.cryptium.coincurrently.core.models.CarouselFilter
import com.cryptium.coincurrently.core.networking.ResponseModels.Toplist
import com.cryptium.coincurrently.features.FearGreed.FearGreedRepository
import com.cryptium.coincurrently.features.GasFees.GasFeesRepository
import com.cryptium.coincurrently.features.GlobalMarket.GlobalMarketRepository
import com.cryptium.coincurrently.features.Home.NewCoins.NewCoinsRepository
import com.cryptium.coincurrently.features.Home.Trending.TrendingRepository
import com.cryptium.coincurrently.features.Home.WinnersLosers.WinnersLosersRepository
import com.cryptium.coincurrently.features.Market.Toplist.Repositories.ToplistRepository
import com.cryptium.coincurrently.testsupport.MainDispatcherRule
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class HomeViewModelTest {

    @get:Rule
    val mainDispatcher = MainDispatcherRule()

    @Before
    fun setUp() {
        mockkStatic(Context::isTablet)
        every { any<Context>().isTablet() } returns false
        mockkObject(
            FearGreedRepository,
            GasFeesRepository,
            GlobalMarketRepository,
            TrendingRepository,
            ToplistRepository,
            WinnersLosersRepository,
            NewCoinsRepository,
        )
        every { FearGreedRepository.fearGreed } returns null
        every { GasFeesRepository.cachedGasFees } returns null
        every { GlobalMarketRepository.globalMarket } returns null
        every { TrendingRepository.orderedCachedTrendingCoins() } returns null
        every { ToplistRepository.smallToplist } returns arrayListOf(Toplist(id = "bitcoin"), Toplist(id = "ethereum"))
        every { ToplistRepository.favorites } returns null
        every { WinnersLosersRepository.winners } returns null
        every { WinnersLosersRepository.losers } returns null
        every { NewCoinsRepository.newCoins } returns null
    }

    @Test
    fun getList_returnsTheCarouselThatMatchesTheFilter() {
        val viewModel = HomeViewModelImp(mockk<Application>(relaxed = true))

        assertEquals(listOf("bitcoin", "ethereum"), viewModel.getList(CarouselFilter.TopCoins)?.map { it.id })
        assertNull(viewModel.getList(null))
        assertNull(viewModel.getList(CarouselFilter.Favorites))
    }
}
