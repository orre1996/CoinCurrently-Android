package com.cryptium.coincurrently.ui.main.SettingsTab.Achievements.Managers

import com.cryptium.coincurrently.ui.main.Managers.StorageKeys
import com.cryptium.coincurrently.ui.main.Managers.StorageManager
import com.cryptium.coincurrently.ui.main.SettingsTab.Achievements.Models.AchievementMetric
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object AchievementStreakTracker {
    private val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    fun recordAppOpen() {
        val today = dateFormatter.format(Date())
        val lastOpenDate: String? = StorageManager.get(StorageKeys.ACHIEVEMENT_LAST_OPEN_DATE)
        var currentStreak: Int = StorageManager.get(StorageKeys.ACHIEVEMENT_CURRENT_STREAK) ?: 0
        var longestStreak: Int = StorageManager.get(StorageKeys.ACHIEVEMENT_LONGEST_STREAK) ?: 0

        if (lastOpenDate == today) {
            return
        }

        val previousStreak = currentStreak.toDouble()

        currentStreak = if (lastOpenDate != null) {
            val lastDate = dateFormatter.parse(lastOpenDate)
            val yesterday = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, -1)
            }.time
            if (lastDate != null && dateFormatter.format(lastDate) == dateFormatter.format(yesterday)) {
                currentStreak + 1
            } else {
                1
            }
        } else {
            1
        }

        longestStreak = maxOf(longestStreak, currentStreak)

        StorageManager.set(StorageKeys.ACHIEVEMENT_LAST_OPEN_DATE, today)
        StorageManager.set(StorageKeys.ACHIEVEMENT_CURRENT_STREAK, currentStreak)
        StorageManager.set(StorageKeys.ACHIEVEMENT_LONGEST_STREAK, longestStreak)

        val newStreak = currentStreak.toDouble()
        AchievementManager.checkMetricUnlock(
            metric = AchievementMetric.dailyStreak,
            previousValue = previousStreak,
            newValue = newStreak,
        )
    }

    val currentStreak: Int
        get() = StorageManager.get(StorageKeys.ACHIEVEMENT_CURRENT_STREAK) ?: 0

    val longestStreak: Int
        get() = StorageManager.get(StorageKeys.ACHIEVEMENT_LONGEST_STREAK) ?: 0
}
