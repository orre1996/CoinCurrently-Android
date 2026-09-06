package com.cryptium.coincurrently.features.Portfolio
import com.cryptium.coincurrently.sharedui.modifiers.imageSize

import com.cryptium.coincurrently.core.networking.ResponseModels.AddressImport
import com.cryptium.coincurrently.app.NavigationScreens
import com.cryptium.coincurrently.core.networking.ResponseModels.TokenInfo
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.invisibleToUser
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cryptium.coincurrently.R
import com.cryptium.coincurrently.sharedui.theme.*
import com.cryptium.coincurrently.sharedui.modifiers.*
import com.cryptium.coincurrently.sharedui.components.AccessibilityLayout
import com.cryptium.coincurrently.sharedui.components.AdaptiveStack
import com.cryptium.coincurrently.sharedui.components.Separator
import com.cryptium.coincurrently.core.extensions.DateFormat
import com.cryptium.coincurrently.core.extensions.accessibleLongDateDescription
import com.cryptium.coincurrently.core.extensions.accessibleTimeDescription
import com.cryptium.coincurrently.core.extensions.changeColor
import com.cryptium.coincurrently.core.extensions.changeWithSymbol
import com.cryptium.coincurrently.core.extensions.formatCurrency
import com.cryptium.coincurrently.core.extensions.formatDate
import com.cryptium.coincurrently.core.extensions.is24HourClock
import com.cryptium.coincurrently.core.extensions.roundTo
import com.cryptium.coincurrently.core.extensions.satoshiBold
import com.cryptium.coincurrently.core.extensions.stars
import com.cryptium.coincurrently.sharedui.navigation.CustomTopAppBar
import com.cryptium.coincurrently.sharedui.components.OnLifecycleEvent
import com.cryptium.coincurrently.sharedui.components.PriceChangeValueView
import com.cryptium.coincurrently.sharedui.models.FormattingThreshold
import com.cryptium.coincurrently.sharedui.loading.ChangeSize
import com.cryptium.coincurrently.sharedui.loading.PriceChangeDirectionView
import com.cryptium.coincurrently.app.NavigationViewModel
import com.cryptium.coincurrently.features.Portfolio.Views.WalletCoinView
import com.cryptium.coincurrently.features.Portfolio.Views.WalletHeaderView
import com.cryptium.coincurrently.features.Portfolio.Views.portfolioChangeAccessibilityLabel
import com.cryptium.coincurrently.features.Portfolio.Sheets.DeleteWalletPopup
import com.cryptium.coincurrently.features.Portfolio.Sheets.RenameWalletSheet
import com.cryptium.coincurrently.features.Portfolio.Sheets.WalletInfoSheet
import com.cryptium.coincurrently.features.Portfolio.ViewModels.WalletDetailsViewModel
import com.cryptium.coincurrently.features.Portfolio.ViewModels.WalletDetailsViewModelFactory
import com.cryptium.coincurrently.core.analytics.AnalyticsManager
import com.cryptium.coincurrently.core.analytics.Navigate
import com.cryptium.coincurrently.core.analytics.PortfolioDataHide
import com.cryptium.coincurrently.core.analytics.PortfolioDataShow
import com.cryptium.coincurrently.core.analytics.ScreenLabel
import com.cryptium.coincurrently.core.analytics.SheetLabel
import com.cryptium.coincurrently.core.analytics.SheetOpened
import com.cryptium.coincurrently.core.analytics.WalletDeleted
import com.cryptium.coincurrently.core.analytics.WalletRenamed
import com.cryptium.coincurrently.core.networking.ResponseModels.*
import com.cryptium.coincurrently.core.networking.ResponseModels.toTokenInfo
import java.util.Date
import kotlin.collections.get
import kotlinx.coroutines.launch

