package networking.ResponseModels

import AddressImport
import WalletImportRequest
import com.cryptium.coincurrently.ui.main.HomeTab.GlobalMarket.DataClasses.GlobalMarketChartModel
import com.cryptium.coincurrently.ui.main.HomeTab.GlobalMarket.DataClasses.GlobalMarketModel
import com.cryptium.coincurrently.ui.main.HomeTab.Home.DataClasses.WinnersLosers
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import com.cryptium.coincurrently.BuildConfig
import okhttp3.ResponseBody

// Crypto endpoints follow the CoinGecko API shape. Set API_BASE_URL in
// Config/secrets.properties to your backend, or CoinGecko's base URL;
// e.g. https://api.coingecko.com
val serverBaseUrl: String
    get() = BuildConfig.API_BASE_URL.trimEnd('/')

interface ApiService{
    @GET("{url}/api/v3/coins/markets?sparkline=true&price_change_percentage=1h%2C24h%2C7d%2C30d")
    suspend fun getToplist(
        @Path(value = "url", encoded = true) url: String,
        @Query(value = "vs_currency", encoded = true) currencyCode: String,
        @Query(value = "order", encoded = true) sortOption: String,
        @Query(value = "per_page", encoded = true) itemsPerPage: Int
    ): Result<ArrayList<Toplist>>

    @GET("{url}/api/v3/coins/markets?sparkline=true&price_change_percentage=1h%2C24h%2C7d%2C30d")
    suspend fun getToplistWithCategory(
        @Path(value = "url", encoded = true) url: String,
        @Query(value = "vs_currency", encoded = true) currencyCode: String,
        @Query(value = "order", encoded = true) sortOption: String,
        @Query(value = "per_page", encoded = true) itemsPerPage: Int,
        @Query(value = "category", encoded = true) category: String
    ): Result<ArrayList<Toplist>>

    @GET("{url}/api/v3/exchanges?per_page=100")
    suspend fun getExchangesToplist(
        @Path(value = "url", encoded = true) url: String,
        @Query(value = "page", encoded = true)page: Int = 1
    ): Result<ArrayList<ExchangeList>>

    @GET("{url}/api/v3/coins/markets?order=market_cap_desc&per_page=100&page=1&sparkline=true&price_change_percentage=1h%2C24h%2C7d%2C30d")
    suspend fun getMultipleCoins(
        @Path(value = "url", encoded = true) url: String,
        @Query(value = "ids", encoded = true) ids: String,
        @Query(value = "vs_currency", encoded = true) currencyCode: String,
    ): Result<ArrayList<Toplist>>

    @GET("{url}/api/v3/global")
    suspend fun getGlobalMarket(
        @Path(value = "url", encoded = true) url: String,
    ): Result<GlobalMarketModel>

    @GET("{url}/api/v3/global/market_cap_chart")
    suspend fun getGlobalChart(
        @Path(value = "url", encoded = true) url: String,
        @Query(value = "days", encoded = true) days: String,
    ): Result<GlobalMarketChartModel>

    @GET("{url}/api/v3/coins/{id}/market_chart")
    suspend fun getPriceAndVolume(
        @Path(value = "url", encoded = true) url: String,
        @Path("id")id: String,
        @Query(value = "vs_currency", encoded = true)currencyCode: String,
        @Query(value = "days", encoded = true)days: String,
        @Query(value = "interval", encoded = true)interval: String?
    ): Result<PriceVolumeModel>

    @GET("{url}/api/v3/coins/{id}/ohlc")
    suspend fun getCandleStickChart(
        @Path(value = "url", encoded = true) url: String,
        @Path("id")id: String,
        @Query(value = "vs_currency", encoded = true)currencyCode: String,
        @Query(value = "days", encoded = true)days: String
    ): Result<String>

    @GET("{url}/api/v3/coins/list")
    suspend fun getCoinsList(
        @Path(value = "url", encoded = true) url: String,
    ): Result<ArrayList<CoinsList>>

    @GET("{url}/api/v3/coins/{id}")
    suspend fun getCryptoDetailInfo(
        @Path(value = "url", encoded = true) url: String,
        @Path("id")id: String,
        @Query(value = "date")date: String = "",
        @Query(value = "sparkline")sparkline: Boolean = true
    ): Result<CryptoDetailInfoModel>

    @GET("{url}/api/v3/exchanges/{id}")
    suspend fun getExchangeDetailInfo(
        @Path(value = "url", encoded = true) url: String,
        @Path("id")id: String,
    ): Result<ExchangeResponse>

    @GET("{url}/api/v3/exchanges/{id}/volume_chart")
    suspend fun getExchangeVolume(
        @Path(value = "url", encoded = true) url: String,
        @Path("id")id: String,
        @Query(value = "days", encoded = true)days: String
    ): Result<String>

    @GET("{url}/api/v3/search/trending")
    suspend fun getTrendingCoins(
        @Path(value = "url", encoded = true) url: String,
    ): Result<TrendingItems>

