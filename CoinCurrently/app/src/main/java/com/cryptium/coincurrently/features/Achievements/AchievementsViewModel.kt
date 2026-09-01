package com.cryptium.coincurrently.features.Achievements
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cryptium.coincurrently.features.Achievements.Managers.AchievementManager
import com.cryptium.coincurrently.features.Achievements.Models.AchievementCategory
import com.cryptium.coincurrently.features.Achievements.Models.AchievementProgress
import com.cryptium.coincurrently.features.Achievements.Models.AchievementsSummary
import kotlinx.coroutines.launch

class AchievementsViewModel : ViewModel() {
    var progressItems by mutableStateOf(AchievementManager.cachedProgress)
    var summary by mutableStateOf(AchievementManager.cachedSummary)
    var isLoading by mutableStateOf(false)

    init {
        progressItems = AchievementManager.cachedProgress
        summary = AchievementManager.cachedSummary
    }

    fun loadAchievements() {
        if (isLoading) return
        viewModelScope.launch {
            isLoading = true
            try {
                val allProgress = AchievementManager.computeAllProgress()
                progressItems = allProgress
                summary = AchievementManager.computeSummary(allProgress)
            } finally {
                isLoading = false
            }
        }
    }

    fun itemsForCategory(category: AchievementCategory): List<AchievementProgress> =
        progressItems.filter { it.definition.category == category }
}
