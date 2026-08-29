package com.cryptium.coincurrently.ui.main.analytics

import com.cryptium.coincurrently.ui.main.General.AppColorScheme
import com.cryptium.coincurrently.ui.main.General.Enums.PriceChange
import com.cryptium.coincurrently.ui.main.General.Enums.SortOption
import com.cryptium.coincurrently.ui.main.General.SelectedStartTab
import com.cryptium.coincurrently.ui.main.HomeTab.Home.Enums.CarouselFilter
import com.cryptium.coincurrently.ui.main.MarketTab.CryptoDetail.Enums.ChartTypes

/**
 * GA4/Firebase use the same camelCase strings as Swift `SortOption.rawValue`.
 */
fun SortOption.firebaseAnalyticsRawValue(): String =
    name.replaceFirstChar { it.lowercase() }

fun CarouselFilter.firebaseAnalyticsFilterLabel(): String = rawValue

fun ChartTypes.firebaseAnalyticsChartTypeLabel(): String =
    when (this) {
        ChartTypes.PRICE -> "price"
        ChartTypes.VOLUME -> "volume"
        ChartTypes.MKTCAP -> "market_cap"
    }

fun AppColorScheme.firebaseAnalyticsThemeLabel(): String = displayName

fun SelectedStartTab.firebaseAnalyticsTabLabel(): String = getName()

fun PriceChange.firebaseAnalyticsOption(): String = rawValue
