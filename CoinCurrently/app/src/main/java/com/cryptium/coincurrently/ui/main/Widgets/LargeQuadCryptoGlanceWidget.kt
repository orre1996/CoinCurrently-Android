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
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxWidth
import androidx.glance.background
import androidx.glance.layout.height
import androidx.glance.layout.width
import com.cryptium.coincurrently.ui.main.Compose.WidgetColorValues
import com.cryptium.coincurrently.ui.main.General.GlobalFunctions
import com.cryptium.coincurrently.ui.main.Repositories.CurrencyRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import networking.ResponseModels.Toplist

/**
 * Large quad crypto widget built with Jetpack Glance (Compose-based app widgets).
 * Layout: 2x2 grid of SmallCryptoContent widgets.
 * Uses widget currency from GlobalFunctions.getWidgetCurrency().
 */

class LargeQuadCryptoGlanceWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        GlobalFunctions.initSharedPreferences(context)

        val widgetData = withContext(Dispatchers.IO) {
            loadWidgetData(context)
        }

        provideContent {
            LargeQuadCryptoContent(
                context = context,
                data1 = widgetData.data1,
                data2 = widgetData.data2,
                data3 = widgetData.data3,
                data4 = widgetData.data4,
                bitmap1 = widgetData.bitmap1,
                bitmap2 = widgetData.bitmap2,
                bitmap3 = widgetData.bitmap3,
                bitmap4 = widgetData.bitmap4,
            )
        }
    }

    private suspend fun loadWidgetData(context: Context): LargeQuadWidgetData {
        val widgetCurrency = GlobalFunctions.getWidgetCurrency()
        val ids = widgetCurrency.take(4).mapNotNull { it.id }
        if (ids.isEmpty()) {
            return LargeQuadWidgetData(null, null, null, null, null, null, null, null)
        }

        val currencyCode = GlobalFunctions.getSelectedCurrency().code
        val idsString = ids.joinToString(",")
        val toplist = CurrencyRepository.getMultipleCurrencies(idsString, currencyCode)
            ?: return LargeQuadWidgetData(null, null, null, null, null, null, null, null)

        val data1 = ids.getOrNull(0)?.let { id -> toplist.find { it.id == id } }
        val data2 = ids.getOrNull(1)?.let { id -> toplist.find { it.id == id } }
        val data3 = ids.getOrNull(2)?.let { id -> toplist.find { it.id == id } }
        val data4 = ids.getOrNull(3)?.let { id -> toplist.find { it.id == id } }

        val bitmap1 = data1?.image?.let { url -> loadBitmapFromCache(context, url) }
        val bitmap2 = data2?.image?.let { url -> loadBitmapFromCache(context, url) }
        val bitmap3 = data3?.image?.let { url -> loadBitmapFromCache(context, url) }
        val bitmap4 = data4?.image?.let { url -> loadBitmapFromCache(context, url) }

        return LargeQuadWidgetData(data1, data2, data3, data4, bitmap1, bitmap2, bitmap3, bitmap4)
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

private data class LargeQuadWidgetData(
    val data1: Toplist?,
    val data2: Toplist?,
    val data3: Toplist?,
    val data4: Toplist?,
    val bitmap1: Bitmap?,
    val bitmap2: Bitmap?,
    val bitmap3: Bitmap?,
    val bitmap4: Bitmap?,
)

@Composable
private fun LargeQuadCryptoContent(
    context: Context,
    data1: Toplist?,
    data2: Toplist?,
    data3: Toplist?,
    data4: Toplist?,
    bitmap1: Bitmap?,
    bitmap2: Bitmap?,
    bitmap3: Bitmap?,
    bitmap4: Bitmap?,
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
                verticalAlignment = Alignment.Top,
                horizontalAlignment = Alignment.Start,
            ) {
                // Row 1: Small1 | Small2
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalAlignment = Alignment.Start,
                ) {
                    Box(modifier = GlanceModifier.defaultWeight()) {
                        SmallCryptoContent(
                            context = context,
                            data = data1,
                            bitmap = bitmap1,
                            showRefresh = false
                        )
                    }

                    Box(
                        modifier = GlanceModifier
                            .width(1.dp)
                            .height(210.dp)
                            .background(WidgetColorValues.onSurfaceDimTransparent())
                    ) {}

                    Box(modifier = GlanceModifier.defaultWeight()) {
                        SmallCryptoContent(
                            context = context,
                            data = data2,
                            bitmap = bitmap2,
                            showRefresh = false
                        )
                    }
                }

                Box(
                    modifier = GlanceModifier
                        .width(350.dp)
                        .height(1.dp)
                        .background(WidgetColorValues.onSurfaceDimTransparent())
                ) {}

                // Row 2: Small3 | Small4
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalAlignment = Alignment.Start,
                ) {
                    Box(modifier = GlanceModifier.defaultWeight()) {
                        SmallCryptoContent(
                            context = context,
                            data = data3,
                            bitmap = bitmap3,
                            showRefresh = false
                        )
                    }

                    Box(
                        modifier = GlanceModifier
                            .width(1.dp)
                            .height(210.dp)
                            .background(WidgetColorValues.onSurfaceDimTransparent())
                    ) {}

                    Box(modifier = GlanceModifier.defaultWeight()) {
                        SmallCryptoContent(
                            context = context,
                            data = data4,
                            bitmap = bitmap4,
                            showRefresh = true,
                            refreshWidgetType = "LargeQuadCryptoGlanceWidget"
                        )
                    }
                }
            }
        }
    }
}

/** Connects the large quad crypto widget to Glance. Declare in AndroidManifest as the app widget receiver. */
class LargeQuadCryptoWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = LargeQuadCryptoGlanceWidget()
}
