package com.cryptium.coincurrently.core.extensions
import android.annotation.SuppressLint
import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.view.View
import androidx.core.view.WindowInsetsCompat
import com.cryptium.coincurrently.app.MainActivity
import com.cryptium.coincurrently.widgets.glance.LargeMultiCryptoWidgetReceiver
import com.cryptium.coincurrently.widgets.glance.LargeQuadCryptoWidgetReceiver
import com.cryptium.coincurrently.widgets.glance.MediumDuoCryptoWidgetReceiver
import com.cryptium.coincurrently.widgets.glance.SmallCryptoWidgetReceiver
import com.cryptium.coincurrently.widgets.receivers.large_portfolio_widget
import com.cryptium.coincurrently.widgets.receivers.medium_portfolio_widget
import com.cryptium.coincurrently.widgets.receivers.small_portfolio_widget

@SuppressLint("SimpleDateFormat")
fun MainActivity.updateCurrecyWidgets() {
    notifyWidgetUpdate(SmallCryptoWidgetReceiver::class.java)
    notifyWidgetUpdate(MediumDuoCryptoWidgetReceiver::class.java)
    notifyWidgetUpdate(LargeQuadCryptoWidgetReceiver::class.java)
    notifyWidgetUpdate(LargeMultiCryptoWidgetReceiver::class.java)
}

private fun Context.notifyWidgetUpdate(receiver: Class<*>) {
    val manager = AppWidgetManager.getInstance(this)
    val ids = manager.getAppWidgetIds(ComponentName(this, receiver))
    if (ids.isEmpty()) return
    sendBroadcast(
        Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE).apply {
            component = ComponentName(this@notifyWidgetUpdate, receiver)
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
        },
    )
}

fun Context.updatePortfolioWidgets() {
    notifyWidgetUpdate(small_portfolio_widget::class.java)
    notifyWidgetUpdate(medium_portfolio_widget::class.java)
    notifyWidgetUpdate(large_portfolio_widget::class.java)
}

fun reloadPortfolioWidgets() {
    MainActivity.current()?.updatePortfolioWidgets()
}

fun MainActivity.getStatusBarHeight(): Int {
    var result = 0
    val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
    if (resourceId > 0) {
        result = resources.getDimensionPixelSize(resourceId)
    }
    return result
}

fun MainActivity.getStatusBarInsetTopPx(compat: WindowInsetsCompat?): Int {
    if (compat != null) {
        val top = compat.getInsets(WindowInsetsCompat.Type.statusBars()).top
        if (top > 0) return top
    }
    return getStatusBarHeight()
}

fun MainActivity.getDisplayCutoutInsetLeftPx(compat: WindowInsetsCompat?): Int {
    if (compat == null) return 0
    return compat.getInsets(WindowInsetsCompat.Type.displayCutout()).left
}

fun MainActivity.getDisplayCutoutInsetRightPx(compat: WindowInsetsCompat?): Int {
    if (compat == null) return 0
    return compat.getInsets(WindowInsetsCompat.Type.displayCutout()).right
}

fun MainActivity.getNavigationBarInsetLeftPx(compat: WindowInsetsCompat?): Int {
    if (compat == null) return 0
    val navBarsLeft = compat.getInsets(WindowInsetsCompat.Type.navigationBars()).left
    if (navBarsLeft > 0) return navBarsLeft
    val tappableLeft = compat.getInsets(WindowInsetsCompat.Type.tappableElement()).left
    if (tappableLeft > 0) return tappableLeft
    val gestureLeft = compat.getInsets(WindowInsetsCompat.Type.systemGestures()).left
    if (gestureLeft > 0) return gestureLeft
    return getLandscapeThreeButtonNavBarInsetPx(
        isStartEdge = true,
        isGestureInteraction = isGestureNavigationInteraction(compat),
    )
}

