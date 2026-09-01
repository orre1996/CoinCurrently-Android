package com.cryptium.coincurrently.features.Portfolio.ViewModels
import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
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
import androidx.lifecycle.*
import com.cryptium.coincurrently.sharedui.theme.onSurfaceDim
import com.cryptium.coincurrently.sharedui.charts.getChangeColorCompose
import com.cryptium.coincurrently.core.extensions.formatCurrency
import com.cryptium.coincurrently.core.networking.ResponseModels.CurrencySelectionModel
import com.cryptium.coincurrently.core.extensions.getAllTransactions
import com.cryptium.coincurrently.core.extensions.getAmount
import com.cryptium.coincurrently.core.extensions.getDifferenceInDecimal
import com.cryptium.coincurrently.core.extensions.getDifferenceInPercent
import com.cryptium.coincurrently.core.extensions.groupedTransactions
import com.cryptium.coincurrently.core.extensions.is24HourClock
import com.cryptium.coincurrently.core.extensions.pieContaining
import com.cryptium.coincurrently.core.extensions.stars
import com.cryptium.coincurrently.sharedui.models.FormattingThreshold
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
import com.cryptium.coincurrently.sharedui.components.lineChartIndexFromPlotX
import com.cryptium.coincurrently.features.Settings.CurrencySelector.allCurrencies
import com.cryptium.coincurrently.core.networking.ResponseModels.AddressImport
import com.cryptium.coincurrently.core.config.AppLocale
import com.cryptium.coincurrently.features.Portfolio.DataClasses.PortfolioPieChartLayout
import com.cryptium.coincurrently.features.Portfolio.DataClasses.PortfolioPieSlice
import com.cryptium.coincurrently.features.Portfolio.DataClasses.NewTransaction
import com.cryptium.coincurrently.features.Portfolio.DataClasses.PortfolioEvent
import com.cryptium.coincurrently.features.Portfolio.DataClasses.toPortfolioExportModel
import com.cryptium.coincurrently.features.Portfolio.Enums.DateOption
import com.cryptium.coincurrently.features.Portfolio.Enums.NewTransactionType
import com.cryptium.coincurrently.features.Portfolio.Enums.PortfolioExportFormat
import com.cryptium.coincurrently.features.Portfolio.Utilities.PortfolioExportHelper
import com.cryptium.coincurrently.features.Portfolio.Utilities.PortfolioTaxExportBuilder
import com.cryptium.coincurrently.core.extensions.toPortfolioExportDateTimeUTC
import com.cryptium.coincurrently.sharedui.models.CachedData
import com.cryptium.coincurrently.core.purchases.StoreKitManager
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import com.cryptium.coincurrently.features.Portfolio.PortfolioPiesData
import com.cryptium.coincurrently.features.Portfolio.PortfolioRepository
import com.cryptium.coincurrently.features.Market.Repositories.ToplistRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import com.cryptium.coincurrently.core.networking.ResponseModels.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone
import java.util.UUID
import kotlin.collections.map
import kotlin.math.abs

class PortfolioViewModel : ViewModel() {

    private val freePortfolioExportLimit = 3
    private val exportJson = Json {
        prettyPrint = true
        encodeDefaults = true
        explicitNulls = false
    }

    private val portfolioRepository by lazy { PortfolioRepository }
    private val calculateMutex = Mutex()

    var portfolio: List<Pie>? by mutableStateOf(null)
    var portfolioAllCurrencies: MutableMap<String, Map<String, Double>>? by mutableStateOf(null)

    var isDownloading: Boolean by mutableStateOf(true)

    var hasCompletedInitialLoad: Boolean = false

    var hideData: Boolean by mutableStateOf(GlobalFunctions.getHidePortfolioData())
    var typeFilters: MutableState<ArrayList<NewTransactionType>> = mutableStateOf(arrayListOf())
    var dateFilter: MutableState<DateOption> = mutableStateOf(DateOption.All)
    var coinFilters: MutableState<ArrayList<Toplist>> = mutableStateOf(arrayListOf())
    var groupedTransactions: Map<Date, List<PortfolioEvent>> by mutableStateOf(mapOf())

    var selectedChartTimeFrame by mutableStateOf(TimeCharts.getPreferredTimeChart(true) )
    var isDragging by mutableStateOf(false)
    var prices by mutableStateOf<List<Double>>(emptyList())
    var timeStamps by mutableStateOf(listOf<Double>())
    private var rawPrices = listOf<Double>()
    private var rawTimeStamps = listOf<Double>()
    var benchmarkCoin by mutableStateOf<Toplist?>(null)
        private set
    var benchmarkPrices by mutableStateOf<List<Double>>(emptyList())
        private set
    private var benchmarkTimeStamps = listOf<Double>()
    private var rawBenchmarkPrices = listOf<Double>()
    var dragPosition = mutableStateOf(0.dp)
    var chartSize = mutableStateOf(DpSize.Zero)
    val hoverPriceTextWidth = mutableStateOf(0.dp)
    val chartHover = ChartHoverController()

    var totalValue by mutableStateOf<Double?>(null)
    var changePercent by mutableStateOf<Double?>(null)
    var changeValue by mutableStateOf<Double?>(null)
    var profitLossPercent by mutableStateOf<Double?>(null)
    var profitLossValue by mutableStateOf<Double?>(null)
    var graphStats by mutableStateOf<List<String?>>(listOf(null, null, null, null, null, null))
    var portfolioPieSlices by mutableStateOf<List<PortfolioPieSlice>>(emptyList())

