package com.cryptium.coincurrently.core.networking.ResponseModels
import java.io.Serializable
import com.google.gson.annotations.SerializedName

data class FearGreed(
    val name: String? = null,
    @SerializedName("data")
    val data: List<FearGreedItem>? = null
): Serializable

data class FearGreedItem(
    val value: String?,
    val value_classification: String?,
    val timestamp: String?,
    val time_until_update : String?
): Serializable
