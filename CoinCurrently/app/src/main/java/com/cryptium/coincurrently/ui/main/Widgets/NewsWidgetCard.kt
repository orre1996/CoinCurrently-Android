package com.cryptium.coincurrently.ui.main.Widgets

import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.glance.ColorFilter
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.ContentScale
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.text.Text
import com.cryptium.coincurrently.R
import com.cryptium.coincurrently.ui.main.Compose.WidgetColorValues
import com.cryptium.coincurrently.ui.main.Extensions.satoshiBoldGlance
import com.cryptium.coincurrently.ui.main.Extensions.satoshiMediumGlance
import com.cryptium.coincurrently.ui.main.Extensions.satoshiRegularGlance
import networking.ResponseModels.NewNewsItem
import networking.ResponseModels.getSentimentSign

/**
 * Shared news card used by Medium and Large news widgets.
 * Displays title, source, thumbnail image with sentiment badge.
 */
@Composable
fun NewsWidgetCard(
    newsItem: NewNewsItem?,
    bitmap: Bitmap?,
    imageSize: Dp,
    imageHeight: Dp = imageSize,
    maxLines: Int = 3,
    modifier: GlanceModifier = GlanceModifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                onClick = actionRunCallback<OpenAppAction>(
                    actionParametersOf(NewsIdKey to (newsItem?.news_url ?: "")),
                ),
            ),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = GlanceModifier.fillMaxSize(),
        ) {
            Column(
                modifier = GlanceModifier
                    .defaultWeight()
                    .fillMaxHeight()
                    .padding(top = 8.dp, bottom = 8.dp, start = 12.dp, end = 12.dp),
            ) {
                Text(
                    text = newsItem?.title ?: "CoinCurrently",
                    style = satoshiMediumGlance(14, WidgetColorValues.onSurface()),
                    maxLines = maxLines,
                    modifier = GlanceModifier.defaultWeight()
                )

                Spacer(modifier = GlanceModifier.height(2.dp))

                Text(
                    text = newsItem?.source_name ?: "Crypto news",
                    style = satoshiRegularGlance(12, WidgetColorValues.onSurfaceDim()),
                    maxLines = 1,
                )
            }

            Box(
                contentAlignment = Alignment.TopEnd,
                modifier = GlanceModifier
                    .padding(top = 8.dp, bottom = 8.dp, end = 8.dp)
                    .size(imageSize, imageHeight)
                    .cornerRadius(16.dp),
            ) {
                if (bitmap != null) {
                    Image(
                        provider = ImageProvider(bitmap),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = GlanceModifier
                            .fillMaxSize()
                            .cornerRadius(16.dp),
                    )
                } else {
                    // Outlined placeholder (no filled background)
                    Box(
                        modifier = GlanceModifier
                            .fillMaxSize()
                            .background(WidgetColorValues.onSurfaceDimTransparent())
                            .cornerRadius(16.dp)
                            .padding(2.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Box(
                            modifier = GlanceModifier
                                .fillMaxSize()
                                .background(WidgetColorValues.surface())
                                .cornerRadius(14.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "?",
                                style = satoshiBoldGlance(16, WidgetColorValues.onSurfaceDim()),
                            )
                        }
                    }
                }
                Box(
                    modifier = GlanceModifier.padding(top = 8.dp, end = 8.dp),
                    contentAlignment = Alignment.TopEnd,
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = GlanceModifier
                            .background(WidgetColorValues.sentimentBadgeBackground())
                            .cornerRadius(4.dp)
                            .padding(4.dp),
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
                }
            }
        }
    }
}
