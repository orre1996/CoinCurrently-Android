package com.cryptium.coincurrently.ui.main.SettingsTab.Compose

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.size
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.cryptium.coincurrently.ui.main.Compose.onSurfaceDimmer
import com.cryptium.coincurrently.ui.main.Compose.scaledImageSize
import com.cryptium.coincurrently.ui.main.Compose.surfaceContainerLowest
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

@Composable
fun CustomSwitch(
    isOn: Boolean,
    toggled: () -> Unit
) {
    var switchON by remember { mutableStateOf(isOn) }
    var canChangeValue by remember { mutableStateOf(false) }

    LaunchedEffect(isOn) {
        if(canChangeValue) {
            switchON = !switchON
        }
    }

    LaunchedEffect(Unit) {
        delay(0.5.seconds)
        canChangeValue = true
    }

    val trackWidth = scaledImageSize(54.dp)
    val trackHeight = scaledImageSize(32.dp)
    val thumbSize = scaledImageSize(24.dp)
    val thumbPadding = scaledImageSize(4.dp)

    val density = LocalDensity.current
    val offPositionPx = with(density) { thumbPadding.toPx() }
    val onPositionPx = with(density) { (trackWidth - thumbPadding - thumbSize).toPx() }

    val animatePosition = animateFloatAsState(
        targetValue = if (switchON) onPositionPx else offPositionPx,
        tween(350)
    )

    val backgroundColor = if(switchON) MaterialTheme.colors.primary else MaterialTheme.colors.surfaceContainerLowest
    val thumbColor = if(switchON) MaterialTheme.colors.onPrimary else MaterialTheme.colors.onSurfaceDimmer

    val color by animateColorAsState(
        backgroundColor,
        tween(350), label = ""
    )

    Canvas(
        modifier = Modifier
            .size(width = trackWidth, height = trackHeight)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        switchON = !switchON
                        toggled()
                    }
                )
            }
    ) {
        val thumbSizePx = thumbSize.toPx()
        val thumbCornerRadius = thumbSizePx / 2f
        val thumbY = (size.height - thumbSizePx) / 2f

        // Track
        drawRoundRect(
            color = color,
            cornerRadius = CornerRadius(x = trackHeight.toPx() / 2f, y = trackHeight.toPx() / 2f)
        )

        // Thumb
        drawRoundRect(
            color = thumbColor,
            cornerRadius = CornerRadius(x = thumbCornerRadius, y = thumbCornerRadius),
            size = Size(thumbSizePx, thumbSizePx),
            topLeft = Offset(animatePosition.value, thumbY)
        )
    }
}