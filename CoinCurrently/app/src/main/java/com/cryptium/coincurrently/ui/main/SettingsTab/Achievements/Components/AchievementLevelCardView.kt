package com.cryptium.coincurrently.ui.main.SettingsTab.Achievements.Components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.cryptium.coincurrently.R
import com.cryptium.coincurrently.ui.main.Compose.onSurfaceDim
import com.cryptium.coincurrently.ui.main.Compose.onSurfaceDimmer
import com.cryptium.coincurrently.ui.main.Compose.outlineVariant
import com.cryptium.coincurrently.ui.main.Compose.secondary15
import com.cryptium.coincurrently.ui.main.Compose.surfaceContainer
import com.cryptium.coincurrently.ui.main.Compose.surfaceContainerHigh
import com.cryptium.coincurrently.ui.main.Compose.surfaceContainerLow
import com.cryptium.coincurrently.ui.main.Extensions.satoshiBold
import com.cryptium.coincurrently.ui.main.Extensions.satoshiMedium
import com.cryptium.coincurrently.ui.main.General.Components.CustomProgressBarView
import com.cryptium.coincurrently.ui.main.SettingsTab.Achievements.Models.AchievementDefinition
import com.cryptium.coincurrently.ui.main.SettingsTab.Achievements.Models.AchievementLevel
import com.cryptium.coincurrently.ui.main.SettingsTab.Achievements.Models.AchievementLevelState
import com.cryptium.coincurrently.ui.main.SettingsTab.Achievements.Models.AchievementProgress
import kotlin.math.roundToInt

@Composable
fun AchievementLevelCardView(
    level: AchievementLevel,
    levelIndex: Int,
    progress: AchievementProgress,
    valueFormat: AchievementDefinition.AchievementValueFormat,
) {
    val state = progress.levelState(levelIndex)
    val progressFraction = progress.progressFraction(level, levelIndex)

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .width(128.dp)
            .background(
                color = when (state) {
                    AchievementLevelState.UNLOCKED -> MaterialTheme.colors.secondary15
                    AchievementLevelState.IN_PROGRESS -> MaterialTheme.colors.surfaceContainer
                    AchievementLevelState.LOCKED -> MaterialTheme.colors.surfaceContainerLow
                },
                shape = RoundedCornerShape(12.dp),
            )
            .border(
                width = 1.dp,
                color = if (state == AchievementLevelState.UNLOCKED) {
                    MaterialTheme.colors.primary
                } else {
                    MaterialTheme.colors.outlineVariant
                },
                shape = RoundedCornerShape(12.dp),
            )
            .alpha(if (state == AchievementLevelState.LOCKED) 0.65f else 1f)
            .padding(12.dp)
            .semantics {
                contentDescription = levelAccessibilityLabel(level, state, progress, valueFormat)
            }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = level.name,
                style = satoshiBold(14),
                color = if (state == AchievementLevelState.LOCKED) {
                    MaterialTheme.colors.onSurfaceDimmer
                } else {
                    MaterialTheme.colors.onSurface
                },
            )

            if (state == AchievementLevelState.UNLOCKED) {
                Image(
                    painter = painterResource(R.drawable.check),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(MaterialTheme.colors.primary),
                    modifier = Modifier.size(14.dp),
                )
            } else if (state == AchievementLevelState.LOCKED) {
                Image(
                    painter = painterResource(R.drawable.baseline_lock_24),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(MaterialTheme.colors.onSurfaceDimmer),
                    modifier = Modifier.size(14.dp),
                )
            }
        }

        Text(
            text = level.description,
            style = satoshiMedium(12),
            color = MaterialTheme.colors.onSurfaceDim,
            maxLines = 2,
        )

        when (state) {
            AchievementLevelState.IN_PROGRESS -> {
                CustomProgressBarView(
                    progress = progressFraction,
                    foregroundColor = MaterialTheme.colors.primary,
                    backgroundColor = MaterialTheme.colors.surfaceContainerHigh,
                    height = 6.dp,
                )
                Text(
                    text = progressLabel(progress.currentValue, level.threshold, valueFormat),
                    style = satoshiMedium(12),
                    color = MaterialTheme.colors.onSurfaceDim,
                )
            }

            AchievementLevelState.UNLOCKED -> {
                Text(
                    text = "Unlocked",
                    style = satoshiMedium(12),
                    color = MaterialTheme.colors.primary,
                )
            }

            AchievementLevelState.LOCKED -> {
                Text(
                    text = "Locked",
                    style = satoshiMedium(12),
                    color = MaterialTheme.colors.onSurfaceDimmer,
                )
            }
        }
    }
}

private fun progressLabel(
    current: Double,
    target: Double,
    valueFormat: AchievementDefinition.AchievementValueFormat,
): String = "${formattedValue(current, valueFormat)}/${formattedValue(target, valueFormat)}"

private fun formattedValue(
    value: Double,
    valueFormat: AchievementDefinition.AchievementValueFormat,
): String = when (valueFormat) {
    AchievementDefinition.AchievementValueFormat.INTEGER,
    AchievementDefinition.AchievementValueFormat.BOOLEAN -> value.roundToInt().toString()
    AchievementDefinition.AchievementValueFormat.PERCENT -> "${value.roundToInt()}%"
}

private fun levelAccessibilityLabel(
    level: AchievementLevel,
    state: AchievementLevelState,
    progress: AchievementProgress,
    valueFormat: AchievementDefinition.AchievementValueFormat,
): String {
    val status = when (state) {
        AchievementLevelState.UNLOCKED -> "unlocked"
        AchievementLevelState.IN_PROGRESS ->
            "in progress, ${progressLabel(progress.currentValue, level.threshold, valueFormat)}"
        AchievementLevelState.LOCKED -> "locked"
    }
    return "${level.name} level, ${level.description}, $status"
}
