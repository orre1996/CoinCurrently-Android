package com.cryptium.coincurrently.core.localization

import android.os.LocaleList
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.cryptium.coincurrently.core.persistence.UserPreferences
import java.util.Locale

/**
 * In-app language. Mirrors iOS `AppLanguage` and the Settings picker.
 * `system` follows the device language when the app has a translation for it.
 */
enum class AppLanguage(val code: String) {
    English("en"),
    Russian("ru"),
    German("de"),
    Polish("pl"),
    Spanish("es"),
    Vietnamese("vi"),
    French("fr"),
    Portuguese("pt"),
    Arabic("ar"),
    Bulgarian("bg"),
    Czech("cs"),
    Danish("da"),
    Greek("el"),
    Finnish("fi"),
    Hebrew("he"),
    Hindi("hi"),
    Croatian("hr"),
    Hungarian("hu"),
    Indonesian("id"),
    Italian("it"),
    Japanese("ja"),
    Korean("ko"),
    Lithuanian("lt"),
    Dutch("nl"),
    Norwegian("no"),
    Romanian("ro"),
    Slovak("sk"),
    Slovenian("sl"),
    Swedish("sv"),
    Thai("th"),
    Turkish("tr"),
    Ukrainian("uk"),
    ChineseSimplified("zh"),
    ChineseTraditional("zh-tw");

    /** Key used in `localization.json` (iOS string catalog locales). */
    val catalogCode: String
        get() = when (this) {
            ChineseSimplified -> "zh-Hans"
            ChineseTraditional -> "zh-Hant"
            Norwegian -> "nb"
            else -> code
        }

    val locale: Locale
        get() = Locale.forLanguageTag(
            when (this) {
                ChineseSimplified -> "zh-Hans"
                ChineseTraditional -> "zh-Hant"
                Norwegian -> "nb"
                else -> code
            },
        )

    val isRtl: Boolean
        get() = this == Arabic || this == Hebrew

    val nativeName: String
        get() = when (this) {
            English -> "English"
            Russian -> "Русский"
            German -> "Deutsch"
            Polish -> "Polski"
            Spanish -> "Español"
            Vietnamese -> "Tiếng Việt"
            French -> "Français"
            Portuguese -> "Português"
            Arabic -> "العربية"
            Bulgarian -> "Български"
            Czech -> "Čeština"
            Danish -> "Dansk"
            Greek -> "Ελληνικά"
            Finnish -> "Suomi"
            Hebrew -> "עברית"
            Hindi -> "हिन्दी"
            Croatian -> "Hrvatski"
            Hungarian -> "Magyar"
            Indonesian -> "Bahasa Indonesia"
            Italian -> "Italiano"
            Japanese -> "日本語"
            Korean -> "한국어"
            Lithuanian -> "Lietuvių"
            Dutch -> "Nederlands"
            Norwegian -> "Norsk"
            Romanian -> "Română"
            Slovak -> "Slovenčina"
            Slovenian -> "Slovenščina"
            Swedish -> "Svenska"
            Thai -> "ไทย"
            Turkish -> "Türkçe"
            Ukrainian -> "Українська"
            ChineseSimplified -> "简体中文"
            ChineseTraditional -> "繁體中文"
        }

    val englishName: String
        get() = when (this) {
            English -> "English"
            Russian -> "Russian"
            German -> "German"
            Polish -> "Polish"
            Spanish -> "Spanish"
            Vietnamese -> "Vietnamese"
            French -> "French"
            Portuguese -> "Portuguese"
            Arabic -> "Arabic"
            Bulgarian -> "Bulgarian"
            Czech -> "Czech"
            Danish -> "Danish"
            Greek -> "Greek"
            Finnish -> "Finnish"
            Hebrew -> "Hebrew"
            Hindi -> "Hindi"
            Croatian -> "Croatian"
            Hungarian -> "Hungarian"
            Indonesian -> "Indonesian"
            Italian -> "Italian"
            Japanese -> "Japanese"
            Korean -> "Korean"
            Lithuanian -> "Lithuanian"
            Dutch -> "Dutch"
            Norwegian -> "Norwegian"
            Romanian -> "Romanian"
            Slovak -> "Slovak"
            Slovenian -> "Slovenian"
            Swedish -> "Swedish"
            Thai -> "Thai"
            Turkish -> "Turkish"
            Ukrainian -> "Ukrainian"
            ChineseSimplified -> "Chinese (Simplified)"
            ChineseTraditional -> "Chinese (Traditional)"
        }

    val subtitle: String?
        get() = if (nativeName == englishName) null else englishName

    companion object {
        fun fromCode(code: String): AppLanguage? = entries.find { it.code == code }

        fun matching(preferredLanguages: List<String> = preferredLanguageTags()): AppLanguage {
            for (identifier in preferredLanguages) {
                match(identifier)?.let { return it }
            }
            return English
        }

        fun match(identifier: String): AppLanguage? {
            val normalized = identifier.replace('_', '-').lowercase(Locale.US)
            fromCode(normalized)?.let { return it }

            val locale = Locale.forLanguageTag(identifier.replace('_', '-'))
            val code = locale.language.ifBlank {
                normalized.substringBefore('-')
            }
            val script = locale.script
            val region = locale.country

            if (code == "zh" || normalized.startsWith("zh")) {
                val traditional = script.equals("Hant", ignoreCase = true) ||
                    region == "TW" || region == "HK" || region == "MO" ||
                    normalized.contains("hant") ||
                    normalized.contains("-tw") ||
                    normalized.contains("-hk") ||
                    normalized.contains("-mo")
                return if (traditional) ChineseTraditional else ChineseSimplified
            }

            if (code == "pt" || normalized.startsWith("pt")) {
                return Portuguese
            }

            if (code == "nb" || code == "no" || code == "nn" ||
                normalized.startsWith("nb") ||
                normalized.startsWith("nn") ||
                normalized.startsWith("no")
            ) {
                return Norwegian
            }

            return fromCode(code)
        }

        private fun preferredLanguageTags(): List<String> {
            val list = LocaleList.getDefault()
            return (0 until list.size()).map { list[it].toLanguageTag() }
        }
    }
}

sealed class AppLanguagePreference {
    data object System : AppLanguagePreference()
    data class Fixed(val language: AppLanguage) : AppLanguagePreference()

    val storageValue: String
        get() = when (this) {
            System -> "system"
            is Fixed -> language.code
        }

    val settingsSubtitle: String
        get() = when (this) {
            System -> "System"
            is Fixed -> language.nativeName
        }

    fun resolve(): AppLanguage = when (this) {
        System -> AppLanguage.matching()
        is Fixed -> language
    }

    companion object {
        fun from(storage: String?): AppLanguagePreference {
            if (storage.isNullOrEmpty() || storage == "system") return System
            val language = AppLanguage.fromCode(storage) ?: return System
            return Fixed(language)
        }
    }
}

/**
 * Observed by Compose. Reading [preference] during composition refreshes translated text.
 */
object AppLanguageState {
    var preference by mutableStateOf<AppLanguagePreference>(AppLanguagePreference.System)
        private set

    fun loadStored() {
        preference = AppLanguagePreference.from(UserPreferences.getSelectedLanguage())
    }

    fun select(preference: AppLanguagePreference) {
        UserPreferences.setSelectedLanguage(preference.storageValue)
        this.preference = preference
    }
}
