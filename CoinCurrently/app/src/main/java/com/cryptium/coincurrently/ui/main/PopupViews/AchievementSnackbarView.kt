package com.cryptium.coincurrently.ui.main.PopupViews

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import kotlinx.coroutines.launch
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cryptium.coincurrently.R
import com.cryptium.coincurrently.ui.main.Compose.onSurfaceDim
import com.cryptium.coincurrently.ui.main.Compose.surfaceContainer
import com.cryptium.coincurrently.ui.main.Compose.surfaceContainerHigh
import com.cryptium.coincurrently.ui.main.Extensions.satoshiBold
import com.cryptium.coincurrently.ui.main.Extensions.satoshiMedium
import com.cryptium.coincurrently.ui.main.General.Components.CustomProgressBarView
import kotlinx.coroutines.delay

object SnackbarConstants {
    const val DISPLAY_DURATION_MS = 7_500L
    const val ANIMATION_DURATION_MS = 750
}

private val AchievementSnackbarSlideSpec = tween<Float>(
    durationMillis = SnackbarConstants.ANIMATION_DURATION_MS,
    easing = FastOutSlowInEasing,
)

@Composable
fun AchievementSnackbarView(
    snackbarTrigger: Int,
    achievementTitle: String,
    levelName: String,
    isTabbarHidden: Boolean,
    standardSnackbarIsVisible: Boolean,
    navBarHeight: Dp,
    systemNavbarHeight: Dp,
    onVisibilityChanged: (Boolean) -> Unit,
    onHeightChanged: (Float) -> Unit,
    onDismissComplete: () -> Unit,
) {
    val offsetY = remember { Animatable(200f) }
    val animatedProgress = remember { Animatable(0f) }
    val density = LocalDensity.current
    val standardSnackbarVisible by rememberUpdatedState(standardSnackbarIsVisible)
    val scope = rememberCoroutineScope()

    val targetBottomInsetY = if (isTabbarHidden) {
        systemNavbarHeight
    } else {
        navBarHeight + systemNavbarHeight
    }
    val bottomInsetY by animateDpAsState(
        targetValue = targetBottomInsetY,
        animationSpec = tween(durationMillis = SnackbarConstants.ANIMATION_DURATION_MS),
        label = "achievementSnackbarBottomInset",
    )

    var interactionLocked by remember { mutableStateOf(false) }
    var isSnackbarVisible by remember { mutableStateOf(false) }
    var previousStandardSnackbarVisible by remember { mutableStateOf(standardSnackbarIsVisible) }

    suspend fun dismissAnimated() {
        if (!isSnackbarVisible) return

        interactionLocked = true
        onVisibilityChanged(false)
        offsetY.animateTo(200f, AchievementSnackbarSlideSpec)
        animatedProgress.snapTo(0f)
        isSnackbarVisible = false
        onHeightChanged(0f)
        interactionLocked = false
        onDismissComplete()
    }

    fun requestDismiss() {
        if (interactionLocked || !isSnackbarVisible) return
        scope.launch { dismissAnimated() }
    }

    LaunchedEffect(snackbarTrigger) {
        if (snackbarTrigger <= 0) return@LaunchedEffect

        interactionLocked = false
        animatedProgress.snapTo(0f)
        isSnackbarVisible = true
        onVisibilityChanged(true)
        offsetY.snapTo(200f)
        offsetY.animateTo(0f, AchievementSnackbarSlideSpec)

        delay(850)
        animatedProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing),
        )

        var elapsed = 0L
        while (elapsed < SnackbarConstants.DISPLAY_DURATION_MS && isSnackbarVisible) {
            if (standardSnackbarVisible) {
                while (standardSnackbarVisible && isSnackbarVisible) {
                    delay(100)
                }
                return@LaunchedEffect
            }
            delay(100)
            elapsed += 100
        }

        if (isSnackbarVisible && !standardSnackbarVisible) {
            dismissAnimated()
        }
    }

    LaunchedEffect(standardSnackbarIsVisible) {
        if (!isSnackbarVisible) {
            previousStandardSnackbarVisible = standardSnackbarIsVisible
            return@LaunchedEffect
        }

        if (previousStandardSnackbarVisible && !standardSnackbarIsVisible) {
            dismissAnimated()
        }

        previousStandardSnackbarVisible = standardSnackbarIsVisible
    }

    if (!isSnackbarVisible && offsetY.value >= 199f) return

    val accessibilityLabel = "Achievement unlocked, $achievementTitle, $levelName"

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .padding(16.dp)
            .offset(y = offsetY.value.dp - bottomInsetY)
            .fillMaxWidth()
            .onSizeChanged { size ->
                onHeightChanged(with(density) { size.height.toDp().value })
            }
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colors.surfaceContainer)
            .border(
                width = 1.dp,
                color = MaterialTheme.colors.primary.copy(alpha = 0.25f),
                shape = RoundedCornerShape(16.dp),
            )
            .padding(horizontal = 16.dp, vertical = 14.dp)
            .semantics { contentDescription = accessibilityLabel }
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
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(36.dp)
                .background(
                    color = MaterialTheme.colors.primary.copy(alpha = 0.15f),
                    shape = CircleShape,
                )
        ) {
            Image(
                painter = painterResource(R.drawable.check),
                contentDescription = null,
                colorFilter = ColorFilter.tint(MaterialTheme.colors.primary),
                modifier = Modifier.size(16.dp),
            )
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = achievementTitle,
                    color = MaterialTheme.colors.onSurface,
                    style = satoshiBold(15),
                    maxLines = 1,
                )

                Text(
                    text = levelName,
                    color = MaterialTheme.colors.onSurfaceDim,
                    style = satoshiMedium(13),
                    maxLines = 1,
                )
            }

            CustomProgressBarView(
                progress = animatedProgress.value.toDouble(),
                foregroundColor = MaterialTheme.colors.primary,
                backgroundColor = MaterialTheme.colors.surfaceContainerHigh,
                height = 5.dp,
            )
        }
    }
}
