package com.cryptium.coincurrently.core.analytics
/** Mirrors iOS `ScreenLabel` (`NavigationScreens.swift`) for Firebase parameter consistency. */
enum class ScreenLabel(val wire: String) {
    MainFlow("MainFlow"),
    CryptoDetail("CryptoDetail"),
    CompareCoins("CompareCoins"),
    AddPortfolioTransaction("NewPortfolioTransaction"),
    PortfolioSwapTransaction("PortfolioSwapTransaction"),
    ImportWalletTransaction("NewImportTransaction"),
    Feedback("Feedback"),
    FearGreed("FearGreed"),
    GlobalMarket("GlobalMarket"),
    GasFees("GasFees"),
    Search("Search"),
    WalletDetails("WalletDetails"),
    NftDetail("NFTDetail"),
    CategoryDetail("CategoryDetail"),
    ExchangeDetail("ExchangeDetail"),
    DerivativeDetail("DerivativeDetail"),
    CurrencySelector("CurrencySelector"),
    Widgets("Widgets"),
    Converter("Converter"),
    PriceAlerts("PriceAlerts"),
    Achievements("Achievements"),
    Referral("Referral"),
    Settings("Settings"),
    Home("Home"),
    Portfolio("Portfolio"),
    Market("Market"),
    News("News"),
    BuyPro("BuyPro"),
    Toplist("Toplist"),
    Nfts("NFTs"),
    Treasuries("Treasuries"),
    TreasuryDetail("TreasuryDetail"),
    Sectors("Sectors"),
    Exchanges("Exchanges"),
    Derivatives("Derivatives"),
}

/** Mirrors iOS `SheetLabel` for Firebase `sheet_opened`. */
enum class SheetLabel(val wire: String) {
    CommunitySentimentHelp("CommunitySentimentHelp"),
    FearGreedHelp("FearGreedHelp"),
    GasFeesHelp("GasFeesHelp"),
    GasFeesCost("GasFeesCost"),
    CoinSearch("CoinSearch"),
    CurrencySelector("CurrencySelector"),
    Language("Language"),
    Theme("Theme"),
    StartTab("StartTab"),
    ResetApp("ResetApp"),
    NewPriceAlert("NewPriceAlert"),
    SortBy("SortBy"),
    PriceChange("PriceChange"),
    Platform("Platform"),
    RenameWallet("RenameWallet"),
    DeleteWallet("DeleteWallet"),
    /** Per-ticker/contract bottom sheet opened from exchange derivative detail lists. */
    DerivativeTickerDetail("DerivativeTickerDetail"),
    WalletInfo("WalletInfo"),
    PortfolioTypeFilter("PortfolioTypeFilter"),
    PortfolioDateFilter("PortfolioDateFilter"),
    PortfolioCoinFilter("PortfolioCoinFilter"),
    PortfolioTransactionDetails("PortfolioTransactionDetails"),
    PortfolioAddTransactionType("PortfolioAddTransactionType"),
    PortfolioExportFormat("PortfolioExportFormat"),
    TreasuryEntityFilter("TreasuryEntityFilter"),
    TreasuryHoldingFilter("TreasuryHoldingFilter"),
    NewTransactionDate("NewTransactionDate"),
    NewTransactionTime("NewTransactionTime"),
}

enum class CompareCoinChangeSelection(val wire: String) {
    Main("main"),
    Compare("compare"),
}

enum class DateFilterCustomField(val wire: String) {
    Start("start"),
    End("end"),
}

enum class BuyProAction(val wire: String) {
    Favorite("Favorite"),
    NewsFilter("NewsFilter"),
    Settings("Settings"),
    PriceAlert("PriceAlert"),
    ImportWallet("ImportWallet"),
    AddTransaction("AddTransaction"),
    ExportPortfolio("ExportPortfolio"),
    HomePromotion("HomePromotion"),
}

enum class PriceAlertInputType(val wire: String) {
    Price("price"),
    Percent("percent"),
}

enum class PortfolioTransactionDeletedKind(val wire: String) {
    Transaction("transaction"),
    Swap("swap"),
}

/** Mirrors iOS `SubscriptionPurchaseOutcome` sent with `subscription_purchase_completed`. */
enum class SubscriptionPurchaseOutcome(val wire: String) {
    Failed("failed"),
    Cancelled("cancelled"),
    Pending("pending"),
    Unknown("unknown"),
    Renewal("renewal"),
    Success("success"),
}

/** Mirrors iOS `SubscriptionRestoreOutcome` sent with `subscription_restore_completed`. */
enum class SubscriptionRestoreOutcome(val wire: String) {
    Restored("restored"),
    NothingToRestore("nothingToRestore"),
    Cancelled("cancelled"),
    Failed("failed"),
}

