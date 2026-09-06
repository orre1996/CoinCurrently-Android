package com.cryptium.coincurrently.features.PriceAlerts
import com.cryptium.coincurrently.core.persistence.UserPreferences
import com.cryptium.coincurrently.sharedui.modifiers.imageSize

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cryptium.coincurrently.R
import androidx.compose.material.MaterialTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.ui.Alignment
import androidx.lifecycle.Lifecycle
import com.cryptium.coincurrently.app.MainActivity
import com.cryptium.coincurrently.sharedui.theme.*
import com.cryptium.coincurrently.sharedui.modifiers.*
import com.cryptium.coincurrently.sharedui.components.AccessibilityLayout
import com.cryptium.coincurrently.sharedui.components.AdaptiveStack
import com.cryptium.coincurrently.sharedui.components.CustomSwitch
import com.cryptium.coincurrently.sharedui.components.Separator
import com.cryptium.coincurrently.sharedui.buttons.SecondaryButton
import com.cryptium.coincurrently.core.extensions.DateFormat
import com.cryptium.coincurrently.core.extensions.convertStringToDate
import com.cryptium.coincurrently.core.extensions.isTablet
import com.cryptium.coincurrently.core.extensions.getActivity
import com.cryptium.coincurrently.core.extensions.satoshiBold
import com.cryptium.coincurrently.core.extensions.satoshiRegular
import com.cryptium.coincurrently.sharedui.navigation.CustomTopAppBar
import com.cryptium.coincurrently.sharedui.loading.ActivityIndicator
import com.cryptium.coincurrently.sharedui.models.ChangeDirection
import com.cryptium.coincurrently.app.NavigationViewModel
import com.cryptium.coincurrently.core.analytics.BuyProAction
import com.cryptium.coincurrently.core.analytics.CurrencySelected
import com.cryptium.coincurrently.core.analytics.DisabledBigMoveAlerts
import com.cryptium.coincurrently.core.analytics.EnabledBigMoveAlerts
import com.cryptium.coincurrently.core.analytics.AnalyticsManager
import com.cryptium.coincurrently.core.analytics.OpenNotificationSettingsPressed
import com.cryptium.coincurrently.core.analytics.PriceAlertAdded
import com.cryptium.coincurrently.core.analytics.PriceAlertDeleted
import com.cryptium.coincurrently.core.analytics.PriceAlertInputType
import com.cryptium.coincurrently.core.analytics.ScreenLabel
import com.cryptium.coincurrently.core.analytics.SheetLabel
import com.cryptium.coincurrently.core.analytics.SheetOpened
import com.cryptium.coincurrently.sharedui.components.OnLifecycleEvent
import com.cryptium.coincurrently.features.CoinDetail.Views.General.CryptoDetailAlertCellView
import com.cryptium.coincurrently.features.CoinDetail.Sheets.SelectCoinSheet
import com.cryptium.coincurrently.features.PriceAlerts.CreatePriceAlertSheet
import com.cryptium.coincurrently.sharedui.components.SnackBarType
import com.cryptium.coincurrently.features.PriceAlerts.PriceAlertCreateResult
import com.cryptium.coincurrently.core.purchases.BillingManager
import com.cryptium.coincurrently.features.Settings.CurrencySelector.CurrencyOptionsSheet
import com.cryptium.coincurrently.features.PriceAlerts.PriceAlertsViewModel
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.cryptium.coincurrently.core.networking.ResponseModels.*
import kotlin.time.Duration.Companion.seconds
import com.cryptium.coincurrently.app.SearchCase
import com.cryptium.coincurrently.app.SearchScreenCase

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyPriceAlertsScreen(
    navigationViewModel: NavigationViewModel,
    modifier: Modifier
) {
    val context = LocalContext.current
    val isTablet = context.isTablet()
    val coroutineScope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp

    var isSelectingCoin by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    var isRefreshing by remember { mutableStateOf(false) }

    val viewModel: PriceAlertsViewModel = viewModel()

    var showCurrencySelector by remember { mutableStateOf(false) }

    var selectedCoin: CryptoDetailInfoModel? by remember { mutableStateOf(null) }
    var tempCoin: CryptoDetailInfoModel? by remember { mutableStateOf(null) }

    var isAddingAlert by remember { mutableStateOf(false) }
    val isAlertByPriceSelected = remember { mutableStateOf(true) }
    val isRecurringAlertOn = remember { mutableStateOf(false) }
    val priceInput = remember { mutableDoubleStateOf(0.0) }
    val percentInput = remember { mutableDoubleStateOf(10.0) }
    var selectedCurrency by remember { mutableStateOf(UserPreferences.getSelectedCurrency()) }
    val selectedDirection = remember { mutableStateOf(ChangeDirection.Up) }
    var showError: Boolean by remember { mutableStateOf(false) }

    val currentSubscription by BillingManager.currentSubscription.observeAsState()

    LaunchedEffect(Unit) {
        viewModel.notificationsEnabled = NotificationManagerCompat.from(context).areNotificationsEnabled()

        coroutineScope.launch {
            listOf(
                async { viewModel.getFilteredToplist() },
                async { viewModel.getPriceAlerts() }
            ).awaitAll()

            viewModel.setGroups()
        }
    }

    OnLifecycleEvent { _, event ->
        when (event) {
            Lifecycle.Event.ON_RESUME -> {
                viewModel.notificationsEnabled = NotificationManagerCompat.from(context).areNotificationsEnabled()
                coroutineScope.launch {
                    listOf(
                        async { viewModel.getFilteredToplist() },
                        async { viewModel.getPriceAlerts() }
                    ).awaitAll()

                    viewModel.setGroups()
                }
            }
            else -> { }
        }
    }

    BackHandler(onBack = {
        navigationViewModel.navigateBack()
    })

    Box {
        Column(
            modifier = modifier
        ) {
            CustomTopAppBar(
                title = "Price Alerts",
                navigateBack = {
                    navigationViewModel.navigateBack()
                },
                actions = listOf({
                    if (isLoading) {
                        IconButton(enabled = false, onClick = { }) {
                            ActivityIndicator(
                                size = 21.dp,
                                fill = false
                            )
                        }
                    } else if (viewModel.notificationsEnabled) {
                        InfoButton(
                            addNewAlert = {
                                if ((viewModel.alerts?.count() ?: 0) >= 2 && currentSubscription == null) {
                                    navigationViewModel.openBuyPro(ScreenLabel.PriceAlerts, BuyProAction.PriceAlert)
                                } else {
                                    AnalyticsManager.log(
                                        SheetOpened(ScreenLabel.PriceAlerts, SheetLabel.CoinSearch),
                                    )
                                    isAlertByPriceSelected.value = true
                                    priceInput.doubleValue = 0.0
                                    percentInput.doubleValue = 10.0
                                    isRecurringAlertOn.value = false
                                    selectedDirection.value = ChangeDirection.Up
                                    isSelectingCoin = true
                                }
                            }
                        )
                    }
                })
            )

            SwipeRefresh(
                state = rememberSwipeRefreshState(isRefreshing = isRefreshing),
                onRefresh = {
                    isRefreshing = true

                    coroutineScope.launch {
                        launch {
                            listOf(
                                async { viewModel.getFilteredToplist() },
                                async { viewModel.getPriceAlerts() }
                            ).awaitAll()

                            viewModel.setGroups()
                            isRefreshing = false
                        }

                        delay(10.seconds)
                        isRefreshing = false
                    }
                },
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(24.dp),
                        modifier = Modifier
                            .height(screenHeight)
                    ) {
                        if (!viewModel.notificationsEnabled) {
                            item {
                                PushesOffView(
                                    openSettings = {
                                        AnalyticsManager.log(
                                            OpenNotificationSettingsPressed(ScreenLabel.PriceAlerts),
                                        )
                                        openSettings(context)
                                    }
                                )
                            }
                        } else {
                            item {
                                LargeMoverView(
                                    enabled = viewModel.isAlertsEnabled,
                                    toggle = {
                                        coroutineScope.launch {
                                            isLoading = true
                                            viewModel.toggleBigMover()
                                            isLoading = false
                                            if (viewModel.isAlertsEnabled) {
                                                AnalyticsManager.log(
                                                    EnabledBigMoveAlerts(ScreenLabel.PriceAlerts),
                                                )
                                            } else {
                                                AnalyticsManager.log(
                                                    DisabledBigMoveAlerts(ScreenLabel.PriceAlerts),
                                                )
                                            }
                                        }
                                    }
                                )
                            }

                            if (viewModel.alerts != null && viewModel.filteredToplist != null && viewModel.alertPrices != null) {
                                if (viewModel.alerts.isNullOrEmpty()) {
                                    item {
                                        AlertsEmptyView()
                                    }
                                } else {
                                    AlertsList(
                                        viewModel = viewModel,
                                        deleteAlert = {
                                            coroutineScope.launch {
                                                val coin = viewModel.getCoin(it.coinId)
                                                val coinName = coin?.name ?: ""
                                                viewModel.deleteAlert(it)
                                                AnalyticsManager.log(
                                                    PriceAlertDeleted(
                                                        ScreenLabel.PriceAlerts,
                                                        coinName
                                                    ),
                                                )
                                                MainActivity.showSnackbar(
                                                    context = context,
                                                    status = SnackBarType.SUCCESS,
                                                    message = "${coin?.symbol?.uppercase() ?: "Coin"} alert deleted."
                                                )
                                            }
                                        }
                                    )
                                }
                            } else {
                                TempList(isTablet = isTablet)
                            }
                        }
                    }
                }
            }
        }

        if (isSelectingCoin) {
            SelectCoinSheet(
                searchScreenCase = SearchScreenCase.Search(
                    list = SearchCase.OnlyCoins
                ),
                fromScreen = ScreenLabel.PriceAlerts,
                highlightId = null,
                hideId = null,
                selectedCoin = { coin ->
                    val id = coin?.id ?: return@SelectCoinSheet

                    isLoading = true

                    coroutineScope.launch {
                        viewModel.getSelectedCoin(id)?.let {
                            isLoading = false
                            selectedCoin = it
                            AnalyticsManager.log(
                                SheetOpened(ScreenLabel.PriceAlerts, SheetLabel.NewPriceAlert),
                            )
                        } ?: run {
                            isLoading = false
                        }
                    }
                },
                close = {
                    isSelectingCoin = false
                }
            )
        }

        selectedCoin?.let {
            CreatePriceAlertSheet(
                sheetState = sheetState,
                cryptoDetailModel = it,
                selectedCurrency = selectedCurrency,
                isLoading = isAddingAlert,
                showError = showError,
                isAlertByPriceSelected = isAlertByPriceSelected,
                priceInput = priceInput,
                percentInput = percentInput,
                isRecurringAlertOn = isRecurringAlertOn,
                selectedDirection = selectedDirection,
                selectNewCurrency = {
                    AnalyticsManager.log(
                        SheetOpened(ScreenLabel.PriceAlerts, SheetLabel.CurrencySelector),
                    )
                    tempCoin = selectedCoin
                    selectedCoin = null
                    showCurrencySelector = true
                },
                addNewAlert = {
                    isAddingAlert = true

                    coroutineScope.launch {
                        when (viewModel.addPriceAlert(it)) {
                            PriceAlertCreateResult.Success -> {
                                AnalyticsManager.log(
                                    PriceAlertAdded(
                                        fromScreen = ScreenLabel.PriceAlerts,
                                        coinName = selectedCoin?.name ?: "",
                                        isRecurring = isRecurringAlertOn.value,
                                        inputType = if (isAlertByPriceSelected.value) {
                                            PriceAlertInputType.Price
                                        } else {
                                            PriceAlertInputType.Percent
                                        },
                                    ),
                                )
                                navigationViewModel.showSnackbar(
                                    context = context,
                                    type = SnackBarType.SUCCESS,
                                    message = "${selectedCoin?.symbol?.uppercase() ?: "Price"} alert added"
                                )
                                sheetState.hide()
                                selectedCoin = null
                            }
                            PriceAlertCreateResult.FcmTokenUnavailable -> {
                                navigationViewModel.showSnackbar(
                                    context = context,
                                    type = SnackBarType.FAILURE,
                                    message = "Something went wrong, restart app and try again"
                                )
                            }
                            PriceAlertCreateResult.Failed -> {
                                showError = !showError
                            }
                        }

                        isAddingAlert = false
                    }
                },
                close = {
                    coroutineScope.launch {
                        sheetState.hide()
                        selectedCoin = null
                    }
                }
            )
        }

        if (showCurrencySelector) {
            CurrencyOptionsSheet(
                selectedOption = selectedCurrency,
                newOptionSelected = {
                    selectedCurrency = it
                    AnalyticsManager.log(
                        CurrencySelected(
                            fromScreen = ScreenLabel.PriceAlerts,
                            currency = selectedCurrency.currency,
                        ),
                    )
                },
                close = {
                    showCurrencySelector = false
                    selectedCoin = tempCoin
                }
            )
        }
    }
}

