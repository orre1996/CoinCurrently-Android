package com.cryptium.coincurrently.features.Market.Treasuries.Repositories
import android.util.Log
import com.cryptium.coincurrently.core.extensions.unwrapOrNull
import com.cryptium.coincurrently.core.networking.TreasuryEntity
import com.cryptium.coincurrently.sharedui.models.CachedData
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import com.cryptium.coincurrently.core.networking.CacheUrls
import com.cryptium.coincurrently.core.networking.MainRepository
import com.cryptium.coincurrently.core.networking.ResponseModels.PublicTreasuryHolder
import com.cryptium.coincurrently.core.networking.ResponseModels.PublicTreasuryModel
import com.cryptium.coincurrently.core.networking.ResponseModels.TreasuryEntityListItem
import com.cryptium.coincurrently.core.networking.RetrofitBuilder
import java.util.Date

object PublicTreasuryRepository {
    private val publicTreasuryType = object : TypeToken<PublicTreasuryModel>() {}.type
    private val entitiesListType = object : TypeToken<List<TreasuryEntityListItem>>() {}.type

    var cachedPublicTreasury: MutableMap<String, CachedData<PublicTreasuryModel>> = mutableMapOf()
        private set

    private var cachedEntitiesList: MutableMap<String, CachedData<List<TreasuryEntityListItem>>> = mutableMapOf()
    private val loadedCacheKeys = mutableSetOf<String>()

    init {
        try {
            listOf(TreasuryEntity.Companies, TreasuryEntity.Governments).forEach { entity ->
                listOf("bitcoin", "ethereum", "solana", "binancecoin").forEach { coinId ->
                    val key = cacheKey(entity, coinId)
                    val url = CacheUrls.publicTreasury(entity, coinId)
                    @Suppress("UNCHECKED_CAST")
                    RetrofitBuilder.getCached(url, publicTreasuryType, includeExpired = true)?.let { cached ->
                        cachedPublicTreasury[key] = cached as CachedData<PublicTreasuryModel>
                        loadedCacheKeys.add(key)
                    }
                }
            }
        } catch (_: Exception) {
            // StorageManager/RetrofitBuilder may not be ready yet; cache will populate on first fetch
        }
    }

    suspend fun ensureCacheLoaded(entity: TreasuryEntity, coinId: String) {
        val key = cacheKey(entity, coinId)
        if (loadedCacheKeys.contains(key)) return
        loadedCacheKeys.add(key)

        ensureEntitiesListLoaded(entity)

        val url = CacheUrls.publicTreasury(entity, coinId)
        @Suppress("UNCHECKED_CAST")
        RetrofitBuilder.getCached(url, publicTreasuryType, includeExpired = true)?.let { cached ->
            @Suppress("UNCHECKED_CAST")
            val typed = cached as CachedData<PublicTreasuryModel>
            val entities = cachedEntitiesList[entity.rawValue]?.data ?: emptyList()
            cachedPublicTreasury[key] = CachedData(
                applyEntityIds(typed.data, entity, entities),
                typed.date,
            )
        }
    }

    fun getCachedTreasury(entity: TreasuryEntity, coinId: String): PublicTreasuryModel? {
        return cachedPublicTreasury[cacheKey(entity, coinId)]?.data
    }

    suspend fun holderWithEntityId(
        holder: PublicTreasuryHolder,
        entity: TreasuryEntity,
    ): PublicTreasuryHolder {
        if (!holder.id.isNullOrEmpty()) return holder

        ensureEntitiesListLoaded(entity)
        val entities = cachedEntitiesList[entity.rawValue]?.data ?: emptyList()
        return mergeEntityId(holder, entities)
    }

    suspend fun entityId(holder: PublicTreasuryHolder, entity: TreasuryEntity): String? {
        val resolved = holderWithEntityId(holder, entity)
        return resolved.id?.takeIf { it.isNotEmpty() }
    }

