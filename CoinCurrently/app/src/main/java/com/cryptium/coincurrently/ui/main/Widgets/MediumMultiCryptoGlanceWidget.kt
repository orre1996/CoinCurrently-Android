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
import androidx.glance.ColorFilter
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.background
import com.cryptium.coincurrently.R
import com.cryptium.coincurrently.ui.main.Compose.WidgetColorValues
import com.cryptium.coincurrently.ui.main.Extensions.satoshiBoldGlance
import com.cryptium.coincurrently.ui.main.General.GlobalFunctions
import com.cryptium.coincurrently.ui.main.Repositories.CurrencyRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import networking.ResponseModels.Toplist
import networking.ResponseModels.getPreferredChange

/**
 * Medium multi crypto widget built with Jetpack Glance.
 * Shows 4 crypto cells in a 2x2 grid. Uses widget currency from GlobalFunctions.getWidgetCurrency().
 * Shows buy pro overlay when user hasn't purchased Pro.
 */
class MediumMultiCryptoGlanceWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        GlobalFunctions.initSharedPreferences(context)

        val hasPro = GlobalFunctions.getHasBoughtPro() != null

        val widgetData = if (hasPro) {
            withContext(Dispatchers.IO) { loadWidgetData(context) }
        } else {
            null
        }

        provideContent {
            Box(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .height(165.dp)
                    .background(WidgetColorValues.surface()),
            ) {
                MediumMultiCryptoContent(
                    context = context,
                    data1 = widgetData?.data1,
                    data2 = widgetData?.data2,
                    data3 = widgetData?.data3,
                    data4 = widgetData?.data4,
                    bitmap1 = widgetData?.bitmap1,
                    bitmap2 = widgetData?.bitmap2,
                    bitmap3 = widgetData?.bitmap3,
                    bitmap4 = widgetData?.bitmap4,
                    graphBitmap1 = widgetData?.graphBitmap1,
                    graphBitmap2 = widgetData?.graphBitmap2,
                    graphBitmap3 = widgetData?.graphBitmap3,
                    graphBitmap4 = widgetData?.graphBitmap4,
                )

                if (!hasPro) {
                    Box(
                        modifier = GlanceModifier
                            .fillMaxSize()
                            .background(WidgetColorValues.overlayScrim())
                            .clickable(
                                onClick = actionRunCallback<OpenAppAction>(
                                    actionParametersOf(CryptoIdKey to "buyPro"),
                                ),
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Box(
                            modifier = GlanceModifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                            BuyProOverlayContent()
                        }
                    }
                }
            }
        }
    }

    private suspend fun loadWidgetData(context: Context): MediumMultiWidgetData {
        val widgetCurrency = GlobalFunctions.getWidgetCurrency()
        val ids = widgetCurrency.take(4).mapNotNull { it.id }
        if (ids.isEmpty()) {
            return MediumMultiWidgetData(null, null, null, null, null, null, null, null, null, null, null, null)
        }

        val currencyCode = GlobalFunctions.getSelectedCurrency().code
        val idsString = ids.joinToString(",")
        val toplist = CurrencyRepository.getMultipleCurrencies(idsString, currencyCode)
            ?: return MediumMultiWidgetData(null, null, null, null, null, null, null, null, null, null, null, null)

        val data1 = ids.getOrNull(0)?.let { id -> toplist.find { it.id == id } }
        val data2 = ids.getOrNull(1)?.let { id -> toplist.find { it.id == id } }
        val data3 = ids.getOrNull(2)?.let { id -> toplist.find { it.id == id } }
        val data4 = ids.getOrNull(3)?.let { id -> toplist.find { it.id == id } }

        val bitmap1 = data1?.image?.let { url -> loadBitmapFromCache(context, url) }
        val bitmap2 = data2?.image?.let { url -> loadBitmapFromCache(context, url) }
        val bitmap3 = data3?.image?.let { url -> loadBitmapFromCache(context, url) }
        val bitmap4 = data4?.image?.let { url -> loadBitmapFromCache(context, url) }

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
        val graphBitmap3 = data3?.let { d ->
            drawGraphBitmap(
                context = context,
                prices = d.sparkline_in_7d?.price,
                priceChange = d.getPreferredChange(),
                width = 90,
                height = 26,
            )
        }
        val graphBitmap4 = data4?.let { d ->
            drawGraphBitmap(
                context = context,
                prices = d.sparkline_in_7d?.price,
                priceChange = d.getPreferredChange(),
                width = 90,
                height = 26,
            )
        }

        return MediumMultiWidgetData(
            data1, data2, data3, data4,
            bitmap1, bitmap2, bitmap3, bitmap4,
            graphBitmap1, graphBitmap2, graphBitmap3, graphBitmap4,
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

private data class MediumMultiWidgetData(
    val data1: Toplist?,
    val data2: Toplist?,
    val data3: Toplist?,
    val data4: Toplist?,
    val bitmap1: Bitmap?,
    val bitmap2: Bitmap?,
    val bitmap3: Bitmap?,
    val bitmap4: Bitmap?,
    val graphBitmap1: Bitmap?,
    val graphBitmap2: Bitmap?,
    val graphBitmap3: Bitmap?,
    val graphBitmap4: Bitmap?,
)

@Composable
private fun MediumMultiCryptoContent(
    context: Context,
    data1: Toplist?,
    data2: Toplist?,
    data3: Toplist?,
    data4: Toplist?,
    bitmap1: Bitmap?,
    bitmap2: Bitmap?,
    bitmap3: Bitmap?,
    bitmap4: Bitmap?,
    graphBitmap1: Bitmap?,
    graphBitmap2: Bitmap?,
    graphBitmap3: Bitmap?,
    graphBitmap4: Bitmap?,
) {
    Box(
        contentAlignment = Alignment.TopStart,
        modifier = GlanceModifier
            .fillMaxWidth()
            .background(WidgetColorValues.surface()),
    ) {
        GlanceTheme {
            Column(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalAlignment = Alignment.Start,
            ) {
                // Row 1: Cell 1 | Cell 2
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalAlignment = Alignment.Start,
                ) {
                    Box(modifier = GlanceModifier.defaultWeight()) {
                        MultiWidgetCellContent(
                            context = context,
                            data = data1,
                            bitmap = bitmap1,
                            graphBitmap = graphBitmap1,
                            showRefresh = true,
                            refreshWidgetType = "MediumMultiCryptoGlanceWidget",
                        )
                    }

                    Box(
                        modifier = GlanceModifier
                            .width(1.dp)
                            .height(80.dp)
                            .background(WidgetColorValues.onSurfaceDimTransparent()),
                    ) {}

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

                // Horizontal separator
                Box(
                    modifier = GlanceModifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(WidgetColorValues.onSurfaceDimTransparent()),
                ) {}

                // Row 2: Cell 3 | Cell 4
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalAlignment = Alignment.Start,
                ) {
                    Box(modifier = GlanceModifier.defaultWeight()) {
                        MultiWidgetCellContent(
                            context = context,
                            data = data3,
                            bitmap = bitmap3,
                            graphBitmap = graphBitmap3,
                            showRefresh = false,
                        )
                    }

                    Box(
                        modifier = GlanceModifier
                            .width(1.dp)
                            .height(80.dp)
                            .background(WidgetColorValues.onSurfaceDimTransparent()),
                    ) {}

                    Box(modifier = GlanceModifier.defaultWeight()) {
                        MultiWidgetCellContent(
                            context = context,
                            data = data4,
                            bitmap = bitmap4,
                            graphBitmap = graphBitmap4,
                            showRefresh = false,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BuyProOverlayContent() {
    GlanceTheme {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                provider = ImageProvider(R.drawable.baseline_lock_24),
                contentDescription = null,
                colorFilter = ColorFilter.tint(WidgetColorValues.onSurface()),
                modifier = GlanceModifier.size(24.dp),
            )
            Text(
                text = "Unlocked with\nCoinCurrently Pro",
                style = satoshiBoldGlance(16, WidgetColorValues.onSurface()).copy(textAlign = TextAlign.Center),
                modifier = GlanceModifier.padding(top = 8.dp),
            )
        }
    }
}

/** Connects the medium multi crypto widget to Glance. Declare in AndroidManifest as the app widget receiver. */
class MediumMultiCryptoWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = MediumMultiCryptoGlanceWidget()
}
