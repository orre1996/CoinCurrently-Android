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
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.background
import com.cryptium.coincurrently.ui.main.Compose.WidgetColorValues
import com.cryptium.coincurrently.ui.main.General.GlobalFunctions
import com.cryptium.coincurrently.ui.main.Repositories.CurrencyRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import networking.ResponseModels.Toplist
import networking.ResponseModels.getPreferredChange

/**
 * Small multi crypto widget built with Jetpack Glance.
 * Shows 2 crypto cells stacked vertically. Uses widget currency from GlobalFunctions.getWidgetCurrency().
 */
class SmallMultiCryptoGlanceWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        GlobalFunctions.initSharedPreferences(context)

        val widgetData = withContext(Dispatchers.IO) {
            loadWidgetData(context)
        }

        provideContent {
            SmallMultiCryptoContent(
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

    private suspend fun loadWidgetData(context: Context): SmallMultiWidgetData {
        val widgetCurrency = GlobalFunctions.getWidgetCurrency()
        val ids = widgetCurrency.take(2).mapNotNull { it.id }
        if (ids.isEmpty()) {
            return SmallMultiWidgetData(null, null, null, null, null, null)
        }

        val currencyCode = GlobalFunctions.getSelectedCurrency().code
        val idsString = ids.joinToString(",")
        val toplist = CurrencyRepository.getMultipleCurrencies(idsString, currencyCode)
            ?: return SmallMultiWidgetData(null, null, null, null, null, null)

        val data1 = ids.getOrNull(0)?.let { id -> toplist.find { it.id == id } }
        val data2 = ids.getOrNull(1)?.let { id -> toplist.find { it.id == id } }

        val bitmap1 = data1?.image?.let { url -> loadBitmapFromCache(context, url) }
        val bitmap2 = data2?.image?.let { url -> loadBitmapFromCache(context, url) }

        val graphBitmap1 = data1?.let { d ->
            drawGraphBitmap(
                context = context,
                prices = d.sparkline_in_7d?.price,
                priceChange = d.getPreferredChange(),
                width = 90,
                height = 26,
            )
        }
        val graphBitmap2 = data2?.let { d ->
            drawGraphBitmap(
                context = context,
                prices = d.sparkline_in_7d?.price,
                priceChange = d.getPreferredChange(),
                width = 90,
                height = 26,
            )
        }

        return SmallMultiWidgetData(
            data1, data2,
            bitmap1, bitmap2,
            graphBitmap1, graphBitmap2,
        )
    }

    private suspend fun loadBitmapFromCache(
        context: Context,
        url: String,
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

private data class SmallMultiWidgetData(
    val data1: Toplist?,
    val data2: Toplist?,
    val bitmap1: Bitmap?,
    val bitmap2: Bitmap?,
    val graphBitmap1: Bitmap?,
    val graphBitmap2: Bitmap?,
)

@Composable
private fun SmallMultiCryptoContent(
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
            .background(WidgetColorValues.surface()),
    ) {
        GlanceTheme {
            Column(
                verticalAlignment = Alignment.Top,
                horizontalAlignment = Alignment.Start,
                modifier = GlanceModifier
                    .fillMaxWidth()
            ) {
                // Cell 1 (with refresh button)
                Box(modifier = GlanceModifier.defaultWeight()) {
                    MultiWidgetCellContent(
                        context = context,
                        data = data1,
                        bitmap = bitmap1,
                        graphBitmap = graphBitmap1,
                        showRefresh = true,
                        refreshWidgetType = "SmallMultiCryptoGlanceWidget",
                    )
                }

                // Separator
                Box(
                    modifier = GlanceModifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(WidgetColorValues.onSurfaceDimTransparent()),
                ) {}

                // Cell 2
                Box(modifier = GlanceModifier.defaultWeight()) {
                    MultiWidgetCellContent(
                        context = context,
                        data = data2,
                        bitmap = bitmap2,
                        graphBitmap = graphBitmap2,
                        showRefresh = false,
                    )
                }
            }
        }
    }
}

/** Connects the small multi crypto widget to Glance. Declare in AndroidManifest as the app widget receiver. */
class SmallMultiCryptoWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = SmallMultiCryptoGlanceWidget()
}
