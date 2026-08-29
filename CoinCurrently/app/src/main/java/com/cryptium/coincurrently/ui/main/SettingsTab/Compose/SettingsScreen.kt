package com.cryptium.coincurrently.ui.main.SettingsTab.Compose

import com.cryptium.coincurrently.ui.main.Compose.ModalSheetNavigationBarExtensionSpacer as ComposeModalSheetNavigationBarExtensionSpacer
import com.cryptium.coincurrently.ui.main.Compose.isEdgeToEdgeEnabled as composeIsEdgeToEdgeEnabled
import com.cryptium.coincurrently.ui.main.Compose.modalSheetNavigationBarExtensionHeight as composeModalSheetNavigationBarExtensionHeight
import com.cryptium.coincurrently.ui.main.Compose.modalSheetNavigationBarsPadding as composeModalSheetNavigationBarsPadding
import com.cryptium.coincurrently.ui.main.Compose.imageSize

import NavigationScreens
import android.app.Activity
import com.cryptium.coincurrently.BuildConfig
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.platform.LocalUriHandler
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.invisibleToUser
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cryptium.coincurrently.R
import com.cryptium.coincurrently.ui.main.Activities.MainActivity
import com.cryptium.coincurrently.ui.main.PopupViews.SnackBarType
import com.cryptium.coincurrently.ui.main.Compose.*
import com.cryptium.coincurrently.ui.main.Extensions.getActivity
import com.cryptium.coincurrently.ui.main.Extensions.pxToDp
import com.cryptium.coincurrently.ui.main.Extensions.poppinsRegular
import com.cryptium.coincurrently.ui.main.Extensions.poppinsSemibold
import com.cryptium.coincurrently.ui.main.Extensions.satoshiBold
import com.cryptium.coincurrently.ui.main.Extensions.satoshiMedium
import com.cryptium.coincurrently.ui.main.Extensions.updateCurrecyWidgets
import com.cryptium.coincurrently.ui.main.Extensions.updatePortfolioWidgets
import com.cryptium.coincurrently.ui.main.General.AppColorScheme
import com.cryptium.coincurrently.ui.main.General.Enums.PriceChange
import com.cryptium.coincurrently.ui.main.General.Enums.SortOption
import com.cryptium.coincurrently.ui.main.General.GlobalFunctions
import com.cryptium.coincurrently.ui.main.General.refreshCachedSectionsAfterCurrencyChange
import com.cryptium.coincurrently.ui.main.General.appShareMessage
import com.cryptium.coincurrently.ui.main.Managers.StorageManager
import com.cryptium.coincurrently.ui.main.Managers.StorageKeys
import com.cryptium.coincurrently.ui.main.SettingsTab.Achievements.Managers.AchievementManager
import com.cryptium.coincurrently.ui.main.SettingsTab.Achievements.Models.AchievementMetric
import com.cryptium.coincurrently.ui.main.General.NavigationViewModel
import com.cryptium.coincurrently.ui.main.analytics.AppShareActivityCompleted
import com.cryptium.coincurrently.ui.main.analytics.BuyProAction
import com.cryptium.coincurrently.ui.main.analytics.CurrencySelected
import com.cryptium.coincurrently.ui.main.analytics.DisabledColorBlindnessFilter
import com.cryptium.coincurrently.ui.main.analytics.DisabledFaceId
import com.cryptium.coincurrently.ui.main.analytics.DisabledWebSocket
import com.cryptium.coincurrently.ui.main.analytics.EnabledColorBlindnessFilter
import com.cryptium.coincurrently.ui.main.analytics.EnabledFaceId
import com.cryptium.coincurrently.ui.main.analytics.EnabledWebSocket
import com.cryptium.coincurrently.ui.main.analytics.AnalyticsManager
import com.cryptium.coincurrently.ui.main.analytics.Navigate
import com.cryptium.coincurrently.ui.main.analytics.ResetApp
import com.cryptium.coincurrently.ui.main.analytics.ScreenLabel
import com.cryptium.coincurrently.ui.main.analytics.SheetLabel
import com.cryptium.coincurrently.ui.main.analytics.SheetOpened
import com.cryptium.coincurrently.ui.main.analytics.TabBarDoubleTapped
import com.cryptium.coincurrently.ui.main.analytics.StartTabSelected
import com.cryptium.coincurrently.ui.main.analytics.ThemeSelected
import com.cryptium.coincurrently.ui.main.analytics.WebViewOpened
import com.cryptium.coincurrently.ui.main.General.SelectedStartTab
import com.cryptium.coincurrently.ui.main.Managers.FirebaseManager
import com.cryptium.coincurrently.ui.main.SettingsTab.ViewModels.AuthenticationStatus
import com.cryptium.coincurrently.ui.main.SettingsTab.ViewModels.BiometricViewModel
import com.cryptium.coincurrently.ui.main.HomeTab.Home.Repositories.NewCoinsRepository
import com.cryptium.coincurrently.ui.main.HomeTab.Home.Repositories.WinnersLosersRepository
import com.cryptium.coincurrently.ui.main.Repositories.PortfolioRepository
import com.cryptium.coincurrently.ui.main.Repositories.PriceAlertsRepository
import com.cryptium.coincurrently.ui.main.Repositories.StoreKitManager
import com.cryptium.coincurrently.ui.main.Repositories.ToplistRepository
import com.cryptium.coincurrently.ui.main.SettingsTab.Popups.CurrencyOptionsSheet
import com.cryptium.coincurrently.ui.main.SettingsTab.Popups.SettingsOptionsSheet
import androidx.core.app.NotificationManagerCompat
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import networking.ResponseModels.CurrencySelectionModel
import kotlin.system.exitProcess

