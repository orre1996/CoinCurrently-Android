package com.cryptium.coincurrently.features.Portfolio.Views
import com.cryptium.coincurrently.core.networking.ResponseModels.AddressImport
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.invisibleToUser
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.cryptium.coincurrently.core.localization.Localization
import com.cryptium.coincurrently.core.extensions.accessibleLongDateDescription
import com.cryptium.coincurrently.sharedui.components.AccessibilityLayout
import com.cryptium.coincurrently.sharedui.components.tabPageTalkBackTraversal
import com.cryptium.coincurrently.sharedui.theme.onSurfaceDim
import com.cryptium.coincurrently.features.Portfolio.Models.PortfolioSwapTransaction
import com.cryptium.coincurrently.core.extensions.DateFormat
import com.cryptium.coincurrently.core.extensions.formatDate
import com.cryptium.coincurrently.core.extensions.pieContaining
import com.cryptium.coincurrently.core.extensions.satoshiBold
import com.cryptium.coincurrently.core.extensions.satoshiMedium
import com.cryptium.coincurrently.sharedui.sheets.EmptyContent
import com.cryptium.coincurrently.sharedui.buttons.FilterButton
import com.cryptium.coincurrently.features.Portfolio.DataClasses.PortfolioTransaction
import com.cryptium.coincurrently.features.Portfolio.DataClasses.PortfolioEvent
import com.cryptium.coincurrently.features.Portfolio.Enums.DateOption
import com.cryptium.coincurrently.features.Portfolio.Enums.PortfolioTransactionType
import com.cryptium.coincurrently.features.Portfolio.Enums.rangeTitle
import com.cryptium.coincurrently.features.Portfolio.Enums.title
import com.cryptium.coincurrently.core.networking.ResponseModels.Pie
import com.cryptium.coincurrently.core.networking.ResponseModels.Toplist
import java.util.Date

private sealed class TransactionListItem {
    data class DateHeader(val date: Date) : TransactionListItem()
    data class Transaction(val event: PortfolioEvent) : TransactionListItem()
}

fun LazyListScope.TransactionsView(
    portfolio: List<Pie>?,
    groupedTransactions: Map<Date, List<PortfolioEvent>>,
    prices: MutableMap<String, Map<String, Double>>?,
    hideData: Boolean,
    typeFilters: ArrayList<PortfolioTransactionType>,
    dateFilter: DateOption,
    coinFilters: List<Toplist>,
    typeFilterPressed: () -> Unit,
    dateFilterPressed: () -> Unit,
    coinFilterPressed: () -> Unit,
    clearFilters: () -> Unit,
    showTransactionDetails: (PortfolioEvent) -> Unit
) {
    val horizontalPadding = Modifier.padding(start = 16.dp, end = 16.dp)
    val topPadding = Modifier.padding(top = 32.dp)
    val bottomPadding = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 32.dp)

    if (portfolio != null && portfolio.isEmpty()) {
        item {
            Box(modifier = Modifier.tabPageTalkBackTraversal().padding(top = 72.dp, start = 32.dp, end = 32.dp, bottom = 40.dp)) {
                EmptyContent(
                    title = Localization.string("No assets yet"),
                    subTitle = Localization.string("Tap the + button to begin building your portfolio.")
                )
            }
        }
    } else {
        item {
            Column(
                modifier = Modifier
                    .tabPageTalkBackTraversal()
                    .then(topPadding)
            ) {
                Column(
                    modifier = if (AccessibilityLayout.isTalkBackEnabled) {
                        Modifier.semantics { isTraversalGroup = true }
                    } else {
                        Modifier
                    }
                ) {
                    FiltersView(
                        typeFilters = typeFilters,
                        dateFilter = dateFilter,
                        coinFilters = coinFilters,
                        typeFilterPressed = typeFilterPressed,
                        dateFilterPressed = dateFilterPressed,
                        coinFilterPressed = coinFilterPressed
                    )

                    if (!typeFilters.isEmpty() || dateFilter != DateOption.All || !coinFilters.isEmpty()) {
                        val talkBackEnabled = AccessibilityLayout.isTalkBackEnabled
                        Box(
                            modifier = Modifier
                                .padding(
                                    start = 32.dp,
                                    top = 22.dp,
                                    end = 32.dp,
                                    bottom = 10.dp
                                )
                                .semantics(mergeDescendants = true) {
                                    contentDescription = Localization.string("Clear filters")
                                    if (talkBackEnabled) {
                                        traversalIndex = 1f
                                    }
                                    onClick(label = Localization.string("Clear filters"), action = {
                                        clearFilters.invoke()
                                        true
                                    })
                                }
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    clearFilters.invoke()
                                }
                        ) {
                            Text(
                                text = Localization.string("Clear filters"),
                                style = satoshiMedium(16).copy(textDecoration = TextDecoration.Underline),
                                color = MaterialTheme.colors.onSurfaceDim,
                                modifier = Modifier.semantics { invisibleToUser() },
                            )
                        }
                    }
                }
            }
        }

        TransactionsView(
            portfolio = portfolio,
            groupedTransactions = groupedTransactions,
            prices = prices,
            hideData = hideData,
            showTransactionDetails = showTransactionDetails,
            horizontalPadding = horizontalPadding,
            bottomPadding = bottomPadding
        )
    }
}

