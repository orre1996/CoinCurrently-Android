package com.cryptium.coincurrently.ui.main.Managers

import com.cryptium.coincurrently.ui.main.MarketTab.CryptoDetail.Components.Markets.DetailMarketsItem
import networking.ResponseModels.ConvertedLastString
import networking.ResponseModels.DerivativeDetailModel
import networking.ResponseModels.ExchangeList
import networking.ResponseModels.NewNewsItem
import networking.ResponseModels.Toplist

object SecretMessageHelper {
    const val SECRET_SYMBOL = "Secret message"
    const val SECRET_TITLE = "You found the secret message. Click here to join the raffle!"

    const val PLACEMENT_NEWS_VIDEOS = "1"
    const val PLACEMENT_EXCHANGES = "2"
    const val PLACEMENT_CATEGORY_TOPLIST = "3"
    const val PLACEMENT_DERIVATIVE_FUTURES = "4"
    const val PLACEMENT_CRYPTO_DETAIL_NEWS = "5"
    const val PLACEMENT_TRENDING = "6"
    const val PLACEMENT_HOME_WINNERS = "7"
    const val PLACEMENT_CRYPTO_DETAIL_MARKETS = "8"

    fun shouldShowSecret(placement: String): Boolean {
        val secret = FirebaseManager.getHiddenSecretSync() ?: return false
        return secret.secretNumber == placement
    }

    fun isSecretToplist(coin: Toplist): Boolean = coin.symbol == SECRET_SYMBOL

    fun isSecretExchange(exchange: ExchangeList): Boolean =
        exchange.name?.contains(SECRET_TITLE) == true

    fun isSecretDerivative(derivative: DerivativeDetailModel): Boolean =
        derivative.symbol == SECRET_TITLE

    fun isSecretNewsItem(item: NewNewsItem): Boolean =
        item.title?.contains(SECRET_TITLE) == true

    fun isSecretMarketsItem(item: DetailMarketsItem): Boolean =
        item.name == SECRET_SYMBOL

    fun secretImageUrl(secret: HiddenSecret? = FirebaseManager.getHiddenSecretSync()): String? =
        secret?.secretImage?.takeIf { it.isNotBlank() }

    fun secretUrlForToplist(coin: Toplist): String? =
        FirebaseManager.getHiddenSecretSync()?.secretUrl?.takeIf { it.isNotBlank() }
            ?: coin.id?.takeIf { it.isNotBlank() }

    fun secretToplistUrl(coin: Toplist): String? {
        if (!isSecretToplist(coin)) return null
        return secretUrlForToplist(coin)
    }

    fun handleSecretToplistClick(coin: Toplist, openUrl: (String) -> Unit): Boolean {
        if (!isSecretToplist(coin)) return false
        secretUrlForToplist(coin)?.let(openUrl)
        return true
    }

    fun handleSecretExchangeClick(exchange: ExchangeList, openUrl: (String) -> Unit): Boolean {
        if (!isSecretExchange(exchange)) return false
        exchange.url?.let(openUrl)
        return true
    }

    fun handleSecretDerivativeClick(
        derivative: DerivativeDetailModel,
        openUrl: (String) -> Unit,
    ): Boolean {
        if (!isSecretDerivative(derivative)) return false
        derivative.trade_url?.let(openUrl)
        return true
    }

    fun <T> insertAtIndex(list: List<T>, index: Int, item: T): List<T> {
        val mutable = list.toMutableList()
        mutable.add(minOf(index, mutable.size), item)
        return mutable
    }

    fun withSecretToplist(list: List<Toplist>, placement: String, index: Int): List<Toplist> {
        if (!shouldShowSecret(placement)) return list
        val secret = FirebaseManager.getHiddenSecretSync() ?: return list
        val withoutSecret = list.filterNot { isSecretToplist(it) }
        return insertAtIndex(withoutSecret, minOf(index, withoutSecret.size), secretToplist(secret))
    }

