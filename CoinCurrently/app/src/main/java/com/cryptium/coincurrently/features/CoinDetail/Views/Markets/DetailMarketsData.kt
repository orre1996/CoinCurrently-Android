package com.cryptium.coincurrently.features.CoinDetail.Views.Markets
/**
 * Configuration for the generic CryptoDetailMarketsView.
 * Used by CryptoDetailScreen, NFTDetailScreen, and ExchangeDetailScreen.
 */
data class DetailMarketsConfig(
    val headers: List<String>,
    val imageSizeDp: Int,
    val useFaviconFormat: Boolean
) {
    companion object {
        val Crypto = DetailMarketsConfig(
            headers = listOf("Exchange", "Price", "24h Volume"),
            imageSizeDp = 16,
            useFaviconFormat = true
        )
        val NFT = DetailMarketsConfig(
            headers = listOf("Market", "Price"),
            imageSizeDp = 24,
            useFaviconFormat = false
        )
        val Exchange = DetailMarketsConfig(
            headers = listOf("Pair", "Price", "24h Volume"),
            imageSizeDp = 24,
            useFaviconFormat = false
        )
    }
}

/**
 * A single market row item for the generic markets view.
 */
data class DetailMarketsItem(
    val imageUrl: String?,
    val name: String?,
    val price: String?,
    val volume: String?,
    val tradeUrl: String?,
    val priceAccessible: String? = null,
    val volumeAccessible: String? = null,
)
