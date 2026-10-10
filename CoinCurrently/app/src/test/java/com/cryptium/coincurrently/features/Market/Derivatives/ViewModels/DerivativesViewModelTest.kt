package com.cryptium.coincurrently.features.Market.Derivatives.ViewModels

import com.cryptium.coincurrently.core.networking.ResponseModels.CryptoDetailInfoModel
import com.cryptium.coincurrently.core.networking.ResponseModels.Derivative
import com.cryptium.coincurrently.core.persistence.CurrencyRepository
import com.cryptium.coincurrently.features.Market.Derivatives.Repositories.DerivativesRepository
import com.cryptium.coincurrently.sharedui.models.SortOption
import com.cryptium.coincurrently.testsupport.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockkObject
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class DerivativesViewModelTest {

    @get:Rule
    val mainDispatcher = MainDispatcherRule()

    @Before
    fun setUp() {
        mockkObject(DerivativesRepository, CurrencyRepository)
        every { DerivativesRepository.derivatives } returns mutableMapOf()
        every { CurrencyRepository.bitcoinData } returns null
    }

    @Test
    fun getDerivatives_clearsTheListWhenTheSortChangesAndKeepsAFailedFetchEmpty() = runTest {
        coEvery { DerivativesRepository.getDerivatives(any()) } returns listOf(Derivative(id = "binance"))
        val viewModel = DerivativesViewModel()
        viewModel.getDerivatives(viewModel.selectedSortOption)
        coEvery { DerivativesRepository.getDerivatives(any()) } returns null

        viewModel.getDerivatives(SortOption.NameAsc)

        assertEquals(SortOption.NameAsc, viewModel.selectedSortOption)
        assertTrue(viewModel.allDerivatives.isEmpty())
    }

    @Test
    fun getBitcoin_storesTheRepositoryCoin() = runTest {
        val bitcoin = CryptoDetailInfoModel(id = "bitcoin")
        coEvery { CurrencyRepository.getBitcoin(any()) } returns bitcoin
        val viewModel = DerivativesViewModel()

        viewModel.getBitcoin()

        assertEquals(bitcoin, viewModel.bitcoin)
    }
}
