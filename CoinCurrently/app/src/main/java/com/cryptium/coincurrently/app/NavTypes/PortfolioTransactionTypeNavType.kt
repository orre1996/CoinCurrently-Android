package com.cryptium.coincurrently.app.NavTypes
import android.net.Uri
import android.os.Bundle
import androidx.navigation.NavType
import com.cryptium.coincurrently.features.Portfolio.Enums.PortfolioTransactionType
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object PortfolioTransactionTypeNavType {
    val transactionType = object : NavType<PortfolioTransactionType>(
        isNullableAllowed = false
    ) {
        override fun get(
            bundle: Bundle,
            key: String
        ): PortfolioTransactionType? {
            return Json.Default.decodeFromString(bundle.getString(key) ?: return null)
        }

        override fun parseValue(value: String): PortfolioTransactionType {
            return Json.Default.decodeFromString(Uri.decode(value))
        }

        override fun serializeAsValue(value: PortfolioTransactionType): String {
            return Uri.encode(Json.Default.encodeToString(value))
        }

        override fun put(
            bundle: Bundle,
            key: String,
            value: PortfolioTransactionType
        ) {
            bundle.putString(key, Json.Default.encodeToString(value))
        }
    }
}
