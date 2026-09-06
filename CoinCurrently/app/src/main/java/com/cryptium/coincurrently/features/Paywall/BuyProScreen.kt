package com.cryptium.coincurrently.features.Paywall
import com.cryptium.coincurrently.BuildConfig
import com.cryptium.coincurrently.sharedui.modifiers.imageSize

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.invisibleToUser
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cryptium.coincurrently.R
import com.cryptium.coincurrently.sharedui.theme.*
import com.cryptium.coincurrently.sharedui.modifiers.*
import com.cryptium.coincurrently.sharedui.components.Separator
import com.cryptium.coincurrently.sharedui.buttons.CloseButton
import com.cryptium.coincurrently.sharedui.buttons.PrimaryButton
import com.cryptium.coincurrently.sharedui.buttons.SecondaryButton
import com.cryptium.coincurrently.sharedui.navigation.HeaderView
import com.cryptium.coincurrently.sharedui.charts.getChangeWithoutSymbol
import com.cryptium.coincurrently.core.extensions.getActivity
import com.cryptium.coincurrently.core.extensions.poppinsRegular
import com.cryptium.coincurrently.core.extensions.satoshiBold
import com.cryptium.coincurrently.core.extensions.satoshiMedium
import com.cryptium.coincurrently.core.extensions.satoshiRegular
import com.cryptium.coincurrently.sharedui.components.OnLifecycleEvent
import com.cryptium.coincurrently.sharedui.sheets.WebView
import com.cryptium.coincurrently.sharedui.components.SnackBarType
import com.cryptium.coincurrently.sharedui.components.SnackbarView
import com.cryptium.coincurrently.features.Paywall.TierButton
import com.cryptium.coincurrently.features.Paywall.BuyProViewModel
import com.cryptium.coincurrently.features.Paywall.BuyProViewModelFactory
import com.cryptium.coincurrently.sharedui.sheets.ModalSheetNavigationBarExtensionSpacer
import com.cryptium.coincurrently.sharedui.sheets.modalSheetNavigationBarsPadding
import com.cryptium.coincurrently.core.analytics.BuyProPressed
import com.cryptium.coincurrently.core.analytics.AnalyticsManager
import com.cryptium.coincurrently.core.analytics.ScreenLabel
import com.cryptium.coincurrently.core.analytics.SubscriptionOptionSelected
import kotlinx.coroutines.launch
import com.cryptium.coincurrently.sharedui.sheets.ModalBottomSheetSystemNavigationBar
import kotlinx.serialization.Serializable

@Serializable
sealed class TierType {
    data class Text(val value: String) : TierType()
    data class Image(val image: Int) : TierType()
}

