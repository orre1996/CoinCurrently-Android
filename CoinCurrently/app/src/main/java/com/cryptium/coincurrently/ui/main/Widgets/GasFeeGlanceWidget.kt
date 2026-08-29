package com.cryptium.coincurrently.ui.main.Widgets

import android.content.Context
import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.text.Text
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.appwidget.provideContent
import com.cryptium.coincurrently.R
import com.cryptium.coincurrently.ui.main.Compose.WidgetColorValues
import com.cryptium.coincurrently.ui.main.Extensions.satoshiBoldGlance
import com.cryptium.coincurrently.ui.main.Extensions.satoshiRegularGlance
import com.cryptium.coincurrently.ui.main.General.GlobalFunctions
import com.cryptium.coincurrently.ui.main.HomeTab.Home.Repositories.GasFeesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import networking.ResponseModels.GasFeesModel

/**
 * Gas fee widget built with Jetpack Glance.
 * Displays Slow, Standard, and Fast Ethereum gas prices in Gwei.
 */
class GasFeeGlanceWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        GlobalFunctions.initSharedPreferences(context)

        val gasFees = withContext(Dispatchers.IO) {
            GasFeesRepository.getGasFees() ?: GasFeesRepository.cachedGasFees
        }

        provideContent {
            GasFeeContent(
                context = context,
                gasFees = gasFees,
            )
        }
    }
}

private fun formatGwei(price: String?): String {
    val value = price?.toDoubleOrNull() ?: 0.0
    return "%.2f Gwei".format(value)
}

@Composable
private fun GasFeeContent(
    context: Context,
    gasFees: GasFeesModel?,
) {
    val data = gasFees?.gasFeeData?.firstOrNull()

    Box(
        contentAlignment = Alignment.TopStart,
        modifier = GlanceModifier
            .fillMaxSize()
            .fillMaxWidth()
            .background(WidgetColorValues.surface())
            .clickable(onClick = actionRunCallback<OpenAppAction>())
            .cornerRadius(24.dp)
            .padding(20.dp),
    ) {
        GlanceTheme {
            Column(
                modifier = GlanceModifier.fillMaxSize().fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalAlignment = Alignment.Start,
            ) {
                // Slow (trendDown) - equal third
                Box(
                    modifier = GlanceModifier
                        .fillMaxWidth()
                        .defaultWeight(),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    GasFeeRow(
                        label = "Slow",
                        value = formatGwei(data?.safeGasPrice),
                        labelColor = WidgetColorValues.trendDown(),
                    )
                }

                // Standard (Neutral) - equal third
                Box(
                    modifier = GlanceModifier
                        .fillMaxWidth()
                        .defaultWeight(),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    GasFeeRow(
                        label = "Standard",
                        value = formatGwei(data?.proposeGasPrice),
                        labelColor = WidgetColorValues.gasNeutral(),
                    )
                }

                // Fast (trendUp) + Refresh button - equal third
                val isDark = (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_YES) != 0
                Box(
                    modifier = GlanceModifier
                        .fillMaxWidth()
                        .defaultWeight(),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    Row(
                        modifier = GlanceModifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalAlignment = Alignment.Start,
                    ) {
                        GasFeeRow(
                            label = "Fast",
                            value = formatGwei(data?.fastGasPrice),
                            labelColor = WidgetColorValues.trendUp(),
                        )
                        Spacer(modifier = GlanceModifier.defaultWeight())
                        Image(
                            provider = ImageProvider(
                                if (isDark) R.drawable.ic_baseline_sync_24_dark else R.drawable.ic_baseline_sync_24_light
                            ),
                            contentDescription = "Refresh",
                            modifier = GlanceModifier
                                .size(24.dp)
                                .clickable(
                                    onClick = actionRunCallback<RefreshWidgetAction>(
                                        actionParametersOf(WidgetTypeKey to "GasFeeGlanceWidget")
                                    )
                                )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GasFeeRow(
    label: String,
    value: String,
    labelColor: androidx.glance.unit.ColorProvider
) {
    Column(
        horizontalAlignment = Alignment.Start,
        verticalAlignment = Alignment.Top,
    ) {
        Text(
            text = label,
            style = satoshiRegularGlance(14, labelColor),
        )
        
        Spacer(modifier = GlanceModifier.height(2.dp))

        Text(
            text = value,
            style = satoshiBoldGlance(18, WidgetColorValues.onSurface()),
        )
    }
}

/** Connects the gas fee widget to Glance. Declare in AndroidManifest as the app widget receiver. */
class GasFeeWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = GasFeeGlanceWidget()
}