@Composable
fun SettingsScreen(
    navigationViewModel: NavigationViewModel,
    modifier: Modifier,
    tabBarHeight: Dp = 0.dp,
) {
    val uriHandler = LocalUriHandler.current
    val context = LocalContext.current
    val biometricViewModel: BiometricViewModel = viewModel()

    var doubleBackToExitPressedOnce by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    val currentSubscription by StoreKitManager.currentSubscription.observeAsState()

    val showResetApp = remember { mutableStateOf(false) }

    val canCloseResetView = remember { mutableStateOf(true) }

    var selectedCurrency by remember { mutableStateOf(GlobalFunctions.getSelectedCurrency()) }
    var isBiometricEnabled by remember { mutableStateOf(GlobalFunctions.getIsBiometricAuthenticationEnabled()) }
    var selectedTheme by remember { mutableStateOf(navigationViewModel.appColorScheme) }
    var selectedStartTab by remember { mutableStateOf(GlobalFunctions.getStartTab()) }
    var isColorBlindEnabled by remember { mutableStateOf(GlobalFunctions.getColorBlind()) }
    var webSocketEnabled by remember { mutableStateOf(false) }

    LaunchedEffect(currentSubscription) {
        webSocketEnabled = GlobalFunctions.isWebSocketEnabled(hasPro = currentSubscription != null)
    }

    LaunchedEffect(Unit) {
        AchievementManager.preloadProgress()
    }

    var isSelectingCurrency by remember { mutableStateOf(false) }
    var isSelectingTheme by remember { mutableStateOf(false) }
    var isSelectingStartTab by remember { mutableStateOf(false) }

    BackHandler(onBack = {
        if (showResetApp.value) {
            if (canCloseResetView.value) {
                showResetApp.value = false
            }
        } else if (scrollState.value > 0) {
            AnalyticsManager.log(TabBarDoubleTapped(ScreenLabel.Settings))
            coroutineScope.launch {
                scrollState.animateScrollTo(0)
            }
        } else if (!doubleBackToExitPressedOnce) {
            Toast.makeText(context, "Press back again to exit the app", Toast.LENGTH_SHORT).show()
            doubleBackToExitPressedOnce = true
            Handler(Looper.getMainLooper()).postDelayed({
                doubleBackToExitPressedOnce = false
            }, 2500L)
        } else {
            context.getActivity()?.finishAffinity()
        }
    })

    Box {
        Column(
            modifier = modifier
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(32.dp),
                modifier = Modifier
                    .verticalScroll(scrollState)
                    .padding(
                        vertical = 32.dp,
                        horizontal = 16.dp
                    )
                    .then(
                        if (showResetApp.value) {
                            Modifier.clearAndSetSemantics { invisibleToUser() }
                        } else {
                            Modifier
                        }
                    ),
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier
                        .primaryBackgroundNoRipple()
                ) {
                    PremiumView(currentSubscription = currentSubscription) {
                        navigationViewModel.openBuyPro(ScreenLabel.Settings, BuyProAction.Settings)
                    }

                    if (currentSubscription != null) {
                        Separator()

                        SettingsSimpleCell(
                            title = "Manage Subscription",
                            subTitle = "Managed in Google Play",
                            withExternal = true
                        ) {
                            try {
                                uriHandler.openUri("https://play.google.com/store/account/subscriptions")
                            } catch (_: ActivityNotFoundException) {
                                return@SettingsSimpleCell
                            }
                        }
                    }

                    if (BuildConfig.DEBUG) {
                        Separator()

                        SettingsSimpleCell(
                            title = "Referral Program",
                            accessibilityText = "Navigate to Referral Program",
                        ) {
                            AnalyticsManager.log(
                                Navigate(
                                    fromScreen = ScreenLabel.Settings,
                                    toScreen = ScreenLabel.Referral,
                                ),
                            )
                            navigationViewModel.navigate(
                                NavigationScreens.Referral(),
                            )
                        }
                    }
                }

                GlobalSection(
                    selectedCurrency = selectedCurrency,
                    selectedStartTab = selectedStartTab,
                    selectedTheme = selectedTheme,
                    isColorBlindEnabled = isColorBlindEnabled,
                    webSocketEnabled = webSocketEnabled,
                    currentSubscription = currentSubscription,
                    selectNewCurrency = {
                        AnalyticsManager.log(
                            SheetOpened(ScreenLabel.Settings, SheetLabel.CurrencySelector),
                        )
                        isSelectingCurrency = true
                    },
                    selectNewStartTab = {
                        AnalyticsManager.log(
                            SheetOpened(ScreenLabel.Settings, SheetLabel.StartTab),
                        )
                        isSelectingStartTab = true
                    },
                    selectNewTheme = {
                        AnalyticsManager.log(
                            SheetOpened(ScreenLabel.Settings, SheetLabel.Theme),
                        )
                        isSelectingTheme = true
                    },
                    toggleColorBlind = {
                        isColorBlindEnabled = !isColorBlindEnabled
                        GlobalFunctions.setColorBlind(isColorBlindEnabled)
                        navigationViewModel.colorBlindFilter = isColorBlindEnabled
                        if (isColorBlindEnabled) {
                            AnalyticsManager.log(
                                EnabledColorBlindnessFilter(ScreenLabel.Settings),
                            )
                        } else {
                            AnalyticsManager.log(
                                DisabledColorBlindnessFilter(ScreenLabel.Settings),
                            )
                        }
                    },
                    toggleWebSocket = {
                        if (!webSocketEnabled && currentSubscription == null) {
                            navigationViewModel.openBuyPro(ScreenLabel.Settings, BuyProAction.Settings)
                            return@GlobalSection
                        }

                        webSocketEnabled = !webSocketEnabled
                        GlobalFunctions.setWebSocketEnabled(webSocketEnabled)
                        if (webSocketEnabled) {
                            AnalyticsManager.log(EnabledWebSocket(ScreenLabel.Settings))
                        } else {
                            AnalyticsManager.log(DisabledWebSocket(ScreenLabel.Settings))
                        }
                    }
                )

                ToolsSection(
                    navigateToWidgets = {
                        AnalyticsManager.log(
                            Navigate(
                                fromScreen = ScreenLabel.Settings,
                                toScreen = ScreenLabel.Widgets,
                            ),
                        )
                        navigationViewModel.navigate(
                            NavigationScreens.Widgets
                        )
                    },
                    navigateToCalculator = {
                        AnalyticsManager.log(
                            Navigate(
                                fromScreen = ScreenLabel.Settings,
                                toScreen = ScreenLabel.Converter,
                            ),
                        )
                        navigationViewModel.navigate(
                            NavigationScreens.Calculator
                        )
                    },
                    navigateToPriceAlerts = {
                        AnalyticsManager.log(
                            Navigate(
                                fromScreen = ScreenLabel.Settings,
                                toScreen = ScreenLabel.PriceAlerts,
                            ),
                        )
                        navigationViewModel.navigate(
                            NavigationScreens.PriceAlerts
                        )
                    },
                    navigateToAchievements = {
                        AnalyticsManager.log(
                            Navigate(
                                fromScreen = ScreenLabel.Settings,
                                toScreen = ScreenLabel.Achievements,
                            ),
                        )
                        navigationViewModel.navigate(
                            NavigationScreens.Achievements
                        )
                    },
                )

                SecuritySection(
                    isBiometricAuthEnabled = isBiometricEnabled,
                    toggleBiometricAuth = {
                        if (biometricViewModel.isBiometricAvailable(context)) {
                            context.getActivity()?.let {
                                biometricViewModel.setPrompt(context, it) {
                                    if (it == AuthenticationStatus.SUCCESS) {
                                        val wasEnabled = isBiometricEnabled
                                        isBiometricEnabled = !isBiometricEnabled
                                        GlobalFunctions.setIsBiometricAuthenticationEnabled(
                                            isBiometricEnabled
                                        )
                                        if (!wasEnabled && isBiometricEnabled) {
                                            AchievementManager.checkMetricUnlock(
                                                metric = AchievementMetric.authenticationEnabled,
                                                previousValue = 0.0,
                                                newValue = 1.0,
                                            )
                                        }
                                        if (isBiometricEnabled) {
                                            AnalyticsManager.log(
                                                EnabledFaceId(ScreenLabel.Settings),
                                            )
                                        } else {
                                            AnalyticsManager.log(
                                                DisabledFaceId(ScreenLabel.Settings),
                                            )
                                        }
                                    }
                                }

                                biometricViewModel.authenticate(context)
                            }
                        } else {
                            MainActivity.showSnackbar(
                                context,
                                SnackBarType.FAILURE,
                                "Something went wrong. Make sure Face ID / Touch ID is enabled and try again."
                            )
                        }
                    },
                    showResetApp = {
                        AnalyticsManager.log(
                            SheetOpened(ScreenLabel.Settings, SheetLabel.ResetApp),
                        )
                        showResetApp.value = true
                    }
                )

                SupportSection(
                    navigateToFeedback = {
                        AnalyticsManager.log(
                            Navigate(
                                fromScreen = ScreenLabel.Settings,
                                toScreen = ScreenLabel.Feedback,
                            ),
                        )
                        navigationViewModel.navigate(
                            to = NavigationScreens.Feedback
                        )
                    }
                )

                LegalSection()

                PoweredByView()
            }
        }

        if (showResetApp.value) {
            ResetAppView(
                closeView = {
                    showResetApp.value = false
                    canCloseResetView.value = true
                },
                bottomBarInset = tabBarHeight,
                resetApp = {
                    context.getActivity()?.let {
                        if (GlobalFunctions.getIsBiometricAuthenticationEnabled()) {
                            biometricViewModel.setPrompt(context, it) {
                                if (it == AuthenticationStatus.SUCCESS) {
                                    AnalyticsManager.log(ResetApp(ScreenLabel.Settings))

                                    resetApp(
                                        context,
                                        canCloseResetView
                                    )
                                }
                            }

                            biometricViewModel.authenticate(context)
                        } else {
                            AnalyticsManager.log(ResetApp(ScreenLabel.Settings))
                            resetApp(
                                context,
                                canCloseResetView
                            )
                        }
                    }
                }
            )
        }

        if(isSelectingCurrency) {
            CurrencyOptionsSheet(
                selectedOption = selectedCurrency,
                newOptionSelected = {
                    selectedCurrency = it
                    GlobalFunctions.setSelectedCurrency(selectedCurrency)
                    AnalyticsManager.log(
                        CurrencySelected(
                            fromScreen = ScreenLabel.Settings,
                            currency = selectedCurrency.currency,
                        ),
                    )
                    coroutineScope.launch {
                        refreshCachedSectionsAfterCurrencyChange()
                        navigationViewModel.notifyCurrencyChanged()
                    }

                    if (NotificationManagerCompat.from(context).areNotificationsEnabled()) {
                        coroutineScope.launch {
                            PriceAlertsRepository.updateUser(force = true)
                        }
                    }
                },
                close = {
                    isSelectingCurrency = false
                }
            )
        }

        if(isSelectingStartTab) {
            SettingsOptionsSheet(
                title = "Default Tab",
                options = SelectedStartTab.entries.map { it.getName() }.toTypedArray(),
                selectedOption = selectedStartTab.getName(),
                newOptionSelected = { option ->
                    val tab = SelectedStartTab.entries.find { it.getName() == option }
                        ?: return@SettingsOptionsSheet
                    selectedStartTab = tab
                    GlobalFunctions.setStartTab(selectedStartTab)
                    AnalyticsManager.log(
                        StartTabSelected(ScreenLabel.Settings, selectedStartTab),
                    )
                },
                close = {
                    isSelectingStartTab = false
                }
            )
        }

        if(isSelectingTheme) {
            SettingsOptionsSheet(
                title = "App Theme",
                options = AppColorScheme.entries.map { it.displayName }.toTypedArray(),
                selectedOption = selectedTheme.displayName,
                newOptionSelected = { option ->
                    val scheme = AppColorScheme.entries.find { it.displayName == option } ?: return@SettingsOptionsSheet
                    selectedTheme = scheme
                    navigationViewModel.appColorScheme = scheme
                    StorageManager.set(StorageKeys.APP_COLOR_SCHEME, scheme)
                    AnalyticsManager.log(ThemeSelected(ScreenLabel.Settings, scheme))
                },
                close = {
                    isSelectingTheme = false
                }
            )
        }
    }
}
@Composable
private fun GlobalSection(
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
private fun ToolsSection(
    navigateToWidgets: () -> Unit,
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
                title = "Widgets",
                accessibilityText = "Navigate to Widgets",
            ) {
                navigateToWidgets.invoke()
            }

            Separator()

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

@Composable
private fun SecuritySection(
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
                title = "Face ID / Touch ID",
                subTitle = "Use Face ID / Touch ID to unlock app",
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
                        contentDescription = "Reset the app"
                        role = Role.Button
                        onClick {
                            showResetApp.invoke()
                            true
                        }
                    }
            ) {
                Text(
                    text = "Reset the app",
                    textAlign = TextAlign.Start,
                    style = satoshiBold(16),
                    color = MaterialTheme.colors.trendDown
                )

                Text(
                    text = "Delete your settings and data",
                    textAlign = TextAlign.Start,
                    style = satoshiBold(12),
                    color = MaterialTheme.colors.onSurfaceDimmer
                )
            }
        }
    }
}