@Composable
private fun FiltersView(
    typeFilters: ArrayList<PortfolioTransactionType>,
    dateFilter: DateOption,
    coinFilters: List<Toplist>,
    typeFilterPressed: () -> Unit,
    dateFilterPressed: () -> Unit,
    coinFilterPressed: () -> Unit,
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        item {
            FilterButton(
                title = if (typeFilters.isEmpty()) {
                    Localization.string("Type")
                } else {
                    Localization.format("Type (%d)", typeFilters.count())
                },
                filterName = "type",
                accessibilityLabel = typeFilterAccessibilityLabel(typeFilters),
                isActive = typeFilters.isNotEmpty(),
                action = {
                    typeFilterPressed.invoke()
                }
            )
        }

        item {
            FilterButton(
                title = dateFilterDisplayTitle(dateFilter),
                filterName = "date",
                accessibilityLabel = dateFilterAccessibilityLabel(dateFilter),
                isActive = dateFilter != DateOption.All,
                action = {
                    dateFilterPressed.invoke()
                }
            )
        }

        item {
            FilterButton(
                title = if (coinFilters.isEmpty()) {
                    Localization.string("Coin")
                } else {
                    Localization.format("Coin (%d)", coinFilters.count())
                },
                filterName = "coin",
                accessibilityLabel = coinFilterAccessibilityLabel(coinFilters),
                isActive = coinFilters.isNotEmpty(),
                action = {
                    coinFilterPressed.invoke()
                }
            )
        }
    }
}

private fun dateFilterDisplayTitle(dateFilter: DateOption): String {
    return when (dateFilter) {
        is DateOption.Custom -> dateFilter.rangeTitle
        DateOption.All -> Localization.string("Date")
        else -> Localization.string(dateFilter.title)
    }
}

private fun typeFilterAccessibilityLabel(typeFilters: List<PortfolioTransactionType>): String {
    val selectedValues = if (typeFilters.isEmpty()) {
        null
    } else {
        typeFilters.joinToString(", ") { Localization.string(it.filterRawValue) }
    }
    return filterAccessibilityLabel(Localization.string("Type filter"), selectedValues)
}

private fun dateFilterAccessibilityLabel(dateFilter: DateOption): String {
    val selectedValues = if (dateFilter == DateOption.All) null else dateFilterAccessibilityValue(dateFilter)
    return filterAccessibilityLabel(Localization.string("Date filter"), selectedValues)
}

private fun dateFilterAccessibilityValue(dateFilter: DateOption): String {
    return when (dateFilter) {
        is DateOption.Custom -> {
            val start = dateFilter.startDate?.accessibleLongDateDescription().orEmpty()
            val end = dateFilter.endDate?.accessibleLongDateDescription().orEmpty()
            if (start.isEmpty() || end.isEmpty()) {
                Localization.string("Custom")
            } else {
                Localization.format("%1\$@ to %2\$@", start, end)
            }
        }
        else -> Localization.string(dateFilter.title)
    }
}

