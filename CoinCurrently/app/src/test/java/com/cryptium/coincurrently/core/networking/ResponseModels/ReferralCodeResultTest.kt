package com.cryptium.coincurrently.core.networking.ResponseModels
import okhttp3.Headers.Companion.toHeaders
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ReferralCodeResultTest {

    @Test
    fun decode_readsReferralCodeHeaders() {
        val headers = mapOf(
            "raw_code" to "ABC1234567",
        ).toHeaders()

        val code = ReferralCodeResult.codeFromHeaders(headers)

        assertEquals("ABC1234567", code)
        assertEquals(
            "https://coincurrently.app/invite/ABC1234567",
            ReferralCodeResult.inviteUrl(code!!),
        )
    }

    @Test
    fun redeemCodeResult_mapsStatusCodes() {
        assertEquals(RedeemCodeResult.Success, RedeemCodeResult.fromStatusCode(200))
        assertEquals(RedeemCodeResult.InvalidCode, RedeemCodeResult.fromStatusCode(404))
        assertEquals("Invalid code", RedeemCodeResult.InvalidCode.errorMessage)
        assertEquals(RedeemCodeResult.AlreadyReferred, RedeemCodeResult.fromStatusCode(400))
        assertEquals(
            "You've already been referred by someone else.",
            RedeemCodeResult.AlreadyReferred.errorMessage,
        )
        assertEquals(RedeemCodeResult.SelfRedeem, RedeemCodeResult.fromStatusCode(418))
        assertEquals("You can't redeem yourself, silly.", RedeemCodeResult.SelfRedeem.errorMessage)
        assertEquals(RedeemCodeResult.Failure, RedeemCodeResult.fromStatusCode(500))
    }

    @Test
    fun decode_returnsNullWhenCodeHeaderIsMissing() {
        val headers = emptyMap<String, String>().toHeaders()

        assertNull(ReferralCodeResult.codeFromHeaders(headers))
        assertNull(ReferralCodeResult.codeFromHeaders(
            mapOf("raw_code" to "  ").toHeaders(),
        ))
    }
}
