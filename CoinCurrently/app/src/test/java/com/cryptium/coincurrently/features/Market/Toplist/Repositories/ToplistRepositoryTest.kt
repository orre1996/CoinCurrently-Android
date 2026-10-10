package com.cryptium.coincurrently.features.Market.Toplist.Repositories

import com.cryptium.coincurrently.core.networking.CacheUrls
import com.cryptium.coincurrently.core.networking.MainRepository
import com.cryptium.coincurrently.core.networking.ResponseModels.Toplist
import com.cryptium.coincurrently.core.networking.RetrofitBuilder
import com.cryptium.coincurrently.core.persistence.UserPreferences
import com.cryptium.coincurrently.sharedui.models.PriceChange
import com.cryptium.coincurrently.sharedui.models.SortOption
import com.cryptium.coincurrently.testsupport.MainDispatcherRule
import com.cryptium.coincurrently.testsupport.seedCache
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockkObject
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ToplistRepositoryTest {

    @get:Rule
    val mainDispatcher = MainDispatcherRule()

    @Before
    fun setUp() {
        mockkObject(MainRepository, UserPreferences)
        every { UserPreferences.getSelectedCurrency(any()) } returns com.cryptium.coincurrently.core.networking.ResponseModels.CurrencySelectionModel()
        every { UserPreferences.getFavorites() } returns emptyList()
        every { UserPreferences.setFavorites(any()) } returns Unit
        RetrofitBuilder.clearResponseCache()
        ToplistRepository.smallToplist = null
        ToplistRepository.favorites = arrayListOf()
        ToplistRepository.toplist.clear()
    }

    @Test
    fun getSmallToplist_returnsCacheWithoutNetwork() = runTest {
        val cached = arrayListOf(Toplist(id = "bitcoin"))
        seedCache(CacheUrls.smallToplist("usd"), cached)
        coEvery { MainRepository.getToplist(any(), any(), any(), any()) } returns Result.failure(IllegalStateException("down"))

        assertEquals(cached, ToplistRepository.getSmallToplist())
        assertEquals(cached, ToplistRepository.smallToplist)
        coVerify(exactly = 0) { MainRepository.getToplist(any(), any(), any(), any()) }
    }

    @Test
    fun getSmallToplist_storesNetworkPage() = runTest {
        val fetched = arrayListOf(Toplist(id = "ethereum"))
        coEvery { MainRepository.getToplist("usd", SortOption.MktCapDesc, 25, null) } returns Result.success(fetched)

        assertEquals(fetched, ToplistRepository.getSmallToplist())
    }

    @Test
    fun getToplist_returnsCachedPageForMarketCapSort() = runTest {
        val cached = arrayListOf(Toplist(id = "solana"))
        seedCache(CacheUrls.toplist("usd", SortOption.MktCapDesc, 250, null), cached)

        val result = ToplistRepository.getToplist(SortOption.MktCapDesc, PriceChange.SevenDays, null)

        assertEquals("solana", result?.first()?.id)
    }

    @Test
    fun favouriteButtonPressed_togglesMembership() {
        val bitcoin = Toplist(id = "bitcoin")

        val added = ToplistRepository.favouriteButtonPressed(bitcoin)
        assertEquals(listOf("bitcoin"), added?.map { it.id })

        val removed = ToplistRepository.favouriteButtonPressed(bitcoin)
        assertEquals(emptyList<String>(), removed?.map { it.id })
    }

    @Test
    fun getFavorites_returnsNullWhenNothingIsSaved() = runTest {
        assertNull(ToplistRepository.getFavorites())
    }

    @Test
    fun getFavorites_mergesSavedIdsFromToplistAndFallbackLookup() = runTest {
        every { UserPreferences.getFavorites() } returns listOf("bitcoin", "obscure")
        coEvery { MainRepository.getToplist(any(), any(), any(), any()) } returns Result.success(
            arrayListOf(Toplist(id = "bitcoin", name = "Bitcoin")),
        )
        coEvery { MainRepository.getMultipleCurrencies("obscure", any()) } returns Result.success(
            arrayListOf(Toplist(id = "obscure", name = "Obscure")),
        )

        val favorites = ToplistRepository.getFavorites()

        assertEquals(listOf("bitcoin", "obscure"), favorites?.map { it.id })
    }

    @Test
    fun getCoin_returnsMatchingCachedCoin() = runTest {
        seedCache(CacheUrls.getMultipleCoins("bitcoin", "usd"), arrayListOf(Toplist(id = "bitcoin", name = "Bitcoin")))
        coEvery { MainRepository.getMultipleCurrencies(any(), any()) } returns Result.failure(IllegalStateException("down"))

        assertEquals("Bitcoin", ToplistRepository.getCoin("bitcoin")?.name)
        coVerify(exactly = 0) { MainRepository.getMultipleCurrencies(any(), any()) }
    }

    @Test
    fun updateFromToplist_replacesFavoriteWithFresherCoin() {
        ToplistRepository.toplist["test"] = com.cryptium.coincurrently.sharedui.models.CachedData(
            arrayListOf(Toplist(id = "bitcoin", current_price = 1.0)),
            java.util.Date(),
        )
        ToplistRepository.favorites = arrayListOf(Toplist(id = "bitcoin", current_price = 1.0))

        ToplistRepository.updateFromToplist(arrayListOf(Toplist(id = "bitcoin", current_price = 9.0)))

        assertEquals(9.0, ToplistRepository.favorites?.first()?.current_price)
    }
}
