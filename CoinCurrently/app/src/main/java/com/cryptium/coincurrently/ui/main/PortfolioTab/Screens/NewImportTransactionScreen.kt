package com.cryptium.coincurrently.ui.main.PortfolioTab.Screens

import Chain
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.invisibleToUser
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.cryptium.coincurrently.ui.main.Compose.*
import com.cryptium.coincurrently.ui.main.Extensions.*
import com.cryptium.coincurrently.ui.main.General.Components.CustomTopAppBar
import com.cryptium.coincurrently.ui.main.General.InputFields.TextInputField
import com.cryptium.coincurrently.ui.main.General.NavigationViewModel
import com.cryptium.coincurrently.ui.main.Repositories.PortfolioRepository
import com.cryptium.coincurrently.ui.main.SettingsTab.Achievements.Managers.AchievementManager
import com.cryptium.coincurrently.ui.main.General.OnLifecycleEvent
import com.cryptium.coincurrently.ui.main.MarketTab.CryptoDetail.Popups.SelectCoinSheet
import com.cryptium.coincurrently.ui.main.analytics.CurrencySelected
import com.cryptium.coincurrently.ui.main.analytics.AnalyticsManager
import com.cryptium.coincurrently.ui.main.analytics.ScreenLabel
import com.cryptium.coincurrently.ui.main.analytics.SheetLabel
import com.cryptium.coincurrently.ui.main.analytics.SheetOpened
import com.cryptium.coincurrently.ui.main.PortfolioTab.ViewModels.NewImportTransactionViewModel
import com.cryptium.coincurrently.ui.main.PortfolioTab.ViewModels.NewImportTransactionViewModelFactory
import com.google.accompanist.pager.ExperimentalPagerApi
import kotlinx.coroutines.launch
import networking.ResponseModels.*
import SearchScreenCase

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPagerApi::class)
@Composable
fun NewImportTransactionScreen(
    navigationViewModel: NavigationViewModel,
    modifier: Modifier,
    popToRoot: Boolean
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val viewModel: NewImportTransactionViewModel = viewModel(
        factory = NewImportTransactionViewModelFactory()
    )

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var isCoinsViewShowing by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.getToplist()
    }

    OnLifecycleEvent { _, event ->
        when (event) {
            Lifecycle.Event.ON_RESUME -> {
                scope.launch {
                    viewModel.getToplist()
                }
            }
            else -> { }
        }
    }

    BackHandler(onBack = {
        navigationViewModel.navigateBack()
    })

    Box(
        modifier = Modifier
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    focusManager.clearFocus()
                    keyboardController?.hide()
                })
            }
    ) {
        Column(
            modifier = modifier
        ) {
            CustomTopAppBar(
                title = "Import Wallet",
                navigateBack = {
                    navigationViewModel.navigateBack()
                }
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.Top),
                modifier = Modifier
                    .weight(1f)
                    .wrapContentHeight()
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                AddressField(
                    addressInput = viewModel.address,
                    selectedChain = viewModel.selectedChain,
                    showError = viewModel.showImportError,
                    changeChain = {
                        AnalyticsManager.log(
                            SheetOpened(ScreenLabel.NewImportTransaction, SheetLabel.CoinSearch),
                        )
                        isCoinsViewShowing = true
                    }
                )

                WalletNameField(
                    nameInput = viewModel.walletName
                )
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(horizontal = 16.dp)
            ) {
                Separator()

                ActionButtons(
                    isLoading = viewModel.isImporting,
                    isEnabled = viewModel.address.value.isNotEmpty() && viewModel.walletName.value.count() <= 40,
                    close = {
                        dismiss(
                            navigationViewModel = navigationViewModel,
                            popToRoot = popToRoot
                        )
                    },
                    import = {
                        scope.launch {
                            val previousCount =
                                PortfolioRepository.portfolio?.data?.data?.allTransactionsCount ?: 0
                            val importedId = viewModel.importWallet(context)
                            if (importedId != null) {
                                navigationViewModel.pendingPortfolioPreviousTransactionCount = previousCount
                                navigationViewModel.pendingPortfolioTransactionId = importedId.toString()

                                AchievementManager.checkPortfolioTransactionUnlock(previousCount)

                                dismiss(
                                    navigationViewModel = navigationViewModel,
                                    popToRoot = popToRoot
                                )
                            }
                        }
                    }
                )
            }
        }

        if (isCoinsViewShowing) {
            val ethereumImage = viewModel.chainCoins?.first { it.id == "ethereum" }?.image

            SelectCoinSheet(
                searchScreenCase = SearchScreenCase.FixedList(
                    list = (viewModel.chainCoins ?: listOf()) + listOf(
                        Toplist(
                            id = "token",
                            symbol = "token",
                            name = "Ethereum Token",
                            image = ethereumImage
                        )
                    ),
                ),
                fromScreen = ScreenLabel.NewImportTransaction,
                highlightId = viewModel.selectedChain.value.coinId,
                hideId = null,
                selectedCoin = { coin ->
                    val symbol = coin?.symbol ?: return@SelectCoinSheet

                    AnalyticsManager.log(
                        CurrencySelected(
                            fromScreen = ScreenLabel.NewImportTransaction,
                            currency = symbol.uppercase(),
                        ),
                    )
                    viewModel.selectedChain.value = Chain.fromString(symbol) ?: Chain.BTC
                },
                close = {
                    isCoinsViewShowing = false
                }
            )
        }
    }
}

