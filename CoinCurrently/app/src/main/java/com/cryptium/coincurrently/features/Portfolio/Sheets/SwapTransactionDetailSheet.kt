package com.cryptium.coincurrently.features.Portfolio.Sheets
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
import com.cryptium.coincurrently.sharedui.components.AccessibilityLayout
import com.cryptium.coincurrently.sharedui.components.AdaptiveStack
import com.cryptium.coincurrently.sharedui.components.CachedUrlImage
import com.cryptium.coincurrently.R
import com.cryptium.coincurrently.sharedui.buttons.CloseButton
import com.cryptium.coincurrently.sharedui.components.DragHandler
import com.cryptium.coincurrently.sharedui.buttons.SecondaryButton
import com.cryptium.coincurrently.sharedui.components.Separator
import com.cryptium.coincurrently.sharedui.theme.onSurfaceDim
import com.cryptium.coincurrently.sharedui.theme.outline
import com.cryptium.coincurrently.sharedui.theme.primaryTextColor
import com.cryptium.coincurrently.sharedui.theme.surfaceContainerLow
import com.cryptium.coincurrently.sharedui.theme.trendDown
import com.cryptium.coincurrently.features.Portfolio.Models.NewSwapTransaction
import com.cryptium.coincurrently.core.extensions.DateFormat
import com.cryptium.coincurrently.core.extensions.formatCurrency
import com.cryptium.coincurrently.core.extensions.formatDate
import com.cryptium.coincurrently.core.extensions.is24HourClock
import com.cryptium.coincurrently.core.extensions.satoshiBold
import com.cryptium.coincurrently.core.extensions.stars
import com.cryptium.coincurrently.core.extensions.symbolAccessible
import com.cryptium.coincurrently.sharedui.models.FormattingThreshold
import com.cryptium.coincurrently.features.Portfolio.Views.accessibleCoinAmountWithSymbol
import kotlinx.coroutines.launch
import com.cryptium.coincurrently.core.networking.ResponseModels.CurrencySelectionModel
import com.cryptium.coincurrently.core.networking.ResponseModels.Toplist
import com.cryptium.coincurrently.sharedui.sheets.ModalBottomSheetSystemNavigationBar
import com.cryptium.coincurrently.sharedui.components.rememberDragHandlerHeight
import com.cryptium.coincurrently.sharedui.sheets.rememberModalSheetMaxBodyHeight
import com.cryptium.coincurrently.sharedui.sheets.ModalSheetBody
import com.cryptium.coincurrently.sharedui.sheets.ModalSheetColumnLayout
import com.cryptium.coincurrently.sharedui.components.GlobalFunctions
import com.cryptium.coincurrently.core.persistence.allCurrencies
import java.util.Date
import kotlin.text.lowercase

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun SwapTransactionDetailSheet(
    coin: Toplist?,
    transaction: NewSwapTransaction,
    hideData: Boolean,
    editTransaction: (NewSwapTransaction) -> Unit,
    deleteTransaction: (NewSwapTransaction) -> Unit,
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
            val currency = allCurrencies.firstOrNull { it.code.lowercase() == transaction.selectedCurrency.lowercase() }
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
                            SwappedFromView(
                                amount = transaction.fromAmount,
                                symbol = transaction.fromCurrencySymbol,
                                hideData = hideData,
                                modifier = modifier,
                            )
                        },
                        right = { modifier ->
                            SwappedToView(
                                amount = transaction.toAmount,
                                symbol = transaction.toCurrencySymbol,
                                hideData = hideData,
                                modifier = modifier,
                            )
                        },
                    )

                    StatRow(
                        left = { modifier ->
                            FromUnitPriceView(
                                unitPrice = transaction.fromUnitPrice,
                                symbol = transaction.fromCurrencySymbol,
                                hideData = hideData,
                                currency = currency,
                                modifier = modifier,
                            )
                        },
                        right = { modifier ->
                            ToUnitPriceView(
                                unitPrice = transaction.toUnitPrice,
                                symbol = transaction.toCurrencySymbol,
                                hideData = hideData,
                                currency = currency,
                                modifier = modifier,
                            )
                        },
                    )

                    StatRow(
                        left = { modifier ->
                            RateView(
                                fromSymbol = transaction.fromCurrencySymbol,
                                toSymbol = transaction.toCurrencySymbol,
                                toRate = transaction.toAmount / transaction.fromAmount,
                                hideData = hideData,
                                modifier = modifier,
                            )
                        },
                        right = { modifier ->
                            FeesView(
                                fees = transaction.fee,
                                hideData = hideData,
                                currency = currency,
                                modifier = modifier,
                            )
                        },
                    )

                    StatRow(
                        left = { modifier ->
                            SwappedValueView(
                                fromAmount = transaction.fromAmount,
                                fromUnitPrice = transaction.fromUnitPrice,
                                hideData = hideData,
                                currency = currency,
                                modifier = modifier,
                            )
                        },
                        right = { modifier ->
                            DateView(
                                date = transaction.date,
                                modifier = modifier,
                            )
                        },
                    )

                    TimeView(
                        time = transaction.date,
                        modifier = Modifier.fillMaxWidth(),
                    )
                } else {
                    SwappedFromView(
                        amount = transaction.fromAmount,
                        symbol = transaction.fromCurrencySymbol,
                        hideData = hideData,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    StatSeparator()
                    SwappedToView(
                        amount = transaction.toAmount,
                        symbol = transaction.toCurrencySymbol,
                        hideData = hideData,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    StatSeparator()
                    FromUnitPriceView(
                        unitPrice = transaction.fromUnitPrice,
                        symbol = transaction.fromCurrencySymbol,
                        hideData = hideData,
                        currency = currency,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    StatSeparator()
                    ToUnitPriceView(
                        unitPrice = transaction.toUnitPrice,
                        symbol = transaction.toCurrencySymbol,
                        hideData = hideData,
                        currency = currency,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    StatSeparator()
                    RateView(
                        fromSymbol = transaction.fromCurrencySymbol,
                        toSymbol = transaction.toCurrencySymbol,
                        toRate = transaction.toAmount / transaction.fromAmount,
                        hideData = hideData,
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
                    SwappedValueView(
                        fromAmount = transaction.fromAmount,
                        fromUnitPrice = transaction.fromUnitPrice,
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
private fun StatView(
    title: String,
    value: String,
    valueAccessible: String? = null,
    titleAccessible: String? = null,
    modifier: Modifier = Modifier,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = if (valueAccessible != null) {
            modifier.semantics(mergeDescendants = true) {
                contentDescription = "${titleAccessible ?: title}: $valueAccessible"
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
private fun HeaderView(
    coin: Toplist?,
    transaction: NewSwapTransaction,
    modifier: Modifier
) {
    val title = "Swapped ${transaction.fromCurrencySymbol.uppercase()} for ${transaction.toCurrencySymbol.uppercase()}"
    val accessibleTitle = "Swapped ${transaction.fromCurrencySymbol.symbolAccessible} for ${transaction.toCurrencySymbol.symbolAccessible}"

    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .semantics(mergeDescendants = true) {
                contentDescription = accessibleTitle
            }
    ) {
        CachedUrlImage(
            model = coin?.image,
            size = 24.dp,
            modifier = Modifier.semantics { invisibleToUser() },
        )

        Text(
            text = title,
            style = satoshiBold(22),
            color = MaterialTheme.colors.onSurfaceDim,
            modifier = Modifier.semantics { invisibleToUser() },
        )
    }
}


@Composable
private fun SwappedFromView(
    amount: Double,
    symbol: String,
    hideData: Boolean,
    modifier: Modifier
) {
    StatView(
        title = "Swapped From",
        value = if (hideData) {
            stars
        } else {
            "${amount.formatCurrency(
                threshold = FormattingThreshold.MILLION,
                currency = CurrencySelectionModel.empty(),
                maxDecimals = 7,
            )}${symbol.uppercase()}"
        },
        valueAccessible = if (hideData) {
            "hidden"
        } else {
            accessibleCoinAmountWithSymbol(amount, symbol)
        },
        modifier = modifier,
    )
}

@Composable
private fun SwappedToView(
    amount: Double,
    symbol: String,
    hideData: Boolean,
    modifier: Modifier
) {
    StatView(
        title = "Swapped To",
        value = if (hideData) {
            stars
        } else {
            "${amount.formatCurrency(
                threshold = FormattingThreshold.MILLION,
                currency = CurrencySelectionModel.empty(),
                maxDecimals = 7,
            )}${symbol.uppercase()}"
        },
        valueAccessible = if (hideData) {
            "hidden"
        } else {
            accessibleCoinAmountWithSymbol(amount, symbol)
        },
        modifier = modifier,
    )
}

@Composable
private fun FromUnitPriceView(
    unitPrice: Double?,
    symbol: String,
    hideData: Boolean,
    currency: CurrencySelectionModel,
    modifier: Modifier
) {
    StatView(
        title = "Unit Price (${symbol.uppercase()})",
        titleAccessible = "Unit Price ${symbol.symbolAccessible}",
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
private fun ToUnitPriceView(
    unitPrice: Double?,
    symbol: String,
    hideData: Boolean,
    currency: CurrencySelectionModel,
    modifier: Modifier
) {
    StatView(
        title = "Unit Price (${symbol.uppercase()})",
        titleAccessible = "Unit Price ${symbol.symbolAccessible}",
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
private fun RateView(
    fromSymbol: String,
    toSymbol: String,
    toRate: Double,
    hideData: Boolean,
    modifier: Modifier
) {
    StatView(
        title = "Rate",
        value = "1 ${fromSymbol.uppercase()} = ${if (hideData) {
            stars
        } else {
            toRate.formatCurrency(
                threshold = FormattingThreshold.MILLION,
                currency = CurrencySelectionModel.empty(),
            )
        }}${toSymbol.uppercase()}",
        valueAccessible = if (hideData) {
            "hidden"
        } else {
            "1 ${fromSymbol.symbolAccessible} equals ${
                toRate.formatCurrency(
                    threshold = FormattingThreshold.MILLION,
                    currency = CurrencySelectionModel.empty(),
                    accessible = true,
                )
            } ${toSymbol.symbolAccessible}"
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
private fun SwappedValueView(
    fromAmount: Double,
    fromUnitPrice: Double,
    hideData: Boolean,
    currency: CurrencySelectionModel,
    modifier: Modifier
) {
    StatView(
        title = "Swapped Value",
        value = if (hideData) {
            stars
        } else {
            (fromAmount * fromUnitPrice).formatCurrency(
                threshold = FormattingThreshold.MILLION,
                currency = currency,
            )
        },
        valueAccessible = if (hideData) {
            "hidden"
        } else {
            (fromAmount * fromUnitPrice).formatCurrency(
                threshold = FormattingThreshold.MILLION,
                currency = currency,
                accessible = true,
            )
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
