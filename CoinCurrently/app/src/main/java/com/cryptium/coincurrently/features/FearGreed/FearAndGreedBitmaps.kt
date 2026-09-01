package com.cryptium.coincurrently.features.FearGreed
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import java.lang.Integer.max


fun drawIndicatorArrow(width: Int, height: Int): Bitmap {
    val bitmap = Bitmap.createBitmap(
        max(width, 1),
        max(height, 1),
        Bitmap.Config.ARGB_8888,
    )

    val canvas = Canvas(bitmap)

    val paintFill = Paint(Paint.FILTER_BITMAP_FLAG).apply {
        isAntiAlias = true
        color = Color.WHITE
        style = Paint.Style.FILL
        isFilterBitmap = false
        isDither = false
    }

    // create a path
    val pricePath = Path().apply {
        fillType = Path.FillType.EVEN_ODD

        moveTo(0f, canvas.height * 0.49f)
        lineTo(0.5f * canvas.width.toFloat(), 0.2f * canvas.height)
        lineTo(0.5f * canvas.width.toFloat(), 0.8f * canvas.height)
        lineTo(0f, 0.51f * canvas.height)
        close()
    }

    canvas.drawPath(pricePath, paintFill)
    return bitmap
}

fun getResizedBitmap(bm: Bitmap, newHeight: Int, newWidth: Int): Bitmap {
    val width = bm.width
    val height = bm.height
    val scaleWidth = newWidth.toFloat() / width
    val scaleHeight = newHeight.toFloat() / height
    val matrix = Matrix()
    matrix.postScale(kotlin.math.max(1f, scaleWidth), kotlin.math.max(1f, scaleHeight))
    return Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false)
}