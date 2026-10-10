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

// Typealias for backward compatibility
typealias SearchItem = SearchResult
