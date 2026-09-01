package com.cryptium.coincurrently.features.DerivativeDetail
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.*
import com.cryptium.coincurrently.core.extensions.getSelectedCurrency
import com.cryptium.coincurrently.sharedui.models.SortOption
import com.cryptium.coincurrently.core.persistence.StorageKeys
import com.cryptium.coincurrently.core.persistence.StorageManager
import com.cryptium.coincurrently.sharedui.models.CachedData
import com.cryptium.coincurrently.sharedui.models.PriceChange
import com.cryptium.coincurrently.sharedui.components.GlobalFunctions
import com.cryptium.coincurrently.core.persistence.CurrencyRepository
import com.cryptium.coincurrently.features.DerivativeDetail.DerivativeDetailRepository
import com.cryptium.coincurrently.features.Market.Repositories.ToplistRepository
import com.cryptium.coincurrently.core.networking.ResponseModels.CryptoDetailInfoModel
import com.cryptium.coincurrently.core.networking.ResponseModels.Derivative
import com.cryptium.coincurrently.core.networking.ResponseModels.DerivativeDetailModel
import com.cryptium.coincurrently.core.networking.ResponseModels.Toplist
import kotlin.text.uppercase

class DerivativeDetailsViewModelFactory(
    val derivative: Derivative
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DerivativeDetailsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DerivativeDetailsViewModel(derivative) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class DerivativeDetailsViewModel(
    derivative: Derivative
) : ViewModel() {

    private val currencyRepository by lazy { CurrencyRepository }
    private val derivativeDetailRepository by lazy { DerivativeDetailRepository }
    private val toplistRepository by lazy { ToplistRepository }

    var derivative: Derivative? by mutableStateOf(derivative)
    var toplist: List<Toplist>? by mutableStateOf(null)
    var bitcoin: CryptoDetailInfoModel? by mutableStateOf(null)
    var multiplier: Double? by mutableStateOf(null)

    var perpetualList: List<DerivativeDetailModel> by mutableStateOf(emptyList())
    var futuresList: List<DerivativeDetailModel> by mutableStateOf(emptyList())

    var selectedSortOption: SortOption by mutableStateOf(SortOption.openInterestDesc)

    init {
        currencyRepository.bitcoinData?.let {
            bitcoin = it

            calculatePriceMultiplier(
                bitcoin = it
            )
        }

        selectedSortOption = StorageManager.get<SortOption>(StorageKeys.DERIVATIVES_SORT_OPTION) ?: SortOption.openInterestDesc

        derivative.id?.let { id ->
            StorageManager.get<MutableMap<String, CachedData<Derivative>>>(StorageKeys.DERIVATIVE_DETAIL)?.get(id)?.data?.let { data ->
                this.derivative = data
                setLists()
            }
        }

        toplistRepository.toplist?.get("${SortOption.MktCapDesc.getOptionKey(PriceChange.SevenDays)}-null")?.data?.let { cachedToplist ->
            if (cachedToplist.isNotEmpty()) {
                toplist = cachedToplist
            }
        }
    }

    suspend fun getBitcoin() {
        currencyRepository.getBitcoin()?.let {
            bitcoin = it

            calculatePriceMultiplier(
                bitcoin = it
            )
        }
    }

    suspend fun getDerivatives() {
        derivativeDetailRepository.getDerivativeDetail(derivative?.id)?.let {
            derivative = it
            setLists()
        }
    }

    fun setSortOption(newSortOption: SortOption) {
        selectedSortOption = newSortOption
        StorageManager.set(StorageKeys.DERIVATIVES_SORT_OPTION, newSortOption)
        setLists()
    }

    private fun setLists() {
        val perpetuals = derivative?.tickers
            ?.filter { it.contract_type == "perpetual" }
            ?.sortedByDescending { it.open_interest_usd ?: 0.0 }
            ?: emptyList()

        perpetualList = sortToplist(derivatives = perpetuals, newSortOption = selectedSortOption)

        val futures = derivative?.tickers
            ?.filter { it.contract_type == "futures" }
            ?.sortedByDescending { it.open_interest_usd ?: 0.0 }
            ?: emptyList()

        futuresList = sortToplist(derivatives = futures, newSortOption = selectedSortOption)
    }

    private fun sortToplist(
        derivatives: List<DerivativeDetailModel>,
        newSortOption: SortOption
    ): List<DerivativeDetailModel> {
        return when (newSortOption) {
            SortOption.openInterestDesc -> derivatives.sortedByDescending { it.open_interest_usd ?: 0.0 }
            SortOption.openInterestAsc -> derivatives.sortedBy { it.open_interest_usd ?: 0.0 }
            SortOption.VolumeDesc -> derivatives.sortedByDescending { it.h24_volume ?: 0.0 }
            SortOption.VolumeAsc -> derivatives.sortedBy { it.h24_volume ?: 0.0 }
            SortOption.priceDesc -> derivatives.sortedByDescending { it.last ?: 0.0 }
            SortOption.priceAsc -> derivatives.sortedBy { it.last ?: 0.0 }
            SortOption.NameDesc -> derivatives.sortedByDescending { it.symbol?.lowercase() ?: "" }
            SortOption.NameAsc -> derivatives.sortedBy { it.symbol?.lowercase() ?: "" }
            else ->
                derivatives
        }
    }

    private fun calculatePriceMultiplier(bitcoin: CryptoDetailInfoModel) {
        val selectedCurrency = GlobalFunctions.getSelectedCurrency()
        val targetPrice = bitcoin.market_data?.current_price?.getSelectedCurrency(currency = selectedCurrency.code.uppercase()) ?: 0.0
        val basePrice = bitcoin.market_data?.current_price?.getSelectedCurrency(currency = "USD") ?: 0.0

        multiplier =  if (basePrice > 0) targetPrice / basePrice else null
    }
}