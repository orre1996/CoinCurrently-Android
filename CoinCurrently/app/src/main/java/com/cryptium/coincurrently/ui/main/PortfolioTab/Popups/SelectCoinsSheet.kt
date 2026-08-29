package com.cryptium.coincurrently.ui.main.PortfolioTab.Popups

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.invisibleToUser
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.cryptium.coincurrently.ui.main.Compose.AccessibilityLayout
import com.cryptium.coincurrently.ui.main.Compose.AdaptiveStack
import com.cryptium.coincurrently.ui.main.Compose.CachedUrlImage
import com.cryptium.coincurrently.ui.main.Compose.PrimaryButton
import com.cryptium.coincurrently.ui.main.Compose.SecondaryButton
import com.cryptium.coincurrently.ui.main.Compose.Separator
import com.cryptium.coincurrently.ui.main.Compose.onSurfaceDimmer
import com.cryptium.coincurrently.ui.main.Compose.surfaceContainerLow
import com.cryptium.coincurrently.ui.main.CryptoDetail.Compose.CheckboxView
import com.cryptium.coincurrently.ui.main.Extensions.satoshiBold
import com.cryptium.coincurrently.ui.main.General.DragHandle
import kotlinx.coroutines.launch
import com.cryptium.coincurrently.ui.main.Compose.ModalBottomSheetSystemNavigationBar
import com.cryptium.coincurrently.ui.main.Compose.rememberModalSheetMaxBodyHeight
import com.cryptium.coincurrently.ui.main.Compose.ModalSheetBody
import com.cryptium.coincurrently.ui.main.Compose.ModalSheetColumnLayout
import com.cryptium.coincurrently.ui.main.analytics.CoinFilterAdded
import com.cryptium.coincurrently.ui.main.analytics.CoinFilterCleared
import com.cryptium.coincurrently.ui.main.analytics.CoinFilterRemoved
import com.cryptium.coincurrently.ui.main.analytics.AnalyticsManager
import com.cryptium.coincurrently.ui.main.analytics.ScreenLabel
import networking.ResponseModels.Toplist

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun SelectCoinsSheet(
    fromScreen: ScreenLabel,
    coins: List<Toplist>,
    selectedCoins: MutableState<ArrayList<Toplist>>,
    close: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val maxBodyHeight = rememberModalSheetMaxBodyHeight(dragHandleTitle = "Filter By Coin")

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
        contentWindowInsets = { WindowInsets(0,0 ,0 ,0) },
        dragHandle = {
            DragHandle(
                title = "Filter By Coin",
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
                scrollItemCount = coins.size,
                modifier = Modifier.padding(horizontal = 16.dp),
                header = {
                    Separator()
                },
                footer = {
                    Separator()
                    ActionButtons(
                        clearCoins = {
                            if (selectedCoins.value.isNotEmpty()) {
                                AnalyticsManager.log(CoinFilterCleared(fromScreen))
                            }
                            selectedCoins.value =
                                ArrayList(selectedCoins.value).apply {
                                    clear()
                                }
                        },
                        applyAction = {
                            coroutineScope.launch {
                                sheetState.hide()
                                close.invoke()
                            }
                        }
                    )
                },
                scrollContent = { scrollModifier ->
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        contentPadding = PaddingValues(vertical = 24.dp),
                        modifier = scrollModifier,
                    ) {
                        items(coins) { coin ->
                            CoinOptionRow(
                                coin = coin,
                                isSelected = selectedCoins.value.any { it.id == coin.id },
                                didSelectOption = {
                                    val wasSelected = selectedCoins.value.any { it.id == coin.id }
                                    selectedCoins.value =
                                        ArrayList(selectedCoins.value).apply {
                                            if (any { it.id == coin.id }) {
                                                removeAll { it.id == coin.id }
                                            } else {
                                                add(coin)
                                            }
                                        }
                                    val coinName = coin.name ?: coin.symbol ?: ""
                                    if (wasSelected) {
                                        AnalyticsManager.log(
                                            CoinFilterRemoved(fromScreen, coinName),
                                        )
                                    } else {
                                        AnalyticsManager.log(
                                            CoinFilterAdded(fromScreen, coinName),
                                        )
                                    }
                                }
                            )
                        }
                    }
                },
            )
        }
    }
}
@Composable
private fun CoinOptionRow(
    coin: Toplist,
    isSelected: Boolean,
    didSelectOption: () -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(vertical = 16.dp)
            .semantics(mergeDescendants = true) {
                val selectionState = if (isSelected) "selected" else "not selected"
                contentDescription = "${coin.name ?: coin.symbol ?: ""}, $selectionState"
                onClick(label = "Toggle ${coin.name ?: coin.symbol ?: ""} filter", action = {
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
        CachedUrlImage(
            model = coin.image,
            size = 32.dp,
            modifier = Modifier.semantics { invisibleToUser() },
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .semantics { invisibleToUser() }
        ) {
            Text(
                text = coin.symbol?.uppercase() ?: "",
                style = satoshiBold(16),
                color = MaterialTheme.colors.onSurface
            )

            Text(
                text = coin.name ?: "",
                style = satoshiBold(12),
                color = MaterialTheme.colors.onSurfaceDimmer
            )
        }

        CheckboxView(
            trailingText = "",
            isChecked = isSelected,
            modifier = Modifier.semantics { invisibleToUser() },
            isInteractive = false,
            onToggle = { }
        )
    }
}

@Composable
private fun ActionButtons(
    clearCoins: () -> Unit,
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
            clearCoins.invoke()
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
