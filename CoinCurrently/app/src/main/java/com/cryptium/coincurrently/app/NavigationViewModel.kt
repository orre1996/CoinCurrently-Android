package com.cryptium.coincurrently.app
import com.cryptium.coincurrently.core.persistence.UserPreferences
import com.cryptium.coincurrently.app.NavigationScreens
import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.compose.DialogNavigator
import com.cryptium.coincurrently.core.models.AppColorScheme
import com.cryptium.coincurrently.core.persistence.StorageManager
import com.cryptium.coincurrently.core.persistence.StorageKeys
import com.cryptium.coincurrently.core.extensions.getActivity
import com.cryptium.coincurrently.core.extensions.selectedTabbarItem
import com.cryptium.coincurrently.sharedui.components.SnackBarType
import com.cryptium.coincurrently.core.analytics.BuyProAction
import com.cryptium.coincurrently.core.analytics.BuyProOpened
import com.cryptium.coincurrently.core.analytics.AnalyticsManager
import com.cryptium.coincurrently.core.analytics.ScreenLabel
import com.cryptium.coincurrently.core.analytics.navRouteSuffixForAnalytics
import com.google.android.play.core.review.ReviewManagerFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class NavigationViewModel(context: Context): ViewModel() {
    companion object {
        var currentScreen = "Loading"
    }

    var navHostController = NavHostController(context)

    val selectedItem = mutableIntStateOf(0)
    val marketSubTabRequestId = mutableIntStateOf(0)
    val marketSelectedPage = mutableIntStateOf(0)
    val animationDuration: MutableState<Int> = mutableIntStateOf(300)

    var showBuyPro: MutableState<Boolean> = mutableStateOf(false)

    fun openBuyPro(fromScreen: ScreenLabel, action: BuyProAction) {
        AnalyticsManager.log(BuyProOpened(fromScreen, action))
        showBuyPro.value = true
    }

/** Route suffix for the current navigation destination; use with `screenLabelFromRoute` when logging [com.cryptium.coincurrently.core.analytics.Navigate]. */
    fun analyticsRouteSuffixForNavigate(): String? =
        navHostController.currentBackStackEntry
            ?.destination
            ?.route
            ?.let { navRouteSuffixForAnalytics(it) }
            ?: NavigationViewModel.currentScreen

    private val _appColorScheme: MutableStateFlow<AppColorScheme> = MutableStateFlow(
        StorageManager.get<AppColorScheme>(StorageKeys.APP_COLOR_SCHEME) ?: AppColorScheme.DARK
    )
    val appColorSchemeFlow: StateFlow<AppColorScheme> = _appColorScheme.asStateFlow()
    var appColorScheme: AppColorScheme
        get() = _appColorScheme.value
        set(value) {
            _appColorScheme.value = value
        }

    private val _colorBlindFilter: MutableStateFlow<Boolean> = MutableStateFlow(
        UserPreferences.getColorBlind()
    )
    val colorBlindFilterFlow: StateFlow<Boolean> = _colorBlindFilter.asStateFlow()
    var colorBlindFilter: Boolean
        get() = _colorBlindFilter.value
        set(value) {
            _colorBlindFilter.value = value
        }

    private val _currencyRevision = MutableStateFlow(0)
    val currencyRevisionFlow: StateFlow<Int> = _currencyRevision.asStateFlow()

    fun notifyCurrencyChanged() {
        _currencyRevision.value++
    }

    private var canShowSnackbar by mutableStateOf(true)
    var snackbarType by mutableStateOf(SnackBarType.SUCCESS)
    var snackbarText by mutableStateOf("")
    var snackbarButtonText by mutableStateOf("")
    var showSnackbar = mutableStateOf(false)
    var snackbarTrigger by mutableIntStateOf(0)
    var snackbarCallback: () -> Unit = { }

    /** Portfolio event id (UUID string); consumed in PortfolioScreen after lookup (manual tx, swap, or wallet import). */
    var pendingPortfolioTransactionId by mutableStateOf<String?>(null)
    /** Opens transaction details on Portfolio after "View Details" on a snackbar shown from a transaction screen. */
    var pendingPortfolioOpenTransactionId by mutableStateOf<String?>(null)

    init {
        navHostController.navigatorProvider.addNavigator(ComposeNavigator())
        navHostController.navigatorProvider.addNavigator(DialogNavigator())
    }

    fun goToTab(newTab: Int) {
        selectedItem.intValue = newTab
        animationDuration.value = 0

        val destination = newTab.selectedTabbarItem
        navHostController.navigate(destination) {
            // Pop the entire nav graph so main tabs are not stacked (back must not switch tabs).
            popUpTo(navHostController.graph.id) {
                inclusive = true
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }

        Handler(Looper.getMainLooper()).postDelayed({
            animationDuration.value = 300
        }, 100L)
    }

    /** Opens Market and selects a sub-tab (0 = Coins). Prefer over goToTab(2) when saved pager state should be overridden. */
    fun goToMarketSubTab(page: Int) {
        marketSelectedPage.intValue = page
        marketSubTabRequestId.intValue++
        goToTab(2)
    }

    fun showSnackbar(
        context: Context,
        type: SnackBarType,
        message: String,
        buttonTitle: String = "",
        callback: () -> Unit = { }
    ) {
        val mainHandler = Handler(Looper.getMainLooper())
        mainHandler.postDelayed({
            if (!canShowSnackbar) return@postDelayed
            canShowSnackbar = false

            mainHandler.postDelayed({
                canShowSnackbar = true
            }, 1000L)

            snackbarType = type
            snackbarText = message
            snackbarButtonText = buttonTitle
            snackbarCallback = callback

            val generation = snackbarTrigger + 1
            snackbarTrigger = generation
            showSnackbar.value = true

            mainHandler.postDelayed({
                if (generation != snackbarTrigger) return@postDelayed
                showAppReview(context, delaySeconds = 0)
                showSnackbar.value = false
            }, 5000L)
        }, 500L)
    }

    @JvmOverloads
    fun showAppReview(context: Context, delaySeconds: Int = 5) {
        Handler(Looper.getMainLooper()).postDelayed({
            val activity = context.getActivity() ?: MainActivity.current() ?: return@postDelayed
            if (activity.isFinishing || activity.isDestroyed) return@postDelayed

            val manager = ReviewManagerFactory.create(activity)
            val request = manager.requestReviewFlow()

            request.addOnCompleteListener { task ->
                if (!task.isSuccessful) return@addOnCompleteListener
                if (activity.isFinishing || activity.isDestroyed) return@addOnCompleteListener
                manager.launchReviewFlow(activity, task.result)
            }
        }, delaySeconds * 1000L)
    }

    fun navigate(to: NavigationScreens) {
        navHostController.navigate(to)
    }

    /** Main tab bar: custom NavOptions (popUpTo saveState etc.). */
    fun navigate(to: NavigationScreens, builder: NavOptionsBuilder.() -> Unit) {
        navHostController.navigate(to, builder)
    }

    /** First navigation after splash; clears [NavigationScreens.Loading] from the back stack so the user cannot return to it. */
    fun navigateToMainTab(to: NavigationScreens) {
        navHostController.navigate(to) {
            popUpTo(navHostController.graph.id) {
                inclusive = true
            }
            launchSingleTop = true
        }
    }

    fun navigateBack() {
        navHostController.popBackStack()
    }

    fun popToRoot() {
        navHostController.popBackStack(route = selectedItem.intValue.selectedTabbarItem, inclusive = false)
    }

    fun deepLink(to: NavigationScreens) {
        navHostController.popBackStack(route = selectedItem.intValue.selectedTabbarItem, inclusive = false)

        Handler(Looper.getMainLooper()).postDelayed({
            navHostController.navigate(to)
        }, 400L)
    }
}