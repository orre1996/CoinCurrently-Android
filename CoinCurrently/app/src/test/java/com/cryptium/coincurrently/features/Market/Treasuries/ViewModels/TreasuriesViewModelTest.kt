package com.cryptium.coincurrently.features.Market.Treasuries.ViewModels

import com.cryptium.coincurrently.core.networking.ResponseModels.PublicTreasuryHolder
import com.cryptium.coincurrently.core.networking.ResponseModels.PublicTreasuryModel
import com.cryptium.coincurrently.core.networking.ResponseModels.Toplist
import com.cryptium.coincurrently.core.networking.TreasuryEntity
import com.cryptium.coincurrently.core.persistence.CurrencyRepository
import com.cryptium.coincurrently.features.Market.Toplist.Repositories.ToplistRepository
import com.cryptium.coincurrently.features.Market.Treasuries.Models.TreasuryEntityFilter
import com.cryptium.coincurrently.features.Market.Treasuries.Repositories.PublicTreasuryRepository
import com.cryptium.coincurrently.testsupport.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockkObject
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class TreasuriesViewModelTest {

    @get:Rule
    val mainDispatcher = MainDispatcherRule()

    @Before
    fun setUp() {
        mockkObject(PublicTreasuryRepository, ToplistRepository, CurrencyRepository)
        every { CurrencyRepository.bitcoinData } returns null
        coEvery { ToplistRepository.getSmallToplist() } returns arrayListOf(Toplist(id = "bitcoin", symbol = "btc"))
        coEvery { PublicTreasuryRepository.ensureCacheLoaded(any(), any()) } returns Unit
        every { PublicTreasuryRepository.getCachedTreasury(any(), any()) } returns null
    }

    @Test
    fun setStartData_selectsBitcoinFromTheSmallToplist() = runTest {
        val viewModel = TreasuriesViewModel()

        viewModel.setStartData()

        assertEquals("bitcoin", viewModel.selectedCoin?.id)
        assertEquals(listOf("bitcoin"), viewModel.availableCoins.map { it.id })
    }

    @Test
    fun getTreasuries_mergesCompaniesAndGovernments() = runTest {
        coEvery { PublicTreasuryRepository.getPublicTreasury(TreasuryEntity.Companies, "bitcoin") } returns PublicTreasuryModel(
            companies = listOf(PublicTreasuryHolder(name = "Strategy", total_holdings = 1.0)),
        )
        coEvery { PublicTreasuryRepository.getPublicTreasury(TreasuryEntity.Governments, "bitcoin") } returns PublicTreasuryModel(
            governments = listOf(PublicTreasuryHolder(name = "USA", total_holdings = 2.0)),
        )
        val viewModel = TreasuriesViewModel()
        viewModel.setStartData()

        viewModel.getTreasuries()

        assertEquals(listOf("Strategy", "USA"), viewModel.holders.map { it.holder.name })
    }

    @Test
    fun setEntityFilter_keepsOnlyTheRequestedKind() = runTest {
        coEvery { PublicTreasuryRepository.getPublicTreasury(TreasuryEntity.Companies, any()) } returns PublicTreasuryModel(
            companies = listOf(PublicTreasuryHolder(name = "Strategy")),
        )
        coEvery { PublicTreasuryRepository.getPublicTreasury(TreasuryEntity.Governments, any()) } returns PublicTreasuryModel(
            governments = listOf(PublicTreasuryHolder(name = "USA")),
        )
        val viewModel = TreasuriesViewModel()
        viewModel.setStartData()
        viewModel.getTreasuries()

        viewModel.setEntityFilter(TreasuryEntityFilter.Governments)

        assertEquals(listOf("USA"), viewModel.holders.map { it.holder.name })
        assertEquals(TreasuryEntityFilter.Governments, viewModel.selectedEntityFilter)
    }
}
