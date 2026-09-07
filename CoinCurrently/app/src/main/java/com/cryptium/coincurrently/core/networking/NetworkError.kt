// Oscar Berggren
// April 27, 2026

package com.cryptium.coincurrently.core.networking
import android.content.Context
import com.cryptium.coincurrently.core.config.AppLocale
import com.cryptium.coincurrently.core.extensions.is24HourClock
import com.google.gson.JsonIOException
import com.google.gson.JsonSyntaxException
import retrofit2.HttpException
import java.net.ConnectException
import java.net.MalformedURLException
import java.net.NoRouteToHostException
import java.net.UnknownHostException
import java.text.SimpleDateFormat
import java.util.Date

enum class NetworkError(val code: Int) {
    InvalidUrl(0),
    InvalidData(1),
    InvalidRequest(400),
    UnauthorizedRequest(401),
    ForbiddenRequest(403),
    NotFound(404),
    TooManyRequests(429),
    ServerError(500),
    ApiUnavailable(503),
    NoInternetConnection(997),
    CouldntGenerateIdToken(998),
    UnknownError(999);

    fun errorMessage(cacheDate: Date?, context: Context): String {
        val cacheDateSuffix: String = if (cacheDate == null) {
            ""
        } else {
            val pattern: String = if (context.is24HourClock) {
                "MMM dd 'at' HH:mm"
            } else {
                "MMM dd 'at' hh:mm a"
            }
            val dateFormatter: SimpleDateFormat = SimpleDateFormat(pattern, AppLocale.enUsPosix)
            " Showing data from ${dateFormatter.format(cacheDate)}"
        }
        return when (this) {
            InvalidUrl -> "Invalid URL. Please contact support.$cacheDateSuffix"
            InvalidData -> "Failed to parse response data.$cacheDateSuffix"
            NoInternetConnection -> "No internet connection.$cacheDateSuffix"
            InvalidRequest -> "Bad request. Please try again.$cacheDateSuffix"
            UnauthorizedRequest -> "Authentication required. Try restarting the app.$cacheDateSuffix"
            ForbiddenRequest -> "Access denied. You don't have permission.$cacheDateSuffix"
            NotFound -> "Resource not found.$cacheDateSuffix"
            TooManyRequests -> "Too many requests. Please wait a moment.$cacheDateSuffix"
            ServerError -> "Server error. Please try again later.$cacheDateSuffix"
            ApiUnavailable -> "Service temporarily unavailable.$cacheDateSuffix"
            CouldntGenerateIdToken -> "Failed to authenticate. Try restarting the app.$cacheDateSuffix"
            UnknownError -> "Something went wrong. Check your connection.$cacheDateSuffix"
        }
    }

    companion object {
        fun isPublicTreasuryListUrl(url: String): Boolean {
            return url.contains("/companies/public_treasury/") ||
                url.contains("/governments/public_treasury/")
        }

        // Backend error bodies use `{ "detail": "..." }`. Upstream 404s are often
        // forwarded as 500s with a HttpRequestException in `detail`.
        fun isUpstreamNotFound(errorBody: String?): Boolean {
            if (errorBody.isNullOrBlank()) return false

            val detail = try {
                org.json.JSONObject(errorBody).optString("detail").ifBlank { errorBody }
            } catch (_: Exception) {
                errorBody
            }

            return detail.contains("404") && detail.contains("Not Found", ignoreCase = true)
        }

        fun fromHttpCode(code: Int, errorBody: String? = null): NetworkError {
            if (isUpstreamNotFound(errorBody)) return NotFound
            return when (code) {
                400 -> InvalidRequest
                401 -> UnauthorizedRequest
                403 -> ForbiddenRequest
                404 -> NotFound
                429 -> TooManyRequests
                500 -> ServerError
                503 -> ApiUnavailable
                998 -> CouldntGenerateIdToken
                else -> UnknownError
            }
        }

        fun fromThrowable(error: Throwable): NetworkError {
            var current: Throwable? = error
            while (current != null) {
                when (current) {
                    is HttpException -> return fromHttpCode(current.code())
                    is JsonSyntaxException, is JsonIOException -> return InvalidData
                    is UnknownHostException, is ConnectException, is NoRouteToHostException -> return NoInternetConnection
                    is MalformedURLException -> return InvalidUrl
                    else -> {}
                }
                current = current.cause
            }
            return UnknownError
        }
    }
}
