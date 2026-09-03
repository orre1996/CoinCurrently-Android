package com.cryptium.coincurrently.core.networking.ResponseModels
import com.cryptium.coincurrently.core.networking.TreasuryEntity
import kotlinx.serialization.Serializable

@Serializable
data class PublicTreasuryModel(
    val total_holdings: Double? = null,
    val total_value_usd: Double? = null,
    val market_cap_dominance: Double? = null,
    val companies: List<PublicTreasuryHolder>? = null,
    val governments: List<PublicTreasuryHolder>? = null,
) {
    val holders: List<PublicTreasuryHolder>
        get() = companies ?: governments ?: emptyList()
}

@Serializable
data class PublicTreasuryHolder(
    val id: String? = null,
    val name: String? = null,
    val symbol: String? = null,
    val country: String? = null,
    val total_holdings: Double? = null,
    val total_entry_value_usd: Double? = null,
    val total_current_value_usd: Double? = null,
    val percentage_of_total_supply: Double? = null,
)

data class TreasuryListItem(
    val holder: PublicTreasuryHolder,
    val entity: TreasuryEntity,
    val coinId: String,
)

val PublicTreasuryHolder.entityId: String
    get() = name?.lowercase()?.replace(" ", "") ?: ""

val PublicTreasuryHolder.resolvedEntityId: String
    get() = id?.takeIf { it.isNotEmpty() } ?: entityId

@Serializable
data class TreasuryEntityListItem(
    val id: String,
    val symbol: String? = null,
    val name: String,
    val country: String? = null,
)

@Serializable
data class TreasuryDetailEntity(
    val name: String? = null,
    val id: String? = null,
    val type: String? = null,
    val symbol: String? = null,
    val country: String? = null,
    val website_url: String? = null,
    val twitter_screen_name: String? = null,
    val total_treasury_value_usd: Double? = null,
    val unrealized_pnl: Double? = null,
    val m_nav: Double? = null,
    val total_asset_value_per_share_usd: Double? = null,
    val holdings: List<TreasuryDetailHolding>? = null,
)

@Serializable
data class TreasuryDetailHolding(
    val coin_id: String? = null,
    val amount: Double? = null,
    val percentage_of_total_supply: Double? = null,
    val amount_per_share: Double? = null,
    val entity_value_usd_percentage: Double? = null,
    val current_value_usd: Double? = null,
    val total_entry_value_usd: Double? = null,
    val average_entry_value_usd: Double? = null,
    val unrealized_pnl: Double? = null,
)

@Serializable
data class TreasuryHoldingChartModel(
    val holdings: List<List<Double?>>? = null,
    val holding_value_in_usd: List<List<Double?>>? = null,
    val prices: List<List<Double?>>? = null,
    val market_caps: List<List<Double?>>? = null,
    val total_volumes: List<List<Double?>>? = null,
)

@Serializable
data class TreasuryTransactionHistoryModel(
    val transactions: List<TreasuryTransaction>? = null,
)

@Serializable
data class TreasuryTransaction(
    val date: Double? = null,
    val source_url: String? = null,
    val coin_id: String? = null,
    val type: TreasuryTransactionType? = null,
    val holding_net_change: Double? = null,
    val transaction_value_usd: Double? = null,
    val holding_balance: Double? = null,
    val average_entry_value_usd: Double? = null,
)

@Serializable
enum class TreasuryTransactionType {
    buy,
    sell,
}
