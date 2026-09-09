package com.cryptium.coincurrently.widgets.receivers

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.widget.RemoteViews
import androidx.core.content.ContextCompat
import com.cryptium.coincurrently.R
import com.cryptium.coincurrently.core.persistence.UserPreferences
import com.cryptium.coincurrently.features.Portfolio.PortfolioRepository
import com.cryptium.coincurrently.widgets.config.WidgetRuntimeInitializer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class medium_portfolio_widget : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        WidgetRuntimeInitializer.ensureInitialized(context)
        for (appWidgetId in appWidgetIds) {
            appWidgetManager.updateAppWidget(
                appWidgetId,
                buildPlaceholderMediumPortfolioWidget(context),
            )
            createMediumPortfolioWidget(context, appWidgetManager, appWidgetId)
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

internal fun createMediumPortfolioWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int,
) {
    WidgetRuntimeInitializer.ensureInitialized(context)

    if (UserPreferences.getIsHidePortfolioEnabled()) {
        val views = RemoteViews(context.packageName, R.layout.medium_portfolio_disabled_widget)
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

    val views = buildPlaceholderMediumPortfolioWidget(context)
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
            val loadedViews = RemoteViews(context.packageName, R.layout.medium_portfolio_widget)
            setSmallPortfolioClickIntent(loadedViews, context)

            bindMediumPortfolioCell(
                parent = loadedViews,
                containerId = R.id.cell_portfolio,
                widgetData = widgetData,
                context = context,
            )

            val hideData = UserPreferences.getHidePortfolioData()

            val winnerBitmap = if (!hideData) {
                loadCoinBitmap(context, widgetData.dailyWinner?.imageUrl)
            } else {
                null
            }
            val loserBitmap = if (!hideData) {
                loadCoinBitmap(context, widgetData.dailyLoser?.imageUrl)
            } else {
                null
            }

            bindPortfolioPerformerRemoteViews(
                views = loadedViews,
                context = context,
                titleId = R.id.txt_winner_title,
                symbolId = R.id.txt_winner_symbol,
                coinImageId = R.id.img_winner_coin,
                changeIconId = R.id.view_winner_change_icon,
                changeTextId = R.id.txt_winner_change,
                title = "Daily Winner",
                coinChange = widgetData.dailyWinner,
                coinBitmap = winnerBitmap,
                hideData = hideData,
                isEmpty = widgetData.isEmpty,
            )
            bindPortfolioPerformerRemoteViews(
                views = loadedViews,
                context = context,
                titleId = R.id.txt_loser_title,
                symbolId = R.id.txt_loser_symbol,
                coinImageId = R.id.img_loser_coin,
                changeIconId = R.id.view_loser_change_icon,
                changeTextId = R.id.txt_loser_change,
                title = "Daily Loser",
                coinChange = widgetData.dailyLoser,
                coinBitmap = loserBitmap,
                hideData = hideData,
                isEmpty = widgetData.isEmpty,
            )

            applyMediumPortfolioTheme(loadedViews, context)
            appWidgetManager.updateAppWidget(appWidgetId, loadedViews)
        } catch (_: Exception) {
            appWidgetManager.updateAppWidget(appWidgetId, buildPlaceholderMediumPortfolioWidget(context))
        }
    }
}

private fun buildPlaceholderMediumPortfolioWidget(context: Context): RemoteViews {
    return createPortfolioWidgetEmptyRemoteViews(context)
}

private fun applyMediumPortfolioTheme(views: RemoteViews, context: Context) {
    views.setInt(
        R.id.ll_medium_portfolio,
        "setBackgroundResource",
        if (isWidgetDarkMode(context)) {
            R.drawable.widget_background_dark
        } else {
            R.drawable.widget_background_light
        },
    )
    views.setInt(R.id.separator_vertical, "setBackgroundColor", widgetOutlineVariant(context))
    views.setTextColor(R.id.txt_winner_title, widgetOnSurfaceDimmer(context))
    views.setTextColor(R.id.txt_loser_title, widgetOnSurfaceDimmer(context))
}

fun getDominantColor(bitmap: Bitmap?, context: Context): Int {
    var red = 0
    var green = 0
    var blue = 0
    var pixelCount = 0

    for (y in 0..(bitmap?.height?.minus(1) ?: 0)) {
        for (x in 0..(bitmap?.width?.minus(1) ?: 0)) {
            val color = bitmap?.getPixel(x, y)

            if (color != null) {
                if (color in 0x1..0xfeffffff) {
                    pixelCount++
                    red += Color.red(color)
                    green += Color.green(color)
                    blue += Color.blue(color)
                }
            }
        }
    }

    if (pixelCount == 0) {
        return ContextCompat.getColor(context, R.color.gray)
    }

    return Color.rgb(red / pixelCount, green / pixelCount, blue / pixelCount)
}
