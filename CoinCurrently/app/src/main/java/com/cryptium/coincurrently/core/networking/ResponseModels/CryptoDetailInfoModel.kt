package com.cryptium.coincurrently.core.networking.ResponseModels
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.cryptium.coincurrently.R
import com.cryptium.coincurrently.sharedui.theme.neutral
import com.cryptium.coincurrently.sharedui.theme.onSurfaceDim
import com.cryptium.coincurrently.sharedui.theme.trendDown
import com.cryptium.coincurrently.sharedui.theme.trendUp
import com.cryptium.coincurrently.sharedui.components.GlobalFunctions
import com.cryptium.coincurrently.sharedui.components.PreferredTimeframe
import com.cryptium.coincurrently.core.extensions.getSelectedCurrency
import com.cryptium.coincurrently.core.networking.ResponseModels.Currency
import kotlinx.serialization.Serializable

@Serializable
data class CryptoDetailInfoModel(
    var id: String? = null,
    var symbol: String? = null,
    var name: String? = null,
    var image: Logo? = null,
    var market_cap_rank: Int? = null,
    var market_data: MarketData? = null,
    var genesis_date: String? = null,
    var tickers: ArrayList<Ticker>? = null,
    var description: Description? = null,
    var links: Links? = null,
    var asset_platform_id: String? = null,
    var sentiment_votes_up_percentage: Double? = null,
    var sentiment_votes_down_percentage: Double? = null
)

@Serializable
data class Logo(
    var thumb: String? = null,
    var small: String? = null,
    var large: String? = null
)

@Serializable
data class MarketData(
    var current_price: Currency? = null,
    var market_cap: Currency? = null,
    var high_24h: Currency? = null,
    var low_24h: Currency? = null,
    var total_volume: Currency? = null,
    var price_change_percentage_24h: Double? = null,
    var price_change_percentage_7d: Double? = null,
    var price_change_percentage_14d: Double? = null,
    var price_change_percentage_30d: Double? = null,
    var price_change_percentage_60d: Double? = null,
    var price_change_percentage_90d: Double? = null,
    var price_change_percentage_180d: Double? = null,
    var price_change_percentage_200d: Double? = null,
    var price_change_percentage_1y: Double? = null,
    var sparkline_7d: Sparkline? = null,
    var ath: Currency? = null,
    var ath_change_percentage: Currency? = null,
    var ath_date: Map<String, String>? = null,
    var atl: Currency? = null,
    var atl_change_percentage: Currency? = null,
    var atl_date: Map<String, String>? = null,
    var max_supply: Double? = null,
    var circulating_supply: Double? = null
)

fun MarketData.getPreferredChange(): Double? {
    return when (GlobalFunctions.getPreferredTimeframe()) {
        PreferredTimeframe.DAILY -> price_change_percentage_24h
        PreferredTimeframe.WEEKLY -> price_change_percentage_7d
        PreferredTimeframe.MONTHLY -> price_change_percentage_30d
    }
}

@Serializable
data class Ticker(
    var base: String? = null,
    var target: String? = null,
    var market: Market? = null,
    var last: Double? = null,
    var trust_score: String? = null,
    var trade_url: String? = null,
    var converted_volume: Currency? = null,
    var converted_last: Currency? = null
)

val Ticker.getTrustColor: Color @Composable
    get() {
        return when (trust_score) {
            "green" -> MaterialTheme.colors.trendUp
            "yellow" -> MaterialTheme.colors.neutral
            "red" -> MaterialTheme.colors.trendDown
            else -> MaterialTheme.colors.onSurfaceDim
        }
    }

@Serializable
data class Market(
    var name: String? = null
)

@Serializable
data class Description(
    var en: String? = null
)

@Serializable
data class Links(
    var homepage: ArrayList<String>? = null,
    var blockchain_site: ArrayList<String>? = null,
    var official_forum_url: ArrayList<String>? = null,
    var chat_url: ArrayList<String>? = null,
    var announcement_url: ArrayList<String>? = null,
    var whitepaper: String? = null,

    var twitter_screen_name: String? = null,
    var facebook_username: String? = null,
    var telegram_channel_identifier: String? = null,
    var subreddit_url: String? = null,
    var repos_url: Repos? = null
)

private fun String.isSteemitOrMedium(): Boolean {
    val lower = lowercase()
    return lower.contains("steemit") || lower.contains("medium")
}

val Links.officialWebsites: List<String>
    get() {
        val home = homepage?.map { it }?.filter { it.isNotEmpty() } ?: listOf()
        val forum = official_forum_url?.map { it }?.filter { it.isNotEmpty() } ?: listOf()
        // Exclude steemit/medium - those go to Communities
        return (home + forum).filter { !it.isSteemitOrMedium() }
    }

