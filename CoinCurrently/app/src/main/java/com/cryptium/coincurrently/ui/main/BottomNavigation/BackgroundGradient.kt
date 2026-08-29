package com.cryptium.coincurrently.ui.main.BottomNavigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun BackgroundGradient() {
    Box(
        modifier = Modifier
            .background(MaterialTheme.colors.surface)
            .fillMaxSize()
    )
}