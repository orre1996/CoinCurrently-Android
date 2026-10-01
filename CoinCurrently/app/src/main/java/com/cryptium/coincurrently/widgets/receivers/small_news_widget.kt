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
import com.cryptium.coincurrently.core.networking.ResponseModels.NewNewsItem
import com.cryptium.coincurrently.features.News.NewsRepository
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
            val items = loadWidgetNews(limit = 1)
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

    views.setViewVisibility(R.id.img_sentiment, View.GONE)

    val tickers = newsItem?.tickers?.take(2).orEmpty()
    if (tickers.isEmpty()) {
        views.setViewVisibility(R.id.ll_ticker, View.GONE)
    } else {
        views.setViewVisibility(R.id.ll_ticker, View.VISIBLE)
        views.setViewVisibility(R.id.txt_tickers, View.VISIBLE)
        views.setTextViewText(R.id.txt_tickers, tickers.joinToString(" · "))
    }

    val source = nonBreaking(newsItem?.source_name ?: "CoinCurrently")
    val formattedDate = newsItem?.date?.getDate()?.getDateTimeAgo()?.let(::nonBreaking).orEmpty()
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

internal suspend fun loadWidgetNews(limit: Int): List<NewNewsItem> {
    return NewsRepository.getNews().orEmpty().take(limit)
}

private fun nonBreaking(value: String): String = value.replace(" ", "\u00A0")
