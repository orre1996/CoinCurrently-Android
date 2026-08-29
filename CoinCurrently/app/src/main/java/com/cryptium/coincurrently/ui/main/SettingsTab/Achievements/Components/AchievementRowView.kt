package com.cryptium.coincurrently.ui.main.SettingsTab.Achievements.Components

import androidx.compose.foundation.Image
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.cryptium.coincurrently.R
import com.cryptium.coincurrently.ui.main.Compose.onSurfaceDim
import com.cryptium.coincurrently.ui.main.Compose.onSurfaceDimmer
import com.cryptium.coincurrently.ui.main.Compose.primaryBackgroundNoRipple
import com.cryptium.coincurrently.ui.main.Extensions.satoshiBold
import com.cryptium.coincurrently.ui.main.Extensions.satoshiMedium
import com.cryptium.coincurrently.ui.main.SettingsTab.Achievements.Models.AchievementProgress

@Composable
fun AchievementRowView(progress: AchievementProgress) {
    val unlockedCount = maxOf(0, progress.unlockedLevelIndex + 1)
    val totalCount = progress.definition.levels.size

    Box(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .primaryBackgroundNoRipple(paddingValues = PaddingValues(0.dp)),
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top,
                modifier = Modifier.padding(horizontal = 16.dp),
            ) {
                Image(
                    painter = painterResource(achievementIcon(progress.definition.icon)),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(MaterialTheme.colors.primary),
                    modifier = Modifier.size(32.dp),
                )

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = progress.definition.title,
                        style = satoshiBold(16),
                        color = MaterialTheme.colors.onSurface,
                    )

                    Text(
                        text = progress.definition.description,
                        style = satoshiMedium(14),
                        color = MaterialTheme.colors.onSurfaceDim,
                    )

                    Text(
                        text = "$unlockedCount/$totalCount levels unlocked",
                        style = satoshiMedium(12),
                        color = MaterialTheme.colors.onSurfaceDimmer,
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(horizontal = 16.dp),
                ) {
                    progress.definition.levels.forEachIndexed { index, level ->
                        AchievementLevelCardView(
                            level = level,
                            levelIndex = index,
                            progress = progress,
                            valueFormat = progress.definition.valueFormat,
                        )
                    }
                }
            }
        }
    }
}

private fun achievementIcon(icon: String): Int = when (icon) {
    "flame" -> R.drawable.lightning
    "chart_pie" -> R.drawable.piechart
    "trend_up" -> R.drawable.trend_up
    "star" -> R.drawable.favorite_filled
    "bell" -> R.drawable.alert
    "widgets" -> R.drawable.list
    "converter" -> R.drawable.converter
    "chart" -> R.drawable.candlestick
    "search" -> R.drawable.search
    "bitcoin" -> R.drawable.market
    "compare" -> R.drawable.compare
    "news" -> R.drawable.news
    "nft" -> R.drawable.explorer
    "exchange" -> R.drawable.trade
    "sector" -> R.drawable.sector
    "gauge" -> R.drawable.percent
    "gas" -> R.drawable.lightning
    "treasury" -> R.drawable.portfolio
    "face_id" -> R.drawable.baseline_lock_24
    "referral" -> R.drawable.share
    "square.and.arrow.up" -> R.drawable.share
    else -> R.drawable.check_circle
}
