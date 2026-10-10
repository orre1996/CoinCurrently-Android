package com.cryptium.coincurrently.features.Portfolio.ViewModels

import com.cryptium.coincurrently.features.Portfolio.PortfolioRepository
import com.cryptium.coincurrently.testsupport.MainDispatcherRule
import io.mockk.every
import io.mockk.mockkObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class PortfolioViewModelTest {

    @get:Rule
    val mainDispatcher = MainDispatcherRule()

    @Before
    fun setUp() {
        mockkObject(PortfolioRepository)
        every { PortfolioRepository.portfolio } returns null
        every { PortfolioRepository.portfolioAllCurrencies } returns null
    }

    @Test
    fun getPrices_dropsTheTwoPaddedPointsWhenAsked() {
        val viewModel = PortfolioViewModel()
        viewModel.prices = listOf(1.0, 2.0, 3.0, 4.0)

        assertEquals(listOf(1.0, 2.0), viewModel.getPrices(dropLastTwo = true))
        assertEquals(listOf(1.0, 2.0, 3.0, 4.0), viewModel.getPrices())
    }

    @Test
    fun toggleHideData_flipsTheVisibleFlag() {
        val viewModel = PortfolioViewModel()

        assertFalse(viewModel.hideData)
        viewModel.toggleHideData()
        assertTrue(viewModel.hideData)
    }

    @Test
    fun canExportPortfolio_allowsTheFreeAllowance() {
        val viewModel = PortfolioViewModel()

        assertFalse(viewModel.hasBoughtPro())
        assertTrue(viewModel.canExportPortfolio())
    }
}