fun MainActivity.getNavigationBarInsetRightPx(compat: WindowInsetsCompat?): Int {
    if (compat == null) return 0
    val navBarsRight = compat.getInsets(WindowInsetsCompat.Type.navigationBars()).right
    if (navBarsRight > 0) return navBarsRight
    val tappableRight = compat.getInsets(WindowInsetsCompat.Type.tappableElement()).right
    if (tappableRight > 0) return tappableRight
    val gestureRight = compat.getInsets(WindowInsetsCompat.Type.systemGestures()).right
    if (gestureRight > 0) return gestureRight
    return getLandscapeThreeButtonNavBarInsetPx(
        isStartEdge = false,
        isGestureInteraction = isGestureNavigationInteraction(compat),
    )
}

/**
 * Horizontal content padding from on-screen navigation only (3-button / 2-button). Gesture and
 * tappable side insets are excluded — content should go edge-to-edge aside from display cutout.
 */
fun MainActivity.getContentHorizontalNavInsetLeftPx(compat: WindowInsetsCompat?): Int {
    if (isSystemGestureNavigationMode()) return 0
    if (compat != null && isGestureNavigationInteraction(compat)) return 0

    val navBarsLeft = compat?.getInsets(WindowInsetsCompat.Type.navigationBars())?.left ?: 0
    if (navBarsLeft > 0) return navBarsLeft

    return getLandscapeThreeButtonNavBarInsetPx(
        isStartEdge = true,
        isGestureInteraction = false,
    )
}

fun MainActivity.getContentHorizontalNavInsetRightPx(compat: WindowInsetsCompat?): Int {
    if (isSystemGestureNavigationMode()) return 0
    if (compat != null && isGestureNavigationInteraction(compat)) return 0

    val navBarsRight = compat?.getInsets(WindowInsetsCompat.Type.navigationBars())?.right ?: 0
    if (navBarsRight > 0) return navBarsRight

    return getLandscapeThreeButtonNavBarInsetPx(
        isStartEdge = false,
        isGestureInteraction = false,
    )
}

private fun MainActivity.systemNavigationBarInteractionMode(): Int? {
    val resourceId = resources.getIdentifier("config_navBarInteractionMode", "integer", "android")
    return if (resourceId > 0) resources.getInteger(resourceId) else null
}

/** 0 = 3-button, 1 = 2-button, 2 = gesture. */
fun MainActivity.isSystemGestureNavigationMode(): Boolean =
    systemNavigationBarInteractionMode() == 2

fun MainActivity.isSystemOnScreenNavigationMode(): Boolean {
    return when (systemNavigationBarInteractionMode()) {
        0, 1 -> true
        else -> false
    }
}

/**
 * Landscape 3-button nav often sits on the screen edge; under edge-to-edge the framework may
 * report zero horizontal insets. Reserve [navigation_bar_height] on the nav edge (end for LTR).
 */
fun MainActivity.getLandscapeThreeButtonNavBarInsetPx(
    isStartEdge: Boolean,
    isGestureInteraction: Boolean,
): Int {
    if (resources.configuration.orientation != Configuration.ORIENTATION_LANDSCAPE) return 0
    if (isSystemGestureNavigationMode()) return 0
    if (isGestureInteraction && !isSystemOnScreenNavigationMode()) return 0

    val barPx = getNavigationBarHeightPx()
    if (barPx <= 0) return 0

    val decorView = window.decorView
    val compat = decorView.rootWindowInsets?.let {
        WindowInsetsCompat.toWindowInsetsCompat(it, decorView)
    }
    val navBars = compat?.getInsets(WindowInsetsCompat.Type.navigationBars())
    val navLeft = navBars?.left ?: 0
    val navRight = navBars?.right ?: 0
    if (navLeft > 0 || navRight > 0) return 0

    val bottomNav = navBars?.bottom ?: 0
    // Side nav in landscape reports no horizontal inset; bottom inset stays 0. A full bottom
    // inset means the bar is still along the short edge (tablets) — no side reserve needed.
    if (bottomNav > barPx / 2) return 0

    val isRtl =
        resources.configuration.layoutDirection == Configuration.SCREENLAYOUT_LAYOUTDIR_RTL
    return when {
        isStartEdge -> if (isRtl) barPx else 0
        else -> if (isRtl) 0 else barPx
    }
}

