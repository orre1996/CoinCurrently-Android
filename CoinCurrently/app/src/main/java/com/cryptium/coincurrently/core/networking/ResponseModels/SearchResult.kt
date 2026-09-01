package com.cryptium.coincurrently.core.networking.ResponseModels
import com.cryptium.coincurrently.app.SearchCase
import kotlinx.serialization.Serializable

@Serializable
data class SearchModel(
    var coins: List<SearchResult>? = null,
    var exchanges: List<SearchResult>? = null,
    var categories: List<SearchResult>? = null,
    var nfts: List<SearchResult>? = null
) {
    val isAllEmpty: Boolean
        get() = (coins?.isEmpty() ?: true) && (exchanges?.isEmpty() ?: true) && (nfts?.isEmpty() ?: true)
    
    fun searchCaseCoins(searchCase: SearchCase): List<SearchResult> {
        return when (searchCase) {
            SearchCase.OnlyCoins, SearchCase.Coins -> coins ?: emptyList()
            SearchCase.NFTs -> nfts ?: emptyList()
            SearchCase.Categories -> categories ?: emptyList()
            SearchCase.Exchanges -> exchanges ?: emptyList()
        }
    }
}

@Serializable
data class SearchResult(
    var type: String? = null,
    var id: String? = null,
    var name: String? = null,
    var symbol: String? = null,
    var market_cap_rank: Int? = null,
    var thumb: String? = null,
    var large: String? = null
) {
    val toToplistCoin: Toplist
        get() = Toplist(
            id = id,
            symbol = symbol,
            name = name,
            image = thumb,
            market_cap_rank = market_cap_rank
        )
    
    val toCryptoDetail: CryptoDetailInfoModel
        get() = CryptoDetailInfoModel(
            id = id,
            symbol = symbol,
            name = name,
            image = Logo(
                thumb = thumb,
                small = thumb,
                large = large
            ),
            market_cap_rank = market_cap_rank
        )
    
    val toNft: NFTToplist
        get() = NFTToplist(
            id = id,
            name = name,
            image = Logo(
                thumb = thumb,
                small = thumb,
                large = large
            ),
            contract_address = null,
            asset_platform_id = null,
            description = null,
            native_currency = null,
            floor_price = null,
            market_cap = null,
            volume_24h = null,
            floor_price_in_usd_24h_percentage_change = null,
            number_of_unique_addresses = null,
            number_of_unique_addresses_24h_percentage_change = null,
            total_supply = null
        )
    
    val toExchange: ExchangeList
        get() = ExchangeList(
            id = id,
            name = name,
            image = thumb,
            year_established = null,
            country = null,
            url = null,
            has_trading_incentive = null,
            trust_score = null,
            trust_score_rank = null,
            trade_volume_24h_btc = null,
            trade_volume_24h_btc_normalized = null
        )
}

@Serializable
data class CategorySearchResult(
    var id: Int? = null,
    var name: String? = null
)

// Typealias for backward compatibility
typealias SearchItem = SearchResult

// Backward compatibility - SearchCategoryEnum is no longer used but kept for compatibility
enum class SearchCategoryEnum {
    COIN,
    EXCHANGE,
    NFT
}

// Deprecated extension function for backward compatibility
@Deprecated("Category is no longer stored in SearchResult. Use the list type instead.")
fun SearchResult.withCategory(category: SearchCategoryEnum): SearchResult {
    return this
}
