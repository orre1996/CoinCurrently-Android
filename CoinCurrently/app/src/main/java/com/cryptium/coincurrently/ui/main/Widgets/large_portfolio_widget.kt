package com.cryptium.coincurrently.ui.main.Widgets

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.res.Configuration
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.RemoteViews
import com.cryptium.coincurrently.*
import com.cryptium.coincurrently.ui.main.Activities.MainActivity
import com.cryptium.coincurrently.ui.main.General.GlobalFunctions
import com.cryptium.coincurrently.ui.main.Repositories.PortfolioRepository
import com.cryptium.coincurrently.ui.main.Extensions.formatCurrencyFromHundred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import networking.ResponseModels.*

/**
 * Implementation of App Widget functionality.
 */
class large_portfolio_widget : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            appWidgetManager.getAppWidgetInfo(appWidgetId)?.let { it ->
                it.provider?.let {
                    if (it.className.contains("large_portfolio_widget")) {
                        updateAppWidget(context, appWidgetManager, appWidgetId)
                    }
                }
            }
        }
    }

    override fun onAppWidgetOptionsChanged(
        context: Context?,
        appWidgetManager: AppWidgetManager?,
        appWidgetId: Int,
        newOptions: Bundle?
    ) {
        if (appWidgetManager != null) {
            if (context != null) {
                onUpdate(context, appWidgetManager, intArrayOf(appWidgetId))
            }
        }
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
    }
}

