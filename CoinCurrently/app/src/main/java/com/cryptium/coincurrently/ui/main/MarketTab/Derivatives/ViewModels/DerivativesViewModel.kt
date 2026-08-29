package com.cryptium.coincurrently.ui.main.MarketTab.Derivatives.ViewModels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.*
import com.cryptium.coincurrently.ui.main.General.Enums.SortOption
import com.cryptium.coincurrently.ui.main.Managers.StorageKeys
import com.cryptium.coincurrently.ui.main.Managers.StorageManager
import com.cryptium.coincurrently.ui.main.Repositories.CurrencyRepository
import com.cryptium.coincurrently.ui.main.Repositories.DerivativesRepository
import kotlinx.coroutines.launch
import networking.ResponseModels.*

class DerivativesViewModel : ViewModel() {

    private val currencyRepository by lazy { CurrencyRepository }
    private val derivativesRepository by lazy { DerivativesRepository }

    var allDerivatives: List<Derivative> by mutableStateOf(listOf())
    var bitcoin: CryptoDetailInfoModel? by mutableStateOf(null)

    var selectedSortOption: SortOption by mutableStateOf(SortOption.openInterestDesc)

    init {
        selectedSortOption = StorageManager.get<SortOption>(StorageKeys.DERIVATIVES_SORT_OPTION) ?: SortOption.openInterestDesc

        derivativesRepository.derivatives[selectedSortOption.name]?.data?.let {
            allDerivatives = it
        }

        bitcoin = currencyRepository.bitcoinData
    }

    fun setNewSortOption(newSortOption: SortOption) {
        selectedSortOption = newSortOption
        StorageManager.set(StorageKeys.DERIVATIVES_SORT_OPTION, newSortOption)
    }

    suspend fun getBitcoin() {
        currencyRepository.getBitcoin()?.let {
            bitcoin = it
        }
    }

    suspend fun getDerivatives(
        newSortOption: SortOption
    ) {
        if (newSortOption != selectedSortOption) {
            allDerivatives = listOf()
        }

        if (newSortOption != selectedSortOption) {
            setNewSortOption(newSortOption)
        }

        derivativesRepository.getDerivatives(selectedSortOption)?.let {
            allDerivatives = it
        }
    }
}
