package com.cryptium.coincurrently.widgets.views
import com.cryptium.coincurrently.core.persistence.UserPreferences
import android.content.Context
import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.glance.ColorFilter
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.Text
import androidx.glance.layout.ContentScale
import com.cryptium.coincurrently.R
import com.cryptium.coincurrently.sharedui.theme.WidgetColorValues
import com.cryptium.coincurrently.core.extensions.changeColorWidget
import com.cryptium.coincurrently.core.extensions.changeSymbolWidget
import com.cryptium.coincurrently.core.extensions.changeWithSymbol
import com.cryptium.coincurrently.core.extensions.formatCurrency
import com.cryptium.coincurrently.core.extensions.satoshiBoldGlance
import com.cryptium.coincurrently.core.extensions.withoutSymbol
import com.cryptium.coincurrently.sharedui.models.FormattingThreshold
import com.cryptium.coincurrently.core.networking.ResponseModels.Toplist
import com.cryptium.coincurrently.core.networking.ResponseModels.getPreferredChange
import com.cryptium.coincurrently.widgets.CryptoIdKey
import com.cryptium.coincurrently.widgets.OpenAppAction
import com.cryptium.coincurrently.widgets.RefreshWidgetAction
import com.cryptium.coincurrently.widgets.WidgetTypeKey

/**
 * Reusable cell content for multi-crypto widgets (small multi, medium multi, large multi).
 * Shows: name, price, preferred timeframe change, sparkline chart, and optional refresh button.
 */
@Composable
internal fun MultiWidgetCellContent(
    context: Context,
    data: Toplist?,
    bitmap: Bitmap?,
    graphBitmap: Bitmap?,
    showRefresh: Boolean = false,
    refreshWidgetType: String = "LargeMultiCryptoGlanceWidget",
    modifier: GlanceModifier = GlanceModifier,
) {
    val currency = UserPreferences.getSelectedCurrency()
    val preferredChange = data?.getPreferredChange() ?: 0.0

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                onClick = actionRunCallback<OpenAppAction>(
                    actionParametersOf(CryptoIdKey to (data?.id ?: ""))
                )
            )
            .padding(horizontal = 8.dp, vertical = 6.dp),
    ) {
        GlanceTheme {
            Column(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalAlignment = Alignment.Start,
            ) {
                // Row 1: [Refresh?] name (left, pushed by logo) | crypto image (right)
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalAlignment = Alignment.Start,
                ) {
                    if (showRefresh) {
                        val isDark =
                            (context.resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_YES) != 0
                        Image(
                            provider = ImageProvider(
                                if (isDark) R.drawable.ic_baseline_sync_24_dark
                                else R.drawable.ic_baseline_sync_24_light,
                            ),
                            contentDescription = "Refresh",
                            modifier = GlanceModifier
                                .size(25.dp)
                                .clickable(
                                    onClick = actionRunCallback<RefreshWidgetAction>(
                                        actionParametersOf(WidgetTypeKey to refreshWidgetType),
                                    ),
                                ),
                        )
                        Spacer(modifier = GlanceModifier.width(4.dp))
                    }
                    Text(
                        text = data?.name ?: "CoinCurrently",
                        style = satoshiBoldGlance(14, WidgetColorValues.onSurface()),
                        modifier = GlanceModifier.defaultWeight(),
                    )
                    Spacer(modifier = GlanceModifier.width(4.dp))
                    Image(
                        provider = when {
                            bitmap != null -> ImageProvider(bitmap)
                            else -> ImageProvider(
                                if ((context.resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_YES) != 0) {
                                    R.drawable.logo_dark
                                } else {
                                    R.drawable.logo_light
                                }
                            )
                        },
                        contentDescription = null,
                        modifier = GlanceModifier
                            .size(25.dp)
                            .cornerRadius(if (data?.name == null) 8.dp else 0.dp),
                    )
                }

                // Row 2: Price (full width, not pushed by logo)
                Text(
                    text = (data?.current_price ?: 0.0).formatCurrency(
                        threshold = FormattingThreshold.NONE,
                        currency = currency,
                    ),
                    style = satoshiBoldGlance(14, WidgetColorValues.onSurface()),
                    modifier = GlanceModifier
                        .fillMaxWidth()
                        .padding(top = 2.dp),
                )

                // Bottom row: timeframe + percentage + trend (left) | chart (right)
                Spacer(modifier = GlanceModifier.height(4.dp))
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalAlignment = Alignment.Start,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalAlignment = Alignment.Start,
                        modifier = GlanceModifier.defaultWeight(),
                    ) {
                        Text(
                            text = UserPreferences.getPreferredTimeframeChangeTitle(),
                            style = satoshiBoldGlance(12, WidgetColorValues.onSurface()),
                        )
                        Spacer(modifier = GlanceModifier.width(3.dp))
                        Text(
                            text = preferredChange.changeWithSymbol().withoutSymbol,
                            style = satoshiBoldGlance(
                                12,
                                preferredChange.changeColorWidget(),
                            ),
                        )
                        Spacer(modifier = GlanceModifier.width(3.dp))
                        Image(
                            provider = ImageProvider(
                                preferredChange.changeSymbolWidget()
                            ),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            colorFilter = ColorFilter.tint(
                                preferredChange.changeColorWidget(),
                            ),
                            modifier = GlanceModifier.size(width = 10.dp, height = 6.dp),
                        )
                    }
                    if (graphBitmap != null) {
                        Image(
                            provider = ImageProvider(graphBitmap),
                            contentDescription = null,
                            contentScale = ContentScale.Fit,
                            modifier = GlanceModifier
                                .size(width = 45.dp, height = 13.dp),
                        )
                    }
                }
            }
        }
    }
}
