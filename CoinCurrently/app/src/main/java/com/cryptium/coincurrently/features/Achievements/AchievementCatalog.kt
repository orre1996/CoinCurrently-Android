package com.cryptium.coincurrently.features.Achievements
import com.cryptium.coincurrently.features.Achievements.Models.AchievementCategory
import com.cryptium.coincurrently.features.Achievements.Models.AchievementDefinition
import com.cryptium.coincurrently.features.Achievements.Models.AchievementLevel
import com.cryptium.coincurrently.features.Achievements.Models.AchievementLevelTier
import com.cryptium.coincurrently.features.Achievements.Models.AchievementMetric

object AchievementCatalog {
    private val dailyDevotee = AchievementDefinition(
        id = "daily_devotee",
        title = "Daily Devotee",
        description = "Open the app on consecutive days",
        category = AchievementCategory.Streaks,
        icon = "flame",
        metric = AchievementMetric.dailyStreak,
        levels = listOf(
            AchievementLevel(AchievementLevelTier.BRONZE, 1.0, "1 day streak"),
            AchievementLevel(AchievementLevelTier.SILVER, 3.0, "3 day streak"),
            AchievementLevel(AchievementLevelTier.GOLD, 7.0, "7 day streak"),
            AchievementLevel(AchievementLevelTier.PLATINUM, 14.0, "14 day streak"),
            AchievementLevel(AchievementLevelTier.DIAMOND, 30.0, "30 day streak"),
            AchievementLevel(AchievementLevelTier.MASTER, 60.0, "60 day streak"),
            AchievementLevel("Legend", 90.0, "90 day streak"),
        ),
        valueFormat = AchievementDefinition.AchievementValueFormat.INTEGER,
    )

    private val portfolioPioneer = AchievementDefinition(
        id = "portfolio_pioneer",
        title = "Portfolio Pioneer",
        description = "Add transactions to your portfolio",
        category = AchievementCategory.Portfolio,
        icon = "chart_pie",
        metric = AchievementMetric.portfolioTransactions,
        levels = listOf(
            AchievementLevel(AchievementLevelTier.BRONZE, 1.0, "1 transaction"),
            AchievementLevel(AchievementLevelTier.SILVER, 3.0, "3 transactions"),
            AchievementLevel(AchievementLevelTier.GOLD, 5.0, "5 transactions"),
            AchievementLevel(AchievementLevelTier.PLATINUM, 10.0, "10 transactions"),
            AchievementLevel(AchievementLevelTier.DIAMOND, 25.0, "25 transactions"),
            AchievementLevel(AchievementLevelTier.MASTER, 50.0, "50 transactions"),
        ),
        valueFormat = AchievementDefinition.AchievementValueFormat.INTEGER,
    )

    private val portfolioExporter = AchievementDefinition(
        id = "portfolio_exporter",
        title = "Portfolio Exporter",
        description = "Export your portfolio",
        category = AchievementCategory.Portfolio,
        icon = "square.and.arrow.up",
        metric = AchievementMetric.portfolioExports,
        levels = listOf(
            AchievementLevel(AchievementLevelTier.BRONZE, 1.0, "1 export"),
            AchievementLevel(AchievementLevelTier.SILVER, 3.0, "3 exports"),
            AchievementLevel(AchievementLevelTier.GOLD, 5.0, "5 exports"),
            AchievementLevel(AchievementLevelTier.PLATINUM, 10.0, "10 exports"),
            AchievementLevel(AchievementLevelTier.DIAMOND, 25.0, "25 exports"),
        ),
        valueFormat = AchievementDefinition.AchievementValueFormat.INTEGER,
    )

    private val moonShot = AchievementDefinition(
        id = "moon_shot",
        title = "Moon Shot",
        description = "Have a portfolio coin up by a big percentage",
        category = AchievementCategory.Portfolio,
        icon = "trend_up",
        metric = AchievementMetric.maxPortfolioGainPercent,
        levels = listOf(
            AchievementLevel(AchievementLevelTier.BRONZE, 50.0, "Up 50%"),
            AchievementLevel(AchievementLevelTier.SILVER, 100.0, "Up 100%"),
            AchievementLevel(AchievementLevelTier.GOLD, 200.0, "Up 200%"),
            AchievementLevel(AchievementLevelTier.PLATINUM, 500.0, "Up 500%"),
            AchievementLevel(AchievementLevelTier.DIAMOND, 1000.0, "Up 1000%"),
        ),
        valueFormat = AchievementDefinition.AchievementValueFormat.PERCENT,
    )

