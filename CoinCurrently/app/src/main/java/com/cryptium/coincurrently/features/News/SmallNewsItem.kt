package com.cryptium.coincurrently.features.News
import com.cryptium.coincurrently.sharedui.modifiers.imageSize

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.invisibleToUser
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.cryptium.coincurrently.R
import com.cryptium.coincurrently.core.localization.Localization
import com.cryptium.coincurrently.sharedui.theme.*
import com.cryptium.coincurrently.sharedui.modifiers.*
import com.cryptium.coincurrently.sharedui.components.AccessibilityLayout
import com.cryptium.coincurrently.sharedui.components.AdaptiveStack
import com.cryptium.coincurrently.sharedui.components.CachedUrlImage
import com.cryptium.coincurrently.core.extensions.getDate
import com.cryptium.coincurrently.core.extensions.getEmptyStringLength
import com.cryptium.coincurrently.core.extensions.satoshiBold
import com.cryptium.coincurrently.widgets.receivers.getDateTimeAgo
import com.cryptium.coincurrently.core.networking.ResponseModels.NewNewsItem
import com.cryptium.coincurrently.core.networking.ResponseModels.getSentimentColor
import com.cryptium.coincurrently.core.networking.ResponseModels.getSentimentSign

enum class NewsMediaType(val rawValue: String) {
    Article("article"),
    Video("video"),
    Podcast("podcast"),
}

@Composable
fun SmallNewsItem(
    newsItem: NewNewsItem?,
    callback: (String) -> Unit,
    index: Int = 0,
    totalCount: Int = 0,
    newsType: NewsMediaType = NewsMediaType.Article,
) {
    val isHorizontal = AccessibilityLayout.isHorizontal
    val accessibilityLabelText = newsAccessibilityLabel(
        newsType = newsType,
        index = index,
        totalCount = totalCount,
        newsItem = newsItem,
    )

    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 80.dp)
            .height(IntrinsicSize.Min)
            .semantics(mergeDescendants = true) {
                contentDescription = accessibilityLabelText
                onClick(label = Localization.string("Double tap to read more"), action = {
                    newsItem?.news_url?.let { callback(it) }
                    true
                })
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
            ) {
                newsItem?.news_url?.let {
                    callback(it)
                }
            }
    ) {
        NewsImageView(
            newsItem?.image_url,
        )

        Column(
            verticalArrangement = Arrangement.Top,
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            TitleView(
                newsItem?.title
            )

            Spacer(modifier = Modifier.weight(1f))

            if (isHorizontal) {
                FlowRow(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    itemVerticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(top = 8.dp),
                ) {
                    TickersView(
                        newsItem,
                        newsItem?.tickers,
                        newsItem?.sentiment
                    )

                    AuthorView(
                        newsItem?.source_name,
                        newsItem?.date,
                    )
                }
            } else {
                AdaptiveStack(
                    isHorizontal = false,
                    spacing = 4.dp,
                    horizontalAlignment = Alignment.Start,
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                ) {
                    TickersView(
                        newsItem,
                        newsItem?.tickers,
                        newsItem?.sentiment
                    )

                    AuthorView(
                        newsItem?.source_name,
                        newsItem?.date,
                        modifier = sectionModifier(),
                    )
                }
            }
        }
    }
}

@Composable
private fun NewsImageView(
    imageUrl: String?,
) {
    CachedUrlImage(
        model = imageUrl,
        size = 80.dp,
        scaleWithFontSize = false,
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .semantics { invisibleToUser() },
    )
}

private fun newsAccessibilityLabel(
    newsType: NewsMediaType,
    index: Int,
    totalCount: Int,
    newsItem: NewNewsItem?,
): String {
    val kind = Localization.string(
        when (newsType) {
            NewsMediaType.Article -> "Article"
            NewsMediaType.Video -> "Video"
            NewsMediaType.Podcast -> "Podcast"
        },
    )
    val position = Localization.format("%1\$@ %2\$d of %3\$d", kind, index, totalCount)
    val title = newsItem?.title.orEmpty()
    val source = Localization.format("By %@", newsItem?.source_name ?: "000000")
    val published = Localization.format(
        "Published %@",
        newsItem?.date?.getDate()?.getDateTimeAgo() ?: "000000",
    )
    val sentimentValue = newsItem?.sentiment.orEmpty()
    val sentiment = if (sentimentValue.isEmpty()) {
        null
    } else {
        Localization.format(
            "Sentiment, %@",
            when (sentimentValue) {
                "Positive", "Negative", "Neutral" -> Localization.string(sentimentValue)
                else -> sentimentValue
            },
        )
    }
    val tickers = newsItem?.tickers?.take(3)?.filter { it.isNotEmpty() }.orEmpty()
    val description = listOfNotNull(
        "$position. $title. $source. $published",
        sentiment,
    ).joinToString(". ")
    return if (tickers.isEmpty()) {
        description
    } else {
        "$description. ${Localization.format("Tickers, %@", tickers.joinToString(", "))}"
    }
}

@Composable
private fun TitleView(
    title: String?
) {
    Text(
        text = title ?: String().getEmptyStringLength(20),
        color = MaterialTheme.colors.onSurface,
        style = satoshiBold(16),
        overflow = TextOverflow.Ellipsis,
        maxLines = 2,
        modifier = Modifier
            .shimmeringModifier(title == null)
            .semantics { invisibleToUser() }
    )
}

@Composable
private fun AuthorView(
    sourceName: String?,
    date: String?,
    modifier: Modifier = Modifier,
) {
    val author = sourceName ?: "000000"
    val formattedDate = date?.getDate()?.getDateTimeAgo() ?: "000000"

    Text(
        text = "$author · $formattedDate",
        color = MaterialTheme.colors.onSurfaceContainerDimmer,
        style = satoshiBold(12),
        overflow = TextOverflow.Ellipsis,
        maxLines = 2,
        modifier = modifier
            .shimmeringModifier(sourceName == null || date == null)
            .semantics { invisibleToUser() }
    )
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
private fun TickersView(
    news: NewNewsItem?,
    tickers: List<String>?,
    sentiment: String?,
    modifier: Modifier = Modifier,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier
            .shimmeringModifier(news == null)
            .semantics { invisibleToUser() }
            .clip(RoundedCornerShape(8.dp))
            .background(
                color = MaterialTheme.colors.surfaceContainerHigh,
                shape = RoundedCornerShape(size = 8.dp)
            )
            .padding(
                top = 4.dp,
                start = 4.dp,
                end = if (news == null || tickers?.isEmpty() == false) 8.dp else 4.dp,
                bottom = 4.dp
            )
    ) {
        MinimumTextSize(
            text = "X",
            font = satoshiBold(12)
        ) {
        if (news == null || sentiment != null) {
            Image(
                painter = painterResource(sentiment?.getSentimentSign() ?: R.drawable.trend_still),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                colorFilter = ColorFilter.tint(
                    sentiment?.getSentimentColor() ?: MaterialTheme.colors.onSurfaceDim
                ),
                modifier = Modifier
                    .imageSize(16.dp, 12.dp),
            )
        }
        }

        if (news == null || tickers?.isEmpty() == false) {
            Text(
                text = tickers?.take(3)?.joinToString(" · ") { it } ?: "000",
                color = MaterialTheme.colors.onSurface,
                style = satoshiBold(12),
                maxLines = 1
            )
        }
    }
}