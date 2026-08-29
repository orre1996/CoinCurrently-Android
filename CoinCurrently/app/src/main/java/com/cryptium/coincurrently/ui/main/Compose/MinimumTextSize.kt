package com.cryptium.coincurrently.ui.main.Compose

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.semantics.invisibleToUser
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Reserves at least the width of [text] plus [extraSize], then lays out [content] with [alignment].
 * Mirrors iOS `MinimumTextSize` (invisible text with horizontal padding + ZStack alignment).
 */
@Composable
fun MinimumTextSize(
    text: String,
    font: TextStyle,
    extraSize: Dp = 0.dp,
    alignment: Alignment = Alignment.Center,
    maxLines: Int = 1,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier,
        contentAlignment = alignment,
    ) {
        Text(
            text = text,
            style = font,
            maxLines = maxLines,
            softWrap = maxLines > 1,
            modifier = Modifier
                .wrapContentWidth()
                .padding(horizontal = extraSize / 2)
                .alpha(0f)
                .semantics { invisibleToUser() },
        )

        content()
    }
}