    var currentPage by mutableStateOf(0)

    private var cachedTransactionAmounts: Map<String, Double> = emptyMap()

    init {
        val cachedPie = portfolioRepository.portfolio
        val piesData = cachedPie?.data
        if (piesData != null) {
            piesData.sanitizeGsonNulls()
            if (piesData.data.isNotEmpty()) {
                processPortfolioData(cachedPie)
            }
        }

        portfolioRepository.portfolioAllCurrencies?.let {
            portfolioAllCurrencies = it
        }
    }

    private fun cacheTransactionAmounts() {
        cachedTransactionAmounts = portfolio?.associate { pie ->
            val id = pie.data?.id ?: ""
            id to (portfolio?.getAllTransactions(id)?.getAmount() ?: 0.0)
        } ?: emptyMap()
    }

    fun getCachedAmount(id: String?): Double = cachedTransactionAmounts[id] ?: 0.0

    suspend fun initializeFromCache() {
        if (portfolio != null && !allCurrencies.isEmpty()) {
            calculatePortfolioTotal()
        }
    }
    val chartDropCount: Int
        get() = if (benchmarkCoin == null) 0 else 2

    fun getPrices(dropLastTwo: Boolean = false): List<Double> {
        if (dropLastTwo && prices.size > 2) return prices.dropLast(2)
        return prices
    }

    fun getTimeStamps(dropLastTwo: Boolean = false): List<Double> {
        if (dropLastTwo && timeStamps.size > 2) return timeStamps.dropLast(2)
        return timeStamps
    }

    fun getBenchmarkPrices(dropLastTwo: Boolean = false): List<Double> {
        if (dropLastTwo && benchmarkPrices.size > 2) return benchmarkPrices.dropLast(2)
        return benchmarkPrices
    }

    val coinFilterList: List<Toplist>
        get() {
            val mainList =
                portfolio
                    ?.onEach { it.sanitizeGsonNullLists() }
                    ?.filter { pie ->
                        pie.transactions.isNotEmpty() ||
                                pie.swaps.isNotEmpty() ||
                                pie.imports.isNotEmpty()
                    }
                    ?.mapNotNull { it.data }
                    ?: emptyList()

            return mainList
        }

    val dragIndex: Int @Composable get() {
        val chartPrices = getPrices(dropLastTwo = benchmarkCoin != null)
        if (chartPrices.size <= 1) return 0
        return chartHover.hoverDragIndex(chartSize.value.width, chartPrices.size)
    }

    val hoverPrice: String @Composable get() {
        val value = chartHover.hoverHelper.hoverPrices.firstOrNull() ?: prices.getOrNull(dragIndex)
        return if (hideData) stars else value?.formatCurrency(threshold = FormattingThreshold.MILLION) ?: "N/A"
    }

    val hoverTimePosition: Dp @Composable get() {
        return getXPosition(hoverPriceTextWidth.value) - hoverPriceTextWidth.value / 2
    }

    val dragPoint: Point @Composable get() {
        val point = getPoint(
            index = dragIndex,
            rect = chartSize.value,
            prices = prices,
            dropLastTwo = benchmarkCoin != null,
        )

        return Point(
            x = point.x,
            y = if ((portfolio?.isEmpty() != false) || prices.all { it == 0.0 }) {
                chartSize.value.height
            } else {
                point.y
            }
        )
    }

    val chartColor: Color @Composable get() {
        return changePercent?.getChangeColorCompose() ?: MaterialTheme.colors.onSurfaceDim
    }

    val hoverColor: Color @Composable get() {
        return MaterialTheme.colors.onSurface
    }

    val hoverTime: String @Composable get() {
        return chartHover.hoverHelper.hoverTimes.firstOrNull() ?: run {
            val context = LocalContext.current
            val formatter = if (context.is24HourClock) "dd MMM 'at' HH:mm" else "dd MMM 'at' hh:mm a"
            val currentTimestamp = timeStamps.getOrNull(dragIndex)?.toLong() ?: 0
            val dateFormat = SimpleDateFormat(formatter, AppLocale.enUsPosix)
            dateFormat.timeZone = TimeZone.getDefault()
            dateFormat.format(Date(currentTimestamp))
        }
    }

    val multiHoverGraphColor: Color @Composable get() = chartHover.multiHoverGraphColor(chartColor)

    val differenceValue: String @Composable get() = chartHover.currencyDifferenceValue()

    val differencePercent: String @Composable get() = chartHover.differencePercent()

    val dragPositions: List<Point> @Composable get() = chartHover.dragPositions(
        chartSize.value,
        prices,
        dropLastCount = chartDropCount,
    )

    fun updateHoverStats(touchXs: List<Dp>, is24HourClock: Boolean) {
        chartHover.updateHoverStats(
            touchXs = touchXs,
            chartWidth = chartSize.value.width,
            prices = if (benchmarkCoin == null) rawPrices else portfolioHoverPrices,
            dates = getTimeStamps(dropLastTwo = benchmarkCoin != null),
            timeChart = selectedChartTimeFrame,
            is24HourClock = is24HourClock,
            isBlocked = portfolio.isNullOrEmpty(),
        )
        isDragging = chartHover.isDragging
        dragPosition.value = chartHover.dragPosition.value
    }

