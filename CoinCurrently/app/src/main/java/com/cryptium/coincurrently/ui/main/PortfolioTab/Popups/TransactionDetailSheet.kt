package com.cryptium.coincurrently.ui.main.PortfolioTab.Popups

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.invisibleToUser
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.cryptium.coincurrently.ui.main.Compose.AccessibilityLayout
import com.cryptium.coincurrently.ui.main.Compose.AdaptiveStack
import com.cryptium.coincurrently.ui.main.Compose.CachedUrlImage
import com.cryptium.coincurrently.R
import com.cryptium.coincurrently.ui.main.Compose.CloseButton
import com.cryptium.coincurrently.ui.main.Compose.DragHandler
import com.cryptium.coincurrently.ui.main.Compose.SecondaryButton
import com.cryptium.coincurrently.ui.main.Compose.Separator
import com.cryptium.coincurrently.ui.main.Compose.onSurfaceDim
import com.cryptium.coincurrently.ui.main.Compose.outline
import com.cryptium.coincurrently.ui.main.Compose.surfaceContainerLow
import com.cryptium.coincurrently.ui.main.Compose.trendDown
import com.cryptium.coincurrently.ui.main.Extensions.DateFormat
import com.cryptium.coincurrently.ui.main.Extensions.formatCurrency
import com.cryptium.coincurrently.ui.main.Extensions.formatDate
import com.cryptium.coincurrently.ui.main.Extensions.is24HourClock
import com.cryptium.coincurrently.ui.main.Extensions.satoshiBold
import com.cryptium.coincurrently.ui.main.Extensions.satoshiRegular
import com.cryptium.coincurrently.ui.main.Extensions.stars
import com.cryptium.coincurrently.ui.main.General.Enums.FormattingThreshold
import com.cryptium.coincurrently.ui.main.PortfolioTab.DataClasses.NewTransaction
import com.cryptium.coincurrently.ui.main.PortfolioTab.DataClasses.isAddOrInTransferTransaction
import com.cryptium.coincurrently.ui.main.PortfolioTab.DataClasses.totalSpent
import kotlinx.coroutines.launch
import networking.ResponseModels.CurrencySelectionModel
import networking.ResponseModels.Toplist
import com.cryptium.coincurrently.ui.main.Compose.ModalBottomSheetSystemNavigationBar
import com.cryptium.coincurrently.ui.main.Compose.rememberDragHandlerHeight
import com.cryptium.coincurrently.ui.main.Compose.rememberModalSheetMaxBodyHeight
import com.cryptium.coincurrently.ui.main.Compose.ModalSheetBody
import com.cryptium.coincurrently.ui.main.Compose.ModalSheetColumnLayout
import com.cryptium.coincurrently.ui.main.General.GlobalFunctions
import com.cryptium.coincurrently.ui.main.SettingsTab.Compose.allCurrencies
import java.util.Date

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun TransactionDetailSheet(
    coin: Toplist?,
    transaction: NewTransaction,
    hideData: Boolean,
    editTransaction: (NewTransaction) -> Unit,
    deleteTransaction: (NewTransaction) -> Unit,
    close: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val maxBodyHeight = rememberModalSheetMaxBodyHeight(rememberDragHandlerHeight(topPadding = 16.dp, bottomPadding = 12.dp))

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
        contentWindowInsets = { WindowInsets(0,0 ,0 ,0) },
        dragHandle = {
            DragHandler(
                modifier = Modifier
                    .padding(
                        top = 16.dp,
                        bottom = 12.dp
                    )
            )
        }
    ) {
        ModalBottomSheetSystemNavigationBar()
        ModalSheetBody(
            context = context,
            maxBodyHeight = maxBodyHeight,
        ) {
            val isHorizontal = AccessibilityLayout.isHorizontal
            val currency = allCurrencies.firstOrNull { it.code.lowercase() == transaction.currency.lowercase() }
                ?: GlobalFunctions.getSelectedCurrency()

            ModalSheetColumnLayout(
                maxBodyHeight = maxBodyHeight,
                header = {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    start = 16.dp,
                                    end = 16.dp,
                                    bottom = 24.dp,
                                )
                        ) {
                            HeaderView(
                                coin = coin,
                                transaction = transaction,
                                modifier = Modifier
                                    .weight(1f)
                            )

                            CloseButton(
                                onClick = {
                                    coroutineScope.launch {
                                        sheetState.hide()
                                        close.invoke()
                                    }
                                }
                            )
                        }

                        Separator(
                            padding = PaddingValues(horizontal = 16.dp)
                        )
                    }
                },
                footer = {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Separator(padding = PaddingValues(horizontal = 16.dp))

                        ActionButtons(
                            deleteAction = {
                                coroutineScope.launch {
                                    sheetState.hide()
                                    deleteTransaction.invoke(transaction)
                                }
                            },
                            editAction = {
                                coroutineScope.launch {
                                    sheetState.hide()
                                    editTransaction.invoke(transaction)
                                }
                            }
                        )
                    }
                },
                scrollContent = { scrollModifier ->
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = scrollModifier
                            .verticalScroll(rememberScrollState())
                            .padding(
                                top = 24.dp,
                                start = 16.dp,
                                end = 16.dp,
                                bottom = if (transaction.note.isEmpty()) 24.dp else 16.dp,
                            ),
                    ) {
                if (isHorizontal) {
                    StatRow(
                        left = { modifier ->
                            AmountView(
                                amount = transaction.amount,
                                hideData = hideData,
                                coinName = coin?.name ?: coin?.symbol ?: "",
                                modifier = modifier,
                            )
                        },
                        right = { modifier ->
                            UnitPriceView(
                                unitPrice = transaction.pricePerCoin,
                                hideData = hideData,
                                currency = currency,
                                modifier = modifier,
                            )
                        },
                    )

                    StatRow(
                        left = { modifier ->
                            FeesView(
                                fees = transaction.fee,
                                hideData = hideData,
                                currency = currency,
                                modifier = modifier,
                            )
                        },
                        right = { modifier ->
                            TotalCostView(
                                totalCost = transaction.totalSpent,
                                hideData = hideData,
                                currency = currency,
                                modifier = modifier,
                            )
                        },
                    )

                    StatRow(
                        left = { modifier ->
                            DateView(
                                date = transaction.date,
                                modifier = modifier,
                            )
                        },
                        right = { modifier ->
                            TimeView(
                                time = transaction.date,
                                modifier = modifier,
                            )
                        },
                    )
                } else {
                    AmountView(
                        amount = transaction.amount,
                        hideData = hideData,
                        coinName = coin?.name ?: coin?.symbol ?: "",
                        modifier = Modifier.fillMaxWidth(),
                    )
                    StatSeparator()
                    UnitPriceView(
                        unitPrice = transaction.pricePerCoin,
                        hideData = hideData,
                        currency = currency,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    StatSeparator()
                    FeesView(
                        fees = transaction.fee,
                        hideData = hideData,
                        currency = currency,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    StatSeparator()
                    TotalCostView(
                        totalCost = transaction.totalSpent,
                        hideData = hideData,
                        currency = currency,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    StatSeparator()
                    DateView(
                        date = transaction.date,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    StatSeparator()
                    TimeView(
                        time = transaction.date,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                if (transaction.note.isNotEmpty()) {
                    if (isHorizontal) {
                        Separator(padding = PaddingValues(top = 8.dp))
                    } else {
                        StatSeparator()
                    }

                    NotesView(note = transaction.note)
                }
                    }
                },
            )
        }
    }
}
@Composable
private fun StatRow(
    left: @Composable (Modifier) -> Unit,
    right: @Composable (Modifier) -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.Top,
        modifier = Modifier.fillMaxWidth(),
    ) {
        left(Modifier.weight(1f))
        right(Modifier.weight(1f))
    }
}

