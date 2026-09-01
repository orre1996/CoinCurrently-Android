package com.cryptium.coincurrently.features.GlobalMarket
import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable


@Serializable
data class GlobalMarketModel(
    @SerializedName("data")
    var data: GlobalMarketData? = null
)

@Serializable
data class GlobalMarketData(
    @SerializedName("total_market_cap")
    var total_market_cap: Map<String, Double>? = null,
    @SerializedName("total_volume")
    var total_volume: Map<String, Double>? = null,
    @SerializedName("market_cap_percentage")
    var market_cap_percentage: Map<String, Double>? = null,
    @SerializedName("market_cap_change_percentage_24h_usd")
    var market_cap_change_percentage_24h_usd: Double? = null,
    var selectedCurrency: Double? = null,
    @SerializedName("markets")
    var markets: Int? = null,
    @SerializedName("active_cryptocurrencies")
    var active_cryptocurrencies: Int? = null,

    // Manually added
    var percentages: List<Percentages>? = listOf()
)

data class MarketCapPercentage(
    var btc: Double? = null
)

@Serializable
data class Percentages (
    val symbol: String?,
    var imageUrl: String?,
    val percentage: Double?,
    var color: Int?
)

@Serializable
data class Currency(
    var thePrice: Double? = null,
    var aed: Double? = thePrice,
    var aud: Double? = thePrice,
    var brl: Double? = thePrice,
    var btc: Double? = thePrice,
    var bch: Double? = thePrice,
    var bnb: Double? = thePrice,
    var bits: Double? = thePrice,
    var cad: Double? = thePrice,
    var chf: Double? = thePrice,
    var clp: Double? = thePrice,
    var cny: Double? = thePrice,
    var czk: Double? = thePrice,
    var dkk: Double? = thePrice,
    var dot: Double? = thePrice,
    var eth: Double? = thePrice,
    var eos: Double? = thePrice,
    var eur: Double? = thePrice,
    var gbp: Double? = thePrice,
    var hkd: Double? = thePrice,
    var huf: Double? = thePrice,
    var idr: Double? = thePrice,
    var ils: Double? = thePrice,
    var inr: Double? = thePrice,
    var jpy: Double? = thePrice,
    var krw: Double? = thePrice,
    var lkr: Double? = thePrice,
    var link: Double? = thePrice,
    var ltc: Double? = thePrice,
    var mxn: Double? = thePrice,
    var myr: Double? = thePrice,
    var nok: Double? = thePrice,
    var nzd: Double? = thePrice,
    var php: Double? = thePrice,
    var pln: Double? = thePrice,
    var pkr: Double? = thePrice,
    var rub: Double? = thePrice,
    var sek: Double? = thePrice,
    var sats: Double? = thePrice,
    var sgd: Double? = thePrice,
    var thb: Double? = thePrice,
    var `try`: Double? = thePrice,
    var uah: Double? = thePrice,
    var usd: Double? = thePrice,
    var vnd: Double? = thePrice,
    var xrp: Double? = thePrice,
    var xlm: Double? = thePrice,
    var zar: Double? = thePrice,
    var yfi: Double? = thePrice,
)

val Currency.allCurrencies: Map<String, Double>
    get() {
        return mapOf(
            "aed" to (aed ?: 0.0),
            "aud" to (aud ?: 0.0),
            "brl" to (brl ?: 0.0),
            "btc" to (btc ?: 0.0),
            "bits" to (bits ?: 0.0),
            "bnb" to (bnb ?: 0.0),
            "cad" to (cad ?: 0.0),
            "chf" to (chf ?: 0.0),
            "clp" to (clp ?: 0.0),
            "cny" to (cny ?: 0.0),
            "czk" to (czk ?: 0.0),
            "dkk" to (dkk ?: 0.0),
            "eth" to (eth ?: 0.0),
            "eur" to (eur ?: 0.0),
            "gbp" to (gbp ?: 0.0),
            "hkd" to (hkd ?: 0.0),
            "huf" to (huf ?: 0.0),
            "idr" to (idr ?: 0.0),
            "ils" to (ils ?: 0.0),
            "inr" to (inr ?: 0.0),
            "jpy" to (jpy ?: 0.0),
            "krw" to (krw ?: 0.0),
            "lkr" to (lkr ?: 0.0),
            "ltc" to (ltc ?: 0.0),
            "mxn" to (mxn ?: 0.0),
            "myr" to (myr ?: 0.0),
            "nok" to (nok ?: 0.0),
            "nzd" to (nzd ?: 0.0),
            "php" to (php ?: 0.0),
            "pln" to (pln ?: 0.0),
            "pkr" to (pkr ?: 0.0),
            "rub" to (rub ?: 0.0),
            "sek" to (sek ?: 0.0),
            "sat" to (sats ?: 0.0),
            "sgd" to (sgd ?: 0.0),
            "thb" to (thb ?: 0.0),
            "try" to (`try` ?: 0.0),
            "uah" to (uah ?: 0.0),
            "usd" to (usd ?: 0.0),
            "vnd" to (vnd ?: 0.0),
            "xrp" to (xrp ?: 0.0),
            "zar" to (zar ?: 0.0)
        )
    }