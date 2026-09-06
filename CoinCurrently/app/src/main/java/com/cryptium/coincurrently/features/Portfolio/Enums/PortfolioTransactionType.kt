package com.cryptium.coincurrently.features.Portfolio.Enums
import kotlinx.serialization.Serializable

@Serializable
enum class PortfolioTransactionType(val value: String) {
    Buy("Buy"),
    Sell("Sell"),
    Swap("Swap"),
    TransferIn("TransferIn"),
    TransferOut("TransferOut"),
    ImportAddress("Import");

    val isTransfer: Boolean
        get() = this == TransferIn || this == TransferOut

    val isAddTransfer: Boolean
        get() = this == Buy || this == TransferIn || this == ImportAddress

    val transactionText: String
        get() {
            return when (this) {
                Buy -> "Bought"
                Sell -> "Sold"
                Swap -> "Swapped"
                TransferIn -> "Received"
                TransferOut -> "Sent"
                ImportAddress -> "Imported"
            }
        }

    val getTransactionPrefixSign: String
        get() {
            return if(isAddTransfer) "+" else "-"
        }
}