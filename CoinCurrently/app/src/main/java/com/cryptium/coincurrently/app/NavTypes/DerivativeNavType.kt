package com.cryptium.coincurrently.app.NavTypes
import android.net.Uri
import android.os.Bundle
import androidx.navigation.NavType
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import com.cryptium.coincurrently.core.networking.ResponseModels.Derivative

object DerivativeNavType {
    val derivative = object: NavType<Derivative>(
        isNullableAllowed = false
    ) {
        override fun get(
            bundle: Bundle,
            key: String
        ): Derivative? {
            return Json.Default.decodeFromString(bundle.getString(key) ?: return null)
        }

        override fun parseValue(value: String): Derivative {
            return Json.Default.decodeFromString(Uri.decode(value))
        }

        override fun serializeAsValue(value: Derivative): String {
            return Uri.encode(Json.Default.encodeToString(value))
        }

        override fun put(
            bundle: Bundle,
            key: String,
            value: Derivative
        ) {
            bundle.putString(key, Json.Default.encodeToString(value))
        }
    }
}