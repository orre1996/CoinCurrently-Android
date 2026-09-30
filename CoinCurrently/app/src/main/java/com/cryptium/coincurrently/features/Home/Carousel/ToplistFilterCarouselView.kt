package com.cryptium.coincurrently.features.Home.Carousel
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.setProgress
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import com.cryptium.coincurrently.sharedui.components.AccessibilityLayout
import com.cryptium.coincurrently.sharedui.theme.onSurfaceContainer
import com.cryptium.coincurrently.sharedui.theme.surfaceContainerHigh
import com.cryptium.coincurrently.core.extensions.dpToPx
import com.cryptium.coincurrently.core.extensions.satoshiMedium
import com.cryptium.coincurrently.core.localization.Localization
import com.cryptium.coincurrently.core.models.CarouselFilter
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun ToplistFilterCarouselView(
    filters: List<CarouselFilter>,
    selectedFilter: CarouselFilter,
    scrollState: LazyListState,
    paddingValues: PaddingValues = PaddingValues(top = 24.dp, bottom = 16.dp),
    hasPro: Boolean = true,
    animateSelectionChanges: Boolean = true,
    onFilterSelected: (CarouselFilter) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val talkBackEnabled = AccessibilityLayout.isTalkBackEnabled
    val offset = -64.dp.dpToPx().toInt()
    var voiceOverFilterIndex by remember {
        mutableIntStateOf(
            filters.indexOfFirst { it.matchesForSelection(selectedFilter) }.coerceAtLeast(0)
        )
    }

    fun selectAdjacentFilter(offsetDelta: Int) {
        val newIndex = voiceOverFilterIndex + offsetDelta
        if (newIndex !in filters.indices) return
        voiceOverFilterIndex = newIndex
    }

    LaunchedEffect(selectedFilter) {
        voiceOverFilterIndex =
            filters.indexOfFirst { it.matchesForSelection(selectedFilter) }.coerceAtLeast(0)
    }

    LaunchedEffect(voiceOverFilterIndex) {
        if (animateSelectionChanges) {
            scrollState.animateScrollToItem(voiceOverFilterIndex, offset)
        } else {
            scrollState.scrollToItem(voiceOverFilterIndex, offset)
        }
    }

    LaunchedEffect(voiceOverFilterIndex, talkBackEnabled) {
        if (!talkBackEnabled) return@LaunchedEffect

        val filter = filters.getOrNull(voiceOverFilterIndex) ?: return@LaunchedEffect
        if (filter.matchesForSelection(selectedFilter)) return@LaunchedEffect
        if (requiresVoiceOverActivation(filter, hasPro)) return@LaunchedEffect

        onFilterSelected(filter)
    }

    val selectedAccessibilityValue = selectedAccessibilityValue(
        filters = filters,
        voiceOverFilterIndex = voiceOverFilterIndex,
        selectedFilter = selectedFilter,
        hasPro = hasPro,
    )

    Box(
        modifier = Modifier
            .background(MaterialTheme.colors.surface)
            .padding(paddingValues)
            .then(
                if (talkBackEnabled) {
                    Modifier.clearAndSetSemantics {
                        contentDescription =
                            Localization.string("Category filter. Swipe up or down to change category.")
                        stateDescription = selectedAccessibilityValue
                        progressBarRangeInfo = ProgressBarRangeInfo(
                            current = voiceOverFilterIndex.toFloat(),
                            range = 0f..filters.lastIndex.toFloat().coerceAtLeast(0f),
                            steps = filters.lastIndex.coerceAtLeast(0),
                        )
                        setProgress(
                            action = { target ->
                                val newIndex = target.roundToInt().coerceIn(0, filters.lastIndex)
                                if (newIndex != voiceOverFilterIndex) {
                                    voiceOverFilterIndex = newIndex
                                }
                                true
                            }
                        )
                        customActions = listOf(
                            CustomAccessibilityAction("Next category") {
                                selectAdjacentFilter(1)
                                true
                            },
                            CustomAccessibilityAction("Previous category") {
                                selectAdjacentFilter(-1)
                                true
                            },
                        )
                        if (!hasPro) {
                            onClick(label = Localization.string("Double tap to select this Pro feature."), action = {
                                activateVoiceOverFilter(
                                    filters = filters,
                                    voiceOverFilterIndex = voiceOverFilterIndex,
                                    hasPro = hasPro,
                                    onFilterSelected = onFilterSelected,
                                )
                                true
                            })
                        }
                    }
                } else {
                    Modifier
                }
            )
    ) {
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            state = scrollState,
            userScrollEnabled = !talkBackEnabled,
        ) {
            itemsIndexed(filters) { index, filter ->
                val isSelected = selectedFilter.matchesForSelection(filter)
                val sourceTitle = if (isSelected) selectedFilter.rawValue else filter.rawValue
                val title = Localization.string(sourceTitle)
                val backgroundColor = if (isSelected) {
                    MaterialTheme.colors.primary
                } else {
                    MaterialTheme.colors.surfaceContainerHigh
                }
                val textColor = if (isSelected) {
                    MaterialTheme.colors.onPrimary
                } else {
                    MaterialTheme.colors.onSurfaceContainer
                }

                Text(
                    text = title,
                    style = satoshiMedium(16),
                    color = textColor,
                    modifier = Modifier
                        .clip(CircleShape)
                        .then(
                            if (talkBackEnabled) {
                                Modifier
                            } else {
                                Modifier.clickable {
                                    onFilterSelected(filter)

                                    coroutineScope.launch {
                                        scrollState.animateScrollToItem(index, offset)
                                    }
                                }
                            }
                        )
                        .background(color = backgroundColor)
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .then(
                            if (talkBackEnabled) {
                                Modifier
                            } else {
                                Modifier.clearAndSetSemantics {
                                    contentDescription = Localization.format(
                                        "Category %@ of %@, %@.",
                                        (index + 1).toString(),
                                        filters.size.toString(),
                                        accessibilityTitle(sourceTitle),
                                    )
                                    if (isSelected) {
                                        selected = true
                                    }
                                }
                            }
                        )
                )
            }
        }
    }
}

