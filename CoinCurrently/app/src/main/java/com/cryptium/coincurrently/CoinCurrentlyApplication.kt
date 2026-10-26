package com.cryptium.coincurrently
import android.app.Application
import coil.Coil
import com.cryptium.coincurrently.sharedui.components.ComposePlatformCompat
import com.cryptium.coincurrently.core.analytics.AnalyticsManager
import com.google.firebase.FirebaseApp
import coil.ImageLoader
import okhttp3.Cache
import okhttp3.OkHttpClient
import java.io.File
import java.util.Locale
import java.util.concurrent.TimeUnit
import com.cryptium.coincurrently.core.config.AppLocale
import com.cryptium.coincurrently.core.localization.AppLanguageState
import com.cryptium.coincurrently.core.localization.Localization
import com.cryptium.coincurrently.core.persistence.UserPreferences

/**
 * Configures Coil so images stay available offline after they have been fetched once.
 *
 * - [ImageLoader.Builder.respectCacheHeaders]: many CDNs send `Cache-Control` that blocks
 *   persisting bytes; ignoring those headers lets Coil write and read its disk cache.
 * - [ImageLoader.Builder.networkObserverEnabled]: the default short-circuits “network” when
 *   offline and can fail with 504 before Coil can fall back to cached data.
 *
 * An OkHttp disk cache is also set so raw responses are retained on disk independently of
 * Coil’s decoded image cache.
 */
class CoinCurrentlyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        ComposePlatformCompat.warmUp(this)
        Locale.setDefault(AppLocale.enUsPosix)
        UserPreferences.init(this)
        Localization.load(this)
        AppLanguageState.loadStored()
        if (BuildConfig.FIREBASE_ENABLED) {
            FirebaseApp.initializeApp(this)
        }
        AnalyticsManager.init(this)

        Coil.setImageLoader(newImageLoader())
    }

    private fun newImageLoader(): ImageLoader {
        val httpCacheDir = File(cacheDir, "coil_http_cache").apply { mkdirs() }
        val httpCache = Cache(httpCacheDir, HTTP_CACHE_MAX_BYTES)
        val okHttpClient = OkHttpClient.Builder()
            .cache(httpCache)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        return ImageLoader.Builder(this)
            .okHttpClient(okHttpClient)
            .respectCacheHeaders(false)
            .networkObserverEnabled(false)
            .build()
    }

    companion object {
        private const val HTTP_CACHE_MAX_BYTES = 250L * 1024 * 1024
    }
}
