package com.cryptium.coincurrently.ui.main.Widgets

import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Shader
import androidx.compose.runtime.Composable
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
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.wrapContentHeight
import androidx.glance.layout.wrapContentSize
import com.cryptium.coincurrently.R
import com.cryptium.coincurrently.ui.main.Compose.WidgetColorValues
import com.cryptium.coincurrently.ui.main.Extensions.normalise
import com.cryptium.coincurrently.ui.main.Extensions.roundOrZero
import com.cryptium.coincurrently.ui.main.Extensions.changeColorWidget
import com.cryptium.coincurrently.ui.main.Extensions.changeSymbolWidget
import com.cryptium.coincurrently.ui.main.Extensions.changeWithSymbol
import com.cryptium.coincurrently.ui.main.Extensions.formatCurrency
import com.cryptium.coincurrently.ui.main.Extensions.satoshiBoldGlance
import com.cryptium.coincurrently.ui.main.Extensions.withoutSymbol
import com.cryptium.coincurrently.ui.main.General.Enums.FormattingThreshold
import com.cryptium.coincurrently.ui.main.General.GlobalFunctions
import com.cryptium.coincurrently.ui.main.General.getRandomChart
import com.cryptium.coincurrently.ui.main.Repositories.CurrencyRepository
import com.cryptium.coincurrently.ui.main.Widgets.widgetOnSurfaceDim
import com.cryptium.coincurrently.ui.main.Widgets.widgetTrendDown
import com.cryptium.coincurrently.ui.main.Widgets.widgetTrendUp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import networking.ResponseModels.Toplist
import kotlin.math.roundToInt
import kotlin.math.pow

/**
 * Medium crypto widget built with Jetpack Glance (Compose-based app widgets).
 * Shows coin image, name/rank, price, 7d sparkline graph, and 24h/7d/30d changes.
 * Uses widget currency from GlobalFunctions.getWidgetCurrency().
 */

class MediumCryptoGlanceWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        GlobalFunctions.initSharedPreferences(context)

        val widgetData = withContext(Dispatchers.IO) {
            loadWidgetData(context)
        }

        provideContent {
            MediumCryptoContent(
                context = context,
                data = widgetData.data,
                bitmap = widgetData.bitmap,
                graphBitmap = widgetData.graphBitmap,
            )
        }
    }

    private suspend fun loadWidgetData(context: Context): MediumWidgetData {
        val widgetCurrency = GlobalFunctions.getWidgetCurrency(context)
        val firstId = widgetCurrency.firstOrNull()?.id ?: ""
        if (firstId.isEmpty()) {
            return MediumWidgetData(null, null, null)
        }

        val currencyCode = GlobalFunctions.getSelectedCurrency(context)?.code ?: "USD"
        val toplist = CurrencyRepository.getMultipleCurrencies(firstId, currencyCode)
        val data = toplist?.firstOrNull()
        val bitmap = data?.image?.let { url -> loadBitmapFromCache(context, url) }

        val graphBitmap = drawGraphBitmap(
            context = context,
            prices = data?.sparkline_in_7d?.price,
            priceChange = data?.price_change_percentage_7d_in_currency ?: 0.0,
        )

        return MediumWidgetData(data, bitmap, graphBitmap)
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

private data class MediumWidgetData(
    val data: Toplist?,
    val bitmap: Bitmap?,
    val graphBitmap: Bitmap?,
)

@Composable
internal fun MediumCryptoContent(
    context: Context,
    data: Toplist?,
    bitmap: Bitmap?,
    graphBitmap: Bitmap?,
    showRefresh: Boolean = true,
    refreshWidgetType: String = "MediumCryptoGlanceWidget",
) {
    val isDark = (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_YES) != 0

    Box(
        contentAlignment = Alignment.TopStart,
        modifier = GlanceModifier
            .background(WidgetColorValues.surface())
            .clickable(
                onClick = actionRunCallback<OpenAppAction>(
                    actionParametersOf(CryptoIdKey to (data?.id ?: ""))
                )
            )
            .padding(
                top = 8.dp,
                bottom = 4.dp
            )
    ) {
        GlanceTheme {
            Column(
                verticalAlignment = Alignment.Top,
                horizontalAlignment = Alignment.Start,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalAlignment = Alignment.Start,
                    modifier = GlanceModifier
                        .fillMaxWidth()
                        .padding(
                            top = 8.dp,
                            bottom = 4.dp
                        )
                ) {
                    ImageView(
                        data = data,
                        bitmap = bitmap,
                        isDark = isDark,
                    )

                    Spacer(modifier = GlanceModifier.width(8.dp))

                    NameRankView(
                        data = data
                    )

                    Spacer(modifier = GlanceModifier.defaultWeight())

                    PriceView(
                        data = data
                    )

                    Spacer(modifier = GlanceModifier.width(8.dp))

                    if (showRefresh) {
                        Image(
                            provider = ImageProvider(
                                if (isDark) R.drawable.ic_baseline_sync_24_dark else R.drawable.ic_baseline_sync_24_light
                            ),
                            contentDescription = "Refresh",
                            modifier = GlanceModifier
                                .size(25.dp)
                                .clickable(
                                    onClick = actionRunCallback<RefreshWidgetAction>(
                                        actionParametersOf(WidgetTypeKey to refreshWidgetType)
                                    )
                                )
                        )
                    }
                }

                // Graph
                if (graphBitmap != null && !graphBitmap.isRecycled) {
                    Image(
                        contentScale = ContentScale.FillBounds,
                        provider = ImageProvider(graphBitmap),
                        contentDescription = null,
                        modifier = GlanceModifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .padding(vertical = 8.dp),
                    )
                }

                // Bottom row: 24h / 7d / 30d changes
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalAlignment = Alignment.Start,
                    modifier = GlanceModifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                ) {
                    DailyChangeView(
                        p24 = data?.price_change_percentage_24h_in_currency
                    )

                    WeeklyChangeView(
                        p7 = data?.price_change_percentage_7d_in_currency,
                        modifier = GlanceModifier
                            .defaultWeight()
                    )

                    MonthlyChangeView(
                        p30 = data?.price_change_percentage_30d_in_currency
                    )
                }
            }
        }
    }
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
            .size(30.dp)
            .cornerRadius(if (data?.name == null) 8.dp else 0.dp),
    )
}

