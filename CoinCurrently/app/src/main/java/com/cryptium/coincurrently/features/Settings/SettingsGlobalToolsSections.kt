package com.cryptium.coincurrently.features.Settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cryptium.coincurrently.BuildConfig
import com.cryptium.coincurrently.core.localization.AppLanguagePreference
import com.cryptium.coincurrently.core.localization.Localization
import com.cryptium.coincurrently.core.models.AppColorScheme
import com.cryptium.coincurrently.core.models.SelectedStartTab
import com.cryptium.coincurrently.core.networking.ResponseModels.CurrencySelectionModel
import com.cryptium.coincurrently.sharedui.components.Separator
import com.cryptium.coincurrently.sharedui.theme.primaryBackgroundNoRipple

@Composable
internal fun GlobalSection(
    selectedCurrency: CurrencySelectionModel,
    selectedLanguage: AppLanguagePreference,
    selectedStartTab: SelectedStartTab,
    selectedTheme: AppColorScheme,
    isColorBlindEnabled: Boolean,
    webSocketEnabled: Boolean,
    currentSubscription: String?,
    selectNewCurrency: () -> Unit,
    selectNewLanguage: () -> Unit,
    selectNewStartTab: () -> Unit,
    selectNewTheme: () -> Unit,
    toggleColorBlind: () -> Unit,
    toggleWebSocket: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        SectionHeader("Global Settings")

        Column(
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .primaryBackgroundNoRipple()
        ) {
            SettingsSimpleCell(
                title = "Currency",
                subTitle = "${selectedCurrency.code.uppercase()} (${selectedCurrency.symbol})",
                accessibilityText = Localization.format(
                    "Currency, currently selected %@",
                    selectedCurrency.localizedName,
                ),
            ) {
                selectNewCurrency.invoke()
            }

            Separator()

            SettingsSimpleCell(
                title = "Language",
                subTitle = selectedLanguage.settingsSubtitle,
                accessibilityText = Localization.format(
                    "Language, currently selected %@",
                    Localization.string(selectedLanguage.settingsSubtitle),
                ),
            ) {
                selectNewLanguage.invoke()
            }

            Separator()

            SettingsSimpleCell(
                title = "Start Tab",
                subTitle = selectedStartTab.getName(),
                accessibilityText = Localization.format(
                    "Start tab, currently selected %@",
                    Localization.string(selectedStartTab.getName()),
                ),
            ) {
                selectNewStartTab.invoke()
            }

            Separator()

            SettingsSimpleCell(
                title = "Theme",
                subTitle = selectedTheme.displayName,
                accessibilityText = Localization.format(
                    "Theme, currently selected %@",
                    Localization.string(selectedTheme.displayName),
                ),
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
                accessibilityText = Localization.string("Navigate to Converter"),
            ) {
                navigateToCalculator.invoke()
            }

            Separator()

            SettingsSimpleCell(
                title = "Price Alerts",
                accessibilityText = Localization.string("Navigate to Price Alerts"),
            ) {
                navigateToPriceAlerts.invoke()
            }

            if (BuildConfig.DEBUG) {
                Separator()

                SettingsSimpleCell(
                    title = "Achievements",
                    accessibilityText = Localization.string("Navigate to Achievements"),
                ) {
                    navigateToAchievements.invoke()
                }
            }
        }
    }
}

