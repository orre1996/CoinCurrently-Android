package com.cryptium.coincurrently.core.localization

import android.content.Context
import org.json.JSONObject
import java.util.Locale

/**
 * Looks up copy from the iOS string catalog using the in-app language.
 * English source strings are the keys. Missing translations stay in English.
 */
object Localization {
    private var catalog: Map<String, Map<String, String>> = emptyMap()

    fun load(context: Context) {
        if (catalog.isNotEmpty()) return
        val json = context.assets.open("localization.json").bufferedReader().use { it.readText() }
        val root = JSONObject(json)
        val parsed = HashMap<String, Map<String, String>>(root.length())
        val keys = root.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            val translations = root.getJSONObject(key)
            val languageMap = HashMap<String, String>(translations.length())
            val languages = translations.keys()
            while (languages.hasNext()) {
                val language = languages.next()
                languageMap[language] = translations.getString(language)
            }
            parsed[key] = languageMap
        }
        catalog = parsed
    }

    fun string(key: String): String = lookup(key, AppLanguageState.preference.resolve())

    /** Widget copy follows the device language, not the in-app picker. */
    fun systemString(key: String): String = lookup(key, AppLanguage.matching())

    fun format(key: String, vararg args: Any?): String = applyFormat(string(key), args)

    fun systemFormat(key: String, vararg args: Any?): String = applyFormat(systemString(key), args)

    private fun lookup(key: String, language: AppLanguage): String {
        val translations = catalog[key] ?: return key
        return translations[language.catalogCode] ?: translations["en"] ?: key
    }

    private fun applyFormat(pattern: String, args: Array<out Any?>): String {
        val javaPattern = toJavaFormat(pattern)
        return try {
            String.format(Locale.US, javaPattern, *args)
        } catch (_: Exception) {
            pattern
        }
    }

    private fun toJavaFormat(pattern: String): String {
        return pattern.replace(Regex("%(\\d+\\$)?@"), "%$1s")
    }
}
