package com.cryptium.coincurrently.app
import com.cryptium.coincurrently.core.persistence.UserPreferences
import com.cryptium.coincurrently.app.NavigationScreens
import android.Manifest
import android.annotation.SuppressLint
import android.content.*
import android.content.res.Configuration
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.TextView
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import com.cryptium.coincurrently.R
import com.cryptium.coincurrently.core.config.AppLocale
import com.cryptium.coincurrently.sharedui.navigation.BackgroundGradient
import com.cryptium.coincurrently.sharedui.navigation.BottomNavigationBar
import com.cryptium.coincurrently.app.NavigationEntries
import com.cryptium.coincurrently.sharedui.navigation.rememberHorizontalSafeAreaInsets
import com.cryptium.coincurrently.sharedui.navigation.rememberMainActivityNavigationBarLayout
import com.cryptium.coincurrently.sharedui.theme.AppTheme
import com.cryptium.coincurrently.sharedui.theme.surfaceContainerLow
import com.cryptium.coincurrently.core.extensions.getActivity
import com.cryptium.coincurrently.core.extensions.isTablet
import com.cryptium.coincurrently.core.extensions.pxToDp
import com.cryptium.coincurrently.core.models.AppColorScheme
import com.cryptium.coincurrently.sharedui.components.ComposePlatformCompat
import com.cryptium.coincurrently.app.NavigationViewModel
import com.cryptium.coincurrently.core.persistence.StorageManager
import com.cryptium.coincurrently.sharedui.components.OnLifecycleEvent
import com.cryptium.coincurrently.features.Settings.Sheets.WhatsNewSheet
import com.cryptium.coincurrently.core.models.SelectedStartTab
import com.cryptium.coincurrently.core.analytics.FirebaseManager
import com.cryptium.coincurrently.core.persistence.StorageKeys
import com.cryptium.coincurrently.features.Settings.ViewModels.AuthenticationStatus
import com.cryptium.coincurrently.features.Settings.ViewModels.BiometricViewModel
import com.cryptium.coincurrently.sharedui.sheets.ForceUpdateScreen
import com.cryptium.coincurrently.sharedui.loading.LoadingScreen
import com.cryptium.coincurrently.sharedui.components.SnackBarType
import com.cryptium.coincurrently.sharedui.components.SnackbarView
import com.cryptium.coincurrently.features.Market.Toplist.Repositories.ToplistRepository
import com.cryptium.coincurrently.features.PriceAlerts.PriceAlertsRepository
import com.cryptium.coincurrently.core.analytics.AnalyticsManager
import com.cryptium.coincurrently.core.analytics.Navigate
import com.cryptium.coincurrently.core.analytics.ScreenLabel
import com.cryptium.coincurrently.core.analytics.screenLabelFromRoute
import com.cryptium.coincurrently.app.getRouteString
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.cryptium.coincurrently.core.networking.ResponseModels.CryptoDetailInfoModel
import com.cryptium.coincurrently.core.networking.RetrofitBuilder
import java.lang.ref.WeakReference
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import kotlin.time.Duration.Companion.seconds

class MainActivity: FragmentActivity() {
    lateinit var navigationViewModel: NavigationViewModel

