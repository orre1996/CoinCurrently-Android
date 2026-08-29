package com.cryptium.coincurrently.ui.main.HomeTab.Home.Repositories

import android.util.Log
import com.cryptium.coincurrently.ui.main.Extensions.unwrapOrNull
import com.cryptium.coincurrently.ui.main.HomeTab.Home.Repositories.NewCoinsRepository
import com.google.gson.reflect.TypeToken
import com.cryptium.coincurrently.ui.main.General.GlobalFunctions
import com.cryptium.coincurrently.ui.main.Managers.StorageKeys
import com.cryptium.coincurrently.ui.main.Managers.StorageManager
import com.cryptium.coincurrently.ui.main.Models.CachedData
import com.cryptium.coincurrently.ui.main.Repositories.ToplistRepository
import networking.ApiHelper
import networking.CacheUrls
import networking.MainRepository
import networking.ResponseModels.Toplist
import networking.ResponseModels.TrendingItems
import networking.RetrofitBuilder

class TrendingRepository {
    companion object {
        private val mainRepository by lazy { MainRepository(ApiHelper(RetrofitBuilder.apiService)) }
        private val trendingItemsType = object : TypeToken<TrendingItems>() {}.type

        private var lastTrendingCoinsUrl: String?
            get() = StorageManager.get<String>(StorageKeys.LAST_TRENDING_COINS_URL)
            set(value) { value?.let { StorageManager.set(StorageKeys.LAST_TRENDING_COINS_URL, it) } }

        var trendingList: TrendingItems? = null
        var cachedTrendingCoins: ArrayList<Toplist>? = null

        init {
            try {
                trendingList = (RetrofitBuilder.getCached(CacheUrls.trending(), trendingItemsType)?.data as? TrendingItems)
                val trending = RetrofitBuilder.getCached(CacheUrls.trending(), trendingItemsType)
                val ids = (trending?.data as? TrendingItems)?.coins?.mapNotNull { it.item?.id }?.joinToString { it }?.replace(" ", "")?.removeSuffix(",")
                if (!ids.isNullOrEmpty()) {
                    val currency = GlobalFunctions.getSelectedCurrency(null)?.code?.lowercase() ?: "usd"
                    lastTrendingCoinsUrl = CacheUrls.getMultipleCoins(ids, currency)
                    cachedTrendingCoins = RetrofitBuilder.getCachedList<Toplist>(lastTrendingCoinsUrl!!, includeExpired = true)?.data
                }
            } catch (_: Exception) {
                // StorageManager/RetrofitBuilder may not be ready yet; cache will populate on first fetch
            }
        }

        suspend fun getTrendingCoins(): ArrayList<Toplist>? {
            lastTrendingCoinsUrl?.let { url ->
                RetrofitBuilder.getCachedList<Toplist>(url)?.let {
                    if (it.data.isNotEmpty()) {
                        cachedTrendingCoins = it.data
                        return orderedTrendingCoins(it.data)
                    }
                }
            }

            Log.w("Network call ", "Trending coins")

            val trendingCoins = getTrendingList()?.coins ?: return null
            val ids = trendingCoins.mapNotNull { it.item?.id }
            if (ids.isEmpty()) return null

            val coinIds = ids.joinToString { it }.replace(" ", "").removeSuffix(",")
            val currencyCode = GlobalFunctions.getSelectedCurrency(null).code.lowercase()
            val url = CacheUrls.getMultipleCoins(coinIds, currencyCode)
            lastTrendingCoinsUrl = url  // persists via setter

            RetrofitBuilder.getCachedList<Toplist>(url)?.let {
                if (it.data.isNotEmpty()) {
                    cachedTrendingCoins = it.data
                    ToplistRepository.updateFromToplist(it.data)
                    NewCoinsRepository.updateFromToplist(it.data)
                    return orderedTrendingCoins(it.data)
                }
            }

            val coins = mainRepository.getMultipleCurrencies(coinIds, currencyCode).unwrapOrNull()
            coins?.let {
                cachedTrendingCoins = it
                ToplistRepository.updateFromToplist(it)
                NewCoinsRepository.updateFromToplist(it)
            }
            return coins?.let { orderedTrendingCoins(it) }
        }

        fun orderedCachedTrendingCoins(): List<Toplist>? {
            return cachedTrendingCoins?.let { orderedTrendingCoins(it) }
        }

        private fun orderedTrendingCoins(coins: List<Toplist>): ArrayList<Toplist> {
            val trendingIds = trendingList?.coins?.mapNotNull { it.item?.id } ?: return ArrayList(coins)
            return coins.orderedByTrendingIds(trendingIds)
        }

        private fun List<Toplist>.orderedByTrendingIds(trendingIds: List<String>): ArrayList<Toplist> =
            trendingIds.mapNotNull { id -> firstOrNull { it.id == id } }.toCollection(ArrayList())

        suspend fun getTrendingList(): TrendingItems? {
            (RetrofitBuilder.getCached(CacheUrls.trending(), trendingItemsType)?.data as? TrendingItems)?.let { data ->
                trendingList = data
                return data
            }

            Log.w("Network call ", "Trending ids")

            val trendingItems = mainRepository.getTrendingCoins().unwrapOrNull()
            trendingItems?.let {
                trendingList = it
            }
            return trendingItems
        }

        fun updateFromToplist(coins: ArrayList<Toplist>?) {
            if (cachedTrendingCoins.isNullOrEmpty()) return

            cachedTrendingCoins?.forEachIndexed { index, coin ->
                coins?.firstOrNull { it.id == coin.id }?.let {
                    cachedTrendingCoins?.set(index, it)
                }
            }
            lastTrendingCoinsUrl?.let { url ->
                RetrofitBuilder.getCachedList<Toplist>(url, includeExpired = true)?.let { cached ->
                    RetrofitBuilder.updateCache(url, CachedData(cachedTrendingCoins ?: arrayListOf(), cached.date))
                }
            }
        }
    }
}