@Composable
private fun SupportSection(
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
                accessibilityText = "Navigate to Help us Improve",
            ) {
                navigateToFeedback.invoke()
            }
        }
    }
}

@Composable
private fun LegalSection() {
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
private fun PoweredByView() {
    Row(
        verticalAlignment = CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = 16.dp
            )
            .clearAndSetSemantics {
                contentDescription = "Powered by CoinGecko"
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
            text = "Powered by CoinGecko",
            textAlign = TextAlign.Start,
            style = satoshiMedium(16),
            color = MaterialTheme.colors.onSurfaceDimmer
        )
    }
}

fun isEdgeToEdgeEnabled(context: Context): Int = composeIsEdgeToEdgeEnabled(context)

/** Apply on the outermost layout inside [ModalBottomSheet] content ([Column], [Box], [LazyColumn], etc.). */
fun Modifier.modalSheetNavigationBarsPadding(context: Context): Modifier =
    composeModalSheetNavigationBarsPadding(context)

@Composable
fun Context.modalSheetNavigationBarExtensionHeight(density: Density): Dp =
    composeModalSheetNavigationBarExtensionHeight(density)

/** Extends wrap-content sheet surfaces through the system nav area on edge-to-edge devices. */
@Composable
fun ModalSheetNavigationBarExtensionSpacer(context: Context) {
    ComposeModalSheetNavigationBarExtensionSpacer(context)
}

