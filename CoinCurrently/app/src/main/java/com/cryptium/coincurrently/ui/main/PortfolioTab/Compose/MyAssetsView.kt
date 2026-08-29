package com.cryptium.coincurrently.ui.main.PortfolioTab.Compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.ui.graphics.Color
import com.cryptium.coincurrently.ui.main.Compose.tabPageTalkBackTraversal
import com.cryptium.coincurrently.ui.main.Compose.onSurfaceDim
import com.cryptium.coincurrently.ui.main.Compose.primaryBackgroundNoRipple
import com.cryptium.coincurrently.ui.main.Extensions.getAllTransactions
import com.cryptium.coincurrently.ui.main.Extensions.isTablet
import com.cryptium.coincurrently.ui.main.Extensions.satoshiBold
import com.cryptium.coincurrently.ui.main.Extensions.satoshiRegular
import com.cryptium.coincurrently.ui.main.General.Components.EmptyContent
import networking.ResponseModels.Pie
import networking.ResponseModels.Toplist
import networking.ResponseModels.shouldShow
import kotlin.collections.get


fun LazyListScope.MyAssetsView(
    portfolio: List<Pie>?,
    allPrices: MutableMap<String, Map<String, Double>>?,
    hideData: Boolean,
    isTablet: Boolean,
    navigateToCoin: (Toplist) -> Unit
) {
    val horizontalPadding = Modifier.padding(start = 16.dp, end = 16.dp)
    val topPadding = Modifier.padding(top = 32.dp, start = 16.dp, bottom = 16.dp, end = 16.dp)
    val bottomPadding = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 32.dp)

    portfolio?.let {
        if (it.isEmpty()) {
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
                Box(modifier = topPadding) {
                    PortfolioHeaderView()
                }
            }

            RealList(
                portfolio = it,
                allPrices = allPrices,
                hideData = hideData,
                navigateToCoin = navigateToCoin,
                horizontalPadding = horizontalPadding,
                bottomPadding = bottomPadding
            )
        }
    } ?: run {
        item {
            Box(modifier = topPadding) {
                PortfolioHeaderView()
            }
        }

        TempList(
            hideData = hideData,
            horizontalPadding = horizontalPadding,
            bottomPadding = bottomPadding,
            isTablet = isTablet
        )
    }
}

private fun LazyListScope.TempList(
    hideData: Boolean,
    horizontalPadding: Modifier,
    bottomPadding: Modifier,
    isTablet: Boolean
) {
    val rowCount = if (isTablet) 20 else 10
    items(rowCount) { index ->
        val modifier = if (index == rowCount - 1) bottomPadding else horizontalPadding.then(Modifier.padding(bottom = 16.dp))
        Box(modifier = Modifier.tabPageTalkBackTraversal().then(modifier)) {
            PortfolioCoinView(
            pie = null,
            transactions = null,
            prices = null,
            hideData = hideData,
                navigateToCoin = { }
            )
        }
    }
}

private fun LazyListScope.RealList(
    portfolio: List<Pie>,
    allPrices: MutableMap<String, Map<String, Double>>?,
    hideData: Boolean,
    navigateToCoin: (Toplist) -> Unit,
    horizontalPadding: Modifier,
    bottomPadding: Modifier
) {
    val filteredPortfolio = portfolio.filter { pie ->
        val id = pie.data?.id ?: return@filter false
        pie.shouldShow && portfolio.getAllTransactions(id).isNotEmpty()
    }

    items(filteredPortfolio.size) { index ->
        val pie = filteredPortfolio[index]
        val modifier = if (index == filteredPortfolio.lastIndex) bottomPadding else horizontalPadding.then(Modifier.padding(bottom = 16.dp))

        Box(modifier = Modifier.tabPageTalkBackTraversal().then(modifier)) {
            PortfolioCoinView(
                pie = pie,
                transactions = portfolio.getAllTransactions(pie.data?.id),
                prices = allPrices?.get(pie.data?.id),
                hideData = hideData,
                index = index,
                totalCount = filteredPortfolio.size,
                navigateToCoin = {
                    pie.data?.let {
                        navigateToCoin.invoke(it)
                    }
                }
            )
        }
    }
}