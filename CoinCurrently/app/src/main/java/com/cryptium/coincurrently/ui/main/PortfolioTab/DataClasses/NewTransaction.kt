package com.cryptium.coincurrently.ui.main.PortfolioTab.DataClasses

import com.cryptium.coincurrently.ui.main.General.DataClasses.DateSerializer
import com.cryptium.coincurrently.ui.main.General.DataClasses.UUIDSerializer
import com.cryptium.coincurrently.ui.main.PortfolioTab.Enums.NewTransactionType
import kotlinx.serialization.Serializable
import java.util.Date
import java.util.UUID

interface PortfolioEvent {
    val id: UUID
    val date: Date
}

@Serializable
data class NewTransaction(
    @Serializable(with = UUIDSerializer::class)
    override val id: UUID = UUID.randomUUID(),
    @Serializable(with = DateSerializer::class)
    override val date: Date = Date(),
    val amount: Double,
    val pricePerCoin: Double? = null,
    val fee: Double,
    val note: String,
    val transactionType: NewTransactionType,
    val address: String? = null,
    val currency: String
): PortfolioEvent

val NewTransaction.totalSpent: Double
    get() = (pricePerCoin ?: 0.0) * amount + fee

fun NewTransaction.getProfitPercent(currentPrice: Double?): Double? {
    val pricePerCoin = pricePerCoin ?: return null

    val originalCost = amount * pricePerCoin
    val currentValue = amount * (currentPrice ?: 0.0)

    return ((currentValue / originalCost) - 1) * 100
}

fun NewTransaction.getProfitValue(currentPrice: Double?): Double? {
    val pricePerCoin = pricePerCoin ?: return null

    val profitPercent = getProfitPercent(currentPrice) ?: 0.0
    return (profitPercent / 100) * pricePerCoin * amount
}

val NewTransaction.isAddOrInTransferTransaction: Boolean
    get() = when (transactionType) {
        NewTransactionType.Buy,
        NewTransactionType.ImportAddress,
        NewTransactionType.TransferIn,
        NewTransactionType.Swap -> true
        NewTransactionType.Sell,
        NewTransactionType.TransferOut -> false
    }