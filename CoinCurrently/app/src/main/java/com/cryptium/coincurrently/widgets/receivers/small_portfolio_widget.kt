package com.cryptium.coincurrently.widgets.receivers
import com.cryptium.coincurrently.core.persistence.UserPreferences
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.os.Bundle
import com.cryptium.coincurrently.core.config.AppLocale

/**
 * Implementation of App Widget functionality.
 */
class small_portfolio_widget : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            appWidgetManager.getAppWidgetInfo(appWidgetId)?.let { it ->
                it.provider?.let {
                    if (it.className.contains("small_portfolio_widget")) {
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

internal fun createSmallPortfolioWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int
) {

    /*if (UserPreferences.getIsHidePortfolioEnabled()) {
        val views = RemoteViews(context.packageName, R.layout.small_portfolio_disabled_widget)
        views.setInt(
            R.id.ll_view,
            "setBackgroundResource",
            if (UserPreferences.getIsWidgetDarkModeEnabled()) R.drawable.widget_background_dark else R.drawable.widget_background_light
        )
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

    val isDark = (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_YES) != 0
    val views = RemoteViews(context.packageName, R.layout.small_portfolio_widget)
    views.setInt(
        R.id.ll_view,
        "setBackgroundResource",
        if (isDark) R.drawable.widget_background_dark else R.drawable.widget_background_light
    )

    views.setTextColor(R.id.txt_total_title, widgetOnSurface(context))
    views.setTextColor(R.id.txt_total_value, widgetOnSurface(context))
    views.setTextColor(R.id.txt_timeframe, widgetOnSurface(context))
    views.setTextColor(R.id.txt_change, widgetOnSurfaceDim(context))
    views.setTextColor(R.id.txt_performer_title, widgetOnSurface(context))
    views.setTextColor(R.id.txt_symbol, widgetOnSurface(context))
    views.setTextColor(R.id.txt_performer_value, widgetOnSurface(context))
    views.setTextColor(R.id.txt_timeframe1, widgetOnSurface(context))
    views.setTextColor(R.id.txt_change1, widgetOnSurfaceDim(context))
    views.setImageViewResource(
        R.id.btn_reload,
        if (isDark) R.drawable.ic_baseline_sync_24_dark else R.drawable.ic_baseline_sync_24_light
    )
    views.setInt(
        R.id.btn_reload,
        "setBackgroundResource",
        if (isDark) R.drawable.widget_reload_button_bg_dark else R.drawable.widget_reload_button_bg_light
    )

    val handler = Handler()

    val coins: List<PortfolioResponse> = UserPreferences.getPortfolioCurrencies(context)

    val array: ArrayList<Pie> = arrayListOf()
    var totalValue = 0.0

    var willRedownload = false
    val hoverHandler = Runnable {
        createSmallPortfolioWidget(context, appWidgetManager, appWidgetId)
    }

    PortfolioRepository.portfolioModels = UserPreferences.getPortfolioCurrencies(context)

    GlobalScope.launch {
        PortfolioRepository.getPortfolioAllCurrencies()?.let { coinPrices ->

            downloadMultipleCurrencies(
                UserPreferences.getPortfolioCurrencies(context).map { it.id ?: "" }, context
            ) { currencies, bitmaps ->

                if (currencies?.isEmpty() == true) {
                    if (!willRedownload) {
                        willRedownload = true
                        handler.postDelayed(hoverHandler, 60000L)
                    }
                }

                UserPreferences.getPortfolioCurrencies(context).forEachIndexed { index, portfolioItem ->

                        val currentCurrency = currencies?.first { it.id == portfolioItem.id }
                        val color = if (bitmaps?.get(index)?.isRecycled == false) {
                            getDominantColor(bitmaps[index], context)
                        } else {
                            Color.GRAY
                        }

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

                        array.forEach {
                            val prices = coinPrices.get(it.currencyInfo?.id)
                            val price = prices?.get(it.currency?.lowercase())?.times(it.getCurrentAmount()) ?: 0.0

                            it.percent = price.div((totalValue)).times(100)
                            it.totalValue = price

                            if (UserPreferences.getShowChangesInPercent()) {
                               // it.getProfitPercent(prices, UserPreferences.getSelectedCurrency(context)?.code?.lowercase() ?: "usd")?.let { value ->
                                    it.profitLossPercent = it.getProfitPercent(prices, UserPreferences.getSelectedCurrency(context)?.code?.lowercase() ?: "usd") ?: 0.0
                               // }
                            } else {
                               // it.getProfitValue(prices, UserPreferences.getSelectedCurrency(context)?.code?.lowercase() ?: "usd")?.let { value ->
                                    it.profitLossPercent = it.getProfitValue(prices, UserPreferences.getSelectedCurrency(context)?.code?.lowercase() ?: "usd") ?: 0.0
                               // }
                            }
                        }

                        views.setTextViewText(
                            R.id.txt_total_value,
                            totalValue.formatCurrencyFromHundred(true, context)
                        )

                        val applicablePerformers: ArrayList<Pie> = arrayListOf()
                        for (pie in array) {
                            pie.profitLossPercent?.let {
                                applicablePerformers.add(pie)
                            }
                        }

                        val bestPerformer = applicablePerformers.let {
                            applicablePerformers.sortedBy { it.profitLossPercent }.reversed()
                        }

                        var currency = CurrencySelectionModel()

                        if (allCurrencies.isNotEmpty()) {
                            val currencies = allCurrencies.filter { currency -> currency.code == bestPerformer.first().currency?.uppercase() }
                            if (currencies.isNotEmpty()) {
                                currency = currencies.first()
                            }
                        }

                        if (bestPerformer.isNotEmpty()) {
                            bestPerformer.first().currencyInfo?.name?.let { it ->
                                views.setTextViewText(R.id.txt_symbol,
                                    it.replaceFirstChar {
                                        if (it.isLowerCase()) it.titlecase(
                                            AppLocale.enUsPosix
                                        ) else it.toString()
                                    })
                            }

                            val prices = coinPrices.get(bestPerformer.first().currencyInfo?.id)
                            val price = prices?.get(bestPerformer.first().currency?.lowercase())?.times(bestPerformer.first().getCurrentAmount()) ?: 0.0

                            price.formatCurrencyFromHundred(true, context, currencySelectionModel = currency).let {
                                views.setTextViewText(R.id.txt_performer_value, "Value: $it")
                            }

                            val weekChange = bestPerformer.first().getProfitPercent(prices, UserPreferences.getSelectedCurrency(context)?.code?.lowercase() ?: "usd", false) ?: 0.0
                            val value = bestPerformer.first().getProfitValue(prices, UserPreferences.getSelectedCurrency(context)?.code?.lowercase() ?: "usd") ?: 0.0
                            val realChange = if (UserPreferences.getShowChangesInPercent()) weekChange.getChangeWithSymbolPortfolio() else value.formatCurrencyFromHundred(
                                fromPortfolio = true,
                                fromPriceChange = true,
                                currencySelectionModel = UserPreferences.getSelectedCurrency(context)
                            ).replace("-", "").replace("+", "")

                            views.setTextViewText(R.id.txt_change1, realChange)

                            val changePercentColor = weekChange.getWidgetPercentChangeColorPortfolio(context)
                            val changeValueColor = value.getWidgetPercentChangeColorPortfolio(context)
                            val realChangeColor = if (UserPreferences.getShowChangesInPercent()) changePercentColor else changeValueColor

                            realChangeColor.let {
                                views.setTextColor(R.id.txt_change1, it)
                            }

                            val changePercentIcon = weekChange.getPercentChangeDrawablePortfolio()
                            val changeValueIcon = value.getPercentChangeDrawablePortfolio()
                            val realChangeIcon = if (UserPreferences.getShowChangesInPercent()) changePercentIcon else changeValueIcon

                            realChangeIcon.let {
                                views.setImageViewResource(R.id.view_price_change1, it)
                                views.setViewVisibility(
                                    R.id.view_price_change1,
                                    View.VISIBLE
                                )
                            }

                            views.setViewVisibility(R.id.view_price_change1, View.VISIBLE)

                            } else {
                                views.setTextViewText(R.id.txt_symbol, "-")
                                views.setTextViewText(
                                    R.id.txt_performer_value,
                                    "Value: ${
                                        0.0.formatCurrencyFromHundred(
                                            true,
                                            context,
                                            currencySelectionModel = currency
                                        )
                                    }"
                                )
                                views.setTextViewText(R.id.txt_change1, "N/A")
                                views.setTextColor(R.id.txt_change1, widgetOnSurfaceDim(context))
                                views.setViewVisibility(R.id.view_price_change1, View.GONE)
                            }

                            val totalProfitLoss = UserPreferences.getPortfolioProfitLossPercent(
                                array,
                                coinPrices,
                                UserPreferences.getSelectedCurrency(context)?.code ?: "usd"
                            )

                            val totalProfitLossValue = UserPreferences.getPortfolioProfitLossValue(
                                array,
                                coinPrices,
                                UserPreferences.getSelectedCurrency(context)?.code ?: "usd"
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

                            val changePercentColor = totalProfitLoss.getWidgetPercentChangeColorPortfolio(context)
                            val changeValueColor = totalProfitLossValue.getWidgetPercentChangeColorPortfolio(context)
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


                            val random = (0..10000).shuffled().first()
                            val intent = Intent(context, MainActivity::class.java)
                            intent.putExtra("cryptoID", "portfolio")

                            val pendingIntent: PendingIntent = PendingIntent.getActivity(
                                context,
                                random,
                                intent,
                                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                            )

                            views.setOnClickPendingIntent(R.id.btn_open, pendingIntent)

                            val random1 = (0..10000).shuffled().first()
                            val ids = appWidgetManager.getAppWidgetIds(
                                ComponentName(
                                    context,
                                    small_portfolio_widget::class.java
                                )
                            )
                            val updateIntent = Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE)
                            updateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)

                            val pendingIntent1: PendingIntent = PendingIntent.getBroadcast(
                                context,
                                random1,
                                updateIntent,
                                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                            )

                            views.setOnClickPendingIntent(R.id.btn_reload, pendingIntent1)

                            if (bestPerformer.isNotEmpty()) {
                                Glide.with(context)
                                    .asBitmap()
                                    .load(bestPerformer.first().currencyInfo?.image)
                                    .apply(RequestOptions().override(200, 200))
                                    .listener(object : RequestListener<Bitmap> {
                                        override fun onLoadFailed(
                                            e: GlideException?,
                                            model: Any?,
                                            target: Target<Bitmap>?,
                                            isFirstResource: Boolean
                                        ): Boolean {
                                            appWidgetManager.updateAppWidget(appWidgetId, views)
                                            return false
                                        }

                                        override fun onResourceReady(
                                            resource: Bitmap?,
                                            model: Any?,
                                            target: Target<Bitmap>?,
                                            dataSource: DataSource?,
                                            isFirstResource: Boolean
                                        ): Boolean {
                                            return try {

                                                if (resource?.isRecycled == false) {
                                                    views.setImageViewBitmap(
                                                        R.id.img_performer,
                                                        resource
                                                    )
                                                }

                                                appWidgetManager.updateAppWidget(appWidgetId, views)
                                                false
                                            } catch (e: Exception) {
                                                appWidgetManager.updateAppWidget(appWidgetId, views)
                                                false
                                            }
                                        }

                                    })
                                    .preload()
                            } else {
                                appWidgetManager.updateAppWidget(appWidgetId, views)
                            }
                        }
                    }
            }
        }
    }

    if (coins.isEmpty()) {
        val random1 = (0..10000).shuffled().first()
        val intent = Intent(context, MainActivity::class.java)
        intent.putExtra("cryptoID", "portfolio")

        val pendingIntent: PendingIntent = PendingIntent.getBroadcast(
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
                small_portfolio_widget::class.java
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
    }*/


}