@Composable
private fun NameRankView(
    data: Toplist?
) {
    val rank = data?.market_cap_rank ?: 0
    val name = data?.name ?: "CoinCurrently"
    val text = if (data != null) "#$rank $name" else "CoinCurrently"

    Text(
        text = text,
        style = satoshiBoldGlance(18, WidgetColorValues.onSurface())
    )
}

@Composable
private fun PriceView(data: Toplist?) {
    val currency = GlobalFunctions.getSelectedCurrency()
    val currentPrice = data?.current_price

    Text(
        text = if (currency != null && currentPrice != null) currentPrice.formatCurrency(threshold = FormattingThreshold.NONE, currency = currency) else "$0.00",
        style = satoshiBoldGlance(18, WidgetColorValues.onSurface())
    )
}

@Composable
private fun DailyChangeView(
    p24: Double?
) {
    Row(
        horizontalAlignment = Alignment.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "24h:",
            style = satoshiBoldGlance(12, WidgetColorValues.onSurface())
        )

        Spacer(modifier = GlanceModifier.width(4.dp))

        Text(
            text = p24?.changeWithSymbol()?.withoutSymbol ?: "0.00%",
            style = satoshiBoldGlance(12, p24?.changeColorWidget() ?: WidgetColorValues.onSurface()),
        )

        Image(
            provider = ImageProvider(p24?.changeSymbolWidget() ?: R.drawable.trend_still),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            colorFilter = ColorFilter.tint(p24?.changeColorWidget() ?: WidgetColorValues.onSurfaceDim()),
            modifier = GlanceModifier.size(width = 14.dp, height = 10.dp)
        )
    }
}

@Composable
private fun WeeklyChangeView(
    p7: Double?,
    modifier: GlanceModifier
) {
    Row(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        Text(
            "7d:",
            style = satoshiBoldGlance(12, WidgetColorValues.onSurface())
        )

        Spacer(modifier = GlanceModifier.width(4.dp))

        Text(
            text = p7?.changeWithSymbol()?.withoutSymbol ?: "0.00%",
            style = satoshiBoldGlance(12, p7?.changeColorWidget() ?: WidgetColorValues.onSurface()),
        )

        Image(
            provider = ImageProvider(p7?.changeSymbolWidget() ?: R.drawable.trend_still),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            colorFilter = ColorFilter.tint(p7?.changeColorWidget() ?: WidgetColorValues.onSurfaceDim()),
            modifier = GlanceModifier.size(width = 14.dp, height = 10.dp)
        )
    }
}

