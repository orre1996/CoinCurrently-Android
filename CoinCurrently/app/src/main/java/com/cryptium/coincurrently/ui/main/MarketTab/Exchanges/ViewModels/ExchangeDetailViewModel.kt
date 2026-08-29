package com.cryptium.coincurrently.ui.main.MarketTab.Exchanges.ViewModels
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import androidx.compose.ui.unit.min
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.cryptium.coincurrently.AppLocale
import com.cryptium.coincurrently.ui.main.Compose.onSurfaceDim
import com.cryptium.coincurrently.ui.main.CustomViews.getChangeColorCompose
import com.cryptium.coincurrently.ui.main.Extensions.formatCurrency
import com.cryptium.coincurrently.ui.main.Extensions.getDifferenceInPercent
import com.cryptium.coincurrently.ui.main.Extensions.getSelectedCurrency
import com.cryptium.coincurrently.ui.main.Extensions.is24HourClock
import com.cryptium.coincurrently.ui.main.General.Enums.FormattingThreshold
import com.cryptium.coincurrently.ui.main.General.Utilities.ChartHoverController
import com.cryptium.coincurrently.ui.main.General.Utilities.Point
import com.cryptium.coincurrently.ui.main.General.Utilities.currencyDifferenceValue
import com.cryptium.coincurrently.ui.main.General.Utilities.differencePercent
import com.cryptium.coincurrently.ui.main.General.Utilities.dragPositions
import com.cryptium.coincurrently.ui.main.General.Utilities.getPoint
import com.cryptium.coincurrently.ui.main.General.Utilities.getPriceXPosition
import com.cryptium.coincurrently.ui.main.General.Utilities.hoverDragIndex
import com.cryptium.coincurrently.ui.main.General.Utilities.multiHoverGraphColor
import com.cryptium.coincurrently.ui.main.General.Enums.PriceChange
import kotlin.math.roundToInt
import com.cryptium.coincurrently.ui.main.General.Enums.SortOption
import com.cryptium.coincurrently.ui.main.Managers.StorageKeys
import com.cryptium.coincurrently.ui.main.Managers.StorageManager
import com.cryptium.coincurrently.ui.main.Repositories.CurrencyRepository
import com.cryptium.coincurrently.ui.main.Repositories.ExchangeDetailsRepostory
import com.cryptium.coincurrently.ui.main.Repositories.NewsRepository
import com.cryptium.coincurrently.ui.main.Repositories.ToplistRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import networking.NewsCategory
import networking.ResponseModels.*
import java.text.SimpleDateFormat
import java.util.Date

