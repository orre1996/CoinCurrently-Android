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

fun FearGreed.getLastUpdated(): String {
    val temp = ((data?.firstOrNull()?.time_until_update ?: "0").toInt()) / 60

    val dayInMilliseconds = (86400000 / 1000 / 60) - temp
    val hours1 = dayInMilliseconds / 60
    val minutes1 = dayInMilliseconds % 60

    return (if (hours1 > 0) ("$hours1" + "h ") else "") + "${minutes1}m ago"
}

fun FearGreed.getUpdatesIn(): String {
    val temp = ((data?.firstOrNull()?.time_until_update ?: "0").toInt()) / 60
    val hours = ((data?.firstOrNull()?.time_until_update ?: "0").toInt()) / 60 / 60
    val minutes = temp % 60

    return (if (hours > 0) ("$hours" + "h ") else "") + "${minutes}m"
}