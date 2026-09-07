package com.cryptium.coincurrently.features.Market.Derivatives.Repositories
import android.util.Log
import com.cryptium.coincurrently.core.extensions.unwrapOrNull
import com.cryptium.coincurrently.sharedui.models.SortOption
import com.cryptium.coincurrently.sharedui.models.CachedData
import com.cryptium.coincurrently.core.networking.CacheUrls
import com.cryptium.coincurrently.core.networking.MainRepository
import com.cryptium.coincurrently.core.networking.ResponseModels.Derivative
import com.cryptium.coincurrently.core.networking.RetrofitBuilder
import java.util.ArrayList
import java.util.Date

object DerivativesRepository {
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

        val derivativesList = MainRepository.getAllDerivatives(sortOption).unwrapOrNull()

        derivativesList?.let {
            derivatives = (derivatives + (cacheKey to CachedData(ArrayList(it), Date()))).toMutableMap()
            return it
        }

        return derivativesList
    }
}
