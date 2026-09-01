package com.cryptium.coincurrently.sharedui.charts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.Dp
import com.cryptium.coincurrently.core.config.AppLocale
import com.cryptium.coincurrently.core.networking.ResponseModels.Candle
import com.cryptium.coincurrently.core.networking.ResponseModels.TimeCharts
import java.text.SimpleDateFormat
import java.util.Date
import com.cryptium.coincurrently.sharedui.components.candleChartIndexFromPlotX
import com.cryptium.coincurrently.sharedui.components.lineChartIndexFromPlotX

data class CurrentChartHover(
    val time: String,
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double,
)

class HoverHelper {
    var hoverPrices by mutableStateOf<List<Double>>(emptyList())
    var hoverTimes by mutableStateOf<List<String>>(emptyList())
    var offset by mutableStateOf<List<Dp>>(emptyList())
    var currentCandle by mutableStateOf<CurrentChartHover?>(null)

    private var tempHoverTimes = emptyList<String>()
    private var latestPositions = emptyList<Int>()

    fun resetValues() {
        offset = emptyList()
        hoverPrices = emptyList()
        hoverTimes = emptyList()
        currentCandle = null
    }

    fun getHoverPrice(
        chartWidth: Dp,
        touchXs: List<Dp>,
        prices: List<Double>,
        dates: List<Double>,
        timeChart: TimeCharts?,
        is24HourClock: Boolean,
        isDragging: Boolean,
        dontShowTime: Boolean = false,
        onIndexChanged: () -> Unit = {},
    ) {
        if (touchXs.isEmpty() || prices.isEmpty()) return

        val indices = touchXs.map { x ->
            lineChartIndexFromPlotX(x, chartWidth, prices.size)
        }

        val localPrices = indices.map { prices.getOrNull(it) ?: 0.0 }
        val dateFormat = SimpleDateFormat(getHoverDateFormat(timeChart, is24HourClock, dontShowTime), AppLocale.enUsPosix)
        val localTimes = indices.map { index ->
            val timestamp = dates.getOrNull(index) ?: 0.0
            dateFormat.format(Date(timestamp.toLong()))
        }

        val tempTimeFormat = SimpleDateFormat(
            if (is24HourClock) "dd MMM 'at' HH:mm" else "dd MMM 'at' hh:mm a",
            AppLocale.enUsPosix,
        )
        val temporaryTimes = indices.map { index ->
            val timestamp = dates.getOrNull(index) ?: 0.0
            tempTimeFormat.format(Date(timestamp.toLong()))
        }

        if (tempHoverTimes != temporaryTimes && isDragging) {
            onIndexChanged()
        }

        tempHoverTimes = temporaryTimes
        hoverPrices = localPrices
        hoverTimes = localTimes
        offset = touchXs
    }

    fun getHoverPriceCandles(
        chartWidth: Dp,
        touchX: Dp,
        candles: List<Candle>,
        timeChart: TimeCharts?,
        is24HourClock: Boolean,
        isDragging: Boolean,
        onIndexChanged: () -> Unit = {},
    ) {
        if (candles.isEmpty()) return

        val position = candleChartIndexFromPlotX(touchX, chartWidth, candles.size)
        val candle = candles.getOrNull(position)
        val dateFormat = SimpleDateFormat(getHoverDateFormat(timeChart, is24HourClock), AppLocale.enUsPosix)
        val timestamp = candle?.timestamp ?: 0.0
        val date = dateFormat.format(Date(timestamp.toLong()))

        currentCandle = CurrentChartHover(
            time = date,
            open = candle?.open ?: 0.0,
            high = candle?.high ?: 0.0,
            low = candle?.low ?: 0.0,
            close = candle?.close ?: 0.0,
        )

        if (latestPositions.firstOrNull() != position && isDragging) {
            onIndexChanged()
        }

        latestPositions = listOf(position)
        offset = listOf(touchX)
        hoverPrices = listOf(candle?.close ?: 0.0)
    }

    private fun getHoverDateFormat(
        timeChart: TimeCharts?,
        is24HourClock: Boolean,
        dontShowTime: Boolean = false,
    ): String {
        return when (timeChart) {
            TimeCharts.aWeek -> {
                if (dontShowTime) "dd MMM yyyy" else {
                    if (is24HourClock) "dd MMM 'at' HH:mm" else "dd MMM 'at' hh:mm a"
                }
            }
            TimeCharts.aMonth, TimeCharts.threeMonths, TimeCharts.aYear -> "dd MMM yyyy"
            TimeCharts.max -> "MMM yyyy"
            else -> if (is24HourClock) "dd MMM 'at' HH:mm" else "dd MMM 'at' hh:mm a"
        }
    }
}
