package com.cryptium.coincurrently.sharedui.navigation
import com.cryptium.coincurrently.app.NavigationScreens
import com.cryptium.coincurrently.R

sealed class NavigationItem(
    var route: NavigationScreens,
    val icon: Int,
    var title: String
) {
    object Home: NavigationItem(
        NavigationScreens.Home,
        R.drawable.home,
        "Home"
    )

    object Portfolio: NavigationItem(
        NavigationScreens.Portfolio,
        R.drawable.portfolio,
        "Portfolio"
    )

    object Market: NavigationItem(
        NavigationScreens.Market,
        R.drawable.market,
        "Market"
    )

    object More: NavigationItem(
        NavigationScreens.Settings,
        R.drawable.settings,
        "Settings"
    )
}