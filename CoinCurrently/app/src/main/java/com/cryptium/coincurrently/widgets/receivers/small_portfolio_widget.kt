package com.cryptium.coincurrently.widgets.receivers

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
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
import kotlin.math.roundToInt
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
                threshold = FormattingThreshold.MILLION,
                currency = selectedCurrency,
            )
        },
    )

    bindPortfolioSignedChange(
        views = views,
        context = context,
        iconId = R.id.view_price_change,
        textId = R.id.txt_pl_change,
        percentTextId = R.id.txt_pl_percent,
        changeValue = widgetData.portfolioDailyChangeValue,
        changePercent = widgetData.portfolioDailyChangePercent,
        hideData = hideData,
        isEmpty = widgetData.isEmpty,
    )
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
    percentTextId: Int,
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
        views.setViewVisibility(percentTextId, View.GONE)
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
        views.setViewVisibility(percentTextId, View.GONE)
        return
    }

    val changeColor = changeValue.getWidgetPercentChangeColorPortfolio(context)
    val changeIcon = changeValue.getPercentChangeDrawablePortfolio()
    views.setViewVisibility(iconId, View.VISIBLE)
    views.setImageViewResource(iconId, changeIcon)
    views.setInt(iconId, "setColorFilter", changeColor)
    views.setTextViewText(textId, formatChangeValue(changeValue))
    views.setTextColor(textId, changeColor)

    val percentText = formatSignedChangePercent(changePercent)
    if (percentText.isEmpty()) {
        views.setViewVisibility(percentTextId, View.GONE)
    } else {
        views.setViewVisibility(percentTextId, View.VISIBLE)
        views.setTextViewText(percentTextId, percentText)
        views.setTextColor(percentTextId, widgetChangePercentColor(changeColor))
    }
}

internal fun formatChangeValue(changeValue: Double?): String {
    if (changeValue == null) {
        return "—"
    }
    return changeValue.formatCurrency(threshold = FormattingThreshold.THOUSAND)
        .replace("-", "")
        .replace("+", "")
}

internal fun widgetChangePercentColor(color: Int): Int {
    return Color.argb(
        (Color.alpha(color) * PORTFOLIO_WIDGET_CHANGE_PERCENT_OPACITY).roundToInt().coerceIn(0, 255),
        Color.red(color),
        Color.green(color),
        Color.blue(color),
    )
}

private const val PORTFOLIO_WIDGET_CHANGE_PERCENT_OPACITY = 0.75f

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
    percentTextId: Int,
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
        bindPortfolioSignedChange(
            views = views,
            context = context,
            iconId = changeIconId,
            textId = changeTextId,
            percentTextId = percentTextId,
            changeValue = null,
            changePercent = null,
            hideData = true,
            isEmpty = isEmpty,
        )
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
        bindPortfolioSignedChange(
            views = views,
            context = context,
            iconId = changeIconId,
            textId = changeTextId,
            percentTextId = percentTextId,
            changeValue = null,
            changePercent = null,
            hideData = false,
            isEmpty = true,
        )
        return
    }

    views.setTextViewText(symbolId, coinChange.symbol.ifEmpty { "—" })
    views.setTextColor(symbolId, widgetOnSurface(context))

    if (coinBitmap != null) {
        views.setImageViewBitmap(coinImageId, coinBitmap)
    } else {
        views.setImageViewResource(coinImageId, R.drawable.bitcoin)
    }

    bindPortfolioSignedChange(
        views = views,
        context = context,
        iconId = changeIconId,
        textId = changeTextId,
        percentTextId = percentTextId,
        changeValue = coinChange.changeValue,
        changePercent = coinChange.changePercent,
        hideData = false,
        isEmpty = isEmpty,
    )
}
