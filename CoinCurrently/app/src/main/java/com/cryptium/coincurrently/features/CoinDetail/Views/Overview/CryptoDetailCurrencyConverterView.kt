package com.cryptium.coincurrently.features.CoinDetail.Views.Overview
import com.cryptium.coincurrently.core.localization.Localization
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.invisibleToUser
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.cryptium.coincurrently.sharedui.components.CachedUrlImage
import com.cryptium.coincurrently.sharedui.buttons.CurrencyButton
import com.cryptium.coincurrently.sharedui.components.tabPageTalkBackTraversal
import com.cryptium.coincurrently.sharedui.theme.onSurfaceDim
import com.cryptium.coincurrently.sharedui.theme.primaryBackgroundNoRipple
import com.cryptium.coincurrently.sharedui.modifiers.shimmeringModifier
import com.cryptium.coincurrently.core.extensions.formatCurrency
import com.cryptium.coincurrently.core.extensions.getSelectedCurrency
import com.cryptium.coincurrently.core.extensions.satoshiBold
import com.cryptium.coincurrently.sharedui.models.FormattingThreshold
import com.cryptium.coincurrently.sharedui.inputs.NumericInputField
import com.cryptium.coincurrently.core.analytics.AnalyticsManager
import com.cryptium.coincurrently.core.analytics.ScreenLabel
import com.cryptium.coincurrently.core.analytics.SheetLabel
import com.cryptium.coincurrently.core.analytics.SheetOpened
import com.cryptium.coincurrently.core.networking.ResponseModels.CryptoDetailInfoModel
import com.cryptium.coincurrently.core.networking.ResponseModels.CurrencySelectionModel

@Composable
fun CryptoDetailCurrencyConverterView(
    cryptoDetailInfoModel: CryptoDetailInfoModel?,
    selectedCurrency: CurrencySelectionModel,
    showCurrencySelector: MutableState<Boolean>,
) {
    val tabPage = Modifier.tabPageTalkBackTraversal()
    var coinAmount by remember { mutableStateOf(1.0) }
    var valueAmount by remember { mutableStateOf(1.0) }

    fun setValueAmount() {
        valueAmount = getValueAmount(
            coinAmount = coinAmount,
            cryptoDetailModel = cryptoDetailInfoModel,
            selectedCurrency = selectedCurrency
        )
    }

    LaunchedEffect(Unit, cryptoDetailInfoModel, selectedCurrency) {
        setValueAmount()
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        HeaderView(tabPage = tabPage)

        InputFields(
            coinAmount = coinAmount,
            valueAmount = valueAmount,
            cryptoDetailModel = cryptoDetailInfoModel,
            selectedCurrency = selectedCurrency,
            tabPage = tabPage,
            selectNewCurrency = {
                AnalyticsManager.log(
                    SheetOpened(ScreenLabel.CryptoDetail, SheetLabel.CurrencySelector),
                )
                showCurrencySelector.value = true
            },
            coinAmountChanged = {
                coinAmount = it
                setValueAmount()
            },
            valueAmountChanged = {
                valueAmount = it
                coinAmount = getCoinAmount(
                    valueAmount = valueAmount,
                    cryptoDetailModel = cryptoDetailInfoModel,
                    selectedCurrency = selectedCurrency
                )
            }
        )
    }
}

@Composable
private fun HeaderView(tabPage: Modifier) {
    Text(
        text = Localization.string("Currency Converter"),
        style = satoshiBold(22),
        color = MaterialTheme.colors.onSurfaceDim,
        modifier = tabPage.semantics { heading() },
    )
}

@Composable
private fun InputFields(
    coinAmount: Double,
    valueAmount: Double,
    cryptoDetailModel: CryptoDetailInfoModel?,
    selectedCurrency: CurrencySelectionModel,
    tabPage: Modifier,
    selectNewCurrency: () -> Unit,
    coinAmountChanged: (Double) -> Unit,
    valueAmountChanged: (Double) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .primaryBackgroundNoRipple()
    ) {
        CoinAmountInputField(
            coinAmount = coinAmount,
            valueAmount = valueAmount,
            cryptoDetailModel = cryptoDetailModel,
            selectedCurrency = selectedCurrency,
            tabPage = tabPage,
            coinAmountChanged = coinAmountChanged
        )

        ValueAmountInputField(
            valueAmount = valueAmount,
            coinAmount = coinAmount,
            cryptoDetailModel = cryptoDetailModel,
            tabPage = tabPage,
            valueAmountChanged = valueAmountChanged,
            selectedCurrency = selectedCurrency,
            selectNewCurrency = selectNewCurrency,
        )
    }
}

