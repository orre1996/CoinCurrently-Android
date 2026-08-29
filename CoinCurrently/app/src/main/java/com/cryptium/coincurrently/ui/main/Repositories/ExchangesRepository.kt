package com.cryptium.coincurrently.ui.main.Repositories

import android.util.Log
import com.cryptium.coincurrently.ui.main.Extensions.unwrapOrNull
import networking.ApiHelper
import networking.CacheUrls
import networking.MainRepository
import networking.ResponseModels.ExchangeList
import networking.RetrofitBuilder

class ExchangesRepository {
    companion object {
        private val mainRepository by lazy { MainRepository(ApiHelper(RetrofitBuilder.apiService)) }

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

            return mainRepository.getExchangesToplist().unwrapOrNull()?.also { cachedExchanges = it }
        }
    }
}