val Links.communityLinks: List<String>
    get() {
        val home = homepage?.map { it }?.filter { it.isNotEmpty() } ?: listOf()
        val forum = official_forum_url?.map { it }?.filter { it.isNotEmpty() } ?: listOf()
        val chat = chat_url?.map { it }?.filter { it.isNotEmpty() } ?: listOf()
        val announcement = announcement_url?.map { it }?.filter { it.isNotEmpty() } ?: listOf()
        val steemitMedium = (home + forum).filter { it.isSteemitOrMedium() }
        return (socialMediaLinks + steemitMedium + chat + announcement).filter { it.isNotEmpty() }
    }

val Links.communityIcons: List<Int>
    get() {
        fun iconForUrl(url: String): Int = when {
            url.contains("steemit", ignoreCase = true) -> R.drawable.steemit
            url.contains("medium", ignoreCase = true) -> R.drawable.medium
            url.contains("telegram", ignoreCase = true) || url.contains("t.me", ignoreCase = true) -> R.drawable.telegram
            url.contains("discord", ignoreCase = true) -> R.drawable.discord
            url.contains("reddit", ignoreCase = true) -> R.drawable.reddit
            url.contains("facebook", ignoreCase = true) || url.contains("fb.com", ignoreCase = true) || url.contains("meta", ignoreCase = true) -> R.drawable.meta
            url.contains("twitter", ignoreCase = true) || url.contains("x.com", ignoreCase = true) -> R.drawable.x
            else -> R.drawable.website
        }
        val socialIcons = socialMediaIcons
        val home = homepage?.map { it }?.filter { it.isNotEmpty() } ?: listOf()
        val forum = official_forum_url?.map { it }?.filter { it.isNotEmpty() } ?: listOf()
        val steemitMediumIcons = (home + forum).filter { it.isSteemitOrMedium() }.map { iconForUrl(it) }
        val chatIcons = chat_url?.filter { it.isNotEmpty() }?.map { iconForUrl(it) } ?: emptyList()
        val announcementIcons = announcement_url?.filter { it.isNotEmpty() }?.map { iconForUrl(it) } ?: emptyList()
        return socialIcons + steemitMediumIcons + chatIcons + announcementIcons
    }

val Links.relatedWebsites: List<String>
    get() = blockchain_site?.filter { it.isNotEmpty() } ?: emptyList()

val Links.socialMediaLinks: List<String>
    get() {
        return listOf(
            subreddit_url,
            getFacebookUrl,
            getTwitterUrl,
            getTelegramUrl
        ).mapNotNull { it }
    }

val Links.repositoryUrls: List<String>
    get() {
        val githubUrls = repos_url?.github?.map({ it }) ?: listOf()
        val bitbucketUrls = repos_url?.bitbucket?.map({ it }) ?: listOf()

        return (githubUrls + bitbucketUrls).filter({ it.isNotEmpty() })
    }

val Links.socialMediaIcons: List<Int>
    get() {
        val icons = mutableListOf<Int>()

        if (!subreddit_url.isNullOrEmpty()) {
            icons.add(R.drawable.reddit)
        }

        if (!facebook_username.isNullOrEmpty()) {
            icons.add(R.drawable.meta)
        }

        if (!twitter_screen_name.isNullOrEmpty()) {
            icons.add(R.drawable.x)
        }

        if (!telegram_channel_identifier.isNullOrEmpty()) {
            icons.add(R.drawable.telegram)
        }

        return icons
    }

val Links.getTelegramUrl: String?
    get() {
        return if (telegram_channel_identifier.isNullOrEmpty()) null else telegram_channel_identifier
    }

val Links.getTwitterUrl: String
    get() {
        return "https://twitter.com/${twitter_screen_name ?: ""}"
    }

val Links.getFacebookUrl: String
    get() {
        return "https://facebook.com/${facebook_username ?: ""}"
    }

val Links.isAllLinksEmpty: Boolean
    get() {
        return (homepage?.all { it.isEmpty() } ?: true) &&
                (announcement_url?.all { it.isEmpty() } ?: true) &&
                (blockchain_site?.all { it.isEmpty() } ?: true) &&
                (official_forum_url?.all { it.isEmpty() } ?: true) &&
                (chat_url?.all { it.isEmpty() } ?: true) &&
                (subreddit_url?.isEmpty() ?: true) &&
                (facebook_username?.isEmpty() ?: true) &&
                (twitter_screen_name?.isEmpty() ?: true) &&
                (telegram_channel_identifier?.isEmpty() ?: true)
    }

@Serializable
data class Repos(
    var github: ArrayList<String>? = arrayListOf(),
    var bitbucket: ArrayList<String>? = arrayListOf()
)

fun Repos.repositories(): List<String> {
    return (github?.map { it } ?: listOf()) + (bitbucket?.map { it } ?: listOf())
}


fun CryptoDetailInfoModel.toToplist(): Toplist {
    return Toplist(
        this.id,
        this.symbol,
        this.name,
        this.image?.large,
        this.market_data?.current_price?.getSelectedCurrency(null),
        this.market_cap_rank,
        this.market_data?.sparkline_7d,
        this.market_data?.price_change_percentage_24h,
        this.market_data?.price_change_percentage_7d,
        this.market_data?.price_change_percentage_30d,
        market_cap = this.market_data?.market_cap?.getSelectedCurrency()
    )
}