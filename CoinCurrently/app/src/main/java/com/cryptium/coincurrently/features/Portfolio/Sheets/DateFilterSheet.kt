package com.cryptium.coincurrently.features.Portfolio.Sheets
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.invisibleToUser
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.cryptium.coincurrently.R
import com.cryptium.coincurrently.sharedui.components.AccessibilityLayout
import com.cryptium.coincurrently.sharedui.components.AdaptiveStack
import com.cryptium.coincurrently.sharedui.buttons.PrimaryButton
import com.cryptium.coincurrently.sharedui.buttons.SecondaryButton
import com.cryptium.coincurrently.sharedui.components.Separator
import com.cryptium.coincurrently.sharedui.theme.onSurfaceDim
import com.cryptium.coincurrently.sharedui.theme.surfaceContainerLow
import com.cryptium.coincurrently.core.extensions.DateFormat
import com.cryptium.coincurrently.core.extensions.accessibleLongDateDescription
import com.cryptium.coincurrently.core.extensions.formatDate
import com.cryptium.coincurrently.core.extensions.satoshiMedium
import com.cryptium.coincurrently.sharedui.components.DragHandle
import com.cryptium.coincurrently.sharedui.inputs.TextInputField
import com.cryptium.coincurrently.features.Portfolio.Enums.DateOption
import kotlinx.coroutines.launch
import com.cryptium.coincurrently.sharedui.sheets.ModalBottomSheetSystemNavigationBar
import com.cryptium.coincurrently.sharedui.sheets.rememberModalSheetMaxBodyHeight
import com.cryptium.coincurrently.sharedui.sheets.ModalSheetBody
import com.cryptium.coincurrently.sharedui.sheets.ModalSheetColumnLayout
import com.cryptium.coincurrently.core.analytics.DateFilterCustomApplied
import com.cryptium.coincurrently.core.analytics.DateFilterCustomCleared
import com.cryptium.coincurrently.core.analytics.DateFilterCustomField
import com.cryptium.coincurrently.core.analytics.DateFilterCustomFieldOpened
import com.cryptium.coincurrently.core.analytics.AnalyticsManager
import com.cryptium.coincurrently.core.analytics.ScreenLabel
import java.util.Date

enum class DateFieldOption {
    start,
    end
}

@Composable
@OptIn(ExperimentalStdlibApi::class, ExperimentalMaterial3Api::class)
fun DateFilterSheet(
    fromScreen: ScreenLabel,
    startDate: Date?,
    endDate: Date?,
    selectDate: (DateFieldOption) -> Unit,
    applyDate: (DateOption) -> Unit,
    clearDate: () -> Unit,
    close: () -> Unit
) {
    val startDate = remember { mutableStateOf(startDate) }
    val endDate = remember { mutableStateOf(endDate) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val maxBodyHeight = rememberModalSheetMaxBodyHeight(dragHandleTitle = "Filter By Date")

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
                title = "Filter By Date",
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
        ) {
            ModalSheetColumnLayout(
                maxBodyHeight = maxBodyHeight,
                scrollItemCount = 2,
                modifier = Modifier.padding(horizontal = 16.dp),
                header = {
                    Separator()
                },
                footer = {
                    Separator()
                    ActionButtons(
                        isEnabled = startDate.value != null && endDate.value != null,
                        clearDates = {
                            AnalyticsManager.log(DateFilterCustomCleared(fromScreen))
                            clearDate.invoke()
                            startDate.value = null
                            endDate.value = null
                        },
                        applyAction = {
                            val start = startDate.value
                            val end = endDate.value
                            if (start != null && end != null) {
                                AnalyticsManager.log(DateFilterCustomApplied(fromScreen))
                                coroutineScope.launch {
                                    sheetState.hide()
                                    applyDate(DateOption.Custom(start, end))
                                    close.invoke()
                                }
                            }
                        }
                    )
                },
                scrollContent = { scrollModifier ->
                    Column(
                        modifier = scrollModifier.verticalScroll(rememberScrollState()),
                    ) {
                    StartDateField(
                        startDate = startDate,
                        selectDate = {
                            AnalyticsManager.log(
                                DateFilterCustomFieldOpened(
                                    fromScreen = fromScreen,
                                    field = DateFilterCustomField.Start
                                ),
                            )
                            coroutineScope.launch {
                                sheetState.hide()
                                close.invoke()
                                selectDate.invoke(DateFieldOption.start)
                            }
                        }
                    )

                    EndDateField(
                        endDate = endDate,
                        selectDate = {
                            AnalyticsManager.log(
                                DateFilterCustomFieldOpened(
                                    fromScreen = fromScreen,
                                    field = DateFilterCustomField.End
                                ),
                            )
                            coroutineScope.launch {
                                sheetState.hide()
                                close.invoke()
                                selectDate.invoke(DateFieldOption.end)
                            }
                        }
                    )
                    }
                },
            )
        }
    }

}

