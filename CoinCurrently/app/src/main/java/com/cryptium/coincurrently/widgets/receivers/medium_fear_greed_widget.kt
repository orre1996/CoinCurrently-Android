package com.cryptium.coincurrently.widgets.receivers
import android.content.Context
import android.content.res.Configuration
import android.graphics.*
import android.widget.RemoteViews
import com.cryptium.coincurrently.core.networking.ResponseModels.FearGreedItem
import com.cryptium.coincurrently.R

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