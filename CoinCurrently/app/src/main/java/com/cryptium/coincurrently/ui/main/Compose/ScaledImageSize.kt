package com.cryptium.coincurrently.ui.main.Compose

import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import kotlin.math.min

/** Max scale for images/icons, matching iOS `min(scale, 1.5)`. */
const val MAX_IMAGE_FONT_SCALE = 1.5f

@Composable
fun cappedImageFontScale(): Float =
    min(LocalDensity.current.fontScale, MAX_IMAGE_FONT_SCALE)

/** Scales a fixed image dimension with accessibility font size (capped at [MAX_IMAGE_FONT_SCALE]). */
@Composable
fun scaledImageSize(size: Dp): Dp = size * cappedImageFontScale()

@Composable
fun scaledImageSize(width: Dp, height: Dp): Pair<Dp, Dp> =
    scaledImageSize(width) to scaledImageSize(height)

@Composable
fun Modifier.imageSize(size: Dp): Modifier = this.size(scaledImageSize(size))

@Composable
fun Modifier.imageSize(width: Dp, height: Dp): Modifier {
    val (scaledWidth, scaledHeight) = scaledImageSize(width, height)
    return this.size(width = scaledWidth, height = scaledHeight)
}

@Composable
fun Modifier.requiredImageSize(size: Dp): Modifier = requiredSize(scaledImageSize(size))

@Composable
fun Modifier.requiredImageSize(width: Dp, height: Dp): Modifier {
    val (scaledWidth, scaledHeight) = scaledImageSize(width, height)
    return requiredSize(width = scaledWidth, height = scaledHeight)
}