@Composable
private fun InfoButton(
    addNewAlert: () -> Unit
) {
    IconButton(onClick = {
        addNewAlert.invoke()
    }) {
        Image(
            painter = painterResource(id = R.drawable.add),
            contentDescription = "Add new price alert",
            colorFilter = ColorFilter.tint(
                MaterialTheme.colors.onSurface
            ),
            modifier = Modifier
                .imageSize(24.dp)
        )
    }
}

private fun openSettings(context: Context) {
    context.getActivity()?.let {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", context.packageName, null)
        intent.data = uri
        it.startActivity(intent)
    }
}

@Composable
fun LargeMoverView(
    enabled: Boolean,
    toggle: () -> Unit
) {
    val isHorizontal = AccessibilityLayout.isHorizontal

    AdaptiveStack(
        isHorizontal = isHorizontal,
        spacing = if (isHorizontal) 16.dp else 8.dp,
        horizontalAlignment = Alignment.Start,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(vertical = 8.dp)
            .primaryBackgroundNoRipple()
            .clearAndSetSemantics {
                contentDescription = "Big Move Alerts. Get notified when BTC or ETH swing more than 5% in either direction."
                role = Role.Switch
                stateDescription = if (enabled) "On" else "Off"
                onClick(label = "Toggle Big Move Alerts") {
                    toggle()
                    true
                }
            },
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalAlignment = Alignment.Start,
            modifier = sectionModifier(),
        ) {
            Text(
                text = "Big Move Alerts",
                style = satoshiBold(16),
                color = MaterialTheme.colors.onSurfaceContainer
            )

            Text(
                text = "Get notified when BTC or ETH swing more than 5% in either direction.",
                style = satoshiBold(12),
                color = MaterialTheme.colors.onSurfaceDimmer
            )
        }

        Box {
            CustomSwitch(enabled, toggle)

            Canvas(
                modifier = Modifier
                    .size(width = 54.dp, height = 28.dp)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = {
                                toggle()
                            }
                        )
                    }
            ) { }
        }
    }
}

