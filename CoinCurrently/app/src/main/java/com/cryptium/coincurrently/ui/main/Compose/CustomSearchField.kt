package com.cryptium.coincurrently.ui.main.Compose

import com.cryptium.coincurrently.ui.main.Compose.imageSize

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.cryptium.coincurrently.R
import com.cryptium.coincurrently.ui.main.Extensions.poppinsRegular

@Composable
fun CustomSearchField(
    state: MutableState<TextFieldValue>,
    searchPressed: (String) -> Unit,
    onValueChanged: (String) -> Unit
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val isFocused = remember { mutableStateOf(false) }

    TextField(
        value = state.value,
        textStyle = poppinsRegular(16).copy(MaterialTheme.colors.primaryTextColor),
        onValueChange = {
            state.value = TextFieldValue(it.text, TextRange(it.text.length))
            onValueChanged(it.text)
        },
        placeholder = {
            Text(
                text = "Search",
                style = poppinsRegular(16),
                color = MaterialTheme.colors.mainGray,
                maxLines = 1
            )
        },
        shape = RoundedCornerShape(8.dp),
        singleLine = true,
        keyboardOptions = KeyboardOptions(autoCorrectEnabled = false, imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(
            onDone = {
                keyboardController?.hide()
                focusManager.clearFocus()
                searchPressed(state.value.text)
            }
        ),
        leadingIcon = {
            Image(
                painter = painterResource(id = R.drawable.ic_search_icon),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                colorFilter = ColorFilter.tint(if (state.value.text.isEmpty()) MaterialTheme.colors.mainGray else MaterialTheme.colors.primaryTextColor),
                modifier = Modifier
                    .padding(start = 16.dp)
                    .imageSize(24.dp)
            )
        },
        trailingIcon = {
            if (state.value.text.isNotEmpty() && isFocused.value) {
                IconButton(
                    onClick = {
                        state.value = state.value.copy("")
                        onValueChanged("")
                    }
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_baseline_cancel_24),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        colorFilter = ColorFilter.tint(MaterialTheme.colors.primaryTextColor),
                        modifier = Modifier
                            .imageSize(24.dp)
                    )
                }
            }
        },
        colors = TextFieldDefaults.textFieldColors(
            backgroundColor = MaterialTheme.colors.surface,
            focusedIndicatorColor = MaterialTheme.colors.transparent,
            unfocusedIndicatorColor = MaterialTheme.colors.transparent,
        ),
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .heightIn(48.dp)
            .clip(shape = RoundedCornerShape(8.dp))
            .background(MaterialTheme.colors.surface)
            .border(
                BorderStroke(
                    1.dp, MaterialTheme.colors.cardBorder
                ), shape = RoundedCornerShape(8.dp)
            )
            .fillMaxWidth()
            .onFocusChanged {
                isFocused.value = it.isFocused
            }
    )
}
