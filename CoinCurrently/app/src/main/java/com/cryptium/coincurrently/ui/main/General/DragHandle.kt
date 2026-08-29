package com.cryptium.coincurrently.ui.main.General

import com.cryptium.coincurrently.ui.main.Compose.imageSize

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.invisibleToUser
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cryptium.coincurrently.R
import com.cryptium.coincurrently.ui.main.Compose.CloseButton
import com.cryptium.coincurrently.ui.main.Compose.contentScreenWidthPx
import com.cryptium.coincurrently.ui.main.Compose.DragHandler
import com.cryptium.coincurrently.ui.main.Compose.onSurfaceDim
import com.cryptium.coincurrently.ui.main.Compose.onSurfaceDimmer
import com.cryptium.coincurrently.ui.main.Extensions.satoshiBold

@Composable
fun rememberDragHandleHeight(
    title: String,
    icon: Int? = null,
    bottomPadding: Dp = 24.dp,
): Dp {
    val density = LocalDensity.current
    val fontScale = LocalConfiguration.current.fontScale
    val measuredHeightPx = remember(title, icon, bottomPadding, fontScale) { mutableIntStateOf(0) }
    val measureWidthPx = contentScreenWidthPx()

    Box(modifier = Modifier.size(0.dp)) {
        SubcomposeLayout { _ ->
            val placeable = subcompose("dragHandle") {
                DragHandle(
                    title = title,
                    icon = icon,
                    bottomPadding = bottomPadding,
                    closeAction = {},
                )
            }.first().measure(
                Constraints(
                    minWidth = measureWidthPx,
                    maxWidth = measureWidthPx,
                    minHeight = 0,
                    maxHeight = Constraints.Infinity,
                ),
            )
            measuredHeightPx.intValue = placeable.height
            layout(0, 0) {}
        }
    }

    return if (measuredHeightPx.intValue > 0) {
        with(density) { measuredHeightPx.intValue.toDp() }
    } else {
        (88.dp * fontScale).coerceAtMost(200.dp)
    }
}

@Composable
fun DragHandle(
    title: String,
    accessibilityTitle: String? = null,
    icon: Int? = null,
    bottomPadding: Dp = 24.dp,
    focusHeaderOnAppear: Boolean = false,
    closeAction: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    val resolvedAccessibilityTitle = accessibilityTitle ?: title
    val accessibilityLabel = if (icon != null) {
        "$resolvedAccessibilityTitle. info"
    } else {
        resolvedAccessibilityTitle
    }

    if (focusHeaderOnAppear) {
        LaunchedEffect(Unit) {
            runCatching { focusRequester.requestFocus() }
        }
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(
                top = 16.dp,
                start = 16.dp,
                bottom = bottomPadding,
                end = 16.dp
            )
    ) {
        DragHandler()

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            if (icon != null) {
                Image(
                    painter = painterResource(icon),
                    colorFilter = ColorFilter.tint(MaterialTheme.colors.onSurfaceDimmer),
                    contentDescription = null,
                    modifier = Modifier
                        .imageSize(24.dp)
                        .semantics { invisibleToUser() }
                )
            }

            Text(
                text = title,
                style = satoshiBold(22),
                color = MaterialTheme.colors.onSurfaceDim,
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(focusRequester)
                    .focusable()
                    .semantics {
                        heading()
                        contentDescription = accessibilityLabel
                    }
            )

            CloseButton(
                onClick = {
                    closeAction.invoke()
                }
            )
        }
    }
}