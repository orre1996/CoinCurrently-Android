package com.cryptium.coincurrently.ui.main.Managers

import com.google.gson.annotations.SerializedName

data class HiddenSecret(
    @SerializedName("secret_number")
    val secretNumber: String,
    @SerializedName("secret_url")
    val secretUrl: String,
    /** Optional image URL from Remote Config (avoids hardcoding Firebase Storage tokens in source). */
    @SerializedName("secret_image")
    val secretImage: String? = null,
)
