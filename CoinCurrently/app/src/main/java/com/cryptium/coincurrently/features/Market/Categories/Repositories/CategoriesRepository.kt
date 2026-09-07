package com.cryptium.coincurrently.features.Market.Categories.Repositories
import android.util.Log
import com.cryptium.coincurrently.core.extensions.unwrapOrNull
import com.cryptium.coincurrently.sharedui.models.SortOption
import com.cryptium.coincurrently.sharedui.models.CachedData
import com.cryptium.coincurrently.core.networking.CacheUrls
import com.cryptium.coincurrently.core.networking.MainRepository
import com.cryptium.coincurrently.core.networking.ResponseModels.CryptoCategory
import com.cryptium.coincurrently.core.networking.RetrofitBuilder
import java.util.ArrayList
import java.util.Date

object CategoriesRepository {
    var categories: MutableMap<String, CachedData<ArrayList<CryptoCategory>>> = mutableMapOf()
        private set

    init {
        try {
            SortOption.categoriesOptions.forEach { sortOption ->
                val cacheKey = sortOption.categoriesApiValue
                RetrofitBuilder.getCachedList<CryptoCategory>(CacheUrls.categories(sortOption), includeExpired = true)?.let {
                    categories[cacheKey] = it
                }
            }
        } catch (_: Exception) {
            // StorageManager/RetrofitBuilder may not be ready yet; cache will populate on first fetch
        }
    }

    suspend fun getCategories(
        sortOption: SortOption
    ): List<CryptoCategory>? {
        val cacheKey = sortOption.categoriesApiValue
        RetrofitBuilder.getCachedList<CryptoCategory>(CacheUrls.categories(sortOption))?.let {
            if (it.data.isNotEmpty()) {
                categories[cacheKey] = it
                return it.data
            }
        }

        Log.w("Network call ", "Categories")

        val categoriesResult = MainRepository.getAllCategories(
            sortOption = sortOption
        ).unwrapOrNull()

        categoriesResult?.let {
            categories[cacheKey] = CachedData(ArrayList(it), Date())
        }

        return categoriesResult
    }
}
