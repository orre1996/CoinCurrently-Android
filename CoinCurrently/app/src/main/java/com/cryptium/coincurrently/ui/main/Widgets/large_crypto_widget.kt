package com.cryptium.coincurrently.ui.main.Widgets

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.*
import android.os.Bundle
import android.view.View
import android.widget.RemoteViews
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.cryptium.coincurrently.*
import com.cryptium.coincurrently.ui.main.Activities.MainActivity
import com.cryptium.coincurrently.ui.main.MarketTab.CryptoDetail.ViewModel.CryptoDetailViewModel
import com.cryptium.coincurrently.ui.main.CustomViews.getChangeWithSymbol
import com.cryptium.coincurrently.ui.main.CustomViews.getFormattedCurrencyString
import com.cryptium.coincurrently.ui.main.CustomViews.getWidgetChangeColor
import com.cryptium.coincurrently.ui.main.Extensions.formatCurrency
import com.cryptium.coincurrently.ui.main.Extensions.getSelectedCurrency
import com.cryptium.coincurrently.ui.main.Extensions.is24HourClock
import com.cryptium.coincurrently.ui.main.Extensions.normalise
import com.cryptium.coincurrently.ui.main.Extensions.roundToNextWholeHour
import com.cryptium.coincurrently.ui.main.Repositories.CurrencyRepository
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import networking.ResponseModels.PriceVolumeModel
import networking.ResponseModels.getPreferredChange
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt
import com.cryptium.coincurrently.ui.main.General.GlobalFunctions
import com.cryptium.coincurrently.ui.main.General.NavigationViewModel
import com.cryptium.coincurrently.ui.main.General.PreferredTimeframe
import com.cryptium.coincurrently.ui.main.Repositories.StoreKitManager
import com.cryptium.coincurrently.ui.main.Managers.StorageManager
import kotlinx.coroutines.DelicateCoroutinesApi
import networking.RetrofitBuilder

/**
 * Implementation of App Widget functionality.
 */

lateinit var viewModel: CryptoDetailViewModel

