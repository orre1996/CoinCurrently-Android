package com.cryptium.coincurrently.ui.main.HomeTab.GlobalMarket.Repositories

import com.google.gson.annotations.SerializedName

import android.content.Context
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.cryptium.coincurrently.ui.main.CustomViews.CustomImageView
import com.cryptium.coincurrently.ui.main.Extensions.isMoreThanDefaultCache
import com.cryptium.coincurrently.ui.main.Extensions.unwrapOrNull
import com.google.gson.reflect.TypeToken
import com.cryptium.coincurrently.ui.main.General.GlobalFunctions
import com.cryptium.coincurrently.ui.main.HomeTab.GlobalMarket.DataClasses.GlobalMarketChartData
import com.cryptium.coincurrently.ui.main.HomeTab.GlobalMarket.DataClasses.GlobalMarketChartModel
import com.cryptium.coincurrently.ui.main.HomeTab.GlobalMarket.DataClasses.GlobalMarketData
import com.cryptium.coincurrently.ui.main.HomeTab.GlobalMarket.DataClasses.GlobalMarketModel
import com.cryptium.coincurrently.ui.main.HomeTab.GlobalMarket.DataClasses.Percentages
import networking.ApiHelper
import networking.CacheUrls
import networking.MainRepository
import networking.ResponseModels.*
import networking.RetrofitBuilder
import java.util.*
import kotlin.collections.ArrayList
import com.cryptium.coincurrently.ui.main.Managers.StorageKeys
import com.cryptium.coincurrently.ui.main.Managers.StorageManager
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.math.max

data class CachedCoinsList(
    @SerializedName("coins")
    val coins: ArrayList<CoinsList>,
    @SerializedName("date")
    val date: Date
)

data class CachedGlobalMarket(
    // Nullable: Gson can deserialize incomplete cache payloads with a null data field.
    @SerializedName("data")
    val data: GlobalMarketData?,
    @SerializedName("date")
    val date: Date?
)

