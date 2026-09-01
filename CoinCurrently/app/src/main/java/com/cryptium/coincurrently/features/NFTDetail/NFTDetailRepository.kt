package com.cryptium.coincurrently.features.NFTDetail
import android.util.Log
import com.cryptium.coincurrently.core.extensions.unwrapOrNull
import com.google.gson.reflect.TypeToken
import kotlinx.serialization.json.Json
import com.cryptium.coincurrently.core.networking.ApiHelper
import com.cryptium.coincurrently.core.networking.CacheUrls
import com.cryptium.coincurrently.core.networking.MainRepository
import com.cryptium.coincurrently.core.networking.ResponseModels.NFTDetail
import com.cryptium.coincurrently.core.networking.ResponseModels.NFTPriceVolumeModel
import com.cryptium.coincurrently.core.networking.ResponseModels.NFTTicker
import com.cryptium.coincurrently.core.networking.ResponseModels.NFTTickerResponse
import com.cryptium.coincurrently.core.networking.RetrofitBuilder

class NFTDetailRepository {
    companion object {
        private val mainRepository by lazy { MainRepository(ApiHelper(RetrofitBuilder.apiService)) }

        private val nftDetailType = object : TypeToken<NFTDetail>() {}.type
        private val nftTickerResponseType = object : TypeToken<NFTTickerResponse>() {}.type

        var nftDetail: MutableMap<String, NFTDetail>? = null
        var nftPrices: MutableMap<String, NFTPriceVolumeModel>? = null
        var nftTickers: MutableMap<String, List<NFTTicker>>? = null

        /** Hydrates [nftDetail] and [nftPrices] from Retrofit persisted cache before first network read. */
        fun loadCachedDataForNft(id: String, days: String, includeExpired: Boolean = true) {
            if (id.isEmpty()) return

            @Suppress("UNCHECKED_CAST")
            (RetrofitBuilder.getCached(CacheUrls.nftDetail(id), nftDetailType, includeExpired)?.data as? NFTDetail)?.let { data ->
                nftDetail = (nftDetail ?: mutableMapOf()).also { m -> m[id] = data }
            }

            RetrofitBuilder.getCached<String>(CacheUrls.nftPriceVolume(id, days), includeExpired)?.let { cached ->
                runCatching {
                    val model = Json.decodeFromString<NFTPriceVolumeModel>(cached.data)
                    nftPrices = (nftPrices ?: mutableMapOf()).also { m -> m["$id-$days"] = model }
                }
            }
        }

        suspend fun getNftDetailInfo(
            id: String?
        ): NFTDetail? {
            val id = id ?: return null
            val url = CacheUrls.nftDetail(id)

            @Suppress("UNCHECKED_CAST")
            (RetrofitBuilder.getCached(url, nftDetailType)?.data as? NFTDetail)?.let { data ->
                nftDetail = (nftDetail ?: mutableMapOf()).also { it[id] = data }
                return data
            }

            Log.w("Network call ", "NFT Detail")

            val nftDetailResult = mainRepository.getNftDetail(id).unwrapOrNull()

            nftDetailResult?.let {
                nftDetail = (nftDetail ?: mutableMapOf()).also { m -> m[id] = it }
            }

            return nftDetailResult
        }

        suspend fun getNftPriceVolume(id: String?, days: String): NFTPriceVolumeModel? {
            val id = id ?: return null
            val url = CacheUrls.nftPriceVolume(id, days)

            RetrofitBuilder.getCached<String>(url)?.let { cached ->
                val model = Json.decodeFromString<NFTPriceVolumeModel>(cached.data)
                nftPrices = (nftPrices ?: mutableMapOf()).also { it["$id-$days"] = model }
                return model
            }

            Log.w("Network call ", "NFT Detail")

            val nftPricesResult = mainRepository.getNftPriceVolume(id, days).unwrapOrNull() ?: return null
            val model: NFTPriceVolumeModel = Json.decodeFromString(nftPricesResult)

            model.let {
                nftPrices = (nftPrices ?: mutableMapOf()).also { m -> m["$id-$days"] = it }
            }

            return model
        }

        suspend fun getNFTTickers(id: String?): List<NFTTicker>? {
            val id = id ?: return null
            val url = CacheUrls.nftTickers(id)

            @Suppress("UNCHECKED_CAST")
            (RetrofitBuilder.getCached(url, nftTickerResponseType)?.data as? NFTTickerResponse)?.tickers?.let { tickers ->
                nftTickers = (nftTickers ?: mutableMapOf()).also { it[id] = tickers }
                return tickers
            }

            return mainRepository.getNFTTickers(id).unwrapOrNull()?.tickers?.let { tickers ->
                nftTickers = (nftTickers ?: mutableMapOf()).also { m -> m[id] = tickers }
                tickers
            }
        }
    }
}