class ExchangeDetailViewModelFactory(
    val exchange: ExchangeResponse
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ExchangeDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ExchangeDetailViewModel(
                exchange
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class ExchangeDetailViewModel(
    exchange: ExchangeResponse
) : ViewModel() {

    private val exchangeDetailsRepostory by lazy { ExchangeDetailsRepostory }
    private val newsRepository by lazy { NewsRepository }
    private val currencyRepository by lazy { CurrencyRepository }
    private val toplistRepository by lazy { ToplistRepository }

    var toplist: List<Toplist>? by mutableStateOf(null)
    var prices: List<Double> by mutableStateOf(listOf())
    var timeStamps: List<Double> by mutableStateOf(listOf())

    var dragPosition = mutableStateOf(0.dp)
    var chartSize = mutableStateOf(DpSize.Zero)
    val hoverPriceTextWidth = mutableStateOf(0.dp)
    val chartHover = ChartHoverController()

    var isRefreshing: Boolean by mutableStateOf(false)

    var currentDays by mutableStateOf(
        TimeCharts.getStartTimeFrameBetween(TimeCharts.aDay, TimeCharts.aYear).days,
    )

    var isDragging by mutableStateOf(false)


    var exchangeInfo: ExchangeResponse? by mutableStateOf(null)
    var exchangeVolume: List<ExchangePriceModel>? by mutableStateOf(null)

    var bitcoin: CryptoDetailInfoModel? by mutableStateOf(null)
    var newsData: List<NewNewsItem>? by mutableStateOf(null)
    var showNewsError: Boolean by mutableStateOf(false)
    var showMainPriceError: Boolean by mutableStateOf(false)

    var mainReloading: Boolean by mutableStateOf(false)
    var selectedTimeFrame by mutableStateOf(
        TimeCharts.getStartTimeFrameBetween(TimeCharts.aDay, TimeCharts.aYear),
    )

    var selectedSortOption by mutableStateOf(SortOption.FloorPriceAsc)

    init {
        exchangeInfo = exchange

        exchangeDetailsRepostory.loadCachedDataForExchange(exchangeInfo?.id ?: "", currentDays)

        exchangeDetailsRepostory.exchangeDetail?.get(exchangeInfo?.id)?.let {
            exchangeInfo = it.copyWithId(exchange.id)
        }

        exchangeDetailsRepostory.exchangePrices?.get("${exchangeInfo?.id}-$currentDays")?.let { cached ->
            exchangeVolume = cached
        }

        bitcoin = currencyRepository.bitcoinData

        val newsId = exchangeInfo?.name?.split(" ")?.first() ?: ""
        newsRepository.cachedNews?.get(NewsCategory.Exchange(newsId).type)?.let {
            newsData = it.data.data
        }

        toplistRepository.toplist?.get("${SortOption.MktCapDesc.getOptionKey(PriceChange.SevenDays)}-null")?.data?.let { cachedToplist ->
            if (cachedToplist.isNotEmpty()) {
                toplist = cachedToplist
            }
        }

        exchangeVolume?.let {
            setPrices()
            setTimestamps()
        }

        selectedSortOption = StorageManager.get<SortOption>(StorageKeys.EXCHANGE_DETAIL_SORT_OPTION) ?: SortOption.VolumeDesc
    }

    suspend fun getExchangeInfo(
        id: String?
    ) {
        exchangeDetailsRepostory.getExchange(id)?.let {
            exchangeInfo = it
        }
    }

    suspend fun getExchangePrices(
        id: String?,
        days: String
    ) {
        exchangeVolume = exchangeDetailsRepostory.getExchangePrices(id, days)
        showMainPriceError = exchangeVolume == null
    }

    val hoverPrice: String @Composable get() {
        return chartHover.hoverHelper.hoverPrices.firstOrNull()
            ?.formatCurrency(threshold = FormattingThreshold.MILLION)
            ?: prices.getOrNull(dragIndex)?.formatCurrency(threshold = FormattingThreshold.MILLION)
            ?: "N/A"
    }

    val hoverTimePosition: Dp @Composable get() {
        return getXPosition(hoverPriceTextWidth.value) - hoverPriceTextWidth.value / 2
    }

    val dragIndex: Int @Composable get() {
        if (prices.size <= 1) return 0
        return chartHover.hoverDragIndex(chartSize.value.width, prices.size)
    }

    val dragPoint: Point
        @Composable get() {
            return getPoint(
                index = dragIndex,
                rect = chartSize.value,
                prices = prices
            )
        }

    val dragPositions: List<Point> @Composable get() = chartHover.dragPositions(chartSize.value, prices)

    val multiHoverGraphColor: Color @Composable get() = chartHover.multiHoverGraphColor(mainChartColor)

    val differenceValue: String @Composable get() = chartHover.currencyDifferenceValue()

    val differencePercent: String @Composable get() = chartHover.differencePercent()

    val hoverTime: String @Composable get() {
        return chartHover.hoverHelper.hoverTimes.firstOrNull() ?: run {
            val context = LocalContext.current
            var formatter = if (context.is24HourClock) "dd MMM 'at' HH:mm" else "dd MMM 'at' hh:mm a"

            when (selectedTimeFrame) {
                TimeCharts.aMonth,
                TimeCharts.threeMonths,
                TimeCharts.aYear -> { formatter = "dd MMM YYYY" }
                TimeCharts.max -> { formatter = "MMM YYYY" }
                else -> {}
            }

            if (timeStamps.isEmpty()) {
                return@run ""
            }

            val currentValue = timeStamps.getOrNull(dragIndex)?.toLong() ?: 0
            SimpleDateFormat(formatter, AppLocale.enUsPosix).format(Date(currentValue))
        }
    }

    fun updateHoverStats(touchXs: List<Dp>, is24HourClock: Boolean) {
        chartHover.updateHoverStats(
            touchXs = touchXs,
            chartWidth = chartSize.value.width,
            prices = prices,
            dates = timeStamps,
            timeChart = selectedTimeFrame,
            is24HourClock = is24HourClock,
            isBlocked = mainReloading,
        )
        isDragging = chartHover.isDragging
        dragPosition.value = chartHover.dragPosition.value
    }

    val changePercent: Double? get() {
        val first = prices.firstOrNull() ?: return null
        val last = prices.lastOrNull() ?: return null

        return getDifferenceInPercent(last, first)
    }

    val mainChartColor: Color @Composable get() {
        return changePercent?.getChangeColorCompose() ?: MaterialTheme.colors.onSurfaceDim
    }

    val mainGraphStats: List<String?> get() {
        return (0 until 6).map { getPriceInChart(prices, it) }
    }

    private fun getPriceInChart(prices: List<Double>, at: Int): String? {
        val min = prices.minOfOrNull { it } ?: return null
        val max = prices.maxOfOrNull { it } ?: return null
        val priceSteps = (max - min) / 5
        val value = min + (priceSteps * at.toDouble())

        return value.formatCurrency(currency = CurrencySelectionModel.empty())
    }

    suspend fun getBitcoin() {
        currencyRepository.getBitcoin()?.let {
            bitcoin = it
        }
    }

    suspend fun getNews() {
        newsData = null

        newsRepository.getNewNews(
            25,
            NewsCategory.Exchange(exchangeInfo?.name?.split(" ")?.first() ?: "")
        )?.let {
            newsData = it.data
        } ?: run {
            showNewsError = true
        }
    }

    suspend fun getToplist() {
        toplistRepository.getToplist(
            sortOption = SortOption.MktCapDesc,
            priceChange = PriceChange.SevenDays,
            platformId = null
        )?.let {
            if (it.isNotEmpty()) {
                toplist = it
            }
        }
    }

    suspend fun refresh(
        newDays: String? = null,
        newTimeChart: TimeCharts? = null,
        forceId: String? = null,
        getNews: Boolean = true
    ) {
        coroutineScope {
            mainReloading = true

            val days = newDays ?: currentDays
            val chart = newTimeChart ?: selectedTimeFrame
            val id = forceId ?: exchangeInfo?.id

            val cryptoInfo = async { getExchangeInfo(id) }
            val priceVolumeModel = async { getExchangePrices(id, days) }
            val bitcoin = async { getBitcoin() }
            val news = async { if (getNews) getNews() }

            cryptoInfo.await()
            priceVolumeModel.await()
            bitcoin.await()
            news.await()

            currentDays = days
            selectedTimeFrame = chart

            setPrices()
            setTimestamps()

            mainReloading = false
        }
    }

    fun setPrices() {
        val btcPrice = bitcoin
            ?.market_data
            ?.current_price
            ?.getSelectedCurrency()
            ?: 0.0

        prices = exchangeVolume
            ?.map { (it.price ?: 0.0) * btcPrice }
            ?: emptyList()
    }

    fun setTimestamps() {
        timeStamps = exchangeVolume
            ?.mapNotNull { it.timestamp }
            ?: emptyList()
    }

    @Composable
    fun getXPosition(textWidth: Dp): Dp {
        val xPosition = getPoint(
            index = dragIndex,
            rect = chartSize.value,
            prices = prices
        ).x

        return getPriceXPosition(xPosition, textWidth, chartSize.value.width)
    }

    fun setNewSortOption(newSortOption: SortOption) {
        selectedSortOption = newSortOption
        StorageManager.set(StorageKeys.EXCHANGE_DETAIL_SORT_OPTION, newSortOption)
    }
}
