package com.cryptium.coincurrently.widgets.receivers
import com.cryptium.coincurrently.core.persistence.UserPreferences
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
import android.widget.RemoteViews
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.cryptium.coincurrently.R
import com.cryptium.coincurrently.app.MainActivity
import com.cryptium.coincurrently.sharedui.charts.getChangeSymbolResource
import com.cryptium.coincurrently.sharedui.charts.getChangeWithSymbol
import com.cryptium.coincurrently.sharedui.charts.getFormattedCurrencyString
import com.cryptium.coincurrently.sharedui.charts.getWidgetChangeColor
import com.cryptium.coincurrently.core.persistence.CurrencyRepository
import com.cryptium.coincurrently.core.extensions.getSelectedCurrency
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import com.cryptium.coincurrently.core.networking.ResponseModels.CryptoDetailInfoModel
import com.cryptium.coincurrently.core.networking.ResponseModels.Toplist

/**
 * Shared helper functions for RemoteViews-based crypto widgets.
 * The medium duo widget is now implemented as MediumDuoCryptoGlanceWidget (Glance/Compose).
 */

fun downloadCryptoDetail( id: String, context: Context, downloadImage: Boolean = true, callback: (CryptoDetailInfoModel?, Bitmap?) -> Unit) {
     if (id.isEmpty()) {
         callback.invoke(null, null)
         return
     }

     GlobalScope.launch {
         CurrencyRepository.getCryptoDetail(id, true)?.let {

            if (!downloadImage) {
                callback.invoke(it, null)
            }

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
                        callback.invoke(it, null)
                        return false
                    }

                    override fun onResourceReady(
                        resource: Bitmap?,
                        model: Any?,
                        target: Target<Bitmap>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        callback.invoke(it, resource)
                        return false
                    }

                })
                .preload()
         }
     }
}

fun downloadMultipleCurrencies(currenciesArray: List<String>, context: Context, callback: (ArrayList<Toplist>?, ArrayList<Bitmap?>?) -> Unit) {
    if (currenciesArray.isEmpty()) {
        callback.invoke(null, null)
        return
    }

    val currencies = currenciesArray.map { it }.joinToString { it }.replace(" ", "").removeSuffix(",")

    GlobalScope.launch {
        CurrencyRepository.getMultipleCurrencies(currencies, UserPreferences.getSelectedCurrency(context)?.code ?: "USD")?.let {

            val tempArray: ArrayList<Boolean> = arrayListOf()
            val tempCurrencies: ArrayList<Toplist?> = arrayListOf()
            val bitmapArray: ArrayList<Bitmap?> = arrayListOf()

            currenciesArray.forEach { _ ->
                bitmapArray.add(null)
                tempCurrencies.add(null)
            }

            currenciesArray.forEachIndexed { index, portfolioResponse ->

                val currentCurrency = it.first { it.id == portfolioResponse }

                Glide.with(context)
                    .asBitmap()
                    .load(currentCurrency.image)
                    .apply(RequestOptions().override(200, 200))
                    .listener(object : RequestListener<Bitmap> {
                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: Target<Bitmap>?,
                            isFirstResource: Boolean
                        ): Boolean {

                            bitmapArray[index] = null
                            tempCurrencies[index] = null
                            tempArray.add(true)

                            if (bitmapArray.size == tempArray.size) {

                                val arrayList = arrayListOf<Toplist>()
                                tempCurrencies.forEach {
                                    arrayList.add(it!!)
                                }

                                callback.invoke(arrayList, bitmapArray)
                            }

                            return false
                        }

                        override fun onResourceReady(
                            resource: Bitmap?,
                            model: Any?,
                            target: Target<Bitmap>?,
                            dataSource: DataSource?,
                            isFirstResource: Boolean
                        ): Boolean {

                            bitmapArray[index] = resource
                            tempCurrencies[index] = currentCurrency
                            tempArray.add(true)

                            if (bitmapArray.size == tempArray.size) {

                                val arrayList = arrayListOf<Toplist>()
                                tempCurrencies.forEach {
                                    arrayList.add(it!!)
                                }

                                callback.invoke(arrayList, bitmapArray)
                            }

                            return false
                        }

                    })
                    .preload()
            }
        }
    }
}


