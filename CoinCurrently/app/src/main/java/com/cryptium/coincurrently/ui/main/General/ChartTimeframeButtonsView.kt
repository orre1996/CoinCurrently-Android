package com.cryptium.coincurrently.ui.main.General

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.SubcomposeLayout
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import com.cryptium.coincurrently.ui.main.Compose.AccessibilityLayout
import com.cryptium.coincurrently.ui.main.General.Components.Chart.CHART_TIMEFRAME_TALKBACK_TRAVERSAL_INDEX
import com.cryptium.coincurrently.ui.main.Extensions.poppinsSemibold
import com.cryptium.coincurrently.ui.main.Compose.primaryTextColor
import com.cryptium.coincurrently.ui.main.Compose.surfaceContainerHigh
import com.cryptium.coincurrently.ui.main.Compose.transparent
import com.cryptium.coincurrently.ui.main.analytics.AnalyticsManager
import com.cryptium.coincurrently.ui.main.analytics.ScreenLabel
import com.cryptium.coincurrently.ui.main.analytics.TimeframeChanged
import networking.ResponseModels.ChartButton
import networking.ResponseModels.TimeCharts

@Composable
fun ChartTimeframeButtonsView(
    buttons: List<ChartButton>,
    selectedTimeCharts: TimeCharts,
    fromScreen: ScreenLabel,
    buttonPressed: (ChartButton) -> Unit
) {
    val talkBackEnabled = AccessibilityLayout.isTalkBackEnabled
    var voiceOverTimeframeIndex by remember { mutableIntStateOf(buttons.indexOfFirst { it.timeChart == selectedTimeCharts }.coerceAtLeast(0)) }

    fun selectAdjacentTimeframe(offsetDelta: Int) {
        val newIndex = voiceOverTimeframeIndex + offsetDelta
        if (newIndex !in buttons.indices) return
        voiceOverTimeframeIndex = newIndex
    }

    LaunchedEffect(selectedTimeCharts) {
        val index = buttons.indexOfFirst { it.timeChart == selectedTimeCharts }
        if (index >= 0) {
            voiceOverTimeframeIndex = index
        }
    }

    LaunchedEffect(voiceOverTimeframeIndex, talkBackEnabled) {
        if (!talkBackEnabled) return@LaunchedEffect
        val selectedButton = buttons.getOrNull(voiceOverTimeframeIndex) ?: return@LaunchedEffect
        if (selectedButton.timeChart == selectedTimeCharts) return@LaunchedEffect
        buttonPressed(selectedButton)
    }

    val scrollState = rememberScrollState()
    val persistChartTimeframeThen = remember(fromScreen, selectedTimeCharts, buttonPressed) {
        { chartButton: ChartButton ->
            if (chartButton.timeChart != selectedTimeCharts) {
                AnalyticsManager.log(
                    TimeframeChanged(fromScreen = fromScreen, days = chartButton.days),
                )
            }
            TimeCharts.saveChartTimeframe(chartButton.timeChart)
            buttonPressed(chartButton)
        }
    }
    SubcomposeLayout(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (talkBackEnabled) {
                    Modifier.clearAndSetSemantics {
                        contentDescription = "Chart timeframe. Swipe up or down to change timeframe."
                        traversalIndex = CHART_TIMEFRAME_TALKBACK_TRAVERSAL_INDEX
                        val selectedButton = buttons.getOrNull(voiceOverTimeframeIndex)
                        stateDescription =
                            "Timeframe ${voiceOverTimeframeIndex + 1} of ${buttons.size}. ${selectedButton?.timeChart?.accessibilityText ?: ""}."
                        progressBarRangeInfo = ProgressBarRangeInfo(
                            current = voiceOverTimeframeIndex.toFloat(),
                            range = 0f..buttons.lastIndex.toFloat().coerceAtLeast(0f),
                            steps = buttons.lastIndex.coerceAtLeast(0),
                        )
                        setProgress(
                            action = { target ->
                                val current = voiceOverTimeframeIndex.toFloat()
                                val newIndex = when {
                                    target > current ->
                                        (voiceOverTimeframeIndex + 1).coerceAtMost(buttons.lastIndex)
                                    target < current ->
                                        (voiceOverTimeframeIndex - 1).coerceAtLeast(0)
                                    else -> voiceOverTimeframeIndex
                                }
                                if (newIndex != voiceOverTimeframeIndex) {
                                    voiceOverTimeframeIndex = newIndex
                                }
                                true
                            }
                        )
                        customActions = listOf(
                            CustomAccessibilityAction("Next timeframe") {
                                selectAdjacentTimeframe(1)
                                true
                            },
                            CustomAccessibilityAction("Previous timeframe") {
                                selectAdjacentTimeframe(-1)
                                true
                            },
                        )
                    }
                } else {
                    Modifier
                }
            )
    ) { constraints ->
        val intrinsicWidth = subcompose("intrinsic") {
            Row(horizontalArrangement = Arrangement.spacedBy(0.dp)) {
                buttons.forEach { chartButton ->
                    TimeChartButton(
                        chartButton = chartButton,
                        selectedTimeframe = selectedTimeCharts
                    ) {
                        persistChartTimeframeThen(chartButton)
                    }
                }
            }
        }.first().measure(
            constraints.copy(minWidth = 0, maxWidth = Constraints.Infinity)
        ).width

        // Spread row uses horizontal padding; compare against the inner width we actually give
        // children, or we falsely "fit", squeeze capsules, and clip labels (e.g. ALL → AL).
        val spreadInnerMaxWidth =
            (constraints.maxWidth - (32.dp).roundToPx()).coerceAtLeast(0)
        val fitsInRow = intrinsicWidth <= spreadInnerMaxWidth

        val placeable = subcompose(if (fitsInRow) "spread" else "scroll") {
            if (fitsInRow) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                ) {
                    buttons.forEach { chartButton ->
                        TimeChartButton(
                            chartButton = chartButton,
                            selectedTimeframe = selectedTimeCharts
                        ) {
                            persistChartTimeframeThen(chartButton)
                        }
                    }
                }
            } else {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .horizontalScroll(scrollState)
                ) {
                    buttons.forEach { chartButton ->
                        val isFirst = chartButton == buttons.first()
                        val isLast = chartButton == buttons.last()
                        TimeChartButton(
                            chartButton = chartButton,
                            selectedTimeframe = selectedTimeCharts,
                            modifier = Modifier.padding(
                                start = if (isFirst) 16.dp else 0.dp,
                                end = if (isLast) 16.dp else 0.dp
                            )
                        ) {
                            persistChartTimeframeThen(chartButton)
                        }
                    }
                }
            }
        }.first().measure(constraints)

        layout(constraints.maxWidth, placeable.height) {
            placeable.place(0, 0)
        }
    }
}

