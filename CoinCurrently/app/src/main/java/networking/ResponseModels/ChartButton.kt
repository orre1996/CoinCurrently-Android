package networking.ResponseModels

import com.cryptium.coincurrently.ui.main.General.GlobalFunctions
import com.cryptium.coincurrently.ui.main.General.PreferredTimeframe
import com.cryptium.coincurrently.ui.main.Managers.StorageKeys
import com.cryptium.coincurrently.ui.main.Managers.StorageManager
import kotlinx.serialization.Serializable

@Serializable
data class ChartButton(
    val time: String,
    val days: String,
    val timeChart: TimeCharts
)

enum class TimeCharts(val label: String) {
    anHour("1H"),
    aDay("1D"),
    aWeek("7D"),
    aMonth("1M"),
    threeMonths("3M"),
    aYear("1Y"),
    max("ALL"),
    sixMonths("6M"),
    twoYears("2Y");

    fun timeframeFormatted(): String {
        return when (this) {
            anHour-> "1h:"
            aDay -> "1d:"
            aWeek -> "7d:"
            aMonth -> "30d:"
            threeMonths -> "3m"
            aYear -> "1y"
            max -> "All"
            sixMonths -> "6m:"
            twoYears -> "2y:"
        }
    }

    val timeAgoText: String
        get() {
            return when (this) {
                anHour-> "This hour"
                aDay -> "Today"
                aWeek -> "Past Week"
                aMonth -> "Past Month"
                threeMonths -> "Past 3 Months"
                aYear -> "Past Year"
                max -> "All Time"
                sixMonths -> "Past 6 Months"
                twoYears -> "Past 2 Years"
            }
        }

    val rangeTitle: String
        get() = when (this) {
            anHour -> "1 Hour Range"
            aDay -> "24 Hour Range"
            aWeek -> "1 Week Range"
            aMonth -> "1 Month Range"
            threeMonths -> "3 Month Range"
            aYear -> "1 Year Range"
            max -> "Full Range"
            sixMonths -> "6 Month Range"
            twoYears -> "2 Year Range"
        }

    val accessibilityText: String
        get() = when (this) {
            anHour -> "1 hour"
            aDay -> "24 hour"
            aWeek -> "7 days"
            aMonth -> "1 month"
            threeMonths -> "3 months"
            aYear -> "1 year"
            max -> "max"
            sixMonths -> "6 months"
            twoYears -> "2 years"
        }

    val accessibilityTimeframe: String
        get() = when (this) {
            anHour -> "1 hour"
            aDay -> "24 hour"
            aWeek -> "7 days"
            aMonth -> "30 days"
            threeMonths -> "3 months"
            aYear -> "1 year"
            max -> "all time"
            sixMonths -> "180 days"
            twoYears -> "2 years"
        }

    val accessibilityChangePeriod: String
        get() = when (this) {
            anHour -> "in the last hour"
            aDay -> "in the last 24 hours"
            aWeek -> "in the last 7 days"
            aMonth -> "in the last 30 days"
            threeMonths -> "in the last 3 months"
            aYear -> "in the last year"
            max -> "over all time"
            sixMonths -> "in the last 6 months"
            twoYears -> "in the last 2 years"
        }

    val days: String
        get() {
            return when (this) {
                anHour, aDay -> "1"
                aWeek -> "7"
                aMonth -> "30"
                threeMonths -> "90"
                aYear -> "365"
                max -> "max"
                sixMonths -> "180"
                twoYears -> "730"
            }
        }

    val interval: String?
        get() {
            return when (this) {
                anHour, aDay, aWeek -> null
                else -> "daily"
            }
        }

    companion object {

        /** Persists the user’s last chart period (used by [getStartTimeFrameBetween]). */
        fun saveChartTimeframe(timeCharts: TimeCharts) {
            StorageManager.set(StorageKeys.CHART_SELECTED_TIMEFRAME, timeCharts)
        }

        /**
         * Returns the stored chart period clamped to [from]...[to].
         * If nothing is stored, defaults to [aWeek] (matches iOS Settings default).
         */
        fun getStartTimeFrameBetween(from: TimeCharts, to: TimeCharts): TimeCharts {
            val current = StorageManager.get<TimeCharts>(StorageKeys.CHART_SELECTED_TIMEFRAME) ?: aWeek
            return when {
                current.ordinal < from.ordinal -> from
                current.ordinal > to.ordinal -> to
                else -> current
            }
        }

        fun getPreferredTimeChart(fromFearGreed: Boolean): TimeCharts {
            return when (GlobalFunctions.getPreferredTimeframe()) {
                PreferredTimeframe.DAILY -> {
                    if (fromFearGreed) aWeek else aDay
                }
                PreferredTimeframe.WEEKLY -> {
                    aWeek
                }
                PreferredTimeframe.MONTHLY -> {
                    aMonth
                }
            }
        }

        fun getButtons(from: TimeCharts, to: TimeCharts): List<ChartButton> {
            return (from.ordinal..to.ordinal).mapNotNull { rawValue ->
                val timeChart = entries.find { it.ordinal == rawValue }
                timeChart?.let {
                    ChartButton(
                        time = it.label,
                        days = it.days,
                        timeChart = it,
                    )
                }
            }
        }

        private val treasuryTimeCharts = listOf(
            TimeCharts.aWeek,
            TimeCharts.aMonth,
            TimeCharts.threeMonths,
            TimeCharts.sixMonths,
            TimeCharts.aYear,
            TimeCharts.twoYears,
        )

        fun treasuryDetailButtons(): List<ChartButton> {
            return treasuryTimeCharts.map {
                ChartButton(
                    time = it.label,
                    days = it.days,
                    timeChart = it,
                )
            }
        }

        fun getTreasuryStartTimeFrame(): TimeCharts {
            val current = StorageManager.get<TimeCharts>(StorageKeys.CHART_SELECTED_TIMEFRAME) ?: aWeek
            if (current in treasuryTimeCharts) {
                return current
            }
            return when (current) {
                anHour, aDay -> aWeek
                max -> twoYears
                else -> aWeek
            }
        }
    }
}