package com.cryptium.coincurrently.core.networking.ResponseModels
import com.cryptium.coincurrently.core.persistence.UserPreferences

import com.cryptium.coincurrently.sharedui.models.DateSerializer
import com.cryptium.coincurrently.sharedui.models.UUIDSerializer
import com.cryptium.coincurrently.features.Portfolio.DataClasses.PortfolioTransaction
import com.cryptium.coincurrently.features.Portfolio.DataClasses.PortfolioEvent
import com.cryptium.coincurrently.features.Portfolio.Enums.PortfolioTransactionType
import kotlinx.serialization.Serializable
import java.util.Date
import java.util.UUID

data class WalletImportRequest(
    val address: String,
    val blockChain: String
)

@Serializable
enum class Chain(val id: String) {
    BTC("btc"),
    ETH("eth"),
    SOL("sol"),
    XRP("xrp"),
    DOGE("doge"),
    TOKEN("token");

    val coinId: String
        get() {
            return when (this) {
                BTC -> "bitcoin"
                ETH, TOKEN -> "ethereum"
                SOL -> "solana"
                XRP -> "ripple"
                DOGE -> "dogecoin"
            }
        }

    companion object {
        fun fromString(value: String?): Chain? =
            values().firstOrNull { it.name.equals(value, ignoreCase = true) }
    }
}

@Serializable
data class AddressImport(
    @Serializable(with = UUIDSerializer::class)
    override val id: UUID = UUID.randomUUID(),
    @Serializable(with = DateSerializer::class)
    override val date: Date = Date(),
    var walletName: String? = null,
    val coinId: String? = null,
    val address: String,
    val balance: String,
    val tokenInfos: List<TokenInfo>? = null,
    val blockChain: Chain = Chain.BTC
): PortfolioEvent {
    fun importTransaction(): PortfolioTransaction? {
        val balanceValue = balance.toDoubleOrNull() ?: return null
        val currency = UserPreferences.getSelectedCurrency()

        return PortfolioTransaction(
            id = id,
            date = date,
            amount = balanceValue,
            pricePerCoin = null,
            fee = 0.0,
            note = "",
            transactionType = PortfolioTransactionType.ImportAddress,
            currency = currency.code
        )
    }

    fun totalValue(mainId: String?, prices: MutableMap<String, Map<String, Double>>?): Double? {
        if (mainId == null || prices == null) return null

        val currency = UserPreferences.getSelectedCurrency()

        var total = 0.0

        prices.get(mainId)?.get(currency.code.lowercase())?.let { price ->
            balance.toDoubleOrNull()?.let { total += it * price }
        }

        tokenInfos?.forEach { token ->
            val tokenPrice = prices.get(token.tokenId)?.get(currency.code.lowercase()) ?: return@forEach
            val tokenBalance = token.balance.toDoubleOrNull() ?: return@forEach
            total += tokenBalance * tokenPrice
        }

        return total
    }
}

fun AddressImport.toTokenInfo(): TokenInfo {
    return TokenInfo(
        accountHash = "",
        balance = balance,
        tokenHash = "",
        tokenSymbol = "",
        tokenId = ""
    )
}

val AddressImport.allIds: List<String>
    get() =
        listOfNotNull(coinId) +
                (tokenInfos?.mapNotNull { it.tokenId } ?: emptyList())

@Serializable
data class TokenInfo(
    val accountHash: String,
    val balance: String,
    val tokenHash: String,
    val tokenSymbol: String,
    val tokenId: String?
) {
    fun toAddressImport(date: Date): AddressImport {
        return AddressImport(
            date = date,
            coinId = tokenId,
            address = tokenHash,
            balance = balance,
            tokenInfos = null,
            blockChain = Chain.TOKEN
        )
    }
}