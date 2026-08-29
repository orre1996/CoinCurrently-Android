package com.cryptium.coincurrently.ui.main.PopupViews

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.Text
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cryptium.coincurrently.ui.main.Compose.PrimaryButton
import com.cryptium.coincurrently.ui.main.Compose.onSurfaceDim
import com.cryptium.coincurrently.ui.main.Compose.overlay
import com.cryptium.coincurrently.ui.main.Extensions.poppinsRegular
import com.cryptium.coincurrently.ui.main.Extensions.poppinsSemibold
import com.cryptium.coincurrently.ui.main.Compose.primaryBackgroundFullStroke
import com.cryptium.coincurrently.ui.main.Compose.primaryBackgroundNoRipple
import com.cryptium.coincurrently.ui.main.Compose.primaryTextColor
import com.cryptium.coincurrently.ui.main.Compose.surfaceContainerLow
import com.cryptium.coincurrently.ui.main.Extensions.satoshiBold
import com.cryptium.coincurrently.ui.main.Extensions.satoshiRegular

@Composable
fun ForceUpdateScreen(openPlayStore: () -> Unit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.overlay)
            .pointerInput(Unit) {
                detectTapGestures { }
            }
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier
                .padding(24.dp)
                .primaryBackgroundNoRipple(
                    backgroundColor = MaterialTheme.colors.surfaceContainerLow
                )
        ) {
            Text(
                text = "There is a new required version",
                style = satoshiBold(18),
                color = MaterialTheme.colors.onSurface,
                textAlign = TextAlign.Start,
                modifier = Modifier
                    .fillMaxWidth()
            )

            Text(
                text = "Head over to Google Play and download the latest version to continue using the app.",
                style = satoshiRegular(16),
                color = MaterialTheme.colors.onSurfaceDim,
                textAlign = TextAlign.Start,
                modifier = Modifier
                    .fillMaxWidth()
            )


            PrimaryButton(
                text = "Go to Google Play",
                modifier = Modifier
            ) {
                openPlayStore()
            }
        }
    }
}