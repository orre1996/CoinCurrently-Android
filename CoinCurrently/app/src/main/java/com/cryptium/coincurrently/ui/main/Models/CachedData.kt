package com.cryptium.coincurrently.ui.main.Models

import com.google.gson.annotations.SerializedName
import java.util.Date

data class CachedData<T>(
    @SerializedName("data")
    var data: T,
    @SerializedName("date")
    var date: Date
)
