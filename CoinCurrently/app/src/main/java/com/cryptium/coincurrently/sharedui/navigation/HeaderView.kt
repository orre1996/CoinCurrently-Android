package com.cryptium.coincurrently.sharedui.navigation
import com.cryptium.coincurrently.sharedui.modifiers.imageSize

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.cryptium.coincurrently.R
import com.cryptium.coincurrently.core.extensions.poppinsSemibold
import com.cryptium.coincurrently.sharedui.components.Separator
import com.cryptium.coincurrently.sharedui.theme.primaryTextColor

@Composable
fun HeaderView(title: String,
               forceRedraw: Boolean = false,
               showSeparator: Boolean = true,
               firstImage: Int? = null,
               firstAction: (() -> Unit)? = null,
               secondImage: Int? = null,
               smallerSecondImage: Boolean = false,
               secondAction: (() -> Unit)? = null,
               content: @Composable (() -> Unit)? = null) {

    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(0.dp),
            modifier = Modifier
                .height(52.dp)
                .padding(horizontal = 8.dp)
        ) {
            Text(
                text = title,
                textAlign = TextAlign.Start,
                style = poppinsSemibold(28),
                color = MaterialTheme.colors.primaryTextColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .wrapContentHeight(align = Alignment.CenterVertically)
                    .padding(start = 8.dp, end = 16.dp)
            )

            Spacer(modifier = Modifier.weight(1f))

            if (firstImage != null) {
                AnimatedVisibility(
                    visible = secondImage != R.drawable.ic_baseline_cancel_24,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    IconButton(
                        onClick = {
                            firstAction?.invoke()
                        }
                    ) {
                        Image(
                            painter = painterResource(id = firstImage),
                            colorFilter = ColorFilter.tint(MaterialTheme.colors.primaryTextColor),
                            contentDescription = null,
                            modifier = Modifier
                                .imageSize(40.dp)
                        )
                    }
                }
            }

            if (secondImage != null) {
                IconButton(
                    onClick = {
                        secondAction?.invoke()
                    }
                ) {
                    Image(
                        painter = painterResource(id = secondImage),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(MaterialTheme.colors.primaryTextColor),
                        modifier = Modifier
                            .imageSize(40.dp)
                            .padding(if (smallerSecondImage) 6.dp else 2.dp)
                    )
                }
            }

            if (content != null) {
                content()
            }
        }

        if (showSeparator) {
            Separator(forceRedraw = forceRedraw)
        }
    }
}