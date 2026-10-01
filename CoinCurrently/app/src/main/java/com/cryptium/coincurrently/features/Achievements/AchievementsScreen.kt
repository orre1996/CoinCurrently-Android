package com.cryptium.coincurrently.features.Achievements
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
import com.cryptium.coincurrently.sharedui.theme.onSurfaceDim
import com.cryptium.coincurrently.sharedui.theme.primaryBackgroundNoRipple
import com.cryptium.coincurrently.sharedui.theme.surface
import com.cryptium.coincurrently.core.extensions.satoshiBold
import com.cryptium.coincurrently.core.extensions.satoshiMedium
import com.cryptium.coincurrently.sharedui.navigation.CustomTopAppBar
import com.cryptium.coincurrently.app.NavigationViewModel
import com.cryptium.coincurrently.features.Achievements.Components.AchievementRowView
import com.cryptium.coincurrently.features.Achievements.Managers.AchievementManager
import com.cryptium.coincurrently.features.Achievements.Models.AchievementCategory
import com.cryptium.coincurrently.features.Achievements.AchievementsViewModel
import com.cryptium.coincurrently.core.localization.Localization
import com.cryptium.coincurrently.features.Settings.SectionHeader

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
            title = Localization.string("Achievements"),
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
                contentDescription = Localization.format(
                    "Your progress. %d of %d levels unlocked. Current streak %d days. Best streak %d days.",
                    unlockedLevels,
                    totalLevels,
                    currentStreak,
                    longestStreak,
                )
            }
    ) {
        Text(
            text = Localization.string("Your progress"),
            style = satoshiBold(22),
            color = MaterialTheme.colors.onSurface,
        )

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            SummaryStat(
                title = Localization.string("Levels"),
                value = "$unlockedLevels/$totalLevels",
                modifier = Modifier.weight(1f),
            )
            SummaryStat(
                title = Localization.string("Current streak"),
                value = Localization.format("%d days", currentStreak),
                modifier = Modifier.weight(1f),
            )
            SummaryStat(
                title = Localization.string("Best streak"),
                value = Localization.format("%d days", longestStreak),
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