    @GET("{url}/api/v3/simple/price?")
    suspend fun getCoinsWithAllCurrencies(
        @Path(value = "url", encoded = true) url: String,
        @Query("ids")ids: String,
        @Query("vs_currencies")vs_currencies: String,
    ): Result<MutableMap<String, Map<String, Double>>>

    @GET("{url}/api/v3/coins/categories")
    suspend fun getAllCategories(
        @Path(value = "url", encoded = true) url: String,
        @Query("order")order: String
    ): Result<ArrayList<CryptoCategory>>

    @GET("{url}/api/v3/derivatives/exchanges?per_page=250")
    suspend fun getAllDerivatives(
        @Path(value = "url", encoded = true) url: String,
        @Query("order") order: String
    ): Result<List<Derivative>>

    @GET("{url}/api/v3/{entity}/public_treasury/{coinId}")
    suspend fun getPublicTreasury(
        @Path(value = "url", encoded = true) url: String,
        @Path("entity") entity: String,
        @Path("coinId") coinId: String,
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 250,
    ): Result<PublicTreasuryModel>

    @GET("{url}/api/v3/public_treasury/{entityId}")
    suspend fun getTreasuryDetail(
        @Path(value = "url", encoded = true) url: String,
        @Path("entityId") entityId: String,
    ): Result<TreasuryDetailEntity>

    @GET("{url}/api/v3/public_treasury/{entityId}/{coinId}/holding_chart")
    suspend fun getTreasuryHoldingChart(
        @Path(value = "url", encoded = true) url: String,
        @Path("entityId") entityId: String,
        @Path("coinId") coinId: String,
        @Query("days") days: String,
    ): Result<TreasuryHoldingChartModel>

    @GET("{url}/api/v3/public_treasury/{entityId}/transaction_history")
    suspend fun getTreasuryTransactionHistory(
        @Path(value = "url", encoded = true) url: String,
        @Path("entityId") entityId: String,
        @Query("coin_ids") coinIds: String? = null,
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 100,
    ): Result<TreasuryTransactionHistoryModel>

    @GET("{url}/api/v3/entities/list")
    suspend fun getTreasuryEntitiesList(
        @Path(value = "url", encoded = true) url: String,
        @Query("entity_type") entityType: String,
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 250,
    ): Result<List<TreasuryEntityListItem>>

    @GET("{url}/api/v3/derivatives/exchanges/{id}?include_tickers=unexpired")
    suspend fun getDerivativeDetail(
        @Path(value = "url", encoded = true) url: String,
        @Path("id")ids: String,
    ): Result<Derivative>

    @GET("{url}/api/v3/coins/list/new")
    suspend fun getNewCoinsIds(
        @Path(value = "url", encoded = true) url: String
    ): Result<ArrayList<CoinsList>>

    @GET("{url}/api/v3/nfts/markets?per_page=250")
    suspend fun getNFTToplist(
        @Path(value = "url", encoded = true) url: String,
        @Query(value = "order", encoded = true) order: String
    ): Result<ArrayList<NFTToplist>>

    @GET("{url}/api/v3/nfts/markets?per_page=250")
    suspend fun getNFTToplistWithPlatform(
        @Path(value = "url", encoded = true) url: String,
        @Query(value = "order", encoded = true) order: String,
        @Query(value = "asset_platform_id", encoded = true) filter: String
    ): Result<ArrayList<NFTToplist>>

    @GET("{url}/api/v3/asset_platforms?filter=nft")
    suspend fun getNFTPlatforms(
        @Path(value = "url", encoded = true) url: String,
    ): Result<ArrayList<NFTPlatform>>

    @GET("{url}/api/v3/nfts/{id}/market_chart")
    suspend fun getNFTPriceVolume(
        @Path(value = "url", encoded = true) url: String,
        @Path(value = "id", encoded = true) id: String,
        @Query(value = "days", encoded = true) days: String
    ): Result<String>

    @GET("{url}/api/v3/nfts/{id}")
    suspend fun getNFTDetail(
        @Path(value = "url", encoded = true) url: String,
        @Path(value = "id", encoded = true) id: String
    ): Result<NFTDetail>

    @GET("{url}/api/v3/nfts/{id}/tickers")
    suspend fun getNFTTickers(
        @Path(value = "url", encoded = true) url: String,
        @Path(value = "id", encoded = true) id: String
    ): Result<NFTTickerResponse>

    @GET("https://api.alternative.me/fng/?limit=0&format=json")
    suspend fun getFearGreedModel(): Result<FearGreed>

    @GET("{url}/api/v3/search")
    suspend fun getSearchResult(
        @Path(value = "url", encoded = true) url: String,
        @Query(value = "query") query: String = ""
    ): Result<SearchModel>

    @GET("{url}/news/api/v1/category?section=alltickers&page=1")
    suspend fun getNewNews(
        @Path(value = "url", encoded = true) url: String,
        @Query(value = "items")count: Int,
    ): Result<NewNewsModel>

