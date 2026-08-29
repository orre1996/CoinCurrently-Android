package com.cryptium.coincurrently.ui.main.SettingsTab.Achievements.Models

data class AchievementDefinition(
    val id: String,
    val title: String,
    val description: String,
    val category: AchievementCategory,
    val icon: String,
    val metric: AchievementMetric,
    val levels: List<AchievementLevel>,
    val valueFormat: AchievementValueFormat,
) {
    enum class AchievementValueFormat {
        INTEGER,
        PERCENT,
        BOOLEAN,
    }
}
