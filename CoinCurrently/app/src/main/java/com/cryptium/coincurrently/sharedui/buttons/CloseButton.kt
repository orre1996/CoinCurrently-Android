package com.cryptium.coincurrently.sharedui.buttons
import com.cryptium.coincurrently.sharedui.modifiers.imageSize

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.cryptium.coincurrently.R
import com.cryptium.coincurrently.core.localization.Localization
import com.cryptium.coincurrently.sharedui.theme.onSurfaceDimmer
import com.cryptium.coincurrently.sharedui.theme.surfaceContainerHigh

@Composable
fun CloseButton(
    contentDescription: String = Localization.string("Close sheet"),
    onClick: () -> Unit,
) {
    Image(
        painter = painterResource(id = R.drawable.cross),
        contentDescription = contentDescription,
        contentScale = ContentScale.Crop,
        colorFilter = ColorFilter.tint(
            MaterialTheme.colors.onSurfaceDimmer
        ),
        modifier = Modifier
            .background(
                color = MaterialTheme.colors.surfaceContainerHigh,
                shape = CircleShape
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
            ) {
                onClick.invoke()
            }
            .padding(12.dp)
            .imageSize(24.dp)
    )
}