@Composable
private fun AddressField(
    addressInput: MutableState<String>,
    selectedChain: MutableState<Chain>,
    showError: Boolean,
    changeChain: () -> Unit
) {
    val accessibilityValue = if (addressInput.value.isEmpty()) {
        "Enter wallet address"
    } else {
        addressInput.value
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Address Name",
            style = satoshiMedium(14),
            color = if(showError) MaterialTheme.colors.trendDown else MaterialTheme.colors.onSurfaceDim,
            modifier = Modifier.semantics { invisibleToUser() }
        )

        Row {
            TextInputField(
                input = addressInput.value,
                startIcon = null,
                placeholder = "Enter address",
                accessibilityLabel = accessibilityValue,
                outlineColor = if(showError) MaterialTheme.colors.trendDown else MaterialTheme.colors.outlineVariant,
                onInputChange = {
                    addressInput.value = it
                },
                onDone = { },
                trailingView = {
                    CurrencyButton(
                        title = selectedChain.value.name.uppercase(),
                        selectNewCurrency = {
                            changeChain.invoke()
                        }
                    )
                },
                modifier = Modifier
                    .weight(1f)
            )
        }

        Text(
            text = if(showError) "Invalid address or chain" else "Select the chain that matches your wallet.",
            style = satoshiMedium(14),
            color = if(showError) MaterialTheme.colors.trendDown else MaterialTheme.colors.onSurfaceDimmer,
            modifier = Modifier.semantics { invisibleToUser() }
        )
    }
}


@Composable
private fun WalletNameField(
    nameInput: MutableState<String>,
) {
    val accessibilityValue = if (nameInput.value.isEmpty()) {
        "Enter wallet name, optional"
    } else {
        nameInput.value
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = "Wallet Name",
            style = satoshiMedium(14),
            color = MaterialTheme.colors.onSurfaceDim,
            modifier = Modifier.semantics { invisibleToUser() }
        )

        Row {
            TextInputField(
                input = nameInput.value,
                startIcon = null,
                placeholder = "Enter name (Optional)",
                accessibilityLabel = accessibilityValue,
                outlineColor = if (nameInput.value.count() > 40) MaterialTheme.colors.trendDown else MaterialTheme.colors.outlineVariant,
                onInputChange = {
                    nameInput.value = it
                },
                onDone = { },
                modifier = Modifier
                    .weight(1f)
            )
        }

        Text(
            text = "${nameInput.value.count()} / 40 characters",
            style = satoshiMedium(14),
            color = if (nameInput.value.count() > 40) MaterialTheme.colors.trendDown else MaterialTheme.colors.onSurfaceDimmer,
            modifier = Modifier.semantics { invisibleToUser() }
        )
    }
}

@Composable
private fun ActionButtons(
    isEnabled: Boolean,
    isLoading: Boolean,
    close: () -> Unit,
    import: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .padding(bottom = 24.dp)
    ) {
        SecondaryButton(
            text = "Close",
            onClick = {
                close()
            },
            modifier = Modifier
                .weight(1f)
        )

        PrimaryButton(
            text = "Import",
            isEnabled = isEnabled,
            isLoading = isLoading,
            accessibilityLabel = if (isEnabled) {
                "Import"
            } else {
                "Import: Disabled because address is empty or wallet name is too long"
            },
            onClick = {
                import.invoke()
            },
            modifier = Modifier
                .weight(1f)
        )
    }
}

private fun dismiss(
    navigationViewModel: NavigationViewModel,
    popToRoot: Boolean
) {
    if (popToRoot) {
        navigationViewModel.popToRoot()
    } else {
        navigationViewModel.navigateBack()
    }
}
