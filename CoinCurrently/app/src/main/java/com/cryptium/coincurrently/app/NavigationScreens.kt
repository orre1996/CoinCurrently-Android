package com.cryptium.coincurrently.app

import com.cryptium.coincurrently.features.Portfolio.Models.PortfolioSwapTransaction
import com.cryptium.coincurrently.features.Portfolio.DataClasses.PortfolioTransaction
import com.cryptium.coincurrently.features.Portfolio.Enums.PortfolioTransactionType
import com.cryptium.coincurrently.core.networking.ResponseModels.CryptoDetailInfoModel
import com.cryptium.coincurrently.core.networking.ResponseModels.Derivative
import com.cryptium.coincurrently.core.networking.ResponseModels.ExchangeList
import com.cryptium.coincurrently.core.networking.ResponseModels.NFTToplist
import kotlinx.serialization.Serializable
import com.cryptium.coincurrently.core.networking.ResponseModels.CryptoCategory
import com.cryptium.coincurrently.core.networking.ResponseModels.ExchangeResponse
import com.cryptium.coincurrently.core.networking.ResponseModels.PriceVolumeModel
import com.cryptium.coincurrently.core.networking.ResponseModels.Toplist
import com.cryptium.coincurrently.core.networking.ResponseModels.AddressImport

@Serializable
enum class SearchCase(val rawValue: String) {
    @kotlinx.serialization.SerialName("")
    OnlyCoins(""),
    
    @kotlinx.serialization.SerialName("Coins")
    Coins("Coins"),
    
    @kotlinx.serialization.SerialName("NFTs")
    NFTs("NFTs"),
    
    @kotlinx.serialization.SerialName("Sectors")
    Categories("Sectors"),
    
    @kotlinx.serialization.SerialName("Exchanges")
    Exchanges("Exchanges"),
}

val SearchCase.accessibilityItemType: String
    get() = when (this) {
        SearchCase.OnlyCoins, SearchCase.Coins -> "Coin"
        SearchCase.NFTs -> "NFT"
        SearchCase.Categories -> "Sector"
        SearchCase.Exchanges -> "Exchange"
    }

@Serializable
sealed class SearchScreenCase {
    @Serializable
    data class Search(val list: SearchCase) : SearchScreenCase()
    @Serializable
    data class Transaction(val transactionType: PortfolioTransactionType) : SearchScreenCase()
    @Serializable
    data class FixedList(val list: List<Toplist>) : SearchScreenCase()
}

val SearchScreenCase.title: String
    get() = when (this) {
        is SearchScreenCase.Search -> when (list) {
            SearchCase.OnlyCoins, SearchCase.Coins -> "Select Coin"
            SearchCase.NFTs -> "Select NFT"
            SearchCase.Categories -> "Select Sector"
            SearchCase.Exchanges -> "Select Exchange"
        }
        is SearchScreenCase.Transaction -> "Select Coin"
        is SearchScreenCase.FixedList -> "Select Coin"
    }

val SearchScreenCase.textfieldPlaceholder: String
    get() = when (this) {
        is SearchScreenCase.Search -> when (list) {
            SearchCase.OnlyCoins, SearchCase.Coins -> "e.g. BTC or Ethereum"
            SearchCase.NFTs -> "e.g. CryptoPunks"
            SearchCase.Categories -> "e.g. Layer 1"
            SearchCase.Exchanges -> "e.g. Coinbase"
        }
        is SearchScreenCase.Transaction -> "e.g. BTC or Ethereum"
        is SearchScreenCase.FixedList -> "e.g. BTC or Ethereum"
    }

val SearchScreenCase.coins: List<Toplist>
    get() = when (this) {
        is SearchScreenCase.FixedList -> list
        else -> emptyList()
    }

@Serializable
sealed class NavigationScreens() {
    @Serializable
    object Loading: NavigationScreens()

    @Serializable
    object Home: NavigationScreens()

    @Serializable
    object Portfolio: NavigationScreens()

    @Serializable
    object Market: NavigationScreens()

    @Serializable
    object News: NavigationScreens()

    @Serializable
    object Settings: NavigationScreens()

    @Serializable
    data class CryptoDetail(
        val cryptoDetailInfoModel: CryptoDetailInfoModel
    ): NavigationScreens()

    @Serializable
    data class CompareCoins(
        val cryptoDetailInfoModel: CryptoDetailInfoModel?,
        val priceVolumeModel: PriceVolumeModel?,
        val compareId: String
    ): NavigationScreens()

