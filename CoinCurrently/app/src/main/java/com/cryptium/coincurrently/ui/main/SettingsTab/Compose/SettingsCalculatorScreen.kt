package com.cryptium.coincurrently.ui.main.SettingsTab.Compose

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.invisibleToUser
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cryptium.coincurrently.ui.main.Compose.CachedUrlImage
import androidx.compose.material.MaterialTheme
import androidx.compose.ui.Alignment
import com.cryptium.coincurrently.ui.main.Compose.*
import com.cryptium.coincurrently.ui.main.Extensions.formatCurrency
import com.cryptium.coincurrently.ui.main.Extensions.numberAccessible
import com.cryptium.coincurrently.ui.main.Extensions.poppinsRegular
import com.cryptium.coincurrently.ui.main.Extensions.satoshiBold
import com.cryptium.coincurrently.ui.main.General.Components.CustomTopAppBar
import com.cryptium.coincurrently.ui.main.General.Enums.FormattingThreshold
import com.cryptium.coincurrently.ui.main.General.GlobalFunctions
import com.cryptium.coincurrently.ui.main.General.InputFields.NumericInputField
import com.cryptium.coincurrently.ui.main.General.NavigationViewModel
import com.cryptium.coincurrently.ui.main.General.OnLifecycleEvent
import com.cryptium.coincurrently.ui.main.SettingsTab.Popups.CurrencyOptionsSheet
import com.cryptium.coincurrently.ui.main.SettingsTab.ViewModels.SettingsCalculatorViewModel
import com.cryptium.coincurrently.ui.main.SettingsTab.ViewModels.SettingsCalculatorViewModelFactory
import com.cryptium.coincurrently.ui.main.SettingsTab.ViewModels.SettingsCalculatorViewModelImp
import com.cryptium.coincurrently.ui.main.analytics.CurrencySelected
import com.cryptium.coincurrently.ui.main.analytics.AnalyticsManager
import com.cryptium.coincurrently.ui.main.analytics.ScreenLabel
import com.cryptium.coincurrently.ui.main.analytics.SheetLabel
import com.cryptium.coincurrently.ui.main.analytics.SheetOpened
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import networking.ResponseModels.CurrencySelectionModel
import networking.ResponseModels.Toplist
import kotlin.math.abs
import kotlin.time.Duration.Companion.seconds

