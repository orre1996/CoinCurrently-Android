package com.cryptium.coincurrently.features.Home
import com.cryptium.coincurrently.core.persistence.UserPreferences
import android.util.Log
import android.app.Application
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.android.billingclient.api.ProductDetails
import com.cryptium.coincurrently.app.MainActivity
import com.cryptium.coincurrently.core.extensions.currencyAccessible
import com.cryptium.coincurrently.core.extensions.formatCurrency
import com.cryptium.coincurrently.core.extensions.getAccessiblePrice
import com.cryptium.coincurrently.core.extensions.getFormattedPrice
import com.cryptium.coincurrently.core.extensions.inTime
import com.cryptium.coincurrently.core.extensions.isMoreThanOneMonthOld
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
import com.cryptium.coincurrently.core.analytics.FirebaseManager
import com.cryptium.coincurrently.core.config.RemoteConfigKeys
import com.cryptium.coincurrently.core.persistence.allCurrencies
import com.cryptium.coincurrently.features.Market.Repositories.ToplistRepository
import com.cryptium.coincurrently.core.purchases.StoreKitManager
import com.cryptium.coincurrently.core.analytics.FavoriteAdded
import com.cryptium.coincurrently.core.analytics.FavoriteRemoved
import com.cryptium.coincurrently.core.analytics.AnalyticsManager
import com.cryptium.coincurrently.core.analytics.ScreenLabel
import com.cryptium.coincurrently.sharedui.models.CachedData
import kotlinx.coroutines.launch
import com.cryptium.coincurrently.core.networking.ResponseModels.*
import com.revenuecat.purchases.models.GooglePurchasingData
import com.revenuecat.purchases.models.GoogleStoreProduct
import com.revenuecat.purchases.models.StoreProduct
import java.util.*
import com.cryptium.coincurrently.core.persistence.StorageKeys
import com.cryptium.coincurrently.core.persistence.StorageManager
import com.cryptium.coincurrently.core.purchases.promotionalName
import com.cryptium.coincurrently.core.purchases.welcomeName

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

    var promotionTitle: String?
    var promotionMessage: String?
    var promotionMessageAccessible: String?
    var offerTimeLeft: String?
    var showFreeTrial: Boolean
    var welcomeOfferTimeLeft: Date?

    fun initObservers(lifecycleOwner: LifecycleOwner)
    fun loadPremiumOfferProducts(context: Context)
    fun getList(filter: CarouselFilter?): List<Toplist>?
    suspend fun downloadData()
    fun seeAllPressed()
    fun premiumViewClosed(title: String)
    suspend fun getFearGreed()
    suspend fun getGlobalMarket()
    fun favouriteButtonPressed(item: Toplist, context: Context, showBuyPro: () -> Unit)
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

    override var selectedFilter: CarouselFilter by mutableStateOf(CarouselFilter.TopCoins)
    var hasResolvedDefaultHomeFilter by mutableStateOf(false)
        private set

    override var fearGreedModel: FearGreed? by mutableStateOf(null)
    override var globalMarket: GlobalMarketData? by mutableStateOf(null)
    override var trendingCoins: List<Toplist>? by mutableStateOf(null)
    override var smallToplist: List<Toplist>? by mutableStateOf(null)
    override var favorites: List<Toplist>? by mutableStateOf(null)
    override var periodWinners: List<Toplist>? by mutableStateOf(null)
    override var periodLosers: List<Toplist>? by mutableStateOf(null)
    override var newCoins: List<Toplist>? by mutableStateOf(null)
    override var gasFees: GasFeesModel? by mutableStateOf(null)

    // Promotions and Information

    override var promotionTitle: String? by mutableStateOf(null)
    override var promotionMessage: String? by mutableStateOf(null)
    override var promotionMessageAccessible: String? by mutableStateOf(null)
    override var showFreeTrial = false
    override var offerTimeLeft: String? by mutableStateOf(null)
    override var welcomeOfferTimeLeft: Date? by mutableStateOf(Date())

    private var didClosePromotions: Boolean  by mutableStateOf(StorageManager.get(StorageKeys.DID_CLOSE_PROMOTION) ?: false)
    private var didCloseWelcome: Boolean by mutableStateOf(StorageManager.get(StorageKeys.DID_CLOSE_WELCOME) ?: false)
    private var timer: Timer? = null

    init {
        setStartData()
        fearGreedModel = fearGreedRepository.fearGreed
        gasFees = gasFeesRepository.cachedGasFees

        globalMarketRepository.globalMarket?.let {
            globalMarket = it
        }

        val hasClosed = StorageManager.get<Boolean>(StorageKeys.HAS_CLOSED_APP)
        if (hasClosed == true) {
            setShowWelcomeMessage()
        }

        setShowFreeTrial()
    }

    private var storeKitObserversInitialized: Boolean = false
    private var cachedPlayProducts: List<ProductDetails>? = null
    private var cachedStoreProducts: List<StoreProduct>? = null

    override fun loadPremiumOfferProducts(context: Context) {
        StoreKitManager.setUpBillingClient(context) { products, revenueCatProducts ->
            cachedPlayProducts = products
            cachedStoreProducts = revenueCatProducts
            refreshPremiumOffer()
        }
    }

    override fun initObservers(lifecycleOwner: LifecycleOwner) {
        if (storeKitObserversInitialized) {
            return
        }
        storeKitObserversInitialized = true
        StoreKitManager.storeProducts.observe(lifecycleOwner) { products ->
            if (!products.isNullOrEmpty()) {
                cachedPlayProducts = products
            }
            refreshPremiumOffer()
        }
        StoreKitManager.isEligableForTrial.observe(lifecycleOwner) { refreshPremiumOffer() }
        StoreKitManager.currentSubscription.observe(lifecycleOwner) { refreshPremiumOffer() }
    }

    private fun refreshPremiumOffer() {
        viewModelScope.launch {
            refreshPremiumOfferInternal()
        }
    }

    private suspend fun refreshPremiumOfferInternal() {
        FirebaseManager.ensureRemoteConfigFetched()
        val playProducts = cachedPlayProducts?.takeIf { it.isNotEmpty() }
            ?: StoreKitManager.storeProducts.value?.takeIf { it.isNotEmpty() }
        val storeProducts = cachedStoreProducts?.takeIf { it.isNotEmpty() }
        val isEligibleForTrial = StoreKitManager.isEligableForTrial.value
        val hasActiveSubscription = StoreKitManager.currentSubscription.value != null

        when {
            !playProducts.isNullOrEmpty() ->
                setPremiumOffer(playProducts, isEligibleForTrial, hasActiveSubscription)
            !storeProducts.isNullOrEmpty() ->
                setPremiumOfferFromStoreProducts(storeProducts, isEligibleForTrial, hasActiveSubscription)
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
        applyDefaultHomeFilterIfNeeded()

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
            else -> null // News filters not used on Home
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
            else -> { } // News filters not used on Home
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

    override fun premiumViewClosed(title: String) {
        if (title == "Free trial") {
            StorageManager.set(StorageKeys.HOME_SHOW_FREE_TRIAL, false)
            StorageManager.set(StorageKeys.HOME_NEXT_FREE_TRIAL_DATE, Date())
            showFreeTrial = false
        } else if (title == "Limited offer") {
            StorageManager.set(StorageKeys.DID_CLOSE_PROMOTION, true)
            didClosePromotions = true
        } else {
            StorageManager.set(StorageKeys.DID_CLOSE_WELCOME, true)
            didCloseWelcome = true
        }

        promotionTitle = null
        promotionMessage = null
        promotionMessageAccessible = null
        offerTimeLeft = null

        timer?.cancel()
        timer?.purge()
        timer = null
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

    private fun applyDefaultHomeFilterIfNeeded() {
        if (hasResolvedDefaultHomeFilter) return
        hasResolvedDefaultHomeFilter = true
        selectedFilter = CarouselFilter.defaultHomeFilter(hasFavorites = !favorites.isNullOrEmpty())
    }

    private suspend fun getFavorites() {
        favorites = toplistRepository.getFavorites()?.sortedBy { it.market_cap_rank } ?: listOf()
        setStartData()
    }

    override fun favouriteButtonPressed(
        item: Toplist,
        context: Context,
        showBuyPro: () -> Unit
    ) {
        val isFavorite = favorites?.any { it.id == item.id } ?: false
        if (StoreKitManager.currentSubscription.value == null && !isFavorite && UserPreferences.getFavorites().orEmpty().size >= 2) {
            showBuyPro.invoke()
            return
        }

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

    private fun setShowFreeTrial() {
        val date: Date? = StorageManager.get(StorageKeys.HOME_NEXT_FREE_TRIAL_DATE)

        if (date != null && date.isMoreThanOneMonthOld()) {
            StorageManager.set(StorageKeys.HOME_SHOW_FREE_TRIAL, true)
        } else if (date == null) {
            StorageManager.set(StorageKeys.HOME_NEXT_FREE_TRIAL_DATE, Date().inTime(days = 3))
            StorageManager.set(StorageKeys.HOME_SHOW_FREE_TRIAL, false)
        }

        showFreeTrial = StorageManager.get(StorageKeys.HOME_SHOW_FREE_TRIAL) ?: false
    }

    private fun setShowWelcomeMessage() {
        var date: Date? = StorageManager.get(StorageKeys.HOME_WELCOME_OFFER_DATE)

        if (date == null) {
            date = Date().inTime(days = 3)
            StorageManager.set(StorageKeys.HOME_WELCOME_OFFER_DATE, date)
        }

        welcomeOfferTimeLeft = date

        if (date < Date()) {
            return
        }

        setWelcomeTimerDate()

        val timerTask = object : TimerTask() {
            override fun run() {
                setWelcomeTimerDate()
            }
        }

        timer = Timer()
        timer?.schedule(timerTask, 0L, 1000L)
    }

    private fun setWelcomeTimerDate() {
        val timeLeft: Date? = StorageManager.get(StorageKeys.HOME_WELCOME_OFFER_DATE)
        val now = Date()

        if (timeLeft == null || timeLeft.before(now)) {
            timer?.run {
                cancel()
                purge()
            }
            timer = null
            offerTimeLeft = null
            didCloseWelcome = true
            StorageManager.set(StorageKeys.DID_CLOSE_WELCOME, true)

            if (promotionTitle == "Welcome offer") {
                promotionTitle = null
                promotionMessage = null
                promotionMessageAccessible = null
            }
            return
        }

        val timeDiffMillis = timeLeft.time - now.time

        val totalSeconds = timeDiffMillis / 1000
        val days = totalSeconds / (60 * 60 * 24)
        val hours = (totalSeconds / (60 * 60)) % 24
        val minutes = (totalSeconds / 60) % 60
        val seconds = totalSeconds % 60

        offerTimeLeft = "Expires in ${days}d ${hours}h ${minutes}m ${seconds}s"
    }

    private suspend fun setPremiumOffer(
        products: List<ProductDetails>?,
        isEligibleForTrial: Boolean?,
        hasActiveSubscription: Boolean
    ) {
        if (hasActiveSubscription || isEligibleForTrial != true || products.isNullOrEmpty()) return

        val showPromotionalOffer = FirebaseManager.getConfigBool(RemoteConfigKeys.ShowPromotionalOffer) == true
        val offerProduct = products.firstOrNull { it.productId == promotionalName }
        val welcomeProduct = products.firstOrNull { it.productId == welcomeName }
        val welcomeOfferExpiry: Date? = StorageManager.get(StorageKeys.HOME_WELCOME_OFFER_DATE)
        val currentDate = Date()

        when {
            !didClosePromotions && showPromotionalOffer && offerProduct != null -> {
                val price = offerProduct.getFormattedPrice()
                val priceAccessible = offerProduct.getAccessiblePrice()
                promotionTitle = "Limited offer"
                promotionMessage = "Get 1 year of CoinCurrently Pro for only $price."
                promotionMessageAccessible = "Get 1 year of CoinCurrently Pro for only $priceAccessible."
            }

            !didCloseWelcome && welcomeProduct != null && welcomeOfferExpiry != null && welcomeOfferExpiry.after(currentDate) -> {
                val price = welcomeProduct.getFormattedPrice()
                val priceAccessible = welcomeProduct.getAccessiblePrice()
                promotionTitle = "Welcome offer"
                promotionMessage = "Get 1 year of CoinCurrently Pro for only $price."
                promotionMessageAccessible = "Get 1 year of CoinCurrently Pro for only $priceAccessible."
            }

            showFreeTrial -> {
                promotionTitle = "Free trial"
                promotionMessage = "Try CoinCurrently Pro for free"
                promotionMessageAccessible = "Try CoinCurrently Pro for free"
            }
        }
    }

    private suspend fun setPremiumOfferFromStoreProducts(
        products: List<StoreProduct>,
        isEligibleForTrial: Boolean?,
        hasActiveSubscription: Boolean,
    ) {
        if (hasActiveSubscription || isEligibleForTrial != true) return

        val showPromotionalOffer = FirebaseManager.getConfigBool(RemoteConfigKeys.ShowPromotionalOffer) == true
        val offerProduct = products.firstOrNull { it.playProductId() == promotionalName }
        val welcomeProduct = products.firstOrNull { it.playProductId() == welcomeName }
        val welcomeOfferExpiry: Date? = StorageManager.get(StorageKeys.HOME_WELCOME_OFFER_DATE)
        val currentDate = Date()

        when {
            !didClosePromotions && showPromotionalOffer && offerProduct != null -> {
                val (price, priceAccessible) = offerProduct.getPromoPrices()
                promotionTitle = "Limited offer"
                promotionMessage = "Get 1 year of CoinCurrently Pro for only $price."
                promotionMessageAccessible = "Get 1 year of CoinCurrently Pro for only $priceAccessible."
            }

            !didCloseWelcome && welcomeProduct != null && welcomeOfferExpiry != null && welcomeOfferExpiry.after(currentDate) -> {
                val (price, priceAccessible) = welcomeProduct.getPromoPrices()
                promotionTitle = "Welcome offer"
                promotionMessage = "Get 1 year of CoinCurrently Pro for only $price."
                promotionMessageAccessible = "Get 1 year of CoinCurrently Pro for only $priceAccessible."
            }

            showFreeTrial -> {
                promotionTitle = "Free trial"
                promotionMessage = "Try CoinCurrently Pro for free"
                promotionMessageAccessible = "Try CoinCurrently Pro for free"
            }
        }
    }

    private fun StoreProduct.playProductId(): String? {
        (this as? GoogleStoreProduct)?.productId?.let { return it }
        when (val data = purchasingData) {
            is GooglePurchasingData -> return data.productId
        }
        val idPrefix = id.substringBefore(":")
        if (idPrefix in setOf(promotionalName, welcomeName)) return idPrefix
        if (id in setOf(promotionalName, welcomeName)) return id
        return null
    }

    private fun StoreProduct.getPromoPrices(): Pair<String, String> {
        val phases = defaultOption?.pricingPhases ?: return "" to ""
        val cheapestPhase = phases.minByOrNull { it.price.amountMicros } ?: return "" to ""
        val currencyModel = allCurrencies.firstOrNull {
            it.code.equals(cheapestPhase.price.currencyCode, ignoreCase = true)
        } ?: CurrencySelectionModel()
        val amount = cheapestPhase.price.amountMicros.toDouble() / 1_000_000
        return amount.formatCurrency(currencySelectionModel = currencyModel) to
            amount.currencyAccessible(currency = currencyModel)
    }

}
