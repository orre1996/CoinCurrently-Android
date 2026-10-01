package com.cryptium.coincurrently.widgets.config

data class SingleCoinWidgetConfig(val coinId: String?)

data class DuoCoinWidgetConfig(
    val firstCoinId: String?,
    val secondCoinId: String?,
)

data class QuadCoinWidgetConfig(
    val coinIds: List<String?>,
)

data class MultiCoinWidgetConfig(
    val coinIds: List<String?>,
)

data class NewsWidgetConfig(
    val category: String = "general",
    val filterValue: String? = null,
)
