package com.cryptium.coincurrently.core.networking.ResponseModels
import kotlinx.serialization.Serializable

@Serializable
data class CoinsList(
    var id: String? = "",
    var symbol: String? = "",
    var name: String? = "",
    var activated_at: Int? = null
)
