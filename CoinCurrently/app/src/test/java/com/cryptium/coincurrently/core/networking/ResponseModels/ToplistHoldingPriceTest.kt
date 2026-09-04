package com.cryptium.coincurrently.core.networking.ResponseModels
import org.junit.Assert.assertEquals
import org.junit.Test

class ToplistHoldingPriceTest {

    @Test
    fun holdingPrice_prefersLiveVsCurrencyThenCurrentThenSparkline() {
        val coin = Toplist(
            id = "bitcoin",
            current_price = 80_000.0,
            sparkline_in_7d = Sparkline(price = listOf(79_000.0, 80_984.70)),
        )

        assertEquals(
            80_731.0,
            coin.holdingPrice(vsCurrencyPrices = mapOf("usd" to 80_731.0), currencyCode = "USD"),
            0.0,
        )
        assertEquals(80_000.0, coin.holdingPrice(vsCurrencyPrices = null, currencyCode = "USD"), 0.0)
        assertEquals(
            80_984.70,
            Toplist(sparkline_in_7d = Sparkline(price = listOf(80_984.70)))
                .holdingPrice(vsCurrencyPrices = null, currencyCode = "USD"),
            0.0,
        )
        assertEquals(0.0, Toplist().holdingPrice(vsCurrencyPrices = emptyMap(), currencyCode = "USD"), 0.0)
    }
}
