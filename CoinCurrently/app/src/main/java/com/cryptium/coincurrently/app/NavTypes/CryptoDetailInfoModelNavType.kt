package com.cryptium.coincurrently.app.NavTypes
import android.net.Uri
import android.os.Bundle
import androidx.navigation.NavType
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import com.cryptium.coincurrently.core.networking.ResponseModels.CryptoDetailInfoModel

object CryptoDetailInfoModelNavType {
    val cryptoDetailInfoModel = object: NavType<CryptoDetailInfoModel?>(
        isNullableAllowed = true
    ) {
        override fun get(
            bundle: Bundle,
            key: String
        ): CryptoDetailInfoModel? {
            return Json.Default.decodeFromString(bundle.getString(key) ?: return null)
        }

        override fun parseValue(value: String): CryptoDetailInfoModel? {
            return Json.Default.decodeFromString(Uri.decode(value))
        }

        override fun serializeAsValue(value: CryptoDetailInfoModel?): String {
            return Uri.encode(Json.Default.encodeToString(value))
        }

        override fun put(
            bundle: Bundle,
            key: String,
            value: CryptoDetailInfoModel?
        ) {
            bundle.putString(key, Json.Default.encodeToString(value))
        }
    }
}