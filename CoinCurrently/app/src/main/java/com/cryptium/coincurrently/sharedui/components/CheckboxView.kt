package com.cryptium.coincurrently.sharedui.components
import com.cryptium.coincurrently.sharedui.modifiers.imageSize

import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cryptium.coincurrently.R
import com.cryptium.coincurrently.sharedui.theme.onSurfaceDim
import com.cryptium.coincurrently.sharedui.theme.outline
import com.cryptium.coincurrently.core.extensions.satoshiMedium

@Composable
fun CheckboxView(
    trailingText: String,
    isChecked: Boolean,
    size: Dp = 18.dp,
    modifier: Modifier = Modifier,
    isInteractive: Boolean = true,
    onToggle: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.then(
            if (isInteractive) {
                Modifier.toggleable(
                    value = isChecked,
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    role = Role.Checkbox,
                    onValueChange = { onToggle.invoke() }
                )
            } else {
                Modifier
            }
        )
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_baseline_check_24),
            contentDescription = null,
            colorFilter = ColorFilter.tint(MaterialTheme.colors.surface.copy(if (isChecked) 1f else 0f)),
            modifier = Modifier
                .padding(16.dp)
                .background(
                    color = if(isChecked) MaterialTheme.colors.primary else Color.Transparent,
                    shape = RoundedCornerShape(2.dp)
                )
                .imageSize(size, size)
                .border(
                    BorderStroke(
                        width = 2.dp, color = if(isChecked) Color.Transparent else MaterialTheme.colors.outline
                    ), shape = RoundedCornerShape(2.dp)
                )
                .padding(1.dp)
        )


        if(trailingText.isNotEmpty()) {
            Text(
                text = trailingText,
                style = satoshiMedium(14),
                color = MaterialTheme.colors.onSurfaceDim
            )
        }
    }
}