package com.cryptium.coincurrently.ui.main.Repositories

import android.util.Log
import com.cryptium.coincurrently.ui.main.Extensions.unwrapOrNull
import com.cryptium.coincurrently.ui.main.General.Enums.SortOption
import com.cryptium.coincurrently.ui.main.Models.CachedData
import networking.ApiHelper
import networking.CacheUrls
import networking.MainRepository
import networking.ResponseModels.CryptoCategory
import networking.RetrofitBuilder
import java.util.ArrayList
import java.util.Date

class CategoriesRepository {
    companion object {
        private val mainRepository by lazy { MainRepository(ApiHelper(RetrofitBuilder.apiService)) }

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

            val categoriesResult = mainRepository.getAllCategories(
                sortOption = sortOption
            ).unwrapOrNull()

            categoriesResult?.let {
                categories[cacheKey] = CachedData(ArrayList(it), Date())
            }

            return categoriesResult
        }
    }
}
