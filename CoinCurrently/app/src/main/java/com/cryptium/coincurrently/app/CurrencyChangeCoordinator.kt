package com.cryptium.coincurrently.app
import com.cryptium.coincurrently.sharedui.models.PriceChange
import com.cryptium.coincurrently.sharedui.models.SortOption
import com.cryptium.coincurrently.features.GlobalMarket.GlobalMarketRepository
import com.cryptium.coincurrently.features.Home.NewCoins.NewCoinsRepository
import com.cryptium.coincurrently.features.Home.Trending.TrendingRepository
import com.cryptium.coincurrently.features.Home.WinnersLosers.WinnersLosersRepository
import com.cryptium.coincurrently.core.persistence.StorageKeys
import com.cryptium.coincurrently.core.persistence.StorageManager
import com.cryptium.coincurrently.features.Market.Repositories.CategoriesRepository
import com.cryptium.coincurrently.features.Market.Repositories.DerivativesRepository
import com.cryptium.coincurrently.features.Market.Repositories.ExchangesRepository
import com.cryptium.coincurrently.features.Market.Repositories.NFTRepository
import com.cryptium.coincurrently.features.Portfolio.PortfolioRepository
import com.cryptium.coincurrently.features.Market.Repositories.ToplistRepository
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import com.cryptium.coincurrently.core.networking.ResponseModels.NFTPlatform

/** Refetches API-backed caches after the user changes display currency. */
suspend fun refreshCachedSectionsAfterCurrencyChange() {
    StorageManager.clearCache()

    val sortOption =
        StorageManager.get<SortOption>(StorageKeys.TOPLIST_SORT_OPTION) ?: SortOption.MktCapDesc
    val priceChange =
        StorageManager.get<PriceChange>(StorageKeys.TOPLIST_PRICE_CHANGE_OPTION)
            ?: PriceChange.SevenDays

    coroutineScope {
        launch { ExchangesRepository.getExchanges() }
        launch { GlobalMarketRepository.getGlobalMarket() }
        launch { refreshHomeCachesWithoutSortSettings() }
        launch { refreshHomeCachesWithSortSettings(priceChange) }
        launch { refreshPortfolioCachesIfNeeded() }
        launch { refreshMarketToplistIfNeeded(sortOption, priceChange) }
        launch {
            CategoriesRepository.getCategories(sortOption = SortOption.MktCapDesc)
        }
        launch {
            DerivativesRepository.getDerivatives(
                sortOption = StorageManager.get<SortOption>(StorageKeys.DERIVATIVES_SORT_OPTION)
                    ?: SortOption.openInterestDesc,
            )
        }
        launch {
            if (!TrendingRepository.cachedTrendingCoins.isNullOrEmpty()) {
                TrendingRepository.getTrendingCoins()
            }
        }
        launch {
            refreshNftCachesIfNeeded()
        }
    }
}

private suspend fun refreshNftCachesIfNeeded() {
    refreshIfCachedFlag(NFTRepository.cachedToplist.isNotEmpty()) {
        val sortOption =
            StorageManager.get<SortOption>(StorageKeys.NFTS_SORT_OPTION) ?: SortOption.MktCapFiatDesc
        val platform = StorageManager.get<NFTPlatform>(StorageKeys.NFT_PLATFORM)
            ?: NFTPlatform(id = "all", name = "All Chains", image = null)
        NFTRepository.getToplist(platform = platform, sortOption = sortOption)
    }
}

private suspend fun <T> refreshIfCached(rows: Collection<T>?, refresh: suspend () -> Unit) {
    if (rows.isNullOrEmpty()) return
    refresh()
}

private suspend fun refreshIfCachedFlag(hasCachedRows: Boolean, refresh: suspend () -> Unit) {
    if (!hasCachedRows) return
    refresh()
}

private suspend fun refreshHomeCachesWithoutSortSettings() {
    ToplistRepository.getSmallToplist()
    if (!ToplistRepository.favorites.isNullOrEmpty()) {
        ToplistRepository.getFavorites()
    }
}

private suspend fun refreshHomeCachesWithSortSettings(priceChange: PriceChange) {
    refreshIfCachedFlag(WinnersLosersRepository.hasCachedWinnerCoinRows(priceChange)) {
        WinnersLosersRepository.getWinners(priceChange)
    }
    refreshIfCachedFlag(WinnersLosersRepository.hasCachedLoserCoinRows(priceChange)) {
        WinnersLosersRepository.getLosers(priceChange)
    }
    refreshIfCached(NewCoinsRepository.newCoins) {
        NewCoinsRepository.getNewCoinIds()
    }
}

private suspend fun refreshPortfolioCachesIfNeeded() {
    refreshIfCached(PortfolioRepository.portfolio?.data?.data) {
        PortfolioRepository.getPortfolio()
        PortfolioRepository.refreshWallets()
        PortfolioRepository.getPortfolioAllCurrencies()
    }
}

private suspend fun refreshMarketToplistIfNeeded(sortOption: SortOption, priceChange: PriceChange) {
    val cacheKey = "${sortOption.getOptionKey(priceChange)}-null"
    refreshIfCached(ToplistRepository.toplist[cacheKey]?.data) {
        ToplistRepository.getToplist(
            sortOption = sortOption,
            priceChange = priceChange,
            platformId = null,
        )
    }
}
