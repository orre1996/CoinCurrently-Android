package com.cryptium.coincurrently.features.CoinDetail

import com.cryptium.coincurrently.core.models.ChartTypes
import com.cryptium.coincurrently.core.networking.ResponseModels.CryptoDetailInfoModel
import com.cryptium.coincurrently.core.persistence.CurrencyRepository
import com.cryptium.coincurrently.features.GlobalMarket.GlobalMarketRepository
import com.cryptium.coincurrently.features.Portfolio.PortfolioRepository
import com.cryptium.coincurrently.features.PriceAlerts.PriceAlertsRepository
import com.cryptium.coincurrently.testsupport.MainDispatcherRule
import io.mockk.every
import io.mockk.mockkObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class CryptoDetailViewModelTest {

    @get:Rule
    val mainDispatcher = MainDispatcherRule()

    @Before
    fun setUp() {
        mockkObject(CurrencyRepository, PortfolioRepository, PriceAlertsRepository, GlobalMarketRepository)
        every { CurrencyRepository.loadCachedDataForCoin(any(), any()) } returns Unit
        every { CurrencyRepository.cryptoDetail } returns null
        every { CurrencyRepository.cryptoPrices } returns null
        every { CurrencyRepository.cryptoCandles } returns null
        every { PortfolioRepository.portfolio } returns null
        every { PriceAlertsRepository.priceAlerts } returns null
        every { GlobalMarketRepository.globalMarket } returns null
    }

    @Test
    fun setChartType_storesTheRequestedType() {
        val viewModel = CryptoDetailViewModel(CryptoDetailInfoModel(id = "bitcoin", name = "Bitcoin"))

        viewModel.setChartType(ChartTypes.VOLUME)

        assertEquals(ChartTypes.VOLUME, viewModel.currentChartType.value)
    }

    @Test
    fun toggleHideData_flipsThePortfolioFlag() {
        val viewModel = CryptoDetailViewModel(CryptoDetailInfoModel(id = "bitcoin"))

        assertFalse(viewModel.hidePortfolioData)
        viewModel.toggleHideData()
        assertTrue(viewModel.hidePortfolioData)
    }

    @Test
    fun changePercent_isNullWithoutPrices() {
        val viewModel = CryptoDetailViewModel(null)

        assertEquals(null, viewModel.changePercent)
        assertEquals(null, viewModel.cryptoInfo)
    }
}