private fun coinFilterAccessibilityLabel(coinFilters: List<Toplist>): String {
    val selectedValues = if (coinFilters.isEmpty()) {
        null
    } else {
        coinFilters.joinToString(", ") { it.name ?: it.symbol ?: "" }
    }
    return filterAccessibilityLabel(Localization.string("Coin filter"), selectedValues)
}

private fun filterAccessibilityLabel(name: String, selectedValues: String?): String {
    if (selectedValues.isNullOrEmpty()) return name
    return Localization.format("%1\$@, currently selected: %2\$@", name, selectedValues)
}

private val PortfolioTransactionType.filterRawValue: String
    get() = when (this) {
        PortfolioTransactionType.Buy -> "Bought"
        PortfolioTransactionType.Sell -> "Sold"
        PortfolioTransactionType.Swap -> "Swapped"
        PortfolioTransactionType.TransferIn -> "Deposited"
        PortfolioTransactionType.TransferOut -> "Withdrew"
        PortfolioTransactionType.ImportAddress -> "Imported"
    }

private fun LazyListScope.TransactionsView(
    portfolio: List<Pie>?,
    groupedTransactions: Map<Date, List<PortfolioEvent>>,
    prices: MutableMap<String, Map<String, Double>>?,
    hideData: Boolean,
    showTransactionDetails: (PortfolioEvent) -> Unit,
    horizontalPadding: Modifier,
    bottomPadding: Modifier
) {
    val flatList = groupedTransactions
        .toSortedMap(compareByDescending { it })
        .flatMap { (date, events) ->
            listOf(TransactionListItem.DateHeader(date)) +
                events.sortedByDescending { it.date }.map { TransactionListItem.Transaction(it) }
        }
    val transactionItems = flatList.filterIsInstance<TransactionListItem.Transaction>()
    val transactionCount = transactionItems.size

    itemsIndexed(
        items = flatList,
        key = { _, item ->
            when (item) {
                is TransactionListItem.DateHeader -> "date-${item.date.time}"
                is TransactionListItem.Transaction -> item.event.id.toString()
            }
        }
    ) { index, item ->
        val nextIsDateHeader: Boolean = flatList.getOrNull(index + 1) is TransactionListItem.DateHeader
        val modifier = if (index == flatList.lastIndex) {
            bottomPadding
        } else {
            val bottom = when {
                item is TransactionListItem.Transaction && nextIsDateHeader -> 0.dp
                else -> 16.dp
            }
            horizontalPadding.then(Modifier.padding(bottom = bottom))
        }
        Box(modifier = Modifier.tabPageTalkBackTraversal().then(modifier)) {
            when (item) {
                is TransactionListItem.DateHeader -> {
                    Text(
                        modifier = Modifier
                            .padding(top = 32.dp)
                            .semantics { heading() },
                        text = item.date.formatDate(DateFormat.MMMddyyyy) ?: "",
                        style = satoshiBold(16),
                        color = MaterialTheme.colors.onSurfaceDim
                    )
                }

                is TransactionListItem.Transaction -> {
                    val event = item.event
                    val coin = portfolio?.pieContaining(event)?.data
                    val coinPrices = prices?.get(coin?.id)
                    val transactionIndex = transactionItems.indexOf(item)

                    when (event) {
                        is PortfolioTransaction -> {
                            PortfolioTransactionView(
                                coin = coin,
                                transaction = event,
                                prices = coinPrices,
                                hideData = hideData,
                                index = transactionIndex,
                                totalCount = transactionCount,
                                showTransactionDetails = { showTransactionDetails(event) }
                            )
                        }

                        is PortfolioSwapTransaction -> {
                            PortfolioSwapView(
                                coin = coin,
                                transaction = event,
                                portfolio = portfolio,
                                hideData = hideData,
                                index = transactionIndex,
                                totalCount = transactionCount,
                                showTransactionDetails = { showTransactionDetails(event) }
                            )
                        }

                        is AddressImport -> {
                            PortfolioImportTransaction(
                                coin = coin,
                                walletImport = event,
                                prices = prices,
                                hideData = hideData,
                                index = transactionIndex,
                                totalCount = transactionCount,
                                showTransactionDetails = { showTransactionDetails(event) }
                            )
                        }
                    }
                }
            }
        }
    }
}