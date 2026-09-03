package com.cryptium.coincurrently.features.Treasuries.ViewModels
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.cryptium.coincurrently.core.extensions.getSelectedCurrency
import com.cryptium.coincurrently.core.networking.TreasuryEntity
import com.cryptium.coincurrently.features.Treasuries.TreasuryEntityFilter
import com.cryptium.coincurrently.sharedui.components.GlobalFunctions
import com.cryptium.coincurrently.core.persistence.StorageKeys
import com.cryptium.coincurrently.core.persistence.StorageManager
import com.cryptium.coincurrently.core.persistence.CurrencyRepository
import com.cryptium.coincurrently.features.Treasuries.PublicTreasuryRepository
import com.cryptium.coincurrently.features.Market.Repositories.ToplistRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import com.cryptium.coincurrently.core.networking.ResponseModels.CryptoDetailInfoModel
import com.cryptium.coincurrently.core.networking.ResponseModels.PublicTreasuryHolder
import com.cryptium.coincurrently.core.networking.ResponseModels.PublicTreasuryModel
import com.cryptium.coincurrently.core.networking.ResponseModels.Toplist
import com.cryptium.coincurrently.core.networking.ResponseModels.TreasuryListItem

class TreasuriesViewModel : ViewModel() {

    private val publicTreasuryRepository by lazy { PublicTreasuryRepository }
    private val toplistRepository by lazy { ToplistRepository }
    private val currencyRepository by lazy { CurrencyRepository }

    var treasury: PublicTreasuryModel? by mutableStateOf(null)
    var filteredTotalHoldings: Double? by mutableStateOf(null)
    var filteredMarketCapDominance: Double? by mutableStateOf(null)
    var holders: List<TreasuryListItem> by mutableStateOf(emptyList())
    var availableCoins: List<Toplist> by mutableStateOf(emptyList())
    var selectedCoin: Toplist? by mutableStateOf(null)
    var bitcoin: CryptoDetailInfoModel? by mutableStateOf(null)
    var multiplier: Double? by mutableStateOf(null)

    var selectedEntityFilter: TreasuryEntityFilter by mutableStateOf(TreasuryEntityFilter.All)
    var selectedCoinId: String by mutableStateOf("bitcoin")

    private var allHolders = listOf<TreasuryListItem>()
    private var companiesTreasury: PublicTreasuryModel? = null
    private var governmentsTreasury: PublicTreasuryModel? = null

    suspend fun setStartData() {
        selectedEntityFilter = StorageManager.get<TreasuryEntityFilter>(StorageKeys.TREASURIES_ENTITY_FILTER)
            ?: TreasuryEntityFilter.All

        StorageManager.get<String>(StorageKeys.TREASURIES_COIN_ID)?.let {
            selectedCoinId = it
        }

        currencyRepository.bitcoinData?.let { bitcoinData ->
            bitcoin = bitcoinData
            multiplier = calculatePriceMultiplier(bitcoinData)
        }

        loadAvailableCoins()
        loadCachedTreasuries()
    }

    private suspend fun loadAvailableCoins() {
        val smallToplist = toplistRepository.getSmallToplist() ?: return

        availableCoins = smallToplist.filter { !it.id.isNullOrEmpty() }

        selectedCoin = availableCoins.firstOrNull { it.id == selectedCoinId }
            ?: availableCoins.firstOrNull()

        selectedCoin?.id?.let { selectedCoinId = it }
    }

    suspend fun getBitcoin() {
        currencyRepository.getBitcoin()?.let { bitcoinData ->
            bitcoin = bitcoinData
            multiplier = calculatePriceMultiplier(bitcoinData)
        }
    }

    suspend fun getTreasuries() {
        coroutineScope {
            val companiesDeferred = async {
                publicTreasuryRepository.getPublicTreasury(
                    entity = TreasuryEntity.Companies,
                    coinId = selectedCoinId,
                )
            }
            val governmentsDeferred = async {
                publicTreasuryRepository.getPublicTreasury(
                    entity = TreasuryEntity.Governments,
                    coinId = selectedCoinId,
                )
            }

            applyMergedTreasury(
                companies = companiesDeferred.await(),
                governments = governmentsDeferred.await(),
            )
        }
    }

    fun setEntityFilter(filter: TreasuryEntityFilter) {
        selectedEntityFilter = filter
        StorageManager.set(StorageKeys.TREASURIES_ENTITY_FILTER, filter)
        applyEntityFilter()
    }

    suspend fun setSelectedCoin(coin: Toplist) {
        val id = coin.id ?: return
        if (id == selectedCoinId) return

        clearTreasuryData()

        selectedCoin = coin
        selectedCoinId = id
        StorageManager.set(StorageKeys.TREASURIES_COIN_ID, id)
        getTreasuries()
    }