fun createSmallWidget(data: CryptoDetailInfoModel?, context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int): RemoteViews {
    val isDark = (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_YES) != 0
    val views = RemoteViews(context.packageName, R.layout.small_crypto_widget_duo)

    views.setTextColor(R.id.txt_currency_rank_shortening, widgetOnSurface(context))
    views.setTextColor(R.id.txt_currency_price, widgetOnSurface(context))
    views.setTextColor(R.id.txt_24h_title, widgetOnSurface(context))
    views.setTextColor(R.id.txt_7d_title, widgetOnSurface(context))
    views.setTextColor(R.id.txt_30d_title, widgetOnSurface(context))
    views.setTextColor(R.id.txt_24h_percentage, widgetOnSurfaceDim(context))
    views.setTextColor(R.id.txt_7d_percentage, widgetOnSurfaceDim(context))
    views.setTextColor(R.id.txt_30d_percentage, widgetOnSurfaceDim(context))
    views.setImageViewResource(R.id.img_24h_change_symbol, if(isDark) R.drawable.trend_still else R.drawable.trend_still)
    views.setImageViewResource(R.id.img_7d_change_symbol, if(isDark) R.drawable.trend_still else R.drawable.trend_still)
    views.setImageViewResource(R.id.img_30d_change_symbol, if(isDark) R.drawable.trend_still else R.drawable.trend_still)
    views.setInt(R.id.img_24h_change_symbol, "setColorFilter", widgetOnSurfaceDim(context))
    views.setInt(R.id.img_7d_change_symbol, "setColorFilter", widgetOnSurfaceDim(context))
    views.setInt(R.id.img_30d_change_symbol, "setColorFilter", widgetOnSurfaceDim(context))
    views.setImageViewResource(R.id.btn_reload, if(isDark) R.drawable.ic_baseline_sync_24_dark else R.drawable.ic_baseline_sync_24_light)
    views.setInt(R.id.btn_reload, "setBackgroundResource", if(isDark) R.drawable.widget_reload_button_bg_bottom_dark else R.drawable.widget_reload_button_bg_bottom_light)


    if (data != null) {

        views.setTextViewText(R.id.txt_currency_rank_shortening, "${data.symbol?.uppercase()}\n${data.name}")
        views.setTextViewText(R.id.txt_currency_price, data.market_data?.current_price?.getSelectedCurrency(context)?.getFormattedCurrencyString(true, context))

        views.setTextViewText(R.id.txt_24h_percentage, data.market_data?.price_change_percentage_24h?.getChangeWithSymbol())
        views.setTextColor(R.id.txt_24h_percentage,  data.market_data?.price_change_percentage_24h?.getWidgetChangeColor( context) ?: widgetOnSurfaceDim(context))
        views.setImageViewResource(R.id.img_24h_change_symbol, data.market_data?.price_change_percentage_24h?.getChangeSymbolResource() ?: R.drawable.trend_still)
        views.setInt(R.id.img_24h_change_symbol, "setColorFilter", data.market_data?.price_change_percentage_24h?.getWidgetChangeColor(context) ?: widgetOnSurfaceDim(context))

        views.setTextViewText(R.id.txt_7d_percentage, data.market_data?.price_change_percentage_7d?.getChangeWithSymbol())
        views.setTextColor(R.id.txt_7d_percentage, data.market_data?.price_change_percentage_7d?.getWidgetChangeColor(context) ?: widgetOnSurfaceDim(context))
        views.setImageViewResource(R.id.img_7d_change_symbol, data.market_data?.price_change_percentage_7d?.getChangeSymbolResource() ?: R.drawable.trend_still)
        views.setInt(R.id.img_7d_change_symbol, "setColorFilter", data.market_data?.price_change_percentage_7d?.getWidgetChangeColor(context) ?: widgetOnSurfaceDim(context))

        views.setTextViewText(R.id.txt_30d_percentage, data.market_data?.price_change_percentage_30d?.getChangeWithSymbol())
        views.setTextColor(R.id.txt_30d_percentage, data.market_data?.price_change_percentage_30d?.getWidgetChangeColor(context) ?: widgetOnSurfaceDim(context))
        views.setImageViewResource(R.id.img_30d_change_symbol, data.market_data?.price_change_percentage_30d?.getChangeSymbolResource() ?: R.drawable.trend_still)
        views.setInt(R.id.img_30d_change_symbol, "setColorFilter", data.market_data?.price_change_percentage_30d?.getWidgetChangeColor(context) ?: widgetOnSurfaceDim(context))
     }

    val random = (0..10000).shuffled().first()

    val intent = Intent(context, MainActivity::class.java)
    intent.putExtra("cryptoID", data?.id)

    val pendingIntent: PendingIntent = PendingIntent.getActivity(context, random, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

    views.setOnClickPendingIntent(R.id.btn_open, pendingIntent)

    return views
}
