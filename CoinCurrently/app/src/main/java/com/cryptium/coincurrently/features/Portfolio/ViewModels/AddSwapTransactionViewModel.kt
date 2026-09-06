package com.cryptium.coincurrently.features.Portfolio.ViewModels
import com.cryptium.coincurrently.core.persistence.UserPreferences
import android.content.Context
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.cryptium.coincurrently.app.MainActivity
import com.cryptium.coincurrently.features.Portfolio.Models.PortfolioSwapTransaction
import com.cryptium.coincurrently.core.extensions.getSelectedCurrency
import com.cryptium.coincurrently.core.persistence.allCurrencies
import com.cryptium.coincurrently.core.persistence.CurrencyRepository
import com.cryptium.coincurrently.features.Portfolio.PortfolioRepository
import kotlinx.coroutines.launch
import com.cryptium.coincurrently.core.networking.ResponseModels.CryptoDetailInfoModel
import com.cryptium.coincurrently.core.networking.ResponseModels.CurrencySelectionModel
import com.cryptium.coincurrently.core.networking.ResponseModels.allCurrencies
import com.cryptium.coincurrently.features.Portfolio.Enums.PortfolioTransactionType
import com.cryptium.coincurrently.core.analytics.AnalyticsManager
import com.cryptium.coincurrently.core.analytics.ScreenLabel
import com.cryptium.coincurrently.core.analytics.TransactionAdded
import com.cryptium.coincurrently.core.analytics.TransactionEdited
import com.cryptium.coincurrently.core.networking.ResponseModels.Toplist
import com.cryptium.coincurrently.core.networking.ResponseModels.toToplist
import java.util.Date
import java.util.UUID

class AddSwapTransactionViewModelFactory(
    val transactionType: PortfolioTransactionType,
    val currencyDetail: CryptoDetailInfoModel?,
    val transaction: PortfolioSwapTransaction?
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddSwapTransactionViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AddSwapTransactionViewModel(
                transactionType,
                currencyDetail,
                transaction
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class AddSwapTransactionViewModel(
    transactionType: PortfolioTransactionType,
    currencyDetail: CryptoDetailInfoModel?,
    transaction: PortfolioSwapTransaction?,
): ViewModel() {
    private val portfolioRepository by lazy { PortfolioRepository }
    private val currencyRepository by lazy { CurrencyRepository }

    var currencyDetail: CryptoDetailInfoModel? by mutableStateOf(currencyDetail)
    var transaction: PortfolioSwapTransaction? by mutableStateOf(transaction)
    var transactionType: PortfolioTransactionType by mutableStateOf(transactionType)

    var selectedCurrency: MutableState<CurrencySelectionModel> = mutableStateOf(UserPreferences.getSelectedCurrency())
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

    suspend private fun editTransaction(oldId: UUID?, transaction: PortfolioSwapTransaction) {
        addTransaction(transaction)
        portfolioRepository.deleteTransaction(
            id = this.transaction?.toCurrencyId ?: toInformation.first,
            uuid = oldId
        )
    }

    suspend fun addTransaction(context: Context): PortfolioSwapTransaction? {
        val fromAmount = fromAmount.value ?: return null
        val toAmount = toAmount.value ?: return null

        val isNewTransaction = transaction == null

        val newTransaction = PortfolioSwapTransaction(
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
                TransactionAdded(ScreenLabel.PortfolioSwapTransaction, coinName),
            )
        } else {
            AnalyticsManager.log(
                TransactionEdited(ScreenLabel.PortfolioSwapTransaction, coinName),
            )
        }

        MainActivity.showAppReview(context)
        return newTransaction
    }

    suspend private fun addTransaction(transaction: PortfolioSwapTransaction) {
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