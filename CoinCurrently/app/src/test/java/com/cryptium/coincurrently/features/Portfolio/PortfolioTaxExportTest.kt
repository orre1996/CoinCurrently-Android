package com.cryptium.coincurrently.features.Portfolio
import com.cryptium.coincurrently.core.extensions.toPortfolioExportDateTimeUTC
import com.cryptium.coincurrently.core.networking.ResponseModels.Pie
import com.cryptium.coincurrently.core.networking.ResponseModels.Toplist
import com.cryptium.coincurrently.features.Portfolio.DataClasses.NewTransaction
import com.cryptium.coincurrently.features.Portfolio.Enums.NewTransactionType
import com.cryptium.coincurrently.features.Portfolio.Utilities.PortfolioTaxExportBuilder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Date
import java.util.TimeZone
import java.util.UUID

class PortfolioTaxExportTest {

    @Test
    fun koinlyCsv_exportsBuyAsFiatOutCryptoIn() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
        val date = Date(1_700_000_000_000L)
        val portfolio = listOf(
            Pie(
                data = Toplist(id = "bitcoin", symbol = "btc", name = "Bitcoin"),
                transactions = mutableListOf(
                    NewTransaction(
                        id = UUID.randomUUID(),
                        date = date,
                        amount = 1.5,
                        pricePerCoin = 100.0,
                        fee = 0.25,
                        note = "test buy",
                        transactionType = NewTransactionType.Buy,
                        currency = "usd",
                    ),
                ),
            ),
        )

        val csv = PortfolioTaxExportBuilder.koinlyCSV(portfolio)
        val lines = csv.lines()

        assertEquals(
            "Date,Sent Amount,Sent Currency,Received Amount,Received Currency,Fee Amount,Fee Currency,Description",
            lines.first(),
        )
        assertEquals(2, lines.size)
        assertEquals(
            listOf(
                date.toPortfolioExportDateTimeUTC(),
                "150.0",
                "USD",
                "1.5",
                "BTC",
                "0.25",
                "USD",
                "test buy",
            ).joinToString(","),
            lines[1],
        )
    }

    @Test
    fun tokenTaxCsv_includesTradeTypeAndHeader() {
        val portfolio = listOf(
            Pie(
                data = Toplist(id = "ethereum", symbol = "eth", name = "Ethereum"),
                transactions = mutableListOf(
                    NewTransaction(
                        amount = 2.0,
                        pricePerCoin = 50.0,
                        fee = 0.0,
                        note = "",
                        transactionType = NewTransactionType.Sell,
                        currency = "usd",
                    ),
                ),
            ),
        )

        val csv = PortfolioTaxExportBuilder.tokenTaxCSV(portfolio)
        assertTrue(csv.startsWith("Type,BuyAmount,BuyCurrency,SellAmount,SellCurrency,"))
        val row = csv.lines()[1]
        assertTrue(row.startsWith("Trade,"))
        assertTrue(row.contains("ETH"))
        assertTrue(row.contains("USD"))
        assertTrue(row.contains("100.0"))
    }
}
