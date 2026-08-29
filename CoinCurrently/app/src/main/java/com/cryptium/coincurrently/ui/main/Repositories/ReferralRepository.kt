package com.cryptium.coincurrently.ui.main.Repositories

import android.util.Log
import com.cryptium.coincurrently.ui.main.Managers.FirebaseManager
import com.cryptium.coincurrently.ui.main.Models.CachedData
import com.cryptium.coincurrently.ui.main.SettingsTab.Achievements.Managers.AchievementManager
import com.cryptium.coincurrently.ui.main.SettingsTab.Achievements.Models.AchievementMetric
import com.google.gson.reflect.TypeToken
import networking.ApiHelper
import networking.CacheUrls
import networking.MainRepository
import networking.ResponseModels.CachedReferralCode
import networking.ResponseModels.ReferralCodeResult
import networking.ResponseModels.UserResponse
import networking.RetrofitBuilder
import java.util.Date

class ReferralRepository {
    companion object {
        private val mainRepository by lazy { MainRepository(ApiHelper(RetrofitBuilder.apiService)) }

        private val userResponseType = object : TypeToken<UserResponse>() {}.type
        private val cachedReferralCodeType = object : TypeToken<CachedReferralCode>() {}.type

        var user: UserResponse? = null
            private set

        var referralCodeResult: ReferralCodeResult? = null
            private set

        init {
            try {
                @Suppress("UNCHECKED_CAST")
                user = RetrofitBuilder.getCached(CacheUrls.getUser(), userResponseType, includeExpired = true)?.data as? UserResponse

                @Suppress("UNCHECKED_CAST")
                referralCodeResult = (RetrofitBuilder.getCached(CacheUrls.referralCode(), cachedReferralCodeType, includeExpired = true)?.data as? CachedReferralCode)?.toResult()
            } catch (_: Exception) {
                // StorageManager/RetrofitBuilder may not be ready yet; cache will populate on first fetch
            }
        }

        suspend fun getReferralCode(): ReferralCodeResult? {
            @Suppress("UNCHECKED_CAST")
            RetrofitBuilder.getCached(CacheUrls.referralCode(), cachedReferralCodeType)?.let { cached ->
                val result = (cached.data as? CachedReferralCode)?.toResult()
                referralCodeResult = result
                return result
            }

            Log.w("Network call ", "Referral code")

            val result = mainRepository.getReferralCode()
            result?.let {
                referralCodeResult = it
                RetrofitBuilder.updateCache(CacheUrls.referralCode(), CachedData(it.toCached(), Date()))
            }
            return result
        }

        suspend fun redeemCode(refCode: String): Boolean {
            Log.w("Network call ", "Redeem referral code")
            val success = mainRepository.redeemCode(refCode).isSuccess
            if (success) {
                FirebaseManager.syncPurchases()
            }
            return success
        }

        suspend fun getUser(): UserResponse? {
            val previousCount = user?.referralCount?.toDouble() ?: 0.0

            @Suppress("UNCHECKED_CAST")
            RetrofitBuilder.getCached(CacheUrls.getUser(), userResponseType)?.let { cached ->
                val cachedUser = cached.data as? UserResponse
                user = cachedUser
                return cachedUser
            }

            Log.w("Network call ", "User")

            val response = mainRepository.getUser()
            user = response

            if (response != null) {
                AchievementManager.checkMetricUnlock(
                    metric = AchievementMetric.referralsCompleted,
                    previousValue = previousCount,
                    newValue = response.referralCount.toDouble(),
                )
            }

            return response
        }
    }
}
