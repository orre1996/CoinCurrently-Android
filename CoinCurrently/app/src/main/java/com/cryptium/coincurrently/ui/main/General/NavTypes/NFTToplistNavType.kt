package com.cryptium.coincurrently.ui.main.General.NavTypes

import android.net.Uri
import android.os.Bundle
import androidx.navigation.NavType
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import networking.ResponseModels.NFTToplist

object NFTToplistNavType {
    val nftToplist = object: NavType<NFTToplist>(
        isNullableAllowed = false
    ) {
        override fun get(
            bundle: Bundle,
            key: String
        ): NFTToplist? {
            return Json.Default.decodeFromString(bundle.getString(key) ?: return null)
        }

        override fun parseValue(value: String): NFTToplist {
            return Json.Default.decodeFromString(Uri.decode(value))
        }

        override fun serializeAsValue(value: NFTToplist): String {
            return Uri.encode(Json.Default.encodeToString(value))
        }

        override fun put(
            bundle: Bundle,
            key: String,
            value: NFTToplist
        ) {
            bundle.putString(key, Json.Default.encodeToString(value))
        }
    }
}