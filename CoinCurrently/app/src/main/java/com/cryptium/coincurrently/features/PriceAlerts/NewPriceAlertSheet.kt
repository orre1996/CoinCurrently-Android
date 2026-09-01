package com.cryptium.coincurrently.features.PriceAlerts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.invisibleToUser
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.setProgress
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cryptium.coincurrently.R
import com.cryptium.coincurrently.core.config.AppLocale
import com.cryptium.coincurrently.sharedui.components.AccessibilityLayout
import com.cryptium.coincurrently.sharedui.buttons.ChangeButton
import com.cryptium.coincurrently.sharedui.modifiers.CappedFontScale
import com.cryptium.coincurrently.sharedui.buttons.CurrencyButton
import com.cryptium.coincurrently.sharedui.modifiers.DynamicTypeSizeCap
import com.cryptium.coincurrently.sharedui.buttons.PrimaryButton
import com.cryptium.coincurrently.sharedui.components.Separator
import com.cryptium.coincurrently.sharedui.theme.onSurfaceContainer
import com.cryptium.coincurrently.sharedui.theme.onSurfaceDim
import com.cryptium.coincurrently.sharedui.theme.onSurfaceDimmer
import com.cryptium.coincurrently.sharedui.theme.surfaceContainerHigh
import com.cryptium.coincurrently.sharedui.theme.surfaceContainerLow
import com.cryptium.coincurrently.sharedui.theme.trendDown
import com.cryptium.coincurrently.sharedui.theme.trendUp
import com.cryptium.coincurrently.sharedui.components.CheckboxView
import com.cryptium.coincurrently.core.extensions.changeDecimalPoints
import com.cryptium.coincurrently.core.extensions.changePercentDecimalPoints
import com.cryptium.coincurrently.core.extensions.currencyAccessible
import com.cryptium.coincurrently.core.extensions.directionAccessible
import com.cryptium.coincurrently.core.extensions.formatCurrency
import com.cryptium.coincurrently.core.extensions.getDifferenceInPercent
import com.cryptium.coincurrently.core.extensions.getSelectedCurrency
import com.cryptium.coincurrently.core.extensions.roundTo
import com.cryptium.coincurrently.core.extensions.numberAccessible
import com.cryptium.coincurrently.core.extensions.satoshiMedium
import com.cryptium.coincurrently.core.extensions.withoutSymbol
import com.cryptium.coincurrently.sharedui.components.DragHandle
import com.cryptium.coincurrently.sharedui.models.ChangeDirection
import com.cryptium.coincurrently.sharedui.models.FormattingThreshold
import com.cryptium.coincurrently.sharedui.inputs.NumericInputField
import com.cryptium.coincurrently.sharedui.components.SnackBarType
import com.cryptium.coincurrently.sharedui.components.SnackbarView
import kotlinx.coroutines.launch
import com.cryptium.coincurrently.core.networking.ResponseModels.AlertDirection
import com.cryptium.coincurrently.core.networking.ResponseModels.CryptoDetailInfoModel
import com.cryptium.coincurrently.core.networking.ResponseModels.CurrencySelectionModel
import com.cryptium.coincurrently.core.networking.ResponseModels.PostAlertCoin
import com.cryptium.coincurrently.sharedui.sheets.ModalBottomSheetSystemNavigationBar
import com.cryptium.coincurrently.sharedui.sheets.rememberModalSheetMaxBodyHeight
import com.cryptium.coincurrently.sharedui.sheets.ModalSheetBody
import com.cryptium.coincurrently.sharedui.sheets.ModalSheetColumnLayout
import java.text.NumberFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewPriceAlertSheet(
    sheetState: SheetState,
    cryptoDetailModel: CryptoDetailInfoModel?,
    selectedCurrency: CurrencySelectionModel,
    isLoading: Boolean,
    showError: Boolean,
    isAlertByPriceSelected: MutableState<Boolean>,
    priceInput: MutableState<Double>,
    percentInput: MutableState<Double>,
    isRecurringAlertOn: MutableState<Boolean>,
    selectedDirection: MutableState<ChangeDirection>,
    selectNewCurrency: () -> Unit,
    addNewAlert: (PostAlertCoin) -> Unit,
    close: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val talkBackEnabled = AccessibilityLayout.isTalkBackEnabled
    val maxBodyHeight = rememberModalSheetMaxBodyHeight(dragHandleTitle = "Set Price Alert")

    var showSnackbar by remember { mutableStateOf(false) }
    val currentPrice by remember(cryptoDetailModel, selectedCurrency) {
        derivedStateOf {
            cryptoDetailModel?.market_data?.current_price?.getSelectedCurrency(currency = selectedCurrency.code)
        }
    }

    val targetPrice by remember(currentPrice, percentInput.value, selectedDirection.value) {
        derivedStateOf {
            val clamped = if (selectedDirection.value == ChangeDirection.Down) {
                percentInput.value.coerceIn(0.0, 100.0)
            } else {
                percentInput.value
            }

            val percent = clamped / 100.0
            val op = selectedDirection.value.operation()
            val base = currentPrice ?: 0.0
            op(base, percent * base)
        }
    }

    val change by remember(currentPrice, percentInput.value, selectedDirection.value) {
        derivedStateOf {
            getDifferenceInPercent(priceInput.value, currentPrice ?: 0.0) ?: 0.0
        }
    }

    val targetPercent by remember(change) {
        derivedStateOf {
            val decimals = change.changeDecimalPoints
            val number = NumberFormat.getNumberInstance(AppLocale.enUsPosix).apply {
                maximumFractionDigits = decimals
                minimumFractionDigits = decimals
                isGroupingUsed = true
            }.format(change)

            (if(change == 0.0) "-" else if(priceInput.value >= (currentPrice ?: 0.0)) "+" else "") + number
        }
    }

    val currentPriceAccessibilityLabel = "Current ${cryptoDetailModel?.name ?: ""} price: " +
        (currentPrice?.currencyAccessible(currency = selectedCurrency) ?: "")

    val percentColor = if (priceInput.value >= (currentPrice ?: 0.0)) {
            MaterialTheme.colors.trendUp
        } else {
            MaterialTheme.colors.trendDown
        }

    LaunchedEffect(cryptoDetailModel, selectedCurrency) {
        val currentPriceValue = cryptoDetailModel?.market_data?.current_price
            ?.getSelectedCurrency(currency = selectedCurrency.code) ?: 0.0
        priceInput.value = (currentPriceValue * 1.1).roundTo(2)
    }

    var ignoreInitialShowError: Boolean by remember { mutableStateOf(true) }
    LaunchedEffect(showError) {
        if (ignoreInitialShowError) {
            ignoreInitialShowError = false
            return@LaunchedEffect
        }
        showSnackbar = !showSnackbar
    }

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
            DragHandle(
                title = "Set Price Alert",
                closeAction = {
                    coroutineScope.launch {
                        sheetState.hide()
                        close.invoke()
                    }
                }
            )
        }
    ) {
        ModalBottomSheetSystemNavigationBar()
        ModalSheetBody(
            context = context,
            maxBodyHeight = maxBodyHeight
        ) {
            Box(
                contentAlignment = Alignment.BottomCenter,
                modifier = Modifier.fillMaxWidth()
            ) {
                ModalSheetColumnLayout(
                    maxBodyHeight = maxBodyHeight,
                    scrollItemCount = 5,
                    scrollItemHeightEstimate = 96.dp,
                    scrollVerticalPadding = 88.dp,
                    header = {
                        Separator(
                            padding = PaddingValues(horizontal = 16.dp)
                        )
                    },
                    footer = {
                        Separator(
                            padding = PaddingValues(horizontal = 16.dp)
                        )

                        CurrentPriceView(
                            currentPrice = cryptoDetailModel?.market_data?.current_price?.getSelectedCurrency(
                                currency = selectedCurrency.code,
                            ),
                            selectedCurrency = selectedCurrency,
                            talkBackEnabled = talkBackEnabled
                        )

                        AddAlertButtonView(
                            isLoading = isLoading,
                            onClick = {
                                val id = cryptoDetailModel?.id ?: return@AddAlertButtonView

                                val target =
                                    if (isAlertByPriceSelected.value) priceInput.value else targetPrice
                                val direction = if (target > (currentPrice ?: 0.0)) {
                                    AlertDirection.Above
                                } else {
                                    AlertDirection.Below
                                }
                                val resetThreshold = if (isRecurringAlertOn.value) 6.0 else null

                                val alertCoin = PostAlertCoin(
                                    coinId = id,
                                    targetPrice = target,
                                    direction = direction,
                                    resetThreshold = resetThreshold,
                                    preferredCurrency = selectedCurrency.code,
                                )

                                addNewAlert.invoke(alertCoin)
                            }
                        )
                    },
                    scrollContent = { scrollModifier ->
                        Column(
                            verticalArrangement = Arrangement.Center,
                            modifier = scrollModifier
                                .verticalScroll(rememberScrollState())
                                .padding(
                                    start = 16.dp,
                                    end = 16.dp,
                                    bottom = 24.dp,
                                ),
                        ) {
                            AlertTypeView(
                                isAlertByPriceSelected = isAlertByPriceSelected,
                                talkBackEnabled = talkBackEnabled
                            )

                            Text(
                                text = if (isAlertByPriceSelected.value) {
                                    "Target Price"
                                } else {
                                    "Target Price Change %"
                                },
                                color = MaterialTheme.colors.onSurfaceDimmer,
                                style = satoshiMedium(14),
                                modifier = Modifier
                                    .semantics { invisibleToUser() }
                            )

                            if (talkBackEnabled) {
                                Box(
                                    modifier = Modifier
                                        .semantics {
                                            contentDescription = currentPriceAccessibilityLabel
                                        }
                                )
                            }

                            InputField(
                                priceInput = priceInput,
                                percentInput = percentInput,
                                isAlertByPriceSelected = isAlertByPriceSelected.value,
                                selectedCurrency = selectedCurrency,
                                selectedDirection = selectedDirection,
                                coinName = cryptoDetailModel?.name,
                                selectNewCurrency = {
                                    coroutineScope.launch {
                                        sheetState.hide()
                                        selectNewCurrency.invoke()
                                        close.invoke()
                                    }
                                }
                            )

                            if (isAlertByPriceSelected.value) {
                                val targetPercentAccessibilityLabel = "Change from current price: " +
                                    "${change.directionAccessible.lowercase()} " +
                                    "${change.numberAccessible(maxPlaces = change.changePercentDecimalPoints)} percent"

                                Text(
                                    buildAnnotatedString {
                                        withStyle(
                                            style = SpanStyle(
                                                fontFamily = FontFamily(Font(R.font.satoshi_medium)),
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = percentColor
                                            )
                                        ) {
                                            append("$targetPercent%")
                                        }

                                        append(" from current Price")
                                    },
                                    style = satoshiMedium(14),
                                    color = MaterialTheme.colors.onSurfaceDim,
                                    maxLines = 1,
                                    modifier = Modifier
                                        .semantics {
                                            contentDescription = targetPercentAccessibilityLabel
                                        }
                                )
                            } else {
                                val formattedTargetPrice = targetPrice.formatCurrency(
                                    threshold = FormattingThreshold.NONE,
                                    currency = selectedCurrency
                                ).withoutSymbol

                                Text(
                                    buildAnnotatedString {
                                        append("Target Price ")

                                        withStyle(
                                            style = SpanStyle(
                                                fontFamily = FontFamily(Font(R.font.satoshi_medium)),
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = if (selectedDirection.value == ChangeDirection.Up) {
                                                    MaterialTheme.colors.trendUp
                                                } else {
                                                    MaterialTheme.colors.trendDown
                                                },
                                            )
                                        ) {
                                            append(formattedTargetPrice)
                                        }
                                    },
                                    style = satoshiMedium(14),
                                    color = MaterialTheme.colors.onSurfaceDim,
                                    maxLines = 1,
                                    modifier = Modifier
                                        .semantics {
                                            contentDescription = "Target Price: $formattedTargetPrice"
                                        }
                                )
                            }

                            RecurringView(
                                isChecked = isRecurringAlertOn
                            )
                        }
                    },
                )

                SnackbarView(
                    showSnackBar = showSnackbar,
                    message = "Couldn't add ${cryptoDetailModel?.symbol?.uppercase() ?: "coin"} alert. Please try again later.",
                    buttonTitle = "",
                    type = SnackBarType.FAILURE,
                    isTabbarHidden = true,
                    navBarHeight = 0.dp,
                    systemNavbarHeight = 0.dp,
                    onDismissRequest = { showSnackbar = false },
                    buttonAction = { }
                )
            }
        }
    }
}

