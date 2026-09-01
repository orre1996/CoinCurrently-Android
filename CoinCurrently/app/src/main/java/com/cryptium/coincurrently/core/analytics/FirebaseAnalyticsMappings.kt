package com.cryptium.coincurrently.core.analytics
import com.cryptium.coincurrently.sharedui.components.AppColorScheme
import com.cryptium.coincurrently.sharedui.models.PriceChange
import com.cryptium.coincurrently.sharedui.models.SortOption
import com.cryptium.coincurrently.sharedui.components.SelectedStartTab
import com.cryptium.coincurrently.features.Home.Carousel.CarouselFilter
import com.cryptium.coincurrently.features.CoinDetail.ChartTypes

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
