package com.cryptium.coincurrently.features.Portfolio.Enums
import com.cryptium.coincurrently.core.localization.Localization
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
                Buy -> Localization.string("Bought")
                Sell -> Localization.string("Sold")
                Swap -> Localization.string("Swapped")
                TransferIn -> Localization.string("Received")
                TransferOut -> Localization.string("Sent")
                ImportAddress -> Localization.string("Imported")
            }
        }

    val getTransactionPrefixSign: String
        get() {
            return if(isAddTransfer) "+" else "-"
        }
}