class large_crypto_widget : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {

        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            appWidgetManager.getAppWidgetInfo(appWidgetId)?.let { it ->
                it.provider?.let {
                    if (it.className.contains("large_crypto_widget")) {
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

internal fun updateAppWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int
) {
    NavigationViewModel.currentScreen = "General"
    RetrofitBuilder.createApiService(context, "Widget")
    StoreKitManager.setUpBillingClient(context)
    StorageManager.setCache(context)

    appWidgetManager.getAppWidgetInfo(appWidgetId)?.let { it ->
        it.provider?.let {

            when {
                it.className.contains("small_fear_greed_widget") -> {
                    createSmallFearWidget(context, appWidgetManager, appWidgetId)
                }
                it.className.contains("SmallMultiCryptoWidgetReceiver") -> {
                    // Glance handles its own update
                }
                it.className.contains("MediumMultiCryptoWidgetReceiver") -> {
                    // Glance handles its own update
                }
                it.className.contains("LargeMultiCryptoWidgetReceiver") -> {
                    // Glance handles its own update
                }
                it.className.contains("SmallNewsWidgetReceiver") -> {
                    // Glance handles its own update
                }
                it.className.contains("MediumNewsWidgetReceiver") -> {
                    // Glance handles its own update
                }
                it.className.contains("LargeNewsWidgetReceiver") -> {
                    // Glance handles its own update
                }
                it.className.contains("small_crypto_widget") -> {
                    createSmallWidget(context, appWidgetManager, appWidgetId)
                }
                it.className.contains("SmallCryptoWidgetReceiver") -> {
                    // Glance handles its own update
                }
                it.className.contains("MediumCryptoWidgetReceiver") -> {
                    // Glance handles its own update
                }
                it.className.contains("MediumDuoCryptoWidgetReceiver") -> {
                    // Glance handles its own update
                }
                it.className.contains("LargeDuoCryptoWidgetReceiver") -> {
                    // Glance handles its own update
                }
                it.className.contains("LargeQuadCryptoWidgetReceiver") -> {
                    // Glance handles its own update
                }
                it.className.contains("small_portfolio_widget") -> {
                    createSmallPortfolioWidget(context, appWidgetManager, appWidgetId)
                }
                it.className.contains("medium_portfolio_widget") -> {
                    createMediumPortfolioWidget(context, appWidgetManager, appWidgetId)
                }
                it.className.contains("large_portfolio_widget") -> {
                    createLargePortfolioWidget(context, appWidgetManager, appWidgetId)
                }
                else -> {
                    createLargeWidget(context, appWidgetManager, appWidgetId)
                }
            }
        }
    }
}

@OptIn(DelicateCoroutinesApi::class)
internal fun createLargeWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int
) {
    val isDark = (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_YES) != 0
    val views = RemoteViews(context.packageName, R.layout.large_crypto_widget)
    views.setInt(R.id.ll_main, "setBackgroundResource", if(isDark) R.drawable.widget_background_dark else  R.drawable.widget_background_light)

    views.setTextColor(R.id.txt_currency_rank, widgetOnSurface(context))
    views.setTextColor(R.id.txt_currency_price, widgetOnSurface(context))

    views.setTextColor(R.id.txt_timeframe_start, widgetOnSurface(context))
    views.setTextColor(R.id.txt_timeframe_middle, widgetOnSurface(context))
    views.setTextColor(R.id.txt_timeframe_end, widgetOnSurface(context))

    views.setTextColor(R.id.txt_open_title, widgetOnSurface(context))
    views.setTextColor(R.id.txt_open_value, widgetOnSurface(context))
    views.setTextColor(R.id.txt_high_title, widgetOnSurface(context))
    views.setTextColor(R.id.txt_high_value, widgetOnSurface(context))
    views.setTextColor(R.id.txt_low_title, widgetOnSurface(context))
    views.setTextColor(R.id.txt_low_value, widgetOnSurface(context))

    views.setTextColor(R.id.txt_price_high, widgetOnSurface(context))
    views.setTextColor(R.id.txt_price_middle, widgetOnSurface(context))
    views.setTextColor(R.id.txt_price_low, widgetOnSurface(context))

    views.setInt(R.id.txt_price_high, "setBackgroundResource", if(isDark) R.drawable.widget_number_background_dark else R.drawable.widget_number_background_light)
    views.setInt(R.id.txt_price_middle, "setBackgroundResource", if(isDark) R.drawable.widget_number_background_dark else R.drawable.widget_number_background_light)
    views.setInt(R.id.txt_price_low, "setBackgroundResource", if(isDark) R.drawable.widget_number_background_dark else R.drawable.widget_number_background_light)

    views.setInt(R.id.view_separator, "setBackgroundColor", widgetOnSurfaceDim(context))

    views.setInt(R.id.chart_vertical_start, "setBackgroundColor", widgetOutline(context))
    views.setInt(R.id.chart_vertical_middle, "setBackgroundColor", widgetOutline(context))
    views.setInt(R.id.chart_vertical_end, "setBackgroundColor", widgetOutline(context))
    views.setInt(R.id.chart_horizontal_top, "setBackgroundColor", widgetOutline(context))
    views.setInt(R.id.chart_horizontal_middle, "setBackgroundColor", widgetOutline(context))
    views.setInt(R.id.chart_horizontal_bottom, "setBackgroundColor", widgetOutline(context))

    views.setTextColor(R.id.txt_volume_title, widgetOnSurface(context))
    views.setTextColor(R.id.txt_volume_value, widgetOnSurface(context))
    views.setTextColor(R.id.txt_mkt_cap_title, widgetOnSurface(context))
    views.setTextColor(R.id.txt_mkt_cap_value, widgetOnSurface(context))
    views.setTextColor(R.id.txt_change_title, widgetOnSurface(context))
    views.setTextColor(R.id.txt_change_value, widgetOnSurface(context))

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

    if (GlobalFunctions.getWidgetCurrency(context).isEmpty()) {
        appWidgetManager.updateAppWidget(appWidgetId, views)
        return
    }

    GlobalScope.launch {
        CurrencyRepository.getCryptoDetail(GlobalFunctions.getWidgetCurrency(context).first().id ?: "", true)?.let { it ->
            GlobalScope.launch {
                CurrencyRepository.getPriceVolume(GlobalFunctions.getWidgetCurrency(context).first().id ?: "", GlobalFunctions.getPreferredTimeframeValue(), context, true)?.let { priceModel ->

                    if (!it.symbol.isNullOrEmpty()) {

                        views.setTextViewText(R.id.txt_currency_rank, "${it.name}")
                        views.setTextViewText(
                            R.id.txt_currency_price,
                            it.market_data?.current_price?.getSelectedCurrency(context)?.getFormattedCurrencyString(true, context)
                        )

                        val prices = priceModel.prices?.map { it.last() }

                        views.setTextViewText(
                            R.id.txt_open_value,
                            prices?.first()?.formatCurrency(true, true, context)
                        )
                        views.setTextViewText(
                            R.id.txt_high_value,
                            prices?.maxOfOrNull { it ?: 0.0 }?.formatCurrency(true, true, context)
                        )
                        views.setTextViewText(
                            R.id.txt_low_value,
                            prices?.minOfOrNull { it ?: 0.0 }?.formatCurrency(true, true, context)
                        )

                        views.setTextViewText(
                            R.id.txt_volume_value,
                            it.market_data?.total_volume?.getSelectedCurrency(context)
                                ?.formatCurrency(true, true, context)
                        )
                        views.setTextViewText(
                            R.id.txt_mkt_cap_value,
                            it.market_data?.market_cap?.getSelectedCurrency(context)
                                ?.formatCurrency(true, true, context)
                        )
                        views.setTextViewText(
                            R.id.txt_change_value,
                            it.market_data?.getPreferredChange()?.getChangeWithSymbol(
                                true
                            )
                        )

                        views.setTextViewText(R.id.txt_timeframe_start, getTimeChartDates(1, context))
                        views.setTextViewText(R.id.txt_timeframe_middle, getTimeChartDates(2, context))
                        views.setTextViewText(R.id.txt_timeframe_end, getTimeChartDates(3, context))

                        views.setTextViewText(
                            R.id.txt_price_high,
                            setChartPrices(prices ?: listOf(), 3, context)
                        )
                        views.setTextViewText(
                            R.id.txt_price_middle,
                            setChartPrices(prices ?: listOf(), 2, context)
                        )
                        views.setTextViewText(
                            R.id.txt_price_low,
                            setChartPrices(prices ?: listOf(), 1, context)
                        )

                        views.removeAllViews(R.id.ll_volume_chart)

                        val realVolumes = priceModel.total_volumes?.map { it.last() }
                        val normalized = realVolumes?.map { it ->
                            normalise(
                                it?.toFloat() ?: 0f,
                                realVolumes.minOfOrNull { it ?: 0.0 }?.toFloat() ?: 0.0f,
                                realVolumes.maxOfOrNull { it ?: 0.0 }?.toFloat() ?: 0.0f
                            )
                        }

                        val trimmed = getTrimmedVolume(normalized!!)

                        trimmed.forEachIndexed { index, volume ->

                            val views1 = RemoteViews(context.packageName, R.layout.volume_candle)

                            if (index > 0) {
                                if (volume < trimmed[index - 1])
                                    views1.setInt(
                                        R.id.img_test,
                                        "setBackgroundResource",
                                        if (isDark) R.drawable.volume_candle_border_price_down_dark else
                                            R.drawable.volume_candle_border_price_down_light
                                    )
                                else views1.setInt(
                                    R.id.img_test,
                                    "setBackgroundResource",
                                    if (isDark) R.drawable.volume_candle_border_price_up_dark else
                                        R.drawable.volume_candle_border_price_up_light
                                )

                            } else {
                                views1.setInt(
                                    R.id.img_test,
                                    "setBackgroundResource",
                                    if (isDark) R.drawable.volume_candle_border_price_up_dark else
                                        R.drawable.volume_candle_border_price_up_light
                                )
                            }

                            views1.setViewPadding(
                                R.id.view_test,
                                0,
                                ((((75) * (1 - volume)) + 0.1).toInt()),
                                0,
                                0
                            )

                            views.addView(R.id.ll_volume_chart, views1)
                        }


                        val bitmap = drawGraphPaths(
                            priceModel,
                            context,
                            600,
                            1625,
                            it.market_data?.getPreferredChange() ?: 0.0
                        )

                        if (bitmap?.isRecycled == false) {
                            views.setImageViewBitmap(R.id.currency_graph, bitmap)
                        }

                        val ids = appWidgetManager.getAppWidgetIds(
                            ComponentName(
                                context,
                                large_crypto_widget::class.java
                            )
                        )
                        val updateIntent = Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE)
                        updateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)

                        val pendingIntent: PendingIntent = PendingIntent.getBroadcast(
                            context,
                            appWidgetId,
                            updateIntent,
                            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                        )

                        views.setOnClickPendingIntent(R.id.btn_reload, pendingIntent)

                        val random = (0..10000).shuffled().first()
                        val intent = Intent(context, MainActivity::class.java)
                        intent.putExtra(
                            "cryptoID",
                            GlobalFunctions.getWidgetCurrency(context).first().id
                        )

                        val pendingIntent1: PendingIntent = PendingIntent.getActivity(
                            context,
                            random,
                            intent,
                            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                        )

                        views.setOnClickPendingIntent(R.id.btn_open, pendingIntent1)

                        appWidgetManager.updateAppWidget(appWidgetId, views)

                        Glide.with(context)
                            .asBitmap()
                            .load(it.image?.large)
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
                                            views.setImageViewBitmap(R.id.img_currency, resource)
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
                    }
                }
            }
        }
    }
}

