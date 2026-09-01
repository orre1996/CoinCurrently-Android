package com.cryptium.coincurrently.features.Portfolio.Sheets
import com.cryptium.coincurrently.sharedui.modifiers.imageSize

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cryptium.coincurrently.R
import com.cryptium.coincurrently.sharedui.buttons.CloseButton
import com.cryptium.coincurrently.sharedui.components.DragHandler
import com.cryptium.coincurrently.sharedui.components.Separator
import com.cryptium.coincurrently.sharedui.theme.onSurfaceDimmer
import com.cryptium.coincurrently.sharedui.theme.surfaceContainer
import com.cryptium.coincurrently.sharedui.theme.surfaceContainerLow
import com.cryptium.coincurrently.core.extensions.satoshiBold
import com.cryptium.coincurrently.sharedui.components.DragHandle
import com.cryptium.coincurrently.features.Portfolio.Enums.DateOption
import com.cryptium.coincurrently.features.Portfolio.Enums.DateOption.All
import com.cryptium.coincurrently.features.Portfolio.Enums.DateOption.Custom
import com.cryptium.coincurrently.features.Portfolio.Enums.DateOption.LastMonth
import com.cryptium.coincurrently.features.Portfolio.Enums.DateOption.LastSixMonths
import com.cryptium.coincurrently.features.Portfolio.Enums.DateOption.LastThreeMonths
import com.cryptium.coincurrently.features.Portfolio.Enums.DateOption.LastWeek
import com.cryptium.coincurrently.features.Portfolio.Enums.DateOption.LastYear
import com.cryptium.coincurrently.features.Portfolio.Enums.DateOption.YearToDate
import com.cryptium.coincurrently.features.Portfolio.Enums.title
import com.cryptium.coincurrently.features.Portfolio.Enums.type
import kotlinx.coroutines.launch
import com.cryptium.coincurrently.sharedui.sheets.ModalBottomSheetSystemNavigationBar
import com.cryptium.coincurrently.sharedui.sheets.rememberModalSheetMaxBodyHeight
import com.cryptium.coincurrently.sharedui.sheets.ModalSheetBody
import com.cryptium.coincurrently.core.analytics.DateFilterOptionSelected
import com.cryptium.coincurrently.core.analytics.AnalyticsManager
import com.cryptium.coincurrently.core.analytics.ScreenLabel
import java.util.Date

private val allCases: List<DateOption> = listOf(
    All,
    LastWeek,
    LastMonth,
    LastThreeMonths,
    LastSixMonths,
    LastYear,
    YearToDate,
    Custom(Date(), Date())
)

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun SelectDateSheet(
    fromScreen: ScreenLabel,
    selectedOption: MutableState<DateOption>,
    selectCustomDate: () -> Unit,
    close: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val maxBodyHeight = rememberModalSheetMaxBodyHeight(dragHandleTitle = "Filter By Dates")

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
                title = "Filter By Dates",
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
            Separator(
                padding = PaddingValues(
                    horizontal = 16.dp
                )
            )

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                contentPadding = PaddingValues(
                    vertical = 8.dp
                ),
                modifier = Modifier.fillMaxWidth(),
            ) {
                items(allCases) { option ->
                    DateOptionRow(
                        option = option,
                        isSelected = option.type == selectedOption.value.type,
                        isCustom = option is Custom,
                        didSelectOption = {
                            AnalyticsManager.log(
                                DateFilterOptionSelected(
                                    fromScreen = fromScreen,
                                    option = option.title,
                                ),
                            )
                            coroutineScope.launch {
                                sheetState.hide()

                                if (option is Custom) {
                                    selectCustomDate()
                                } else {
                                    selectedOption.value = option
                                }

                                close.invoke()
                            }
                        },
                        modifier = Modifier
                            .padding(
                                top = if (option == All) 24.dp else 0.dp,
                                bottom = if (option is Custom) 24.dp else 4.dp
                            )
                    )
                }
            }
        }
    }

}

@Composable
private fun DateOptionRow(
    option: DateOption,
    isSelected: Boolean,
    isCustom: Boolean,
    didSelectOption: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .background(
                color = if (isSelected) MaterialTheme.colors.surfaceContainer else Color.Transparent,
                shape = RoundedCornerShape(size = 16.dp)
            )
            .padding(vertical = 8.dp)
            .semantics(mergeDescendants = true) {
                val selectionState = if (isSelected) "selected" else "not selected"
                contentDescription = "${option.title}, $selectionState"
                onClick(label = "Select ${option.title}", action = {
                    didSelectOption.invoke()
                    true
                })
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
            ) {
                didSelectOption.invoke()
            }
    ) {
        Image(
            painter = painterResource(id = R.drawable.check_circle),
            contentDescription = null,
            colorFilter = ColorFilter.tint(MaterialTheme.colors.primary),
            modifier = Modifier
                .padding(12.dp)
                .imageSize(24.dp)
                .alpha(if (isSelected) 1f else 0f)
        )

        Text(
            text = option.title,
            style = satoshiBold(16),
            color = MaterialTheme.colors.onSurface,
            textAlign = TextAlign.Start,
            modifier = Modifier
                .weight(1f)
        )

        if (isCustom) {
            Image(
                painter = painterResource(id = R.drawable.chevron_right),
                contentDescription = null,
                colorFilter = ColorFilter.tint(MaterialTheme.colors.onSurfaceDimmer),
                modifier = Modifier
                    .padding(12.dp)
                    .imageSize(24.dp)
            )
        }
    }
}
