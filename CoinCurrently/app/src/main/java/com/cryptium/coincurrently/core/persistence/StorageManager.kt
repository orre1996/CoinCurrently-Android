package com.cryptium.coincurrently.core.persistence
import android.content.Context
import com.bumptech.glide.disklrucache.DiskLruCache
import com.google.gson.reflect.TypeToken
import com.google.gson.Gson
import java.io.File
import java.lang.reflect.Type
import java.security.MessageDigest

enum class StorageKeys(private val rawOverride: String? = null) {
    FAVORITE,

    SEARCH_HISTORY,

    GLOBAL_MARKET_DATA_1,

    PORTFOLIO,
    PORTFOLIO_PRICES,
    WALLETS_REFRESH_DATE,
    NEW_PORTFOLIO,

    DID_CLOSE_PROMOTION,
    DID_CLOSE_WELCOME,
    HOME_SHOW_FREE_TRIAL,
    HOME_NEXT_FREE_TRIAL_DATE,
    HOME_WELCOME_OFFER_DATE,
    HAS_CLOSED_APP,

    IS_CANDLE_CHART_ENABLED,
    SELECTED_CHART_TYPE,

    /** Persisted chart period; clamp with TimeCharts.getStartTimeFrameBetween. */
    CHART_SELECTED_TIMEFRAME,

    DERIVATIVE_DETAIL,

    APP_COLOR_SCHEME,

    HAS_ASKED_FOR_PUSH_NOTIFICATIONS,
    LAST_PUSH_NOTIFICATION_TOKEN,
    SUBSCIBRES_TO_BIG_MOVERS,

    TOPLIST_SORT_OPTION,
    TOPLIST_PRICE_CHANGE_OPTION,
    NFTS_SORT_OPTION,
    NFT_PLATFORM,
    EXCHANGES_SORT_OPTION,
    CATEGORIES_SORT_OPTION,

    DERIVATIVES_SORT_OPTION,

    TREASURIES_ENTITY_FILTER,
    TREASURIES_COIN_ID,
    IS_TREASURY_CHART_FIAT,

    LAST_NEW_COINS_URL,
    LAST_TRENDING_COINS_URL,
    LAST_WINNERS_LOSERS_URLS,

    EXCHANGE_DETAIL_SORT_OPTION,

    CRYPTO_DETAIL_MARKETS_SORT_OPTION,

    NFT_DETAIL_MARKETS_SORT_OPTION,

    APP_RATING_POPUP_DATE,
    APP_ACTION_RATING_DATE,

    /** What's New sheet content version (e.g. "3.5"); last-seen copy lives in SharedPreferences. */
    CURRENT_WHATS_NEW_VERSION("3.5");

    val rawValue: String get() = rawOverride ?: name
}

class StorageManager {
    companion object {
        lateinit var cache: DiskLruCache
        private lateinit var cacheDirectory: File
        private const val CACHE_MAX_SIZE_BYTES = 1024 * 1024 * 100L // 100MB

        fun setCache(context: Context) {
            UserPreferences.init(context)
            cacheDirectory = File(context.cacheDir, "cache_directory")
            cache = openCache()
        }

        fun clearCache() {
            if (!::cacheDirectory.isInitialized) return

            try {
                if (::cache.isInitialized) {
                    try {
                        cache.delete()
                    } catch (_: Exception) {
                        wipeCacheDirectory()
                    }
                } else {
                    wipeCacheDirectory()
                }
            } catch (_: Exception) {
                wipeCacheDirectory()
            }

            cache = openCache()
            com.cryptium.coincurrently.core.networking.RetrofitBuilder.clearResponseCache()
        }

        private fun openCache(): DiskLruCache =
            DiskLruCache.open(cacheDirectory, 1, 1, CACHE_MAX_SIZE_BYTES)

        private fun wipeCacheDirectory() {
            cacheDirectory.listFiles()?.forEach { file ->
                file.deleteRecursively()
            }
        }

        fun ensureCache(context: Context) {
            if (!this::cache.isInitialized) {
                setCache(context)
            }
        }

        fun set(key: StorageKeys, data: Any?) {
            try {
                val editor = cache.edit(key.toString())
                editor?.set(0, Gson().toJson(data))
                editor?.commit()
            } catch(_: Exception) {
                return
            }
        }

        inline fun <reified T>get(key: StorageKeys): T? {
            return try {
                val snapshot = cache.get(key.name)
                val data = snapshot?.getString(0)

                if (data.isNullOrEmpty()) {
                    return null
                }

                val type = object : TypeToken<T>() {}.type
                Gson().fromJson(data, type)
            } catch (e: Exception) {
                null
            }
        }

        /**
         * Clears [key] when the first element is not [T] (e.g. LinkedTreeMap after R8 signature strip).
         * Returns true if the cache was cleared.
         */
        inline fun <reified T> clearIfCorruptTypedList(
            key: StorageKeys,
            list: List<*>?,
            tag: String = "StorageManager"
        ): Boolean {
            val first = list?.firstOrNull() ?: return false
            if (first is T) return false
            android.util.Log.e(tag, "Corrupt cache for $key element=${first.javaClass.name} expected=${T::class.java.name}; clearing")
            removeKey(key)
            return true
        }

        fun removeKey(key: StorageKeys) {
            cache.remove(key.name)
        }

        /** URL-based cache for RetrofitBuilder. Key is hashed URL (DiskLruCache requires [a-z0-9_-]). */
        fun setUrlCache(url: String, data: Any?) {
            try {
                val key = urlToCacheKey(url)
                val editor = cache.edit(key)
                editor?.set(0, Gson().toJson(data))
                editor?.commit()
            } catch (_: Exception) {}
        }

        fun removeUrlCache(url: String) {
            try {
                cache.remove(urlToCacheKey(url))
            } catch (_: Exception) {}
        }

        internal inline fun <reified T> getUrlCache(url: String): T? {
            return try {
                val key = urlToCacheKey(url)
                val snapshot = cache.get(key)
                val data = snapshot?.getString(0)
                if (data.isNullOrEmpty()) null
                else {
                    val type = object : TypeToken<T>() {}.type
                    Gson().fromJson(data, type)
                }
            } catch (e: Exception) { null }
        }

        /** Use explicit Type for proper Gson deserialization of nested generics (avoids LinkedTreeMap cast). */
        internal fun getUrlCache(url: String, type: Type): Any? {
            return try {
                val key = urlToCacheKey(url)
                val snapshot = cache.get(key)
                val data = snapshot?.getString(0)
                if (data.isNullOrEmpty()) null else Gson().fromJson(data, type)
            } catch (e: Exception) { null }
        }

        /** Bump when cache format changes (e.g. TypeToken / R8 model keep) to invalidate old entries. */
        private const val URL_CACHE_VERSION = 4

        private fun urlToCacheKey(url: String): String {
            val hash = MessageDigest.getInstance("SHA-256").digest(url.toByteArray())
            return "url_v${URL_CACHE_VERSION}_" + hash.joinToString("") { "%02x".format(it) }
        }
    }
}