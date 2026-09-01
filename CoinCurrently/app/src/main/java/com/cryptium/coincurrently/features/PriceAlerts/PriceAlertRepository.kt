package com.cryptium.coincurrently.features.PriceAlerts
import android.util.Log
import com.cryptium.coincurrently.core.extensions.unwrapOrNull
import com.cryptium.coincurrently.sharedui.components.GlobalFunctions
import com.cryptium.coincurrently.features.Settings.CurrencySelector.allCurrencies
import com.google.gson.reflect.TypeToken
import com.cryptium.coincurrently.core.analytics.FirebaseManager
import com.cryptium.coincurrently.core.persistence.StorageKeys
import com.cryptium.coincurrently.core.persistence.StorageManager
import com.cryptium.coincurrently.features.Achievements.Managers.AchievementManager
import com.cryptium.coincurrently.features.Achievements.Models.AchievementMetric
import com.cryptium.coincurrently.core.networking.ApiHelper
import com.cryptium.coincurrently.core.networking.CacheUrls
import com.cryptium.coincurrently.core.networking.MainRepository
import com.cryptium.coincurrently.core.networking.ResponseModels.AlertCoin
import com.cryptium.coincurrently.core.networking.ResponseModels.PostAlertCoin
import com.cryptium.coincurrently.core.networking.ResponseModels.Toplist
import com.cryptium.coincurrently.core.networking.RetrofitBuilder
import com.cryptium.coincurrently.features.Referral.ReferralRepository
import com.cryptium.coincurrently.sharedui.models.CachedData
import java.util.Date

enum class PriceAlertCreateResult {
    Success,
    FcmTokenUnavailable,
    Failed,
}

