package com.cryptium.coincurrently.ui.main.Extensions

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.text.format.DateFormat
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.cryptium.coincurrently.ui.main.Activities.MainActivity

val Context.is24HourClock: Boolean get() {
    return DateFormat.is24HourFormat(this)
}

fun Context.getActivity(): MainActivity? = when (this) {
    is ComponentActivity -> this as? MainActivity
    is ContextWrapper -> baseContext.getActivity()
    else -> null
}

fun Context.sendMail(to: String, subject: String) {
    try {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "vnd.android.cursor.item/email"
        intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(to))
        intent.putExtra(Intent.EXTRA_SUBJECT, subject)
        startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(this, "There is no email client installed.", Toast.LENGTH_SHORT).show()
    } catch (t: Throwable) {
        Toast.makeText(this, "Unknown error.", Toast.LENGTH_SHORT).show()
    }
}

fun Context.sendSMS(text: String) {
    val sendIntent: Intent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, text)
        type = "text/plain"
    }

    val shareIntent = Intent.createChooser(sendIntent, null)
    startActivity(shareIntent)
}