package com.cryptium.coincurrently.features.FearGreed
import android.util.Log
import com.cryptium.coincurrently.core.extensions.unwrapOrNull
import com.google.gson.reflect.TypeToken
import com.cryptium.coincurrently.core.networking.MainRepository
import com.cryptium.coincurrently.core.networking.ResponseModels.FearGreed
import com.cryptium.coincurrently.core.networking.RetrofitBuilder

object FearGreedRepository {
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

        return MainRepository.getFearGreedModel().unwrapOrNull()?.also { cachedFearGreed = it }
    }
}
