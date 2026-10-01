package com.cryptium.coincurrently.features.Portfolio
import com.cryptium.coincurrently.app.SearchScreenCase
import com.cryptium.coincurrently.app.SearchCase
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.invisibleToUser
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.material.MaterialTheme
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextAlign
import com.cryptium.coincurrently.R
import com.cryptium.coincurrently.sharedui.theme.*
import com.cryptium.coincurrently.sharedui.modifiers.*
import com.cryptium.coincurrently.sharedui.components.AccessibilityLayout
import com.cryptium.coincurrently.sharedui.components.AdaptiveStack
import com.cryptium.coincurrently.sharedui.components.Separator
import com.cryptium.coincurrently.sharedui.components.contentScreenWidth
import com.cryptium.coincurrently.sharedui.buttons.CurrencyButton
import com.cryptium.coincurrently.sharedui.buttons.PrimaryButton
import com.cryptium.coincurrently.sharedui.buttons.SecondaryButton
import com.cryptium.coincurrently.sharedui.inputs.CustomFreeInputField
import com.cryptium.coincurrently.features.Portfolio.Models.PortfolioSwapTransaction
import com.cryptium.coincurrently.features.Portfolio.PortfolioRepository
import com.cryptium.coincurrently.features.Achievements.Managers.AchievementManager
import com.cryptium.coincurrently.core.extensions.*
import com.cryptium.coincurrently.core.localization.Localization
import com.cryptium.coincurrently.sharedui.navigation.CustomTopAppBar
import com.cryptium.coincurrently.sharedui.inputs.NumericInputField
import com.cryptium.coincurrently.sharedui.inputs.TextInputField
import com.cryptium.coincurrently.app.NavigationViewModel
import com.cryptium.coincurrently.sharedui.components.SnackBarType
import com.cryptium.coincurrently.sharedui.components.OnLifecycleEvent
import com.cryptium.coincurrently.core.analytics.CurrencySelected
import com.cryptium.coincurrently.core.analytics.AnalyticsManager
import com.cryptium.coincurrently.core.analytics.ScreenLabel
import com.cryptium.coincurrently.core.analytics.SheetLabel
import com.cryptium.coincurrently.core.analytics.SheetOpened
import com.cryptium.coincurrently.features.Settings.CurrencySelector.CurrencyOptionsSheet
import com.cryptium.coincurrently.features.CoinDetail.Sheets.SelectCoinSheet
import com.cryptium.coincurrently.features.Portfolio.Enums.PortfolioTransactionType
import com.cryptium.coincurrently.features.Portfolio.Sheets.DateRangePickerSheet
import com.cryptium.coincurrently.features.Portfolio.Sheets.TimeRangePickerSheet
import com.cryptium.coincurrently.features.Portfolio.ViewModels.AddSwapTransactionViewModel
import com.cryptium.coincurrently.features.Portfolio.ViewModels.AddSwapTransactionViewModelFactory
import com.google.accompanist.pager.ExperimentalPagerApi
import kotlinx.coroutines.launch
import com.cryptium.coincurrently.core.networking.ResponseModels.*
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPagerApi::class)
@Composable
fun AddSwapTransactionScreen(
    navigationViewModel: NavigationViewModel,
    modifier: Modifier,
    transactionType: PortfolioTransactionType,
    currencyDetail: CryptoDetailInfoModel?,
    transaction: PortfolioSwapTransaction?,
    popToRoot: Boolean
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val configuration = LocalConfiguration.current
    val screenWidth = contentScreenWidth()

    val viewModel: AddSwapTransactionViewModel = viewModel(
        factory = AddSwapTransactionViewModelFactory(
            transactionType = transactionType,
            currencyDetail = currencyDetail,
            transaction = transaction
        )
    )

    var showCurrencySelector by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var showDateDialog by remember { mutableStateOf(false) }
    var showTimeDialog by remember { mutableStateOf(false) }

    var isCoinsViewShowing by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.getCryptoDetail()
    }

    OnLifecycleEvent { _, event ->
        when (event) {
            Lifecycle.Event.ON_RESUME -> {
                scope.launch {
                    viewModel.getCryptoDetail()
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
                title = Localization.format(
                    "Swapped %@",
                    viewModel.currencyDetail?.symbol?.uppercase() ?: "",
                ),
                accessibilityTitle = Localization.format(
                    "Swapped %@",
                    viewModel.currencyDetail?.name ?: viewModel.currencyDetail?.symbol?.uppercase() ?: "",
                ),
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
                FromAmountField(
                    amountInput = viewModel.fromAmount,
                    cryptoDetailInfoModel = viewModel.currencyDetail
                )

                UnitPriceField(
                    unitPriceInput = viewModel.fromAverageCost,
                    cryptoDetailInfoModel = viewModel.currencyDetail,
                    selectedCurrency = viewModel.selectedCurrency.value,
                    selectNewCurrency = {
                        AnalyticsManager.log(
                            SheetOpened(ScreenLabel.PortfolioSwapTransaction, SheetLabel.CurrencySelector),
                        )
                        showCurrencySelector = true
                    }
                )

                ToAmountField(
                    amountInput = viewModel.toAmount,
                    toSymbol = viewModel.toInformation.second,
                    selectCoin = {
                        AnalyticsManager.log(
                            SheetOpened(ScreenLabel.PortfolioSwapTransaction, SheetLabel.CoinSearch),
                        )
                        isCoinsViewShowing = true
                    }
                )

                FeesField(
                    feesInput = viewModel.fees,
                    selectedCurrency = viewModel.selectedCurrency.value
                )

                val isHorizontal = AccessibilityLayout.isHorizontal

                AdaptiveStack(
                    isHorizontal = isHorizontal,
                    spacing = 16.dp,
                    horizontalAlignment = Alignment.Start,
                    verticalAlignment = Alignment.Top,
                ) {
                    DateField(
                        selectedDate = viewModel.date,
                        selectDate = {
                            AnalyticsManager.log(
                                SheetOpened(ScreenLabel.PortfolioSwapTransaction, SheetLabel.NewTransactionDate),
                            )
                            showDateDialog = true
                        },
                        modifier = sectionModifier(),
                    )

                    TimeField(
                        selectedDate = viewModel.date,
                        selectDate = {
                            AnalyticsManager.log(
                                SheetOpened(ScreenLabel.PortfolioSwapTransaction, SheetLabel.NewTransactionTime),
                            )
                            showTimeDialog = true
                        },
                        modifier = if (isHorizontal) {
                            Modifier.width((screenWidth.dpToPx() / 2.6f).pxToDp())
                        } else {
                            sectionModifier()
                        },
                    )
                }

                NoteField(
                    noteInput = viewModel.note
                )
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(24.dp),
                modifier = Modifier
                    .padding(horizontal = 16.dp)
            ) {
                Separator()

                ActionButtonsView(
                    viewModel.isInputValid,
                    viewModel.isLoading,
                    navigateBack = {
                        dismiss(
                            navigationViewModel = navigationViewModel,
                            popToRoot = popToRoot
                        )
                    }, submit = {
                        scope.launch {
                            val previousCount = if (transaction == null) {
                                PortfolioRepository.portfolio?.data?.data?.allTransactionsCount ?: 0
                            } else {
                                -1
                            }
                            val swap = viewModel.addTransaction(context)
                            if (swap != null) {
                                navigationViewModel.pendingPortfolioPreviousTransactionCount = previousCount

                                if (previousCount >= 0) {
                                    AchievementManager.checkPortfolioTransactionUnlock(previousCount)
                                }

                                val symbol = viewModel.currencyDetail?.symbol?.uppercase() ?: "Coin"
                                navigationViewModel.showSnackbar(
                                    context = context,
                                    type = SnackBarType.SUCCESS,
                                    message = "$symbol transaction added to your portfolio!",
                                    buttonTitle = "View Details",
                                    callback = {
                                        navigationViewModel.pendingPortfolioOpenTransactionId =
                                            swap.id.toString()
                                    },
                                )

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

        if (showDateDialog) {
            DateRangePickerSheet(
                fromScreen = ScreenLabel.PortfolioSwapTransaction,
                startDate = viewModel.date.value,
                close = {
                    showDateDialog = false
                },
                applyDate = {
                    viewModel.date.value = it.withTime(
                        hour = viewModel.date.value.hour,
                        minute = viewModel.date.value.minute
                    )
                }
            )
        }

        if (showTimeDialog) {
            TimeRangePickerSheet(
                fromScreen = ScreenLabel.PortfolioSwapTransaction,
                startDate = viewModel.date.value,
                close = {
                    showTimeDialog = false
                },
                applyDate = {
                    viewModel.date.value = it
                }
            )
        }

        if (showCurrencySelector) {
            CurrencyOptionsSheet(
                selectedOption = viewModel.selectedCurrency.value,
                newOptionSelected = {
                    AnalyticsManager.log(
                        CurrencySelected(
                            fromScreen = ScreenLabel.PortfolioSwapTransaction,
                            currency = it.currency,
                        ),
                    )
                    viewModel.selectedCurrency.value = it
                },
                close = {
                    showCurrencySelector = false
                }
            )
        }

        if (isCoinsViewShowing) {
            SelectCoinSheet(
                searchScreenCase = SearchScreenCase.Search(
                    list = SearchCase.OnlyCoins
                ),
                fromScreen = ScreenLabel.PortfolioSwapTransaction,
                highlightId = viewModel.toInformation.first,
                hideId = viewModel.currencyDetail?.id,
                selectedCoin = { coin ->
                    val id = coin?.id ?: return@SelectCoinSheet
                    val symbol = coin.symbol ?: return@SelectCoinSheet

                    AnalyticsManager.log(
                        CurrencySelected(
                            fromScreen = ScreenLabel.PortfolioSwapTransaction,
                            currency = symbol.uppercase(),
                        ),
                    )
                    viewModel.toInformation = Pair(id, symbol)
                },
                close = {
                    isCoinsViewShowing = false
                }
            )
        }
    }
}

@Composable
private fun FromAmountField(
    amountInput: MutableState<Double?>,
    cryptoDetailInfoModel: CryptoDetailInfoModel?,
) {
    val accessibleAmount = amountInput.value?.numberAccessible() ?: ""
    val amountLabel = if (amountInput.value == null) {
        Localization.string("Amount")
    } else {
        Localization.format("Amount: %@", accessibleAmount)
    }
    val accessibilityValue = "${Localization.string("Swap From")}. ${
        Localization.format("%@. Textfield", amountLabel)
    }"

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = Localization.string("Swap From"),
            style = satoshiMedium(14),
            color = MaterialTheme.colors.onSurfaceDim,
            modifier = Modifier.semantics { invisibleToUser() }
        )

        Row {
            NumericInputField(
                input = amountInput.value,
                placeholder = Localization.string("Enter Amount"),
                accessibilityValue = accessibilityValue,
                onInputChange = {
                    amountInput.value = it
                },
                trailingView = {
                    Text(
                        text = cryptoDetailInfoModel?.symbol?.uppercase() ?: "",
                        style = satoshiBold(18),
                        color = MaterialTheme.colors.onSurfaceDimmer,
                        modifier = Modifier.semantics { invisibleToUser() }
                    )
                },
                modifier = Modifier
                    .weight(1f)
            )
        }
    }
}

@Composable
private fun UnitPriceField(
    unitPriceInput: MutableState<Double?>,
    cryptoDetailInfoModel: CryptoDetailInfoModel?,
    selectedCurrency: CurrencySelectionModel,
    selectNewCurrency: () -> Unit,
) {
    val currentPrice = cryptoDetailInfoModel?.market_data?.current_price?.getSelectedCurrency(
        currency = selectedCurrency.code
    )
    val accessiblePrice = (unitPriceInput.value ?: currentPrice)?.currencyAccessible(
        currency = selectedCurrency
    ) ?: ""
    val accessibilityValue = Localization.format(
        "%@. Textfield",
        Localization.format("Unit Price: %@", accessiblePrice),
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = Localization.format(
                "Unit Price (%@)",
                cryptoDetailInfoModel?.symbol?.uppercase() ?: "",
            ),
            style = satoshiMedium(14),
            color = MaterialTheme.colors.onSurfaceDim,
            modifier = Modifier.semantics { invisibleToUser() }
        )

        Row {
            NumericInputField(
                input = unitPriceInput.value,
                placeholder = (currentPrice ?: 0.0).formatCurrencyInput(
                    currency = selectedCurrency
                ),
                currency = selectedCurrency,
                forceShowCurrency = !selectedCurrency.suffix,
                accessibilityValue = accessibilityValue,
                onInputChange = {
                    unitPriceInput.value = it
                },
                trailingView = {
                    CurrencyButton(
                        title = selectedCurrency.code.uppercase(),
                        selectNewCurrency = selectNewCurrency,
                    )
                },
                modifier = Modifier
                    .weight(1f)
            )
        }
    }
}

@Composable
private fun ToAmountField(
    amountInput: MutableState<Double?>,
    toSymbol: String,
    selectCoin: () -> Unit
) {
    val accessibleAmount = amountInput.value?.numberAccessible() ?: ""
    val amountLabel = if (amountInput.value == null) {
        Localization.string("Amount")
    } else {
        Localization.format("Amount: %@", accessibleAmount)
    }
    val accessibilityValue = "${Localization.string("Swap To")}. ${
        Localization.format("%@. Textfield", amountLabel)
    }"

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = Localization.string("Swap To"),
            style = satoshiMedium(14),
            color = MaterialTheme.colors.onSurfaceDim,
            modifier = Modifier.semantics { invisibleToUser() }
        )

        Row {
            NumericInputField(
                input = amountInput.value,
                placeholder = Localization.string("Enter Amount"),
                accessibilityValue = accessibilityValue,
                onInputChange = {
                    amountInput.value = it
                },
                trailingView = {
                    CurrencyButton(
                        title = toSymbol.uppercase(),
                        selectNewCurrency = {
                            selectCoin.invoke()
                        },
                    )
                },
                modifier = Modifier
                    .weight(1f)
            )
        }
    }
}

