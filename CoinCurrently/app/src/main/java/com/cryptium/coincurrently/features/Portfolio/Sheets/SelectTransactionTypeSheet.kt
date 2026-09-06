package com.cryptium.coincurrently.features.Portfolio.Sheets
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.cryptium.coincurrently.R
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
import kotlinx.coroutines.launch

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun SelectTransactionTypeSheet(
    transactionSelected: (PortfolioTransactionType?) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val maxBodyHeight = rememberModalSheetMaxBodyHeight(dragHandleTitle = "Select Transaction Type")

    ModalBottomSheet(
        onDismissRequest = {
            coroutineScope.launch {
                transactionSelected.invoke(null)
            }
        },
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        containerColor = MaterialTheme.colors.surfaceContainerLow,
        tonalElevation = 16.dp,
        contentWindowInsets = { WindowInsets(0, 0, 0, 0) },
        dragHandle = {
            DragHandle(
                title = "Select Transaction Type",
                closeAction = {
                    coroutineScope.launch {
                        sheetState.hide()
                        transactionSelected.invoke(null)
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
            modifier = Modifier
                    .padding(horizontal = 16.dp)
            ) {

            Separator()

            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 24.dp)
            ) {
                TransactionTypeView(
                    title = "Buy",
                    subtitle = "Bought coins with cash.",
                    image = R.drawable.plus,
                    viewTapped = {
                        coroutineScope.launch {
                            sheetState.hide()
                            transactionSelected.invoke(PortfolioTransactionType.Buy)
                        }
                    }
                )

                TransactionTypeView(
                    title = "Sell",
                    subtitle = "Sold coins with cash.",
                    image = R.drawable.minus,
                    viewTapped = {
                        coroutineScope.launch {
                            sheetState.hide()
                            transactionSelected.invoke(PortfolioTransactionType.Sell)
                        }
                    }
                )

                TransactionTypeView(
                    title = "Swap",
                    subtitle = "Swapped one coin for another.",
                    image = R.drawable.trade,
                    viewTapped = {
                        coroutineScope.launch {
                            sheetState.hide()
                            transactionSelected.invoke(PortfolioTransactionType.Swap)
                        }
                    }
                )

                TransactionTypeView(
                    title = "Deposit",
                    subtitle = "Deposited coins into your wallet.",
                    image = R.drawable.deposit,
                    viewTapped = {
                        coroutineScope.launch {
                            sheetState.hide()
                            transactionSelected.invoke(PortfolioTransactionType.TransferIn)
                        }
                    }
                )

                TransactionTypeView(
                    title = "Withdraw",
                    subtitle = "Withdrawn coins out of your wallet.",
                    image = R.drawable.withdraw,
                    viewTapped = {
                        coroutineScope.launch {
                            sheetState.hide()
                            transactionSelected.invoke(PortfolioTransactionType.TransferOut)
                        }
                    },
                )
            }
        }
    }
}
}
