package com.cryptium.coincurrently.app
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import com.cryptium.coincurrently.sharedui.loading.LoadingScreen

@Composable
fun RootLoadingScreen(
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
