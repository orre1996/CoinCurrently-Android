package com.cryptium.coincurrently.core.models
import com.cryptium.coincurrently.core.extensions.TimeframeLabels

/**
 * Carousel filter for the Home screen.
 * Home filters: TopCoins, Favorited, Trending, TimeframeWinners, TimeframeLosers, NewlyListed.
 */
sealed class CarouselFilter {

    data object TopCoins : CarouselFilter()
    data object Favorites : CarouselFilter()
    data object Trending : CarouselFilter()
    data object TimeframeWinners : CarouselFilter()
    data object TimeframeLosers : CarouselFilter()
    data object NewlyListed : CarouselFilter()

    fun matchesForSelection(other: CarouselFilter?): Boolean = this == other

    val rawValue: String
        get() = when (this) {
            TopCoins -> "Top Coins"
            Favorites -> "Favorites"
            Trending -> "Trending"
            TimeframeWinners -> TimeframeLabels.preferredWinnersTimeframe
            TimeframeLosers -> TimeframeLabels.preferredLosersTimeframe
            NewlyListed -> "Newly Listed"
        }

    val shouldShowViewAll: Boolean
        get() = when (this) {
            TopCoins, TimeframeWinners, TimeframeLosers -> true
            else -> false
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
    }
}