@Composable
fun WalletDetailsScreen(
    navigationViewModel: NavigationViewModel,
    modifier: Modifier,
    wallet: AddressImport,
    coin: Toplist
) {
    var showRenameSheet by remember { mutableStateOf(false) }
    var showDeletePopup by remember { mutableStateOf(false) }
    var isWalletInfoShowing by remember { mutableStateOf(false) }

    val isDropDownExpanded = remember { mutableStateOf(false) }

    val viewModel: WalletDetailsViewModel = viewModel(
        factory = WalletDetailsViewModelFactory(
            wallet = wallet,
            coin = coin
        )
    )

    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.getAllPortfolioCurrencies()
        viewModel.getCoins()
    }

    OnLifecycleEvent { _, event ->
        when (event) {
            Lifecycle.Event.ON_RESUME -> {
                coroutineScope.launch {
                    viewModel.getAllPortfolioCurrencies()
                    viewModel.getCoins()
                }
            }
            else -> { }
        }
    }

    BackHandler(onBack = {
        if (showDeletePopup) {
            showDeletePopup = false
        } else {
            navigationViewModel.navigateBack()
        }
    })

    val talkBackEnabled = AccessibilityLayout.isTalkBackEnabled

    Box {
        Column(
            modifier = modifier.then(
                if (showDeletePopup && talkBackEnabled) {
                    Modifier.semantics(mergeDescendants = true) {
                        invisibleToUser()
                    }
                } else {
                    Modifier
                }
            )
        ) {
            CustomTopAppBar(
                title = viewModel.wallet.walletName ?: "Wallet",
                navigateBack = {
                    navigationViewModel.navigateBack()
                },
                actions = listOf({
                    MoreButton(
                        isDropDownExpanded = isDropDownExpanded,
                        renameWallet = {
                            AnalyticsManager.log(
                                SheetOpened(
                                    fromScreen = ScreenLabel.WalletDetails,
                                    sheet = SheetLabel.RenameWallet,
                                ),
                            )
                            showRenameSheet = true
                        },
                        deleteWallet = {
                            AnalyticsManager.log(
                                SheetOpened(
                                    fromScreen = ScreenLabel.WalletDetails,
                                    sheet = SheetLabel.DeleteWallet,
                                ),
                            )
                            showDeletePopup = true
                        }
                    )
                })
            )

            LazyColumn(
                contentPadding = PaddingValues(
                    top = 16.dp,
                    bottom = 32.dp
                ),
                verticalArrangement = Arrangement.spacedBy(
                    0.dp,
                    Alignment.Top
                ),
                modifier = Modifier
                    .weight(1f)
                    .wrapContentHeight()
                    .fillMaxSize()
                    .then(
                        if (talkBackEnabled) {
                            Modifier.semantics { isTraversalGroup = false }
                        } else {
                            Modifier
                        }
                    )
            ) {
                item {
                    WalletTotals(
                        totalValue = viewModel.totalValue,
                        hideData = viewModel.hideData,
                        toggleHideData = {
                            viewModel.toggleHideData()
                            if (viewModel.hideData) {
                                AnalyticsManager.log(
                                    PortfolioDataHide(ScreenLabel.WalletDetails),
                                )
                            } else {
                                AnalyticsManager.log(
                                    PortfolioDataShow(ScreenLabel.WalletDetails),
                                )
                            }
                        },
                        showInfo = {
                            AnalyticsManager.log(
                                SheetOpened(
                                    fromScreen = ScreenLabel.WalletDetails,
                                    sheet = SheetLabel.WalletInfo,
                                ),
                            )
                            isWalletInfoShowing = true
                        }
                    )
                }

                item {
                    ChangeView(
                        changePercent = viewModel.changePercent,
                        changeValue = viewModel.changeValue,
                        syncedDate = viewModel.syncedDate,
                        hideData = viewModel.hideData
                    )
                }

                item {
                    Separator(
                        padding = PaddingValues(
                            vertical = 32.dp
                        )
                    )
                }

                CoinsList(
                    coin = viewModel.coin,
                    wallet = viewModel.wallet,
                    coins = viewModel.coins,
                    hideData = viewModel.hideData,
                    sortedTokens = viewModel.sortedTokens,
                    navigateToCoin = {
                        val dest = NavigationScreens.CryptoDetail(
                            it.toCryptoDetail()
                        )
                        AnalyticsManager.log(
                            Navigate(
                                fromScreen = ScreenLabel.WalletDetails,
                                toScreen = ScreenLabel.CryptoDetail,
                                coinName = it.name ?: "",
                            ),
                        )
                        navigationViewModel.navigate(dest)
                    }
                )
            }
        }

        if (showRenameSheet) {
            RenameWalletSheet(
                initialName = viewModel.wallet.walletName ?: "",
                renameWallet = {
                    showRenameSheet = false

                    it?.let { newName ->
                        AnalyticsManager.log(
                            WalletRenamed(ScreenLabel.WalletDetails),
                        )
                        viewModel.renameWallet(newName)
                    }
                }
            )
        }

        if (showDeletePopup) {
            DeleteWalletPopup(
                walletName = viewModel.wallet.walletName ?: "",
                onClose = {
                    showDeletePopup = false

                    if (it) {
                        AnalyticsManager.log(
                            WalletDeleted(ScreenLabel.WalletDetails),
                        )
                        viewModel.deleteWallet()
                        navigationViewModel.navigateBack()
                    }
                }
            )
        }

        if (isWalletInfoShowing) {
            WalletInfoSheet(
                wallet = viewModel.wallet,
                coin = viewModel.coin,
                syncedDate = viewModel.syncedDate,
                close = {
                    isWalletInfoShowing = false
                }
            )
        }
    }
}

