package com.cryptium.coincurrently.features.Home
import com.cryptium.coincurrently.core.persistence.UserPreferences
import android.util.Log
import android.app.Application
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.cryptium.coincurrently.app.MainActivity
import com.cryptium.coincurrently.core.extensions.isTablet
import com.cryptium.coincurrently.sharedui.models.PriceChange
import com.cryptium.coincurrently.sharedui.models.SortOption
import com.cryptium.coincurrently.core.models.PreferredTimeframe
import com.cryptium.coincurrently.core.models.CarouselFilter
import com.cryptium.coincurrently.features.FearGreed.FearGreedRepository
import com.cryptium.coincurrently.core.networking.ResponseModels.GlobalMarketData
import com.cryptium.coincurrently.features.GasFees.GasFeesRepository
import com.cryptium.coincurrently.features.GlobalMarket.GlobalMarketRepository
import com.cryptium.coincurrently.features.Home.NewCoins.NewCoinsRepository
import com.cryptium.coincurrently.features.Home.Trending.TrendingRepository
import com.cryptium.coincurrently.features.Home.WinnersLosers.WinnersLosersRepository
import com.cryptium.coincurrently.features.Market.Toplist.Repositories.ToplistRepository
import com.cryptium.coincurrently.core.analytics.FavoriteAdded
import com.cryptium.coincurrently.core.analytics.FavoriteRemoved
import com.cryptium.coincurrently.core.analytics.AnalyticsManager
import com.cryptium.coincurrently.core.analytics.ScreenLabel
import kotlinx.coroutines.launch
import com.cryptium.coincurrently.core.networking.ResponseModels.*
import com.cryptium.coincurrently.core.persistence.StorageKeys
import com.cryptium.coincurrently.core.persistence.StorageManager

class HomeViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return HomeViewModelImp(application) as T
    }
}

interface HomeViewModel {
    var selectedFilter: CarouselFilter
    var fearGreedModel: FearGreed?
    var globalMarket: GlobalMarketData?
    var trendingCoins: List<Toplist>?
    var smallToplist: List<Toplist>?
    var favorites: List<Toplist>?
    var periodWinners: List<Toplist>?
    var periodLosers: List<Toplist>?
    var newCoins: List<Toplist>?
    var gasFees: GasFeesModel?

    fun getList(filter: CarouselFilter?): List<Toplist>?
    suspend fun downloadData()
    fun seeAllPressed()
    suspend fun getFearGreed()
    suspend fun getGlobalMarket()
    fun favouriteButtonPressed(item: Toplist, context: Context)
    suspend fun getNewCoins()
    suspend fun getGasFees()
    suspend fun refreshAllHomeData()
}

class HomeViewModelImp(private val application: Application) : ViewModel(), HomeViewModel {
    private val homeCarouselLimit: Int
        get() = if (application.isTablet()) 20 else 10

    private val fearGreedRepository by lazy { FearGreedRepository }
    private val globalMarketRepository by lazy { GlobalMarketRepository }
    private val trendingRepository by lazy { TrendingRepository }
    private val toplistRepository by lazy { ToplistRepository }
    private val newCoinsRepository by lazy { NewCoinsRepository }
    private val gasFeesRepository by lazy { GasFeesRepository }
    private val winnersLosersRepository by lazy { WinnersLosersRepository }

    override var selectedFilter: CarouselFilter by mutableStateOf(
        CarouselFilter.defaultHomeFilter(hasFavorites = UserPreferences.getFavorites().isNotEmpty())
    )

    override var fearGreedModel: FearGreed? by mutableStateOf(null)
    override var globalMarket: GlobalMarketData? by mutableStateOf(null)
    override var trendingCoins: List<Toplist>? by mutableStateOf(null)
    override var smallToplist: List<Toplist>? by mutableStateOf(null)
    override var favorites: List<Toplist>? by mutableStateOf(null)
    override var periodWinners: List<Toplist>? by mutableStateOf(null)
    override var periodLosers: List<Toplist>? by mutableStateOf(null)
    override var newCoins: List<Toplist>? by mutableStateOf(null)
    override var gasFees: GasFeesModel? by mutableStateOf(null)

    init {
        setStartData()
        fearGreedModel = fearGreedRepository.fearGreed
        gasFees = gasFeesRepository.cachedGasFees

        globalMarketRepository.globalMarket?.let {
            globalMarket = it
        }
    }

    fun setStartData() {
        trendingRepository.orderedCachedTrendingCoins()?.take(homeCarouselLimit)?.let {
            trendingCoins = it
        }

        toplistRepository.smallToplist?.take(homeCarouselLimit)?.let {
            smallToplist = it
        }

        toplistRepository.favorites?.let {
            favorites = it
        }

        winnersLosersRepository.winners?.take(homeCarouselLimit)?.let {
            periodWinners = it
        }

        winnersLosersRepository.losers?.take(homeCarouselLimit)?.let {
            periodLosers = it
        }

        newCoinsRepository.newCoins?.take(homeCarouselLimit)?.let {
            newCoins = it
        }
    }