    fun setBenchmarkCoin(id: String) {
        viewModelScope.launch {
            val coin = ToplistRepository.getCoin(id) ?: return@launch
            benchmarkCoin = coin
            setTimestamps()
            withContext(Dispatchers.Default) {
                setPrices()
            }
            addExtraElementsToTimeline()
            setChangePercent()
            setChangeValue()
            setGraphStats()
        }
    }

    fun clearBenchmark() {
        benchmarkCoin = null
        benchmarkPrices = emptyList()
        benchmarkTimeStamps = emptyList()
        rawBenchmarkPrices = emptyList()
        prices = rawPrices
        timeStamps = rawTimeStamps
        setChangePercent()
        setChangeValue()
        setGraphStats()
    }

    fun toggleHideData() {
        hideData = !hideData
        GlobalFunctions.setHidePortfolioData(hideData)
    }
    private fun setTimestamps() {
        val now = Date()
        val hourInterval = 60 * 60 * 1000.0

        val elements = portfolio?.maxByOrNull {
            it.data?.sparkline_in_7d?.price?.size ?: 0
        }?.data?.sparkline_in_7d?.price?.size ?: 169

        timeStamps = (0 until elements).map { i ->
            val timestamp = now.time - ((elements - 1 - i) * hourInterval)
            timestamp
        }
        rawTimeStamps = timeStamps
    }

    private fun setPrices() {
        if (rawTimeStamps.isNotEmpty()) {
            timeStamps = rawTimeStamps
        }

        val portfolio = portfolio ?: run {
            prices = List(timeStamps.count()) { 0.0 }
            rawPrices = prices
            return
        }

        if (portfolio.isEmpty()) {
            prices = List(timeStamps.count()) { 0.0 }
            rawPrices = prices
            return
        }

        val timestampSize = timeStamps.size
        // Use primitive DoubleArray for better performance
        val newPrices = DoubleArray(timestampSize)

        portfolio.forEach { pie ->
            val sparkline = pie.data?.sparkline_in_7d?.price ?: return@forEach
            val id = pie.data?.id ?: return@forEach
            val offset = timestampSize - sparkline.size

            // Get transactions ONCE per pie, not per sparkline point
            val transactions = portfolio.getAllTransactions(id)

            sparkline.forEachIndexed { index, value ->
                val globalIndex = index + offset
                if (globalIndex < 0 || globalIndex >= timestampSize) return@forEachIndexed

                val date = Date(timeStamps[globalIndex].toLong())
                val amount = transactions.getAmount(before = date)

                newPrices[globalIndex] += amount * value
            }
        }

        // Assign new immutable list to trigger recomposition
        prices = newPrices.toList()
        rawPrices = prices
    }

    private fun setBenchmarkSeries() {
        val sparkline = benchmarkCoin?.sparkline_in_7d?.price
        if (sparkline.isNullOrEmpty() || rawPrices.isEmpty()) {
            benchmarkPrices = emptyList()
            benchmarkTimeStamps = emptyList()
            return
        }

        val firstPortfolio = rawPrices.firstOrNull { it != -1.0 }
        if (firstPortfolio == null || firstPortfolio == 0.0) {
            benchmarkPrices = emptyList()
            benchmarkTimeStamps = emptyList()
            return
        }

        val sparklinePrices = sparkline.toMutableList()
        benchmarkCoin?.current_price?.let { lastPrice ->
            if (sparklinePrices.isNotEmpty()) {
                sparklinePrices[sparklinePrices.lastIndex] = lastPrice
            }
        }

        val firstBenchmark = sparklinePrices.firstOrNull()
        if (firstBenchmark == null || firstBenchmark == 0.0) {
            benchmarkPrices = emptyList()
            benchmarkTimeStamps = emptyList()
            return
        }

        val offset = rawTimeStamps.size - sparklinePrices.size
        val alignedPrices = MutableList(rawTimeStamps.size) { -1.0 }

        sparklinePrices.forEachIndexed { index, value ->
            val globalIndex = index + offset
            if (globalIndex in alignedPrices.indices) {
                alignedPrices[globalIndex] = firstPortfolio * (value / firstBenchmark)
            }
        }

        benchmarkPrices = alignedPrices
        benchmarkTimeStamps = rawTimeStamps
        rawBenchmarkPrices = alignedPrices
    }