@Serializable
data class Tier(
    val title: String,
    val freeText: TierType,
    val proText: TierType,
    val accessibilityLabel: String,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuyProScreen(close: () -> Unit) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()

    val context = LocalContext.current

    var selectedUrl: String? by remember { mutableStateOf(null) }
    var showAllFeatures: Boolean by remember { mutableStateOf(false) }

    val viewModel: BuyProViewModel = viewModel(
        factory = BuyProViewModelFactory()
    )

    val appFeatures: List<Tier> = listOf(
        Tier(
            title = "Market data sync frequency (minutes)",
            freeText = TierType.Text("15"),
            proText = TierType.Text("3"),
            accessibilityLabel = "App updates every 15 minutes with the free version and every 3 minutes with pro",
        ),

        Tier(
            title = "Live price updates",
            freeText = TierType.Image(R.drawable.minus),
            proText = TierType.Image(R.drawable.check),
            accessibilityLabel = "Live price updates on coin detail pages are available with pro only",
        ),

        Tier(
            title = "Import addresses",
            freeText = TierType.Image(R.drawable.minus),
            proText = TierType.Image(R.drawable.check),
            accessibilityLabel = "With coincurrently pro you can import wallet addresses",
        ),

        Tier(
            title = "Portfolio transactions",
            freeText = TierType.Text("3"),
            proText = TierType.Image(R.drawable.infinity),
            accessibilityLabel = "3 portfolio transactions can be added with the free version and unlimited with pro",
        ),

        Tier(
            title = "Portfolio exports",
            freeText = TierType.Text("3"),
            proText = TierType.Image(R.drawable.infinity),
            accessibilityLabel = "3 portfolio exports are included with the free version and unlimited with pro",
        ),

        Tier(
            title = "Home screen widgets",
            freeText = TierType.Text("8"),
            proText = TierType.Text("14"),
            accessibilityLabel = "8 home screen widgets are included in the free version and 14 with pro",
        ),

        Tier(
            title = "Favorite coins",
            freeText = TierType.Text("2"),
            proText = TierType.Image(R.drawable.infinity),
            accessibilityLabel = "2 currencies can be favorite marked with the free version and unlimited with pro",
        ),

        Tier(
            title = "News categories",
            freeText = TierType.Text("4"),
            proText = TierType.Text("8"),
            accessibilityLabel = "4 news categories are included in the free version and 8 with pro",
        ),

        Tier(
            title = "Price alerts",
            freeText = TierType.Text("2"),
            proText = TierType.Image(R.drawable.infinity),
            accessibilityLabel = "2 price alerts can be added with the free version and unlimited with pro",
        ),
    )

    LaunchedEffect(Unit) {
        viewModel.setHasPush(context)
        viewModel.getProducts(context)
    }

    OnLifecycleEvent { _, event ->
        when (event) {
            Lifecycle.Event.ON_RESUME -> {
                coroutineScope.launch {
                    viewModel.setHasPush(context)
                    viewModel.getProducts(context)
                }
            }
            else -> { }
        }
    }

    BackHandler(onBack = {
        coroutineScope.launch {
            sheetState.hide()
            close.invoke()
        }
    })

    ModalBottomSheet(
        onDismissRequest = {
            coroutineScope.launch {
                sheetState.hide()
                close.invoke()
            }
        },
        sheetState = sheetState,
        shape = RoundedCornerShape(0.dp),
        containerColor = MaterialTheme.colors.surface,
        tonalElevation = 16.dp,
        contentWindowInsets = { WindowInsets(0,0 ,0 ,0) },
        dragHandle = { }
    ) {
        ModalBottomSheetSystemNavigationBar(containerColor = MaterialTheme.colors.surface)
        Box(
            modifier = Modifier.modalSheetNavigationBarsPadding(context),
            contentAlignment = Alignment.TopEnd
        ) {
            CappedFontScale(DynamicTypeSizeCap.Large) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .semantics { isTraversalGroup = true }
            ) {
                CloseButton(
                    restorePurchases = {
                        viewModel.restorePurchases(context) {
                            coroutineScope.launch {
                                sheetState.hide()
                                close.invoke()
                            }
                        }
                    },
                    dismiss = {
                        coroutineScope.launch {
                            sheetState.hide()
                            close.invoke()
                        }
                    },
                    modifier = Modifier.semantics {
                        isTraversalGroup = true
                        traversalIndex = 2f
                    }
                )

                Column(
                    horizontalAlignment = Alignment.Start,
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .padding(
                            start = 16.dp,
                            end = 16.dp,
                            bottom = 32.dp
                        )
                        .weight(1f)
                        .semantics {
                            isTraversalGroup = true
                            traversalIndex = 0f
                        }
                ) {
                    HeaderView()

                    Column(
                        modifier = Modifier
                            .primaryBackgroundNoRipple(
                                paddingValues = PaddingValues(0.dp)
                            )
                            .padding(
                                top = 16.dp,
                                start = 16.dp,
                                bottom = 8.dp,
                                end = 16.dp
                            )
                    ) {
                        TierHeader()

                        val features = if(showAllFeatures) appFeatures else appFeatures.take(4)

                        features.forEach {
                            FeatureRow(
                                title = it.title,
                                freeTitle = it.freeText,
                                premiumTitle = it.proText,
                                firstRow = it == features.first(),
                                accessibilityLabel = it.accessibilityLabel
                            )

                            if (it != features.last()) {
                                Separator()
                            }
                        }
                    }

                    SecondaryButton(
                        text = if(showAllFeatures) "Show less features" else "Show all features",
                        fullWidth = true,
                        onClick = {
                            showAllFeatures = !showAllFeatures
                        },
                        modifier = Modifier
                            .padding(top = 16.dp)
                    )

                    if (viewModel.buttons == null || !viewModel.buttons.isNullOrEmpty()) {
                        Text(
                            "Select Plan",
                            style = satoshiBold(22),
                            color = MaterialTheme.colors.onSurfaceDim,
                            textAlign = TextAlign.Start,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    top = 32.dp,
                                    bottom = 16.dp
                                )
                                .semantics { heading() }
                        )
                    }

                    if (viewModel.buttons == null) {
                        (0 until 2).forEach { _ ->
                            TierButtonView(
                                button = null,
                                isSelected = false,
                                action = { }
                            )
                        }
                    } else if (viewModel.buttons?.isEmpty() ?: true) {
                        Text(
                            if (BuildConfig.REVENUECAT_API_KEY.startsWith("test_")) {
                                "Couldn't find test subscriptions. Add products to your RevenueCat Test Store under Product catalog, using the same product IDs as production."
                            } else {
                                "Couldn't find subscriptions. Make sure you are logged in to Google Play, restart app and try again."
                            },
                            style = satoshiRegular(14),
                            color = MaterialTheme.colors.onSurfaceDimmer,
                            modifier = Modifier
                                .padding(top = 32.dp)
                        )
                    } else {
                        (viewModel.buttons)?.forEach {
                            TierButtonView(
                                button = it,
                                isSelected = it == viewModel.selectedButton,
                            ) {
                                viewModel.selectedButton = it
                                AnalyticsManager.log(
                                    SubscriptionOptionSelected(
                                        fromScreen = ScreenLabel.BuyPro,
                                        subscription = it.productId,
                                    ),
                                )
                            }
                        }
                    }

                    if (viewModel.isEligibleForTrial && (viewModel.buttons == null || !viewModel.buttons.isNullOrEmpty())) {
                        FreeTrialInformation()
                    } else {
                        Text(
                            text = "Cancel anytime",
                            style = satoshiRegular(14),
                            color = MaterialTheme.colors.onSurfaceDimmer,
                            textAlign = TextAlign.Start,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp)
                        )
                    }

                    TermsAndConditionsView()
                }

                if (viewModel.buttons == null || !viewModel.buttons.isNullOrEmpty()) {
                    Separator()

                    SubscribeButton(
                        title = viewModel.subscribeButtonText,
                        isLoading = viewModel.isLoading,
                        modifier = Modifier.semantics { traversalIndex = 1f },
                        onClick = {
                            viewModel.selectedButton?.productId?.let { productId ->
                                AnalyticsManager.log(
                                    BuyProPressed(ScreenLabel.BuyPro, subscription = productId),
                                )
                            }
                            viewModel.buyProduct(context) {
                                close.invoke()
                            }
                        }
                    )
                }

                ModalSheetNavigationBarExtensionSpacer(context)
            }
            }

            Column {
                Spacer(modifier = Modifier.weight(1f))

                SnackbarView(
                    showSnackBar = viewModel.showSnackBar.value,
                    viewModel.snackbarMessage,
                    buttonTitle = "",
                    type = SnackBarType.FAILURE,
                    isTabbarHidden = false,
                    navBarHeight = 0.dp,
                    systemNavbarHeight = 0.dp,
                    onDismissRequest = { viewModel.showSnackBar.value = false },
                    buttonAction = { }
                )
            }

            selectedUrl?.let {
                WebViewSheet(it) {
                    selectedUrl = null
                }
            }

        }
    }
}

