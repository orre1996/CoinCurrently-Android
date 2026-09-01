package com.cryptium.coincurrently.core.networking.ResponseModels
import kotlinx.serialization.Serializable

@Serializable
data class ExchangeList(
    var id: String?,
    var name: String?,
    var year_established: Int?,
    var country: String?,
    var url: String?,
    var image: String?,
    var has_trading_incentive: Boolean?,
    var trust_score: Int?,
    var trust_score_rank: Int?,
    var trade_volume_24h_btc: Double?,
    var trade_volume_24h_btc_normalized: Double?
)

val ExchangeList.toExchange: ExchangeResponse
    get() = ExchangeResponse(
        id = id,
        name = name,
        year_established = year_established,
        country = country,
        url = url,
        image = image,
        has_trading_incentive = has_trading_incentive,
        trust_score = trust_score,
        trust_score_rank = trust_score_rank,
        trade_volume_24h_btc = trade_volume_24h_btc,
        trade_volume_24h_btc_normalized = trade_volume_24h_btc_normalized
    )

@Serializable
data class ExchangeResponse(
    var id: String? = null,
    var name: String? = null,
    var year_established: Int? = null,
    var country: String? = null,
    var description: String? = null,
    var url: String? = null,
    var image: String? = null,
    var facebook_url: String? = null,
    var reddit_url: String? = null,
    var telegram_url: String? = null,
    var slack_url: String? = null,
    var other_url_1: String? = null,
    var other_url_2: String? = null,
    var twitter_handle: String? = null,
    var has_trading_incentive: Boolean? = null,
    var centralized: Boolean? = null,
    var public_notice: String? = null,
    var alert_notice: String? = null,
    var trust_score: Int? = null,
    var trust_score_rank: Int? = null,
    var trade_volume_24h_btc: Double? = null,
    var trade_volume_24h_btc_normalized: Double? = null,
    var coins: Int? = null,
    var pairs: Int? = null,
    var tickers: List<ExchangeTicker>? = null,
    var status_updates: List<Status>? = null
)

@Serializable
data class ExchangeTicker(
    var base: String? = null,
    var target: String? = null,
    var market: ExchangeTickerMarket? = null,
    var last: Double? = null,
    var volume: Double? = null,
    var cost_to_move_up_usd: Double? = null,
    var cost_to_move_down_usd: Double? = null,
    var converted_last: ExchangeTickerConverted? = null,
    var converted_volume: ExchangeTickerConverted? = null,
    var trust_score: String? = null,
    var bid_ask_spread_percentage: Double? = null,
    var timestamp: String? = null,
    var last_traded_at: String? = null,
    var last_fetch_at: String? = null,
    var is_anomaly: Boolean? = null,
    var is_stale: Boolean? = null,
    var trade_url: String? = null,
    var token_info_url: String? = null,
    var coin_id: String? = null,
    var target_coin_id: String? = null
)

@Serializable
data class ExchangeTickerMarket(
    var name: String? = null,
    var identifier: String? = null,
    var has_trading_incentive: Boolean? = null,
    var logo: String? = null
)

@Serializable
data class ExchangeTickerConverted(
    var btc: Double? = null,
    var eth: Double? = null,
    var usd: Double? = null
)

fun ExchangeResponse.copyWithId(withId: String?): ExchangeResponse {
    return copy(id = withId)
}

@Serializable
data class Status(
    var description: String?,
    var category: String?,
    var created_at: String?
)