    private fun addExtraElementsToTimeline() {
        prices = rawPrices
        timeStamps = rawTimeStamps
        benchmarkPrices = rawBenchmarkPrices
        benchmarkTimeStamps = rawTimeStamps

        if (benchmarkCoin == null) {
            benchmarkPrices = emptyList()
            benchmarkTimeStamps = emptyList()
            rawBenchmarkPrices = emptyList()
            return
        }

        setBenchmarkSeries()

        if (prices.isEmpty() || benchmarkPrices.isEmpty()) {
            return
        }

        val validMain = prices.filter { it != -1.0 }
        val validCompare = benchmarkPrices.filter { it != -1.0 }

        val firstMain = validMain.firstOrNull()
        val mainMax = validMain.maxOrNull()
        val mainMin = validMain.minOrNull()
        val firstCompare = validCompare.firstOrNull()
        val compareMax = validCompare.maxOrNull()
        val compareMin = validCompare.minOrNull()

        if (firstMain == null || mainMax == null || mainMin == null ||
            firstCompare == null || compareMax == null || compareMin == null ||
            firstCompare == 0.0
        ) {
            return
        }

        val firstTimestamp = timeStamps.firstOrNull { it != -1.0 } ?: timeStamps.firstOrNull() ?: 0.0
        val compareTimestamp = benchmarkTimeStamps.firstOrNull { it != -1.0 }
            ?: benchmarkTimeStamps.firstOrNull() ?: 0.0

        val compareMaxPercent = getDifferenceInDecimal(compareMax, firstCompare) ?: 0.0
        val compareMinPercent = getDifferenceInDecimal(compareMin, firstCompare) ?: 0.0

        prices = prices + listOf(firstMain * compareMaxPercent, firstMain * compareMinPercent)

        val oneDayMs = 86_400_000.0
        if (abs(firstTimestamp - compareTimestamp) > oneDayMs) {
            timeStamps = timeStamps + listOf(compareTimestamp, compareTimestamp)
        } else {
            timeStamps = timeStamps + listOf(firstTimestamp, firstTimestamp)
        }

        val mainMaxPercent = getDifferenceInDecimal(mainMax, firstMain) ?: 0.0
        val mainMinPercent = getDifferenceInDecimal(mainMin, firstMain) ?: 0.0

        benchmarkPrices = benchmarkPrices + listOf(
            firstCompare * mainMaxPercent,
            firstCompare * mainMinPercent,
        )

        if (abs(compareTimestamp - firstTimestamp) > oneDayMs) {
            benchmarkTimeStamps = benchmarkTimeStamps + listOf(firstTimestamp, firstTimestamp)
        } else {
            benchmarkTimeStamps = benchmarkTimeStamps + listOf(compareTimestamp, compareTimestamp)
        }

        val delta = abs(prices.size - benchmarkPrices.size)
        if (prices.size > benchmarkPrices.size) {
            benchmarkPrices = List(delta) { -1.0 } + benchmarkPrices
            benchmarkTimeStamps = List(delta) { -1.0 } + benchmarkTimeStamps
        } else if (benchmarkPrices.size > prices.size) {
            prices = List(delta) { -1.0 } + prices
            timeStamps = List(delta) { -1.0 } + timeStamps
        }
    }

    private val portfolioHoverPrices: List<Double>
        get() = getPrices(dropLastTwo = benchmarkCoin != null).mapIndexed { index, chartPrice ->
            if (chartPrice == -1.0) {
                -1.0
            } else {
                portfolioRawValue(atChartIndex = index) ?: chartPrice
            }
        }

    private fun portfolioRawValue(atChartIndex: Int): Double? {
        if (atChartIndex < 0) return null

        val leadingPadding = prices.takeWhile { it == -1.0 }.size
        val rawIndex = atChartIndex - leadingPadding

        if (rawIndex < 0 || rawIndex >= rawPrices.size) return null
        return rawPrices[rawIndex]
    }

    fun setGroupTransactions() {
        groupedTransactions = portfolio?.groupedTransactions(
            types = typeFilters.value,
            dateOption = dateFilter.value,
            coinNames = coinFilters.value
        ) ?: mapOf()
    }

    fun deleteTransaction(transaction: PortfolioEvent) {
        viewModelScope.launch {
            val id = portfolio?.pieContaining(transaction)?.data?.id ?: ""

            val portfolio = portfolioRepository.deleteTransaction(
                id = id,
                uuid = transaction.id
            )

            processPortfolioData(portfolio)
            calculatePortfolioTotal()
        }
    }

    suspend fun findTransaction(transactionId: String): Pair<PortfolioEvent?, Toplist?> {
        return portfolioRepository.findPortfolioEventByTransactionId(transactionId)
    }

    @Composable
    fun getXPosition(textWidth: Dp): Dp {
        val xPosition = getPoint(
            index = dragIndex,
            rect = chartSize.value,
            prices = prices,
        ).x

        return getPriceXPosition(xPosition, textWidth, chartSize.value.width)
    }

    private fun processPortfolioData(portfolio: CachedData<PortfolioPiesData>?) {
        portfolio?.data?.sanitizeGsonNulls()
        val dataPies = portfolio?.data?.data ?: emptyList()
        val coinColors = portfolio?.data?.coinColors ?: emptyMap()

        dataPies.forEach { pie ->
            val id = pie.data?.id ?: return@forEach
            if (needsPieColor(pie.color)) {
                coinColors[id]?.let { cachedColor ->
                    if (!needsPieColor(cachedColor)) {
                        pie.color = cachedColor
                    }
                }
            }
        }

        val otherPies = portfolio?.data?.otherCoins
            ?.filter { otherCoin ->
                dataPies.none { it.data?.id == otherCoin.id }
            }
            ?.map { otherCoin ->
                Pie(
                    data = otherCoin,
                    color = coinColors[otherCoin.id ?: ""],
                )
            }
            ?: emptyList()

        val combinedPortfolio = (dataPies + otherPies).toMutableList()

        val transactionAmounts: Map<String, Double> =
            combinedPortfolio
                .mapNotNull { pie ->
                    val id = pie.data?.id ?: return@mapNotNull null
                    val amount = portfolio?.data?.data
                        ?.getAllTransactions(id)
                        ?.getAmount()
                        ?: 0.0
                    id to amount
                }
                .toMap()

        combinedPortfolio.sortByDescending { pie ->
            val id = pie.data?.id ?: ""
            val amount = transactionAmounts[id] ?: 0.0
            val price = pie.data?.current_price ?: 0.0
            amount * price
        }

        this.portfolio = combinedPortfolio
    }

