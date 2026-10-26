package com.cryptium.coincurrently.widgets.config

import android.content.Context
import com.cryptium.coincurrently.features.Market.Toplist.Repositories.ToplistRepository

object WidgetConfigResolver {
    suspend fun resolveCoinIds(
        context: Context,
        configuredIds: List<String?>,
        limit: Int,
    ): List<String> {
        val ids = configuredIds.filterNotNull().filter { it.isNotEmpty() }
            .ifEmpty { WidgetCoinStorage.getSavedCoinIds(context) }

        if (ids.isEmpty()) {
            return toplistCoinIds(limit)
        }

        return ids.take(limit)
    }

    private suspend fun toplistCoinIds(limit: Int): List<String> {
        ToplistRepository.smallToplist
            ?.mapNotNull { it.id }
            ?.take(limit)
            ?.takeIf { it.isNotEmpty() }
            ?.let { return it }

        ToplistRepository.getSmallToplist()
            ?.mapNotNull { it.id }
            ?.take(limit)
            ?.takeIf { it.isNotEmpty() }
            ?.let { return it }

        return WidgetCoinStorage.hardcodedFallbackIds.take(limit)
    }
}