@Composable
private fun AlertTypeView(
    isAlertByPriceSelected: MutableState<Boolean>,
    talkBackEnabled: Boolean
) {
    val selectedIndex = if (isAlertByPriceSelected.value) 0 else 1
    val selectedLabel = if (isAlertByPriceSelected.value) "Alert by price" else "Alert by percent"

    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        modifier = Modifier
            .padding(
                top = 24.dp,
                bottom = 64.dp,
            )
            .then(
                if (talkBackEnabled) {
                    Modifier.clearAndSetSemantics {
                        contentDescription = "Alert type. Swipe up or down to change alert type."
                        stateDescription = "Type ${selectedIndex + 1} of 2, $selectedLabel."
                        progressBarRangeInfo = ProgressBarRangeInfo(
                            current = selectedIndex.toFloat(),
                            range = 0f..1f,
                            steps = 1,
                        )
                        setProgress(
                            action = { target ->
                                val current = selectedIndex.toFloat()
                                val newIndex = when {
                                    target > current -> 1
                                    target < current -> 0
                                    else -> selectedIndex
                                }
                                isAlertByPriceSelected.value = newIndex == 0
                                true
                            }
                        )
                    }
                } else {
                    Modifier
                }
            )
    ) {
        CappedFontScale(DynamicTypeSizeCap.Accessibility2) {
        TypeButton(
            title = "By Price",
            isSelected = isAlertByPriceSelected.value,
            accessibilityLabel = "Alert by price",
            onClick = {
                isAlertByPriceSelected.value = true
            }
        )

        TypeButton(
            title = "By Δ%",
            isSelected = !isAlertByPriceSelected.value,
            accessibilityLabel = "Alert by percent",
            onClick = {
                isAlertByPriceSelected.value = false
            }
        )
        }
    }
}