@Composable
fun AlertsEmptyView() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                vertical = 48.dp,
                horizontal = 32.dp
            )
            .semantics(mergeDescendants = true) {}
    ) {
        Text(
            text = "No price alerts yet",
            style = satoshiBold(18),
            color = MaterialTheme.colors.onSurface,
            textAlign = TextAlign.Center
        )

        Text(
            text = "Tap + to create your first alert.",
            style = satoshiRegular(14),
            color = MaterialTheme.colors.onSurfaceDim,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun PushesOffView(
    openSettings: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .padding(
                vertical = 48.dp,
                horizontal = 32.dp
            )
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .semantics(mergeDescendants = true) {}
        ) {
            Text(
                text = "Notifications are off",
                style = satoshiBold(18),
                color = MaterialTheme.colors.onSurface,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Enable push notifications in your device settings to receive price alerts.",
                style = satoshiRegular(14),
                color = MaterialTheme.colors.onSurfaceDim,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(bottom = 32.dp)
            )
        }

        SecondaryButton(
            text = "Open Settings"
        ) {
            openSettings()
        }
    }
}

private fun LazyListScope.TempList(isTablet: Boolean) {
    val rowsPerGroup = if (isTablet) 6 else 3
    items(3) { _ ->
        Column(
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .primaryBackgroundNoRipple()
        ) {
            (0 until rowsPerGroup).forEach { i ->
                CryptoDetailAlertCellView(
                    symbol = "000",
                    alert = null,
                    deleteAlert = { }
                )

                if (i != rowsPerGroup - 1) {
                    Separator()
                }
            }
        }
    }
}

private fun LazyListScope.AlertsList(
    viewModel: PriceAlertsViewModel,
    deleteAlert: (AlertCoin) -> Unit
) {
    itemsIndexed(
        items = viewModel.groups,
        key = { _, group -> group }
    ) { index, group ->
        val currentCoin = viewModel.getCoin(group)
        val alertsForGroup = viewModel.alerts
            ?.filter { it.coinId == group }
            ?.sortedByDescending {
                it.createdDate.convertStringToDate(DateFormat.yyyyMMddTHHmmssSSSZ)?.time ?: 0L
            } ?: listOf()

        Column(
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .primaryBackgroundNoRipple()
        ) {
            alertsForGroup.forEachIndexed { alertIndex, alert ->
                CryptoDetailAlertCellView(
                    symbol = currentCoin?.symbol,
                    alert = alert,
                    name = currentCoin?.name,
                    index = alertIndex,
                    alertsCount = alertsForGroup.size,
                    deleteAlert = {
                        deleteAlert.invoke(alert)
                    }
                )

                if (alertIndex < alertsForGroup.lastIndex) {
                    Separator()
                }
            }
        }
    }
}

