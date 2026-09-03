package com.cryptium.coincurrently.widgets.glance
import com.cryptium.coincurrently.core.persistence.UserPreferences
import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import androidx.glance.ColorFilter
import coil.Coil
import coil.request.ImageRequest
import coil.request.SuccessResult
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.ImageProvider
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.Text
import androidx.glance.Image
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.layout.ContentScale
import com.cryptium.coincurrently.R
import com.cryptium.coincurrently.sharedui.theme.WidgetColorValues
import com.cryptium.coincurrently.core.extensions.roundOrZero
import com.cryptium.coincurrently.core.extensions.changeColorWidget
import com.cryptium.coincurrently.core.extensions.changeSymbolWidget
import com.cryptium.coincurrently.core.extensions.changeWithSymbol
import com.cryptium.coincurrently.core.extensions.formatCurrency
import com.cryptium.coincurrently.core.extensions.satoshiBoldGlance
import com.cryptium.coincurrently.core.extensions.withoutSymbol
import com.cryptium.coincurrently.sharedui.models.FormattingThreshold
import com.cryptium.coincurrently.widgets.CryptoIdKey
import com.cryptium.coincurrently.widgets.OpenAppAction
import com.cryptium.coincurrently.widgets.RefreshWidgetAction
import com.cryptium.coincurrently.widgets.WidgetTypeKey
import com.cryptium.coincurrently.core.persistence.CurrencyRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.cryptium.coincurrently.core.networking.ResponseModels.Toplist

/**
 * Small crypto widget built with Jetpack Glance (Compose-based app widgets).
 * See: https://developer.android.com/develop/ui/compose/glance/create-app-widget
 */

class SmallCryptoGlanceWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        UserPreferences.init(context)

        val widgetData = withContext(Dispatchers.IO) {
            loadWidgetData(context)
        }

        provideContent {
            SmallCryptoContent(
                context = context,
                data = widgetData.data,
                bitmap = widgetData.bitmap,
            )
        }
    }

    private suspend fun loadWidgetData(context: Context): WidgetData {
        val currency = UserPreferences.getFavorites()
        val firstId = currency.firstOrNull() ?: ""
        if (firstId.isEmpty()) {
            return WidgetData(null, null)
        }

        val currencyCode = UserPreferences.getSelectedCurrency(context)?.code ?: "USD"
        val toplist = CurrencyRepository.getMultipleCurrencies(firstId, currencyCode)
        val data = toplist?.firstOrNull()
        val bitmap = data?.image?.let { url -> loadBitmapFromCache(context, url) }
        return WidgetData(data, bitmap)
    }

    /** Load bitmap with allowHardware(false) so ImageProvider(bitmap) works in app widgets. */
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

private data class WidgetData(
    val data: Toplist?,
    val bitmap: Bitmap?,
)