@Composable
private fun MonthlyChangeView(
    p30: Double?
) {
    Row(
        horizontalAlignment = Alignment.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "30d:",
            style = satoshiBoldGlance(12, WidgetColorValues.onSurface())
        )

        Spacer(modifier = GlanceModifier.width(4.dp))

        Text(
            text = p30?.changeWithSymbol()?.withoutSymbol ?: "0.00%",
            style = satoshiBoldGlance(12, p30?.changeColorWidget() ?: WidgetColorValues.onSurface()),
        )

        Image(
            provider = ImageProvider(p30?.changeSymbolWidget() ?: R.drawable.trend_still),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            colorFilter = ColorFilter.tint(p30?.changeColorWidget() ?: WidgetColorValues.onSurfaceDim()),
            modifier = GlanceModifier.size(width = 14.dp, height = 10.dp)
        )
    }
}

/** Draws the 7d sparkline graph as a bitmap for use in Glance. */
internal fun drawGraphBitmap(
    context: Context,
    prices: List<Double>?,
    priceChange: Double,
    width: Int = 425,
    height: Int = 200,
): Bitmap? {
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    val prices = (prices ?: listOf()).ifEmpty { getRandomChart(168) }
    val minP = prices.minOrNull()?.toFloat() ?: 0f
    val maxP = prices.maxOrNull()?.toFloat() ?: 0f
    val normalizedPrices = prices.map { price ->
        if (maxP == minP) 0.5f
        else normalise(price.toFloat(), minP, maxP)
    }

    val colors = getLinearColorsMediumWidget(priceChange, context)
    val paintFill = Paint().apply {
        isAntiAlias = true
        shader = LinearGradient(
            0f, 0f, 0f, canvas.height.toFloat(),
            colors,
            null,
            Shader.TileMode.CLAMP
        )
        style = Paint.Style.FILL
    }

    val pricePath = Path().apply {
        fillType = Path.FillType.EVEN_ODD
        moveTo(-20f, normalizedPrices.first() * canvas.height)
        normalizedPrices.forEachIndexed { index, it ->
            lineTo(
                (canvas.width * index).toFloat() / (normalizedPrices.size - 1).coerceAtLeast(1),
                (1 - it) * canvas.height
            )
        }
        lineTo(1.1f * canvas.width.toFloat(), canvas.height * 1.5f)
        lineTo(-1.1f * canvas.width.toFloat(), canvas.height * 1.5f)
        close()
    }

    try {
        canvas.drawPath(pricePath, paintFill)
    } catch (e: Exception) {
        return null
    }

    val strokeColor = when {
        roundToPlaces(priceChange, 2) > 0 -> widgetTrendUp(context)
        roundToPlaces(priceChange, 2) < 0 -> widgetTrendDown(context)
        else -> widgetOnSurfaceDim(context)
    }
    val paintStroke = Paint().apply {
        isAntiAlias = true
        color = strokeColor
        strokeWidth = 2f
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }

    try {
        canvas.drawPath(pricePath, paintStroke)
    } catch (e: Exception) {
        return null
    }

    return bitmap
}

private fun roundToPlaces(value: Double, places: Int): Double {
    val divisor = 10.0.pow(places)
    return (value * divisor).roundToInt() / divisor
}

private fun getLinearColorsMediumWidget(priceChange: Double, context: Context): IntArray {
    val isDark = (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_YES) != 0
    return when {
        roundToPlaces(priceChange, 2) > 0 -> if (isDark) intArrayOf(
            Color.parseColor("#405ABF84"),
            Color.parseColor("#305ABF84"),
            Color.parseColor("#005ABF84")
        ) else intArrayOf(
            Color.parseColor("#4059B568"),
            Color.parseColor("#3059B568"),
            Color.parseColor("#0059B568")
        )
        roundToPlaces(priceChange, 2) < 0 -> if (isDark) intArrayOf(
            Color.parseColor("#40EB5757"),
            Color.parseColor("#30EB5757"),
            Color.parseColor("#00EB5757")
        ) else intArrayOf(
            Color.parseColor("#40CA5049"),
            Color.parseColor("#30CA5049"),
            Color.parseColor("#00CA5049")
        )
        else -> if (isDark) intArrayOf(
            Color.parseColor("#40C9C6C5"),
            Color.parseColor("#30C9C6C5"),
            Color.parseColor("#00C9C6C5")
        ) else intArrayOf(
            Color.parseColor("#4093A4EB"),
            Color.parseColor("#3093A4EB"),
            Color.parseColor("#0093A4EB")
        )
    }
}

/** Connects the medium crypto widget to Glance. Declare in AndroidManifest as the app widget receiver. */
class MediumCryptoWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = MediumCryptoGlanceWidget()
}