@Composable
private fun MoreButton(
    isDropDownExpanded: MutableState<Boolean>,
    renameWallet: () -> Unit,
    deleteWallet: () -> Unit,
) {
    IconButton(onClick = {
        isDropDownExpanded.value = true
    }) {
        Box {
            Image(
                painter = painterResource(id = R.drawable.more),
                contentDescription = "More options",
                colorFilter = ColorFilter.tint(
                    MaterialTheme.colors.onSurface
                ),
            )

            DropdownMenu(
                expanded = isDropDownExpanded.value,
                onDismissRequest = {
                    isDropDownExpanded.value = false
                }
            ) {
                DropdownMenuItem(
                    content = {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Rename Wallet",
                                style = satoshiBold(16)
                            )

                            Spacer(
                                modifier = Modifier
                                    .widthIn(50.dp)
                                    .weight(1f)
                            )

                            Image(
                                painter = painterResource(R.drawable.edit),
                                contentDescription = null
                            )
                        }
                    },
                    onClick = {
                        isDropDownExpanded.value = false
                        renameWallet.invoke()
                    }
                )

                Separator()

                DropdownMenuItem(
                    content = {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Remove Wallet",
                                style = satoshiBold(16)
                            )

                            Spacer(
                                modifier = Modifier
                                    .widthIn(50.dp)
                                    .weight(1f)
                            )

                            Image(
                                painter = painterResource(R.drawable.delete),
                                contentDescription = null
                            )
                        }
                    },
                    onClick = {
                        isDropDownExpanded.value = false
                        deleteWallet.invoke()
                    }
                )
            }
        }
    }
}

@Composable
private fun WalletTotals(
    totalValue: Double?,
    hideData: Boolean,
    toggleHideData: () -> Unit,
    showInfo: () -> Unit
) {
    val formattedTotalValue = totalValue?.formatCurrency(threshold = FormattingThreshold.MILLION) ?: ""
    val walletValueLabel = if (hideData) {
        "Wallet value, hidden"
    } else {
        "Wallet value: $formattedTotalValue"
    }
    val hideDataLabel = "${if (hideData) "show" else "hide"} wallet data"
    val talkBackEnabled = AccessibilityLayout.isTalkBackEnabled

    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
            .padding(
                start = 16.dp,
                bottom = 12.dp,
                end = 16.dp
            )
    ) {
        Text(
            text = "Wallet Value",
            style = satoshiBold(18),
            color = MaterialTheme.colors.onSurfaceDim,
            modifier = Modifier.semantics { invisibleToUser() }
        )

        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (talkBackEnabled) {
                        Modifier.semantics { isTraversalGroup = true }
                    } else {
                        Modifier
                    }
                )
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f, fill = false)
            ) {
                Text(
                    text = if (hideData) stars else formattedTotalValue,
                    style = satoshiBold(36),
                    color = MaterialTheme.colors.onSurface,
                    maxLines = 1,
                    softWrap = false,
                    modifier = Modifier
                        .then(
                            if (talkBackEnabled) {
                                Modifier.semantics {
                                    contentDescription = walletValueLabel
                                    traversalIndex = 0f
                                }
                            } else {
                                Modifier.semantics {
                                    contentDescription = walletValueLabel
                                }
                            }
                        )
                )

                Image(
                    painter = painterResource(if (hideData) R.drawable.unhide else R.drawable.hide),
                    contentDescription = null,
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
                        .then(
                            if (talkBackEnabled) {
                                Modifier.semantics {
                                    contentDescription = hideDataLabel
                                    traversalIndex = 1f
                                }
                            } else {
                                Modifier.semantics {
                                    contentDescription = hideDataLabel
                                }
                            }
                        )
                )
            }

            Image(
                painter = painterResource(id = R.drawable.info),
                contentDescription = null,
                colorFilter = ColorFilter.tint(MaterialTheme.colors.onSurfaceDimmer),
                modifier = Modifier
                    .padding(12.dp)
                    .imageSize(24.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        showInfo.invoke()
                    }
                    .then(
                        if (talkBackEnabled) {
                            Modifier.semantics {
                                contentDescription = "Wallet details"
                                traversalIndex = 2f
                            }
                        } else {
                            Modifier.semantics {
                                contentDescription = "Wallet details"
                            }
                        }
                    )
            )
        }
    }
}

