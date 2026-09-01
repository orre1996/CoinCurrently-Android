package com.cryptium.coincurrently.features.Settings.Views
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cryptium.coincurrently.sharedui.components.AccessibilityLayout
import com.cryptium.coincurrently.sharedui.components.AdaptiveStack
import com.cryptium.coincurrently.sharedui.buttons.PrimaryButton
import com.cryptium.coincurrently.sharedui.buttons.SecondaryButton
import com.cryptium.coincurrently.sharedui.theme.onSurfaceDim
import com.cryptium.coincurrently.sharedui.theme.overlay
import com.cryptium.coincurrently.sharedui.theme.primaryBackgroundNoRipple
import com.cryptium.coincurrently.core.extensions.satoshiBold
import com.cryptium.coincurrently.core.extensions.satoshiRegular

@Composable
fun ResetAppView(
    closeView: () -> Unit,
    resetApp: () -> Unit,
    bottomBarInset: Dp = 0.dp,
) {
    var isLoading by remember { mutableStateOf(false) }

    BoxWithConstraints(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = bottomBarInset)
            .background(MaterialTheme.colors.overlay)
            .pointerInput(Unit) {
                detectTapGestures { }
            }
    ) {
        val maxDialogHeight = maxHeight - 48.dp
        val isHorizontal = AccessibilityLayout.isHorizontal

        Column(
            horizontalAlignment = Alignment.Start,
            modifier = Modifier
                .padding(24.dp)
                .widthIn(max = 500.dp)
                .heightIn(max = maxDialogHeight)
                .primaryBackgroundNoRipple(
                    paddingValues = PaddingValues(0.dp),
                    cornerRadius = 16.dp
                )
        ) {
            Column(
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(24.dp),
                modifier = Modifier
                    .weight(1f, fill = false)
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Reset App?",
                    style = satoshiBold(22),
                    color = MaterialTheme.colors.onSurface,
                    textAlign = TextAlign.Start,
                    modifier = Modifier
                        .padding(top = 24.dp)
                        .semantics { heading() }
                )

                Text(
                    text = "This will delete all app settings and saved data on this device. This action cannot be undone.",
                    style = satoshiRegular(16),
                    color = MaterialTheme.colors.onSurfaceDim,
                    textAlign = TextAlign.Start
                )
            }

            AdaptiveStack(
                isHorizontal = isHorizontal,
                spacing = 12.dp,
                horizontalAlignment = Alignment.Start,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        24.dp
                    )
            ) {
                if (!isLoading) {
                    SecondaryButton(
                        text = "Cancel",
                        fullWidth = true,
                        modifier = sectionModifier()
                    ) {
                        closeView()
                    }
                }

                PrimaryButton(
                    text = "Reset App",
                    isLoading = isLoading,
                    fullWidth = true,
                    modifier = sectionModifier()
                ) {
                    isLoading = true
                    resetApp()
                }
            }
        }
    }
}