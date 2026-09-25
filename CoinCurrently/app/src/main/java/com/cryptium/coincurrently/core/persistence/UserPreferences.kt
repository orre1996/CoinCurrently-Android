package com.cryptium.coincurrently.core.persistence
import android.content.Context
import android.content.SharedPreferences
import com.cryptium.coincurrently.core.config.AppLocale
import com.cryptium.coincurrently.core.models.PreferredTimeframe
import com.cryptium.coincurrently.core.models.SelectedStartTab
import com.cryptium.coincurrently.core.networking.ResponseModels.CurrencySelectionModel
import com.cryptium.coincurrently.core.networking.ResponseModels.PortfolioResponse
import com.cryptium.coincurrently.core.networking.ResponseModels.TimeCharts
import com.cryptium.coincurrently.core.networking.ResponseModels.Toplist
import com.google.gson.Gson
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import kotlin.math.min

/**
 * Durable user settings. Backed by SharedPreferences so [StorageManager.clearCache]
 * (DiskLruCache) does not wipe currency, Pro, or UI preferences.
 *
 * Keys and the `push_notification_preferences_file` name must stay stable.
 */
object UserPreferences {
    private const val PREFS_NAME = "push_notification_preferences_file"

    private var preferences: SharedPreferences? = null

    fun init(context: Context) {
        if (preferences == null) {
            preferences = context.applicationContext.getSharedPreferences(PREFS_NAME, 0)
        }
    }

    private fun prefs(context: Context? = null): SharedPreferences? {
        if (preferences == null && context != null) {
            preferences = context.applicationContext.getSharedPreferences(PREFS_NAME, 0)
        }
        return preferences
    }

    fun getStoredWhatsNewVersion(): String? =
        prefs()?.getString("stored_whats_new_version", null)

    fun setStoredWhatsNewVersion(version: String) {
        prefs()?.edit()?.putString("stored_whats_new_version", version)?.apply()
    }

    fun getIsEligableForTrial(): Boolean {
        return prefs()?.getBoolean("ssEligableForTrial", true) ?: true
    }

    fun setIsEligableForTrial(isEligable: Boolean) {
        prefs()?.edit()?.putBoolean("ssEligableForTrial", isEligable)?.apply()
    }

    fun getHasBoughtPro(): String? {
        return prefs()?.getString("hasBoughtPro-1", null)
    }

    fun setHasBoughtPro(enable: String?) {
        prefs()?.edit()?.putString("hasBoughtPro-1", enable)?.apply()
    }

    fun getProExpirationDate(): Date? {
        val millis = prefs()?.getLong("proExpirationDate", -1L) ?: -1L
        return if (millis > 0L) Date(millis) else null
    }

    fun setProExpirationDate(date: Date?) {
        val editor = prefs()?.edit() ?: return
        if (date != null) {
            editor.putLong("proExpirationDate", date.time)
        } else {
            editor.remove("proExpirationDate")
        }
        editor.apply()
    }

    fun getIsBiometricAuthenticationEnabled(): Boolean {
        return prefs()?.getBoolean("biometricAuthenticationEnabled", false) ?: false
    }

    fun setIsBiometricAuthenticationEnabled(enabled: Boolean) {
        prefs()?.edit()?.putBoolean("biometricAuthenticationEnabled", enabled)?.apply()
    }

    fun getPreferredTimeframe(): PreferredTimeframe {
        val preferredTimeframe = prefs()?.getInt("preferredTimeframe", 1) ?: 1
        return PreferredTimeframe.entries[preferredTimeframe]
    }

    fun setPreferredTimeframe(timeframe: PreferredTimeframe) {
        prefs()?.edit()?.putInt("preferredTimeframe", timeframe.ordinal)?.apply()
    }

    fun setStartTab(tab: SelectedStartTab) {
        prefs()?.edit()?.putInt("startTab", tab.ordinal)?.apply()
    }

    fun getStartTab(): SelectedStartTab {
        val value = prefs()?.getInt("startTab", 1) ?: 1
        return SelectedStartTab.entries[value]
    }

    fun setColorBlind(enabled: Boolean) {
        prefs()?.edit()?.putBoolean("setColorBlind", enabled)?.apply()
    }

