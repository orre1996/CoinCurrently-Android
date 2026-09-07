package com.cryptium.coincurrently.features.Market.NFT.ViewModels
import com.cryptium.coincurrently.core.persistence.UserPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.*
import com.cryptium.coincurrently.core.extensions.getSelectedCurrency
import com.cryptium.coincurrently.sharedui.models.SortOption
import com.cryptium.coincurrently.features.GlobalMarket.GlobalMarketRepository
import com.cryptium.coincurrently.core.persistence.StorageKeys
import com.cryptium.coincurrently.core.persistence.StorageManager
import com.cryptium.coincurrently.core.persistence.CurrencyRepository
import com.cryptium.coincurrently.features.Market.NFT.Repositories.NFTRepository
import com.cryptium.coincurrently.features.GlobalMarket.CachedCoinsList
import com.cryptium.coincurrently.core.networking.ResponseModels.*

class NftToplistViewModel : ViewModel() {

    private val nftRepository by lazy { NFTRepository }
    private val currencyRepository by lazy { CurrencyRepository }
    private val globalRepository by lazy { GlobalMarketRepository }

    private var coinsList: List<CoinsList> = listOf()
    private var chainSymbolLookup: Map<String, String> = mapOf()

    var nftToplist: List<NFTToplist> by mutableStateOf(listOf())
    var nftPlatforms: List<NFTPlatform>? by mutableStateOf(null)
    var bitcoin: CryptoDetailInfoModel? by mutableStateOf(null)
    var multiplier: Double? by mutableStateOf(null)

    var selectedSortOption: SortOption by mutableStateOf(SortOption.MktCapFiatDesc)
    var selectedPlatform: NFTPlatform by mutableStateOf(NFTPlatform(id = "all", name = "All Chains", image = null))

    init {
        selectedSortOption = StorageManager.get<SortOption>(StorageKeys.NFTS_SORT_OPTION) ?: SortOption.MktCapFiatDesc
        selectedPlatform = StorageManager.get<NFTPlatform>(StorageKeys.NFT_PLATFORM) ?: NFTPlatform(id = "all", name = "All Chains", image = null)

        val platformId = if (selectedPlatform.id == "all") null else selectedPlatform.id
        nftRepository.getCachedToplist(selectedSortOption, platformId)?.let {
            if (it.isNotEmpty()) {
                nftToplist = sortToplist(sortOption = selectedSortOption, toplist = it)
            }
        }

        nftRepository.cachedPlatforms?.let {
            if (it.isNotEmpty()) {
                nftPlatforms = sortPlatforms(platforms = it)
            }
        }

        currencyRepository.bitcoinData?.let {
            bitcoin = it
            multiplier = calculatePriceMultiplier(it)
        }

        globalRepository.coinsList?.let {
            coinsList = it
            buildChainSymbolLookup()
        }
    }

    fun setNewSortOption(newSortOption: SortOption) {
        selectedSortOption = newSortOption
        StorageManager.set(StorageKeys.NFTS_SORT_OPTION, newSortOption)
    }

    fun setNewPlatform(newPlatform: NFTPlatform) {
        selectedPlatform = newPlatform
        StorageManager.set(StorageKeys.NFT_PLATFORM, newPlatform)
    }

    suspend fun getNftToplist(
        newPlatform: NFTPlatform,
        newSortOption: SortOption
    ) {
        if (newPlatform != selectedPlatform || newSortOption != selectedSortOption) {
            nftToplist = listOf()
        }

        if (newSortOption != selectedSortOption) {
            setNewSortOption(newSortOption)
        }

        if (newPlatform != selectedPlatform) {
            setNewPlatform(newPlatform)
        }

        nftRepository.getToplist(
            platform = newPlatform,
            sortOption = newSortOption
        )?.let {
            nftToplist = sortToplist(sortOption = newSortOption, toplist = it)
        }
    }

    suspend fun getNFTPlatforms() {
        nftRepository.getNFTPlatforms()?.let {
            nftPlatforms = sortPlatforms(platforms = it)
        }
    }

    suspend fun getBitcoin() {
        currencyRepository.getBitcoin()?.let {
            bitcoin = it
            multiplier = calculatePriceMultiplier(it)
        }
    }

    suspend fun getAllCoins() {
        globalRepository.getAllCoins()?.let {
            coinsList = it
            buildChainSymbolLookup()
        }
    }

    fun getChainSymbol(nativeCurrency: String?): String {
        val currency = nativeCurrency?.lowercase() ?: return ""
        return chainSymbolLookup[currency] ?: ""
    }

    private fun sortToplist(sortOption: SortOption, toplist: List<NFTToplist>): List<NFTToplist> {
        return when (sortOption) {
            SortOption.FloorPriceDesc -> {
                toplist.sortedByDescending { it.floor_price?.usd ?: 0.0 }
            }
            SortOption.FloorPriceAsc -> {
                toplist.sortedBy { it.floor_price?.usd ?: 0.0 }
            }
            SortOption.FloorPriceChangeDesc -> {
                toplist.sortedByDescending { it.floor_price_in_usd_24h_percentage_change ?: 0.0 }
            }
            SortOption.FloorPriceChangeAsc -> {
                toplist.sortedBy { it.floor_price_in_usd_24h_percentage_change ?: 0.0 }
            }
            else -> {
                toplist
            }
        }
    }

    private fun sortPlatforms(platforms: List<NFTPlatform>): List<NFTPlatform> {
        val allChains = NFTPlatform(id = "all", name = "All Chains", image = null)
        return listOf(allChains) + platforms.sortedBy { it.name ?: "" }
    }

    private fun buildChainSymbolLookup() {
        val lookup = mutableMapOf<String, String>()
        for (coin in coinsList) {
            val symbol = coin.symbol?.uppercase() ?: continue
            coin.id?.lowercase()?.let { lookup[it] = symbol }
            coin.name?.lowercase()?.let { lookup[it] = symbol }
        }
        chainSymbolLookup = lookup
    }

    private fun calculatePriceMultiplier(bitcoin: CryptoDetailInfoModel): Double? {
        val selectedCurrency = UserPreferences.getSelectedCurrency()
        val targetPrice = bitcoin.market_data?.current_price?.getSelectedCurrency(currency = selectedCurrency.code.uppercase()) ?: 0.0
        val basePrice = bitcoin.market_data?.current_price?.getSelectedCurrency(currency = "USD") ?: 0.0

        return if (basePrice > 0) targetPrice / basePrice else null
    }
}
