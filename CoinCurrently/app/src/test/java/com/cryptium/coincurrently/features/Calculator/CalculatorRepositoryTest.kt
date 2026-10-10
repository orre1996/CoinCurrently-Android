package com.cryptium.coincurrently.features.Calculator

import com.cryptium.coincurrently.core.networking.MainRepository
import com.cryptium.coincurrently.core.networking.RetrofitBuilder
import com.cryptium.coincurrently.testsupport.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.mockkObject
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class CalculatorRepositoryTest {

    @get:Rule
    val mainDispatcher = MainDispatcherRule()

    @Before
    fun setUp() {
        mockkObject(MainRepository)
        RetrofitBuilder.clearResponseCache()
        CalculatorRepository.calculatorPrices = null
    }

    @Test
    fun getCalculatorPrices_storesNetworkMap() = runTest {
        val prices = mutableMapOf("ethereum" to mapOf("eur" to 2000.0))
        coEvery { MainRepository.getCoinsWithAllCurrencies("ethereum") } returns Result.success(prices)

        assertEquals(prices, CalculatorRepository.getCalculatorPrices(listOf("ethereum")))
        assertEquals(prices, CalculatorRepository.calculatorPrices)
    }

    @Test
    fun getCalculatorPrices_keepsNullWhenNetworkFails() = runTest {
        coEvery { MainRepository.getCoinsWithAllCurrencies(any()) } returns Result.failure(IllegalStateException("down"))

        org.junit.Assert.assertNull(CalculatorRepository.getCalculatorPrices(listOf("bitcoin")))
        org.junit.Assert.assertNull(CalculatorRepository.calculatorPrices)
    }
}
