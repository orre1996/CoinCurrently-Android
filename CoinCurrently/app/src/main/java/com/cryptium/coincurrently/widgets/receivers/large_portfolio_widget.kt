package com.cryptium.coincurrently.widgets.receivers

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.widget.RemoteViews
import com.cryptium.coincurrently.R
import com.cryptium.coincurrently.core.extensions.formatCurrency
import com.cryptium.coincurrently.core.extensions.stars
import com.cryptium.coincurrently.core.persistence.UserPreferences
import com.cryptium.coincurrently.features.Portfolio.PortfolioRepository
import com.cryptium.coincurrently.sharedui.models.FormattingThreshold
import com.cryptium.coincurrently.widgets.config.WidgetRuntimeInitializer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class large_portfolio_widget : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        WidgetRuntimeInitializer.ensureInitialized(context)
        for (appWidgetId in appWidgetIds) {
            appWidgetManager.updateAppWidget(
                appWidgetId,
                buildPlaceholderLargePortfolioWidget(context),
            )
            createLargePortfolioWidget(context, appWidgetManager, appWidgetId)
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

internal fun createLargePortfolioWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int,
) {
    WidgetRuntimeInitializer.ensureInitialized(context)

    if (UserPreferences.getIsHidePortfolioEnabled()) {
        val views = RemoteViews(context.packageName, R.layout.large_portfolio_disabled_widget)
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

    val views = buildPlaceholderLargePortfolioWidget(context)
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
            val loadedViews = createLargePortfolioRemoteViews(
                context = context,
                widgetData = widgetData,
            )
            appWidgetManager.updateAppWidget(appWidgetId, loadedViews)
        } catch (_: Exception) {
            appWidgetManager.updateAppWidget(appWidgetId, buildPlaceholderLargePortfolioWidget(context))
        }
    }
}

private fun buildPlaceholderLargePortfolioWidget(context: Context): RemoteViews {
    return createPortfolioWidgetEmptyRemoteViews(context)
}

private suspend fun createLargePortfolioRemoteViews(
    context: Context,
    widgetData: PortfolioWidgetData,
): RemoteViews {
    val views = RemoteViews(context.packageName, R.layout.large_portfolio_widget)
    val hideData = UserPreferences.getHidePortfolioData()
    val selectedCurrency = UserPreferences.getSelectedCurrency(context)

    applyLargePortfolioTheme(views, context)

    views.setTextViewText(
        R.id.txt_total_value,
        when {
            hideData -> stars
            widgetData.totalValue == null -> "—"
            else -> widgetData.totalValue.formatCurrency(
                threshold = FormattingThreshold.BILLION,
                currency = selectedCurrency,
            )
        },
    )

    bindPortfolioSignedChange(
        views = views,
        context = context,
        iconId = R.id.view_today_change,
        textId = R.id.txt_today_change,
        percentTextId = R.id.txt_today_percent,
        changeValue = widgetData.portfolioDailyChangeValue,
        changePercent = widgetData.portfolioDailyChangePercent,
        hideData = hideData,
        isEmpty = widgetData.isEmpty,
    )
    bindPortfolioSignedChange(
        views = views,
        context = context,
        iconId = R.id.view_pl_change,
        textId = R.id.txt_pl_change,
        percentTextId = R.id.txt_pl_percent,
        changeValue = widgetData.profitLossValue,
        changePercent = widgetData.profitLossPercent,
        hideData = hideData,
        isEmpty = widgetData.isEmpty,
    )

    views.removeAllViews(R.id.ll_coins)
    widgetData.topCoinsByProfitLoss.forEach { coin ->
        val rowViews = RemoteViews(context.packageName, R.layout.large_portfolio_widget_item)
        val coinBitmap = if (!hideData) loadCoinBitmap(context, coin.imageUrl) else null
        bindLargePortfolioCoinRow(
            views = rowViews,
            context = context,
            coin = coin,
            coinBitmap = coinBitmap,
            hideData = hideData,
            isEmpty = widgetData.isEmpty,
        )
        views.addView(R.id.ll_coins, rowViews)
    }

    setSmallPortfolioClickIntent(views, context)
    return views
}

private fun bindLargePortfolioCoinRow(
    views: RemoteViews,
    context: Context,
    coin: CoinDailyChange,
    coinBitmap: Bitmap?,
    hideData: Boolean,
    isEmpty: Boolean,
) {
    if (hideData) {
        views.setTextViewText(R.id.txt_symbol, stars)
        views.setTextColor(R.id.txt_symbol, widgetOnSurface(context))
        views.setImageViewResource(R.id.img_coin, R.drawable.bitcoin)
        bindPortfolioSignedChange(
            views = views,
            context = context,
            iconId = R.id.view_change_icon,
            textId = R.id.txt_change,
            percentTextId = R.id.txt_change_percent,
            changeValue = null,
            changePercent = null,
            hideData = true,
            isEmpty = isEmpty,
        )
        return
    }

    views.setTextViewText(R.id.txt_symbol, coin.symbol.ifEmpty { "—" })
    views.setTextColor(R.id.txt_symbol, widgetOnSurface(context))
    if (coinBitmap != null) {
        views.setImageViewBitmap(R.id.img_coin, coinBitmap)
    } else {
        views.setImageViewResource(R.id.img_coin, R.drawable.bitcoin)
    }

    bindPortfolioSignedChange(
        views = views,
        context = context,
        iconId = R.id.view_change_icon,
        textId = R.id.txt_change,
        percentTextId = R.id.txt_change_percent,
        changeValue = coin.changeValue,
        changePercent = coin.changePercent,
        hideData = false,
        isEmpty = isEmpty,
    )
}

private fun applyLargePortfolioTheme(views: RemoteViews, context: Context) {
    views.setInt(
        R.id.ll_large_portfolio,
        "setBackgroundResource",
        if (isWidgetDarkMode(context)) {
            R.drawable.widget_background_dark
        } else {
            R.drawable.widget_background_light
        },
    )
    views.setInt(R.id.separator_horizontal, "setBackgroundColor", widgetOutlineVariant(context))
    views.setTextColor(R.id.txt_portfolio_title, widgetOnSurfaceDimmer(context))
    views.setTextColor(R.id.txt_total_value, widgetOnSurface(context))
    views.setTextColor(R.id.txt_today_title, widgetOnSurfaceDimmer(context))
    views.setTextColor(R.id.txt_pl_title, widgetOnSurfaceDimmer(context))
    views.setTextColor(R.id.txt_coin_header, widgetOnSurfaceDimmer(context))
    views.setTextColor(R.id.txt_pl_header, widgetOnSurfaceDimmer(context))
}
