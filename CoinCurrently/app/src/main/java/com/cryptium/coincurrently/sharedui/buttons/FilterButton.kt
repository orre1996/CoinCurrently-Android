package com.cryptium.coincurrently.sharedui.buttons
import com.cryptium.coincurrently.sharedui.modifiers.imageSize

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.invisibleToUser
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.cryptium.coincurrently.R
import com.cryptium.coincurrently.sharedui.theme.onSurfaceDim
import com.cryptium.coincurrently.sharedui.theme.outlineVariant
import com.cryptium.coincurrently.core.extensions.satoshiBold
import com.cryptium.coincurrently.core.extensions.satoshiMedium

@Composable
fun FilterButton(
    title: String,
    filterName: String,
    isActive: Boolean,
    selectedCount: Int = 0,
    selectedTitle: String? = null,
    accessibilityLabel: String? = null,
    action: () -> Unit
) {
    val resolvedAccessibilityLabel = accessibilityLabel ?: when {
        selectedTitle != null -> {
            val label = filterName.replaceFirstChar { char ->
                if (char.isLowerCase()) char.titlecase() else char.toString()
            }
            "$label, $selectedTitle"
        }
        selectedCount > 0 -> "Filter by $filterName, $selectedCount selected"
        else -> "Filter by $filterName"
    }

    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .semantics(mergeDescendants = true) {
                contentDescription = resolvedAccessibilityLabel
            }
            .border(
                width = 1.dp,
                color = MaterialTheme.colors.outlineVariant,
                shape = RoundedCornerShape(size = 1000.dp)
            )
            .padding(
                PaddingValues(
                    start = 12.dp,
                    top = 10.dp,
                    end = 16.dp,
                    bottom = 10.dp
                )
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
            ) {
                action.invoke()
            }
    ) {
        Text(
            text = title,
            style = satoshiMedium(16),
            color = if(isActive) MaterialTheme.colors.onSurface else MaterialTheme.colors.onSurfaceDim,
            maxLines = 1,
            modifier = Modifier.semantics { invisibleToUser() },
        )

        Image(
            painter = painterResource(R.drawable.chevron_down),
            contentDescription = null,
            colorFilter = ColorFilter.tint(if(isActive) MaterialTheme.colors.onSurface else MaterialTheme.colors.onSurfaceDim),
            modifier = Modifier
                .imageSize(16.dp)
                .semantics { invisibleToUser() }
        )
    }
}