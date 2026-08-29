package com.cryptium.coincurrently.ui.main.General.NavTypes

import SearchScreenCase
import android.net.Uri
import android.os.Bundle
import androidx.navigation.NavType
import com.cryptium.coincurrently.ui.main.PortfolioTab.DataClasses.NewTransaction
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object SearchCaseNavType {
    val searchScreenCase = object: NavType<SearchScreenCase>(
        isNullableAllowed = false
    ) {
        override fun get(
            bundle: Bundle,
            key: String
        ): SearchScreenCase? {
            return Json.Default.decodeFromString(bundle.getString(key) ?: return null)
        }

        override fun parseValue(value: String): SearchScreenCase {
            return Json.Default.decodeFromString(Uri.decode(value))
        }

        override fun serializeAsValue(value: SearchScreenCase): String {
            return Uri.encode(Json.Default.encodeToString(value))
        }

        override fun put(
            bundle: Bundle,
            key: String,
            value: SearchScreenCase
        ) {
            bundle.putString(key, Json.Default.encodeToString(value))
        }
    }
}