package com.cryptium.coincurrently.features.Widgets
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.cryptium.coincurrently.core.persistence.CurrencyRepository
import com.cryptium.coincurrently.core.networking.ResponseModels.Toplist

class MyWidgetsViewModel: ViewModel() {
    val currencyRepository by lazy { CurrencyRepository }
    var widgetCurrencies: List<Toplist> by mutableStateOf(listOf())

    init {
        if (currencyRepository.widgetCurrencies.isNotEmpty()) {
            widgetCurrencies = currencyRepository.widgetCurrencies
        }
    }

    fun changeOrder(currencies: List<Toplist>, context: Context) {
        currencyRepository.changeWidgetsOrder(currencies, context)
    }

    fun deleteWidget(coin: Toplist, context: Context) {
        currencyRepository.deleteWidget(coin, context)
        widgetCurrencies = currencyRepository.widgetCurrencies
    }
}