package com.cryptium.coincurrently.ui.main.General.Components

import com.cryptium.coincurrently.ui.main.Compose.imageSize

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cryptium.coincurrently.ui.main.Compose.CachedUrlImage
import com.cryptium.coincurrently.R
import com.cryptium.coincurrently.ui.main.Compose.onSurfaceDimmer
import com.cryptium.coincurrently.ui.main.Compose.shimmeringModifier
import com.cryptium.coincurrently.ui.main.Compose.surfaceContainer
import com.cryptium.coincurrently.ui.main.Extensions.satoshiBold

sealed class ImageCase {
    object Nothing : ImageCase()
    data class Image(val value: String?) : ImageCase()
    data class Fixed(val image: Int): ImageCase()
    data class Text(val text: String): ImageCase()
}

@Composable
fun SingleSelectView(
    title: String,
    subtitle: String?,
    imageCase: ImageCase,
    isSelected: Boolean,
    canNavigate: Boolean = false,
    accessibilityTitle: String? = null,
    didSelectOption: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accessibilityLabel = accessibilityTitle
        ?: if (!subtitle.isNullOrEmpty()) "$title. $subtitle" else title

    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .background(
                color = if (isSelected) MaterialTheme.colors.surfaceContainer else Color.Transparent,
                shape = RoundedCornerShape(size = 16.dp)
            )
            .padding(vertical = 8.dp)
            .semantics(mergeDescendants = true) {
                contentDescription = accessibilityLabel
                role = Role.Button
                selected = isSelected
                onClick {
                    didSelectOption.invoke()
                    true
                }
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
            ) {
                didSelectOption.invoke()
            }
    ) {
        Image(
            painter = painterResource(id = R.drawable.check_circle),
            contentDescription = null,
            colorFilter = ColorFilter.tint(MaterialTheme.colors.primary),
            modifier = Modifier
                .padding(12.dp)
                .imageSize(24.dp)
                .alpha(if (isSelected) 1f else 0f)
        )

        when (imageCase) {
            is ImageCase.Nothing -> {}
            is ImageCase.Image -> {
                CachedUrlImage(
                    model = imageCase.value,
                    size = 32.dp,
                    modifier = Modifier.padding(end = 4.dp),
                )
            }
            else -> {}
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = if(canNavigate) 0.dp else 12.dp)
        ) {
            Text(
                text = title,
                style = satoshiBold(16),
                color = MaterialTheme.colors.onSurface,
                textAlign = TextAlign.Start
            )

            subtitle?.let {
                Text(
                    text = it,
                    style = satoshiBold(12),
                    color = MaterialTheme.colors.onSurfaceDimmer,
                    textAlign = TextAlign.Start
                )
            }
        }

        if (canNavigate) {
            Image(
                painter = painterResource(id = R.drawable.chevron_right),
                contentDescription = null,
                colorFilter = ColorFilter.tint(MaterialTheme.colors.onSurfaceDimmer),
                modifier = Modifier
                    .padding(12.dp)
                    .imageSize(24.dp)
            )
        }
    }
}