package com.cryptium.coincurrently.features.Portfolio.DataClasses
import com.cryptium.coincurrently.core.networking.ResponseModels.AddressImport
import com.cryptium.coincurrently.features.Portfolio.Models.PortfolioSwapTransaction
import kotlinx.serialization.Serializable

@Serializable
data class PortfolioModel(
    val id: String,
    // Nullable at runtime: Gson ignores Kotlin defaults for older / partial prefs JSON.
    val transactions: MutableList<PortfolioTransaction> = mutableListOf(),
    val swaps: MutableList<PortfolioSwapTransaction> = mutableListOf(),
    val imports: MutableList<AddressImport> = mutableListOf()
)

/** Rebuild with non-null lists after Gson may have left them null. */
fun PortfolioModel.sanitized(): PortfolioModel {
    @Suppress("UNCHECKED_CAST")
    return PortfolioModel(
        id = id,
        transactions = (transactions as MutableList<PortfolioTransaction>?) ?: mutableListOf(),
        swaps = (swaps as MutableList<PortfolioSwapTransaction>?) ?: mutableListOf(),
        imports = (imports as MutableList<AddressImport>?) ?: mutableListOf(),
    )
}

fun List<PortfolioModel>.getAllTransactions(id: String?): List<PortfolioTransaction> {
    if (id == null) return emptyList()

    val coins = filter { it.id == id }

    @Suppress("UNCHECKED_CAST")
    val transactions = coins.flatMap {
        (it.transactions as List<PortfolioTransaction>?) ?: emptyList()
    }

    @Suppress("UNCHECKED_CAST")
    val buySwapTransactions = coins
        .flatMap { (it.swaps as List<PortfolioSwapTransaction>?) ?: emptyList() }
        .map { it.buyTransaction }

    @Suppress("UNCHECKED_CAST")
    val swapSellTransactions = flatMap {
        (it.swaps as List<PortfolioSwapTransaction>?) ?: emptyList()
    }
        .filter { it.fromCurrencyId == id }
        .map { it.sellTransaction }

    @Suppress("UNCHECKED_CAST")
    val importTransactions = coins
        .flatMap { (it.imports as List<AddressImport>?) ?: emptyList() }
        .mapNotNull { it.importTransaction() }

    @Suppress("UNCHECKED_CAST")
    val tokenTransactions = flatMap {
        (it.imports as List<AddressImport>?) ?: emptyList()
    }
        .flatMap { importItem ->
            (importItem.tokenInfos ?: emptyList()).map { token ->
                token to importItem.date
            }
        }
        .filter { (token, _) -> token.tokenId == id }
        .mapNotNull { (token, date) ->
            token.toAddressImport(date).importTransaction()
        }

    return transactions + buySwapTransactions + swapSellTransactions + importTransactions + tokenTransactions
}