    fun getPortfolioAndCurrencies(context: Context? = null, callback: (() -> Unit)? = null) {
        viewModelScope.launch {
            val portfolioDeferred = async {
                portfolioRepository.getPortfolio()
            }
            val currenciesDeferred = async {
                portfolioRepository.getPortfolioAllCurrencies()
            }

            awaitAll(portfolioDeferred, currenciesDeferred)

            val portfolio = portfolioDeferred.await()
            processPortfolioData(portfolio)

            benchmarkCoin?.id?.let { benchmarkId ->
                ToplistRepository.getCoin(benchmarkId)?.let { coin ->
                    benchmarkCoin = coin
                }
            }
            
            portfolioAllCurrencies = currenciesDeferred.await()

            calculatePortfolioTotal()
            hasCompletedInitialLoad = true

            context?.let { fetchCoinColors(it) }

            callback?.invoke()
        }
    }

    fun refreshImportWallets() {
        viewModelScope.launch {
            portfolioRepository.refreshWallets()?.let {
                processPortfolioData(it)
                calculatePortfolioTotal()
            }
        }
    }

    fun getCoinColors(context: Context) {
        viewModelScope.launch {
            fetchCoinColors(context)
        }
    }

    private suspend fun fetchCoinColors(context: Context) {
        portfolioRepository.getCoinColors(context) {
            reloadPortfolioFromCache()
            setPortfolioPieSlices()
        }
    }

    private fun needsPieColor(color: String?): Boolean {
        if (color == null) return true
        return color.equals("#FFFFFF", ignoreCase = true)
    }

    private fun reloadPortfolioFromCache() {
        portfolioRepository.portfolio?.let {
            processPortfolioData(it)
        }
    }

    private fun setPortfolioPieSlices() {
        val portfolio = portfolio
        val total = totalValue

        if (portfolio == null || total == null || total <= 0.0) {
            portfolioPieSlices = emptyList()
            return
        }

        val currencyCode = GlobalFunctions.getSelectedCurrency().code.lowercase()
        val outerGap = PortfolioPieChartLayout.outerGapDegrees
        val innerGap = PortfolioPieChartLayout.innerGapDegrees

        val holdings = portfolio.mapNotNull { pie ->
            if (!pie.shouldShow) return@mapNotNull null
            val id = pie.data?.id ?: return@mapNotNull null
            if (portfolio.getAllTransactions(id).isEmpty()) return@mapNotNull null

            val lastPrice = portfolioAllCurrencies?.get(id)?.get(currencyCode) ?: 0.0
            val amount = getCachedAmount(id)
            val value = lastPrice * amount
            if (value <= 0.0) return@mapNotNull null

            pie to value
        }.sortedByDescending { it.second }

        if (holdings.isEmpty()) {
            portfolioPieSlices = emptyList()
            return
        }

        val mainHoldings = holdings.filter { (_, value) ->
            value / total >= PortfolioPieChartLayout.minimumSlicePercentage
        }
        val otherHoldings = holdings.filter { (_, value) ->
            value / total < PortfolioPieChartLayout.minimumSlicePercentage
        }

        val sliceInputs = mainHoldings.map { (pie, value) ->
            SliceInput(
                id = pie.data?.id ?: UUID.randomUUID().toString(),
                symbol = pie.data?.symbol?.uppercase(AppLocale.enUsPosix) ?: "",
                value = value,
                color = pie.color,
            )
        }.toMutableList()

        if (otherHoldings.isNotEmpty()) {
            val otherValue = otherHoldings.sumOf { it.second }
            sliceInputs.add(
                SliceInput(
                    id = PortfolioPieChartLayout.otherSliceId,
                    symbol = "OTHER",
                    value = otherValue,
                    color = null,
                ),
            )
        }

        if (sliceInputs.size == 1) {
            val slice = sliceInputs.first()
            val percentage = (slice.value / total).coerceIn(0.0, 1.0)
            portfolioPieSlices = listOf(
                PortfolioPieSlice(
                    id = slice.id,
                    symbol = slice.symbol,
                    percentage = percentage,
                    color = slice.color,
                    outerStartAngle = -90.0,
                    outerEndAngle = 270.0,
                    innerStartAngle = -90.0,
                    innerEndAngle = 270.0,
                ),
            )
            return
        }

        val outerAvailable = 360.0 - outerGap * sliceInputs.size
        val innerAvailable = 360.0 - innerGap * sliceInputs.size
        val sliceTotal = sliceInputs.sumOf { it.value }
        var outerCurrent = -90.0 + outerGap / 2
        var innerCurrent = -90.0 + innerGap / 2

        portfolioPieSlices = sliceInputs.map { slice ->
            val percentage = (slice.value / total).coerceIn(0.0, 1.0)
            val allocationRatio = if (sliceTotal > 0.0) slice.value / sliceTotal else 0.0
            val outerStart = outerCurrent
            val outerEnd = outerStart + allocationRatio * outerAvailable
            val innerStart = innerCurrent
            val innerEnd = innerStart + allocationRatio * innerAvailable
            outerCurrent = outerEnd + outerGap
            innerCurrent = innerEnd + innerGap

            PortfolioPieSlice(
                id = slice.id,
                symbol = slice.symbol,
                percentage = percentage,
                color = slice.color,
                outerStartAngle = outerStart,
                outerEndAngle = outerEnd,
                innerStartAngle = innerStart,
                innerEndAngle = innerEnd,
            )
        }
    }