@Composable
fun SectionHeader(text: String) {
    Text(
        text = text,
        textAlign = TextAlign.Start,
        style = poppinsSemibold(22),
        color = MaterialTheme.colors.onSurfaceDim,
        modifier = Modifier.semantics { heading() },
    )
}

@Composable
private fun PremiumView(
    currentSubscription: String?,
    action: () -> Unit
) {
    val subscriptionStatus = if (currentSubscription != null) {
        val tier = StoreKitManager.subscriptionTierDisplayName(currentSubscription)
        if (tier != null) "Active · $tier" else "Active"
    } else {
        "Inactive"
    }

    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = CenterVertically,
        modifier = Modifier
            .padding(end = 8.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                action()
            }
            .clearAndSetSemantics {
                contentDescription = "CoinCurrently Pro. $subscriptionStatus"
                role = Role.Button
                onClick {
                    action()
                    true
                }
            }
    ) {
        Image(
            painter = painterResource(
                id = if (MaterialTheme.colors.isLight) R.drawable.premium_logo_light
                else R.drawable.premium_logo_dark
            ),
            contentDescription = null,
            modifier = Modifier
                .imageSize(48.dp)
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalAlignment = Alignment.Start,
            modifier = Modifier
                .weight(1f)
        ) {
            Text(
                text = "CoinCurrently Pro",
                textAlign = TextAlign.Start,
                style = satoshiBold(16),
                color = MaterialTheme.colors.onSurface,
            )

            Text(
                text = subscriptionStatus,
                textAlign = TextAlign.Start,
                style = satoshiBold(12),
                color = if (currentSubscription != null) MaterialTheme.colors.primary else MaterialTheme.colors.onSurfaceDimmer,
            )
        }

        Image(
            painter = painterResource(id = R.drawable.arrow_forward),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            colorFilter = ColorFilter.tint(MaterialTheme.colors.onSurfaceDimmer),
            modifier = Modifier
                .imageSize(24.dp, 24.dp)
        )
    }
}

