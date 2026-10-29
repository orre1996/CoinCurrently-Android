package com.cryptium.coincurrently.core.networking
import com.cryptium.coincurrently.sharedui.models.SortOption
import com.cryptium.coincurrently.core.analytics.FirebaseManager
import com.cryptium.coincurrently.core.networking.ResponseModels.cryptoApiHost
import com.cryptium.coincurrently.core.networking.ResponseModels.serverBaseUrl

/**
 * Builds cache URLs that match Retrofit request URLs.
 * ResultCall uses request.url.toString() as cache key; path param resolves to absolute URL.
 */
object CacheUrls {
    private val baseUrl: String
        get() = cryptoApiHost

    fun gasFees(includeHistory: Boolean) =
        "$serverBaseUrl/gas?includeHistory=$includeHistory"

    fun smallToplist(currencyCode: String) =
        "$baseUrl/api/v3/coins/markets?sparkline=true&price_change_percentage=1h%2C24h%2C7d%2C30d&vs_currency=$currencyCode&order=market_cap_desc&per_page=25"

    fun toplist(currencyCode: String, sortOption: SortOption, itemsPerPage: Int, platformId: String?) =
        if (platformId != null && platformId != "all") {
            "$baseUrl/api/v3/coins/markets?sparkline=true&price_change_percentage=1h%2C24h%2C7d%2C30d&vs_currency=$currencyCode&order=${sortOption.toplistApiValue}&per_page=$itemsPerPage&category=$platformId"
        } else {
            "$baseUrl/api/v3/coins/markets?sparkline=true&price_change_percentage=1h%2C24h%2C7d%2C30d&vs_currency=$currencyCode&order=${sortOption.toplistApiValue}&per_page=$itemsPerPage"
        }

    fun winnersLosers(currencyCode: String, duration: String) =
        "$baseUrl/api/v3/coins/top_gainers_losers?vs_currency=$currencyCode&duration=$duration&top_coins=500"

    fun exchanges(page: Int = 1) =
        "$baseUrl/api/v3/exchanges?per_page=100&page=$page"

    fun categories(sortOption: SortOption) =
        "$baseUrl/api/v3/coins/categories?order=${sortOption.categoriesApiValue}"

    fun derivatives(sortOption: SortOption) =
        "$baseUrl/api/v3/derivatives/exchanges?per_page=250&order=${sortOption.derivativesApiValue}"

    fun publicTreasury(
        entity: TreasuryEntity,
        coinId: String,
        page: Int = 1,
        perPage: Int = 250,
    ) = "$baseUrl/api/v3/${entity.rawValue}/public_treasury/$coinId?page=$page&per_page=$perPage"

    fun treasuryDetail(entityId: String) =
        "$baseUrl/api/v3/public_treasury/$entityId"

    fun treasuryHoldingChart(entityId: String, coinId: String, days: String) =
        "$baseUrl/api/v3/public_treasury/$entityId/$coinId/holding_chart?days=$days"

    fun treasuryTransactionHistory(
        entityId: String,
        coinIds: String? = null,
        page: Int = 1,
        perPage: Int = 100,
    ): String {
        val coinQuery = coinIds?.let { "&coin_ids=$it" } ?: ""
        return "$baseUrl/api/v3/public_treasury/$entityId/transaction_history?page=$page&per_page=$perPage$coinQuery"
    }

    fun treasuryEntitiesList(
        entity: TreasuryEntity,
        page: Int = 1,
        perPage: Int = 250,
    ) = "$baseUrl/api/v3/entities/list?entity_type=${entity.apiEntityType}&page=$page&per_page=$perPage"

    fun trending() =
        "$baseUrl/api/v3/search/trending"

    fun newCoins() =
        "$baseUrl/api/v3/coins/list/new"

    fun search(query: String) =
        "$baseUrl/api/v3/search?query=$query"

    /** Must match actual Retrofit request URL (ResultCall uses request.url.toString() as cache key). */
    fun nftToplist(order: SortOption, platformId: String?) =
        if (platformId != null && platformId != "all") {
            "$baseUrl/api/v3/nfts/markets?per_page=250&order=${order.nftApiValue}&asset_platform_id=$platformId"
        } else {
            "$baseUrl/api/v3/nfts/markets?per_page=250&order=${order.nftApiValue}"
        }

    /** Must match actual Retrofit request URL (ResultCall uses request.url.toString() as cache key). */
    fun nftPlatforms() =
        "$baseUrl/api/v3/asset_platforms?filter=nft"

    fun multipleCoins(ids: String, vsCurrencies: String) =
        "$baseUrl/api/v3/simple/price?ids=$ids&vs_currencies=$vsCurrencies"

    fun calculatorPrices(ids: String, vsCurrencies: String) =
        multipleCoins(ids, vsCurrencies)

    fun getMultipleCoins(ids: String, currencyCode: String) =
        "$baseUrl/api/v3/coins/markets?order=market_cap_desc&per_page=100&page=1&sparkline=true&price_change_percentage=1h%2C24h%2C7d%2C30d&ids=$ids&vs_currency=$currencyCode"

    /** Coin detail, OHLC, and market charts use the same market host as every other CoinGecko route. */
    @Suppress("UNUSED_PARAMETER")
    private fun cryptoBaseUrl(freeUrl: Boolean, days: String? = null): String =
        cryptoApiHost

    fun cryptoDetail(id: String, freeUrl: Boolean) =
        "${cryptoBaseUrl(freeUrl)}/api/v3/coins/$id?date=&sparkline=true"

    fun priceVolume(id: String, currencyCode: String, days: String, interval: String?, freeUrl: Boolean): String {
        val base = "${cryptoBaseUrl(freeUrl, days)}/api/v3/coins/$id/market_chart?vs_currency=$currencyCode&days=$days"
        return if (interval != null) "$base&interval=$interval" else base
    }

    fun candles(id: String, currencyCode: String, days: String, freeUrl: Boolean) =
        "${cryptoBaseUrl(freeUrl, days)}/api/v3/coins/$id/ohlc?vs_currency=$currencyCode&days=$days"

    fun exchangeDetail(id: String) =
        "$baseUrl/api/v3/exchanges/$id"

    fun exchangeVolume(id: String, days: String) =
        "$baseUrl/api/v3/exchanges/$id/volume_chart?days=$days"

    fun derivativeDetail(id: String) =
        "$baseUrl/api/v3/derivatives/exchanges/$id?include_tickers=unexpired"

    fun nftDetail(id: String) =
        "$baseUrl/api/v3/nfts/$id"

    fun nftPriceVolume(id: String, days: String) =
        "$baseUrl/api/v3/nfts/$id/market_chart?days=$days"

    fun nftTickers(id: String) =
        "$baseUrl/api/v3/nfts/$id/tickers"

    fun coinsList() =
        "$baseUrl/api/v3/coins/list"

    fun globalMarket() =
        "$baseUrl/api/v3/global"

    fun globalChart(days: String) =
        "$baseUrl/api/v3/global/market_cap_chart?days=$days"

    fun getAlerts() =
        "$serverBaseUrl/get-alerts"

    fun getUser() =
        "$serverBaseUrl/user"
}
