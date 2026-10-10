package com.cryptium.coincurrently.features.ExchangeDetail

import com.cryptium.coincurrently.core.networking.ResponseModels.ExchangePriceModel
import com.cryptium.coincurrently.core.networking.ResponseModels.ExchangeResponse
import com.cryptium.coincurrently.sharedui.models.SortOption
import com.cryptium.coincurrently.testsupport.MainDispatcherRule
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class ExchangeDetailViewModelTest {

    @get:Rule
    val mainDispatcher = MainDispatcherRule()

    @Test
    fun keepsTheExchangePassedIn() {
        val viewModel = ExchangeDetailViewModel(ExchangeResponse(id = "binance", name = "Binance"))

        assertEquals("binance", viewModel.exchangeInfo?.id)
        assertEquals("Binance", viewModel.exchangeInfo?.name)
    }

    @Test
    fun setPrices_readsTheLatestVolume() {
        val viewModel = ExchangeDetailViewModel(ExchangeResponse(id = "binance"))
        viewModel.exchangeVolume = listOf(
            ExchangePriceModel(timestamp = 1.0, price = 2.0),
            ExchangePriceModel(timestamp = 3.0, price = 4.0),
        )

        viewModel.setPrices()
        viewModel.setTimestamps()

        assertEquals(listOf(0.0, 0.0), viewModel.prices)
        assertEquals(listOf(1.0, 3.0), viewModel.timeStamps)
    }

    @Test
    fun setNewSortOption_updatesTheMarketSort() {
        val viewModel = ExchangeDetailViewModel(ExchangeResponse(id = "binance"))

        viewModel.setNewSortOption(SortOption.VolumeDesc)

        assertEquals(SortOption.VolumeDesc, viewModel.selectedSortOption)
    }
}
