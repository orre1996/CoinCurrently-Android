package com.cryptium.coincurrently.core.networking.ResponseModels
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.cryptium.coincurrently.R
import com.cryptium.coincurrently.sharedui.theme.onSurfaceDim
import com.cryptium.coincurrently.sharedui.theme.trendDown
import com.cryptium.coincurrently.sharedui.theme.trendUp
import com.google.gson.annotations.SerializedName

data class NewNewsModel(
    @SerializedName("data")
    // Nullable: Gson/cache JSON can omit or null this field despite the non-null Kotlin type.
    var data: List<NewNewsItem>? = emptyList()
)

data class NewNewsItem(
    var news_url: String? = null,
    var image_url: String? = null,
    var title: String? = null,
    var text: String? = null,
    var source_name: String? = null,
    var date: String? = null,
    var topics: List<String>? = null,
    var sentiment: String? = null,
    var type: String? = null,
    var tickers: List<String>? = null,
)

data class CoinGeckoNewsItem(
    var title: String? = null,
    var url: String? = null,
    var image: String? = null,
    var author: String? = null,
    var posted_at: String? = null,
    var source_name: String? = null,
    var related_coin_ids: List<String>? = null,
)

fun String.getSentimentSign(): Int {
    if (this == "Negative") {
        return R.drawable.trend_down
    } else if (this == "Positive") {
        return R.drawable.trend_up
    } else {
        return R.drawable.trend_still
    }
}

@Composable
fun String.getSentimentColor(): Color {
    if (this == "Negative") {
        return MaterialTheme.colors.trendDown
    } else if (this == "Positive") {
        return MaterialTheme.colors.trendUp
    } else {
        return MaterialTheme.colors.onSurfaceDim
    }
}