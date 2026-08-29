package com.cryptium.coincurrently.ui.main.HomeTab.GasFees.Popups

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
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
import com.cryptium.coincurrently.ui.main.Compose.Separator
import com.cryptium.coincurrently.ui.main.Compose.onSurface
import com.cryptium.coincurrently.ui.main.Compose.onSurfaceDim
import com.cryptium.coincurrently.ui.main.Compose.surfaceContainerLow
import com.cryptium.coincurrently.ui.main.Extensions.satoshiBold
import com.cryptium.coincurrently.ui.main.Extensions.satoshiRegular
import com.cryptium.coincurrently.ui.main.General.DragHandle
import com.cryptium.coincurrently.ui.main.Compose.ModalSheetBody
import com.cryptium.coincurrently.ui.main.Compose.rememberModalSheetMaxBodyHeight
import com.cryptium.coincurrently.ui.main.Compose.ModalBottomSheetSystemNavigationBar
import kotlinx.coroutines.launch

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun GasCostHelpSheet(
    close: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
    )

    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val maxBodyHeight = rememberModalSheetMaxBodyHeight(
        dragHandleTitle = "Estimated Cost",
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
                title = "Estimated Cost",
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
                OverviewView()

                CalculationView()

                MeaningView()
            }
        }
    }
}

@Composable
private fun OverviewView() {
    Text(
        text = "These are approximate fees for common Ethereum transactions based on the current network gas price. Actual costs may vary depending on network activity and transaction complexity.",
        style = satoshiRegular(16),
        color = MaterialTheme.colors.onSurfaceDim,
        lineHeight = 24.sp
    )
}

@Composable
private fun CalculationView() {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "How estimates are calculated",
            style = satoshiBold(16),
            color = MaterialTheme.colors.onSurface,
            modifier = Modifier.semantics { heading() },
        )

        BulletRow(
            text = "Uses the latest ETH gas price (Gwei).",
            accessibilityLabel = "Uses the latest Ethereum gas price, Gwei.",
        )

        BulletRow(
            text = "Applies typical gas usage for each action.",
        )

        BulletRow(
            text = "Converts result to USD using current ETH price.",
            accessibilityLabel = "Converts result to USD using current Ethereum price.",
        )
    }
}

@Composable
private fun MeaningView() {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "What each action means",
            style = satoshiBold(16),
            color = MaterialTheme.colors.onSurface,
            modifier = Modifier.semantics { heading() },
        )

        BulletRow(
            boldPrefix = "Swap: ",
            text = "Swapping tokens on a decentralized exchange.",
        )

        BulletRow(
            boldPrefix = "NFT Sale: ",
            text = "Listing or selling an NFT.",
        )

        BulletRow(
            boldPrefix = "Bridging: ",
            text = "Moving assets between networks.",
        )

        BulletRow(
            boldPrefix = "Borrowing: ",
            text = "Opening a lending position on a DeFi protocol.",
        )
    }
}

@Composable
private fun BulletRow(
    text: String,
    accessibilityLabel: String? = null,
    boldPrefix: String? = null,
) {
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = "·",
            style = satoshiRegular(16),
            color = MaterialTheme.colors.onSurfaceDim,
            modifier = Modifier.semantics { invisibleToUser() },
        )

        if (boldPrefix != null) {
            Text(
                buildAnnotatedString {
                    withStyle(
                        style = SpanStyle(
                            fontFamily = FontFamily(Font(R.font.satoshi_bold)),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colors.onSurfaceDim
                        )
                    ) {
                        append(boldPrefix)
                    }

                    append(text)
                },
                style = satoshiRegular(16),
                color = MaterialTheme.colors.onSurfaceDim,
                modifier = if (accessibilityLabel != null) {
                    Modifier.semantics { contentDescription = accessibilityLabel }
                } else {
                    Modifier
                },
            )
        } else {
            Text(
                text = text,
                style = satoshiRegular(16),
                color = MaterialTheme.colors.onSurfaceDim,
                modifier = if (accessibilityLabel != null) {
                    Modifier.semantics { contentDescription = accessibilityLabel }
                } else {
                    Modifier
                },
            )
        }
    }
}
