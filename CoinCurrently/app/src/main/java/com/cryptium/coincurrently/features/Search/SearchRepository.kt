package com.cryptium.coincurrently.features.Search
import android.util.Log
import com.cryptium.coincurrently.core.extensions.unwrapOrNull
import com.google.gson.reflect.TypeToken
import com.cryptium.coincurrently.core.persistence.StorageKeys
import com.cryptium.coincurrently.core.persistence.StorageManager
import com.cryptium.coincurrently.core.networking.ApiHelper
import com.cryptium.coincurrently.core.networking.CacheUrls
import com.cryptium.coincurrently.core.networking.MainRepository
import com.cryptium.coincurrently.core.networking.ResponseModels.SearchItem
import com.cryptium.coincurrently.core.networking.ResponseModels.SearchModel
import com.cryptium.coincurrently.core.networking.RetrofitBuilder

class SearchRepository {
    companion object {
        private val mainRepository by lazy { MainRepository(ApiHelper(RetrofitBuilder.apiService)) }

        private val searchModelType = object : TypeToken<SearchModel>() {}.type

        var searchHistory: MutableMap<String, List<SearchItem>> = run {
            val loaded = StorageManager.get<MutableMap<String, List<SearchItem>>>(StorageKeys.SEARCH_HISTORY) ?: mutableMapOf()
            // Gson can leave map values null (e.g. {"coins":null}); flatten()/iteration would NPE.
            @Suppress("UNCHECKED_CAST")
            val sanitized = loaded.mapValues { (_, v) ->
                (v as List<SearchItem>?) ?: emptyList()
            }.toMutableMap()
            val firstList = sanitized.values.firstOrNull()
            if (StorageManager.clearIfCorruptTypedList<SearchItem>(StorageKeys.SEARCH_HISTORY, firstList, "SearchRepo")) {
                mutableMapOf()
            } else {
                sanitized
            }
        }

        suspend fun getSearchResult(query: String): SearchModel? {
            val url = CacheUrls.search(query)

            @Suppress("UNCHECKED_CAST")
            (RetrofitBuilder.getCached(url, searchModelType)?.data as? SearchModel)?.let { data ->
                return data
            }

            Log.w("Network call ", "Search")

            return mainRepository.getSearchResults(query).unwrapOrNull()
        }

        fun appendSearchHistory(key: String, newCoin: SearchItem) {
            val currentMap = searchHistory
            val currentList = currentMap[key] ?: listOf()

            val updated = buildList {
                add(newCoin)
                currentList
                    .filter { it.id != newCoin.id }
                    .forEach { add(it) }
            }
            .distinctBy { it.id }
            .take(7)

            currentMap[key] = updated
            searchHistory = currentMap
            StorageManager.set(StorageKeys.SEARCH_HISTORY, currentMap)
        }
    }
}
