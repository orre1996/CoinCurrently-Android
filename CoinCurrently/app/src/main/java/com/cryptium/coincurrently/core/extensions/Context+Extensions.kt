package com.cryptium.coincurrently.core.extensions
import android.content.Context
import android.content.ContextWrapper
import android.text.format.DateFormat
import androidx.activity.ComponentActivity
import com.cryptium.coincurrently.app.MainActivity

val Context.is24HourClock: Boolean get() {
    return DateFormat.is24HourFormat(this)
}

fun Context.getActivity(): MainActivity? = when (this) {
    is ComponentActivity -> this as? MainActivity
    is ContextWrapper -> baseContext.getActivity()
    else -> null
}