    private data class SliceInput(
        val id: String,
        val symbol: String,
        val value: Double,
        val color: String?,
    )

    private fun setTotalValue() {
        totalValue = portfolio?.fold(0.0) { sum, pie ->
            val lastPrice = pie.data?.sparkline_in_7d?.price?.lastOrNull() ?: 0.0
            val amount = getCachedAmount(pie.data?.id)
            sum + (lastPrice * amount)
        }
    }

    private fun setChangePercent() {
        changePercent = run {
            val chartPrices = rawPrices.filter { it != -1.0 }
            val firstPrice = chartPrices.firstOrNull() ?: return@run null
            val lastPrice = chartPrices.lastOrNull() ?: return@run null
            getDifferenceInPercent(lastPrice, firstPrice)
        }
    }

    private fun setChangeValue() {
        changeValue = run {
            val chartPrices = rawPrices.filter { it != -1.0 }
            val firstPrice = chartPrices.firstOrNull() ?: return@run null
            val lastPrice = chartPrices.lastOrNull() ?: return@run null
            lastPrice - firstPrice
        }
    }

    private fun setProfitLossPercent() {
        profitLossPercent = GlobalFunctions.getPortfolioProfitLossPercent(
            portfolio = portfolio,
            coinPrices = portfolioAllCurrencies
        )
    }

    private fun setProfitLossValue() {
        profitLossValue = GlobalFunctions.getPortfolioProfitLossValue(
            portfolio = portfolio,
            coinPrices = portfolioAllCurrencies,
            showCurrency = GlobalFunctions.getSelectedCurrency().code
        )
    }

    private fun setGraphStats() {
        val chartCurrency = CurrencySelectionModel.empty()
        val validPrices: List<Double> = when {
            benchmarkCoin != null -> rawPrices.filter { it != -1.0 } + rawBenchmarkPrices.filter { it != -1.0 }
            else -> rawPrices.filter { it != -1.0 }
        }

        graphStats = when {
            portfolio == null -> List(6) { null }
            portfolio?.isEmpty() ?: true -> List(6) { it.toDouble().formatCurrency(currency = chartCurrency) }
            validPrices.all { it == 0.0 } -> List(6) { it.toDouble().formatCurrency(currency = chartCurrency) }
            hideData -> List(6) { stars }
            else -> {
                val minPrice = validPrices.minOrNull()
                val maxPrice = validPrices.maxOrNull()
                
                if (minPrice == null || maxPrice == null) {
                    List(6) { null }
                } else {
                    val step = (maxPrice - minPrice) / 5
                    List(6) { (minPrice + (step * it.toDouble())).formatCurrency(currency = chartCurrency) }
                }
            }
        }
    }

    suspend fun calculatePortfolioTotal() {
        calculateMutex.withLock {
            // Heavy computation on background thread
            withContext(Dispatchers.Default) {
                setTimestamps()
                cacheTransactionAmounts() // Cache first for reuse
                setPrices()
            }

            // These are fast, can stay on main thread
            setGroupTransactions()

            addExtraElementsToTimeline()

            // Run independent calculations in parallel
            coroutineScope {
                launch { setTotalValue() }
                launch { setChangePercent() }
                launch { setChangeValue() }
                launch { setProfitLossPercent() }
                launch { setProfitLossValue() }
                launch { setGraphStats() }
            }

            setPortfolioPieSlices()

            isDownloading = false
        }
    }

    fun hasBoughtPro(): Boolean = StoreKitManager.currentSubscription.value != null

    val portfolioExportCount: Int
        get() = GlobalFunctions.getPortfolioExportCount()

    val remainingFreePortfolioExports: Int
        get() = maxOf(0, freePortfolioExportLimit - portfolioExportCount)

    fun canExportPortfolio(): Boolean =
        hasBoughtPro() || portfolioExportCount < freePortfolioExportLimit

    fun recordPortfolioExport() {
        if (hasBoughtPro()) return
        GlobalFunctions.incrementPortfolioExportCount()
    }

    fun exportPortfolio(
        context: Context,
        format: PortfolioExportFormat,
        launcher: ActivityResultLauncher<Intent>? = null,
        onComplete: (Boolean) -> Unit = {},
    ) {
        val currentPortfolio = portfolio ?: run {
            onComplete(false)
            return
        }

        when (format) {
            PortfolioExportFormat.CSV -> exportPortfolioCSV(context, currentPortfolio, launcher, onComplete)
            PortfolioExportFormat.JSON -> exportPortfolioJSON(context, currentPortfolio, launcher, onComplete)
            PortfolioExportFormat.PlainText -> exportPortfolioText(context, currentPortfolio, launcher, onComplete)
            PortfolioExportFormat.Koinly -> exportTaxCSV(
                context = context,
                content = PortfolioTaxExportBuilder.koinlyCSV(currentPortfolio),
                fileName = "CoinCurrently-Portfolio-Koinly",
                launcher = launcher,
                onComplete = onComplete,
            )
            PortfolioExportFormat.CoinTracker -> exportTaxCSV(
                context = context,
                content = PortfolioTaxExportBuilder.coinTrackerCSV(currentPortfolio),
                fileName = "CoinCurrently-Portfolio-CoinTracker",
                launcher = launcher,
                onComplete = onComplete,
            )
            PortfolioExportFormat.TokenTax -> exportTaxCSV(
                context = context,
                content = PortfolioTaxExportBuilder.tokenTaxCSV(currentPortfolio),
                fileName = "CoinCurrently-Portfolio-TokenTax",
                launcher = launcher,
                onComplete = onComplete,
            )
            PortfolioExportFormat.ZenLedger -> exportTaxCSV(
                context = context,
                content = PortfolioTaxExportBuilder.zenLedgerCSV(currentPortfolio),
                fileName = "CoinCurrently-Portfolio-ZenLedger",
                launcher = launcher,
                onComplete = onComplete,
            )
        }
    }

