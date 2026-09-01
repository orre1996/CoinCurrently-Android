package com.cryptium.coincurrently.features.Portfolio.Views
import com.cryptium.coincurrently.sharedui.modifiers.imageSize

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Checkbox
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.invisibleToUser
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.cryptium.coincurrently.R
import com.cryptium.coincurrently.sharedui.theme.onSurfaceDim
import com.cryptium.coincurrently.sharedui.theme.onSurfaceDimmer
import com.cryptium.coincurrently.sharedui.theme.surfaceContainer
import com.cryptium.coincurrently.sharedui.theme.surfaceContainerHighest
import com.cryptium.coincurrently.sharedui.components.CheckboxView
import com.cryptium.coincurrently.core.extensions.satoshiBold

@Composable
fun TransactionTypeView(
    title: String,
    subtitle: String,
    image: Int,
    isMultiSelectEnabled: Boolean = false,
    isSelected: Boolean = false,
    modifier: Modifier = Modifier,
    viewTapped: () -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .padding(vertical = 16.dp)
            .semantics(mergeDescendants = true) {
                val selectionState = if (isMultiSelectEnabled && isSelected) "selected" else ""
                contentDescription = buildString {
                    append(title)
                    if (subtitle.isNotEmpty()) append(". $subtitle")
                    if (selectionState.isNotEmpty()) append(". $selectionState")
                }
                onClick(label = "Select $title", action = {
                    viewTapped.invoke()
                    true
                })
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
            ) {
                viewTapped.invoke()
            }
    ) {
        Image(
            painter = painterResource(image),
            contentDescription = null,
            colorFilter = ColorFilter.tint(MaterialTheme.colors.onSurface),
            modifier = Modifier
                .clip(CircleShape)
                .background(MaterialTheme.colors.surfaceContainer)
                .padding(8.dp)
                .imageSize(20.dp)
                .semantics { invisibleToUser() }
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .semantics { invisibleToUser() }
        ) {
            Text(
                text = title,
                style = satoshiBold(18),
                color = MaterialTheme.colors.onSurface
            )

            Text(
                text = subtitle,
                style = satoshiBold(12),
                color = MaterialTheme.colors.onSurfaceDimmer
            )
        }

        if(isMultiSelectEnabled) {
            CheckboxView(
                trailingText = "",
                isChecked = isSelected,
                modifier = Modifier.semantics { invisibleToUser() },
                isInteractive = false,
                onToggle = { }
            )
        } else {
            Image(
                painter = painterResource(R.drawable.chevron_right),
                contentDescription = null,
                colorFilter = ColorFilter.tint(MaterialTheme.colors.onSurfaceDim),
                modifier = Modifier
                    .padding(8.dp)
                    .imageSize(20.dp)
                    .semantics { invisibleToUser() }
            )
        }
    }
}