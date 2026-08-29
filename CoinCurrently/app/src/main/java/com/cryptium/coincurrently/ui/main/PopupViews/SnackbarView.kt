package com.cryptium.coincurrently.ui.main.PopupViews

import com.cryptium.coincurrently.ui.main.Compose.imageSize

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import com.cryptium.coincurrently.ui.main.Compose.surfaceContainerHighest
import com.cryptium.coincurrently.ui.main.Compose.trendDown
import com.cryptium.coincurrently.ui.main.Compose.trendUp
import com.cryptium.coincurrently.ui.main.Extensions.satoshiMedium
import kotlinx.coroutines.delay
import kotlin.math.max

enum class SnackBarType {
    SUCCESS, FAILURE
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
    achievementSnackbarIsVisible: Boolean = false,
    achievementSnackbarHeight: Dp = 0.dp,
    /** Increment when showing a new snackbar so a visible one re-slides from the bottom. */
    snackbarTrigger: Int = 0,
    onDismissRequest: () -> Unit = { },
    buttonAction: () -> Unit
) {
    val offsetY = remember { Animatable(200f) }

    val icon = if (type == SnackBarType.SUCCESS) R.drawable.check_circle else R.drawable.x_circle
    val iconColor = if (type == SnackBarType.SUCCESS) MaterialTheme.colors.trendUp else MaterialTheme.colors.trendDown

    val stackedOffset = if (achievementSnackbarIsVisible) {
        max(achievementSnackbarHeight.value, 72f).dp + 12.dp
    } else {
        0.dp
    }
    val animatedStackedOffset by animateDpAsState(
        targetValue = stackedOffset,
        animationSpec = tween(durationMillis = SnackbarConstants.ANIMATION_DURATION_MS),
        label = "snackbarStackedOffset",
    )

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

    LaunchedEffect(showSnackBar, snackbarTrigger) {
        if (showSnackBar) {
            interactionLocked = false
            offsetY.snapTo(200f)
            offsetY.animateTo(0f, SnackbarSlideSpec)
            delay(SnackbarConstants.DISPLAY_DURATION_MS)
            onDismissRequest()
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
        onDismissRequest()
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .padding(16.dp)
            .offset(y = offsetY.value.dp - bottomInsetY - animatedStackedOffset)
            .clip(RoundedCornerShape(100))
            .background(MaterialTheme.colors.surfaceContainerHighest)
            .padding(16.dp)
            .pointerInput(Unit) {
                detectVerticalDragGestures { _, _ ->
                    requestDismiss()
                }
            }
            .pointerInput(Unit) {
                detectTapGestures {
                    requestDismiss()
                }
            }
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
                            onDismissRequest()
                        }
                    }
            )
        }
    }
}
