package com.cryptium.coincurrently.core.networking

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NetworkErrorTest {

    @Test
    fun isUpstreamNotFound_reads404FromJsonDetail() {
        val body = """{"detail":"System.Net.Http.HttpRequestException: Response status code does not indicate success: 404 (Not Found)."}"""

        assertTrue(NetworkError.isUpstreamNotFound(body))
    }

    @Test
    fun fromHttpCode_remaps500WithUpstream404DetailToNotFound() {
        val body = """{"detail":"System.Net.Http.HttpRequestException: Response status code does not indicate success: 404 (Not Found)."}"""

        assertEquals(NetworkError.NotFound, NetworkError.fromHttpCode(500, body))
        assertEquals(NetworkError.ServerError, NetworkError.fromHttpCode(500, """{"detail":"An unexpected error occurred."}"""))
        assertEquals(NetworkError.ServerError, NetworkError.fromHttpCode(500))
    }

    @Test
    fun isUpstreamNotFound_ignoresUnrelated500Detail() {
        val body = """{"detail":"An unexpected error occurred."}"""

        assertFalse(NetworkError.isUpstreamNotFound(body))
        assertFalse(NetworkError.isUpstreamNotFound(null))
        assertFalse(NetworkError.isUpstreamNotFound(""))
    }

    @Test
    fun isPublicTreasuryListUrl_matchesEntityTreasuryPathsOnly() {
        assertTrue(
            NetworkError.isPublicTreasuryListUrl(
                "https://api.coincurrently.app/crypto/api/v3/companies/public_treasury/dogecoin?page=1&per_page=250",
            ),
        )
        assertTrue(
            NetworkError.isPublicTreasuryListUrl(
                "https://api.coincurrently.app/crypto/api/v3/governments/public_treasury/dogecoin?page=1&per_page=250",
            ),
        )
        assertFalse(
            NetworkError.isPublicTreasuryListUrl(
                "https://api.coincurrently.app/crypto/api/v3/public_treasury/strategy",
            ),
        )
    }
}
