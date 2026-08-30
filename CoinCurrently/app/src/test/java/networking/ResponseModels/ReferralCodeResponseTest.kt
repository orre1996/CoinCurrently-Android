package networking.ResponseModels

import okhttp3.Headers.Companion.toHeaders
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class ReferralCodeResultTest {

    @Test
    fun decode_readsReferralCodeHeaders() {
        val pngBytes = minimalPngBytes()
        val headers = mapOf(
            "raw_code" to "ABC1234567",
        ).toHeaders()

        val result = ReferralCodeResult.decode(pngBytes, headers)

        assertNotNull(result)
        assertEquals("ABC1234567", result!!.code)
        assertEquals("https://coincurrently.app/invite/ABC1234567", result.inviteLink)
        assertNotNull(result.qrImage)
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
        val pngBytes = minimalPngBytes()
        val headers = emptyMap<String, String>().toHeaders()

        assertNull(ReferralCodeResult.decode(pngBytes, headers))
    }

    private fun minimalPngBytes(): ByteArray {
        return byteArrayOf(
            0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A,
            0x00, 0x00, 0x00, 0x0D, 0x49, 0x48, 0x44, 0x52,
            0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x01,
            0x08, 0x06, 0x00, 0x00, 0x00, 0x1F, 0x15.toByte(), 0xC4.toByte(),
            0x89.toByte(), 0x00, 0x00, 0x00, 0x0A, 0x49, 0x44, 0x41,
            0x54, 0x78, 0x9C.toByte(), 0x63, 0x00, 0x01, 0x00, 0x00,
            0x05, 0x00, 0x01, 0x0D, 0x0A, 0x2D, 0xB4.toByte(), 0x00,
            0x00, 0x00, 0x00, 0x49, 0x45, 0x4E, 0x44, 0xAE.toByte(),
            0x42, 0x60, 0x82.toByte(),
        )
    }
}
