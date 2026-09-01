package com.cryptium.coincurrently.core.networking.ResponseModels
import kotlinx.serialization.Serializable

@Serializable
data class PriceVolumeModel(
    var prices: ArrayList<ArrayList<Double?>>? = null,
    var market_caps: ArrayList<ArrayList<Double?>>? = null,
    var total_volumes: ArrayList<ArrayList<Double?>>? = null
)
