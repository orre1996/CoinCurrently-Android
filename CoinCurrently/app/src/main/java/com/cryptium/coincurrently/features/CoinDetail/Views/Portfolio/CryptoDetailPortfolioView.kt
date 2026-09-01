package com.cryptium.coincurrently.features.CoinDetail.Views.Portfolio
import com.cryptium.coincurrently.sharedui.modifiers.imageSize

import com.cryptium.coincurrently.core.networking.ResponseModels.AddressImport
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import com.cryptium.coincurrently.R
import com.cryptium.coincurrently.sharedui.components.AccessibilityLayout
import com.cryptium.coincurrently.sharedui.components.tabPageTalkBackTraversal
import com.cryptium.coincurrently.sharedui.components.AdaptiveStack
import com.cryptium.coincurrently.sharedui.components.Separator
import com.cryptium.coincurrently.sharedui.theme.onSurfaceDim
import com.cryptium.coincurrently.sharedui.theme.onSurfaceDimmer
import com.cryptium.coincurrently.sharedui.theme.primaryBackgroundNoRipple
import com.cryptium.coincurrently.sharedui.modifiers.shimmeringModifier
import com.cryptium.coincurrently.features.Portfolio.Models.NewSwapTransaction
import com.cryptium.coincurrently.core.extensions.DateFormat
import com.cryptium.coincurrently.core.extensions.allTransactionsCount
import com.cryptium.coincurrently.core.extensions.formatCurrency
import com.cryptium.coincurrently.core.extensions.formatDate
import com.cryptium.coincurrently.core.extensions.getAllTransactions
import com.cryptium.coincurrently.core.extensions.getAmount
import com.cryptium.coincurrently.core.extensions.getProfitPercent
import com.cryptium.coincurrently.core.extensions.getProfitValue
import com.cryptium.coincurrently.core.extensions.getSelectedCurrency
import com.cryptium.coincurrently.core.extensions.getTotalCost
import com.cryptium.coincurrently.core.extensions.numberAccessible
import com.cryptium.coincurrently.core.extensions.pieContaining
import com.cryptium.coincurrently.core.extensions.satoshiBold
import com.cryptium.coincurrently.core.extensions.satoshiMedium
import com.cryptium.coincurrently.core.extensions.changePercentDecimalPoints
import com.cryptium.coincurrently.core.extensions.directionAccessible
import com.cryptium.coincurrently.sharedui.sheets.EmptyContent
import com.cryptium.coincurrently.sharedui.components.NewPriceChangePercentView
import com.cryptium.coincurrently.sharedui.components.NewPriceChangeValueView
import com.cryptium.coincurrently.sharedui.models.FormattingThreshold
import com.cryptium.coincurrently.sharedui.components.GlobalFunctions
import com.cryptium.coincurrently.sharedui.loading.ChangeSize
import com.cryptium.coincurrently.app.NavigationViewModel
import com.cryptium.coincurrently.core.analytics.BuyProAction
import com.cryptium.coincurrently.core.analytics.AnalyticsManager
import com.cryptium.coincurrently.core.analytics.PortfolioDataHide
import com.cryptium.coincurrently.core.analytics.PortfolioDataShow
import com.cryptium.coincurrently.core.analytics.ScreenLabel
import com.cryptium.coincurrently.core.analytics.SheetLabel
import com.cryptium.coincurrently.core.analytics.SheetOpened
import com.cryptium.coincurrently.features.GlobalMarket.allCurrencies
import com.cryptium.coincurrently.features.CoinDetail.CryptoDetailViewModel
import com.cryptium.coincurrently.features.Portfolio.Views.PortfolioImportTransaction
import com.cryptium.coincurrently.features.Portfolio.Views.PortfolioSwapView
import com.cryptium.coincurrently.features.Portfolio.Views.PortfolioTransactionView
import com.cryptium.coincurrently.features.Portfolio.DataClasses.NewTransaction
import com.cryptium.coincurrently.features.Portfolio.DataClasses.PortfolioEvent
import com.cryptium.coincurrently.core.purchases.StoreKitManager
import com.cryptium.coincurrently.core.networking.ResponseModels.CryptoDetailInfoModel
import com.cryptium.coincurrently.core.networking.ResponseModels.CurrencySelectionModel
import com.cryptium.coincurrently.core.networking.ResponseModels.Pie
import java.util.Date
import kotlin.Boolean

