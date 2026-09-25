package com.cryptium.coincurrently.widgets.receivers

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.widget.RemoteViews
import androidx.core.graphics.drawable.toBitmap
import coil.Coil
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.cryptium.coincurrently.R
import com.cryptium.coincurrently.app.MainActivity
import com.cryptium.coincurrently.core.networking.ResponseModels.CryptoDetailInfoModel
import com.cryptium.coincurrently.core.networking.ResponseModels.Toplist
import com.cryptium.coincurrently.core.extensions.getSelectedCurrency
import com.cryptium.coincurrently.core.persistence.CurrencyRepository
import com.cryptium.coincurrently.core.persistence.UserPreferences
import com.cryptium.coincurrently.widgets.config.WidgetConfigResolver
import com.cryptium.coincurrently.widgets.config.WidgetConfigStore
import com.cryptium.coincurrently.widgets.config.WidgetRuntimeInitializer
import com.cryptium.coincurrently.sharedui.charts.getChangeSymbolResource
import com.cryptium.coincurrently.sharedui.charts.getChangeWithSymbol
import com.cryptium.coincurrently.sharedui.charts.getFormattedCurrencyString
import com.cryptium.coincurrently.sharedui.charts.getWidgetChangeColor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

private data class SmallCryptoDisplayData(
    val id: String?,
    val name: String?,
    val symbol: String?,
    val price: String?,
    val change24h: Double?,
)

private fun CryptoDetailInfoModel.toSmallCryptoDisplayData(context: Context): SmallCryptoDisplayData {
    return SmallCryptoDisplayData(
        id = id,
        name = name,
        symbol = symbol?.uppercase(),
        price = market_data?.current_price?.getSelectedCurrency(context)
            ?.getFormattedCurrencyString(true, context),
        change24h = market_data?.price_change_percentage_24h,
    )
}

private fun Toplist.toSmallCryptoDisplayData(context: Context): SmallCryptoDisplayData {
    return SmallCryptoDisplayData(
        id = id,
        name = name,
        symbol = symbol?.uppercase(),
        price = current_price?.getFormattedCurrencyString(true, context),
        change24h = price_change_percentage_24h_in_currency,
    )
}

private fun widgetBackground(context: Context): Int {
    return if (isWidgetDarkMode(context)) {
        R.drawable.widget_background_dark
    } else {
        R.drawable.widget_background_light
    }
}

internal suspend fun loadCoinBitmap(context: Context, url: String?): Bitmap? {
    if (url.isNullOrEmpty()) return null
    val request = ImageRequest.Builder(context)
        .data(url)
        .allowHardware(false)
        .build()
    return when (val result = Coil.imageLoader(context).execute(request)) {
        is SuccessResult -> result.drawable.toBitmap()
        else -> null
    }
}

internal suspend fun loadWidgetToplist(context: Context, coinIds: List<String>): List<Toplist> {
    if (coinIds.isEmpty()) return emptyList()
    val currencyCode = UserPreferences.getSelectedCurrency(context).code
    return CurrencyRepository.getMultipleCurrencies(coinIds.joinToString(","), currencyCode).orEmpty()
}

fun createSmallCryptoWidget(
    data: CryptoDetailInfoModel?,
    context: Context,
    bitmap: Bitmap? = null,
): RemoteViews {
    return bindSmallCryptoWidgetViews(
        data = data?.toSmallCryptoDisplayData(context),
        context = context,
        bitmap = bitmap,
    )
}

fun createSmallCryptoWidgetWithToplist(
    data: Toplist?,
    context: Context,
    bitmap: Bitmap? = null,
    embedded: Boolean = false,
): RemoteViews {
    return bindSmallCryptoWidgetViews(
        data = data?.toSmallCryptoDisplayData(context),
        context = context,
        bitmap = bitmap,
        embedded = embedded,
    )
}

