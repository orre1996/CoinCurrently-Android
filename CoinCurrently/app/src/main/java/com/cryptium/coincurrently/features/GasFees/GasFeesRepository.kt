package com.cryptium.coincurrently.features.GasFees
import android.util.Log
import com.cryptium.coincurrently.core.extensions.unwrapOrNull
import com.google.gson.reflect.TypeToken
import com.cryptium.coincurrently.core.networking.CacheUrls
import com.cryptium.coincurrently.core.networking.MainRepository
import com.cryptium.coincurrently.core.networking.ResponseModels.GasFeesModel
import com.cryptium.coincurrently.core.networking.RetrofitBuilder

object GasFeesRepository {
    private val gasFeesType = object : TypeToken<GasFeesModel>() {}.type

    var cachedGasFees: GasFeesModel? = null
    var cachedGasFeesPrices: GasFeesModel? = null

    init {
        try {
            @Suppress("UNCHECKED_CAST")
            cachedGasFees = RetrofitBuilder.getCached(CacheUrls.gasFees(false), gasFeesType, includeExpired = true)?.data as? GasFeesModel
            @Suppress("UNCHECKED_CAST")
            cachedGasFeesPrices = RetrofitBuilder.getCached(CacheUrls.gasFees(true), gasFeesType, includeExpired = true)?.data as? GasFeesModel
        } catch (_: Exception) {
            // StorageManager/RetrofitBuilder may not be ready yet; cache will populate on first fetch
        }
    }

    suspend fun getGasFees(): GasFeesModel? {
        @Suppress("UNCHECKED_CAST")
        RetrofitBuilder.getCached(CacheUrls.gasFees(false), gasFeesType)?.let {
            cachedGasFees = it.data as? GasFeesModel
            return it.data as? GasFeesModel
        }

        Log.w("Network call ", "Gas fees")

        return MainRepository.getGasFees(includeHistory = false).unwrapOrNull()?.also { cachedGasFees = it }
    }

    suspend fun getGasFeesPrices(): GasFeesModel? {
        @Suppress("UNCHECKED_CAST")
        RetrofitBuilder.getCached(CacheUrls.gasFees(true), gasFeesType)?.let {
            cachedGasFeesPrices = it.data as? GasFeesModel
            return it.data as? GasFeesModel
        }

        Log.w("Network call ", "Gas fees prices")

        return MainRepository.getGasFees(includeHistory = true).unwrapOrNull()?.also { cachedGasFeesPrices = it }
    }
}
