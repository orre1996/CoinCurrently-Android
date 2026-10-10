package com.cryptium.coincurrently.features.Market.NFT.Repositories

import com.cryptium.coincurrently.core.networking.CacheUrls
import com.cryptium.coincurrently.core.networking.MainRepository
import com.cryptium.coincurrently.core.networking.ResponseModels.NFTPlatform
import com.cryptium.coincurrently.core.networking.ResponseModels.NFTToplist
import com.cryptium.coincurrently.core.networking.RetrofitBuilder
import com.cryptium.coincurrently.sharedui.models.SortOption
import com.cryptium.coincurrently.testsupport.MainDispatcherRule
import com.cryptium.coincurrently.testsupport.seedCache
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockkObject
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class NFTRepositoryTest {

    @get:Rule
    val mainDispatcher = MainDispatcherRule()

    private val sort = SortOption.nftOptions.first()

    @Before
    fun setUp() {
        mockkObject(MainRepository)
        RetrofitBuilder.clearResponseCache()
    }

    @Test
    fun getToplist_returnsCacheForAllPlatforms() = runTest {
        val cached = arrayListOf(NFTToplist(id = "punks"))
        seedCache(CacheUrls.nftToplist(sort, null), cached)
        coEvery { MainRepository.getNftToplist(any(), any()) } returns Result.success(arrayListOf())

        val result = NFTRepository.getToplist(NFTPlatform(id = "all", name = "All"), sort)

        assertEquals("punks", result?.first()?.id)
        assertEquals("punks", NFTRepository.getCachedToplist(sort, null)?.first()?.id)
        coVerify(exactly = 0) { MainRepository.getNftToplist(any(), any()) }
    }

    @Test
    fun getToplist_passesConcretePlatformToNetwork() = runTest {
        val fetched = arrayListOf(NFTToplist(id = "eth-nft"))
        coEvery { MainRepository.getNftToplist(sort, "ethereum") } returns Result.success(fetched)

        val result = NFTRepository.getToplist(NFTPlatform(id = "ethereum"), sort)

        assertEquals(fetched, result)
        assertEquals("eth-nft", NFTRepository.getCachedToplist(sort, "ethereum")?.first()?.id)
    }

    @Test
    fun getNFTPlatforms_storesNetworkPlatforms() = runTest {
        val platforms = arrayListOf(NFTPlatform(id = "ethereum", name = "Ethereum"))
        coEvery { MainRepository.getNftPlatforms() } returns Result.success(platforms)

        assertEquals(platforms, NFTRepository.getNFTPlatforms())
        assertEquals(platforms, NFTRepository.cachedPlatforms)
    }

    @Test
    fun getNFTPlatforms_returnsNullWhenNetworkFails() = runTest {
        coEvery { MainRepository.getNftPlatforms() } returns Result.failure(IllegalStateException("down"))

        assertNull(NFTRepository.getNFTPlatforms())
    }
}
