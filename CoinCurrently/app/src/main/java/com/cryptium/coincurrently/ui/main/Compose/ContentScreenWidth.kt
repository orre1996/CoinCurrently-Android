package com.cryptium.coincurrently.ui.main.Compose

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowInsetsCompat
import com.cryptium.coincurrently.ui.main.Activities.MainActivity
import com.cryptium.coincurrently.ui.main.Extensions.getActivity
import com.cryptium.coincurrently.ui.main.Extensions.getContentHorizontalNavInsetLeftPx
import com.cryptium.coincurrently.ui.main.Extensions.getContentHorizontalNavInsetRightPx
import com.cryptium.coincurrently.ui.main.Extensions.getDisplayCutoutInsetLeftPx
import com.cryptium.coincurrently.ui.main.Extensions.getDisplayCutoutInsetRightPx
import com.cryptium.coincurrently.ui.main.Extensions.pxToDp

/** Usable screen width after horizontal display-cutout and system-bar insets. */
@Composable
fun contentScreenWidth(): Dp {
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val layoutDirection = LocalLayoutDirection.current
    val activity = LocalContext.current.getActivity() as? MainActivity

    val composeCutoutStartPx = WindowInsets.displayCutout.getLeft(density, layoutDirection)
    val composeCutoutEndPx = WindowInsets.displayCutout.getRight(density, layoutDirection)

    val compat = activity?.window?.decorView?.rootWindowInsets?.let { insets ->
        WindowInsetsCompat.toWindowInsetsCompat(insets, activity.window.decorView)
    }
    val cutoutStartPx = maxOf(
        composeCutoutStartPx,
        activity?.getDisplayCutoutInsetLeftPx(compat) ?: 0,
    )
    val cutoutEndPx = maxOf(
        composeCutoutEndPx,
        activity?.getDisplayCutoutInsetRightPx(compat) ?: 0,
    )
    val navStartPx = activity?.getContentHorizontalNavInsetLeftPx(compat) ?: 0
    val navEndPx = activity?.getContentHorizontalNavInsetRightPx(compat) ?: 0

    val horizontalInsetPx = maxOf(cutoutStartPx, navStartPx) + maxOf(cutoutEndPx, navEndPx)
    return (configuration.screenWidthDp.dp - horizontalInsetPx.pxToDp()).coerceAtLeast(0.dp)
}

@Composable
fun contentScreenWidthPx(): Int {
    val density = LocalDensity.current
    return with(density) { contentScreenWidth().roundToPx() }
}
