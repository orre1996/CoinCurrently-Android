package com.cryptium.coincurrently.core.networking

import com.cryptium.coincurrently.core.networking.ResponseModels.PUBLIC_COINGECKO_HOST
import com.cryptium.coincurrently.core.networking.ResponseModels.cryptoApiHostFor
import org.junit.Assert.assertEquals
import org.junit.Test

class CryptoApiHostTest {

    @Test
    fun missingOrPlaceholderBaseUrl_usesKeylessCoinGecko() {
        assertEquals(PUBLIC_COINGECKO_HOST, cryptoApiHostFor(""))
        assertEquals(PUBLIC_COINGECKO_HOST, cryptoApiHostFor("   "))
        assertEquals(PUBLIC_COINGECKO_HOST, cryptoApiHostFor("https://api.example.com"))
        assertEquals(PUBLIC_COINGECKO_HOST, cryptoApiHostFor("https://api.example.com/"))
        assertEquals(PUBLIC_COINGECKO_HOST, cryptoApiHostFor("HTTPS://API.EXAMPLE.COM"))
    }

    @Test
    fun publicCoinGeckoBaseUrl_doesNotAddCryptoPrefix() {
        assertEquals(PUBLIC_COINGECKO_HOST, cryptoApiHostFor("https://api.coingecko.com"))
        assertEquals(PUBLIC_COINGECKO_HOST, cryptoApiHostFor("https://api.coingecko.com/"))
        assertEquals(PUBLIC_COINGECKO_HOST, cryptoApiHostFor("https://api.coingecko.com/api/v3"))
    }

    @Test
    fun configuredBackend_keepsCryptoPrefix() {
        assertEquals(
            "https://api.coincurrently.app/crypto",
            cryptoApiHostFor("https://api.coincurrently.app"),
        )
        assertEquals(
            "https://api.coincurrently.app/crypto",
            cryptoApiHostFor("https://api.coincurrently.app/"),
        )
    }
}
