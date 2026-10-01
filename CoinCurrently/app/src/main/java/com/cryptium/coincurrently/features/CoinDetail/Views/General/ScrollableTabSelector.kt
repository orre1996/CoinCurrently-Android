package com.cryptium.coincurrently.features.CoinDetail.Views.General
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ScrollableTabRow
import androidx.compose.material.Tab
import androidx.compose.material.TabRowDefaults
import androidx.compose.material.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.invisibleToUser
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.setProgress
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.unit.dp
import com.cryptium.coincurrently.sharedui.components.AccessibilityLayout
import com.cryptium.coincurrently.sharedui.charts.DEFERRED_AFTER_CHART_OVERLAY_TALKBACK_TRAVERSAL_INDEX
import com.cryptium.coincurrently.sharedui.components.Separator
import com.cryptium.coincurrently.sharedui.theme.onSurfaceDimmer
import com.cryptium.coincurrently.core.extensions.satoshiBold
import com.cryptium.coincurrently.core.localization.Localization

@Composable
fun ScrollableTabSelector(
    tabs: List<String>,
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit
) {
    val talkBackEnabled = AccessibilityLayout.isTalkBackEnabled
    var voiceOverTabIndex by remember { mutableIntStateOf(selectedTabIndex) }

    fun selectAdjacentTab(offsetDelta: Int) {
        val newIndex = voiceOverTabIndex + offsetDelta
        if (newIndex !in tabs.indices) return
        voiceOverTabIndex = newIndex
    }

    LaunchedEffect(selectedTabIndex) {
        voiceOverTabIndex = selectedTabIndex
    }

    LaunchedEffect(voiceOverTabIndex, talkBackEnabled) {
        if (!talkBackEnabled) return@LaunchedEffect
        if (voiceOverTabIndex == selectedTabIndex) return@LaunchedEffect
        if (voiceOverTabIndex in tabs.indices) {
            onTabSelected(voiceOverTabIndex)
        }
    }

    Column(
        modifier = Modifier
            .background(MaterialTheme.colors.surface)
            .then(
                if (talkBackEnabled) {
                    Modifier.clearAndSetSemantics {
                        traversalIndex = DEFERRED_AFTER_CHART_OVERLAY_TALKBACK_TRAVERSAL_INDEX
                        contentDescription = Localization.string("Page selector. Swipe up or down to change page.")
                        stateDescription = Localization.format(
                            "Page %d of %d, %@.",
                            voiceOverTabIndex + 1,
                            tabs.size,
                            Localization.string(tabs.getOrNull(voiceOverTabIndex) ?: ""),
                        )
                        progressBarRangeInfo = ProgressBarRangeInfo(
                            current = voiceOverTabIndex.toFloat(),
                            range = 0f..tabs.lastIndex.toFloat().coerceAtLeast(0f),
                            steps = tabs.lastIndex.coerceAtLeast(0),
                        )
                        setProgress(
                            action = { target ->
                                val current = voiceOverTabIndex.toFloat()
                                val newIndex = when {
                                    target > current ->
                                        (voiceOverTabIndex + 1).coerceAtMost(tabs.lastIndex)
                                    target < current ->
                                        (voiceOverTabIndex - 1).coerceAtLeast(0)
                                    else -> voiceOverTabIndex
                                }
                                if (newIndex != voiceOverTabIndex) {
                                    voiceOverTabIndex = newIndex
                                }
                                true
                            }
                        )
                        customActions = listOf(
                            CustomAccessibilityAction("Next page") {
                                selectAdjacentTab(1)
                                true
                            },
                            CustomAccessibilityAction("Previous page") {
                                selectAdjacentTab(-1)
                                true
                            },
                        )
                    }
                } else {
                    Modifier
                }
            )
    ) {
        ScrollableTabRow(
            selectedTabIndex = selectedTabIndex,
            backgroundColor = Color.Transparent,
            edgePadding = 16.dp,
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    color = MaterialTheme.colors.primary,
                    modifier = Modifier
                        .tabIndicatorOffset(tabPositions[selectedTabIndex])
                        .fillMaxWidth()
                )
            },
            divider = {
                TabRowDefaults.Divider(color = Color.Transparent)
            },
            modifier = if (talkBackEnabled) {
                Modifier.semantics { invisibleToUser() }
            } else {
                Modifier
            },
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    text = {
                        Text(
                            text = Localization.string(title),
                            style = satoshiBold(16),
                            color = if (index == selectedTabIndex) {
                                MaterialTheme.colors.onSurface
                            } else {
                                MaterialTheme.colors.onSurfaceDimmer
                            },
                        )
                    },
                    selected = index == selectedTabIndex,
                    onClick = { onTabSelected(index) }
                )
            }
        }

        Separator()
    }
}
