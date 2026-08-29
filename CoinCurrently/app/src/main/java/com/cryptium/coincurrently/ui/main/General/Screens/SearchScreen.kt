package com.cryptium.coincurrently.ui.main.General.Screens

import NavigationScreens
import SearchScreenCase
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import com.cryptium.coincurrently.ui.main.General.Components.CustomTopAppBar
import com.cryptium.coincurrently.ui.main.General.NavigationViewModel
import com.cryptium.coincurrently.ui.main.General.WebView
import com.cryptium.coincurrently.ui.main.Managers.SecretMessageHelper
import com.cryptium.coincurrently.ui.main.MarketTab.CryptoDetail.Popups.SelectCoinBaseView
import com.cryptium.coincurrently.ui.main.PortfolioTab.Enums.NewTransactionType
import com.cryptium.coincurrently.ui.main.analytics.AnalyticsManager
import com.cryptium.coincurrently.ui.main.analytics.Navigate
import com.cryptium.coincurrently.ui.main.analytics.ScreenLabel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import networking.ResponseModels.toCryptoDetail

@Composable
fun SearchScreen(
    navigationViewModel: NavigationViewModel,
    modifier: Modifier,
    screenCase: SearchScreenCase
) {
    var selectedUrl by remember { mutableStateOf<String?>(null) }
    val keyboardController = LocalSoftwareKeyboardController.current
    val coroutineScope = rememberCoroutineScope()

    BackHandler(onBack = {
        if (selectedUrl != null) {
            selectedUrl = null
        } else {
            navigationViewModel.navigateBack()
        }
    })

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            CustomTopAppBar(
                title = "Select Coin",
                navigateBack = {
                    if (selectedUrl != null) {
                        selectedUrl = null
                    } else {
                        navigationViewModel.navigateBack()
                    }
                }
            )

            SelectCoinBaseView(
                screenCase = screenCase,
                fromScreen = ScreenLabel.Search,
                highlightId = null,
                hideId = null,
                modifier = Modifier.weight(1f),
                close = close@{ coin ->
                    SecretMessageHelper.secretToplistUrl(coin)?.let { url ->
                        keyboardController?.hide()
                        coroutineScope.launch {
                            delay(100)
                            selectedUrl = url
                        }
                        return@close
                    }

                    when (val screenCase = screenCase) {
                        is SearchScreenCase.FixedList -> { }
                        is SearchScreenCase.Search -> {
                            val dest = NavigationScreens.CryptoDetail(
                                cryptoDetailInfoModel = coin.toCryptoDetail()
                            )
                            navigationViewModel.navigate(dest)
                            AnalyticsManager.log(
                                Navigate(
                                    fromScreen = ScreenLabel.Search,
                                    toScreen = ScreenLabel.CryptoDetail,
                                    coinName = coin.name ?: "",
                                ),
                            )
                        }
                        is SearchScreenCase.Transaction -> {
                            if (screenCase.transactionType == NewTransactionType.Swap) {
                                val dest = NavigationScreens.NewPortfolioSwapTransaction(
                                    transactionType = screenCase.transactionType,
                                    currencyDataModel = coin.toCryptoDetail(),
                                    transaction = null,
                                    popToRoot = true
                                )
                                navigationViewModel.navigate(dest)
                                AnalyticsManager.log(
                                    Navigate(
                                        fromScreen = ScreenLabel.Search,
                                        toScreen = ScreenLabel.NewSwapTransaction,
                                        coinName = coin.name ?: "",
                                    ),
                                )
                            } else {
                                val dest = NavigationScreens.NewPortfolioTransaction(
                                    transactionType = screenCase.transactionType,
                                    currencyDataModel = coin.toCryptoDetail(),
                                    transaction = null,
                                    popToRoot = true
                                )
                                navigationViewModel.navigate(dest)
                                AnalyticsManager.log(
                                    Navigate(
                                        fromScreen = ScreenLabel.Search,
                                        toScreen = ScreenLabel.NewPortfolioTransaction,
                                        coinName = coin.name ?: "",
                                    ),
                                )
                            }
                        }
                    }
                }
            )
        }

        selectedUrl?.let {
            WebView(it) {
                selectedUrl = null
            }
        }
    }
}
