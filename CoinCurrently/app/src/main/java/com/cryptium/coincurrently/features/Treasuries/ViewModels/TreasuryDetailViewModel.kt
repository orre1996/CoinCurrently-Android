package com.cryptium.coincurrently.features.Treasuries.ViewModels
import com.cryptium.coincurrently.core.persistence.UserPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.cryptium.coincurrently.core.extensions.getSelectedCurrency
import com.cryptium.coincurrently.core.extensions.formatCurrency
import com.cryptium.coincurrently.core.extensions.getDifferenceInPercent
import com.cryptium.coincurrently.core.extensions.roundTo
import com.cryptium.coincurrently.sharedui.models.PriceChange
import com.cryptium.coincurrently.sharedui.models.SortOption
import com.cryptium.coincurrently.sharedui.charts.ChartHoverController
import com.cryptium.coincurrently.features.GlobalMarket.GlobalMarketRepository
import com.cryptium.coincurrently.core.persistence.StorageKeys
import com.cryptium.coincurrently.core.persistence.StorageManager
import com.cryptium.coincurrently.core.persistence.CurrencyRepository
import com.cryptium.coincurrently.features.Market.Repositories.ToplistRepository
import com.cryptium.coincurrently.features.Treasuries.TreasuryDetailRepository
import com.cryptium.coincurrently.core.analytics.AnalyticsManager
import com.cryptium.coincurrently.core.analytics.ChartFiatNativeModeChanged
import com.cryptium.coincurrently.core.analytics.ScreenLabel
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import com.cryptium.coincurrently.core.networking.ResponseModels.CryptoDetailInfoModel
import com.cryptium.coincurrently.core.networking.ResponseModels.CurrencySelectionModel
import com.cryptium.coincurrently.core.networking.ResponseModels.PublicTreasuryHolder
import com.cryptium.coincurrently.core.networking.ResponseModels.TimeCharts
import com.cryptium.coincurrently.core.networking.ResponseModels.Toplist
import com.cryptium.coincurrently.core.networking.ResponseModels.TreasuryDetailEntity
import com.cryptium.coincurrently.core.networking.ResponseModels.TreasuryHoldingChartModel
import com.cryptium.coincurrently.core.networking.ResponseModels.TreasuryTransactionHistoryModel
import com.cryptium.coincurrently.core.networking.ResponseModels.resolvedEntityId

