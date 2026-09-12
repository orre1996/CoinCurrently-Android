package com.cryptium.coincurrently.features.Settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cryptium.coincurrently.BuildConfig
import com.cryptium.coincurrently.core.models.AppColorScheme
import com.cryptium.coincurrently.core.models.SelectedStartTab
import com.cryptium.coincurrently.core.networking.ResponseModels.CurrencySelectionModel
import com.cryptium.coincurrently.sharedui.components.Separator
import com.cryptium.coincurrently.sharedui.theme.primaryBackgroundNoRipple

@Composable
internal fun GlobalSection(
    selectedCurrency: CurrencySelectionModel,
    selectedStartTab: SelectedStartTab,
    selectedTheme: AppColorScheme,
    isColorBlindEnabled: Boolean,
    webSocketEnabled: Boolean,
    currentSubscription: String?,
    selectNewCurrency: () -> Unit,
    selectNewStartTab: () -> Unit,
    selectNewTheme: () -> Unit,
    toggleColorBlind: () -> Unit,
    toggleWebSocket: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        SectionHeader("Global settings")

        Column(
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .primaryBackgroundNoRipple()
        ) {
            SettingsSimpleCell(
                title = "Currency",
                subTitle = "${selectedCurrency.code.uppercase()} (${selectedCurrency.symbol})",
                accessibilityText = "Currency. currently selected: ${selectedCurrency.currency}",
            ) {
                selectNewCurrency.invoke()
            }

            Separator()

            SettingsSimpleCell(
                title = "Start Tab",
                subTitle = selectedStartTab.getName(),
                accessibilityText = "Start tab. currently selected: ${selectedStartTab.getName()}",
            ) {
                selectNewStartTab.invoke()
            }

            Separator()

            SettingsSimpleCell(
                title = "Theme",
                subTitle = selectedTheme.displayName,
                accessibilityText = "Theme. currently selected: ${selectedTheme.displayName}",
            ) {
                selectNewTheme.invoke()
            }

            Separator()

            SettingsCellSwitch(
                title = "Live price updates",
                subTitle = "See prices change on coin pages while you're viewing them",
                isOn = webSocketEnabled,
            ) {
                toggleWebSocket.invoke()
            }

            Separator()

            SettingsCellSwitch(
                title = "Color blindness filter",
                subTitle = "Uses blue and orange instead of green and red for price changes",
                isOn = isColorBlindEnabled,
            ) {
                toggleColorBlind.invoke()
            }
        }
    }
}

@Composable
internal fun ToolsSection(
    navigateToCalculator: () -> Unit,
    navigateToPriceAlerts: () -> Unit,
    navigateToAchievements: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        SectionHeader("Tools")

        Column(
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .primaryBackgroundNoRipple()
        ) {
            SettingsSimpleCell(
                title = "Converter",
                accessibilityText = "Navigate to Converter",
            ) {
                navigateToCalculator.invoke()
            }

            Separator()

            SettingsSimpleCell(
                title = "Price Alerts",
                accessibilityText = "Navigate to Price Alerts",
            ) {
                navigateToPriceAlerts.invoke()
            }

            if (BuildConfig.DEBUG) {
                Separator()

                SettingsSimpleCell(
                    title = "Achievements",
                    accessibilityText = "Navigate to Achievements",
                ) {
                    navigateToAchievements.invoke()
                }
            }
        }
    }
}

