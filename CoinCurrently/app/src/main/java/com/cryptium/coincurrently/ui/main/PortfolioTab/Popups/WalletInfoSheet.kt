package com.cryptium.coincurrently.ui.main.PortfolioTab.Popups

import com.cryptium.coincurrently.ui.main.Compose.imageSize

import AddressImport
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
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
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.invisibleToUser
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.cryptium.coincurrently.R
import com.cryptium.coincurrently.ui.main.Compose.AccessibilityLayout
import com.cryptium.coincurrently.ui.main.Compose.ModalBottomSheetSystemNavigationBar
import com.cryptium.coincurrently.ui.main.Compose.ModalSheetBody
import com.cryptium.coincurrently.ui.main.Compose.Separator
import com.cryptium.coincurrently.ui.main.Compose.onSurfaceDim
import com.cryptium.coincurrently.ui.main.Compose.onSurfaceDimmer
import com.cryptium.coincurrently.ui.main.Compose.rememberModalSheetMaxBodyHeight
import com.cryptium.coincurrently.ui.main.Compose.surfaceContainerLow
import com.cryptium.coincurrently.ui.main.Extensions.DateFormat
import com.cryptium.coincurrently.ui.main.Extensions.accessibleLongDateDescription
import com.cryptium.coincurrently.ui.main.Extensions.accessibleTimeDescription
import com.cryptium.coincurrently.ui.main.Extensions.formatDate
import com.cryptium.coincurrently.ui.main.Extensions.is24HourClock
import com.cryptium.coincurrently.ui.main.Extensions.satoshiBold
import com.cryptium.coincurrently.ui.main.General.DragHandle
import com.cryptium.coincurrently.ui.main.PopupViews.SnackBarType
import com.cryptium.coincurrently.ui.main.PopupViews.SnackbarView
import com.cryptium.coincurrently.ui.main.PortfolioTab.Compose.accessibilityAddressDescription
import com.cryptium.coincurrently.ui.main.analytics.AnalyticsManager
import com.cryptium.coincurrently.ui.main.analytics.ScreenLabel
import com.cryptium.coincurrently.ui.main.analytics.WalletAddressCopied
import kotlinx.coroutines.launch
import networking.ResponseModels.Toplist
import java.util.Date

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun WalletInfoSheet(
    wallet: AddressImport,
    coin: Toplist,
    syncedDate: Date?,
    close: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()
    var showSnackbar by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val maxBodyHeight = rememberModalSheetMaxBodyHeight(dragHandleTitle = "Wallet Details")

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
        contentWindowInsets = { WindowInsets(0, 0, 0, 0) },
        dragHandle = {
            DragHandle(
                title = "Wallet Details",
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
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.BottomCenter,
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Separator(padding = PaddingValues(horizontal = 16.dp))

                    Column(
                        verticalArrangement = Arrangement.spacedBy(
                            16.dp
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .padding(
                                top = 24.dp,
                                start = 16.dp,
                                end = 16.dp,
                                bottom = 24.dp,
                            )
                    ) {
                        WalletInfoContent(
                            wallet = wallet,
                            coin = coin,
                            syncedDate = syncedDate,
                            showClipboardSnackbar = {
                                showSnackbar = !showSnackbar
                            },
                        )
                    }
                }

                SnackbarView(
                    showSnackBar = showSnackbar,
                    message = "Wallet address copied!",
                    buttonTitle = "",
                    type = SnackBarType.SUCCESS,
                    isTabbarHidden = true,
                    navBarHeight = 0.dp,
                    systemNavbarHeight = 0.dp,
                    onDismissRequest = { showSnackbar = false },
                    buttonAction = { }
                )
            }
        }
    }
}

