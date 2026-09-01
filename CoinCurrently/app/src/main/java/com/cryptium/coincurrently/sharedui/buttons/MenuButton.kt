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
import com.cryptium.coincurrently.sharedui.theme.onSurfaceContainer

@Composable
fun MenuButton(
    options: List<String>,
    selectedOption: String,
    optionChanged: (String) -> Unit
) {
    var isDropDownExpanded by remember { mutableStateOf(false) }

    Box {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    isDropDownExpanded = true
                }
        ) {
            Text(
                text = selectedOption,
                style = satoshiBold(18),
                maxLines = 1,
                color = MaterialTheme.colors.onSurfaceContainer
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
            options.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = option,
                            style = satoshiBold(18),
                            color = MaterialTheme.colors.onSurfaceContainer
                        )
                    },
                    onClick = {
                        optionChanged.invoke(option)
                        isDropDownExpanded = false
                    }
                )
            }
        }
    }
}