    private val watchlistWatcher = AchievementDefinition(
        id = "watchlist_watcher",
        title = "Watchlist Watcher",
        description = "Add coins to your favorites",
        category = AchievementCategory.Favorites,
        icon = "star",
        metric = AchievementMetric.favoritesCount,
        levels = listOf(
            AchievementLevel(AchievementLevelTier.BRONZE, 1.0, "1 favorite"),
            AchievementLevel(AchievementLevelTier.SILVER, 3.0, "3 favorites"),
            AchievementLevel(AchievementLevelTier.GOLD, 5.0, "5 favorites"),
            AchievementLevel(AchievementLevelTier.PLATINUM, 10.0, "10 favorites"),
            AchievementLevel(AchievementLevelTier.DIAMOND, 25.0, "25 favorites"),
        ),
        valueFormat = AchievementDefinition.AchievementValueFormat.INTEGER,
    )

    private val alertAce = AchievementDefinition(
        id = "alert_ace",
        title = "Alert Ace",
        description = "Create price alerts",
        category = AchievementCategory.Tools,
        icon = "bell",
        metric = AchievementMetric.priceAlertsCount,
        levels = listOf(
            AchievementLevel(AchievementLevelTier.BRONZE, 1.0, "1 alert"),
            AchievementLevel(AchievementLevelTier.SILVER, 2.0, "2 alerts"),
            AchievementLevel(AchievementLevelTier.GOLD, 3.0, "3 alerts"),
            AchievementLevel(AchievementLevelTier.PLATINUM, 5.0, "5 alerts"),
            AchievementLevel(AchievementLevelTier.DIAMOND, 10.0, "10 alerts"),
            AchievementLevel(AchievementLevelTier.MASTER, 20.0, "20 alerts"),
        ),
        valueFormat = AchievementDefinition.AchievementValueFormat.INTEGER,
    )

    private val widgetWizard = AchievementDefinition(
        id = "widget_wizard",
        title = "Widget Wizard",
        description = "Add coins to your widgets",
        category = AchievementCategory.Tools,
        icon = "widgets",
        metric = AchievementMetric.widgetCoinsCount,
        levels = listOf(
            AchievementLevel(AchievementLevelTier.BRONZE, 1.0, "1 widget coin"),
            AchievementLevel(AchievementLevelTier.SILVER, 2.0, "2 widget coins"),
            AchievementLevel(AchievementLevelTier.GOLD, 4.0, "4 widget coins"),
            AchievementLevel(AchievementLevelTier.PLATINUM, 8.0, "8 widget coins"),
        ),
        valueFormat = AchievementDefinition.AchievementValueFormat.INTEGER,
    )

    private val converterPro = AchievementDefinition(
        id = "converter_pro",
        title = "Converter Pro",
        description = "Use the currency converter",
        category = AchievementCategory.Tools,
        icon = "converter",
        metric = AchievementMetric.converterUses,
        levels = listOf(
            AchievementLevel(AchievementLevelTier.BRONZE, 1.0, "1 conversion"),
            AchievementLevel(AchievementLevelTier.SILVER, 10.0, "10 conversions"),
            AchievementLevel(AchievementLevelTier.GOLD, 50.0, "50 conversions"),
            AchievementLevel(AchievementLevelTier.PLATINUM, 100.0, "100 conversions"),
        ),
        valueFormat = AchievementDefinition.AchievementValueFormat.INTEGER,
    )

    private val chartAnalyst = AchievementDefinition(
        id = "chart_analyst",
        title = "Chart Analyst",
        description = "Switch between chart types",
        category = AchievementCategory.Tools,
        icon = "chart",
        metric = AchievementMetric.candleChartToggles,
        levels = listOf(
            AchievementLevel(AchievementLevelTier.BRONZE, 1.0, "1 toggle"),
            AchievementLevel(AchievementLevelTier.SILVER, 10.0, "10 toggles"),
            AchievementLevel(AchievementLevelTier.GOLD, 50.0, "50 toggles"),
        ),
        valueFormat = AchievementDefinition.AchievementValueFormat.INTEGER,
    )

    private val searchSavant = AchievementDefinition(
        id = "search_savant",
        title = "Search Savant",
        description = "Search for unique coins and assets",
        category = AchievementCategory.Explorer,
        icon = "search",
        metric = AchievementMetric.uniqueSearches,
        levels = listOf(
            AchievementLevel(AchievementLevelTier.BRONZE, 5.0, "5 unique searches"),
            AchievementLevel(AchievementLevelTier.SILVER, 10.0, "10 unique searches"),
            AchievementLevel(AchievementLevelTier.GOLD, 25.0, "25 unique searches"),
            AchievementLevel(AchievementLevelTier.PLATINUM, 50.0, "50 unique searches"),
            AchievementLevel(AchievementLevelTier.DIAMOND, 100.0, "100 unique searches"),
        ),
        valueFormat = AchievementDefinition.AchievementValueFormat.INTEGER,
    )

