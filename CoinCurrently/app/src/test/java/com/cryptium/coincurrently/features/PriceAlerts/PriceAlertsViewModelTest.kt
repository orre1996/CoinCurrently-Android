package com.cryptium.coincurrently.features.PriceAlerts

import com.cryptium.coincurrently.core.networking.ResponseModels.AlertCoin
import com.cryptium.coincurrently.core.networking.ResponseModels.AlertDirection
import com.cryptium.coincurrently.core.networking.ResponseModels.CryptoDetailInfoModel
import com.cryptium.coincurrently.core.networking.ResponseModels.Toplist
import com.cryptium.coincurrently.core.persistence.CurrencyRepository
import com.cryptium.coincurrently.features.Market.Toplist.Repositories.ToplistRepository
import com.cryptium.coincurrently.testsupport.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockkObject
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class PriceAlertsViewModelTest {

    @get:Rule
    val mainDispatcher = MainDispatcherRule()

    @Before
    fun setUp() {
        mockkObject(PriceAlertsRepository, ToplistRepository, CurrencyRepository)
        every { ToplistRepository.smallToplist } returns null
        every { PriceAlertsRepository.alertPricesAllCurrencies } returns null
        every { PriceAlertsRepository.priceAlerts } returns null
    }

    @Test
    fun setGroups_ordersAlertsByMarketCapRank() {
        val viewModel = PriceAlertsViewModel()
        viewModel.filteredToplist = mutableListOf(
            Toplist(id = "ethereum", market_cap_rank = 2),
            Toplist(id = "bitcoin", market_cap_rank = 1),
        )
        viewModel.alerts = listOf(alert("ethereum"), alert("bitcoin"))

        viewModel.setGroups()

        assertEquals(listOf("bitcoin", "ethereum"), viewModel.groups)
    }

    @Test
    fun getCoin_returnsNullForAMissingId() {
        val viewModel = PriceAlertsViewModel()

        assertNull(viewModel.getCoin(null))
        assertNull(viewModel.getCoin("bitcoin"))
    }

    @Test
    fun getFilteredToplist_sortsByRankAndLoadsPrices() = runTest {
        coEvery { ToplistRepository.getSmallToplist() } returns arrayListOf(
            Toplist(id = "ethereum", market_cap_rank = 2),
            Toplist(id = "bitcoin", market_cap_rank = 1),
        )
        coEvery { PriceAlertsRepository.getAlertPrices(listOf("bitcoin", "ethereum")) } returns mutableMapOf(
            "bitcoin" to mapOf("usd" to 1.0),
        )
        val viewModel = PriceAlertsViewModel()

        viewModel.getFilteredToplist()

        assertEquals(listOf("bitcoin", "ethereum"), viewModel.filteredToplist?.map { it.id })
        assertEquals(1.0, viewModel.alertPrices?.get("bitcoin")?.get("usd"))
    }

    @Test
    fun getPriceAlerts_usesAnEmptyListWhenTheRepositoryReturnsNothing() = runTest {
        coEvery { PriceAlertsRepository.getAlerts() } returns null
        val viewModel = PriceAlertsViewModel()

        viewModel.getPriceAlerts()

        assertEquals(emptyList<AlertCoin>(), viewModel.alerts)
    }

    @Test
    fun getSelectedCoin_delegatesToTheCurrencyRepository() = runTest {
        val detail = CryptoDetailInfoModel(id = "bitcoin")
        coEvery { CurrencyRepository.getCryptoDetail("bitcoin", any()) } returns detail
        val viewModel = PriceAlertsViewModel()

        assertEquals(detail, viewModel.getSelectedCoin("bitcoin"))
    }

    private fun alert(coinId: String) = AlertCoin(
        id = coinId,
        resetThreshold = null,
        direction = AlertDirection.Above,
        coinId = coinId,
        targetPrice = 1.0,
        preferredCurrency = "USD",
        createdDate = "2026-01-01",
    )
}
