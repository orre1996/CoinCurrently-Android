package com.cryptium.coincurrently.features.Calculator
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.cryptium.coincurrently.features.Calculator.CalculatorRepository
import com.cryptium.coincurrently.features.Market.Toplist.Repositories.ToplistRepository
import kotlinx.coroutines.launch
import com.cryptium.coincurrently.core.networking.ResponseModels.CurrencySelectionModel
import com.cryptium.coincurrently.core.networking.ResponseModels.Toplist

class SettingsCalculatorViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SettingsCalculatorViewModelImp() as T
    }
}

interface SettingsCalculatorViewModel {
    var calculatorList: List<Toplist>?
    var input: Double

    suspend fun getCalculatorPrices()

    fun getAmount(
        id: String?,
        selectedCurrency: CurrencySelectionModel
    ): Double?
}

class SettingsCalculatorViewModelImp : ViewModel(), SettingsCalculatorViewModel {
    private val toplistRepository by lazy { ToplistRepository }
    private val calculatorRepository by lazy { CalculatorRepository }

    override var calculatorList: List<Toplist>? by mutableStateOf(listOf())
    private var calculatorPrices: MutableMap<String, Map<String, Double>>? by mutableStateOf(null)
    override var input: Double by mutableStateOf(1000.0)

    init {
        toplistRepository.smallToplist?.let {
            calculatorList = it
        }

        calculatorRepository.calculatorPrices?.let {
            calculatorPrices = it
        }
    }

    override suspend fun getCalculatorPrices() {
        toplistRepository.getSmallToplist()?.let {
            calculatorList = it
            getFilteredToplist()
        }
    }

    override fun getAmount(
        id: String?,
        selectedCurrency: CurrencySelectionModel
    ): Double? {
        if (calculatorPrices?.isEmpty() ?: true) {
            return null
        }

        try {
            val price = calculatorPrices?.get(id)?.get(selectedCurrency.code.lowercase()) ?: 0.0
            val input = input
            return input / price
        } catch (_: Exception) {
            return null
        }
    }

    private suspend fun getFilteredToplist() {
        calculatorList?.mapNotNull { it.id }?.let {
            calculatorRepository.getCalculatorPrices(it)?.let {
                calculatorPrices = it
            }
        }
    }
}
