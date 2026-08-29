package com.cryptium.coincurrently.ui.main.General.NavTypes

import AddressImport
import android.net.Uri
import android.os.Bundle
import androidx.navigation.NavType
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object WalletNavType {
    val wallet = object: NavType<AddressImport?>(
        isNullableAllowed = false
    ) {
        override fun get(
            bundle: Bundle,
            key: String
        ): AddressImport? {
            return Json.Default.decodeFromString(bundle.getString(key) ?: return null)
        }

        override fun parseValue(value: String): AddressImport {
            return Json.Default.decodeFromString(Uri.decode(value))
        }

        override fun serializeAsValue(value: AddressImport?): String {
            return Uri.encode(Json.Default.encodeToString(value))
        }

        override fun put(
            bundle: Bundle,
            key: String,
            value: AddressImport?
        ) {
            bundle.putString(key, Json.Default.encodeToString(value))
        }
    }
}