    fun getColorBlind(): Boolean {
        return prefs()?.getBoolean("setColorBlind", false) ?: false
    }

    fun setWebSocketEnabled(enabled: Boolean) {
        prefs()?.edit()?.putBoolean("webSocketEnabled", enabled)?.apply()
    }

    fun getWebSocketEnabledSetting(): Boolean? {
        val stored = prefs() ?: return null
        return if (stored.contains("webSocketEnabled")) {
            stored.getBoolean("webSocketEnabled", false)
        } else {
            null
        }
    }

    fun isWebSocketEnabled(hasPro: Boolean): Boolean {
        return getWebSocketEnabledSetting() ?: hasPro
    }

    fun setHidePortfolioData(hide: Boolean) {
        prefs()?.edit()?.putBoolean("hidePortfolioData", hide)?.apply()
    }

    fun getHidePortfolioData(): Boolean {
        return prefs()?.getBoolean("hidePortfolioData", false) ?: false
    }

    fun setPortfolioPieChartShowing(showing: Boolean) {
        prefs()?.edit()?.putBoolean("isPortfolioPieChartShowing", showing)?.apply()
    }

    fun getPortfolioPieChartShowing(): Boolean {
        return prefs()?.getBoolean("isPortfolioPieChartShowing", false) ?: false
    }

    fun getPortfolioExportCount(): Int {
        return prefs()?.getInt("portfolioExportCount", 0) ?: 0
    }

    fun incrementPortfolioExportCount() {
        prefs()?.edit()?.putInt("portfolioExportCount", getPortfolioExportCount() + 1)?.apply()
    }

    fun setLatestTabIndex(page: Int) {
        prefs()?.edit()?.putInt("latestTab", page)?.apply()
    }

    fun getLatestTabIndex(): Int {
        return prefs()?.getInt("latestTab", 0) ?: 0
    }

    fun getShowChangesInPercent(): Boolean {
        return prefs()?.getBoolean("showChangesInPercent", true) ?: true
    }

    fun setShowChangesInPercent(inPercent: Boolean) {
        prefs()?.edit()?.putBoolean("showChangesInPercent", inPercent)?.apply()
    }

    fun setSelectedCurrency(currency: CurrencySelectionModel?) {
        prefs()?.edit()?.putString("savedCurrency", Gson().toJson(currency))?.apply()
    }

    fun getSelectedCurrency(context: Context? = null): CurrencySelectionModel {
        try {
            val json: String = prefs(context)?.getString("savedCurrency", "") ?: "savedCurrency"
            if (json.isEmpty()) {
                return CurrencySelectionModel()
            }
            return Gson().fromJson(json, CurrencySelectionModel::class.java)
        } catch (_: Exception) {
            return CurrencySelectionModel()
        }
    }

    fun setPortfolioSelectedCurrency(currency: CurrencySelectionModel?) {
        prefs()?.edit()?.putString("savedCurrencyPortfolio", Gson().toJson(currency))?.apply()
    }

    fun getPortfolioSelectedCurrency(context: Context? = null): CurrencySelectionModel? {
        try {
            val json: String = prefs(context)?.getString("savedCurrencyPortfolio", "") ?: "savedCurrency"
            if (json.isEmpty()) {
                return CurrencySelectionModel()
            }
            return Gson().fromJson(json, CurrencySelectionModel::class.java)
        } catch (_: Exception) {
            return CurrencySelectionModel()
        }
    }

    fun setWidgetCurrency(ids: List<Toplist>) {
        val newList: ArrayList<Toplist> = arrayListOf()
        for (i in 0 until min(8, ids.size)) {
            newList.add(ids[i])
        }
        prefs()?.edit()?.putString("widgetCurrencies", Gson().toJson(SavedCurrencies(newList)))?.apply()
    }

    fun getWidgetCurrency(context: Context? = null): List<Toplist> {
        val currency = prefs(context)?.getString("widgetCurrencies", "") ?: return arrayListOf()
        return try {
            val currencyArray = Gson().fromJson(currency, SavedCurrencies::class.java)
            val list = currencyArray?.currencies.orEmpty()
            list.subList(0, min(8, list.size))
        } catch (_: Throwable) {
            arrayListOf()
        }
    }

