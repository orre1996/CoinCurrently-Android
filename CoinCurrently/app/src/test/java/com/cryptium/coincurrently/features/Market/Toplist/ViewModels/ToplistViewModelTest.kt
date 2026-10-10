package com.cryptium.coincurrently.features.Market.Toplist.ViewModels

import com.cryptium.coincurrently.core.networking.ResponseModels.Toplist
import com.cryptium.coincurrently.features.Market.Toplist.Repositories.ToplistRepository
import com.cryptium.coincurrently.sharedui.models.PriceChange
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

class ToplistViewModelTest {

    @get:Rule
    val mainDispatcher = MainDispatcherRule()

    @Before
    fun setUp() {
        mockkObject(ToplistRepository)
        every { ToplistRepository.toplist } returns mutableMapOf()
        every { ToplistRepository.favorites } returns arrayListOf()
    }

    @Test
    fun getToplist_storesTheFetchedPage() = runTest {
        coEvery { ToplistRepository.getToplist(any(), any(), any()) } returns arrayListOf(Toplist(id = "bitcoin"))
        val viewModel = ToplistViewModel(category = null)

        viewModel.getToplist(SortOption.MktCapDesc, PriceChange.SevenDays)

        assertEquals(listOf("bitcoin"), viewModel.toplist.map { it.id })
    }

    @Test
    fun setNewSortOption_updatesTheSelectionBeforeTheNextFetch() {
        val viewModel = ToplistViewModel(category = null)

        viewModel.setNewSortOption(SortOption.VolumeDesc)
        viewModel.setNewPriceChangeOption(PriceChange.OneDay)

        assertEquals(SortOption.VolumeDesc, viewModel.selectedSortOption)
        assertEquals(PriceChange.OneDay, viewModel.selectedPriceChangeOption)
    }
}
