package com.cryptium.coincurrently.features.NFTDetail

import com.cryptium.coincurrently.core.networking.ResponseModels.NFTToplist
import com.cryptium.coincurrently.sharedui.models.SortOption
import com.cryptium.coincurrently.testsupport.MainDispatcherRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

class NFTDetailViewModelTest {

    @get:Rule
    val mainDispatcher = MainDispatcherRule()

    @Test
    fun startsFromTheToplistCoin() {
        val viewModel = NFTDetailViewModel(NFTToplist(id = "punks", name = "Punks"))

        assertEquals("punks", viewModel.nftDetailInfo?.id)
        assertEquals("Punks", viewModel.nftDetailInfo?.name)
    }

    @Test
    fun getPriceMultiplier_isNullWithoutBitcoin() {
        val viewModel = NFTDetailViewModel(NFTToplist(id = "punks"))

        assertNull(viewModel.getPriceMultiplier())
    }

    @Test
    fun setNewSortOption_updatesTheMarketSort() {
        val viewModel = NFTDetailViewModel(NFTToplist(id = "punks"))

        viewModel.setNewSortOption(SortOption.FloorPriceAsc)

        assertEquals(SortOption.FloorPriceAsc, viewModel.selectedSortOption)
    }
}