fun roundToHundreds(x : Int): Int {
    return 100 * ((x / 100.0).roundToInt())
}

private fun getTrimmedVolume(volumes: List<Float>): List<Float> {
    var firstNumber = 1
    if (volumes.size > 100) {

        val count = roundToHundreds(volumes.size)
        val temp = "$count"
        firstNumber = temp.substring(0, 1).toInt()

        if (volumes.size > 1000) {
            firstNumber *= 10
            firstNumber += temp.substring(1, 2).toInt()
        }
    }

    val trimmedArray = mutableListOf<Float>()

    volumes.forEachIndexed { index, volume ->
        if (index % firstNumber == 0) {
            trimmedArray.add(volume)
        }
    }

    return trimmedArray
}

private fun setChartPrices(prices: List<Double?>, at: Int, context: Context): String {
    val lowest = prices.minOfOrNull { it ?: 0.0 } ?: 0.0
    val priceSteps =
        ((prices.maxOfOrNull { it ?: 0.0 } ?: 0.0 ).minus((prices.minOfOrNull { it ?: 0.0 } ?: 0.0 ))).div(4)
    return (lowest + (priceSteps * at)).formatCurrency(false, true, context)
}

fun getTimeChartDates(at: Int, context: Context): String {
    var value = 0
    var format = "MMM dd"
    val closestHour = if (Date().minutes >= 30) 60 - Date().minutes else -Date().minutes

    when (GlobalFunctions.getPreferredTimeframe()) {
        PreferredTimeframe.DAILY -> {
            format = if(context.is24HourClock) "HH:mm" else "hh:mm a"

            val extraHour = if (closestHour <= 0) 0 else 1

            value = when (at) {
                1 -> 18 + extraHour
                2 -> 12 + extraHour
                3 -> 6 + extraHour
                else -> 0
            }
        }

        PreferredTimeframe.WEEKLY -> {

            value = when (at) {
                1 -> 126
                2 -> 84
                3 -> 42
                else -> 0
            }
        }

        PreferredTimeframe.MONTHLY -> {

            value = when (at) {
                1 -> 480
                2 -> 320
                3 -> 160
                else -> 0

            }
        }
    }


    val dateFormat = SimpleDateFormat(format, AppLocale.enUsPosix)
    val calendar = Calendar.getInstance()
    calendar.time = Date().roundToNextWholeHour()

    calendar.add(Calendar.HOUR, -value)

    return dateFormat.format(calendar.time)
}

