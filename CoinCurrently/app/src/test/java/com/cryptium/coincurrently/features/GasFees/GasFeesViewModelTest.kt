package com.cryptium.coincurrently.features.GasFees

import com.cryptium.coincurrently.core.networking.ResponseModels.GasFeesModel
import com.cryptium.coincurrently.core.networking.ResponseModels.GasResult
import com.cryptium.coincurrently.core.networking.ResponseModels.Toplist
import com.cryptium.coincurrently.features.Market.Toplist.Repositories.ToplistRepository
import com.cryptium.coincurrently.testsupport.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockkObject
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class GasFeesViewModelTest {

    @get:Rule
    val mainDispatcher = MainDispatcherRule()

    @Before
    fun setUp() {
        mockkObject(GasFeesRepository)
        mockkObject(ToplistRepository)
        every { GasFeesRepository.cachedGasFees } returns null
        every { GasFeesRepository.cachedGasFeesPrices } returns null
    }

    @Test
    fun currentTimeFrames_readsLowAverageAndHighFromLatestSample() {
        val viewModel = GasFeesViewModelImp()
        viewModel.gasFees = gasFees(low = "3", average = "8", high = "21")

        assertEquals(
            listOf("3" to "Current Low", "8" to "Current Average", "21" to "Current High"),
            viewModel.currentTimeFrames,
        )
    }

    @Test
    fun normalizedPosition_mapsProposePriceIntoFixedSegments() {
        val viewModel = GasFeesViewModelImp()

        viewModel.gasFees = gasFees(average = "0")
        assertEquals(0.0, viewModel.normalizedPosition, 0.0001)

        viewModel.gasFees = gasFees(average = "6.5")
        assertEquals(0.3, viewModel.normalizedPosition, 0.0001)

        viewModel.gasFees = gasFees(average = "200")
        assertEquals(1.0, viewModel.normalizedPosition, 0.0001)
    }

    @Test
    fun getGasPrice_returnsNullUntilBothGasAndEthPricesExist() {
        val viewModel = GasFeesViewModelImp()

        assertNull(viewModel.getGasPrice(GasUnits.Swap))

        viewModel.gasFees = gasFees(average = "20")
        assertNull(viewModel.getGasPrice(GasUnits.Swap))

        viewModel.ethereum = Toplist(id = "ethereum", current_price = 2000.0)
        val price = viewModel.getGasPrice(GasUnits.Swap)
        assertTrue(price?.isNotBlank() == true)
    }

    @Test
    fun getGasFees_keepsPreviousValueWhenRepositoryReturnsNull() = runTest {
        val existing = gasFees(average = "4")
        every { GasFeesRepository.cachedGasFees } returns existing
        coEvery { GasFeesRepository.getGasFees() } returns null
        val viewModel = GasFeesViewModelImp()

        viewModel.getGasFees()

        assertEquals(existing, viewModel.gasFees)
    }

    @Test
    fun getEthereumPrice_selectsEthereumFromSmallToplist() = runTest {
        val eth = Toplist(id = "ethereum", symbol = "eth", current_price = 1800.0)
        coEvery { ToplistRepository.getSmallToplist() } returns arrayListOf(
            Toplist(id = "bitcoin"),
            eth,
        )
        val viewModel = GasFeesViewModelImp()

        viewModel.getEthereumPrice()

        assertEquals(eth, viewModel.ethereum)
    }

    @Test
    fun getCurrentTextAccessible_includesTheTierForEachIndex() {
        val viewModel = GasFeesViewModelImp()

        assertTrue(viewModel.getCurrentTextAccessible(0, "1.5").contains("low"))
        assertTrue(viewModel.getCurrentTextAccessible(1, "2").contains("average"))
        assertTrue(viewModel.getCurrentTextAccessible(2, "9").contains("high"))
    }

    private fun gasFees(low: String = "1", average: String = "5", high: String = "10") = GasFeesModel(
        gasFeeData = listOf(
            GasResult(
                lastBlock = "1",
                sK_Date = "2026-01-01T00:00:00.000Z",
                safeGasPrice = low,
                proposeGasPrice = average,
                fastGasPrice = high,
                suggestBaseFee = "1",
                gasUsedRatio = "0.4",
            ),
        ),
    )
}
