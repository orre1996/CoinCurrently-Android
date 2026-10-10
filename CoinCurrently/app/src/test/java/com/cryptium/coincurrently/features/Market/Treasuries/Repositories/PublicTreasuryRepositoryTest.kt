package com.cryptium.coincurrently.features.Market.Treasuries.Repositories

import com.cryptium.coincurrently.core.networking.CacheUrls
import com.cryptium.coincurrently.core.networking.MainRepository
import com.cryptium.coincurrently.core.networking.ResponseModels.PublicTreasuryHolder
import com.cryptium.coincurrently.core.networking.ResponseModels.PublicTreasuryModel
import com.cryptium.coincurrently.core.networking.ResponseModels.TreasuryEntityListItem
import com.cryptium.coincurrently.core.networking.RetrofitBuilder
import com.cryptium.coincurrently.core.networking.TreasuryEntity
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

class PublicTreasuryRepositoryTest {

    @get:Rule
    val mainDispatcher = MainDispatcherRule()

    @Before
    fun setUp() {
        mockkObject(MainRepository)
        RetrofitBuilder.clearResponseCache()
        PublicTreasuryRepository.cachedPublicTreasury.clear()
    }

    @Test
    fun getPublicTreasury_returnsFreshCacheWithoutNetwork() = runTest {
        val cached = PublicTreasuryModel(total_holdings = 10.0, companies = listOf(PublicTreasuryHolder(name = "Strategy")))
        seedCache(CacheUrls.publicTreasury(TreasuryEntity.Companies, "cachecoin"), cached)
        coEvery { MainRepository.getPublicTreasury(any(), any(), any(), any()) } returns Result.failure(IllegalStateException("down"))

        val result = PublicTreasuryRepository.getPublicTreasury(TreasuryEntity.Companies, "cachecoin")

        assertEquals(10.0, result?.total_holdings)
        assertEquals(cached, PublicTreasuryRepository.getCachedTreasury(TreasuryEntity.Companies, "cachecoin"))
        coVerify(exactly = 0) { MainRepository.getPublicTreasury(any(), any(), any(), any()) }
    }

    @Test
    fun getPublicTreasury_mergesEntityIdBySymbol() = runTest {
        coEvery { MainRepository.getPublicTreasury(TreasuryEntity.Companies, "bitcoin", 1, 250) } returns Result.success(
            PublicTreasuryModel(companies = listOf(PublicTreasuryHolder(name = "Strategy", symbol = "mstr"))),
        )
        coEvery { MainRepository.getTreasuryEntitiesList(TreasuryEntity.Companies, 1, 250) } returns Result.success(
            listOf(TreasuryEntityListItem(id = "microstrategy", symbol = "MSTR", name = "Strategy Inc")),
        )

        val result = PublicTreasuryRepository.getPublicTreasury(TreasuryEntity.Companies, "bitcoin")

        assertEquals("microstrategy", result?.companies?.first()?.id)
    }

    @Test
    fun getPublicTreasury_matchesNameAndCountryWhenSymbolsDiffer() = runTest {
        coEvery { MainRepository.getPublicTreasury(TreasuryEntity.Governments, "bitcoin", 1, 250) } returns Result.success(
            PublicTreasuryModel(
                governments = listOf(PublicTreasuryHolder(name = "United States", country = "us")),
            ),
        )
        coEvery { MainRepository.getTreasuryEntitiesList(TreasuryEntity.Governments, 1, 250) } returns Result.success(
            listOf(TreasuryEntityListItem(id = "usa", name = " united states ", country = "US")),
        )

        val result = PublicTreasuryRepository.getPublicTreasury(TreasuryEntity.Governments, "bitcoin")

        assertEquals("usa", result?.governments?.first()?.id)
    }

    @Test
    fun entityId_returnsNullUntilAHolderIdIsKnown() = runTest {
        coEvery { MainRepository.getTreasuryEntitiesList(any(), any(), any()) } returns Result.success(emptyList())

        assertNull(
            PublicTreasuryRepository.entityId(
                PublicTreasuryHolder(name = "Unknown"),
                TreasuryEntity.Companies,
            ),
        )
    }
}
