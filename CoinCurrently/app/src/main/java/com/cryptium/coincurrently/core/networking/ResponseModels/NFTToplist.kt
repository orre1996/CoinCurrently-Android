package com.cryptium.coincurrently.core.networking.ResponseModels
import kotlinx.serialization.Serializable

@Serializable
data class NFTToplist(
    val id: String? = null,
    val contract_address: String? = null,
    val asset_platform_id: String? = null,
    val name: String? = null,
    val symbol: String? = null,
    val image: Logo? = null,
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
    val number_of_unique_addresses: Int? = null,
    val number_of_unique_addresses_24h_percentage_change: Double? = null,
    val volume_in_usd_24h_percentage_change: Double? = null,
    val total_supply: Long? = null,
    val one_day_sales: Int? = null,
    val one_day_sales_24h_percentage_change: Double? = null,
    val one_day_average_sale_price: Double? = null,
    val one_day_average_sale_price_24h_percentage_change: Double? = null
)

@Serializable
data class FloorPrice(
    val native_currency: Double? = null,
    val usd: Double? = null
) {
    fun getSelectedPrice(isFiatSelected: Boolean, multiplier: Double?): Double? {
        return if (isFiatSelected) (usd ?: 1.0) * (multiplier ?: 1.0) else native_currency
    }
}

val NFTToplist.toNFTDetail: NFTDetail
    get() = NFTDetail(
    id = id,
    contract_address = contract_address,
    asset_platform_id = asset_platform_id,
    name = name,
    symbol = symbol,
    image = image,
    banner_image = null,
    description = description,
    native_currency = native_currency,
    native_currency_symbol = native_currency_symbol,
    floor_price = floor_price,
    market_cap = market_cap,
    volume_24h = volume_24h,
    market_cap_rank = market_cap_rank,
    floor_price_in_usd_24h_percentage_change = floor_price_in_usd_24h_percentage_change,
    floor_price_24h_percentage_change = floor_price_24h_percentage_change,
    market_cap_24h_percentage_change = market_cap_24h_percentage_change,
    volume_24h_percentage_change = volume_24h_percentage_change,
    number_of_unique_addresses = number_of_unique_addresses?.toDouble(),
    number_of_unique_addresses_24h_percentage_change = number_of_unique_addresses_24h_percentage_change,
    volume_in_usd_24h_percentage_change = volume_in_usd_24h_percentage_change,
    total_supply = total_supply?.toDouble(),
    one_day_sales = one_day_sales,
    one_day_sales_24h_percentage_change = one_day_sales_24h_percentage_change,
    one_day_average_sale_price = one_day_average_sale_price,
    one_day_average_sale_price_24h_percentage_change = one_day_average_sale_price_24h_percentage_change,
    links = null,
    floor_price_7d_percentage_change = null,
    floor_price_14d_percentage_change = null,
    floor_price_30d_percentage_change = null,
    floor_price_60d_percentage_change = null,
    floor_price_1y_percentage_change = null,
    explorers = null,
    user_favorites_count = null,
    ath = null,
    ath_change_percentage = null,
    ath_date = null
)