private fun bindSmallCryptoWidgetViews(
    data: SmallCryptoDisplayData?,
    context: Context,
    bitmap: Bitmap?,
    embedded: Boolean = false,
): RemoteViews {
    val views = RemoteViews(
        context.packageName,
        if (embedded) R.layout.small_crypto_widget_cell else R.layout.small_crypto_widget,
    )

    if (!embedded) {
        views.setInt(R.id.ll_view, "setBackgroundResource", widgetBackground(context))
    }

    views.setTextColor(R.id.txt_currency_name, widgetOnSurface(context))
    views.setTextColor(R.id.txt_currency_symbol, widgetOnSurfaceDim(context))
    views.setTextColor(R.id.txt_currency_price, widgetOnSurface(context))
    views.setTextColor(R.id.txt_24h_title, widgetOnSurfaceDimmer(context))
    views.setTextColor(R.id.txt_24h_percentage, widgetOnSurfaceDim(context))
    views.setImageViewResource(
        R.id.img_currency,
        if (isWidgetDarkMode(context)) R.drawable.logo_dark else R.drawable.logo_light,
    )
    views.setImageViewResource(R.id.img_24h_change_symbol, R.drawable.trend_still)
    views.setInt(R.id.img_24h_change_symbol, "setColorFilter", widgetOnSurfaceDim(context))

    views.setTextViewText(R.id.txt_currency_name, data?.name ?: "CoinCurrently")
    views.setTextViewText(R.id.txt_currency_symbol, data?.symbol ?: "CNCR")
    views.setTextViewText(R.id.txt_currency_price, data?.price ?: "$0.00")

    val change24h = data?.change24h
    views.setTextViewText(R.id.txt_24h_percentage, change24h?.getChangeWithSymbol() ?: "0.00%")
    views.setTextColor(
        R.id.txt_24h_percentage,
        change24h?.getWidgetChangeColor(context) ?: widgetOnSurfaceDim(context),
    )
    views.setImageViewResource(
        R.id.img_24h_change_symbol,
        change24h?.getChangeSymbolResource() ?: R.drawable.trend_still,
    )
    views.setInt(
        R.id.img_24h_change_symbol,
        "setColorFilter",
        change24h?.getWidgetChangeColor(context) ?: widgetOnSurfaceDim(context),
    )

    if (bitmap != null && !bitmap.isRecycled) {
        views.setImageViewBitmap(R.id.img_currency, bitmap)
    }

    setSmallCryptoOpenIntent(views, context, data?.id)
    return views
}

fun setSmallCryptoOpenIntent(
    views: RemoteViews,
    context: Context,
    cryptoId: String?,
) {
    val intent = Intent(context, MainActivity::class.java).apply {
        putExtra("cryptoID", cryptoId)
    }
    val pendingIntent = PendingIntent.getActivity(
        context,
        (0..10000).random(),
        intent,
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
    )
    views.setOnClickPendingIntent(R.id.btn_open, pendingIntent)
}

private fun bindCoinCell(
    parent: RemoteViews,
    containerId: Int,
    data: Toplist?,
    bitmap: Bitmap?,
    context: Context,
) {
    parent.removeAllViews(containerId)
    parent.addView(containerId, createSmallCryptoWidgetWithToplist(data, context, bitmap, embedded = true))
}

internal fun createSmallWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int,
) {
    WidgetRuntimeInitializer.ensureInitialized(context)
    GlobalScope.launch(Dispatchers.IO) {
        val config = WidgetConfigStore.getSingleCoinConfig(context, appWidgetId)
        val coinIds = WidgetConfigResolver.resolveCoinIds(
            context = context,
            configuredIds = listOf(config.coinId),
            limit = 1,
        )
        val toplist = loadWidgetToplist(context, coinIds)
        val data = coinIds.firstOrNull()?.let { id -> toplist.find { it.id == id } }
        val views = createSmallCryptoWidgetWithToplist(
            data = data,
            context = context,
            bitmap = loadCoinBitmap(context, data?.image),
        )
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }
}

internal fun createMediumDuoWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int,
) {
    WidgetRuntimeInitializer.ensureInitialized(context)
    val placeholder = buildMediumDuoWidget(context, emptyList(), emptyList())
    appWidgetManager.updateAppWidget(appWidgetId, placeholder)

    GlobalScope.launch(Dispatchers.IO) {
        val config = WidgetConfigStore.getDuoCoinConfig(context, appWidgetId)
        val coinIds = WidgetConfigResolver.resolveCoinIds(
            context = context,
            configuredIds = listOf(config.firstCoinId, config.secondCoinId),
            limit = 2,
        )
        val toplist = loadWidgetToplist(context, coinIds)
        val coins = coinIds.map { id -> toplist.find { it.id == id } }
        val bitmaps = coins.map { loadCoinBitmap(context, it?.image) }
        appWidgetManager.updateAppWidget(appWidgetId, buildMediumDuoWidget(context, coins, bitmaps))
    }
}

private fun buildMediumDuoWidget(
    context: Context,
    coins: List<Toplist?>,
    bitmaps: List<Bitmap?>,
): RemoteViews {
    val views = RemoteViews(context.packageName, R.layout.medium_duo_crypto_widget)
    views.setInt(R.id.ll_duo_widget, "setBackgroundResource", widgetBackground(context))
    views.setInt(R.id.separator_vertical, "setBackgroundColor", widgetOutlineVariant(context))
    bindCoinCell(views, R.id.cell_1, coins.getOrNull(0), bitmaps.getOrNull(0), context)
    bindCoinCell(views, R.id.cell_2, coins.getOrNull(1), bitmaps.getOrNull(1), context)
    return views
}

