package com.cryptium.coincurrently.ui.main.Compose

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun Modifier.primaryBackground(
    paddingValues: PaddingValues = PaddingValues(16.dp),
    fillWidth: Boolean = true,
    backgroundColor: Color =  MaterialTheme.colors.surfaceContainerLow,
    outlineColor: Color = Color.Transparent,
    cornerRadius: Dp = 12.dp,
    lineWidth: Dp = 1.dp,
    callback: () -> Unit
) =
    this
        .conditional(fillWidth) {
            fillMaxWidth()
        }
        .clip(RoundedCornerShape(cornerRadius))
        .clickable {
            callback()
        }
        .background(
            color = backgroundColor,
            shape = RoundedCornerShape(size = cornerRadius)
        )
        .border(
            width = lineWidth,
            color = outlineColor,
            shape = RoundedCornerShape(size = cornerRadius)
        )
        .padding(paddingValues)

@Composable
fun Modifier.primaryBackgroundNoRipple(
    paddingValues: PaddingValues = PaddingValues(16.dp),
    backgroundColor: Color =  MaterialTheme.colors.surfaceContainerLow,
    fillWidth: Boolean = true,
    cornerRadius: Dp = 8.dp,
): Modifier {
    val talkBackEnabled = AccessibilityLayout.isTalkBackEnabled
    return this
        .conditional(fillWidth) {
            fillMaxWidth()
        }
        .clip(RoundedCornerShape(cornerRadius))
        .background(
            color = backgroundColor,
            shape = RoundedCornerShape(size = cornerRadius)
        )
        .padding(paddingValues)
        .then(
            if (!talkBackEnabled) {
                Modifier.pointerInput(Unit) {
                    detectTapGestures(
                        onTap = {}
                    )
                }
            } else {
                Modifier
            }
        )
}

@Composable
fun Modifier.primaryBackgroundFullStroke(
    padding: PaddingValues = PaddingValues(16.dp),
    fromCandleChart: Boolean = false
) =
    this
        .clip(RoundedCornerShape(if (fromCandleChart) 0.dp else 8.dp))
        .background(
            color = if (fromCandleChart) MaterialTheme.colors.cardBackground else MaterialTheme.colors.surfaceContainerLow,
            shape = RoundedCornerShape(size = if (fromCandleChart) 0.dp else 8.dp)
        )
        .padding(padding)

@Composable
fun Modifier.conditional(condition : Boolean, modifier: @Composable Modifier.() -> Modifier) : Modifier {
    return if (condition) {
        then(modifier(Modifier))
    } else {
        this
    }
}