package com.cryptium.coincurrently.ui.main.CryptoDetail.Compose.Portfolio.Models

enum class TransactionType {
    BUY,
    SELL,
    TRANSFER,
    IMPORT_ADDRESS;

    val rawValue: Int
        get() {
            return when (this) {
                IMPORT_ADDRESS -> { 0 }
                BUY -> { 1 }
                SELL -> { 2 }
                else -> { 2 }
            }
        }
}