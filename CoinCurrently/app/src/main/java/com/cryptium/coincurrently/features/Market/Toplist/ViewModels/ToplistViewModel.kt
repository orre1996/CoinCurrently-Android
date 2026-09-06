package com.cryptium.coincurrently.features.Market.Toplist.ViewModels
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.*
import com.cryptium.coincurrently.app.MainActivity
import com.cryptium.coincurrently.features.Market.Repositories.ToplistRepository
import com.cryptium.coincurrently.sharedui.models.SortOption
import com.cryptium.coincurrently.sharedui.models.PriceChange
import com.cryptium.coincurrently.core.persistence.StorageKeys
import com.cryptium.coincurrently.core.persistence.StorageManager
import com.cryptium.coincurrently.features.CoinDetail.CryptoDetailViewModel
import com.cryptium.coincurrently.core.purchases.BillingManager
import com.cryptium.coincurrently.sharedui.models.CachedData
import com.cryptium.coincurrently.core.analytics.FavoriteAdded
import com.cryptium.coincurrently.core.analytics.FavoriteRemoved
import com.cryptium.coincurrently.core.analytics.AnalyticsManager
import com.cryptium.coincurrently.core.analytics.ScreenLabel
import com.cryptium.coincurrently.core.networking.ResponseModels.*

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

        toplistRepository.getToplist(
            sortOption = newSortOption,
            priceChange = newPriceChangeOption,
            platformId = category?.id
        )?.let {
            toplist = it
        }
    }

    fun favouriteButtonPressed(
        toplistItem: Toplist,
        context: Context,
        showBuyPro: () -> Unit
    ) {
        if (BillingManager.currentSubscription.value == null && !favoritesList.any { it.id == toplistItem.id } && favoritesList.size >= 2) {
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