private fun drawGraphPaths(prices: PriceVolumeModel, context: Context, height: Int, width: Int, priceChange: Double): Bitmap? {

    val width = if (width > 0) width else Resources.getSystem().displayMetrics.widthPixels
    val height = if (height > 0) height else Resources.getSystem().displayMetrics.heightPixels
    val bitmap = Bitmap.createBitmap(
        width,
        height,
        Bitmap.Config.ARGB_8888
    )


    // canvas to draw path
    val canvas = Canvas(bitmap)

    val prices = prices.prices?.map{ it.last() }?.toMutableList()

    val normalizedPrices = prices?.map { it ->
        normalise(
            it?.toFloat() ?: 0f,
            prices.minOfOrNull { it ?: 0.0 }?.toFloat() ?: 0f,
            prices.maxOfOrNull { it ?: 0.0 }?.toFloat() ?: 0f
        )
    }
    
    // Gradient Shade colors
   /* val colors = getLinearColorsMediumWidget(priceChange, context)

    // paint to draw path fill color
    val paintFill = Paint().apply {
        isAntiAlias = false
        shader = LinearGradient(0f, 0f, 0f, canvas.height.toFloat(),
            colors,
            null,
            Shader.TileMode.CLAMP)
        style = Paint.Style.FILL
    }


    // create a path
    val pricePath = Path().apply {

        fillType = Path.FillType.EVEN_ODD

        // -100f to not see a line going from top left corner down to start position
        moveTo( -20f, (normalizedPrices?.first() ?: 0f) * canvas.height)

        normalizedPrices?.forEachIndexed { index, it ->
            lineTo((canvas.width * index).toFloat() / (normalizedPrices.size - 1.0f),
                (1 - it) * canvas.height)
        }

        // Make line around and outside of screen to not see line going from end to start
        lineTo(1.1f * canvas.width.toFloat(), canvas.height * 1.5.toFloat())

        lineTo(-1.1f * canvas.width.toFloat(), canvas.height * 1.5.toFloat())

        close()
    }

    //  draw fill under path
    try {
        canvas.drawPath(pricePath, paintFill)
    } catch (e: Exception) {
        return null
    }*/

    // paint to draw path border
    val paintStroke = Paint().apply {
        isAntiAlias = false
        color = priceChange.getWidgetChangeColor(context)
        strokeWidth = 7F
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }

    // draw price path
    try {
       // canvas.drawPath(pricePath, paintStroke)
    } catch (e: Exception) {
        return null
    }

    return bitmap
}