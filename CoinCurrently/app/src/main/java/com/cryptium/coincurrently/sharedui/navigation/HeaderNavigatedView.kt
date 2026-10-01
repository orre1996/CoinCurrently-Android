package com.cryptium.coincurrently.sharedui.navigation
import com.cryptium.coincurrently.sharedui.modifiers.imageSize

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.ui.Modifier
import androidx.compose.foundation.Image
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.cryptium.coincurrently.R
import com.cryptium.coincurrently.core.localization.Localization
import com.cryptium.coincurrently.sharedui.components.Separator
import com.cryptium.coincurrently.core.extensions.poppinsSemibold
import com.cryptium.coincurrently.sharedui.theme.primaryTextColor
import com.cryptium.coincurrently.sharedui.modifiers.shimmeringModifier

@Composable
fun HeaderNavigatedView(
    title: String,
    showSeparator: Boolean = true,
    firstImage: Int? = null,
    firstAction: (() -> Unit)? = null,
    secondImage: Int? = null,
    smallerSecondImage: Boolean = false,
    secondAction: (() -> Unit)? = null,
    secondImagePadding: Int = 2,
    horizontalPadding: Int = 108,
    isLoading: Boolean = false,
    isTitleLoading: Boolean = false,
    content: @Composable (() -> Unit)? = null,
    contentSecond: @Composable (() -> Unit)? = null,
    goBack: () -> Unit
) {

    Column {
        Box(
            contentAlignment = Center,
            modifier = Modifier
                .height(52.dp)
                .wrapContentSize(Center)
        ) {
            Text(
                text = title.ifEmpty { "00000000" },
                textAlign = TextAlign.Center,
                style = poppinsSemibold(28),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colors.primaryTextColor,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .wrapContentSize(Center)
                    .align(Center)
                    .wrapContentHeight(align = CenterVertically)
                    .padding(horizontal = horizontalPadding.dp)
                    .shimmeringModifier(title.isEmpty() || isTitleLoading)
            )

            Row(
                verticalAlignment = CenterVertically,
                modifier = Modifier
                    .padding(horizontal = 2.dp)
            ) {
                IconButton(
                    onClick = {
                        goBack()
                    }
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.arrow_forward),
                        contentDescription = Localization.string("Navigate back"),
                        contentScale = ContentScale.Crop,
                        colorFilter = ColorFilter.tint(MaterialTheme.colors.primaryTextColor),
                        modifier = Modifier
                            .align(CenterVertically)
                            .height(40.dp)
                            .width(40.dp)
                            .rotate(180f)
                            .padding(6.dp)
                    )
                }

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

                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .padding(end = 14.dp)
                            .size(28.dp),
                        color = MaterialTheme.colors.primaryTextColor,
                    )
                } else if (secondImage != null) {
                    IconButton(
                        onClick = {
                            secondAction?.invoke()
                        }
                    ) {
                        Image(
                            painter = painterResource(id = secondImage),
                            colorFilter = ColorFilter.tint(MaterialTheme.colors.primaryTextColor),
                            contentDescription = null,
                            modifier = Modifier
                                .padding(end = 4.dp)
                                .imageSize(40.dp)
                                .padding(if (smallerSecondImage) 6.dp else secondImagePadding.dp)
                        )
                    }
                }

                if(content != null) {
                    content()
                }

                if (contentSecond != null) {
                    contentSecond()
                }
            }
        }

        if (showSeparator) {
            Separator()
        }
    }
}