@Composable
private fun StatSeparator() {
    Separator(padding = PaddingValues(vertical = 8.dp))
}

@Composable
private fun HeaderView(
    coin: Toplist?,
    transaction: NewTransaction,
    modifier: Modifier
) {
    val coinName = coin?.name ?: coin?.symbol ?: ""

    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .semantics(mergeDescendants = true) {
                contentDescription = "${transaction.transactionType.transactionText} $coinName"
            }
    ) {
        CachedUrlImage(
            model = coin?.image,
            size = 24.dp,
            modifier = Modifier.semantics { invisibleToUser() },
        )

        Text(
            text = "${transaction.transactionType.transactionText} ${coin?.symbol?.uppercase() ?: "0000"}",
            style = satoshiBold(22),
            color = MaterialTheme.colors.onSurfaceDim,
            modifier = Modifier.semantics { invisibleToUser() },
        )
    }
}

@Composable
private fun StatView(
    title: String,
    value: String,
    valueAccessible: String? = null,
    modifier: Modifier = Modifier,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = if (valueAccessible != null) {
            modifier.semantics(mergeDescendants = true) {
                contentDescription = "$title: $valueAccessible"
            }
        } else {
            modifier.semantics(mergeDescendants = true) { }
        }
    ) {
        Text(
            text = title,
            style = satoshiBold(12),
            color = MaterialTheme.colors.onSurfaceDim,
            modifier = if (valueAccessible != null) {
                Modifier.semantics { invisibleToUser() }
            } else {
                Modifier
            }
        )

        Text(
            text = value,
            style = satoshiBold(16),
            color = MaterialTheme.colors.onSurface,
            modifier = if (valueAccessible != null) {
                Modifier.semantics { invisibleToUser() }
            } else {
                Modifier
            }
        )
    }
}

