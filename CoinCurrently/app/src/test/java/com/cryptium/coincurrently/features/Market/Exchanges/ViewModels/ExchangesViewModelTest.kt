package com.cryptium.coincurrently.features.Market.Exchanges.ViewModels

import com.cryptium.coincurrently.core.networking.ResponseModels.ExchangeList
import com.cryptium.coincurrently.core.persistence.CurrencyRepository
import com.cryptium.coincurrently.features.Market.Exchanges.Repositories.ExchangesRepository
import com.cryptium.coincurrently.sharedui.models.SortOption
import com.cryptium.coincurrently.testsupport.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockkObject
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ExchangesViewModelTest {

    @get:Rule
    val mainDispatcher = MainDispatcherRule()

    @Before
    fun setUp() {
        mockkObject(ExchangesRepository, CurrencyRepository)
        every { ExchangesRepository.exchanges } returns null
        every { CurrencyRepository.bitcoinData } returns null
    }

    @Test
    fun setNewSortOption_reordersByVolume() = runTest {
        coEvery { ExchangesRepository.getExchanges() } returns listOf(
            exchange("small", volume = 1.0),
            exchange("large", volume = 9.0),
        )
        val viewModel = ExchangesViewModel()
        viewModel.getExchanges()

        viewModel.setNewSortOption(SortOption.VolumeDesc)

        assertEquals(listOf("large", "small"), viewModel.exchanges.map { it.id })
    }

    @Test
    fun setNewSortOption_ordersTrustScoreByRank() = runTest {
        coEvery { ExchangesRepository.getExchanges() } returns listOf(
            exchange("second", rank = 2),
            exchange("first", rank = 1),
        )
        val viewModel = ExchangesViewModel()
        viewModel.selectedSortOption = SortOption.TrustScoreDesc

        viewModel.getExchanges()

        assertEquals(listOf("first", "second"), viewModel.exchanges.map { it.id })
    }

    private fun exchange(id: String, volume: Double = 0.0, rank: Int = 1) = ExchangeList(
        id = id,
        name = id,
        year_established = null,
        country = null,
        url = null,
        image = null,
        has_trading_incentive = null,
        trust_score = null,
        trust_score_rank = rank,
        trade_volume_24h_btc = volume,
        trade_volume_24h_btc_normalized = null,
    )
}
