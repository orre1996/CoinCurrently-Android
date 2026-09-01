package com.cryptium.coincurrently.features.Treasuries
import android.util.Log
import com.cryptium.coincurrently.core.extensions.unwrapOrNull
import com.cryptium.coincurrently.sharedui.models.CachedData
import com.google.gson.reflect.TypeToken
import com.cryptium.coincurrently.core.networking.ApiHelper
import com.cryptium.coincurrently.core.networking.CacheUrls
import com.cryptium.coincurrently.core.networking.MainRepository
import com.cryptium.coincurrently.core.networking.ResponseModels.TreasuryDetailEntity
import com.cryptium.coincurrently.core.networking.ResponseModels.TreasuryHoldingChartModel
import com.cryptium.coincurrently.core.networking.ResponseModels.TreasuryTransactionHistoryModel
import com.cryptium.coincurrently.core.networking.RetrofitBuilder
import java.util.Date

class TreasuryDetailRepository {
    companion object {
        private val mainRepository by lazy { MainRepository(ApiHelper(RetrofitBuilder.apiService)) }

        private val treasuryDetailType = object : TypeToken<TreasuryDetailEntity>() {}.type
        private val holdingChartType = object : TypeToken<TreasuryHoldingChartModel>() {}.type
        private val transactionHistoryType = object : TypeToken<TreasuryTransactionHistoryModel>() {}.type

        var cachedTreasuryDetails: MutableMap<String, CachedData<TreasuryDetailEntity>> = mutableMapOf()
            private set

        var cachedHoldingCharts: MutableMap<String, CachedData<TreasuryHoldingChartModel>> = mutableMapOf()
            private set

        var cachedTransactionHistory: MutableMap<String, CachedData<TreasuryTransactionHistoryModel>> = mutableMapOf()
            private set

        private val loadedCacheKeys = mutableSetOf<String>()

        suspend fun ensureCacheLoaded(entityId: String, coinId: String, days: String) {
            val key = cacheKey(entityId, coinId, days)
            if (loadedCacheKeys.contains(key)) return
            loadedCacheKeys.add(key)

            val detailUrl = CacheUrls.treasuryDetail(entityId)
            @Suppress("UNCHECKED_CAST")
            RetrofitBuilder.getCached(detailUrl, treasuryDetailType, includeExpired = true)?.let { cached ->
                cachedTreasuryDetails[entityId] = cached as CachedData<TreasuryDetailEntity>
            }

            val chartUrl = CacheUrls.treasuryHoldingChart(entityId, coinId, days)
            @Suppress("UNCHECKED_CAST")
            RetrofitBuilder.getCached(chartUrl, holdingChartType, includeExpired = true)?.let { cached ->
                cachedHoldingCharts[chartCacheKey(entityId, coinId, days)] = cached as CachedData<TreasuryHoldingChartModel>
            }

            val historyUrl = CacheUrls.treasuryTransactionHistory(entityId)
            @Suppress("UNCHECKED_CAST")
            RetrofitBuilder.getCached(historyUrl, transactionHistoryType, includeExpired = true)?.let { cached ->
                cachedTransactionHistory[transactionHistoryCacheKey(entityId, null)] =
                    cached as CachedData<TreasuryTransactionHistoryModel>
            }
        }

        fun getCachedTreasuryDetail(entityId: String): TreasuryDetailEntity? {
            return cachedTreasuryDetails[entityId]?.data
        }

        fun getCachedHoldingChart(entityId: String, coinId: String, days: String): TreasuryHoldingChartModel? {
            return cachedHoldingCharts[chartCacheKey(entityId, coinId, days)]?.data
        }

        fun getCachedTransactionHistory(entityId: String, coinId: String?): TreasuryTransactionHistoryModel? {
            return cachedTransactionHistory[transactionHistoryCacheKey(entityId, coinId)]?.data
        }

        suspend fun getTreasuryDetail(entityId: String): TreasuryDetailEntity? {
            val url = CacheUrls.treasuryDetail(entityId)
            @Suppress("UNCHECKED_CAST")
            RetrofitBuilder.getCached(url, treasuryDetailType, includeExpired = true)?.let { cached ->
                cachedTreasuryDetails[entityId] = cached as CachedData<TreasuryDetailEntity>
            }

            @Suppress("UNCHECKED_CAST")
            RetrofitBuilder.getCached(url, treasuryDetailType)?.let { cached ->
                cachedTreasuryDetails[entityId] = cached as CachedData<TreasuryDetailEntity>
                return cached.data
            }

            Log.w("Network call ", "Treasury detail $entityId")

            val detail = mainRepository.getTreasuryDetail(entityId).unwrapOrNull()
            detail?.let {
                cachedTreasuryDetails[entityId] = CachedData(it, Date())
            }

            return detail
        }

        suspend fun getHoldingChart(
            entityId: String,
            coinId: String,
            days: String,
        ): TreasuryHoldingChartModel? {
            ensureCacheLoaded(entityId, coinId, days)

            val url = CacheUrls.treasuryHoldingChart(entityId, coinId, days)
            @Suppress("UNCHECKED_CAST")
            RetrofitBuilder.getCached(url, holdingChartType)?.let { cached ->
                cachedHoldingCharts[chartCacheKey(entityId, coinId, days)] = cached as CachedData<TreasuryHoldingChartModel>
                return cached.data
            }

            Log.w("Network call ", "Treasury holding chart $entityId/$coinId/$days")

            val chart = mainRepository.getTreasuryHoldingChart(entityId, coinId, days).unwrapOrNull()
            chart?.let {
                cachedHoldingCharts[chartCacheKey(entityId, coinId, days)] = CachedData(it, Date())
            }

            return chart
        }

        suspend fun getTransactionHistory(
            entityId: String,
            coinId: String?,
        ): TreasuryTransactionHistoryModel? {
            val url = CacheUrls.treasuryTransactionHistory(entityId, coinId)
            @Suppress("UNCHECKED_CAST")
            RetrofitBuilder.getCached(url, transactionHistoryType, includeExpired = true)?.let { cached ->
                cachedTransactionHistory[transactionHistoryCacheKey(entityId, coinId)] =
                    cached as CachedData<TreasuryTransactionHistoryModel>
            }

            @Suppress("UNCHECKED_CAST")
            RetrofitBuilder.getCached(url, transactionHistoryType)?.let { cached ->
                cachedTransactionHistory[transactionHistoryCacheKey(entityId, coinId)] =
                    cached as CachedData<TreasuryTransactionHistoryModel>
                return cached.data
            }

            Log.w("Network call ", "Treasury transaction history $entityId/${coinId ?: "all"}")

            val history = mainRepository.getTreasuryTransactionHistory(
                entityId = entityId,
                coinIds = coinId,
            ).unwrapOrNull()

            history?.let {
                cachedTransactionHistory[transactionHistoryCacheKey(entityId, coinId)] = CachedData(it, Date())
            }

            return history
        }

        private fun cacheKey(entityId: String, coinId: String, days: String): String {
            return "$entityId-$coinId-$days"
        }

        private fun chartCacheKey(entityId: String, coinId: String, days: String): String {
            return "$entityId-$coinId-$days"
        }

        private fun transactionHistoryCacheKey(entityId: String, coinId: String?): String {
            return "$entityId-${coinId ?: "all"}"
        }
    }
}
