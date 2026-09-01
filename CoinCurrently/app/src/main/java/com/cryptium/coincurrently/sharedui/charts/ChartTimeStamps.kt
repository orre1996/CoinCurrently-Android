package com.cryptium.coincurrently.sharedui.charts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.cryptium.coincurrently.core.config.AppLocale
import com.cryptium.coincurrently.sharedui.theme.onSurfaceDimmest
import com.cryptium.coincurrently.sharedui.modifiers.CappedFontScale
import com.cryptium.coincurrently.sharedui.modifiers.DynamicTypeSizeCap
import com.cryptium.coincurrently.core.extensions.poppinsRegular
import com.cryptium.coincurrently.sharedui.theme.primaryTextColor
import com.cryptium.coincurrently.sharedui.modifiers.shimmeringModifier
import com.cryptium.coincurrently.core.extensions.is24HourClock
import com.cryptium.coincurrently.core.extensions.isAtMidnight
import com.cryptium.coincurrently.core.extensions.roundToNextWholeHour
import com.cryptium.coincurrently.core.extensions.satoshiBold
import com.cryptium.coincurrently.sharedui.charts.hideFromTalkBackWhenEnabled
import com.cryptium.coincurrently.core.networking.ResponseModels.TimeCharts
import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone
import kotlin.math.floor

@Composable
fun ChartTimeStamps(
    currentTimeChart: TimeCharts,
    timeStamps: List<Double>?,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val numberOfSteps = if(!context.is24HourClock && currentTimeChart <= TimeCharts.aDay) 4 else 5

    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier
            .hideFromTalkBackWhenEnabled()
            .padding(
                top = 12.dp
            )
    ) {
        CappedFontScale(DynamicTypeSizeCap.Large) {
        for (index in 1 until numberOfSteps + 1) {
            val date = getTimeChartDates(
                timeStamps,
                currentTimeChart,
                at = index,
                numberOfSteps
            )

            Text(
                text = date ?: "000",
                style = satoshiBold(12),
                color = MaterialTheme.colors.onSurfaceDimmest,
                modifier = Modifier
                    .widthIn(min = 40.dp)
                    .weight(1f)
                    .wrapContentWidth(Alignment.CenterHorizontally)
                    .shimmeringModifier(date == null)
                    .hideFromTalkBackWhenEnabled()
            )
        }
        }
    }
}

@Composable
private fun getTimeChartDates(
    timeStamps: List<Double>?,
    currentTimeChart: TimeCharts,
    at: Int,
    numberOfSteps: Int
): String? {
    val localTimeStamps = timeStamps ?: return null

    val context = LocalContext.current
    var format: String = when (currentTimeChart) {
        TimeCharts.aWeek, TimeCharts.aMonth, TimeCharts.threeMonths, TimeCharts.sixMonths -> { "MMM dd" }
        TimeCharts.aYear, TimeCharts.twoYears -> { "MMM" }
        TimeCharts.max -> { "YYYY" }
        else -> { if (context.is24HourClock) "HH:mm" else "hh:mm a" }
    }

    val labelCount = numberOfSteps
    val maxIndex = (localTimeStamps.size - 1).coerceAtLeast(0)
    val fraction = (at - 0.5) / labelCount
    val currentIndex = floor(maxIndex * fraction).toInt().coerceIn(0, maxIndex)
    val currentValue: Long = localTimeStamps.getOrNull(currentIndex)?.toLong() ?: return null

    val dateFormat = SimpleDateFormat(format, AppLocale.enUsPosix)
    dateFormat.timeZone = TimeZone.getDefault()
    return dateFormat.format(Date(currentValue))
}