package com.cryptium.coincurrently.features.Achievements.Models
data class AchievementCounters(
    var cryptoDetailsViewed: Int = 0,
    var compareSessions: Int = 0,
    var converterUses: Int = 0,
    var newsArticlesOpened: Int = 0,
    var nftDetailsViewed: Int = 0,
    var exchangeDetailsViewed: Int = 0,
    var sectorDetailsViewed: Int = 0,
    var fearGreedVisits: Int = 0,
    var gasFeesVisits: Int = 0,
    var treasuryVisits: Int = 0,
    var candleChartToggles: Int = 0,
    var referralsCompleted: Int = 0,
    var selfRedeems: Int = 0,
    var portfolioExports: Int = 0,
) {
    fun increment(key: AchievementCounterKey) {
        when (key) {
            AchievementCounterKey.cryptoDetailsViewed -> cryptoDetailsViewed += 1
            AchievementCounterKey.compareSessions -> compareSessions += 1
            AchievementCounterKey.converterUses -> converterUses += 1
            AchievementCounterKey.newsArticlesOpened -> newsArticlesOpened += 1
            AchievementCounterKey.nftDetailsViewed -> nftDetailsViewed += 1
            AchievementCounterKey.exchangeDetailsViewed -> exchangeDetailsViewed += 1
            AchievementCounterKey.sectorDetailsViewed -> sectorDetailsViewed += 1
            AchievementCounterKey.fearGreedVisits -> fearGreedVisits += 1
            AchievementCounterKey.gasFeesVisits -> gasFeesVisits += 1
            AchievementCounterKey.treasuryVisits -> treasuryVisits += 1
            AchievementCounterKey.candleChartToggles -> candleChartToggles += 1
            AchievementCounterKey.referralsCompleted -> referralsCompleted += 1
            AchievementCounterKey.selfRedeems -> selfRedeems += 1
            AchievementCounterKey.portfolioExports -> portfolioExports += 1
        }
    }

    fun value(key: AchievementCounterKey): Int = when (key) {
        AchievementCounterKey.cryptoDetailsViewed -> cryptoDetailsViewed
        AchievementCounterKey.compareSessions -> compareSessions
        AchievementCounterKey.converterUses -> converterUses
        AchievementCounterKey.newsArticlesOpened -> newsArticlesOpened
        AchievementCounterKey.nftDetailsViewed -> nftDetailsViewed
        AchievementCounterKey.exchangeDetailsViewed -> exchangeDetailsViewed
        AchievementCounterKey.sectorDetailsViewed -> sectorDetailsViewed
        AchievementCounterKey.fearGreedVisits -> fearGreedVisits
        AchievementCounterKey.gasFeesVisits -> gasFeesVisits
        AchievementCounterKey.treasuryVisits -> treasuryVisits
        AchievementCounterKey.candleChartToggles -> candleChartToggles
        AchievementCounterKey.referralsCompleted -> referralsCompleted
        AchievementCounterKey.selfRedeems -> selfRedeems
        AchievementCounterKey.portfolioExports -> portfolioExports
    }
}
