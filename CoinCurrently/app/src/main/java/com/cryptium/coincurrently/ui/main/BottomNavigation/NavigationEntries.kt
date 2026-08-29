package com.cryptium.coincurrently.ui.main.BottomNavigation

import AddressImport
import NavigationScreens
import SearchScreenCase
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
import com.cryptium.coincurrently.ui.main.Activities.FragmentLoadingScreen
import com.cryptium.coincurrently.ui.main.CryptoDetail.Compose.Portfolio.Models.NewSwapTransaction
import com.cryptium.coincurrently.ui.main.General.NavTypes.CategoryDetailNavType
import com.cryptium.coincurrently.ui.main.PortfolioTab.Screens.NewTransactionScreen
import com.cryptium.coincurrently.ui.main.General.NavTypes.CryptoDetailInfoModelNavType
import com.cryptium.coincurrently.ui.main.General.NavTypes.DerivativeNavType
import com.cryptium.coincurrently.ui.main.General.NavTypes.ExchangeResponseNavType
import com.cryptium.coincurrently.ui.main.General.NavTypes.NFTToplistNavType
import com.cryptium.coincurrently.ui.main.General.NavTypes.NewTransactionTypeNavType
import com.cryptium.coincurrently.ui.main.General.NavTypes.PublicTreasuryHolderNavType
import com.cryptium.coincurrently.ui.main.General.NavTypes.PriceVolumeModelNavType
import com.cryptium.coincurrently.ui.main.General.NavTypes.SearchCaseNavType
import com.cryptium.coincurrently.ui.main.PortfolioTab.Enums.NewTransactionType
import com.cryptium.coincurrently.ui.main.General.NavTypes.SwapTransactionNavType
import com.cryptium.coincurrently.ui.main.General.NavTypes.ToplistNavType
import com.cryptium.coincurrently.ui.main.General.NavTypes.TransactionNavType
import com.cryptium.coincurrently.ui.main.General.NavTypes.WalletNavType
import com.cryptium.coincurrently.ui.main.MarketTab.CryptoDetail.Screens.CryptoDetailScreen
import com.cryptium.coincurrently.ui.main.MarketTab.Exchanges.Screens.ExchangeDetailScreen
import com.cryptium.coincurrently.ui.main.General.NavigationViewModel
import com.cryptium.coincurrently.ui.main.General.Screens.SearchScreen
import com.cryptium.coincurrently.ui.main.HomeTab.Home.Screens.HomeScreen
import com.cryptium.coincurrently.ui.main.HomeTab.FearGreed.Screens.FearAndGreedScreen
import com.cryptium.coincurrently.ui.main.HomeTab.GasFees.Screens.GasFeesScreen
import com.cryptium.coincurrently.ui.main.HomeTab.GlobalMarket.Screens.GlobalMarketScreen
import com.cryptium.coincurrently.ui.main.MarketTab.Categories.Views.CategoryDetailScreen
import com.cryptium.coincurrently.ui.main.MarketTab.CryptoDetail.Screens.CompareCoinsScreen
import com.cryptium.coincurrently.ui.main.MarketTab.Treasuries.Screens.TreasuryDetailScreen
import com.cryptium.coincurrently.ui.main.MarketTab.NFT.Screens.NftDetailScreen
import com.cryptium.coincurrently.ui.main.MarketTab.Derivatives.Views.DerivativeDetailScreen
import com.cryptium.coincurrently.ui.main.MarketTab.MarketScreen
import com.cryptium.coincurrently.ui.main.SettingsTab.Compose.CurrencySelectorScreen
import com.cryptium.coincurrently.ui.main.SettingsTab.Compose.SettingsScreen
import com.cryptium.coincurrently.ui.main.SettingsTab.Compose.MyPriceAlertsScreen
import com.cryptium.coincurrently.ui.main.SettingsTab.Compose.MyWidgetsScreen
import com.cryptium.coincurrently.ui.main.SettingsTab.Compose.ReferralScreen
import com.cryptium.coincurrently.ui.main.SettingsTab.Compose.SettingsCalculatorScreen
import com.cryptium.coincurrently.ui.main.NewsTab.Compose.NewsScreen
import com.cryptium.coincurrently.ui.main.PortfolioTab.Screens.PortfolioScreen
import com.cryptium.coincurrently.ui.main.PortfolioTab.DataClasses.NewTransaction
import com.cryptium.coincurrently.ui.main.PortfolioTab.Screens.NewImportTransactionScreen
import com.cryptium.coincurrently.ui.main.PortfolioTab.Screens.NewSwapTransactionScreen
import com.cryptium.coincurrently.ui.main.PortfolioTab.Screens.WalletDetailsScreen
import com.cryptium.coincurrently.ui.main.SettingsTab.FeedbackScreen
import networking.ResponseModels.CryptoCategory
import networking.ResponseModels.CryptoDetailInfoModel
import networking.ResponseModels.Derivative
import networking.ResponseModels.ExchangeResponse
import networking.ResponseModels.NFTToplist
import networking.ResponseModels.PriceVolumeModel
import networking.ResponseModels.Toplist
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
            FragmentLoadingScreen(
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

        composable<NavigationScreens.NewPortfolioTransaction>(
            typeMap = mapOf(
                typeOf<NewTransactionType>() to NewTransactionTypeNavType.transactionType,
                typeOf<CryptoDetailInfoModel?>() to CryptoDetailInfoModelNavType.cryptoDetailInfoModel,
                typeOf<NewTransaction?>() to TransactionNavType.transaction
            )
        ) {
            val args = it.toRoute<NavigationScreens.NewPortfolioTransaction>()

            NewTransactionScreen(
                navigationViewModel,
                childScreenBottomOffsetModifier,
                transactionType = args.transactionType,
                currencyDetail = args.currencyDataModel,
                transaction = args.transaction,
                popToRoot = args.popToRoot
            )
        }

        composable<NavigationScreens.NewPortfolioSwapTransaction>(
            typeMap = mapOf(
                typeOf<NewTransactionType>() to NewTransactionTypeNavType.transactionType,
                typeOf<CryptoDetailInfoModel?>() to CryptoDetailInfoModelNavType.cryptoDetailInfoModel,
                typeOf<NewSwapTransaction?>() to SwapTransactionNavType.swapTransaction
            )
        ) {
            val args = it.toRoute<NavigationScreens.NewPortfolioSwapTransaction>()

            NewSwapTransactionScreen(
                navigationViewModel,
                childScreenBottomOffsetModifier,
                transactionType = args.transactionType,
                currencyDetail = args.currencyDataModel,
                transaction = args.transaction,
                popToRoot = args.popToRoot
            )
        }

        composable<NavigationScreens.NewImportTransaction> {
            val args = it.toRoute<NavigationScreens.NewImportTransaction>()

            NewImportTransactionScreen(
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
                typeOf<networking.ResponseModels.PublicTreasuryHolder>() to PublicTreasuryHolderNavType.holder,
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

        composable<NavigationScreens.Widgets> {
            MyWidgetsScreen(
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

        composable<NavigationScreens.Achievements> {
            com.cryptium.coincurrently.ui.main.SettingsTab.Achievements.Screens.AchievementsScreen(
                navigationViewModel = navigationViewModel,
                modifier = childScreenBottomOffsetModifier,
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
