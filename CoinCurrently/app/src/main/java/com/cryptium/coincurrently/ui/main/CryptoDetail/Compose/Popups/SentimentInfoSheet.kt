package com.cryptium.coincurrently.ui.main.CryptoDetail.Compose.Popups

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.cryptium.coincurrently.R
import com.cryptium.coincurrently.ui.main.Compose.Separator
import com.cryptium.coincurrently.ui.main.Compose.onSurfaceDim
import com.cryptium.coincurrently.ui.main.Compose.surfaceContainerLow
import com.cryptium.coincurrently.ui.main.Extensions.satoshiRegular
import com.cryptium.coincurrently.ui.main.General.DragHandle
import com.cryptium.coincurrently.ui.main.Compose.ModalBottomSheetSystemNavigationBar
import com.cryptium.coincurrently.ui.main.Compose.rememberModalSheetMaxBodyHeight
import com.cryptium.coincurrently.ui.main.Compose.ModalSheetBody
import kotlinx.coroutines.launch

@Composable
@OptIn(ExperimentalStdlibApi::class, ExperimentalMaterial3Api::class)
fun SentimentInfoSheet(
    close: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val maxBodyHeight = rememberModalSheetMaxBodyHeight(dragHandleTitle = "Community Sentiment", dragHandleIcon = R.drawable.info)

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
                title = "Community Sentiment",
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
            Column(
                modifier = Modifier.fillMaxWidth(),
            ) {

            Separator()

            Text(
                text = "Community Sentiment reflects votes from CoinGecko users on whether they feel bullish or bearish about this asset. It does not represent financial advice or market forecasts.",
                style = satoshiRegular(18),
                color = MaterialTheme.colors.onSurfaceDim,
                modifier = Modifier
                    .padding(
                        vertical = 24.dp,
                        horizontal = 16.dp
                    )
            )
        }
    }
}
}
