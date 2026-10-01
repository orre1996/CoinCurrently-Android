package com.cryptium.coincurrently.sharedui.buttons
import com.cryptium.coincurrently.core.localization.Localization
import com.cryptium.coincurrently.sharedui.modifiers.imageSize

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.cryptium.coincurrently.R
import com.cryptium.coincurrently.core.extensions.satoshiBold
import com.cryptium.coincurrently.sharedui.loading.ActivityIndicator
import com.cryptium.coincurrently.sharedui.modifiers.CappedFontScale
import com.cryptium.coincurrently.sharedui.modifiers.DynamicTypeSizeCap
import com.cryptium.coincurrently.sharedui.modifiers.MinimumTextSize
import com.cryptium.coincurrently.sharedui.theme.conditional
import com.cryptium.coincurrently.sharedui.theme.onSurfaceDimmer
import com.cryptium.coincurrently.sharedui.theme.surfaceContainerHigh

@Composable
fun SecondaryButton(
    text: String,
    icon: Int? = null,
    accessibilityLabel: String? = null,
    isEnabled: Boolean = true,
    isLoading: Boolean = false,
    fullWidth: Boolean = false,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    CappedFontScale(DynamicTypeSizeCap.Accessibility2) {
    MinimumTextSize(
        text = text,
        font = satoshiBold(18),
        modifier = modifier
            .semantics(mergeDescendants = true) {
                contentDescription = accessibilityLabel ?: text
                role = Role.Button
            }
            .alpha(if(isEnabled) 1f else 0.4f)
            .conditional(fullWidth) {
                fillMaxWidth()
            }
            .clip(CircleShape)
            .clickable {
                if (!isLoading && isEnabled) {
                    onClick.invoke()
                }
            }
            .background(MaterialTheme.colors.surfaceContainerHigh)
            .padding(
                top = 12.dp,
                start = 16.dp,
                bottom = 12.dp,
                end = if(icon == null) 16.dp else 24.dp
            )
    ) {
        if (isLoading) {
            ActivityIndicator(
                size = 22.dp,
                fill = false,
                color = MaterialTheme.colors.onSurface,
                modifier = Modifier.semantics {
                    contentDescription = Localization.string("Loading")
                },
            )
        } else {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                icon?.let {
                    Image(
                        painter = painterResource(id = icon),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        colorFilter = ColorFilter.tint(MaterialTheme.colors.onSurfaceDimmer),
                        modifier = Modifier
                            .imageSize(20.dp)
                    )
                }

                Text(
                    text = text,
                    style = satoshiBold(18),
                    color = MaterialTheme.colors.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.clearAndSetSemantics {},
                )
            }
        }
    }
    }
}