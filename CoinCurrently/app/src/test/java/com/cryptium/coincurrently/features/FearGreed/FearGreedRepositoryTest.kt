package com.cryptium.coincurrently.features.FearGreed

import com.cryptium.coincurrently.core.networking.MainRepository
import com.cryptium.coincurrently.core.networking.ResponseModels.FearGreed
import com.cryptium.coincurrently.core.networking.ResponseModels.FearGreedItem
import com.cryptium.coincurrently.core.networking.RetrofitBuilder
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

class FearGreedRepositoryTest {

    @get:Rule
    val mainDispatcher = MainDispatcherRule()

    @Before
    fun setUp() {
        mockkObject(MainRepository)
        RetrofitBuilder.clearResponseCache()
    }

    @Test
    fun getFearGreedModel_returnsFreshCacheWithoutNetwork() = runTest {
        val cached = fear(value = "24")
        seedCache(RetrofitBuilder.FEAR_GREED_URL, cached)
        coEvery { MainRepository.getFearGreedModel() } returns Result.success(fear("90"))

        val result = FearGreedRepository.getFearGreedModel()

        assertEquals("24", result?.data?.first()?.value)
        assertEquals("24", FearGreedRepository.fearGreed?.data?.first()?.value)
        coVerify(exactly = 0) { MainRepository.getFearGreedModel() }
    }

    @Test
    fun getFearGreedModel_storesNetworkResult() = runTest {
        val fetched = fear("55")
        coEvery { MainRepository.getFearGreedModel() } returns Result.success(fetched)

        assertEquals(fetched, FearGreedRepository.getFearGreedModel())
        assertEquals(fetched, FearGreedRepository.fearGreed)
    }

    @Test
    fun getFearGreedModel_returnsNullWhenNetworkFails() = runTest {
        coEvery { MainRepository.getFearGreedModel() } returns Result.failure(IllegalStateException("down"))

        assertNull(FearGreedRepository.getFearGreedModel())
    }

    private fun fear(value: String) = FearGreed(
        name = "Fear and Greed",
        data = listOf(FearGreedItem(value, "Neutral", "0", "3600")),
    )
}
