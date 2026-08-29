package com.cryptium.coincurrently.ui.main.Repositories

import AddressImport
import Chain
import android.content.Context
import android.util.Log
import com.cryptium.coincurrently.ui.main.CustomViews.CustomImageView
import com.cryptium.coincurrently.ui.main.CryptoDetail.Compose.Portfolio.Models.NewSwapTransaction
import com.cryptium.coincurrently.ui.main.Extensions.allIds
import com.cryptium.coincurrently.ui.main.Extensions.unwrapOrNull
import com.cryptium.coincurrently.ui.main.Extensions.allWallets
import com.cryptium.coincurrently.ui.main.Extensions.defaultWalletName
import com.cryptium.coincurrently.ui.main.Extensions.isMoreThanDefaultCache
import com.cryptium.coincurrently.ui.main.Extensions.allTransactionsCount
import com.cryptium.coincurrently.ui.main.Extensions.tenYearsAgo
import com.cryptium.coincurrently.ui.main.SettingsTab.Achievements.Managers.AchievementManager
import com.cryptium.coincurrently.ui.main.General.GlobalFunctions
import com.cryptium.coincurrently.ui.main.HomeTab.GlobalMarket.DataClasses.allCurrencies
import networking.ApiHelper
import networking.MainRepository
import networking.ResponseModels.*
import networking.RetrofitBuilder
import java.util.Date
import com.cryptium.coincurrently.ui.main.Managers.StorageKeys
import com.cryptium.coincurrently.ui.main.Managers.StorageManager
import com.cryptium.coincurrently.ui.main.PortfolioTab.DataClasses.NewTransaction
import com.cryptium.coincurrently.ui.main.Models.CachedData
import com.cryptium.coincurrently.ui.main.PortfolioTab.DataClasses.PortfolioEvent
import com.cryptium.coincurrently.ui.main.PortfolioTab.DataClasses.PortfolioModel
import com.cryptium.coincurrently.ui.main.PortfolioTab.DataClasses.sanitized
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import java.util.UUID

data class PortfolioPiesData(
    @SerializedName("data")
    var data: List<Pie>,
    @SerializedName("otherCoins")
    var otherCoins: List<Toplist>,
    // Nullable: Gson ignores Kotlin defaults and can leave this null for older / partial cache JSON.
    @SerializedName("coinColors")
    var coinColors: MutableMap<String, String>? = mutableMapOf(),
) {
    fun coinColorsMutable(): MutableMap<String, String> {
        val colors = coinColors ?: mutableMapOf()
        coinColors = colors
        return colors
    }

    /** Repair null lists left by Gson before any iteration. */
    fun sanitizeGsonNulls(): PortfolioPiesData {
        @Suppress("UNCHECKED_CAST")
        data = ((data as List<Pie>?) ?: emptyList()).onEach { it.sanitizeGsonNullLists() }
        @Suppress("UNCHECKED_CAST")
        otherCoins = (otherCoins as List<Toplist>?) ?: emptyList()
        coinColorsMutable()
        return this
    }
}

