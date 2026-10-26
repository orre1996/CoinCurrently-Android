package com.cryptium.coincurrently.core.analytics
/** Last segment of a NavController [androidx.navigation.NavDestination.route], aligned with [MainActivity] `currentRoute`. */
fun navRouteSuffixForAnalytics(fullRoute: String?): String? {
    if (fullRoute.isNullOrBlank()) return null
    val afterDot = fullRoute.substringAfterLast(".", fullRoute)
    return afterDot.substringBefore("?").substringBefore("/").takeIf { it.isNotBlank() }
}

/** Maps a route suffix (from [navRouteSuffixForAnalytics] or [MainActivity] `currentRoute`) to [ScreenLabel]. */
fun screenLabelFromRoute(routeSuffix: String?): ScreenLabel =
    when (routeSuffix) {
        null, "" -> ScreenLabel.MainFlow
        "Loading" -> ScreenLabel.MainFlow
        "Home" -> ScreenLabel.Home
        "Portfolio" -> ScreenLabel.Portfolio
        "Market" -> ScreenLabel.Market
        "Settings" -> ScreenLabel.Settings
        "CryptoDetail" -> ScreenLabel.CryptoDetail
        "CompareCoins" -> ScreenLabel.CompareCoins
        "WalletDetails" -> ScreenLabel.WalletDetails
        "NewPortfolioTransaction", "AddPortfolioTransaction" -> ScreenLabel.AddPortfolioTransaction
        "NewPortfolioSwapTransaction", "AddPortfolioSwapTransaction" -> ScreenLabel.PortfolioSwapTransaction
        "NewImportTransaction", "ImportWalletTransaction" -> ScreenLabel.ImportWalletTransaction
        "Feedback" -> ScreenLabel.Feedback
        "FearGreed" -> ScreenLabel.FearGreed
        "FearGreedHelp" -> ScreenLabel.FearGreed
        "GlobalMarket" -> ScreenLabel.GlobalMarket
        "GasFees" -> ScreenLabel.GasFees
        "NFTDetail" -> ScreenLabel.NftDetail
        "CategoryDetail" -> ScreenLabel.CategoryDetail
        "ExchangeDetail" -> ScreenLabel.ExchangeDetail
        "DerivativeDetail" -> ScreenLabel.DerivativeDetail
        "TreasuryDetail" -> ScreenLabel.TreasuryDetail
        "CurrencySelector" -> ScreenLabel.CurrencySelector
        "Widgets" -> ScreenLabel.Widgets
        "Calculator" -> ScreenLabel.Converter
        "PriceAlerts" -> ScreenLabel.PriceAlerts
        "SelectCoin" -> ScreenLabel.Search
        else -> ScreenLabel.MainFlow
    }