    private fun exportPortfolioCSV(
        context: Context,
        portfolio: List<Pie>,
        launcher: ActivityResultLauncher<Intent>?,
        onComplete: (Boolean) -> Unit,
    ) {
        val header = "Date (UTC),Coin,Type,Amount,Price per coin,Fee,Value traded,Currency,Note,Address"
        val entries = mutableListOf<Pair<Date, String>>()

        portfolio.forEach { pie ->
            pie.sanitizeGsonNullLists()
            val coin = pie.data?.symbol?.uppercase(AppLocale.enUsPosix) ?: pie.data?.name ?: ""

            pie.transactions.forEach { transaction ->
                entries.add(transaction.date to csvRow(coin, transaction))
            }

            pie.swaps.forEach { swap ->
                entries.add(swap.date to csvSwapRow(swap))
            }

            pie.imports.forEach { importEvent ->
                entries.add(importEvent.date to csvImportRow(coin, importEvent))
            }
        }

        val content = (listOf(header) + entries.sortedByDescending { it.first }.map { it.second })
            .joinToString("\n")

        shareExport(
            context = context,
            content = content,
            fileName = "CoinCurrently-Portfolio",
            extension = "csv",
            mimeType = "text/csv",
            launcher = launcher,
            onComplete = onComplete,
        )
    }

    private fun exportPortfolioJSON(
        context: Context,
        portfolio: List<Pie>,
        launcher: ActivityResultLauncher<Intent>?,
        onComplete: (Boolean) -> Unit,
    ) {
        val models = portfolio.mapNotNull { it.toPortfolioExportModel() }
        val jsonString = try {
            exportJson.encodeToString(models)
        } catch (_: Exception) {
            onComplete(false)
            return
        }

        shareExport(
            context = context,
            content = jsonString,
            fileName = "CoinCurrently-Portfolio",
            extension = "json",
            mimeType = "application/json",
            launcher = launcher,
            onComplete = onComplete,
        )
    }

    private fun exportPortfolioText(
        context: Context,
        portfolio: List<Pie>,
        launcher: ActivityResultLauncher<Intent>?,
        onComplete: (Boolean) -> Unit,
    ) {
        val text = portfolio
            .joinToString("") { textExportSection(it) }
            .trim()

        if (text.isEmpty()) {
            onComplete(false)
            return
        }

        shareExport(
            context = context,
            content = text,
            fileName = "CoinCurrently-Portfolio",
            extension = "txt",
            mimeType = "text/plain",
            launcher = launcher,
            onComplete = onComplete,
        )
    }

    private fun exportTaxCSV(
        context: Context,
        content: String,
        fileName: String,
        launcher: ActivityResultLauncher<Intent>?,
        onComplete: (Boolean) -> Unit,
    ) {
        shareExport(
            context = context,
            content = content,
            fileName = fileName,
            extension = "csv",
            mimeType = "text/csv",
            launcher = launcher,
            onComplete = onComplete,
        )
    }

    private fun shareExport(
        context: Context,
        content: String,
        fileName: String,
        extension: String,
        mimeType: String,
        launcher: ActivityResultLauncher<Intent>?,
        onComplete: (Boolean) -> Unit,
    ) {
        val started = PortfolioExportHelper.shareExport(
            context = context,
            content = content,
            fileName = fileName,
            extension = extension,
            mimeType = mimeType,
            launcher = launcher,
        )
        if (!started) {
            onComplete(false)
        } else if (launcher == null) {
            onComplete(true)
        }
    }

    private fun csvField(value: String): String = value.replace(",", ".")

    private fun csvNumericField(value: Double?): String =
        value?.let { csvField(it.toString()) } ?: ""

    private fun csvRow(coin: String, transaction: NewTransaction): String {
        val isTransfer = transaction.transactionType.isTransfer
        val value = (transaction.amount * (transaction.pricePerCoin ?: 0.0)) + transaction.fee

        return listOf(
            transaction.date.toPortfolioExportDateTimeUTC(),
            coin,
            transaction.transactionType.value,
            csvField(transaction.amount.toString()),
            if (isTransfer) "" else csvNumericField(transaction.pricePerCoin),
            if (isTransfer) "" else csvNumericField(transaction.fee),
            if (isTransfer || value == 0.0) "" else csvNumericField(value),
            if (isTransfer) "" else csvField(transaction.currency),
            csvField(transaction.note),
            csvField(transaction.address ?: ""),
        ).joinToString(",")
    }

    private fun csvSwapRow(swap: com.cryptium.coincurrently.features.Portfolio.Models.NewSwapTransaction): String {
        val coin = "${swap.fromCurrencySymbol.uppercase(AppLocale.enUsPosix)} → ${swap.toCurrencySymbol.uppercase(AppLocale.enUsPosix)}"
        val value = (swap.fromAmount * swap.fromUnitPrice) + swap.fee

        return listOf(
            swap.date.toPortfolioExportDateTimeUTC(),
            coin,
            NewTransactionType.Swap.value,
            csvField(swap.fromAmount.toString()),
            csvNumericField(swap.fromUnitPrice),
            csvNumericField(swap.fee),
            if (value == 0.0) "" else csvNumericField(value),
            csvField(swap.selectedCurrency),
            csvField(swap.note),
            "",
        ).joinToString(",")
    }

