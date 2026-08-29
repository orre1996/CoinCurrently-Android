package com.cryptium.coincurrently.ui.main.HomeTab.Trending

import SearchCase
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.cryptium.coincurrently.ui.main.HomeTab.Home.Repositories.TrendingRepository
import com.cryptium.coincurrently.ui.main.Managers.FirebaseManager
import com.cryptium.coincurrently.ui.main.Managers.SecretMessageHelper
import networking.ResponseModels.Toplist
import networking.ResponseModels.TrendingItems
import networking.ResponseModels.toToplist

class TrendingViewModel: ViewModel() {
    val trendingRepository by lazy { TrendingRepository }

    var trendingList: TrendingItems? by mutableStateOf(null)

    init {
        trendingRepository.trendingList?.let {
            trendingList = it
        }
    }

    suspend fun getTrendingCoins() {
        FirebaseManager.ensureRemoteConfigFetched()
        trendingRepository.getTrendingList()?.let {
            trendingList = it
        }
    }

    fun getTrendingItems(searchCase: SearchCase): List<Toplist>? {
        val items = when (searchCase) {
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
        } ?: return null

        if (searchCase == SearchCase.OnlyCoins || searchCase == SearchCase.Coins) {
            return SecretMessageHelper.withSecretToplist(
                items,
                SecretMessageHelper.PLACEMENT_TRENDING,
                4,
            )
        }

        return items
    }
}