@Composable
internal fun SmallCryptoContent(
    context: Context,
    data: Toplist?,
    bitmap: Bitmap?,
    showRefresh: Boolean = true,
    refreshWidgetType: String = "SmallCryptoGlanceWidget",
    contentPadding: Dp = 16.dp,
) {
    val isDark = (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_YES) != 0

    Box(
        contentAlignment = Alignment.TopStart,
        modifier = GlanceModifier
            .fillMaxWidth()
            .background(WidgetColorValues.surface())
            .clickable(
                onClick = actionRunCallback<OpenAppAction>(
                    actionParametersOf(CryptoIdKey to (data?.id ?: ""))
                )
            )
            .padding(contentPadding),
    ) {
        GlanceTheme {

            Column(
                verticalAlignment = Alignment.Top,
                horizontalAlignment = Alignment.Start,
                modifier = GlanceModifier.fillMaxWidth()
            ) {
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalAlignment = Alignment.Start,
                ) {
                    NameView(
                        data = data,
                        modifier = GlanceModifier
                            .defaultWeight()
                    )

                    ImageView(
                        data = data,
                        bitmap = bitmap,
                        isDark = isDark
                    )
                }

                // Price
                PriceView(
                    data = data
                )

                // Bottom row: 24h/7d/30d changes (left) + reload button (right)
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalAlignment = Alignment.Start,
                    modifier = GlanceModifier.fillMaxWidth()
                ) {
                    // 24h / 7d / 30d
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalAlignment = Alignment.Start,
                    ) {
                        TimeframeViews()

                        Spacer(modifier = GlanceModifier.width(8.dp))

                        val p24 = data?.price_change_percentage_24h_in_currency?.roundOrZero(2)
                        val p7 = data?.price_change_percentage_7d_in_currency?.roundOrZero(2)
                        val p30 = data?.price_change_percentage_30d_in_currency?.roundOrZero(2)

                        ChangeView(
                            p24,
                            p7,
                            p30
                        )

                        Spacer(modifier = GlanceModifier.width(4.dp))

                        ChangeSymbolView(
                            p24,
                            p7,
                            p30
                        )
                    }

                    Spacer(modifier = GlanceModifier.defaultWeight())

                    if (showRefresh) {
                        Image(
                            provider = ImageProvider(
                                if (isDark) R.drawable.ic_baseline_sync_24_dark else R.drawable.ic_baseline_sync_24_light
                            ),
                            contentDescription = "Refresh",
                            modifier = GlanceModifier
                                .size(25.dp)
                                .clickable(onClick = actionRunCallback<RefreshWidgetAction>(
                                    actionParametersOf(WidgetTypeKey to refreshWidgetType)
                                ))
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NameView(
    data: Toplist?,
    modifier: GlanceModifier
) {
    Text(
        text = data?.let { "${it.symbol?.uppercase()}\n${it.name}" } ?: "Coin\nCurrently",
        style = satoshiBoldGlance(20, WidgetColorValues.onSurface()),
        modifier = modifier
    )
}

@Composable
private fun ImageView(
    data: Toplist?,
    bitmap: Bitmap?,
    isDark: Boolean
) {
    Image(
        provider = when {
            bitmap != null -> ImageProvider(bitmap)
            isDark -> ImageProvider(R.drawable.logo_dark)
            else -> ImageProvider(R.drawable.logo_light)
        },
        contentDescription = null,
        modifier = GlanceModifier
            .size(40.dp)
            .cornerRadius(if (data?.name == null) 8.dp else 0.dp),
    )
}

@Composable
private fun PriceView(
    data: Toplist?
) {
    val currentPrice = data?.current_price

    Text(
        text = currentPrice?.formatCurrency(threshold = FormattingThreshold.NONE) ?: "$0.00",
        style = satoshiBoldGlance(24, WidgetColorValues.onSurface()),
        modifier = GlanceModifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    )
}

@Composable
private fun TimeframeViews() {
    Column(horizontalAlignment = Alignment.Start) {
        Text(
            "24h:",
            style = satoshiBoldGlance(14, WidgetColorValues.onSurface())
        )

        Text(
            "7d:",
            style = satoshiBoldGlance(14, WidgetColorValues.onSurface())
        )

        Text(
            "30d:",
            style = satoshiBoldGlance(14, WidgetColorValues.onSurface())
        )
    }
}

@Composable
private fun ChangeView(
    p24: Double?,
    p7: Double?,
    p30: Double?
) {
    Column(horizontalAlignment = Alignment.Start) {
        Text(
            text = p24?.changeWithSymbol()?.withoutSymbol ?: "0.00%",
            style = satoshiBoldGlance(14, p24?.changeColorWidget() ?: WidgetColorValues.onSurface()),
        )
        Text(
            text = p7?.changeWithSymbol()?.withoutSymbol ?: "0.00%",
            style = satoshiBoldGlance(14, p7?.changeColorWidget() ?: WidgetColorValues.onSurface()),
        )
        Text(
            text = p30?.changeWithSymbol()?.withoutSymbol ?: "0.00%",
            style = satoshiBoldGlance(14, p30?.changeColorWidget() ?: WidgetColorValues.onSurface()),
        )
    }
}

@Composable
private fun ChangeSymbolView(
    p24: Double?,
    p7: Double?,
    p30: Double?
) {
    Column(
        horizontalAlignment = Alignment.Start
    ) {
        Image(
            provider = ImageProvider(p24?.changeSymbolWidget() ?: R.drawable.trend_still),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            colorFilter = ColorFilter.tint(p24?.changeColorWidget() ?: WidgetColorValues.onSurfaceDim()),
            modifier = GlanceModifier.size(width = 14.dp, height = 10.dp)
        )

        Spacer(modifier = GlanceModifier.height(9.dp))

        Image(
            provider = ImageProvider(p7?.changeSymbolWidget() ?: R.drawable.trend_still),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            colorFilter = ColorFilter.tint(p7?.changeColorWidget() ?: WidgetColorValues.onSurfaceDim()),
            modifier = GlanceModifier
                .size(width = 14.dp, height = 10.dp)
        )

        Spacer(modifier = GlanceModifier.height(9.dp))

        Image(
            provider = ImageProvider(p30?.changeSymbolWidget() ?: R.drawable.trend_still),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            colorFilter = ColorFilter.tint(p30?.changeColorWidget() ?: WidgetColorValues.onSurfaceDim()),
            modifier = GlanceModifier.size(width = 14.dp, height = 10.dp)
        )
    }
}

/** Connects the small crypto widget to Glance. Declare in AndroidManifest as the app widget receiver. */
class SmallCryptoWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = SmallCryptoGlanceWidget()
}