internal fun createLargePortfolioWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int
) {

    val isDark = (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_YES) != 0
    val views = RemoteViews(context.packageName, R.layout.large_portfolio_widget)

    views.setInt(R.id.ll_main, "setBackgroundResource", if(isDark) R.drawable.widget_background_dark else  R.drawable.widget_background_light)

    views.setTextColor(R.id.txt_total_value, widgetOnSurface(context))
    views.setTextColor(R.id.txt_timeframe, widgetOnSurface(context))
    views.setTextColor(R.id.txt_change, widgetOnSurfaceDim(context))

    views.setTextColor(R.id.txt_symbol, widgetOnSurface(context))
    views.setTextColor(R.id.txt_value, widgetOnSurface(context))
    views.setTextColor(R.id.txt_ratio, widgetOnSurface(context))
    views.setTextColor(R.id.txt_balance, widgetOnSurface(context))
    views.setTextColor(R.id.txt_PLN, widgetOnSurface(context))

    views.setInt(R.id.first_separator, "setBackgroundColor", widgetOutline(context))
    views.setInt(R.id.second_separator, "setBackgroundColor", widgetOutline(context))
    views.setInt(R.id.third_separator, "setBackgroundColor", widgetOutline(context))
    views.setInt(R.id.fourth_separator, "setBackgroundColor", widgetOutline(context))

    views.setImageViewResource(R.id.btn_reload, if(isDark) R.drawable.ic_baseline_sync_24_dark else  R.drawable.ic_baseline_sync_24_light)
    views.setInt(R.id.btn_reload, "setBackgroundResource", if(isDark) R.drawable.widget_reload_button_bg_dark else  R.drawable.widget_reload_button_bg_light)

    if (GlobalFunctions.getHasBoughtPro() == null) {
        if (isDark) {
            views.setViewVisibility(R.id.buy_pro, View.VISIBLE)
        } else {
            views.setViewVisibility(R.id.buy_pro_light, View.VISIBLE)
        }

        val random1 = (0..10000).shuffled().first()
        val intent = Intent(context, MainActivity::class.java)
        intent.putExtra("cryptoID", "buyPro")

        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context,
            random1,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        views.setOnClickPendingIntent(R.id.buy_pro, pendingIntent)
        views.setOnClickPendingIntent(R.id.buy_pro_light, pendingIntent)

        appWidgetManager.updateAppWidget(appWidgetId, views)
        return
    }

    if (GlobalFunctions.getIsHidePortfolioEnabled()) {
        val isDarkDisabled = (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_YES) != 0
        val views = RemoteViews(context.packageName, R.layout.large_portfolio_disabled_widget)
        views.setInt(R.id.ll_view, "setBackgroundResource", if(isDarkDisabled) R.drawable.widget_background_dark else  R.drawable.widget_background_light)
        views.setTextColor(R.id.txt_message, widgetOnSurface(context))

        val random = (0..10000).shuffled().first()
        val intent = Intent(context, MainActivity::class.java)

        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context,
            random,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        views.setOnClickPendingIntent(R.id.btn_open, pendingIntent)

        appWidgetManager.updateAppWidget(appWidgetId, views)
        return
    }

    val handler = Handler()

    val coins: List<PortfolioResponse> = GlobalFunctions.getPortfolioCurrencies(context)

    var array: ArrayList<Pie> = arrayListOf()
    var totalValue = 0.0

    var willRedownload = false
    val hoverHandler = Runnable {
        createLargePortfolioWidget(context, appWidgetManager, appWidgetId)
    }

    // PortfolioRepository.portfolioModels.value = GlobalFunctions.getPortfolioCurrencies(context)

    GlobalScope.launch {
    PortfolioRepository.getPortfolioAllCurrencies()?.let { coinPrices ->
        downloadMultipleCurrencies(GlobalFunctions.getPortfolioCurrencies(context).map { it.id ?: "" }, context) { currencies, bitmaps ->

            if (currencies?.isEmpty() == true) {
                if (!willRedownload) {
                    willRedownload = true
                    handler.postDelayed(hoverHandler, 60000L)
                }
            }

            GlobalFunctions.getPortfolioCurrencies(context).forEachIndexed { index, portfolioItem ->

                val currentCurrency = currencies?.firstOrNull { it.id == portfolioItem.id }
                val color = if (bitmaps?.get(index)?.isRecycled == false) getDominantColor(bitmaps[index], context) else Color.GRAY

                /*array.add(
                    Pie(
                        currentCurrency,
                        portfolioItem.
                    )
                )*/

                val prices = coinPrices.get(currentCurrency?.id)
                val price = prices?.get(portfolioItem.currency?.lowercase())?.times(portfolioItem.getCurrentAmount()) ?: 0.0

                totalValue += price

                if (array.size == coins.size) {

                    views.removeAllViews(R.id.ll_coins)

                    /*array.forEach {
                        val price = if (prices?.get(it.currency?.lowercase()).toString() == "null") 0.0 else prices?.get(it.currency?.lowercase()).toString().toDouble().times(it.getCurrentAmount())
                        it.percent = price.div((totalValue)).times(100)
                        it.weeklyChange = it.currencyInfo?.price_change_percentage_7d_in_currency ?: 0.00
                    }*/

                   // val totalProfitLoss = GlobalFunctions.getPortfolioProfitLossPercent(array, coinPrices, GlobalFunctions.getSelectedCurrency(context)?.code ?: "usd")
                    //val totalProfitLossValue = GlobalFunctions.getPortfolioProfitLossValue(array, coinPrices, GlobalFunctions.getSelectedCurrency(context)?.code ?: "usd")
                    array = getPortfolio(array, coinPrices, context)

                    array.forEach {
                        val views1 = RemoteViews(context.packageName, R.layout.large_portfolio_widget_item)

                        views1.setTextColor(R.id.txt_symbol, widgetOnSurface(context))
                        views1.setTextColor(R.id.txt_value, widgetOnSurface(context))
                        views1.setTextColor(R.id.txt_percent, widgetOnSurface(context))
                        views1.setTextColor(R.id.txt_balance, widgetOnSurface(context))
                        views1.setTextColor(R.id.txt_7d_change, widgetOnSurface(context))
                        views1.setInt(
                            R.id.separator,
                            "setBackgroundColor",
                            widgetOutline(context)
                        )

                        /*if (it.data != null) {
                            views1.setInt(
                                R.id.img_coin_color,
                                "setBackgroundColor",
                                it.color ?: 0
                            )
                            views1.setTextViewText(
                                R.id.txt_symbol,
                                it.currencyInfo?.symbol?.uppercase() ?: "-"
                            )

                            val prices = coinPrices.get(it.currencyInfo?.id)

                            val prefix = if((it.getProfitValue(prices, GlobalFunctions.getSelectedCurrency(context)?.code?.lowercase() ?: "usd") ?: 0.0) > 0.0) "+" else ""

                            views1.setTextViewText(R.id.txt_7d_change,

                            if(GlobalFunctions.getShowChangesInPercent()) {
                                (it.getProfitPercent(prices, GlobalFunctions.getSelectedCurrency(context)?.code?.lowercase() ?: "usd", false) ?: 0.0).getChangeWithSymbol(true, currentPrice = it.totalValue, context = context)
                            } else {
                                prefix + it.getProfitValue(prices, GlobalFunctions.getSelectedCurrency(context)?.code?.lowercase() ?: "usd")?.formatCurrencyFromThousand(true, context = context)
                            })

                            val changePercentColor = it.getProfitPercent(prices, GlobalFunctions.getSelectedCurrency(context)?.code?.lowercase() ?: "usd", true)?.getWidgetPercentChangeColorPortfolio()
                            val changeValueColor = it.getProfitValue(prices, GlobalFunctions.getSelectedCurrency(context)?.code?.lowercase() ?: "usd")?.getWidgetPercentChangeColorPortfolio()
                            val realChangeColor = if (GlobalFunctions.getShowChangesInPercent()) changePercentColor else changeValueColor

                            views1.setTextColor(R.id.txt_7d_change, realChangeColor ?: widgetOnSurfaceDim(context))

                            val price = prices?.get(it.currency?.lowercase())?.times(it.getCurrentAmount()) ?: 0.0

                            var currency = CurrencySelectionModel()
                            if (allCurrencies.isNotEmpty()) {
                                val currencies = allCurrencies.filter { currency -> currency.code == it.currency?.uppercase() }
                                if (currencies.isNotEmpty()) {
                                    currency = currencies.first()
                                }
                            }

                            views1.setTextViewText(
                                R.id.txt_value,
                                if (it.currencyInfo?.symbol == "Other") {
                                    it.totalValue.formatCurrencyFromThousand(
                                        true,
                                        context,
                                        currencySelectionModel = currency
                                    )
                                } else {
                                    price.formatCurrencyFromThousand(
                                        true,
                                        context,
                                        currencySelectionModel = currency
                                    )
                                }
                            )

                            views1.setTextViewText(
                                R.id.txt_balance,
                                it.getCurrentAmount().formatCurrencyNoCurrency(true, context)
                            )

                            val percent = it.percent?.getChangeWithoutSymbol()
                            views1.setTextViewText(R.id.txt_percent, "${percent ?: 0.00}")
                        } else {
                            views1.setViewVisibility(R.id.ll_symbol, View.INVISIBLE)
                            views1.setViewVisibility(R.id.txt_value, View.INVISIBLE)
                            views1.setViewVisibility(R.id.txt_percent, View.INVISIBLE)
                            views1.setViewVisibility(R.id.txt_balance, View.INVISIBLE)
                            views1.setViewVisibility(R.id.txt_7d_change, View.INVISIBLE)
                            views1.setInt(
                                R.id.separator,
                                "setBackgroundColor",
                                widgetOutline(context)
                            )
                        }*/

                        views.addView(R.id.ll_coins, views1)
                    }

                    for (i in array.size until Integer.max(array.size, 4)) {
                        val views1 =
                            RemoteViews(
                                context.packageName,
                                R.layout.large_portfolio_widget_item
                            )

                        views1.setViewVisibility(R.id.ll_symbol, View.INVISIBLE)
                        views1.setViewVisibility(R.id.txt_value, View.INVISIBLE)
                        views1.setViewVisibility(R.id.txt_percent, View.INVISIBLE)
                        views1.setViewVisibility(R.id.txt_balance, View.INVISIBLE)
                        views1.setViewVisibility(R.id.txt_7d_change, View.INVISIBLE)
                        views1.setInt(
                            R.id.separator,
                            "setBackgroundColor",
                            widgetOutline(context)
                        )

                        views.addView(R.id.ll_coins, views1)
                    }

                    views.setTextViewText(
                        R.id.txt_total_value,
                        totalValue.formatCurrencyFromHundred(true, context)
                    )

                    /*val changePercentColor = totalProfitLoss.getWidgetPercentChangeColorPortfolio()
                    val changeValueColor = totalProfitLossValue.getWidgetPercentChangeColorPortfolio()
                    val realChangeColor = if (GlobalFunctions.getShowChangesInPercent()) changePercentColor else changeValueColor

                    val changePercentIcon = totalProfitLoss.getPercentChangeDrawablePortfolio()
                    val changeValueIcon = totalProfitLossValue.getPercentChangeDrawablePortfolio()
                    val realChangeIcon = if (GlobalFunctions.getShowChangesInPercent()) changePercentIcon else changeValueIcon

                    val realChange = if (GlobalFunctions.getShowChangesInPercent()) totalProfitLoss.getChangeWithSymbolPortfolio() else totalProfitLossValue.formatCurrencyFromHundred(
                        fromPortfolio = true,
                        fromPriceChange = true,
                        currencySelectionModel = GlobalFunctions.getSelectedCurrency(context)
                    ).replace("-", "").replace("+", "")

                    views.setTextViewText(R.id.txt_change, realChange)
                    views.setTextColor(R.id.txt_change, realChangeColor)
                    views.setImageViewResource(R.id.view_price_change, realChangeIcon)
                    views.setViewVisibility(R.id.view_price_change, View.VISIBLE)*/

                    views.setViewPadding(R.id.img_pie, 0, 0, 0, 0)

                    val bitmap = drawPieChart(array, context, false)

                    if (bitmap?.isRecycled == false) {
                        views.setImageViewBitmap(R.id.img_pie, bitmap)
                    }

                    val random1 = (0..10000).shuffled().first()
                    val intent = Intent(context, MainActivity::class.java)
                    intent.putExtra("cryptoID", "portfolio")

                    val pendingIntent: PendingIntent = PendingIntent.getActivity(
                        context,
                        random1,
                        intent,
                        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                    )

                    views.setOnClickPendingIntent(R.id.btn_open, pendingIntent)

                    val random = (0..10000).shuffled().first()
                    val ids = appWidgetManager.getAppWidgetIds(
                        ComponentName(
                            context,
                            large_portfolio_widget::class.java
                        )
                    )
                    val updateIntent = Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE)
                    updateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)

                    val pendingIntent1: PendingIntent = PendingIntent.getBroadcast(
                        context,
                        random,
                        updateIntent,
                        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                    )

                    views.setOnClickPendingIntent(R.id.btn_reload, pendingIntent1)

                    appWidgetManager.updateAppWidget(appWidgetId, views)
                }
            }
        }

        if (coins.isEmpty()) {
            val intent = Intent(context, MainActivity::class.java)
            intent.putExtra("cryptoID", "portfolio")

            val pendingIntent: PendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            views.setOnClickPendingIntent(R.id.btn_open, pendingIntent)

            val random = (0..10000).shuffled().first()
            val ids = appWidgetManager.getAppWidgetIds(
                ComponentName(
                    context,
                    large_portfolio_widget::class.java
                )
            )
            val updateIntent = Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE)
            updateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)

            val pendingIntent1: PendingIntent =
                PendingIntent.getActivity(
                    context,
                    random,
                    updateIntent,
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )

            views.setOnClickPendingIntent(R.id.btn_reload, pendingIntent1)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
    }
}