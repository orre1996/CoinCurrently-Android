package com.cryptium.coincurrently.features.Achievements.Views
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import com.cryptium.coincurrently.sharedui.theme.onSurfaceDim
import com.cryptium.coincurrently.sharedui.theme.surfaceContainer
import com.cryptium.coincurrently.sharedui.theme.surfaceContainerHigh
import com.cryptium.coincurrently.core.extensions.satoshiBold
import com.cryptium.coincurrently.core.extensions.satoshiMedium
import com.cryptium.coincurrently.sharedui.components.CustomProgressBarView
import com.cryptium.coincurrently.core.localization.Localization
import com.cryptium.coincurrently.sharedui.components.SnackbarConstants
import kotlinx.coroutines.delay

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
    navBarHeight: Dp,
    systemNavbarHeight: Dp,
    onVisibilityChanged: (Boolean) -> Unit,
    onHeightChanged: (Float) -> Unit,
    onDismissComplete: () -> Unit,
) {
    val offsetY = remember { Animatable(200f) }
    val animatedProgress = remember { Animatable(0f) }
    val density = LocalDensity.current
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

        delay(SnackbarConstants.DISPLAY_DURATION_MS)

        if (isSnackbarVisible) {
            dismissAnimated()
        }
    }

    if (!isSnackbarVisible && offsetY.value >= 199f) return

    val accessibilityLabel = Localization.format(
        "Achievement unlocked, %@, %@",
        Localization.string(achievementTitle),
        Localization.string(levelName),
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .padding(
                start = 16.dp,
                end = 16.dp,
                top = 16.dp,
                bottom = 16.dp + bottomInsetY,
            )
            .offset(y = offsetY.value.dp)
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
            .pointerInput(snackbarTrigger) {
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
                    text = Localization.string(achievementTitle),
                    color = MaterialTheme.colors.onSurface,
                    style = satoshiBold(15),
                    maxLines = 1,
                )

                Text(
                    text = Localization.string(levelName),
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
