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

    fun string(key: String): String {
        val code = AppLanguageState.preference.resolve().catalogCode
        val translations = catalog[key] ?: return key
        return translations[code] ?: translations["en"] ?: key
    }

    fun format(key: String, vararg args: Any?): String {
        val pattern = toJavaFormat(string(key))
        return try {
            String.format(Locale.US, pattern, *args)
        } catch (_: Exception) {
            key
        }
    }

    private fun toJavaFormat(pattern: String): String {
        return pattern.replace(Regex("%(\\d+\\$)?@"), "%$1s")
    }
}