@Composable
fun SettingsCalculatorScreen(
    navigationViewModel: NavigationViewModel,
    modifier: Modifier
) {
    val viewModel: SettingsCalculatorViewModel = viewModel<SettingsCalculatorViewModelImp>(
        factory = SettingsCalculatorViewModelFactory()
    )

    var showCurrencySelector by remember { mutableStateOf(false) }
    var selectedCurrency by remember { mutableStateOf(GlobalFunctions.getSelectedCurrency()) }
    var isRefreshing: Boolean by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.getCalculatorPrices()
    }

    OnLifecycleEvent { _, event ->
        when (event) {
            Lifecycle.Event.ON_RESUME -> {
                coroutineScope.launch {
                    viewModel.getCalculatorPrices()
                }
            }
            else -> { }
        }
    }

    BackPressHandler(onBackPressed = {
        if (showCurrencySelector) {
            showCurrencySelector = false
        } else {
            navigationViewModel.navigateBack()
        }
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
                title = "Converter",
                navigateBack = {
                    navigationViewModel.navigateBack()
                },
            )

            SwipeRefresh(
                state = rememberSwipeRefreshState(
                    isRefreshing = isRefreshing
                ),
                onRefresh = {
                    isRefreshing = true

                    coroutineScope.launch {
                        launch {
                            viewModel.getCalculatorPrices()
                            isRefreshing = false
                        }

                        delay(10.seconds)
                        isRefreshing = false
                    }
                },
            ) {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                ) {
                    ValueAmountInputField(
                        valueAmount = viewModel.input,
                        selectedCurrency = selectedCurrency,
                        valueAmountChanged = {
                            if (it != 0.0 && viewModel.input == 0.0) {
                                com.cryptium.coincurrently.ui.main.SettingsTab.Achievements.Managers.AchievementManager.increment(
                                    com.cryptium.coincurrently.ui.main.SettingsTab.Achievements.Models.AchievementCounterKey.converterUses
                                )
                            }
                            viewModel.input = it
                        },
                        selectNewCurrency = {
                            AnalyticsManager.log(
                                SheetOpened(ScreenLabel.Converter, SheetLabel.CurrencySelector),
                            )
                            showCurrencySelector = true
                        }
                    )

                    Column(
                        modifier = Modifier
                            .verticalScroll(rememberScrollState())
                            .pointerInput(Unit) {
                                detectTapGestures(onTap = {
                                    focusManager.clearFocus()
                                    keyboardController?.hide()
                                })
                            }
                    ) {
                        Spacer(modifier = Modifier.height(16.dp))

                        val totalCount = viewModel.calculatorList?.size ?: 25

                        for (index in 0 until totalCount) {
                            val coin = viewModel.calculatorList?.get(index)
                            val amount = viewModel.getAmount(
                                id = coin?.id,
                                selectedCurrency = selectedCurrency
                            )

                            CalculatorItem(
                                coin = coin,
                                price = amount?.formatCurrency(
                                    threshold = FormattingThreshold.MILLION,
                                    currency = CurrencySelectionModel.empty()
                                ),
                                amount = amount,
                                input = viewModel.input,
                                selectedCurrency = selectedCurrency,
                                index = index,
                                totalCount = totalCount
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }

        if (showCurrencySelector) {
            CurrencyOptionsSheet(
                selectedOption = selectedCurrency,
                newOptionSelected = {
                    selectedCurrency = it
                    GlobalFunctions.setSelectedCurrency(selectedCurrency)
                    AnalyticsManager.log(
                        CurrencySelected(
                            fromScreen = ScreenLabel.Converter,
                            currency = selectedCurrency.currency,
                        ),
                    )
                },
                close = {
                    showCurrencySelector = false
                }
            )
        }
    }
}

@Composable
private fun ValueAmountInputField(
    valueAmount: Double,
    selectedCurrency: CurrencySelectionModel,
    valueAmountChanged: (Double) -> Unit,
    selectNewCurrency: ()  -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .padding(top = 16.dp)
    ) {
        NumericInputField(
            input = valueAmount,
            isFromConverter = true,
            onInputChange = {
                valueAmountChanged(it ?: 0.0)
            },
            trailingView = {
                CurrencyButton(
                    title = selectedCurrency.code.uppercase(),
                    selectNewCurrency = selectNewCurrency
                )
            },
            modifier = Modifier
                .weight(1f)
        )
    }
}

@Composable
private fun CalculatorItem(
    coin: Toplist?,
    price: String?,
    amount: Double?,
    input: Double,
    selectedCurrency: CurrencySelectionModel,
    index: Int,
    totalCount: Int
) {
    val accessibilityLabel = calculatorItemAccessibilityLabel(
        coin = coin,
        amount = amount,
        input = input,
        selectedCurrency = selectedCurrency,
        index = index,
        totalCount = totalCount
    )

    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(
                vertical = 16.dp,
                horizontal = 24.dp
            )
            .then(
                if (coin == null) {
                    Modifier.clearAndSetSemantics { invisibleToUser() }
                } else {
                    Modifier.clearAndSetSemantics { contentDescription = accessibilityLabel }
                }
            )
    ) {
        CachedUrlImage(
            model = coin?.image,
            size = 32.dp,
        )

        Column(
            horizontalAlignment = Alignment.Start,
            modifier = Modifier
                .weight(1f)
        ) {
            Text(
                text = coin?.symbol?.uppercase() ?: "0000",
                style = satoshiBold(16),
                color = MaterialTheme.colors.onSurface,
                textAlign = TextAlign.Start,
                modifier = Modifier
                    .shimmeringModifier(coin?.symbol == null)
            )

            Text(
                text = coin?.name ?: "00000",
                style = satoshiBold(12),
                color = MaterialTheme.colors.onSurfaceDim,
                textAlign = TextAlign.Start,
                modifier = Modifier
                    .shimmeringModifier(coin?.name == null)
            )
        }

        Text(
            text = price ?: "000000",
            style = poppinsRegular(18),
            color = MaterialTheme.colors.primaryTextColor,
            textAlign = TextAlign.End,
            modifier = Modifier
                .shimmeringModifier(price == null)
                .weight(1f)
        )
    }
}

private fun calculatorItemAccessibilityLabel(
    coin: Toplist?,
    amount: Double?,
    input: Double,
    selectedCurrency: CurrencySelectionModel,
    index: Int,
    totalCount: Int
): String {
    val name = coin?.name ?: ""
    val places = if (abs(amount ?: 0.0) < 0.1) 7 else 4
    val amountAccessible = amount?.numberAccessible(maxPlaces = places) ?: ""

    return "Currency ${index + 1} of $totalCount, $name, " +
        "${input.numberAccessible()} ${selectedCurrency.currency} equals $amountAccessible $name"
}