package com.cryptium.coincurrently.ui.main.PortfolioTab.Popups

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.dialog
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.cryptium.coincurrently.ui.main.Compose.AccessibilityLayout
import com.cryptium.coincurrently.ui.main.Compose.AdaptiveStack
import com.cryptium.coincurrently.ui.main.Compose.PrimaryButton
import com.cryptium.coincurrently.ui.main.Compose.SecondaryButton
import com.cryptium.coincurrently.ui.main.Compose.onSurfaceDim
import com.cryptium.coincurrently.ui.main.Compose.overlayColor
import com.cryptium.coincurrently.ui.main.Compose.primaryBackgroundNoRipple
import com.cryptium.coincurrently.ui.main.Compose.surfaceContainerLow
import com.cryptium.coincurrently.ui.main.Extensions.satoshiBold
import com.cryptium.coincurrently.ui.main.Extensions.satoshiRegular

@Composable
fun DeleteWalletPopup(
    walletName: String,
    onClose: (Boolean) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.overlayColor)
            .semantics {
                dialog()
                paneTitle = "Remove Wallet"
            }
            .pointerInput(Unit) {
                detectTapGestures { }
            }
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.Start,
            modifier = Modifier
                .align(Alignment.Center)
                .padding(24.dp)
                .primaryBackgroundNoRipple(
                    paddingValues = PaddingValues(24.dp),
                    backgroundColor = MaterialTheme.colors.surfaceContainerLow,
                    cornerRadius = 16.dp
                )
        ) {
            Header()
            BodyText(walletName)
            ActionButtons(onClose)
        }
    }
}

@Composable
private fun Header() {
    Text(
        text = "Remove Wallet",
        color = MaterialTheme.colors.onSurface,
        style = satoshiBold(18)
    )
}

@Composable
private fun BodyText(walletName: String) {
    Text(
        text = "Remove “$walletName” from your portfolio?\nYou can re-import it at any time.",
        color = MaterialTheme.colors.onSurfaceDim,
        style = satoshiRegular(16)
    )
}

@Composable
private fun ActionButtons(
    onClose: (Boolean) -> Unit
) {
    val isHorizontal = AccessibilityLayout.isHorizontal

    AdaptiveStack(
        isHorizontal = isHorizontal,
        spacing = 12.dp,
        horizontalAlignment = Alignment.Start,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
    ) {
        SecondaryButton(
            text = "Cancel",
            fullWidth = true,
            onClick = {
                onClose(false)
            },
            modifier = sectionModifier()
                .widthIn(max = 400.dp)
        )

        PrimaryButton(
            text = "Remove",
            fullWidth = true,
            onClick = {
                onClose(true)
            },
            modifier = sectionModifier()
                .widthIn(max = 400.dp)
        )
    }
}
