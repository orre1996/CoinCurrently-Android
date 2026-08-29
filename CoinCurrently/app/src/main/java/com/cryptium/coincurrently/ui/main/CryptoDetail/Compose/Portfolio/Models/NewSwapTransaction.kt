package com.cryptium.coincurrently.ui.main.CryptoDetail.Compose.Portfolio.Models

import com.cryptium.coincurrently.ui.main.General.DataClasses.DateSerializer
import com.cryptium.coincurrently.ui.main.General.DataClasses.UUIDSerializer
import com.cryptium.coincurrently.ui.main.PortfolioTab.DataClasses.NewTransaction
import com.cryptium.coincurrently.ui.main.PortfolioTab.DataClasses.PortfolioEvent
import com.cryptium.coincurrently.ui.main.PortfolioTab.Enums.NewTransactionType
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class NewSwapTransaction(
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
    val sellTransaction: NewTransaction
        get() = NewTransaction(
            date = date,
            amount = fromAmount,
            pricePerCoin = fromUnitPrice,
            fee = fee,
            note = note,
            transactionType = NewTransactionType.Sell,
            currency = selectedCurrency
        )

    val buyTransaction: NewTransaction
        get() = NewTransaction(
            date = date,
            amount = toAmount,
            pricePerCoin = toUnitPrice,
            fee = 0.0,
            note = note,
            transactionType = NewTransactionType.Buy,
            currency = selectedCurrency
        )
}