@Composable
fun SettingsSimpleCell(
    title: String,
    subTitle: String? = null,
    withExternal: Boolean = false,
    accessibilityText: String? = null,
    action: () -> Unit
) {
    Row(
        verticalAlignment = CenterVertically,
        modifier = Modifier
            .padding(end = 8.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
            ) {
                action()
            }
            .clearAndSetSemantics {
                contentDescription = accessibilityText
                    ?: subTitle?.let { "$title. $it" }
                    ?: title
                role = Role.Button
                onClick {
                    action()
                    true
                }
            }
    ) {
        Column(
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier
                .padding(vertical = if (subTitle == null) 12.dp else 0.dp)
                .weight(1f)
        ) {
            Text(
                text = title,
                textAlign = TextAlign.Start,
                style = satoshiBold(16),
                color = MaterialTheme.colors.onSurface
            )

            subTitle?.let {
                Text(
                    text = it,
                    textAlign = TextAlign.Start,
                    style = satoshiBold(12),
                    color = MaterialTheme.colors.onSurfaceDimmer
                )
            }
        }

        if (withExternal) {
            Image(
                painter = painterResource(id = R.drawable.external),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                colorFilter = ColorFilter.tint(MaterialTheme.colors.onSurfaceDimmer),
                modifier = Modifier
                    .imageSize(24.dp, 24.dp)
            )
        } else {
            Image(
                painter = painterResource(id = R.drawable.arrow_forward),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                colorFilter = ColorFilter.tint(MaterialTheme.colors.onSurfaceDimmer),
                modifier = Modifier
                    .imageSize(24.dp, 24.dp)
            )
        }
    }
}

