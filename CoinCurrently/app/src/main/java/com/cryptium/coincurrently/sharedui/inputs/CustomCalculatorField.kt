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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.cryptium.coincurrently.core.extensions.isNumeric
import com.cryptium.coincurrently.core.extensions.poppinsRegular
import com.cryptium.coincurrently.core.networking.ResponseModels.CurrencySelectionModel
import com.cryptium.coincurrently.sharedui.theme.cardBackground
import com.cryptium.coincurrently.sharedui.theme.cardBorder
import com.cryptium.coincurrently.sharedui.theme.mainGray
import com.cryptium.coincurrently.sharedui.theme.primaryTextColor

@Composable
fun CustomCalculatorField(startingValue: String, selectedCurrency: CurrencySelectionModel?, searchPressed: ((String) -> Unit)? = null, valueChanged: (String) -> Unit) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val isFocused = remember { mutableStateOf(false) }
    var input by remember { mutableStateOf(startingValue) }

    BasicTextField(
        value = input,
        textStyle = poppinsRegular(16).copy(MaterialTheme.colors.primaryTextColor),
        onValueChange = { value ->
            input = trimmedInput(value)
            valueChanged(input)
        },
        singleLine = true,
        cursorBrush = SolidColor(MaterialTheme.colors.primaryTextColor),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done, keyboardType = KeyboardType.Decimal),
        keyboardActions = KeyboardActions(
            onDone = {
                keyboardController?.hide()
                focusManager.clearFocus()
                searchPressed?.invoke(input)

                if (input.isEmpty() || !input.isNumeric()) {
                    input = "0"
                    valueChanged(input)
                }
            }
        ),
        modifier = Modifier
            .background(
                color = MaterialTheme.colors.cardBackground,
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
                        color = MaterialTheme.colors.primaryTextColor,
                        maxLines = 1
                    )
                }

                Box(
                    modifier = Modifier.weight(1f)
                ) {
                    if (input == "") {
                        Text(
                            text = "Value",
                            style = poppinsRegular(16),
                            color = MaterialTheme.colors.mainGray,
                            maxLines = 1
                        )
                    } else {
                        Text(
                            text = input,
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
                        color = MaterialTheme.colors.primaryTextColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    )
}

fun trimmedInput(input: String): String {
    var hasColon = false
    var newString = ""

    for (char in input) {

        if (!hasColon || (char.toString() != "," && char.toString() != ".")) {
            newString += char
        }

        if (char.toString() == "," || char.toString() == ".") {
            hasColon = true
        }
    }

    return newString
}