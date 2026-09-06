package com.cryptium.coincurrently.features.Portfolio.DataClasses
import com.cryptium.coincurrently.sharedui.models.DateSerializer
import com.cryptium.coincurrently.sharedui.models.UUIDSerializer
import com.cryptium.coincurrently.features.Portfolio.Enums.PortfolioTransactionType
import kotlinx.serialization.Serializable
import java.util.Date
import java.util.UUID

interface PortfolioEvent {
    val id: UUID
    val date: Date
}

@Serializable
data class PortfolioTransaction(
    @Serializable(with = UUIDSerializer::class)
    override val id: UUID = UUID.randomUUID(),
    @Serializable(with = DateSerializer::class)
    override val date: Date = Date(),
    val amount: Double,
    val pricePerCoin: Double? = null,
    val fee: Double,
    val note: String,
    val transactionType: PortfolioTransactionType,
    val address: String? = null,
    val currency: String
): PortfolioEvent

val PortfolioTransaction.totalSpent: Double
    get() = (pricePerCoin ?: 0.0) * amount + fee

fun PortfolioTransaction.getProfitPercent(currentPrice: Double?): Double? {
    val pricePerCoin = pricePerCoin ?: return null

    val originalCost = amount * pricePerCoin
    val currentValue = amount * (currentPrice ?: 0.0)

    return ((currentValue / originalCost) - 1) * 100
}

fun PortfolioTransaction.getProfitValue(currentPrice: Double?): Double? {
    val pricePerCoin = pricePerCoin ?: return null

    val profitPercent = getProfitPercent(currentPrice) ?: 0.0
    return (profitPercent / 100) * pricePerCoin * amount
}

val PortfolioTransaction.isAddOrInTransferTransaction: Boolean
    get() = when (transactionType) {
        PortfolioTransactionType.Buy,
        PortfolioTransactionType.ImportAddress,
        PortfolioTransactionType.TransferIn,
        PortfolioTransactionType.Swap -> true
        PortfolioTransactionType.Sell,
        PortfolioTransactionType.TransferOut -> false
    }