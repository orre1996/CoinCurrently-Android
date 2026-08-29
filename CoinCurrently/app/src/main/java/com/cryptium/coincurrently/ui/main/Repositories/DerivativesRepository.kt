package com.cryptium.coincurrently.ui.main.Repositories

import android.util.Log
import com.cryptium.coincurrently.ui.main.Extensions.unwrapOrNull
import com.cryptium.coincurrently.ui.main.General.Enums.SortOption
import com.cryptium.coincurrently.ui.main.Models.CachedData
import networking.ApiHelper
import networking.CacheUrls
import networking.MainRepository
import networking.ResponseModels.Derivative
import networking.RetrofitBuilder
import java.util.ArrayList
import java.util.Date

class DerivativesRepository {
    companion object {
        private val mainRepository by lazy { MainRepository(ApiHelper(RetrofitBuilder.apiService)) }

        var derivatives: MutableMap<String, CachedData<ArrayList<Derivative>>> = mutableMapOf()
            private set

        init {
            try {
                SortOption.derivativesOptions.forEach { sortOption ->
                    val cacheKey = sortOption.name
                    RetrofitBuilder.getCachedList<Derivative>(CacheUrls.derivatives(sortOption), includeExpired = true)?.let {
                        derivatives = (derivatives + (cacheKey to it)).toMutableMap()
                    }
                }
            } catch (_: Exception) {
                // StorageManager/RetrofitBuilder may not be ready yet; cache will populate on first fetch
            }
        }

        suspend fun getDerivatives(
            sortOption: SortOption
        ): List<Derivative>? {
            val cacheKey = sortOption.name

            RetrofitBuilder.getCachedList<Derivative>(CacheUrls.derivatives(sortOption))?.let {
                if (it.data.isNotEmpty()) {
                    derivatives = (derivatives + (cacheKey to it)).toMutableMap()
                    return it.data
                }
            }

            Log.w("Network call ", "Derivatives")

            val derivativesList = mainRepository.getAllDerivatives(sortOption).unwrapOrNull()

            derivativesList?.let {
                derivatives = (derivatives + (cacheKey to CachedData(ArrayList(it), Date()))).toMutableMap()
                return it
            }

            return derivativesList
        }
    }
}