class GlobalMarketRepository {
    companion object {
        private const val TAG = "GlobalMarket"

        private val mainRepository by lazy { MainRepository(ApiHelper(RetrofitBuilder.apiService)) }

        private val coinsListType = object : TypeToken<ArrayList<CoinsList>>() {}.type
        private val globalMarketModelType = object : TypeToken<GlobalMarketModel>() {}.type
        private val globalMarketChartModelType = object : TypeToken<GlobalMarketChartModel>() {}.type

        var coinsList: ArrayList<CoinsList>? = null
        var globalMarket: GlobalMarketData? = StorageManager.get<CachedGlobalMarket>(StorageKeys.GLOBAL_MARKET_DATA_1)?.data
        var globalChart: MutableMap<String, GlobalMarketChartData>? = null

        var downloadingColors = false

        private val invalidIds = listOf("binance-", "bridged-", "harry", "mapped", "bridge-", "batcat", "nova", "osmosis-", "department", "open-", "tokamak-", "big-")

        init {
            try {
                @Suppress("UNCHECKED_CAST")
                coinsList = RetrofitBuilder.getCached(CacheUrls.coinsList(), coinsListType)?.data as? ArrayList<CoinsList>
            } catch (_: Exception) {
                // StorageManager/RetrofitBuilder may not be ready yet; cache will populate on first fetch
            }

            GlobalScope.launch {
                try {
                    getAllCoins()
                } catch (_: Exception) {
                    // Network / Retrofit may not be ready at process start; callers can fetch later
                }
            }
        }

        suspend fun getAllCoins(): ArrayList<CoinsList>? {
            val url = CacheUrls.coinsList()
            @Suppress("UNCHECKED_CAST")
            (RetrofitBuilder.getCached(url, coinsListType)?.data as? ArrayList<CoinsList>)?.let { data ->
                coinsList = data
                return data
            }

            Log.w("Network call ", "All coins")

            val coins = mainRepository.getCoinsList().unwrapOrNull()

            coins?.let {
                coinsList = it
            }

            return coins
        }

        /** Hydrates [globalChart] from Retrofit persisted cache before first network read. */
        fun loadCachedDataForGlobalChart(days: String, includeExpired: Boolean = true) {
            val url = CacheUrls.globalChart(days)

            @Suppress("UNCHECKED_CAST")
            (RetrofitBuilder.getCached(url, globalMarketChartModelType, includeExpired)?.data as? GlobalMarketChartModel)?.let { data ->
                filterGlobalChart(data, days)?.market_cap_chart?.let { chartData ->
                    globalChart = (globalChart ?: mutableMapOf()).also { it[days] = chartData }
                }
            }
        }

        suspend fun getGlobalChart(days: String): GlobalMarketChartData? {
            val url = CacheUrls.globalChart(days)

            @Suppress("UNCHECKED_CAST")
            (RetrofitBuilder.getCached(url, globalMarketChartModelType)?.data as? GlobalMarketChartModel)?.let { data ->
                val filtered = filterGlobalChart(data, days)
                filtered?.market_cap_chart?.let { chartData ->
                    globalChart = (globalChart ?: mutableMapOf()).also { it[days] = chartData }
                }
                return filtered?.market_cap_chart
            }

            Log.w("Network call ", "Global chart")

            val globalChartResult = mainRepository.getGlobalChart(days).unwrapOrNull()
            val filtered = filterGlobalChart(globalChartResult, days)

            filtered?.market_cap_chart?.let { chartData ->
                globalChart = (globalChart ?: mutableMapOf()).also { it[days] = chartData }
            }

            return filtered?.market_cap_chart
        }

        private fun filterGlobalChart(
            chart: GlobalMarketChartModel?,
            days: String
        ): GlobalMarketChartModel? {
            chart ?: return null
            val chartData = chart.market_cap_chart ?: return chart

            val divider = if (days == "max") {
                max(1, (chartData.market_cap?.size ?: 0) / 400)
            } else {
                1
            }

            val filteredData = GlobalMarketChartData(
                market_cap = filterDataPoints(
                    chartData.market_cap,
                    chartData.market_cap?.mapNotNull { it.lastOrNull() }?.maxOrNull() ?: 0.0,
                    chartData.market_cap?.mapNotNull { it.lastOrNull() }?.minOrNull() ?: 0.0,
                    divider
                ),
                volume = filterDataPoints(
                    chartData.volume,
                    chartData.volume?.mapNotNull { it.lastOrNull() }?.maxOrNull() ?: 0.0,
                    chartData.volume?.mapNotNull { it.lastOrNull() }?.minOrNull() ?: 0.0,
                    divider
                )
            )

            return GlobalMarketChartModel(market_cap_chart = filteredData)
        }

        private fun filterDataPoints(
            dataPoints: ArrayList<ArrayList<Double?>>?,
            maxElement: Double,
            minElement: Double,
            divider: Int
        ): ArrayList<ArrayList<Double?>>? {
            val safeDivider = max(1, divider)
            val filtered = dataPoints?.mapIndexedNotNull { index, element ->
                if (index % safeDivider == 0 || element.lastOrNull() == maxElement || element.lastOrNull() == minElement) {
                    element
                } else {
                    null
                }
            }

            return ArrayList(filtered?.map { ArrayList(it) } ?: arrayListOf())
        }

        suspend fun getGlobalMarket(): GlobalMarketData? {
            val url = CacheUrls.globalMarket()
            Log.i(TAG, "getGlobalMarket start url=$url")
            val cached = StorageManager.get<CachedGlobalMarket>(StorageKeys.GLOBAL_MARKET_DATA_1)
            // Gson can leave non-null Kotlin fields null at runtime if the payload was incomplete.
            val oldData = cached?.data
            val cacheExpired = cached?.date?.isMoreThanDefaultCache() == true
            val colorsReady = oldData?.percentages?.all { it.color != Color.GRAY } == true
            Log.d(
                TAG,
                "storage cache: hasCached=${cached != null} hasData=${oldData != null} " +
                    "date=${cached?.date} expired=$cacheExpired colorsReady=$colorsReady " +
                    "pctCount=${oldData?.percentages?.size} " +
                    "mcapKeys=${oldData?.total_market_cap?.keys} " +
                    "mcapPctKeys=${oldData?.market_cap_percentage?.keys}"
            )

            if (oldData != null && colorsReady && !cacheExpired) {
                Log.i(TAG, "returning fresh StorageManager cache (skip network)")
                Companion.globalMarket = oldData
                return oldData
            }

            @Suppress("UNCHECKED_CAST")
            val retrofitCached = RetrofitBuilder.getCached(url, globalMarketModelType)?.data as? GlobalMarketModel
            if (retrofitCached != null && retrofitCached.data != null) {
                Log.i(
                    TAG,
                    "using RetrofitBuilder cache mcapPctKeys=${retrofitCached.data?.market_cap_percentage?.keys}"
                )
                return processAndSaveGlobalMarket(retrofitCached, oldData)
            }
            if (retrofitCached != null && retrofitCached.data == null) {
                Log.w(TAG, "dropping RetrofitBuilder cache with null data for $url")
                RetrofitBuilder.removeCached(url)
            }
            Log.i(TAG, "no usable RetrofitBuilder cache for url")

            Log.w("Network call ", "Global market")
            Log.i(TAG, "fetching network global market")

            val result = mainRepository.getGlobalMarket()
            result.exceptionOrNull()?.let { err ->
                Log.e(TAG, "network Result failure: ${err.javaClass.simpleName}: ${err.message}", err)
            }
            val globalMarket = result.unwrapOrNull()
            Log.d(
                TAG,
                "network result: success=${result.isSuccess} modelNull=${globalMarket == null} " +
                    "dataNull=${globalMarket?.data == null} " +
                    "mcapPctKeys=${globalMarket?.data?.market_cap_percentage?.keys} " +
                    "active=${globalMarket?.data?.active_cryptocurrencies}"
            )
            return processAndSaveGlobalMarket(globalMarket, oldData)
        }

        private fun processAndSaveGlobalMarket(
            globalMarket: GlobalMarketModel?,
            oldData: GlobalMarketData?
        ): GlobalMarketData? {
            if (globalMarket == null) {
                Log.e(TAG, "processAndSaveGlobalMarket: model is null")
                return null
            }
            if (globalMarket.data == null) {
                Log.e(
                    TAG,
                    "processAndSaveGlobalMarket: model.data is null — JSON likely missing 'data' " +
                        "(R8 field rename or API error body). Check ProGuard keep + @SerializedName."
                )
                RetrofitBuilder.removeCached(CacheUrls.globalMarket())
                return null
            }
            Log.d(
                TAG,
                "processAndSave: mcapPct=${globalMarket.data?.market_cap_percentage?.size} " +
                    "active=${globalMarket.data?.active_cryptocurrencies} " +
                    "markets=${globalMarket.data?.markets}"
            )
            val percentageArray = arrayListOf<Percentages>()

            globalMarket.data?.market_cap_percentage?.toList()?.sortedByDescending { it.second }?.take(5)?.forEach { coin ->
                percentageArray.add(
                    Percentages(
                        coin.first.uppercase(),
                        oldData?.percentages?.firstOrNull { it.symbol?.lowercase() == coin.first.lowercase() }?.imageUrl,
                        coin.second / 100,
                        oldData?.percentages?.firstOrNull { it.symbol?.lowercase() == coin.first.lowercase() }?.color
                    )
                )
            }

            percentageArray.sortBy { it.percentage }
            percentageArray.reverse()
            percentageArray.add(
                Percentages(
                    "OTHER",
                    "",
                    1 - percentageArray.map { it.percentage ?: 0.0 }.sumOf { it },
                    null
                )
            )

            globalMarket.data?.percentages = percentageArray

            globalMarket.data?.let {
                StorageManager.set(
                    StorageKeys.GLOBAL_MARKET_DATA_1,
                    CachedGlobalMarket(it, Date())
                )
            }

            Companion.globalMarket = globalMarket.data
            Log.d(
                TAG,
                "processAndSave done: percentages=${globalMarket.data?.percentages?.size} " +
                    "symbols=${globalMarket.data?.percentages?.map { it.symbol }} " +
                    "usdMcap=${globalMarket.data?.total_market_cap?.get("usd")}"
            )
            return globalMarket.data
        }

        suspend fun getColors(
            context: Context,
            onUpdate: ((GlobalMarketData) -> Unit)? = null
        ): GlobalMarketData? {
            if (downloadingColors) {
                return null
            }

            if (coinsList?.isNotEmpty() == true && globalMarket != null) {
                val ids = getValidCoinIds(coinsList ?: listOf())

                if (ids.isEmpty()) {
                    return globalMarket
                }

                downloadingColors = true

                Log.w("Network call ", "Multiple coins")

                val currencyCode = GlobalFunctions.getSelectedCurrency(context).code.lowercase()

                val currencies = mainRepository.getMultipleCurrencies(ids.map { it }.joinToString { it }.replace(" ", "").removeSuffix(","), currencyCode).unwrapOrNull()
                downloadingColors = false

                currencies?.let {
                    it.forEach { coin ->
                        val imageView = CustomImageView(context)
                        imageView.setImageWithCompletion(coin.image, context) { color, _ ->

                            globalMarket?.percentages
                                ?.firstOrNull { it.symbol?.lowercase() == coin.symbol?.lowercase() }
                                ?.let { percentage ->
                                    percentage.color = color
                                    percentage.imageUrl = coin.image ?: ""
                                    globalMarket?.let { gm ->
                                        StorageManager.set(
                                            StorageKeys.GLOBAL_MARKET_DATA_1,
                                            CachedGlobalMarket(gm, Date())
                                        )
                                        onUpdate?.let { handler ->
                                            Handler(Looper.getMainLooper()).post {
                                                handler(gm)
                                            }
                                        }
                                    }
                                }
                        }
                    }
                }
                return globalMarket
            }
            return null
        }

        private fun getValidCoinIds(coins: List<CoinsList>): List<String> {
            val validCoins = globalMarket?.percentages?.filter { it.symbol != "OTHER" }

            return validCoins?.mapNotNull { coin ->
                coins.filter { it.symbol?.lowercase() == coin.symbol?.lowercase() }.firstOrNull { isValidId(it.id) }?.id
            } ?: listOf()
        }

        private fun isValidId(id: String?): Boolean {
            return !invalidIds.any { id?.contains(it) ?: false }
        }
    }
}