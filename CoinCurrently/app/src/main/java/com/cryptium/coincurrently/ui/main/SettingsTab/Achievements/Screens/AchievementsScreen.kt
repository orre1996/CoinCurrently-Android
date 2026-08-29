package com.cryptium.coincurrently.ui.main.SettingsTab.Achievements.Screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cryptium.coincurrently.ui.main.Compose.onSurfaceDim
import com.cryptium.coincurrently.ui.main.Compose.primaryBackgroundNoRipple
import com.cryptium.coincurrently.ui.main.Compose.surface
import com.cryptium.coincurrently.ui.main.Extensions.satoshiBold
import com.cryptium.coincurrently.ui.main.Extensions.satoshiMedium
import com.cryptium.coincurrently.ui.main.General.Components.CustomTopAppBar
import com.cryptium.coincurrently.ui.main.General.NavigationViewModel
import com.cryptium.coincurrently.ui.main.SettingsTab.Achievements.Components.AchievementRowView
import com.cryptium.coincurrently.ui.main.SettingsTab.Achievements.Managers.AchievementManager
import com.cryptium.coincurrently.ui.main.SettingsTab.Achievements.Models.AchievementCategory
import com.cryptium.coincurrently.ui.main.SettingsTab.Achievements.ViewModels.AchievementsViewModel
import com.cryptium.coincurrently.ui.main.SettingsTab.Compose.SectionHeader

@Composable
fun AchievementsScreen(
    navigationViewModel: NavigationViewModel,
    modifier: Modifier = Modifier,
    viewModel: AchievementsViewModel = viewModel(),
) {
    BackHandler {
        navigationViewModel.navigateBack()
    }

    LaunchedEffect(Unit) {
        AchievementManager.preloadProgress()
        viewModel.loadAchievements()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.surface)
    ) {
        CustomTopAppBar(
            title = "Achievements",
            navigateBack = { navigationViewModel.navigateBack() },
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(32.dp),
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        ) {
            SummarySection(
                unlockedLevels = viewModel.summary.unlockedLevels,
                totalLevels = viewModel.summary.totalLevels,
                currentStreak = viewModel.summary.currentStreak,
                longestStreak = viewModel.summary.longestStreak,
            )

            AchievementCategory.entries.forEach { category ->
                val items = viewModel.itemsForCategory(category)
                if (items.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SectionHeader(category.displayName)

                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            items.forEach { item ->
                                AchievementRowView(progress = item)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SummarySection(
    unlockedLevels: Int,
    totalLevels: Int,
    currentStreak: Int,
    longestStreak: Int,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .primaryBackgroundNoRipple()
            .semantics {
                contentDescription =
                    "Your progress. $unlockedLevels of $totalLevels levels unlocked. " +
                    "Current streak $currentStreak days. Best streak $longestStreak days."
            }
    ) {
        Text(
            text = "Your progress",
            style = satoshiBold(22),
            color = MaterialTheme.colors.onSurface,
        )

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            SummaryStat(
                title = "Levels",
                value = "$unlockedLevels/$totalLevels",
                modifier = Modifier.weight(1f),
            )
            SummaryStat(
                title = "Current streak",
                value = "$currentStreak days",
                modifier = Modifier.weight(1f),
            )
            SummaryStat(
                title = "Best streak",
                value = "$longestStreak days",
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun SummaryStat(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = Alignment.Start,
        modifier = modifier,
    ) {
        Text(
            text = title,
            style = satoshiMedium(12),
            color = MaterialTheme.colors.onSurfaceDim,
        )
        Text(
            text = value,
            style = satoshiBold(16),
            color = MaterialTheme.colors.onSurface,
        )
    }
}
