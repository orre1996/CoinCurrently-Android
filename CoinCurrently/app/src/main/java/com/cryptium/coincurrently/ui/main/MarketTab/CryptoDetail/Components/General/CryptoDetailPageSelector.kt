package com.cryptium.coincurrently.ui.main.MarketTab.CryptoDetail.Components.General

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
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.unit.dp
import com.cryptium.coincurrently.ui.main.Compose.AccessibilityLayout
import com.cryptium.coincurrently.ui.main.Compose.Separator
import com.cryptium.coincurrently.ui.main.Compose.onSurfaceDimmer
import com.cryptium.coincurrently.ui.main.Extensions.satoshiBold
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.PagerState
import kotlinx.coroutines.launch

@OptIn(ExperimentalPagerApi::class)
@Composable
fun CryptoDetailPageSelector(
    tabs: List<String>,
    pagerState: PagerState,
    modifier: Modifier = Modifier,
    selectedPageIndex: Int = pagerState.currentPage,
    onPageSelected: ((Int) -> Unit)? = null,
) {
    val scope = rememberCoroutineScope()
    val talkBackEnabled = AccessibilityLayout.isTalkBackEnabled
    var voiceOverPageIndex by remember { mutableIntStateOf(selectedPageIndex) }

    fun selectAdjacentPage(offsetDelta: Int) {
        val newIndex = voiceOverPageIndex + offsetDelta
        if (newIndex !in tabs.indices) return
        voiceOverPageIndex = newIndex
    }

    LaunchedEffect(selectedPageIndex) {
        voiceOverPageIndex = selectedPageIndex
    }

    LaunchedEffect(voiceOverPageIndex, talkBackEnabled) {
        if (!talkBackEnabled) return@LaunchedEffect
        if (voiceOverPageIndex == selectedPageIndex) return@LaunchedEffect
        if (onPageSelected != null) {
            onPageSelected(voiceOverPageIndex)
        } else if (pagerState.pageCount > 0 && voiceOverPageIndex < pagerState.pageCount) {
            pagerState.animateScrollToPage(voiceOverPageIndex)
        }
    }

    Column(
        modifier = modifier
            .then(
                if (talkBackEnabled) {
                    Modifier.clearAndSetSemantics {
                        contentDescription = "Page selector. Swipe up or down to change page."
                        stateDescription =
                            "Page ${voiceOverPageIndex + 1} of ${tabs.size}, ${tabs.getOrNull(voiceOverPageIndex) ?: ""}."
                        progressBarRangeInfo = ProgressBarRangeInfo(
                            current = voiceOverPageIndex.toFloat(),
                            range = 0f..tabs.lastIndex.toFloat().coerceAtLeast(0f),
                            steps = tabs.lastIndex.coerceAtLeast(0),
                        )
                        setProgress(
                            action = { target ->
                                val current = voiceOverPageIndex.toFloat()
                                val newIndex = when {
                                    target > current ->
                                        (voiceOverPageIndex + 1).coerceAtMost(tabs.lastIndex)
                                    target < current ->
                                        (voiceOverPageIndex - 1).coerceAtLeast(0)
                                    else -> voiceOverPageIndex
                                }
                                if (newIndex != voiceOverPageIndex) {
                                    voiceOverPageIndex = newIndex
                                }
                                true
                            }
                        )
                        customActions = listOf(
                            CustomAccessibilityAction("Next page") {
                                selectAdjacentPage(1)
                                true
                            },
                            CustomAccessibilityAction("Previous page") {
                                selectAdjacentPage(-1)
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
            selectedTabIndex = selectedPageIndex,
            backgroundColor = Color.Transparent,
            edgePadding = 16.dp,
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    color = MaterialTheme.colors.primary,
                    modifier = Modifier
                        .tabIndicatorOffset(tabPositions[selectedPageIndex])
                        .fillMaxWidth()
                )
            },
            divider = {
                TabRowDefaults.Divider(
                    color = Color.Transparent
                )
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
                            text = title,
                            style = satoshiBold(16),
                            color = if (index == selectedPageIndex) MaterialTheme.colors.onSurface else MaterialTheme.colors.onSurfaceDimmer,
                        )
                    },
                    selected = true,
                    onClick = {
                        if (onPageSelected != null) {
                            onPageSelected(index)
                        } else {
                            scope.launch {
                                if (pagerState.pageCount > 0 && index < pagerState.pageCount) {
                                    pagerState.animateScrollToPage(index)
                                }
                            }
                        }
                    }
                )
            }
        }

        Separator()
    }
}