    @Serializable
    data class AddPortfolioTransaction(
        val transactionType: PortfolioTransactionType,
        val currencyDataModel: CryptoDetailInfoModel?,
        val transaction: PortfolioTransaction?,
        val popToRoot: Boolean
    ): NavigationScreens()

    @Serializable
    data class AddPortfolioSwapTransaction(
        val transactionType: PortfolioTransactionType,
        val currencyDataModel: CryptoDetailInfoModel?,
        val transaction: PortfolioSwapTransaction?,
        val popToRoot: Boolean
    ): NavigationScreens()


    @Serializable
    data class ImportWalletTransaction(
        val popToRoot: Boolean
    ): NavigationScreens()

    @Serializable
    data class Search(
        val screenCase: SearchScreenCase
    ): NavigationScreens()

    @Serializable
    object Feedback: NavigationScreens()

    // Home

    @Serializable
    object FearGreed: NavigationScreens()

    @Serializable
    object FearGreedHelp: NavigationScreens()

    @Serializable
    object GlobalMarket: NavigationScreens()

    @Serializable
    object GasFees: NavigationScreens()

    // Portfolio

    @Serializable
    data class WalletDetails(
        val wallet: AddressImport,
        val coin: Toplist
    ): NavigationScreens()

    // Market

    @Serializable
    data class NFTDetail(
        val nftToplist: NFTToplist
    ): NavigationScreens()

    @Serializable
    data class CategoryDetail(
        val category: CryptoCategory
    ): NavigationScreens()

    @Serializable
    data class ExchangeDetail(
        val exchange: ExchangeResponse
    ): NavigationScreens()

    @Serializable
    data class DerivativeDetail(
        val derivative: Derivative
    ): NavigationScreens()

    @Serializable
    data class TreasuryDetail(
        val holder: com.cryptium.coincurrently.core.networking.ResponseModels.PublicTreasuryHolder,
        val coinId: String,
    ): NavigationScreens()

    // More

    @Serializable
    object CurrencySelector: NavigationScreens()

    @Serializable
    object Calculator: NavigationScreens()

    @Serializable
    object PriceAlerts: NavigationScreens()

    @Serializable
    object Achievements: NavigationScreens()

    @Serializable
    data class Referral(val referralCode: String = ""): NavigationScreens()
}

fun NavigationScreens.getRouteString(): String {
    return when (this) {
        is NavigationScreens.Loading -> "Loading"
        is NavigationScreens.Home -> "Home"
        is NavigationScreens.Portfolio -> "Portfolio"
        is NavigationScreens.Market -> "Market"
        is NavigationScreens.News -> "News"
        is NavigationScreens.Settings -> "Settings"
        is NavigationScreens.CryptoDetail -> "CryptoDetail"
        is NavigationScreens.CompareCoins -> "CompareCoins"
        is NavigationScreens.WalletDetails -> "WalletDetails"
        is NavigationScreens.AddPortfolioTransaction -> "NewPortfolioTransaction"
        is NavigationScreens.AddPortfolioSwapTransaction -> "NewPortfolioSwapTransaction"
        is NavigationScreens.ImportWalletTransaction -> "NewImportTransaction"
        is NavigationScreens.Feedback -> "Feedback"
        is NavigationScreens.FearGreed -> "FearGreed"
        is NavigationScreens.FearGreedHelp -> "FearGreedHelp"
        is NavigationScreens.GlobalMarket -> "GlobalMarket"
        is NavigationScreens.GasFees -> "GasFees"
        is NavigationScreens.NFTDetail -> "NFTDetail"
        is NavigationScreens.CategoryDetail -> "CategoryDetail"
        is NavigationScreens.ExchangeDetail -> "ExchangeDetail"
        is NavigationScreens.DerivativeDetail -> "DerivativeDetail"
        is NavigationScreens.TreasuryDetail -> "TreasuryDetail"
        is NavigationScreens.CurrencySelector -> "CurrencySelector"
        is NavigationScreens.Calculator -> "Calculator"
        is NavigationScreens.PriceAlerts -> "PriceAlerts"
        is NavigationScreens.Achievements -> "Achievements"
        is NavigationScreens.Referral -> "Referral"
        is NavigationScreens.Search -> "SelectCoin"
    }
}

fun navigationScreenForLatestTab(index: Int): NavigationScreens = when (index) {
    0 -> NavigationScreens.Home
    1 -> NavigationScreens.Portfolio
    2 -> NavigationScreens.Market
    3 -> NavigationScreens.News
    else -> NavigationScreens.Settings
}