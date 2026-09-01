package com.cryptium.coincurrently.sharedui.buttons
import com.cryptium.coincurrently.sharedui.modifiers.imageSize

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.DropdownMenu
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.cryptium.coincurrently.R
import com.cryptium.coincurrently.core.extensions.satoshiBold
import com.cryptium.coincurrently.sharedui.models.ChangeDirection
import com.cryptium.coincurrently.sharedui.theme.onSurfaceContainer

@Composable
fun ChangeButton(
    direction: ChangeDirection,
    modifier: Modifier = Modifier,
    currencyChanged: (ChangeDirection) -> Unit
) {
    var isDropDownExpanded by remember { mutableStateOf(false) }

    Box {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = modifier
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    isDropDownExpanded = true
                }
        ) {
            Text(
                text = direction.symbol,
                style = satoshiBold(18),
                maxLines = 1,
                color = direction.color
            )

            Image(
                painter = painterResource(R.drawable.trend_down),
                contentDescription = null,
                colorFilter = ColorFilter.tint(MaterialTheme.colors.onSurfaceContainer),
                modifier = Modifier
                    .imageSize(20.dp)
            )
        }

        DropdownMenu(
            expanded = isDropDownExpanded,
            onDismissRequest = {
                isDropDownExpanded = false
            }
        ) {
            ChangeDirection.entries.forEach { direction ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = direction.symbol,
                            style = satoshiBold(18),
                            color = direction.color
                        )
                    },
                    onClick = {
                        currencyChanged.invoke(direction)
                        isDropDownExpanded = false
                    }
                )
            }
        }
    }
}