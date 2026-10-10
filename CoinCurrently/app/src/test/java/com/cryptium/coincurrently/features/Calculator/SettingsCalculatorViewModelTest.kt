package com.cryptium.coincurrently.features.Calculator

import com.cryptium.coincurrently.core.networking.ResponseModels.CurrencySelectionModel
import com.cryptium.coincurrently.core.networking.ResponseModels.Toplist
import com.cryptium.coincurrently.features.Market.Toplist.Repositories.ToplistRepository
import com.cryptium.coincurrently.testsupport.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockkObject
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SettingsCalculatorViewModelTest {

    @get:Rule
    val mainDispatcher = MainDispatcherRule()

    @Before
    fun setUp() {
        mockkObject(ToplistRepository, CalculatorRepository)
        every { ToplistRepository.smallToplist } returns null
        every { CalculatorRepository.calculatorPrices } returns null
    }

    @Test
    fun getAmount_returnsNullUntilPricesAreLoaded() {
        val viewModel = SettingsCalculatorViewModelImp()

        assertNull(viewModel.getAmount("bitcoin", CurrencySelectionModel()))
    }

    @Test
    fun getAmount_dividesTheInputByTheSelectedCurrencyPrice() = runTest {
        every { CalculatorRepository.calculatorPrices } returns mutableMapOf("bitcoin" to mapOf("usd" to 50.0))
        coEvery { ToplistRepository.getSmallToplist() } returns arrayListOf(Toplist(id = "bitcoin"))
        coEvery { CalculatorRepository.getCalculatorPrices(any()) } returns mutableMapOf("bitcoin" to mapOf("usd" to 50.0))
        val viewModel = SettingsCalculatorViewModelImp()
        viewModel.input = 100.0

        viewModel.getCalculatorPrices()

        assertEquals(2.0, viewModel.getAmount("bitcoin", CurrencySelectionModel())!!, 0.0001)
    }

    @Test
    fun getAmount_treatsAMissingPriceAsZero() = runTest {
        coEvery { ToplistRepository.getSmallToplist() } returns arrayListOf(Toplist(id = "bitcoin"))
        coEvery { CalculatorRepository.getCalculatorPrices(any()) } returns mutableMapOf("ethereum" to mapOf("usd" to 10.0))
        val viewModel = SettingsCalculatorViewModelImp()

        viewModel.getCalculatorPrices()

        assertEquals(Double.POSITIVE_INFINITY, viewModel.getAmount("bitcoin", CurrencySelectionModel())!!, 0.0)
    }
}
