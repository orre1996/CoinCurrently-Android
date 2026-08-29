package com.cryptium.coincurrently.ui.main.HomeTab.Home.Repositories

import android.util.Log
import com.cryptium.coincurrently.ui.main.Extensions.unwrapOrNull
import com.cryptium.coincurrently.ui.main.General.GlobalFunctions
import com.cryptium.coincurrently.ui.main.Managers.StorageKeys
import com.cryptium.coincurrently.ui.main.Managers.StorageManager
import com.cryptium.coincurrently.ui.main.Models.CachedData
import com.cryptium.coincurrently.ui.main.Repositories.ToplistRepository
import networking.ApiHelper
import networking.CacheUrls
import networking.MainRepository
import networking.ResponseModels.Toplist
import networking.RetrofitBuilder
import java.lang.Integer.min
import java.util.Date

class NewCoinsRepository {
    companion object {
        private val mainRepository by lazy { MainRepository(ApiHelper(RetrofitBuilder.apiService)) }
        private var lastNewCoinsUrl: String?
            get() = StorageManager.get<String>(StorageKeys.LAST_NEW_COINS_URL)
            set(value) { value?.let { StorageManager.set(StorageKeys.LAST_NEW_COINS_URL, it) } }

        var newCoins: ArrayList<Toplist>? = null

        init {
            try {
                newCoins = lastNewCoinsUrl?.let { url ->
                    RetrofitBuilder.getCachedList<Toplist>(url, includeExpired = true)?.data
                }
            } catch (_: Exception) {
                // StorageManager/RetrofitBuilder may not be ready yet; cache will populate on first fetch
            }
        }

        suspend fun getNewCoinIds(): ArrayList<Toplist>? {
            lastNewCoinsUrl?.let { url ->
                RetrofitBuilder.getCachedList<Toplist>(url)?.let {
                    if (it.data.isNotEmpty()) {
                        newCoins = it.data
                        return it.data
                    }
                }
            }

            Log.w("Network call ", "New Coins IDs")

            val newCoinIds = mainRepository.getNewCoins().unwrapOrNull()

            newCoinIds?.let {
                val coinIds = it.sortedBy { it.activated_at }.reversed().filter { id -> id.id != null }.subList(0, min(it.size, 25))
                val ids = coinIds.map { it.id ?: "" }.joinToString { it }.replace(" ", "").removeSuffix(",")
                return getNewCoins(ids)
            } ?: return null
        }

        private suspend fun getNewCoins(ids: String): ArrayList<Toplist>? {
            Log.w("Network call ", "New Coins")

            val currencyCode = GlobalFunctions.getSelectedCurrency().code.lowercase()
            val url = CacheUrls.getMultipleCoins(ids, currencyCode)
            lastNewCoinsUrl = url

            RetrofitBuilder.getCachedList<Toplist>(url)?.let {
                if (it.data.isNotEmpty()) {
                    newCoins = it.data
                    ToplistRepository.updateFromToplist(it.data)
                    TrendingRepository.updateFromToplist(it.data)
                    return it.data
                }
            }

            val result = mainRepository.getMultipleCurrencies(ids, currencyCode).unwrapOrNull()
            result?.let {
                newCoins = it
                ToplistRepository.updateFromToplist(it)
                TrendingRepository.updateFromToplist(it)
            }
            return result
        }

        fun updateFromToplist(coins: ArrayList<Toplist>?) {
            if (newCoins.isNullOrEmpty()) return

            newCoins?.forEachIndexed { index, coin ->
                coins?.firstOrNull { it.id == coin.id }?.let {
                    newCoins?.set(index, it)
                }
            }
            lastNewCoinsUrl?.let { url ->
                RetrofitBuilder.getCachedList<Toplist>(url, includeExpired = true)?.let { cached ->
                    RetrofitBuilder.updateCache(url, CachedData(newCoins ?: arrayListOf(), cached.date))
                }
            }
        }
    }
}