@Composable
private fun AmountView(
    amount: Double,
    hideData: Boolean,
    coinName: String,
    modifier: Modifier
) {
    StatView(
        title = "Amount",
        value = if (hideData) {
            stars
        } else {
            amount.formatCurrency(
                threshold = FormattingThreshold.MILLION,
                currency = CurrencySelectionModel.empty(),
                maxDecimals = 7,
            )
        },
        valueAccessible = if (hideData) {
            "hidden"
        } else {
            "${amount.formatCurrency(currency = CurrencySelectionModel.empty(), maxDecimals = 7, accessible = true)} $coinName"
        },
        modifier = modifier,
    )
}

@Composable
private fun UnitPriceView(
    unitPrice: Double?,
    hideData: Boolean,
    currency: CurrencySelectionModel,
    modifier: Modifier
) {
    StatView(
        title = "Unit Price",
        value = if (hideData) {
            stars
        } else {
            unitPrice?.formatCurrency(
                threshold = FormattingThreshold.MILLION,
                currency = currency,
            ) ?: "N/A"
        },
        valueAccessible = if (hideData) {
            "hidden"
        } else {
            unitPrice?.formatCurrency(
                threshold = FormattingThreshold.MILLION,
                currency = currency,
                accessible = true,
            ) ?: "Not available"
        },
        modifier = modifier,
    )
}

@Composable
private fun FeesView(
    fees: Double,
    hideData: Boolean,
    currency: CurrencySelectionModel,
    modifier: Modifier
) {
    StatView(
        title = "Fee",
        value = if (hideData) {
            stars
        } else {
            fees.formatCurrency(
                threshold = FormattingThreshold.MILLION,
                currency = currency,
            )
        },
        valueAccessible = if (hideData) {
            "hidden"
        } else {
            fees.formatCurrency(
                threshold = FormattingThreshold.MILLION,
                currency = currency,
                accessible = true,
            )
        },
        modifier = modifier,
    )
}

@Composable
private fun TotalCostView(
    totalCost: Double?,
    hideData: Boolean,
    currency: CurrencySelectionModel,
    modifier: Modifier
) {
    StatView(
        title = "Total Cost",
        value = if (hideData) {
            stars
        } else {
            totalCost?.formatCurrency(
                threshold = FormattingThreshold.MILLION,
                currency = currency,
            ) ?: "N/A"
        },
        valueAccessible = if (hideData) {
            "hidden"
        } else {
            totalCost?.formatCurrency(
                threshold = FormattingThreshold.MILLION,
                currency = currency,
                accessible = true,
            ) ?: "Not available"
        },
        modifier = modifier,
    )
}

@Composable
private fun DateView(
    date: Date,
    modifier: Modifier
) {
    StatView(
        title = "Date",
        value = date.formatDate(DateFormat.MMMddyyyy) ?: "",
        modifier = modifier,
    )
}

@Composable
private fun TimeView(
    time: Date,
    modifier: Modifier
) {
    val context = LocalContext.current

    StatView(
        title = "Time",
        value = time.formatDate(if (context.is24HourClock) DateFormat.HHmm else DateFormat.hhmma) ?: "",
        modifier = modifier,
    )
}

@Composable
private fun NotesView(
    note: String?
) {
    StatView(
        title = "Notes",
        value = note ?: "",
    )
}

@Composable
private fun ActionButtons(
    deleteAction: () -> Unit,
    editAction: () -> Unit
) {
    val isHorizontal = AccessibilityLayout.isHorizontal

    AdaptiveStack(
        isHorizontal = isHorizontal,
        spacing = 8.dp,
        horizontalAlignment = Alignment.Start,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                top = 24.dp,
                start = 16.dp,
                end = 16.dp,
                bottom = 24.dp,
            )
    ) {
        SecondaryButton(
            text = "Delete",
            icon = R.drawable.delete,
            fullWidth = true,
            onClick = {
                deleteAction.invoke()
            },
            modifier = sectionModifier()
        )

        SecondaryButton(
            text = "Edit",
            icon = R.drawable.edit,
            fullWidth = true,
            onClick = {
                editAction.invoke()
            },
            modifier = sectionModifier()
        )
    }
}

private fun canRemoveTransaction(
    transaction: NewTransaction,
    currentAmount: Double
): Boolean {
    return if (transaction.isAddOrInTransferTransaction) {
        currentAmount - transaction.amount >= 0
    } else {
        true
    }
}
