package com.cryptium.coincurrently.ui.main.Repositories

import android.util.Log
import com.cryptium.coincurrently.ui.main.Extensions.unwrapOrNull
import com.google.gson.reflect.TypeToken
import com.cryptium.coincurrently.ui.main.SettingsTab.Compose.allCurrencies
import networking.ApiHelper
import networking.CacheUrls
import networking.MainRepository
import networking.RetrofitBuilder

class CalculatorRepository {
    companion object {
        private val mainRepository by lazy { MainRepository(ApiHelper(RetrofitBuilder.apiService)) }

        private val calculatorPricesType = object : TypeToken<MutableMap<String, Map<String, Double>>>() {}.type

        var calculatorPrices: MutableMap<String, Map<String, Double>>? = null

        suspend fun getCalculatorPrices(coinIds: List<String>): MutableMap<String, Map<String, Double>>? {
            val ids = coinIds.joinToString { it }.replace(" ", "").removeSuffix(",")
            val vsCurrencies = allCurrencies.map { it.code.uppercase() }.joinToString { it }.replace(" ", "").removeSuffix(",")
            val url = CacheUrls.calculatorPrices(ids, vsCurrencies)

            @Suppress("UNCHECKED_CAST")
            (RetrofitBuilder.getCached(url, calculatorPricesType)?.data as? MutableMap<String, Map<String, Double>>)?.let { data ->
                calculatorPrices = data
                return data
            }

            Log.w("Network call ", "Calculator prices")

            val currencies = mainRepository.getCoinsWithAllCurrencies(ids).unwrapOrNull()

            currencies?.let {
                calculatorPrices = it
            }

            return currencies
        }
    }
}
