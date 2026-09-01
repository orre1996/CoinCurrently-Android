package com.cryptium.coincurrently.sharedui.modifiers
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import com.cryptium.coincurrently.R
import com.valentinilk.shimmer.ShimmerBounds
import com.valentinilk.shimmer.defaultShimmerTheme
import com.valentinilk.shimmer.rememberShimmer
import com.valentinilk.shimmer.shimmer
import com.cryptium.coincurrently.sharedui.theme.skeletonBackground

@Composable
fun Modifier.shimmeringModifier(active: Boolean) =
    if (active) {
        this
            .clip(RoundedCornerShape(4.dp))
            .shimmer(1500, true)
            .background(MaterialTheme.colors.skeletonBackground)
            .alpha(0f)
    } else {
        this
    }

@Composable
private fun createCustomTheme(duration: Int) = defaultShimmerTheme.copy(
    animationSpec = infiniteRepeatable(
        animation = tween(
            durationMillis = duration,
            delayMillis = 400,
            easing = LinearEasing,
        ),
        repeatMode = RepeatMode.Restart,
    ),
    rotation = 10f,
    shaderColors = listOf(
        Color.Unspecified.copy(alpha = 1f),
        Color.Unspecified.copy(alpha = 0.25f),
        Color.Unspecified.copy(alpha = 1f)
    ),
    shaderColorStops = null,
    shimmerWidth = 200.dp,
)

fun Modifier.shimmer(
    duration: Int,
    active: Boolean
): Modifier = composed {
    val shimmer = rememberShimmer(
        shimmerBounds = ShimmerBounds.View,
        theme = createCustomTheme(duration),
    )

    if (active) {
        padding(4.dp)
        shimmer(customShimmer = shimmer)
    } else {
        this
    }
}