package com.cryptium.coincurrently.ui.main.MarketTab.NFT.Popups

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.cryptium.coincurrently.ui.main.Compose.Separator
import com.cryptium.coincurrently.ui.main.Compose.surfaceContainerLow
import com.cryptium.coincurrently.ui.main.General.Components.ImageCase
import com.cryptium.coincurrently.ui.main.General.Components.SingleSelectView
import com.cryptium.coincurrently.ui.main.General.DragHandle
import kotlinx.coroutines.launch
import com.cryptium.coincurrently.ui.main.Compose.ModalBottomSheetSystemNavigationBar
import com.cryptium.coincurrently.ui.main.Compose.rememberModalSheetMaxBodyHeight
import com.cryptium.coincurrently.ui.main.Compose.ModalSheetBody
import networking.ResponseModels.NFTPlatform

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun NFTsPlatformsSheet(
    allPlatforms: List<NFTPlatform>,
    selectedPlatform: NFTPlatform,
    newOptionSelected: (NFTPlatform) -> Unit,
    close: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val maxBodyHeight = rememberModalSheetMaxBodyHeight(dragHandleTitle = "Filter By Chain")

    ModalBottomSheet(
        onDismissRequest = {
            coroutineScope.launch {
                sheetState.hide()
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
                title = "Filter By Chain",
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
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(4.dp),
            contentPadding = PaddingValues(
                vertical = 8.dp
            ),
            modifier = Modifier.fillMaxWidth(),
            ) {

            item {
                Separator()
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }

            items(allPlatforms) { platform ->
                SingleSelectView(
                    title = platform.name ?:  "",
                    subtitle = null,
                    imageCase = if(platform.image == null) ImageCase.Nothing else ImageCase.Image(platform.image.small ?: platform.image.thumb ?: ""),
                    isSelected = selectedPlatform == platform,
                    didSelectOption = {
                        coroutineScope.launch {
                            newOptionSelected.invoke(platform)
                            sheetState.hide()
                            close.invoke()
                        }
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
}
