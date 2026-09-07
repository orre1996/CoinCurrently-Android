package com.cryptium.coincurrently.features.Market.Toplist.Repositories
import com.cryptium.coincurrently.core.persistence.UserPreferences
import android.util.Log
import com.cryptium.coincurrently.core.extensions.isMoreThanDefaultCache
import com.cryptium.coincurrently.core.extensions.unwrapOrNull
import com.cryptium.coincurrently.sharedui.models.PriceChange
import com.cryptium.coincurrently.sharedui.models.SortOption
import com.cryptium.coincurrently.features.Home.NewCoins.NewCoinsRepository
import com.cryptium.coincurrently.features.Home.Trending.TrendingRepository
import com.cryptium.coincurrently.features.Home.WinnersLosers.WinnersLosersRepository
import com.cryptium.coincurrently.core.persistence.StorageKeys
import com.cryptium.coincurrently.core.persistence.StorageManager
import com.cryptium.coincurrently.features.Achievements.Managers.AchievementManager
import com.cryptium.coincurrently.features.Achievements.Models.AchievementMetric
import com.cryptium.coincurrently.sharedui.models.CachedData
import com.cryptium.coincurrently.core.networking.CacheUrls
import com.cryptium.coincurrently.core.networking.MainRepository
import com.cryptium.coincurrently.core.networking.ResponseModels.Toplist
import com.cryptium.coincurrently.core.networking.RetrofitBuilder
import java.util.*
import kotlin.collections.ArrayList

object ToplistRepository {
    private val winnersLosersRepository by lazy { WinnersLosersRepository }
    private val cacheKeyToUrl = mutableMapOf<String, String>()

    var toplist: MutableMap<String, CachedData<ArrayList<Toplist>>> = mutableMapOf()
        private set
    var smallToplist: ArrayList<Toplist>? = null
    var favorites: ArrayList<Toplist>? = null

    init {
        try {
            val currency = UserPreferences.getSelectedCurrency().code.lowercase()
            smallToplist = RetrofitBuilder.getCachedList<Toplist>(CacheUrls.smallToplist(currency), includeExpired = true)?.data
            val favoriteCached = StorageManager.get<CachedData<ArrayList<Toplist>>>(StorageKeys.FAVORITE)
            favorites = favoriteCached?.data
            if (StorageManager.clearIfCorruptTypedList<Toplist>(StorageKeys.FAVORITE, favorites, "ToplistRepo")) {
                favorites = null
            }
            listOf(SortOption.MktCapDesc, SortOption.MktCapAsc, SortOption.VolumeDesc, SortOption.VolumeAsc).forEach { sortOption ->
                val key = "${sortOption.getOptionKey(PriceChange.SevenDays)}-null"
                val url = CacheUrls.toplist(currency, sortOption, 250, null)
                RetrofitBuilder.getCachedList<Toplist>(url, includeExpired = true)?.let {
                    toplist = (toplist + (key to it)).toMutableMap()
                    cacheKeyToUrl[key] = url
                }
            }
        } catch (_: Exception) {
            // StorageManager/RetrofitBuilder may not be ready yet; cache will populate on first fetch
        }
    }

    suspend fun getSmallToplist(): ArrayList<Toplist>? {
        val currencyCode = UserPreferences.getSelectedCurrency().code.lowercase()
        RetrofitBuilder.getCachedList<Toplist>(CacheUrls.smallToplist(currencyCode))?.let {
            if (it.data.isNotEmpty()) {
                smallToplist = it.data
                return it.data
            }
        }

        Log.w("Network call ", "Small toplist")

        val coins = MainRepository.getToplist(
            currencyCode,
            order = SortOption.MktCapDesc,
            itemsPerPage = 25,
            platformId = null
        ).unwrapOrNull()

        coins?.let {
            smallToplist = it

            TrendingRepository.updateFromToplist(favorites)
            NewCoinsRepository.updateFromToplist(favorites)
        }

        return coins
    }

    suspend fun getToplist(
        sortOption: SortOption,
        priceChange: PriceChange,
        platformId: String?
    ): ArrayList<Toplist>? {
        val cacheKey = "${sortOption.getOptionKey(priceChange)}-$platformId"
        val currencyCode = UserPreferences.getSelectedCurrency(null).code.lowercase()

        if (sortOption != SortOption.PriceChangeDesc && sortOption != SortOption.PriceChangeAsc) {
            val url = CacheUrls.toplist(currencyCode, sortOption, 250, platformId)
            RetrofitBuilder.getCachedList<Toplist>(url)?.let {
                if (it.data.isNotEmpty()) {
                    toplist = (toplist + (cacheKey to it)).toMutableMap()
                    cacheKeyToUrl[cacheKey] = url
                    return it.data
                }
            }
        }

        val coins: List<Toplist>? = when {
            sortOption == SortOption.PriceChangeDesc -> winnersLosersRepository.getWinners(priceChange)
            sortOption == SortOption.PriceChangeAsc -> winnersLosersRepository.getLosers(priceChange)
            else -> MainRepository.getToplist(
                currencyCode,
                order = sortOption,
                itemsPerPage = 250,
                platformId = platformId
            ).unwrapOrNull()
        }

        if (sortOption != SortOption.PriceChangeDesc && sortOption != SortOption.PriceChangeAsc) {
            Log.w("Network call ", "Large toplist")
        }

        coins?.let {
            val url = CacheUrls.toplist(currencyCode, sortOption, 250, platformId)
            val cached = CachedData(ArrayList(it), Date())
            toplist = (toplist + (cacheKey to cached)).toMutableMap()
            cacheKeyToUrl[cacheKey] = url

            TrendingRepository.updateFromToplist(it as ArrayList<Toplist>)
            NewCoinsRepository.updateFromToplist(it as ArrayList<Toplist>)
        }

        return coins?.let { ArrayList(it) }
    }

