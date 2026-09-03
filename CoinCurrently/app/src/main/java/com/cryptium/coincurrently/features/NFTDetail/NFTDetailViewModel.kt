package com.cryptium.coincurrently.features.NFTDetail
import android.content.Context
import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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
import com.cryptium.coincurrently.core.config.AppLocale
import com.cryptium.coincurrently.sharedui.theme.onSurfaceDim
import com.cryptium.coincurrently.sharedui.charts.getChangeColorCompose
import com.cryptium.coincurrently.core.extensions.formatCurrency
import com.cryptium.coincurrently.core.extensions.getDifferenceInPercent
import com.cryptium.coincurrently.core.extensions.getSelectedCurrency
import com.cryptium.coincurrently.core.extensions.is24HourClock
import com.cryptium.coincurrently.sharedui.models.SortOption
import com.cryptium.coincurrently.sharedui.components.GlobalFunctions
import com.cryptium.coincurrently.sharedui.charts.ChartHoverController
import com.cryptium.coincurrently.sharedui.components.Point
import com.cryptium.coincurrently.sharedui.charts.currencyDifferenceValue
import com.cryptium.coincurrently.sharedui.charts.differencePercent
import com.cryptium.coincurrently.sharedui.charts.dragPositions
import com.cryptium.coincurrently.sharedui.components.getPoint
import com.cryptium.coincurrently.sharedui.components.getPriceXPosition
import com.cryptium.coincurrently.sharedui.charts.hoverDragIndex
import com.cryptium.coincurrently.sharedui.charts.multiHoverGraphColor
import com.cryptium.coincurrently.sharedui.components.CustomImageView
import kotlin.math.roundToInt
import com.cryptium.coincurrently.core.persistence.CurrencyRepository
import com.cryptium.coincurrently.features.News.NewsRepository
import com.cryptium.coincurrently.core.persistence.StorageKeys
import com.cryptium.coincurrently.core.persistence.StorageManager
import com.cryptium.coincurrently.features.GlobalMarket.GlobalMarketRepository
import com.cryptium.coincurrently.core.models.ChartTypes
import com.cryptium.coincurrently.features.NFTDetail.NFTDetailRepository
import com.cryptium.coincurrently.features.Market.Repositories.NFTRepository
import com.cryptium.coincurrently.core.analytics.ChartFiatNativeModeChanged
import com.cryptium.coincurrently.core.analytics.AnalyticsManager
import com.cryptium.coincurrently.core.analytics.ScreenLabel
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import com.cryptium.coincurrently.core.networking.NewsCategory
import com.cryptium.coincurrently.core.networking.ResponseModels.CryptoDetailInfoModel
import com.cryptium.coincurrently.core.networking.ResponseModels.CurrencySelectionModel
import com.cryptium.coincurrently.core.networking.ResponseModels.NFTDetail
import com.cryptium.coincurrently.core.networking.ResponseModels.NftDataPoint
import com.cryptium.coincurrently.core.networking.ResponseModels.NFTPriceVolumeModel
import com.cryptium.coincurrently.core.networking.ResponseModels.NFTTicker
import com.cryptium.coincurrently.core.networking.ResponseModels.NFTToplist
import com.cryptium.coincurrently.core.networking.ResponseModels.NewNewsItem
import com.cryptium.coincurrently.core.networking.ResponseModels.PriceVolumeModel
import com.cryptium.coincurrently.core.networking.ResponseModels.TimeCharts
import com.cryptium.coincurrently.core.networking.ResponseModels.toNFTDetail
import java.text.SimpleDateFormat
import java.util.*