    override fun getList(filter: CarouselFilter?): List<Toplist>? {
        if (filter == null) return null
        return when (filter) {
            CarouselFilter.TopCoins -> smallToplist
            CarouselFilter.Favorites -> favorites
            CarouselFilter.Trending -> trendingCoins
            CarouselFilter.TimeframeWinners -> periodWinners
            CarouselFilter.TimeframeLosers -> periodLosers
            CarouselFilter.NewlyListed -> newCoins
        }
    }

    override suspend fun downloadData() {
        when (selectedFilter) {
            CarouselFilter.TopCoins -> getSmallToplist()
            CarouselFilter.Favorites -> getFavorites()
            CarouselFilter.Trending -> getTrendingCoins()
            CarouselFilter.TimeframeWinners -> getWinners()
            CarouselFilter.TimeframeLosers -> getLosers()
            CarouselFilter.NewlyListed -> getNewCoins()
        }
    }

    override fun seeAllPressed() {
        when (selectedFilter) {
            CarouselFilter.TopCoins -> {
                StorageManager.set(StorageKeys.TOPLIST_SORT_OPTION, SortOption.MktCapDesc)
                StorageManager.set(StorageKeys.TOPLIST_PRICE_CHANGE_OPTION, PriceChange.SevenDays)
            }
            CarouselFilter.TimeframeWinners -> {
                StorageManager.set(StorageKeys.TOPLIST_SORT_OPTION, SortOption.PriceChangeDesc)
                StorageManager.set(StorageKeys.TOPLIST_PRICE_CHANGE_OPTION, PriceChange.SevenDays)
            }
            CarouselFilter.TimeframeLosers -> {
                StorageManager.set(StorageKeys.TOPLIST_SORT_OPTION, SortOption.PriceChangeAsc)
                StorageManager.set(StorageKeys.TOPLIST_PRICE_CHANGE_OPTION, PriceChange.SevenDays)
            }
            else -> return
        }
    }

    override suspend fun getFearGreed() {
        fearGreedRepository.getFearGreedModel()?.let {
            fearGreedModel = it
        }
    }

    override suspend fun getGlobalMarket() {
        Log.i("GlobalMarket", "HomeViewModel.getGlobalMarket() called")
        val data = globalMarketRepository.getGlobalMarket()
        Log.d(
            "GlobalMarket",
            "HomeViewModel.getGlobalMarket() result null=${data == null} " +
                "active=${data?.active_cryptocurrencies} pct=${data?.percentages?.size} " +
                "usdMcap=${data?.total_market_cap?.get("usd")}"
        )
        if (data != null) {
            globalMarket = data
            Log.i("GlobalMarket", "HomeViewModel state updated globalMarket != null")
        } else {
            Log.w("GlobalMarket", "HomeViewModel keeping previous globalMarket=${globalMarket != null}")
        }
    }

    private suspend fun getTrendingCoins() {
        trendingRepository.getTrendingCoins()?.let {
            trendingCoins = it.take(homeCarouselLimit)
            setStartData()
        }
    }

    private suspend fun getSmallToplist() {
        toplistRepository.getSmallToplist()?.let {
            smallToplist = it.take(homeCarouselLimit)
            setStartData()
        }
    }

    private suspend fun getWinners() {
        winnersLosersRepository.getWinners(
            priceChange = PriceChange.SevenDays
        )
        setStartData()
    }

    private suspend fun getLosers() {
        winnersLosersRepository.getLosers(
            priceChange = PriceChange.SevenDays
        )?.let {
            periodLosers = it.take(homeCarouselLimit)
        }
        setStartData()
    }

    private suspend fun getFavorites() {
        favorites = toplistRepository.getFavorites()?.sortedBy { it.market_cap_rank } ?: listOf()
        setStartData()
    }

    override fun favouriteButtonPressed(
        item: Toplist,
        context: Context,
    ) {
        viewModelScope.launch {
            val wasFavorite = favorites?.any { it.id == item.id } ?: false
            if (wasFavorite) {
                AnalyticsManager.log(
                    FavoriteRemoved(
                        coinName = item.name ?: "",
                        fromScreen = ScreenLabel.Home,
                        carouselFilter = selectedFilter,
                    ),
                )
            } else {
                AnalyticsManager.log(
                    FavoriteAdded(
                        coinName = item.name ?: "",
                        fromScreen = ScreenLabel.Home,
                        carouselFilter = selectedFilter,
                    ),
                )
            }
            favorites = listOf()
            favorites = toplistRepository.favouriteButtonPressed(item)?.sortedBy { it.market_cap_rank } ?: listOf()
            MainActivity.showAppReview(context)
        }
    }

    override suspend fun getNewCoins() {
        newCoinsRepository.getNewCoinIds()?.let {
            newCoins = it.take(homeCarouselLimit)
            setStartData()
        }
    }

    override suspend fun getGasFees() {
        gasFeesRepository.getGasFees()?.let {
            gasFees = it
        }
    }

    override suspend fun refreshAllHomeData() {
        globalMarketRepository.getGlobalMarket()?.let {
            globalMarket = it
        }
        setStartData()
    }
}