    private fun clearTreasuryData() {
        treasury = null
        companiesTreasury = null
        governmentsTreasury = null
        filteredTotalHoldings = null
        filteredMarketCapDominance = null
        holders = emptyList()
        allHolders = emptyList()
    }

    private suspend fun loadCachedTreasuries() {
        publicTreasuryRepository.ensureCacheLoaded(TreasuryEntity.Companies, selectedCoinId)
        publicTreasuryRepository.ensureCacheLoaded(TreasuryEntity.Governments, selectedCoinId)

        applyMergedTreasury(
            companies = publicTreasuryRepository.getCachedTreasury(TreasuryEntity.Companies, selectedCoinId),
            governments = publicTreasuryRepository.getCachedTreasury(TreasuryEntity.Governments, selectedCoinId),
        )
    }

    private fun applyMergedTreasury(
        companies: PublicTreasuryModel?,
        governments: PublicTreasuryModel?,
    ) {
        companiesTreasury = companies
        governmentsTreasury = governments

        val companyHolders = companies?.companies.orEmpty()
        val governmentHolders = governments?.governments.orEmpty()

        allHolders = (companyHolders.map { TreasuryListItem(it, TreasuryEntity.Companies, selectedCoinId) }
            + governmentHolders.map { TreasuryListItem(it, TreasuryEntity.Governments, selectedCoinId) })
            .sortedByDescending { it.holder.total_current_value_usd ?: 0.0 }

        if (companies == null && governments == null) {
            treasury = null
            applyEntityFilter()
            return
        }

        treasury = PublicTreasuryModel(
            total_holdings = (companies?.total_holdings ?: 0.0) + (governments?.total_holdings ?: 0.0),
            total_value_usd = (companies?.total_value_usd ?: 0.0) + (governments?.total_value_usd ?: 0.0),
            market_cap_dominance = (companies?.market_cap_dominance ?: 0.0) +
                (governments?.market_cap_dominance ?: 0.0),
            companies = companyHolders.ifEmpty { null },
            governments = governmentHolders.ifEmpty { null },
        )

        applyEntityFilter()
    }

    private fun applyEntityFilter() {
        val hasLoadedData = companiesTreasury != null || governmentsTreasury != null

        when (selectedEntityFilter) {
            TreasuryEntityFilter.All -> {
                holders = allHolders
                if (!hasLoadedData) {
                    filteredTotalHoldings = null
                    filteredMarketCapDominance = null
                    return
                }
                filteredTotalHoldings = (companiesTreasury?.total_holdings ?: 0.0) +
                    (governmentsTreasury?.total_holdings ?: 0.0)
                filteredMarketCapDominance = (companiesTreasury?.market_cap_dominance ?: 0.0) +
                    (governmentsTreasury?.market_cap_dominance ?: 0.0)
            }

            TreasuryEntityFilter.Companies -> {
                holders = allHolders.filter { it.entity == TreasuryEntity.Companies }
                if (!hasLoadedData) {
                    filteredTotalHoldings = null
                    filteredMarketCapDominance = null
                    return
                }
                filteredTotalHoldings = companiesTreasury?.total_holdings ?: 0.0
                filteredMarketCapDominance = companiesTreasury?.market_cap_dominance ?: 0.0
            }

            TreasuryEntityFilter.Governments -> {
                holders = allHolders.filter { it.entity == TreasuryEntity.Governments }
                if (!hasLoadedData) {
                    filteredTotalHoldings = null
                    filteredMarketCapDominance = null
                    return
                }
                filteredTotalHoldings = governmentsTreasury?.total_holdings ?: 0.0
                filteredMarketCapDominance = governmentsTreasury?.market_cap_dominance ?: 0.0
            }
        }
    }

    private fun calculatePriceMultiplier(bitcoin: CryptoDetailInfoModel): Double? {
        val selectedCurrency = GlobalFunctions.getSelectedCurrency()
        val targetPrice = bitcoin.market_data?.current_price?.getSelectedCurrency(
            currency = selectedCurrency.code.uppercase(),
        ) ?: 0.0
        val basePrice = bitcoin.market_data?.current_price?.getSelectedCurrency(currency = "USD") ?: 0.0

        return if (basePrice > 0) targetPrice / basePrice else null
    }

    suspend fun resolveHolderWithEntityId(
        holder: PublicTreasuryHolder,
        entity: com.cryptium.coincurrently.core.networking.TreasuryEntity,
    ): PublicTreasuryHolder {
        return publicTreasuryRepository.holderWithEntityId(holder, entity)
    }
}
