package com.cryptium.coincurrently.widgets.receivers
import android.appwidget.AppWidgetManager
import android.content.Context
import com.cryptium.coincurrently.app.NavigationViewModel
import com.cryptium.coincurrently.core.persistence.StorageManager
import com.cryptium.coincurrently.core.purchases.StoreKitManager
import com.cryptium.coincurrently.core.networking.RetrofitBuilder

internal fun updateAppWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int
) {
    NavigationViewModel.currentScreen = "General"
    RetrofitBuilder.createApiService(context, "Widget")
    StoreKitManager.setUpBillingClient(context)
    StorageManager.setCache(context)

    appWidgetManager.getAppWidgetInfo(appWidgetId)?.provider?.let { provider ->
        when {
            provider.className.contains("small_fear_greed_widget") -> {
                createSmallFearWidget(context, appWidgetManager, appWidgetId)
            }
            provider.className.contains("small_crypto_widget") -> {
                createSmallWidget(context, appWidgetManager, appWidgetId)
            }
            provider.className.contains("small_portfolio_widget") -> {
                createSmallPortfolioWidget(context, appWidgetManager, appWidgetId)
            }
            provider.className.contains("medium_portfolio_widget") -> {
                createMediumPortfolioWidget(context, appWidgetManager, appWidgetId)
            }
            else -> {
                // Glance widgets handle their own updates.
            }
        }
    }
}
