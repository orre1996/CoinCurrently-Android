package com.cryptium.coincurrently.core.networking

import android.graphics.Bitmap
import com.cryptium.coincurrently.core.networking.ResponseModels.AlertCoin
import com.cryptium.coincurrently.core.networking.ResponseModels.AlertDirection
import com.cryptium.coincurrently.core.networking.ResponseModels.CoinsList
import com.cryptium.coincurrently.core.networking.ResponseModels.CryptoDetailInfoModel
import com.cryptium.coincurrently.core.networking.ResponseModels.Derivative
import com.cryptium.coincurrently.core.networking.ResponseModels.ExchangeResponse
import com.cryptium.coincurrently.core.networking.ResponseModels.GlobalMarketChartModel
import com.cryptium.coincurrently.core.networking.ResponseModels.GlobalMarketModel
import com.cryptium.coincurrently.core.networking.ResponseModels.NFTDetail
import com.cryptium.coincurrently.core.networking.ResponseModels.NFTPlatform
import com.cryptium.coincurrently.core.networking.ResponseModels.NFTTickerResponse
import com.cryptium.coincurrently.core.networking.ResponseModels.NFTToplist
import com.cryptium.coincurrently.core.networking.ResponseModels.NewNewsModel
import com.cryptium.coincurrently.core.networking.ResponseModels.PostAlertCoin
import com.cryptium.coincurrently.core.networking.ResponseModels.PriceVolumeModel
import com.cryptium.coincurrently.core.networking.ResponseModels.PublicTreasuryModel
import com.cryptium.coincurrently.core.networking.ResponseModels.ReferralCodeResult
import com.cryptium.coincurrently.core.networking.ResponseModels.SearchModel
import com.cryptium.coincurrently.core.networking.ResponseModels.Toplist
import com.cryptium.coincurrently.core.networking.ResponseModels.TreasuryDetailEntity
import com.cryptium.coincurrently.core.networking.ResponseModels.TreasuryEntityListItem
import com.cryptium.coincurrently.core.networking.ResponseModels.TreasuryHoldingChartModel
import com.cryptium.coincurrently.core.networking.ResponseModels.TreasuryTransactionHistoryModel
import com.cryptium.coincurrently.core.networking.ResponseModels.TrendingItems
import com.cryptium.coincurrently.core.networking.ResponseModels.UserResponse
import com.cryptium.coincurrently.core.networking.ResponseModels.WinnersLosers
import com.cryptium.coincurrently.sharedui.models.SortOption
import com.cryptium.coincurrently.testsupport.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.mockkObject
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class MainRepositoryTest {

    @get:Rule
    val mainDispatcher = MainDispatcherRule()

    @Before
    fun setUp() {
        mockkObject(ApiHelper)
    }

    @Test
    fun marketAndListCalls_delegateToApiHelper() = runTest {
        val market = Result.success(GlobalMarketModel())
        val chart = Result.success(GlobalMarketChartModel())
        val coins = Result.success(arrayListOf(CoinsList(id = "bitcoin")))
        val toplist = Result.success(arrayListOf(Toplist(id = "bitcoin")))
        val exchanges = Result.success(arrayListOf<com.cryptium.coincurrently.core.networking.ResponseModels.ExchangeList>())
        coEvery { ApiHelper.getGlobalMarket() } returns market
        coEvery { ApiHelper.getGlobalChart("7") } returns chart
        coEvery { ApiHelper.getCoinsList() } returns coins
        coEvery { ApiHelper.getToplist("usd", SortOption.MktCapDesc, 25, null) } returns toplist
        coEvery { ApiHelper.getExchangesToplist() } returns exchanges

        assertEquals(market, MainRepository.getGlobalMarket())
        assertEquals(chart, MainRepository.getGlobalChart("7"))
        assertEquals(coins, MainRepository.getCoinsList())
        assertEquals(toplist, MainRepository.getToplist("usd", SortOption.MktCapDesc, 25, null))
        assertEquals(exchanges, MainRepository.getExchangesToplist())
        coVerify { ApiHelper.getToplist("usd", SortOption.MktCapDesc, 25, null) }
    }

    @Test
    fun coinDetailCalls_delegateArguments() = runTest {
        val prices = Result.success(PriceVolumeModel())
        val candles = Result.success("[]")
        val detail = Result.success(CryptoDetailInfoModel(id = "bitcoin"))
        val multi = Result.success(arrayListOf(Toplist(id = "eth")))
        val allPrices = Result.success(mutableMapOf("bitcoin" to mapOf("usd" to 1.0)))
        coEvery { ApiHelper.getPriceVolume("bitcoin", "usd", "7", null, true) } returns prices
        coEvery { ApiHelper.getCandlChart("bitcoin", "usd", "1", false) } returns candles
        coEvery { ApiHelper.getCryptoDetailInfo("bitcoin", true) } returns detail
        coEvery { ApiHelper.getMultipleCurrencies("bitcoin,eth", "usd") } returns multi
        coEvery { ApiHelper.getCoinsWithAllCurrencies("bitcoin") } returns allPrices

        assertEquals(prices, MainRepository.getPriceVolume("bitcoin", "usd", "7", null, true))
        assertEquals(candles, MainRepository.getCandleChart("bitcoin", "usd", "1", false))
        assertEquals(detail, MainRepository.getCryptoDetailInfo("bitcoin", true))
        assertEquals(multi, MainRepository.getMultipleCurrencies("bitcoin,eth", "usd"))
        assertEquals(allPrices, MainRepository.getCoinsWithAllCurrencies("bitcoin"))
    }

    @Test
    fun exchangeDerivativeAndCategoryCalls_delegateArguments() = runTest {
        val volume = Result.success("[]")
        val exchange = Result.success(ExchangeResponse(id = "binance"))
        val categories = Result.success(arrayListOf<com.cryptium.coincurrently.core.networking.ResponseModels.CryptoCategory>())
        val derivatives = Result.success(listOf(Derivative(id = "okx")))
        val derivative = Result.success(Derivative(id = "okx"))
        coEvery { ApiHelper.getExchangeVolume("binance", "7") } returns volume
        coEvery { ApiHelper.getExchangeDetailInfo("binance") } returns exchange
        coEvery { ApiHelper.getAllCategories(SortOption.MktCapDesc) } returns categories
        coEvery { ApiHelper.getAllDerivatives(SortOption.MktCapDesc) } returns derivatives
        coEvery { ApiHelper.getDerivativeDetail("okx") } returns derivative

        assertEquals(volume, MainRepository.getExchangeVolume("binance", "7"))
        assertEquals(exchange, MainRepository.getExchangeDetailInfo("binance"))
        assertEquals(categories, MainRepository.getAllCategories(SortOption.MktCapDesc))
        assertEquals(derivatives, MainRepository.getAllDerivatives(SortOption.MktCapDesc))
        assertEquals(derivative, MainRepository.getDerivativeDetail("okx"))
    }

    @Test
    fun treasuryCalls_delegateArguments() = runTest {
        val treasury = Result.success(PublicTreasuryModel(total_holdings = 1.0))
        val detail = Result.success(TreasuryDetailEntity(id = "microstrategy"))
        val holding = Result.success(TreasuryHoldingChartModel())
        val history = Result.success(TreasuryTransactionHistoryModel())
        val entities = Result.success(listOf(TreasuryEntityListItem(id = "mstr", name = "Strategy")))
        coEvery { ApiHelper.getPublicTreasury(TreasuryEntity.Companies, "bitcoin", 2, 50) } returns treasury
        coEvery { ApiHelper.getTreasuryDetail("microstrategy") } returns detail
        coEvery { ApiHelper.getTreasuryHoldingChart("microstrategy", "bitcoin", "30") } returns holding
        coEvery { ApiHelper.getTreasuryTransactionHistory("microstrategy", "bitcoin", 1, 20) } returns history
        coEvery { ApiHelper.getTreasuryEntitiesList(TreasuryEntity.Governments, 1, 250) } returns entities

        assertEquals(treasury, MainRepository.getPublicTreasury(TreasuryEntity.Companies, "bitcoin", 2, 50))
        assertEquals(detail, MainRepository.getTreasuryDetail("microstrategy"))
        assertEquals(holding, MainRepository.getTreasuryHoldingChart("microstrategy", "bitcoin", "30"))
        assertEquals(history, MainRepository.getTreasuryTransactionHistory("microstrategy", "bitcoin", 1, 20))
        assertEquals(entities, MainRepository.getTreasuryEntitiesList(TreasuryEntity.Governments))
    }

    @Test
    fun discoveryCalls_delegateArguments() = runTest {
        val fear = Result.success(com.cryptium.coincurrently.core.networking.ResponseModels.FearGreed())
        val search = Result.success(SearchModel())
        val newCoins = Result.success(arrayListOf(CoinsList(id = "new")))
        val trending = Result.success(TrendingItems())
        val nfts = Result.success(arrayListOf(NFTToplist(id = "punks")))
        val platforms = Result.success(arrayListOf(NFTPlatform(id = "ethereum")))
        val nftChart = Result.success("{}")
        val nftDetail = Result.success(NFTDetail(id = "punks"))
        val tickers = Result.success(NFTTickerResponse())
        coEvery { ApiHelper.getFearGreedModel() } returns fear
        coEvery { ApiHelper.getSearchResult("btc") } returns search
        coEvery { ApiHelper.getNewCoins() } returns newCoins
        coEvery { ApiHelper.getTrendingCoins() } returns trending
        coEvery { ApiHelper.getNftToplist(SortOption.MktCapDesc, "ethereum") } returns nfts
        coEvery { ApiHelper.getNftPlatforms() } returns platforms
        coEvery { ApiHelper.getNftPriceVolume("punks", "7") } returns nftChart
        coEvery { ApiHelper.getNftDetail("punks") } returns nftDetail
        coEvery { ApiHelper.getNFTTickers("punks") } returns tickers

        assertEquals(fear, MainRepository.getFearGreedModel())
        assertEquals(search, MainRepository.getSearchResults("btc"))
        assertEquals(newCoins, MainRepository.getNewCoins())
        assertEquals(trending, MainRepository.getTrendingCoins())
        assertEquals(nfts, MainRepository.getNftToplist(SortOption.MktCapDesc, "ethereum"))
        assertEquals(platforms, MainRepository.getNftPlatforms())
        assertEquals(nftChart, MainRepository.getNftPriceVolume("punks", "7"))
        assertEquals(nftDetail, MainRepository.getNftDetail("punks"))
        assertEquals(tickers, MainRepository.getNFTTickers("punks"))
    }

    @Test
    fun newsAndAccountCalls_delegateArguments() = runTest {
        val gecko = Result.success(NewNewsModel())
        val news = Result.success(NewNewsModel())
        val gas = Result.success(com.cryptium.coincurrently.core.networking.ResponseModels.GasFeesModel(null))
        val winners = Result.success(WinnersLosers(emptyList(), emptyList()))
        val code = ReferralCodeResult(qrImage = mockk<Bitmap>(relaxed = true), code = "ABC")
        val user = mockk<UserResponse>()
        val alert = Result.success(mockk<AlertCoin>())
        val alerts = Result.success(arrayListOf<AlertCoin>())
        val post = PostAlertCoin("bitcoin", 1.0, AlertDirection.Above, null, "USD")
        coEvery { ApiHelper.getCoinGeckoNews("bitcoin") } returns gecko
        coEvery { ApiHelper.getNewNews(25, any()) } returns news
        coEvery { ApiHelper.getGasFees(true) } returns gas
        coEvery { ApiHelper.getWalletAddress("0xabc", "eth") } returns Result.success(mockk(relaxed = true))
        coEvery { ApiHelper.getWinnersLosers("usd", "24h") } returns winners
        coEvery { ApiHelper.getAlerts() } returns alerts
        coEvery { ApiHelper.createAlert(post) } returns alert
        coEvery { ApiHelper.deleteAlert("alert-1") } returns Result.success(Unit)
        coEvery { ApiHelper.updateUser("token") } returns Result.success(Unit)
        coEvery { ApiHelper.triggerPriceAlert() } returns Result.success(Unit)
        coEvery { ApiHelper.subBigMover() } returns Result.success(Unit)
        coEvery { ApiHelper.ubsubBigMovers() } returns Result.success(Unit)
        coEvery { ApiHelper.bigMoverTrigger() } returns Result.success(Unit)
        coEvery { ApiHelper.sendFeedback(5, "nice") } returns Result.success(Unit)
        coEvery { ApiHelper.getReferralCode() } returns code
        coEvery { ApiHelper.getUser() } returns user
        coEvery { ApiHelper.redeemCode("FRIEND") } returns Result.success(Unit)
        coEvery { ApiHelper.subCoin(listOf("bitcoin"), listOf("usd")) } returns Result.success(Unit)

        assertEquals(gecko, MainRepository.getCoinGeckoNews("bitcoin"))
        assertEquals(news, MainRepository.getNewNews(25, NewsCategory.Videos()))
        assertEquals(gas, MainRepository.getGasFees(true))
        MainRepository.getWalletAddress("0xabc", "eth")
        assertEquals(winners, MainRepository.getWinnersLosers("usd", "24h"))
        assertEquals(alerts, MainRepository.getAlerts())
        assertEquals(alert, MainRepository.createPriceAlert(post))
        assertEquals(Result.success(Unit), MainRepository.deletePriceAlert("alert-1"))
        assertEquals(Result.success(Unit), MainRepository.updateUser("token"))
        assertEquals(Result.success(Unit), MainRepository.triggerPriceAlert())
        assertEquals(Result.success(Unit), MainRepository.subBigMover())
        assertEquals(Result.success(Unit), MainRepository.ubsubBigMover())
        assertEquals(Result.success(Unit), MainRepository.bigMoverTrigger())
        assertEquals(Result.success(Unit), MainRepository.sendFeedback(5, "nice"))
        assertEquals(code, MainRepository.getReferralCode())
        assertEquals(user, MainRepository.getUser())
        assertEquals(Result.success(Unit), MainRepository.redeemCode("FRIEND"))
        assertEquals(Result.success(Unit), MainRepository.subCoin(listOf("bitcoin"), listOf("usd")))
        coVerify { ApiHelper.getWalletAddress("0xabc", "eth") }
        coVerify { ApiHelper.getNewNews(25, NewsCategory.Videos()) }
    }
}
