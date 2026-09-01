// Oscar Berggren
// 2026-04-27

package com.cryptium.coincurrently.core.networking.ResponseModels
data class SendFeedbackRequest(
    val rating: Int,
    val feedback: String? = null,
)
