package com.cryptium.coincurrently.app.NavTypes
import android.net.Uri
import android.os.Bundle
import androidx.navigation.NavType
import com.cryptium.coincurrently.features.Portfolio.Models.PortfolioSwapTransaction
import com.cryptium.coincurrently.features.Portfolio.DataClasses.PortfolioTransaction
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object SwapTransactionNavType {
    val swapTransaction = object: NavType<PortfolioSwapTransaction?>(
        isNullableAllowed = true
    ) {
        override fun get(
            bundle: Bundle,
            key: String
        ): PortfolioSwapTransaction? {
            return Json.Default.decodeFromString(bundle.getString(key) ?: return null)
        }

        override fun parseValue(value: String): PortfolioSwapTransaction? {
            return Json.Default.decodeFromString(Uri.decode(value))
        }

        override fun serializeAsValue(value: PortfolioSwapTransaction?): String {
            return Uri.encode(Json.Default.encodeToString(value))
        }

        override fun put(
            bundle: Bundle,
            key: String,
            value: PortfolioSwapTransaction?
        ) {
            bundle.putString(key, Json.Default.encodeToString(value))
        }
    }
}