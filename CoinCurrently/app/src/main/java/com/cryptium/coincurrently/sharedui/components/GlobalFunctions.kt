package com.cryptium.coincurrently.sharedui.components
import com.cryptium.coincurrently.app.NavigationScreens
import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import com.cryptium.coincurrently.core.extensions.getAllTransactions
import com.cryptium.coincurrently.core.extensions.getAverageCost
import com.cryptium.coincurrently.core.extensions.getProfitPercent
import com.cryptium.coincurrently.core.extensions.getProfitValue
import com.cryptium.coincurrently.core.extensions.totalBuyAmount
import com.cryptium.coincurrently.features.Portfolio.DataClasses.NewTransaction
import com.cryptium.coincurrently.core.purchases.StoreKitManager
import com.cryptium.coincurrently.core.config.AppLocale
import com.google.gson.Gson
import com.cryptium.coincurrently.core.networking.ResponseModels.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.min
import kotlin.random.Random

enum class SortOptions(value: String) {
    MktCapDesc("Market Cap (Rank) Desc"),
    MktCapAsc("Market Cap (Rank) Asc"),
    PriceDesc("Price Desc"),
    PriceAsc("Price Asc"),
    VolumeDesc("Volume Desc"),
    VolumeAsc("Volume Asc"),
    DailyWinners("Daily Winners"),
    DailyLosers("Daily Losers"),
    WeeklyWinners("Weekly Winners"),
    WeeklyLosers("Weekly Losers"),
    MonthlyWinners("Monthly Winners"),
    MonthlyLosers("Monthly Losers"),
    TotalSupplyDesc("Total Supply Desc"),
    TotalSupplyAsc("Total Supply Asc"),
    FloorChangeDesc("Floor Price Change Desc"),
    FloorChangeAsc("Floor Price Change Asc"),
}

enum class PreferredTimeframe(value: Int) {
    DAILY(0),
    WEEKLY(1),
    MONTHLY(2);
}

enum class AppColorScheme(val displayName: String) {
    LIGHT("Light Mode"),
    DARK("Dark Mode"),
    SYSTEM("System");

    fun resolvedColorScheme(isSystemDark: Boolean): Boolean =
        when (this) {
            LIGHT -> false
            DARK -> true
            SYSTEM -> isSystemDark
        }

    val preferredScheme: Boolean?
        get() = when (this) {
            SYSTEM -> null
            LIGHT -> false
            DARK -> true
        }
}

enum class SelectedStartTab {
    LastUsed,
    Home,
    Portfolio,
    Market,
    News,
    More;

    fun getName(): String {
        return when(this) {
            LastUsed -> "Last used"
            Home -> "Home"
            Portfolio -> "Portfolio"
            Market -> "Market"
            News -> "News"
            else -> "Settings"

        }
    }
}

