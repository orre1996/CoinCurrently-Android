package com.cryptium.coincurrently.sharedui.inputs
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Text
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cryptium.coincurrently.sharedui.theme.cardBorder
import com.cryptium.coincurrently.sharedui.theme.mainGray
import com.cryptium.coincurrently.sharedui.theme.mainRed
import com.cryptium.coincurrently.sharedui.theme.onSurfaceDimmer
import com.cryptium.coincurrently.sharedui.theme.outlineVariant
import com.cryptium.coincurrently.core.extensions.poppinsRegular
import com.cryptium.coincurrently.sharedui.theme.primaryTextColor
import com.cryptium.coincurrently.sharedui.theme.surfaceContainerLowest
import com.cryptium.coincurrently.sharedui.theme.trendDown
import com.cryptium.coincurrently.core.extensions.satoshiBold
import com.cryptium.coincurrently.core.extensions.satoshiRegular

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun CustomFreeInputField(
    startingValue: String,
    placeholder: String = "Enter notes (Optional)",
    accessibilityLabel: String? = null,
    maxLines: Int = 1,
    type: KeyboardType = KeyboardType.Decimal,
    showError: Boolean = false,
    height: Dp = 132.dp,
    alignment: Alignment.Vertical = Alignment.Top,
    valueChanged: (String) -> Unit
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val isFocused = remember { mutableStateOf(false) }

    BasicTextField(
        value = startingValue,
        textStyle = satoshiBold(16).copy(MaterialTheme.colors.primaryTextColor),
        onValueChange = { value ->
            valueChanged(value)
        },
        maxLines = maxLines,
        singleLine = maxLines == 1,
        cursorBrush = SolidColor(MaterialTheme.colors.primaryTextColor),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done, keyboardType = type),
        keyboardActions = KeyboardActions(
            onDone = {
                keyboardController?.hide()
                focusManager.clearFocus()
            }
        ),
        modifier = Modifier
            .background(
                color = MaterialTheme.colors.surface,
                shape = RoundedCornerShape(size = 8.dp)
            )
            .clip(shape = RoundedCornerShape(8.dp))
            .border(
                BorderStroke(
                    1.dp,
                    if (showError) MaterialTheme.colors.trendDown else MaterialTheme.colors.outlineVariant
                ), shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .then(
                if (accessibilityLabel != null) {
                    Modifier.semantics {
                        contentDescription = accessibilityLabel
                    }
                } else {
                    Modifier
                }
            )
            .onFocusChanged {
                isFocused.value = it.isFocused
            },
        decorationBox = { innerTextField ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = alignment,
                modifier = Modifier
                    .heightIn(height)

            ) {
                Box(
                    modifier = Modifier
                        .padding(vertical = 16.dp)
                        .weight(1f)
                ) {
                    if (startingValue == "") {
                        Text(
                            text = placeholder,
                            style = satoshiRegular(16),
                            color = MaterialTheme.colors.onSurfaceDimmer,
                            maxLines = maxLines
                        )
                    }

                    innerTextField()
                }
            }
        }
    )
}