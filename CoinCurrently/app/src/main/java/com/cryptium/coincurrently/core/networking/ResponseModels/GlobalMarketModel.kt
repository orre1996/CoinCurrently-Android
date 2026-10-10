package com.cryptium.coincurrently.core.networking.ResponseModels
import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class GlobalMarketModel(
    @SerializedName("data")
    var data: GlobalMarketData? = null
)

@Serializable
data class GlobalMarketData(
    @SerializedName("total_market_cap")
    var total_market_cap: Map<String, Double>? = null,
    @SerializedName("total_volume")
    var total_volume: Map<String, Double>? = null,
    @SerializedName("market_cap_percentage")
    var market_cap_percentage: Map<String, Double>? = null,
    @SerializedName("market_cap_change_percentage_24h_usd")
    var market_cap_change_percentage_24h_usd: Double? = null,
    var selectedCurrency: Double? = null,
    @SerializedName("markets")
    var markets: Int? = null,
    @SerializedName("active_cryptocurrencies")
    var active_cryptocurrencies: Int? = null,

    // Manually added
    var percentages: List<Percentages>? = listOf()
)

@Serializable
data class Percentages (
    val symbol: String?,
    var imageUrl: String?,
    val percentage: Double?,
    var color: Int?
)