@Composable
private fun FeesField(
    feesInput: MutableState<Double?>,
    selectedCurrency: CurrencySelectionModel
) {
    val accessibleFees = feesInput.value?.currencyAccessible(currency = selectedCurrency) ?: ""
    val accessibilityValue = Localization.format(
        "%@. Textfield",
        if (feesInput.value == null) {
            Localization.string("Fees")
        } else {
            Localization.format("Transaction fee: %@", accessibleFees)
        },
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = Localization.string("Fees"),
            style = satoshiMedium(14),
            color = MaterialTheme.colors.onSurfaceDim,
            modifier = Modifier.semantics { invisibleToUser() }
        )

        Row {
            NumericInputField(
                input = feesInput.value,
                placeholder = Localization.string("Enter Fees"),
                currency = selectedCurrency,
                accessibilityValue = accessibilityValue,
                onInputChange = {
                    feesInput.value = it
                },
                modifier = Modifier
                    .weight(1f)
            )
        }
    }
}

@Composable
private fun DateField(
    selectedDate: MutableState<Date>,
    selectDate: () -> Unit,
    modifier: Modifier
) {
    val formattedDate = selectedDate.value.formatDate(DateFormat.MMMddyyyy) ?: ""
    val accessibilityLabel = Localization.format(
        "Date: %@",
        selectedDate.value.accessibleLongDateDescription(),
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
    ) {
        Text(
            text = Localization.string("Date"),
            style = satoshiMedium(14),
            color = MaterialTheme.colors.onSurfaceDim,
            modifier = Modifier.semantics { invisibleToUser() }
        )

        Box(
            modifier = Modifier
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    selectDate()
                }
                .semantics(mergeDescendants = true) {
                    role = Role.Button
                    contentDescription = accessibilityLabel
                    onClick(label = "Change date") {
                        selectDate()
                        true
                    }
                }
        ) {
            Row {
                TextInputField(
                    input = formattedDate,
                    accessibilityLabel = formattedDate,
                    startIcon = R.drawable.date,
                    isEnabled = false,
                    onInputChange = { },
                    onDone = { },
                    modifier = Modifier
                        .weight(1f)
                )
            }
        }
    }
}

