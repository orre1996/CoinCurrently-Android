package com.cryptium.coincurrently.core.networking.ResponseModels
import kotlinx.serialization.Serializable

@Serializable
data class NFTPlatform(
    val id: String? = null,
    val name: String? = null,
    val image: Logo? = null,
    val native_coin_id: String? = null
) {
    val accessibilityTitle: String
        get() = if (id == "all") "All Chains" else (name ?: "All Chains")
}
