package com.cryptium.coincurrently.features.Home.NewCoins
import com.cryptium.coincurrently.core.persistence.UserPreferences
import android.util.Log
import com.cryptium.coincurrently.core.extensions.unwrapOrNull
import com.cryptium.coincurrently.core.persistence.StorageKeys
import com.cryptium.coincurrently.core.persistence.StorageManager
import com.cryptium.coincurrently.sharedui.models.CachedData
import com.cryptium.coincurrently.features.Market.Repositories.ToplistRepository
import com.cryptium.coincurrently.core.networking.CacheUrls
import com.cryptium.coincurrently.core.networking.MainRepository
import com.cryptium.coincurrently.core.networking.ResponseModels.Toplist
import com.cryptium.coincurrently.core.networking.RetrofitBuilder
import java.lang.Integer.min
import java.util.Date
import com.cryptium.coincurrently.features.Home.Trending.TrendingRepository

object NewCoinsRepository {
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

        val newCoinIds = MainRepository.getNewCoins().unwrapOrNull()

        newCoinIds?.let {
            val coinIds = it.sortedBy { it.activated_at }.reversed().filter { id -> id.id != null }.subList(0, min(it.size, 25))
            val ids = coinIds.map { it.id ?: "" }.joinToString { it }.replace(" ", "").removeSuffix(",")
            return getNewCoins(ids)
        } ?: return null
    }

    private suspend fun getNewCoins(ids: String): ArrayList<Toplist>? {
        Log.w("Network call ", "New Coins")

        val currencyCode = UserPreferences.getSelectedCurrency().code.lowercase()
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

        val result = MainRepository.getMultipleCurrencies(ids, currencyCode).unwrapOrNull()
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
