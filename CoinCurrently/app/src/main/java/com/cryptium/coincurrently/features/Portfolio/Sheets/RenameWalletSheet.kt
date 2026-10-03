package com.cryptium.coincurrently.features.Portfolio.Sheets
import com.cryptium.coincurrently.core.localization.Localization
import com.cryptium.coincurrently.core.networking.ResponseModels.Chain
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.cryptium.coincurrently.sharedui.components.AccessibilityLayout
import com.cryptium.coincurrently.sharedui.components.AdaptiveStack
import com.cryptium.coincurrently.sharedui.buttons.MenuButton
import com.cryptium.coincurrently.sharedui.buttons.PrimaryButton
import com.cryptium.coincurrently.sharedui.buttons.SecondaryButton
import com.cryptium.coincurrently.sharedui.components.Separator
import com.cryptium.coincurrently.sharedui.theme.onSurfaceDim
import com.cryptium.coincurrently.sharedui.theme.outline
import com.cryptium.coincurrently.sharedui.theme.surfaceContainerLow
import com.cryptium.coincurrently.sharedui.theme.trendDown
import com.cryptium.coincurrently.core.extensions.satoshiBold
import com.cryptium.coincurrently.core.extensions.satoshiMedium
import com.cryptium.coincurrently.sharedui.components.DragHandle
import com.cryptium.coincurrently.sharedui.inputs.TextInputField
import com.cryptium.coincurrently.features.Portfolio.ViewModels.PortfolioViewModel
import com.cryptium.coincurrently.sharedui.sheets.ModalBottomSheetSystemNavigationBar
import com.cryptium.coincurrently.sharedui.sheets.rememberModalSheetMaxBodyHeight
import com.cryptium.coincurrently.sharedui.sheets.ModalSheetBody
import kotlinx.coroutines.launch

@Composable
@OptIn(ExperimentalStdlibApi::class, ExperimentalMaterial3Api::class)
fun RenameWalletSheet(
    initialName: String,
    renameWallet: (String?) -> Unit
) {
    val walletName = remember { mutableStateOf(initialName) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val maxBodyHeight = rememberModalSheetMaxBodyHeight(dragHandleTitle = "Rename Wallet")

    ModalBottomSheet(
        onDismissRequest = {
            coroutineScope.launch {
                renameWallet.invoke(null)
            }
        },
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        containerColor = MaterialTheme.colors.surfaceContainerLow,
        tonalElevation = 16.dp,
        contentWindowInsets = { WindowInsets(0,0 ,0 ,0) },
        dragHandle = {
            DragHandle(
                title = "Rename Wallet",
                closeAction = {
                    coroutineScope.launch {
                        sheetState.hide()
                        renameWallet.invoke(null)
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
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                    .padding(horizontal = 16.dp)
            ) {

            Separator()

            WalletNameField(
                nameInput = walletName
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Separator()

                ActionButtons(
                    walletName = walletName.value,
                    initialName = initialName,
                    close = {
                        coroutineScope.launch {
                            sheetState.hide()
                            renameWallet.invoke(null)
                        }
                    },
                    renameWallet = {
                        coroutineScope.launch {
                            sheetState.hide()
                            renameWallet.invoke(walletName.value)
                        }
                    }
                )
            }
        }
    }
}

}

@Composable
private fun WalletNameField(
    nameInput: MutableState<String>,
) {
    val accessibilityValue = if (nameInput.value.isEmpty()) {
        Localization.string("Enter wallet name, optional")
    } else {
        nameInput.value
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
                    .padding(vertical = 16.dp)
    ) {
        Row {
            TextInputField(
                input = nameInput.value,
                placeholder = "Enter name (Optional)",
                accessibilityLabel = accessibilityValue,
                onInputChange = {
                    nameInput.value = it
                },
                onDone = { },
                modifier = Modifier
                    .weight(1f)
            )
        }
    }
}

@Composable
private fun ActionButtons(
    walletName: String,
    initialName: String,
    close: () -> Unit,
    renameWallet: () -> Unit
) {
    val isHorizontal = AccessibilityLayout.isHorizontal
    val isEnabled = walletName.isNotEmpty() && initialName != walletName
    val saveTitle = Localization.string("Save")

    AdaptiveStack(
        isHorizontal = isHorizontal,
        spacing = 12.dp,
        horizontalAlignment = Alignment.Start,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
    ) {
        SecondaryButton(
            text = Localization.string("Close"),
            fullWidth = true,
            onClick = {
                close()
            },
            modifier = sectionModifier()
        )

        PrimaryButton(
            text = saveTitle,
            isEnabled = isEnabled,
            accessibilityLabel = saveTitle,
            fullWidth = true,
            onClick = {
                renameWallet.invoke()
            },
            modifier = sectionModifier()
        )
    }
}