    fun setPortfolioCurrency(portfolio: List<PortfolioResponse>) {
        val list = arrayListOf<PortfolioResponse>()
        portfolio.forEach {
            if (!list.contains(it)) {
                list.add(it)
            }
        }
        prefs()?.edit()?.putString("portfolioModelCurrencies", Gson().toJson(SavedPortfolio(list)))?.apply()
    }

    fun getPortfolioCurrencies(context: Context? = null): List<PortfolioResponse> {
        val currency = prefs(context)?.getString("portfolioModelCurrencies", null)
        return try {
            val currencyArray = Gson().fromJson(currency, SavedPortfolio::class.java)
            currencyArray?.portfolio.orEmpty()
        } catch (_: Exception) {
            listOf()
        }
    }

    fun setFavorites(favorites: List<String>) {
        val test = arrayListOf<String>()
        favorites.forEach {
            if (!test.contains(it)) {
                test.add(it)
            }
        }
        prefs()?.edit()?.putString("favorites", Gson().toJson(FavoriteCurrencies(test)))?.apply()
    }

    fun getFavorites(): List<String> {
        val currency = prefs()?.getString("favorites", null) ?: return emptyList()
        return try {
            val currencyArray = Gson().fromJson(currency, FavoriteCurrencies::class.java)
            currencyArray?.currencies.orEmpty()
        } catch (_: Throwable) {
            emptyList()
        }
    }

    fun getRatingDate(): String? {
        return prefs()?.getString("ratingDate", null)
    }

    fun setRatingDate(inDays: Int) {
        prefs()?.edit()?.putString("ratingDate", dateStringAddingDays(inDays))?.apply()
    }

    fun getPortfolioHintDate(): String? {
        return prefs()?.getString("portfolioHintDate", null)
    }

    fun setPortfolioHintDate(inDays: Int) {
        prefs()?.edit()?.putString("portfolioHintDate", dateStringAddingDays(inDays))?.apply()
    }

    fun getMoreHintDate(): String? {
        return prefs()?.getString("moreHintDate", null)
    }

    fun setMoreHintDate(inDays: Int) {
        prefs()?.edit()?.putString("moreHintDate", dateStringAddingDays(inDays))?.apply()
    }

    fun getIsWidgetDarkModeEnabled(): Boolean {
        return prefs()?.getBoolean("isWidgetDarkModeEnabled", true) ?: true
    }

    fun setIsWidgetDarkModeEnabled(enabled: Boolean) {
        prefs()?.edit()?.putBoolean("isWidgetDarkModeEnabled", enabled)?.apply()
    }

    fun getPreferredTimeframeTitle(): String = getPreferredTimeframe().title

    fun getPreferredTimeframeChangeTitle(): String = getPreferredTimeframe().changeTitle

    fun getPreferredTimeframeIndex(fromCryptoDetail: Boolean = false): Int =
        getPreferredTimeframe().chartIndex(fromCryptoDetail)

    fun getPreferredTimeframeIndexRaw(): String = getPreferredTimeframe().daysRaw

    fun getPreferredTimeframeValue(): TimeCharts {
        return when (getPreferredTimeframe()) {
            PreferredTimeframe.DAILY -> TimeCharts.aDay
            PreferredTimeframe.WEEKLY -> TimeCharts.aWeek
            PreferredTimeframe.MONTHLY -> TimeCharts.aMonth
        }
    }

    fun preferredTimeFrameTextChange(): String = getPreferredTimeframe().changeLabel

    private fun dateStringAddingDays(inDays: Int): String {
        val calendar = Calendar.getInstance(AppLocale.enUsPosix)
        calendar.add(Calendar.DAY_OF_YEAR, inDays)
        return SimpleDateFormat("yyyy-MM-dd HH:mm:ss", AppLocale.enUsPosix).format(calendar.time)
    }

    private data class FavoriteCurrencies(
        var currencies: List<String>? = null
    )

    private data class SavedCurrencies(
        var currencies: List<Toplist>? = null
    )

    private data class SavedPortfolio(
        var portfolio: List<PortfolioResponse>? = null
    )
}
