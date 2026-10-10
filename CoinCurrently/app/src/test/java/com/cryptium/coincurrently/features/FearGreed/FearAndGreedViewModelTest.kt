package com.cryptium.coincurrently.features.FearGreed

import com.cryptium.coincurrently.core.networking.ResponseModels.FearGreed
import com.cryptium.coincurrently.core.networking.ResponseModels.FearGreedItem
import com.cryptium.coincurrently.core.networking.ResponseModels.TimeCharts
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

class FearAndGreedViewModelTest {

    @get:Rule
    val mainDispatcher = MainDispatcherRule()

    @Before
    fun setUp() {
        mockkObject(FearGreedRepository)
        every { FearGreedRepository.fearGreed } returns null
    }

    @Test
    fun setPrices_keepsTheSelectedWindowAndPadsTheScale() {
        val viewModel = FearAndGreedViewModelImp()
        viewModel.selectedChartTimeFrame = TimeCharts.aWeek
        viewModel.fearGreedModel = FearGreed(
            data = (1..10).map { index ->
                FearGreedItem(index.toString(), "Neutral", index.toString(), null)
            },
        )

        viewModel.setPrices()

        assertEquals(listOf(7.0, 6.0, 5.0, 4.0, 3.0, 2.0, 1.0, 0.0, 100.0), viewModel.prices)
        assertEquals(7, viewModel.timeStamps.size)
    }

    @Test
    fun getFearGreed_leavesStateAloneWhenTheRepositoryReturnsNothing() = runTest {
        coEvery { FearGreedRepository.getFearGreedModel() } returns null
        val viewModel = FearAndGreedViewModelImp()
        var called = false

        viewModel.getFearGreed { called = true }

        assertEquals(null, viewModel.fearGreedModel)
        assertTrue(!called)
    }

    @Test
    fun factory_createsTheImplementation() {
        val created = FearAndGreedViewModelFactory().create(FearAndGreedViewModelImp::class.java)

        assertTrue(created is FearAndGreedViewModelImp)
    }
}
