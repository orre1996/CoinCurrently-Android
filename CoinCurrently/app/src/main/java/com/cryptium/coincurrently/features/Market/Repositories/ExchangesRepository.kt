package com.cryptium.coincurrently.features.Market.Repositories
import android.util.Log
import com.cryptium.coincurrently.core.extensions.unwrapOrNull
import com.cryptium.coincurrently.core.networking.CacheUrls
import com.cryptium.coincurrently.core.networking.MainRepository
import com.cryptium.coincurrently.core.networking.ResponseModels.ExchangeList
import com.cryptium.coincurrently.core.networking.RetrofitBuilder

object ExchangesRepository {
    private var cachedExchanges: List<ExchangeList>? = null

    var exchanges: List<ExchangeList>?
        get() = cachedExchanges
        set(value) { /* cache is source of truth */ }

    init {
        try {
            cachedExchanges = RetrofitBuilder.getCachedList<ExchangeList>(CacheUrls.exchanges(), includeExpired = true)?.data
        } catch (_: Exception) {
            // StorageManager/RetrofitBuilder may not be ready yet; cache will populate on first fetch
        }
    }

    suspend fun getExchanges(): List<ExchangeList>? {
        RetrofitBuilder.getCachedList<ExchangeList>(CacheUrls.exchanges())?.let {
            if (it.data.isNotEmpty()) {
                cachedExchanges = it.data
                return it.data
            }
        }

        Log.w("Network call ", "Exchanges")

        return MainRepository.getExchangesToplist().unwrapOrNull()?.also { cachedExchanges = it }
    }
}
