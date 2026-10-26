package com.cryptium.coincurrently.sharedui.components
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.cryptium.coincurrently.sharedui.modifiers.scaledImageSize
import com.cryptium.coincurrently.sharedui.modifiers.shimmeringModifier

/**
 * Coil image with loading + skeleton shimmer semantics used across the app. Non-blank [String]
 * models use an [ImageRequest] with memory, disk, and HTTP cache policies enabled. Offline
 * behaviour also depends on [com.cryptium.coincurrently.CoinCurrentlyApplication] (Coil singleton
 * ignores restrictive cache headers and avoids offline network short-circuit).
 *
 * @param respectMissingContentShimmer When false, overlay shimmer for null/blank content is only
 *   driven by [isDownloading] — use when the row’s loading flag differs from “image URL missing”.
 * @param scaleWithFontSize When false, [size] is used as-is.
 */
@Composable
fun CachedUrlImage(
    model: Any?,
    size: Dp,
    isDownloading: Boolean = false,
    respectMissingContentShimmer: Boolean = true,
    scaleWithFontSize: Boolean = true,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit,
    contentDescription: String? = null,
) {
    val appContext = LocalContext.current.applicationContext
    val missingModel = when (model) {
        null -> true
        is String -> model.isBlank()
        else -> false
    }
    val showPlaceholderShimmer =
        (respectMissingContentShimmer && missingModel) || isDownloading
    val scaledSize = if (scaleWithFontSize) scaledImageSize(size) else size
    val cornerRadius = 8.dp
    val shape = RoundedCornerShape(cornerRadius)

    val coilModel = remember(model) {
        when (model) {
            null -> null
            is String ->
                if (model.isBlank()) null
                else ImageRequest.Builder(appContext)
                    .data(model)
                    .memoryCachePolicy(CachePolicy.ENABLED)
                    .diskCachePolicy(CachePolicy.ENABLED)
                    .networkCachePolicy(CachePolicy.ENABLED)
                    .build()
            else -> model
        }
    }

    SubcomposeAsyncImage(
        model = coilModel,
        contentDescription = contentDescription,
        contentScale = contentScale,
        loading = {
            Box(
                modifier = Modifier
                    .size(scaledSize)
                    .clip(shape)
                    .shimmeringModifier(true)
            )
        },
        modifier = Modifier
            .size(scaledSize)
            .clip(shape)
            .shimmeringModifier(showPlaceholderShimmer)
            .then(modifier)
    )
}
