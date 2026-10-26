package com.cryptium.coincurrently.core.networking
import com.cryptium.coincurrently.core.persistence.UserPreferences
import com.cryptium.coincurrently.core.networking.ResponseModels.WalletImportRequest
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import com.cryptium.coincurrently.sharedui.models.SortOption
import com.cryptium.coincurrently.core.analytics.FirebaseManager
import com.cryptium.coincurrently.core.persistence.allCurrencies
import com.cryptium.coincurrently.core.extensions.unwrapOrNull
import com.cryptium.coincurrently.core.networking.ResponseModels.ApiService
import com.cryptium.coincurrently.core.networking.ResponseModels.PostAlertCoin
import com.cryptium.coincurrently.core.networking.ResponseModels.SendFeedbackRequest
import com.cryptium.coincurrently.core.networking.ResponseModels.SubCoinRequest
import com.cryptium.coincurrently.core.networking.ResponseModels.UserResponse
import com.cryptium.coincurrently.core.networking.ResponseModels.serverBaseUrl

object ApiHelper {
    private val apiService: ApiService
        get() = RetrofitBuilder.apiService

    private val baseUrl: String
        get() = "${serverBaseUrl}/crypto"

    /** Coin detail, OHLC, and market charts always go through the backend. */
    @Suppress("UNUSED_PARAMETER")
    private fun freeUrl(isFree: Boolean, days: String? = null): String = baseUrl

    suspend fun getGlobalMarket() = apiService.getGlobalMarket(baseUrl)
    suspend fun getGlobalChart(days: String) = apiService.getGlobalChart(baseUrl, days)
    suspend fun getCoinsList() = apiService.getCoinsList(baseUrl)
    suspend fun getToplist(currencyCode: String, order: SortOption, itemsPerPage: Int, category: String?) =
        if (category != null) {
            apiService.getToplistWithCategory(baseUrl, currencyCode, order.toplistApiValue, itemsPerPage, category)
        } else {
            apiService.getToplist(baseUrl, currencyCode, order.toplistApiValue, itemsPerPage)
        }
    suspend fun getExchangesToplist() = apiService.getExchangesToplist(baseUrl)
    suspend fun getPriceVolume(id: String, currencyCode: String, days: String, interval: String?, freeUrl: Boolean) = apiService.getPriceAndVolume(freeUrl(freeUrl, days), id, currencyCode, days, interval)
    suspend fun getCandlChart(id: String, currencyCode: String, days: String, freeUrl: Boolean) = apiService.getCandleStickChart(freeUrl(freeUrl, days), id, currencyCode, days)
    suspend fun getCryptoDetailInfo(id: String, freeUrl: Boolean) = apiService.getCryptoDetailInfo(freeUrl(freeUrl), id)
    suspend fun getExchangeDetailInfo(id: String) = apiService.getExchangeDetailInfo(baseUrl, id)
    suspend fun getExchangeVolume(id: String, days: String) = apiService.getExchangeVolume(baseUrl, id, days)
    suspend fun getMultipleCurrencies(ids: String, currencyCode: String) = apiService.getMultipleCoins(baseUrl, ids, currencyCode)
    suspend fun getTrendingCoins() = apiService.getTrendingCoins(baseUrl)
    suspend fun getCoinsWithAllCurrencies(ids: String) = apiService.getCoinsWithAllCurrencies(baseUrl, ids, allCurrencies.map { it.code.uppercase() }.joinToString { it }.replace(" ", "").removeSuffix(","))
    suspend fun getAllCategories(sortOption: SortOption) = apiService.getAllCategories(baseUrl, sortOption.categoriesApiValue)
    suspend fun getAllDerivatives(sortOption: SortOption) = apiService.getAllDerivatives(baseUrl, sortOption.derivativesApiValue)
    suspend fun getPublicTreasury(
        entity: TreasuryEntity,
        coinId: String,
        page: Int = 1,
        perPage: Int = 250,
    ) = apiService.getPublicTreasury(baseUrl, entity.rawValue, coinId, page, perPage)
    suspend fun getTreasuryDetail(entityId: String) =
        apiService.getTreasuryDetail(baseUrl, entityId)
    suspend fun getTreasuryHoldingChart(entityId: String, coinId: String, days: String) =
        apiService.getTreasuryHoldingChart(baseUrl, entityId, coinId, days)
    suspend fun getTreasuryTransactionHistory(
        entityId: String,
        coinIds: String? = null,
        page: Int = 1,
        perPage: Int = 100,
    ) = apiService.getTreasuryTransactionHistory(baseUrl, entityId, coinIds, page, perPage)
    suspend fun getTreasuryEntitiesList(
        entity: TreasuryEntity,
        page: Int = 1,
        perPage: Int = 250,
    ) = apiService.getTreasuryEntitiesList(baseUrl, entity.apiEntityType, page, perPage)
    suspend fun getDerivativeDetail(id: String) = apiService.getDerivativeDetail(baseUrl, id)
    suspend fun getFearGreedModel() = apiService.getFearGreedModel()
    suspend fun getSearchResult(query: String) = apiService.getSearchResult(baseUrl, query)
    suspend fun getNewCoins() = apiService.getNewCoinsIds("${serverBaseUrl}/crypto")
    suspend fun getNftToplist(order: SortOption, platformId: String? = null) = 
        if (platformId != null && platformId != "all") {
            apiService.getNFTToplistWithPlatform("${serverBaseUrl}/crypto", order.nftApiValue, platformId)
        } else {
            apiService.getNFTToplist("${serverBaseUrl}/crypto", order.nftApiValue)
        }

