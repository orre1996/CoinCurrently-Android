package com.cryptium.coincurrently.features.Referral
import android.util.Log
import com.cryptium.coincurrently.core.analytics.FirebaseManager
import com.cryptium.coincurrently.sharedui.models.CachedData
import com.cryptium.coincurrently.features.Achievements.Managers.AchievementManager
import com.cryptium.coincurrently.features.Achievements.Models.AchievementMetric
import com.google.gson.reflect.TypeToken
import com.cryptium.coincurrently.core.networking.CacheUrls
import com.cryptium.coincurrently.core.networking.MainRepository
import com.cryptium.coincurrently.core.networking.ResponseModels.CachedReferralCode
import com.cryptium.coincurrently.core.networking.ResponseModels.RedeemCodeResult
import com.cryptium.coincurrently.core.networking.ResponseModels.ReferralCodeResult
import com.cryptium.coincurrently.core.networking.ResponseModels.UserResponse
import com.cryptium.coincurrently.core.networking.RetrofitBuilder
import java.util.Date

object ReferralRepository {
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

        val result = MainRepository.getReferralCode()
        result?.let {
            referralCodeResult = it
            RetrofitBuilder.updateCache(CacheUrls.referralCode(), CachedData(it.toCached(), Date()))
        }
        return result
    }

    suspend fun redeemCode(refCode: String): RedeemCodeResult {
        Log.w("Network call ", "Redeem referral code")
        val result = RedeemCodeResult.fromResult(MainRepository.redeemCode(refCode))
        if (result == RedeemCodeResult.Success) {
            FirebaseManager.awaitSyncPurchases()
        }
        return result
    }

    suspend fun getUser(ignoreCache: Boolean = false): UserResponse? {
        val previousCount = user?.referralCount?.toDouble() ?: 0.0

        if (ignoreCache) {
            RetrofitBuilder.removeCached(CacheUrls.getUser())
        }

        @Suppress("UNCHECKED_CAST")
        RetrofitBuilder.getCached(CacheUrls.getUser(), userResponseType)?.let { cached ->
            val cachedUser = cached.data as? UserResponse
            user = cachedUser
            return cachedUser
        }

        Log.w("Network call ", "User")

        val response = MainRepository.getUser()
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