@Composable
private fun CloseButton(
    restorePurchases: () -> Unit,
    dismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .padding(
                vertical = 8.dp,
                horizontal = 16.dp
            )
    ) {
        Text(
            text = "Restore",
            style = satoshiBold(18),
            color = MaterialTheme.colors.primary,
            modifier = Modifier
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                ) {
                    restorePurchases.invoke()
                }
                .semantics {
                    role = Role.Button
                    contentDescription = "Restore previous purchase"
                }
        )

        Spacer(modifier = Modifier.weight(1f))

        Image(
            painter = painterResource(R.drawable.cross),
            contentDescription = "Close window",
            colorFilter = ColorFilter.tint(MaterialTheme.colors.onSurface),
            modifier = Modifier
                .clip(CircleShape)
                .clickable {
                    dismiss.invoke()
                }
                .background(MaterialTheme.colors.surfaceContainer)
                .padding(8.dp)
                .imageSize(24.dp, 24.dp)
                .semantics { role = Role.Button }
        )
    }
}

@Composable
private fun HeaderView() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .padding(
                top = 16.dp,
                bottom = 32.dp,
            )
            .primaryBackgroundNoRipple()
            .semantics(mergeDescendants = true) {}
    ) {
        Image(
            painter = painterResource(
                if (MaterialTheme.colors.isLight) R.drawable.premium_logo_light
                else R.drawable.premium_logo_dark
            ),
            contentDescription = null,
            modifier = Modifier
                .size(84.dp, 84.dp)
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "CoinCurrently Pro",
                style = satoshiBold(22),
                color = MaterialTheme.colors.onSurface
            )

            Text(
                text = "Upgrade to CoinCurrently Pro for enhanced features.",
                style = satoshiRegular(16),
                color = MaterialTheme.colors.onSurfaceDim
            )
        }
    }
}

