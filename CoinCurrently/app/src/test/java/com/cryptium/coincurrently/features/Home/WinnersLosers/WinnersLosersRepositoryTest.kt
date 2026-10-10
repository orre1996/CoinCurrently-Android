package com.cryptium.coincurrently.features.Home.WinnersLosers

import com.cryptium.coincurrently.core.networking.MainRepository
import com.cryptium.coincurrently.core.networking.ResponseModels.Toplist
import com.cryptium.coincurrently.core.networking.ResponseModels.WinnerLoser
import com.cryptium.coincurrently.core.networking.ResponseModels.WinnersLosers
import com.cryptium.coincurrently.core.networking.RetrofitBuilder
import com.cryptium.coincurrently.sharedui.models.PriceChange
import com.cryptium.coincurrently.testsupport.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.mockkObject
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class WinnersLosersRepositoryTest {

    @get:Rule
    val mainDispatcher = MainDispatcherRule()

    @Before
    fun setUp() {
        mockkObject(MainRepository)
        RetrofitBuilder.clearResponseCache()
        WinnersLosersRepository.winners = null
        WinnersLosersRepository.losers = null
    }

    @Test
    fun getWinners_loadsIdsThenStoresSortedCoins() = runTest {
        coEvery { MainRepository.getWinnersLosers(any(), "7d") } returns Result.success(
            WinnersLosers(
                top_gainers = listOf(WinnerLoser("pepe"), WinnerLoser("bitcoin")),
                top_losers = listOf(WinnerLoser("doge")),
            ),
        )
        coEvery { MainRepository.getMultipleCurrencies("pepe,bitcoin", any()) } returns Result.success(
            arrayListOf(Toplist(id = "bitcoin"), Toplist(id = "pepe")),
        )

        val winners = WinnersLosersRepository.getWinners(PriceChange.SevenDays)

        assertEquals(setOf("bitcoin", "pepe"), winners?.map { it.id }?.toSet())
        assertEquals(winners, WinnersLosersRepository.winners)
    }

    @Test
    fun getLosers_returnsNullWhenIdLookupFails() = runTest {
        coEvery { MainRepository.getWinnersLosers(any(), any()) } returns Result.failure(IllegalStateException("down"))
        coEvery { MainRepository.getMultipleCurrencies(any(), any()) } returns Result.failure(IllegalStateException("down"))

        assertNull(WinnersLosersRepository.getLosers(PriceChange.SevenDays))
    }

    @Test
    fun hasCachedWinnerCoinRows_isFalseWithoutAStoredUrl() {
        WinnersLosersRepository.winners = arrayListOf(Toplist(id = "bitcoin"))

        assertFalse(WinnersLosersRepository.hasCachedWinnerCoinRows(PriceChange.SevenDays))
        assertFalse(WinnersLosersRepository.hasCachedLoserCoinRows(PriceChange.SevenDays))
    }
}
