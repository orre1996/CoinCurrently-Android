package com.cryptium.coincurrently.app
import com.cryptium.coincurrently.core.networking.ResponseModels.AddressImport
import com.cryptium.coincurrently.app.NavigationScreens
import com.cryptium.coincurrently.app.SearchScreenCase
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.cryptium.coincurrently.app.RootLoadingScreen
import com.cryptium.coincurrently.features.Portfolio.Models.PortfolioSwapTransaction
import com.cryptium.coincurrently.app.NavTypes.CategoryDetailNavType
import com.cryptium.coincurrently.features.Portfolio.AddTransactionScreen
import com.cryptium.coincurrently.app.NavTypes.CryptoDetailInfoModelNavType
import com.cryptium.coincurrently.app.NavTypes.DerivativeNavType
import com.cryptium.coincurrently.app.NavTypes.ExchangeResponseNavType
import com.cryptium.coincurrently.app.NavTypes.NFTToplistNavType
import com.cryptium.coincurrently.app.NavTypes.PortfolioTransactionTypeNavType
import com.cryptium.coincurrently.app.NavTypes.PublicTreasuryHolderNavType
import com.cryptium.coincurrently.app.NavTypes.PriceVolumeModelNavType
import com.cryptium.coincurrently.app.NavTypes.SearchCaseNavType
import com.cryptium.coincurrently.features.Portfolio.Enums.PortfolioTransactionType
import com.cryptium.coincurrently.app.NavTypes.SwapTransactionNavType
import com.cryptium.coincurrently.app.NavTypes.ToplistNavType
import com.cryptium.coincurrently.app.NavTypes.TransactionNavType
import com.cryptium.coincurrently.app.NavTypes.WalletNavType
import com.cryptium.coincurrently.features.CoinDetail.CryptoDetailScreen
import com.cryptium.coincurrently.features.ExchangeDetail.ExchangeDetailScreen
import com.cryptium.coincurrently.app.NavigationViewModel
import com.cryptium.coincurrently.features.Search.SearchScreen
import com.cryptium.coincurrently.features.Home.HomeScreen
import com.cryptium.coincurrently.features.FearGreed.FearAndGreedScreen
import com.cryptium.coincurrently.features.GasFees.GasFeesScreen
import com.cryptium.coincurrently.features.GlobalMarket.GlobalMarketScreen
import com.cryptium.coincurrently.features.CategoryDetail.CategoryDetailScreen
import com.cryptium.coincurrently.features.CoinDetail.CompareCoinsScreen
import com.cryptium.coincurrently.features.Treasuries.Screens.TreasuryDetailScreen
import com.cryptium.coincurrently.features.NFTDetail.NftDetailScreen
import com.cryptium.coincurrently.features.DerivativeDetail.DerivativeDetailScreen
import com.cryptium.coincurrently.features.Market.MarketScreen
import com.cryptium.coincurrently.features.Settings.CurrencySelector.CurrencySelectorScreen
import com.cryptium.coincurrently.features.Settings.SettingsScreen
import com.cryptium.coincurrently.features.PriceAlerts.MyPriceAlertsScreen
import com.cryptium.coincurrently.features.Referral.ReferralScreen
import com.cryptium.coincurrently.features.Calculator.SettingsCalculatorScreen
import com.cryptium.coincurrently.features.News.NewsScreen
import com.cryptium.coincurrently.features.Portfolio.PortfolioScreen
import com.cryptium.coincurrently.features.Portfolio.DataClasses.PortfolioTransaction
import com.cryptium.coincurrently.features.Portfolio.ImportWalletTransactionScreen
import com.cryptium.coincurrently.features.Portfolio.AddSwapTransactionScreen
import com.cryptium.coincurrently.features.Portfolio.WalletDetailsScreen
import com.cryptium.coincurrently.features.Settings.Feedback.FeedbackScreen
import com.cryptium.coincurrently.core.networking.ResponseModels.CryptoCategory
import com.cryptium.coincurrently.core.networking.ResponseModels.CryptoDetailInfoModel
import com.cryptium.coincurrently.core.networking.ResponseModels.Derivative
import com.cryptium.coincurrently.core.networking.ResponseModels.ExchangeResponse
import com.cryptium.coincurrently.core.networking.ResponseModels.NFTToplist
import com.cryptium.coincurrently.core.networking.ResponseModels.PriceVolumeModel
import com.cryptium.coincurrently.core.networking.ResponseModels.Toplist
import kotlin.reflect.typeOf

