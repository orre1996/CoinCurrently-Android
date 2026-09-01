package com.cryptium.coincurrently.features.Settings.CurrencySelector
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.cryptium.coincurrently.core.extensions.poppinsRegular
import com.cryptium.coincurrently.sharedui.theme.primaryBackground
import com.cryptium.coincurrently.sharedui.theme.primaryTextColor
import com.cryptium.coincurrently.sharedui.theme.tabbarAccent
import com.cryptium.coincurrently.sharedui.theme.transparent
import com.cryptium.coincurrently.sharedui.components.GlobalFunctions
import com.cryptium.coincurrently.app.NavigationViewModel
import com.cryptium.coincurrently.sharedui.components.refreshCachedSectionsAfterCurrencyChange
import androidx.core.app.NotificationManagerCompat
import com.cryptium.coincurrently.features.PriceAlerts.PriceAlertsRepository
import kotlinx.coroutines.launch
import com.cryptium.coincurrently.core.networking.ResponseModels.CurrencySelectionModel
import com.cryptium.coincurrently.sharedui.navigation.HeaderNavigatedView

@Composable
fun CurrencySelectorScreen(
    navigationViewModel: NavigationViewModel,
    modifier: Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var selectedCurrency by remember { mutableStateOf(GlobalFunctions.getSelectedCurrency(context)) }

    Column(
        modifier = modifier
    ) {
        HeaderNavigatedView("Currency") {
            navigationViewModel.navigateBack()
        }

        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(allCurrencies) {
                CurrencySelectorView(
                    currency = it,
                    selectedCurrency = selectedCurrency,
                    callback = { currency ->
                        selectedCurrency = currency
                        GlobalFunctions.setSelectedCurrency(currency)

                        coroutineScope.launch {
                            refreshCachedSectionsAfterCurrencyChange()
                            navigationViewModel.notifyCurrencyChanged()
                        }

                        if (NotificationManagerCompat.from(context).areNotificationsEnabled()) {
                            coroutineScope.launch {
                                PriceAlertsRepository.updateUser(force = true)
                            }
                        }
                    }
                )
            }
        }
    }
}


val allCurrencies = arrayListOf(
    CurrencySelectionModel("Emirati dirham", "AED", "د.إ", "ar", "AE", true),
    CurrencySelectionModel("Australian dollars", "AUD", "$", "en", "AU"),
    CurrencySelectionModel("Bitcoin cash", "BCH", "Ƀ", "en", "US"),
    CurrencySelectionModel("Bitcoin", "BTC", "₿", "en", "US"),
    CurrencySelectionModel("Bit", "BITS", "₿", "en", "US"),
    CurrencySelectionModel("Binance coin", "BNB", "BNB", "en", "US", true),
    CurrencySelectionModel("Brazilian real", "BRL", "R$", "pt", "BR"),
    CurrencySelectionModel("Canadian dollars", "CAD", "$", "en", "CA"),
    CurrencySelectionModel("Swiss franc", "CHF", "CHF", "gsw", "CH", true),
    CurrencySelectionModel("Chilean peso", "CLP", "$", "es", "CL"),
    CurrencySelectionModel("Chinese yuan", "CNY", "¥", "zh_Hant", "HK"),
    CurrencySelectionModel("Czech koruna", "CZK", "Kč", "CZ", "CZ", true),
    CurrencySelectionModel("Danish krone", "DKK", "kr", "da", "DK", true),
    CurrencySelectionModel("Polkadot", "DOT", "DOT", "en", "EU", true),
    CurrencySelectionModel("Ethereum", "ETH", "Ξ", "en", "US"),
    CurrencySelectionModel("EOS", "EOS", "EOS", "en", "US", true),
    CurrencySelectionModel("Euro", "EUR", "€", "en", "EU"),
    CurrencySelectionModel("British pound sterling", "GBP", "£", "en", "IE"),
    CurrencySelectionModel("Hong Kong dollar", "HKD", "$", "en", "HK"),
    CurrencySelectionModel("Hungarian forint", "HUF", "ft", "zh_Hant", "HK"),
    CurrencySelectionModel("Indonesian rupiah", "IDR", "Rp", "id", "ID"),
    CurrencySelectionModel("Israeli shekel", "ILS", "₪", "he", "IL"),
    CurrencySelectionModel("Indian rupee", "INR", "₹", "en", "IN"),
    CurrencySelectionModel("Japanese yen", "JPY", "¥", "ja", "JP"),
    CurrencySelectionModel("South Korean won", "KRW", "₩", "ko", "KR"),
    CurrencySelectionModel("Sri Lankan rupee", "LKR", "Rs", "si", "LK"),
    CurrencySelectionModel("Chainlink", "LINK", "LINK", "en", "US", true),
    CurrencySelectionModel("Litecoin", "LTC", "Ł", "en", "US"),
    CurrencySelectionModel("Mexican peso", "MXN", "$", "es", "MX"),
    CurrencySelectionModel("Malaysian ringgit", "MYR", "RM", "ms", "MY"),
    CurrencySelectionModel("Norwegian krone", "NOK", "kr", "nn", "NO", true),
    CurrencySelectionModel("New Zealand dollar", "NZD", "$", "en", "NZ"),
    CurrencySelectionModel("Philippine peso", "PHP", "₱", "fil", "PH"),
    CurrencySelectionModel("Pakistani rupee", "PKR", "Rs", "en", "PK"),
    CurrencySelectionModel("Złoty", "PLN", "zł", "pl", "PL", true),
    CurrencySelectionModel("Russian ruble", "RUB", "₽", "ru", "RU", true),
    CurrencySelectionModel("Swedish krona", "SEK", "kr", "sv", "SE", true),
    CurrencySelectionModel("Satoshi", "SATS", "sats", "en", "US", true),
    CurrencySelectionModel("Singapore dollar", "SGD", "$", "zh_Hans", "SG"),
    CurrencySelectionModel("Thai baht", "THB", "฿", "th", "TH"),
    CurrencySelectionModel("Turkish lira", "TRY", "₺", "tr", "TR"),
    CurrencySelectionModel("Ukrainian hryvna", "UAH", "₴", "uk", "UA", true),
    CurrencySelectionModel("United States dollar", "USD", "$", "en", "US"),
    CurrencySelectionModel("Vietnamese dong", "VND", "₫", "vi", "VN", true),
    CurrencySelectionModel("Ripple", "XRP", "XRP", "en", "US", true),
    CurrencySelectionModel("South African rand", "ZAR", "R", "af", "ZA"),
    CurrencySelectionModel("Stellar", "XLM", "XLM", "en", "US", true),
    CurrencySelectionModel("Yearn finance", "YFI", "YFI", "en", "US", true)
).sortedBy { it.currency }

@Composable
private fun CurrencySelectorView(currency: CurrencySelectionModel,
                         selectedCurrency: CurrencySelectionModel?,
                         callback: (CurrencySelectionModel) -> Unit) {
    Row(
        modifier = Modifier
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
                text = currency.currency,
                style = poppinsRegular(18),
                color = MaterialTheme.colors.primaryTextColor,
            )

            Text(text = "${currency.code.uppercase()} (${currency.symbol})",
                style = poppinsRegular(14),
                color = MaterialTheme.colors.primaryTextColor)
        }
    }
}