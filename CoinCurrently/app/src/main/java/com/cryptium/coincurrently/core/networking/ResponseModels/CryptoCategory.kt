package com.cryptium.coincurrently.core.networking.ResponseModels
import java.io.Serializable

@kotlinx.serialization.Serializable
data class CryptoCategory(
    var id: String? = "",
    var name: String? = "",
    var market_cap: Double? = 0.0,
    var market_cap_change_24h: Double? = 0.0,
    var content: String? = "",
    var top_3_coins: ArrayList<String>? = arrayListOf(),
    var volume_24h: Double? = 0.0,
    var updated_at: String? = "",
    var position: Int = 0
): Serializable
