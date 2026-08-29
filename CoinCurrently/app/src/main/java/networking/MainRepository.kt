package networking

import com.cryptium.coincurrently.ui.main.General.Enums.SortOption
import networking.ResponseModels.AlertCoin
import networking.ResponseModels.PostAlertCoin

class MainRepository(private val apiHelper: ApiHelper) {
    suspend fun getGlobalMarket() = apiHelper.getGlobalMarket()
    suspend fun getGlobalChart(days: String) = apiHelper.getGlobalChart(days)
    suspend fun getCoinsList() = apiHelper.getCoinsList()
    suspend fun getToplist(currencyCode: String, order: SortOption, itemsPerPage: Int, platformId: String?) = apiHelper.getToplist(currencyCode, order, itemsPerPage, platformId)
    suspend fun getExchangesToplist() = apiHelper.getExchangesToplist()
    suspend fun getPriceVolume(id: String, currencyCode: String, days: String, interval: String?, freeUrl: Boolean) = apiHelper.getPriceVolume(id, currencyCode, days, interval, freeUrl)
    suspend fun getCandleChart(id: String, currencyCode: String, days: String, freeUrl: Boolean) = apiHelper.getCandlChart(id, currencyCode, days, freeUrl)
    suspend fun getCryptoDetailInfo(id: String, freeUrl: Boolean) = apiHelper.getCryptoDetailInfo(id, freeUrl)
    suspend fun getExchangeVolume(id: String, days: String) = apiHelper.getExchangeVolume(id, days)
    suspend fun getExchangeDetailInfo(id: String) = apiHelper.getExchangeDetailInfo(id)
    suspend fun getMultipleCurrencies(ids: String, currencyCode: String) = apiHelper.getMultipleCurrencies(ids, currencyCode)
    suspend fun getTrendingCoins() = apiHelper.getTrendingCoins()
    suspend fun getCoinsWithAllCurrencies(ids: String) = apiHelper.getCoinsWithAllCurrencies(ids)
    suspend fun getAllCategories(sortOption: SortOption) = apiHelper.getAllCategories(sortOption = sortOption)
    suspend fun getAllDerivatives(sortOption: SortOption) = apiHelper.getAllDerivatives(sortOption)
    suspend fun getPublicTreasury(
        entity: com.cryptium.coincurrently.ui.main.General.Enums.TreasuryEntity,
        coinId: String,
        page: Int = 1,
        perPage: Int = 250,
    ) = apiHelper.getPublicTreasury(entity, coinId, page, perPage)
    suspend fun getTreasuryDetail(entityId: String) = apiHelper.getTreasuryDetail(entityId)
    suspend fun getTreasuryHoldingChart(entityId: String, coinId: String, days: String) =
        apiHelper.getTreasuryHoldingChart(entityId, coinId, days)
    suspend fun getTreasuryTransactionHistory(
        entityId: String,
        coinIds: String? = null,
        page: Int = 1,
        perPage: Int = 100,
    ) = apiHelper.getTreasuryTransactionHistory(entityId, coinIds, page, perPage)
    suspend fun getTreasuryEntitiesList(
        entity: com.cryptium.coincurrently.ui.main.General.Enums.TreasuryEntity,
        page: Int = 1,
        perPage: Int = 250,
    ) = apiHelper.getTreasuryEntitiesList(entity, page, perPage)
    suspend fun getDerivativeDetail(id: String) = apiHelper.getDerivativeDetail(id)
    suspend fun getFearGreedModel() = apiHelper.getFearGreedModel()
    suspend fun getSearchResults(query: String) = apiHelper.getSearchResult(query)
    suspend fun getNewCoins() = apiHelper.getNewCoins()
    suspend fun getNftToplist(order: SortOption, platformId: String? = null) = apiHelper.getNftToplist(order, platformId)
    suspend fun getNftPlatforms() = apiHelper.getNftPlatforms()
    suspend fun getNftPriceVolume(id: String, days: String) = apiHelper.getNftPriceVolume(id, days)
    suspend fun getNftDetail(id: String) = apiHelper.getNftDetail(id)
    suspend fun getNFTTickers(id: String) = apiHelper.getNFTTickers(id)
    suspend fun getNewNews(count: Int, category: NewsCategory) = apiHelper.getNewNews(count, category)
    suspend fun getGasFees(includeHistory: Boolean) = apiHelper.getGasFees(includeHistory)
    suspend fun getWalletAddress(address: String, blockChain: String) = apiHelper.getWalletAddress(address, blockChain)
    suspend fun getWinnersLosers(currencyCode: String, duration: String) = apiHelper.getWinnersLosers(currencyCode, duration)
    suspend fun getAlerts() = apiHelper.getAlerts()
    suspend fun createPriceAlert(alertCoin: PostAlertCoin) = apiHelper.createAlert(alertCoin)
    suspend fun deletePriceAlert(alertId: String) = apiHelper.deleteAlert(alertId)
    suspend fun updateUser(token: String) = apiHelper.updateUser(token)
    suspend fun triggerPriceAlert() = apiHelper.triggerPriceAlert()
    suspend fun subBigMover() = apiHelper.subBigMover()
    suspend fun ubsubBigMover() = apiHelper.ubsubBigMovers()
    suspend fun bigMoverTrigger() = apiHelper.bigMoverTrigger()
    suspend fun sendFeedback(rating: Int, feedback: String?) = apiHelper.sendFeedback(rating, feedback)
    suspend fun getReferralCode() = apiHelper.getReferralCode()
    suspend fun getUser() = apiHelper.getUser()
    suspend fun redeemCode(refCode: String) = apiHelper.redeemCode(refCode)
    suspend fun subCoin(coinIds: List<String>, currencies: List<String>) = apiHelper.subCoin(coinIds, currencies)
}