fun LazyListScope.CryptoDetailPortfolioView(
    viewModel: CryptoDetailViewModel,
    navigationViewModel: NavigationViewModel,
    hasPro: Boolean,
    showAddTransactionSheet: MutableState<Boolean>,
    isTablet: Boolean,
    showTransactionDetails: (PortfolioEvent) -> Unit
) {
    val onAddTransaction: () -> Unit = {
        if ((viewModel.portfolio?.allTransactionsCount ?: 0) >= 3 && !hasPro) {
            navigationViewModel.openBuyPro(ScreenLabel.CryptoDetail, BuyProAction.AddTransaction)
        } else {
            AnalyticsManager.log(
                SheetOpened(ScreenLabel.CryptoDetail, SheetLabel.PortfolioAddTransactionType)
            )
            showAddTransactionSheet.value = true
        }
    }

    val onTogglePortfolioHide: () -> Unit = {
        viewModel.toggleHideData()
        if (viewModel.hidePortfolioData) {
            AnalyticsManager.log(PortfolioDataHide(ScreenLabel.CryptoDetail))
        } else {
            AnalyticsManager.log(PortfolioDataShow(ScreenLabel.CryptoDetail))
        }
    }

    viewModel.groupedTransactions?.let {
        if (it.isEmpty()) {
            item(key = "portfolio_empty") {
                Box(
                    modifier = Modifier
                        .tabPageTalkBackTraversal(groupChildren = true)
                        .padding(
                            top = 72.dp,
                            start = 32.dp,
                            end = 32.dp,
                            bottom = 72.dp
                        )
                ) {
                    EmptyContent(
                        title = "No transactions yet",
                        subTitle = "Add your ${viewModel.cryptoInfo?.name ?: ""} transactions to start tracking your portfolio holdings here.",
                        buttonTitle = "Add Transaction",
                        buttonAction = onAddTransaction,
                    )
                }
            }
        } else {
            item(key = "portfolio_overview") {
                Box(modifier = Modifier.tabPageTalkBackTraversal(groupChildren = true)) {
                    OverviewView(
                        portfolio = viewModel.portfolio,
                        pie = viewModel.pie,
                        cryptoInfo = viewModel.cryptoInfo,
                        isDownloading = viewModel.groupedTransactions == null,
                        hidePortfolioData = viewModel.hidePortfolioData,
                        toggleHideData = onTogglePortfolioHide
                    )
                }
            }

            RealList(
                cryptoInfo = viewModel.cryptoInfo,
                portfolio = viewModel.portfolio,
                allPrices = viewModel.portfolioAllCurrencies,
                groupedTransactions = viewModel.groupedTransactions,
                hidePortfolioData = viewModel.hidePortfolioData,
                showTransactionDetails = showTransactionDetails,
                addNewTransaction = onAddTransaction
            )
        }
    } ?: run {
        item(key = "portfolio_overview_loading") {
            Box(modifier = Modifier.tabPageTalkBackTraversal(groupChildren = true)) {
                OverviewView(
                    portfolio = viewModel.portfolio,
                    pie = viewModel.pie,
                    cryptoInfo = viewModel.cryptoInfo,
                    isDownloading = viewModel.groupedTransactions == null,
                    hidePortfolioData = viewModel.hidePortfolioData,
                    toggleHideData = onTogglePortfolioHide
                )
            }
        }

        TempList(
            groupedTransactions = viewModel.groupedTransactions,
            hidePortfolioData = viewModel.hidePortfolioData,
            isTablet = isTablet,
            addNewTransaction = onAddTransaction
        )
    }
}

@Composable
private fun HeaderView(
    addNewTransaction: () -> Unit,
    groupedTransactions: Map<Date, List<PortfolioEvent>>?,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .padding(
                top = 32.dp,
                start = 16.dp,
                end = 16.dp,
                bottom = 4.dp
            )
    ) {
        Text(
            text = "Transactions",
            style = satoshiBold(22),
            color = MaterialTheme.colors.onSurfaceDim,
            modifier = Modifier
                .weight(1f)
                .semantics { heading() },
        )

        Image(
            painter = painterResource(id = R.drawable.add),
            contentDescription = "Add new transaction",
            colorFilter = ColorFilter.tint(MaterialTheme.colors.onSurfaceDimmer),
            modifier = Modifier
                .alpha(if (groupedTransactions?.isEmpty() ?: true) 0f else 1f)
                .padding(8.dp)
                .imageSize(24.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    addNewTransaction.invoke()
                }
        )
    }
}

