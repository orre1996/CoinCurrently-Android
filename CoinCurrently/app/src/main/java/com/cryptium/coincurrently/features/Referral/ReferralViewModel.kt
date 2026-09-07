package com.cryptium.coincurrently.features.Referral
import android.graphics.Bitmap
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cryptium.coincurrently.features.Referral.ReferralRepository
import com.cryptium.coincurrently.features.Achievements.Managers.AchievementManager
import com.cryptium.coincurrently.features.Achievements.Models.AchievementCounterKey
import kotlinx.coroutines.launch
import com.cryptium.coincurrently.core.networking.ResponseModels.RedeemCodeResult
import com.cryptium.coincurrently.core.networking.ResponseModels.ReferralCodeResult
import com.cryptium.coincurrently.core.networking.ResponseModels.UserResponse

class ReferralViewModel(
    initialRedeemCodeInput: String = "",
) : ViewModel() {
    var referralQRImage by mutableStateOf<Bitmap?>(null)
    var referralLink by mutableStateOf<String?>(null)
    var referralCode by mutableStateOf<String?>(null)
    var redeemCodeInput by mutableStateOf(initialRedeemCodeInput)
    var isLoadingReferralCode by mutableStateOf(false)
    var isRedeemingCode by mutableStateOf(false)
    var redeemSucceeded by mutableStateOf(false)
    var redeemErrorMessage by mutableStateOf<String?>(null)
    val showError: Boolean
        get() = redeemErrorMessage != null
    var user by mutableStateOf<UserResponse?>(null)
    var currentReferrals by mutableIntStateOf(0)

    fun setStartData() {
        ReferralRepository.user?.let { cachedUser ->
            user = cachedUser
            currentReferrals = cachedUser.referralCount
        }

        ReferralRepository.referralCodeResult?.let { result ->
            referralQRImage = result.qrImage
            referralCode = result.code
            referralLink = result.inviteLink
        }
    }

    suspend fun loadReferralCode() {
        if (isLoadingReferralCode) return

        val showLoading = referralCode == null
        if (showLoading) {
            isLoadingReferralCode = true
        }
        try {
            val result = ReferralRepository.getReferralCode() ?: return

            applyReferralCodeResult(result)
        } finally {
            if (showLoading) {
                isLoadingReferralCode = false
            }
        }
    }

    private fun applyReferralCodeResult(result: ReferralCodeResult) {
        referralQRImage = result.qrImage
        referralCode = result.code
        referralLink = result.inviteLink
    }

    suspend fun getUser(ignoreCache: Boolean = false) {
        val response = ReferralRepository.getUser(ignoreCache = ignoreCache) ?: return
        user = response
        currentReferrals = response.referralCount
    }

    fun redeemCode() {
        val trimmedCode = redeemCodeInput.trim()
        if (trimmedCode.isEmpty() || isRedeemingCode) return

        viewModelScope.launch {
            isRedeemingCode = true
            val result = ReferralRepository.redeemCode(trimmedCode)
            redeemSucceeded = result == RedeemCodeResult.Success
            redeemErrorMessage = result.errorMessage
            isRedeemingCode = false

            if (result == RedeemCodeResult.SelfRedeem) {
                AchievementManager.increment(AchievementCounterKey.selfRedeems)
            }

            if (redeemSucceeded) {
                redeemCodeInput = ""
            }
        }
    }
}
