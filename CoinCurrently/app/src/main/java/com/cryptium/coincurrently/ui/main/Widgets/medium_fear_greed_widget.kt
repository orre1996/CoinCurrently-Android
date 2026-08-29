package com.cryptium.coincurrently.ui.main.Widgets

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.res.Configuration
import android.graphics.*
import android.widget.RemoteViews
import networking.ResponseModels.FearGreedItem

import android.graphics.Bitmap

import android.os.Bundle
import com.cryptium.coincurrently.R
import com.cryptium.coincurrently.ui.main.General.GlobalFunctions


/**
 * Implementation of App Widget functionality.
 */
class medium_fear_greed_widget : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            appWidgetManager.getAppWidgetInfo(appWidgetId)?.let { it ->
                it.provider?.let {
                    if (it.className.contains("medium_fear_greed_widget")) {
                        updateAppWidget(context, appWidgetManager, appWidgetId)
                    }
                }
            }
        }
    }

    override fun onAppWidgetOptionsChanged(
        context: Context?,
        appWidgetManager: AppWidgetManager?,
        appWidgetId: Int,
        newOptions: Bundle?
    ) {
        if (appWidgetManager != null) {
            if (context != null) {
                onUpdate(context, appWidgetManager, intArrayOf(appWidgetId))
            }
        }
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
    }
}

fun createFearGreedCell(view: RemoteViews, id: Int, fearGreedItem: FearGreedItem?, context: Context, timeframe: String, callback: () -> Unit) {

    val views = RemoteViews(context.packageName, R.layout.fear_greed_linear_yesterday)

    views.setTextColor(R.id.txt_index, widgetOnSurface(context))
    views.setTextColor(R.id.txt_status, widgetOnSurface(context))
    views.setTextColor(R.id.txt_timeframe, widgetOnSurface(context))

   if (fearGreedItem != null) {
        views.setTextViewText(R.id.txt_index, (fearGreedItem.value?.toInt() ?: 0).toString())
        views.setTextViewText(R.id.txt_status, fearGreedItem.value_classification)
        views.setTextViewText(R.id.txt_timeframe, timeframe)

       views.setInt(R.id.indicator_circle, "setBackgroundResource", Color.TRANSPARENT)
       views.setImageViewBitmap(R.id.indicator_circle, drawHoverCircle(Color.parseColor(getColorString(fearGreedItem.value?.toInt() ?: 0, context))))
    }

    view.removeAllViews(id)
    view.addView(id, views)

    callback.invoke()

}

fun getColorString(index: Int, context: Context): String {
    val isDark = (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_YES) != 0
    return if (index < 20) {
        if (isDark) {
            "#EB5757"
        } else {
            "#A2352F"
        }
    } else if (index < 40) {
        if (isDark) {
            "#ef9152"
        } else {
            "#95430e"
        }
    } else if (index < 60) {
        if (isDark) {
            "#EAE679"
        } else {
            "#605E00"
        }
    } else if (index < 80) {
        if (isDark) {
            "#7ed12b"
        } else {
            "#3e6615"
        }
    } else {
        if (isDark) {
            "#5ABF84"
        } else {
            "#2E6737"
        }
    }
}


private fun drawHoverCircle(colorInt: Int): Bitmap? {

    val width = 200
    val height = 200
    val bitmap = Bitmap.createBitmap(
        width,
        height,
        Bitmap.Config.ARGB_8888
    )

    // canvas to draw path
    val canvas = Canvas(bitmap)

    // create circle
    val path = Path().apply {
        addCircle(100f, 100f, 100f, Path.Direction.CW)
    }

    val circleFillPaintStroke = Paint().apply {
        isDither = true
        style = Paint.Style.FILL
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
        color = colorInt
        isAntiAlias = false
    }

    // draw path on canvas
    try {
        canvas.drawPath(path, circleFillPaintStroke)
    } catch (e: Exception) {
        return null
    }

    return bitmap
}