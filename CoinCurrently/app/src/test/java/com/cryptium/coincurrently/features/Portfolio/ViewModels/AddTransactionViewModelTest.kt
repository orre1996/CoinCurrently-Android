package com.cryptium.coincurrently.features.Portfolio.ViewModels

import android.content.Context
import com.cryptium.coincurrently.core.networking.ResponseModels.CryptoDetailInfoModel
import com.cryptium.coincurrently.features.Portfolio.Enums.PortfolioTransactionType
import com.cryptium.coincurrently.features.Portfolio.PortfolioRepository
import com.cryptium.coincurrently.testsupport.MainDispatcherRule
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class AddTransactionViewModelTest {

    @get:Rule
    val mainDispatcher = MainDispatcherRule()

    @Before
    fun setUp() {
        mockkObject(PortfolioRepository)
        every { PortfolioRepository.portfolioModels } returns null
    }

    @Test
    fun isInputValid_requiresAPositiveAmountAndAShortNote() {
        val viewModel = viewModel()

        assertFalse(viewModel.isInputValid)
        viewModel.amount.value = 1.5
        assertTrue(viewModel.isInputValid)
        viewModel.note.value = "x".repeat(201)
        assertFalse(viewModel.isInputValid)
    }

    @Test
    fun isInputValid_forImportsRequiresAnAddressInsteadOfAnAmount() {
        val viewModel = viewModel(PortfolioTransactionType.ImportAddress)

        viewModel.amount.value = 1.0
        assertFalse(viewModel.isInputValid)
        viewModel.address.value = "bc1q"
        assertTrue(viewModel.isInputValid)
    }

    @Test
    fun addTransaction_returnsNullWithoutAnAmount() {
        val viewModel = viewModel()

        assertNull(viewModel.addTransaction(mockk<Context>(relaxed = true)))
    }

    @Test
    fun addTransaction_returnsNullWhenTheCoinHasNoId() {
        val viewModel = viewModel()
        viewModel.amount.value = 2.0

        assertNull(viewModel.addTransaction(mockk<Context>(relaxed = true)))
    }

    private fun viewModel(
        type: PortfolioTransactionType = PortfolioTransactionType.Buy,
    ) = AddTransactionViewModel(
        transactionType = type,
        currencyDetail = CryptoDetailInfoModel(name = "Bitcoin"),
        transaction = null,
    )
}
