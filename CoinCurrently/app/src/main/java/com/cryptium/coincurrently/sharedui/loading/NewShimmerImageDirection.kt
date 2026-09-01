package com.cryptium.coincurrently.sharedui.loading
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cryptium.coincurrently.R
import com.cryptium.coincurrently.sharedui.modifiers.scaledImageSize
import com.cryptium.coincurrently.sharedui.theme.onSurfaceContainerDim
import com.cryptium.coincurrently.sharedui.modifiers.shimmeringModifier
import com.cryptium.coincurrently.core.extensions.changeColor
import com.cryptium.coincurrently.core.extensions.changeImage

enum class ChangeSize(val triangleSize: Dp, val fontSize: Int) {
    EXTRA_SMALL(14.dp, 12),
    SMALL(14.dp, 14),
    MEDIUM(16.dp, 16),
    LARGE(20.dp, 20);
}

@Composable
fun NewShimmerImageDirection(
    change: Double?,
    size: ChangeSize = ChangeSize.LARGE,
    isDownloading: Boolean
) {
    val scaledTriangleSize = scaledImageSize(size.triangleSize)

    Image(
        painter = change?.changeImage() ?: painterResource(R.drawable.trend_still),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        colorFilter = ColorFilter.tint(change?.changeColor() ?: MaterialTheme.colors.onSurfaceContainerDim),
        modifier = Modifier
            .size(scaledTriangleSize)
            .shimmeringModifier(isDownloading)
    )
}