@Composable
fun NavigationEntries(
    navigationViewModel: NavigationViewModel,
    mainScreenTopPadding: Dp,
    animationDuration: MutableState<Int>,
    navBarControllerHeight: Dp,
    navBarHeight: Dp,
    systemNavigationBarBottomInset: Dp = 0.dp,
    displayCutoutStartInset: Dp = 0.dp,
    displayCutoutEndInset: Dp = 0.dp,
    isLoggedIn: Boolean = false,
    isLocked: Boolean = false,
    needsToUpdate: Boolean = false,
    onLoadingAuthenticate: () -> Unit = {}
) {
    NavHost(
        navController = navigationViewModel.navHostController,
        startDestination = NavigationScreens.Loading,
        enterTransition = { slideIntoContainer(
            AnimatedContentTransitionScope.SlideDirection.Left,
            animationSpec = tween(animationDuration.value)
        )},
        exitTransition = { slideOutOfContainer(
            AnimatedContentTransitionScope.SlideDirection.Left,
            animationSpec = tween(animationDuration.value)
        )},
        popEnterTransition = { slideIntoContainer(
            AnimatedContentTransitionScope.SlideDirection.Right,
            animationSpec = tween(animationDuration.value)
        )},
        popExitTransition = { slideOutOfContainer(
            AnimatedContentTransitionScope.SlideDirection.Right,
            animationSpec = tween(animationDuration.value)
        )},
    ) {
        val mainScreenBottomOffsetModifier = Modifier
            .padding(
                start = displayCutoutStartInset,
                end = displayCutoutEndInset,
                top = mainScreenTopPadding,
                bottom = (navBarHeight - 1.dp).coerceAtLeast(0.dp) + systemNavigationBarBottomInset,
            )

        val childScreenBottomOffsetModifier = Modifier
            .padding(
                start = displayCutoutStartInset,
                end = displayCutoutEndInset,
                top = mainScreenTopPadding,
                bottom = navBarControllerHeight
            )

        composable<NavigationScreens.Loading> {
            RootLoadingScreen(
                isLoggedIn = isLoggedIn,
                isLocked = isLocked,
                needsToUpdate = needsToUpdate,
                onAuthenticate = onLoadingAuthenticate
            )
        }

        // Home

        composable<NavigationScreens.Home> {
            HomeScreen(
                navigationViewModel,
                mainScreenBottomOffsetModifier
            )
        }

        composable<NavigationScreens.GlobalMarket> {
            GlobalMarketScreen(
                navigationViewModel,
                childScreenBottomOffsetModifier
            )
        }

        composable<NavigationScreens.FearGreed> {
            FearAndGreedScreen(
                navigationViewModel,
                childScreenBottomOffsetModifier
            )
        }

        composable<NavigationScreens.GasFees> {
            GasFeesScreen(
                navigationViewModel,
                childScreenBottomOffsetModifier
            )
        }

        // Portfolio

        composable<NavigationScreens.Portfolio> {
            PortfolioScreen(
                navigationViewModel,
                mainScreenBottomOffsetModifier
            )
        }

        composable<NavigationScreens.WalletDetails>(
            typeMap = mapOf(
                typeOf<AddressImport>() to WalletNavType.wallet,
                typeOf<Toplist>() to ToplistNavType.toplist
            )
        ) {
            val args = it.toRoute<NavigationScreens.WalletDetails>()

            WalletDetailsScreen(
                navigationViewModel,
                childScreenBottomOffsetModifier,
                wallet = args.wallet,
                coin = args.coin
            )
        }

        composable<NavigationScreens.Search>(
            typeMap = mapOf(
                typeOf<SearchScreenCase>() to SearchCaseNavType.searchScreenCase
            )
        ) {
            val args = it.toRoute<NavigationScreens.Search>()

            SearchScreen(
                navigationViewModel,
                childScreenBottomOffsetModifier,
                screenCase = args.screenCase
            )
        }

        // Market

        composable<NavigationScreens.Market> {
            MarketScreen(
                navigationViewModel,
                mainScreenBottomOffsetModifier
            )
        }

        composable<NavigationScreens.CryptoDetail>(
            typeMap = mapOf(
                typeOf<CryptoDetailInfoModel>() to CryptoDetailInfoModelNavType.cryptoDetailInfoModel
            )
        ) {
            val args = it.toRoute<NavigationScreens.CryptoDetail>()

            CryptoDetailScreen(
                args.cryptoDetailInfoModel,
                navigationViewModel,
                childScreenBottomOffsetModifier
            )
        }

        composable<NavigationScreens.CompareCoins>(
            typeMap = mapOf(
                typeOf<CryptoDetailInfoModel?>() to CryptoDetailInfoModelNavType.cryptoDetailInfoModel,
                typeOf<PriceVolumeModel?>() to PriceVolumeModelNavType.priceVolumeModel
            )
        ) {
            val args = it.toRoute<NavigationScreens.CompareCoins>()

            CompareCoinsScreen(
                args.cryptoDetailInfoModel,
                args.priceVolumeModel,
                args.compareId,
                navigationViewModel,
                childScreenBottomOffsetModifier
            )
        }

        composable<NavigationScreens.AddPortfolioTransaction>(
            typeMap = mapOf(
                typeOf<PortfolioTransactionType>() to PortfolioTransactionTypeNavType.transactionType,
                typeOf<CryptoDetailInfoModel?>() to CryptoDetailInfoModelNavType.cryptoDetailInfoModel,
                typeOf<PortfolioTransaction?>() to TransactionNavType.transaction
            )
        ) {
            val args = it.toRoute<NavigationScreens.AddPortfolioTransaction>()

            AddTransactionScreen(
                navigationViewModel,
                childScreenBottomOffsetModifier,
                transactionType = args.transactionType,
                currencyDetail = args.currencyDataModel,
                transaction = args.transaction,
                popToRoot = args.popToRoot
            )
        }

        composable<NavigationScreens.AddPortfolioSwapTransaction>(
            typeMap = mapOf(
                typeOf<PortfolioTransactionType>() to PortfolioTransactionTypeNavType.transactionType,
                typeOf<CryptoDetailInfoModel?>() to CryptoDetailInfoModelNavType.cryptoDetailInfoModel,
                typeOf<PortfolioSwapTransaction?>() to SwapTransactionNavType.swapTransaction
            )
        ) {
            val args = it.toRoute<NavigationScreens.AddPortfolioSwapTransaction>()

            AddSwapTransactionScreen(
                navigationViewModel,
                childScreenBottomOffsetModifier,
                transactionType = args.transactionType,
                currencyDetail = args.currencyDataModel,
                transaction = args.transaction,
                popToRoot = args.popToRoot
            )
        }

        composable<NavigationScreens.ImportWalletTransaction> {
            val args = it.toRoute<NavigationScreens.ImportWalletTransaction>()

            ImportWalletTransactionScreen(
                navigationViewModel,
                childScreenBottomOffsetModifier,
                popToRoot = args.popToRoot
            )
        }

        composable<NavigationScreens.NFTDetail>(
            typeMap = mapOf(
                typeOf<NFTToplist>() to NFTToplistNavType.nftToplist,
            )
        ) {
            val args = it.toRoute<NavigationScreens.NFTDetail>()

            NftDetailScreen(
                navigationViewModel,
                childScreenBottomOffsetModifier,
                nftToplist = args.nftToplist
            )
        }

        composable<NavigationScreens.CategoryDetail>(
            typeMap = mapOf(
                typeOf<CryptoCategory>() to CategoryDetailNavType.category,
            )
        ) {
            val args = it.toRoute<NavigationScreens.CategoryDetail>()

            CategoryDetailScreen(
                args.category,
                navigationViewModel,
                childScreenBottomOffsetModifier
            )
        }

        composable<NavigationScreens.ExchangeDetail>(
            typeMap = mapOf(
                typeOf<ExchangeResponse>() to ExchangeResponseNavType.exchange,
            )
        ) {
            val args = it.toRoute<NavigationScreens.ExchangeDetail>()

            ExchangeDetailScreen(
                navigationViewModel,
                childScreenBottomOffsetModifier,
                args.exchange
            )
        }

        composable<NavigationScreens.DerivativeDetail>(
            typeMap = mapOf(
                typeOf<Derivative>() to DerivativeNavType.derivative,
            )
        ) {
            val args = it.toRoute<NavigationScreens.DerivativeDetail>()

            DerivativeDetailScreen(
                navigationViewModel,
                childScreenBottomOffsetModifier,
                args.derivative
            )
        }

        composable<NavigationScreens.TreasuryDetail>(
            typeMap = mapOf(
                typeOf<com.cryptium.coincurrently.core.networking.ResponseModels.PublicTreasuryHolder>() to PublicTreasuryHolderNavType.holder,
            )
        ) {
            val args = it.toRoute<NavigationScreens.TreasuryDetail>()

            TreasuryDetailScreen(
                navigationViewModel = navigationViewModel,
                modifier = childScreenBottomOffsetModifier,
                holder = args.holder,
                coinId = args.coinId,
            )
        }

        // News

        composable<NavigationScreens.News> {
            NewsScreen(
                navigationViewModel,
                mainScreenBottomOffsetModifier
            )
        }

        // More

        composable<NavigationScreens.Settings> {
            SettingsScreen(
                navigationViewModel,
                mainScreenBottomOffsetModifier,
                tabBarHeight = navBarHeight,
            )
        }

        composable<NavigationScreens.CurrencySelector> {
            CurrencySelectorScreen(
                navigationViewModel,
                childScreenBottomOffsetModifier
            )
        }

        composable<NavigationScreens.Calculator> {
            SettingsCalculatorScreen(
                navigationViewModel,
                childScreenBottomOffsetModifier
            )
        }

        composable<NavigationScreens.PriceAlerts> {
            MyPriceAlertsScreen(
                navigationViewModel,
                childScreenBottomOffsetModifier
            )
        }

        composable<NavigationScreens.Feedback> {
            FeedbackScreen(
                navigationViewModel,
                childScreenBottomOffsetModifier
            )
        }

        composable<NavigationScreens.Referral> {
            val args = it.toRoute<NavigationScreens.Referral>()
            ReferralScreen(
                navigationViewModel = navigationViewModel,
                referralCode = args.referralCode,
                modifier = childScreenBottomOffsetModifier,
            )
        }
    }
}
