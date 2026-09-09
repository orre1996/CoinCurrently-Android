package com.cryptium.coincurrently.widgets.receivers
import android.content.Context
import android.content.res.Configuration
import android.graphics.*
import com.cryptium.coincurrently.core.extensions.normalise
import com.cryptium.coincurrently.core.extensions.roundTo
import com.cryptium.coincurrently.core.networking.ResponseModels.Pie
import kotlin.math.pow
import kotlin.math.roundToInt

fun drawPieChart(currencies: List<Pie>, context: Context, mediumWidget: Boolean): Bitmap? { //canvas to draw on it
    return null
    /*val bitmap = Bitmap.createBitmap(
        300,
        300,
        Bitmap.Config.ARGB_8888
    )

    val colors = currencies.map { it.color }.toMutableList()
    val slices = currencies.map { it.percent }.toMutableList()

    if (colors.isEmpty()) {
     colors.add(widgetOnSurfaceDim(context))
    }

    if (slices.isEmpty()) {
        slices.add(1.0)
    }

    // canvas to draw path
    val canvas = Canvas(bitmap)
    val box = RectF(2f, 2f, (bitmap.width - 2).toFloat(), (bitmap.height - 2).toFloat())
    //get value for 100%
    var sum = 0f
    for (slice in slices) {
        sum += slice?.toFloat() ?: 0f
    }
    //initalize painter
    val paint = Paint()
    paint.isAntiAlias = true
    paint.strokeWidth = 1f
    paint.style = Paint.Style.FILL_AND_STROKE
    var start = 0f
    //draw slices

    for (i in slices.indices) {
        val color = Color.rgb(colors[i]?.red ?: 0, colors[i]?.green ?: 0, colors[i]?.blue ?: 0).toHexString().uppercase().substring(2)
        paint.color = Color.parseColor("#$color")
        val angle: Float = 360.0f / sum * (slices[i]?.toFloat() ?: 0f)

        try {
            canvas.drawArc(box, start, angle, true, paint)
        } catch (e: Exception) {
            return null
        }

        start += angle
    }

    val box1 = RectF(30f, 30f, (bitmap.width - 30).toFloat(), (bitmap.height - 30).toFloat())

    paint.style = Paint.Style.FILL_AND_STROKE

    val isDark = (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_YES) != 0
    val darkColor = if(mediumWidget) Color.parseColor("#252864") else Color.parseColor("#2b2e80")
    val lightColor = if(mediumWidget) Color.parseColor("#e6e6fa") else Color.parseColor("#dcdcfa")
    paint.color = if(isDark) darkColor else lightColor
    val angle = 360.0f

    try {
        canvas.drawArc(box1, 0f, angle, true, paint)
    } catch (e: Exception) {
        return null
    }


    return bitmap*/
}

/** Draws the 7d sparkline graph as a bitmap for use in Glance. */
internal fun drawGraphBitmap(
    context: Context,
    prices: List<Double>?,
    priceChange: Double,
    width: Int = 425,
    height: Int = 200,
): Bitmap? {
    if (prices.isNullOrEmpty()) return null

    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    val minP = prices.minOrNull()?.toFloat() ?: 0f
    val maxP = prices.maxOrNull()?.toFloat() ?: 0f
    val normalizedPrices = prices.map { price ->
        if (maxP == minP) 0.5f
        else normalise(price.toFloat(), minP, maxP)
    }

    val colors = getLinearColorsMediumWidget(priceChange, context)
    val paintFill = Paint().apply {
        isAntiAlias = true
        shader = LinearGradient(
            0f, 0f, 0f, canvas.height.toFloat(),
            colors,
            null,
            Shader.TileMode.CLAMP
        )
        style = Paint.Style.FILL
    }

    val pricePath = Path().apply {
        fillType = Path.FillType.EVEN_ODD
        moveTo(-20f, normalizedPrices.first() * canvas.height)
        normalizedPrices.forEachIndexed { index, it ->
            lineTo(
                (canvas.width * index).toFloat() / (normalizedPrices.size - 1).coerceAtLeast(1),
                (1 - it) * canvas.height
            )
        }
        lineTo(1.1f * canvas.width.toFloat(), canvas.height * 1.5f)
        lineTo(-1.1f * canvas.width.toFloat(), canvas.height * 1.5f)
        close()
    }

    try {
        canvas.drawPath(pricePath, paintFill)
    } catch (e: Exception) {
        return null
    }

    val strokeColor = when {
        roundToPlaces(priceChange, 2) > 0 -> widgetTrendUp(context)
        roundToPlaces(priceChange, 2) < 0 -> widgetTrendDown(context)
        else -> widgetOnSurfaceDim(context)
    }
    val paintStroke = Paint().apply {
        isAntiAlias = true
        color = strokeColor
        strokeWidth = 2f
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }

    try {
        canvas.drawPath(pricePath, paintStroke)
    } catch (e: Exception) {
        return null
    }

    return bitmap
}

private fun roundToPlaces(value: Double, places: Int): Double {
    val divisor = 10.0.pow(places)
    return (value * divisor).roundToInt() / divisor
}

private fun getLinearColorsMediumWidget(priceChange: Double, context: Context): IntArray {
    val isDark = (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_YES) != 0
    return when {
        roundToPlaces(priceChange, 2) > 0 -> if (isDark) intArrayOf(
            Color.parseColor("#405ABF84"),
            Color.parseColor("#305ABF84"),
            Color.parseColor("#005ABF84")
        ) else intArrayOf(
            Color.parseColor("#4059B568"),
            Color.parseColor("#3059B568"),
            Color.parseColor("#0059B568")
        )
        roundToPlaces(priceChange, 2) < 0 -> if (isDark) intArrayOf(
            Color.parseColor("#40EB5757"),
            Color.parseColor("#30EB5757"),
            Color.parseColor("#00EB5757")
        ) else intArrayOf(
            Color.parseColor("#40CA5049"),
            Color.parseColor("#30CA5049"),
            Color.parseColor("#00CA5049")
        )
        else -> if (isDark) intArrayOf(
            Color.parseColor("#40C9C6C5"),
            Color.parseColor("#30C9C6C5"),
            Color.parseColor("#00C9C6C5")
        ) else intArrayOf(
            Color.parseColor("#4093A4EB"),
            Color.parseColor("#3093A4EB"),
            Color.parseColor("#0093A4EB")
        )
    }
}