/** Timeframe capsule row; persists selection and forwards to [buttonPressed]. */
@Composable
fun ChartButtonsViewNew(
    buttons: List<ChartButton>,
    selectedTimeCharts: TimeCharts,
    fromScreen: ScreenLabel,
    buttonPressed: (ChartButton) -> Unit,
) {
    ChartTimeframeButtonsView(buttons, selectedTimeCharts, fromScreen, buttonPressed)
}

@Composable
fun TimeChartButton(
    chartButton: ChartButton,
    selectedTimeframe: TimeCharts,
    modifier: Modifier = Modifier,
    callback: (ChartButton) -> Unit
) {
    val talkBackEnabled = AccessibilityLayout.isTalkBackEnabled

    Box(
        contentAlignment = Center,
        modifier = modifier
            .wrapContentSize(Center)
            .clip(CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                callback(chartButton)
            }
            .background(
                color = if (chartButton.timeChart != selectedTimeframe) MaterialTheme.colors.transparent else MaterialTheme.colors.surfaceContainerHigh,
            )
            .then(
                if (talkBackEnabled) {
                    Modifier.semantics { invisibleToUser() }
                } else {
                    Modifier
                }
            )
    ) {
        Text(
            text = chartButton.time,
            style = poppinsSemibold(14),
            color = MaterialTheme.colors.primaryTextColor,
            textAlign = TextAlign.Center,
            maxLines = 1,
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .wrapContentSize()
        )
    }
}
