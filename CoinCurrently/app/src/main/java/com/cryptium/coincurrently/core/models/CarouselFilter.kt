package com.cryptium.coincurrently.core.models
import com.cryptium.coincurrently.core.extensions.TimeframeLabels
import com.cryptium.coincurrently.core.networking.NewsCategory

/**
 * Carousel filter for Home and News screens.
 * Home filters: TopCoins, Favorited, Trending, TimeframeWinners, TimeframeLosers, NewlyListed.
 * News filters: General, Positive, Neutral, Negative, Coins, Exchanges, Nfts, TechnicalAnalysis.
 */
sealed class CarouselFilter {

    // Home
    data object TopCoins : CarouselFilter()
    data object Favorites : CarouselFilter()
    data object Trending : CarouselFilter()
    data object TimeframeWinners : CarouselFilter()
    data object TimeframeLosers : CarouselFilter()
    data object NewlyListed : CarouselFilter()

    // News
    data object General : CarouselFilter()
    data object Positive : CarouselFilter()
    data object Neutral : CarouselFilter()
    data object Negative : CarouselFilter()
    data class Coins(val symbol: String? = null) : CarouselFilter()
    data class Exchanges(val id: String? = null) : CarouselFilter()
    data object Nfts : CarouselFilter()
    data object TechnicalAnalysis : CarouselFilter()

    /**
     * Returns true when this filter matches the other for selection highlighting.
     * Filters with associated values (e.g. Coins, Exchanges) match by type, not by value.
     */
    fun matchesForSelection(other: CarouselFilter?): Boolean = when {
        other == null -> false
        this is Coins && other is Coins -> true
        this is Exchanges && other is Exchanges -> true
        else -> this == other
    }

    val rawValue: String
        get() = when (this) {
            TopCoins -> "Top Coins"
            Favorites -> "Favorites"
            Trending -> "Trending"
            TimeframeWinners -> TimeframeLabels.preferredWinnersTimeframe
            TimeframeLosers -> TimeframeLabels.preferredLosersTimeframe
            NewlyListed -> "Newly Listed"
            General -> "General"
            Positive -> "Positive"
            Neutral -> "Neutral"
            Negative -> "Negative"
            is Coins -> symbol ?: "Coins (Pro)"
            is Exchanges -> "Exchanges (Pro)"
            Nfts -> "NFTs (Pro)"
            TechnicalAnalysis -> "Technical Analysis (Pro)"
        }

    val shouldShowViewAll: Boolean
        get() = when (this) {
            TopCoins, TimeframeWinners, TimeframeLosers -> true
            else -> false
        }

    val requiresPro: Boolean
        get() = when (this) {
            is Coins, is Exchanges, Nfts, TechnicalAnalysis -> true
            else -> false
        }

    val toNewsCategory: NewsCategory
        get() = when (this) {
            General -> NewsCategory.General()
            Positive -> NewsCategory.Positive()
            Neutral -> NewsCategory.Neutral()
            Negative -> NewsCategory.Negative()
            is Coins -> NewsCategory.Coin(symbol ?: "")
            is Exchanges -> NewsCategory.Exchange(id ?: "")
            Nfts -> NewsCategory.NFT()
            TechnicalAnalysis -> NewsCategory.Technical_Analysis()
            else -> NewsCategory.General()
        }

    companion object {
        val homeFilters: List<CarouselFilter> by lazy {
            listOf(
                TopCoins,
                Favorites,
                Trending,
                TimeframeWinners,
                TimeframeLosers,
                NewlyListed
            )
        }

        fun defaultHomeFilter(hasFavorites: Boolean): CarouselFilter =
            if (hasFavorites) Favorites else TopCoins

        val newsFilters: List<CarouselFilter> by lazy {
            listOf(
                General,
                Positive,
                Neutral,
                Negative,
                Coins(null),
                Exchanges(null),
                Nfts,
                TechnicalAnalysis
            )
        }
    }
}
