package com.cryptium.coincurrently.core.networking.ResponseModels
data class GasFeesModel(
    val gasFeeData: List<GasResult>?
)

data class GasResult(
    val lastBlock: String,
    val sK_Date: String,
    val safeGasPrice: String,
    val proposeGasPrice: String,
    val fastGasPrice: String,
    val suggestBaseFee: String,
    val gasUsedRatio: String
)
