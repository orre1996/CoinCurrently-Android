package com.cryptium.coincurrently.ui.main.Activities

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import com.cryptium.coincurrently.ui.main.PopupViews.LoadingScreen

@Composable
fun FragmentLoadingScreen(
    isLoggedIn: Boolean,
    isLocked: Boolean,
    needsToUpdate: Boolean,
    onAuthenticate: () -> Unit
) {
    Box {
        LoadingScreen(
            isLoggedIn = isLoggedIn,
            isLocked = isLocked,
            authenticate = onAuthenticate
        )
    }
}
