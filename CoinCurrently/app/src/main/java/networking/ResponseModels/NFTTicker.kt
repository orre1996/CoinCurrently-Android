package networking.ResponseModels

import kotlinx.serialization.Serializable

@Serializable
data class NFTTickerResponse(
    val tickers: List<NFTTicker>? = null
)

@Serializable
data class NFTTicker(
    val floor_price_in_native_currency: Double? = null,
    val h24_volume_in_native_currency: Double? = null,
    @kotlinx.serialization.SerialName("nativeCurrency")
    val nativeCurrency: String? = null,
    val native_currency_symbol: String? = null,
    val updated_at: String? = null,
    val nft_marketplace_id: String? = null,
    val name: String? = null,
    val image: String? = null,
    val nft_collection_url: String? = null
)