    private fun csvImportRow(coin: String, importEvent: AddressImport): String {
        return listOf(
            importEvent.date.toPortfolioExportDateTimeUTC(),
            coin,
            NewTransactionType.ImportAddress.value,
            csvField(importEvent.balance),
            "",
            "",
            "",
            "",
            csvField(importEvent.walletName ?: ""),
            csvField(importEvent.address),
        ).joinToString(",")
    }

    private fun textExportSection(pie: Pie): String {
        pie.sanitizeGsonNullLists()
        val coinName = pie.data?.name ?: pie.data?.symbol?.uppercase(AppLocale.enUsPosix) ?: "Coin"
        var section = "• $coinName\n\n"

        val transactionsByType = pie.transactions.groupBy { it.transactionType }

        listOf(
            NewTransactionType.Buy,
            NewTransactionType.Sell,
            NewTransactionType.TransferIn,
            NewTransactionType.TransferOut,
        ).forEach { type ->
            val transactions = transactionsByType[type].orEmpty()
            if (transactions.isEmpty()) return@forEach
            section += "-- ${type.value} Transactions --\n"
            section += transactions
                .sortedByDescending { it.date }
                .joinToString("") { textExportBlock(it) }
        }

        if (pie.swaps.isNotEmpty()) {
            section += "-- Swap Transactions --\n"
            section += pie.swaps
                .sortedByDescending { it.date }
                .joinToString("") { textExportBlock(it) }
        }

        if (pie.imports.isNotEmpty()) {
            section += "-- Wallet Imports --\n"
            section += pie.imports
                .sortedByDescending { it.date }
                .joinToString("") { textExportBlock(it) }
        }

        return section
    }

    private fun textExportBlock(transaction: NewTransaction): String {
        val selectedCurrency = allCurrencies.firstOrNull {
            it.code.equals(transaction.currency, ignoreCase = true)
        }
        var block = "Date (UTC): ${transaction.date.toPortfolioExportDateTimeUTC()}\n"
        block += "Amount: ${transaction.amount}\n"

        if (!transaction.transactionType.isTransfer && transaction.pricePerCoin != null) {
            block += "Price per coin: ${transaction.pricePerCoin.formatCurrency(currency = selectedCurrency ?: GlobalFunctions.getSelectedCurrency(), threshold = FormattingThreshold.NONE)}\n"
        }

        if (!transaction.transactionType.isTransfer && transaction.fee != 0.0) {
            block += "Fee: ${transaction.fee.formatCurrency(currency = selectedCurrency ?: GlobalFunctions.getSelectedCurrency(), threshold = FormattingThreshold.NONE)}\n"
        }

        val value = (transaction.amount * (transaction.pricePerCoin ?: 0.0)) + transaction.fee
        if (!transaction.transactionType.isTransfer && value != 0.0) {
            block += "Value traded: ${value.formatCurrency(currency = selectedCurrency ?: GlobalFunctions.getSelectedCurrency(), threshold = FormattingThreshold.NONE)}\n"
        }

        if (!transaction.transactionType.isTransfer) {
            block += "Currency: ${transaction.currency}\n"
        }

        if (transaction.note.isNotEmpty()) {
            block += "Note: ${transaction.note}\n"
        }

        if (!transaction.address.isNullOrEmpty()) {
            block += "Address: ${transaction.address}\n"
        }

        return "$block\n"
    }

    private fun textExportBlock(swap: com.cryptium.coincurrently.features.Portfolio.Models.NewSwapTransaction): String {
        val selectedCurrency = allCurrencies.firstOrNull {
            it.code.equals(swap.selectedCurrency, ignoreCase = true)
        } ?: GlobalFunctions.getSelectedCurrency()
        var block = "Date (UTC): ${swap.date.toPortfolioExportDateTimeUTC()}\n"
        block += "From: ${swap.fromAmount} ${swap.fromCurrencySymbol.uppercase(AppLocale.enUsPosix)} @ ${swap.fromUnitPrice.formatCurrency(currency = selectedCurrency, threshold = FormattingThreshold.NONE)}\n"
        block += "To: ${swap.toAmount} ${swap.toCurrencySymbol.uppercase(AppLocale.enUsPosix)} @ ${swap.toUnitPrice.formatCurrency(currency = selectedCurrency, threshold = FormattingThreshold.NONE)}\n"

        if (swap.fee != 0.0) {
            block += "Fee: ${swap.fee.formatCurrency(currency = selectedCurrency, threshold = FormattingThreshold.NONE)}\n"
        }

        if (swap.note.isNotEmpty()) {
            block += "Note: ${swap.note}\n"
        }

        return "$block\n"
    }

    private fun textExportBlock(importEvent: AddressImport): String {
        var block = "Date (UTC): ${importEvent.date.toPortfolioExportDateTimeUTC()}\n"

        if (!importEvent.walletName.isNullOrEmpty()) {
            block += "Wallet: ${importEvent.walletName}\n"
        }

        block += "Address: ${importEvent.address}\n"
        block += "Balance: ${importEvent.balance}\n"
        return "$block\n"
    }
}
