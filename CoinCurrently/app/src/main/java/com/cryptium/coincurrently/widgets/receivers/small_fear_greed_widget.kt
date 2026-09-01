package com.cryptium.coincurrently.widgets.receivers
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.*
import android.os.Bundle
import android.view.View
import android.widget.RemoteViews
import com.cryptium.coincurrently.R
import com.cryptium.coincurrently.app.MainActivity
import com.cryptium.coincurrently.sharedui.components.GlobalFunctions
import com.cryptium.coincurrently.features.FearGreed.FearGreedRepository
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * Implementation of App Widget functionality.
 */
class small_fear_greed_widget : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            appWidgetManager.getAppWidgetInfo(appWidgetId)?.let { it ->
                it.provider?.let {
                    if (it.className.contains("small_fear_greed_widget")) {
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

internal fun createSmallFearWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int
) {

    val isDark = (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_YES) != 0
    val views = RemoteViews(context.packageName, R.layout.small_fear_greed_widget)
    views.setInt(R.id.ll_view, "setBackgroundResource", if (isDark) R.drawable.widget_background_dark else R.drawable.widget_background_light)
    views.setInt(R.id.indicator_circle, "setBackgroundResource", if (isDark) R.drawable.fear_greed_circular_indicator_white else R.drawable.fear_greed_circular_indicator)
    views.setInt(R.id.img_meter, "setBackgroundResource", if (isDark) R.drawable.small_fear_greed_meter else R.drawable.small_fear_greed_meter_dark)

    val random = (0..10000).shuffled().first()
    val intent = Intent(context, MainActivity::class.java)
    intent.putExtra("fearID", "fear")

    val pendingIntent1: PendingIntent = PendingIntent.getActivity(
            context,
            random,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

    views.setOnClickPendingIntent(R.id.btn_open, pendingIntent1)

    GlobalScope.launch {
        FearGreedRepository.getFearGreedModel()?.let {

            val rotation = it.data?.first()?.value?.toInt() ?: 0
            views.setImageViewBitmap(R.id.img_meter_arrow, createSmallMeterArrow(rotation, 320, 2000, context))
            views.setViewVisibility(R.id.img_temp_arrow, View.GONE)

            createFearGreedCell(views, R.id.cell_fear_1, it.data?.first(), context, "Now: ") {
                appWidgetManager.updateAppWidget(appWidgetId, views)
            }
        }
    }
}

fun createSmallMeterArrow(index: Int, height: Int, width: Int, context: Context): Bitmap? {

    val height = if (height > 0) height else Resources.getSystem().displayMetrics.heightPixels
    val bitmap = Bitmap.createBitmap(
        width,
        height,
        Bitmap.Config.ARGB_8888
    )

    val isDark = (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_YES) != 0
    // canvas to draw path
    val canvas = Canvas(bitmap)

    // paint to draw path fill color
    val paintFill = Paint().apply {
        isAntiAlias = true
        color = if(isDark) Color.WHITE else widgetOnSurface(context)
        style = Paint.Style.FILL
    }

    val xPos = ((canvas.width / 100) * index).toFloat() - ((canvas.height / 82) * index).toFloat()

    // create a path
    val pricePath = Path().apply {

        fillType = Path.FillType.EVEN_ODD

        moveTo(xPos, canvas.height.toFloat())

        lineTo((canvas.height / 2).toFloat() + xPos, 0f)

        lineTo(canvas.height.toFloat() + xPos, canvas.height.toFloat())

        close()
    }

    //  draw fill under path
    try {
        canvas.drawPath(pricePath, paintFill)
    } catch (e: Exception) {
        return null
    }

    return bitmap
}
