package com.cryptium.coincurrently.widgets.config

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.cryptium.coincurrently.R
import com.cryptium.coincurrently.core.extensions.poppinsRegular
import com.cryptium.coincurrently.core.models.CarouselFilter
import com.cryptium.coincurrently.core.persistence.UserPreferences
import com.cryptium.coincurrently.sharedui.components.DragHandle
import com.cryptium.coincurrently.sharedui.components.Separator
import com.cryptium.coincurrently.sharedui.sheets.ModalBottomSheetSystemNavigationBar
import com.cryptium.coincurrently.sharedui.sheets.ModalSheetBody
import com.cryptium.coincurrently.sharedui.sheets.rememberModalSheetMaxBodyHeight
import com.cryptium.coincurrently.sharedui.theme.onSurfaceDim
import com.cryptium.coincurrently.sharedui.theme.surfaceContainerLow
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsCategoryPickerScreen(
    selectedConfig: NewsWidgetConfig? = null,
    onCategorySelected: (NewsWidgetConfig) -> Unit,
    onCancel: () -> Unit,
) {
    val hasPro = remember { UserPreferences.getHasBoughtPro() != null }
    var showingCoinPicker by remember { mutableStateOf(false) }
    var showProFallback by remember { mutableStateOf(false) }

    if (showingCoinPicker) {
        CoinSearchPickerScreen(
            title = "Select Coin",
            coinCount = 1,
            requiresPro = true,
            onCoinsSelected = { coins ->
                val symbol = coins.firstOrNull()?.symbol
                if (symbol.isNullOrEmpty()) {
                    onCategorySelected(NewsWidgetConfig(category = "general"))
                } else {
                    onCategorySelected(
                        CarouselFilter.Coins(symbol).toNewsWidgetConfig(coinSymbol = symbol),
                    )
                }
            },
            onCancel = { showingCoinPicker = false },
        )
        return
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val title = if (showProFallback) "Pro Required" else "Select Category"
    val maxBodyHeight = rememberModalSheetMaxBodyHeight(dragHandleTitle = title)

    val dismiss = {
        coroutineScope.launch {
            sheetState.hide()
            if (showProFallback) {
                showProFallback = false
            } else {
                onCancel()
            }
        }
    }

    BackHandler(onBack = { dismiss() })

    ModalBottomSheet(
        onDismissRequest = { dismiss() },
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        containerColor = MaterialTheme.colors.surfaceContainerLow,
        tonalElevation = 16.dp,
        contentWindowInsets = { androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0) },
        dragHandle = {
            DragHandle(
                title = title,
                closeAction = { dismiss() },
            )
        },
    ) {
        ModalBottomSheetSystemNavigationBar()
        ModalSheetBody(
            context = context,
            maxBodyHeight = maxBodyHeight,
            wrapContent = false,
        ) {
            if (showProFallback) {
                ProRequiredMessage(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    message = "This news category requires CoinCurrently Pro. Continue with General news instead.",
                    continueText = "Continue with General",
                    onContinue = { onCategorySelected(NewsWidgetConfig(category = "general")) },
                )
            } else {
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(CarouselFilter.newsFilters) { filter ->
                        val isLocked = filter.requiresPro && !hasPro
                        val isSelected = selectedConfig?.toCarouselFilter()?.matchesForSelection(filter) == true

                        CategoryMenuItem(
                            title = filter.rawValue,
                            isLocked = isLocked,
                            isSelected = isSelected,
                            onClick = {
                                when {
                                    isLocked -> showProFallback = true
                                    filter is CarouselFilter.Coins -> showingCoinPicker = true
                                    else -> onCategorySelected(filter.toNewsWidgetConfig())
                                }
                            },
                        )

                        Separator()
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryMenuItem(
    title: String,
    isLocked: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(
                if (isSelected) {
                    MaterialTheme.colors.primary.copy(alpha = 0.12f)
                } else {
                    MaterialTheme.colors.surfaceContainerLow
                },
            )
            .padding(horizontal = 16.dp, vertical = 16.dp),
    ) {
        Text(
            text = title,
            style = poppinsRegular(16),
            color = MaterialTheme.colors.onSurfaceDim,
            modifier = Modifier.weight(1f),
        )

        if (isLocked) {
            Image(
                painter = painterResource(id = R.drawable.baseline_lock_24),
                contentDescription = null,
                colorFilter = ColorFilter.tint(MaterialTheme.colors.onSurfaceDim),
                modifier = Modifier.size(24.dp),
            )
        }
    }
}
