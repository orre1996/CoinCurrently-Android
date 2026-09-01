package com.cryptium.coincurrently.core.networking.ResponseModels
import kotlinx.serialization.Serializable

@Serializable
data class DerivativeDetailModel(
    var symbol: String? = null,
    var base: String? = null,
    var target: String? = null,
    var trade_url: String? = null,
    var contract_type: String? = null,
    var last: Double? = null,
    var h24_percentage_change: Double? = null,
    var index: Double? = null,
    var index_basis_percentage: Double? = null,
    var bid_ask_spread: Double? = null,
    var funding_rate: Double? = null,
    var open_interest_usd: Double? = null,
    var h24_volume: Double? = null,
    var converted_volume: ConvertedLastString? = null,
    var converted_last: ConvertedLastString? = null,
    var last_traded: Double? = null,
    var expired_at: Double? = null,
)


@Serializable
data class ConvertedLastString(
    var btc: String? = null,
    var eth: String? = null,
    var usd: String? = null
)

