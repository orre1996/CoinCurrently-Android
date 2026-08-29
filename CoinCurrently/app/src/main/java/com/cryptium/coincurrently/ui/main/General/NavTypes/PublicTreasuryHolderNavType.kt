package com.cryptium.coincurrently.ui.main.General.NavTypes

import android.net.Uri
import android.os.Bundle
import androidx.navigation.NavType
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import networking.ResponseModels.PublicTreasuryHolder

object PublicTreasuryHolderNavType {
    val holder = object : NavType<PublicTreasuryHolder>(
        isNullableAllowed = false,
    ) {
        override fun get(
            bundle: Bundle,
            key: String,
        ): PublicTreasuryHolder? {
            return Json.Default.decodeFromString(bundle.getString(key) ?: return null)
        }

        override fun parseValue(value: String): PublicTreasuryHolder {
            return Json.Default.decodeFromString(Uri.decode(value))
        }

        override fun serializeAsValue(value: PublicTreasuryHolder): String {
            return Uri.encode(Json.Default.encodeToString(value))
        }

        override fun put(
            bundle: Bundle,
            key: String,
            value: PublicTreasuryHolder,
        ) {
            bundle.putString(key, Json.Default.encodeToString(value))
        }
    }
}
