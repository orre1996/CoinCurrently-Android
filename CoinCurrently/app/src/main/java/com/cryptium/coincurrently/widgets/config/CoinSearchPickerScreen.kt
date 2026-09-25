package com.cryptium.coincurrently.widgets.config

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.cryptium.coincurrently.app.SearchCase
import com.cryptium.coincurrently.app.SearchScreenCase
import com.cryptium.coincurrently.core.analytics.ScreenLabel
import com.cryptium.coincurrently.core.extensions.poppinsRegular
import com.cryptium.coincurrently.core.networking.ResponseModels.Toplist
import com.cryptium.coincurrently.features.CoinDetail.Sheets.SelectCoinBaseView
import com.cryptium.coincurrently.sharedui.components.DragHandle
import com.cryptium.coincurrently.sharedui.sheets.ModalBottomSheetSystemNavigationBar
import com.cryptium.coincurrently.sharedui.sheets.ModalSheetBody
import com.cryptium.coincurrently.sharedui.sheets.rememberModalSheetMaxBodyHeight
import com.cryptium.coincurrently.sharedui.theme.onSurfaceDim
import com.cryptium.coincurrently.sharedui.theme.surfaceContainerLow
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoinSearchPickerScreen(
    title: String = "Select Coin",
    coinCount: Int = 1,
    initialSelectedIds: List<String> = emptyList(),
    onCoinsSelected: (List<Toplist>) -> Unit,
    onCancel: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val maxBodyHeight = rememberModalSheetMaxBodyHeight(dragHandleTitle = title)

    val selectedCoins = remember(initialSelectedIds, coinCount) {
        mutableStateListOf<Toplist>().apply {
            addAll(initialSelectedIds.filter { it.isNotEmpty() }.take(coinCount).map { Toplist(id = it) })
        }
    }

    val dismiss = {
        coroutineScope.launch {
            sheetState.hide()
            onCancel()
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
            when {
                coinCount > 1 -> {
                    Text(
                        text = "Select $coinCount coins (${selectedCoins.size}/$coinCount)",
                        style = poppinsRegular(14),
                        color = MaterialTheme.colors.onSurfaceDim,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    )

                    SelectCoinBaseView(
                        screenCase = SearchScreenCase.Search(SearchCase.OnlyCoins),
                        fromScreen = ScreenLabel.Widgets,
                        highlightId = null,
                        highlightIds = selectedCoins.mapNotNull { it.id }.toSet(),
                        hideId = null,
                        modifier = Modifier.weight(1f),
                        close = { coin ->
                            val existingIndex = selectedCoins.indexOfFirst { it.id == coin.id }
                            if (existingIndex >= 0) {
                                selectedCoins.removeAt(existingIndex)
                            } else if (selectedCoins.size < coinCount) {
                                selectedCoins.add(coin)
                                if (selectedCoins.size == coinCount) {
                                    onCoinsSelected(selectedCoins.toList())
                                }
                            }
                        },
                    )
                }
                else -> {
                    SelectCoinBaseView(
                        screenCase = SearchScreenCase.Search(SearchCase.OnlyCoins),
                        fromScreen = ScreenLabel.Widgets,
                        highlightId = initialSelectedIds.firstOrNull(),
                        hideId = null,
                        modifier = Modifier.weight(1f),
                        close = { coin ->
                            onCoinsSelected(listOf(coin))
                        },
                    )
                }
            }
        }
    }
}