@Composable
private fun TierHeader() {
    Row(
        horizontalArrangement = Arrangement.spacedBy(24.dp),
        modifier = Modifier.clearAndSetSemantics { invisibleToUser() }
    ) {
        Spacer(modifier = Modifier.weight(1f))

        MinimumTextSize(
            text = "FREE",
            font = satoshiBold(16),
            extraSize = 16.dp
        ) {
            Text(
                text = "FREE",
                style = satoshiBold(12),
                color = MaterialTheme.colors.onSurface,
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colors.surfaceContainerHigh,
                        shape = RoundedCornerShape(size = 8.dp)
                    )
                    .padding(
                        vertical = 4.dp,
                        horizontal = 8.dp
                    )
            )
        }

        MinimumTextSize(
            text = "FREE",
            font = satoshiBold(16),
            extraSize = 16.dp
        ) {
            Text(
                text = "PRO",
                style = satoshiBold(12),
                color = MaterialTheme.colors.primary,
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colors.primaryContainer,
                        shape = RoundedCornerShape(size = 8.dp)
                    )
                    .padding(
                        vertical = 4.dp,
                        horizontal = 8.dp
                    )
            )
        }
    }
}

@Composable
private fun FeatureRow(
    title: String,
    freeTitle: TierType,
    premiumTitle: TierType,
    firstRow: Boolean,
    accessibilityLabel: String
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(24.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(vertical = if (firstRow) 16.dp else 8.dp)
            .heightIn(min = 54.dp)
            .clearAndSetSemantics { contentDescription = accessibilityLabel }
    ) {
        Text(
            title,
            style = satoshiBold(16),
            color = MaterialTheme.colors.onSurfaceDim,
            modifier = Modifier
                .weight(1f)
        )

        MinimumTextSize(
            text = "FREE",
            font = satoshiBold(16),
            extraSize = 16.dp
        ) {
            when(freeTitle) {
                is TierType.Text -> {
                    Text(
                        text = freeTitle.value,
                        style = satoshiBold(16),
                        color = MaterialTheme.colors.onSurface
                    )
                }
                is TierType.Image -> {
                    Image(
                        painter = painterResource(freeTitle.image),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(MaterialTheme.colors.onSurface),
                        modifier = Modifier
                            .imageSize(24.dp),
                    )
                }
            }
        }

        MinimumTextSize(
            text = "FREE",
            font = satoshiBold(16),
            extraSize = 16.dp
        ) {
            when(premiumTitle) {
                is TierType.Text -> {
                    Text(
                        text = premiumTitle.value,
                        style = satoshiBold(16),
                        color = MaterialTheme.colors.primary
                    )
                }
                is TierType.Image -> {
                    Image(
                        painter = painterResource(premiumTitle.image),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(MaterialTheme.colors.primary),
                        modifier = Modifier
                            .imageSize(24.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun TierButtonView(
    button: TierButton?,
    isSelected: Boolean,
    action: () -> Unit
) {
    Box(
        contentAlignment = Alignment.TopEnd,
        modifier = Modifier
            .padding(bottom = 16.dp)
            .dropShadow(
                shape = RoundedCornerShape(12.dp),
                shadow = Shadow(
                    radius = 2.dp,
                    spread = 0.dp,
                    color = Color.Black.copy(alpha = 0.08f),
                    offset = DpOffset(0.dp, 2.dp)
                )
            )
            .dropShadow(
                shape = RoundedCornerShape(12.dp),
                shadow = Shadow(
                    radius = 8.dp,
                    spread = 0.dp,
                    color = Color.Black.copy(alpha = 0.12f),
                    offset = DpOffset(0.dp, 4.dp)
                )
            )
            .primaryBackground(
                paddingValues = PaddingValues(0.dp),
                lineWidth = 2.dp,
                backgroundColor =  if (isSelected) MaterialTheme.colors.secondary15 else MaterialTheme.colors.surfaceContainer,
                outlineColor = if (isSelected) MaterialTheme.colors.primary else MaterialTheme.colors.outlineVariant,
                cornerRadius = 12.dp
            ) {
                action.invoke()
            }
            .semantics(mergeDescendants = true) {
                role = Role.Button
                selected = isSelected
            }
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier
                .padding(paddingValues = PaddingValues(24.dp))
                .fillMaxWidth()
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = button?.title ?: "00000000",
                    style = satoshiBold(18),
                    color = MaterialTheme.colors.primary90,
                    modifier = Modifier
                        .shimmeringModifier(button?.title == null)
                )

                button?.saveAmount?.let {
                    Text(
                        text = "Save ${it.getChangeWithoutSymbol()}",
                        style = satoshiBold(12),
                        color = MaterialTheme.colors.primary90,
                        modifier = Modifier
                            .background(
                                color = MaterialTheme.colors.surfaceContainerHighest,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(
                                horizontal = 8.dp,
                                vertical = 4.dp
                            )
                    )
                }
            }

            Text(
                text = button?.formattedPrice ?: "0000000000000",
                style = satoshiBold(18),
                color = MaterialTheme.colors.onSurfaceContainer,
                modifier = Modifier
                    .padding(top = 4.dp)
                    .shimmeringModifier(button?.formattedPrice == null)
                    .semantics {
                        button?.formattedPriceAccessibilityLabel?.let {
                            contentDescription = it
                        }
                    }
            )

            if (button?.subTitle?.isNotEmpty() ?: false) {
                Text(
                    text = button.subTitle,
                    style = satoshiRegular(14),
                    color = MaterialTheme.colors.onSurfaceDim,
                )
            }

            button?.timeLeft?.let {
                Text(
                    text = it,
                    style = satoshiRegular(12),
                    color = MaterialTheme.colors.onSurfaceDim
                )
            }
        }
    }
}

@Composable
private fun FreeTrialInformation() {
    val context = LocalContext.current
    val hasNotifications = NotificationManagerCompat.from(context).areNotificationsEnabled()
    val linkColor = MaterialTheme.colors.primary

    val annotatedText = buildAnnotatedString {
        append("All plans include a 7-day trial, with limited offers excluded. You’ll get a push notification reminder a day before the trial ends.")

        if (!hasNotifications) {
            append(" Please enable notifications in the app ")
            pushStringAnnotation(tag = "SETTINGS", annotation = "settings")
            withStyle(
                SpanStyle(
                    color = linkColor,
                    textDecoration = TextDecoration.Underline
                )
            ) {
                append("settings")
            }
            pop()
            append(" to get reminders.")
        }

        append(" Cancel anytime.")
    }

    ClickableText(
        text = annotatedText,
        style = satoshiRegular(14).copy(color = MaterialTheme.colors.onSurfaceDimmer),
        onClick = { offset ->
            annotatedText.getStringAnnotations(tag = "SETTINGS", start = offset, end = offset)
                .firstOrNull()
                ?.let { openAppSettings(context) }
        },
        modifier = Modifier.padding(top = 16.dp)
    )
}

private fun openAppSettings(context: android.content.Context) {
    context.getActivity()?.let {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", context.packageName, null)
        intent.data = uri
        it.startActivity(intent)
    }
}

@Composable
private fun SubscribeButton(
    title: String,
    isLoading: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    PrimaryButton(
        text = title,
        fullWidth = true,
        isLoading = isLoading,
        modifier = modifier
            .padding(
                vertical = 24.dp,
                horizontal = 16.dp
            )
    ) {
        onClick.invoke()
    }
}

@Composable
private fun TermsAndConditionsView() {
    val context = LocalContext.current
    val linkColor = MaterialTheme.colors.primary

    val annotatedText = buildAnnotatedString {
        append("By subscribing, you agree to the ")
        pushStringAnnotation(
            tag = "URL",
            annotation = "https://coincurrently.app/terms"
        )
        withStyle(
            SpanStyle(
                color = linkColor,
                textDecoration = TextDecoration.Underline
            )
        ) {
            append("Terms & Conditions")
        }
        pop()
        append(" and ")
        pushStringAnnotation(
            tag = "URL",
            annotation = "https://coincurrently.app/privacy"
        )
        withStyle(
            SpanStyle(
                color = linkColor,
                textDecoration = TextDecoration.Underline
            )
        ) {
            append("Privacy Policy")
        }
        pop()
        append(".")
    }

    ClickableText(
        text = annotatedText,
        style = satoshiRegular(14).copy(color = MaterialTheme.colors.onSurfaceDimmer),
        onClick = { offset ->
            annotatedText.getStringAnnotations(tag = "URL", start = offset, end = offset)
                .firstOrNull()
                ?.item
                ?.let { url ->
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                }
        },
        modifier = Modifier.padding(top = 16.dp)
    )
}