@Composable
private fun ChangeView(
    changePercent: Double?,
    changeValue: Double?,
    syncedDate: Date?,
    hideData: Boolean
) {
    val context = LocalContext.current
    val isHorizontal = AccessibilityLayout.isHorizontal
    val synced = syncedDate ?: Date()
    val valueChangeAccessibilityLabel = portfolioChangeAccessibilityLabel(
        title = "7 days value change",
        value = changeValue,
        percent = changePercent,
        hideData = hideData,
    )
    val lastSyncedAccessibilityLabel =
        "Last Synced: ${synced.accessibleLongDateDescription()} at ${synced.accessibleTimeDescription(context.is24HourClock)}"

    AdaptiveStack(
        isHorizontal = isHorizontal,
        spacing = if (isHorizontal) 16.dp else 8.dp,
        horizontalAlignment = Alignment.Start,
        verticalAlignment = Alignment.Top,
        modifier = Modifier
            .padding(
                start = 16.dp,
                bottom = 12.dp,
                end = 16.dp
            )
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalAlignment = Alignment.Start,
            modifier = sectionModifier()
                .semantics(mergeDescendants = true) {
                    contentDescription = valueChangeAccessibilityLabel
                },
        ) {
            Text(
                text = "Value Change (7d)",
                style = satoshiBold(12),
                color = MaterialTheme.colors.onSurfaceDim,
                modifier = Modifier.semantics { invisibleToUser() }
            )

            val roundedChange = changePercent?.roundTo(2)
            val formattedChange = "%.2f".format(roundedChange ?: 0.0).changeWithSymbol

            FlowRow(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                itemVerticalAlignment = Alignment.CenterVertically
            ) {
                if (hideData) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.semantics { invisibleToUser() }
                    ) {
                        PriceChangeDirectionView(
                            change = 0.0,
                            size = ChangeSize.SMALL,
                            isDownloading = changePercent == null
                        )

                        Text(
                            text = stars,
                            style = satoshiBold(16),
                            color = MaterialTheme.colors.onSurfaceDim,
                            modifier = Modifier.semantics { invisibleToUser() }
                        )
                    }
                } else {
                    PriceChangeValueView(
                        change = changeValue,
                        isDownloading = changeValue == null
                    )
                }

                Text(
                    text = "(${if (changePercent?.isInfinite() == true) "-" else "$formattedChange%"})",
                    style = satoshiBold(16),
                    color = if (changePercent?.isInfinite() == true) MaterialTheme.colors.onSurfaceDim else roundedChange?.changeColor() ?: MaterialTheme.colors.onSurfaceDim,
                    modifier = Modifier
                        .shimmeringModifier(changePercent == null)
                        .semantics { invisibleToUser() }
                )
            }
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalAlignment = Alignment.Start,
            modifier = sectionModifier()
                .semantics(mergeDescendants = true) {
                    contentDescription = lastSyncedAccessibilityLabel
                },
        ) {
            Text(
                text = "Last Synced",
                style = satoshiBold(12),
                color = MaterialTheme.colors.onSurfaceDim,
                modifier = Modifier.semantics { invisibleToUser() }
            )

            Text(
                text = synced.formatDate(if (context.is24HourClock) DateFormat.MMMddyyyyHHmm else DateFormat.MMMddyyyyhhmma) ?: "",
                style = satoshiBold(16),
                color = MaterialTheme.colors.onSurface,
                modifier = Modifier.semantics { invisibleToUser() }
            )
        }
    }
}

private fun LazyListScope.CoinsList(
    coin: Toplist,
    wallet: AddressImport,
    coins: List<Toplist>?,
    sortedTokens: List<TokenInfo>,
    hideData: Boolean,
    navigateToCoin: (Toplist) -> Unit
) {
    val horizontalPadding = Modifier.padding(horizontal = 16.dp)
    val hasMainCoin = wallet.balance.toDoubleOrNull() != null
    val totalCount = (if (hasMainCoin) 1 else 0) + sortedTokens.size

    item {
        Box(
            modifier = horizontalPadding
                .clearAndSetSemantics { },
        ) {
            WalletHeaderView()
        }
    }

    if (hasMainCoin) {
        item {
            Box(
                modifier = horizontalPadding.then(
                    Modifier.padding(bottom = if (sortedTokens.isEmpty()) 32.dp else 16.dp)
                )
            ) {
                WalletCoinView(
                    toplist = coin,
                    token = wallet.toTokenInfo(),
                    hideData = hideData,
                    isDownloading = coins == null,
                    index = 0,
                    totalCount = totalCount,
                    navigateToCoin = {
                        navigateToCoin.invoke(coin)
                    }
                )
            }
        }
    }

    itemsIndexed(sortedTokens) { index, token ->
        val coin = coins?.firstOrNull { it.id == token.tokenId }
        val isLast = index == sortedTokens.lastIndex
        val itemIndex = (if (hasMainCoin) 1 else 0) + index

        Box(
            modifier = horizontalPadding.then(
                Modifier.padding(bottom = if (isLast) 32.dp else 16.dp)
            )
        ) {
            WalletCoinView(
                toplist = coin,
                token = token,
                hideData = hideData,
                isDownloading = coins == null,
                index = itemIndex,
                totalCount = totalCount,
                navigateToCoin = {
                    coin?.let {
                        navigateToCoin.invoke(it)
                    }
                }
            )
        }
    }
}