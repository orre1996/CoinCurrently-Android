package networking.ResponseModels

import com.google.gson.Gson
import kotlinx.serialization.Serializable

@Serializable
data class CandleStickChartModel(
    val candle: List<Candle>
)

@Serializable
data class Candle(
    val timestamp: Double?,
    val open: Double?,
    val high: Double?,
    val low: Double?,
    val close: Double?
)

fun parseCandles(json: String?): CandleStickChartModel? {
    val json = json ?: return null

    val gson = Gson()
    val values = gson.fromJson(json, Array<Array<Any>>::class.java)

    return CandleStickChartModel(
        values.map {
            Candle(
                timestamp = it.getOrNull(0) as? Double,
                open = it.getOrNull(1) as? Double,
                high = it.getOrNull(2) as? Double,
                low = it.getOrNull(3) as? Double,
                close = it.getOrNull(4) as? Double,
            )
        }
    )
}