@Composable
private fun WalletInfoContent(
    wallet: AddressImport,
    coin: Toplist,
    syncedDate: Date?,
    showClipboardSnackbar: () -> Unit,
) {
    val isHorizontal = AccessibilityLayout.isHorizontal

    if (isHorizontal) {
        StatRow(
            left = { modifier ->
                ImportDateView(
                    date = wallet.date,
                    modifier = modifier,
                )
            },
            right = { modifier ->
                LastSyncedView(
                    lastSynced = syncedDate ?: Date(),
                    modifier = modifier,
                )
            },
        )

        StatRow(
            left = { modifier ->
                NetworkView(
                    network = coin.symbol?.uppercase() ?: "",
                    coinName = coin.name ?: coin.symbol ?: "",
                    modifier = modifier,
                )
            },
            right = { modifier ->
                AddressView(
                    wallet = wallet,
                    showClipboardSnackbar = showClipboardSnackbar,
                    modifier = modifier,
                )
            },
        )
    } else {
        ImportDateView(
            date = wallet.date,
            modifier = Modifier.fillMaxWidth(),
        )
        StatSeparator()
        LastSyncedView(
            lastSynced = syncedDate ?: Date(),
            modifier = Modifier.fillMaxWidth(),
        )
        StatSeparator()
        NetworkView(
            network = coin.symbol?.uppercase() ?: "",
            coinName = coin.name ?: coin.symbol ?: "",
            modifier = Modifier.fillMaxWidth(),
        )
        StatSeparator()
        AddressView(
            wallet = wallet,
            showClipboardSnackbar = showClipboardSnackbar,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun StatRow(
    left: @Composable (Modifier) -> Unit,
    right: @Composable (Modifier) -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.Top,
        modifier = Modifier.fillMaxWidth(),
    ) {
        left(Modifier.weight(1f))
        right(Modifier.weight(1f))
    }
}

@Composable
private fun StatSeparator() {
    Separator(padding = PaddingValues(vertical = 8.dp))
}

@Composable
private fun StatView(
    title: String,
    value: String,
    valueAccessible: String? = null,
    modifier: Modifier = Modifier,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = if (valueAccessible != null) {
            modifier.semantics(mergeDescendants = true) {
                contentDescription = "$title: $valueAccessible"
            }
        } else {
            modifier.semantics(mergeDescendants = true) { }
        },
    ) {
        Text(
            text = title,
            style = satoshiBold(12),
            color = MaterialTheme.colors.onSurfaceDim,
            modifier = if (valueAccessible != null) {
                Modifier.semantics { invisibleToUser() }
            } else {
                Modifier
            },
        )

        Text(
            text = value,
            style = satoshiBold(16),
            color = MaterialTheme.colors.onSurface,
            modifier = if (valueAccessible != null) {
                Modifier.semantics { invisibleToUser() }
            } else {
                Modifier
            },
        )
    }
}

@Composable
private fun ImportDateView(
    date: Date,
    modifier: Modifier
) {
    StatView(
        title = "Date Imported",
        value = date.formatDate(DateFormat.MMMddyyyy) ?: "",
        valueAccessible = date.accessibleLongDateDescription(),
        modifier = modifier,
    )
}

@Composable
private fun LastSyncedView(
    lastSynced: Date,
    modifier: Modifier
) {
    val context = LocalContext.current
    val accessibleSynced =
        "${lastSynced.accessibleLongDateDescription()} at ${lastSynced.accessibleTimeDescription(context.is24HourClock)}"

    StatView(
        title = "Last Synced",
        value = lastSynced.formatDate(if (context.is24HourClock) DateFormat.MMMddyyyyHHmm else DateFormat.MMMddyyyyhhmma) ?: "",
        valueAccessible = accessibleSynced,
        modifier = modifier,
    )
}

@Composable
private fun NetworkView(
    network: String,
    coinName: String,
    modifier: Modifier
) {
    StatView(
        title = "Network",
        value = "$network Mainnet",
        valueAccessible = "$coinName Mainnet",
        modifier = modifier,
    )
}

@Composable
private fun AddressView(
    wallet: AddressImport,
    showClipboardSnackbar: () -> Unit,
    modifier: Modifier
) {
    val clipboardManager = LocalClipboardManager.current
    val address = wallet.address

    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.Top,
        modifier = modifier,
    ) {
        StatView(
            title = "Wallet Address",
            value = "${address.take(5).uppercase()}...${address.takeLast(5).uppercase()}",
            valueAccessible = accessibilityAddressDescription(wallet),
            modifier = Modifier.weight(1f),
        )

        Image(
            painter = painterResource(R.drawable.copy),
            contentDescription = "Copy wallet address",
            colorFilter = ColorFilter.tint(MaterialTheme.colors.onSurfaceDimmer),
            modifier = Modifier
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                ) {
                    AnalyticsManager.log(WalletAddressCopied(ScreenLabel.WalletDetails))
                    clipboardManager.setText(AnnotatedString(address))
                    showClipboardSnackbar.invoke()
                }
                .padding(12.dp)
                .imageSize(24.dp)
        )
    }
}