@Composable
fun SettingsImageCell(
    image: Int,
    title: String,
    subTitle: String? = null,
    withArrow: Boolean = false,
    withExternalIcon: Boolean = false,
    accessibilityText: String? = null,
    action: () -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = CenterVertically,
        modifier = Modifier
            .padding(
                top = if (subTitle == null) 12.dp else 0.dp,
                end = 8.dp,
                bottom = if (subTitle == null) 12.dp else 0.dp,
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                action()
            }
            .clearAndSetSemantics {
                contentDescription = accessibilityText
                    ?: subTitle?.let { "$title. $it" }
                    ?: title
                role = Role.Button
                onClick {
                    action()
                    true
                }
            }
    ) {
        Image(
            painter = painterResource(id = image),
            contentDescription = null,
            colorFilter = ColorFilter.tint(MaterialTheme.colors.onSurfaceDim),
            modifier = Modifier
                .imageSize(24.dp)
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalAlignment = Alignment.Start,
            modifier = Modifier
                .weight(1f)
        ) {
            Text(
                text = title,
                textAlign = TextAlign.Start,
                style = poppinsRegular(16),
                color = MaterialTheme.colors.onSurface,
            )

            subTitle?.let {
                Text(
                    text = it,
                    textAlign = TextAlign.Start,
                    style = satoshiBold(12),
                    color = MaterialTheme.colors.onSurfaceDimmer,
                )
            }
        }

        if (withArrow) {
            Image(
                painter = painterResource(R.drawable.arrow_forward),
                contentDescription = null,
                colorFilter = ColorFilter.tint(MaterialTheme.colors.onSurfaceDimmer),
                modifier = Modifier
                    .imageSize(24.dp)
            )
        } else if (withExternalIcon) {
            Image(
                painter = painterResource(R.drawable.external),
                contentDescription = null,
                colorFilter = ColorFilter.tint(MaterialTheme.colors.onSurfaceDimmer),
                modifier = Modifier
                    .imageSize(24.dp)
            )
        }
    }
}

