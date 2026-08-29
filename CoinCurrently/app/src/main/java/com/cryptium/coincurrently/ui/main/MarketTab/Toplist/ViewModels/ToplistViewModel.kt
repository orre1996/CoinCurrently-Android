package com.cryptium.coincurrently.ui.main.MarketTab.Toplist.ViewModels

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.*
import com.cryptium.coincurrently.ui.main.Activities.MainActivity
import com.cryptium.coincurrently.ui.main.Managers.FirebaseManager
import com.cryptium.coincurrently.ui.main.Managers.SecretMessageHelper
import com.cryptium.coincurrently.ui.main.Repositories.ToplistRepository
import com.cryptium.coincurrently.ui.main.General.Enums.SortOption
import com.cryptium.coincurrently.ui.main.General.Enums.PriceChange
import com.cryptium.coincurrently.ui.main.Managers.StorageKeys
import com.cryptium.coincurrently.ui.main.Managers.StorageManager
import com.cryptium.coincurrently.ui.main.MarketTab.CryptoDetail.ViewModel.CryptoDetailViewModel
import com.cryptium.coincurrently.ui.main.Repositories.StoreKitManager
import com.cryptium.coincurrently.ui.main.Models.CachedData
import com.cryptium.coincurrently.ui.main.analytics.FavoriteAdded
import com.cryptium.coincurrently.ui.main.analytics.FavoriteRemoved
import com.cryptium.coincurrently.ui.main.analytics.AnalyticsManager
import com.cryptium.coincurrently.ui.main.analytics.ScreenLabel
import kotlinx.coroutines.launch
import networking.ResponseModels.*

class ToplistViewModelFactory(
    val category: CryptoCategory?,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ToplistViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ToplistViewModel(category) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class ToplistViewModel(
    val category: CryptoCategory?
) : ViewModel() {

    private val toplistRepository by lazy { ToplistRepository }

    var favoritesList: List<Toplist> by mutableStateOf(listOf())
    var toplist: List<Toplist> by mutableStateOf(listOf())

    var selectedSortOption: SortOption by mutableStateOf(SortOption.MktCapDesc)
    var selectedPriceChangeOption: PriceChange by mutableStateOf(PriceChange.SevenDays)

    init {
        selectedSortOption = StorageManager.get<SortOption>(StorageKeys.TOPLIST_SORT_OPTION) ?: SortOption.MktCapDesc
        selectedPriceChangeOption = StorageManager.get<PriceChange>(StorageKeys.TOPLIST_PRICE_CHANGE_OPTION) ?: PriceChange.SevenDays

        val cacheKey = "${selectedSortOption.getOptionKey(selectedPriceChangeOption)}-${category?.id}"
        toplistRepository.toplist?.get(cacheKey)?.let {
            if (it.data.isNotEmpty()) {
                toplist = it.data
            }
        }

        toplistRepository.favorites?.let {
            if (it.isNotEmpty()) {
                favoritesList = it
            }
        }

        viewModelScope.launch {
            FirebaseManager.ensureRemoteConfigFetched()
            val cacheKey = "${selectedSortOption.getOptionKey(selectedPriceChangeOption)}-${category?.id}"
            toplistRepository.toplist?.get(cacheKey)?.let {
                if (it.data.isNotEmpty()) {
                    toplist = if (category != null) {
                        SecretMessageHelper.withSecretToplist(
                            it.data,
                            SecretMessageHelper.PLACEMENT_CATEGORY_TOPLIST,
                            12,
                        )
                    } else {
                        it.data
                    }
                }
            }
        }
    }

    fun setNewSortOption(newSortOption: SortOption) {
        selectedSortOption = newSortOption
        StorageManager.set(StorageKeys.TOPLIST_SORT_OPTION, newSortOption)
    }

    fun setNewPriceChangeOption(newPriceChangeOption: PriceChange) {
        selectedPriceChangeOption = newPriceChangeOption
        StorageManager.set(StorageKeys.TOPLIST_PRICE_CHANGE_OPTION, newPriceChangeOption)
    }

    suspend fun getToplist(
        newSortOption: SortOption,
        newPriceChangeOption: PriceChange
    ) {
        if (newSortOption != selectedSortOption || newPriceChangeOption != selectedPriceChangeOption) {
            toplist = listOf()
        }

        if (newSortOption != selectedSortOption) {
            setNewSortOption(newSortOption)
        }

        if (newPriceChangeOption != selectedPriceChangeOption) {
            setNewPriceChangeOption(newPriceChangeOption)
        }

        FirebaseManager.ensureRemoteConfigFetched()

        toplistRepository.getToplist(
            sortOption = newSortOption,
            priceChange = newPriceChangeOption,
            platformId = category?.id
        )?.let {
            toplist = if (category != null) {
                SecretMessageHelper.withSecretToplist(
                    it,
                    SecretMessageHelper.PLACEMENT_CATEGORY_TOPLIST,
                    12,
                )
            } else {
                it
            }
        }
    }

    fun favouriteButtonPressed(
        toplistItem: Toplist,
        context: Context,
        showBuyPro: () -> Unit
    ) {
        if (StoreKitManager.currentSubscription.value == null && !favoritesList.any { it.id == toplistItem.id } && favoritesList.size >= 2) {
            showBuyPro.invoke()
            return
        }

        val wasFavorite = favoritesList.any { it.id == toplistItem.id }
        val favoriteFromScreen =
            if (category?.id.isNullOrBlank()) ScreenLabel.Toplist else ScreenLabel.CategoryDetail
        if (wasFavorite) {
            AnalyticsManager.log(
                FavoriteRemoved(
                    coinName = toplistItem.name ?: "",
                    fromScreen = favoriteFromScreen,
                ),
            )
        } else {
            AnalyticsManager.log(
                FavoriteAdded(
                    coinName = toplistItem.name ?: "",
                    fromScreen = favoriteFromScreen,
                ),
            )
        }
        favoritesList = toplistRepository.favouriteButtonPressed(toplistItem) ?: listOf()
        MainActivity.showAppReview(context)
    }
}