@Composable
private fun TypeButton(
    title: String,
    isSelected: Boolean,
    accessibilityLabel: String,
    onClick: () -> Unit
) {
    Text(
        text = title,
        style = satoshiMedium(16),
        color = if (isSelected) MaterialTheme.colors.onPrimary else MaterialTheme.colors.onSurfaceContainer,
        maxLines = 2,
        modifier = Modifier
            .background(
                color = if (isSelected) MaterialTheme.colors.primary else MaterialTheme.colors.surfaceContainerHigh,
                shape = CircleShape
            )
            .clip(CircleShape)
            .clickable {
                onClick.invoke()
            }
            .semantics {
                contentDescription = accessibilityLabel
                role = Role.Tab
                selected = isSelected
            }
            .padding(
                vertical = 12.dp,
                horizontal = 16.dp
            )
    )
}

@Composable
private fun CurrentPriceView(
    currentPrice: Double?,
    selectedCurrency: CurrencySelectionModel,
    talkBackEnabled: Boolean
) {
    Text(
        buildAnnotatedString {
            append("Current price ")

            withStyle(style = SpanStyle(color = MaterialTheme.colors.onSurface)) {
                append(
                    currentPrice?.formatCurrency(
                        threshold = FormattingThreshold.NONE,
                        currency = selectedCurrency
                    ) ?: "0000000"
                )
            }
        },
        style = satoshiMedium(14),
        textAlign = TextAlign.Center,
        color = MaterialTheme.colors.onSurfaceDimmer,
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = 16.dp,
                top = 24.dp,
                end = 16.dp,
                bottom = 12.dp
            )
            .then(
                if (talkBackEnabled) {
                    Modifier.semantics { invisibleToUser() }
                } else {
                    Modifier
                }
            )
    )
}

