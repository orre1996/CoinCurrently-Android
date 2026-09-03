package com.cryptium.coincurrently.widgets.receivers
import com.cryptium.coincurrently.core.persistence.UserPreferences
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import androidx.core.content.ContextCompat
import com.cryptium.coincurrently.*
import com.cryptium.coincurrently.core.networking.ResponseModels.*

/**
 * Implementation of App Widget functionality.
 */
class medium_portfolio_widget : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            appWidgetManager.getAppWidgetInfo(appWidgetId)?.let { it ->
                it.provider?.let {
                    if (it.className.contains("medium_portfolio_widget")) {
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

internal fun createMediumPortfolioWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int
) {
    /*val views = RemoteViews(context.packageName, R.layout.medium_portfolio_widget)
    views.setInt(R.id.ll_view, "setBackgroundResource", if(UserPreferences.getIsWidgetDarkModeEnabled()) R.drawable.widget_background_dark else  R.drawable.widget_background_light)

    views.setTextColor(R.id.txt_total_value, widgetOnSurface(context))
    views.setTextColor(R.id.txt_timeframe, widgetOnSurface(context))
    views.setTextColor(R.id.txt_change, widgetOnSurfaceDim(context))

    views.setTextColor(R.id.txt_symbol, widgetOnSurface(context))
    views.setTextColor(R.id.txt_value, widgetOnSurface(context))
    views.setTextColor(R.id.txt_ratio, widgetOnSurface(context))

    views.setInt(R.id.first_separator, "setBackgroundColor", widgetOutline(context))
    views.setInt(R.id.second_separator, "setBackgroundColor", widgetOutline(context))
    views.setInt(R.id.third_separator, "setBackgroundColor", widgetOutline(context))

    views.setImageViewResource(R.id.btn_reload, if(UserPreferences.getIsWidgetDarkModeEnabled()) R.drawable.ic_baseline_sync_24_dark else  R.drawable.ic_baseline_sync_24_light)
    views.setInt(R.id.btn_reload, "setBackgroundResource", if(UserPreferences.getIsWidgetDarkModeEnabled()) R.drawable.widget_reload_button_bg_dark else  R.drawable.widget_reload_button_bg_light)

    if (!UserPreferences.getHasBoughtPro()) {
        if (UserPreferences.getIsWidgetDarkModeEnabled()) {
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

    if (UserPreferences.getIsHidePortfolioEnabled()) {
        val isDark = (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_YES) != 0
        val views = RemoteViews(context.packageName, R.layout.medium_portfolio_disabled_widget)
        views.setInt(R.id.ll_view, "setBackgroundResource", if(isDark) R.drawable.widget_background_dark else  R.drawable.widget_background_light)
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

    val coins: List<PortfolioResponse> = UserPreferences.getPortfolioCurrencies(context)

    var array: ArrayList<Pie> = arrayListOf()
    var totalValue = 0.0

    var willRedownload = false
    val hoverHandler = Runnable {
        createMediumPortfolioWidget(context, appWidgetManager, appWidgetId)
    }

    PortfolioRepository.portfolioModels = UserPreferences.getPortfolioCurrencies(context)

    GlobalScope.launch {
        PortfolioRepository.getPortfolioAllCurrencies()?.let { coinPrices ->

            downloadMultipleCurrencies(
                UserPreferences.getPortfolioCurrencies(context).map { it.id ?: "" },
                context
            ) { currencies, bitmaps ->

                if (currencies?.isEmpty() == true) {
                    if (!willRedownload) {
                        willRedownload = true
                        handler.postDelayed(hoverHandler, 60000L)
                    }
                }

                UserPreferences.getPortfolioCurrencies(context).forEachIndexed { index, portfolioItem ->

                        val currentCurrency = currencies?.first { it.id == portfolioItem.id }

                        val color =
                            if (bitmaps?.get(index)?.isRecycled == false) getDominantColor(
                                bitmaps[index],
                                context
                            ) else Color.GRAY
                        array.add(
                            Pie(
                                currentCurrency,
                                portfolioItem.amount,
                                null,
                                color,
                                0,
                                entryPrice = portfolioItem.entryCost,
                                currency = UserPreferences.getSelectedCurrency(context)?.code ?: "USD",
                                buyTransactions = portfolioItem.buyTransactions ?: arrayListOf(),
                                sellTransactions = portfolioItem.sellTransactions ?: arrayListOf(),
                                inTransactions = portfolioItem.inTransferTransactions ?: arrayListOf(),
                                outTransactions = portfolioItem.outTransferTransactions ?: arrayListOf(),
                                importTransactions = portfolioItem.importTransactions ?: arrayListOf()
                            )
                        )

                        val prices = coinPrices.get(currentCurrency?.id)
                        val price = prices?.get(portfolioItem.currency?.lowercase())?.times(portfolioItem.getCurrentAmount()) ?: 0.0

                        totalValue += price

                        if (array.size == coins.size) {

                            views.removeAllViews(R.id.ll_coins)

                            array.forEach {
                                val price = prices?.get(it.currency?.lowercase())?.times(it.getCurrentAmount()) ?: 0.0
                                it.percent = price.div((totalValue)).times(100)
                            }

                            val totalProfitLoss = UserPreferences.getPortfolioProfitLossPercent(array, coinPrices, UserPreferences.getSelectedCurrency(context)?.code ?: "usd")
                            val totalProfitLossValue = UserPreferences.getPortfolioProfitLossValue(array, coinPrices, UserPreferences.getSelectedCurrency(context)?.code ?: "usd")

                            array = getPortfolio(array, coinPrices, context)

                            array.forEach {
                                val views1 =
                                    RemoteViews(
                                        context.packageName,
                                        R.layout.medium_portfolio_widget_item
                                    )

                                views1.setTextColor(R.id.txt_symbol, widgetOnSurface(context))
                                views1.setTextColor(R.id.txt_value, widgetOnSurface(context))
                                views1.setTextColor(R.id.txt_percent, widgetOnSurface(context))
                                views1.setInt(R.id.separator, "setBackgroundColor",
                                    widgetOutline(context)
                                )

                                if (it.currencyInfo != null) {
                                    views1.setInt(
                                        R.id.img_coin_color,
                                        "setBackgroundColor",
                                        it.color ?: 0
                                    )
                                    views1.setTextViewText(
                                        R.id.txt_symbol,
                                        it.currencyInfo?.symbol?.uppercase() ?: "-"
                                    )

                                    val price = coinPrices.get(it.currencyInfo?.id)?.get(it.currency?.lowercase())?.times(it.getCurrentAmount()) ?: 0.0

                                    var currency = CurrencySelectionModel()
                                    if (allCurrencies.isNotEmpty()) {
                                        val currencies = allCurrencies.filter { currency -> currency.code == it.currency?.uppercase() }
                                        if (currencies.isNotEmpty()) {
                                            currency = currencies.first()
                                        }
                                    }

                                    views1.setTextViewText(
                                        R.id.txt_value,
                                        if(it.currencyInfo?.symbol == "Other") {
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

                                    val percent = it.percent?.getChangeWithoutSymbol()
                                    views1.setTextViewText(R.id.txt_percent, "${percent ?: 0.00}")
                                } else {
                                    views1.setViewVisibility(R.id.ll_symbol, View.INVISIBLE)
                                    views1.setViewVisibility(R.id.txt_value, View.INVISIBLE)
                                    views1.setViewVisibility(R.id.txt_percent, View.INVISIBLE)
                                }

                                views.addView(R.id.ll_coins, views1)
                            }

                            for (i in array.size until max(array.size, 4)) {
                                val views1 = RemoteViews(
                                    context.packageName,
                                    R.layout.medium_portfolio_widget_item
                                )
                                views1.setViewVisibility(R.id.ll_symbol, View.INVISIBLE)
                                views1.setViewVisibility(R.id.txt_value, View.INVISIBLE)
                                views1.setViewVisibility(R.id.txt_percent, View.INVISIBLE)
                                views1.setInt(R.id.separator, "setBackgroundColor",
                                    widgetOutline(context)
                                )

                                views.addView(R.id.ll_coins, views1)
                            }

                            views.setTextViewText(
                                R.id.txt_total_value,
                                totalValue.formatCurrencyFromHundred(true, context)
                            )

                            val realChange = if (UserPreferences.getShowChangesInPercent()) totalProfitLoss.getChangeWithSymbolPortfolio() else totalProfitLossValue.formatCurrencyFromHundred(
                                fromPortfolio = true,
                                fromPriceChange = true,
                                currencySelectionModel = UserPreferences.getSelectedCurrency(context)
                            ).replace("-", "").replace("+", "")

                                views.setTextViewText(
                                    R.id.txt_change,
                                    realChange
                                )

                            val changePercentColor = totalProfitLoss.getWidgetPercentChangeColorPortfolio()
                            val changeValueColor = totalProfitLossValue.getWidgetPercentChangeColorPortfolio()
                            val realChangeColor = if (UserPreferences.getShowChangesInPercent()) changePercentColor else changeValueColor

                            val changePercentIcon = totalProfitLoss.getPercentChangeDrawablePortfolio()
                            val changeValueIcon = totalProfitLossValue.getPercentChangeDrawablePortfolio()
                            val realChangeIcon = if (UserPreferences.getShowChangesInPercent()) changePercentIcon else changeValueIcon

                            views.setTextColor(
                                R.id.txt_change,
                                realChangeColor
                            )

                            views.setImageViewResource(
                                R.id.view_price_change,
                                realChangeIcon
                            )

                            views.setViewVisibility(R.id.view_price_change, View.VISIBLE)

                            views.setViewPadding(R.id.img_pie, 0, 0, 0, 0)

                            val bitmap = drawPieChart(array, context, true)

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
                                    medium_portfolio_widget::class.java
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
                        medium_portfolio_widget::class.java
                    )
                )
                val updateIntent = Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE)
                updateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)

                val pendingIntent1 = PendingIntent.getBroadcast(
                        context,
                        random,
                        updateIntent,
                        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                    )

                views.setOnClickPendingIntent(R.id.btn_reload, pendingIntent1)

                appWidgetManager.updateAppWidget(appWidgetId, views)
            }
        }
    }*/
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

fun getPortfolio(currencies: ArrayList<Pie>, coinPrices: MutableMap<String, Map<String, Double>>, context: Context): ArrayList<Pie> {

    return arrayListOf()
    /*var newData = currencies.toMutableList()

    newData = newData.sortedBy { it.percent }.reversed().toMutableList()
    newData.reversed().forEach {
        val price = coinPrices.get(it.currencyInfo?.id)?.get(it.currency?.lowercase())?.times(it.getCurrentAmount()) ?: 0.0

        it.totalValue = price

        if (it.currencyInfo == null) {
            newData.remove(it)
        }
    }

    if (newData.size > 4) {
        val otherPies = newData.subList(3, newData.size)
        val totalValue = otherPies.map { it.currencyInfo?.current_price?.times(it.getCurrentAmount()) }.sumOf{ it ?: 0.0 }
        val totalPercent: Double = otherPies.map { it.percent }.sumOf { it ?: 0.0 }

        val tempList = arrayListOf<Pie>()
        tempList.addAll(otherPies)
        val profitLossPercent = UserPreferences.getPortfolioProfitLossPercent(tempList, coinPrices, UserPreferences.getSelectedCurrency(context)?.code ?: "usd")

        newData = newData.slice(0..2).toMutableList()
        newData.add(Pie(Toplist(null, "Other"), null, totalPercent, Color.rgb(97/255f, 105/255f, 218/255f), 0, totalValue, null, null, null, profitLossPercent))
    }

    val tempList = arrayListOf<Pie>()
    tempList.addAll(newData)
    return tempList*/
}