package com.cryptium.coincurrently.features.Settings.CurrencySelector
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Text
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cryptium.coincurrently.core.extensions.poppinsRegular
import com.cryptium.coincurrently.core.extensions.poppinsSemibold
import com.cryptium.coincurrently.features.Settings.CurrencySelector.allCurrencies
import com.cryptium.coincurrently.features.Settings.isEdgeToEdgeEnabled
import com.cryptium.coincurrently.core.networking.ResponseModels.CurrencySelectionModel
import com.cryptium.coincurrently.sharedui.buttons.PrimaryButton
import com.cryptium.coincurrently.sharedui.components.Separator
import com.cryptium.coincurrently.sharedui.theme.overlay
import com.cryptium.coincurrently.sharedui.theme.primaryBackground
import com.cryptium.coincurrently.sharedui.theme.primaryBackgroundFullStroke
import com.cryptium.coincurrently.sharedui.theme.primaryTextColor

@Composable
fun CurrencySelectorView(closeView: () -> Unit, currencySelected: (CurrencySelectionModel) -> Unit) {
    val context = LocalContext.current

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.overlay)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        if (isEdgeToEdgeEnabled(context) == 2) {
                            closeView()
                        }
                    })
            }
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .widthIn(max = 352.dp)
                .primaryBackgroundFullStroke(PaddingValues(0.dp))
        ) {
            Text(
                text = "Select Currency",
                style = poppinsSemibold(22),
                color = MaterialTheme.colors.primaryTextColor,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(vertical = 16.dp)
            )

            Separator()

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(16.dp),
                modifier = Modifier
                    .heightIn(max = 340.dp)
            ) {
                items(allCurrencies) {
                    Column(
                        modifier = Modifier
                            .primaryBackground {
                                currencySelected(it)
                            }
                    ) {
                        Text(
                            text = it.currency,
                            style = poppinsRegular(18),
                            color = MaterialTheme.colors.primaryTextColor
                        )

                        Text(
                            text = "${it.code.uppercase()} (${it.symbol})",
                            style = poppinsRegular(14),
                            color = MaterialTheme.colors.primaryTextColor
                        )
                    }
                }
            }

            Separator()

            PrimaryButton(
                text = "Close",
            ) {
                closeView()
            }
        }
    }
}