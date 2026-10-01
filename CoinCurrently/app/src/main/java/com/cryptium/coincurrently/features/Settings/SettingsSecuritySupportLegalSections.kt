package com.cryptium.coincurrently.features.Settings

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cryptium.coincurrently.R
import com.cryptium.coincurrently.core.analytics.AppShareActivityCompleted
import com.cryptium.coincurrently.core.analytics.AnalyticsManager
import com.cryptium.coincurrently.core.analytics.ScreenLabel
import com.cryptium.coincurrently.core.analytics.WebViewOpened
import com.cryptium.coincurrently.core.extensions.satoshiBold
import com.cryptium.coincurrently.core.localization.Localization
import com.cryptium.coincurrently.core.extensions.satoshiMedium
import com.cryptium.coincurrently.sharedui.components.Separator
import com.cryptium.coincurrently.sharedui.components.appShareMessage
import com.cryptium.coincurrently.sharedui.theme.*
import com.cryptium.coincurrently.sharedui.modifiers.imageSize

@Composable
internal fun SecuritySection(
    isBiometricAuthEnabled: Boolean,
    toggleBiometricAuth: () -> Unit,
    showResetApp: () -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.Start
    ) {
        SectionHeader("Security")

        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.Start,
            modifier = Modifier
                .primaryBackgroundNoRipple()
        ) {
            SettingsCellSwitch(
                title = "Biometric authentication",
                subTitle = "Use biometrics to unlock the app",
                isOn = isBiometricAuthEnabled,
            ) {
                toggleBiometricAuth.invoke()
            }

            Separator()

            Column(
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        showResetApp.invoke()
                    }
                    .clearAndSetSemantics {
                        contentDescription = "${Localization.string("Reset The App")}. ${Localization.string("Delete your settings and data")}"
                        role = Role.Button
                        onClick {
                            showResetApp.invoke()
                            true
                        }
                    }
            ) {
                Text(
                    text = Localization.string("Reset The App"),
                    textAlign = TextAlign.Start,
                    style = satoshiBold(16),
                    color = MaterialTheme.colors.trendDown
                )

                Text(
                    text = Localization.string("Delete your settings and data"),
                    textAlign = TextAlign.Start,
                    style = satoshiBold(12),
                    color = MaterialTheme.colors.onSurfaceDimmer
                )
            }
        }
    }
}

@Composable
internal fun SupportSection(
    navigateToFeedback: () -> Unit
) {
    val uriHandler = LocalUriHandler.current

    val shareLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = { result ->
            val activityType = result.data?.component?.className ?: "none"
            AnalyticsManager.log(
                AppShareActivityCompleted(
                    fromScreen = ScreenLabel.Settings,
                    activityType = activityType,
                    completed = result.resultCode == Activity.RESULT_OK,
                ),
            )
        },
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.Start,
    ) {
        SectionHeader("Support")

        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.Start,
            modifier = Modifier
                .primaryBackgroundNoRipple()
        ) {
            SettingsImageCell(
                image = R.drawable.google_play,
                title = "Rate on Google Play",
                withExternalIcon = true
            ) {
                val marketUrl = "market://details?id=com.cryptium.coincurrently"
                val httpsUrl = "https://play.google.com/store/apps/details?id=com.cryptium.coincurrently"
                try {
                    AnalyticsManager.log(
                        WebViewOpened(ScreenLabel.Settings, marketUrl),
                    )
                    uriHandler.openUri(marketUrl)
                } catch (e: ActivityNotFoundException) {
                    AnalyticsManager.log(
                        WebViewOpened(ScreenLabel.Settings, httpsUrl),
                    )
                    uriHandler.openUri(httpsUrl)
                }
            }

            Separator()

            SettingsImageCell(
                image = R.drawable.reddit,
                title = "Follow on Reddit",
                withExternalIcon = true
            ) {
                val url = "https://www.reddit.com/r/coincurrently"
                AnalyticsManager.log(WebViewOpened(ScreenLabel.Settings, url))
                uriHandler.openUri(url)
            }

            Separator()

            SettingsImageCell(
                image = R.drawable.x,
                title = "Follow on X",
                withExternalIcon = true
            ) {
                val url = "https://twitter.com/CoinCurrently"
                AnalyticsManager.log(WebViewOpened(ScreenLabel.Settings, url))
                uriHandler.openUri(url)
            }

            Separator()

            SettingsImageCell(
                image = R.drawable.share,
                title = "Share the App"
            ) {
                val sendIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, appShareMessage())
                    type = "text/plain"
                }
                shareLauncher.launch(Intent.createChooser(sendIntent, null))
            }

            Separator()

            SettingsImageCell(
                image = R.drawable.feedback,
                title = "Help us Improve",
                withArrow = true,
                accessibilityText = Localization.string("Navigate to Help us improve"),
            ) {
                navigateToFeedback.invoke()
            }
        }
    }
}

@Composable
internal fun LegalSection() {
    val uriHandler = LocalUriHandler.current

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.Start,
    ) {
        SectionHeader("Legal")

        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.Start,
            modifier = Modifier
                .primaryBackgroundNoRipple()
        ) {
            SettingsSimpleCell(
                title = "Terms and Conditions",
                withExternal = true
            ) {
                val url = "https://coincurrently.app/terms"
                AnalyticsManager.log(WebViewOpened(ScreenLabel.Settings, url))
                uriHandler.openUri(url)
            }

            Separator()

            SettingsSimpleCell(
                title = "Privacy Policy",
                withExternal = true
            ) {
                val url = "https://coincurrently.app/privacy"
                AnalyticsManager.log(WebViewOpened(ScreenLabel.Settings, url))
                uriHandler.openUri(url)
            }
        }
    }
}

@Composable
internal fun PoweredByView() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = 16.dp
            )
            .clearAndSetSemantics {
                contentDescription = Localization.string("Powered by coin gecko")
            }
    ) {
        Image(
            painter = painterResource(id = R.drawable.coin_gecko_logo),
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier
                .imageSize(24.dp)
        )

        Text(
            text = Localization.string("Powered by CoinGecko"),
            textAlign = TextAlign.Start,
            style = satoshiMedium(16),
            color = MaterialTheme.colors.onSurfaceDimmer
        )
    }
}