    suspend fun getNftPlatforms() = apiService.getNFTPlatforms("${serverBaseUrl}/crypto")

    suspend fun getNftPriceVolume(id: String, days: String) = apiService.getNFTPriceVolume("${serverBaseUrl}/crypto", id, days)

    suspend fun triggerPriceAlert() = apiService.triggerPriceAlert(serverBaseUrl)
    suspend fun subBigMover() = apiService.subBigMover(serverBaseUrl, mapOf("preferredCurrency" to UserPreferences.getSelectedCurrency().code))
    suspend fun ubsubBigMovers() = apiService.ubsubBigMover(serverBaseUrl)
    suspend fun bigMoverTrigger() = apiService.bigMoverTrigger(serverBaseUrl)
    suspend fun sendFeedback(rating: Int, feedback: String?) = apiService.sendFeedback(
        serverBaseUrl,
        SendFeedbackRequest(
            rating = rating,
            feedback = feedback?.takeIf { it.isNotBlank() }
        )
    )

    suspend fun getUser(): UserResponse? = apiService.getUser(serverBaseUrl).unwrapOrNull()

    suspend fun subCoin(coinIds: List<String>, currencies: List<String>) = apiService.subCoin(
        serverBaseUrl,
        SubCoinRequest(
            coinIds = coinIds,
            currencies = currencies,
        ),
    )

    suspend fun getNftDetail(id: String) = apiService.getNFTDetail(baseUrl, id)
    suspend fun getNFTTickers(id: String) = apiService.getNFTTickers(baseUrl, id)
    suspend fun getGasFees(includeHistory: Boolean) = apiService.getGasFees(serverBaseUrl, includeHistory)
    suspend fun getWalletAddress(address: String, blockChain: String) = apiService.getWalletAddress(serverBaseUrl, request = WalletImportRequest(
        address = address,
        blockChain = blockChain
    ))
    suspend fun getWinnersLosers(currencyCode: String, duration: String) = apiService.getWinnersLosers(serverBaseUrl, currencyCode, duration, "500")
    suspend fun getAlerts() = apiService.getAlerts(serverBaseUrl)
    suspend fun createAlert(alertCoin: PostAlertCoin) = apiService.createAlert(serverBaseUrl, alertCoin)
    suspend fun deleteAlert(alertId: String) = apiService.deleteAlert(serverBaseUrl, mapOf("alertId" to alertId))
    suspend fun updateUser(token: String) = apiService.updateUser(serverBaseUrl, mapOf("newFCM" to token, "preferredCurrency" to UserPreferences.getSelectedCurrency().code))
}

fun isOnline(context: Context): Boolean {
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val n = cm.activeNetwork
        if (n != null) {
            val nc = cm.getNetworkCapabilities(n)
            //It will check for both wifi and cellular network
            return nc!!.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) || nc.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
        }
        return false
    } else {
        val netInfo = cm.activeNetworkInfo
        return netInfo != null && netInfo.isConnectedOrConnecting
    }
}