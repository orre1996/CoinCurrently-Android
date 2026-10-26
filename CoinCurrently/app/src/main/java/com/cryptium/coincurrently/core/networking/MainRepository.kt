package com.cryptium.coincurrently.core.networking
import com.cryptium.coincurrently.sharedui.models.SortOption
import com.cryptium.coincurrently.core.networking.ResponseModels.AlertCoin
import com.cryptium.coincurrently.core.networking.ResponseModels.PostAlertCoin

object MainRepository {
    suspend fun getGlobalMarket() = ApiHelper.getGlobalMarket()
    suspend fun getGlobalChart(days: String) = ApiHelper.getGlobalChart(days)
    suspend fun getCoinsList() = ApiHelper.getCoinsList()
    suspend fun getToplist(currencyCode: String, order: SortOption, itemsPerPage: Int, platformId: String?) = ApiHelper.getToplist(currencyCode, order, itemsPerPage, platformId)
    suspend fun getExchangesToplist() = ApiHelper.getExchangesToplist()
    suspend fun getPriceVolume(id: String, currencyCode: String, days: String, interval: String?, freeUrl: Boolean) = ApiHelper.getPriceVolume(id, currencyCode, days, interval, freeUrl)
    suspend fun getCandleChart(id: String, currencyCode: String, days: String, freeUrl: Boolean) = ApiHelper.getCandlChart(id, currencyCode, days, freeUrl)
    suspend fun getCryptoDetailInfo(id: String, freeUrl: Boolean) = ApiHelper.getCryptoDetailInfo(id, freeUrl)
    suspend fun getExchangeVolume(id: String, days: String) = ApiHelper.getExchangeVolume(id, days)
    suspend fun getExchangeDetailInfo(id: String) = ApiHelper.getExchangeDetailInfo(id)
    suspend fun getMultipleCurrencies(ids: String, currencyCode: String) = ApiHelper.getMultipleCurrencies(ids, currencyCode)
    suspend fun getTrendingCoins() = ApiHelper.getTrendingCoins()
    suspend fun getCoinsWithAllCurrencies(ids: String) = ApiHelper.getCoinsWithAllCurrencies(ids)
    suspend fun getAllCategories(sortOption: SortOption) = ApiHelper.getAllCategories(sortOption = sortOption)
    suspend fun getAllDerivatives(sortOption: SortOption) = ApiHelper.getAllDerivatives(sortOption)
    suspend fun getPublicTreasury(
        entity: TreasuryEntity,
        coinId: String,
        page: Int = 1,
        perPage: Int = 250,
    ) = ApiHelper.getPublicTreasury(entity, coinId, page, perPage)
    suspend fun getTreasuryDetail(entityId: String) = ApiHelper.getTreasuryDetail(entityId)
    suspend fun getTreasuryHoldingChart(entityId: String, coinId: String, days: String) =
        ApiHelper.getTreasuryHoldingChart(entityId, coinId, days)
    suspend fun getTreasuryTransactionHistory(
        entityId: String,
        coinIds: String? = null,
        page: Int = 1,
        perPage: Int = 100,
    ) = ApiHelper.getTreasuryTransactionHistory(entityId, coinIds, page, perPage)
    suspend fun getTreasuryEntitiesList(
        entity: TreasuryEntity,
        page: Int = 1,
        perPage: Int = 250,
    ) = ApiHelper.getTreasuryEntitiesList(entity, page, perPage)
    suspend fun getDerivativeDetail(id: String) = ApiHelper.getDerivativeDetail(id)
    suspend fun getFearGreedModel() = ApiHelper.getFearGreedModel()
    suspend fun getSearchResults(query: String) = ApiHelper.getSearchResult(query)
    suspend fun getNewCoins() = ApiHelper.getNewCoins()
    suspend fun getNftToplist(order: SortOption, platformId: String? = null) = ApiHelper.getNftToplist(order, platformId)
    suspend fun getNftPlatforms() = ApiHelper.getNftPlatforms()
    suspend fun getNftPriceVolume(id: String, days: String) = ApiHelper.getNftPriceVolume(id, days)
    suspend fun getNftDetail(id: String) = ApiHelper.getNftDetail(id)
    suspend fun getNFTTickers(id: String) = ApiHelper.getNFTTickers(id)
    suspend fun getGasFees(includeHistory: Boolean) = ApiHelper.getGasFees(includeHistory)
    suspend fun getWalletAddress(address: String, blockChain: String) = ApiHelper.getWalletAddress(address, blockChain)
    suspend fun getWinnersLosers(currencyCode: String, duration: String) = ApiHelper.getWinnersLosers(currencyCode, duration)
    suspend fun getAlerts() = ApiHelper.getAlerts()
    suspend fun createPriceAlert(alertCoin: PostAlertCoin) = ApiHelper.createAlert(alertCoin)
    suspend fun deletePriceAlert(alertId: String) = ApiHelper.deleteAlert(alertId)
    suspend fun updateUser(token: String) = ApiHelper.updateUser(token)
    suspend fun triggerPriceAlert() = ApiHelper.triggerPriceAlert()
    suspend fun subBigMover() = ApiHelper.subBigMover()
    suspend fun ubsubBigMover() = ApiHelper.ubsubBigMovers()
    suspend fun bigMoverTrigger() = ApiHelper.bigMoverTrigger()
    suspend fun sendFeedback(rating: Int, feedback: String?) = ApiHelper.sendFeedback(rating, feedback)
    suspend fun getUser() = ApiHelper.getUser()
    suspend fun subCoin(coinIds: List<String>, currencies: List<String>) = ApiHelper.subCoin(coinIds, currencies)
}