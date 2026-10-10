package com.cryptium.coincurrently.features.CoinDetail

import com.cryptium.coincurrently.core.models.ChartTypes
import com.cryptium.coincurrently.core.networking.ResponseModels.CryptoDetailInfoModel
import com.cryptium.coincurrently.core.persistence.CurrencyRepository
import com.cryptium.coincurrently.features.Market.Toplist.Repositories.ToplistRepository
import com.cryptium.coincurrently.testsupport.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockkObject
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class CompareCoinsViewModelTest {

    @get:Rule
    val mainDispatcher = MainDispatcherRule()

    @Before
    fun setUp() {
        mockkObject(CurrencyRepository, ToplistRepository)
        every { CurrencyRepository.filterPriceVolume(any(), any()) } answers { firstArg() }
        every { CurrencyRepository.cryptoDetail } returns null
        every { CurrencyRepository.cryptoPrices } returns null
        every { ToplistRepository.toplist } returns mutableMapOf()
        every { ToplistRepository.smallToplist } returns null
    }

    @Test
    fun getPrices_dropsTheLastTwoPointsWhenTheSeriesIsLongEnough() {
        val viewModel = CompareCoinsViewModel(CryptoDetailInfoModel(id = "bitcoin"), null, "ethereum")
        viewModel.prices = listOf(1.0, 2.0, 3.0)

        assertEquals(listOf(1.0), viewModel.getPrices(dropLastTwo = true))
        assertEquals(listOf(1.0, 2.0, 3.0), viewModel.getPrices())
    }

    @Test
    fun setChartType_updatesTheSelectedType() {
        val viewModel = CompareCoinsViewModel(null, null, "ethereum")

        viewModel.setChartType(ChartTypes.VOLUME)

        assertEquals(ChartTypes.VOLUME, viewModel.currentChartType.value)
    }

    @Test
    fun getToplist_replacesTheComparableCoins() = runTest {
        coEvery { ToplistRepository.getToplist(any(), any(), any()) } returns arrayListOf(
            com.cryptium.coincurrently.core.networking.ResponseModels.Toplist(id = "solana"),
        )
        val viewModel = CompareCoinsViewModel(null, null, "ethereum")

        viewModel.getToplist()

        assertEquals(listOf("solana"), viewModel.comparableCoins?.map { it.id })
    }
}
