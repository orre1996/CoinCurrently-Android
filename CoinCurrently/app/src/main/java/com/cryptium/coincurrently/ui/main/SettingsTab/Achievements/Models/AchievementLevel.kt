package com.cryptium.coincurrently.ui.main.SettingsTab.Achievements.Models

data class AchievementLevel(
    val name: String,
    val threshold: Double,
    val description: String,
) {
    val id: String = "$name-$threshold"
}

object AchievementLevelTier {
    const val BRONZE = "Bronze"
    const val SILVER = "Silver"
    const val GOLD = "Gold"
    const val PLATINUM = "Platinum"
    const val DIAMOND = "Diamond"
    const val MASTER = "Master"
}
