package com.cryptium.coincurrently.core.networking.ResponseModels
import com.cryptium.coincurrently.core.persistence.UserPreferences
import com.cryptium.coincurrently.features.Portfolio.DataClasses.PortfolioTransaction
import com.cryptium.coincurrently.features.Portfolio.DataClasses.PortfolioModel
import com.cryptium.coincurrently.features.Portfolio.Enums.PortfolioTransactionType
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

fun PortfolioResponse.migrateToNewPortfolioModel(): PortfolioModel {
    val transactions = mutableListOf<PortfolioTransaction>()
    val defaultCurrency = currency ?: UserPreferences.getSelectedCurrency().code

    amount?.let {
        transactions.add(
            PortfolioTransaction(
                amount = it,
                pricePerCoin = entryCost,
                fee = 0.0,
                note = "",
                transactionType = PortfolioTransactionType.Buy,
                currency = defaultCurrency
            )
        )
    }

    buyTransactions.orEmpty().forEach {
        transactions.add(
            PortfolioTransaction(
                amount = it.amount,
                pricePerCoin = it.pricePerCoin,
                fee = it.fee ?: 0.0,
                note = it.note ?: "",
                transactionType = PortfolioTransactionType.Buy,
                currency = it.currency ?: defaultCurrency
            )
        )
    }

    sellTransactions.orEmpty().forEach {
        transactions.add(
            PortfolioTransaction(
                amount = it.amount,
                pricePerCoin = it.pricePerCoin,
                fee = it.fee ?: 0.0,
                note = it.note ?: "",
                transactionType = PortfolioTransactionType.Sell,
                currency = it.currency ?: defaultCurrency
            )
        )
    }

    inTransferTransactions.orEmpty().forEach {
        transactions.add(
            PortfolioTransaction(
                amount = it.amount,
                pricePerCoin = it.pricePerCoin,
                fee = it.fee ?: 0.0,
                note = it.note ?: "",
                transactionType = PortfolioTransactionType.TransferIn,
                currency = it.currency ?: defaultCurrency
            )
        )
    }

    outTransferTransactions.orEmpty().forEach {
        transactions.add(
            PortfolioTransaction(
                amount = it.amount,
                pricePerCoin = it.pricePerCoin,
                fee = it.fee ?: 0.0,
                note = it.note ?: "",
                transactionType = PortfolioTransactionType.TransferOut,
                currency = it.currency ?: defaultCurrency
            )
        )
    }

    importTransactions.orEmpty().forEach {
        transactions.add(
            PortfolioTransaction(
                amount = it.amount,
                pricePerCoin = it.pricePerCoin,
                fee = it.fee ?: 0.0,
                note = it.note ?: "",
                transactionType = PortfolioTransactionType.ImportAddress,
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