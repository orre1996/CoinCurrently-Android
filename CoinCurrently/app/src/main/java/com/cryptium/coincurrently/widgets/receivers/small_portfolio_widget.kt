package com.cryptium.coincurrently.widgets.receivers

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.widget.RemoteViews
import com.cryptium.coincurrently.R
import com.cryptium.coincurrently.app.MainActivity
import com.cryptium.coincurrently.core.extensions.changeWithSymbol
import com.cryptium.coincurrently.core.extensions.formatCurrency
import com.cryptium.coincurrently.core.extensions.roundTo
import com.cryptium.coincurrently.core.extensions.stars
import com.cryptium.coincurrently.core.networking.ResponseModels.Pie
import com.cryptium.coincurrently.core.persistence.UserPreferences
import com.cryptium.coincurrently.features.Portfolio.PortfolioRepository
import com.cryptium.coincurrently.sharedui.charts.getPercentChangeDrawablePortfolio
import com.cryptium.coincurrently.sharedui.charts.getWidgetPercentChangeColorPortfolio
import com.cryptium.coincurrently.sharedui.models.FormattingThreshold
import com.cryptium.coincurrently.widgets.config.WidgetRuntimeInitializer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class small_portfolio_widget : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        WidgetRuntimeInitializer.ensureInitialized(context)
        for (appWidgetId in appWidgetIds) {
            appWidgetManager.updateAppWidget(
                appWidgetId,
                buildPlaceholderSmallPortfolioWidget(context),
            )
            createSmallPortfolioWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onAppWidgetOptionsChanged(
        context: Context?,
        appWidgetManager: AppWidgetManager?,
        appWidgetId: Int,
        newOptions: Bundle?,
    ) {
        if (appWidgetManager != null && context != null) {
            onUpdate(context, appWidgetManager, intArrayOf(appWidgetId))
        }
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
    }
}

internal fun createSmallPortfolioWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int,
) {
    WidgetRuntimeInitializer.ensureInitialized(context)

    if (UserPreferences.getIsHidePortfolioEnabled()) {
        val views = RemoteViews(context.packageName, R.layout.small_portfolio_disabled_widget)
        views.setInt(
            R.id.ll_view,
            "setBackgroundResource",
            if (isWidgetDarkMode(context)) R.drawable.widget_background_dark else R.drawable.widget_background_light,
        )
        views.setTextColor(R.id.txt_message, widgetOnSurface(context))
        setSmallPortfolioClickIntent(views, context)
        appWidgetManager.updateAppWidget(appWidgetId, views)
        return
    }

    val views = buildPlaceholderSmallPortfolioWidget(context)
    setSmallPortfolioClickIntent(views, context)
    appWidgetManager.updateAppWidget(appWidgetId, views)

    GlobalScope.launch(Dispatchers.IO) {
        try {
            PortfolioRepository.getPortfolio()
            PortfolioRepository.getPortfolioAllCurrencies()
            val portfolio = buildPortfolioList()
            val selectedCurrency = UserPreferences.getSelectedCurrency(context)
            val widgetData = buildPortfolioWidgetData(
                portfolio = portfolio,
                allPrices = PortfolioRepository.portfolioAllCurrencies,
                currencyCode = selectedCurrency.code,
            )
            if (widgetData.isEmpty) {
                appWidgetManager.updateAppWidget(
                    appWidgetId,
                    createPortfolioWidgetEmptyRemoteViews(context),
                )
                return@launch
            }
            val loadedViews = createSmallPortfolioRemoteViews(
                context = context,
                widgetData = widgetData,
            )
            appWidgetManager.updateAppWidget(appWidgetId, loadedViews)
        } catch (_: Exception) {
            appWidgetManager.updateAppWidget(appWidgetId, buildPlaceholderSmallPortfolioWidget(context))
        }
    }
}

private fun buildPlaceholderSmallPortfolioWidget(context: Context): RemoteViews {
    return createPortfolioWidgetEmptyRemoteViews(context)
}

internal fun createSmallPortfolioRemoteViews(
    context: Context,
    widgetData: PortfolioWidgetData,
    embeddedInMedium: Boolean = false,
): RemoteViews {
    val views = RemoteViews(
        context.packageName,
        if (embeddedInMedium) R.layout.small_portfolio_widget_cell else R.layout.small_portfolio_widget,
    )
    bindSmallPortfolioRemoteViews(
        views = views,
        context = context,
        widgetData = widgetData,
        embeddedInMedium = embeddedInMedium,
    )
    if (!embeddedInMedium) {
        setSmallPortfolioClickIntent(views, context)
    }
    return views
}

