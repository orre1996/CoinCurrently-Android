package networking.ResponseModels

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import okhttp3.Headers
import java.io.ByteArrayOutputStream

data class RedeemCodeRequest(
    val refCode: String,
)

data class CachedReferralCode(
    val code: String,
    val qrPngBytes: ByteArray,
) {
    fun toResult(): ReferralCodeResult? {
        val bitmap = BitmapFactory.decodeByteArray(qrPngBytes, 0, qrPngBytes.size) ?: return null
        return ReferralCodeResult(
            qrImage = bitmap,
            code = code,
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CachedReferralCode

        if (code != other.code) return false
        if (!qrPngBytes.contentEquals(other.qrPngBytes)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = code.hashCode()
        result = 31 * result + qrPngBytes.contentHashCode()
        return result
    }
}

data class ReferralCodeResult(
    val qrImage: Bitmap,
    val code: String,
) {
    val inviteLink: String
        get() = "https://coincurrently.app/invite/$code"

    fun toCached(): CachedReferralCode {
        val stream = ByteArrayOutputStream()
        qrImage.compress(Bitmap.CompressFormat.PNG, 100, stream)
        return CachedReferralCode(
            code = code,
            qrPngBytes = stream.toByteArray(),
        )
    }

    companion object {
        fun decode(data: ByteArray, headers: Headers): ReferralCodeResult? {
            val options = BitmapFactory.Options().apply {
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }
            val bitmap = BitmapFactory.decodeByteArray(data, 0, data.size, options) ?: return null
            val code = headers["raw_code"]?.trim()?.takeIf { it.isNotEmpty() } ?: return null

            return ReferralCodeResult(
                qrImage = bitmap,
                code = code,
            )
        }
    }
}
