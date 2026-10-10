package com.cryptium.coincurrently.features.Treasuries.ViewModels

import com.cryptium.coincurrently.core.networking.ResponseModels.PublicTreasuryHolder
import com.cryptium.coincurrently.core.persistence.CurrencyRepository
import com.cryptium.coincurrently.features.GlobalMarket.GlobalMarketRepository
import com.cryptium.coincurrently.features.Treasuries.Repositories.TreasuryDetailRepository
import com.cryptium.coincurrently.testsupport.MainDispatcherRule
import io.mockk.every
import io.mockk.mockkObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class TreasuryDetailViewModelTest {

    @get:Rule
    val mainDispatcher = MainDispatcherRule()

    @Before
    fun setUp() {
        mockkObject(TreasuryDetailRepository, CurrencyRepository, GlobalMarketRepository)
        every { TreasuryDetailRepository.getCachedTreasuryDetail(any()) } returns null
        every { TreasuryDetailRepository.getCachedHoldingChart(any(), any(), any()) } returns null
        every { TreasuryDetailRepository.getCachedTransactionHistory(any(), any()) } returns null
        every { CurrencyRepository.bitcoinData } returns null
        every { GlobalMarketRepository.coinsList } returns null
    }

    @Test
    fun usesTheHolderIdAsTheEntityId() {
        val viewModel = TreasuryDetailViewModel(
            PublicTreasuryHolder(id = "microstrategy", name = "Strategy"),
            coinId = "bitcoin",
        )

        assertEquals("microstrategy", viewModel.entityId)
        assertEquals("bitcoin", viewModel.coinId)
    }

    @Test
    fun fallsBackToANameDerivedEntityId() {
        val viewModel = TreasuryDetailViewModel(
            PublicTreasuryHolder(name = "United States"),
            coinId = "ethereum",
        )

        assertEquals("unitedstates", viewModel.entityId)
    }

    @Test
    fun toggleCurrencyType_flipsTheFiatFlag() {
        val viewModel = TreasuryDetailViewModel(PublicTreasuryHolder(id = "mstr"), "bitcoin")

        val before = viewModel.isChartFiat
        viewModel.toggleCurrencyType()

        assertEquals(!before, viewModel.isChartFiat)
        assertTrue(viewModel.prices.isEmpty())
    }
}
