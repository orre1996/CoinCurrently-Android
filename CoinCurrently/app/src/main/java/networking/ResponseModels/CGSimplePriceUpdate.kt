package networking.ResponseModels

import com.cryptium.coincurrently.ui.main.HomeTab.GlobalMarket.DataClasses.Currency
import com.google.gson.annotations.SerializedName

/**
 * CoinGecko CGSimplePrice websocket payload.
 * https://docs.coingecko.com/websocket/cgsimpleprice
 */
data class CGSimplePriceUpdate(
    @SerializedName("c") val channelType: String? = null,
    @SerializedName("i") val coinId: String? = null,
    @SerializedName("vs") val vsCurrency: String? = null,
    @SerializedName("p") val price: Double? = null,
    @SerializedName("pp") val priceChangePercentage24h: Double? = null,
    @SerializedName("m") val marketCap: Double? = null,
    @SerializedName("v") val volume24h: Double? = null,
    @SerializedName("t") val lastUpdatedAt: Int? = null,
) {
    fun apply(to: CryptoDetailInfoModel): CryptoDetailInfoModel {
        val updateCoinId = coinId ?: return to
        val currencyCode = vsCurrency?.lowercase() ?: return to

        val updatedId = to.id?.takeIf { it.isNotEmpty() } ?: updateCoinId
        val marketData = to.market_data ?: MarketData()

        val updatedMarketData = marketData.copy(
            current_price = price?.let {
                (marketData.current_price ?: Currency()).withValue(currencyCode, it)
            } ?: marketData.current_price,
            market_cap = marketCap?.let {
                (marketData.market_cap ?: Currency()).withValue(currencyCode, it)
            } ?: marketData.market_cap,
            total_volume = volume24h?.let {
                (marketData.total_volume ?: Currency()).withValue(currencyCode, it)
            } ?: marketData.total_volume,
            price_change_percentage_24h = priceChangePercentage24h ?: marketData.price_change_percentage_24h,
        )

        return to.copy(
            id = updatedId,
            market_data = updatedMarketData,
        )
    }
}

fun Currency.withValue(currencyCode: String, value: Double): Currency {
    return when (currencyCode.lowercase()) {
        "aed" -> copy(aed = value, thePrice = value)
        "aud" -> copy(aud = value, thePrice = value)
        "brl" -> copy(brl = value, thePrice = value)
        "btc" -> copy(btc = value, thePrice = value)
        "bch" -> copy(bch = value, thePrice = value)
        "bits" -> copy(bits = value, thePrice = value)
        "bnb" -> copy(bnb = value, thePrice = value)
        "cad" -> copy(cad = value, thePrice = value)
        "chf" -> copy(chf = value, thePrice = value)
        "clp" -> copy(clp = value, thePrice = value)
        "cny" -> copy(cny = value, thePrice = value)
        "czk" -> copy(czk = value, thePrice = value)
        "dkk" -> copy(dkk = value, thePrice = value)
        "dot" -> copy(dot = value, thePrice = value)
        "eth" -> copy(eth = value, thePrice = value)
        "eur" -> copy(eur = value, thePrice = value)
        "eos" -> copy(eos = value, thePrice = value)
        "gbp" -> copy(gbp = value, thePrice = value)
        "hkd" -> copy(hkd = value, thePrice = value)
        "huf" -> copy(huf = value, thePrice = value)
        "idr" -> copy(idr = value, thePrice = value)
        "ils" -> copy(ils = value, thePrice = value)
        "inr" -> copy(inr = value, thePrice = value)
        "jpy" -> copy(jpy = value, thePrice = value)
        "krw" -> copy(krw = value, thePrice = value)
        "lkr" -> copy(lkr = value, thePrice = value)
        "link" -> copy(link = value, thePrice = value)
        "ltc" -> copy(ltc = value, thePrice = value)
        "mxn" -> copy(mxn = value, thePrice = value)
        "myr" -> copy(myr = value, thePrice = value)
        "nok" -> copy(nok = value, thePrice = value)
        "nzd" -> copy(nzd = value, thePrice = value)
        "php" -> copy(php = value, thePrice = value)
        "pkr" -> copy(pkr = value, thePrice = value)
        "pln" -> copy(pln = value, thePrice = value)
        "rub" -> copy(rub = value, thePrice = value)
        "sek" -> copy(sek = value, thePrice = value)
        "sats" -> copy(sats = value, thePrice = value)
        "sgd" -> copy(sgd = value, thePrice = value)
        "thb" -> copy(thb = value, thePrice = value)
        "try" -> copy(`try` = value, thePrice = value)
        "uah" -> copy(uah = value, thePrice = value)
        "usd" -> copy(usd = value, thePrice = value)
        "vnd" -> copy(vnd = value, thePrice = value)
        "xrp" -> copy(xrp = value, thePrice = value)
        "xlm" -> copy(xlm = value, thePrice = value)
        "zar" -> copy(zar = value, thePrice = value)
        "yfi" -> copy(yfi = value, thePrice = value)
        else -> copy(usd = value, thePrice = value)
    }
}
