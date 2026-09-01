package com.cryptium.coincurrently.features.Achievements.Models
enum class AchievementMetric {
    dailyStreak,
    favoritesCount,
    portfolioTransactions,
    portfolioExports,
    maxPortfolioGainPercent,
    priceAlertsCount,
    widgetCoinsCount,
    uniqueSearches,
    cryptoDetailsViewed,
    compareSessions,
    newsArticlesOpened,
    nftDetailsViewed,
    exchangeDetailsViewed,
    sectorDetailsViewed,
    fearGreedVisits,
    gasFeesVisits,
    treasuryVisits,
    candleChartToggles,
    converterUses,
    authenticationEnabled,
    referralsCompleted,
    selfRedeems,
}

enum class AchievementCounterKey {
    cryptoDetailsViewed,
    compareSessions,
    converterUses,
    newsArticlesOpened,
    nftDetailsViewed,
    exchangeDetailsViewed,
    sectorDetailsViewed,
    fearGreedVisits,
    gasFeesVisits,
    treasuryVisits,
    candleChartToggles,
    referralsCompleted,
    selfRedeems,
    portfolioExports;

    val metric: AchievementMetric
        get() = AchievementMetric.valueOf(name)
}
