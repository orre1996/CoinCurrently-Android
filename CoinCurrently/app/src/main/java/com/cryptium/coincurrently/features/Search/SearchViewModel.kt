package com.cryptium.coincurrently.features.Search
import com.cryptium.coincurrently.app.SearchCase
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.cryptium.coincurrently.features.Search.SearchRepository
import com.cryptium.coincurrently.core.networking.ResponseModels.SearchItem
import com.cryptium.coincurrently.core.networking.ResponseModels.SearchModel

class SearchViewModel: ViewModel() {

    private val searchRepository by lazy { SearchRepository }

    var searchInput = mutableStateOf("")
    var searchResult: SearchModel? by mutableStateOf(null)
    var currentlySearching: Boolean by mutableStateOf(false)
    var searchHistory: MutableMap<String, List<SearchItem>> by mutableStateOf(searchRepository.searchHistory)
    var showSearchError: Boolean by mutableStateOf(false)

    suspend fun getSearchResult(query: String) {
        searchInput.value = query
        searchResult = null
        showSearchError = false

        if (query.isEmpty()) {
            return
        }

        currentlySearching = true
        try {
            searchRepository.getSearchResult(query)?.let {
                searchResult = it
            } ?: run {
                showSearchError = true
            }
        } finally {
            currentlySearching = false
        }
    }

    fun appendSearchHistory(searchCase: SearchCase, newCoin: SearchItem) {
        searchRepository.appendSearchHistory(searchCase.rawValue, newCoin)
        searchHistory = searchRepository.searchHistory
    }
}