package com.cryptium.coincurrently.ui.main.Widgets

import android.content.Context
import android.content.Intent
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.ActionCallback
import com.cryptium.coincurrently.ui.main.Activities.MainActivity

/** Key for passing crypto ID when opening the app. Use with actionParametersOf(CryptoIdKey to id). */
val CryptoIdKey = ActionParameters.Key<String>("cryptoId")

/** Key for passing news URL when opening the app. Use with actionParametersOf(NewsIdKey to url). */
val NewsIdKey = ActionParameters.Key<String>("newsId")

/** Key for opening Fear & Greed screen. Use with actionParametersOf(FearIdKey to "fear"). */
val FearIdKey = ActionParameters.Key<String>("fearId")

/** Opens MainActivity; optionally pass cryptoID, newsID, or fearID via actionParametersOf. */
class OpenAppAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters,
    ) {
        val cryptoId = parameters.get(CryptoIdKey)
        val newsId = parameters.get(NewsIdKey)
        val fearId = parameters.get(FearIdKey)
        val intent = Intent(context, MainActivity::class.java).apply {
            cryptoId?.let { putExtra("cryptoID", it) }
            newsId?.let { putExtra("newsID", it) }
            fearId?.let { putExtra("fearID", it) }
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}

/** Key for passing which widget to refresh. Use with actionParametersOf(WidgetTypeKey to "SmallCryptoGlanceWidget"). */
val WidgetTypeKey = ActionParameters.Key<String>("widgetType")

/** Refreshes the widget by triggering GlanceAppWidget.update. Pass widget type via WidgetTypeKey. */
class RefreshWidgetAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters,
    ) {
        val widgetType = parameters.get(WidgetTypeKey) ?: return
        val widget: GlanceAppWidget = when (widgetType) {
            "SmallCryptoGlanceWidget" -> SmallCryptoGlanceWidget()
            "MediumDuoCryptoGlanceWidget" -> MediumDuoCryptoGlanceWidget()
            "LargeQuadCryptoGlanceWidget" -> LargeQuadCryptoGlanceWidget()
            "LargeMultiCryptoGlanceWidget" -> LargeMultiCryptoGlanceWidget()
            "SmallNewsGlanceWidget" -> SmallNewsGlanceWidget()
            "MediumNewsGlanceWidget" -> MediumNewsGlanceWidget()
            else -> return
        }
        widget.update(context, glanceId)
    }
}
