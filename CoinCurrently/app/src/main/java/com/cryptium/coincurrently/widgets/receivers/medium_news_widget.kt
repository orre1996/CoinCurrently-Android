package com.cryptium.coincurrently.widgets.receivers

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.os.Bundle
import android.widget.RemoteViews
import com.cryptium.coincurrently.R
import com.cryptium.coincurrently.core.networking.ResponseModels.NewNewsItem
import com.cryptium.coincurrently.widgets.config.WidgetConfigStore
import com.cryptium.coincurrently.widgets.config.WidgetRuntimeInitializer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MediumNewsWidgetReceiver : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        for (appWidgetId in appWidgetIds) {
            createMediumNewsWidget(context, appWidgetManager, appWidgetId)
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

internal fun createMediumNewsWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int,
) {
    WidgetRuntimeInitializer.ensureInitialized(context)

    val views = buildPlaceholderMediumNewsWidget(context, appWidgetId)
    appWidgetManager.updateAppWidget(appWidgetId, views)

    GlobalScope.launch(Dispatchers.IO) {
        try {
            val newsItems = loadWidgetNews(context, appWidgetId, limit = 2)
            val showProOverlay = widgetNewsNeedsProOverlay(context, appWidgetId)
            applyMediumNewsTheme(views, context)
            bindMediumNewsCell(views, R.id.cell_1, newsItems.getOrNull(0), context, showProOverlay)
            bindMediumNewsCell(views, R.id.cell_2, newsItems.getOrNull(1), context, showProOverlay)
            bindNewsProOverlay(views, context, showProOverlay)
            appWidgetManager.updateAppWidget(appWidgetId, views)
        } catch (_: Exception) {
            appWidgetManager.updateAppWidget(
                appWidgetId,
                buildPlaceholderMediumNewsWidget(context, appWidgetId),
            )
        }
    }
}

private fun buildPlaceholderMediumNewsWidget(
    context: Context,
    appWidgetId: Int,
): RemoteViews {
    val views = RemoteViews(context.packageName, R.layout.medium_news_widget)
    applyMediumNewsTheme(views, context)
    val cachedNews = getWidgetNewsItems(context, appWidgetId, limit = 2)
    bindMediumNewsCell(views, R.id.cell_1, cachedNews.getOrNull(0), context, showProOverlay = false)
    bindMediumNewsCell(views, R.id.cell_2, cachedNews.getOrNull(1), context, showProOverlay = false)
    bindNewsProOverlay(views, context, showOverlay = false)
    return views
}

private fun applyMediumNewsTheme(views: RemoteViews, context: Context) {
    views.setInt(
        R.id.ll_view,
        "setBackgroundResource",
        if (isWidgetDarkMode(context)) {
            R.drawable.widget_background_dark
        } else {
            R.drawable.widget_background_light
        },
    )
    views.setInt(R.id.separator_vertical, "setBackgroundColor", widgetOutlineVariant(context))
}

private fun bindMediumNewsCell(
    parent: RemoteViews,
    containerId: Int,
    newsItem: NewNewsItem?,
    context: Context,
    showProOverlay: Boolean,
) {
    val cellViews = createSmallNewsRemoteViews(context, embeddedInMedium = true)
    bindSmallNewsRemoteViews(cellViews, newsItem, context, embeddedInMedium = true)
    if (!showProOverlay) {
        setSmallNewsClickIntent(cellViews, context, newsItem?.news_url)
    }
    parent.removeAllViews(containerId)
    parent.addView(containerId, cellViews)
}
