package com.cryptium.coincurrently.ui.main.Compose

import com.cryptium.coincurrently.ui.main.Compose.imageSize

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.cryptium.coincurrently.R

@Composable
fun ScrollToTopButton(
    onClick: () -> Unit,
    visible: Boolean,
    respectNavigationBars: Boolean = false,
) {
    val opacity by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween()
    )

    Box(
        modifier = Modifier
            .then(if (respectNavigationBars) Modifier.navigationBarsPadding() else Modifier)
            .padding(16.dp)
            .graphicsLayer { alpha = opacity }
            .clip(CircleShape)
            .background(MaterialTheme.colors.surfaceContainerHighest)
            .semantics {
                contentDescription = "Scroll to top"
            }
            .clickable(enabled = visible, onClick = onClick)
            .padding(16.dp)
    ) {
        Image(
            painter = painterResource(R.drawable.chevron_up),
            contentDescription = null,
            colorFilter = ColorFilter.tint(MaterialTheme.colors.onSurface),
            modifier = Modifier.imageSize(24.dp)
        )
    }
}