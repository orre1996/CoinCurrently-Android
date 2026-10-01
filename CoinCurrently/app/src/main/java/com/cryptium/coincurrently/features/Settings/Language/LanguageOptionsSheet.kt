package com.cryptium.coincurrently.features.Settings.Language

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
import com.cryptium.coincurrently.core.localization.AppLanguage
import com.cryptium.coincurrently.core.localization.AppLanguagePreference
import com.cryptium.coincurrently.core.localization.Localization
import com.cryptium.coincurrently.sharedui.components.DragHandle
import com.cryptium.coincurrently.sharedui.components.ImageCase
import com.cryptium.coincurrently.sharedui.components.Separator
import com.cryptium.coincurrently.sharedui.components.SingleSelectView
import com.cryptium.coincurrently.sharedui.inputs.TextInputField
import com.cryptium.coincurrently.sharedui.sheets.ModalBottomSheetSystemNavigationBar
import com.cryptium.coincurrently.sharedui.sheets.ModalSheetBody
import com.cryptium.coincurrently.sharedui.sheets.modalSheetNavigationBarExtensionHeight
import com.cryptium.coincurrently.sharedui.sheets.rememberModalSheetMaxBodyHeight
import com.cryptium.coincurrently.sharedui.theme.surfaceContainerLow
import kotlinx.coroutines.launch

private val LanguageRowHeightEstimate = 72.dp
private val LanguageSheetHeaderEstimate = 120.dp
private val LanguageSheetVerticalPadding = 48.dp

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun LanguageOptionsSheet(
    selectedOption: AppLanguagePreference,
    newOptionSelected: (AppLanguagePreference) -> Unit,
    close: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()

    var searchInput by remember { mutableStateOf("") }
    val context = LocalContext.current
    val density = LocalDensity.current
    val maxBodyHeight = rememberModalSheetMaxBodyHeight(dragHandleTitle = "Select Language")
    val extensionHeight = context.modalSheetNavigationBarExtensionHeight(density)
    val contentMaxHeight = (maxBodyHeight - extensionHeight).coerceAtLeast(0.dp)
    val filteredLanguages = remember(searchInput) {
        AppLanguage.entries.filter { language ->
            searchInput.isEmpty() ||
                language.nativeName.contains(searchInput, ignoreCase = true) ||
                language.englishName.contains(searchInput, ignoreCase = true) ||
                language.code.contains(searchInput, ignoreCase = true)
        }
    }
    val showsSystem = searchInput.isEmpty()
    val rowCount = filteredLanguages.size + if (showsSystem) 1 else 0
    val shouldExpandToMaxHeight = remember(rowCount, contentMaxHeight, density) {
        val estimatedContentHeight =
            LanguageSheetHeaderEstimate +
                (LanguageRowHeightEstimate * density.fontScale * rowCount) +
                LanguageSheetVerticalPadding
        estimatedContentHeight > contentMaxHeight
    }

    fun dismissAfter(action: () -> Unit) {
        coroutineScope.launch {
            action()
            sheetState.hide()
            close()
        }
    }

    ModalBottomSheet(
        onDismissRequest = { dismissAfter { } },
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        containerColor = MaterialTheme.colors.surfaceContainerLow,
        tonalElevation = 16.dp,
        contentWindowInsets = { WindowInsets(0, 0, 0, 0) },
        dragHandle = {
            DragHandle(
                title = "Select Language",
                closeAction = { dismissAfter { } },
            )
        },
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
                TextInputField(
                    searchInput,
                    placeholder = "Search for language",
                    accessibilityLabel = if (searchInput.isEmpty()) {
                        Localization.string("Search for language")
                    } else {
                        searchInput
                    },
                    onInputChange = { searchInput = it },
                    onDone = { },
                    modifier = Modifier.padding(bottom = 24.dp),
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
                    if (showsSystem) {
                        item(key = "system") {
                            SingleSelectView(
                                title = Localization.string("System"),
                                subtitle = Localization.format(
                                    "Currently %@",
                                    AppLanguage.matching().nativeName,
                                ),
                                imageCase = ImageCase.Nothing,
                                isSelected = selectedOption is AppLanguagePreference.System,
                                didSelectOption = {
                                    dismissAfter { newOptionSelected(AppLanguagePreference.System) }
                                },
                            )
                        }
                    }

                    items(filteredLanguages, key = { it.code }) { language ->
                        SingleSelectView(
                            title = language.nativeName,
                            subtitle = language.subtitle,
                            imageCase = ImageCase.Nothing,
                            isSelected = selectedOption == AppLanguagePreference.Fixed(language),
                            didSelectOption = {
                                dismissAfter { newOptionSelected(AppLanguagePreference.Fixed(language)) }
                            },
                        )
                    }
                }
            }
        }
    }
}
