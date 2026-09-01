package com.cryptium.coincurrently.core.networking.ResponseModels
import java.util.Locale

data class CurrencySelectionModel(
    var currency: String = "United States dollar",
    var code: String = "USD",
    var symbol: String = "$",
    var language: String = "en",
    var country: String = "US",
    var suffix: Boolean = false,
    var voiceOverName: String? = null,
    var readAccessibilityAsSymbol: Boolean = false,
) {
    val voiceOverCurrencyName: String
        get() {
            voiceOverName?.trim()?.takeIf { it.isNotEmpty() }?.let { return it }
            voiceOverNamesByCode[code]?.let { return it }
            val name = currency.trim()
            if (name.isEmpty()) return ""
            return name
        }

    val formattingLocale: Locale
        get() {
            if (language.isBlank() || country.isBlank()) {
                return Locale("en", "US", "POSIX")
            }
            val parts = language.split("_")
            val builder = Locale.Builder().setRegion(country)
            when (parts.size) {
                1 -> builder.setLanguage(parts[0])
                2 -> builder.setLanguage(parts[0]).setScript(parts[1])
                else -> builder.setLanguage(language)
            }
            return builder.setUnicodeLocaleKeyword("nu", "latn").build()
        }

    companion object {
        private val voiceOverNamesByCode = mapOf(
            "AED" to "dirhams",
            "AUD" to "dollars",
            "BCH" to "bitcoin cash",
            "BTC" to "bitcoin",
            "BITS" to "bits",
            "BNB" to "binance coins",
            "BRL" to "reais",
            "CAD" to "dollars",
            "CHF" to "francs",
            "CLP" to "pesos",
            "CNY" to "yuan",
            "CZK" to "korunas",
            "DKK" to "crowns",
            "DOT" to "polkadot",
            "ETH" to "ether",
            "EOS" to "EOS",
            "EUR" to "euros",
            "GBP" to "pounds",
            "HKD" to "dollars",
            "HUF" to "forints",
            "IDR" to "rupiah",
            "ILS" to "shekels",
            "INR" to "rupees",
            "JPY" to "yen",
            "KRW" to "won",
            "LKR" to "rupees",
            "LINK" to "chainlink",
            "LTC" to "litecoin",
            "MXN" to "pesos",
            "MYR" to "ringgit",
            "NOK" to "crowns",
            "NZD" to "dollars",
            "PHP" to "pesos",
            "PKR" to "rupees",
            "PLN" to "zloty",
            "RUB" to "rubles",
            "SEK" to "crowns",
            "SATS" to "satoshis",
            "SGD" to "dollars",
            "THB" to "baht",
            "TRY" to "lira",
            "UAH" to "hryvnas",
            "USD" to "dollars",
            "VND" to "dong",
            "XRP" to "ripple",
            "ZAR" to "rand",
            "XLM" to "stellar",
            "YFI" to "yearn finance",
        )

        fun empty(): CurrencySelectionModel {
            return CurrencySelectionModel(
                currency = "",
                code = "",
                symbol = "",
                language = "",
                country = "",
                suffix = false
            )
        }
    }
}

