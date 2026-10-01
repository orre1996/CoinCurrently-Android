package com.cryptium.coincurrently.features.Portfolio.Sheets
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.cryptium.coincurrently.core.localization.Localization
import com.cryptium.coincurrently.sharedui.components.AccessibilityLayout
import com.cryptium.coincurrently.sharedui.components.AdaptiveStack
import com.cryptium.coincurrently.sharedui.buttons.CloseButton
import com.cryptium.coincurrently.sharedui.components.DragHandler
import com.cryptium.coincurrently.sharedui.buttons.PrimaryButton
import com.cryptium.coincurrently.sharedui.buttons.SecondaryButton
import com.cryptium.coincurrently.sharedui.components.Separator
import com.cryptium.coincurrently.sharedui.theme.outline
import com.cryptium.coincurrently.sharedui.theme.surfaceContainerLow
import com.cryptium.coincurrently.core.extensions.hour
import com.cryptium.coincurrently.core.extensions.is24HourClock
import com.cryptium.coincurrently.core.extensions.minute
import com.cryptium.coincurrently.core.extensions.satoshiMedium
import com.cryptium.coincurrently.core.extensions.toDate
import com.cryptium.coincurrently.sharedui.components.DragHandle
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalTime
import network.chaintech.kmp_date_time_picker.ui.timepicker.WheelTimePickerComponent.WheelTimePicker
import network.chaintech.kmp_date_time_picker.utils.TimeFormat
import network.chaintech.kmp_date_time_picker.utils.WheelPickerDefaults
import com.cryptium.coincurrently.sharedui.sheets.ModalBottomSheetSystemNavigationBar
import com.cryptium.coincurrently.sharedui.sheets.rememberModalSheetMaxBodyHeight
import com.cryptium.coincurrently.sharedui.sheets.ModalSheetBody
import com.cryptium.coincurrently.core.analytics.DatePickerValueConfirmed
import com.cryptium.coincurrently.core.analytics.AnalyticsManager
import com.cryptium.coincurrently.core.analytics.ScreenLabel
import java.util.Date


@Composable
@OptIn(ExperimentalStdlibApi::class, ExperimentalMaterial3Api::class)
fun TimeRangePickerSheet(
    fromScreen: ScreenLabel,
    startDate: Date,
    close: () -> Unit,
    applyDate: (Date) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()

    val context = LocalContext.current
    val maxBodyHeight = rememberModalSheetMaxBodyHeight(dragHandleTitle = "Select Time")
    var selectedDate by remember { mutableStateOf(startDate) }

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
                title = "Select Time",
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
            Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                    .padding(
                    start = 16.dp,
                    end = 16.dp
                )
            ) {

            Separator()

            WheelTimePicker(
                hideHeader = true,
                rowCount = 5,
                timeFormat = if(context.is24HourClock) TimeFormat.HOUR_24 else TimeFormat.AM_PM,
                startTime = run {
                    LocalTime(startDate.hour, startDate.minute, 0)
                },
                selectedTextStyle = satoshiMedium(20).copy(
                    color = MaterialTheme.colors.onSurface
                ),
                defaultTextStyle = satoshiMedium(20).copy(
                    color = MaterialTheme.colors.onSurface
                ),
                height = 200.dp,
                selectorProperties = WheelPickerDefaults.selectorProperties(
                    borderColor = MaterialTheme.colors.outline.copy(0.3f)
                ),
                onTimeChangeListener = {
                    selectedDate = it.toDate(selectedDate)
                }
            )

            Separator()

            ActionButtons(
                close = {
                    coroutineScope.launch {
                        sheetState.hide()
                        close.invoke()
                    }
                },
                applyAction = {
                    AnalyticsManager.log(
                        DatePickerValueConfirmed(
                            fromScreen = fromScreen,
                            pickerKind = "time",
                        ),
                    )
                    coroutineScope.launch {
                        sheetState.hide()
                        applyDate(selectedDate)
                        close.invoke()
                    }
                }
            )
        }
    }
}

}
@Composable
private fun ActionButtons(
    close: () -> Unit,
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
            text = Localization.string("Cancel"),
            fullWidth = true,
            modifier = sectionModifier()
                .widthIn(max = 400.dp)
        ) {
            close.invoke()
        }

        PrimaryButton(
            text = Localization.string("Apply"),
            fullWidth = true,
            modifier = sectionModifier()
                .widthIn(max = 400.dp)
        ) {
            applyAction.invoke()
        }
    }
}
