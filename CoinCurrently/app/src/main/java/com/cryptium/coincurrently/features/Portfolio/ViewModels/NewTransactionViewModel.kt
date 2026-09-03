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
import com.cryptium.coincurrently.core.extensions.getSelectedCurrency
import com.cryptium.coincurrently.core.extensions.ofType
import com.cryptium.coincurrently.core.persistence.allCurrencies
import com.cryptium.coincurrently.core.persistence.CurrencyRepository
import com.cryptium.coincurrently.features.Portfolio.PortfolioRepository
import com.cryptium.coincurrently.features.Portfolio.WalletImportRepository
import kotlinx.coroutines.launch
import com.cryptium.coincurrently.core.networking.ResponseModels.CryptoDetailInfoModel
import com.cryptium.coincurrently.core.networking.ResponseModels.CurrencySelectionModel
import com.cryptium.coincurrently.core.networking.ResponseModels.allCurrencies
import com.cryptium.coincurrently.features.Portfolio.DataClasses.NewTransaction
import com.cryptium.coincurrently.features.Portfolio.DataClasses.PortfolioModel
import com.cryptium.coincurrently.features.Portfolio.DataClasses.getAllTransactions
import com.cryptium.coincurrently.features.Portfolio.Enums.NewTransactionType
import com.cryptium.coincurrently.core.analytics.AnalyticsManager
import com.cryptium.coincurrently.core.analytics.ScreenLabel
import com.cryptium.coincurrently.core.analytics.TransactionAdded
import com.cryptium.coincurrently.core.analytics.TransactionEdited
import com.cryptium.coincurrently.core.networking.ResponseModels.toToplist
import java.util.Date
import java.util.UUID

class NewTransactionViewModelFactory(
    val transactionType: NewTransactionType,
    val currencyDetail: CryptoDetailInfoModel?,
    val transaction: NewTransaction?
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NewTransactionViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NewTransactionViewModel(
                transactionType,
                currencyDetail,
                transaction
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class NewTransactionViewModel(
    transactionType: NewTransactionType,
    currencyDetail: CryptoDetailInfoModel?,
    transaction: NewTransaction?,
): ViewModel() {
    private val portfolioRepository by lazy { PortfolioRepository }
    private val walletImportRepository by lazy { WalletImportRepository }
    private val currencyRepository by lazy { CurrencyRepository }

    var currencyDetail: CryptoDetailInfoModel? by mutableStateOf(currencyDetail)
    var transaction: NewTransaction? by mutableStateOf(transaction)
    var transactionType: NewTransactionType by mutableStateOf(transactionType)

    var selectedCurrency: MutableState<CurrencySelectionModel> = mutableStateOf(UserPreferences.getSelectedCurrency())
    var containsCoin: Boolean by mutableStateOf(false)
    var pie: PortfolioModel? by mutableStateOf(null)

    var allTransactions: List<NewTransaction>? by mutableStateOf(null)

    // Input fields
    var address: MutableState<String> = mutableStateOf("")
    var amount: MutableState<Double?> = mutableStateOf(null)
    var averageCost: MutableState<Double?> = mutableStateOf(null)
    var fees: MutableState<Double?> = mutableStateOf(null)
    val date: MutableState<Date> = mutableStateOf(Date())
    var note: MutableState<String> = mutableStateOf("")


    init {
        this.currencyDetail = currencyDetail
        this.transaction = transaction
        this.pie = portfolioRepository.portfolioModels?.firstOrNull { it.id == currencyDetail?.id }
        this.allTransactions = portfolioRepository.portfolioModels?.getAllTransactions(currencyDetail?.id)
        this.containsCoin = portfolioRepository.portfolioModels?.any { it.id == currencyDetail?.id } ?: false
        this.selectedCurrency.value = allCurrencies.firstOrNull { it.code == (transaction?.currency ?: selectedCurrency.value.code) } ?: selectedCurrency.value

        this.address.value = transaction?.address ?: ""
        this.amount.value = transaction?.amount
        this.averageCost.value = transaction?.pricePerCoin
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

    val currenValue: Double
        get() {
            return currencyDetail?.market_data?.current_price?.getSelectedCurrency(currency =  selectedCurrency.value.code) ?: 0.0

        }

    val isInputValid: Boolean
        get() {
            val isAmountValid = (amount.value ?: 0.0) > 0.0
            val isNotesValid = note.value.length <= 200

            return if (transactionType == NewTransactionType.ImportAddress) {
                address.value.isNotEmpty() && isNotesValid
            } else {
                isAmountValid && isNotesValid
            }
        }

    private fun editTransaction(oldId: UUID?, transaction: NewTransaction): Boolean {
        if (!persistTransaction(transaction)) return false
        portfolioRepository.deleteTransaction(
            id = currencyDetail?.id ?: "",
            uuid = oldId
        )
        return true
    }

    fun addTransaction(context: Context): NewTransaction? {
        val amountValue = amount.value ?: return null

        val isNewTransaction = transaction == null

        val newTransaction = NewTransaction(
            date = date.value,
            amount = amountValue,
            pricePerCoin = if (transactionType.isTransfer) {
                null
            } else {
                averageCost.value ?: currenValue
            },
            fee = fees.value ?: 0.0,
            note = note.value,
            transactionType = transactionType,
            currency = selectedCurrency.value.code
        )

        val saved = if (transaction != null) {
            editTransaction(transaction?.id, newTransaction)
        } else {
            persistTransaction(newTransaction)
        }

        if (!saved) return null

        val coinName = currencyDetail?.name ?: ""
        if (isNewTransaction) {
            AnalyticsManager.log(
                TransactionAdded(ScreenLabel.NewPortfolioTransaction, coinName),
            )
        } else {
            AnalyticsManager.log(
                TransactionEdited(ScreenLabel.NewPortfolioTransaction, coinName),
            )
        }

        MainActivity.showAppReview(context)
        return newTransaction
    }

    private fun persistTransaction(transaction: NewTransaction): Boolean {
        val detail = currencyDetail ?: return false
        val id = detail.id ?: return false

        if (pie != null) {
            portfolioRepository.addNewTransaction(id, transaction)
        } else {
            portfolioRepository.addNewCoin(
                coin = detail.toToplist(),
                event = transaction,
                allCurrencies = detail.market_data?.current_price?.allCurrencies
            )
        }
        return true
    }
}