    fun favouriteButtonPressed(item: Toplist): ArrayList<Toplist>? {
        if (favorites == null) {
            favorites = arrayListOf()
        }

        val previousCount = favorites?.size?.toDouble() ?: 0.0
        val isAdding = favorites?.none { it.id == item.id } != false

        favorites?.firstOrNull { it.id == item.id }?.let {
            favorites?.removeAll { it.id == item.id }
        } ?: run {
            favorites?.add(item)
        }

        favorites?.let {
            StorageManager.set(StorageKeys.FAVORITE, CachedData(it, Date()))
            UserPreferences.setFavorites(favorites?.mapNotNull { it.id } ?: listOf())
        }

        if (isAdding) {
            AchievementManager.checkMetricUnlock(
                metric = AchievementMetric.favoritesCount,
                previousValue = previousCount,
                newValue = previousCount + 1,
            )
        }

        return favorites
    }

    suspend fun getFavorites(): List<Toplist>? {
        val favoriteIds = UserPreferences.getFavorites()

        if (favoriteIds.isEmpty()) {
            return null
        }

        if (favorites == null) {
            favorites = arrayListOf()
        }

        val toplistResult = getToplist(
            sortOption = SortOption.MktCapDesc,
            priceChange = PriceChange.SevenDays,
            platformId = null
        )

        val localFavorites = mutableListOf<Toplist>()

        favoriteIds.forEach { id ->
            toplistResult?.firstOrNull { it.id == id }?.let { coin ->
                localFavorites.add(coin)
            }
        }

        localFavorites.forEach { coin ->
            val index = favorites?.indexOfFirst { it.id == coin.id } ?: -1
            if (index >= 0) {
                favorites?.set(index, coin)
            } else {
                favorites?.add(coin)
            }
        }

        favorites?.let {
            StorageManager.set(StorageKeys.FAVORITE, CachedData(it, Date()))
        }

        val otherFavorites = favoriteIds.filterNot { id ->
            localFavorites.any { it.id == id }
        }

        if (otherFavorites.isEmpty()) {
            return favorites
        }

        Log.w("Network call ", "Favorites")

        val ids = otherFavorites.joinToString{ it }.replace(" ", "")
        val favoriteCoins: List<Toplist> = MainRepository.getMultipleCurrencies(ids, UserPreferences.getSelectedCurrency().code).unwrapOrNull() ?: return null

        favoriteCoins.forEach { coin ->
            val index = favorites?.indexOfFirst { it.id == coin.id } ?: -1
            if (index >= 0) {
                favorites?.set(index, coin)
            } else {
                favorites?.add(coin)
            }
        }

        favorites?.let {
            StorageManager.set(StorageKeys.FAVORITE, CachedData(it, Date()))
        }

        TrendingRepository.updateFromToplist(favorites)
        NewCoinsRepository.updateFromToplist(favorites)
        // WinnersLosersRepository.updateFromToplist(favoriteCoins)

        return favorites
    }

    suspend fun getCoin(id: String): Toplist? {
        val currencyCode = UserPreferences.getSelectedCurrency().code.lowercase()
        val url = CacheUrls.getMultipleCoins(id, currencyCode)

        RetrofitBuilder.getCachedList<Toplist>(url)?.data?.firstOrNull { it.id == id }?.let {
            return it
        }

        Log.w("Network call ", "Single coin")

        val coins = MainRepository.getMultipleCurrencies(id, currencyCode).unwrapOrNull()
        val coin = coins?.firstOrNull { it.id == id } ?: coins?.firstOrNull() ?: return null

        updateFromToplist(arrayListOf(coin))
        return coin
    }

    fun updateFromToplist(coins: ArrayList<Toplist>) {
        if (coins.isEmpty()) return

        toplist.forEach { (key, value) ->
            val tempToplist = value.data
            coins.forEachIndexed { index, coin ->
                val coinIndex = tempToplist.indexOfFirst { it.id == coin.id }
                if (coinIndex != -1) tempToplist[coinIndex] = coins[index]
                val favIndex = favorites?.indexOfFirst { it.id == coin.id } ?: -1
                if (favIndex != -1) favorites?.set(favIndex, coins[index])
            }
            toplist[key] = CachedData(tempToplist, value.date)
            cacheKeyToUrl[key]?.let { url ->
                RetrofitBuilder.updateCache(url, CachedData(tempToplist, value.date))
            }
        }
        StorageManager.get<CachedData<ArrayList<Toplist>>>(StorageKeys.FAVORITE)?.let {
            StorageManager.set(StorageKeys.FAVORITE, CachedData(favorites ?: arrayListOf(), it.date))
        }
    }
}
