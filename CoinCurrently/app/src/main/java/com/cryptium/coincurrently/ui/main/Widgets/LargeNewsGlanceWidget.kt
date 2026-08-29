package com.cryptium.coincurrently.ui.main.Widgets

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
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.ContentScale
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import com.cryptium.coincurrently.R
import com.cryptium.coincurrently.ui.main.Compose.WidgetColorValues
import com.cryptium.coincurrently.ui.main.Extensions.satoshiBoldGlance
import com.cryptium.coincurrently.ui.main.Extensions.satoshiMediumGlance
import com.cryptium.coincurrently.ui.main.Extensions.satoshiRegularGlance
import com.cryptium.coincurrently.ui.main.General.GlobalFunctions
import com.cryptium.coincurrently.ui.main.Repositories.NewsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import networking.NewsCategory
import networking.ResponseModels.NewNewsItem
import networking.ResponseModels.getSentimentSign

/**
 * Large news widget built with Jetpack Glance.
 * Displays a featured article (large image) plus two compact news cards below.
 */
class LargeNewsGlanceWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        GlobalFunctions.initSharedPreferences(context)

        val hasPro = GlobalFunctions.getHasBoughtPro() != null
        val newsData = if (hasPro) {
            withContext(Dispatchers.IO) { loadNewsData(context) }
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
                LargeNewsContent(
                    news1 = newsData?.news1,
                    news2 = newsData?.news2,
                    news3 = newsData?.news3,
                    bitmap1 = newsData?.bitmap1,
                    bitmap2 = newsData?.bitmap2,
                    bitmap3 = newsData?.bitmap3,
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

    private suspend fun loadNewsData(context: Context): LargeNewsWidgetData {
        val news = NewsRepository.getNewNews(3, NewsCategory.General())
        val data = news?.data ?: emptyList()
        val item1 = data.getOrNull(0)
        val item2 = data.getOrNull(1)
        val item3 = data.getOrNull(2)
        val bitmap1 = item1?.image_url?.let { loadBitmapFromCache(context, it) }
        val bitmap2 = item2?.image_url?.let { loadBitmapFromCache(context, it) }
        val bitmap3 = item3?.image_url?.let { loadBitmapFromCache(context, it) }
        return LargeNewsWidgetData(
            news1 = item1,
            news2 = item2,
            news3 = item3,
            bitmap1 = bitmap1,
            bitmap2 = bitmap2,
            bitmap3 = bitmap3,
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

private data class LargeNewsWidgetData(
    val news1: NewNewsItem?,
    val news2: NewNewsItem?,
    val news3: NewNewsItem?,
    val bitmap1: Bitmap?,
    val bitmap2: Bitmap?,
    val bitmap3: Bitmap?,
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
                modifier = GlanceModifier.padding(top = 8.dp),
            )
        }
    }
}

@Composable
private fun LargeNewsContent(
    news1: NewNewsItem?,
    news2: NewNewsItem?,
    news3: NewNewsItem?,
    bitmap1: Bitmap?,
    bitmap2: Bitmap?,
    bitmap3: Bitmap?,
) {
    Column(
        modifier = GlanceModifier.fillMaxSize(),
    ) {
        // Featured article (weight 2 of 4)
        Box(
            modifier = GlanceModifier
                .fillMaxWidth()
                .height(150.dp)
        ) {
            LargeNewsFeaturedCard(
                newsItem = news1,
                bitmap = bitmap1,
            )
        }

        // Separator
        Box(
            modifier = GlanceModifier
                .fillMaxWidth()
                .height(1.dp)
                .background(WidgetColorValues.onSurfaceDimTransparent()),
        ) { }

        // Card 2 (weight 1 of 4)
        Box(
            modifier = GlanceModifier
                .fillMaxWidth()
                .height(90.dp)
        ) {
            NewsWidgetCard(
                newsItem = news2,
                bitmap = bitmap2,
                imageSize = 90.dp,
            )
        }

        // Separator
        Box(
            modifier = GlanceModifier
                .fillMaxWidth()
                .height(1.dp)
                .background(WidgetColorValues.onSurfaceDimTransparent()),
        ) { }

        // Card 3 (weight 1 of 4)
        Box(
            modifier = GlanceModifier
                .fillMaxWidth()
                .height(90.dp)
        ) {
            NewsWidgetCard(
                newsItem = news3,
                bitmap = bitmap3,
                imageSize = 90.dp,
            )
        }
    }
}

@Composable
private fun LargeNewsFeaturedCard(
    newsItem: NewNewsItem?,
    bitmap: Bitmap?,
    modifier: GlanceModifier = GlanceModifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .clickable(
                onClick = actionRunCallback<OpenAppAction>(
                    actionParametersOf(NewsIdKey to (newsItem?.news_url ?: "")),
                ),
            ),
    ) {
        // Background image or placeholder
        if (bitmap != null) {
            Image(
                provider = ImageProvider(bitmap),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = GlanceModifier.fillMaxSize(),
            )
        } else {
            // Outlined placeholder (no filled background)
            Box(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(WidgetColorValues.onSurfaceDimTransparent())
                    .cornerRadius(12.dp)
                    .padding(2.dp),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .background(WidgetColorValues.surface())
                        .cornerRadius(10.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "?",
                        style = satoshiBoldGlance(24, WidgetColorValues.onSurfaceDim()),
                    )
                }
            }
        }

        // Gradient overlay (transparent top, dark bottom)
        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(
                    ImageProvider(R.drawable.large_news_card_fade_widget_large),
                    ContentScale.FillBounds,
                ),
        ) { }

        // Content overlay
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .padding(12.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalAlignment = Alignment.Start,
        ) {
            // Sentiment badge (top-left)
            Box(
                contentAlignment = Alignment.Center,
                modifier = GlanceModifier
                    .padding(4.dp)
                    .background(WidgetColorValues.sentimentBadgeBackground())
                    .cornerRadius(4.dp),
            ) {
                Image(
                    provider = ImageProvider(
                        newsItem?.sentiment?.getSentimentSign() ?: R.drawable.trend_still
                    ),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    colorFilter = ColorFilter.tint(
                        when (newsItem?.sentiment) {
                            "Negative" -> WidgetColorValues.trendDown()
                            "Positive" -> WidgetColorValues.trendUp()
                            else -> WidgetColorValues.onSurfaceDim()
                        }
                    ),
                    modifier = GlanceModifier.size(width = 16.dp, height = 12.dp),
                )
            }

            Spacer(modifier = GlanceModifier.defaultWeight())

            // Title
            Text(
                text = newsItem?.title ?: "Crypto News",
                style = satoshiMediumGlance(14, WidgetColorValues.onSurface()),
                modifier = GlanceModifier.fillMaxWidth(),
                maxLines = 4,
            )

            Spacer(modifier = GlanceModifier.height(4.dp))

            Text(
                text = newsItem?.source_name ?: "CoinCurrently",
                style = satoshiRegularGlance(12, WidgetColorValues.onSurface()),
                modifier = GlanceModifier.fillMaxWidth(),
                maxLines = 1,
            )
        }
    }
}

/** Connects the large news widget to Glance. Declare in AndroidManifest as the app widget receiver. */
class LargeNewsWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = LargeNewsGlanceWidget()
}