    fun withSecretNews(list: List<NewNewsItem>, placement: String, index: Int): List<NewNewsItem> {
        if (!shouldShowSecret(placement)) return list
        val secret = FirebaseManager.getHiddenSecretSync() ?: return list
        val withoutSecret = list.filterNot { isSecretNewsItem(it) }
        return insertAtIndex(withoutSecret, minOf(index, withoutSecret.size), secretNewsItem(secret))
    }

    fun withSecretExchange(list: List<ExchangeList>, placement: String, index: Int): List<ExchangeList> {
        if (!shouldShowSecret(placement)) return list
        val secret = FirebaseManager.getHiddenSecretSync() ?: return list
        val withoutSecret = list.filterNot { isSecretExchange(it) }
        return insertAtIndex(withoutSecret, minOf(index, withoutSecret.size), secretExchange(secret))
    }

    fun withSecretDerivative(
        list: List<DerivativeDetailModel>,
        placement: String,
        index: Int,
    ): List<DerivativeDetailModel> {
        if (!shouldShowSecret(placement)) return list
        val secret = FirebaseManager.getHiddenSecretSync() ?: return list
        val withoutSecret = list.filterNot { isSecretDerivative(it) }
        return insertAtIndex(withoutSecret, minOf(index, withoutSecret.size), secretDerivative(secret))
    }

    fun withSecretMarketsItem(
        list: List<DetailMarketsItem>,
        placement: String,
        index: Int,
    ): List<DetailMarketsItem> {
        if (!shouldShowSecret(placement)) return list
        val secret = FirebaseManager.getHiddenSecretSync() ?: return list
        val withoutSecret = list.filterNot { isSecretMarketsItem(it) }
        return insertAtIndex(withoutSecret, minOf(index, withoutSecret.size), secretMarketsItem(secret))
    }

    fun secretToplist(secret: HiddenSecret): Toplist = Toplist(
        id = secret.secretUrl,
        symbol = SECRET_SYMBOL,
        name = "CoinCurrently",
        image = secretImageUrl(secret),
        current_price = 123.45,
        market_cap_rank = 0,
        price_change_percentage_1h_in_currency = 99.99,
        price_change_percentage_24h_in_currency = 99.99,
        price_change_percentage_7d_in_currency = 99.99,
        price_change_percentage_30d_in_currency = 99.99,
        market_cap = 123450.0,
    )

    fun secretNewsItem(secret: HiddenSecret): NewNewsItem = NewNewsItem(
        news_url = secret.secretUrl,
        image_url = secretImageUrl(secret),
        title = SECRET_TITLE,
        source_name = "CoinCurrently",
        date = "Thu, 30 Jul 2026 15:24:35 -0400",
        sentiment = "Positive",
    )

    fun secretExchange(secret: HiddenSecret): ExchangeList = ExchangeList(
        id = secret.secretUrl,
        name = SECRET_TITLE,
        url = secret.secretUrl,
        image = secretImageUrl(secret),
        trust_score = 10,
        trade_volume_24h_btc = 1.0,
        year_established = null,
        country = null,
        has_trading_incentive = null,
        trust_score_rank = null,
        trade_volume_24h_btc_normalized = null,
    )

    fun secretDerivative(secret: HiddenSecret): DerivativeDetailModel = DerivativeDetailModel(
        symbol = SECRET_TITLE,
        trade_url = secret.secretUrl,
        open_interest_usd = 12345.0,
        converted_last = ConvertedLastString(btc = "12345", eth = "12345", usd = "12345"),
    )

    fun secretMarketsItem(secret: HiddenSecret): DetailMarketsItem = DetailMarketsItem(
        name = SECRET_SYMBOL,
        imageUrl = secretImageUrl(secret),
        tradeUrl = secret.secretUrl,
        price = "12345",
        volume = "12345",
    )
}
