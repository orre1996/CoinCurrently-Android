package com.cryptium.coincurrently.sharedui.inputs
import com.cryptium.coincurrently.sharedui.modifiers.imageSize

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.invisibleToUser
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cryptium.coincurrently.R
import com.cryptium.coincurrently.core.localization.Localization
import com.cryptium.coincurrently.sharedui.theme.onSurfaceDimmer
import com.cryptium.coincurrently.sharedui.theme.outlineVariant
import com.cryptium.coincurrently.sharedui.modifiers.CappedFontScale
import com.cryptium.coincurrently.sharedui.modifiers.DynamicTypeSizeCap
import com.cryptium.coincurrently.core.extensions.satoshiBold

@Composable
fun TextInputField(
    input: String,
    startIcon: Int? = R.drawable.search,
    placeholder: String = "Search for coins",
    accessibilityLabel: String? = null,
    outlineColor: Color = MaterialTheme.colors.outlineVariant,
    isEnabled: Boolean = true,
    modifier: Modifier = Modifier,
    trailingView: (@Composable() () -> Unit)? = null,
    onInputChange: (String) -> Unit,
    onDone: () -> Unit
) {
    val localizedPlaceholder = Localization.string(placeholder)
    val fieldLabel = accessibilityLabel ?: localizedPlaceholder
    val fieldKind = Localization.string(
        if (startIcon == R.drawable.search) "search field" else "text field",
    )
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    CappedFontScale(DynamicTypeSizeCap.Accessibility2) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(shape = RoundedCornerShape(8.dp))
            .background(MaterialTheme.colors.surface)
            .border(
                BorderStroke(
                    width = 1.dp,
                    color = outlineColor
                ), shape = RoundedCornerShape(8.dp)
            )
            .padding(
                horizontal = 16.dp,
                vertical = 16.dp
            )
    ) {
        startIcon?.let {
            Image(
                painter = painterResource(it),
                contentDescription = null,
                colorFilter = ColorFilter.tint(MaterialTheme.colors.onSurfaceDimmer),
                modifier = Modifier
                    .imageSize(20.dp)
            )
        }

        BasicTextField(
            value = input,
            onValueChange = { newText ->
                onInputChange(newText)
            },
            keyboardActions = KeyboardActions(
                onDone = {
                    keyboardController?.hide()
                    focusManager.clearFocus()
                    onDone()
                }
            ),
            textStyle = TextStyle(
                fontFamily = satoshiBold,
                color = MaterialTheme.colors.onSurface,
                fontSize = 16.sp,
                platformStyle = PlatformTextStyle(
                    includeFontPadding = false
                )
            ),
            singleLine = true,
            cursorBrush = SolidColor(MaterialTheme.colors.onSurface),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Done,
            ),
            decorationBox = { innerTextField ->
                if (input.isEmpty()) {
                    Text(
                        text = localizedPlaceholder,
                        style = satoshiBold(16),
                        color = MaterialTheme.colors.onSurfaceDimmer,
                        maxLines = 1,
                        modifier = Modifier.semantics { invisibleToUser() }
                    )
                }

                innerTextField()
            },
            enabled = isEnabled,
            modifier = Modifier
                .weight(1f)
                .semantics {
                    contentDescription = Localization.format("%@, %@", fieldLabel, fieldKind)
                }
        )

        trailingView?.invoke()
    }
    }
}