package com.cryptium.coincurrently.core.networking.ResponseModels
import com.cryptium.coincurrently.core.localization.Localization
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import okhttp3.Headers
import java.io.ByteArrayOutputStream

data class RedeemCodeRequest(
    val refCode: String,
)

enum class RedeemCodeResult {
    Success,
    InvalidCode,
    AlreadyReferred,
    SelfRedeem,
    Failure;

    val errorMessage: String?
        get() = when (this) {
            Success -> null
            InvalidCode -> Localization.string("Invalid code")
            AlreadyReferred -> Localization.string("You've already been referred by someone else.")
            SelfRedeem -> Localization.string("You can't redeem yourself, silly.")
            Failure -> Localization.string("Something went wrong. Please try again.")
        }

    companion object {
        fun fromStatusCode(statusCode: Int): RedeemCodeResult = when (statusCode) {
            200 -> Success
            404 -> InvalidCode
            400 -> AlreadyReferred
            418 -> SelfRedeem
            else -> Failure
        }

        fun fromResult(result: Result<*>): RedeemCodeResult {
            if (result.isSuccess) return Success

            var current: Throwable? = result.exceptionOrNull()
            while (current != null) {
                if (current is retrofit2.HttpException) {
                    return fromStatusCode(current.code())
                }
                current = current.cause
            }
            return Failure
        }
    }
}

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
        get() = inviteUrl(code)

    fun toCached(): CachedReferralCode {
        val stream = ByteArrayOutputStream()
        qrImage.compress(Bitmap.CompressFormat.PNG, 100, stream)
        return CachedReferralCode(
            code = code,
            qrPngBytes = stream.toByteArray(),
        )
    }

    companion object {
        fun codeFromHeaders(headers: Headers): String? =
            headers["raw_code"]?.trim()?.takeIf { it.isNotEmpty() }

        fun inviteUrl(code: String): String = "https://coincurrently.app/invite/$code"

        fun decode(data: ByteArray, headers: Headers): ReferralCodeResult? {
            val code = codeFromHeaders(headers) ?: return null
            val options = BitmapFactory.Options().apply {
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }
            val bitmap = BitmapFactory.decodeByteArray(data, 0, data.size, options) ?: return null

            return ReferralCodeResult(
                qrImage = bitmap,
                code = code,
            )
        }
    }
}
