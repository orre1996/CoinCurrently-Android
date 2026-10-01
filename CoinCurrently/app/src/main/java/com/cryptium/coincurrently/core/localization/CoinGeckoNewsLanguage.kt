package com.cryptium.coincurrently.core.localization

import java.util.Locale

/**
 * CoinGecko news `language` query value for the in-app language.
 * Unsupported languages fall back to English.
 */
object CoinGeckoNewsLanguage {
    private val supported = setOf(
        "en", "ru", "de", "pl", "es", "vi", "fr", "pt-br", "ar", "bg", "cs", "da",
        "el", "fi", "he", "hi", "hr", "hu", "id", "it", "ja", "ko", "lt", "nl", "no",
        "ro", "sk", "sl", "sv", "th", "tr", "uk", "zh", "zh-tw",
    )

    val code: String
        get() {
            val preference = AppLanguageState.preference
            val raw = when (preference) {
                AppLanguagePreference.System -> systemCode()
                is AppLanguagePreference.Fixed -> when (preference.language) {
                    AppLanguage.Portuguese -> "pt-br"
                    else -> preference.language.code
                }
            }
            return if (supported.contains(raw)) raw else "en"
        }

    private fun systemCode(): String {
        val locales = Locale.getDefault().let { listOf(it) } +
            android.os.LocaleList.getDefault().let { list ->
                (0 until list.size()).map { list[it] }
            }
        for (locale in locales) {
            val normalized = locale.toLanguageTag().replace('_', '-').lowercase()
            if (normalized.startsWith("zh")) {
                return if (
                    normalized.contains("hant") ||
                    normalized.contains("-tw") ||
                    normalized.contains("-hk") ||
                    normalized.contains("-mo")
                ) {
                    "zh-tw"
                } else {
                    "zh"
                }
            }
            if (normalized.startsWith("pt")) return "pt-br"
            if (normalized.startsWith("no") || normalized.startsWith("nb") || normalized.startsWith("nn")) {
                return "no"
            }
            val code = normalized.substringBefore('-')
            if (supported.contains(code)) return code
        }
        return "en"
    }
}
