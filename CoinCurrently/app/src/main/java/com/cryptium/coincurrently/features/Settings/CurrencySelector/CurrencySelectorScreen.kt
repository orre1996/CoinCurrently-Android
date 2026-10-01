package com.cryptium.coincurrently.features.Settings.CurrencySelector
import com.cryptium.coincurrently.core.persistence.UserPreferences
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.cryptium.coincurrently.core.extensions.poppinsRegular
import com.cryptium.coincurrently.sharedui.theme.primaryBackground
import com.cryptium.coincurrently.sharedui.theme.primaryTextColor
import com.cryptium.coincurrently.sharedui.theme.tabbarAccent
import com.cryptium.coincurrently.sharedui.theme.transparent
import com.cryptium.coincurrently.app.NavigationViewModel
import com.cryptium.coincurrently.app.refreshCachedSectionsAfterCurrencyChange
import androidx.core.app.NotificationManagerCompat
import com.cryptium.coincurrently.features.PriceAlerts.PriceAlertsRepository
import kotlinx.coroutines.launch
import com.cryptium.coincurrently.core.localization.Localization
import com.cryptium.coincurrently.core.networking.ResponseModels.CurrencySelectionModel
import com.cryptium.coincurrently.core.persistence.allCurrencies
import com.cryptium.coincurrently.sharedui.navigation.HeaderNavigatedView

@Composable
fun CurrencySelectorScreen(
    navigationViewModel: NavigationViewModel,
    modifier: Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var selectedCurrency by remember { mutableStateOf(UserPreferences.getSelectedCurrency(context)) }

    Column(
        modifier = modifier
    ) {
        HeaderNavigatedView(Localization.string("Currency")) {
            navigationViewModel.navigateBack()
        }

        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            itemsIndexed(allCurrencies) { index, currency ->
                CurrencySelectorView(
                    currency = currency,
                    selectedCurrency = selectedCurrency,
                    index = index,
                    totalCount = allCurrencies.size,
                    callback = { selected ->
                        selectedCurrency = selected
                        UserPreferences.setSelectedCurrency(selected)

                        coroutineScope.launch {
                            refreshCachedSectionsAfterCurrencyChange()
                            navigationViewModel.notifyCurrencyChanged()
                        }

                        if (NotificationManagerCompat.from(context).areNotificationsEnabled()) {
                            coroutineScope.launch {
                                PriceAlertsRepository.updateUser()
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun CurrencySelectorView(currency: CurrencySelectionModel,
                         selectedCurrency: CurrencySelectionModel?,
                         index: Int,
                         totalCount: Int,
                         callback: (CurrencySelectionModel) -> Unit) {
    Row(
        modifier = Modifier
            .semantics(mergeDescendants = true) {
                contentDescription = Localization.format(
                    "Currency %d of %d. %@.",
                    index + 1,
                    totalCount,
                    currency.localizedName,
                )
            }
            .primaryBackground {
                callback(currency)
            },
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Box(
            modifier = Modifier
                .size(size = 32.dp)
                .clip(shape = CircleShape)
                .border(
                    BorderStroke(
                        1.5.dp,
                        MaterialTheme.colors.tabbarAccent
                    ), shape = CircleShape
                )
                .background(
                    color = if (currency.code == selectedCurrency?.code) {
                        MaterialTheme.colors.tabbarAccent
                    } else {
                        MaterialTheme.colors.transparent
                    }
                )
                .align(Alignment.CenterVertically)
        )

        Column {
            Text(
                text = currency.localizedName,
                style = poppinsRegular(18),
                color = MaterialTheme.colors.primaryTextColor,
            )

            Text(text = "${currency.code.uppercase()} (${currency.symbol})",
                style = poppinsRegular(14),
                color = MaterialTheme.colors.primaryTextColor)
        }
    }
}