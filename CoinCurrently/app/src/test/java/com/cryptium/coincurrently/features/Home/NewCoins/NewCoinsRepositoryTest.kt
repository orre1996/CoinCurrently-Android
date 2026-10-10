package com.cryptium.coincurrently.features.Home.NewCoins

import com.cryptium.coincurrently.core.networking.MainRepository
import com.cryptium.coincurrently.core.networking.ResponseModels.CoinsList
import com.cryptium.coincurrently.core.networking.ResponseModels.Toplist
import com.cryptium.coincurrently.core.networking.RetrofitBuilder
import com.cryptium.coincurrently.testsupport.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.mockkObject
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class NewCoinsRepositoryTest {

    @get:Rule
    val mainDispatcher = MainDispatcherRule()

    @Before
    fun setUp() {
        mockkObject(MainRepository)
        RetrofitBuilder.clearResponseCache()
        NewCoinsRepository.newCoins = null
    }

    @Test
    fun getNewCoinIds_returnsNullWhenIdLookupFails() = runTest {
        coEvery { MainRepository.getNewCoins() } returns Result.failure(IllegalStateException("down"))

        assertNull(NewCoinsRepository.getNewCoinIds())
    }

    @Test
    fun getNewCoinIds_requestsNewestIdsFirstAndStoresCoins() = runTest {
        coEvery { MainRepository.getNewCoins() } returns Result.success(
            arrayListOf(
                CoinsList(id = "older", activated_at = 1),
                CoinsList(id = "newer", activated_at = 10),
            ),
        )
        val coins = arrayListOf(Toplist(id = "newer", name = "Newer"), Toplist(id = "older", name = "Older"))
        coEvery { MainRepository.getMultipleCurrencies("newer,older", any()) } returns Result.success(coins)

        val result = NewCoinsRepository.getNewCoinIds()

        assertEquals(listOf("newer", "older"), result?.map { it.id })
        assertEquals(coins, NewCoinsRepository.newCoins)
    }

    @Test
    fun updateFromToplist_replacesMatchingCachedCoins() {
        NewCoinsRepository.newCoins = arrayListOf(Toplist(id = "a", name = "Old"), Toplist(id = "b", name = "Keep"))

        NewCoinsRepository.updateFromToplist(arrayListOf(Toplist(id = "a", name = "New")))

        assertEquals(listOf("New", "Keep"), NewCoinsRepository.newCoins?.map { it.name })
    }

    @Test
    fun updateFromToplist_doesNothingWhenCacheIsEmpty() {
        NewCoinsRepository.newCoins = null

        NewCoinsRepository.updateFromToplist(arrayListOf(Toplist(id = "a")))

        assertNull(NewCoinsRepository.newCoins)
    }
}