@Composable
private fun TimeField(
    selectedDate: MutableState<Date>,
    selectDate: () -> Unit,
    modifier: Modifier
) {
    val context = LocalContext.current
    val formattedTime = selectedDate.value.formatDate(
        if (context.is24HourClock) DateFormat.HHmm else DateFormat.hhmma
    ) ?: ""
    val accessibilityLabel = Localization.format(
        "Time: %@",
        selectedDate.value.accessibleTimeDescription(context.is24HourClock),
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
    ) {
        Text(
            text = Localization.string("Time"),
            style = satoshiMedium(14),
            color = MaterialTheme.colors.onSurfaceDim,
            modifier = Modifier.semantics { invisibleToUser() }
        )

        Box(
            modifier = Modifier
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    selectDate()
                }
                .semantics(mergeDescendants = true) {
                    role = Role.Button
                    contentDescription = accessibilityLabel
                    onClick(label = "Change time") {
                        selectDate()
                        true
                    }
                }
        ) {
            Row {
                TextInputField(
                    input = formattedTime,
                    accessibilityLabel = formattedTime,
                    startIcon = R.drawable.time,
                    isEnabled = false,
                    onInputChange = { },
                    onDone = { }
                )
            }
        }
    }
}

@Composable
private fun NoteField(
    noteInput: MutableState<String>,
) {
    val accessibilityLabel = Localization.format(
        "%@. Textfield",
        if (noteInput.value.isEmpty()) {
            Localization.string("Input notes here, optional")
        } else {
            Localization.format("Note: %@", noteInput.value)
        },
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = Localization.string("Notes"),
            style = satoshiMedium(14),
            color = MaterialTheme.colors.onSurfaceDim,
            modifier = Modifier.semantics { invisibleToUser() }
        )

        CustomFreeInputField(
            startingValue = noteInput.value,
            placeholder = Localization.string("Enter notes (Optional)"),
            type = KeyboardType.Text,
            maxLines = 10,
            showError = noteInput.value.count() > 200,
            accessibilityLabel = accessibilityLabel,
            valueChanged = {
                noteInput.value = it
            }
        )

        Text(
            text = Localization.format("%@ / 200 characters", noteInput.value.count().toString()),
            style = satoshiMedium(14),
            color = if (noteInput.value.count() > 200) MaterialTheme.colors.trendDown else MaterialTheme.colors.onSurfaceDimmer,
            modifier = Modifier
                .padding(end = 16.dp)
                .semantics { invisibleToUser() }
        )
    }
}

@Composable
private fun ActionButtonsView(
    isInputValid: Boolean,
    isLoading: Boolean,
    navigateBack: () -> Unit,
    submit: () -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .padding(bottom = 24.dp)
    ) {
        SecondaryButton(
            text = Localization.string("Cancel"),
            modifier = Modifier
                .weight(1f)
                .widthIn(max = 400.dp)
        ) {
            navigateBack.invoke()
        }

        PrimaryButton(
            text = Localization.string("Save"),
            isEnabled = isInputValid,
            isLoading = isLoading,
            accessibilityLabel = Localization.format(
                "Save: %@",
                if (isInputValid) "" else Localization.string("Disabled because invalid or no amount is entered"),
            ),
            modifier = Modifier
                .weight(1f)
                .widthIn(max = 400.dp)
        ) {
            submit.invoke()
        }
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

