package networking.ResponseModels

import com.cryptium.coincurrently.R
import kotlinx.serialization.Serializable

@Serializable
data class NFTDetail(
    val id: String? = null,
    val contract_address: String? = null,
    val asset_platform_id: String? = null,
    val name: String? = null,
    val symbol: String? = null,
    val image: Logo? = null,
    val banner_image: String? = null,
    val description: String? = null,
    val native_currency: String? = null,
    val native_currency_symbol: String? = null,
    val floor_price: FloorPrice? = null,
    val market_cap: FloorPrice? = null,
    val volume_24h: FloorPrice? = null,
    val market_cap_rank: Int? = null,
    val floor_price_in_usd_24h_percentage_change: Double? = null,
    val floor_price_24h_percentage_change: FloorPrice? = null,
    val market_cap_24h_percentage_change: FloorPrice? = null,
    val volume_24h_percentage_change: FloorPrice? = null,
    val number_of_unique_addresses: Double? = null,
    val number_of_unique_addresses_24h_percentage_change: Double? = null,
    val volume_in_usd_24h_percentage_change: Double? = null,
    val total_supply: Double? = null,
    val one_day_sales: Int? = null,
    val one_day_sales_24h_percentage_change: Double? = null,
    val one_day_average_sale_price: Double? = null,
    val one_day_average_sale_price_24h_percentage_change: Double? = null,
    val links: NFTLinks? = null,
    val floor_price_7d_percentage_change: FloorPrice? = null,
    val floor_price_14d_percentage_change: FloorPrice? = null,
    val floor_price_30d_percentage_change: FloorPrice? = null,
    val floor_price_60d_percentage_change: FloorPrice? = null,
    val floor_price_1y_percentage_change: FloorPrice? = null,
    val explorers: List<NFTExplorer>? = null,
    val user_favorites_count: Int? = null,
    val ath: FloorPrice? = null,
    val ath_change_percentage: FloorPrice? = null,
    val ath_date: FloorPriceDate? = null
)

@Serializable
data class NFTLinks(
    val homepage: String? = null,
    val twitter: String? = null,
    val discord: String? = null
)

val NFTLinks.officialWebsites: List<String>
    get() = listOfNotNull(homepage).filter { it.isNotEmpty() }

val NFTLinks.socialMediaUrls: List<String>
    get() = listOfNotNull(twitter, discord).filter { it.isNotEmpty() }

val NFTLinks.socialMediaIcons: List<Int>
    get() {
        val icons = mutableListOf<Int>()
        if (!twitter.isNullOrEmpty()) icons.add(R.drawable.x)
        if (!discord.isNullOrEmpty()) icons.add(R.drawable.discord)
        return icons
    }

val NFTLinks.isAllLinksEmpty: Boolean
    get() = homepage.isNullOrEmpty() && twitter.isNullOrEmpty() && discord.isNullOrEmpty()

val NFTDetail.isAllLinksEmpty: Boolean
    get() {
        val linksEmpty = links?.isAllLinksEmpty ?: true
        val descriptionEmpty = description.isNullOrEmpty()
        val explorersEmpty = explorers?.all { it.link.isNullOrEmpty() } ?: true
        return linksEmpty && descriptionEmpty && explorersEmpty
    }

@Serializable
data class NFTExplorer(
    val name: String? = null,
    val link: String? = null
)

@Serializable
data class FloorPriceDate(
    val native_currency: String? = null,
    val usd: String? = null
)
