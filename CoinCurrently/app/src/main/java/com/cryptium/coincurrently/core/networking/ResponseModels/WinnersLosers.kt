package com.cryptium.coincurrently.core.networking.ResponseModels
import java.io.Serializable

data class WinnersLosers (
    val top_gainers: List<WinnerLoser>?,
    val top_losers: List<WinnerLoser>?
): Serializable

data class WinnerLoser(
    val id: String
)
