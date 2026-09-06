package com.cryptium.coincurrently.app.NavTypes
import android.net.Uri
import android.os.Bundle
import androidx.navigation.NavType
import com.cryptium.coincurrently.features.Portfolio.DataClasses.PortfolioTransaction
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object TransactionNavType {
    val transaction = object: NavType<PortfolioTransaction?>(
        isNullableAllowed = true
    ) {
        override fun get(
            bundle: Bundle,
            key: String
        ): PortfolioTransaction? {
            return Json.Default.decodeFromString(bundle.getString(key) ?: return null)
        }

        override fun parseValue(value: String): PortfolioTransaction? {
            return Json.Default.decodeFromString(Uri.decode(value))
        }

        override fun serializeAsValue(value: PortfolioTransaction?): String {
            return Uri.encode(Json.Default.encodeToString(value))
        }

        override fun put(
            bundle: Bundle,
            key: String,
            value: PortfolioTransaction?
        ) {
            bundle.putString(key, Json.Default.encodeToString(value))
        }
    }
}