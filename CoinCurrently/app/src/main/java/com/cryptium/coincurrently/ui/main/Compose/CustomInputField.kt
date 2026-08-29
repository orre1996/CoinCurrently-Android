package com.cryptium.coincurrently.ui.main.Compose

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.cryptium.coincurrently.ui.main.Extensions.poppinsRegular
import networking.ResponseModels.CurrencySelectionModel

@Composable
fun CustomInputField(startingValue: String, selectedCurrency: CurrencySelectionModel?, placeHolder: String = "Value", backgroundColor: Color = MaterialTheme.colors.surface, onDone: (String) -> Unit, valueChanged: (String) -> Unit) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val isFocused = remember { mutableStateOf(false) }
    var isEditing by remember { mutableStateOf(false) }

    BasicTextField(
        value = startingValue,
        textStyle = poppinsRegular(16).copy(MaterialTheme.colors.primaryTextColor),
        onValueChange = { value ->
            valueChanged(value)
        },
        maxLines = 1,
        singleLine = true,
        cursorBrush = SolidColor(MaterialTheme.colors.primaryTextColor),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done, keyboardType = KeyboardType.Decimal),
        keyboardActions = KeyboardActions(
            onDone = {
                keyboardController?.hide()
                focusManager.clearFocus()
                onDone(startingValue)
            }
        ),
        modifier = Modifier
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(size = 8.dp)
            )
            .clip(shape = RoundedCornerShape(8.dp))
            .border(
                BorderStroke(
                    1.dp, MaterialTheme.colors.cardBorder
                ), shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .onFocusChanged {
                isFocused.value = it.isFocused
                isEditing = it.isFocused
            },
        decorationBox = { innerTextField ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .heightIn(48.dp)
            ) {
                if (selectedCurrency?.suffix == false) {
                    Text(
                        text = selectedCurrency.symbol,
                        style = poppinsRegular(16),
                        color = if(isEditing || startingValue.isNotEmpty()) MaterialTheme.colors.primaryTextColor else MaterialTheme.colors.mainGray,
                        maxLines = 1
                    )
                }

                Box(
                    modifier = Modifier.weight(1f)
                ) {
                    if (startingValue == "") {
                        Text(
                            text = placeHolder,
                            style = poppinsRegular(16),
                            color = MaterialTheme.colors.mainGray,
                            maxLines = 1
                        )
                    } else {
                        Text(
                            text = startingValue,
                            style = poppinsRegular(16),
                            color = MaterialTheme.colors.primaryTextColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    innerTextField()
                }

                if (selectedCurrency?.suffix == true) {
                    Text(
                        text = selectedCurrency.symbol,
                        style = poppinsRegular(16),
                        color = if(isEditing || startingValue.isNotEmpty()) MaterialTheme.colors.primaryTextColor else MaterialTheme.colors.mainGray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    )
}