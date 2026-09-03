package com.cryptium.coincurrently.sharedui.charts

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import com.cryptium.coincurrently.core.config.AppLocale
import com.cryptium.coincurrently.core.extensions.is24HourClock
import com.cryptium.coincurrently.core.networking.ResponseModels.TimeCharts
import com.cryptium.coincurrently.sharedui.components.Point
import com.cryptium.coincurrently.sharedui.components.getPoint
import com.cryptium.coincurrently.sharedui.components.getPriceXPosition
import com.cryptium.coincurrently.sharedui.components.lineChartIndexFromPlotX
import com.cryptium.coincurrently.sharedui.theme.onSurfaceDim
import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone

data class LineChartHoverUi(
    val dragIndex: Int,
    val hoverPrice: String,
    val hoverTime: String,
    val hoverTimePosition: Dp,
    val dragPositions: List<Point>,
    val dragPoint: Point,
    val chartColor: Color,
    val hoverColor: Color,
    val multiHoverGraphColor: Color,
    val differenceValue: String,
    val differencePercent: String,
    val isMultiHover: Boolean,
    val hoverTimes: List<String>,
)

enum class HoverTimeFallback {
    FromTimeChart,
    DateOnly,
    MonthYearOrDate,
    LocalClock,
}

@Composable
fun rememberLineChartHoverUi(
    chartHover: ChartHoverController,
    prices: List<Double>,
    timeStamps: List<Double>,
    chartSize: DpSize,
    hoverPriceTextWidth: Dp,
    timeChart: TimeCharts?,
    changePercent: Double?,
    formatPrice: (Double) -> String,
    formatDifferenceValue: HoverHelper.() -> String = { currencyDifferenceValue() },
    dropLastCount: Int = 0,
    chartColor: Color? = null,
    hoverColor: Color? = null,
    timeFallback: HoverTimeFallback = HoverTimeFallback.FromTimeChart,
    emptyPrice: String = "N/A",
    transformDragPoint: (Point) -> Point = { it },
): LineChartHoverUi = rememberLineChartHoverUi(
    hoverHelper = chartHover.hoverHelper,
    isDragging = chartHover.isDragging,
    dragPosition = chartHover.dragPosition.value,
    prices = prices,
    timeStamps = timeStamps,
    chartSize = chartSize,
    hoverPriceTextWidth = hoverPriceTextWidth,
    timeChart = timeChart,
    changePercent = changePercent,
    formatPrice = formatPrice,
    formatDifferenceValue = formatDifferenceValue,
    dropLastCount = dropLastCount,
    chartColor = chartColor,
    hoverColor = hoverColor,
    timeFallback = timeFallback,
    emptyPrice = emptyPrice,
    transformDragPoint = transformDragPoint,
)

@Composable
fun rememberLineChartHoverUi(
    hoverHelper: HoverHelper,
    isDragging: Boolean,
    dragPosition: Dp,
    prices: List<Double>,
    timeStamps: List<Double>,
    chartSize: DpSize,
    hoverPriceTextWidth: Dp,
    timeChart: TimeCharts?,
    changePercent: Double?,
    formatPrice: (Double) -> String,
    formatDifferenceValue: HoverHelper.() -> String = { currencyDifferenceValue() },
    dropLastCount: Int = 0,
    chartColor: Color? = null,
    hoverColor: Color? = null,
    timeFallback: HoverTimeFallback = HoverTimeFallback.FromTimeChart,
    emptyPrice: String = "N/A",
    transformDragPoint: (Point) -> Point = { it },
): LineChartHoverUi {
    val drawCount = (prices.size - dropLastCount).coerceAtLeast(1)
    val hoverX = hoverHelper.currentHoverXPosition(chartSize.width, dragPosition)
    val dragIndex = if (drawCount <= 1 || chartSize.width.value <= 0f) {
        0
    } else {
        lineChartIndexFromPlotX(hoverX, chartSize.width, drawCount)
    }
    val resolvedChartColor = chartColor
        ?: changePercent?.getChangeColorCompose()
        ?: MaterialTheme.colors.onSurfaceDim
    val resolvedHoverColor = hoverColor ?: MaterialTheme.colors.onSurface
    val hoverValue = hoverHelper.hoverPrices.firstOrNull() ?: prices.getOrNull(dragIndex)
    val hoverPrice = hoverValue?.let(formatPrice) ?: emptyPrice
    val is24Hour = LocalContext.current.is24HourClock
    val hoverTime = hoverHelper.hoverTimes.firstOrNull()
        ?: formatHoverTimestamp(
            timeStamps.getOrNull(dragIndex),
            timeChart,
            is24Hour,
            timeFallback,
        )
    val dropLastTwo = dropLastCount >= 2
    val dragPoint = transformDragPoint(
        getPoint(
            index = dragIndex,
            rect = chartSize,
            prices = prices,
            dropLastTwo = dropLastTwo,
        )
    )
    val hoverTimePosition = getPriceXPosition(
        dragPoint.x,
        hoverPriceTextWidth,
        chartSize.width,
    ) - hoverPriceTextWidth / 2
    val dragPositions = hoverHelper.offset.map { touchX ->
        val index = lineChartIndexFromPlotX(touchX, chartSize.width, drawCount)
        getPoint(
            index = index,
            rect = chartSize,
            prices = prices,
            dropLastTwo = dropLastTwo,
        )
    }

    return LineChartHoverUi(
        dragIndex = dragIndex,
        hoverPrice = hoverPrice,
        hoverTime = hoverTime,
        hoverTimePosition = hoverTimePosition,
        dragPositions = dragPositions,
        dragPoint = dragPoint,
        chartColor = resolvedChartColor,
        hoverColor = resolvedHoverColor,
        multiHoverGraphColor = hoverHelper.multiHoverGraphColor(resolvedChartColor, isDragging),
        differenceValue = hoverHelper.formatDifferenceValue(),
        differencePercent = hoverHelper.differencePercent(),
        isMultiHover = hoverHelper.hoverPrices.size == 2,
        hoverTimes = hoverHelper.hoverTimes,
    )
}

fun formatHoverTimestamp(
    timestamp: Double?,
    timeChart: TimeCharts?,
    is24HourClock: Boolean,
    style: HoverTimeFallback,
): String {
    if (timestamp == null || timestamp == 0.0) return ""
    val pattern = when (style) {
        HoverTimeFallback.FromTimeChart -> hoverDatePattern(timeChart, is24HourClock)
        HoverTimeFallback.DateOnly -> "dd MMM yyyy"
        HoverTimeFallback.MonthYearOrDate -> if (timeChart == TimeCharts.max) "MMM yyyy" else "dd MMM yyyy"
        HoverTimeFallback.LocalClock -> if (is24HourClock) "dd MMM 'at' HH:mm" else "dd MMM 'at' hh:mm a"
    }
    val dateFormat = SimpleDateFormat(pattern, AppLocale.enUsPosix)
    if (style == HoverTimeFallback.LocalClock) {
        dateFormat.timeZone = TimeZone.getDefault()
    }
    return dateFormat.format(Date(timestamp.toLong()))
}