@Composable
private fun OverviewView(
    portfolio: List<Pie>?,
    pie: Pie?,
    cryptoInfo: CryptoDetailInfoModel?,
    isDownloading: Boolean,
    hidePortfolioData: Boolean,
    toggleHideData: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
            .padding(
                top = 32.dp,
                start = 16.dp,
                end = 16.dp
            )
    ) {
        val currency = GlobalFunctions.getSelectedCurrency()
        val allTransactions = portfolio?.getAllTransactions(cryptoInfo?.id)
        val currentPrice = pie?.data?.sparkline_in_7d?.price?.lastOrNull()
            ?: cryptoInfo?.market_data?.current_price?.getSelectedCurrency()
        val amount = allTransactions?.getAmount()

        OverViewHeader(
            hidePortfolioData = hidePortfolioData,
            toggleHideData = toggleHideData,
        )

        val isHorizontal = AccessibilityLayout.isHorizontal

        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .primaryBackgroundNoRipple()
        ) {
            AdaptiveStack(
                isHorizontal = isHorizontal,
                spacing = 16.dp,
                horizontalAlignment = Alignment.Start,
                verticalAlignment = Alignment.Top,
            ) {
                HoldingsView(
                    symbol = cryptoInfo?.symbol?.uppercase(),
                    coinName = cryptoInfo?.name ?: cryptoInfo?.symbol?.uppercase().orEmpty(),
                    totalAmount = amount,
                    isDownloading = isDownloading,
                    modifier = sectionModifier(),
                )

                if (!isHorizontal) {
                    Separator()
                }

                CurrentValueView(
                    totalAmount = amount,
                    currentPrice = currentPrice,
                    isDownloading = isDownloading,
                    modifier = sectionModifier(),
                )
            }

            Separator()

            AdaptiveStack(
                isHorizontal = isHorizontal,
                spacing = 16.dp,
                horizontalAlignment = Alignment.Start,
                verticalAlignment = Alignment.Top,
            ) {
                PurchaseCostView(
                    totalCost = allTransactions?.getTotalCost(
                        prices = mapOf(currency.code.lowercase() to (currentPrice ?: 0.0))
                    ),
                    isDownloading = isDownloading,
                    modifier = sectionModifier(),
                )

                if (!isHorizontal) {
                    Separator()
                }

                ProfitLossView(
                    profitPercent = allTransactions?.getProfitPercent(
                        prices = mapOf(currency.code.lowercase() to (currentPrice ?: 0.0))
                    ),
                    profitValue = allTransactions?.getProfitValue(
                        prices = mapOf(currency.code.lowercase() to (currentPrice ?: 0.0))
                    ),
                    hidePortfolioData = hidePortfolioData,
                    isDownloading = isDownloading,
                    modifier = sectionModifier(),
                )
            }
        }
    }
}

@Composable
private fun OverViewHeader(
    hidePortfolioData: Boolean,
    toggleHideData: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Statistics",
            style = satoshiBold(22),
            color = MaterialTheme.colors.onSurfaceDim,
            modifier = Modifier
                .weight(1f)
                .semantics { heading() },
        )

        Image(
            painter = painterResource(if (hidePortfolioData) R.drawable.unhide else R.drawable.hide),
            contentDescription = if (hidePortfolioData) {
                "Show portfolio data"
            } else {
                "Hide portfolio data"
            },
            colorFilter = ColorFilter.tint(MaterialTheme.colors.onSurfaceDimmer),
            modifier = Modifier
                .padding(12.dp)
                .imageSize(24.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                ) {
                    toggleHideData.invoke()
                }
        )
    }
}

@Composable
private fun HoldingsView(
    symbol: String?,
    coinName: String,
    totalAmount: Double?,
    isDownloading: Boolean,
    modifier: Modifier
) {
    val amountAccessible = totalAmount?.formatCurrency(
        currency = CurrencySelectionModel.empty(),
        accessible = true,
    ) ?: ""

    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier.semantics(mergeDescendants = true) {
            contentDescription = "Total Holdings. $amountAccessible $coinName"
        },
    ) {
        Text(
            text = "Total Holdings",
            style = satoshiBold(12),
            color = MaterialTheme.colors.onSurfaceDim
        )

        Text(
            text = "${totalAmount?.formatCurrency(currency = CurrencySelectionModel.empty()) ?: "000"} (${symbol ?: ""})",
            style = satoshiBold(16),
            color = MaterialTheme.colors.onSurface,
            modifier = Modifier
                .shimmeringModifier(isDownloading)
        )
    }
}

