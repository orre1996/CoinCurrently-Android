package com.cryptium.coincurrently.ui.main.Widgets

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import coil.Coil
import coil.request.ImageRequest
import coil.request.SuccessResult
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.width
import androidx.glance.background
import androidx.glance.layout.Column
import com.cryptium.coincurrently.ui.main.Compose.WidgetColorValues
import com.cryptium.coincurrently.ui.main.General.GlobalFunctions
import com.cryptium.coincurrently.ui.main.Repositories.CurrencyRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import networking.ResponseModels.Toplist

/**
 * Large duo crypto widget built with Jetpack Glance (Compose-based app widgets).
 * Consists of two MediumCryptoContent widgets with a vertical separator in between.
 * Uses widget currency from GlobalFunctions.getWidgetCurrency().
 */

class LargeDuoCryptoGlanceWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        GlobalFunctions.initSharedPreferences(context)

        val widgetData = withContext(Dispatchers.IO) {
            loadWidgetData(context)
        }

        provideContent {
            LargeDuoCryptoContent(
                context = context,
                data1 = widgetData.data1,
                data2 = widgetData.data2,
                bitmap1 = widgetData.bitmap1,
                bitmap2 = widgetData.bitmap2,
                graphBitmap1 = widgetData.graphBitmap1,
                graphBitmap2 = widgetData.graphBitmap2,
            )
        }
    }

    private suspend fun loadWidgetData(context: Context): LargeDuoWidgetData {
        val widgetCurrency = GlobalFunctions.getWidgetCurrency()
        val ids = widgetCurrency.take(2).mapNotNull { it.id }
        if (ids.isEmpty()) {
            return LargeDuoWidgetData(null, null, null, null, null, null)
        }

        val currencyCode = GlobalFunctions.getSelectedCurrency().code
        val idsString = ids.joinToString(",")
        val toplist = CurrencyRepository.getMultipleCurrencies(idsString, currencyCode)
            ?: return LargeDuoWidgetData(null, null, null, null, null, null)

        val data1 = ids.getOrNull(0)?.let { id -> toplist.find { it.id == id } }
        val data2 = ids.getOrNull(1)?.let { id -> toplist.find { it.id == id } }

        val bitmap1 = data1?.image?.let { url -> loadBitmapFromCache(context, url) }
        val bitmap2 = data2?.image?.let { url -> loadBitmapFromCache(context, url) }

        val graphBitmap1 = drawGraphBitmap(
            context = context,
            prices = data1?.sparkline_in_7d?.price,
            priceChange = data1?.price_change_percentage_7d_in_currency ?: 0.0,
        )

        val graphBitmap2 = drawGraphBitmap(
            context = context,
            prices = data2?.sparkline_in_7d?.price,
            priceChange = data2?.price_change_percentage_7d_in_currency ?: 0.0,
        )

        return LargeDuoWidgetData(data1, data2, bitmap1, bitmap2, graphBitmap1, graphBitmap2)
    }

    private suspend fun loadBitmapFromCache(
        context: Context,
        url: String
    ): Bitmap? {
        val request = ImageRequest.Builder(context)
            .data(url)
            .allowHardware(false)
            .build()
        return when (val result = Coil.imageLoader(context).execute(request)) {
            is SuccessResult -> result.drawable.toBitmap()
            else -> null
        }
    }
}

private data class LargeDuoWidgetData(
    val data1: Toplist?,
    val data2: Toplist?,
    val bitmap1: Bitmap?,
    val bitmap2: Bitmap?,
    val graphBitmap1: Bitmap?,
    val graphBitmap2: Bitmap?,
)

@Composable
private fun LargeDuoCryptoContent(
    context: Context,
    data1: Toplist?,
    data2: Toplist?,
    bitmap1: Bitmap?,
    bitmap2: Bitmap?,
    graphBitmap1: Bitmap?,
    graphBitmap2: Bitmap?,
) {
    Box(
        contentAlignment = Alignment.TopStart,
        modifier = GlanceModifier
            .fillMaxWidth()
            .background(WidgetColorValues.surface())
    ) {
        GlanceTheme {
            Column(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalAlignment = Alignment.Start,
            ) {
                // First medium widget
                Box(modifier = GlanceModifier.defaultWeight()) {
                    MediumCryptoContent(
                        context = context,
                        data = data1,
                        bitmap = bitmap1,
                        graphBitmap = graphBitmap1,
                        showRefresh = false
                    )
                }

                // Vertical separator - 0.5 opacity, fixed height for medium widget content
                Box(
                    modifier = GlanceModifier
                        .width(350.dp)
                        .height(1.dp)
                        .background(WidgetColorValues.onSurfaceDimTransparent())
                ) {}

                // Second medium widget
                Box(modifier = GlanceModifier.defaultWeight()) {
                    MediumCryptoContent(
                        context = context,
                        data = data2,
                        bitmap = bitmap2,
                        graphBitmap = graphBitmap2,
                        showRefresh = true,
                        refreshWidgetType = "LargeDuoCryptoGlanceWidget"
                    )
                }
            }
        }
    }
}

/** Connects the large duo crypto widget to Glance. Declare in AndroidManifest as the app widget receiver. */
class LargeDuoCryptoWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = LargeDuoCryptoGlanceWidget()
}
