package com.cryptium.coincurrently.core.networking.ResponseModels
import com.cryptium.coincurrently.core.persistence.UserPreferences
import com.cryptium.coincurrently.features.Portfolio.DataClasses.NewTransaction
import com.cryptium.coincurrently.features.Portfolio.DataClasses.PortfolioModel
import com.cryptium.coincurrently.features.Portfolio.Enums.NewTransactionType
import kotlinx.serialization.Serializable
import java.util.ArrayList

@Serializable
data class PortfolioResponse(
    var id: String? = "",
    var amount: Double? = 0.0,
    var entryCost: Double? = null,
    var currency: String? = "usd",
    // Nullable: Gson ignores Kotlin defaults and can leave these null for older prefs JSON.
    var buyTransactions: ArrayList<Transaction>? = arrayListOf(),
    var sellTransactions: ArrayList<Transaction>? = arrayListOf(),
    var inTransferTransactions: ArrayList<Transaction>? = arrayListOf(),
    var outTransferTransactions: ArrayList<Transaction>? = arrayListOf(),
    var importTransactions: ArrayList<Transaction>? = arrayListOf()
)

fun PortfolioResponse.getCurrentAmount(withTransfers: Boolean = true): Double {
    val buyAmount = buyTransactions.orEmpty().sumOf { it.amount }
    val inTransactions = if(withTransfers) inTransferTransactions.orEmpty().sumOf { it.amount } else 0.0
    val sellAmount = sellTransactions.orEmpty().sumOf { it.amount }
    val outTransactions = if(withTransfers) outTransferTransactions.orEmpty().sumOf { it.amount } else 0.0
    return (amount ?: 0.0) + buyAmount + inTransactions - sellAmount - outTransactions
}

fun PortfolioResponse.migrateToNewPortfolioModel(): PortfolioModel {
    val transactions = mutableListOf<NewTransaction>()
    val defaultCurrency = currency ?: UserPreferences.getSelectedCurrency().code

    amount?.let {
        transactions.add(
            NewTransaction(
                amount = it,
                pricePerCoin = entryCost,
                fee = 0.0,
                note = "",
                transactionType = NewTransactionType.Buy,
                currency = defaultCurrency
            )
        )
    }

    buyTransactions.orEmpty().forEach {
        transactions.add(
            NewTransaction(
                amount = it.amount,
                pricePerCoin = it.pricePerCoin,
                fee = it.fee ?: 0.0,
                note = it.note ?: "",
                transactionType = NewTransactionType.Buy,
                currency = it.currency ?: defaultCurrency
            )
        )
    }

    sellTransactions.orEmpty().forEach {
        transactions.add(
            NewTransaction(
                amount = it.amount,
                pricePerCoin = it.pricePerCoin,
                fee = it.fee ?: 0.0,
                note = it.note ?: "",
                transactionType = NewTransactionType.Sell,
                currency = it.currency ?: defaultCurrency
            )
        )
    }

    inTransferTransactions.orEmpty().forEach {
        transactions.add(
            NewTransaction(
                amount = it.amount,
                pricePerCoin = it.pricePerCoin,
                fee = it.fee ?: 0.0,
                note = it.note ?: "",
                transactionType = NewTransactionType.TransferIn,
                currency = it.currency ?: defaultCurrency
            )
        )
    }

    outTransferTransactions.orEmpty().forEach {
        transactions.add(
            NewTransaction(
                amount = it.amount,
                pricePerCoin = it.pricePerCoin,
                fee = it.fee ?: 0.0,
                note = it.note ?: "",
                transactionType = NewTransactionType.TransferOut,
                currency = it.currency ?: defaultCurrency
            )
        )
    }

    importTransactions.orEmpty().forEach {
        transactions.add(
            NewTransaction(
                amount = it.amount,
                pricePerCoin = it.pricePerCoin,
                fee = it.fee ?: 0.0,
                note = it.note ?: "",
                transactionType = NewTransactionType.ImportAddress,
                address = it.address,
                currency = it.currency ?: defaultCurrency
            )
        )
    }

    return PortfolioModel(
        id = id ?: "",
        transactions = transactions,
        swaps = mutableListOf()
    )
}