package com.cryptium.coincurrently.sharedui.models
import com.google.gson.annotations.SerializedName
import java.util.Date

data class CachedData<T>(
    @SerializedName("data")
    var data: T,
    @SerializedName("date")
    var date: Date
)
