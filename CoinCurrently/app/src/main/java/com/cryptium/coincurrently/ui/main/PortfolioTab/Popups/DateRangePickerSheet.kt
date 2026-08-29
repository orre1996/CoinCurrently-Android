package com.cryptium.coincurrently.ui.main.PortfolioTab.Popups

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
import com.cryptium.coincurrently.ui.main.Compose.AccessibilityLayout
import com.cryptium.coincurrently.ui.main.Compose.AdaptiveStack
import com.cryptium.coincurrently.ui.main.Compose.CloseButton
import com.cryptium.coincurrently.ui.main.Compose.DragHandler
import com.cryptium.coincurrently.ui.main.Compose.PrimaryButton
import com.cryptium.coincurrently.ui.main.Compose.SecondaryButton
import com.cryptium.coincurrently.ui.main.Compose.Separator
import com.cryptium.coincurrently.ui.main.Compose.outline
import com.cryptium.coincurrently.ui.main.Compose.surfaceContainerLow
import com.cryptium.coincurrently.ui.main.Extensions.satoshiMedium
import com.cryptium.coincurrently.ui.main.Extensions.toDate
import com.cryptium.coincurrently.ui.main.Extensions.toLocalDate
import com.cryptium.coincurrently.ui.main.General.DragHandle
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toJavaLocalDate
import network.chaintech.kmp_date_time_picker.ui.datepicker.WheelDatePickerComponent.WheelDatePicker
import network.chaintech.kmp_date_time_picker.utils.WheelPickerDefaults
import network.chaintech.kmp_date_time_picker.utils.now
import com.cryptium.coincurrently.ui.main.Compose.ModalBottomSheetSystemNavigationBar
import com.cryptium.coincurrently.ui.main.Compose.rememberModalSheetMaxBodyHeight
import com.cryptium.coincurrently.ui.main.Compose.ModalSheetBody
import com.cryptium.coincurrently.ui.main.analytics.DatePickerValueConfirmed
import com.cryptium.coincurrently.ui.main.analytics.AnalyticsManager
import com.cryptium.coincurrently.ui.main.analytics.ScreenLabel
import java.util.Date


@Composable
@OptIn(ExperimentalStdlibApi::class, ExperimentalMaterial3Api::class)
fun DateRangePickerSheet(
    fromScreen: ScreenLabel,
    startDate: Date,
    close: () -> Unit,
    applyDate: (Date) -> Unit,
    pickerKind: String = "date",
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()

    var selectedDate by remember { mutableStateOf(startDate) }
    val context = LocalContext.current
    val maxBodyHeight = rememberModalSheetMaxBodyHeight(dragHandleTitle = "Select Date")

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
                title = "Select Date",
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

            WheelDatePicker(
                hideHeader = true,
                rowCount = 5,
                startDate = run {
                    val javaDate = startDate.toLocalDate()
                    LocalDate(javaDate.year, javaDate.monthValue, javaDate.dayOfMonth)
                },
                minDate = LocalDate(2009,1,1),
                maxDate = LocalDate.now(),
                yearsRange = IntRange(2009, LocalDate.now().year),
                selectedDateTextStyle = satoshiMedium(20).copy(
                    color = MaterialTheme.colors.onSurface
                ),
                defaultDateTextStyle = satoshiMedium(20).copy(
                    color = MaterialTheme.colors.onSurface
                ),
                height = 200.dp,
                selectorProperties = WheelPickerDefaults.selectorProperties(
                    borderColor = MaterialTheme.colors.outline.copy(0.3f)
                ),
                onDateChangeListener = {
                    selectedDate = it.toJavaLocalDate().toDate(0, 0)
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
                            pickerKind = pickerKind,
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
            text = "Cancel",
            accessibilityLabel = "Cancel, close window",
            fullWidth = true,
            modifier = sectionModifier()
                .widthIn(max = 400.dp)
        ) {
            close.invoke()
        }

        PrimaryButton(
            text = "Apply",
            accessibilityLabel = "Apply filters",
            fullWidth = true,
            modifier = sectionModifier()
                .widthIn(max = 400.dp)
        ) {
            applyAction.invoke()
        }
    }
}
