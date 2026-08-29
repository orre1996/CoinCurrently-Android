package com.cryptium.coincurrently.ui.main.General.NavTypes

import android.net.Uri
import android.os.Bundle
import androidx.navigation.NavType
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import networking.ResponseModels.CryptoDetailInfoModel
import networking.ResponseModels.PriceVolumeModel

object PriceVolumeModelNavType {
    val priceVolumeModel = object: NavType<PriceVolumeModel?>(
        isNullableAllowed = true
    ) {
        override fun get(
            bundle: Bundle,
            key: String
        ): PriceVolumeModel? {
            return Json.Default.decodeFromString(bundle.getString(key) ?: return null)
        }

        override fun parseValue(value: String): PriceVolumeModel? {
            return Json.Default.decodeFromString(Uri.decode(value))
        }

        override fun serializeAsValue(value: PriceVolumeModel?): String {
            return Uri.encode(Json.Default.encodeToString(value))
        }

        override fun put(
            bundle: Bundle,
            key: String,
            value: PriceVolumeModel?
        ) {
            bundle.putString(key, Json.Default.encodeToString(value))
        }
    }
}