    suspend fun getPublicTreasury(
        entity: TreasuryEntity,
        coinId: String,
        page: Int = 1,
        perPage: Int = 250,
    ): PublicTreasuryModel? {
        ensureCacheLoaded(entity, coinId)

        val url = CacheUrls.publicTreasury(entity, coinId, page, perPage)
        @Suppress("UNCHECKED_CAST")
        RetrofitBuilder.getCached(url, publicTreasuryType)?.let { cached ->
            cachedPublicTreasury[cacheKey(entity, coinId)] = cached as CachedData<PublicTreasuryModel>
            return cached.data
        }

        Log.w("Network call ", "Public treasury ${entity.rawValue}/$coinId")

        val treasury = coroutineScope {
            val treasuryDeferred = async {
                MainRepository.getPublicTreasury(
                    entity = entity,
                    coinId = coinId,
                    page = page,
                    perPage = perPage,
                ).unwrapOrNull()
            }
            val entitiesDeferred = async { getEntitiesList(entity, page, perPage) }

            var result = treasuryDeferred.await()
            val entities = entitiesDeferred.await()
            if (result != null && entities != null) {
                result = applyEntityIds(result, entity, entities)
            }
            result
        }

        treasury?.let {
            cachedPublicTreasury[cacheKey(entity, coinId)] = CachedData(it, Date())
        }

        return treasury
    }

    private suspend fun getEntitiesList(
        entity: TreasuryEntity,
        page: Int,
        perPage: Int,
    ): List<TreasuryEntityListItem>? {
        val url = CacheUrls.treasuryEntitiesList(entity, page, perPage)
        @Suppress("UNCHECKED_CAST")
        RetrofitBuilder.getCached(url, entitiesListType)?.let { cached ->
            cachedEntitiesList[entity.rawValue] = cached as CachedData<List<TreasuryEntityListItem>>
            return cached.data
        }

        val entities = MainRepository.getTreasuryEntitiesList(entity, page, perPage).unwrapOrNull()
        entities?.let {
            cachedEntitiesList[entity.rawValue] = CachedData(it, Date())
        }
        return entities
    }

    private suspend fun ensureEntitiesListLoaded(entity: TreasuryEntity) {
        if (cachedEntitiesList.containsKey(entity.rawValue)) return

        val url = CacheUrls.treasuryEntitiesList(entity)
        @Suppress("UNCHECKED_CAST")
        RetrofitBuilder.getCached(url, entitiesListType, includeExpired = true)?.let { cached ->
            cachedEntitiesList[entity.rawValue] = cached as CachedData<List<TreasuryEntityListItem>>
        }
    }

    private fun applyEntityIds(
        treasury: PublicTreasuryModel,
        entity: TreasuryEntity,
        entities: List<TreasuryEntityListItem>,
    ): PublicTreasuryModel {
        return when (entity) {
            TreasuryEntity.Companies -> treasury.copy(
                companies = treasury.companies?.map { mergeEntityId(it, entities) },
            )
            TreasuryEntity.Governments -> treasury.copy(
                governments = treasury.governments?.map { mergeEntityId(it, entities) },
            )
        }
    }

    private fun mergeEntityId(
        holder: PublicTreasuryHolder,
        entities: List<TreasuryEntityListItem>,
    ): PublicTreasuryHolder {
        val matched = entities.firstOrNull { matches(it, holder) } ?: return holder
        return holder.copy(id = matched.id)
    }

    private fun matches(entity: TreasuryEntityListItem, holder: PublicTreasuryHolder): Boolean {
        val holderSymbol = normalizedSymbol(holder.symbol)
        val entitySymbol = normalizedSymbol(entity.symbol)
        if (holderSymbol != null && entitySymbol != null && holderSymbol == entitySymbol) {
            return true
        }

        if (normalizedName(holder.name) != normalizedName(entity.name)) {
            return false
        }

        val holderCountry = holder.country?.trim()?.uppercase()
        val entityCountry = entity.country?.trim()?.uppercase()
        if (!holderCountry.isNullOrEmpty() && !entityCountry.isNullOrEmpty()) {
            return holderCountry == entityCountry
        }

        return true
    }

    private fun normalizedName(name: String?): String {
        return name?.trim()?.lowercase() ?: ""
    }

    private fun normalizedSymbol(symbol: String?): String? {
        val normalized = symbol?.trim()?.uppercase()
        return normalized?.takeIf { it.isNotEmpty() }
    }

    private fun cacheKey(entity: TreasuryEntity, coinId: String): String {
        return "${entity.rawValue}-$coinId"
    }
}