fun MainActivity.getNavigationBarHeightPx(): Int {
    val resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android")
    return if (resourceId > 0) {
        resources.getDimensionPixelSize(resourceId)
    } else {
        0
    }
}

/**
 * Bottom inset for laying out above the nav / gesture area (navigation bar inset can be 0 with
 * gesture navigation; system gestures / tappable element still reserve space).
 */
fun MainActivity.getNavigationBarInsetBottomPx(compat: WindowInsetsCompat?): Int {
    var bottomInset = 0
    if (compat != null) {
        val navBars = compat.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
        if (navBars > 0) {
            bottomInset = navBars
        } else {
            val gestures = compat.getInsets(WindowInsetsCompat.Type.systemGestures()).bottom
            if (gestures > 0) {
                bottomInset = gestures
            } else {
                val tappable = compat.getInsets(WindowInsetsCompat.Type.tappableElement()).bottom
                if (tappable > 0) bottomInset = tappable
            }
        }
    }
    if (bottomInset == 0) {
        bottomInset = getNavigationBarHeightPx()
    }
    // Landscape 3-button nav is often on the side — do not keep a bottom reserve in that case.
    // When there is no side nav, keep the measured bottom inset (gesture strip or 3-button bar).
    if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
        val sideNavPx = maxOf(
            getNavigationBarInsetLeftPx(compat),
            getNavigationBarInsetRightPx(compat),
        )
        if (sideNavPx > 0) {
            bottomInset = compat?.getInsets(WindowInsetsCompat.Type.navigationBars())?.bottom ?: 0
        }
    }
    return bottomInset
}

/**
 * Gesture vs on-screen nav bar, inferred from [WindowInsetsCompat] (not [config_navBarInteractionMode],
 * which can disagree with real insets on emulators). Used for the same UI branches that previously
 * checked framework gestural mode.
 */
fun MainActivity.isGestureNavigationInteraction(compat: WindowInsetsCompat?): Boolean {
    if (isSystemGestureNavigationMode()) return true
    if (isSystemOnScreenNavigationMode()) return false
    if (compat == null) return false
    val navBars = compat.getInsets(WindowInsetsCompat.Type.navigationBars())
    if (navBars.left > 0 || navBars.right > 0) {
        return false
    }
    val navBarsBottom = navBars.bottom
    val gestureBottom = compat.getInsets(WindowInsetsCompat.Type.systemGestures()).bottom
    val tappableBottom = compat.getInsets(WindowInsetsCompat.Type.tappableElement()).bottom
    if (navBarsBottom <= 0) {
        // Landscape 3-button nav on the side often reports zero bottom inset while still
        // exposing bottom gesture/tappable insets — that is not gesture navigation.
        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            return false
        }
        return gestureBottom > 0 || tappableBottom > 0
    }
    val resId = resources.getIdentifier("navigation_bar_height", "dimen", "android")
    val typicalBarPx = if (resId > 0) {
        resources.getDimensionPixelSize(resId)
    } else {
        (48f * resources.displayMetrics.density).toInt()
    }
    if (typicalBarPx <= 0) return false
    // Classic bar inset is usually close to [navigation_bar_height]; gesture home strip is shorter.
    return navBarsBottom < typicalBarPx * 82 / 100
}

fun MainActivity.getNavigationBarHeight(): Int {
    val decorView: View = window.decorView
    val rootInsets = decorView.rootWindowInsets
    val compat = rootInsets?.let { WindowInsetsCompat.toWindowInsetsCompat(it, decorView) }
    return getNavigationBarInsetBottomPx(compat)
}

fun Context.isTablet(): Boolean {
    return ((this.resources.configuration.screenLayout
    and Configuration.SCREENLAYOUT_SIZE_MASK)
    >= Configuration.SCREENLAYOUT_SIZE_LARGE)
}