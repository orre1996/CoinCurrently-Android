package com.cryptium.coincurrently.widgets.glance
import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
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
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.Text
import androidx.glance.layout.ContentScale
import com.cryptium.coincurrently.R
import com.cryptium.coincurrently.sharedui.theme.WidgetColorValues
import com.cryptium.coincurrently.core.extensions.getDate
import com.cryptium.coincurrently.widgets.receivers.getDateTimeAgo
import com.cryptium.coincurrently.core.extensions.satoshiBoldGlance
import com.cryptium.coincurrently.core.extensions.satoshiMediumGlance
import com.cryptium.coincurrently.core.extensions.satoshiRegularGlance
import com.cryptium.coincurrently.sharedui.components.GlobalFunctions
import com.cryptium.coincurrently.widgets.NewsIdKey
import com.cryptium.coincurrently.widgets.OpenAppAction
import com.cryptium.coincurrently.features.News.NewsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.cryptium.coincurrently.core.networking.NewsCategory
import com.cryptium.coincurrently.core.networking.ResponseModels.NewNewsItem
import com.cryptium.coincurrently.core.networking.ResponseModels.getSentimentSign

/**
 * Small news widget built with Jetpack Glance.
 * Displays a single news article with image, title, source, and sentiment.
 */
class SmallNewsGlanceWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        GlobalFunctions.initSharedPreferences(context)

        val newsData = withContext(Dispatchers.IO) {
            loadNewsData(context)
        }

        provideContent {
            SmallNewsContent(
                context = context,
                newsItem = newsData.newsItem,
                bitmap = newsData.bitmap,
            )
        }
    }

    private suspend fun loadNewsData(context: Context): NewsWidgetData {
        val news = NewsRepository.getNewNews(3, NewsCategory.General())
        val firstItem = news?.data?.firstOrNull()
        val bitmap = firstItem?.image_url?.let { url ->
            loadBitmapFromCache(context, url)
        }
        return NewsWidgetData(firstItem, bitmap)
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

private data class NewsWidgetData(
    val newsItem: NewNewsItem?,
    val bitmap: Bitmap?,
)

@Composable
private fun SmallNewsContent(
    context: Context,
    newsItem: NewNewsItem?,
    bitmap: Bitmap?,
) {
    val isDark = (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_YES) != 0

    Box(
        contentAlignment = Alignment.BottomStart,
        modifier = GlanceModifier
            .fillMaxWidth()
            .background(WidgetColorValues.surface())
            .clickable(
                onClick = actionRunCallback<OpenAppAction>(
                    actionParametersOf(NewsIdKey to (newsItem?.news_url ?: ""))
                )
            )
            .padding(12.dp),
    ) {
        GlanceTheme {
            Box(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .height(165.dp)
                    .cornerRadius(12.dp),
            ) {
                // Background image or outlined placeholder
                if (bitmap != null) {
                    Image(
                        provider = ImageProvider(bitmap),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = GlanceModifier
                            .fillMaxSize()
                            .cornerRadius(12.dp),
                    )
                } else {
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

                // Gradient overlay (dark at bottom behind text, clear at top)
                Box(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .background(
                            ImageProvider(R.drawable.small_news_overlay_gradient),
                            ContentScale.FillBounds,
                        )
                        .cornerRadius(12.dp),
                ) { }

                // Content overlay
                Column(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .padding(12.dp),
                    verticalAlignment = Alignment.Bottom,
                    horizontalAlignment = Alignment.Start,
                ) {
                    // Sentiment icon (top-left) and refresh (top-right)
                    Row(
                        modifier = GlanceModifier
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalAlignment = Alignment.Start,
                    ) {
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
                                modifier = GlanceModifier
                                    .size(width = 16.dp, height = 12.dp),
                            )
                        }

                        Spacer(modifier = GlanceModifier.defaultWeight())
                    }

                    Spacer(modifier = GlanceModifier.defaultWeight())

                    // Title
                    Text(
                        text = newsItem?.title ?: "Crypto News",
                        style = satoshiMediumGlance(14, WidgetColorValues.onSurface()),
                        maxLines = 4,
                        modifier = GlanceModifier
                            .fillMaxWidth(),
                    )

                    Spacer(modifier = GlanceModifier.height(4.dp))

                    Text(
                        text = newsItem?.source_name ?: "CoinCurrently",
                        style = satoshiRegularGlance(12, WidgetColorValues.onSurface()),
                        modifier = GlanceModifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}

/** Connects the small news widget to Glance. Declare in AndroidManifest as the app widget receiver. */
class SmallNewsWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = SmallNewsGlanceWidget()
}
