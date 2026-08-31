package com.cryptium.coincurrently.ui.main.MarketTab.Exchanges.ViewModels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.*
import com.cryptium.coincurrently.ui.main.Managers.StorageKeys
import com.cryptium.coincurrently.ui.main.Managers.StorageManager
import com.cryptium.coincurrently.ui.main.Repositories.CurrencyRepository
import com.cryptium.coincurrently.ui.main.General.Enums.SortOption
import com.cryptium.coincurrently.ui.main.Repositories.ExchangesRepository
import networking.ResponseModels.CryptoDetailInfoModel
import networking.ResponseModels.ExchangeList

class ExchangesViewModel : ViewModel() {

    private val exchangesRepository by lazy { ExchangesRepository }
    private val currencyRepository by lazy { CurrencyRepository }

    var exchanges: List<ExchangeList> by mutableStateOf(listOf())
    var bitcoin: CryptoDetailInfoModel? by mutableStateOf(null)
    var selectedSortOption: SortOption by mutableStateOf(SortOption.MktCapDesc)

    init {
        selectedSortOption = StorageManager.get<SortOption>(StorageKeys.EXCHANGES_SORT_OPTION) ?: SortOption.TrustScoreDesc

        exchangesRepository.exchanges?.let {
            exchanges = sortToplist(
                exchanges = it,
                newSortOption = selectedSortOption,
            )
        }

        bitcoin = currencyRepository.bitcoinData
    }

    suspend fun getExchanges() {
        exchangesRepository.getExchanges()?.let {
            exchanges = sortToplist(
                exchanges = it,
                newSortOption = selectedSortOption
            )
        }
    }

    suspend fun getBitcoinData() {
        currencyRepository.getBitcoin()?.let {
            bitcoin = it
        }
    }

    fun setNewSortOption(newSortOption: SortOption) {
        selectedSortOption = newSortOption
        StorageManager.set(StorageKeys.EXCHANGES_SORT_OPTION, newSortOption)

        exchanges = sortToplist(
            exchanges = exchanges,
            newSortOption = selectedSortOption
        )
    }

    private fun sortToplist(
        exchanges: List<ExchangeList>,
        newSortOption: SortOption
    ): List<ExchangeList> {
        return when (newSortOption) {
            SortOption.NameDesc ->
                exchanges.sortedBy { it.name?.lowercase() ?: "" }
            SortOption.NameAsc ->
                exchanges.sortedByDescending { it.name?.lowercase() ?: "" }
            SortOption.VolumeDesc ->
                exchanges.sortedByDescending { it.trade_volume_24h_btc ?: 0.0 }
            SortOption.VolumeAsc ->
                exchanges.sortedBy { it.trade_volume_24h_btc ?: 0.0 }
            SortOption.TrustScoreDesc ->
                exchanges.sortedBy { it.trust_score_rank ?: Int.MAX_VALUE }
            SortOption.TrustScoreAsc ->
                exchanges.sortedByDescending { it.trust_score_rank ?: 0 }
            else ->
                exchanges
        }
    }
}
