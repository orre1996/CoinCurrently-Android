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
import androidx.glance.appwidget.background
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import com.cryptium.coincurrently.R
import com.cryptium.coincurrently.sharedui.theme.WidgetColorValues
import com.cryptium.coincurrently.core.extensions.satoshiBoldGlance
import com.cryptium.coincurrently.core.extensions.satoshiMediumGlance
import com.cryptium.coincurrently.core.extensions.satoshiRegularGlance
import com.cryptium.coincurrently.widgets.CryptoIdKey
import com.cryptium.coincurrently.widgets.OpenAppAction
import com.cryptium.coincurrently.features.News.NewsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.cryptium.coincurrently.core.networking.NewsCategory
import com.cryptium.coincurrently.core.networking.ResponseModels.NewNewsItem
import com.cryptium.coincurrently.widgets.views.NewsWidgetCard

/**
 * Medium news widget built with Jetpack Glance.
 * Displays two news articles with thumbnail image, title, source, date, and sentiment.
 */
class MediumNewsGlanceWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        UserPreferences.init(context)

        val hasPro = UserPreferences.getHasBoughtPro() != null
        val newsData = if (hasPro) {
            withContext(Dispatchers.IO) { loadNewsData(context) }
        } else {
            null
        }

        provideContent {
            Box(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(WidgetColorValues.surface()),
            ) {
                MediumNewsContent(
                    news1 = newsData?.news1,
                    news2 = newsData?.news2,
                    bitmap1 = newsData?.bitmap1,
                    bitmap2 = newsData?.bitmap2,
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
                        BuyProOverlayContent()
                    }
                }
            }
        }
    }

    private suspend fun loadNewsData(context: Context): MediumNewsWidgetData {
        val news = NewsRepository.getNewNews(3, NewsCategory.General())
        val data = news?.data ?: emptyList()
        val item1 = data.getOrNull(0)
        val item2 = data.getOrNull(1)
        val bitmap1 = item1?.image_url?.let { loadBitmapFromCache(context, it) }
        val bitmap2 = item2?.image_url?.let { loadBitmapFromCache(context, it) }
        return MediumNewsWidgetData(
            news1 = item1,
            news2 = item2,
            bitmap1 = bitmap1,
            bitmap2 = bitmap2,
        )
    }

    private suspend fun loadBitmapFromCache(context: Context, url: String): Bitmap? {
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

private data class MediumNewsWidgetData(
    val news1: NewNewsItem?,
    val news2: NewNewsItem?,
    val bitmap1: Bitmap?,
    val bitmap2: Bitmap?,
)

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
                modifier = GlanceModifier
                    .padding(top = 8.dp),
            )
        }
    }
}


private val mediumNewsImageSize = 90.dp

@Composable
private fun MediumNewsContent(
    news1: NewNewsItem?,
    news2: NewNewsItem?,
    bitmap1: Bitmap?,
    bitmap2: Bitmap?,
) {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            //.padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        // Card 1 (iOS: frame height = reader.size.height / 2)
        Box(
            modifier = GlanceModifier
                .defaultWeight()
        ) {
            NewsWidgetCard(
                newsItem = news1,
                bitmap = bitmap1,
                imageSize = mediumNewsImageSize,
            )
        }

        // Separator
        Box(
            modifier = GlanceModifier
                .fillMaxWidth()
                .height(1.dp)
                .padding(vertical = 4.dp)
                .background(WidgetColorValues.onSurfaceDimTransparent()),
        ) { }

        // Card 2
        Box(
            modifier = GlanceModifier
                .defaultWeight()
        ) {
            NewsWidgetCard(
                newsItem = news2,
                bitmap = bitmap2,
                imageSize = mediumNewsImageSize,
            )
        }
    }
}

/** Connects the medium news widget to Glance. Declare in AndroidManifest as the app widget receiver. */
class MediumNewsWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = MediumNewsGlanceWidget()
}