@Composable
private fun CurrentValueView(
    totalAmount: Double?,
    currentPrice: Double?,
    isDownloading: Boolean,
    modifier: Modifier
) {
    val currentValue = (currentPrice ?: 0.0) * (totalAmount ?: 0.0)

    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier.semantics(mergeDescendants = true) {
            contentDescription = currencyStatAccessibilityLabel("Current Value", currentValue)
        },
    ) {
        Text(
            text = "Current Value",
            style = satoshiBold(12),
            color = MaterialTheme.colors.onSurfaceDim
        )

        Text(
            text = ((currentPrice ?: 0.0) * (totalAmount
                ?: 0.0)).formatCurrency(threshold = FormattingThreshold.MILLION),
            style = satoshiBold(16),
            color = MaterialTheme.colors.onSurface,
            modifier = Modifier
                .shimmeringModifier(isDownloading)
        )
    }
}

@Composable
private fun PurchaseCostView(
    totalCost: Double?,
    isDownloading: Boolean,
    modifier: Modifier
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier.semantics(mergeDescendants = true) {
            contentDescription = currencyStatAccessibilityLabel("Purchase Cost", totalCost)
        },
    ) {
        Text(
            text = "Purchase Cost",
            style = satoshiBold(12),
            color = MaterialTheme.colors.onSurfaceDim
        )

        Text(
            text = totalCost?.formatCurrency(
                threshold = FormattingThreshold.MILLION,
                currency = CurrencySelectionModel.empty()
            ) ?: "000",
            style = satoshiBold(16),
            color = MaterialTheme.colors.onSurface,
            modifier = Modifier
                .shimmeringModifier(isDownloading)
        )
    }
}

@Composable
private fun ProfitLossView(
    profitPercent: Double?,
    profitValue: Double?,
    hidePortfolioData: Boolean,
    isDownloading: Boolean,
    modifier: Modifier
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier.semantics(mergeDescendants = true) {
            contentDescription = profitLossAccessibilityLabel(
                value = profitValue,
                percent = profitPercent,
                hidePortfolioData = hidePortfolioData,
            )
        },
    ) {
        Text(
            text = "Profit/Loss",
            style = satoshiBold(12),
            color = MaterialTheme.colors.onSurfaceDim
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            NewPriceChangeValueView(
                change = profitValue,
                size = ChangeSize.MEDIUM,
                hideData = hidePortfolioData,
                showDirection = false,
                isDownloading = isDownloading
            )

            NewPriceChangePercentView(
                change = profitPercent,
                size = ChangeSize.EXTRA_SMALL,
                isDownloading = isDownloading,
                modifier = Modifier
                    .alpha(0.75f)
            )
        }
    }
}

private fun LazyListScope.TempList(
    addNewTransaction: () -> Unit,
    groupedTransactions: Map<Date, List<PortfolioEvent>>?,
    hidePortfolioData: Boolean,
    isTablet: Boolean
) {
    val outerGroups = 2
    val innerTransactions = if (isTablet) 6 else 3
    item(key = "portfolio_transactions_header_loading") {
        Box(modifier = Modifier.tabPageTalkBackTraversal(groupChildren = true)) {
            HeaderView(
                groupedTransactions = groupedTransactions,
                addNewTransaction = addNewTransaction,
            )
        }
    }

    (0 until outerGroups).forEach { outerIndex ->
        item(key = "portfolio_transactions_loading_group_$outerIndex") {
            Box(
                modifier = Modifier
                    .tabPageTalkBackTraversal(groupChildren = true)
                    .padding(horizontal = 16.dp)
                    .primaryBackgroundNoRipple()
            ) {
                Column {
                    Text(
                        text = "Apr 7, 2025",
                        style = satoshiMedium(14),
                        color = MaterialTheme.colors.onSurfaceDim,
                        modifier = Modifier
                            .padding(
                                bottom = 16.dp
                            )
                            .shimmeringModifier(true)
                    )

                    (0 until innerTransactions).forEach { innerIndex ->
                        Box(
                            modifier = Modifier
                                .padding(
                                    bottom = if (outerIndex == outerGroups - 1 && innerIndex == innerTransactions - 1) 0.dp else 16.dp
                                )
                        ) {
                            PortfolioTransactionView(
                                coin = null,
                                transaction = null,
                                prices = mapOf(),
                                hideData = hidePortfolioData,
                                showTransactionDetails = { }
                            )
                        }
                    }

                    if (outerIndex != outerGroups - 1) {
                        Separator(
                            padding = PaddingValues(
                                bottom = 16.dp
                            )
                        )
                    }
                }
            }
        }
    }
}

