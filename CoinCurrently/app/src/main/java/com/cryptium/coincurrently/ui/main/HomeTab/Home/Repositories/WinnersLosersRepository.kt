package com.cryptium.coincurrently.ui.main.HomeTab.Home.Repositories

import android.util.Log
import com.cryptium.coincurrently.ui.main.Extensions.unwrapOrNull
import com.google.gson.reflect.TypeToken
import com.cryptium.coincurrently.ui.main.General.Enums.PriceChange
import com.cryptium.coincurrently.ui.main.General.Enums.SortOption
import com.cryptium.coincurrently.ui.main.General.GlobalFunctions
import com.cryptium.coincurrently.ui.main.HomeTab.Home.DataClasses.WinnersLosers
import com.cryptium.coincurrently.ui.main.Managers.StorageKeys
import com.cryptium.coincurrently.ui.main.Managers.StorageManager
import com.cryptium.coincurrently.ui.main.Models.CachedData
import networking.ApiHelper
import networking.CacheUrls
import networking.MainRepository
import networking.ResponseModels.Toplist
import networking.ResponseModels.getSortedToplist
import networking.RetrofitBuilder
import java.util.Date

class WinnersLosersRepository {
    companion object {
        private val mainRepository by lazy { MainRepository(ApiHelper(RetrofitBuilder.apiService)) }
        private var winnersLosersUrls: MutableMap<String, String>
            get() = StorageManager.get<MutableMap<String, String>>(StorageKeys.LAST_WINNERS_LOSERS_URLS) ?: mutableMapOf()
            set(value) { StorageManager.set(StorageKeys.LAST_WINNERS_LOSERS_URLS, value) }

        private val winnersLosersType = object : TypeToken<WinnersLosers>() {}.type

        private var winnerLoserIds: WinnersLosers? = null
        var winners: List<Toplist>? = null
        var losers: List<Toplist>? = null

        init {
            try {
                val duration = GlobalFunctions.preferredTimeFrameTextChange()  // "24h", "7d", "30d"
                val currency = GlobalFunctions.getSelectedCurrency().code
                winnerLoserIds = (RetrofitBuilder.getCached(CacheUrls.winnersLosers(currency, duration), winnersLosersType, includeExpired = true)?.data as? WinnersLosers)
                PriceChange.entries.forEach { priceChange ->
                    if (priceChange.rawValue == duration) {
                        winnersLosersUrls["winners_${priceChange.rawValue}"]?.let { url ->
                            winners = RetrofitBuilder.getCachedList<Toplist>(url, includeExpired = true)?.data
                                ?.getSortedToplist(SortOption.PriceChangeDesc, priceChange)
                        }
                        winnersLosersUrls["losers_${priceChange.rawValue}"]?.let { url ->
                            losers = RetrofitBuilder.getCachedList<Toplist>(url, includeExpired = true)?.data
                                ?.getSortedToplist(SortOption.PriceChangeAsc, priceChange)
                        }
                    }
                }
            } catch (_: Exception) {
                // StorageManager/RetrofitBuilder may not be ready yet; cache will populate on first fetch
            }
        }

        suspend fun getWinners(priceChange: PriceChange): ArrayList<Toplist>? {
            val url = winnersLosersUrls["winners_${priceChange.rawValue}"]
            url?.let {
                RetrofitBuilder.getCachedList<Toplist>(it)?.let { cached ->
                    if (cached.data.isNotEmpty()) {
                        val sorted = ArrayList(cached.data.getSortedToplist(SortOption.PriceChangeDesc, priceChange))
                        winners = sorted
                        return sorted
                    }
                }
            }

            val winnersLosers = getWinnersLosersList(priceChange)
            val coinIds = winnersLosers?.top_gainers?.joinToString(",") { it.id } ?: ""

            Log.w("Network call ", "Winners")

            val currency = GlobalFunctions.getSelectedCurrency().code
            val winnersUrl = CacheUrls.getMultipleCoins(coinIds, currency)
            RetrofitBuilder.getCachedList<Toplist>(winnersUrl)?.let {
                if (it.data.isNotEmpty()) {
                    val sorted = ArrayList(it.data.getSortedToplist(SortOption.PriceChangeDesc, priceChange))
                    winners = sorted
                    val urls = winnersLosersUrls.toMutableMap()
                    urls["winners_${priceChange.rawValue}"] = winnersUrl
                    winnersLosersUrls = urls
                    return sorted
                }
            }

            val result = mainRepository.getMultipleCurrencies(coinIds, currency).unwrapOrNull()?.let { raw ->
                ArrayList(raw.getSortedToplist(SortOption.PriceChangeDesc, priceChange))
            }
            result?.let {
                winners = it
                val urls = winnersLosersUrls.toMutableMap()
                urls["winners_${priceChange.rawValue}"] = winnersUrl
                winnersLosersUrls = urls
            }
            return result
        }

        suspend fun getLosers(priceChange: PriceChange): ArrayList<Toplist>? {
            val url = winnersLosersUrls["losers_${priceChange.rawValue}"]
            url?.let {
                RetrofitBuilder.getCachedList<Toplist>(it)?.let { cached ->
                    if (cached.data.isNotEmpty()) {
                        val sorted = ArrayList(cached.data.getSortedToplist(SortOption.PriceChangeAsc, priceChange))
                        losers = sorted
                        return sorted
                    }
                }
            }

            val winnersLosers = getWinnersLosersList(priceChange)
            val coinIds = winnersLosers?.top_losers?.joinToString(",") { it.id } ?: ""

            Log.w("Network call ", "Losers")

            val currency = GlobalFunctions.getSelectedCurrency().code
            val losersUrl = CacheUrls.getMultipleCoins(coinIds, currency)
            RetrofitBuilder.getCachedList<Toplist>(losersUrl)?.let {
                if (it.data.isNotEmpty()) {
                    val sorted = ArrayList(it.data.getSortedToplist(SortOption.PriceChangeAsc, priceChange))
                    losers = sorted
                    val urls = winnersLosersUrls.toMutableMap()
                    urls["losers_${priceChange.rawValue}"] = losersUrl
                    winnersLosersUrls = urls
                    return sorted
                }
            }

            val result = mainRepository.getMultipleCurrencies(coinIds, currency).unwrapOrNull()?.let { raw ->
                ArrayList(raw.getSortedToplist(SortOption.PriceChangeAsc, priceChange))
            }
            result?.let {
                losers = it
                val urls = winnersLosersUrls.toMutableMap()
                urls["losers_${priceChange.rawValue}"] = losersUrl
                winnersLosersUrls = urls
            }
            return result
        }

        private suspend fun getWinnersLosersList(priceChange: PriceChange): WinnersLosers? {
            val url = CacheUrls.winnersLosers(GlobalFunctions.getSelectedCurrency().code, priceChange.rawValue)
            (RetrofitBuilder.getCached(url, winnersLosersType)?.data as? WinnersLosers)?.let { data ->
                if (!data.top_gainers.isNullOrEmpty() && !data.top_losers.isNullOrEmpty()) {
                    winnerLoserIds = data
                    return data
                }
            }

            Log.w("Network call ", "Winner loser ids")

            val result = mainRepository.getWinnersLosers(GlobalFunctions.getSelectedCurrency().code, priceChange.rawValue).unwrapOrNull()
            result?.let {
                winnerLoserIds = it
            }
            return result
        }

        /** True if we previously loaded non-empty winners/losers for this timeframe (URL map + disk or in-memory rows). */
        fun hasCachedWinnerCoinRows(priceChange: PriceChange): Boolean {
            val key = "winners_${priceChange.rawValue}"
            winnersLosersUrls[key]?.let { url ->
                if (RetrofitBuilder.getCachedList<Toplist>(url, includeExpired = true)?.data?.isNotEmpty() == true) {
                    return true
                }
            }
            return winners?.isNotEmpty() == true && winnersLosersUrls.containsKey(key)
        }

        fun hasCachedLoserCoinRows(priceChange: PriceChange): Boolean {
            val key = "losers_${priceChange.rawValue}"
            winnersLosersUrls[key]?.let { url ->
                if (RetrofitBuilder.getCachedList<Toplist>(url, includeExpired = true)?.data?.isNotEmpty() == true) {
                    return true
                }
            }
            return losers?.isNotEmpty() == true && winnersLosersUrls.containsKey(key)
        }
    }
}