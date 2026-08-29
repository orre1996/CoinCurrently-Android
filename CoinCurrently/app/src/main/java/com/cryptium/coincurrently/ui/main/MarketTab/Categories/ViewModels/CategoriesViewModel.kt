package com.cryptium.coincurrently.ui.main.MarketTab.Categories.ViewModels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.*
import com.cryptium.coincurrently.ui.main.Extensions.getSelectedCurrency
import com.cryptium.coincurrently.ui.main.General.Enums.SortOption
import com.cryptium.coincurrently.ui.main.General.GlobalFunctions
import com.cryptium.coincurrently.ui.main.Managers.StorageKeys
import com.cryptium.coincurrently.ui.main.Managers.StorageManager
import com.cryptium.coincurrently.ui.main.Repositories.CategoriesRepository
import com.cryptium.coincurrently.ui.main.Repositories.CurrencyRepository
import kotlinx.coroutines.launch
import networking.ResponseModels.*

class CategoriesViewModel : ViewModel() {

    private val categoriesRepository by lazy { CategoriesRepository }
    private val currencyRepository by lazy { CurrencyRepository }

    var categories: List<CryptoCategory> by mutableStateOf(listOf())
    var bitcoin: CryptoDetailInfoModel? by mutableStateOf(null)
    var selectedSortOption: SortOption by mutableStateOf(SortOption.MktCapDesc)
    var multiplier: Double? by mutableStateOf(null)

    init {
        selectedSortOption = StorageManager.get<SortOption>(StorageKeys.CATEGORIES_SORT_OPTION) ?: SortOption.MktCapDesc

        categoriesRepository.categories[selectedSortOption.categoriesApiValue]?.data?.let {
            categories = it
        }

        bitcoin = currencyRepository.bitcoinData
        setPriceMultiplier()
    }

    fun setNewSortOption(newSortOption: SortOption) {
        selectedSortOption = newSortOption
        StorageManager.set(StorageKeys.CATEGORIES_SORT_OPTION, newSortOption)
    }

    suspend fun getCategories(
        newSortOption: SortOption
    ) {
        if (newSortOption != selectedSortOption) {
            categories = listOf()
        }

        if (newSortOption != selectedSortOption) {
            setNewSortOption(newSortOption)
        }

        categoriesRepository.getCategories(
            sortOption = newSortOption
        )?.let {
            categories = it
        }
    }

    suspend fun getBitcoin() {
        currencyRepository.getBitcoin()?.let {
            bitcoin = it
            setPriceMultiplier()
        }
    }

    private fun setPriceMultiplier() {
        if (bitcoin == null) {
            return
        }

        val code = GlobalFunctions.getSelectedCurrency().code
        val target = bitcoin?.market_data?.current_price?.getSelectedCurrency(currency = code.uppercase()) ?: 0.0
        val base = bitcoin?.market_data?.current_price?.getSelectedCurrency(currency = "USD") ?: 0.0
        multiplier = target / base
    }
}
