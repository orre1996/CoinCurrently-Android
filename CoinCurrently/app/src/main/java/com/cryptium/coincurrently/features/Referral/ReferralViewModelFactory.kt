package com.cryptium.coincurrently.features.Referral
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class ReferralViewModelFactory(
    private val referralCode: String,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ReferralViewModel::class.java)) {
            return ReferralViewModel(initialRedeemCodeInput = referralCode) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
