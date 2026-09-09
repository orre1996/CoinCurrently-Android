package com.cryptium.coincurrently.widgets.config

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object WidgetConfigStore {
    private const val PREFS_NAME = "widget_config_store"
    private const val KEY_SINGLE = "single_coin_configs"
    private const val KEY_DUO = "duo_coin_configs"
    private const val KEY_QUAD = "quad_coin_configs"
    private const val KEY_MULTI = "multi_coin_configs"
    private const val KEY_NEWS = "news_configs"

    private val gson = Gson()

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getSingleCoinConfig(context: Context, appWidgetId: Int): SingleCoinWidgetConfig {
        val map = loadMap<SingleCoinWidgetConfig>(context, KEY_SINGLE)
        return map[appWidgetId.toString()] ?: SingleCoinWidgetConfig(null)
    }

    fun setSingleCoinConfig(context: Context, appWidgetId: Int, config: SingleCoinWidgetConfig) {
        val map = loadMap<SingleCoinWidgetConfig>(context, KEY_SINGLE).toMutableMap()
        map[appWidgetId.toString()] = config
        saveMap(context, KEY_SINGLE, map)
    }

    fun deleteSingleCoinConfig(context: Context, appWidgetId: Int) {
        val map = loadMap<SingleCoinWidgetConfig>(context, KEY_SINGLE).toMutableMap()
        map.remove(appWidgetId.toString())
        saveMap(context, KEY_SINGLE, map)
    }

    fun getDuoCoinConfig(context: Context, appWidgetId: Int): DuoCoinWidgetConfig {
        val map = loadMap<DuoCoinWidgetConfig>(context, KEY_DUO)
        return map[appWidgetId.toString()] ?: DuoCoinWidgetConfig(null, null)
    }

    fun setDuoCoinConfig(context: Context, appWidgetId: Int, config: DuoCoinWidgetConfig) {
        val map = loadMap<DuoCoinWidgetConfig>(context, KEY_DUO).toMutableMap()
        map[appWidgetId.toString()] = config
        saveMap(context, KEY_DUO, map)
    }

    fun deleteDuoCoinConfig(context: Context, appWidgetId: Int) {
        val map = loadMap<DuoCoinWidgetConfig>(context, KEY_DUO).toMutableMap()
        map.remove(appWidgetId.toString())
        saveMap(context, KEY_DUO, map)
    }

    fun getQuadCoinConfig(context: Context, appWidgetId: Int): QuadCoinWidgetConfig {
        val map = loadMap<QuadCoinWidgetConfig>(context, KEY_QUAD)
        return map[appWidgetId.toString()] ?: QuadCoinWidgetConfig(emptyList())
    }

    fun setQuadCoinConfig(context: Context, appWidgetId: Int, config: QuadCoinWidgetConfig) {
        val map = loadMap<QuadCoinWidgetConfig>(context, KEY_QUAD).toMutableMap()
        map[appWidgetId.toString()] = config
        saveMap(context, KEY_QUAD, map)
    }

    fun deleteQuadCoinConfig(context: Context, appWidgetId: Int) {
        val map = loadMap<QuadCoinWidgetConfig>(context, KEY_QUAD).toMutableMap()
        map.remove(appWidgetId.toString())
        saveMap(context, KEY_QUAD, map)
    }

    fun getMultiCoinConfig(context: Context, appWidgetId: Int): MultiCoinWidgetConfig {
        val map = loadMap<MultiCoinWidgetConfig>(context, KEY_MULTI)
        return map[appWidgetId.toString()] ?: MultiCoinWidgetConfig(emptyList())
    }

    fun setMultiCoinConfig(context: Context, appWidgetId: Int, config: MultiCoinWidgetConfig) {
        val map = loadMap<MultiCoinWidgetConfig>(context, KEY_MULTI).toMutableMap()
        map[appWidgetId.toString()] = config
        saveMap(context, KEY_MULTI, map)
    }

    fun deleteMultiCoinConfig(context: Context, appWidgetId: Int) {
        val map = loadMap<MultiCoinWidgetConfig>(context, KEY_MULTI).toMutableMap()
        map.remove(appWidgetId.toString())
        saveMap(context, KEY_MULTI, map)
    }

    fun getNewsConfig(context: Context, appWidgetId: Int): NewsWidgetConfig {
        return getNewsConfigOrNull(context, appWidgetId) ?: NewsWidgetConfig()
    }

    fun getNewsConfigOrNull(context: Context, appWidgetId: Int): NewsWidgetConfig? {
        val map = loadMap<NewsWidgetConfig>(context, KEY_NEWS)
        return map[appWidgetId.toString()]
    }

    fun setNewsConfig(context: Context, appWidgetId: Int, config: NewsWidgetConfig) {
        val map = loadMap<NewsWidgetConfig>(context, KEY_NEWS).toMutableMap()
        map[appWidgetId.toString()] = config
        saveMap(context, KEY_NEWS, map)
    }

    fun deleteNewsConfig(context: Context, appWidgetId: Int) {
        val map = loadMap<NewsWidgetConfig>(context, KEY_NEWS).toMutableMap()
        map.remove(appWidgetId.toString())
        saveMap(context, KEY_NEWS, map)
    }

    private inline fun <reified T> loadMap(context: Context, key: String): Map<String, T> {
        val json = prefs(context).getString(key, null) ?: return emptyMap()
        return try {
            val type = object : TypeToken<Map<String, T>>() {}.type
            gson.fromJson<Map<String, T>>(json, type) ?: emptyMap()
        } catch (_: Exception) {
            emptyMap()
        }
    }

    private fun saveMap(context: Context, key: String, map: Map<String, Any?>) {
        prefs(context).edit().putString(key, gson.toJson(map)).apply()
    }
}