internal fun bindMediumPortfolioCell(
    parent: RemoteViews,
    containerId: Int,
    widgetData: PortfolioWidgetData,
    context: Context,
) {
    val cellViews = createSmallPortfolioRemoteViews(
        context = context,
        widgetData = widgetData,
        embeddedInMedium = true,
    )
    parent.removeAllViews(containerId)
    parent.addView(containerId, cellViews)
}

internal fun buildPortfolioList(): List<Pie> {
    val cachedPortfolio = PortfolioRepository.portfolio ?: return emptyList()
    val piesData = cachedPortfolio.data
    piesData.sanitizeGsonNulls()
    val dataPies = piesData.data
    val otherPies = piesData.otherCoins
        .filter { otherCoin -> dataPies.none { it.data?.id == otherCoin.id } }
        .map { Pie(data = it) }
    return dataPies + otherPies
}

internal fun bindSmallPortfolioRemoteViews(
    views: RemoteViews,
    context: Context,
    widgetData: PortfolioWidgetData,
    embeddedInMedium: Boolean = false,
) {
    applySmallPortfolioTheme(views, context, embeddedInMedium)

    val hideData = UserPreferences.getHidePortfolioData()
    val selectedCurrency = UserPreferences.getSelectedCurrency(context)

    views.setTextViewText(
        R.id.txt_total_value,
        when {
            hideData -> stars
            widgetData.isEmpty || widgetData.totalValue == null -> "—"
            else -> widgetData.totalValue.formatCurrency(
                threshold = FormattingThreshold.BILLION,
                currency = selectedCurrency,
            )
        },
    )

    if (hideData) {
        views.setViewVisibility(R.id.view_price_change, View.VISIBLE)
        views.setImageViewResource(R.id.view_price_change, R.drawable.trend_still)
        views.setInt(R.id.view_price_change, "setColorFilter", widgetOnSurfaceDim(context))
        views.setTextViewText(R.id.txt_pl_change, stars)
        views.setTextColor(R.id.txt_pl_change, widgetOnSurfaceDim(context))
        return
    }

    val changeValue = widgetData.portfolioDailyChangeValue
    val changePercent = widgetData.portfolioDailyChangePercent
    val hasValidChange = !widgetData.isEmpty &&
        changeValue != null &&
        changePercent != null &&
        !changePercent.isNaN() &&
        !changePercent.isInfinite()

    if (!hasValidChange) {
        views.setViewVisibility(R.id.view_price_change, View.GONE)
        views.setTextViewText(R.id.txt_pl_change, "—")
        views.setTextColor(R.id.txt_pl_change, widgetOnSurfaceDim(context))
        return
    }

    val changeColor = changeValue.getWidgetPercentChangeColorPortfolio(context)
    val changeIcon = changeValue.getPercentChangeDrawablePortfolio()

    views.setViewVisibility(R.id.view_price_change, View.VISIBLE)
    views.setImageViewResource(R.id.view_price_change, changeIcon)
    views.setInt(R.id.view_price_change, "setColorFilter", changeColor)

    views.setTextViewText(
        R.id.txt_pl_change,
        formatChangeValueAndPercent(changeValue, changePercent),
    )
    views.setTextColor(R.id.txt_pl_change, changeColor)
}

private fun applySmallPortfolioTheme(
    views: RemoteViews,
    context: Context,
    embeddedInMedium: Boolean = false,
) {
    if (!embeddedInMedium) {
        views.setInt(
            R.id.ll_view,
            "setBackgroundResource",
            if (isWidgetDarkMode(context)) {
                R.drawable.widget_background_dark
            } else {
                R.drawable.widget_background_light
            },
        )
    }
    views.setTextColor(R.id.txt_portfolio_title, widgetOnSurfaceDimmer(context))
    views.setTextColor(R.id.txt_total_value, widgetOnSurface(context))
    views.setTextColor(R.id.txt_pl_title, widgetOnSurfaceDimmer(context))
}

internal fun formatSignedChangePercent(percent: Double?): String {
    if (percent == null || percent.isNaN() || percent.isInfinite()) {
        return ""
    }
    val formattedPercent = "%.2f".format(percent.roundTo(2)).changeWithSymbol
    return "($formattedPercent%)"
}

