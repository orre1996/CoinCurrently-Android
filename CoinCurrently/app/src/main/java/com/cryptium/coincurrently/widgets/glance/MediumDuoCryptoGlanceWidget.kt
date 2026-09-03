package com.cryptium.coincurrently.widgets.glance
import com.cryptium.coincurrently.core.persistence.UserPreferences
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
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.background
import com.cryptium.coincurrently.sharedui.theme.WidgetColorValues
import com.cryptium.coincurrently.core.persistence.CurrencyRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.cryptium.coincurrently.core.networking.ResponseModels.Toplist

/**
 * Medium duo crypto widget built with Jetpack Glance (Compose-based app widgets).
 * Consists of two SmallCryptoContent widgets with a vertical separator in between.
 * Uses widget currency from UserPreferences.getWidgetCurrency().
 */

class MediumDuoCryptoGlanceWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        UserPreferences.init(context)

        val widgetData = withContext(Dispatchers.IO) {
            loadWidgetData(context)
        }

        provideContent {
            MediumDuoCryptoContent(
                context = context,
                data1 = widgetData.data1,
                data2 = widgetData.data2,
                bitmap1 = widgetData.bitmap1,
                bitmap2 = widgetData.bitmap2,
            )
        }
    }

    private suspend fun loadWidgetData(context: Context): MediumDuoWidgetData {
        val widgetCurrency = UserPreferences.getWidgetCurrency(context)
        val ids = widgetCurrency.take(2).mapNotNull { it.id }
        if (ids.isEmpty()) {
            return MediumDuoWidgetData(null, null, null, null)
        }

        val currencyCode = UserPreferences.getSelectedCurrency(context)?.code ?: "USD"
        val idsString = ids.joinToString(",")
        val toplist = CurrencyRepository.getMultipleCurrencies(idsString, currencyCode) ?: return MediumDuoWidgetData(null, null, null, null)

        val data1 = ids.getOrNull(0)?.let { id -> toplist.find { it.id == id } }
        val data2 = ids.getOrNull(1)?.let { id -> toplist.find { it.id == id } }

        val bitmap1 = data1?.image?.let { url -> loadBitmapFromCache(context, url) }
        val bitmap2 = data2?.image?.let { url -> loadBitmapFromCache(context, url) }

        return MediumDuoWidgetData(data1, data2, bitmap1, bitmap2)
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

private data class MediumDuoWidgetData(
    val data1: Toplist?,
    val data2: Toplist?,
    val bitmap1: Bitmap?,
    val bitmap2: Bitmap?,
)

@Composable
private fun MediumDuoCryptoContent(
    context: Context,
    data1: Toplist?,
    data2: Toplist?,
    bitmap1: Bitmap?,
    bitmap2: Bitmap?,
) {
    Box(
        contentAlignment = Alignment.TopStart,
        modifier = GlanceModifier
            .fillMaxWidth()
            .background(WidgetColorValues.surface())
    ) {
        GlanceTheme {
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalAlignment = Alignment.Start,
            ) {
                // First small widget
                Box(modifier = GlanceModifier.defaultWeight()) {
                    SmallCryptoContent(
                        context = context,
                        data = data1,
                        bitmap = bitmap1,
                        showRefresh = false
                    )
                }

                Spacer(modifier = GlanceModifier.width(8.dp))

                // Vertical separator - 0.5 opacity, fixed height to avoid stretching the view
                Box(
                    modifier = GlanceModifier
                        .width(1.dp)
                        .height(200.dp)
                        .background(WidgetColorValues.onSurfaceDimTransparent())
                ) {}

                Spacer(modifier = GlanceModifier.width(8.dp))

                // Second small widget
                Box(modifier = GlanceModifier.defaultWeight()) {
                    SmallCryptoContent(
                        context = context,
                        data = data2,
                        bitmap = bitmap2,
                        showRefresh = true,
                        refreshWidgetType = "MediumDuoCryptoGlanceWidget"
                    )
                }
            }
        }
    }
}

/** Connects the medium duo crypto widget to Glance. Declare in AndroidManifest as the app widget receiver. */
class MediumDuoCryptoWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = MediumDuoCryptoGlanceWidget()
}
