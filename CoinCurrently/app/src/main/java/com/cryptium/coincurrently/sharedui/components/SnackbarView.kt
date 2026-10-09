package com.cryptium.coincurrently.sharedui.components
import com.cryptium.coincurrently.sharedui.modifiers.imageSize

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cryptium.coincurrently.R
import com.cryptium.coincurrently.sharedui.theme.surfaceContainerHighest
import com.cryptium.coincurrently.sharedui.theme.trendDown
import com.cryptium.coincurrently.sharedui.theme.trendUp
import com.cryptium.coincurrently.core.extensions.satoshiMedium
import kotlinx.coroutines.delay
enum class SnackBarType {
    SUCCESS, FAILURE
}

object SnackbarConstants {
    const val DISPLAY_DURATION_MS = 7_500L
    const val ANIMATION_DURATION_MS = 750
}

private val SnackbarSlideSpec = tween<Float>(
    durationMillis = SnackbarConstants.ANIMATION_DURATION_MS,
    easing = FastOutSlowInEasing,
)

@Composable
fun SnackbarView(
    showSnackBar: Boolean,
    message: String,
    buttonTitle: String,
    type: SnackBarType = SnackBarType.SUCCESS,
    isTabbarHidden: Boolean,
    navBarHeight: Dp,
    systemNavbarHeight: Dp,
    /** Increment when showing a new snackbar so a visible one re-slides from the bottom. */
    snackbarTrigger: Int = 0,
    onDismissRequest: () -> Unit = { },
    buttonAction: () -> Unit
) {
    val offsetY = remember { Animatable(200f) }

    val icon = if (type == SnackBarType.SUCCESS) R.drawable.check_circle else R.drawable.x_circle
    val iconColor = if (type == SnackBarType.SUCCESS) MaterialTheme.colors.trendUp else MaterialTheme.colors.trendDown

    val targetBottomInsetY = if (isTabbarHidden) {
        systemNavbarHeight
    } else {
        navBarHeight + systemNavbarHeight
    }
    val bottomInsetY by animateDpAsState(
        targetValue = targetBottomInsetY,
        animationSpec = tween(durationMillis = 300),
        label = "snackbarBottomInset",
    )

    var interactionLocked by remember { mutableStateOf(false) }
    val onDismiss by rememberUpdatedState(onDismissRequest)

    LaunchedEffect(showSnackBar, snackbarTrigger) {
        if (showSnackBar) {
            interactionLocked = false
            offsetY.snapTo(200f)
            offsetY.animateTo(0f, SnackbarSlideSpec)
            delay(SnackbarConstants.DISPLAY_DURATION_MS)
            onDismiss()
        } else {
            if (offsetY.value < 199f) {
                offsetY.animateTo(200f, SnackbarSlideSpec)
            }
        }
    }

    if (!showSnackBar && offsetY.value >= 199f) return

    fun requestDismiss() {
        if (interactionLocked) return
        interactionLocked = true
        onDismiss()
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .padding(
                start = 16.dp,
                end = 16.dp,
                top = 16.dp,
                bottom = 16.dp + bottomInsetY,
            )
            .offset(y = offsetY.value.dp)
            .clip(RoundedCornerShape(100))
            .background(MaterialTheme.colors.surfaceContainerHighest)
            .pointerInput(showSnackBar, snackbarTrigger) {
                detectVerticalDragGestures { _, _ ->
                    requestDismiss()
                }
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
            ) {
                requestDismiss()
            }
            .padding(16.dp)
    ) {
        Image(
            painter = painterResource(icon),
            contentDescription = null,
            colorFilter = ColorFilter.tint(iconColor),
            modifier = Modifier
                .imageSize(24.dp)
        )

        Text(
            text = message,
            color = MaterialTheme.colors.onSurface,
            style = satoshiMedium(16),
            modifier = Modifier
                .weight(1f, fill = false)
        )

        if (buttonTitle.isNotEmpty()) {
            Text(
                text = buttonTitle,
                style = satoshiMedium(16),
                color = MaterialTheme.colors.primary,
                modifier = Modifier
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                    ) {
                        if (!interactionLocked) {
                            interactionLocked = true
                            buttonAction()
                            onDismiss()
                        }
                    }
            )
        }
    }
}
