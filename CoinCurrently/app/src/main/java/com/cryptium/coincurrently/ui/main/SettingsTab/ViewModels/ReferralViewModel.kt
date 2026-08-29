package com.cryptium.coincurrently.ui.main.SettingsTab.ViewModels

import android.graphics.Bitmap
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cryptium.coincurrently.ui.main.Repositories.ReferralRepository
import kotlinx.coroutines.launch
import networking.ResponseModels.ReferralCodeResult
import networking.ResponseModels.UserResponse

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
    var showError by mutableStateOf(false)
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

    suspend fun getUser() {
        val response = ReferralRepository.getUser() ?: return
        user = response
        currentReferrals = response.referralCount
    }

    fun redeemCode() {
        val trimmedCode = redeemCodeInput.trim()
        if (trimmedCode.isEmpty() || isRedeemingCode) return

        viewModelScope.launch {
            isRedeemingCode = true
            redeemSucceeded = ReferralRepository.redeemCode(trimmedCode)
            showError = !redeemSucceeded
            isRedeemingCode = false

            if (redeemSucceeded) {
                redeemCodeInput = ""
            }
        }
    }
}