@Composable
fun SettingsCellSwitch(
    title: String,
    subTitle: String?,
    isOn: Boolean,
    action: () -> Unit
) {
    val isHorizontal = AccessibilityLayout.isHorizontal

    AdaptiveStack(
        isHorizontal = isHorizontal,
        spacing = if (isHorizontal) 16.dp else 8.dp,
        horizontalAlignment = Alignment.Start,
        verticalAlignment = CenterVertically,
        modifier = Modifier.clearAndSetSemantics {
            contentDescription = subTitle?.let { "$title. $it" } ?: title
            role = Role.Switch
            stateDescription = if (isOn) "On" else "Off"
            onClick {
                action()
                true
            }
        },
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalAlignment = Alignment.Start,
            modifier = sectionModifier(),
        ) {
            Text(
                text = title,
                textAlign = TextAlign.Start,
                style = satoshiBold(16),
                color = MaterialTheme.colors.onSurface,
            )

            subTitle?.let {
                Text(
                    text = it,
                    textAlign = TextAlign.Start,
                    style = satoshiBold(12),
                    color = MaterialTheme.colors.onSurfaceDimmer,
                )
            }
        }

        Box {
            CustomSwitch(
                isOn = isOn,
                toggled = action
            )

            Canvas(
                modifier = Modifier
                    .size(width = 54.dp, height = 32.dp)
                    .pointerInput(Unit) {
                        detectTapGestures(onTap = {
                            action()
                        })
                    }
            ) { }
        }
    }
}

private fun resetApp(
    context: Context,
    canCloseResetView: MutableState<Boolean>,
) {
    canCloseResetView.value = false

    FirebaseManager.resetApp {
        context.getActivity()?.updateCurrecyWidgets()
        context.getActivity()?.updatePortfolioWidgets()

        exitProcess(1)
    }
}