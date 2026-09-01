package com.cryptium.coincurrently.sharedui.components
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import com.cryptium.coincurrently.BuildConfig

/**
 * Detects runtimes where Compose will crash reading [Configuration.fontWeightAdjustment].
 *
 * Some vendor ROMs report API 31+ without the field, and release builds with minSdk 31 can
 * have R8 elide Compose's SDK guard — both surface as [NoSuchFieldError] at startup.
 */
object ComposePlatformCompat {
    @Volatile
    private var probed = false

    @Volatile
    private var composeSupported = true

    fun warmUp(context: Context) {
        isComposeSupported(context)
    }

    fun isComposeSupported(context: Context): Boolean {
        if (!probed) {
            synchronized(this) {
                if (!probed) {
                    composeSupported = canAccessFontWeightAdjustment(context.applicationContext)
                    probed = true
                }
            }
        }
        return composeSupported
    }

    private fun canAccessFontWeightAdjustment(context: Context): Boolean {
        return try {
            when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                    context.resources.configuration.fontWeightAdjustment
                }
                !BuildConfig.DEBUG -> {
                    // Release uses minSdk 31; R8 may still execute Compose's API 31 path on older runtimes.
                    readFontWeightAdjustmentReflectively(context.resources.configuration)
                }
            }
            true
        } catch (_: LinkageError) {
            false
        } catch (_: ReflectiveOperationException) {
            false
        }
    }

    private fun readFontWeightAdjustmentReflectively(configuration: Configuration) {
        val field = Configuration::class.java.getDeclaredField("fontWeightAdjustment")
        field.getInt(configuration)
    }
}
