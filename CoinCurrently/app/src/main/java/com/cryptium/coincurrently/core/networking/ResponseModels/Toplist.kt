package com.cryptium.coincurrently.core.networking.ResponseModels
import com.cryptium.coincurrently.app.NavigationScreens
import com.cryptium.coincurrently.sharedui.models.PriceChange
import com.cryptium.coincurrently.sharedui.models.SortOption
import com.cryptium.coincurrently.sharedui.components.GlobalFunctions
import com.cryptium.coincurrently.sharedui.components.PreferredTimeframe
import com.cryptium.coincurrently.features.GlobalMarket.Currency
import kotlinx.serialization.Serializable

@Serializable
data class Toplist(
    var id: String? = null,
    var symbol: String? = null,
    var name: String? = null,
    var image: String? = null,
    var current_price: Double? = null,
    var market_cap_rank: Int? = null,
    var sparkline_in_7d: Sparkline? = null,
    var price_change_percentage_1h_in_currency: Double? = null,
    var price_change_percentage_24h_in_currency: Double? = null,
    var price_change_percentage_7d_in_currency: Double? = null,
    var price_change_percentage_30d_in_currency: Double? = null,
    var total_volume: Double? = null,
    var market_cap: Double? = null,
)

@Serializable
data class Sparkline(
    var price: List<Double>? = null
)

/** Bundle-safe key for LazyColumn / reorderable; never use [Toplist] as the key object. */
fun Toplist.stableListKey(): String = id ?: symbol ?: name ?: ""

fun Toplist.getPreferredChange(): Double {
    return when(GlobalFunctions.getPreferredTimeframe()) {
        PreferredTimeframe.DAILY -> price_change_percentage_24h_in_currency ?: 0.0
        PreferredTimeframe.WEEKLY -> price_change_percentage_7d_in_currency ?: 0.0
        PreferredTimeframe.MONTHLY -> price_change_percentage_30d_in_currency ?: 0.0
    }
}

fun Toplist.getPriceChange(selectedChangeOption: PriceChange): Double {
    return when (selectedChangeOption) {
        PriceChange.OneHour -> price_change_percentage_1h_in_currency ?: 0.0
        PriceChange.OneDay -> price_change_percentage_24h_in_currency ?: 0.0
        PriceChange.SevenDays -> price_change_percentage_7d_in_currency ?: 0.0
        PriceChange.OneMonth -> price_change_percentage_30d_in_currency ?: 0.0
    }
}

fun List<Toplist>.getSortedToplist(sortOption: SortOption, priceChange: PriceChange): List<Toplist> {
    return when (sortOption) {
        SortOption.PriceChangeDesc -> sortedByDescending { it.getPriceChange(priceChange) }
        SortOption.PriceChangeAsc -> sortedBy { it.getPriceChange(priceChange) }
        else -> this
    }
}

fun Toplist.toCryptoDetail(): CryptoDetailInfoModel {
    return CryptoDetailInfoModel(
        id,
        symbol,
        name,
        Logo(large = image),
        market_cap_rank,
        MarketData(
            Currency(thePrice = current_price),
            null,
            null,
            null,
            null,
            price_change_percentage_24h_in_currency,
            price_change_percentage_7d_in_currency,
            price_change_percentage_30d_in_currency
        )
    )
}

fun Toplist.toExchange(): ExchangeResponse {
    return ExchangeResponse(
        id,
        name,
        image = image,
    )
}

/** For navigation to [NavigationScreens.NFTDetail] from search / sheet flows using [Toplist] rows. */
fun Toplist.toNftToplist(): NFTToplist = NFTToplist(
    id = id,
    name = name,
    symbol = symbol,
    image = image?.let { Logo(thumb = it, small = it, large = it) },
    contract_address = null,
    asset_platform_id = null,
    description = null,
    native_currency = null,
    native_currency_symbol = null,
    floor_price = null,
    market_cap = null,
    volume_24h = null,
    market_cap_rank = market_cap_rank,
    floor_price_in_usd_24h_percentage_change = null,
    floor_price_24h_percentage_change = null,
    market_cap_24h_percentage_change = null,
    volume_24h_percentage_change = null,
    number_of_unique_addresses = null,
    number_of_unique_addresses_24h_percentage_change = null,
    volume_in_usd_24h_percentage_change = null,
    total_supply = null,
    one_day_sales = null,
    one_day_sales_24h_percentage_change = null,
    one_day_average_sale_price = null,
    one_day_average_sale_price_24h_percentage_change = null,
)