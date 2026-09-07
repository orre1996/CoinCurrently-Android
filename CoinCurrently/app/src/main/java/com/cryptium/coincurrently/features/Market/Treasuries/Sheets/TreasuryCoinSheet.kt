package com.cryptium.coincurrently.features.Market.Treasuries.Sheets
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.cryptium.coincurrently.sharedui.sheets.ModalBottomSheetSystemNavigationBar
import com.cryptium.coincurrently.sharedui.sheets.ModalSheetBody
import com.cryptium.coincurrently.sharedui.components.Separator
import com.cryptium.coincurrently.sharedui.sheets.rememberModalSheetMaxBodyHeight
import com.cryptium.coincurrently.sharedui.theme.surfaceContainerLow
import com.cryptium.coincurrently.sharedui.components.ImageCase
import com.cryptium.coincurrently.sharedui.components.SingleSelectView
import com.cryptium.coincurrently.sharedui.components.DragHandle
import kotlinx.coroutines.launch
import com.cryptium.coincurrently.core.networking.ResponseModels.Toplist

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun TreasuryCoinSheet(
    coins: List<Toplist>,
    selectedCoinId: String,
    newCoinSelected: (Toplist) -> Unit,
    close: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val maxBodyHeight = rememberModalSheetMaxBodyHeight(dragHandleTitle = "Select Coin")

    ModalBottomSheet(
        onDismissRequest = {
            coroutineScope.launch {
                sheetState.hide()
                close()
            }
        },
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        containerColor = MaterialTheme.colors.surfaceContainerLow,
        tonalElevation = 16.dp,
        contentWindowInsets = { WindowInsets(0, 0, 0, 0) },
        dragHandle = {
            DragHandle(
                title = "Select Coin",
                closeAction = {
                    coroutineScope.launch {
                        sheetState.hide()
                        close()
                    }
                },
            )
        },
    ) {
        ModalBottomSheetSystemNavigationBar()
        ModalSheetBody(
            context = context,
            maxBodyHeight = maxBodyHeight,
        ) {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                contentPadding = PaddingValues(vertical = 8.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                item { Separator() }
                item { Spacer(modifier = Modifier.height(24.dp)) }

                items(coins, key = { it.id ?: it.name ?: it.hashCode() }) { coin ->
                    SingleSelectView(
                        title = coin.name ?: "",
                        accessibilityTitle = coin.name,
                        subtitle = coin.symbol?.uppercase(),
                        imageCase = ImageCase.Image(coin.image),
                        isSelected = coin.id == selectedCoinId,
                        didSelectOption = {
                            coroutineScope.launch {
                                newCoinSelected(coin)
                                sheetState.hide()
                                close()
                            }
                        },
                    )
                }

                item { Spacer(modifier = Modifier.height(24.dp)) }
            }
        }
    }
}
