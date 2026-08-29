package com.cryptium.coincurrently.ui.main.PortfolioTab.ViewModels

import android.content.Context
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.cryptium.coincurrently.ui.main.Activities.MainActivity
import com.cryptium.coincurrently.ui.main.CryptoDetail.Compose.Portfolio.Models.NewSwapTransaction
import com.cryptium.coincurrently.ui.main.Extensions.getSelectedCurrency
import com.cryptium.coincurrently.ui.main.General.GlobalFunctions
import com.cryptium.coincurrently.ui.main.SettingsTab.Compose.allCurrencies
import com.cryptium.coincurrently.ui.main.Repositories.CurrencyRepository
import com.cryptium.coincurrently.ui.main.Repositories.PortfolioRepository
import kotlinx.coroutines.launch
import networking.ResponseModels.CryptoDetailInfoModel
import networking.ResponseModels.CurrencySelectionModel
import com.cryptium.coincurrently.ui.main.HomeTab.GlobalMarket.DataClasses.allCurrencies
import com.cryptium.coincurrently.ui.main.PortfolioTab.Enums.NewTransactionType
import com.cryptium.coincurrently.ui.main.analytics.AnalyticsManager
import com.cryptium.coincurrently.ui.main.analytics.ScreenLabel
import com.cryptium.coincurrently.ui.main.analytics.TransactionAdded
import com.cryptium.coincurrently.ui.main.analytics.TransactionEdited
import networking.ResponseModels.Toplist
import networking.ResponseModels.toToplist
import java.util.Date
import java.util.UUID

class NewSwapTransactionViewModelFactory(
    val transactionType: NewTransactionType,
    val currencyDetail: CryptoDetailInfoModel?,
    val transaction: NewSwapTransaction?
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NewSwapTransactionViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NewSwapTransactionViewModel(
                transactionType,
                currencyDetail,
                transaction
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class NewSwapTransactionViewModel(
    transactionType: NewTransactionType,
    currencyDetail: CryptoDetailInfoModel?,
    transaction: NewSwapTransaction?,
): ViewModel() {
    private val portfolioRepository by lazy { PortfolioRepository }
    private val currencyRepository by lazy { CurrencyRepository }

    var currencyDetail: CryptoDetailInfoModel? by mutableStateOf(currencyDetail)
    var transaction: NewSwapTransaction? by mutableStateOf(transaction)
    var transactionType: NewTransactionType by mutableStateOf(transactionType)

    var selectedCurrency: MutableState<CurrencySelectionModel> = mutableStateOf(GlobalFunctions.getSelectedCurrency())
    var toInformation: Pair<String, String> by mutableStateOf(Pair("", ""))

    // Input fields
    var fromAmount: MutableState<Double?> = mutableStateOf(null)
    var fromAverageCost: MutableState<Double?> = mutableStateOf(null)
    var toAmount: MutableState<Double?> = mutableStateOf(null)
    var fees: MutableState<Double?> = mutableStateOf(null)
    val date: MutableState<Date> = mutableStateOf(Date())
    var note: MutableState<String> = mutableStateOf("")

    var isLoading: Boolean by mutableStateOf(false)

    init {
        this.currencyDetail = currencyDetail
        this.transaction = transaction

        this.toInformation = if (transaction != null) {
            Pair(transaction.toCurrencyId, transaction.toCurrencySymbol)
        } else if (currencyDetail?.id == "bitcoin") {
            Pair("ethereum", "ETH")
        } else {
            Pair("bitcoin", "BTC")
        }

        this.selectedCurrency.value = allCurrencies.firstOrNull { it.code == (transaction?.selectedCurrency ?: selectedCurrency.value.code) } ?: selectedCurrency.value

        this.fromAmount.value = transaction?.fromAmount
        this.fromAverageCost.value = transaction?.fromUnitPrice
        this.toAmount.value = transaction?.toAmount
        this.fees.value = transaction?.fee
        this.note.value = transaction?.note ?: ""
        this.date.value = transaction?.date ?: Date()
    }

    fun getCryptoDetail() {
        viewModelScope.launch {
            currencyRepository.getCryptoDetail(currencyDetail?.id)?.let {
                currencyDetail = it
            }
        }
    }

    val currentPrice: Double
        get() {
            return currencyDetail?.market_data?.current_price?.getSelectedCurrency(currency = selectedCurrency.value.code) ?: 0.0
        }

    val isInputValid: Boolean
        get() {
            val isAmountValid = (fromAmount.value ?: 0.0) > 0.0 && (toAmount.value ?: 0.0) > 0.0
            val isNotesValid = note.value.length <= 200

            return isAmountValid && isNotesValid
        }

    suspend private fun editTransaction(oldId: UUID?, transaction: NewSwapTransaction) {
        addTransaction(transaction)
        portfolioRepository.deleteTransaction(
            id = this.transaction?.toCurrencyId ?: toInformation.first,
            uuid = oldId
        )
    }

    suspend fun addTransaction(context: Context): NewSwapTransaction? {
        val fromAmount = fromAmount.value ?: return null
        val toAmount = toAmount.value ?: return null

        val isNewTransaction = transaction == null

        val newTransaction = NewSwapTransaction(
            selectedCurrency = selectedCurrency.value.code,
            fromCurrencyId = currencyDetail?.id ?: "",
            fromCurrencySymbol = currencyDetail?.symbol ?: "",
            fromAmount = fromAmount,
            fromUnitPrice = fromAverageCost.value ?: currentPrice,
            toCurrencyId = toInformation.first,
            toCurrencySymbol = toInformation.second.lowercase(),
            toAmount = toAmount,
            toUnitPrice = (currentPrice * fromAmount) / toAmount,
            fee = fees.value ?: 0.0,
            note = note.value
        )

        transaction?.let {
            editTransaction(transaction?.id, newTransaction)
        } ?: run {
            addTransaction(newTransaction)
        }

        val coinName = currencyDetail?.name ?: ""
        if (isNewTransaction) {
            AnalyticsManager.log(
                TransactionAdded(ScreenLabel.NewSwapTransaction, coinName),
            )
        } else {
            AnalyticsManager.log(
                TransactionEdited(ScreenLabel.NewSwapTransaction, coinName),
            )
        }

        MainActivity.showAppReview(context)
        return newTransaction
    }

    suspend private fun addTransaction(transaction: NewSwapTransaction) {
        val existing = portfolioRepository.portfolioModels?.firstOrNull { it.id == toInformation.first }

        if (existing != null) {
            portfolioRepository.addNewTransaction(
                id = toInformation.first,
                event = transaction
            )

            return
        }

        isLoading = true
        val cryptoInfo = currencyRepository.getCryptoDetail(toInformation.first)

        portfolioRepository.addNewCoin(
            coin = cryptoInfo?.toToplist() ?: Toplist(id = toInformation.first),
            event = transaction,
            allCurrencies = cryptoInfo?.market_data?.current_price?.allCurrencies
        )
    }
}