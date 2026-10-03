package com.cryptium.coincurrently.widgets.receivers

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.RemoteViews
import com.cryptium.coincurrently.R
import com.cryptium.coincurrently.app.MainActivity
import com.cryptium.coincurrently.core.extensions.getDate
import com.cryptium.coincurrently.core.networking.NewsCategory
import com.cryptium.coincurrently.core.networking.ResponseModels.NewNewsItem
import com.cryptium.coincurrently.core.networking.ResponseModels.getSentimentSign
import com.cryptium.coincurrently.features.News.NewsRepository
import com.cryptium.coincurrently.widgets.config.WidgetConfigResolver
import com.cryptium.coincurrently.widgets.config.WidgetConfigStore
import com.cryptium.coincurrently.widgets.config.WidgetRuntimeInitializer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class SmallNewsWidgetReceiver : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        for (appWidgetId in appWidgetIds) {
            createSmallNewsWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onAppWidgetOptionsChanged(
        context: Context?,
        appWidgetManager: AppWidgetManager?,
        appWidgetId: Int,
        newOptions: Bundle?,
    ) {
        if (appWidgetManager != null && context != null) {
            onUpdate(context, appWidgetManager, intArrayOf(appWidgetId))
        }
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        appWidgetIds.forEach { WidgetConfigStore.deleteNewsConfig(context, it) }
    }
}

internal fun createSmallNewsWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int,
) {
    WidgetRuntimeInitializer.ensureInitialized(context)

    val views = buildPlaceholderSmallNewsWidget(context)
    appWidgetManager.updateAppWidget(appWidgetId, views)

    GlobalScope.launch(Dispatchers.IO) {
        try {
            val items = loadWidgetNews(context, appWidgetId, limit = 1)
            bindSmallNewsRemoteViews(views, items.firstOrNull(), context, embeddedInMedium = false)
            setSmallNewsClickIntent(views, context, items.firstOrNull()?.news_url)
            appWidgetManager.updateAppWidget(appWidgetId, views)
        } catch (_: Exception) {
            appWidgetManager.updateAppWidget(appWidgetId, buildPlaceholderSmallNewsWidget(context))
        }
    }
}

private fun buildPlaceholderSmallNewsWidget(context: Context): RemoteViews {
    val views = createSmallNewsRemoteViews(context, embeddedInMedium = false)
    bindSmallNewsRemoteViews(views, null, context, embeddedInMedium = false)
    setSmallNewsClickIntent(views, context, null)
    return views
}

internal fun createSmallNewsRemoteViews(context: Context, embeddedInMedium: Boolean): RemoteViews {
    val layout = if (embeddedInMedium) R.layout.small_news_widget_cell else R.layout.small_news_widget
    return RemoteViews(context.packageName, layout)
}