@Composable
private fun InputField(
    priceInput: MutableState<Double>,
    percentInput: MutableState<Double>,
    isAlertByPriceSelected: Boolean,
    selectedCurrency: CurrencySelectionModel,
    selectedDirection: MutableState<ChangeDirection>,
    coinName: String?,
    selectNewCurrency: () -> Unit,
) {
    val accessibilityValue = if (isAlertByPriceSelected) {
        val formattedPrice = priceInput.value.formatCurrency(
            threshold = FormattingThreshold.NONE,
            currency = selectedCurrency
        )
        "Trigger alert at $formattedPrice"
    } else {
        val decimals = percentInput.value.changePercentDecimalPoints
        val formattedPercent = NumberFormat.getNumberInstance(AppLocale.enUsPosix).apply {
            maximumFractionDigits = decimals
            minimumFractionDigits = decimals
            isGroupingUsed = true
        }.format(percentInput.value)
        "Trigger alert when ${coinName ?: ""} is ${selectedDirection.value.value} $formattedPercent percent"
    }

    NumericInputField(
        input = if (isAlertByPriceSelected) priceInput.value else percentInput.value,
        currencyInputFormat = if (isAlertByPriceSelected) selectedCurrency else null,
        accessibilityValue = accessibilityValue,
        onInputChange = {
            if (isAlertByPriceSelected) {
                val value = it ?: 0.0
                val decimals = if (kotlin.math.abs(value) < 1) 7 else 2
                priceInput.value = value.roundTo(decimals)
            } else {
                percentInput.value = it ?: 0.0
            }
        },
        trailingView = {
            if (isAlertByPriceSelected) {
                CurrencyButton(
                    title = selectedCurrency.code.uppercase(),
                    selectNewCurrency = selectNewCurrency,
                )
            } else {
                ChangeButton(
                    direction = selectedDirection.value,
                    currencyChanged = {
                        selectedDirection.value = it
                    },
                    modifier = Modifier
                        .semantics {
                            contentDescription = "Change percent direction. currently selected: ${selectedDirection.value.value}"
                            role = Role.Button
                        }
                )
            }
        },
        modifier = Modifier
                    .padding(vertical = 8.dp),
    )
}

@Composable
private fun RecurringView(
    isChecked: MutableState<Boolean>
) {
    CheckboxView(
        trailingText = "Set as recurring alert",
        isChecked = isChecked.value,
        onToggle = {
            isChecked.value = !isChecked.value
        },
        modifier = Modifier
            .padding(top = 64.dp)
            .wrapContentWidth(Alignment.CenterHorizontally)
    )
}

@Composable
private fun AddAlertButtonView(
    isLoading: Boolean,
    onClick: () -> Unit
) {
    PrimaryButton(
        text = "Set Price Alert",
        isLoading = isLoading,
        fullWidth = true,
        modifier = Modifier
            .padding(
                start = 16.dp,
                end = 16.dp,
                bottom = 24.dp
            )
    ) {
        onClick.invoke()
    }
}
