package networking.ResponseModels

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class TrendingItems(
    val coins: List<TrendingCoin>? = null,
    val nfts: List<TrendingNFT>? = null,
    val categories: List<TrendingCategory>? = null
)

@Serializable
data class TrendingCoin(
    val item: CoinItem? = null
)

@Serializable
data class CoinItem(
    val id: String? = null,
    val coin_id: Int? = null,
    val name: String? = null,
    val symbol: String? = null,
    val market_cap_rank: Int? = null,
    val thumb: String? = null,
    val small: String? = null,
    val large: String? = null,
    val slug: String? = null,
    val price_btc: Double? = null,
    val score: Int? = null,
    @SerializedName("data")
    val data: CoinData? = null
)

@Serializable
data class CoinData(
    val price: Double? = null,
    val price_btc: String? = null,
    val price_change_percentage_24h: Map<String, Double>? = null,
    val market_cap: String? = null,
    val market_cap_btc: String? = null,
    val total_volume: String? = null,
    val total_volume_btc: String? = null,
    val sparkline: String? = null,
    val content: CoinContent? = null
)

@Serializable
data class CoinContent(
    val title: String? = null,
    val description: String? = null
)

@Serializable
data class TrendingNFT(
    val id: String? = null,
    val name: String? = null,
    val symbol: String? = null,
    val thumb: String? = null,
    val nft_contract_id: Int? = null,
    val native_currency_symbol: String? = null,
    val floor_price_in_native_currency: Double? = null,
    val floor_price_24h_percentage_change: Double? = null,
    @SerializedName("data")
    val data: NFTData? = null
)

@Serializable
data class NFTData(
    val floor_price: String? = null,
    val floor_price_in_usd_24h_percentage_change: String? = null,
    val h24_volume: String? = null,
    val h24_average_sale_price: String? = null,
    val sparkline: String? = null,
    val content: NFTContent? = null
)

@Serializable
data class NFTContent(
    val title: String? = null,
    val description: String? = null
)

@Serializable
data class TrendingCategory(
    val id: Int? = null,
    val name: String? = null,
    val market_cap_1h_change: Double? = null,
    val slug: String? = null,
    val coins_count: String? = null,
    @SerializedName("data")
    val data: CategoryData? = null
)

@Serializable
data class CategoryData(
    val market_cap: Double? = null,
    val market_cap_btc: Double? = null,
    val total_volume: Double? = null,
    val total_volume_btc: Double? = null,
    val market_cap_change_percentage_24h: Map<String, Double>? = null,
    val sparkline: String? = null
)

// Extension functions for toToplist conversions
fun CoinItem.toToplist(): Toplist {
    return Toplist(
        id = id,
        symbol = symbol,
        name = name,
        image = large,
        market_cap_rank = market_cap_rank
    )
}

fun TrendingNFT.toToplist(): Toplist {
    return Toplist(
        id = id,
        symbol = symbol,
        name = name,
        image = thumb
    )
}

fun TrendingCategory.toToplist(): Toplist {
    return Toplist(
        id = id?.toString(),
        name = name
    )
}
