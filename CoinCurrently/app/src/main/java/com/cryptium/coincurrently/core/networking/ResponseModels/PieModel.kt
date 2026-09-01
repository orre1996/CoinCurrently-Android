package com.cryptium.coincurrently.core.networking.ResponseModels
import com.cryptium.coincurrently.core.networking.ResponseModels.AddressImport
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.cryptium.coincurrently.core.config.AppLocale
import com.cryptium.coincurrently.R
import com.cryptium.coincurrently.sharedui.theme.mainGreen
import com.cryptium.coincurrently.sharedui.theme.mainRed
import com.cryptium.coincurrently.features.Portfolio.Models.NewSwapTransaction
import com.cryptium.coincurrently.features.Portfolio.Models.TransactionType
import com.cryptium.coincurrently.features.Portfolio.Models.TransferType
import com.cryptium.coincurrently.core.extensions.getAmount
import com.cryptium.coincurrently.core.extensions.getTransferType
import com.cryptium.coincurrently.core.extensions.sumAmounts
import com.cryptium.coincurrently.sharedui.components.GlobalFunctions
import com.cryptium.coincurrently.sharedui.components.PreferredTimeframe
import com.cryptium.coincurrently.features.Portfolio.DataClasses.NewTransaction
import com.cryptium.coincurrently.features.Portfolio.DataClasses.PortfolioEvent
import com.cryptium.coincurrently.features.Portfolio.DataClasses.PortfolioModel
import com.cryptium.coincurrently.features.Portfolio.Enums.NewTransactionType
import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable
import java.text.SimpleDateFormat
import java.util.*

@Serializable
data class Pie(
    @SerializedName("data")
    val data: Toplist?,
    var color: String? = null,
    // Nullable at runtime: Gson ignores Kotlin defaults for older / partial cache JSON.
    var transactions: MutableList<NewTransaction> = mutableListOf(),
    var swaps: MutableList<NewSwapTransaction> = mutableListOf(),
    var imports: MutableList<AddressImport> = mutableListOf()
)

/**
 * Gson can leave list fields null despite non-null Kotlin types. Repair before iteration.
 */
fun Pie.sanitizeGsonNullLists(): Pie {
    @Suppress("UNCHECKED_CAST")
    transactions = (transactions as MutableList<NewTransaction>?) ?: mutableListOf()
    @Suppress("UNCHECKED_CAST")
    swaps = (swaps as MutableList<NewSwapTransaction>?) ?: mutableListOf()
    @Suppress("UNCHECKED_CAST")
    imports = (imports as MutableList<AddressImport>?) ?: mutableListOf()
    return this
}

fun Pie.toPortfolioCurrencyModel(): PortfolioModel? {
    sanitizeGsonNullLists()
    return data?.id?.let {
        PortfolioModel(
            id = it,
            transactions = transactions,
            swaps = swaps,
            imports = imports
        )
    } ?: run {
        null
    }
}

val Pie.shouldShow: Boolean
    get() {
        sanitizeGsonNullLists()
        val hasActivity =
            transactions.isNotEmpty() ||
                    swaps.isNotEmpty() ||
                    imports.any { it.balance.isNotEmpty() }

        val isCompletelyEmpty =
            transactions.isEmpty() &&
                    swaps.isEmpty() &&
                    imports.isEmpty()

        return hasActivity || isCompletelyEmpty
    }

@Serializable
data class Transaction(
    var id: String = UUID.randomUUID().toString(),
    var date: String = SimpleDateFormat("MMMM d, yyyy HH:mm:ss", AppLocale.enUsPosix).format(Date()),
    var amount: Double,
    var pricePerCoin: Double?,
    var fee: Double? = null,
    var note: String? = null,
    var transferType: Int = TransferType.INTYPE.ordinal,
    var address: String? = null,
    var currency: String? = GlobalFunctions.getSelectedCurrency().code,
)

/*fun Pie.getUnrealizedProfitLossValue(
    prices: Map<String, Double>?,
    showCurrency: String = GlobalFunctions.getSelectedCurrency().code.lowercase()
): Double? {
    var spent = transactions
        .filter { it.transactionType == NewTransactionType.Buy }
        .sumOf { tx ->
            val multiplier = priceMultiplier(prices, showCurrency, tx.currency)
            tx.amount * ((tx.pricePerCoin ?: 1.0) * multiplier)
        }

    spent += transactions
        .filter { it.transactionType == NewTransactionType.TransferIn }
        .sumOf { tx ->
            val multiplier = priceMultiplier(prices, showCurrency, tx.currency)
            tx.amount * ((prices?.get(showCurrency.lowercase()) ?: 0.0) * multiplier)
        }

    transactions
        .filter { it.transactionType == NewTransactionType.Sell }
        .forEach { tx ->
            val multiplier = priceMultiplier(prices, showCurrency, tx.currency)
            spent -= tx.amount * ((tx.pricePerCoin ?: 1.0) * multiplier)
        }

    transactions
        .filter { it.transactionType == NewTransactionType.TransferOut }
        .forEach { tx ->
            val multiplier = priceMultiplier(prices, showCurrency, tx.currency)
            spent -= tx.amount * ((prices?.get(showCurrency.lowercase()) ?: 0.0) * multiplier)
        }

    spent += transactions
        .filter { it.transactionType == NewTransactionType.ImportAddress }
        .sumOf { tx ->
            val multiplier = priceMultiplier(prices, showCurrency, tx.currency)
            tx.amount * ((prices?.get(showCurrency.lowercase()) ?: 0.0) * multiplier)
        }

    val currentValue =
        transactions.getAmount(withTransfers = true) * (prices?.get(showCurrency.lowercase()) ?: 1.0)

    return currentValue - spent
}*/