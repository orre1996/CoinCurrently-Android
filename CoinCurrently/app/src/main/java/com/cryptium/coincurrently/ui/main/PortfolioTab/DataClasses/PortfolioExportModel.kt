package com.cryptium.coincurrently.ui.main.PortfolioTab.DataClasses

import AddressImport
import Chain
import TokenInfo
import com.cryptium.coincurrently.ui.main.CryptoDetail.Compose.Portfolio.Models.NewSwapTransaction
import com.cryptium.coincurrently.ui.main.General.DataClasses.Iso8601DateSerializer
import com.cryptium.coincurrently.ui.main.PortfolioTab.Enums.NewTransactionType
import kotlinx.serialization.Serializable
import networking.ResponseModels.Pie
import networking.ResponseModels.sanitizeGsonNullLists
import java.util.Date

@Serializable
data class PortfolioExportModel(
    val coinId: String,
    val transactions: List<PortfolioExportTransaction>,
    val swaps: List<PortfolioExportSwap>,
    val imports: List<PortfolioExportImport>,
)

@Serializable
data class PortfolioExportTransaction(
    @Serializable(with = Iso8601DateSerializer::class)
    val date: Date,
    val amount: Double,
    val pricePerCoin: Double?,
    val fee: Double,
    val note: String,
    val transactionType: NewTransactionType,
    val address: String?,
    val currency: String,
)

@Serializable
data class PortfolioExportSwap(
    @Serializable(with = Iso8601DateSerializer::class)
    val date: Date,
    val selectedCurrency: String,
    val fromCurrencyId: String,
    val fromCurrencySymbol: String,
    val fromAmount: Double,
    val fromUnitPrice: Double,
    val toCurrencyId: String,
    val toCurrencySymbol: String,
    val toAmount: Double,
    val toUnitPrice: Double,
    val fee: Double,
    val note: String,
)

@Serializable
data class PortfolioExportImport(
    @Serializable(with = Iso8601DateSerializer::class)
    val date: Date,
    val walletName: String?,
    val coinId: String?,
    val address: String,
    val balance: String,
    val tokenInfos: List<TokenInfo>?,
    val blockChain: Chain,
)

fun NewTransaction.toPortfolioExportTransaction() = PortfolioExportTransaction(
    date = date,
    amount = amount,
    pricePerCoin = pricePerCoin,
    fee = fee,
    note = note,
    transactionType = transactionType,
    address = address?.trim()?.takeIf { it.isNotEmpty() },
    currency = currency,
)

fun NewSwapTransaction.toPortfolioExportSwap() = PortfolioExportSwap(
    date = date,
    selectedCurrency = selectedCurrency,
    fromCurrencyId = fromCurrencyId,
    fromCurrencySymbol = fromCurrencySymbol,
    fromAmount = fromAmount,
    fromUnitPrice = fromUnitPrice,
    toCurrencyId = toCurrencyId,
    toCurrencySymbol = toCurrencySymbol,
    toAmount = toAmount,
    toUnitPrice = toUnitPrice,
    fee = fee,
    note = note,
)

fun AddressImport.toPortfolioExportImport() = PortfolioExportImport(
    date = date,
    walletName = walletName,
    coinId = coinId,
    address = address,
    balance = balance,
    tokenInfos = tokenInfos,
    blockChain = blockChain,
)

fun Pie.toPortfolioExportModel(): PortfolioExportModel? {
    sanitizeGsonNullLists()
    val coinId = data?.id ?: return null
    return PortfolioExportModel(
        coinId = coinId,
        transactions = transactions.map { it.toPortfolioExportTransaction() },
        swaps = swaps.map { it.toPortfolioExportSwap() },
        imports = imports.map { it.toPortfolioExportImport() },
    )
}
