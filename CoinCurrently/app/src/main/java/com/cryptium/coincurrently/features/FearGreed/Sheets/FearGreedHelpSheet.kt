package com.cryptium.coincurrently.features.FearGreed.Sheets
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.invisibleToUser
import androidx.compose.ui.semantics.semantics
import com.cryptium.coincurrently.sharedui.sheets.ModalSheetBody
import com.cryptium.coincurrently.sharedui.sheets.rememberModalSheetMaxBodyHeight
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
import com.cryptium.coincurrently.sharedui.theme.onSurfaceDim
import com.cryptium.coincurrently.sharedui.theme.onSurfaceDimmer
import com.cryptium.coincurrently.sharedui.theme.surfaceContainerLow
import com.cryptium.coincurrently.core.extensions.satoshiBold
import com.cryptium.coincurrently.core.extensions.satoshiRegular
import com.cryptium.coincurrently.sharedui.components.DragHandle
import com.cryptium.coincurrently.sharedui.charts.hideFromTalkBackWhenEnabled
import com.cryptium.coincurrently.features.FearGreed.FearGreedLevelExplanation
import com.cryptium.coincurrently.features.FearGreed.FearGreedPercentExplanation
import com.cryptium.coincurrently.sharedui.sheets.ModalBottomSheetSystemNavigationBar
import kotlinx.coroutines.launch

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun FearGreedHelpSheet(
    close: () -> Unit
) {
    val levelExplanations: List<FearGreedLevelExplanation> = listOf(
        FearGreedLevelExplanation(
            "Extreme Greed",
            MaterialTheme.colors.extremeGreed,
            " (80-100)",
            "Optimism is elevated and pullbacks become more likely. Consider reducing high-risk exposure."
        ),
        FearGreedLevelExplanation(
            "Greed",
            MaterialTheme.colors.greed,
            " (60-79)",
            "Confidence is rising and buying activity is increasing. Stay selective and avoid chasing rallies."
        ),
        FearGreedLevelExplanation(
            "Neutral",
            MaterialTheme.colors.neutral,
            " (40-59)",
            "Sentiment is balanced with no strong trend direction. Watch for momentum shifts."
        ),
        FearGreedLevelExplanation(
            "Fear",
            MaterialTheme.colors.fear,
            " (20-39)",
            "Caution is increasing across the market. Look for discounted quality assets."
        ),
        FearGreedLevelExplanation(
            "Extreme Fear",
            MaterialTheme.colors.extremeFear,
            " (0-19)",
            "Panic selling is widespread. Long-term investors often accumulate during these periods."
        )
    )

    val percentExplanations: List<FearGreedPercentExplanation> = listOf(
        FearGreedPercentExplanation(
            "Volatility",
            " (25%)",
            "Compares Bitcoin’s recent volatility and drawdowns to its 30- and 90-day averages. Higher volatility often signals fearful market conditions."
        ),
        FearGreedPercentExplanation(
            "Market Momentum / Volume",
            " (25%)",
            "Measures trading volume and price momentum against recent averages. Strong momentum typically reflects growing market confidence."
        ),
        FearGreedPercentExplanation(
            "Social Media",
            " (15%)",
            "Analyzes engagement across crypto discussions on X. Sudden increases often signal a bullish market."
        ),
        FearGreedPercentExplanation(
            "Surveys",
            " (15%) - Currently paused",
            "Weekly surveys are conducted on a polling platform to see what individuals are thinking of the markets."
        ),
        FearGreedPercentExplanation(
            "Dominance",
            " (10%)",
            "Tracks Bitcoin’s share of total crypto market capitalization. Higher dominance suggests a shift toward safer assets."
        ),
        FearGreedPercentExplanation(
            "Trends",
            " (10%)",
            "Weekly surveys were conducted to see what investors are thinking of the markets."
        )
    )

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
    )

    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val maxBodyHeight = rememberModalSheetMaxBodyHeight(
        dragHandleTitle = "Fear & Greed",
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
        contentWindowInsets = { WindowInsets(0,0 ,0 ,0) },
        dragHandle = {
            DragHandle(
                title = "Fear & Greed",
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

                Separator(
                    padding = PaddingValues(bottom = 8.dp)
                )

                Text(
                    text = "Index Factors",
                    style = satoshiBold(22),
                    color = MaterialTheme.colors.onSurfaceDim,
                    modifier = Modifier.semantics { heading() },
                )

                percentExplanations.forEach {
                    HelpSection(it)
                }
            }
        }
    }
}

@Composable
private fun LevelSection(
    explanation: FearGreedLevelExplanation,
) {
    val titleLabel = titlePercentAccessibilityLabel(
        title = explanation.title,
        percent = explanation.percent,
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
            .padding(bottom = 8.dp)
            .semantics(mergeDescendants = true) {
                contentDescription = "$titleLabel. ${explanation.body}"
            },
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
                    .hideFromTalkBackWhenEnabled()
            )

            Text(
                buildAnnotatedString {
                    append(explanation.title)

                    withStyle(
                        style = SpanStyle(
                            fontFamily = FontFamily(Font(R.font.satoshi_bold)),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colors.onSurfaceDimmer
                        )
                    ) {
                        append(explanation.percent)
                    }
                },
                style = satoshiBold(16),
                color = MaterialTheme.colors.onSurface,
                modifier = Modifier.semantics { invisibleToUser() },
            )
        }

        Text(
            explanation.body,
            style = satoshiRegular(16),
            color = MaterialTheme.colors.onSurfaceDim,
            modifier = Modifier.semantics { invisibleToUser() },
        )
    }
}

@Composable
private fun HelpSection(
    explanation: FearGreedPercentExplanation
) {
    val titleLabel = titlePercentAccessibilityLabel(
        title = explanation.title,
        percent = explanation.percent,
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
            .padding(bottom = 8.dp)
            .semantics(mergeDescendants = true) {
                contentDescription = "$titleLabel. ${explanation.body}"
            },
    ) {
        Text(
            buildAnnotatedString {
                append(explanation.title)

                withStyle(
                    style = SpanStyle(
                        fontFamily = FontFamily(Font(R.font.satoshi_bold)),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colors.onSurfaceDimmer
                    )
                ) {
                    append(explanation.percent)
                }
            },
            style = satoshiBold(16),
            color = MaterialTheme.colors.onSurface,
            modifier = Modifier.semantics { invisibleToUser() },
        )

        Text(
            explanation.body,
            style = satoshiRegular(16),
            color = MaterialTheme.colors.onSurfaceDim,
            modifier = Modifier.semantics { invisibleToUser() },
        )
    }
}

private fun titlePercentAccessibilityLabel(title: String, percent: String): String {
    return " ${title.lowercase()}, ${accessiblePercent(percent)}"
}

private fun accessiblePercent(percent: String): String {
    return percent
        .replace("(", "")
        .replace(")", "")
        .replace("-", " to ")
        .trim()
}