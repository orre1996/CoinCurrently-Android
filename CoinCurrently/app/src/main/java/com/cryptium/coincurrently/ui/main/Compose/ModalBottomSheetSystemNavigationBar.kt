package com.cryptium.coincurrently.ui.main.Compose

import android.os.Build
import android.view.View
import android.view.ViewParent
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.window.DialogWindowProvider
import androidx.core.view.WindowCompat
import com.cryptium.coincurrently.ui.main.Extensions.getActivity

/**
 * Modal bottom sheets run in a separate [android.view.Window]. That window often defaults to a
 * light navigation bar (and Android 10+ may enforce extra contrast unless disabled).
 * This aligns the system navigation bar with the sheet surface.
 */
@Composable
fun ModalBottomSheetSystemNavigationBar(
    containerColor: Color = MaterialTheme.colors.surfaceContainerLow,
) {
    val view = LocalView.current
    DisposableEffect(containerColor) {
        val window = findDialogWindow(view) ?: view.context.getActivity()?.window
        if (window == null) {
            onDispose { }
        } else {
            val previousNavColor = window.navigationBarColor
            val controller = WindowCompat.getInsetsController(window, window.decorView)
            val previousLightNav = controller.isAppearanceLightNavigationBars
            val previousContrastEnforced = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                window.isNavigationBarContrastEnforced
            } else {
                false
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                window.isNavigationBarContrastEnforced = false
            }
            window.navigationBarColor = containerColor.toArgb()
            controller.isAppearanceLightNavigationBars = containerColor.luminance() > 0.5f

            onDispose {
                window.navigationBarColor = previousNavColor
                controller.isAppearanceLightNavigationBars = previousLightNav
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    window.isNavigationBarContrastEnforced = previousContrastEnforced
                }
            }
        }
    }
}

private fun findDialogWindow(view: View): android.view.Window? {
    var parent: ViewParent? = view.parent
    while (parent != null) {
        if (parent is DialogWindowProvider) {
            return parent.window
        }
        parent = parent.parent
    }
    return null
}
