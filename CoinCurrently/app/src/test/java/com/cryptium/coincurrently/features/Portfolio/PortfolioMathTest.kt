package com.cryptium.coincurrently.features.Portfolio
import com.cryptium.coincurrently.core.extensions.getAverageCost
import com.cryptium.coincurrently.core.extensions.getProfitLossPercent
import com.cryptium.coincurrently.core.extensions.getProfitLossValue
import com.cryptium.coincurrently.core.extensions.groupedTransactions
import com.cryptium.coincurrently.core.networking.ResponseModels.Pie
import com.cryptium.coincurrently.core.networking.ResponseModels.Toplist
import com.cryptium.coincurrently.features.Portfolio.DataClasses.NewTransaction
import com.cryptium.coincurrently.features.Portfolio.Enums.DateOption
import com.cryptium.coincurrently.features.Portfolio.Enums.NewTransactionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar
import java.util.Date
import java.util.UUID

class PortfolioMathTest {

    @Test
    fun getProfitLoss_buyThenHoldUsesCurrentPrice() {
        val portfolio = listOf(
            pie(
                id = "bitcoin",
                transactions = listOf(
                    buy(amount = 1.0, price = 100.0),
                ),
            ),
        )
        val prices = mapOf("bitcoin" to mapOf("usd" to 150.0))

        assertEquals(50.0, portfolio.getProfitLossValue(prices, "usd")!!, 0.0001)
        assertEquals(50.0, portfolio.getProfitLossPercent(prices, "usd"), 0.0001)
    }

    @Test
    fun getProfitLoss_realizedAndUnrealizedGainsAddUp() {
        val portfolio = listOf(
            pie(
                id = "bitcoin",
                transactions = listOf(
                    buy(amount = 2.0, price = 100.0, date = daysAgo(10)),
                    sell(amount = 1.0, price = 150.0, date = daysAgo(1)),
                ),
            ),
        )
        val prices = mapOf("bitcoin" to mapOf("usd" to 200.0))

        assertEquals(150.0, portfolio.getProfitLossValue(prices, "usd")!!, 0.0001)
        assertEquals(75.0, portfolio.getProfitLossPercent(prices, "usd"), 0.0001)
    }

    @Test
    fun getProfitLoss_emptyOrMissingPricesIsZero() {
        val portfolio = listOf(pie(id = "bitcoin", transactions = listOf(buy(1.0, 100.0))))

        assertEquals(0.0, emptyList<Pie>().getProfitLossValue(mapOf("bitcoin" to mapOf("usd" to 1.0)), "usd")!!, 0.0)
        assertEquals(0.0, portfolio.getProfitLossValue(null, "usd")!!, 0.0)
        assertEquals(0.0, portfolio.getProfitLossPercent(emptyMap(), "usd"), 0.0)
    }

    @Test
    fun getAverageCost_weightsBuysByAmount() {
        val transactions = listOf(
            buy(amount = 1.0, price = 100.0),
            buy(amount = 3.0, price = 200.0),
        )
        val prices = mapOf("usd" to 1.0)

        assertEquals(175.0, transactions.getAverageCost(prices, "usd")!!, 0.0001)
    }

    @Test
    fun groupedTransactions_bucketsByDayAndHonorsFilters() {
        val todayBuy = buy(amount = 1.0, price = 100.0, date = startOfDay())
        val todaySell = sell(amount = 0.5, price = 120.0, date = hoursAfter(startOfDay(), 3))
        val oldBuy = buy(amount = 2.0, price = 50.0, date = daysAgo(40))
        val ethBuy = buy(amount = 1.0, price = 10.0, date = startOfDay())

        val portfolio = listOf(
            pie(id = "bitcoin", symbol = "btc", name = "Bitcoin", transactions = listOf(todayBuy, todaySell, oldBuy)),
            pie(id = "ethereum", symbol = "eth", name = "Ethereum", transactions = listOf(ethBuy)),
        )

        val allBtc = portfolio.groupedTransactions(
            types = emptyList(),
            dateOption = DateOption.All,
            coinNames = listOf(Toplist(id = "bitcoin", symbol = "btc", name = "Bitcoin")),
        )
        assertEquals(2, allBtc.size)
        assertEquals(2, allBtc[startOfDay()]?.size)

        val buysOnly = portfolio.groupedTransactions(
            types = listOf(NewTransactionType.Buy),
            dateOption = DateOption.All,
            coinNames = listOf(Toplist(id = "bitcoin", symbol = "btc", name = "Bitcoin")),
        )
        assertEquals(setOf(todayBuy.id, oldBuy.id), buysOnly.values.flatten().map { it.id }.toSet())

        val lastMonth = portfolio.groupedTransactions(
            types = emptyList(),
            dateOption = DateOption.LastMonth,
            coinNames = emptyList(),
        )
        val lastMonthIds = lastMonth.values.flatten().map { it.id }.toSet()
        assertTrue(todayBuy.id in lastMonthIds)
        assertTrue(ethBuy.id in lastMonthIds)
        assertTrue(oldBuy.id !in lastMonthIds)
    }

    private fun pie(
        id: String,
        symbol: String = id,
        name: String = id,
        transactions: List<NewTransaction>,
    ) = Pie(
        data = Toplist(id = id, symbol = symbol, name = name),
        transactions = transactions.toMutableList(),
    )

    private fun buy(amount: Double, price: Double, date: Date = Date()) = NewTransaction(
        id = UUID.randomUUID(),
        date = date,
        amount = amount,
        pricePerCoin = price,
        fee = 0.0,
        note = "",
        transactionType = NewTransactionType.Buy,
        currency = "usd",
    )

    private fun sell(amount: Double, price: Double, date: Date = Date()) = NewTransaction(
        id = UUID.randomUUID(),
        date = date,
        amount = amount,
        pricePerCoin = price,
        fee = 0.0,
        note = "",
        transactionType = NewTransactionType.Sell,
        currency = "usd",
    )

    private fun startOfDay(): Date {
        return Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time
    }

    private fun hoursAfter(date: Date, hours: Int): Date {
        return Calendar.getInstance().apply {
            time = date
            add(Calendar.HOUR_OF_DAY, hours)
        }.time
    }

    private fun daysAgo(days: Int): Date {
        return Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -days)
        }.time
    }
}
