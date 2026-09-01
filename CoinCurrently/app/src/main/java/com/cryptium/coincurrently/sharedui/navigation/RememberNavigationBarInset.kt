package com.cryptium.coincurrently.sharedui.navigation
import android.view.View
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.cryptium.coincurrently.app.MainActivity
import com.cryptium.coincurrently.core.extensions.getContentHorizontalNavInsetLeftPx
import com.cryptium.coincurrently.core.extensions.getContentHorizontalNavInsetRightPx
import com.cryptium.coincurrently.core.extensions.getDisplayCutoutInsetLeftPx
import com.cryptium.coincurrently.core.extensions.getDisplayCutoutInsetRightPx
import com.cryptium.coincurrently.core.extensions.getNavigationBarHeight
import com.cryptium.coincurrently.core.extensions.getNavigationBarInsetBottomPx
import com.cryptium.coincurrently.core.extensions.getNavigationBarInsetLeftPx
import com.cryptium.coincurrently.core.extensions.getNavigationBarInsetRightPx
import com.cryptium.coincurrently.core.extensions.getStatusBarInsetTopPx
import com.cryptium.coincurrently.core.extensions.isGestureNavigationInteraction

data class MainActivityNavigationBarLayout(
    val statusBarTopPx: Int,
    val bottomInsetPx: Int,
    val isGestureInteraction: Boolean,
    val displayCutoutLeftPx: Int = 0,
    val displayCutoutRightPx: Int = 0,
    val navigationBarLeftPx: Int = 0,
    val navigationBarRightPx: Int = 0,
    val contentHorizontalNavLeftPx: Int = 0,
    val contentHorizontalNavRightPx: Int = 0,
)

data class HorizontalSafeAreaInsets(
    val startDp: Dp,
    val endDp: Dp,
)

/**
 * Start/end safe area from display cutout plus on-screen navigation bar insets (3-button /
 * 2-button only). Gesture navigation keeps cutout padding only.
 */
@Composable
fun rememberHorizontalSafeAreaInsets(
    navBarLayout: MainActivityNavigationBarLayout,
): HorizontalSafeAreaInsets {
    val density = LocalDensity.current
    val layoutDirection = LocalLayoutDirection.current
    val configuration = LocalConfiguration.current

    val composeCutoutStartPx = WindowInsets.displayCutout.getLeft(density, layoutDirection)
    val composeCutoutEndPx = WindowInsets.displayCutout.getRight(density, layoutDirection)

    val viewCutoutStartPx = if (layoutDirection == LayoutDirection.Ltr) {
        navBarLayout.displayCutoutLeftPx
    } else {
        navBarLayout.displayCutoutRightPx
    }
    val viewCutoutEndPx = if (layoutDirection == LayoutDirection.Ltr) {
        navBarLayout.displayCutoutRightPx
    } else {
        navBarLayout.displayCutoutLeftPx
    }
    val navStartPx = if (layoutDirection == LayoutDirection.Ltr) {
        navBarLayout.contentHorizontalNavLeftPx
    } else {
        navBarLayout.contentHorizontalNavRightPx
    }
    val navEndPx = if (layoutDirection == LayoutDirection.Ltr) {
        navBarLayout.contentHorizontalNavRightPx
    } else {
        navBarLayout.contentHorizontalNavLeftPx
    }

    return remember(
        configuration.orientation,
        configuration.screenWidthDp,
        layoutDirection,
        navBarLayout.isGestureInteraction,
        composeCutoutStartPx,
        composeCutoutEndPx,
        viewCutoutStartPx,
        viewCutoutEndPx,
        navStartPx,
        navEndPx,
    ) {
        HorizontalSafeAreaInsets(
            startDp = with(density) {
                maxOf(composeCutoutStartPx, viewCutoutStartPx, navStartPx).toDp()
            },
            endDp = with(density) {
                maxOf(composeCutoutEndPx, viewCutoutEndPx, navEndPx).toDp()
            },
        )
    }
}

/**
 * Bottom inset and gesture-vs-button navigation, updated when window insets are applied (initial
 * Compose pass often runs before [android.view.WindowInsets] are available).
 */
@Composable
fun rememberMainActivityNavigationBarLayout(mainActivity: MainActivity?): State<MainActivityNavigationBarLayout> {
    val view = LocalView.current
    val orientation = LocalConfiguration.current.orientation
    val state = remember { mutableStateOf(MainActivityNavigationBarLayout(0, 0, false)) }

    DisposableEffect(view, mainActivity, orientation) {
        val act = mainActivity
        if (act == null) {
            onDispose { }
        } else {
            val decorView = act.window.decorView

            fun compatFrom(insets: android.view.WindowInsets?): WindowInsetsCompat? {
                if (insets != null) {
                    return WindowInsetsCompat.toWindowInsetsCompat(insets, decorView)
                }
                val rootInsetsCompat = ViewCompat.getRootWindowInsets(decorView)
                if (rootInsetsCompat != null) return rootInsetsCompat
                val rootInsets = decorView.rootWindowInsets
                return rootInsets?.let { WindowInsetsCompat.toWindowInsetsCompat(it, decorView) }
            }

            fun refresh(insets: android.view.WindowInsets?): MainActivityNavigationBarLayout {
                val compat = compatFrom(insets)
                return MainActivityNavigationBarLayout(
                    statusBarTopPx = act.getStatusBarInsetTopPx(compat),
                    bottomInsetPx = if (compat != null) {
                        act.getNavigationBarInsetBottomPx(compat)
                    } else {
                        act.getNavigationBarHeight()
                    },
                    isGestureInteraction = act.isGestureNavigationInteraction(compat),
                    displayCutoutLeftPx = act.getDisplayCutoutInsetLeftPx(compat),
                    displayCutoutRightPx = act.getDisplayCutoutInsetRightPx(compat),
                    navigationBarLeftPx = act.getNavigationBarInsetLeftPx(compat),
                    navigationBarRightPx = act.getNavigationBarInsetRightPx(compat),
                    contentHorizontalNavLeftPx = act.getContentHorizontalNavInsetLeftPx(compat),
                    contentHorizontalNavRightPx = act.getContentHorizontalNavInsetRightPx(compat),
                )
            }

            fun update() {
                state.value = refresh(decorView.rootWindowInsets)
            }

            update()
            decorView.post {
                ViewCompat.requestApplyInsets(decorView)
                update()
            }

            val listener = View.OnApplyWindowInsetsListener { _, insets ->
                state.value = refresh(insets)
                insets
            }
            decorView.setOnApplyWindowInsetsListener(listener)
            onDispose {
                decorView.setOnApplyWindowInsetsListener(null)
            }
        }
    }

    return state
}
