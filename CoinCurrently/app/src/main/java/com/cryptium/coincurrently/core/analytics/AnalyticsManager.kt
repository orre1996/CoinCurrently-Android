package com.cryptium.coincurrently.core.analytics
import android.content.Context
import android.os.Bundle
import android.util.Log
import com.aptabase.Aptabase
import com.cryptium.coincurrently.BuildConfig

/**
 * Logs typed [AnalyticsEvent]s to Aptabase.
 *
 * Initialize once from [com.cryptium.coincurrently.CoinCurrentlyApplication.onCreate].
 */
object AnalyticsManager {

    @Volatile
    private var initialized = false

    /** Idempotent; use application context. */
    fun init(context: Context) {
        if (initialized) return
        val appKey = BuildConfig.APTABASE_APP_KEY
        if (appKey.isBlank()) return
        val appContext = context.applicationContext
        Aptabase.instance.initialize(appContext, appKey)
        Aptabase.instance.trackEvent("app_started")
        initialized = true
    }

    fun log(event: AnalyticsEvent) {
        if (!initialized) {
            if (BuildConfig.DEBUG) {
                Log.w("Analytics", "log(${event.eventName}) skipped — init(application) never ran")
            }
            return
        }
        val props = bundleToAptabaseProps(event.toBundle())
        Aptabase.instance.trackEvent(event.eventName, props)
        if (BuildConfig.DEBUG) {
            Log.d("Analytics", "${event.eventName} props=$props")
        }
    }

    /** Aptabase accepts only [String] and [Int] property values. */
    private fun bundleToAptabaseProps(bundle: Bundle): Map<String, Any> {
        if (bundle.keySet().isEmpty()) return emptyMap()
        val map = linkedMapOf<String, Any>()
        for (key in bundle.keySet()) {
            when (val value = bundle.get(key)) {
                null -> Unit
                is String -> map[key] = value
                is Int -> map[key] = value
                is Long -> map[key] = if (value in Int.MIN_VALUE..Int.MAX_VALUE) value.toInt() else value.toString()
                is Double, is Float -> map[key] = value.toString()
                is Boolean -> map[key] = if (value) 1 else 0
                else -> map[key] = value.toString()
            }
        }
        return map
    }
}
