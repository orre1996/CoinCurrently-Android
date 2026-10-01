package com.cryptium.coincurrently.features.Settings
import com.cryptium.coincurrently.core.persistence.UserPreferences
import com.cryptium.coincurrently.sharedui.sheets.ModalSheetNavigationBarExtensionSpacer as ComposeModalSheetNavigationBarExtensionSpacer
import com.cryptium.coincurrently.sharedui.sheets.isEdgeToEdgeEnabled as composeIsEdgeToEdgeEnabled
import com.cryptium.coincurrently.sharedui.sheets.modalSheetNavigationBarExtensionHeight as composeModalSheetNavigationBarExtensionHeight
import com.cryptium.coincurrently.sharedui.sheets.modalSheetNavigationBarsPadding as composeModalSheetNavigationBarsPadding
import com.cryptium.coincurrently.sharedui.modifiers.imageSize

import com.cryptium.coincurrently.app.NavigationScreens
import android.app.Activity
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cryptium.coincurrently.R
import com.cryptium.coincurrently.app.MainActivity
import com.cryptium.coincurrently.sharedui.components.SnackBarType
import com.cryptium.coincurrently.sharedui.theme.*
import com.cryptium.coincurrently.sharedui.modifiers.*
import com.cryptium.coincurrently.sharedui.components.AccessibilityLayout
import com.cryptium.coincurrently.sharedui.components.AdaptiveStack
import com.cryptium.coincurrently.sharedui.components.CustomSwitch
import com.cryptium.coincurrently.sharedui.components.Separator
import com.cryptium.coincurrently.sharedui.sheets.ModalSheetNavigationBarExtensionSpacer
import com.cryptium.coincurrently.sharedui.sheets.isEdgeToEdgeEnabled
import com.cryptium.coincurrently.sharedui.sheets.modalSheetNavigationBarExtensionHeight
import com.cryptium.coincurrently.sharedui.sheets.modalSheetNavigationBarsPadding
import com.cryptium.coincurrently.core.extensions.getActivity
import com.cryptium.coincurrently.core.extensions.pxToDp
import com.cryptium.coincurrently.core.extensions.poppinsRegular
import com.cryptium.coincurrently.core.extensions.poppinsSemibold
import com.cryptium.coincurrently.core.extensions.satoshiBold
import com.cryptium.coincurrently.core.extensions.satoshiMedium
import com.cryptium.coincurrently.core.extensions.updateCurrecyWidgets
import com.cryptium.coincurrently.core.extensions.updatePortfolioWidgets
import com.cryptium.coincurrently.core.models.AppColorScheme
import com.cryptium.coincurrently.sharedui.models.PriceChange
import com.cryptium.coincurrently.sharedui.models.SortOption
import com.cryptium.coincurrently.app.refreshCachedSectionsAfterCurrencyChange
import com.cryptium.coincurrently.sharedui.components.appShareMessage
import com.cryptium.coincurrently.core.persistence.StorageManager
import com.cryptium.coincurrently.core.persistence.StorageKeys
import com.cryptium.coincurrently.features.Achievements.Managers.AchievementManager
import com.cryptium.coincurrently.features.Achievements.Models.AchievementMetric
import com.cryptium.coincurrently.app.NavigationViewModel
import com.cryptium.coincurrently.core.analytics.AppShareActivityCompleted
import com.cryptium.coincurrently.core.analytics.BuyProAction
import com.cryptium.coincurrently.core.analytics.CurrencySelected
import com.cryptium.coincurrently.core.analytics.LanguageSelected
import com.cryptium.coincurrently.core.analytics.DisabledColorBlindnessFilter
import com.cryptium.coincurrently.core.analytics.DisabledFaceId
import com.cryptium.coincurrently.core.analytics.DisabledWebSocket
import com.cryptium.coincurrently.core.analytics.EnabledColorBlindnessFilter
import com.cryptium.coincurrently.core.analytics.EnabledFaceId
import com.cryptium.coincurrently.core.analytics.EnabledWebSocket
import com.cryptium.coincurrently.core.analytics.AnalyticsManager
import com.cryptium.coincurrently.core.analytics.Navigate
import com.cryptium.coincurrently.core.analytics.ResetApp
import com.cryptium.coincurrently.core.analytics.ScreenLabel
import com.cryptium.coincurrently.core.analytics.SheetLabel
import com.cryptium.coincurrently.core.analytics.SheetOpened
import com.cryptium.coincurrently.core.analytics.TabBarDoubleTapped
import com.cryptium.coincurrently.core.analytics.StartTabSelected
import com.cryptium.coincurrently.core.analytics.ThemeSelected
import com.cryptium.coincurrently.core.analytics.WebViewOpened
import com.cryptium.coincurrently.core.models.SelectedStartTab
import com.cryptium.coincurrently.core.analytics.FirebaseManager
import com.cryptium.coincurrently.features.Settings.ViewModels.AuthenticationStatus
import com.cryptium.coincurrently.features.Settings.ViewModels.BiometricViewModel
import com.cryptium.coincurrently.features.Home.NewCoins.NewCoinsRepository
import com.cryptium.coincurrently.features.Home.WinnersLosers.WinnersLosersRepository
import com.cryptium.coincurrently.features.Portfolio.PortfolioRepository
import com.cryptium.coincurrently.features.PriceAlerts.PriceAlertsRepository
import com.cryptium.coincurrently.core.purchases.BillingManager
import java.util.Date
import com.cryptium.coincurrently.features.Market.Toplist.Repositories.ToplistRepository
import com.cryptium.coincurrently.core.localization.AppLanguagePreference
import com.cryptium.coincurrently.core.localization.AppLanguageState
import com.cryptium.coincurrently.core.localization.Localization
import com.cryptium.coincurrently.features.Settings.CurrencySelector.CurrencyOptionsSheet
import com.cryptium.coincurrently.features.Settings.Language.LanguageOptionsSheet
import com.cryptium.coincurrently.features.Settings.Sheets.SettingsOptionsSheet
import androidx.core.app.NotificationManagerCompat
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import com.cryptium.coincurrently.core.networking.ResponseModels.CurrencySelectionModel
import kotlin.system.exitProcess
import com.cryptium.coincurrently.features.Settings.Views.ResetAppView

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

    val currentSubscription by BillingManager.currentSubscription.observeAsState()
    val subscriptionExpirationDate by BillingManager.subscriptionExpirationDate.observeAsState()

    val showResetApp = remember { mutableStateOf(false) }

    val canCloseResetView = remember { mutableStateOf(true) }

    var selectedCurrency by remember { mutableStateOf(UserPreferences.getSelectedCurrency()) }
    var selectedLanguage by remember { mutableStateOf<AppLanguagePreference>(AppLanguageState.preference) }
    var isBiometricEnabled by remember { mutableStateOf(UserPreferences.getIsBiometricAuthenticationEnabled()) }
    var selectedTheme by remember { mutableStateOf(navigationViewModel.appColorScheme) }
    var selectedStartTab by remember { mutableStateOf(UserPreferences.getStartTab()) }
    var isColorBlindEnabled by remember { mutableStateOf(UserPreferences.getColorBlind()) }
    var webSocketEnabled by remember { mutableStateOf(false) }

    LaunchedEffect(currentSubscription) {
        webSocketEnabled = UserPreferences.isWebSocketEnabled(hasPro = currentSubscription != null)
    }

    LaunchedEffect(Unit) {
        AchievementManager.preloadProgress()
    }

    var isSelectingCurrency by remember { mutableStateOf(false) }
    var isSelectingLanguage by remember { mutableStateOf(false) }
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
                    PremiumView(
                        currentSubscription = currentSubscription,
                        expirationDate = subscriptionExpirationDate,
                    ) {
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

                GlobalSection(
                    selectedCurrency = selectedCurrency,
                    selectedLanguage = selectedLanguage,
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
                    selectNewLanguage = {
                        AnalyticsManager.log(
                            SheetOpened(ScreenLabel.Settings, SheetLabel.Language),
                        )
                        isSelectingLanguage = true
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
                        UserPreferences.setColorBlind(isColorBlindEnabled)
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
                        UserPreferences.setWebSocketEnabled(webSocketEnabled)
                        if (webSocketEnabled) {
                            AnalyticsManager.log(EnabledWebSocket(ScreenLabel.Settings))
                        } else {
                            AnalyticsManager.log(DisabledWebSocket(ScreenLabel.Settings))
                        }
                    }
                )

                ToolsSection(
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
                                        UserPreferences.setIsBiometricAuthenticationEnabled(
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
                        if (UserPreferences.getIsBiometricAuthenticationEnabled()) {
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

        if (isSelectingLanguage) {
            LanguageOptionsSheet(
                selectedOption = selectedLanguage,
                newOptionSelected = {
                    selectedLanguage = it
                    AppLanguageState.select(it)
                    AnalyticsManager.log(
                        LanguageSelected(
                            fromScreen = ScreenLabel.Settings,
                            language = it.storageValue,
                        ),
                    )
                },
                close = { isSelectingLanguage = false },
            )
        }

        if(isSelectingCurrency) {
            CurrencyOptionsSheet(
                selectedOption = selectedCurrency,
                newOptionSelected = {
                    selectedCurrency = it
                    UserPreferences.setSelectedCurrency(selectedCurrency)
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
                            PriceAlertsRepository.updateUser()
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
                    UserPreferences.setStartTab(selectedStartTab)
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
        text = Localization.string(text),
        textAlign = TextAlign.Start,
        style = poppinsSemibold(22),
        color = MaterialTheme.colors.onSurfaceDim,
        modifier = Modifier.semantics { heading() },
    )
}

@Composable
private fun PremiumView(
    currentSubscription: String?,
    expirationDate: Date? = null,
    action: () -> Unit
) {
    val formattedExpiration = expirationDate?.let { date ->
        java.text.DateFormat.getDateInstance(
            java.text.DateFormat.MEDIUM,
            AppLanguageState.preference.resolve().locale,
        ).format(date)
    }
    val subscriptionStatus = when {
        currentSubscription == null -> Localization.string("Inactive")
        formattedExpiration != null -> Localization.format("Active · Expires %@", formattedExpiration)
        else -> Localization.string("Active")
    }
    val subscriptionStatusText = buildAnnotatedString {
        if (currentSubscription == null) {
            withStyle(SpanStyle(color = MaterialTheme.colors.onSurfaceDimmer)) {
                append(Localization.string("Inactive"))
            }
        } else {
            withStyle(SpanStyle(color = MaterialTheme.colors.primary)) {
                append(Localization.string("Active"))
            }
            if (formattedExpiration != null) {
                withStyle(SpanStyle(color = MaterialTheme.colors.onSurfaceDimmer)) {
                    append(Localization.format(" · Expires %@", formattedExpiration))
                }
            }
        }
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
                contentDescription = "${Localization.string("CoinCurrently Pro")}. $subscriptionStatus"
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
                text = Localization.string("CoinCurrently Pro"),
                textAlign = TextAlign.Start,
                style = satoshiBold(16),
                color = MaterialTheme.colors.onSurface,
            )

            Text(
                text = subscriptionStatusText,
                textAlign = TextAlign.Start,
                style = satoshiBold(12),
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
    val localizedTitle = Localization.string(title)
    val localizedSubtitle = subTitle?.let { Localization.string(it) }
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
                    ?: localizedSubtitle?.let { "$localizedTitle. $it" }
                    ?: localizedTitle
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
                text = localizedTitle,
                textAlign = TextAlign.Start,
                style = satoshiBold(16),
                color = MaterialTheme.colors.onSurface
            )

            localizedSubtitle?.let {
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
    val localizedTitle = Localization.string(title)
    val localizedSubtitle = subTitle?.let { Localization.string(it) }
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
                    ?: localizedSubtitle?.let { "$localizedTitle. $it" }
                    ?: localizedTitle
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
                text = localizedTitle,
                textAlign = TextAlign.Start,
                style = poppinsRegular(16),
                color = MaterialTheme.colors.onSurface,
            )

            localizedSubtitle?.let {
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
    val localizedTitle = Localization.string(title)
    val localizedSubtitle = subTitle?.let { Localization.string(it) }
    val switchLabel = Localization.format("%@, switch button", localizedTitle)

    AdaptiveStack(
        isHorizontal = isHorizontal,
        spacing = if (isHorizontal) 16.dp else 8.dp,
        horizontalAlignment = Alignment.Start,
        verticalAlignment = CenterVertically,
        modifier = Modifier.clearAndSetSemantics {
            contentDescription = if (localizedSubtitle != null) {
                "$switchLabel. $localizedSubtitle"
            } else {
                switchLabel
            }
            role = Role.Switch
            stateDescription = Localization.string(if (isOn) "On" else "Off")
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
                text = localizedTitle,
                textAlign = TextAlign.Start,
                style = satoshiBold(16),
                color = MaterialTheme.colors.onSurface,
            )

            localizedSubtitle?.let {
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