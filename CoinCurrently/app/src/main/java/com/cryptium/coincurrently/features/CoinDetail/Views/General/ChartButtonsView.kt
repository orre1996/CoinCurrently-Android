package com.cryptium.coincurrently.features.CoinDetail.Views.General
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.invisibleToUser
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.setProgress
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cryptium.coincurrently.sharedui.components.AutoSizeConstraint
import com.cryptium.coincurrently.sharedui.components.AutoSizeText
import com.cryptium.coincurrently.sharedui.components.CachedUrlImage
import com.cryptium.coincurrently.sharedui.modifiers.CappedFontScale
import com.cryptium.coincurrently.sharedui.modifiers.DynamicTypeSizeCap
import com.cryptium.coincurrently.R
import com.cryptium.coincurrently.sharedui.components.AccessibilityLayout
import com.cryptium.coincurrently.sharedui.theme.onSurfaceContainer
import com.cryptium.coincurrently.sharedui.modifiers.scaledImageSize
import com.cryptium.coincurrently.sharedui.theme.surfaceContainerHigh
import com.cryptium.coincurrently.core.extensions.satoshiBold
import com.cryptium.coincurrently.core.extensions.satoshiMedium
import com.cryptium.coincurrently.sharedui.components.ImageCase
import com.cryptium.coincurrently.features.CoinDetail.ChartTypes
import com.cryptium.coincurrently.core.analytics.ChartTypeChanged
import com.cryptium.coincurrently.core.analytics.AnalyticsManager
import com.cryptium.coincurrently.core.analytics.ScreenLabel

@Composable
@OptIn(ExperimentalLayoutApi::class)
fun ChartButtonsView(
    fromScreen: ScreenLabel,
    selectedGraphType: ChartTypes,
    isCandleChartEnabled: Boolean,
    isInCompareMode: Boolean,
    isAlwaysSelectable: Boolean = false,
    imageCase: ImageCase,
    chartTypeAction: (ChartTypes) -> Unit,
    toggleCandleChart: () -> Unit
) {
    val talkBackEnabled = AccessibilityLayout.isTalkBackEnabled
    val chartTypeOptions = chartTypeOptionsForScreen(fromScreen)
    val chartTypeTitles = chartTypeOptions.associate { it.type to it.title }

    fun isChartTypeDisabled(type: ChartTypes): Boolean = when (type) {
        ChartTypes.PRICE -> false
        ChartTypes.VOLUME, ChartTypes.MKTCAP -> isCandleChartEnabled && !isAlwaysSelectable
    }

    fun isChartTypeSelected(type: ChartTypes): Boolean = when (type) {
        ChartTypes.PRICE ->
            selectedGraphType == ChartTypes.PRICE || (isCandleChartEnabled && !isAlwaysSelectable)
        ChartTypes.VOLUME ->
            selectedGraphType == ChartTypes.VOLUME && (!isCandleChartEnabled || isAlwaysSelectable)
        ChartTypes.MKTCAP ->
            selectedGraphType == ChartTypes.MKTCAP && (!isCandleChartEnabled || isAlwaysSelectable)
    }

    val selectableChartTypes = chartTypeOptions.filter { !isChartTypeDisabled(it.type) }
    var voiceOverChartTypeIndex by remember {
        mutableIntStateOf(
            selectableChartTypes.indexOfFirst { it.type == selectedGraphType }.coerceAtLeast(0)
        )
    }

    fun selectAdjacentChartType(offsetDelta: Int) {
        val newIndex = voiceOverChartTypeIndex + offsetDelta
        if (newIndex !in selectableChartTypes.indices) return
        voiceOverChartTypeIndex = newIndex
    }

    val selectChartType: (ChartTypes) -> Unit = { type ->
        if (type != selectedGraphType) {
            AnalyticsManager.log(
                ChartTypeChanged(fromScreen = fromScreen, chartType = type),
            )
        }
        chartTypeAction(type)
    }

    LaunchedEffect(selectedGraphType, isCandleChartEnabled, selectableChartTypes) {
        val index = selectableChartTypes.indexOfFirst { it.type == selectedGraphType }
        if (index >= 0) {
            voiceOverChartTypeIndex = index
        }
    }

    LaunchedEffect(voiceOverChartTypeIndex, talkBackEnabled) {
        if (!talkBackEnabled) return@LaunchedEffect
        val newType = selectableChartTypes.getOrNull(voiceOverChartTypeIndex)?.type ?: return@LaunchedEffect
        if (!isChartTypeSelected(newType)) {
            selectChartType(newType)
        }
    }

    val candleToggleLabel = when (fromScreen) {
        ScreenLabel.NftDetail -> {
            if (isCandleChartEnabled) "Switch to Fiat chart" else "Switch to NFT chart"
        }
        ScreenLabel.TreasuryDetail -> {
            if (isCandleChartEnabled) "Switch to Fiat chart" else "Switch to native chart"
        }
        else -> {
            if (isCandleChartEnabled) "Switch to line chart" else "Switch to candle chart"
        }
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .padding(
                top = 34.dp,
                bottom = 16.dp,
                start = 16.dp,
                end = 16.dp
            )
    ) {
        CappedFontScale(DynamicTypeSizeCap.Accessibility2) {
            FlowRow(
                verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .weight(1f)
                    .then(
                        if (talkBackEnabled) {
                            Modifier.clearAndSetSemantics {
                                contentDescription =
                                    "Chart type selector. Swipe up or down to change chart type."
                                val selectedType = selectableChartTypes.getOrNull(voiceOverChartTypeIndex)?.type
                                stateDescription =
                                    "Chart type ${voiceOverChartTypeIndex + 1} of ${selectableChartTypes.size}, ${chartTypeTitles[selectedType] ?: ""}."
                                progressBarRangeInfo = ProgressBarRangeInfo(
                                    current = voiceOverChartTypeIndex.toFloat(),
                                    range = 0f..selectableChartTypes.lastIndex.toFloat().coerceAtLeast(0f),
                                    steps = selectableChartTypes.lastIndex.coerceAtLeast(0),
                                )
                                setProgress(
                                    action = { target ->
                                        val current = voiceOverChartTypeIndex.toFloat()
                                        val newIndex = when {
                                            target > current ->
                                                (voiceOverChartTypeIndex + 1).coerceAtMost(selectableChartTypes.lastIndex)
                                            target < current ->
                                                (voiceOverChartTypeIndex - 1).coerceAtLeast(0)
                                            else -> voiceOverChartTypeIndex
                                        }
                                        if (newIndex != voiceOverChartTypeIndex) {
                                            voiceOverChartTypeIndex = newIndex
                                        }
                                        true
                                    }
                                )
                                customActions = listOf(
                                    CustomAccessibilityAction("Next chart type") {
                                        selectAdjacentChartType(1)
                                        true
                                    },
                                    CustomAccessibilityAction("Previous chart type") {
                                        selectAdjacentChartType(-1)
                                        true
                                    },
                                )
                            }
                        } else {
                            Modifier
                        }
                    )
            ) {
                chartTypeOptions.forEach { option ->
                    ChartButton(
                        title = option.title,
                        isSelected = isChartTypeSelected(option.type),
                        isDisabled = isChartTypeDisabled(option.type),
                        hideFromTalkBack = talkBackEnabled,
                        onClick = {
                            selectChartType(option.type)
                        }
                    )
                }
            }

            if (!isInCompareMode) {
                CandleChartToggleButton(
                    toggleCandleChart = toggleCandleChart,
                    imageCase = imageCase,
                    accessibilityLabel = candleToggleLabel,
                )
            }
        }
    }
}

