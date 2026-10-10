package com.cryptium.coincurrently.features.Market.NFT.ViewModels

import com.cryptium.coincurrently.core.networking.ResponseModels.FloorPrice
import com.cryptium.coincurrently.core.networking.ResponseModels.NFTPlatform
import com.cryptium.coincurrently.core.networking.ResponseModels.NFTToplist
import com.cryptium.coincurrently.core.persistence.CurrencyRepository
import com.cryptium.coincurrently.features.GlobalMarket.GlobalMarketRepository
import com.cryptium.coincurrently.features.Market.NFT.Repositories.NFTRepository
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

class NftToplistViewModelTest {

    @get:Rule
    val mainDispatcher = MainDispatcherRule()

    @Before
    fun setUp() {
        mockkObject(NFTRepository, CurrencyRepository, GlobalMarketRepository)
        every { NFTRepository.getCachedToplist(any(), any()) } returns null
        every { NFTRepository.cachedPlatforms } returns null
        every { CurrencyRepository.bitcoinData } returns null
        every { GlobalMarketRepository.coinsList } returns null
    }

    @Test
    fun getNftToplist_clearsTheListWhenTheSortChangesAndKeepsAFailedFetchEmpty() = runTest {
        val platform = NFTPlatform(id = "all", name = "All Chains")
        coEvery { NFTRepository.getToplist(any(), any()) } returns listOf(
            NFTToplist(id = "cheap", floor_price = FloorPrice(usd = 1.0)),
            NFTToplist(id = "expensive", floor_price = FloorPrice(usd = 9.0)),
        )
        val viewModel = NftToplistViewModel()
        viewModel.getNftToplist(platform, SortOption.FloorPriceDesc)
        coEvery { NFTRepository.getToplist(any(), any()) } returns null

        viewModel.getNftToplist(platform, SortOption.FloorPriceAsc)

        assertEquals(SortOption.FloorPriceAsc, viewModel.selectedSortOption)
        assertEquals(emptyList<String>(), viewModel.nftToplist.map { it.id })
    }

    @Test
    fun getNFTPlatforms_putsAllChainsFirst() = runTest {
        coEvery { NFTRepository.getNFTPlatforms() } returns listOf(
            NFTPlatform(id = "solana", name = "Solana"),
            NFTPlatform(id = "ethereum", name = "Ethereum"),
        )
        val viewModel = NftToplistViewModel()

        viewModel.getNFTPlatforms()

        assertEquals(listOf("all", "ethereum", "solana"), viewModel.nftPlatforms?.map { it.id })
    }

    @Test
    fun getChainSymbol_returnsEmptyUntilCoinsAreLoaded() {
        val viewModel = NftToplistViewModel()

        assertEquals("", viewModel.getChainSymbol("ethereum"))
        assertEquals("", viewModel.getChainSymbol(null))
    }
}
