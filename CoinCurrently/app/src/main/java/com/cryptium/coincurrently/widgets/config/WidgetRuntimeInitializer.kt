package com.cryptium.coincurrently.widgets.config

import android.content.Context
import com.cryptium.coincurrently.app.NavigationViewModel
import com.cryptium.coincurrently.core.localization.AppLanguageState
import com.cryptium.coincurrently.core.localization.Localization
import com.cryptium.coincurrently.core.networking.RetrofitBuilder
import com.cryptium.coincurrently.core.persistence.StorageManager
import com.cryptium.coincurrently.core.persistence.UserPreferences

object WidgetRuntimeInitializer {
    fun ensureInitialized(context: Context) {
        val appContext = context.applicationContext
        UserPreferences.init(appContext)
        Localization.load(appContext)
        AppLanguageState.loadStored()
        NavigationViewModel.currentScreen = "Widget"
        StorageManager.ensureCache(appContext)
        RetrofitBuilder.ensureApiService(appContext, "Widget")
    }
}
