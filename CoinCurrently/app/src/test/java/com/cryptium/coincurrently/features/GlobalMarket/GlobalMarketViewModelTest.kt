package com.cryptium.coincurrently.features.GlobalMarket

import com.cryptium.coincurrently.core.models.ChartTypes
import com.cryptium.coincurrently.core.networking.ResponseModels.GlobalMarketChartData
import com.cryptium.coincurrently.core.networking.ResponseModels.GlobalMarketData
import com.cryptium.coincurrently.testsupport.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockkObject
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class GlobalMarketViewModelTest {

    @get:Rule
    val mainDispatcher = MainDispatcherRule()

    @Before
    fun setUp() {
        mockkObject(GlobalMarketRepository)
        every { GlobalMarketRepository.globalMarket } returns null
        every { GlobalMarketRepository.loadCachedDataForGlobalChart(any()) } returns Unit
        every { GlobalMarketRepository.globalChart } returns mutableMapOf()
    }

    @Test
    fun setChartType_treatsAPriceChartAsMarketCap() {
        val viewModel = GlobalMarketViewModelImp()

        viewModel.setChartType(ChartTypes.PRICE)

        assertEquals(ChartTypes.MKTCAP, viewModel.selectedChartType)
    }

    @Test
    fun refresh_flagsAnErrorWhenTheChartIsMissing() = runTest {
        coEvery { GlobalMarketRepository.getGlobalChart(any()) } returns null
        val viewModel = GlobalMarketViewModelImp()

        viewModel.refresh()

        assertTrue(viewModel.showMainPriceError)
        assertEquals(false, viewModel.mainReloading)
    }

    @Test
    fun getGlobalMarket_keepsThePreviousSnapshotWhenTheFetchFails() = runTest {
        val cached = GlobalMarketData(active_cryptocurrencies = 12)
        every { GlobalMarketRepository.globalMarket } returns cached
        coEvery { GlobalMarketRepository.getGlobalMarket() } returns null
        val viewModel = GlobalMarketViewModelImp()

        viewModel.getGlobalMarket()

        assertEquals(12, viewModel.globalMarket?.active_cryptocurrencies)
    }

    @Test
    fun marketCapChangePercent_isNullUntilTwoPointsExist() {
        val viewModel = GlobalMarketViewModelImp()
        viewModel.globalMarketChart = GlobalMarketChartData(
            market_cap = arrayListOf(arrayListOf(1.0, 10.0)),
        )

        assertNull(viewModel.marketCapChangePercent)
        assertEquals(listOf(10.0), viewModel.marketCapPrices)
    }
}
