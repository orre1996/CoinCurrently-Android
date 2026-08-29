package com.cryptium.coincurrently.ui.main.MarketTab.CryptoDetail.Popups

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cryptium.coincurrently.R
import com.cryptium.coincurrently.ui.main.Compose.AccessibilityLayout
import com.cryptium.coincurrently.ui.main.Compose.AdaptiveStack
import com.cryptium.coincurrently.ui.main.Compose.CloseButton
import com.cryptium.coincurrently.ui.main.Compose.PrimaryButton
import com.cryptium.coincurrently.ui.main.Compose.Separator
import com.cryptium.coincurrently.ui.main.Compose.onSurfaceDim
import com.cryptium.coincurrently.ui.main.Compose.onSurfaceDimmer
import com.cryptium.coincurrently.ui.main.Compose.outline
import com.cryptium.coincurrently.ui.main.Compose.surfaceContainerHigh
import com.cryptium.coincurrently.ui.main.Compose.surfaceContainerLow
import com.cryptium.coincurrently.ui.main.Compose.surfaceContainerLowest
import com.cryptium.coincurrently.ui.main.Extensions.dpToPx
import com.cryptium.coincurrently.ui.main.Extensions.pxToDp
import com.cryptium.coincurrently.ui.main.Extensions.satoshiBold
import com.cryptium.coincurrently.ui.main.Extensions.satoshiMedium
import com.cryptium.coincurrently.ui.main.General.DragHandle
import com.cryptium.coincurrently.ui.main.Compose.ModalBottomSheetSystemNavigationBar
import com.cryptium.coincurrently.ui.main.Compose.rememberModalSheetMaxBodyHeight
import com.cryptium.coincurrently.ui.main.Compose.ModalSheetBody
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddToWidgetSheet(
    widgetsCount: Int,
    deepLinkToWidget: () -> Unit,
    close: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val maxBodyHeight = rememberModalSheetMaxBodyHeight(dragHandleTitle = "Add to Widget")

    ModalBottomSheet(
        onDismissRequest = {
            coroutineScope.launch {
                close.invoke()
            }
        },
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        containerColor = MaterialTheme.colors.surfaceContainerLow,
        tonalElevation = 16.dp,
        contentWindowInsets = { WindowInsets(0,0 ,0 ,0) },
        dragHandle = {
            DragHandle(
                title = "Add to Widget",
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
            wrapContent = false,
        ) {
            Column(
            modifier = Modifier
                    .padding(horizontal = 16.dp)
            ) {

            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
            ) {
                Separator()

                BodyView()
            }

            Separator(padding = PaddingValues(vertical = 24.dp))

            ProgressView(
                widgetsCount = widgetsCount
            )

            Separator(padding = PaddingValues(vertical = 24.dp))

            ActionButtons(
                dismiss = {
                    coroutineScope.launch {
                        sheetState.hide()
                        close.invoke()
                    }
                },
                deepLinkToWidget = {
                    coroutineScope.launch {
                        sheetState.hide()
                        deepLinkToWidget.invoke()
                        close.invoke()
                    }
                }
            )
        }
    }
}

}
@Composable
private fun HeaderView(
    closeAction: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
                    .padding(bottom = 16.dp)
    ) {
        Text(
            text = "Add to Widget",
            style = satoshiBold(20),
            color = MaterialTheme.colors.onSurface
        )

        Spacer(Modifier.weight(1f))

        CloseButton(
            onClick = {
                closeAction.invoke()
            }
        )
    }
}

@Composable
private fun BodyView() {
    Column(
        verticalArrangement = Arrangement.spacedBy(24.dp),
        modifier = Modifier
                    .padding(top = 16.dp)
    ) {
        Text(
            text = "This coin will be added to your CoinCurrently widget. You can add up to 8 coins.",
            style = satoshiMedium(16),
            color = MaterialTheme.colors.onSurfaceDim
        )

        Text(
            text = "If you haven’t added the widget yet, long-press your home screen, tap +, and select CoinCurrently",
            style = satoshiMedium(16),
            color = MaterialTheme.colors.onSurfaceDim
        )

        Text(
            text = buildAnnotatedString {
                append("Manage the coins displayed on your widget anytime in ")

                withStyle(
                    style = SpanStyle(
                        fontFamily = FontFamily(Font(R.font.satoshi_bold)),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colors.onSurfaceDim
                    )
                ) {
                    append("Settings > My Widgets.")
                }
            },
            style = satoshiMedium(16),
            color = MaterialTheme.colors.onSurfaceDim
        )
    }
}

@Composable
private fun ProgressView(
    widgetsCount: Int
) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val localDensity = LocalDensity.current
    var progressWidth by remember { mutableStateOf(0.dp) }

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Coins Added",
                style = satoshiBold(12),
                color = MaterialTheme.colors.onSurface
            )

            Spacer(Modifier.weight(1f))

            Text(
                text = buildAnnotatedString {
                    append(widgetsCount.toString())

                    withStyle(
                        style = SpanStyle(
                            fontFamily = FontFamily(Font(R.font.satoshi_bold)),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colors.onSurfaceDimmer
                        )
                    ) {
                        append(" of 8")
                    }
                },
                style = satoshiBold(12),
                color = MaterialTheme.colors.onSurfaceDimmer
            )
        }

        Box(
            modifier = Modifier
                .clip(CircleShape)
                .background(MaterialTheme.colors.surfaceContainerLowest)
                .fillMaxWidth()
                .height(8.dp)
                .onGloballyPositioned { coordinates ->
                    progressWidth = with(localDensity) { coordinates.size.width.toDp() }
                }
        ) {
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .widthIn(
                        min = (widgetsCount.toDouble() / 8.0).pxToDp() * progressWidth.dpToPx(),
                        max = screenWidth
                    )
                    .background(MaterialTheme.colors.primary)
            )
        }

        Text(
            text = "Coin added to your widgets.",
            style = satoshiMedium(14),
            color = MaterialTheme.colors.onSurfaceDim
        )
    }
}

@Composable
private fun ActionButtons(
    dismiss: () -> Unit,
    deepLinkToWidget: () -> Unit
) {
    val isHorizontal = AccessibilityLayout.isHorizontal

    AdaptiveStack(
        isHorizontal = isHorizontal,
        spacing = 12.dp,
        horizontalAlignment = Alignment.Start,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
    ) {
        Text(
            text = "Close",
            style = satoshiBold(18),
            color = MaterialTheme.colors.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = sectionModifier()
                .clickable {
                    dismiss.invoke()
                }
                .clip(CircleShape)
                .background(MaterialTheme.colors.surfaceContainerHigh)
                .padding(
                    horizontal = 24.dp,
                    vertical = 12.dp
                )
                .widthIn(min = 100.dp)
        )

        PrimaryButton(
            text = "Manage Widgets",
            fullWidth = true,
            onClick = {
                deepLinkToWidget.invoke()
            },
            modifier = sectionModifier()
        )
    }
}
