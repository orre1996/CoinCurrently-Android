package com.cryptium.coincurrently.features.Settings.Sheets
import com.cryptium.coincurrently.sharedui.modifiers.imageSize

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.cryptium.coincurrently.R
import com.cryptium.coincurrently.sharedui.buttons.PrimaryButton
import com.cryptium.coincurrently.sharedui.components.Separator
import com.cryptium.coincurrently.sharedui.theme.onSurfaceDim
import com.cryptium.coincurrently.sharedui.theme.surfaceContainerLow
import com.cryptium.coincurrently.core.extensions.satoshiBold
import com.cryptium.coincurrently.core.extensions.satoshiRegular
import com.cryptium.coincurrently.core.localization.Localization
import com.cryptium.coincurrently.sharedui.components.DragHandle
import com.cryptium.coincurrently.sharedui.sheets.ModalBottomSheetSystemNavigationBar
import com.cryptium.coincurrently.sharedui.sheets.rememberModalSheetMaxBodyHeight
import com.cryptium.coincurrently.sharedui.sheets.ModalSheetBody
import kotlinx.coroutines.launch

data class WhatsNewSection(
    val title: String,
    val icon: Int,
    val bulletPoints: List<String>,
)

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun WhatsNewSheet(
    close: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
    )

    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val maxBodyHeight = rememberModalSheetMaxBodyHeight(dragHandleTitle = "What's New")

    val sections: List<WhatsNewSection> = listOf(
        WhatsNewSection(
            title = "Free",
            icon = R.drawable.gift,
            bulletPoints = listOf(
                "The paywall has been removed",
                "CoinCurrently is completely free, and every feature is included",
                "There is no subscription to buy, renew, or restore",
            )
        ),
        WhatsNewSection(
            title = "Open source",
            icon = R.drawable.repository,
            bulletPoints = listOf(
                "The project is now open source, so you can read the code and see what the app does with your data",
                "Want to contribute? The iOS and Android repositories are linked under Source code in Settings",
            )
        )
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
                title = "What's New",
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
                Separator(
                    padding = PaddingValues(horizontal = 16.dp)
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(32.dp),
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .padding(
                            start = 16.dp,
                            end = 16.dp,
                            top = 24.dp,
                            bottom = 24.dp
                        )
                ) {
                    sections.forEach {
                        WhatsNewSectionView(it)
                    }
                }

                Separator(
                    padding = PaddingValues(horizontal = 16.dp)
                )

                PrimaryButton(
                    text = Localization.string("Explore"),
                    fullWidth = true,
                    modifier = Modifier
                        .padding(
                            start = 16.dp,
                            end = 16.dp,
                            top = 24.dp,
                            bottom = 24.dp
                        )
                ) {
                    coroutineScope.launch {
                        sheetState.hide()
                        close.invoke()
                    }
                }
            }
        }
    }
}

@Composable
private fun WhatsNewSectionView(section: WhatsNewSection) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.Top
        ) {
            Image(
                painter = painterResource(id = section.icon),
                contentDescription = null,
                colorFilter = ColorFilter.tint(MaterialTheme.colors.primary),
                modifier = Modifier
                    .imageSize(16.dp)
            )

            Text(
                text = Localization.string(section.title),
                style = satoshiBold(16),
                color = MaterialTheme.colors.onSurface
            )
        }

        section.bulletPoints.forEach {
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "·",
                    style = satoshiRegular(16),
                    color = MaterialTheme.colors.onSurfaceDim
                )

                Text(
                    text = Localization.string(it),
                    style = satoshiRegular(16),
                    color = MaterialTheme.colors.onSurfaceDim
                )
            }
        }
    }
}