private data class ChartTypeOption(
    val title: String,
    val type: ChartTypes,
)

private fun chartTypeOptionsForScreen(fromScreen: ScreenLabel): List<ChartTypeOption> {
    return when (fromScreen) {
        ScreenLabel.GlobalMarket -> listOf(
            ChartTypeOption("Volume", ChartTypes.VOLUME),
            ChartTypeOption("Market Cap", ChartTypes.MKTCAP),
        )
        ScreenLabel.TreasuryDetail -> listOf(
            ChartTypeOption("Price", ChartTypes.PRICE),
        )
        else -> listOf(
            ChartTypeOption("Price", ChartTypes.PRICE),
            ChartTypeOption("Volume", ChartTypes.VOLUME),
            ChartTypeOption("Market Cap", ChartTypes.MKTCAP),
        )
    }
}

@Composable
private fun ChartButton(
    title: String,
    isSelected: Boolean,
    isDisabled: Boolean,
    hideFromTalkBack: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) MaterialTheme.colors.primary else MaterialTheme.colors.surfaceContainerHigh
    val textColor = if (isSelected) MaterialTheme.colors.onPrimary else MaterialTheme.colors.onSurfaceContainer
    val alpha = if (isDisabled) 0.35f else 1f

    Text(
        text = title,
        style = satoshiMedium(16),
        color = textColor,
        modifier = Modifier
            .clip(CircleShape)
            .alpha(alpha)
            .background(backgroundColor)
            .clickable {
                if (!isDisabled) {
                    onClick.invoke()
                }
            }
            .then(
                if (hideFromTalkBack) {
                    Modifier.semantics { invisibleToUser() }
                } else {
                    Modifier
                }
            )
            .padding(
                horizontal = 16.dp,
                vertical = 12.dp
            )
    )
}

@Composable
private fun CandleChartToggleButton(
    imageCase: ImageCase,
    toggleCandleChart: () -> Unit,
    accessibilityLabel: String,
) {
    val iconSize = scaledImageSize(24.dp)

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .clip(CircleShape)
            .clickable {
                toggleCandleChart.invoke()
            }
            .background(MaterialTheme.colors.surfaceContainerHigh)
            .semantics(mergeDescendants = true) {
                contentDescription = accessibilityLabel
                role = Role.Button
            }
            .padding(12.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(iconSize)
        ) {
            when (imageCase) {
                is ImageCase.Image -> {
                    CachedUrlImage(
                        model = imageCase.value,
                        size = 24.dp,
                        contentScale = ContentScale.Crop,
                    )
                }
                is ImageCase.Fixed -> {
                    Image(
                        painter = painterResource(imageCase.image),
                        colorFilter = ColorFilter.tint(MaterialTheme.colors.onSurface),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
                is ImageCase.Text -> {
                    AutoSizeText(
                        text = imageCase.text,
                        style = satoshiBold(18),
                        color = MaterialTheme.colors.onSurface,
                        maxLines = 1,
                        softWrap = false,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxSize(),
                        constraint = AutoSizeConstraint.Width(min = 9.sp),
                    )
                }
                else -> { }
            }
        }
    }
}
