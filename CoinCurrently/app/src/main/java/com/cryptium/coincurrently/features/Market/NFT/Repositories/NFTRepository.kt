package com.cryptium.coincurrently.features.Market.NFT.Repositories
import android.util.Log
import com.cryptium.coincurrently.core.extensions.unwrapOrNull
import com.cryptium.coincurrently.sharedui.models.SortOption
import com.cryptium.coincurrently.sharedui.models.CachedData
import com.cryptium.coincurrently.core.networking.CacheUrls
import com.cryptium.coincurrently.core.networking.MainRepository
import com.cryptium.coincurrently.core.networking.ResponseModels.NFTPlatform
import com.cryptium.coincurrently.core.networking.ResponseModels.NFTToplist
import com.cryptium.coincurrently.core.networking.RetrofitBuilder
import java.util.ArrayList
import java.util.Date

object NFTRepository {
    var cachedToplist: MutableMap<String, CachedData<ArrayList<NFTToplist>>> = mutableMapOf()
        private set
    var cachedPlatforms: List<NFTPlatform>? = null
        private set

    /** Synchronous cache lookup (for init/instant UI). */
    fun getCachedToplist(sortOption: SortOption, platformId: String?): List<NFTToplist>? {
        val cacheKey = "${sortOption.name}-${platformId ?: "all"}"
        return cachedToplist[cacheKey]?.data
    }

    init {
        try {
            SortOption.nftOptions.forEach { sortOption ->
                val cacheKey = "${sortOption.name}-all"
                RetrofitBuilder.getCachedList<NFTToplist>(CacheUrls.nftToplist(sortOption, null), includeExpired = true)?.let {
                    cachedToplist[cacheKey] = it
                }
            }
            cachedPlatforms = RetrofitBuilder.getCachedList<NFTPlatform>(CacheUrls.nftPlatforms(), includeExpired = true)?.data
        } catch (_: Exception) {
            // StorageManager/RetrofitBuilder may not be ready yet; cache will populate on first fetch
        }
    }

    suspend fun getToplist(
        platform: NFTPlatform,
        sortOption: SortOption
    ): List<NFTToplist>? {
        val platformId = if (platform.id == "all") null else platform.id
        val cacheKey = "${sortOption.name}-${platformId ?: "all"}"
        val url = CacheUrls.nftToplist(sortOption, platformId)

        RetrofitBuilder.getCachedList<NFTToplist>(url)?.let {
            if (it.data.isNotEmpty()) {
                cachedToplist[cacheKey] = it
                return it.data
            }
        }

        Log.w("Network call ", "NFT Toplist")

        return MainRepository.getNftToplist(sortOption, platformId).unwrapOrNull()?.let { data ->
            cachedToplist[cacheKey] = CachedData(ArrayList(data), Date())
            data
        }
    }

    suspend fun getNFTPlatforms(): List<NFTPlatform>? {
        RetrofitBuilder.getCachedList<NFTPlatform>(CacheUrls.nftPlatforms())?.let {
            if (it.data.isNotEmpty()) {
                cachedPlatforms = it.data
                return it.data
            }
        }

        Log.w("Network call ", "NFT Platforms")

        return MainRepository.getNftPlatforms().unwrapOrNull()?.also { cachedPlatforms = it }
    }
}
