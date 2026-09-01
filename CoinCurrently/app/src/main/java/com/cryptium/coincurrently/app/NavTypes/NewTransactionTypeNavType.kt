package com.cryptium.coincurrently.app.NavTypes
import android.net.Uri
import android.os.Bundle
import androidx.navigation.NavType
import com.cryptium.coincurrently.features.Portfolio.Enums.NewTransactionType
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object NewTransactionTypeNavType {
    val transactionType = object : NavType<NewTransactionType>(
        isNullableAllowed = false
    ) {
        override fun get(
            bundle: Bundle,
            key: String
        ): NewTransactionType? {
            return Json.Default.decodeFromString(bundle.getString(key) ?: return null)
        }

        override fun parseValue(value: String): NewTransactionType {
            return Json.Default.decodeFromString(Uri.decode(value))
        }

        override fun serializeAsValue(value: NewTransactionType): String {
            return Uri.encode(Json.Default.encodeToString(value))
        }

        override fun put(
            bundle: Bundle,
            key: String,
            value: NewTransactionType
        ) {
            bundle.putString(key, Json.Default.encodeToString(value))
        }
    }
}
