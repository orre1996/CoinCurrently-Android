package com.cryptium.coincurrently.features.CoinDetail
import android.content.Context
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.cryptium.coincurrently.core.persistence.CurrencyRepository
import com.cryptium.coincurrently.core.networking.ResponseModels.CryptoDetailInfoModel
import com.cryptium.coincurrently.core.networking.ResponseModels.Toplist
import com.cryptium.coincurrently.core.networking.ResponseModels.toToplist

class AddToWidgetViewModelFactory(
    val cryptoDetailInfoModel: CryptoDetailInfoModel?
): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddToWidgetViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AddToWidgetViewModel(cryptoDetailInfoModel) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class AddToWidgetViewModel(
    val cryptoDetailInfoModel: CryptoDetailInfoModel?
): ViewModel() {
    private val currencyRepository by lazy { CurrencyRepository }

    var widgets: MutableState<List<Toplist>> = mutableStateOf(listOf())
    var justAddedToWidget: Boolean by mutableStateOf(false)

    fun init(context: Context) {
        widgets.value = currencyRepository.widgetCurrencies

        if (widgets.value.size < 8) {
            addCurrencyToWidgets(context)
            justAddedToWidget = true
        }
    }

    fun addCurrencyToWidgets(context: Context) {
        val toplistItem = cryptoDetailInfoModel?.toToplist() ?: return

        widgets.value = widgets.value.plus(toplistItem)
        currencyRepository.changeWidgetsOrder(widgets.value, context)
    }

    fun replaceWidget(replacedWidget: Toplist, context: Context) {
        val toplistItem = cryptoDetailInfoModel?.toToplist() ?: return
        val arrayList = ArrayList(widgets.value)

        val index = widgets.value.indexOf(replacedWidget)
        arrayList[index] = toplistItem
        widgets.value = arrayList.toList()
        currencyRepository.changeWidgetsOrder(widgets.value, context)
    }

    fun setWidgets(currencies: List<Toplist>, context: Context) {
        widgets.value = currencies
        currencyRepository.changeWidgetsOrder(currencies, context)
    }
}