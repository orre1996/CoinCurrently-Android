package com.cryptium.coincurrently.features.ExchangeDetail
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.cryptium.coincurrently.core.extensions.formatCurrency
import com.cryptium.coincurrently.core.extensions.getDifferenceInPercent
import com.cryptium.coincurrently.core.extensions.getSelectedCurrency
import com.cryptium.coincurrently.sharedui.charts.ChartHoverController
import com.cryptium.coincurrently.sharedui.models.PriceChange
import com.cryptium.coincurrently.sharedui.models.SortOption
import com.cryptium.coincurrently.core.persistence.StorageKeys
import com.cryptium.coincurrently.core.persistence.StorageManager
import com.cryptium.coincurrently.core.persistence.CurrencyRepository
import com.cryptium.coincurrently.features.ExchangeDetail.ExchangeDetailsRepostory
import com.cryptium.coincurrently.features.News.NewsRepository
import com.cryptium.coincurrently.features.Market.Repositories.ToplistRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import com.cryptium.coincurrently.core.networking.NewsCategory
import com.cryptium.coincurrently.core.networking.ResponseModels.*

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

    fun setNewSortOption(newSortOption: SortOption) {
        selectedSortOption = newSortOption
        StorageManager.set(StorageKeys.EXCHANGE_DETAIL_SORT_OPTION, newSortOption)
    }
}
