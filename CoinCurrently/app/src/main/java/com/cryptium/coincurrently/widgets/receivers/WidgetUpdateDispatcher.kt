package com.cryptium.coincurrently.widgets.receivers
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import com.cryptium.coincurrently.app.NavigationViewModel
import com.cryptium.coincurrently.widgets.glance.LargeMultiCryptoWidgetReceiver
import com.cryptium.coincurrently.widgets.glance.LargeQuadCryptoWidgetReceiver
import com.cryptium.coincurrently.widgets.glance.MediumDuoCryptoWidgetReceiver
import com.cryptium.coincurrently.widgets.glance.SmallCryptoWidgetReceiver
import com.cryptium.coincurrently.core.persistence.StorageManager
import com.cryptium.coincurrently.core.purchases.BillingManager
import com.cryptium.coincurrently.core.networking.RetrofitBuilder

internal fun updateAppWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int
) {
    NavigationViewModel.currentScreen = "General"
    RetrofitBuilder.createApiService(context, "Widget")
    BillingManager.setUpBillingClient(context)
    StorageManager.setCache(context)

    appWidgetManager.getAppWidgetInfo(appWidgetId)?.provider?.let { provider ->
        when {
            provider.className.contains("small_fear_greed_widget") -> {
                createSmallFearWidget(context, appWidgetManager, appWidgetId)
            }
            provider.className.contains("SmallCryptoWidgetReceiver") ||
                provider.className.contains("small_crypto_widget") -> {
                createSmallWidget(context, appWidgetManager, appWidgetId)
            }
            provider.className.contains("MediumDuoCryptoWidgetReceiver") -> {
                createMediumDuoWidget(context, appWidgetManager, appWidgetId)
            }
            provider.className.contains("LargeQuadCryptoWidgetReceiver") -> {
                createLargeQuadWidget(context, appWidgetManager, appWidgetId)
            }
            provider.className.contains("LargeMultiCryptoWidgetReceiver") -> {
                createLargeMultiWidget(context, appWidgetManager, appWidgetId)
            }
            provider.className.contains("small_portfolio_widget") -> {
                createSmallPortfolioWidget(context, appWidgetManager, appWidgetId)
            }
            provider.className.contains("medium_portfolio_widget") -> {
                createMediumPortfolioWidget(context, appWidgetManager, appWidgetId)
            }
            provider.className.contains("large_portfolio_widget") -> {
                createLargePortfolioWidget(context, appWidgetManager, appWidgetId)
            }
            provider.className.contains("SmallNewsWidgetReceiver") -> {
                createSmallNewsWidget(context, appWidgetManager, appWidgetId)
            }
            provider.className.contains("MediumNewsWidgetReceiver") -> {
                createMediumNewsWidget(context, appWidgetManager, appWidgetId)
            }
            else -> { }
        }
    }
}

internal fun reloadAllWidgets(context: Context) {
    val appContext = context.applicationContext
    val manager = AppWidgetManager.getInstance(appContext)
    val providers = listOf(
        small_fear_greed_widget::class.java,
        SmallNewsWidgetReceiver::class.java,
        MediumNewsWidgetReceiver::class.java,
        small_portfolio_widget::class.java,
        medium_portfolio_widget::class.java,
        large_portfolio_widget::class.java,
        SmallCryptoWidgetReceiver::class.java,
        MediumDuoCryptoWidgetReceiver::class.java,
        LargeQuadCryptoWidgetReceiver::class.java,
        LargeMultiCryptoWidgetReceiver::class.java,
    )
    providers.forEach { provider ->
        val ids = manager.getAppWidgetIds(ComponentName(appContext, provider))
        ids.forEach { id -> updateAppWidget(appContext, manager, id) }
    }
}