internal fun createLargeQuadWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int,
) {
    WidgetRuntimeInitializer.ensureInitialized(context)
    val placeholder = buildLargeQuadWidget(context, emptyList(), emptyList())
    appWidgetManager.updateAppWidget(appWidgetId, placeholder)

    GlobalScope.launch(Dispatchers.IO) {
        val config = WidgetConfigStore.getQuadCoinConfig(context, appWidgetId)
        val coinIds = WidgetConfigResolver.resolveCoinIds(
            context = context,
            configuredIds = config.coinIds,
            limit = 4,
        )
        val toplist = loadWidgetToplist(context, coinIds)
        val coins = coinIds.map { id -> toplist.find { it.id == id } }
        val bitmaps = coins.map { loadCoinBitmap(context, it?.image) }
        appWidgetManager.updateAppWidget(appWidgetId, buildLargeQuadWidget(context, coins, bitmaps))
    }
}

private fun buildLargeQuadWidget(
    context: Context,
    coins: List<Toplist?>,
    bitmaps: List<Bitmap?>,
): RemoteViews {
    val views = RemoteViews(context.packageName, R.layout.large_quad_crypto_widget)
    views.setInt(R.id.ll_quad_widget, "setBackgroundResource", widgetBackground(context))
    val separatorColor = widgetOutlineVariant(context)
    views.setInt(R.id.separator_vertical_1, "setBackgroundColor", separatorColor)
    views.setInt(R.id.separator_vertical_2, "setBackgroundColor", separatorColor)
    views.setInt(R.id.first_separator, "setBackgroundColor", separatorColor)
    views.setInt(R.id.second_separator, "setBackgroundColor", separatorColor)
    listOf(R.id.cell_1, R.id.cell_2, R.id.cell_3, R.id.cell_4).forEachIndexed { index, cellId ->
        bindCoinCell(views, cellId, coins.getOrNull(index), bitmaps.getOrNull(index), context)
    }
    return views
}

internal fun createLargeMultiWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int,
) {
    WidgetRuntimeInitializer.ensureInitialized(context)
    val placeholder = buildLargeMultiWidget(context, emptyList(), emptyList())
    appWidgetManager.updateAppWidget(appWidgetId, placeholder)

    GlobalScope.launch(Dispatchers.IO) {
        val config = WidgetConfigStore.getMultiCoinConfig(context, appWidgetId)
        val coinIds = WidgetConfigResolver.resolveCoinIds(
            context = context,
            configuredIds = config.coinIds,
            limit = 6,
        )
        val toplist = loadWidgetToplist(context, coinIds)
        val coins = coinIds.map { id -> toplist.find { it.id == id } }
        val bitmaps = coins.map { loadCoinBitmap(context, it?.image) }
        appWidgetManager.updateAppWidget(appWidgetId, buildLargeMultiWidget(context, coins, bitmaps))
    }
}

private fun buildLargeMultiWidget(
    context: Context,
    coins: List<Toplist?>,
    bitmaps: List<Bitmap?>,
): RemoteViews {
    val views = RemoteViews(context.packageName, R.layout.large_crypto_multi_widget)
    views.setInt(R.id.ll_main, "setBackgroundResource", widgetBackground(context))
    val separatorColor = widgetOutlineVariant(context)
    views.setInt(R.id.separator_vertical1, "setBackgroundColor", separatorColor)
    views.setInt(R.id.separator_vertical2, "setBackgroundColor", separatorColor)
    views.setInt(R.id.separator_vertical3, "setBackgroundColor", separatorColor)
    views.setInt(R.id.separator_horizontal1_start, "setBackgroundColor", separatorColor)
    views.setInt(R.id.separator_horizontal1_end, "setBackgroundColor", separatorColor)
    views.setInt(R.id.separator_horizontal2_start, "setBackgroundColor", separatorColor)
    views.setInt(R.id.separator_horizontal2_end, "setBackgroundColor", separatorColor)

    listOf(R.id.cell_1, R.id.cell_2, R.id.cell_3, R.id.cell_4, R.id.cell_5, R.id.cell_6)
        .forEachIndexed { index, cellId ->
            bindCoinCell(views, cellId, coins.getOrNull(index), bitmaps.getOrNull(index), context)
        }

    return views
}
