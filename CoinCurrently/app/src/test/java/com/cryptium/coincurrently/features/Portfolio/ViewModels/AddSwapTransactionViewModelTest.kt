package com.cryptium.coincurrently.features.Portfolio.ViewModels

import com.cryptium.coincurrently.core.networking.ResponseModels.CryptoDetailInfoModel
import com.cryptium.coincurrently.features.Portfolio.Enums.PortfolioTransactionType
import com.cryptium.coincurrently.features.Portfolio.PortfolioRepository
import com.cryptium.coincurrently.testsupport.MainDispatcherRule
import io.mockk.every
import io.mockk.mockkObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class AddSwapTransactionViewModelTest {

    @get:Rule
    val mainDispatcher = MainDispatcherRule()

    @Before
    fun setUp() {
        mockkObject(PortfolioRepository)
        every { PortfolioRepository.portfolioModels } returns null
    }

    @Test
    fun defaultsTheDestinationToEthereumWhenSellingBitcoin() {
        val viewModel = AddSwapTransactionViewModel(
            PortfolioTransactionType.Swap,
            CryptoDetailInfoModel(id = "bitcoin", symbol = "btc"),
            null,
        )

        assertEquals("ethereum" to "ETH", viewModel.toInformation)
    }

    @Test
    fun defaultsTheDestinationToBitcoinForOtherCoins() {
        val viewModel = AddSwapTransactionViewModel(
            PortfolioTransactionType.Swap,
            CryptoDetailInfoModel(id = "ethereum", symbol = "eth"),
            null,
        )

        assertEquals("bitcoin" to "BTC", viewModel.toInformation)
    }

    @Test
    fun isInputValid_requiresBothAmountsAndAShortNote() {
        val viewModel = AddSwapTransactionViewModel(PortfolioTransactionType.Swap, null, null)

        assertFalse(viewModel.isInputValid)
        viewModel.fromAmount.value = 1.0
        viewModel.toAmount.value = 2.0
        assertTrue(viewModel.isInputValid)
        viewModel.note.value = "n".repeat(201)
        assertFalse(viewModel.isInputValid)
    }
}
