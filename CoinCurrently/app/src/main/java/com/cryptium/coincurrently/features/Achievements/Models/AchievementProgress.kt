package com.cryptium.coincurrently.features.Achievements.Models
data class AchievementProgress(
    val definition: AchievementDefinition,
    val currentValue: Double,
    val unlockedLevelIndex: Int,
) {
    val id: String get() = definition.id

    val isFullyUnlocked: Boolean
        get() = unlockedLevelIndex >= definition.levels.lastIndex

    val nextLevelIndex: Int?
        get() = if (isFullyUnlocked) null else unlockedLevelIndex + 1

    fun levelState(index: Int): AchievementLevelState = when {
        index <= unlockedLevelIndex -> AchievementLevelState.UNLOCKED
        index == unlockedLevelIndex + 1 -> AchievementLevelState.IN_PROGRESS
        else -> AchievementLevelState.LOCKED
    }

    fun progressFraction(level: AchievementLevel, index: Int): Double {
        if (index != unlockedLevelIndex + 1 && !(unlockedLevelIndex == -1 && index == 0)) {
            return if (index <= unlockedLevelIndex) 1.0 else 0.0
        }

        val previousThreshold = if (index > 0) definition.levels[index - 1].threshold else 0.0
        val range = level.threshold - previousThreshold
        if (range <= 0) return if (currentValue >= level.threshold) 1.0 else 0.0
        val progressInRange = currentValue - previousThreshold
        return (progressInRange / range).coerceIn(0.0, 1.0)
    }
}

enum class AchievementLevelState {
    UNLOCKED,
    IN_PROGRESS,
    LOCKED,
}

data class AchievementsSummary(
    val unlockedLevels: Int,
    val totalLevels: Int,
    val currentStreak: Int,
    val longestStreak: Int,
)