    @OptIn(DelicateCoroutinesApi::class)
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                GlobalScope.launch {
                    PriceAlertsRepository.updateUser()
                }
            } else {
                StorageManager.set(StorageKeys.HAS_ASKED_FOR_PUSH_NOTIFICATIONS, true)
            }
        }

    @SuppressLint("UnusedMaterialScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        instance = WeakReference(this)
        UserPreferences.init(application)
        setStartData()
        RetrofitBuilder.createApiService(baseContext, "App")
        navigationViewModel = NavigationViewModel(baseContext)
        setStatusBarColor(resolvedDarkMode())

        if (!ComposePlatformCompat.isComposeSupported(this)) {
            showUnsupportedDeviceScreen()
            return
        }

        setContent {
            val appColorScheme by navigationViewModel.appColorSchemeFlow.collectAsState()
            val colorBlindFilter by navigationViewModel.colorBlindFilterFlow.collectAsState()
            val isSystemDark = (LocalConfiguration.current.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
            val isDarkMode = appColorScheme.resolvedColorScheme(isSystemDark)
            AppTheme(darkTheme = isDarkMode, colorBlindFilter = colorBlindFilter) {
                val biometricViewModel: BiometricViewModel = viewModel()

                val context = LocalContext.current

                val mainViews = listOf(
                    NavigationScreens.Home,
                    NavigationScreens.Portfolio,
                    NavigationScreens.Market,
                    NavigationScreens.Settings,
                ).map {
                    it.getRouteString()
                }

                val currentBackStackEntry by navigationViewModel.navHostController.currentBackStackEntryAsState()
                val currentRoute by remember {
                    derivedStateOf {
                        currentBackStackEntry?.destination?.route?.split(".")?.last() ?: NavigationScreens.Loading.getRouteString()
                    }
                }

                var canShowNavBar by remember { mutableStateOf(false) }
                val isNavBarVisible = canShowNavBar && mainViews.any { it == currentRoute }
                val mainActivity = context.getActivity()
                val navBarLayout by rememberMainActivityNavigationBarLayout(mainActivity)
                val navBarHeight = navBarLayout.bottomInsetPx.pxToDp()
                val horizontalSafeArea = rememberHorizontalSafeAreaInsets(navBarLayout)
                val displayCutoutStartInset = horizontalSafeArea.startDp
                val displayCutoutEndInset = horizontalSafeArea.endDp
                // Use the real bottom inset in all orientations. Landscape 3-button nav may sit
                // on the side (bottom inset 0) or remain at the bottom — do not force 0 here.
                val tabBarSystemNavInset = if (
                    !navBarLayout.isGestureInteraction || isNavBarVisible
                ) {
                    navBarHeight
                } else {
                    0.dp
                }
                val realNavbarHeight: MutableState<Dp> = remember { mutableStateOf(0.dp) }

                val showMoreTip: MutableState<Boolean> = remember { mutableStateOf(false) }

                var hasEnteredMainFlow by remember { mutableStateOf(false) }
                var isLoggedIn by remember { mutableStateOf(false) }
                var isLocked by remember { mutableStateOf(false) }
                var needsToUpdate by remember { mutableStateOf(false) }
                var showWhatsNew by remember { mutableStateOf(false) }

                LaunchedEffect(Unit) {
                    launch {
                        login { mustUpdate ->
                            isLoggedIn = true
                            if (mustUpdate) {
                                needsToUpdate = true
                            }

                            val now = Date()
                            val ratingPopupDate: Date? = StorageManager.get(StorageKeys.APP_RATING_POPUP_DATE)
                            val actionRatingDate: Date? = StorageManager.get(StorageKeys.APP_ACTION_RATING_DATE)

                            if (ratingPopupDate != null && now.after(ratingPopupDate)) {
                                navigationViewModel.showAppReview(context)
                            } else if (ratingPopupDate == null) {
                                StorageManager.set(StorageKeys.APP_RATING_POPUP_DATE, dateByAddingDays(days = 4))
                            }

                            if (actionRatingDate == null) {
                                StorageManager.set(StorageKeys.APP_ACTION_RATING_DATE, dateByAddingDays(days = 4))
                            }
                        }
                    }

                    // Allow Loading screen to fully render and system to settle before navigating.
                    delay(50)

                    // If smallToplist is empty, download it before navigating
                    if (ToplistRepository.smallToplist.isNullOrEmpty()) {
                        ToplistRepository.getSmallToplist()
                    }

                    val whatsNewContentVersion = StorageKeys.CURRENT_WHATS_NEW_VERSION.rawValue
                    if (UserPreferences.getStoredWhatsNewVersion() != whatsNewContentVersion) {
                        UserPreferences.setStoredWhatsNewVersion(whatsNewContentVersion)
                        showWhatsNew = true
                    }

                    if (UserPreferences.getIsBiometricAuthenticationEnabled()) {
                        isLocked = true
                        authenticate(biometricViewModel) {
                            navigateToMainFlow()
                        }
                    } else {
                        navigateToMainFlow()
                    }
                }

                LaunchedEffect(Unit) {
                    setStatusBarColor(isDarkMode)
                }

                LaunchedEffect(Unit) {
                    delay(0.1.seconds)
                    canShowNavBar = true
                }

                LaunchedEffect(currentRoute) {
                    if (UserPreferences.getStartTab().getName() == currentRoute) {
                        shouldShowMoreTip(showMoreTip)
                    }

                    if (currentRoute != "Loading") {
                        if (!hasEnteredMainFlow) {
                            hasEnteredMainFlow = true
                            askNotificationPermission()

                            GlobalScope.launch {
                                PriceAlertsRepository.updateUser()
                            }
                        }
                    }

                    NavigationViewModel.currentScreen = currentRoute

                    when (currentRoute) {
                        NavigationScreens.Home.getRouteString() -> UserPreferences.setLatestTabIndex(0)
                        NavigationScreens.Portfolio.getRouteString() -> UserPreferences.setLatestTabIndex(1)
                        NavigationScreens.Market.getRouteString() -> UserPreferences.setLatestTabIndex(2)
                        NavigationScreens.Settings.getRouteString() -> UserPreferences.setLatestTabIndex(3)
                    }
                }

                LaunchedEffect(appColorScheme, isSystemDark) {
                    setStatusBarColor(isDarkMode)
                }

                LaunchedEffect(context.getActivity()?.intent, currentRoute) {
                    if (currentRoute != "Loading") {
                        deepLink()
                    }
                }

                OnLifecycleEvent { _, event ->
                    when (event) {
                        Lifecycle.Event.ON_DESTROY -> {
                            StorageManager.set(StorageKeys.HAS_CLOSED_APP, true)
                        }

                        Lifecycle.Event.ON_RESUME -> {
                            window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)

                            if (currentRoute != "Loading") {
                                GlobalScope.launch {
                                    PriceAlertsRepository.updateUser()
                                }
                            }

                            if (UserPreferences.getIsBiometricAuthenticationEnabled() && biometricViewModel.isAppLocked) {
                                authenticate(biometricViewModel) {
                                    deepLink()
                                }
                            } else {
                                Handler(Looper.getMainLooper()).postDelayed({
                                    deepLink()
                                }, 1000L)
                            }
                        }

                        Lifecycle.Event.ON_PAUSE -> {
                            if (UserPreferences.getIsBiometricAuthenticationEnabled()) {
                                biometricViewModel.isAppLocked = true
                            }

                            window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
                        }

                        else -> {}
                    }
                }

                Scaffold(
                    topBar = { },
                    floatingActionButton = { },
                    content = {
                        NavigationEntries(
                            navigationViewModel,
                            navBarLayout.statusBarTopPx.pxToDp(),
                            navigationViewModel.animationDuration,
                            if (!navBarLayout.isGestureInteraction || isNavBarVisible) navBarHeight else 0.dp,
                            realNavbarHeight.value,
                            systemNavigationBarBottomInset = tabBarSystemNavInset,
                            displayCutoutStartInset = displayCutoutStartInset,
                            displayCutoutEndInset = displayCutoutEndInset,
                            isLoggedIn = isLoggedIn,
                            isLocked = isLocked,
                            needsToUpdate = needsToUpdate,
                            onLoadingAuthenticate = {
                                authenticate(biometricViewModel) {
                                    navigateToMainFlow()
                                }
                            }
                        )

                        Box(
                            contentAlignment = Alignment.BottomCenter,
                            modifier = Modifier
                                .fillMaxSize()
                                .alpha(if (currentRoute == "Loading") 0f else 1f)
                        ) {
                            BottomNavigationBar(
                                navigationViewModel,
                                navigationViewModel.animationDuration,
                                realNavbarHeight,
                                navigationViewModel.selectedItem,
                                showMoreTip,
                                isNavBarVisible,
                                systemNavigationBarBottomInset = tabBarSystemNavInset,
                                systemNavigationBarStartInset = displayCutoutStartInset,
                                systemNavigationBarEndInset = displayCutoutEndInset,
                            )

                            SnackBarView(
                                navigationViewModel,
                                isNavBarVisible,
                                navBarHeight = realNavbarHeight.value,
                                systemNavbarHeight = navBarHeight,
                            )

                            if (biometricViewModel.isAppLocked) {
                                BackgroundGradient()

                                LoadingScreen(
                                    isLoggedIn = true,
                                    isLocked = true,
                                    authenticate = {
                                        authenticate(biometricViewModel) {
                                            deepLink()
                                        }
                                    }
                                )
                            }
                        }

                        if (showWhatsNew) {
                            WhatsNewSheet {
                                showWhatsNew = false
                            }
                        }

                        if (needsToUpdate) {
                            ForceUpdateScreen {
                                navigateToMarketplace()
                            }
                        }
                    },
                    bottomBar = {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colors.surfaceContainerLow)
                        )
                    }
                )
            }
        }
    }

    companion object {
        @Volatile
        private var instance: WeakReference<MainActivity>? = null

        fun current(): MainActivity? = instance?.get()

        private fun dateByAddingDays(days: Int, from: Date = Date()): Date {
            val cal: Calendar = Calendar.getInstance()
            cal.time = from
            cal.add(Calendar.DATE, days)
            return cal.time
        }

        fun showSnackbar(
            context: Context,
            status: SnackBarType,
            message: String,
        ) {
            val deliver = Runnable {
                val activity = context.getActivity() ?: instance?.get() ?: return@Runnable
                if (!activity::navigationViewModel.isInitialized) return@Runnable
                activity.navigationViewModel.showSnackbar(
                    context = activity,
                    type = status,
                    message = message
                )
            }
            if (Looper.myLooper() == Looper.getMainLooper()) {
                deliver.run()
            } else {
                Handler(Looper.getMainLooper()).post(deliver)
            }
        }

        fun showAppReview(
            context: Context,
            force: Boolean = false
        ) {
            val activity = context.getActivity() ?: instance?.get() ?: return
            if (force) {
                activity.navigationViewModel.showAppReview(activity, delaySeconds = 2)
                return
            }

            val now = Date()
            val actionDate: Date? = StorageManager.get(StorageKeys.APP_ACTION_RATING_DATE)

            if (actionDate != null && now.after(actionDate)) {
                StorageManager.set(StorageKeys.APP_RATING_POPUP_DATE, dateByAddingDays(days = 30))
                StorageManager.set(StorageKeys.APP_ACTION_RATING_DATE, dateByAddingDays(days = 21))

                activity.navigationViewModel.showAppReview(activity)
            }
        }
    }

    override fun onDestroy() {
        if (instance?.get() === this) {
            instance = null
        }
        super.onDestroy()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    private fun login(onComplete: (mustUpdate: Boolean) -> Unit) {
        FirebaseManager.setupFirebase(this, baseContext) { mustUpdate ->
            onComplete(mustUpdate)
        }
    }

    private fun navigateToMarketplace() {
        try {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("market://details?id=com.cryptium.coincurrently")
                )
            )
        } catch (_: ActivityNotFoundException) {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=com.cryptium.coincurrently")
                )
            )
        }
    }

    private fun navigateToMainFlow() {
        val screen = when (UserPreferences.getStartTab()) {
            SelectedStartTab.LastUsed -> navigationScreenForLatestTab(UserPreferences.getLatestTabIndex())
            SelectedStartTab.Home -> NavigationScreens.Home
            SelectedStartTab.Portfolio -> NavigationScreens.Portfolio
            SelectedStartTab.Market -> NavigationScreens.Market
            else -> NavigationScreens.Settings
        }

        navigationViewModel.selectedItem.intValue = when (screen) {
            NavigationScreens.Home -> 0
            NavigationScreens.Portfolio -> 1
            NavigationScreens.Market -> 2
            NavigationScreens.Settings -> 3
            else -> 0
        }

        navigationViewModel.navigateToMainTab(screen)
        deepLink()
    }

    private fun setStartData() {
        StorageManager.setCache(baseContext)
    }

    private fun resolvedDarkMode(): Boolean {
        val scheme = StorageManager.get<AppColorScheme>(StorageKeys.APP_COLOR_SCHEME) ?: AppColorScheme.DARK
        val isSystemDark = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
        return scheme.resolvedColorScheme(isSystemDark)
    }

    private fun showUnsupportedDeviceScreen() {
        setStatusBarColor(resolvedDarkMode())
        val message = TextView(this).apply {
            text = "CoinCurrently can't run on this device's Android build. Updating your system software may help."
            gravity = Gravity.CENTER
            setPadding(48, 48, 48, 48)
            setTextColor(ContextCompat.getColor(context, R.color.on_surface))
        }
        setContentView(
            FrameLayout(this).apply {
                setBackgroundColor(ContextCompat.getColor(context, R.color.surface))
                addView(
                    message,
                    FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.WRAP_CONTENT,
                        Gravity.CENTER,
                    ),
                )
            },
        )
    }

    private fun setStatusBarColor(isDarkMode: Boolean) {
        val scheme = StorageManager.get<AppColorScheme>(StorageKeys.APP_COLOR_SCHEME) ?: AppColorScheme.DARK
        val preferredScheme = scheme.preferredScheme
        AppCompatDelegate.setDefaultNightMode(
            when (preferredScheme) {
                null -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                true -> AppCompatDelegate.MODE_NIGHT_YES
                false -> AppCompatDelegate.MODE_NIGHT_NO
            }
        )

        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = !isDarkMode

        window.statusBarColor = ContextCompat.getColor(baseContext, R.color.headerColor)
        window.navigationBarColor = ContextCompat.getColor(baseContext, R.color.surface_container_low)
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightNavigationBars = !isDarkMode
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
    }

    private fun authenticate(
        biometricViewModel: BiometricViewModel,
        callback: () -> Unit
    ) {
        biometricViewModel.setPrompt(baseContext, this) {
            if (it == AuthenticationStatus.SUCCESS) {
                callback.invoke()
            }
        }

        biometricViewModel.authenticate(baseContext)
    }

    private fun deepLink() {
        intent?.getStringExtra("fearID")?.let {
            if (it.isNotEmpty()) {

                AnalyticsManager.log(
                    Navigate(
                        fromScreen = screenLabelFromRoute(navigationViewModel.analyticsRouteSuffixForNavigate()),
                        toScreen = ScreenLabel.FearGreed,
                    ),
                )
                navigationViewModel.deepLink(NavigationScreens.FearGreed)
            }
        }

        intent?.getStringExtra("cryptoID")?.let {
            if (it.isNotEmpty() && it == "market") {
                try {
                    this.startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("market://details?id=com.cryptium.coincurrently")
                        )
                    )
                } catch (_: ActivityNotFoundException) {
                    this.startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://play.google.com/store/apps/details?id=com.cryptium.coincurrently")
                        )
                    )
                }

            } else if (it.isNotEmpty() && it == "portfolio") {
                AnalyticsManager.log(
                    Navigate(
                        fromScreen = screenLabelFromRoute(navigationViewModel.analyticsRouteSuffixForNavigate()),
                        toScreen = ScreenLabel.Portfolio,
                    ),
                )
                navigationViewModel.deepLink(NavigationScreens.Portfolio)
            } else if (it.isNotEmpty()) {
                val cryptoDetail = CryptoDetailInfoModel(it)
                val cryptoDest = NavigationScreens.CryptoDetail(cryptoDetail)

                AnalyticsManager.log(
                    Navigate(
                        fromScreen = screenLabelFromRoute(navigationViewModel.analyticsRouteSuffixForNavigate()),
                        toScreen = ScreenLabel.CryptoDetail,
                        coinName = cryptoDetail.name ?: "",
                    ),
                )
                navigationViewModel.deepLink(cryptoDest)
            } else {
            }
        }

        intent = Intent()
    }

    private fun shouldShowMoreTip(showMoreTip: MutableState<Boolean>) {
        if (!isTablet()) {
            if (UserPreferences.getMoreHintDate() == null) {
                UserPreferences.setMoreHintDate(1)
            } else {
                Handler(Looper.getMainLooper()).postDelayed({
                    val calendar = Calendar.getInstance(AppLocale.enUsPosix).time
                    val savedDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", AppLocale.enUsPosix).parse(UserPreferences.getMoreHintDate())

                    if (calendar.after(savedDate)) {
                        UserPreferences.setMoreHintDate(9999)
                        showMoreTip.value = true
                    }
                }, 1000L)
            }
        }
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    GlobalScope.launch {
                        PriceAlertsRepository.updateUser()
                    }
                }

                shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                    val hasAskedForPush = StorageManager.get<Boolean>(StorageKeys.HAS_ASKED_FOR_PUSH_NOTIFICATIONS) ?: false

                    if (!hasAskedForPush) {
                        requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }
                else -> {
                    val hasAskedForPush = StorageManager.get<Boolean>(StorageKeys.HAS_ASKED_FOR_PUSH_NOTIFICATIONS) ?: false

                    if (!hasAskedForPush) {
                        requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }
            }
        } else if (NotificationManagerCompat.from(this).areNotificationsEnabled()) {
            GlobalScope.launch {
                PriceAlertsRepository.updateUser()
            }
        }
    }
}

@Composable
private fun SnackBarView(
    navigationViewModel: NavigationViewModel,
    isNavBarVisible: Boolean,
    navBarHeight: Dp,
    systemNavbarHeight: Dp
) {
    SnackbarView(
        showSnackBar = navigationViewModel.showSnackbar.value,
        message = navigationViewModel.snackbarText,
        buttonTitle = navigationViewModel.snackbarButtonText,
        type = navigationViewModel.snackbarType,
        isTabbarHidden = !isNavBarVisible,
        navBarHeight = navBarHeight,
        systemNavbarHeight = systemNavbarHeight,
        snackbarTrigger = navigationViewModel.snackbarTrigger,
        onDismissRequest = { navigationViewModel.showSnackbar.value = false },
        buttonAction = {
            navigationViewModel.snackbarCallback.invoke()
        }
    )
}