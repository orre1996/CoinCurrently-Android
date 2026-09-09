package com.cryptium.coincurrently.widgets.receivers

import com.cryptium.coincurrently.core.networking.ResponseModels.Pie
import com.cryptium.coincurrently.core.networking.ResponseModels.Toplist
import com.cryptium.coincurrently.features.Portfolio.DataClasses.PortfolioTransaction
import com.cryptium.coincurrently.features.Portfolio.Enums.PortfolioTransactionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Date
import java.util.UUID

class PortfolioWidgetCalculatorTest {

    @Test
    fun topCoinsByProfitLoss_sortsByValueAndKeepsSix() {
        val portfolio = listOf(
            coin("high-percent", buyAmount = 1.0, buyPrice = 10.0, currentPrice = 30.0),
            coin("high-value", buyAmount = 10.0, buyPrice = 100.0, currentPrice = 120.0),
            coin("mid", buyAmount = 1.0, buyPrice = 100.0, currentPrice = 115.0),
            coin("low", buyAmount = 1.0, buyPrice = 100.0, currentPrice = 110.0),
            coin("flat", buyAmount = 1.0, buyPrice = 100.0, currentPrice = 100.0),
            coin("down", buyAmount = 1.0, buyPrice = 100.0, currentPrice = 80.0),
            coin("lowest", buyAmount = 1.0, buyPrice = 100.0, currentPrice = 40.0),
        )
        val prices = portfolio.associate { pie ->
            pie.data!!.id!! to mapOf("usd" to pie.data!!.current_price!!)
        }

        val data = buildPortfolioWidgetData(portfolio, prices, "usd")

        assertEquals(
            listOf("high-value", "high-percent", "mid", "low", "flat", "down"),
            data.topCoinsByProfitLoss.map { it.coinId },
        )
        assertEquals(200.0, data.topCoinsByProfitLoss.first().changeValue!!, 0.0001)
        assertEquals(20.0, data.topCoinsByProfitLoss[1].changeValue!!, 0.0001)
        assertEquals(165.0, data.profitLossValue!!, 0.0001)
    }

    @Test
    fun emptyPortfolio_isMarkedEmpty() {
        val data = buildPortfolioWidgetData(emptyList(), emptyMap(), "usd")

        assertTrue(data.isEmpty)
        assertTrue(data.topCoinsByProfitLoss.isEmpty())
        assertEquals(null, data.profitLossValue)
    }

    private fun coin(
        id: String,
        buyAmount: Double,
        buyPrice: Double,
        currentPrice: Double,
    ) = Pie(
        data = Toplist(
            id = id,
            symbol = id,
            name = id,
            current_price = currentPrice,
            price_change_percentage_24h_in_currency = 1.0,
        ),
        transactions = mutableListOf(
            PortfolioTransaction(
                id = UUID.randomUUID(),
                date = Date(),
                amount = buyAmount,
                pricePerCoin = buyPrice,
                fee = 0.0,
                note = "",
                transactionType = PortfolioTransactionType.Buy,
                currency = "usd",
            ),
        ),
    )
}