class PortfolioRepository {
    companion object {
        private val mainRepository by lazy { MainRepository(ApiHelper(RetrofitBuilder.apiService)) }

        var portfolioModels: List<PortfolioModel>? = run {
            val loaded = StorageManager.get<List<PortfolioModel>>(StorageKeys.NEW_PORTFOLIO)
            if (StorageManager.clearIfCorruptTypedList<PortfolioModel>(StorageKeys.NEW_PORTFOLIO, loaded, "PortfolioRepo")) {
                null
            } else {
                loaded?.map { it.sanitized() }
            }
        }
        var portfolio: CachedData<PortfolioPiesData>? = loadPortfolioCache()
        var portfolioAllCurrencies: MutableMap<String, Map<String, Double>>? = StorageManager.get<CachedData<MutableMap<String, Map<String, Double>>>>(StorageKeys.PORTFOLIO_PRICES)?.data

        init {
            migratePortfolio()

            if (portfolioModels == null) {
                portfolioModels = listOf()
            }

            if (portfolio == null) {
                portfolio = CachedData(
                    PortfolioPiesData(listOf(), listOf()),
                    Date().tenYearsAgo()
                )
            }

            if (portfolioAllCurrencies == null) {
                portfolioAllCurrencies = mutableMapOf()
            }
        }

        /**
         * R8 can strip List<Pie> signatures from [PortfolioPiesData], so Gson may deserialize
         * pies as LinkedTreeMap. Drop that cache so the next fetch rebuilds typed [Pie]s.
         */
        private fun loadPortfolioCache(): CachedData<PortfolioPiesData>? {
            val cached = StorageManager.get<CachedData<PortfolioPiesData>>(StorageKeys.PORTFOLIO) ?: return null
            // Gson can leave CachedData.data null despite the non-null Kotlin type.
            @Suppress("UNCHECKED_CAST")
            val piesData = cached.data as PortfolioPiesData? ?: run {
                Log.e("PortfolioRepo", "PORTFOLIO cache has null data; clearing")
                StorageManager.removeKey(StorageKeys.PORTFOLIO)
                return null
            }
            val first = (piesData.data as List<*>?)?.firstOrNull()
            if (first != null && first !is Pie) {
                Log.e(
                    "PortfolioRepo",
                    "Corrupt PORTFOLIO cache element=${first.javaClass.name}; clearing"
                )
                StorageManager.removeKey(StorageKeys.PORTFOLIO)
                return null
            }
            piesData.sanitizeGsonNulls()
            return cached
        }

        /**
         * One-shot prefs → StorageManager migration. Must not throw from clinit:
         * Gson/prefs can yield null lists despite non-null Kotlin types.
         */
        fun migratePortfolio() {
            try {
                val saved = StorageManager.get<List<PortfolioModel>>(StorageKeys.NEW_PORTFOLIO).orEmpty()
                val old = GlobalFunctions.getPortfolioCurrencies()

                if (old.isEmpty() || saved.isNotEmpty()) {
                    return
                }

                portfolioModels = old.map { it.migrateToNewPortfolioModel().sanitized() }
                StorageManager.set(StorageKeys.NEW_PORTFOLIO, portfolioModels.orEmpty())
                GlobalFunctions.setPortfolioCurrency(emptyList())
            } catch (e: Exception) {
                Log.e("PortfolioRepo", "migratePortfolio failed", e)
            }
        }

        suspend fun findPortfolioEventByTransactionId(transactionId: String): Pair<PortfolioEvent?, Toplist?> {
            return withContext(Dispatchers.Default) {
                val uuid = try {
                    UUID.fromString(transactionId)
                } catch (_: IllegalArgumentException) {
                    return@withContext Pair(null, null)
                }

                val pies = portfolio?.data?.data ?: return@withContext Pair(null, null)

                for (pie in pies) {
                    pie.sanitizeGsonNullLists()
                    pie.transactions.firstOrNull { it.id == uuid }?.let {
                        return@withContext Pair(it, pie.data)
                    }
                    pie.swaps.firstOrNull { it.id == uuid }?.let {
                        return@withContext Pair(it, pie.data)
                    }
                    pie.imports.firstOrNull { it.id == uuid }?.let {
                        return@withContext Pair(it, pie.data)
                    }
                }

                Pair(null, null)
            }
        }

        suspend fun getPortfolio(): CachedData<PortfolioPiesData>? {
            StorageManager.get<CachedData<PortfolioPiesData>>(StorageKeys.PORTFOLIO)?.let { cached ->
                @Suppress("UNCHECKED_CAST")
                val piesData = cached.data as PortfolioPiesData? ?: return@let
                val first = (piesData.data as List<*>?)?.firstOrNull()
                if (first != null && first !is Pie) {
                    Log.e(
                        "PortfolioRepo",
                        "Corrupt PORTFOLIO cache on read element=${first.javaClass.name}; clearing"
                    )
                    StorageManager.removeKey(StorageKeys.PORTFOLIO)
                } else if ((piesData.data as List<*>?)?.isNotEmpty() == true &&
                    !cached.date.isMoreThanDefaultCache()
                ) {
                    piesData.sanitizeGsonNulls()
                    portfolio = cached
                    return cached
                }
            }

            val portfolioIds = portfolioModels?.allIds?.joinToString { it }?.replace(" ", "")?.removeSuffix(",") ?: ""
            val currencyCode = GlobalFunctions.getSelectedCurrency().code.lowercase()

            if (portfolioIds.isEmpty()) {
                return null
            }

            Log.w("Network call ", "Portfolio")

            val portfolioResult = mainRepository.getMultipleCurrencies(portfolioIds, currencyCode).unwrapOrNull()

            portfolioResult?.let {
                val portfolioArray = arrayListOf<Pie>()
                val otherCoins = arrayListOf<Toplist>()
                val existingPies = portfolio?.data?.data ?: emptyList()
                val preservedCoinColors = portfolio?.data?.coinColorsMutable() ?: mutableMapOf()

                it.forEach { coin ->
                    val preservedColor = preservedCoinColor(
                        coinId = coin.id,
                        existingPies = existingPies,
                        coinColors = preservedCoinColors,
                    )

                    portfolioModels?.firstOrNull { it.id == coin.id }?.let { portfolioModel ->
                        val model = portfolioModel.sanitized()
                        portfolioArray.add(
                            Pie(
                                data = coin,
                                color = preservedColor,
                                transactions = model.transactions,
                                swaps = model.swaps,
                                imports = model.imports
                            )
                        )
                    } ?: run {
                        otherCoins.add(coin)
                        val coinId = coin.id
                        if (coinId != null && preservedColor != null) {
                            preservedCoinColors[coinId] = preservedColor
                        }
                    }
                }

                val refreshedPortfolio = CachedData(
                    PortfolioPiesData(portfolioArray, otherCoins, preservedCoinColors),
                    Date(),
                )
                StorageManager.set(StorageKeys.PORTFOLIO, refreshedPortfolio)
                portfolio = refreshedPortfolio
            }

            return this.portfolio
        }

        suspend fun getPortfolioAllCurrencies(): MutableMap<String, Map<String, Double>>? {
            StorageManager.get<CachedData<MutableMap<String, Map<String, Double>>>>(StorageKeys.PORTFOLIO_PRICES)?.let {
                if (it.data.isNotEmpty() && !it.date.isMoreThanDefaultCache()) {
                    return it.data
                }
            }

            val portfolioIds = portfolioModels?.allIds?.joinToString { it }?.replace(" ", "") ?: ""

            if (portfolioIds.isEmpty()) {
                return mutableMapOf()
            }

            Log.w("Network call ", "All coins")

            val currencies = mainRepository.getCoinsWithAllCurrencies(portfolioIds).unwrapOrNull()

            currencies?.let {
                updatePriceCache(currencies)
            }

            return currencies
        }

        suspend fun refreshWallets(): CachedData<PortfolioPiesData>? {
            val walletRefreshDate: Date? = StorageManager.get<Date>(StorageKeys.WALLETS_REFRESH_DATE)
            if (walletRefreshDate != null && !walletRefreshDate.isMoreThanDefaultCache()) {
                return null
            }

            coroutineScope {
                val jobs = mutableListOf<Job>()

                val wallets = portfolio?.data?.data?.allWallets ?: emptyList()

                for (wallet in wallets) {
                    jobs += launch {
                        importWallet(
                            walletName = wallet.walletName,
                            coinData = null,
                            address = wallet.address,
                            chain = wallet.blockChain,
                            isRefreshing = true
                        )
                    }
                }

                jobs.joinAll()  // Wait for all tasks to finish
            }

            StorageManager.set(StorageKeys.WALLETS_REFRESH_DATE, Date())
            return portfolio
        }

        fun addNewCoin(
            coin: Toplist,
            event: PortfolioEvent,
            allCurrencies: Map<String, Double>?
        ) {
            val id = coin.id ?: return

            when (val event = event) {
                is NewTransaction -> {
                    val newPie = Pie(data = coin, transactions = mutableListOf(event))
                    appendPie(newPie)
                }
                is NewSwapTransaction -> {
                    val newPie = Pie(data = coin, swaps = mutableListOf(event))
                    appendPie(newPie)
                }
                is AddressImport -> {
                    val newPie = Pie(data = coin, imports = mutableListOf(event))
                    appendPie(newPie)
                }
            }

            allCurrencies?.let {
                upsertPrice(id, it)
            } ?: run {
                StorageManager.set(
                    StorageKeys.PORTFOLIO_PRICES,
                    CachedData(portfolioAllCurrencies ?: mutableMapOf(), Date().tenYearsAgo())
                )
            }
        }

        fun addNewTransaction(id: String, event: PortfolioEvent) {
            val currentPortfolio = portfolio ?: return
            val index = currentPortfolio.data.data.indexOfFirst { it.data?.id == id }
            if (index == -1) {
                return
            }

            val currentPie = currentPortfolio.data.data[index]

            when (val event = event) {
                is NewTransaction -> {
                    currentPie.transactions.add(event)
                }
                is NewSwapTransaction -> {
                    currentPie.swaps.add(event)
                }
                is AddressImport -> {
                    currentPie.imports.add(event)
                }
            }

            val updatedData = currentPortfolio.data.data.toMutableList().apply {
                this[index] = currentPie
            }

            val updatedPortfolio = currentPortfolio.copy(data = currentPortfolio.data.copy(data = updatedData))

            updatePortfolioPersistence(updatedPortfolio)
        }

        fun deletePortfolioItem(id: String) {
            val currentPortfolio = portfolio ?: return

            val index = currentPortfolio.data.data.indexOfFirst { it.data?.id == id }
            if (index == -1) return

            val updatedData = currentPortfolio.data.data.toMutableList().apply {
                removeAt(index)
            }

            val updatedPortfolio = currentPortfolio.copy(data = currentPortfolio.data.copy(data = updatedData))

            updatePortfolioPersistence(updatedPortfolio)
        }

        fun deleteTransaction(id: String, uuid: UUID?): CachedData<PortfolioPiesData>? {
            val currentPortfolio = portfolio ?: return portfolio
            val index = currentPortfolio.data.data.indexOfFirst { it.data?.id == id }
            if (index == -1) {
                return portfolio
            }

            val currentPie = currentPortfolio.data.data[index]

            currentPie.transactions.removeAll { it.id == uuid }
            currentPie.swaps.removeAll { it.id == uuid }
            currentPie.imports.removeAll { it.id == uuid }

            if (currentPie.transactions.isEmpty() && currentPie.swaps.isEmpty() && currentPie.imports.isEmpty()) {
                currentPortfolio.data.data = currentPortfolio.data.data.filterIndexed { i, _ -> i != index }
            }

            updatePortfolioPersistence(currentPortfolio)
            return currentPortfolio
        }

        private fun updatePortfolioCache(portfolio: CachedData<PortfolioPiesData>) {
            this.portfolio = CachedData(
                portfolio.data.copy(),
                Date()
            )
            StorageManager.set(StorageKeys.PORTFOLIO, this.portfolio)
        }

        private fun updatePriceCache(prices: MutableMap<String, Map<String, Double>>) {
            val previousMaxGain = AchievementManager.currentMaxPortfolioGainPercent()
            portfolioAllCurrencies = prices
            StorageManager.set(StorageKeys.PORTFOLIO_PRICES, CachedData(prices, Date()))
            AchievementManager.checkMoonShotUnlock(previousMaxGain)
        }

        private fun upsertPrice(
            id: String,
            currencies: Map<String, Double>
        ) {
            val previousMaxGain = AchievementManager.currentMaxPortfolioGainPercent()
            val prices = portfolioAllCurrencies?.toMutableMap() ?: mutableMapOf()

            val existing = prices[id]?.toMutableMap() ?: mutableMapOf()

            for ((currency, value) in currencies) {
                existing[currency] = value
            }

            prices[id] = existing

            portfolioAllCurrencies = prices
            val date = StorageManager.get<CachedData<MutableMap<String, Map<String, Double>>>>(StorageKeys.PORTFOLIO_PRICES)?.date
            StorageManager.set(StorageKeys.PORTFOLIO_PRICES, CachedData(prices, date ?: Date().tenYearsAgo()))
            AchievementManager.checkMoonShotUnlock(previousMaxGain)
        }

        private fun appendPie(pie: Pie) {
            val current = portfolio ?: CachedData(PortfolioPiesData(listOf(), listOf()), Date())

            val updatedData = current.data.data.toMutableList().apply {
                add(pie)
            }

            val updatedPortfolio = CachedData(
                PortfolioPiesData(
                    updatedData,
                    current.data.otherCoins,
                    current.data.coinColorsMutable(),
                ),
                Date().tenYearsAgo()
            )

            this.portfolio = updatedPortfolio

            StorageManager.set(StorageKeys.PORTFOLIO, updatedPortfolio)
            updatePortfolioPersistence(updatedPortfolio)
        }

        private fun updatePortfolioPersistence(portfolio: CachedData<PortfolioPiesData>) {
            updatePortfolioCache(portfolio)
            portfolioModels = Companion.portfolio?.data?.data?.mapNotNull { it.toPortfolioCurrencyModel() }
            StorageManager.set(StorageKeys.NEW_PORTFOLIO, portfolioModels)
        }

        suspend fun importWallet(
            walletName: String?,
            coinData: CryptoDetailInfoModel?,
            address: String,
            chain: Chain,
            isRefreshing: Boolean = false
        ): Pair<CachedData<PortfolioPiesData>?, MutableMap<String, Map<String, Double>>?> {
            val response = mainRepository.getWalletAddress(
                address = address,
                blockChain = chain.id.lowercase()
            ).unwrapOrNull() ?: return Pair(null, null)

            val cached = portfolio
            val existingImport = cached?.data?.data?.firstNotNullOfOrNull { it.imports.firstOrNull { it.address == address } }
            val walletName = walletName ?: portfolioModels?.defaultWalletName

            val wallet = AddressImport(
                id = UUID.randomUUID(),
                date = existingImport?.date ?: Date(),
                address = response.address,
                balance = response.balance,
                tokenInfos = response.tokenInfos,
                walletName = walletName,
                coinId = chain.coinId,
                blockChain = chain
            )

            val chainId = chain.coinId.lowercase()
            val coin = cached?.data?.data?.firstOrNull { it.data?.id?.lowercase() == chainId }
            val coinId = coin?.data?.id

            when {
                coinId != null && existingImport != null -> {
                    addNewTransaction(coinId, wallet.copy(date = wallet.date))
                    deleteTransaction(coinId, existingImport.id)
                }

                coinId != null -> {
                    addNewTransaction(coinId, wallet)
                }

                coinData != null -> {
                    addNewCoin(
                        coin = coinData.toToplist(),
                        event = wallet,
                        allCurrencies = coinData.market_data?.current_price?.allCurrencies
                    )
                }
            }

            wallet.tokenInfos?.let { tokenInfos ->
                val cached = portfolio ?: return@let

                // Build the set of existing IDs
                val existingIds = buildSet {
                    addAll(cached.data.data.mapNotNull { it.data?.id })
                    addAll(cached.data.otherCoins.map { it.id })
                }

                val coinIds = tokenInfos
                    .mapNotNull { it.tokenId }
                    .filter { id ->
                        if (isRefreshing) {
                            !existingIds.contains(id)
                        } else {
                            true
                        }
                    }

                if (coinIds.isNotEmpty()) {
                    val selectedCurrency = GlobalFunctions.getSelectedCurrency().code.lowercase()

                    val coins: List<Toplist>? = mainRepository.getMultipleCurrencies(
                        ids = coinIds.joinToString{ it }.replace(" ", ""),
                        currencyCode = selectedCurrency
                    ).unwrapOrNull()

                    coins?.let { coinList ->
                        var updatedCache = cached.data

                        coinList.forEach { coin ->
                            val existingIndex = updatedCache.otherCoins.indexOfFirst { it.id == coin.id }

                            updatedCache = updatedCache.copy(otherCoins =
                                if (existingIndex != -1) {
                                    updatedCache.otherCoins.toMutableList().apply {
                                        this[existingIndex] = coin
                                    }
                                } else if (updatedCache.data.none { it.data?.id == coin.id }) {
                                    updatedCache.otherCoins.toMutableList().apply {
                                        add(coin)
                                    }
                                } else {
                                    updatedCache.otherCoins
                                }
                            )

                            val coinId = coin.id ?: return@forEach
                            val price = coin.current_price ?: return@forEach

                            upsertPrice(
                                id = coinId,
                                currencies = mapOf(selectedCurrency to price)
                            )
                        }

                        updatePortfolioCache(CachedData(updatedCache, portfolio?.date ?: Date()))
                    }
                }
            }

            return Pair(portfolio, portfolioAllCurrencies)
        }

        fun renameWallet(
            id: String,
            address: String,
            newName: String
        ) {
            val portfolio = portfolio ?: return

            val walletIndex = portfolio.data.data.indexOfFirst { it.data?.id == id }
            if (walletIndex == -1) return

            val importIndex = portfolio.data.data[walletIndex]
                .imports
                .indexOfFirst { it.address == address }
            if (importIndex == -1) return

            portfolio.data.data[walletIndex]
                .imports[importIndex]
                .walletName = newName

            updatePortfolioPersistence(portfolio)
        }

        suspend fun getCoinColors(
            context: Context,
            onUpdate: ((List<Pie>) -> Unit)? = null,
        ) {
            val cachedPortfolio = portfolio ?: return
            if (cachedPortfolio.data.data.isEmpty() && cachedPortfolio.data.otherCoins.isEmpty()) {
                return
            }

            data class ColorTarget(
                val id: String,
                val imageURL: String,
                val isOtherCoin: Boolean,
            )

            val colorTargets = buildList {
                cachedPortfolio.data.data.forEach { pie ->
                    if (!needsCoinColor(pie.color)) return@forEach
                    val id = pie.data?.id ?: return@forEach
                    val imageURL = pie.data?.image ?: return@forEach
                    add(ColorTarget(id = id, imageURL = imageURL, isOtherCoin = false))
                }

                cachedPortfolio.data.otherCoins.forEach { coin ->
                    val id = coin.id ?: return@forEach
                    val imageURL = coin.image ?: return@forEach
                    val existingColor = cachedPortfolio.data.data
                        .firstOrNull { it.data?.id == id }
                        ?.color
                        ?: cachedPortfolio.data.coinColorsMutable()[id]

                    if (!needsCoinColor(existingColor)) return@forEach
                    add(ColorTarget(id = id, imageURL = imageURL, isOtherCoin = true))
                }
            }

            if (colorTargets.isEmpty()) {
                onUpdate?.invoke(cachedPortfolio.data.data)
                return
            }

            var updatedPortfolio = cachedPortfolio

            coroutineScope {
                val jobs = colorTargets.map { target ->
                    launch(Dispatchers.IO) {
                        val hexColor = fetchCoinColorHex(context, target.imageURL) ?: return@launch

                        withContext(Dispatchers.Main) {
                            val portfolioData = updatedPortfolio.data
                            val colors = portfolioData.coinColorsMutable()
                            if (target.isOtherCoin) {
                                colors[target.id] = hexColor
                            } else {
                                val index = portfolioData.data.indexOfFirst { it.data?.id == target.id }
                                if (index != -1) {
                                    portfolioData.data[index].color = hexColor
                                }
                            }

                            updatedPortfolio = CachedData(
                                portfolioData.copy(
                                    data = portfolioData.data.toList(),
                                    coinColors = colors.toMutableMap(),
                                ),
                                updatedPortfolio.date,
                            )
                            updatePortfolioCache(updatedPortfolio)
                            onUpdate?.invoke(updatedPortfolio.data.data)
                        }
                    }
                }
                jobs.joinAll()
            }

            onUpdate?.invoke(updatedPortfolio.data.data)
        }

        private fun preservedCoinColor(
            coinId: String?,
            existingPies: List<Pie>,
            coinColors: Map<String, String>,
        ): String? {
            if (coinId == null) return null

            val pieColor = existingPies.firstOrNull { it.data?.id == coinId }?.color
            if (!needsCoinColor(pieColor)) return pieColor

            val cachedColor = coinColors[coinId]
            if (!needsCoinColor(cachedColor)) return cachedColor

            return null
        }

        private suspend fun fetchCoinColorHex(context: Context, imageURL: String): String? {
            return suspendCancellableCoroutine { continuation ->
                val imageView = CustomImageView(context)
                imageView.setImageWithCompletion(imageURL, context) { color, _ ->
                    if (continuation.isActive) {
                        continuation.resume(color?.toHexColorString()) {}
                    }
                }
            }
        }

        private fun needsCoinColor(color: String?): Boolean {
            if (color == null) return true
            return color.uppercase() == "#FFFFFF"
        }

        private fun Int.toHexColorString(): String = String.format("#%06X", 0xFFFFFF and this)
    }
}
