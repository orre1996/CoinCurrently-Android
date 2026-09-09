package com.cryptium.coincurrently.widgets.config

import android.appwidget.AppWidgetManager
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import com.cryptium.coincurrently.sharedui.theme.AppTheme
import com.cryptium.coincurrently.widgets.receivers.createSmallWidget

class SingleCoinConfigActivity : ComponentActivity() {
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

        val existingConfig = WidgetConfigStore.getSingleCoinConfig(this, appWidgetId)
        val initialSelectedIds = listOfNotNull(existingConfig.coinId)
        val isDark = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_YES) != 0

        setContent {
            AppTheme(darkTheme = isDark || isSystemInDarkTheme()) {
                CoinSearchPickerScreen(
                    title = "Select Coin",
                    coinCount = 1,
                    initialSelectedIds = initialSelectedIds,
                    onCoinsSelected = { coins ->
                        val coinId = coins.firstOrNull()?.id
                        WidgetConfigStore.setSingleCoinConfig(
                            this,
                            appWidgetId,
                            SingleCoinWidgetConfig(coinId = coinId),
                        )
                        updateConfiguredWidget()
                        setResult(RESULT_OK, widgetConfigOkIntent(appWidgetId))
                        finish()
                    },
                    onCancel = { finish() },
                )
            }
        }
    }

    private fun updateConfiguredWidget() {
        createSmallWidget(this, AppWidgetManager.getInstance(this), appWidgetId)
    }
}
