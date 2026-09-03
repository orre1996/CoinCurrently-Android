package com.cryptium.coincurrently.features.Settings.CurrencySelector
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.cryptium.coincurrently.sharedui.sheets.ModalBottomSheetSystemNavigationBar
import com.cryptium.coincurrently.sharedui.sheets.ModalSheetBody
import com.cryptium.coincurrently.sharedui.components.Separator
import com.cryptium.coincurrently.sharedui.sheets.modalSheetNavigationBarExtensionHeight
import com.cryptium.coincurrently.sharedui.sheets.rememberModalSheetMaxBodyHeight
import com.cryptium.coincurrently.sharedui.theme.surfaceContainerLow
import com.cryptium.coincurrently.sharedui.components.ImageCase
import com.cryptium.coincurrently.sharedui.components.SingleSelectView
import com.cryptium.coincurrently.sharedui.components.DragHandle
import com.cryptium.coincurrently.sharedui.inputs.TextInputField
import com.cryptium.coincurrently.core.persistence.allCurrencies
import kotlinx.coroutines.launch
import com.cryptium.coincurrently.core.networking.ResponseModels.CurrencySelectionModel

private val CurrencyRowHeightEstimate = 72.dp
private val CurrencySheetHeaderEstimate = 120.dp
private val CurrencySheetVerticalPadding = 48.dp

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun CurrencyOptionsSheet(
    selectedOption: CurrencySelectionModel,
    newOptionSelected: (CurrencySelectionModel) -> Unit,
    close: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()

    var searchInput by remember { mutableStateOf("") }
    val context = LocalContext.current
    val density = LocalDensity.current
    val maxBodyHeight = rememberModalSheetMaxBodyHeight(dragHandleTitle = "Select Currency")
    val extensionHeight = context.modalSheetNavigationBarExtensionHeight(density)
    val contentMaxHeight = (maxBodyHeight - extensionHeight).coerceAtLeast(0.dp)
    val filteredOptions = remember(searchInput) {
        allCurrencies.filter {
            searchInput.isEmpty() ||
                it.currency.contains(searchInput, true) ||
                it.code.contains(searchInput, true) ||
                it.symbol.contains(searchInput, true)
        }.sortedBy { it.currency }
    }
    val shouldExpandToMaxHeight = remember(filteredOptions.size, contentMaxHeight, density) {
        val estimatedContentHeight =
            CurrencySheetHeaderEstimate +
                (CurrencyRowHeightEstimate * density.fontScale * filteredOptions.size) +
                CurrencySheetVerticalPadding
        estimatedContentHeight > contentMaxHeight
    }

    ModalBottomSheet(
        onDismissRequest = {
            coroutineScope.launch {
                sheetState.hide()
                close.invoke()
            }
        },
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        containerColor = MaterialTheme.colors.surfaceContainerLow,
        tonalElevation = 16.dp,
        contentWindowInsets = { WindowInsets(0, 0, 0, 0) },
        dragHandle = {
            DragHandle(
                title = "Select Currency",
                closeAction = {
                    coroutineScope.launch {
                        sheetState.hide()
                        close.invoke()
                    }
                }
            )
        }
    ) {
        ModalBottomSheetSystemNavigationBar()
        ModalSheetBody(
            context = context,
            maxBodyHeight = maxBodyHeight,
            wrapContent = !shouldExpandToMaxHeight,
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .then(
                        if (shouldExpandToMaxHeight) {
                            Modifier.fillMaxHeight()
                        } else {
                            Modifier
                        },
                    ),
            ) {
                InputFieldView(
                    searchInput,
                    valueChanged = {
                        searchInput = it
                    },
                    onDone = { }
                )

                Separator()

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    contentPadding = PaddingValues(vertical = 24.dp),
                    modifier = if (shouldExpandToMaxHeight) {
                        Modifier.weight(1f)
                    } else {
                        Modifier
                    },
                ) {
                    items(filteredOptions) { option ->
                        SingleSelectView(
                            title = option.currency,
                            subtitle = "${option.code} (${option.symbol})",
                            imageCase = ImageCase.Nothing,
                            isSelected = selectedOption == option,
                            didSelectOption = {
                                coroutineScope.launch {
                                    newOptionSelected.invoke(option)
                                    sheetState.hide()
                                    close.invoke()
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InputFieldView(
    searchInput: String,
    valueChanged: (String) -> Unit,
    onDone: () -> Unit
) {
    TextInputField(
        searchInput,
        placeholder = "Search for currency",
        accessibilityLabel = "Search for currency",
        onInputChange = {
            valueChanged.invoke(it)
        },
        onDone = onDone,
        modifier = Modifier.padding(bottom = 24.dp)
    )
}
