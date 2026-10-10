package com.cryptium.coincurrently.core.persistence

import com.cryptium.coincurrently.core.analytics.FirebaseManager
import com.cryptium.coincurrently.core.networking.CacheUrls
import com.cryptium.coincurrently.core.networking.MainRepository
import com.cryptium.coincurrently.core.networking.ResponseModels.CryptoDetailInfoModel
import com.cryptium.coincurrently.core.networking.ResponseModels.PriceVolumeModel
import com.cryptium.coincurrently.core.networking.ResponseModels.TimeCharts
import com.cryptium.coincurrently.core.networking.ResponseModels.Toplist
import com.cryptium.coincurrently.core.networking.RetrofitBuilder
import com.cryptium.coincurrently.testsupport.MainDispatcherRule
import com.cryptium.coincurrently.testsupport.seedCache
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockkObject
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class CurrencyRepositoryTest {

    @get:Rule
    val mainDispatcher = MainDispatcherRule()

    @Before
    fun setUp() {
        mockkObject(MainRepository, FirebaseManager, UserPreferences)
        every { UserPreferences.getSelectedCurrency(any()) } returns com.cryptium.coincurrently.core.networking.ResponseModels.CurrencySelectionModel()
        every { FirebaseManager.getFirebaseToken() } returns ""
        RetrofitBuilder.clearResponseCache()
        CurrencyRepository.cryptoDetail = null
        CurrencyRepository.cryptoPrices = null
        CurrencyRepository.cryptoInfo = null
    }

    @Test
    fun getCryptoDetail_returnsNullWithoutId() = runTest {
        assertNull(CurrencyRepository.getCryptoDetail(null))
    }

    @Test
    fun getCryptoDetail_returnsCacheAndRemembersIt() = runTest {
        val cached = CryptoDetailInfoModel(id = "bitcoin", name = "Bitcoin")
        seedCache(CacheUrls.cryptoDetail("bitcoin", true), cached)
        coEvery { MainRepository.getCryptoDetailInfo(any(), any()) } returns Result.failure(IllegalStateException("down"))

        val result = CurrencyRepository.getCryptoDetail("bitcoin")

        assertEquals("Bitcoin", result?.name)
        assertEquals(cached, CurrencyRepository.cryptoDetail?.get("bitcoin"))
        coVerify(exactly = 0) { MainRepository.getCryptoDetailInfo(any(), any()) }
    }

    @Test
    fun getCryptoDetail_retriesPaidUrlWhenFreeLookupFails() = runTest {
        coEvery { MainRepository.getCryptoDetailInfo("bitcoin", true) } returns Result.failure(IllegalStateException("free"))
        coEvery { MainRepository.getCryptoDetailInfo("bitcoin", false) } returns Result.success(
            CryptoDetailInfoModel(id = "bitcoin", name = "Bitcoin"),
        )

        assertEquals("Bitcoin", CurrencyRepository.getCryptoDetail("bitcoin")?.name)
        coVerify { MainRepository.getCryptoDetailInfo("bitcoin", false) }
    }

    @Test
    fun getPriceVolume_storesChartUnderCoinAndDays() = runTest {
        val model = PriceVolumeModel(prices = arrayListOf(arrayListOf(1.0, 2.0)))
        coEvery { MainRepository.getPriceVolume("bitcoin", "USD", "7", null, true) } returns Result.success(model)

        val result = CurrencyRepository.getPriceVolume("bitcoin", TimeCharts.aWeek)

        assertEquals(model, result)
        assertEquals(model, CurrencyRepository.cryptoPrices?.get("bitcoin-7"))
    }

    @Test
    fun getMultipleCurrencies_returnsNetworkCoins() = runTest {
        val coins = arrayListOf(Toplist(id = "eth"))
        coEvery { MainRepository.getMultipleCurrencies("eth", "usd") } returns Result.success(coins)

        assertEquals(coins, CurrencyRepository.getMultipleCurrencies("eth", "usd"))
    }

    @Test
    fun filterPriceVolume_keepsEveryPointWhenDividerIsOne() {
        val model = PriceVolumeModel(
            prices = arrayListOf(arrayListOf(1.0, 10.0), arrayListOf(2.0, 1.0), arrayListOf(3.0, 5.0)),
            total_volumes = arrayListOf(),
            market_caps = arrayListOf(),
        )

        val filtered = CurrencyRepository.filterPriceVolume(model, divider = 0)

        assertEquals(3, filtered?.prices?.size)
    }

    @Test
    fun filterPriceVolume_dropsInteriorPoints() {
        val prices = ArrayList<ArrayList<Double?>>()
        repeat(4) { index -> prices.add(arrayListOf(index.toDouble(), index.toDouble())) }
        val model = PriceVolumeModel(prices = prices, total_volumes = arrayListOf(), market_caps = arrayListOf())

        val filtered = CurrencyRepository.filterPriceVolume(model, divider = 2)

        assertEquals(listOf(0.0, 2.0, 3.0), filtered?.prices?.map { it.first() })
    }

    @Test
    fun connectSignalR_returnsFalseWithoutAnAuthToken() = runTest {
        assertFalse(CurrencyRepository.connectSignalR { })
    }

    @Test
    fun getCurrencies_returnsFalseForBlankIdsAndReportsSubscribeSuccess() = runTest {
        coEvery { MainRepository.subCoin(any(), any()) } returns Result.success(Unit)

        assertFalse(CurrencyRepository.getCurrencies(listOf("")))
        assertTrue(CurrencyRepository.getCurrency("Bitcoin"))
        coVerify { MainRepository.subCoin(listOf("bitcoin"), listOf("usd")) }
    }
}
