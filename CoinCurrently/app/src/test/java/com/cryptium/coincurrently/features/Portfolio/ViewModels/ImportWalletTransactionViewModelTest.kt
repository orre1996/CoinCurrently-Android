package com.cryptium.coincurrently.features.Portfolio.ViewModels

import android.content.Context
import com.cryptium.coincurrently.core.networking.ResponseModels.Chain
import com.cryptium.coincurrently.core.networking.ResponseModels.Toplist
import com.cryptium.coincurrently.core.persistence.CurrencyRepository
import com.cryptium.coincurrently.features.Market.Toplist.Repositories.ToplistRepository
import com.cryptium.coincurrently.features.Search.SearchViewModel
import com.cryptium.coincurrently.testsupport.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.mockkObject
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ImportWalletTransactionViewModelTest {

    @get:Rule
    val mainDispatcher = MainDispatcherRule()

    @Before
    fun setUp() {
        mockkObject(ToplistRepository, CurrencyRepository)
    }

    @Test
    fun getToplist_keepsOnlyChainCoins() = runTest {
        coEvery { ToplistRepository.getToplist(any(), any(), any()) } returns arrayListOf(
            Toplist(id = "bitcoin"),
            Toplist(id = "dogecoin"),
            Toplist(id = "obscure"),
        )
        val viewModel = ImportWalletTransactionViewModel()

        viewModel.getToplist()

        assertEquals(listOf("bitcoin", "dogecoin"), viewModel.chainCoins?.map { it.id })
    }

    @Test
    fun importWallet_showsAnErrorWhenTheCoinCannotBeLoaded() = runTest {
        coEvery { CurrencyRepository.getCryptoDetail(any(), any()) } returns null
        val viewModel = ImportWalletTransactionViewModel()
        viewModel.address.value = "bc1q"
        viewModel.selectedChain.value = Chain.BTC

        assertNull(viewModel.importWallet(mockk<Context>(relaxed = true)))
        assertTrue(viewModel.showImportError)
        assertEquals(false, viewModel.isImporting)
    }

    @Test
    fun factory_rejectsUnknownTypes() {
        assertThrows(IllegalArgumentException::class.java) {
            ImportWalletTransactionViewModelFactory().create(SearchViewModel::class.java)
        }
    }
}
