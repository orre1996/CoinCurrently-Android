package com.cryptium.coincurrently.features.Search
import com.cryptium.coincurrently.app.NavigationScreens
import com.cryptium.coincurrently.app.SearchScreenCase
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.cryptium.coincurrently.sharedui.navigation.CustomTopAppBar
import com.cryptium.coincurrently.app.NavigationViewModel
import com.cryptium.coincurrently.features.CoinDetail.Sheets.SelectCoinBaseView
import com.cryptium.coincurrently.features.Portfolio.Enums.NewTransactionType
import com.cryptium.coincurrently.core.analytics.AnalyticsManager
import com.cryptium.coincurrently.core.analytics.Navigate
import com.cryptium.coincurrently.core.analytics.ScreenLabel
import com.cryptium.coincurrently.core.networking.ResponseModels.toCryptoDetail

@Composable
fun SearchScreen(
    navigationViewModel: NavigationViewModel,
    modifier: Modifier,
    screenCase: SearchScreenCase
) {
    BackHandler(onBack = {
        navigationViewModel.navigateBack()
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
                    navigationViewModel.navigateBack()
                }
            )

            SelectCoinBaseView(
                screenCase = screenCase,
                fromScreen = ScreenLabel.Search,
                highlightId = null,
                hideId = null,
                modifier = Modifier.weight(1f),
                close = { coin ->
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
    }
}
