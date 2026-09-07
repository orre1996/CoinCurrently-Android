package com.cryptium.coincurrently.features.Referral
import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.lifecycle.Lifecycle
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cryptium.coincurrently.sharedui.buttons.PrimaryButton
import com.cryptium.coincurrently.sharedui.buttons.SecondaryButton
import com.cryptium.coincurrently.sharedui.theme.onSurfaceDim
import com.cryptium.coincurrently.sharedui.theme.outlineVariant
import com.cryptium.coincurrently.sharedui.theme.primaryBackgroundNoRipple
import com.cryptium.coincurrently.sharedui.modifiers.shimmeringModifier
import com.cryptium.coincurrently.sharedui.theme.trendDown
import com.cryptium.coincurrently.sharedui.theme.trendUp
import com.cryptium.coincurrently.core.extensions.satoshiBold
import com.cryptium.coincurrently.core.extensions.satoshiMedium
import com.cryptium.coincurrently.sharedui.navigation.CustomTopAppBar
import com.cryptium.coincurrently.sharedui.inputs.TextInputField
import com.cryptium.coincurrently.app.NavigationViewModel
import com.cryptium.coincurrently.sharedui.components.OnLifecycleEvent
import com.cryptium.coincurrently.sharedui.components.SnackBarType
import com.cryptium.coincurrently.features.Referral.ReferralViewModel
import com.cryptium.coincurrently.features.Referral.ReferralViewModelFactory
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

@Composable
fun ReferralScreen(
    navigationViewModel: NavigationViewModel,
    referralCode: String,
    modifier: Modifier,
) {
    val viewModel: ReferralViewModel = viewModel(
        key = referralCode,
        factory = ReferralViewModelFactory(referralCode),
    )
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val swipeScope = rememberCoroutineScope()

    val shareLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) { }

    var isRefreshing by remember { mutableStateOf(false) }

    BackHandler(onBack = { navigationViewModel.navigateBack() })

    LaunchedEffect(Unit) {
        viewModel.setStartData()

        coroutineScope {
            async { viewModel.loadReferralCode() }
            async { viewModel.getUser() }
        }

        val trimmedCode = viewModel.redeemCodeInput.trim()
        if (trimmedCode.isNotEmpty() && trimmedCode.length == 10) {
            viewModel.redeemCode()
        }
    }

    OnLifecycleEvent { _, event ->
        when (event) {
            Lifecycle.Event.ON_RESUME -> {
                swipeScope.launch {
                    coroutineScope {
                        async { viewModel.loadReferralCode() }
                        async { viewModel.getUser(ignoreCache = true) }
                    }
                }
            }

            else -> { }
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier,
    ) {
        CustomTopAppBar(
            title = "Referral Program",
            navigateBack = { navigationViewModel.navigateBack() },
        )

        SwipeRefresh(
            state = rememberSwipeRefreshState(isRefreshing = isRefreshing),
            onRefresh = {
                isRefreshing = true
                swipeScope.launch {
                    try {
                        coroutineScope {
                            async { viewModel.loadReferralCode() }
                            async { viewModel.getUser(ignoreCache = true) }
                        }
                    } finally {
                        isRefreshing = false
                    }
                }
            }) {
            Column(
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(32.dp),
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
            ) {
                ReferralCodeSection(
                    viewModel = viewModel,
                    onCopyLink = {
                        val link = viewModel.referralLink ?: return@ReferralCodeSection

                        clipboardManager.setText(AnnotatedString(link))
                        navigationViewModel.showSnackbar(
                            context = context,
                            type = SnackBarType.SUCCESS,
                            message = "Referral link copied to clipboard.",
                        )
                    },
                    onShareLink = {
                        viewModel.referralLink?.let { link ->
                            val text = """
                            Join me on CoinCurrently and track crypto in real time!
                            $link
                        """.trimIndent()

                            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, text)
                                type = "text/plain"
                            }
                            shareLauncher.launch(Intent.createChooser(sendIntent, null))
                        }
                    },
                )

                RedeemCodeSection(
                    viewModel = viewModel,
                    onRedeem = { viewModel.redeemCode() },
                )
            }
        }
    }
}

@Composable
private fun ReferralCodeSection(
    viewModel: ReferralViewModel,
    onCopyLink: () -> Unit,
    onShareLink: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = "Your referral code",
            style = satoshiBold(22),
            color = MaterialTheme.colors.onSurface,
        )

        Text(
            text = "${viewModel.currentReferrals}/3 towards earnings 6 months of CoinCurrently Pro. Thereafter 1 month per referral.",
            style = satoshiMedium(16),
            color = MaterialTheme.colors.onSurfaceDim,
        )

        BoxWithConstraints(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .semantics { contentDescription = "Referral QR code" },
        ) {
            val qrSize = (maxWidth / 3) * 2

            if (viewModel.referralQRImage != null) {
                Image(
                    bitmap = viewModel.referralQRImage!!.asImageBitmap(),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.size(qrSize),
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(qrSize)
                        .clip(RoundedCornerShape(8.dp))
                        .shimmeringModifier(active = viewModel.isLoadingReferralCode),
                )
            }
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.primaryBackgroundNoRipple(),
        ) {
            Text(
                text = viewModel.referralCode ?: "000000000000",
                style = satoshiBold(26),
                color = MaterialTheme.colors.primary,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .shimmeringModifier(
                        active = viewModel.referralCode == null
                    ),
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
            ) {
                SecondaryButton(
                    text = "Copy link",
                    isEnabled = viewModel.referralLink != null,
                    fullWidth = true,
                    modifier = Modifier.weight(1f),
                    onClick = onCopyLink,
                )

                PrimaryButton(
                    text = "Share",
                    isEnabled = viewModel.referralLink != null,
                    fullWidth = true,
                    modifier = Modifier.weight(1f),
                    onClick = onShareLink,
                )
            }
        }
    }
}

@Composable
private fun RedeemCodeSection(
    viewModel: ReferralViewModel,
    onRedeem: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = "Redeem a code",
            style = satoshiBold(22),
            color = MaterialTheme.colors.onSurface,
        )

        Text(
            text = "Redeem a code and get 7 days of CoinCurrently Pro for free",
            style = satoshiMedium(16),
            color = MaterialTheme.colors.onSurfaceDim,
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(top = 12.dp)
                .primaryBackgroundNoRipple(),
        ) {
            TextInputField(
                input = viewModel.redeemCodeInput,
                startIcon = null,
                placeholder = "Enter referral code",
                accessibilityLabel = viewModel.redeemCodeInput.ifEmpty {
                    "Enter referral code"
                },
                outlineColor = if (viewModel.showError) {
                    MaterialTheme.colors.trendDown
                } else {
                    MaterialTheme.colors.outlineVariant
                },
                onInputChange = { viewModel.redeemCodeInput = it },
                onDone = onRedeem,
            )

            when {
                viewModel.redeemSucceeded -> {
                    Text(
                        text = "Referral code redeemed successfully.",
                        style = satoshiMedium(14),
                        color = MaterialTheme.colors.trendUp,
                    )
                }

                viewModel.redeemErrorMessage != null -> {
                    Text(
                        text = viewModel.redeemErrorMessage.orEmpty(),
                        style = satoshiMedium(14),
                        color = MaterialTheme.colors.trendDown,
                    )
                }
            }

            PrimaryButton(
                text = "Redeem Code",
                isEnabled = viewModel.redeemCodeInput.trim().isNotEmpty() && !viewModel.isRedeemingCode,
                isLoading = viewModel.isRedeemingCode,
                onClick = onRedeem,
            )
        }
    }
}