    private val marketExplorer = AchievementDefinition(
        id = "market_explorer",
        title = "Market Explorer",
        description = "View coin detail pages",
        category = AchievementCategory.Explorer,
        icon = "bitcoin",
        metric = AchievementMetric.cryptoDetailsViewed,
        levels = listOf(
            AchievementLevel(AchievementLevelTier.BRONZE, 5.0, "5 coins viewed"),
            AchievementLevel(AchievementLevelTier.SILVER, 25.0, "25 coins viewed"),
            AchievementLevel(AchievementLevelTier.GOLD, 100.0, "100 coins viewed"),
            AchievementLevel(AchievementLevelTier.PLATINUM, 500.0, "500 coins viewed"),
        ),
        valueFormat = AchievementDefinition.AchievementValueFormat.INTEGER,
    )

    private val compareMaster = AchievementDefinition(
        id = "compare_master",
        title = "Compare Master",
        description = "Compare coins side by side",
        category = AchievementCategory.Explorer,
        icon = "compare",
        metric = AchievementMetric.compareSessions,
        levels = listOf(
            AchievementLevel(AchievementLevelTier.BRONZE, 1.0, "1 compare session"),
            AchievementLevel(AchievementLevelTier.SILVER, 5.0, "5 compare sessions"),
            AchievementLevel(AchievementLevelTier.GOLD, 15.0, "15 compare sessions"),
            AchievementLevel(AchievementLevelTier.PLATINUM, 50.0, "50 compare sessions"),
        ),
        valueFormat = AchievementDefinition.AchievementValueFormat.INTEGER,
    )

    private val newsReader = AchievementDefinition(
        id = "news_reader",
        title = "News Reader",
        description = "Read crypto news articles",
        category = AchievementCategory.Explorer,
        icon = "news",
        metric = AchievementMetric.newsArticlesOpened,
        levels = listOf(
            AchievementLevel(AchievementLevelTier.BRONZE, 5.0, "5 articles"),
            AchievementLevel(AchievementLevelTier.SILVER, 25.0, "25 articles"),
            AchievementLevel(AchievementLevelTier.GOLD, 100.0, "100 articles"),
            AchievementLevel(AchievementLevelTier.PLATINUM, 500.0, "500 articles"),
        ),
        valueFormat = AchievementDefinition.AchievementValueFormat.INTEGER,
    )

    private val nftNavigator = AchievementDefinition(
        id = "nft_navigator",
        title = "NFT Navigator",
        description = "Explore NFT collections",
        category = AchievementCategory.Explorer,
        icon = "nft",
        metric = AchievementMetric.nftDetailsViewed,
        levels = listOf(
            AchievementLevel(AchievementLevelTier.BRONZE, 1.0, "1 NFT viewed"),
            AchievementLevel(AchievementLevelTier.SILVER, 5.0, "5 NFTs viewed"),
            AchievementLevel(AchievementLevelTier.GOLD, 15.0, "15 NFTs viewed"),
            AchievementLevel(AchievementLevelTier.PLATINUM, 50.0, "50 NFTs viewed"),
        ),
        valueFormat = AchievementDefinition.AchievementValueFormat.INTEGER,
    )

    private val exchangeExpert = AchievementDefinition(
        id = "exchange_expert",
        title = "Exchange Expert",
        description = "View exchange detail pages",
        category = AchievementCategory.Explorer,
        icon = "exchange",
        metric = AchievementMetric.exchangeDetailsViewed,
        levels = listOf(
            AchievementLevel(AchievementLevelTier.BRONZE, 1.0, "1 exchange viewed"),
            AchievementLevel(AchievementLevelTier.SILVER, 5.0, "5 exchanges viewed"),
            AchievementLevel(AchievementLevelTier.GOLD, 15.0, "15 exchanges viewed"),
        ),
        valueFormat = AchievementDefinition.AchievementValueFormat.INTEGER,
    )

    private val sectorScout = AchievementDefinition(
        id = "sector_scout",
        title = "Sector Scout",
        description = "Browse crypto sectors",
        category = AchievementCategory.Explorer,
        icon = "sector",
        metric = AchievementMetric.sectorDetailsViewed,
        levels = listOf(
            AchievementLevel(AchievementLevelTier.BRONZE, 1.0, "1 sector viewed"),
            AchievementLevel(AchievementLevelTier.SILVER, 5.0, "5 sectors viewed"),
            AchievementLevel(AchievementLevelTier.GOLD, 15.0, "15 sectors viewed"),
        ),
        valueFormat = AchievementDefinition.AchievementValueFormat.INTEGER,
    )

