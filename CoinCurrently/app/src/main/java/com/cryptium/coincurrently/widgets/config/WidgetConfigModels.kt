package com.cryptium.coincurrently.widgets.config

import com.cryptium.coincurrently.core.models.CarouselFilter
import com.cryptium.coincurrently.core.networking.NewsCategory

data class SingleCoinWidgetConfig(val coinId: String?)

data class DuoCoinWidgetConfig(
    val firstCoinId: String?,
    val secondCoinId: String?,
)

data class QuadCoinWidgetConfig(
    val coinIds: List<String?>,
)

data class MultiCoinWidgetConfig(
    val coinIds: List<String?>,
)

data class NewsWidgetConfig(
    val category: String = "general",
    val filterValue: String? = null,
) {
    val requiresPro: Boolean
        get() = when (category.lowercase()) {
            "exchanges", "nfts", "nft", "technical_analysis", "technical analysis", "coins" -> true
            else -> false
        }
}

fun NewsWidgetConfig.toNewsCategory(): NewsCategory {
    return when (category.lowercase()) {
        "positive" -> NewsCategory.Positive()
        "neutral" -> NewsCategory.Neutral()
        "negative" -> NewsCategory.Negative()
        "coins" -> NewsCategory.Coin(filterValue?.uppercase().orEmpty())
        "exchanges" -> NewsCategory.Exchanges()
        "nfts", "nft" -> NewsCategory.NFT()
        "technical_analysis", "technical analysis" -> NewsCategory.Technical_Analysis()
        else -> NewsCategory.General()
    }
}

fun CarouselFilter.toNewsWidgetConfig(coinSymbol: String? = null): NewsWidgetConfig {
    return when (this) {
        CarouselFilter.General -> NewsWidgetConfig(category = "general")
        CarouselFilter.Positive -> NewsWidgetConfig(category = "positive")
        CarouselFilter.Neutral -> NewsWidgetConfig(category = "neutral")
        CarouselFilter.Negative -> NewsWidgetConfig(category = "negative")
        is CarouselFilter.Coins -> NewsWidgetConfig(category = "coins", filterValue = coinSymbol)
        is CarouselFilter.Exchanges -> NewsWidgetConfig(category = "exchanges")
        CarouselFilter.Nfts -> NewsWidgetConfig(category = "nfts")
        CarouselFilter.TechnicalAnalysis -> NewsWidgetConfig(category = "technical_analysis")
        else -> NewsWidgetConfig(category = "general")
    }
}

fun NewsWidgetConfig.toCarouselFilter(): CarouselFilter {
    return when (category.lowercase()) {
        "positive" -> CarouselFilter.Positive
        "neutral" -> CarouselFilter.Neutral
        "negative" -> CarouselFilter.Negative
        "coins" -> CarouselFilter.Coins(filterValue)
        "exchanges" -> CarouselFilter.Exchanges(null)
        "nfts", "nft" -> CarouselFilter.Nfts
        "technical_analysis", "technical analysis" -> CarouselFilter.TechnicalAnalysis
        else -> CarouselFilter.General
    }
}
