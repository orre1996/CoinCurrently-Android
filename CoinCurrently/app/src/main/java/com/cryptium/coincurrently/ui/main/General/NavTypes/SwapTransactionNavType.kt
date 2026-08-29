package com.cryptium.coincurrently.ui.main.General.NavTypes

import android.net.Uri
import android.os.Bundle
import androidx.navigation.NavType
import com.cryptium.coincurrently.ui.main.CryptoDetail.Compose.Portfolio.Models.NewSwapTransaction
import com.cryptium.coincurrently.ui.main.PortfolioTab.DataClasses.NewTransaction
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object SwapTransactionNavType {
    val swapTransaction = object: NavType<NewSwapTransaction?>(
        isNullableAllowed = true
    ) {
        override fun get(
            bundle: Bundle,
            key: String
        ): NewSwapTransaction? {
            return Json.Default.decodeFromString(bundle.getString(key) ?: return null)
        }

        override fun parseValue(value: String): NewSwapTransaction? {
            return Json.Default.decodeFromString(Uri.decode(value))
        }

        override fun serializeAsValue(value: NewSwapTransaction?): String {
            return Uri.encode(Json.Default.encodeToString(value))
        }

        override fun put(
            bundle: Bundle,
            key: String,
            value: NewSwapTransaction?
        ) {
            bundle.putString(key, Json.Default.encodeToString(value))
        }
    }
}