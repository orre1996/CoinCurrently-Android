package com.cryptium.coincurrently.widgets.receivers

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.widget.RemoteViews
import com.cryptium.coincurrently.R
import com.cryptium.coincurrently.core.localization.Localization
import com.cryptium.coincurrently.app.MainActivity
import com.cryptium.coincurrently.core.networking.ResponseModels.FearGreedItem
import com.cryptium.coincurrently.features.FearGreed.FearGreedRepository
import com.cryptium.coincurrently.widgets.config.WidgetRuntimeInitializer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class small_fear_greed_widget : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        for (appWidgetId in appWidgetIds) {
            createSmallFearWidget(context, appWidgetManager, appWidgetId)
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
}

internal fun createSmallFearWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int,
) {
    WidgetRuntimeInitializer.ensureInitialized(context)
    val views = buildPlaceholderSmallFearGreedWidget(context, appWidgetManager, appWidgetId)
    setSmallFearGreedClickIntent(views, context)
    appWidgetManager.updateAppWidget(appWidgetId, views)

    GlobalScope.launch(Dispatchers.IO) {
        try {
            val item = FearGreedRepository.getFearGreedModel()?.data?.firstOrNull()
            bindSmallFearGreedRemoteViews(
                views = views,
                context = context,
                item = item,
                meterWidthPx = getWidgetWidthPx(context, appWidgetManager, appWidgetId),
            )
            appWidgetManager.updateAppWidget(appWidgetId, views)
        } catch (_: Exception) {
            appWidgetManager.updateAppWidget(
                appWidgetId,
                buildPlaceholderSmallFearGreedWidget(context, appWidgetManager, appWidgetId),
            )
        }
    }
}

private fun buildPlaceholderSmallFearGreedWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int,
): RemoteViews {
    val views = RemoteViews(context.packageName, R.layout.small_fear_greed_widget)
    bindSmallFearGreedRemoteViews(
        views = views,
        context = context,
        item = null,
        meterWidthPx = getWidgetWidthPx(context, appWidgetManager, appWidgetId),
    )
    setSmallFearGreedClickIntent(views, context)
    return views
}

internal fun bindSmallFearGreedRemoteViews(
    views: RemoteViews,
    context: Context,
    item: FearGreedItem?,
    meterWidthPx: Int,
) {
    views.setInt(
        R.id.ll_view,
        "setBackgroundResource",
        if (isWidgetDarkMode(context)) R.drawable.widget_background_dark else R.drawable.widget_background_light,
    )
    views.setViewVisibility(R.id.img_indicator_preview, View.GONE)

    val indexValue = item?.value?.toIntOrNull()

    views.setTextColor(
        R.id.txt_index,
        if (item == null) widgetOnSurfaceDim(context) else widgetOnSurface(context),
    )
    views.setTextColor(
        R.id.txt_classification,
        widgetFearGreedIndexColor(context, indexValue ?: 0),
    )
    views.setInt(R.id.meter_fear, "setBackgroundColor", widgetFear(context))
    views.setInt(R.id.meter_neutral, "setBackgroundColor", widgetNeutral(context))
    views.setInt(R.id.meter_greed, "setBackgroundColor", widgetGreed(context))
    bindMeterEndCapBitmaps(views, context, meterWidthPx)

    views.setTextViewText(R.id.txt_index, indexValue?.toString() ?: "0")
    views.setTextViewText(
        R.id.txt_classification,
        Localization.systemString(item?.value_classification ?: fearGreedClassification(indexValue ?: 0)),
    )

    bindFearGreedMeter(
        views = views,
        context = context,
        index = indexValue ?: 0,
        widgetWidthPx = meterWidthPx,
    )
}

internal fun setSmallFearGreedClickIntent(
    views: RemoteViews,
    context: Context,
) {
    val intent = Intent(context, MainActivity::class.java).apply {
        putExtra("fearID", "fear")
    }
    val pendingIntent = PendingIntent.getActivity(
        context,
        (0..10000).random(),
        intent,
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
    )
    views.setOnClickPendingIntent(R.id.btn_open, pendingIntent)
}

internal fun widgetFearGreedIndexColor(context: Context, index: Int): Int {
    return when {
        index < 20 -> widgetExtremeFear(context)
        index < 40 -> widgetFear(context)
        index < 60 -> widgetNeutral(context)
        index < 80 -> widgetGreed(context)
        else -> widgetExtremeGreed(context)
    }
}

internal fun fearGreedClassification(index: Int): String {
    return when {
        index < 20 -> "Extreme Fear"
        index < 40 -> "Fear"
        index < 60 -> "Neutral"
        index < 80 -> "Greed"
        else -> "Extreme Greed"
    }
}