    @GET("{url}/news/api/v1/category?section=alltickers&type=video")
    suspend fun getNewVideoNews(
        @Path(value = "url", encoded = true) url: String,
        @Query(value = "items")count: Int,
    ): Result<NewNewsModel>

    @GET("{url}/news/api/v1/category?section=alltickers&topic=podcast")
    suspend fun getNewPodcastNews(
        @Path(value = "url", encoded = true) url: String,
        @Query(value = "items")count: Int,
    ): Result<NewNewsModel>

    @GET("{url}/news/api/v1/category?section=general&search=exchanges&page=1")
    suspend fun getNewsExchanges(
        @Path(value = "url", encoded = true) url: String,
        @Query(value = "items")count: Int,
    ): Result<NewNewsModel>

    @GET("{url}/news/api/v1/category?section=general&topic=NFT&page=1")
    suspend fun getNewsNFT(
        @Path(value = "url", encoded = true) url: String,
        @Query(value = "items") count: Int,
    ): Result<NewNewsModel>

    @GET("{url}/news/api/v1/category?section=general&page=1")
    suspend fun getNewsExchange(
        @Path(value = "url", encoded = true) url: String,
        @Query(value = "search")id: String,
        @Query(value = "items")count: Int,
    ): Result<NewNewsModel>


    @GET("{url}/news/api/v1/category?section=alltickers&page=1&topic=Tanalysis")
    suspend fun getNewsTAnalysis(
        @Path(value = "url", encoded = true) url: String,
        @Query(value = "items")count: Int,
    ): Result<NewNewsModel>

    @GET("{url}/news/api/v1?page=1")
    suspend fun getNewsCoin(
        @Path(value = "url", encoded = true) url: String,
        @Query(value = "tickers")id: String,
        @Query(value = "items")count: Int,
    ): Result<NewNewsModel>

    @GET("{url}/gas")
    suspend fun getGasFees(
        @Path(value = "url", encoded = true) url: String,
        @Query(value = "includeHistory")includeHistory: Boolean,
    ): Result<GasFeesModel>

    @POST("{url}/wallet-import")
    suspend fun getWalletAddress(
        @Path(value = "url", encoded = true) url: String,
        @Body request: WalletImportRequest
    ): Result<AddressImport>

    @GET("{url}/crypto/api/v3/coins/top_gainers_losers")
    suspend fun getWinnersLosers(
        @Path(value = "url", encoded = true) url: String,
        @Query("vs_currency") currencyCode: String,
        @Query(value = "duration", encoded = true) duration: String,
        @Query(value = "top_coins", encoded = true) coinsCount: String
    ): Result<WinnersLosers>

    @GET("{url}/get-alerts")
    suspend fun getAlerts(
        @Path(value = "url", encoded = true) url: String,
    ): Result<List<AlertCoin>>

    @POST("{url}/create-alert")
    suspend fun createAlert(
        @Path(value = "url", encoded = true) url: String,
        @Body alert: PostAlertCoin,
    ): Result<AlertCoin>

    @POST("{url}/delete-alert")
    suspend fun deleteAlert(
        @Path(value = "url", encoded = true) url: String,
        @Body alertId: Map<String, String>,
    ): Result<Unit>

    @POST("{url}/update-user")
    suspend fun updateUser(
        @Path(value = "url", encoded = true) url: String,
        @Body token: Map<String, String>,
    ): Result<Unit>

    @POST("{url}/price-alert-trigger")
    suspend fun triggerPriceAlert(
        @Path(value = "url", encoded = true) url: String
    ): Result<Unit>

    @POST("{url}/sub-big-mover")
    suspend fun subBigMover(
        @Path(value = "url", encoded = true) url: String,
        @Body preferredCurrency: Map<String, String>,
    ): Result<Unit>

    @POST("{url}/unsub-big-mover")
    suspend fun ubsubBigMover(
        @Path(value = "url", encoded = true) url: String
    ): Result<Unit>

    @POST("{url}/big-mover-trigger")
    suspend fun bigMoverTrigger(
        @Path(value = "url", encoded = true) url: String
    ): Result<Unit>

    @POST("{url}/feedback")
    suspend fun sendFeedback(
        @Path(value = "url", encoded = true) url: String,
        @Body body: SendFeedbackRequest,
    ): Result<Unit>

    @GET("{url}/user")
    suspend fun getUser(
        @Path(value = "url", encoded = true) url: String,
    ): Result<UserResponse>

    @POST("{url}/redeem-code")
    suspend fun redeemCode(
        @Path(value = "url", encoded = true) url: String,
        @Body body: RedeemCodeRequest,
    ): Result<Unit>

    @POST("{url}/premium/sub-coin")
    suspend fun subCoin(
        @Path(value = "url", encoded = true) url: String,
        @Body body: SubCoinRequest,
    ): Result<Unit>
}

data class SubCoinRequest(
    val coinIds: List<String>,
    val currencies: List<String>,
)