package com.cryptium.coincurrently.ui.main.HomeTab.FearGreed.Repositories

import android.util.Log
import com.cryptium.coincurrently.ui.main.Extensions.unwrapOrNull
import com.google.gson.reflect.TypeToken
import networking.ApiHelper
import networking.MainRepository
import networking.ResponseModels.FearGreed
import networking.RetrofitBuilder

class FearGreedRepository {
    companion object {
        private val mainRepository by lazy { MainRepository(ApiHelper(RetrofitBuilder.apiService)) }

        private val fearGreedType = object : TypeToken<FearGreed>() {}.type

        private var cachedFearGreed: FearGreed? = null

        var fearGreed: FearGreed?
            get() = cachedFearGreed
            set(value) { /* backing field not needed; cache is source of truth */ }

        init {
            try {
                @Suppress("UNCHECKED_CAST")
                cachedFearGreed = RetrofitBuilder.getCached(RetrofitBuilder.FEAR_GREED_URL, fearGreedType, includeExpired = true)?.data as? FearGreed
            } catch (_: Exception) {
                // StorageManager/RetrofitBuilder may not be ready yet; cache will populate on first fetch
            }
        }

        suspend fun getFearGreedModel(): FearGreed? {
            @Suppress("UNCHECKED_CAST")
            RetrofitBuilder.getCached(RetrofitBuilder.FEAR_GREED_URL, fearGreedType)?.let {
                cachedFearGreed = it.data as? FearGreed
                return it.data as? FearGreed
            }

            Log.w("Network call ", "Fear greed")

            return mainRepository.getFearGreedModel().unwrapOrNull()?.also { cachedFearGreed = it }
        }
    }
}