@Composable
private fun CoinAmountInputField(
    coinAmount: Double,
    valueAmount: Double,
    cryptoDetailModel: CryptoDetailInfoModel?,
    selectedCurrency: CurrencySelectionModel,
    tabPage: Modifier,
    coinAmountChanged: (Double) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        NumericInputField(
            input = coinAmount,
            onInputChange = {
                coinAmountChanged(it ?: 0.0)
            },
            accessibilityValue = Localization.format(
                "%1$@ %2$@ equals %3$@",
                formatConverterAmount(coinAmount),
                cryptoDetailModel?.name ?: "",
                valueAmount.formatCurrency(threshold = FormattingThreshold.NONE, accessible = true),
            ),
            tabPageTalkBack = true,
            trailingView = {
                SymbolView(
                    cryptoDetailModel = cryptoDetailModel,
                    selectedCurrency = selectedCurrency
                )
            },
            modifier = Modifier
                .weight(1f)
        )
    }
}

@Composable
private fun SymbolView(
    cryptoDetailModel: CryptoDetailInfoModel?,
    selectedCurrency: CurrencySelectionModel,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.clearAndSetSemantics { invisibleToUser() },
    ) {
        CurrencyButton(
            title = selectedCurrency.code.uppercase(),
            isEnabled = false,
            selectNewCurrency = { },
            modifier = Modifier
                .alpha(0f)
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CachedUrlImage(
                model = cryptoDetailModel?.image?.small,
                size = 20.dp,
            )

            Text(
                text = cryptoDetailModel?.symbol?.uppercase() ?: "0000",
                style = satoshiBold(18),
                color = MaterialTheme.colors.onSurfaceDim,
                modifier = Modifier
                    .shimmeringModifier(cryptoDetailModel?.symbol == null)
            )
        }
    }
}

@Composable
private fun ValueAmountInputField(
    valueAmount: Double,
    coinAmount: Double,
    cryptoDetailModel: CryptoDetailInfoModel?,
    selectedCurrency: CurrencySelectionModel,
    tabPage: Modifier,
    valueAmountChanged: (Double) -> Unit,
    selectNewCurrency: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        NumericInputField(
            input = valueAmount,
            onInputChange = {
                valueAmountChanged(it ?: 0.0)
            },
            accessibilityValue = Localization.format(
                "%1$@ equals %2$@ %3$@",
                valueAmount.formatCurrency(threshold = FormattingThreshold.NONE, accessible = true),
                formatConverterAmount(coinAmount),
                cryptoDetailModel?.name ?: "",
            ),
            tabPageTalkBack = true,
            trailingView = {
                CurrentCurrencyButton(
                    selectedCurrency = selectedCurrency,
                    selectNewCurrency = selectNewCurrency,
                    tabPage = tabPage,
                )
            },
            modifier = Modifier
                .weight(1f)
        )
    }
}

@Composable
private fun CurrentCurrencyButton(
    selectedCurrency: CurrencySelectionModel,
    selectNewCurrency: () -> Unit,
    tabPage: Modifier,
) {
    CurrencyButton(
        title = selectedCurrency.code.uppercase(),
        selectNewCurrency = selectNewCurrency,
        modifier = tabPage,
    )
}

private fun getCoinAmount(
    valueAmount: Double,
    cryptoDetailModel: CryptoDetailInfoModel?,
    selectedCurrency: CurrencySelectionModel
): Double {
    val currentPrice = cryptoDetailModel?.market_data?.current_price?.getSelectedCurrency(currency = selectedCurrency.code) ?: 0.9
    return valueAmount / currentPrice
}

private fun getValueAmount(
    coinAmount: Double,
    cryptoDetailModel: CryptoDetailInfoModel?,
    selectedCurrency: CurrencySelectionModel
): Double {
    val currentPrice = cryptoDetailModel?.market_data?.current_price?.getSelectedCurrency(currency = selectedCurrency.code) ?: 0.0
    return coinAmount * currentPrice
}

private fun formatConverterAmount(amount: Double): String {
    return if (amount % 1.0 == 0.0) {
        amount.toLong().toString()
    } else {
        amount.toString()
    }
}