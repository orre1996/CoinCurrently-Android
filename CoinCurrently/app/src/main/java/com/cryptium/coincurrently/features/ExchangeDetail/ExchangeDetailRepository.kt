package com.cryptium.coincurrently.features.ExchangeDetail
import android.util.Log
import com.cryptium.coincurrently.core.extensions.unwrapOrNull
import com.google.gson.reflect.TypeToken
import kotlinx.serialization.json.Json
import com.cryptium.coincurrently.core.networking.CacheUrls
import com.cryptium.coincurrently.core.networking.MainRepository
import com.cryptium.coincurrently.core.networking.ResponseModels.ExchangePriceModel
import com.cryptium.coincurrently.core.networking.ResponseModels.ExchangeResponse
import com.cryptium.coincurrently.core.networking.ResponseModels.copyWithId
import com.cryptium.coincurrently.core.networking.RetrofitBuilder

object ExchangeDetailRepository {
    private val exchangeResponseType = object : TypeToken<ExchangeResponse>() {}.type

    var exchangeDetail: MutableMap<String, ExchangeResponse>? = null
    var exchangePrices: MutableMap<String, List<ExchangePriceModel>>? = null

    /** Hydrates [exchangeDetail] and [exchangePrices] from Retrofit persisted cache before first network read. */
    fun loadCachedDataForExchange(id: String, days: String, includeExpired: Boolean = true) {
        if (id.isEmpty()) return

        @Suppress("UNCHECKED_CAST")
        (RetrofitBuilder.getCached(CacheUrls.exchangeDetail(id), exchangeResponseType, includeExpired)?.data as? ExchangeResponse)?.let { data ->
            exchangeDetail = (exchangeDetail ?: mutableMapOf()).also { m -> m[id] = data.copyWithId(id) }
        }

        RetrofitBuilder.getCached<String>(CacheUrls.exchangeVolume(id, days), includeExpired)?.let { cached ->
            runCatching {
                val model = Json.decodeFromString<List<ExchangePriceModel>>(cached.data)
                exchangePrices = (exchangePrices ?: mutableMapOf()).also { m -> m["$id-$days"] = model }
            }
        }
    }

    suspend fun getExchange(id: String?): ExchangeResponse? {
        val id = id ?: return null
        val url = CacheUrls.exchangeDetail(id)

        @Suppress("UNCHECKED_CAST")
        (RetrofitBuilder.getCached(url, exchangeResponseType)?.data as? ExchangeResponse)?.let { data ->
            exchangeDetail = (exchangeDetail ?: mutableMapOf()).also { it[id] = data.copyWithId(id) }
            return data.copyWithId(id)
        }

        Log.w("Network call ", "Exchange detail")

        val exchangeInfo = MainRepository.getExchangeDetailInfo(id).unwrapOrNull()

        exchangeInfo?.let {
            exchangeDetail = (exchangeDetail ?: mutableMapOf()).also { m -> m[id] = it.copyWithId(id) }
        }

        return exchangeInfo?.copyWithId(id)
    }

    suspend fun getExchangePrices(
        id: String?,
        days: String
    ): List<ExchangePriceModel>? {
        val id = id ?: return null
        val url = CacheUrls.exchangeVolume(id, days)

        RetrofitBuilder.getCached<String>(url)?.let { cached ->
            val model = Json.decodeFromString<List<ExchangePriceModel>>(cached.data)
            exchangePrices = (exchangePrices ?: mutableMapOf()).also { it["${id}-$days"] = model }
            return model
        }

        Log.w("Network call ", "Exchange volume")

        val exchangePricesResult = MainRepository.getExchangeVolume(id, days).unwrapOrNull() ?: return null
        val model: List<ExchangePriceModel> = Json.decodeFromString(exchangePricesResult)

        model.let {
            exchangePrices = (exchangePrices ?: mutableMapOf()).also { m -> m["${id}-$days"] = it }
        }

        return model
    }
}