private fun requiresVoiceOverActivation(filter: CarouselFilter, hasPro: Boolean): Boolean =
    filter.requiresPro && !hasPro

private fun selectedAccessibilityValue(
    filters: List<CarouselFilter>,
    voiceOverFilterIndex: Int,
    selectedFilter: CarouselFilter,
    hasPro: Boolean,
): String {
    val filter = filters.getOrNull(voiceOverFilterIndex) ?: selectedFilter
    val title = if (filter.matchesForSelection(selectedFilter)) {
        selectedFilter.rawValue
    } else {
        filter.rawValue
    }
    val category = Localization.format(
        "Category %@ of %@, %@.",
        (voiceOverFilterIndex + 1).toString(),
        filters.size.toString(),
        accessibilityTitle(title),
    )

    return if (requiresVoiceOverActivation(filter, hasPro)) {
        "$category ${Localization.string("Double tap to select this Pro feature.")}"
    } else {
        category
    }
}

private fun activateVoiceOverFilter(
    filters: List<CarouselFilter>,
    voiceOverFilterIndex: Int,
    hasPro: Boolean,
    onFilterSelected: (CarouselFilter) -> Unit,
) {
    val filter = filters.getOrNull(voiceOverFilterIndex) ?: return
    if (!requiresVoiceOverActivation(filter, hasPro)) return
    onFilterSelected(filter)
}

private fun accessibilityTitle(title: String): String {
    return if (title.contains("(Pro)")) {
        val base = title.replace(" (Pro)", "")
        Localization.format("%@, Pro feature", Localization.string(base))
    } else {
        Localization.string(title)
    }
}
