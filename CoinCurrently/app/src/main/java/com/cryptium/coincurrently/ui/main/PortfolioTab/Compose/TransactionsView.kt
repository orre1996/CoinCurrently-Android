package com.cryptium.coincurrently.ui.main.PortfolioTab.Compose

import AddressImport
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
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionRunCallback
import com.cryptium.coincurrently.ui.main.Compose.AccessibilityLayout
import com.cryptium.coincurrently.ui.main.Compose.tabPageTalkBackTraversal
import com.cryptium.coincurrently.ui.main.Compose.onSurfaceDim
import com.cryptium.coincurrently.ui.main.CryptoDetail.Compose.Portfolio.Models.NewSwapTransaction
import com.cryptium.coincurrently.ui.main.Extensions.DateFormat
import com.cryptium.coincurrently.ui.main.Extensions.formatDate
import com.cryptium.coincurrently.ui.main.Extensions.pieContaining
import com.cryptium.coincurrently.ui.main.Extensions.satoshiBold
import com.cryptium.coincurrently.ui.main.Extensions.satoshiMedium
import com.cryptium.coincurrently.ui.main.General.Components.EmptyContent
import com.cryptium.coincurrently.ui.main.General.Components.FilterButton
import com.cryptium.coincurrently.ui.main.PortfolioTab.DataClasses.NewTransaction
import com.cryptium.coincurrently.ui.main.PortfolioTab.DataClasses.PortfolioEvent
import com.cryptium.coincurrently.ui.main.PortfolioTab.Enums.DateOption
import com.cryptium.coincurrently.ui.main.PortfolioTab.Enums.NewTransactionType
import com.cryptium.coincurrently.ui.main.PortfolioTab.Enums.rangeTitle
import com.cryptium.coincurrently.ui.main.Widgets.NewsIdKey
import com.cryptium.coincurrently.ui.main.Widgets.OpenAppAction
import networking.ResponseModels.Pie
import networking.ResponseModels.Toplist
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
    typeFilters: ArrayList<NewTransactionType>,
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
                    title = "No assets yet",
                    subTitle = "Tap the + button to begin building your portfolio."
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
                                    contentDescription = "Clear filters"
                                    if (talkBackEnabled) {
                                        traversalIndex = 1f
                                    }
                                    onClick(label = "Clear filters", action = {
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
                                text = "Clear filters",
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
    typeFilters: ArrayList<NewTransactionType>,
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
                title = if (typeFilters.isEmpty()) "Type" else "Type (${typeFilters.count()})",
                filterName = "type",
                selectedCount = typeFilters.count(),
                isActive = typeFilters.isNotEmpty(),
                action = {
                    typeFilterPressed.invoke()
                }
            )
        }

        item {
            FilterButton(
                title = dateFilter.rangeTitle,
                filterName = "date",
                selectedTitle = if (dateFilter == DateOption.All) null else dateFilter.rangeTitle,
                isActive = dateFilter != DateOption.All,
                action = {
                    dateFilterPressed.invoke()
                }
            )
        }

        item {
            FilterButton(
                title = if (coinFilters.isEmpty()) "Coin" else "Coin (${coinFilters.count()})",
                filterName = "coin",
                selectedCount = coinFilters.count(),
                isActive = coinFilters.isNotEmpty(),
                action = {
                    coinFilterPressed.invoke()
                }
            )
        }
    }
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
                        is NewTransaction -> {
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

                        is NewSwapTransaction -> {
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