    private val fearGreedFollower = AchievementDefinition(
        id = "fear_greed_follower",
        title = "Fear & Greed Follower",
        description = "Check the Fear & Greed Index",
        category = AchievementCategory.Explorer,
        icon = "gauge",
        metric = AchievementMetric.fearGreedVisits,
        levels = listOf(
            AchievementLevel(AchievementLevelTier.BRONZE, 1.0, "1 visit"),
            AchievementLevel(AchievementLevelTier.SILVER, 7.0, "7 visits"),
            AchievementLevel(AchievementLevelTier.GOLD, 30.0, "30 visits"),
        ),
        valueFormat = AchievementDefinition.AchievementValueFormat.INTEGER,
    )

    private val gasTracker = AchievementDefinition(
        id = "gas_tracker",
        title = "Gas Tracker",
        description = "Check Ethereum gas fees",
        category = AchievementCategory.Explorer,
        icon = "gas",
        metric = AchievementMetric.gasFeesVisits,
        levels = listOf(
            AchievementLevel(AchievementLevelTier.BRONZE, 1.0, "1 visit"),
            AchievementLevel(AchievementLevelTier.SILVER, 7.0, "7 visits"),
            AchievementLevel(AchievementLevelTier.GOLD, 30.0, "30 visits"),
        ),
        valueFormat = AchievementDefinition.AchievementValueFormat.INTEGER,
    )

    private val treasuryTracker = AchievementDefinition(
        id = "treasury_tracker",
        title = "Treasury Tracker",
        description = "Explore corporate crypto treasuries",
        category = AchievementCategory.Explorer,
        icon = "treasury",
        metric = AchievementMetric.treasuryVisits,
        levels = listOf(
            AchievementLevel(AchievementLevelTier.BRONZE, 1.0, "1 visit"),
            AchievementLevel(AchievementLevelTier.SILVER, 5.0, "5 visits"),
            AchievementLevel(AchievementLevelTier.GOLD, 15.0, "15 visits"),
        ),
        valueFormat = AchievementDefinition.AchievementValueFormat.INTEGER,
    )

    private val securitySentinel = AchievementDefinition(
        id = "security_sentinel",
        title = "Security Sentinel",
        description = "Enable Face ID or Touch ID",
        category = AchievementCategory.Security,
        icon = "face_id",
        metric = AchievementMetric.authenticationEnabled,
        levels = listOf(
            AchievementLevel(AchievementLevelTier.GOLD, 1.0, "Biometric auth enabled"),
        ),
        valueFormat = AchievementDefinition.AchievementValueFormat.BOOLEAN,
    )

    private val referralChampion = AchievementDefinition(
        id = "referral_champion",
        title = "Referral Champion",
        description = "Refer friends to CoinCurrently",
        category = AchievementCategory.Security,
        icon = "referral",
        metric = AchievementMetric.referralsCompleted,
        levels = listOf(
            AchievementLevel(AchievementLevelTier.BRONZE, 1.0, "1 referral"),
            AchievementLevel(AchievementLevelTier.SILVER, 2.0, "2 referrals"),
            AchievementLevel(AchievementLevelTier.GOLD, 3.0, "3 referrals"),
        ),
        valueFormat = AchievementDefinition.AchievementValueFormat.INTEGER,
    )

    private val imATeapot = AchievementDefinition(
        id = "418_im_a_teapot",
        title = "418 I'm a teapot",
        description = "Try to redeem your own referral code",
        category = AchievementCategory.Security,
        icon = "teapot",
        metric = AchievementMetric.selfRedeems,
        levels = listOf(
            AchievementLevel(AchievementLevelTier.GOLD, 1.0, "Tried to redeem yourself"),
        ),
        valueFormat = AchievementDefinition.AchievementValueFormat.BOOLEAN,
    )

    val all: List<AchievementDefinition> = listOf(
        dailyDevotee,
        watchlistWatcher,
        portfolioPioneer,
        portfolioExporter,
        moonShot,
        alertAce,
        widgetWizard,
        searchSavant,
        marketExplorer,
        compareMaster,
        newsReader,
        nftNavigator,
        exchangeExpert,
        sectorScout,
        fearGreedFollower,
        gasTracker,
        treasuryTracker,
        chartAnalyst,
        converterPro,
        securitySentinel,
        referralChampion,
        imATeapot,
    )

    fun achievements(category: AchievementCategory): List<AchievementDefinition> =
        all.filter { it.category == category }
}
