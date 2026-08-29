package networking.ResponseModels

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

enum class AlertDirection(val value: String) {
    @SerializedName("above")
    Above("above"),

    @SerializedName("below")
    Below("below");

    companion object {
        fun fromValue(value: String): AlertDirection? {
            return values().firstOrNull { it.value == value }
        }
    }
}

@Serializable
data class AlertCoin(
    @SerializedName("alertId")
    val id: String,

    val resetThreshold: Double?,

    val direction: AlertDirection,

    @SerializedName("coinId")
    val coinId: String,

    @SerializedName("price")
    val targetPrice: Double,

    val preferredCurrency: String,

    val createdDate: String
) {
    val isRecurringAlert: Boolean
        get() = resetThreshold != null
}

@Serializable
data class PostAlertCoin(
    val coinId: String,
    val targetPrice: Double,
    val direction: AlertDirection,
    val resetThreshold: Double?,
    val preferredCurrency: String
)
