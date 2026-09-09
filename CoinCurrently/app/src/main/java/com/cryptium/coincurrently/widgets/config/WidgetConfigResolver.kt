package com.cryptium.coincurrently.widgets.config

import android.content.Context
import com.cryptium.coincurrently.core.networking.NewsCategory
import com.cryptium.coincurrently.core.persistence.UserPreferences
import com.cryptium.coincurrently.features.Market.Toplist.Repositories.ToplistRepository

object WidgetConfigResolver {
    suspend fun resolveCoinIds(
        context: Context,
        configuredIds: List<String?>,
        limit: Int,
        requiresPro: Boolean = false,
    ): List<String> {
        if (requiresPro && UserPreferences.getHasBoughtPro() == null) {
            return toplistCoinIds(limit)
        }

        val ids = configuredIds.filterNotNull().filter { it.isNotEmpty() }
            .ifEmpty { WidgetCoinStorage.getSavedCoinIds(context) }

        if (ids.isEmpty()) {
            return toplistCoinIds(limit)
        }

        return ids.take(limit)
    }

    fun resolveNewsCategory(config: NewsWidgetConfig): Pair<NewsCategory, Boolean> {
        val locked = config.requiresPro && UserPreferences.getHasBoughtPro() == null
        val category = if (locked) NewsCategory.General() else config.toNewsCategory()
        return category to locked
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
