package com.cryptium.coincurrently.features.Portfolio

import com.cryptium.coincurrently.core.networking.ResponseModels.Toplist
import com.cryptium.coincurrently.features.Portfolio.DataClasses.PortfolioTransaction
import com.cryptium.coincurrently.features.Portfolio.Enums.PortfolioTransactionType
import com.cryptium.coincurrently.testsupport.MainDispatcherRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.Date
import java.util.UUID

class PortfolioRepositoryTest {

    @get:Rule
    val mainDispatcher = MainDispatcherRule()

    @Before
    fun setUp() {
        PortfolioRepository.portfolio = null
        PortfolioRepository.portfolioAllCurrencies = null
        PortfolioRepository.portfolioModels = null
    }

    @Test
    fun addNewCoin_ignoresCoinsWithoutAnId() {
        PortfolioRepository.addNewCoin(Toplist(name = "Nameless"), buy(), mapOf("usd" to 1.0))

        assertNull(PortfolioRepository.portfolio)
    }

    @Test
    fun addNewCoin_appendsPieAndPrice() {
        PortfolioRepository.addNewCoin(
            Toplist(id = "bitcoin", symbol = "btc"),
            buy(),
            mapOf("usd" to 100.0),
        )

        val pie = PortfolioRepository.portfolio?.data?.data?.single()
        assertEquals("bitcoin", pie?.data?.id)
        assertEquals(1, pie?.transactions?.size)
        assertEquals(100.0, PortfolioRepository.portfolioAllCurrencies?.get("bitcoin")?.get("usd"))
    }

    @Test
    fun addNewTransaction_appendsToExistingCoin() {
        PortfolioRepository.addNewCoin(Toplist(id = "bitcoin"), buy(amount = 1.0), null)
        val extra = buy(amount = 2.0)

        PortfolioRepository.addNewTransaction("bitcoin", extra)

        assertEquals(listOf(1.0, 2.0), transactions("bitcoin").map { it.amount })
    }

    @Test
    fun addNewTransaction_doesNothingForUnknownCoin() {
        PortfolioRepository.addNewCoin(Toplist(id = "bitcoin"), buy(), null)

        PortfolioRepository.addNewTransaction("ethereum", buy(amount = 9.0))

        assertEquals(1, transactions("bitcoin").size)
    }

    @Test
    fun deletePortfolioItem_removesTheCoin() {
        PortfolioRepository.addNewCoin(Toplist(id = "bitcoin"), buy(), null)
        PortfolioRepository.addNewCoin(Toplist(id = "ethereum"), buy(), null)

        PortfolioRepository.deletePortfolioItem("bitcoin")

        assertEquals(listOf("ethereum"), PortfolioRepository.portfolio?.data?.data?.map { it.data?.id })
    }

    @Test
    fun deleteTransaction_removesPieWhenItHasNoEventsLeft() {
        val transaction = buy()
        PortfolioRepository.addNewCoin(Toplist(id = "bitcoin"), transaction, null)

        val updated = PortfolioRepository.deleteTransaction("bitcoin", transaction.id)

        assertTrue(updated?.data?.data.isNullOrEmpty())
        assertTrue(PortfolioRepository.portfolio?.data?.data.isNullOrEmpty())
    }

    @Test
    fun deleteTransaction_keepsPieWhenAnotherEventRemains() {
        val first = buy(amount = 1.0)
        PortfolioRepository.addNewCoin(Toplist(id = "bitcoin"), first, null)
        PortfolioRepository.addNewTransaction("bitcoin", buy(amount = 3.0))

        PortfolioRepository.deleteTransaction("bitcoin", first.id)

        assertEquals(listOf(3.0), transactions("bitcoin").map { it.amount })
    }

    private fun transactions(id: String): List<PortfolioTransaction> {
        return PortfolioRepository.portfolio?.data?.data?.first { it.data?.id == id }?.transactions.orEmpty()
    }

    private fun buy(amount: Double = 1.0) = PortfolioTransaction(
        id = UUID.randomUUID(),
        date = Date(),
        amount = amount,
        pricePerCoin = 10.0,
        fee = 0.0,
        note = "",
        transactionType = PortfolioTransactionType.Buy,
        currency = "USD",
    )
}
