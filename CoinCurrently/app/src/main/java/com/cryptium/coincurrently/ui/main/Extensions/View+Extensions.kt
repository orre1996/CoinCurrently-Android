package com.cryptium.coincurrently.ui.main.Extensions

import android.util.TypedValue
import android.view.View
import kotlin.math.roundToInt

fun View.dpToPx(dp: Float) = TypedValue.applyDimension(
    TypedValue.COMPLEX_UNIT_DIP,
    dp,
    resources.displayMetrics
).roundToInt()
