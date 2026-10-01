package com.cryptium.coincurrently.features.News

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.cryptium.coincurrently.core.networking.ResponseModels.NewNewsItem

class NewsViewModel : ViewModel() {
    private val newsRepository by lazy { NewsRepository }

    var news: List<NewNewsItem>? by mutableStateOf(null)

    suspend fun getNews() {
        newsRepository.getNews()?.let {
            news = it
        }
    }
}
