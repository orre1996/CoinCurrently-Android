package com.cryptium.coincurrently.core.networking.ResponseModels
import kotlinx.serialization.Serializable

@Serializable
data class Derivative(
    var name: String? = null,
    var id: String? = null,
    var open_interest_btc: Double? = null,
    var trade_volume_24h_btc: String? = null,
    var number_of_perpetual_pairs: Int? = null,
    var number_of_futures_pairs: Int? = null,
    var image: String? = null,
    var year_established: Int? = null,
    var country: String? = null,
    var description: String? = null,
    var url: String? = null,
    var tickers: List<DerivativeDetailModel>? = null
)

val Derivative.displayName: String?
    get() {
        val name = name ?: return null
        return name
            .replace("Futures", "")
            .replace("Derivatives", "")
            .replace(Regex("\\([^)]*\\)"), "")
            .trim()
    }
