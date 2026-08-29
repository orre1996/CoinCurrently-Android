package com.cryptium.coincurrently.ui.main.HomeTab.Home.Repositories

import android.util.Log
import com.cryptium.coincurrently.ui.main.Extensions.unwrapOrNull
import com.google.gson.reflect.TypeToken
import networking.ApiHelper
import networking.CacheUrls
import networking.MainRepository
import networking.ResponseModels.GasFeesModel
import networking.RetrofitBuilder

class GasFeesRepository {
    companion object {
        private val mainRepository by lazy { MainRepository(ApiHelper(RetrofitBuilder.apiService)) }

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

            return mainRepository.getGasFees(includeHistory = false).unwrapOrNull()?.also { cachedGasFees = it }
        }

        suspend fun getGasFeesPrices(): GasFeesModel? {
            @Suppress("UNCHECKED_CAST")
            RetrofitBuilder.getCached(CacheUrls.gasFees(true), gasFeesType)?.let {
                cachedGasFeesPrices = it.data as? GasFeesModel
                return it.data as? GasFeesModel
            }

            Log.w("Network call ", "Gas fees prices")

            return mainRepository.getGasFees(includeHistory = true).unwrapOrNull()?.also { cachedGasFeesPrices = it }
        }
    }
}