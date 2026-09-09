package com.cryptium.coincurrently.widgets.config

import android.appwidget.AppWidgetManager
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import com.cryptium.coincurrently.core.networking.ResponseModels.Toplist
import com.cryptium.coincurrently.sharedui.theme.AppTheme
import com.cryptium.coincurrently.widgets.receivers.createLargeMultiWidget

class MultiCoinConfigActivity : ComponentActivity() {
    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setResult(RESULT_CANCELED)
        prepareWidgetConfigWindow()

        appWidgetId = readAppWidgetId()
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        WidgetRuntimeInitializer.ensureInitialized(this)

        val existingConfig = WidgetConfigStore.getMultiCoinConfig(this, appWidgetId)
        val initialSelectedIds = existingConfig.coinIds.filterNotNull()
        val isDark = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_YES) != 0

        setContent {
            AppTheme(darkTheme = isDark || isSystemInDarkTheme()) {
                CoinSearchPickerScreen(
                    title = "Select Coins",
                    coinCount = 6,
                    initialSelectedIds = initialSelectedIds,
                    onCoinsSelected = { coins -> saveAndFinish(coins) },
                    onCancel = { finish() },
                )
            }
        }
    }

    private fun saveAndFinish(coins: List<Toplist>) {
        WidgetConfigStore.setMultiCoinConfig(
            this,
            appWidgetId,
            MultiCoinWidgetConfig(
                coinIds = (0 until 6).map { index -> coins.getOrNull(index)?.id },
            ),
        )
        createLargeMultiWidget(this, AppWidgetManager.getInstance(this), appWidgetId)
        setResult(RESULT_OK, widgetConfigOkIntent(appWidgetId))
        finish()
    }
}
