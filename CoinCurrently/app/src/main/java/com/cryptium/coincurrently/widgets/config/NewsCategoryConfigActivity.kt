package com.cryptium.coincurrently.widgets.config

import android.appwidget.AppWidgetManager
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import com.cryptium.coincurrently.sharedui.theme.AppTheme
import com.cryptium.coincurrently.widgets.receivers.createMediumNewsWidget
import com.cryptium.coincurrently.widgets.receivers.createSmallNewsWidget

class NewsCategoryConfigActivity : ComponentActivity() {
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

        val savedConfig = WidgetConfigStore.getNewsConfigOrNull(this, appWidgetId)
        val isDark = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_YES) != 0

        setContent {
            AppTheme(darkTheme = isDark || isSystemInDarkTheme()) {
                NewsCategoryPickerScreen(
                    selectedConfig = savedConfig,
                    onCategorySelected = { config ->
                        WidgetConfigStore.setNewsConfig(this, appWidgetId, config)
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
        val manager = AppWidgetManager.getInstance(this)
        val className = manager.getAppWidgetInfo(appWidgetId)?.provider?.className.orEmpty()
        if (className.contains("MediumNewsWidgetReceiver")) {
            createMediumNewsWidget(this, manager, appWidgetId)
        } else {
            createSmallNewsWidget(this, manager, appWidgetId)
        }
    }
}