class PriceAlertsRepository {
    companion object {
        private val mainRepository by lazy { MainRepository(ApiHelper(RetrofitBuilder.apiService)) }

        private val alertPricesType = object : TypeToken<MutableMap<String, Map<String, Double>>>() {}.type

        var priceAlerts: List<AlertCoin>? = null

        var alertPricesAllCurrencies: MutableMap<String, Map<String, Double>>? = null

        init {
            try {
                priceAlerts = RetrofitBuilder.getCachedList<AlertCoin>(CacheUrls.getAlerts(), includeExpired = true)?.data
            } catch (_: Exception) {
                // StorageManager/RetrofitBuilder may not be ready yet; cache will populate on first fetch
            }
        }

        suspend fun downloadRemainingCoins(ids: List<String>): List<Toplist>? {
            val idsStr = ids.joinToString { it }.replace(" ", "")

            if (idsStr.isEmpty()) {
                return null
            }

            val currencyCode = GlobalFunctions.getSelectedCurrency(null).code
            val url = CacheUrls.getMultipleCoins(idsStr, currencyCode)

            RetrofitBuilder.getCachedList<Toplist>(url)?.let { cached ->
                return cached.data
            }

            Log.w("Network call ", "Multiple coins")

            return mainRepository.getMultipleCurrencies(idsStr, currencyCode).unwrapOrNull()
        }

        suspend fun updateUser(): Boolean {
            if (!FirebaseManager.fetchFcmTokenIfNeeded()) {
                return false
            }

            val fcmToken = FirebaseManager.fcmToken
            if (fcmToken.isBlank()) {
                return false
            }

            val currency = GlobalFunctions.getSelectedCurrency()
            val claims = ReferralRepository.getUser()?.user?.customClaims
            val fcmMatches = claims?.fcmToken == fcmToken
            val currencyMatches = claims?.preferredCurrency?.equals(currency.code, ignoreCase = true) == true

            if (fcmMatches && currencyMatches) {
                return true
            }

            val result = mainRepository.updateUser(fcmToken)
            result.onSuccess {
                StorageManager.set(StorageKeys.LAST_PUSH_NOTIFICATION_TOKEN, fcmToken)
                ReferralRepository.getUser(ignoreCache = true)

                if (StorageManager.get<Boolean>(StorageKeys.SUBSCIBRES_TO_BIG_MOVERS) == null) {
                    subBigMovers()
                }
            }
            return result.isSuccess
        }

        suspend fun subBigMovers(): Boolean {
            val result = mainRepository.subBigMover().unwrapOrNull()

            if (result != null) {
                StorageManager.set(StorageKeys.SUBSCIBRES_TO_BIG_MOVERS, true)
                return true
            } else {
                return false
            }
        }

        suspend fun ubsubBigMover(): Boolean {
            val result = mainRepository.ubsubBigMover().unwrapOrNull()

            if (result != null) {
                StorageManager.set(StorageKeys.SUBSCIBRES_TO_BIG_MOVERS, false)
                return false
            } else {
                return true
            }
        }

        suspend fun triggerAlert() {
            mainRepository.triggerPriceAlert().unwrapOrNull()
        }

        suspend fun triggerBigMover() {
            mainRepository.bigMoverTrigger().unwrapOrNull()
        }

        suspend fun getAlertPrices(ids: List<String>): MutableMap<String, Map<String, Double>>? {
            if (ids.isEmpty()) {
                return null
            }

            val idsStr = ids.joinToString { it }.replace(" ", "").removeSuffix(",")
            val vsCurrencies = allCurrencies.map { it.code.uppercase() }.joinToString { it }.replace(" ", "").removeSuffix(",")
            val url = CacheUrls.calculatorPrices(idsStr, vsCurrencies)

            @Suppress("UNCHECKED_CAST")
            (RetrofitBuilder.getCached(url, alertPricesType)?.data as? MutableMap<String, Map<String, Double>>)?.let { data ->
                alertPricesAllCurrencies = data
                return data
            }

            Log.w("Network call ", "Alert prices all currencies")

            val currencies = mainRepository.getCoinsWithAllCurrencies(idsStr).unwrapOrNull()

            currencies?.let {
                alertPricesAllCurrencies = it
            }

            return currencies
        }

        suspend fun getAlerts(): List<AlertCoin>? {
            val url = CacheUrls.getAlerts()

            RetrofitBuilder.getCachedList<AlertCoin>(url)?.let { cached ->
                priceAlerts = cached.data
                return cached.data
            }

            Log.w("Network call ", "Get alerts")

            val alerts = mainRepository.getAlerts().unwrapOrNull()

            alerts?.let {
                priceAlerts = it
            }

            return alerts
        }

        suspend fun createAlert(alertCoin: PostAlertCoin): PriceAlertCreateResult {
            if (!FirebaseManager.fetchFcmTokenIfNeeded()) {
                return PriceAlertCreateResult.FcmTokenUnavailable
            }

            updateUser()

            val alert = mainRepository.createPriceAlert(alertCoin).unwrapOrNull()
                ?: return PriceAlertCreateResult.Failed

            val previousCount = priceAlerts?.size?.toDouble() ?: 0.0
            val oldAlerts = priceAlerts?.toMutableList() ?: mutableListOf()
            oldAlerts.add(alert)

            priceAlerts = oldAlerts
            RetrofitBuilder.updateCache(CacheUrls.getAlerts(), CachedData(oldAlerts, Date()))

            AchievementManager.checkMetricUnlock(
                metric = AchievementMetric.priceAlertsCount,
                previousValue = previousCount,
                newValue = oldAlerts.size.toDouble(),
            )

            return PriceAlertCreateResult.Success
        }

        suspend fun deleteAlert(alertId: String): List<AlertCoin>? {
            val result = mainRepository.deletePriceAlert(alertId)

            var finalAlerts: List<AlertCoin>? = priceAlerts

            result.onSuccess {
                val oldAlerts = priceAlerts?.toMutableList() ?: mutableListOf()
                oldAlerts.removeAll { it.id == alertId }

                priceAlerts = oldAlerts
                RetrofitBuilder.updateCache(CacheUrls.getAlerts(), CachedData(oldAlerts, Date()))

                finalAlerts = oldAlerts
            }

            return finalAlerts
        }

        fun deleteLocalAlert(id: String) {
            val alert = priceAlerts?.firstOrNull { it.id == id }

            if (alert == null || alert.isRecurringAlert) {
                return
            }

            val oldAlerts = priceAlerts?.toMutableList() ?: mutableListOf()
            oldAlerts.removeAll { it.id == id }

            priceAlerts = oldAlerts
            RetrofitBuilder.updateCache(CacheUrls.getAlerts(), CachedData(oldAlerts, Date()))
        }

        suspend fun sendFeedback(rating: Int, feedback: String?): Boolean {
            return mainRepository.sendFeedback(rating, feedback).isSuccess
        } 
    }
}