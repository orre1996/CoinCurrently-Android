package com.cryptium.coincurrently.features.Portfolio.Models
enum class TransferType {
    OUTTYPE,
    INTYPE;

    val rawValue: Int
        get() {
            return when (this) {
                OUTTYPE -> { 0 }
                else -> { 1 }
            }
        }
}