private fun LazyListScope.RealList(
    cryptoInfo: CryptoDetailInfoModel?,
    portfolio: List<Pie>?,
    allPrices: MutableMap<String, Map<String, Double>>?,
    groupedTransactions: Map<Date, List<PortfolioEvent>>?,
    hidePortfolioData: Boolean,
    showTransactionDetails: (PortfolioEvent) -> Unit,
    addNewTransaction: () -> Unit
) {
    val prices = cryptoInfo?.market_data?.current_price?.allCurrencies

    item(key = "portfolio_transactions_header") {
        Box(modifier = Modifier.tabPageTalkBackTraversal(groupChildren = true)) {
            HeaderView(
                groupedTransactions = groupedTransactions,
                addNewTransaction = addNewTransaction,
            )
        }
    }

    val allEvents = groupedTransactions?.values?.flatten() ?: emptyList()
    val groupsList: List<Map.Entry<Date, List<PortfolioEvent>>> =
        groupedTransactions?.entries?.sortedByDescending { it.key } ?: emptyList()

    groupsList.forEachIndexed { groupIndex, group ->
        val isLastGroup: Boolean = groupIndex == groupsList.lastIndex
        item(key = "portfolio_transactions_date_${group.key}") {
            Box(
                modifier = Modifier
                    .tabPageTalkBackTraversal(groupChildren = true)
                    .padding(horizontal = 16.dp)
                    .primaryBackgroundNoRipple()
            ) {
                Column {
                    Text(
                        text = group.key.formatDate(DateFormat.MMMddyyyy) ?: "",
                        style = satoshiMedium(14),
                        color = MaterialTheme.colors.onSurfaceDim,
                        modifier = Modifier
                            .padding(
                                bottom = 16.dp
                            )
                    )
                    val events: List<PortfolioEvent> = group.value
                    events.forEachIndexed { eventIndex, event ->
                        val isLastEvent: Boolean = eventIndex == events.lastIndex
                        val globalIndex = allEvents.indexOf(event)
                        Box(
                            modifier = Modifier
                                .padding(
                                    bottom = if (isLastGroup && isLastEvent) 0.dp else 16.dp
                                )
                        ) {
                            val coin = portfolio?.pieContaining(event)?.data
                            when (event) {
                                is NewTransaction -> {
                                    PortfolioTransactionView(
                                        coin = coin,
                                        transaction = event,
                                        prices = prices,
                                        hideData = hidePortfolioData,
                                        index = globalIndex,
                                        totalCount = allEvents.size,
                                        showTransactionDetails = { showTransactionDetails(event) }
                                    )
                                }

                                is NewSwapTransaction -> {
                                    PortfolioSwapView(
                                        coin = coin,
                                        transaction = event,
                                        hideData = hidePortfolioData,
                                        index = globalIndex,
                                        totalCount = allEvents.size,
                                        showTransactionDetails = { showTransactionDetails(event) }
                                    )
                                }

                                is AddressImport -> {
                                    PortfolioImportTransaction(
                                        coin = coin,
                                        walletImport = event,
                                        prices = allPrices,
                                        hideData = hidePortfolioData,
                                        index = globalIndex,
                                        totalCount = allEvents.size,
                                        showTransactionDetails = { showTransactionDetails(event) }
                                    )
                                }
                            }
                        }
                    }
                    if (!isLastGroup) {
                        Separator(
                            padding = PaddingValues(
                                bottom = 16.dp
                            )
                        )
                    }
                }
            }
        }
    }
}

private fun currencyStatAccessibilityLabel(title: String, value: Double?): String {
    val formattedValue = value?.formatCurrency(
        threshold = FormattingThreshold.MILLION,
        accessible = true,
    ) ?: ""
    return "$title, $formattedValue"
}

private fun profitLossAccessibilityLabel(
    value: Double?,
    percent: Double?,
    hidePortfolioData: Boolean,
): String {
    val changePercent = percent ?: 0.0
    val percentText = if (changePercent.isInfinite()) {
        null
    } else {
        val decimals = changePercent.changePercentDecimalPoints
        val accessiblePercent = kotlin.math.abs(changePercent).numberAccessible(maxPlaces = decimals)
        val direction = changePercent.directionAccessible.lowercase()
        "$direction $accessiblePercent percent"
    }

    if (hidePortfolioData) {
        return if (percentText != null) "Profit/Loss, $percentText" else "Profit/Loss"
    }

    val changeValue = value ?: 0.0
    val valueDirection = changeValue.directionAccessible.lowercase()
    val valueText = kotlin.math.abs(changeValue).formatCurrency(accessible = true)
    return if (percentText != null) {
        "Profit/Loss, $valueDirection $valueText, $percentText"
    } else {
        "Profit/Loss, $valueDirection $valueText"
    }
}