class NFTDetailViewModelFactory(
    val nftToplist: NFTToplist
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NFTDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NFTDetailViewModel(
                nftToplist
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class NFTDetailViewModel(
    nftToplist: NFTToplist
) : ViewModel() {

    private val nftDetailRepository by lazy { NFTDetailRepository }
    private val newsRepository by lazy { NewsRepository }
    private val currencyRepository by lazy { CurrencyRepository }
    private val globalRepository by lazy { GlobalMarketRepository }
    private val nftsRepository by lazy { NFTRepository }

    var nftDetailInfo: NFTDetail? by mutableStateOf(null)
    var priceVolumeModel: NFTPriceVolumeModel? by mutableStateOf(null)
    var nftTickers: List<NFTTicker>? by mutableStateOf(null)
    var cryptoDetailModel: CryptoDetailInfoModel? by mutableStateOf(null)

    var nftDetailSmallImageBitmap: Bitmap? by mutableStateOf(null)

    var prices: List<Double> by mutableStateOf(listOf())
    var onlyPrices: List<Double> by mutableStateOf(listOf())
    var timeStamps: List<Double> by mutableStateOf(listOf())

    var dragPosition = mutableStateOf(0.dp)
    var chartSize = mutableStateOf(DpSize.Zero)
    val hoverPriceTextWidth = mutableStateOf(0.dp)
    val chartHover = ChartHoverController()

    var isRefreshing: Boolean by mutableStateOf(false)
    var currentChartType = mutableStateOf(StorageManager.get<ChartTypes>(StorageKeys.SELECTED_CHART_TYPE) ?: ChartTypes.PRICE)

    var currentDays by mutableStateOf(
        TimeCharts.getStartTimeFrameBetween(TimeCharts.anHour, TimeCharts.aYear).days,
    )

    var isDragging by mutableStateOf(false)

    var isChartFiat by mutableStateOf(false)

    var bitcoin: CryptoDetailInfoModel? by mutableStateOf(null)
    var newsData: List<NewNewsItem>? by mutableStateOf(null)
    var showNewsError: Boolean by mutableStateOf(false)
    var showMainPriceError: Boolean by mutableStateOf(false)

    var mainReloading: Boolean by mutableStateOf(false)
    var selectedTimeFrame by mutableStateOf(
        TimeCharts.getStartTimeFrameBetween(TimeCharts.anHour, TimeCharts.aYear),
    )
    var selectedCurrency: CurrencySelectionModel by mutableStateOf(CurrencySelectionModel("", "", "", "", "", true))

    var selectedSortOption by mutableStateOf(SortOption.FloorPriceDesc)

    init {
        nftDetailInfo = nftToplist.toNFTDetail

        nftDetailRepository.loadCachedDataForNft(nftDetailInfo?.id ?: "", currentDays)

        nftDetailRepository.nftDetail?.get(nftDetailInfo?.id)?.let {
            nftDetailInfo = it
        }

        nftDetailRepository.nftPrices?.get("${nftDetailInfo?.id}-$currentDays")?.let { cached ->
            priceVolumeModel = cached
        }

        bitcoin = currencyRepository.bitcoinData

        val newsId = nftDetailInfo?.name?.split(" ")?.firstOrNull() ?: ""
        newsRepository.cachedNews?.get(NewsCategory.NFT(newsId).type)?.let {
            newsData = it.data.data
        }

        setSelectedCurrency()

        priceVolumeModel?.let {
            setPrices()
            setOnlyPrices()
            setTimestamps()
        }

        selectedSortOption = StorageManager.get<SortOption>(StorageKeys.NFT_DETAIL_MARKETS_SORT_OPTION) ?: SortOption.FloorPriceDesc
    }

    fun setSelectedCurrency() {
        val fiatCurrency = GlobalFunctions.getSelectedCurrency()

        if (isChartFiat) {
            selectedCurrency = fiatCurrency
            return
        }

        val symbol = nftDetailInfo?.native_currency_symbol?.trim()?.uppercase()
            ?: globalRepository.coinsList?.firstOrNull {
                it.id?.lowercase() == (nftDetailInfo?.native_currency?.lowercase()
                    ?: "") || it.name?.lowercase() == (nftDetailInfo?.native_currency?.lowercase()
                    ?: "")
            }?.symbol?.uppercase()
            ?: ""

        if (symbol.isEmpty()) {
            selectedCurrency = CurrencySelectionModel("", "", "", "", "", true)
            return
        }

        val spelledSymbol = symbol.toCharArray().joinToString(" ") { it.toString() }
        selectedCurrency = CurrencySelectionModel(
            currency = spelledSymbol,
            code = symbol,
            symbol = symbol,
            suffix = true,
            readAccessibilityAsSymbol = true,
        )
    }

    suspend fun getNftDetailInfo(context: Context, id: String?) {
        nftDetailRepository.getNftDetailInfo(id)?.let {
            nftDetailInfo = it
            setSelectedCurrency()
            nftDetailSmallImageBitmap = fetchNftSmallIcon(context, it.image?.small)
        }
    }

    /** Bitmap is copied inside [CustomImageView.setImageWithCompletion] before Glide recycles pooled data. */
    private suspend fun fetchNftSmallIcon(context: Context, url: String?): Bitmap? {
        if (url.isNullOrBlank()) return null
        val appContext = context.applicationContext
        return suspendCancellableCoroutine { continuation ->
            val imageView = CustomImageView(appContext)
            val mainHandler = Handler(Looper.getMainLooper())
            imageView.setImageWithCompletion(url, context) { _, detachedCopy ->
                mainHandler.post {
                    if (continuation.isActive) {
                        continuation.resume(detachedCopy)
                    } else {
                        detachedCopy?.takeUnless { it.isRecycled }?.recycle()
                    }
                }
            }
        }
    }

    suspend fun getCurrentCurrency() {
        val platforms = nftsRepository.getNFTPlatforms() ?: return

        val nativeId = platforms.firstOrNull { it.id == nftDetailInfo?.asset_platform_id }?.native_coin_id

        currencyRepository.getCryptoDetail(nativeId)?.let {
            cryptoDetailModel = it
        }
    }

    suspend fun getMainCurrencyPrices(id: String?, days: String) {
        priceVolumeModel = nftDetailRepository.getNftPriceVolume(id, days)

        showMainPriceError = priceVolumeModel == null
    }

    suspend fun getBitcoin() {
        currencyRepository.getBitcoin()?.let {
            bitcoin = it
        }
    }

    suspend fun getTickers(id: String?) {
        nftTickers = nftDetailRepository.getNFTTickers(id)
    }

    suspend fun getNews() {
        newsData = null

        newsRepository.getNewNews(
            25,
            NewsCategory.Exchange(nftDetailInfo?.name?.split(" ")?.firstOrNull() ?: "")
        )?.let {
            newsData = it.data
        } ?: run {
            showNewsError = true
        }
    }

    suspend fun refresh(
        context: Context,
        newDays: String? = null,
        newTimeChart: TimeCharts? = null,
        forceId: String? = null,
        fetchNews: Boolean = true,
    ) {
        val appContext = context.applicationContext
        coroutineScope {
            mainReloading = true

            val days = newDays ?: currentDays
            val chart = newTimeChart ?: selectedTimeFrame
            val id = forceId ?: nftDetailInfo?.id

            val cryptoInfo = async { getNftDetailInfo(appContext, id) }
            val priceVolume = async { getMainCurrencyPrices(id, days) }
            val tickers = async { getTickers(id) }
            val btc = async { getBitcoin() }
            val news = async { if (fetchNews) getNews() }
            val cryptoDetail = async {
                cryptoInfo.await()
                getCurrentCurrency()
            }

            cryptoInfo.await()
            priceVolume.await()
            tickers.await()
            btc.await()
            news.await()
            cryptoDetail.await()

            currentDays = days
            selectedTimeFrame = chart

            setPrices()
            setOnlyPrices()
            setTimestamps()

            mainReloading = false
        }
    }

    fun toggleCurrencyType() {
        isChartFiat = !isChartFiat
        AnalyticsManager.log(
            ChartFiatNativeModeChanged(
                fromScreen = ScreenLabel.NftDetail,
                isChartFiat = isChartFiat,
            ),
        )
        setSelectedCurrency()
        setTimestamps()
        setPrices()
        setOnlyPrices()
    }

    fun setChartType(type: ChartTypes) {
        currentChartType.value = type
        StorageManager.set(StorageKeys.SELECTED_CHART_TYPE, currentChartType.value)

        setPrices()
    }

    private fun getChartDataPoints(): List<NftDataPoint>? = when (currentChartType.value) {
        ChartTypes.PRICE -> if (isChartFiat) priceVolumeModel?.floor_price_usd else priceVolumeModel?.floor_price_native
        ChartTypes.VOLUME -> if (isChartFiat) priceVolumeModel?.h24_volume_usd else priceVolumeModel?.h24_volume_native
        else -> if (isChartFiat) priceVolumeModel?.market_cap_usd else priceVolumeModel?.market_cap_native
    }

    private fun getPriceOnlyDataPoints(): List<NftDataPoint>? =
        if (isChartFiat) priceVolumeModel?.floor_price_usd else priceVolumeModel?.floor_price_native

    private fun getPricesRangePrefix(priceVolume: NFTPriceVolumeModel?): Int {
        return when (selectedTimeFrame) {
            TimeCharts.anHour -> { ((priceVolume?.floor_price_usd?.size ?: 0) / 24) }
            else -> (priceVolume?.floor_price_usd?.size ?: 0)
        }
    }

    private fun setOnlyPrices() {
        val prefix = getPricesRangePrefix(priceVolumeModel)

        val multiplier = if (isChartFiat) (getPriceMultiplier() ?: 1.0) else 1.0
        onlyPrices = getPriceOnlyDataPoints()?.take(prefix)?.map { it.price?.times(multiplier) ?: 0.0 } ?: emptyList()
    }

    private fun setPrices() {
        val prefix = getPricesRangePrefix(priceVolumeModel)

        val multiplier = if (isChartFiat) (getPriceMultiplier() ?: 1.0) else 1.0
        this.prices = getChartDataPoints()?.take(prefix)?.map { it.price?.times(multiplier) ?: 0.0 }?.filter { it != 0.0 } ?: emptyList()
    }

    private fun setTimestamps() {
        val prefix = getPricesRangePrefix(priceVolumeModel)

        this.timeStamps = getChartDataPoints()?.take(prefix)?.map { it.timestamp ?: 0.0 }?.filter { it != 0.0 } ?: emptyList()
    }

    val hoverPrice: String @Composable get() {
        return chartHover.hoverHelper.hoverPrices.firstOrNull()
            ?.formatCurrency(currency = selectedCurrency)
            ?: prices.getOrNull(dragIndex)?.formatCurrency(currency = selectedCurrency)
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

    fun getPriceMultiplier(): Double? {
        if (bitcoin == null) {
            return null
        }

        val target = bitcoin?.market_data?.current_price?.getSelectedCurrency() ?: 0.0
        val base = bitcoin?.market_data?.current_price?.getSelectedCurrency(currency = "USD") ?: 0.0
        return target / base
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
        StorageManager.set(StorageKeys.NFT_DETAIL_MARKETS_SORT_OPTION, newSortOption)
    }
}
