package com.cryptium.coincurrently.features.Portfolio

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.cryptium.coincurrently.core.localization.Localization
import com.cryptium.coincurrently.R
import com.cryptium.coincurrently.core.config.AppLocale
import com.cryptium.coincurrently.core.networking.ResponseModels.Toplist
import com.cryptium.coincurrently.sharedui.buttons.PrimaryButton
import com.cryptium.coincurrently.sharedui.components.AccessibilityLayout
import com.cryptium.coincurrently.sharedui.modifiers.imageSize
import com.cryptium.coincurrently.sharedui.theme.*

@OptIn(ExperimentalAnimationApi::class)
@Composable
internal fun ActionButtons(
    isActionMenuExpanded: MutableState<Boolean>,
    hasTransactions: Boolean,
    benchmarkCoin: Toplist?,
    modifier: Modifier,
    addTransaction: () -> Unit,
    importWallet: () -> Unit,
    exportPortfolio: () -> Unit,
    selectBenchmark: () -> Unit,
    removeBenchmark: () -> Unit,
) {
    val talkBackEnabled = AccessibilityLayout.isTalkBackEnabled
    val isExpanded = isActionMenuExpanded.value

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.End,
        modifier = modifier
            .fillMaxSize()
            .then(
                if (talkBackEnabled && isExpanded) {
                    Modifier.semantics { isTraversalGroup = true }
                } else {
                    Modifier
                }
            )
            .conditional(isExpanded) {
                clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                ) {
                    isActionMenuExpanded.value = false
                }
            }
    ) {
        Spacer(modifier = Modifier.weight(1f))

        AnimatedVisibility(
            visible = isExpanded,
            enter = slideInHorizontally(
                initialOffsetX = { fullWidth -> fullWidth },
                animationSpec = tween(durationMillis = 300)
            ),
            exit = slideOutHorizontally(
                targetOffsetX = { fullWidth -> fullWidth },
                animationSpec = tween(durationMillis = 300)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.End,
                modifier = Modifier.fillMaxWidth()
            ) {
                PrimaryButton(
                    text = Localization.string("Add Transaction"),
                    onClick = {
                        addTransaction.invoke()
                        isActionMenuExpanded.value = false
                    },
                    modifier = Modifier
                        .padding(end = 16.dp)
                        .then(
                            if (talkBackEnabled) {
                                Modifier.semantics { traversalIndex = 1f }
                            } else {
                                Modifier
                            }
                        )
                )

                PrimaryButton(
                    text = Localization.string("Import Wallet"),
                    onClick = {
                        importWallet.invoke()
                        isActionMenuExpanded.value = false
                    },
                    modifier = Modifier
                        .padding(end = 16.dp)
                        .then(
                            if (talkBackEnabled) {
                                Modifier.semantics { traversalIndex = 2f }
                            } else {
                                Modifier
                            }
                        )
                )

                PrimaryButton(
                    text = Localization.string("Export Portfolio"),
                    accessibilityLabel = Localization.string("Export portfolio"),
                    isEnabled = hasTransactions,
                    onClick = {
                        exportPortfolio.invoke()
                        isActionMenuExpanded.value = false
                    },
                    modifier = Modifier
                        .padding(end = 16.dp)
                        .then(
                            if (talkBackEnabled) {
                                Modifier.semantics { traversalIndex = 3f }
                            } else {
                                Modifier
                            }
                        )
                )

                PrimaryButton(
                    text = if (benchmarkCoin == null) {
                        Localization.string("Benchmark")
                    } else {
                        Localization.format(
                            "Benchmark (%@)",
                            benchmarkCoin.symbol?.uppercase(AppLocale.enUsPosix) ?: "",
                        )
                    },
                    accessibilityLabel = if (benchmarkCoin == null) {
                        Localization.string("Benchmark portfolio")
                    } else {
                        Localization.format("Change benchmark, currently %@", benchmarkCoin.name ?: "")
                    },
                    onClick = {
                        selectBenchmark.invoke()
                        isActionMenuExpanded.value = false
                    },
                    modifier = Modifier
                        .padding(end = 16.dp)
                        .then(
                            if (talkBackEnabled) {
                                Modifier.semantics { traversalIndex = 4f }
                            } else {
                                Modifier
                            }
                        )
                )

                if (benchmarkCoin != null) {
                    PrimaryButton(
                        text = Localization.string("Remove Benchmark"),
                        accessibilityLabel = Localization.string("Remove benchmark"),
                        onClick = {
                            removeBenchmark.invoke()
                            isActionMenuExpanded.value = false
                        },
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .then(
                                if (talkBackEnabled) {
                                    Modifier.semantics { traversalIndex = 5f }
                                } else {
                                    Modifier
                                }
                            )
                    )
                }
            }
        }

        val backgroundColor by animateColorAsState(
            targetValue = if (isExpanded) MaterialTheme.colors.surfaceContainerHighest else MaterialTheme.colors.primary,
            animationSpec = tween(durationMillis = 300),
            label = ""
        )

        Image(
            painter = painterResource(R.drawable.plus),
            contentDescription = null,
            colorFilter = ColorFilter.tint(
                if (isExpanded) MaterialTheme.colors.onSurfaceContainerDim else MaterialTheme.colors.onPrimary
            ),
            modifier = Modifier
                .semantics {
                    contentDescription = if (isExpanded) {
                        Localization.string("Close add menu")
                    } else {
                        "${Localization.string("Add portfolio item")}. ${Localization.string("Shows add transaction, import wallet, export portfolio, and benchmark options")}"
                    }
                    if (talkBackEnabled && isExpanded) {
                        traversalIndex = 0f
                    }
                }
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                ) {
                    isActionMenuExpanded.value = !isActionMenuExpanded.value
                }
                .padding(
                    bottom = 16.dp,
                    end = 16.dp
                )
                .clip(CircleShape)
                .dropShadow(
                    shape = RoundedCornerShape(12.dp),
                    shadow = Shadow(
                        radius = 2.dp,
                        spread = 0.dp,
                        color = Color.Black.copy(alpha = 0.08f),
                        offset = DpOffset(0.dp, 2.dp)
                    )
                )
                .dropShadow(
                    shape = RoundedCornerShape(12.dp),
                    shadow = Shadow(
                        radius = 8.dp,
                        spread = 0.dp,
                        color = Color.Black.copy(alpha = 0.12f),
                        offset = DpOffset(0.dp, 4.dp)
                    )
                )
                .background(backgroundColor)
                .padding(16.dp)
                .imageSize(24.dp)
        )
    }
}

