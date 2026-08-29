package com.cryptium.coincurrently.ui.main.HomeTab.Compose

import com.cryptium.coincurrently.ui.main.Compose.imageSize

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.invisibleToUser
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cryptium.coincurrently.R
import com.cryptium.coincurrently.ui.main.Compose.onSurfaceContainer
import com.cryptium.coincurrently.ui.main.Compose.onSurfaceDim
import com.cryptium.coincurrently.ui.main.Compose.primaryBackground
import com.cryptium.coincurrently.ui.main.Compose.primaryTextColor
import com.cryptium.coincurrently.ui.main.Compose.surfaceContainerHigh
import com.cryptium.coincurrently.ui.main.Extensions.satoshiBold
import com.cryptium.coincurrently.ui.main.Extensions.satoshiRegular

@Composable
fun PremiumView(
    title: String,
    message: String,
    accessibleMessage: String? = null,
    timeLeft: String?,
    viewPressed: () -> Unit,
    closeAction: (String) -> Unit
) {
    val accessibilityLabelText = when {
        !accessibleMessage.isNullOrEmpty() -> accessibleMessage
        timeLeft != null && title == "Welcome offer" -> "$title. $message. $timeLeft"
        else -> "$title. $message"
    }

    Box(
        contentAlignment = Alignment.TopEnd,
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(16.dp),
    ) {
        Box(
            modifier = Modifier
                .dropShadow(
                    shape = RoundedCornerShape(12.dp),
                    shadow = Shadow(
                        radius = 2.dp,
                        spread = 0.dp,
                        color = Color.Black.copy(alpha = 0.08f),
                        offset = DpOffset(0.dp, 2.dp)
                    )
                )
                .dropShadow(
                    shape = RoundedCornerShape(12.dp),
                    shadow = Shadow(
                        radius = 8.dp,
                        spread = 0.dp,
                        color = Color.Black.copy(alpha = 0.12f),
                        offset = DpOffset(0.dp, 4.dp)
                    )
                )
                .primaryBackground(
                    paddingValues = PaddingValues(0.dp),
                    backgroundColor = MaterialTheme.colors.surfaceContainerHigh
                ) {
                    viewPressed.invoke()
                }
                .semantics(mergeDescendants = true) {
                    contentDescription = accessibilityLabelText
                    onClick(label = null, action = {
                        viewPressed.invoke()
                        true
                    })
                }
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
                    .semantics { invisibleToUser() }
            ) {
            Image(
                painter = painterResource(
                    if (MaterialTheme.colors.isLight) R.drawable.premium_logo_light
                    else R.drawable.premium_logo_dark
                ),
                contentDescription = null,
                modifier = Modifier
                    .imageSize(84.dp, 84.dp)
                    .padding(end = 16.dp)
            )

            Column {
                Text(
                    title,
                    style = satoshiBold(22),
                    color = MaterialTheme.colors.onSurfaceContainer,
                    modifier = Modifier
                        .padding(
                            bottom = 8.dp,
                            end = 40.dp
                        )
                )

                Text(
                    message,
                    style = satoshiRegular(16).copy(lineHeight = 24.sp),
                    color = MaterialTheme.colors.onSurfaceDim,
                    modifier = Modifier
                        .padding(bottom = 4.dp)
                )

                if (timeLeft != null && title == "Welcome offer")
                    Text(
                        timeLeft,
                        style = satoshiRegular(14),
                        color = MaterialTheme.colors.onSurfaceDim
                    )
            }
        }
        }

        CloseButton {
            closeAction.invoke(title)
        }
    }
}

@Composable
private fun CloseButton(
    callback: () -> Unit
) {
    Image(painter = painterResource(id = R.drawable.cross),
        contentDescription = "Hide offer",
        colorFilter = ColorFilter.tint(MaterialTheme.colors.primaryTextColor),
        modifier = Modifier
            .padding(4.dp)
            .size(48.dp)
            .padding(12.dp)
            .imageSize(24.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
            ) {
                callback.invoke()
            }
    )
}
