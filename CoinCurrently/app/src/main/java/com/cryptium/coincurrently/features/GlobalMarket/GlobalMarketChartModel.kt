package com.cryptium.coincurrently.features.GlobalMarket
import kotlinx.serialization.Serializable

@Serializable
data class GlobalMarketChartModel(
    val market_cap_chart: GlobalMarketChartData? = null
)

@Serializable
data class GlobalMarketChartData(
    val market_cap: ArrayList<ArrayList<Double?>>? = null,
    val volume: ArrayList<ArrayList<Double?>>? = null
)
