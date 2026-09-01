package com.cryptium.coincurrently.widgets.glance
import android.content.Context
import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import coil.Coil
import coil.request.ImageRequest
import coil.request.SuccessResult
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
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
import com.cryptium.coincurrently.sharedui.theme.WidgetColorValues
import com.cryptium.coincurrently.core.extensions.satoshiBoldGlance
import com.cryptium.coincurrently.sharedui.components.GlobalFunctions
import com.cryptium.coincurrently.widgets.CryptoIdKey
import com.cryptium.coincurrently.widgets.OpenAppAction
import com.cryptium.coincurrently.core.persistence.CurrencyRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.cryptium.coincurrently.core.networking.ResponseModels.Toplist
import com.cryptium.coincurrently.core.networking.ResponseModels.getPreferredChange
import com.cryptium.coincurrently.widgets.views.MultiWidgetCellContent
import com.cryptium.coincurrently.widgets.receivers.drawGraphBitmap

/**
 * Large multi crypto widget built with Jetpack Glance.
 * Shows 8 crypto cells in a 4x2 grid. Uses widget currency from GlobalFunctions.getWidgetCurrency().
 * Shows buy pro overlay when user hasn't purchased Pro.
 */
class LargeMultiCryptoGlanceWidget : GlanceAppWidget() {

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
                    .height(330.dp)
                    .background(WidgetColorValues.surface()),
            ) {
                LargeMultiCryptoContent(
                    context = context,
                    widgetData = widgetData,
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

    private suspend fun loadWidgetData(context: Context): LargeMultiWidgetData {
        val widgetCurrency = GlobalFunctions.getWidgetCurrency(context)
        val ids = widgetCurrency.take(8).mapNotNull { it.id }
        if (ids.isEmpty()) {
            return LargeMultiWidgetData(emptyList(), emptyList(), emptyList())
        }

        val currencyCode = GlobalFunctions.getSelectedCurrency(context)?.code ?: "USD"
        val idsString = ids.joinToString(",")
        val toplist = CurrencyRepository.getMultipleCurrencies(idsString, currencyCode)
            ?: return LargeMultiWidgetData(emptyList(), emptyList(), emptyList())

        val dataList = ids.map { id -> toplist.find { it.id == id } }
        val bitmapList = dataList.map { it?.image?.let { url -> loadBitmapFromCache(context, url) } }
        val graphBitmapList = dataList.map { d ->
            d?.let {
                drawGraphBitmap(
                    context = context,
                    prices = it.sparkline_in_7d?.price,
                    priceChange = it.getPreferredChange(),
                    width = 90,
                    height = 26,
                )
            }
        }

        return LargeMultiWidgetData(dataList, bitmapList, graphBitmapList)
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

private data class LargeMultiWidgetData(
    val dataList: List<Toplist?>,
    val bitmapList: List<Bitmap?>,
    val graphBitmapList: List<Bitmap?>,
)

@Composable
private fun LargeMultiCryptoContent(
    context: Context,
    widgetData: LargeMultiWidgetData?,
) {
    val data = widgetData?.dataList
    val bitmaps = widgetData?.bitmapList
    val graphs = widgetData?.graphBitmapList

    fun get(idx: Int) = data?.getOrNull(idx)
    fun bitmap(idx: Int) = bitmaps?.getOrNull(idx)
    fun graph(idx: Int) = graphs?.getOrNull(idx)

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
                MultiWidgetRow(
                    context = context,
                    data1 = get(0),
                    data2 = get(1),
                    bitmap1 = bitmap(0),
                    bitmap2 = bitmap(1),
                    graph1 = graph(0),
                    graph2 = graph(1),
                    showRefresh = true,
                )

                SeparatorH()

                // Row 2: Cell 3 | Cell 4
                MultiWidgetRow(
                    context = context,
                    data1 = get(2),
                    data2 = get(3),
                    bitmap1 = bitmap(2),
                    bitmap2 = bitmap(3),
                    graph1 = graph(2),
                    graph2 = graph(3),
                    showRefresh = false,
                )

                SeparatorH()

                // Row 3: Cell 5 | Cell 6
                MultiWidgetRow(
                    context = context,
                    data1 = get(4),
                    data2 = get(5),
                    bitmap1 = bitmap(4),
                    bitmap2 = bitmap(5),
                    graph1 = graph(4),
                    graph2 = graph(5),
                    showRefresh = false,
                )

                SeparatorH()

                // Row 4: Cell 7 | Cell 8
                MultiWidgetRow(
                    context = context,
                    data1 = get(6),
                    data2 = get(7),
                    bitmap1 = bitmap(6),
                    bitmap2 = bitmap(7),
                    graph1 = graph(6),
                    graph2 = graph(7),
                    showRefresh = false,
                )
            }
        }
    }
}

@Composable
private fun MultiWidgetRow(
    context: Context,
    data1: Toplist?,
    data2: Toplist?,
    bitmap1: Bitmap?,
    bitmap2: Bitmap?,
    graph1: Bitmap?,
    graph2: Bitmap?,
    showRefresh: Boolean,
) {
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
                graphBitmap = graph1,
                showRefresh = showRefresh,
                refreshWidgetType = "LargeMultiCryptoGlanceWidget",
            )
        }

        Box(
            modifier = GlanceModifier
                .width(1.dp)
                .height(82.dp)
                .background(WidgetColorValues.onSurfaceDimTransparent()),
        ) {}

        Box(modifier = GlanceModifier.defaultWeight()) {
            MultiWidgetCellContent(
                context = context,
                data = data2,
                bitmap = bitmap2,
                graphBitmap = graph2,
                showRefresh = false,
            )
        }
    }
}

@Composable
private fun SeparatorH() {
    Box(
        modifier = GlanceModifier
            .fillMaxWidth()
            .height(1.dp)
            .background(WidgetColorValues.onSurfaceDimTransparent()),
    ) {}
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

/** Connects the large multi crypto widget to Glance. Declare in AndroidManifest as the app widget receiver. */
class LargeMultiCryptoWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = LargeMultiCryptoGlanceWidget()
}
