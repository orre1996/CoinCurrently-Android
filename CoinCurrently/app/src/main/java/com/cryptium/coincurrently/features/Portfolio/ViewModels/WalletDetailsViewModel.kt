package com.cryptium.coincurrently.features.Portfolio.ViewModels
import com.cryptium.coincurrently.core.networking.ResponseModels.AddressImport
import com.cryptium.coincurrently.core.networking.ResponseModels.TokenInfo
import com.cryptium.coincurrently.core.networking.ResponseModels.allIds
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.cryptium.coincurrently.sharedui.components.GlobalFunctions
import com.cryptium.coincurrently.core.persistence.StorageKeys
import com.cryptium.coincurrently.core.persistence.StorageManager
import com.cryptium.coincurrently.features.Portfolio.PortfolioRepository
import com.cryptium.coincurrently.features.PriceAlerts.PriceAlertsRepository
import kotlinx.coroutines.launch
import com.cryptium.coincurrently.core.networking.ResponseModels.Toplist
import com.cryptium.coincurrently.core.networking.ResponseModels.getPreferredChange
import java.util.Date

class WalletDetailsViewModelFactory(
    val wallet: AddressImport,
    val coin: Toplist,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WalletDetailsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return WalletDetailsViewModel(
                wallet = wallet,
                coin = coin
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class WalletDetailsViewModel(
    val wallet: AddressImport,
    val coin: Toplist
): ViewModel() {
    private val portfolioRepository by lazy { PortfolioRepository }
    private val priceAlertsRepository by lazy { PriceAlertsRepository }

    var allPrices: MutableMap<String, Map<String, Double>>? by mutableStateOf(null)
    var coins: List<Toplist>? by mutableStateOf(null)
    var syncedDate: Date? by mutableStateOf(null)
    var sortedTokens: List<TokenInfo> by mutableStateOf(listOf())

    var hideData: Boolean by mutableStateOf(GlobalFunctions.getHidePortfolioData())


    init {
        portfolioRepository.portfolioAllCurrencies?.let {
            allPrices = it
        }

        syncedDate = StorageManager.get(StorageKeys.WALLETS_REFRESH_DATE)

        sortCoins()
    }

    val totalValue: Double?
        get() = wallet.totalValue(
            mainId = coin.id,
            prices = allPrices
        )

    val changePercent: Double?
        get() {
            val changeValue = changeValue ?: return null
            val totalValue = totalValue ?: return null

            val previousValue = totalValue - changeValue
            if (previousValue == 0.0) return null

            return (changeValue / previousValue) * 100
        }

    val changeValue: Double?
        get() {
            val totalValue = totalValue ?: return null
            val previousValue = calculatePreviousValue(coins, wallet) ?: return null
            return totalValue - previousValue
        }

    fun toggleHideData() {
        hideData = !hideData
        GlobalFunctions.setHidePortfolioData(hideData)
    }

    private fun previousPrice(
        current: Double,
        percentChange: Double
    ): Double? {
        val change = percentChange / 100.0
        val denominator = 1 + change
        if (denominator <= 0) return null
        return current / denominator
    }


    private fun calculatePreviousValue(
        coins: List<Toplist>?,
        wallet: AddressImport
    ): Double? {
        coins ?: return null

        var total = 0.0

        wallet.coinId?.let { coinId ->
            val coin = coins.firstOrNull { it.id == coinId } ?: return@let
            val currentPrice = coin.current_price ?: return@let
            val balance = wallet.balance.toDoubleOrNull() ?: return@let
            val prevPrice = previousPrice(
                current = currentPrice,
                percentChange = coin.getPreferredChange()
            ) ?: return@let

            total += prevPrice * balance
        }

        wallet.tokenInfos.orEmpty().forEach { token ->
            val tokenId = token.tokenId ?: return@forEach
            val coin = coins.firstOrNull { it.id == tokenId } ?: return@forEach
            val currentPrice = coin.current_price ?: return@forEach
            val balance = token.balance.toDoubleOrNull() ?: return@forEach
            val prevPrice = previousPrice(
                current = currentPrice,
                percentChange = coin.getPreferredChange()
            ) ?: return@forEach

            total += prevPrice * balance
        }

        return total
    }

    fun getAllPortfolioCurrencies() {
        viewModelScope.launch {
            allPrices = portfolioRepository.getPortfolioAllCurrencies()
        }
    }

    fun getCoins() {
        viewModelScope.launch {
            coins = priceAlertsRepository.downloadRemainingCoins(wallet.allIds)

            sortCoins()
        }
    }

    fun deleteWallet() {
        val id = coin.id ?: return

        portfolioRepository.deleteTransaction(
            id = id,
            uuid = wallet.id
        )
    }

    fun renameWallet(
        newName: String
    ) {
        val id = coin.id ?: return

        wallet.walletName = newName
        portfolioRepository.renameWallet(
            id = id,
            address = wallet.address,
            newName = newName
        )
    }

    fun sortCoins() {
        val prices: Map<String, Double> = coins
            ?.mapNotNull { coin ->
                val id = coin.id
                val price = coin.current_price
                if (id != null && price != null) id to price else null
            }
            ?.toMap()
            ?: emptyMap()

        sortedTokens = (wallet.tokenInfos ?: emptyList())
            .sortedByDescending { token ->
                val balance = token.balance.toDoubleOrNull() ?: 0.0
                val price = prices[token.tokenId] ?: 0.0
                balance * price
            }
    }
}