class TreasuryDetailViewModelFactory(
    private val holder: PublicTreasuryHolder,
    private val coinId: String,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TreasuryDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TreasuryDetailViewModel(
                holder = holder,
                coinId = coinId,
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class TreasuryDetailViewModel(
    holder: PublicTreasuryHolder,
    coinId: String,
) : ViewModel() {

    private val treasuryDetailRepository by lazy { TreasuryDetailRepository }
    private val currencyRepository by lazy { CurrencyRepository }
    private val globalRepository by lazy { GlobalMarketRepository }
    private val toplistRepository by lazy { ToplistRepository }

    val entityId: String = holder.resolvedEntityId

    var holder: PublicTreasuryHolder by mutableStateOf(holder)
    var coinId: String by mutableStateOf(coinId)
    var treasuryDetail: TreasuryDetailEntity? by mutableStateOf(null)
    var holdingChart: TreasuryHoldingChartModel? by mutableStateOf(null)
    var transactionHistory: TreasuryTransactionHistoryModel? by mutableStateOf(null)
    var availableHoldingCoins: List<Toplist> by mutableStateOf(emptyList())
    var selectedTransactionHoldingCoinId: String? by mutableStateOf(null)
    var currentCurrency: CryptoDetailInfoModel? by mutableStateOf(null)

    var prices: List<Double> by mutableStateOf(emptyList())
    var onlyPrices: List<Double> by mutableStateOf(emptyList())
    var timeStamps: List<Double> by mutableStateOf(emptyList())

    var dragPosition = mutableStateOf(0.dp)
    var chartSize = mutableStateOf(DpSize.Zero)
    val hoverPriceTextWidth = mutableStateOf(0.dp)
    val chartHover = ChartHoverController()

    var isDragging by mutableStateOf(false)
    var isChartFiat by mutableStateOf(false)
    var bitcoin: CryptoDetailInfoModel? by mutableStateOf(null)
    var multiplier: Double? by mutableStateOf(null)
    var showMainPriceError: Boolean by mutableStateOf(false)
    var mainReloading: Boolean by mutableStateOf(false)

    var selectedTimeFrame by mutableStateOf(TimeCharts.getTreasuryStartTimeFrame())
    var currentDays by mutableStateOf(TimeCharts.getTreasuryStartTimeFrame().days)
    var selectedCurrency: CurrencySelectionModel by mutableStateOf(CurrencySelectionModel("", "", "", "", "", true))

    private var coinSymbol = ""

    init {
        isChartFiat = StorageManager.get<Boolean>(StorageKeys.IS_TREASURY_CHART_FIAT) ?: false

        treasuryDetailRepository.getCachedTreasuryDetail(entityId)?.let {
            treasuryDetail = it
        }

        treasuryDetailRepository.getCachedHoldingChart(entityId, coinId, currentDays)?.let {
            holdingChart = it
        }

        treasuryDetailRepository.getCachedTransactionHistory(
            entityId,
            selectedTransactionHoldingCoinId,
        )?.let {
            transactionHistory = it
        }

        bitcoin = currencyRepository.bitcoinData
        bitcoin?.let { setPriceMultiplier() }

        setSelectedCurrency()
        holdingChart?.let {
            setPrices()
            setOnlyPrices()
            setTimestamps()
            showMainPriceError = prices.isEmpty()
        }
    }

    suspend fun setStartData() {
        treasuryDetailRepository.ensureCacheLoaded(entityId, coinId, currentDays)

        treasuryDetailRepository.getCachedTreasuryDetail(entityId)?.let {
            treasuryDetail = it
        }

        treasuryDetailRepository.getCachedHoldingChart(entityId, coinId, currentDays)?.let {
            holdingChart = it
        }

        treasuryDetailRepository.getCachedTransactionHistory(
            entityId,
            selectedTransactionHoldingCoinId,
        )?.let {
            transactionHistory = it
        }

        currencyRepository.bitcoinData?.let {
            bitcoin = it
            setPriceMultiplier()
        }

        setSelectedCurrency()
        setPrices()
        setOnlyPrices()
        setTimestamps()
        showMainPriceError = prices.isEmpty()
    }

    fun setSelectedCurrency() {
        val fiatCurrency = UserPreferences.getSelectedCurrency()
        if (isChartFiat) {
            selectedCurrency = fiatCurrency
            return
        }

        val symbol = coinSymbol
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

    suspend fun getCurrentCurrency() {
        currencyRepository.getCryptoDetail(coinId)?.let { currency ->
            currentCurrency = currency
            updateCoinSymbol()
            setSelectedCurrency()
        }
    }

    private fun updateCoinSymbol() {
        currentCurrency?.symbol?.uppercase()?.takeIf { it.isNotEmpty() }?.let {
            coinSymbol = it
            return
        }

        coinSymbol = globalRepository.coinsList
            ?.firstOrNull { it.id == coinId }
            ?.symbol
            ?.uppercase()
            ?: coinId.uppercase()
    }

    suspend fun getBitcoin() {
        currencyRepository.getBitcoin()?.let {
            bitcoin = it
            setPriceMultiplier()
        }
    }

    suspend fun getDetail() {
        treasuryDetailRepository.getTreasuryDetail(entityId)?.let { detail ->
            treasuryDetail = detail
            updateHolderFromDetail(detail)
            updateAvailableHoldingCoins()
            setSelectedCurrency()
        }
    }

    private fun updateHolderFromDetail(detail: TreasuryDetailEntity) {
        val holding = detail.holdings?.firstOrNull { it.coin_id == coinId }
        holder = PublicTreasuryHolder(
            id = detail.id ?: holder.id,
            name = detail.name ?: holder.name,
            symbol = detail.symbol ?: holder.symbol,
            country = detail.country ?: holder.country,
            total_holdings = holding?.amount ?: holder.total_holdings,
            total_entry_value_usd = holding?.total_entry_value_usd ?: holder.total_entry_value_usd,
            total_current_value_usd = holding?.current_value_usd ?: holder.total_current_value_usd,
            percentage_of_total_supply = holding?.percentage_of_total_supply
                ?: holder.percentage_of_total_supply,
        )
    }

    suspend fun getHoldingChart(days: String) {
        holdingChart = treasuryDetailRepository.getHoldingChart(
            entityId = entityId,
            coinId = coinId,
            days = days,
        )
    }

    suspend fun getTransactionHistory() {
        transactionHistory = treasuryDetailRepository.getTransactionHistory(
            entityId = entityId,
            coinId = selectedTransactionHoldingCoinId,
        )
    }

    suspend fun setTransactionHoldingFilter(coin: Toplist?) {
        val newFilterId = coin?.id
        if (newFilterId == selectedTransactionHoldingCoinId) return

        selectedTransactionHoldingCoinId = newFilterId

        val chartCoinId: String
        val chartCoinSymbol: String?

        if (newFilterId != null) {
            chartCoinId = newFilterId
            chartCoinSymbol = coin.symbol?.uppercase()
        } else {
            if (treasuryDetail?.holdings.isNullOrEmpty()) {
                getDetail()
            }

            val topCoinId = highestValueHoldingCoinId()
            if (topCoinId == null) {
                getTransactionHistory()
                return
            }

            chartCoinId = topCoinId
            chartCoinSymbol = availableHoldingCoins
                .firstOrNull { it.id == topCoinId }
                ?.symbol
                ?.uppercase()
        }

        setChartCoin(coinId = chartCoinId, symbol = chartCoinSymbol)
    }

    private fun highestValueHoldingCoinId(): String? {
        return treasuryDetail?.holdings
            ?.mapNotNull { holding ->
                holding.coin_id?.let { id ->
                    id to (holding.current_value_usd ?: 0.0)
                }
            }
            ?.maxByOrNull { it.second }
            ?.first
    }

    private suspend fun setChartCoin(coinId: String, symbol: String?) {
        this.coinId = coinId

        if (!symbol.isNullOrEmpty()) {
            coinSymbol = symbol
        }

        prices = emptyList()
        onlyPrices = emptyList()
        timeStamps = emptyList()
        holdingChart = null
        setSelectedCurrency()
        refresh()
    }

    private suspend fun updateAvailableHoldingCoins() {
        val holdingIds = treasuryDetail?.holdings?.mapNotNull { it.coin_id }.orEmpty()
        if (holdingIds.isEmpty()) {
            availableHoldingCoins = emptyList()
            return
        }

        val toplist = toplistRepository.getToplist(
            sortOption = SortOption.MktCapDesc,
            priceChange = PriceChange.SevenDays,
            platformId = null,
        ).orEmpty()
        val coinList = globalRepository.coinsList.orEmpty()

        availableHoldingCoins = holdingIds.map { id ->
            toplist.firstOrNull { it.id == id }
                ?: coinList.firstOrNull { it.id == id }?.let { meta ->
                    Toplist(
                        id = id,
                        symbol = meta.symbol,
                        name = meta.name ?: id.replaceFirstChar { it.uppercase() },
                        image = null,
                    )
                }
                ?: Toplist(
                    id = id,
                    symbol = null,
                    name = id.replaceFirstChar { it.uppercase() },
                    image = null,
                )
        }
    }

    val transactionFilterButtonTitle: String
        get() {
            selectedTransactionHoldingCoinId ?: return "All Holdings"
            return availableHoldingCoins.firstOrNull { it.id == selectedTransactionHoldingCoinId }?.name
                ?: selectedTransactionHoldingCoinId?.replaceFirstChar { it.uppercase() }
                ?: "All Holdings"
        }

    suspend fun refresh(
        showReload: Boolean = true,
        newDays: String? = null,
        newTimeChart: TimeCharts? = null,
    ) {
        coroutineScope {
            if (showReload) {
                mainReloading = true
            }

            val days = newDays ?: currentDays
            val chart = newTimeChart ?: selectedTimeFrame

            val detail = async { getDetail() }
            val currency = async { getCurrentCurrency() }
            val chartData = async { getHoldingChart(days) }
            val history = async { getTransactionHistory() }
            val btc = async { getBitcoin() }

            detail.await()
            currency.await()
            chartData.await()
            history.await()
            btc.await()

            currentDays = days
            selectedTimeFrame = chart

            setPrices()
            setOnlyPrices()
            setTimestamps()
            showMainPriceError = prices.isEmpty()

            mainReloading = false
        }
    }

    fun toggleCurrencyType() {
        isChartFiat = !isChartFiat
        StorageManager.set(StorageKeys.IS_TREASURY_CHART_FIAT, isChartFiat)
        AnalyticsManager.log(
            ChartFiatNativeModeChanged(
                fromScreen = ScreenLabel.TreasuryDetail,
                isChartFiat = isChartFiat,
            ),
        )
        setSelectedCurrency()
        setTimestamps()
        setPrices()
        setOnlyPrices()
        showMainPriceError = prices.isEmpty()
    }

    private fun chartSeries(isFiat: Boolean): List<List<Double?>>? {
        val chart = holdingChart ?: return null
        return if (isFiat) {
            chart.holding_value_in_usd ?: chart.market_caps
        } else {
            chart.holdings ?: chart.prices
        }
    }

    // Needs ≥2 points so line/hover math (count - 1) stays valid.
    private fun chartDataPoints(): List<List<Double?>>? {
        val dataPoints = chartSeries(isChartFiat) ?: return null
        if (dataPoints.size < 2) return null
        return dataPoints
    }

    private fun fallbackHoldingValue(): Double? {
        val holding = treasuryDetail?.holdings?.firstOrNull { it.coin_id == coinId } ?: return null
        return if (isChartFiat) {
            holding.current_value_usd?.times(multiplier ?: 1.0)
        } else {
            holding.amount
        }
    }

    private fun resolvedDayCount(): Int {
        currentDays.toIntOrNull()?.takeIf { it > 0 }?.let { return it }
        selectedTimeFrame.days.toIntOrNull()?.takeIf { it > 0 }?.let { return it }
        return 30
    }

    // Flat series from current holdings when the API returns an empty/sparse chart.
    private fun fallbackChartSeries(): Pair<List<Double>, List<Double>>? {
        val value = fallbackHoldingValue() ?: return null

        val days = resolvedDayCount()
        val pointCount = maxOf(2, minOf(days, 90))
        val now = System.currentTimeMillis().toDouble()
        val start = now - (days.toDouble() * 24 * 60 * 60 * 1000)
        val step = (now - start) / (pointCount - 1)

        val prices = List(pointCount) { value }
        val timestamps = List(pointCount) { index -> start + index * step }
        return prices to timestamps
    }

    private fun setPrices() {
        val chartMultiplier = if (isChartFiat) multiplier ?: 1.0 else 1.0
        val dataPoints = chartDataPoints()
        prices = if (dataPoints != null) {
            dataPoints.mapNotNull { point -> (point.lastOrNull() ?: 0.0)?.times(chartMultiplier) }
        } else {
            fallbackChartSeries()?.first ?: emptyList()
        }
    }

    private fun setOnlyPrices() {
        setPrices()
        onlyPrices = prices
    }

    private fun setTimestamps() {
        val dataPoints = chartDataPoints()
        timeStamps = if (dataPoints != null) {
            dataPoints.mapNotNull { point -> point.firstOrNull() ?: 0.0 }
        } else {
            fallbackChartSeries()?.second ?: emptyList()
        }
    }

    private fun setPriceMultiplier() {
        val btc = bitcoin ?: return
        val code = UserPreferences.getSelectedCurrency().code
        val target = btc.market_data?.current_price?.getSelectedCurrency(currency = code.uppercase()) ?: 0.0
        val base = btc.market_data?.current_price?.getSelectedCurrency(currency = "USD") ?: 0.0
        multiplier = if (base > 0) target / base else null
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
        return getDifferenceInPercent(last, first)?.roundTo(7)
    }

    val mainGraphStats: List<String?> get() {
        return (0 until 6).map { getPriceInChart(prices, it) }
    }

    private fun getPriceInChart(prices: List<Double>, at: Int): String? {
        if (showMainPriceError) return null
        val min = prices.minOrNull() ?: return null
        val max = prices.maxOrNull() ?: return null
        val step = (max - min) / 5
        val value = min + (step * at)
        return value.formatCurrency(currency = CurrencySelectionModel.empty())
    }
}
