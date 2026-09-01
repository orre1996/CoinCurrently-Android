package com.cryptium.coincurrently.features.Home.Trending
import com.cryptium.coincurrently.app.SearchCase
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.cryptium.coincurrently.features.Home.Trending.TrendingRepository
import com.cryptium.coincurrently.core.networking.ResponseModels.Toplist
import com.cryptium.coincurrently.core.networking.ResponseModels.TrendingItems
import com.cryptium.coincurrently.core.networking.ResponseModels.toToplist

class TrendingViewModel: ViewModel() {
    val trendingRepository by lazy { TrendingRepository }

    var trendingList: TrendingItems? by mutableStateOf(null)

    init {
        trendingRepository.trendingList?.let {
            trendingList = it
        }
    }

    suspend fun getTrendingCoins() {
        trendingRepository.getTrendingList()?.let {
            trendingList = it
        }
    }

    fun getTrendingItems(searchCase: SearchCase): List<Toplist>? {
        return when (searchCase) {
            SearchCase.OnlyCoins, SearchCase.Coins -> {
                trendingList?.coins?.mapNotNull { it.item?.toToplist() }
            }
            SearchCase.NFTs -> {
                trendingList?.nfts?.map { it.toToplist() }
            }
            SearchCase.Categories -> {
                trendingList?.categories?.map { it.toToplist() }
            }
            SearchCase.Exchanges -> {
                emptyList()
            }
        }
    }
}