internal fun bindPortfolioSignedChange(
    views: RemoteViews,
    context: Context,
    iconId: Int,
    textId: Int,
    changeValue: Double?,
    changePercent: Double?,
    hideData: Boolean,
    isEmpty: Boolean,
) {
    if (hideData) {
        views.setViewVisibility(iconId, View.VISIBLE)
        views.setImageViewResource(iconId, R.drawable.trend_still)
        views.setInt(iconId, "setColorFilter", widgetOnSurfaceDim(context))
        views.setTextViewText(textId, stars)
        views.setTextColor(textId, widgetOnSurfaceDim(context))
        return
    }

    val hasValidChange = !isEmpty &&
        changeValue != null &&
        changePercent != null &&
        !changePercent.isNaN() &&
        !changePercent.isInfinite()

    if (!hasValidChange) {
        views.setViewVisibility(iconId, View.GONE)
        views.setTextViewText(textId, "—")
        views.setTextColor(textId, widgetOnSurfaceDim(context))
        return
    }

    val changeColor = changeValue.getWidgetPercentChangeColorPortfolio(context)
    val changeIcon = changeValue.getPercentChangeDrawablePortfolio()
    views.setViewVisibility(iconId, View.VISIBLE)
    views.setImageViewResource(iconId, changeIcon)
    views.setInt(iconId, "setColorFilter", changeColor)
    views.setTextViewText(textId, formatChangeValueAndPercent(changeValue, changePercent))
    views.setTextColor(textId, changeColor)
}

internal fun formatChangeValueAndPercent(changeValue: Double?, changePercent: Double?): String {
    if (changeValue == null) {
        return "—"
    }
    val valueText = changeValue.formatCurrency(threshold = FormattingThreshold.THOUSAND)
        .replace("-", "")
        .replace("+", "")
    val percentText = formatSignedChangePercent(changePercent)
    return if (percentText.isEmpty()) valueText else "$valueText $percentText"
}

internal fun setSmallPortfolioClickIntent(
    views: RemoteViews,
    context: Context,
) {
    val intent = Intent(context, MainActivity::class.java).apply {
        putExtra("cryptoID", "portfolio")
    }
    val pendingIntent = PendingIntent.getActivity(
        context,
        (0..10000).random(),
        intent,
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
    )
    views.setOnClickPendingIntent(R.id.btn_open, pendingIntent)
}

internal fun bindPortfolioPerformerRemoteViews(
    views: RemoteViews,
    context: Context,
    titleId: Int,
    symbolId: Int,
    coinImageId: Int,
    changeIconId: Int,
    changeTextId: Int,
    title: String,
    coinChange: CoinDailyChange?,
    coinBitmap: Bitmap?,
    hideData: Boolean,
    isEmpty: Boolean,
) {
    views.setTextViewText(titleId, title)
    views.setTextColor(titleId, widgetOnSurfaceDimmer(context))

    if (hideData) {
        views.setTextViewText(symbolId, stars)
        views.setTextColor(symbolId, widgetOnSurface(context))
        views.setImageViewResource(coinImageId, R.drawable.bitcoin)
        views.setViewVisibility(changeIconId, View.VISIBLE)
        views.setImageViewResource(changeIconId, R.drawable.trend_still)
        views.setInt(changeIconId, "setColorFilter", widgetOnSurfaceDim(context))
        views.setTextViewText(changeTextId, stars)
        views.setTextColor(changeTextId, widgetOnSurfaceDim(context))
        return
    }

    val hasValidData = !isEmpty &&
        coinChange != null &&
        coinChange.changePercent != null &&
        coinChange.changeValue != null &&
        !coinChange.changePercent.isNaN() &&
        !coinChange.changePercent.isInfinite()

    if (!hasValidData) {
        views.setTextViewText(symbolId, "—")
        views.setTextColor(symbolId, widgetOnSurfaceDim(context))
        views.setImageViewResource(coinImageId, R.drawable.bitcoin)
        views.setViewVisibility(changeIconId, View.GONE)
        views.setTextViewText(changeTextId, "—")
        views.setTextColor(changeTextId, widgetOnSurfaceDim(context))
        return
    }

    views.setTextViewText(symbolId, coinChange.symbol.ifEmpty { "—" })
    views.setTextColor(symbolId, widgetOnSurface(context))

    if (coinBitmap != null) {
        views.setImageViewBitmap(coinImageId, coinBitmap)
    } else {
        views.setImageViewResource(coinImageId, R.drawable.bitcoin)
    }

    val changeValue = coinChange.changeValue!!
    val changeColor = changeValue.getWidgetPercentChangeColorPortfolio(context)
    val changeIcon = changeValue.getPercentChangeDrawablePortfolio()
    views.setViewVisibility(changeIconId, View.VISIBLE)
    views.setImageViewResource(changeIconId, changeIcon)
    views.setInt(changeIconId, "setColorFilter", changeColor)

    views.setTextViewText(
        changeTextId,
        formatChangeValueAndPercent(changeValue, coinChange.changePercent),
    )
    views.setTextColor(changeTextId, changeColor)
}
