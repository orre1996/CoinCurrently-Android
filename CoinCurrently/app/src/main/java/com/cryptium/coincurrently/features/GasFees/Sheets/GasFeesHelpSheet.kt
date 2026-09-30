package com.cryptium.coincurrently.features.GasFees.Sheets

import com.cryptium.coincurrently.core.localization.Localization
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.material.MaterialTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.invisibleToUser
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cryptium.coincurrently.R
import com.cryptium.coincurrently.sharedui.components.Separator
import com.cryptium.coincurrently.sharedui.theme.extremeFear
import com.cryptium.coincurrently.sharedui.theme.extremeGreed
import com.cryptium.coincurrently.sharedui.theme.fear
import com.cryptium.coincurrently.sharedui.theme.greed
import com.cryptium.coincurrently.sharedui.theme.neutral
import com.cryptium.coincurrently.sharedui.theme.onSurface
import com.cryptium.coincurrently.sharedui.theme.onSurfaceDim
import com.cryptium.coincurrently.sharedui.theme.onSurfaceDimmer
import com.cryptium.coincurrently.sharedui.theme.surfaceContainerLow
import com.cryptium.coincurrently.core.extensions.satoshiBold
import com.cryptium.coincurrently.core.extensions.satoshiRegular
import com.cryptium.coincurrently.sharedui.components.DragHandle
import com.cryptium.coincurrently.sharedui.sheets.ModalSheetBody
import com.cryptium.coincurrently.sharedui.sheets.rememberModalSheetMaxBodyHeight
import com.cryptium.coincurrently.features.FearGreed.FearGreedLevelExplanation
import com.cryptium.coincurrently.sharedui.sheets.ModalBottomSheetSystemNavigationBar
import kotlinx.coroutines.launch

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun GasFeesHelpSheet(
    close: () -> Unit
) {
    val levelExplanations: List<FearGreedLevelExplanation> = listOf(
        FearGreedLevelExplanation(
            "Very Low",
            MaterialTheme.colors.extremeGreed,
            " (0-3 Gwei)",
            "Transactions are extremely cheap. Ideal time for swaps, transfers, and contract interactions."
        ),
        FearGreedLevelExplanation(
            "Low",
            MaterialTheme.colors.greed,
            " (3.1-10 Gwei)",
            "Transactions are cheaper than usual. Most actions should confirm quickly at low cost."
        ),
        FearGreedLevelExplanation(
            "Neutral",
            MaterialTheme.colors.neutral,
            " (10.1-30 Gwei)",
            "Standard fees and confirmation times expected. Typical network activity."
        ),
        FearGreedLevelExplanation(
            "High",
            MaterialTheme.colors.fear,
            " (30.1-80 Gwei)",
            "Transactions cost more and may take longer without higher fees. Network is busy."
        ),
        FearGreedLevelExplanation(
            "Very High",
            MaterialTheme.colors.extremeFear,
            " (80+ Gwei)",
            "Expect expensive fees and slower confirmations unless prioritized. Network congestion is heavy."
        )
    )

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
    )

    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val maxBodyHeight = rememberModalSheetMaxBodyHeight(
        dragHandleTitle = "ETH Gas Fee",
        dragHandleIcon = R.drawable.info,
    )

    ModalBottomSheet(
        onDismissRequest = {
            close.invoke()
        },
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        containerColor = MaterialTheme.colors.surfaceContainerLow,
        tonalElevation = 16.dp,
        contentWindowInsets = { WindowInsets(0, 0, 0, 0) },
        dragHandle = {
            DragHandle(
                title = "ETH Gas Fee",
                accessibilityTitle = "Ethereum gas fee",
                icon = R.drawable.info,
                closeAction = {
                    coroutineScope.launch {
                        sheetState.hide()
                        close.invoke()
                    }
                }
            )
        }
    ) {
        ModalBottomSheetSystemNavigationBar()
        ModalSheetBody(
            context = context,
            maxBodyHeight = maxBodyHeight,
        ) {
            Separator(
                padding = PaddingValues(horizontal = 16.dp)
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(
                        start = 16.dp,
                        end = 16.dp,
                        top = 24.dp,
                        bottom = 24.dp
                    )
            ) {
                levelExplanations.forEach {
                    LevelSection(it)
                }
            }
        }
    }
}

@Composable
private fun LevelSection(
    explanation: FearGreedLevelExplanation,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
            .semantics(mergeDescendants = true) {}
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .size(12.dp)
                    .background(explanation.color)
                    .semantics { invisibleToUser() }
            )

            Text(
                buildAnnotatedString {
                    append(Localization.string(explanation.title))

                    withStyle(
                        style = SpanStyle(
                            fontFamily = FontFamily(Font(R.font.satoshi_bold)),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colors.onSurfaceDimmer
                        )
                    ) {
                        append(" ${Localization.string(explanation.percent)}")
                    }
                },
                style = satoshiBold(16),
                color = MaterialTheme.colors.onSurface,
                modifier = Modifier
                    .clearAndSetSemantics {
                        contentDescription = titlePercentAccessibilityLabel(
                            title = explanation.title,
                            percent = explanation.percent,
                        )
                    }
            )
        }

        Text(
            Localization.string(explanation.body),
            style = satoshiRegular(16),
            color = MaterialTheme.colors.onSurfaceDim,
        )
    }
}

private fun titlePercentAccessibilityLabel(title: String, percent: String): String {
    return Localization.format("%@, %@", Localization.string(title), accessiblePercent(percent))
}

private fun accessiblePercent(percent: String): String {
    return when (percent) {
        " (0-3 Gwei)" -> Localization.string("0 to 3 Gwei")
        " (3.1-10 Gwei)" -> Localization.string("3.1 to 10 Gwei")
        " (10.1-30 Gwei)" -> Localization.string("10.1 to 30 Gwei")
        " (30.1-80 Gwei)" -> Localization.string("30.1 to 80 Gwei")
        " (80+ Gwei)" -> Localization.string("80 or more Gwei")
        else -> percent
    }
}
