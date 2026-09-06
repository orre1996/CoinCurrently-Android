package com.cryptium.coincurrently.features.Portfolio.Models
import com.cryptium.coincurrently.sharedui.models.DateSerializer
import com.cryptium.coincurrently.sharedui.models.UUIDSerializer
import com.cryptium.coincurrently.features.Portfolio.DataClasses.PortfolioTransaction
import com.cryptium.coincurrently.features.Portfolio.DataClasses.PortfolioEvent
import com.cryptium.coincurrently.features.Portfolio.Enums.PortfolioTransactionType
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class PortfolioSwapTransaction(
    @Serializable(with = UUIDSerializer::class)
    override val id: UUID = UUID.randomUUID(),
    @Serializable(with = DateSerializer::class)
    override val date: Date = Date(),
    val selectedCurrency: String,

    // What you're selling
    val fromCurrencyId: String,
    val fromCurrencySymbol: String,
    val fromAmount: Double,
    val fromUnitPrice: Double,

    // What you're buying
    val toCurrencyId: String,
    val toCurrencySymbol: String,
    val toAmount: Double,
    val toUnitPrice: Double,

    // Optional fields
    val fee: Double = 0.0,
    val note: String = ""
) : PortfolioEvent {
    val sellTransaction: PortfolioTransaction
        get() = PortfolioTransaction(
            date = date,
            amount = fromAmount,
            pricePerCoin = fromUnitPrice,
            fee = fee,
            note = note,
            transactionType = PortfolioTransactionType.Sell,
            currency = selectedCurrency
        )

    val buyTransaction: PortfolioTransaction
        get() = PortfolioTransaction(
            date = date,
            amount = toAmount,
            pricePerCoin = toUnitPrice,
            fee = 0.0,
            note = note,
            transactionType = PortfolioTransactionType.Buy,
            currency = selectedCurrency
        )
}
