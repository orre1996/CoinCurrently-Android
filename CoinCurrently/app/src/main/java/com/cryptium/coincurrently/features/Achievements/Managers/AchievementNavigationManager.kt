package com.cryptium.coincurrently.features.Achievements.Managers
import com.cryptium.coincurrently.BuildConfig
import com.cryptium.coincurrently.app.NavigationViewModel
import com.cryptium.coincurrently.features.Achievements.Models.AchievementUnlockEvent

object AchievementNavigationManager {
    private var navigationViewModel: NavigationViewModel? = null
    private val pendingUnlocks = mutableListOf<AchievementUnlockEvent>()

    fun register(viewModel: NavigationViewModel) {
        if (!BuildConfig.DEBUG) return

        navigationViewModel = viewModel

        if (pendingUnlocks.isEmpty()) return

        val unlocks = pendingUnlocks.toList()
        pendingUnlocks.clear()

        for (unlock in unlocks) {
            present(unlock, viewModel)
        }
    }

    fun showUnlock(unlock: AchievementUnlockEvent) {
        if (!BuildConfig.DEBUG) return

        val viewModel = navigationViewModel
        if (viewModel == null) {
            pendingUnlocks.add(unlock)
            return
        }
        present(unlock, viewModel)
    }

    private fun present(unlock: AchievementUnlockEvent, viewModel: NavigationViewModel) {
        viewModel.showAchievementSnackbar(
            achievementTitle = unlock.achievementTitle,
            levelName = unlock.levelName,
        )
    }
}