@Composable
private fun StartDateField(
    startDate: MutableState<Date?>,
    selectDate: () -> Unit,
) {
    val accessibleStartDate = startDate.value?.accessibleLongDateDescription() ?: "Enter start date"

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .padding(vertical = 24.dp)
            .semantics(mergeDescendants = true) {
                contentDescription = "Start Date: $accessibleStartDate"
                onClick(label = "Select start date", action = {
                    selectDate.invoke()
                    true
                })
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                selectDate.invoke()
            }
    ) {
        Text(
            text = "Start Date",
            style = satoshiMedium(14),
            color = MaterialTheme.colors.onSurfaceDim,
            modifier = Modifier.semantics { invisibleToUser() }
        )

        Row {
            TextInputField(
                input = startDate.value?.formatDate(DateFormat.MMMddyyyy) ?: "",
                startIcon = R.drawable.date,
                placeholder = "Start Date",
                accessibilityLabel = if (startDate.value == null) "Enter start date" else accessibleStartDate,
                isEnabled = false,
                onInputChange = { },
                onDone = { },
                trailingView = { },
                modifier = Modifier
                    .weight(1f)
                    .pointerInput(Unit) { }
                    .semantics { invisibleToUser() }
            )
        }
    }
}

@Composable
private fun EndDateField(
    endDate: MutableState<Date?>,
    selectDate: () -> Unit,
) {
    val accessibleEndDate = endDate.value?.accessibleLongDateDescription() ?: "Enter end date"

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .padding(bottom = 24.dp)
            .semantics(mergeDescendants = true) {
                contentDescription = "End Date: $accessibleEndDate"
                onClick(label = "Select end date", action = {
                    selectDate.invoke()
                    true
                })
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                selectDate.invoke()
            }
    ) {
        Text(
            text = "End Date",
            style = satoshiMedium(14),
            color = MaterialTheme.colors.onSurfaceDim,
            modifier = Modifier.semantics { invisibleToUser() }
        )

        Row {
            TextInputField(
                input = endDate.value?.formatDate(DateFormat.MMMddyyyy) ?: "",
                placeholder = "End Date",
                startIcon = R.drawable.date,
                accessibilityLabel = if (endDate.value == null) "Enter end date" else accessibleEndDate,
                isEnabled = false,
                onInputChange = { },
                onDone = { },
                trailingView = { },
                modifier = Modifier
                    .weight(1f)
                    .semantics { invisibleToUser() }
            )
        }
    }
}

@Composable
private fun ActionButtons(
    isEnabled: Boolean,
    clearDates: () -> Unit,
    applyAction: () -> Unit
) {
    val isHorizontal = AccessibilityLayout.isHorizontal

    AdaptiveStack(
        isHorizontal = isHorizontal,
        spacing = 12.dp,
        horizontalAlignment = Alignment.Start,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                top = 24.dp,
                bottom = 24.dp
            )
    ) {
        SecondaryButton(
            text = "Clear",
            accessibilityLabel = "Clear filters",
            fullWidth = true,
            modifier = sectionModifier()
                .widthIn(max = 400.dp)
        ) {
            clearDates.invoke()
        }

        PrimaryButton(
            text = "Apply",
            accessibilityLabel = "Apply filters",
            isEnabled = isEnabled,
            fullWidth = true,
            modifier = sectionModifier()
                .widthIn(max = 400.dp)
        ) {
            applyAction.invoke()
        }
    }
}
