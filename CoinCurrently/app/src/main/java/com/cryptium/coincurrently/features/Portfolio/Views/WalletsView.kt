package com.cryptium.coincurrently.features.Portfolio.Views
import com.cryptium.coincurrently.core.networking.ResponseModels.AddressImport
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import com.cryptium.coincurrently.sharedui.components.tabPageTalkBackTraversal
import com.cryptium.coincurrently.core.extensions.allWallets
import com.cryptium.coincurrently.core.extensions.pieContaining
import com.cryptium.coincurrently.sharedui.sheets.EmptyContent
import com.cryptium.coincurrently.features.Portfolio.DataClasses.PortfolioEvent
import com.cryptium.coincurrently.core.networking.ResponseModels.Pie
import java.util.Date

fun LazyListScope.WalletsView(
    portfolio: List<Pie>?,
    groupedTransactions: Map<Date, List<PortfolioEvent>>,
    prices: MutableMap<String, Map<String, Double>>?,
    hideData: Boolean,
    showTransactionDetails: (PortfolioEvent) -> Unit
) {
    if (portfolio != null && portfolio.allWallets.isEmpty()) {
        item {
            Box(
                modifier = Modifier
                    .tabPageTalkBackTraversal()
                    .padding(top = 72.dp, start = 32.dp, end = 32.dp, bottom = 40.dp)
            ) {
                EmptyContent(
                    title = "No wallets yet",
                    subTitle = "Tap the + button to import a wallet into your portfolio."
                )
            }
        }
    } else {
        WalletsListContent(
            portfolio = portfolio,
            groupedTransactions = groupedTransactions,
            prices = prices,
            hideData = hideData,
            showTransactionDetails = showTransactionDetails
        )
    }
}

private fun LazyListScope.WalletsListContent(
    portfolio: List<Pie>?,
    groupedTransactions: Map<Date, List<PortfolioEvent>>,
    prices: MutableMap<String, Map<String, Double>>?,
    hideData: Boolean,
    showTransactionDetails: (PortfolioEvent) -> Unit
) {
    val bottomPadding = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 32.dp)
    val events: List<AddressImport> = groupedTransactions
        .mapValues { (_, evs) ->
            evs.filter { it is AddressImport }
        }
        .filterValues { it.isNotEmpty() }
        .toSortedMap(compareByDescending { it })
        .flatMap { (_, evs) ->
            evs.sortedByDescending { it.date }.map { it as AddressImport }
        }

    itemsIndexed(
        items = events,
        key = { _, event -> event.id.toString() }
    ) { index, event ->
        val itemPadding = if (index == events.lastIndex) {
            bottomPadding.then(Modifier.padding(top = if (index == 0) 32.dp else 16.dp))
        } else {
            Modifier.padding(start = 16.dp, end = 16.dp, top = if (index == 0) 32.dp else 16.dp)
        }

        Box(modifier = Modifier.tabPageTalkBackTraversal().then(itemPadding)) {
            val coin = portfolio?.pieContaining(event)?.data
            PortfolioImportView(
                coin = coin,
                walletImport = event,
                prices = prices,
                hideData = hideData,
                index = index,
                totalCount = events.size,
                showTransactionDetails = { showTransactionDetails(event) }
            )
        }
    }
}
