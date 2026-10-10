package com.cryptium.coincurrently.features.DerivativeDetail

import com.cryptium.coincurrently.core.networking.ResponseModels.Derivative
import com.cryptium.coincurrently.core.networking.ResponseModels.DerivativeDetailModel
import com.cryptium.coincurrently.core.persistence.CurrencyRepository
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

class DerivativeDetailsViewModelTest {

    @get:Rule
    val mainDispatcher = MainDispatcherRule()

    @Before
    fun setUp() {
        mockkObject(CurrencyRepository, DerivativeDetailRepository)
        every { CurrencyRepository.bitcoinData } returns null
    }

    @Test
    fun getDerivatives_splitsPerpetualsAndFutures() = runTest {
        coEvery { DerivativeDetailRepository.getDerivativeDetail("binance") } returns Derivative(
            id = "binance",
            tickers = listOf(
                DerivativeDetailModel(symbol = "perp", contract_type = "perpetual", open_interest_usd = 2.0),
                DerivativeDetailModel(symbol = "future", contract_type = "futures", open_interest_usd = 1.0),
            ),
        )
        val viewModel = DerivativeDetailsViewModel(Derivative(id = "binance"))

        viewModel.getDerivatives()

        assertEquals(listOf("perp"), viewModel.perpetualList.map { it.symbol })
        assertEquals(listOf("future"), viewModel.futuresList.map { it.symbol })
    }

    @Test
    fun setSortOption_reordersByName() = runTest {
        coEvery { DerivativeDetailRepository.getDerivativeDetail(any()) } returns Derivative(
            id = "binance",
            tickers = listOf(
                DerivativeDetailModel(symbol = "bbb", contract_type = "perpetual"),
                DerivativeDetailModel(symbol = "aaa", contract_type = "perpetual"),
            ),
        )
        val viewModel = DerivativeDetailsViewModel(Derivative(id = "binance"))
        viewModel.getDerivatives()

        viewModel.setSortOption(SortOption.NameAsc)

        assertEquals(listOf("aaa", "bbb"), viewModel.perpetualList.map { it.symbol })
    }
}
