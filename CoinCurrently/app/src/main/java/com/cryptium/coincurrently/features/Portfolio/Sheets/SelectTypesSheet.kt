package com.cryptium.coincurrently.features.Portfolio.Sheets
import com.cryptium.coincurrently.core.localization.Localization
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.cryptium.coincurrently.R
import com.cryptium.coincurrently.sharedui.components.AccessibilityLayout
import com.cryptium.coincurrently.sharedui.components.AdaptiveStack
import com.cryptium.coincurrently.sharedui.buttons.PrimaryButton
import com.cryptium.coincurrently.sharedui.buttons.SecondaryButton
import com.cryptium.coincurrently.sharedui.components.Separator
import com.cryptium.coincurrently.sharedui.theme.onSurfaceDim
import com.cryptium.coincurrently.sharedui.theme.outline
import com.cryptium.coincurrently.sharedui.theme.surfaceContainerLow
import com.cryptium.coincurrently.core.extensions.satoshiBold
import com.cryptium.coincurrently.sharedui.components.DragHandle
import com.cryptium.coincurrently.features.Portfolio.Views.TransactionTypeView
import com.cryptium.coincurrently.features.Portfolio.Enums.PortfolioTransactionType
import com.cryptium.coincurrently.sharedui.sheets.ModalBottomSheetSystemNavigationBar
import com.cryptium.coincurrently.sharedui.sheets.rememberModalSheetMaxBodyHeight
import com.cryptium.coincurrently.sharedui.sheets.ModalSheetBody
import com.cryptium.coincurrently.sharedui.sheets.ModalSheetColumnLayout
import com.cryptium.coincurrently.core.analytics.AnalyticsManager
import com.cryptium.coincurrently.core.analytics.ScreenLabel
import com.cryptium.coincurrently.core.analytics.TransactionTypeFilterAdded
import com.cryptium.coincurrently.core.analytics.TransactionTypeFilterCleared
import com.cryptium.coincurrently.core.analytics.TransactionTypeFilterRemoved
import kotlinx.coroutines.launch

private fun toggleTransactionType(
    transactionTypes: MutableState<ArrayList<PortfolioTransactionType>>,
    fromScreen: ScreenLabel,
    type: PortfolioTransactionType,
) {
    val current = transactionTypes.value
    val wasSelected = current.contains(type)
    transactionTypes.value = ArrayList(current).apply {
        if (wasSelected) {
            remove(type)
            AnalyticsManager.log(
                TransactionTypeFilterRemoved(
                    fromScreen = fromScreen,
                    transactionType = type.value,
                ),
            )
        } else {
            add(type)
            AnalyticsManager.log(
                TransactionTypeFilterAdded(
                    fromScreen = fromScreen,
                    transactionType = type.value,
                ),
            )
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun SelectTypesSheet(
    fromScreen: ScreenLabel,
    transactionTypes: MutableState<ArrayList<PortfolioTransactionType>>,
    close: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val maxBodyHeight = rememberModalSheetMaxBodyHeight(dragHandleTitle = "Filter By Type")

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
                title = "Filter By Type",
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
                scrollItemCount = 6,
                modifier = Modifier.padding(horizontal = 16.dp),
                header = {
                    Separator()
                },
                footer = {
                    Separator()
                    ActionButtons(
                        clearTransactionTypes = {
                            if (transactionTypes.value.isNotEmpty()) {
                                AnalyticsManager.log(TransactionTypeFilterCleared(fromScreen))
                            }
                            transactionTypes.value =
                                ArrayList(transactionTypes.value).apply {
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
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = scrollModifier
                            .verticalScroll(rememberScrollState())
                            .padding(vertical = 24.dp),
                    ) {
                TransactionTypeView(
                    title = Localization.string("Import Wallet"),
                    subtitle = Localization.string("Imported a crypto wallet."),
                    image = R.drawable.walletimport,
                    isMultiSelectEnabled = true,
                    isSelected = transactionTypes.value.contains(PortfolioTransactionType.ImportAddress),
                    viewTapped = {
                        toggleTransactionType(transactionTypes, fromScreen, PortfolioTransactionType.ImportAddress)
                    },
                )

                TransactionTypeView(
                    title = Localization.string("Buy"),
                    subtitle = Localization.string("Bought coins with cash."),
                    image = R.drawable.plus,
                    isMultiSelectEnabled = true,
                    isSelected = transactionTypes.value.contains(PortfolioTransactionType.Buy),
                    viewTapped = {
                        toggleTransactionType(transactionTypes, fromScreen, PortfolioTransactionType.Buy)
                    }
                )

                TransactionTypeView(
                    title = Localization.string("Sell"),
                    subtitle = Localization.string("Sold coins with cash."),
                    image = R.drawable.minus,
                    isMultiSelectEnabled = true,
                    isSelected = transactionTypes.value.contains(PortfolioTransactionType.Sell),
                    viewTapped = {
                        toggleTransactionType(transactionTypes, fromScreen, PortfolioTransactionType.Sell)
                    },
                )

                TransactionTypeView(
                    title = Localization.string("Swap"),
                    subtitle = Localization.string("Swapped one coin for another."),
                    image = R.drawable.trade,
                    isMultiSelectEnabled = true,
                    isSelected = transactionTypes.value.contains(PortfolioTransactionType.Swap),
                    viewTapped = {
                        toggleTransactionType(transactionTypes, fromScreen, PortfolioTransactionType.Swap)
                    },
                )

                TransactionTypeView(
                    title = Localization.string("Deposit"),
                    subtitle = Localization.string("Deposited coins into your wallet."),
                    image = R.drawable.deposit,
                    isMultiSelectEnabled = true,
                    isSelected = transactionTypes.value.contains(PortfolioTransactionType.TransferIn),
                    viewTapped = {
                        toggleTransactionType(transactionTypes, fromScreen, PortfolioTransactionType.TransferIn)
                    }
                )

                TransactionTypeView(
                    title = Localization.string("Withdraw"),
                    subtitle = Localization.string("Withdrawn coins out of your wallet."),
                    image = R.drawable.withdraw,
                    isMultiSelectEnabled = true,
                    isSelected = transactionTypes.value.contains(PortfolioTransactionType.TransferOut),
                    viewTapped = {
                        toggleTransactionType(transactionTypes, fromScreen, PortfolioTransactionType.TransferOut)
                    },
                )
                    }
                },
            )
        }
    }
}
@Composable
private fun ActionButtons(
    clearTransactionTypes: () -> Unit,
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
                bottom = 24.dp,
            )
    ) {
        SecondaryButton(
            text = Localization.string("Clear"),
            accessibilityLabel = Localization.string("Clear filters"),
            fullWidth = true,
            modifier = sectionModifier()
                .widthIn(max = 400.dp),
            onClick = {
                clearTransactionTypes.invoke()
            }
        )

        PrimaryButton(
            text = Localization.string("Apply"),
            accessibilityLabel = Localization.string("Apply filters"),
            fullWidth = true,
            modifier = sectionModifier()
                .widthIn(max = 400.dp),
            onClick = {
                applyAction.invoke()
            }
        )
    }
}
