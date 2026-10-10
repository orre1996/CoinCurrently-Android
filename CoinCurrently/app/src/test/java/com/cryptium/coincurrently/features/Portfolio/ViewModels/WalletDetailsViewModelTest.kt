package com.cryptium.coincurrently.features.Portfolio.ViewModels

import com.cryptium.coincurrently.core.networking.ResponseModels.AddressImport
import com.cryptium.coincurrently.core.networking.ResponseModels.Toplist
import com.cryptium.coincurrently.core.networking.ResponseModels.TokenInfo
import com.cryptium.coincurrently.features.Portfolio.PortfolioRepository
import com.cryptium.coincurrently.testsupport.MainDispatcherRule
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class WalletDetailsViewModelTest {

    @get:Rule
    val mainDispatcher = MainDispatcherRule()

    @Before
    fun setUp() {
        mockkObject(PortfolioRepository)
        every { PortfolioRepository.portfolioAllCurrencies } returns null
        every { PortfolioRepository.renameWallet(any(), any(), any()) } returns Unit
    }

    @Test
    fun sortCoins_ordersTokensByBalanceTimesPrice() {
        val wallet = AddressImport(
            address = "0xabc",
            balance = "1",
            tokenInfos = listOf(
                TokenInfo("a", "10", "h1", "S", "small"),
                TokenInfo("b", "2", "h2", "L", "large"),
            ),
        )
        val viewModel = WalletDetailsViewModel(wallet, Toplist(id = "ethereum"))
        viewModel.coins = listOf(
            Toplist(id = "small", current_price = 1.0),
            Toplist(id = "large", current_price = 100.0),
        )

        viewModel.sortCoins()

        assertEquals(listOf("large", "small"), viewModel.sortedTokens.map { it.tokenId })
    }

    @Test
    fun toggleHideData_flipsTheFlag() {
        val viewModel = WalletDetailsViewModel(
            AddressImport(address = "bc1", balance = "0"),
            Toplist(id = "bitcoin"),
        )

        assertFalse(viewModel.hideData)
        viewModel.toggleHideData()
        assertTrue(viewModel.hideData)
    }

    @Test
    fun renameWallet_updatesTheLocalNameAndPersistsIt() {
        val wallet = AddressImport(address = "bc1", balance = "1", coinId = "bitcoin")
        val viewModel = WalletDetailsViewModel(wallet, Toplist(id = "bitcoin"))

        viewModel.renameWallet("Cold storage")

        assertEquals("Cold storage", wallet.walletName)
        verify { PortfolioRepository.renameWallet("bitcoin", "bc1", "Cold storage") }
    }
}