internal fun bindSmallNewsRemoteViews(
    views: RemoteViews,
    newsItem: NewNewsItem?,
    context: Context,
    embeddedInMedium: Boolean,
) {
    if (!embeddedInMedium) {
        views.setInt(
            R.id.ll_view,
            "setBackgroundResource",
            if (isWidgetDarkMode(context)) {
                R.drawable.widget_background_dark
            } else {
                R.drawable.widget_background_light
            },
        )
    }
    views.setInt(
        R.id.ll_ticker,
        "setBackgroundResource",
        if (isWidgetDarkMode(context)) {
            R.drawable.widget_ticker_badge_background_dark
        } else {
            R.drawable.widget_ticker_badge_background_light
        },
    )
    views.setTextColor(R.id.txt_title, widgetOnSurface(context))
    views.setTextColor(R.id.txt_tickers, widgetOnSurface(context))
    views.setTextColor(R.id.txt_published, widgetOnSurfaceDimmer(context))

    views.setTextViewText(R.id.txt_title, newsItem?.title ?: "CoinCurrently")

    val sentiment = newsItem?.sentiment
    views.setImageViewResource(
        R.id.img_sentiment,
        sentiment?.getSentimentSign() ?: R.drawable.trend_still,
    )
    views.setInt(R.id.img_sentiment, "setColorFilter", widgetSentimentColor(context, sentiment))

    val tickers = newsItem?.tickers?.take(2).orEmpty()
    val showTickers = newsItem == null || tickers.isNotEmpty()
    if (newsItem == null || tickers.isEmpty()) {
        views.setViewVisibility(R.id.txt_tickers, View.GONE)
    } else {
        views.setViewVisibility(R.id.txt_tickers, View.VISIBLE)
        views.setTextViewText(R.id.txt_tickers, tickers.joinToString(" · "))
    }

    val density = context.resources.displayMetrics.density
    val pad = (4 * density).toInt()
    val endPad = if (showTickers && tickers.isNotEmpty()) (8 * density).toInt() else pad
    views.setViewPadding(R.id.ll_ticker, pad, pad, endPad, pad)

    val source = nonBreaking(newsItem?.source_name ?: "CoinCurrently")
    val formattedDate = newsItem?.date?.getDate()?.getDateTimeAgo(useSystemLanguage = true)?.let(::nonBreaking).orEmpty()
    views.setTextViewText(
        R.id.txt_published,
        if (formattedDate.isEmpty()) source else "$source · $formattedDate",
    )
}

internal fun setSmallNewsClickIntent(
    views: RemoteViews,
    context: Context,
    newsUrl: String?,
) {
    val intent = Intent(context, MainActivity::class.java).apply {
        newsUrl?.let { putExtra("newsID", it) }
    }
    val pendingIntent = PendingIntent.getActivity(
        context,
        (0..10000).random(),
        intent,
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
    )
    views.setOnClickPendingIntent(R.id.btn_open, pendingIntent)
}

internal fun widgetSentimentColor(context: Context, sentiment: String?): Int {
    return when (sentiment) {
        "Negative" -> widgetTrendDown(context)
        "Positive" -> widgetTrendUp(context)
        else -> widgetOnSurfaceDim(context)
    }
}

internal fun newsCountForCategory(category: NewsCategory): Int {
    return when (category) {
        is NewsCategory.General,
        is NewsCategory.Neutral,
        is NewsCategory.Positive,
        is NewsCategory.Negative,
        -> 100
        else -> 25
    }
}

internal fun filterNewsForCategory(
    news: List<NewNewsItem>,
    category: NewsCategory,
): List<NewNewsItem> {
    return when (category) {
        is NewsCategory.Positive -> news.filter { it.sentiment == "Positive" }
        is NewsCategory.Neutral -> news.filter { it.sentiment == "Neutral" }
        is NewsCategory.Negative -> news.filter { it.sentiment == "Negative" }
        else -> news
    }
}

internal suspend fun loadWidgetNews(
    context: Context,
    appWidgetId: Int,
    limit: Int,
): List<NewNewsItem> {
    val category = widgetNewsCategory(context, appWidgetId)
    val fetched = NewsRepository.getNewNews(
        newsCountForCategory(category),
        category,
        useCategoryApi = true,
    )?.data.orEmpty()
    val news = fetched.ifEmpty {
        NewsRepository.getCachedNewsItems(category, useCategoryApi = true)
    }
    return filterNewsForCategory(news, category).take(limit)
}

internal fun getWidgetNewsItems(
    context: Context,
    appWidgetId: Int,
    limit: Int = 2,
): List<NewNewsItem> {
    val category = widgetNewsCategory(context, appWidgetId)
    return filterNewsForCategory(
        NewsRepository.getCachedNewsItems(category, useCategoryApi = true),
        category,
    ).take(limit)
}

private fun widgetNewsCategory(context: Context, appWidgetId: Int): NewsCategory {
    return WidgetConfigResolver.resolveNewsCategory(
        WidgetConfigStore.getNewsConfig(context, appWidgetId),
    )
}

private fun nonBreaking(value: String): String = value.replace(" ", "\u00A0")