private fun getWidgetWidthPx(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int,
): Int {
    val options = appWidgetManager.getAppWidgetOptions(appWidgetId)
    val minWidthDp = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 150)
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        minWidthDp.toFloat(),
        context.resources.displayMetrics,
    ).roundToInt().coerceAtLeast(200)
}

private fun bindMeterEndCapBitmaps(
    views: RemoteViews,
    context: Context,
    widgetWidthPx: Int,
) {
    val density = context.resources.displayMetrics.density
    val horizontalPaddingPx = (32f * density).roundToInt()
    val meterWidthPx = (widgetWidthPx - horizontalPaddingPx).coerceAtLeast(1)
    val heightPx = (8f * density).roundToInt().coerceAtLeast(4)
    val segmentWidthPx = (meterWidthPx / 5f).roundToInt().coerceAtLeast(1)

    createMeterEndCapBitmap(
        widthPx = segmentWidthPx,
        heightPx = heightPx,
        color = widgetExtremeFear(context),
        roundLeft = true,
    )?.let { views.setImageViewBitmap(R.id.meter_extreme_fear, it) }

    createMeterEndCapBitmap(
        widthPx = segmentWidthPx,
        heightPx = heightPx,
        color = widgetExtremeGreed(context),
        roundRight = true,
    )?.let { views.setImageViewBitmap(R.id.meter_extreme_greed, it) }
}

private fun createMeterEndCapBitmap(
    widthPx: Int,
    heightPx: Int,
    color: Int,
    roundLeft: Boolean = false,
    roundRight: Boolean = false,
): Bitmap? {
    if (widthPx <= 0 || heightPx <= 0) return null

    val bitmap = Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        this.color = color
    }
    val radius = heightPx / 2f

    when {
        roundLeft -> {
            canvas.drawRect(radius, 0f, widthPx.toFloat(), heightPx.toFloat(), paint)
            canvas.drawRoundRect(
                RectF(0f, 0f, heightPx.toFloat(), heightPx.toFloat()),
                radius,
                radius,
                paint,
            )
        }
        roundRight -> {
            canvas.drawRect(0f, 0f, widthPx - radius, heightPx.toFloat(), paint)
            canvas.drawRoundRect(
                RectF(widthPx - heightPx.toFloat(), 0f, widthPx.toFloat(), heightPx.toFloat()),
                radius,
                radius,
                paint,
            )
        }
        else -> canvas.drawRect(0f, 0f, widthPx.toFloat(), heightPx.toFloat(), paint)
    }

    return bitmap
}

private fun bindFearGreedMeter(
    views: RemoteViews,
    context: Context,
    index: Int,
    widgetWidthPx: Int,
) {
    val density = context.resources.displayMetrics.density
    val horizontalPaddingPx = (32f * density).roundToInt()
    val meterWidthPx = (widgetWidthPx - horizontalPaddingPx).coerceAtLeast(1)
    val outerSizePx = (12f * density).roundToInt().coerceAtLeast(8)

    createFearGreedIndicatorBitmap(context)?.let { bitmap ->
        views.setImageViewBitmap(R.id.img_indicator, bitmap)
    }

    val percent = index.coerceIn(0, 100) / 100f
    val marginPx = ((meterWidthPx - outerSizePx) * percent).roundToInt()
    val marginDp = marginPx / density

    views.setViewLayoutMargin(
        R.id.img_indicator,
        RemoteViews.MARGIN_START,
        marginDp,
        TypedValue.COMPLEX_UNIT_DIP,
    )
}

private fun createFearGreedIndicatorBitmap(context: Context): Bitmap? {
    val density = context.resources.displayMetrics.density
    val outerSizePx = (12f * density).roundToInt().coerceAtLeast(8)
    val outlinePaddingPx = (2f * density).roundToInt()
    val innerSizePx = (outerSizePx - (outlinePaddingPx * 2)).coerceAtLeast(4)

    val bitmap = Bitmap.createBitmap(outerSizePx, outerSizePx, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    val outerRadius = outerSizePx / 2f
    val innerRadius = innerSizePx / 2f
    val circleCenter = outerSizePx / 2f

    val outlinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = widgetOutline(context)
    }
    val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = widgetSurfaceContainerHighest(context)
    }

    canvas.drawCircle(circleCenter, circleCenter, outerRadius, outlinePaint)
    canvas.drawCircle(circleCenter, circleCenter, innerRadius, fillPaint)

    return bitmap
}
