package com.cryptium.coincurrently.features.CoinDetail.Sheets
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cryptium.coincurrently.sharedui.sheets.ModalBottomSheetSystemNavigationBar
import com.cryptium.coincurrently.sharedui.sheets.ModalSheetBody
import com.cryptium.coincurrently.sharedui.buttons.SecondaryButton
import com.cryptium.coincurrently.sharedui.components.Separator
import com.cryptium.coincurrently.sharedui.theme.onSurfaceDim
import com.cryptium.coincurrently.sharedui.sheets.rememberModalSheetMaxBodyHeight
import com.cryptium.coincurrently.sharedui.theme.surfaceContainerLow
import com.cryptium.coincurrently.core.extensions.satoshiRegular
import com.cryptium.coincurrently.sharedui.components.DragHandle
import com.cryptium.coincurrently.features.CoinDetail.Views.General.ReplaceWidgetListView
import com.cryptium.coincurrently.sharedui.sheets.modalSheetNavigationBarExtensionHeight
import kotlinx.coroutines.launch
import com.cryptium.coincurrently.core.networking.ResponseModels.Toplist

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditWidgetSheet(
    widgetCoins: List<Toplist>,
    currentCoinName: String?,
    replaceCoin: (Toplist) -> Unit,
    close: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val density = LocalDensity.current
    val maxBodyHeight = rememberModalSheetMaxBodyHeight(
        dragHandleTitle = "Replace Coin",
        dragHandleBottomPadding = 16.dp
    )
    val navBarExtensionHeight = context.modalSheetNavigationBarExtensionHeight(density)
    val availableBodyHeight = (maxBodyHeight - navBarExtensionHeight).coerceAtLeast(0.dp)
    var headerSectionHeight by remember { mutableStateOf(0.dp) }
    var bottomSectionHeight by remember { mutableStateOf(0.dp) }
    val scrollMaxHeight by remember(availableBodyHeight, headerSectionHeight, bottomSectionHeight) {
        derivedStateOf {
            (availableBodyHeight - headerSectionHeight - bottomSectionHeight).coerceAtLeast(0.dp)
        }
    }

    ModalBottomSheet(
        onDismissRequest = {
            coroutineScope.launch {
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
                title = "Replace Coin",
                bottomPadding = 16.dp,
                closeAction = {
                    coroutineScope.launch {
                        sheetState.hide()
                        close.invoke()
                    }
                })
        }) {
        ModalBottomSheetSystemNavigationBar()
        ModalSheetBody(
            context = context,
            maxBodyHeight = maxBodyHeight,
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Column(
                    modifier = Modifier.onGloballyPositioned { coordinates ->
                        headerSectionHeight = with(density) { coordinates.size.height.toDp() }
                    }
                ) {
                    BodyView(
                        currentCoinName = currentCoinName
                    )

                    Separator()
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .heightIn(max = scrollMaxHeight)
                        .verticalScroll(rememberScrollState())
                        .padding(vertical = 24.dp)
                ) {
                    WidgetsView(
                        widgetCoins = widgetCoins,
                        replaceCoin = {
                            coroutineScope.launch {
                                sheetState.hide()
                                replaceCoin.invoke(it)
                                close.invoke()
                            }
                        }
                    )
                }

                Column(
                    modifier = Modifier.onGloballyPositioned { coordinates ->
                        bottomSectionHeight = with(density) { coordinates.size.height.toDp() }
                    }
                ) {
                    Separator()

                    ActionButtons(
                        dismiss = {
                            coroutineScope.launch {
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

@Composable
private fun BodyView(
    currentCoinName: String?
) {
    Text(
        text = "Your widgets list is full (8/8).\nChoose a coin in your widgets to replace with ${currentCoinName ?: "coin"}.",
        style = satoshiRegular(16),
        color = MaterialTheme.colors.onSurfaceDim,
        lineHeight = 24.sp,
        modifier = Modifier.padding(bottom = 24.dp)
    )
}

@Composable
private fun ActionButtons(
    dismiss: () -> Unit
) {
    SecondaryButton(
        text = "Close",
        fullWidth = true,
        onClick = {
            dismiss.invoke()
        },
        modifier = Modifier
            .padding(
                top = 24.dp,
                bottom = 24.dp,
            )
    )
}

@Composable
private fun WidgetsView(
    widgetCoins: List<Toplist>,
    replaceCoin: (Toplist) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
    ) {
        widgetCoins.forEach { coin ->
            ReplaceWidgetListView(
                coin = coin,
                isSelected = false,
                itemClicked = {
                    replaceCoin(coin)
                }
            )
        }
    }
}
