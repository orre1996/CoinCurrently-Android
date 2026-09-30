package com.cryptium.coincurrently.features.GasFees
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.cryptium.coincurrently.core.extensions.DateFormat
import com.cryptium.coincurrently.core.extensions.convertStringToDate
import com.cryptium.coincurrently.core.extensions.formatCurrency
import com.cryptium.coincurrently.core.extensions.numberAccessible
import com.cryptium.coincurrently.core.localization.Localization
import com.cryptium.coincurrently.sharedui.charts.ChartHoverController
import com.cryptium.coincurrently.features.GasFees.GasUnits
import com.cryptium.coincurrently.features.GasFees.GasFeesRepository
import com.cryptium.coincurrently.features.Market.Toplist.Repositories.ToplistRepository
import com.cryptium.coincurrently.core.networking.ResponseModels.GasFeesModel
import com.cryptium.coincurrently.core.networking.ResponseModels.TimeCharts
import com.cryptium.coincurrently.core.networking.ResponseModels.Toplist

class GasFeesViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return GasFeesViewModelImp() as T
    }
}

interface GasFeesViewModel {
    var gasFees: GasFeesModel?
    var gasFeePrices: GasFeesModel?
    var ethereum: Toplist?

    val currentTimeFrames: List<Pair<String?, String>>
    val historicTimeframes: List<Pair<Int, String>>
    val normalizedPosition: Double
    var isDragging: Boolean
    var prices: MutableState<List<Double>>
    var allPrices: MutableState<List<Double>>
    var timeStamps: MutableState<List<Double>>
    var dragPosition: MutableState<Dp>
    var chartSize: MutableState<DpSize>
    var viewSize: MutableState<DpSize>
    val hoverPriceTextWidth: MutableState<Dp>
    val chartHover: ChartHoverController

    fun updateHoverStats(touchXs: List<Dp>, is24HourClock: Boolean)

    suspend fun getGasFees()
    suspend fun getGasFeePrices()
    suspend fun getEthereumPrice()
    fun getGasPrice(gasUnit: GasUnits, accessible: Boolean = false): String?
    fun getCurrentTextAccessible(index: Int, value: String): String
}

class GasFeesViewModelImp: ViewModel(), GasFeesViewModel {
    private val gasFeesRepository by lazy { GasFeesRepository }
    private val toplistRepository by lazy { ToplistRepository }

    override var gasFees: GasFeesModel? by mutableStateOf(null)
    override var gasFeePrices: GasFeesModel? by mutableStateOf(null)
    override var ethereum: Toplist? by mutableStateOf(null)

    override var isDragging by mutableStateOf(false)
    override var prices = mutableStateOf(listOf<Double>())
    override var allPrices = mutableStateOf(listOf<Double>())
    override var timeStamps = mutableStateOf(listOf<Double>())
    override var dragPosition = mutableStateOf(0.dp)
    override var chartSize = mutableStateOf(DpSize.Zero)
    override var viewSize = mutableStateOf(DpSize.Zero)
    override val hoverPriceTextWidth = mutableStateOf(0.dp)
    override val chartHover = ChartHoverController()

    override val currentTimeFrames: List<Pair<String?, String>>
        get() = listOf(
            Pair(gasFees?.gasFeeData?.firstOrNull()?.safeGasPrice, "Current Low"),
            Pair(gasFees?.gasFeeData?.firstOrNull()?.proposeGasPrice, "Current Average"),
            Pair(gasFees?.gasFeeData?.firstOrNull()?.fastGasPrice, "Current High")
        )

    override val historicTimeframes: List<Pair<Int, String>>
        get() = listOf(
            Pair(0, "Now"),
            Pair(1, "Yesterday"),
            Pair(7, "Last week"),
            Pair(30, "Last month")
        )

    init {
        gasFees = gasFeesRepository.cachedGasFees
        gasFeePrices = gasFeesRepository.cachedGasFeesPrices
        setPrices()
    }

    override fun updateHoverStats(touchXs: List<Dp>, is24HourClock: Boolean) {
        chartHover.updateHoverStats(
            touchXs = touchXs,
            chartWidth = chartSize.value.width,
            prices = prices.value.dropLast(2),
            dates = timeStamps.value,
            timeChart = TimeCharts.aWeek,
            is24HourClock = is24HourClock,
            hoverPrices = prices.value.dropLast(2),
            dontShowTime = true,
        )
        isDragging = chartHover.isDragging
        dragPosition.value = chartHover.dragPosition.value
    }

    override suspend fun getGasFees() {
        gasFeesRepository.getGasFees()?.let {
            gasFees = it
        }
    }

    override suspend fun getGasFeePrices() {
        gasFeesRepository.getGasFeesPrices()?.let {
            gasFeePrices = it
            setPrices()
        }
    }

    override suspend fun getEthereumPrice() {
        toplistRepository.getSmallToplist()?.let {
            it.firstOrNull { it.id == "ethereum" }?.let {
                ethereum = it
            }
        }
    }

    override fun getGasPrice(gasUnit: GasUnits, accessible: Boolean): String? {
        val gasPrice = gasFees?.gasFeeData?.firstOrNull()?.proposeGasPrice?.toDoubleOrNull() ?: return null
        val ethPrice = ethereum?.current_price ?: return null

        val gasUnits = gasUnit.value
        val cost = (gasPrice / 1_000_000_000) * gasUnits * ethPrice

        return cost.formatCurrency(maxDecimals = 2, accessible = accessible)
    }

    override fun getCurrentTextAccessible(index: Int, value: String): String {
        val formattedValue = value.toDoubleOrNull()?.numberAccessible(maxPlaces = 3) ?: "0"
        val unit = Localization.string("Gwei")
        val key = when (index) {
            0 -> "The current low Ethereum gas fee is %@ %@"
            1 -> "The current average Ethereum gas fee is %@ %@"
            else -> "The current high Ethereum gas fee is %@ %@"
        }
        return Localization.format(key, formattedValue, unit)
    }

    override val normalizedPosition: Double get() {
        val value = gasFees?.gasFeeData?.firstOrNull()?.proposeGasPrice?.toDoubleOrNull() ?: return 0.0

        val segments = listOf(
            0.0 to 3.0,
            3.0 to 10.0,
            10.0 to 30.0,
            30.0 to 80.0,
            80.0 to 100.0
        )

        val segmentWidth = 1.0 / segments.size

        for ((index, segment) in segments.withIndex()) {
            val (start, end) = segment
            if (value in start..end) {
                val range = end - start
                val localProgress = (value - start) / range
                return index * segmentWidth + localProgress * segmentWidth
            }
        }

        // Clamp values outside range
        if (value < segments.first().first) return 0.0
        if (value > segments.last().second) return 1.0

        return 0.0
    }

    private fun setPrices() {
        val data = gasFeePrices?.gasFeeData ?: return
        if (data.isEmpty()) return

        val array = data.take(7).asReversed()

        val cgFloatArray = array.map { it.proposeGasPrice.toDoubleOrNull() ?: 0.0 }.toMutableList()
        cgFloatArray.addAll(listOf(0.0, 10.0))

        val doubleArray = array.map { it.sK_Date.convertStringToDate(dateFormat = DateFormat.yyyyMMddTHHmmssSSSZ)?.time?.toDouble() ?: 0.0 }

        prices.value = cgFloatArray
        timeStamps.value = doubleArray
        allPrices.value = data.map { it.proposeGasPrice.toDoubleOrNull() ?: 0.0 }.toMutableList()
    }
}