class GlobalFunctions {
    companion object {

        private var preferences: SharedPreferences? = null

        fun initSharedPreferences(context: Context) {
            preferences = context.getSharedPreferences("push_notification_preferences_file",0)
        }

        fun getStoredWhatsNewVersion(): String? =
            preferences?.getString("stored_whats_new_version", null)

        fun setStoredWhatsNewVersion(version: String) {
            preferences?.edit()?.putString("stored_whats_new_version", version)?.apply()
        }

        fun getIsEligableForTrial(): Boolean {
            val candlestickEnabled = preferences?.getBoolean("ssEligableForTrial", true)
            return candlestickEnabled ?: true
        }

        fun setIsEligableForTrial(isEligable: Boolean) {
            val prefsEditor: SharedPreferences.Editor? = preferences?.edit()
            prefsEditor?.putBoolean("ssEligableForTrial", isEligable)
            prefsEditor?.apply()
        }
        fun getHasBoughtPro(): String? {
            val candlestickEnabled = preferences?.getString("hasBoughtPro-1", null)
            return candlestickEnabled
        }

        fun setHasBoughtPro(enable: String?) {
            val prefsEditor: SharedPreferences.Editor? = preferences?.edit()
            prefsEditor?.putString("hasBoughtPro-1", enable)
            prefsEditor?.apply()
        }

        fun getIsBiometricAuthenticationEnabled(): Boolean {
            return preferences?.getBoolean("biometricAuthenticationEnabled", false) ?: false
        }

        fun setIsBiometricAuthenticationEnabled(enabled: Boolean) {
            val prefsEditor: SharedPreferences.Editor? = preferences?.edit()
            prefsEditor?.putBoolean("biometricAuthenticationEnabled", enabled)
            prefsEditor?.apply()
        }

        fun getPreferredTimeframe(): PreferredTimeframe {
            val preferredTimeframe = preferences?.getInt("preferredTimeframe", 1) ?: 1
            return PreferredTimeframe.entries[preferredTimeframe]
        }

        fun setIsHidePortfolioEnabled(enabled: Boolean) {
            val prefsEditor: SharedPreferences.Editor? = preferences?.edit()
            prefsEditor?.putBoolean("hidePortfolioEnabled", enabled)
            prefsEditor?.apply()
        }

        fun getIsHidePortfolioEnabled(): Boolean {
            return preferences?.getBoolean("hidePortfolioEnabled", false) ?: false
        }

        fun setStartTab(tab: SelectedStartTab) {
            val prefsEditor: SharedPreferences.Editor? = preferences?.edit()
            prefsEditor?.putInt("startTab", tab.ordinal)
            prefsEditor?.apply()
        }

        fun getStartTab(): SelectedStartTab {
            val value = preferences?.getInt("startTab", 1) ?: 1
            return SelectedStartTab.entries[value]
        }


        fun setColorBlind(enabled: Boolean) {
            val prefsEditor: SharedPreferences.Editor? = preferences?.edit()
            prefsEditor?.putBoolean("setColorBlind", enabled)
            prefsEditor?.apply()
        }

        fun getColorBlind(): Boolean {
            val value = preferences?.getBoolean("setColorBlind", false) ?: false
            return value
        }

        fun setWebSocketEnabled(enabled: Boolean) {
            preferences?.edit()?.putBoolean("webSocketEnabled", enabled)?.apply()
        }

        fun getWebSocketEnabledSetting(): Boolean? {
            return if (preferences?.contains("webSocketEnabled") == true) {
                preferences?.getBoolean("webSocketEnabled", false)
            } else {
                null
            }
        }

        fun isWebSocketEnabled(hasPro: Boolean): Boolean {
            return getWebSocketEnabledSetting() ?: hasPro
        }

        fun setHidePortfolioData(hide: Boolean) {
            val prefsEditor: SharedPreferences.Editor? = preferences?.edit()
            prefsEditor?.putBoolean("hidePortfolioData", hide)
            prefsEditor?.apply()
        }

        fun getHidePortfolioData(): Boolean {
            return preferences?.getBoolean("hidePortfolioData", false) ?: false
        }

        fun setPortfolioPieChartShowing(showing: Boolean) {
            val prefsEditor: SharedPreferences.Editor? = preferences?.edit()
            prefsEditor?.putBoolean("isPortfolioPieChartShowing", showing)
            prefsEditor?.apply()
        }

        fun getPortfolioPieChartShowing(): Boolean {
            return preferences?.getBoolean("isPortfolioPieChartShowing", false) ?: false
        }

        fun getPortfolioExportCount(): Int {
            return preferences?.getInt("portfolioExportCount", 0) ?: 0
        }

        fun incrementPortfolioExportCount() {
            val prefsEditor: SharedPreferences.Editor? = preferences?.edit()
            prefsEditor?.putInt("portfolioExportCount", getPortfolioExportCount() + 1)
            prefsEditor?.apply()
        }

        fun setLatestTab(page: Int) {
            val prefsEditor: SharedPreferences.Editor? = preferences?.edit()
            prefsEditor?.putInt("latestTab", page)
            prefsEditor?.apply()
        }

        fun getLatestTab(): NavigationScreens {
            val tabIndex = preferences?.getInt("latestTab", 0) ?: 0

            return when (tabIndex) {
                0 -> NavigationScreens.Home
                1 -> NavigationScreens.Portfolio
                2 -> NavigationScreens.Market
                3 -> NavigationScreens.News
                else -> NavigationScreens.Settings
            }
        }

        fun setPreferredTimeframe(timeframe: PreferredTimeframe) {
            val prefsEditor: SharedPreferences.Editor? = preferences?.edit()
            prefsEditor?.putInt("preferredTimeframe", timeframe.ordinal)
            prefsEditor?.apply()
        }

        fun getSelectedSortOption(): SortOptions {
            val preferredTimeframe = preferences?.getInt("SelectedSortOption", 1) ?: 1
            return SortOptions.entries[preferredTimeframe]
        }

        fun setSelectedSortOption(sortOption: SortOptions) {
            val prefsEditor: SharedPreferences.Editor? = preferences?.edit()
            prefsEditor?.putInt("SelectedSortOption", sortOption.ordinal)
            prefsEditor?.apply()
        }

        fun getShowChangesInPercent(): Boolean {
            return preferences?.getBoolean("showChangesInPercent", true) ?: true
        }

        fun setShowChangesInPercent(inPercent: Boolean) {
            val prefsEditor: SharedPreferences.Editor? = preferences?.edit()
            prefsEditor?.putBoolean("showChangesInPercent", inPercent)
            prefsEditor?.apply()
        }

        fun setSelectedCurrency(currency: CurrencySelectionModel?) {
            val prefsEditor: SharedPreferences.Editor? = preferences?.edit()
            val gson = Gson()
            val json = gson.toJson(currency)

            prefsEditor?.putString("savedCurrency", json)
            prefsEditor?.apply()
        }

        fun getSelectedCurrency(context: Context? = null): CurrencySelectionModel {
            val gson = Gson()

            if (preferences == null && context != null) {
                preferences = context.getSharedPreferences("push_notification_preferences_file",0)
            }

            try {
                val json: String = preferences?.getString("savedCurrency", "") ?: "savedCurrency"

                if (json.isEmpty()) {
                    return CurrencySelectionModel()
                }

                return gson.fromJson(json, CurrencySelectionModel::class.java)
            } catch (e: Exception) {
                return CurrencySelectionModel()
            }
        }

        fun setPortfolioSelectedCurrency(currency: CurrencySelectionModel?) {
            val prefsEditor: SharedPreferences.Editor? = preferences?.edit()
            val gson = Gson()
            val json = gson.toJson(currency)

            prefsEditor?.putString("savedCurrencyPortfolio", json)
            prefsEditor?.apply()
        }

        fun getPortfolioSelectedCurrency(context: Context? = null): CurrencySelectionModel? {
            val gson = Gson()

            if (preferences == null && context != null) {
                preferences = context.getSharedPreferences("push_notification_preferences_file",0)
            }

            try {
                val json: String = preferences?.getString("savedCurrencyPortfolio", "") ?: "savedCurrency"

                if (json.isEmpty()) {
                    return CurrencySelectionModel()
                }

                return gson.fromJson(json, CurrencySelectionModel::class.java)
            } catch (e: Exception) {
                return CurrencySelectionModel()
            }
        }

        fun setWidgetCurrency(ids: List<Toplist>) {
            val prefsEditor: SharedPreferences.Editor? = preferences?.edit()

            val newList: ArrayList<Toplist> = arrayListOf()
            for (i in 0 until min(8, ids.size)) {
                newList.add(ids[i])
            }

            val jsonText = Gson().toJson(SavedCurrencies(newList))
            prefsEditor?.putString("widgetCurrencies", jsonText)
            prefsEditor?.apply()
        }

        fun getWidgetCurrency(context: Context? = null): List<Toplist> {
            if (preferences == null && context != null) {
                preferences = context.getSharedPreferences("push_notification_preferences_file",0)
            }

            val currency = preferences?.getString("widgetCurrencies", "") ?: return arrayListOf()

            return try {
                val currencyArray = Gson().fromJson(currency, SavedCurrencies::class.java)
                val list = currencyArray?.currencies.orEmpty()
                list.subList(0, min(8, list.size))
            } catch (t: Throwable) {
                arrayListOf()
            }
        }

        fun setPortfolioCurrency(portfolio: List<PortfolioResponse>) {
            val prefsEditor: SharedPreferences.Editor? = preferences?.edit()

            val list = arrayListOf<PortfolioResponse>()

            portfolio.forEach {
                if (!list.contains(it)) {
                    list.add(it)
                }
            }

            val jsonText = Gson().toJson(SavedPortfolio(list))
            prefsEditor?.putString("portfolioModelCurrencies", jsonText)
            prefsEditor?.apply()
        }

        fun getPortfolioCurrencies(context: Context? = null): List<PortfolioResponse> {
            if (preferences == null && context != null) {
                preferences = context.getSharedPreferences("push_notification_preferences_file",0)
            }

            val currency = preferences?.getString("portfolioModelCurrencies", null)

            return try {
                val currencyArray = Gson().fromJson(currency, SavedPortfolio::class.java)
                currencyArray?.portfolio.orEmpty()
            } catch (exception: Exception){
                listOf()
            }
        }

        fun setFavorites(favorites: List<String>) {
            val prefsEditor: SharedPreferences.Editor? = preferences?.edit()

            val test = arrayListOf<String>()

            favorites.forEach {
                if (!test.contains(it)) {
                    test.add(it)
                }
            }

            val jsonText = Gson().toJson(FavoriteCurrencies(test))
            prefsEditor?.putString("favorites", jsonText)
            prefsEditor?.apply()
        }

        fun getFavorites(): List<String> {
            val currency = preferences?.getString("favorites", null) ?: return emptyList()

            return try {
                // Gson ignores Kotlin defaults and can yield a null wrapper or null list.
                val currencyArray = Gson().fromJson(currency, FavoriteCurrencies::class.java)
                currencyArray?.currencies.orEmpty()
            } catch (t: Throwable) {
                emptyList()
            }
        }

        fun getRatingDate(): String? {
            return preferences?.getString("ratingDate", null)
        }

        fun setRatingDate(inDays: Int) {
            val calendar = Calendar.getInstance(AppLocale.enUsPosix)

            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", AppLocale.enUsPosix)
            calendar.add(Calendar.DAY_OF_YEAR, inDays)
            val nextTwoWeeks = dateFormat.format(calendar.time)

            val prefsEditor: SharedPreferences.Editor? = preferences?.edit()
            prefsEditor?.putString("ratingDate", nextTwoWeeks)
            prefsEditor?.apply()
        }

        fun getPortfolioHintDate(): String? {
            return preferences?.getString("portfolioHintDate", null)
        }

        fun setPortfolioHintDate(inDays: Int) {
            val calendar = Calendar.getInstance(AppLocale.enUsPosix)

            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", AppLocale.enUsPosix)
            calendar.add(Calendar.DAY_OF_YEAR, inDays)
            val nextTwoWeeks = dateFormat.format(calendar.time)

            val prefsEditor: SharedPreferences.Editor? = preferences?.edit()
            prefsEditor?.putString("portfolioHintDate", nextTwoWeeks)
            prefsEditor?.apply()
        }

        fun getMoreHintDate(): String? {
            return preferences?.getString("moreHintDate", null)
        }

        fun setMoreHintDate(inDays: Int) {
            val calendar = Calendar.getInstance(AppLocale.enUsPosix)

            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", AppLocale.enUsPosix)
            calendar.add(Calendar.DAY_OF_YEAR, inDays)
            val nextTwoWeeks = dateFormat.format(calendar.time)

            val prefsEditor: SharedPreferences.Editor? = preferences?.edit()
            prefsEditor?.putString("moreHintDate", nextTwoWeeks)
            prefsEditor?.apply()
        }

        fun getLastLoginCollection(): Int? {
            return preferences?.getInt("lastLoginCollection", -1)
        }

        fun setLastLoginCollection(collection: Int) {
            val prefsEditor: SharedPreferences.Editor? = preferences?.edit()
            prefsEditor?.putInt("lastLoginCollection", collection)
            prefsEditor?.apply()
        }

        fun getIsWidgetDarkModeEnabled(): Boolean {
            val isWidgetDarkModeEnabled = preferences?.getBoolean("isWidgetDarkModeEnabled", true)
            return isWidgetDarkModeEnabled ?: true
        }

        fun setIsWidgetDarkModeEnabled(enabled: Boolean) {
            val prefsEditor: SharedPreferences.Editor? = preferences?.edit()
            prefsEditor?.putBoolean("isWidgetDarkModeEnabled", enabled)
            prefsEditor?.apply()
        }

        fun getPortfolioProfitLossValue(
            portfolio: List<Pie>?,
            coinPrices: MutableMap<String, Map<String, Double>>?,
            showCurrency: String = getSelectedCurrency().code
        ): Double? {
            val portfolioList = portfolio ?: return null
            if (portfolioList.isEmpty() || coinPrices.isNullOrEmpty()) return 0.0

            var profitLoss = 0.0
            var hasEntryPrices = false

            for (currency in portfolioList) {
                val coin = portfolio.getAllTransactions(currency.data?.id)
                val prices = coinPrices.get(currency.data?.id)

                val profit = getProfitLossOfCoin(coin, prices, showCurrency)
                if (profit != null) {
                    profitLoss += profit
                    hasEntryPrices = true
                }
            }

            return if (hasEntryPrices) profitLoss else 0.0
        }

        fun getProfitLossOfCoin(
            transactions: List<NewTransaction>?,
            coinPrices: Map<String, Double>?,
            showCurrency: String = getSelectedCurrency().code
        ): Double? {
            return transactions?.getProfitValue(coinPrices, showCurrency)
        }

        fun getPortfolioProfitLossPercent(
            portfolio: List<Pie>?,
            coinPrices: MutableMap<String, Map<String, Double>>?
        ): Double {
            if (portfolio.isNullOrEmpty() || coinPrices.isNullOrEmpty()) {
                return 0.0
            }

            var totalProfit = 0.0
            var totalSpent = 0.0

            for (pie in portfolio) {
                val coin = portfolio.getAllTransactions(pie.data?.id)
                val prices = coinPrices.get(pie.data?.id)

                val profitPercent = coin.getProfitPercent(prices)
                if (profitPercent != null) {
                    totalProfit += coin.getProfitValue(prices, withTransfers = false) ?: 0.0
                    totalSpent += (coin.totalBuyAmount) * (coin.getAverageCost(prices) ?: 0.0)
                }
            }

            val percent = totalProfit / totalSpent
            return if (percent.isNaN() || percent.isInfinite()) 0.0 else percent * 100
        }

        fun getPreferredTimeframeTitle(): String {
            return when (getPreferredTimeframe()) {
                PreferredTimeframe.DAILY -> "Daily"
                PreferredTimeframe.WEEKLY -> "Weekly"
                PreferredTimeframe.MONTHLY -> "Monthly"
            }
        }

        fun getPreferredTimeframeChangeTitle(): String {
            return when (getPreferredTimeframe()) {
                PreferredTimeframe.DAILY -> "24h:"
                PreferredTimeframe.WEEKLY -> "7d:"
                PreferredTimeframe.MONTHLY -> "30d:"
            }
        }

        fun getPreferredTimeframeIndex(fromCryptoDetail: Boolean = false): Int {
            return when (getPreferredTimeframe()) {
                PreferredTimeframe.DAILY -> if (fromCryptoDetail) 4 else 0
                PreferredTimeframe.WEEKLY -> if (fromCryptoDetail) 5 else 1
                PreferredTimeframe.MONTHLY -> if (fromCryptoDetail) 6 else 2
            }
        }

        fun getPreferredTimeframeIndexRaw(): String {
            return when (getPreferredTimeframe()) {
                PreferredTimeframe.DAILY -> "1"
                PreferredTimeframe.WEEKLY -> "7"
                PreferredTimeframe.MONTHLY -> "30"
            }
        }

        fun getPreferredTimeframeValue(): TimeCharts {
            return when (getPreferredTimeframe()) {
                PreferredTimeframe.DAILY -> TimeCharts.aDay
                PreferredTimeframe.WEEKLY -> TimeCharts.aWeek
                PreferredTimeframe.MONTHLY -> TimeCharts.aMonth
            }
        }

        fun preferredTimeFrameTextChange(): String {
            return when (getPreferredTimeframe()) {
                PreferredTimeframe.DAILY -> "24h"
                PreferredTimeframe.WEEKLY -> "7d"
                PreferredTimeframe.MONTHLY -> "30d"
            }
        }
    }
}

data class FavoriteCurrencies (
    var currencies: List<String>? = null
)
data class SavedCurrencies (
    var currencies: List<Toplist>? = null
)

data class SavedPortfolio (
    var portfolio: List<PortfolioResponse>? = null
)

fun getRandomChart(count: Int = 167): ArrayList<Double> {
    val priceArray = ArrayList<Double>(count)
    var previousPrice = Random.nextDouble(0.01, 0.99)

    for (i in 0 until count) {
        previousPrice = Random.nextDouble(
            (previousPrice - 0.01).coerceAtLeast(0.0),
            (previousPrice + 0.01).coerceAtMost(1.0)
        )
        priceArray.add(previousPrice)
    }

    return priceArray
}