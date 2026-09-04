package com.cryptium.coincurrently.features.Calculator
import android.util.Log
import com.cryptium.coincurrently.core.extensions.unwrapOrNull
import com.google.gson.reflect.TypeToken
import com.cryptium.coincurrently.core.persistence.allCurrencies
import com.cryptium.coincurrently.core.networking.CacheUrls
import com.cryptium.coincurrently.core.networking.MainRepository
import com.cryptium.coincurrently.core.networking.RetrofitBuilder

object CalculatorRepository {
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

        val currencies = MainRepository.getCoinsWithAllCurrencies(ids).unwrapOrNull()

        currencies?.let {
            calculatorPrices = it
        }

        return currencies
    }
}
