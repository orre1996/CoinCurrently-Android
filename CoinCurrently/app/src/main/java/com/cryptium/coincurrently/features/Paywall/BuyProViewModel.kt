package com.cryptium.coincurrently.features.Paywall
import com.cryptium.coincurrently.core.persistence.UserPreferences
import android.content.Context
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.android.billingclient.api.ProductDetails
import com.cryptium.coincurrently.app.MainActivity
import com.cryptium.coincurrently.core.extensions.formatCurrency
import com.cryptium.coincurrently.core.extensions.getActivity
import com.cryptium.coincurrently.core.extensions.getDifferenceInPercent
import com.cryptium.coincurrently.core.extensions.getIntroductoryOffer
import com.cryptium.coincurrently.core.extensions.getNormalPricePhase
import com.cryptium.coincurrently.core.extensions.pricingPhaseToDouble
import com.cryptium.coincurrently.core.extensions.updateAllWidgets
import com.cryptium.coincurrently.sharedui.models.FormattingThreshold
import com.cryptium.coincurrently.core.analytics.FirebaseManager
import com.cryptium.coincurrently.core.config.RemoteConfigKeys
import com.cryptium.coincurrently.core.work.NotificationWorker
import com.cryptium.coincurrently.core.persistence.StorageKeys
import com.cryptium.coincurrently.core.persistence.StorageManager
import com.cryptium.coincurrently.features.Paywall.TierButton
import com.cryptium.coincurrently.core.persistence.allCurrencies
import com.cryptium.coincurrently.sharedui.components.SnackBarType
import com.cryptium.coincurrently.core.purchases.StoreKitManager
import com.cryptium.coincurrently.core.analytics.AnalyticsManager
import com.cryptium.coincurrently.core.analytics.ScreenLabel
import com.cryptium.coincurrently.core.analytics.SubscriptionPurchaseCompleted
import com.cryptium.coincurrently.core.analytics.SubscriptionPurchaseOutcome
import com.cryptium.coincurrently.core.analytics.SubscriptionRestoreCompleted
import com.cryptium.coincurrently.core.purchases.monthlyName
import com.cryptium.coincurrently.core.purchases.promotionalName
import com.cryptium.coincurrently.core.purchases.welcomeName
import com.cryptium.coincurrently.core.purchases.yearlyName
import com.revenuecat.purchases.models.StoreProduct
import com.cryptium.coincurrently.core.networking.ResponseModels.CurrencySelectionModel
import java.util.Calendar
import java.util.Date
import java.util.Timer
import java.util.concurrent.TimeUnit
import kotlin.concurrent.fixedRateTimer
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class BuyProViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return BuyProViewModelImp() as T
    }
}

interface BuyProViewModel {
    val buttons: List<TierButton>?
    var selectedButton: TierButton?
    val isEligibleForTrial: Boolean
    val hasPush: Boolean
    val isLoading: Boolean
    var showSnackBar: MutableState<Boolean>
    var snackbarMessage: String
    val offerTimeLeft: String?
    val offerTimeLeftDate: Date?
    val subscribeButtonText: String

    fun setHasPush(context: Context)
    fun getProducts(context: Context)
    fun buyProduct(context: Context,  dismissScreen: () -> Unit)
    fun restorePurchases(context: Context,  dismissScreen: () -> Unit)
}

class BuyProViewModelImp: ViewModel(), BuyProViewModel {
    private val storeKitManager = StoreKitManager

    override var buttons: List<TierButton>? by mutableStateOf(null)
    override var selectedButton: TierButton? by mutableStateOf(null)
    override var isEligibleForTrial: Boolean by mutableStateOf(false)
    override var hasPush: Boolean by mutableStateOf(false)
    override var isLoading: Boolean by mutableStateOf(false)

    override var showSnackBar: MutableState<Boolean> = mutableStateOf(false)
    override var snackbarMessage: String by mutableStateOf("")

    override var offerTimeLeft: String? by mutableStateOf(null)
    override var offerTimeLeftDate: Date? by mutableStateOf(null)

    private var timer: Timer? = null

    override val subscribeButtonText: String
        get() = if (
            selectedButton?.productId != promotionalName &&
            selectedButton?.productId != welcomeName &&
            isEligibleForTrial
        ) "Start Your Free Trial" else "Upgrade Now"

    override fun setHasPush(context: Context) {
        val notificationManager = NotificationManagerCompat.from(context)
        hasPush = notificationManager.areNotificationsEnabled()
    }

    override fun getProducts(context: Context) {
        storeKitManager.setUpBillingClient(context) { products, revenueCatProducts ->
            isEligibleForTrial = storeKitManager.isEligableForTrial.value == true
            viewModelScope.launch {
                val showPromo = FirebaseManager.getConfigBool(RemoteConfigKeys.ShowPromotionalOffer) == true
                when {
                    products.isNotEmpty() -> setAvailableProducts(products, showPromo)
                    revenueCatProducts.isNotEmpty() -> setAvailableStoreProducts(revenueCatProducts, showPromo)
                }
            }
        }
    }

    override fun buyProduct(
        context: Context,
        dismissScreen: () -> Unit
    ) {
        if (isLoading) return

        val activity = context.getActivity() ?: return
        val button = selectedButton ?: return
        val product = button.product
        val storeProduct = button.storeProduct

        val wasEligibleForTrial = isEligibleForTrial
        val shouldSchedulePush = wasEligibleForTrial
                && hasPush
                && button.productId != promotionalName
                && button.productId != welcomeName

        val productId = button.productId
        val hadExistingSubscription = UserPreferences.getHasBoughtPro() != null

        var hasLoggedPurchaseOutcome = false
        fun logPurchase(outcome: SubscriptionPurchaseOutcome) {
            if (hasLoggedPurchaseOutcome) return
            hasLoggedPurchaseOutcome = true
            AnalyticsManager.log(
                SubscriptionPurchaseCompleted(
                    fromScreen = ScreenLabel.BuyPro,
                    productId = productId,
                    outcome = outcome,
                ),
            )
        }

        isLoading = true

        storeKitManager.pendingPurchaseCallback = {
            logPurchase(SubscriptionPurchaseOutcome.Pending)
            isLoading = false
        }

        storeKitManager.successCallback = {
            logPurchase(
                if (hadExistingSubscription) SubscriptionPurchaseOutcome.Renewal
                else SubscriptionPurchaseOutcome.Success,
            )

            if (shouldSchedulePush) {
                scheduleTrialNotification(context)
            }

            isLoading = false
            context.getActivity()?.updateAllWidgets()
            dismissScreen.invoke()
            MainActivity.showAppReview(context, force = true)
            MainActivity.showSnackbar(
                context,
                SnackBarType.SUCCESS,
                "Purchase complete. Thank you! ${if (shouldSchedulePush) "Reminder scheduled." else ""}"
            )
        }

        storeKitManager.cancelCallback = {
            logPurchase(SubscriptionPurchaseOutcome.Cancelled)
            isLoading = false
        }

        storeKitManager.failureCallback = { message ->
            logPurchase(SubscriptionPurchaseOutcome.Failed)
            snackbarMessage = message
            showSnackBar.value = true
            isLoading = false
        }

        when {
            storeProduct != null -> storeKitManager.purchaseStoreProduct(storeProduct, activity)
            product != null -> storeKitManager.purchaseProduct(product, activity)
        }
    }

    override fun restorePurchases(
        context: Context,
        dismissScreen: () -> Unit
    ) {
        isLoading = true

        storeKitManager.checkIfUserSubscribes(
            context = context,
            restoreAnalytics = { outcome ->
                AnalyticsManager.log(
                    SubscriptionRestoreCompleted(ScreenLabel.BuyPro, outcome),
                )
            },
            success = {
                isLoading = false

                context.getActivity()?.updateAllWidgets()
                dismissScreen.invoke()
                MainActivity.showAppReview(context, force = true)
                MainActivity.showSnackbar(
                    context,
                    SnackBarType.SUCCESS,
                    "Purchase restored"
                )
            },
            failure = {
                isLoading = false
                snackbarMessage = "No purchases to restore"
                showSnackBar.value = true
            },
        )
    }

    private fun setAvailableProducts(products: List<ProductDetails>, showPromo: Boolean) {
        val newButtons = mutableListOf<TierButton>()

        val promo = products.firstOrNull { it.productId == promotionalName }

        val welcome = products.firstOrNull { it.productId == welcomeName }
        val offerDate: Date? = StorageManager.get(StorageKeys.HOME_WELCOME_OFFER_DATE)

        val monthly = products.firstOrNull { it.productId == monthlyName }
        val monthlyPrice = monthly?.monthlyToYearly()

        if (promo != null && showPromo && isEligibleForTrial) {
            newButtons.add(
                promo.toPromoTierButton(
                    title = "Limited Offer",
                    compareTo = monthlyPrice ?: 0.0
                )
            )
        } else if (welcome != null && offerDate != null && offerDate > Date() && isEligibleForTrial) {
            newButtons.add(
                welcome.toWelcomeTierButton(
                    title = "Welcome Plan",
                    compareTo = monthlyPrice ?: 0.0,
                )
            )

            if (StorageManager.get<Boolean>(StorageKeys.HAS_CLOSED_APP) == true) {
                setShowWelcomeMessage()
            }
        } else {
            val annual = products.firstOrNull { it.productId == yearlyName }
            annual?.let {
                newButtons.add(
                    it.toTierButton(
                        title = "Annual Plan",
                        unit = "year",
                        total = it.getNormalPricePhase()?.pricingPhaseToDouble(),
                        compareTo = monthlyPrice,
                        subTitle = "Equivalent to ${it.yearlyToMonthly().formatCurrency(threshold = FormattingThreshold.NONE, currency = it.getCurrency())} per month"
                    )
                )
            }
        }

        monthly?.let {
            newButtons.add(
                it.toTierButton(
                    title = "Monthly Plan",
                    unit ="month",
                    total = it.monthlyToYearly(),
                    subTitle = "For a total of ${monthlyPrice?.formatCurrency(threshold = FormattingThreshold.NONE, currency = monthly.getCurrency())} per year",
                )
            )
        }

        buttons = newButtons
        selectedButton = newButtons.firstOrNull()
    }

    private fun setAvailableStoreProducts(products: List<StoreProduct>, showPromo: Boolean) {
        val newButtons = mutableListOf<TierButton>()

        val promo = products.firstOrNull { it.playProductId() == promotionalName }

        val welcome = products.firstOrNull { it.playProductId() == welcomeName }
        val offerDate: Date? = StorageManager.get(StorageKeys.HOME_WELCOME_OFFER_DATE)

        val monthly = products.firstOrNull { it.playProductId() == monthlyName }
        val monthlyPrice = monthly?.monthlyToYearly()

        if (promo != null && showPromo && isEligibleForTrial) {
            newButtons.add(
                promo.toPromoTierButton(
                    title = "Limited Offer",
                    compareTo = monthlyPrice ?: 0.0,
                ),
            )
        } else if (welcome != null && offerDate != null && offerDate > Date() && isEligibleForTrial) {
            newButtons.add(
                welcome.toWelcomeTierButton(
                    title = "Welcome Plan",
                    compareTo = monthlyPrice ?: 0.0,
                ),
            )

            if (StorageManager.get<Boolean>(StorageKeys.HAS_CLOSED_APP) == true) {
                setShowWelcomeMessage()
            }
        } else {
            val annual = products.firstOrNull { it.playProductId() == yearlyName }
            annual?.let {
                newButtons.add(
                    it.toTierButton(
                        title = "Annual Plan",
                        unit = "year",
                        total = it.normalPriceAmount(),
                        compareTo = monthlyPrice,
                        subTitle = "Equivalent to ${it.yearlyToMonthly().formatCurrency(threshold = FormattingThreshold.NONE, currency = it.getCurrency())} per month",
                    ),
                )
            }
        }

        monthly?.let {
            newButtons.add(
                it.toTierButton(
                    title = "Monthly Plan",
                    unit = "month",
                    total = it.monthlyToYearly(),
                    subTitle = "For a total of ${monthlyPrice?.formatCurrency(threshold = FormattingThreshold.NONE, currency = monthly.getCurrency())} per year",
                ),
            )
        }

        buttons = newButtons
        selectedButton = newButtons.firstOrNull()
    }

    private fun scheduleTrialNotification(context: Context) {
        val workRequest =
            OneTimeWorkRequestBuilder<NotificationWorker>()
                .setInitialDelay(6, TimeUnit.DAYS)
                .build()

        WorkManager.getInstance(context).enqueue(workRequest)
    }

    private fun setShowWelcomeMessage() {
        val savedDate: Date? = StorageManager.get(StorageKeys.HOME_WELCOME_OFFER_DATE)
        val date = savedDate ?: Calendar.getInstance().apply {
            add(Calendar.DATE, 3)
        }.time

        offerTimeLeftDate = date
        if (savedDate == null) {
            StorageManager.set(StorageKeys.HOME_WELCOME_OFFER_DATE, date)
        }

        setWelcomeTimer(date)
    }

    private fun setWelcomeTimer(targetDate: Date) {
        timer?.cancel()
        timer = fixedRateTimer("welcomeTimer", initialDelay = 0, period = 1000) {
            val now = Date()
            if (targetDate <= now) {
                timer?.cancel()
                offerTimeLeft = null
                StorageManager.set(StorageKeys.DID_CLOSE_WELCOME, true)
                return@fixedRateTimer
            }

            val diff = targetDate.time - now.time
            val seconds = (diff / 1000) % 60
            val minutes = (diff / (1000 * 60)) % 60
            val hours = (diff / (1000 * 60 * 60)) % 24
            val days = diff / (1000 * 60 * 60 * 24)

            val formatted = "Expires in ${days}d ${hours}h ${minutes}m ${seconds}s"
            buttons = buttons?.map {
                if (it.title == "Welcome Plan") it.copy(timeLeft = formatted) else it
            }
            offerTimeLeft = formatted
        }
    }
}

fun ProductDetails.toTierButton(title: String, unit: String, total: Double?, compareTo: Double? = 0.0, subTitle: String?): TierButton {
    val price = this.getNormalPricePhase()?.pricingPhaseToDouble()?.formatCurrency(threshold = FormattingThreshold.NONE, currency = this.getCurrency())
    return TierButton(
        title = title,
        formattedPrice = "$price / $unit",
        formattedPriceAccessibilityLabel = "$price per $unit",
        subTitle = subTitle?.ifEmpty { "" },
        saveAmount = if ((compareTo ?: 0.0) > 0) getDifferenceInPercent(total ?: 0.0, compareTo ?: 0.0) else null,
        timeLeft = null,
        product = this
    )
}

fun ProductDetails.toPromoTierButton(title: String, compareTo: Double): TierButton {
    val price = this.getIntroductoryOffer()?.pricingPhaseToDouble()?.formatCurrency(threshold = FormattingThreshold.NONE, currency = this.getCurrency()) ?: "N/A"
    return TierButton(
        title = title,
        formattedPrice = "$price / year",
        formattedPriceAccessibilityLabel = "$price per year",
        subTitle = "For the first year, then ${this.getNormalPricePhase()?.pricingPhaseToDouble()?.formatCurrency(threshold = FormattingThreshold.NONE, currency = this.getCurrency())} per year",
        saveAmount = getDifferenceInPercent(0.0, compareTo),
        timeLeft = null,
        product = this
    )
}

fun ProductDetails.toWelcomeTierButton(title: String, compareTo: Double) = toPromoTierButton(title, compareTo)

fun ProductDetails.monthlyToYearly(): Double {
    return this.getNormalPricePhase()?.pricingPhaseToDouble()?.times(12) ?: 0.0
}

fun ProductDetails.yearlyToMonthly(): Double {
    return this.getNormalPricePhase()?.pricingPhaseToDouble()?.div(12) ?: 0.0
}



fun ProductDetails.getCurrency(): CurrencySelectionModel {
    val currencyCode = this.getNormalPricePhase()?.priceCurrencyCode ?: "USD"
    return allCurrencies.first { it.code == currencyCode }
}

private fun StoreProduct.playProductId(): String? {
    val idPrefix = id.substringBefore(":")
    if (idPrefix in setOf(monthlyName, yearlyName, promotionalName, welcomeName)) return idPrefix
    if (id in setOf(monthlyName, yearlyName, promotionalName, welcomeName)) return id
    return null
}

private fun StoreProduct.normalPriceAmount(): Double {
    return price.amountMicros.toDouble() / 1_000_000
}

private fun StoreProduct.introductoryPriceAmount(): Double? {
    val phases = defaultOption?.pricingPhases ?: return null
    val cheapestPhase = phases.minByOrNull { it.price.amountMicros } ?: return null
    return cheapestPhase.price.amountMicros.toDouble() / 1_000_000
}

fun StoreProduct.getCurrency(): CurrencySelectionModel {
    val currencyCode = price.currencyCode.ifBlank { "USD" }
    return allCurrencies.firstOrNull { it.code == currencyCode }
        ?: allCurrencies.first { it.code == "USD" }
}

fun StoreProduct.toTierButton(
    title: String,
    unit: String,
    total: Double?,
    compareTo: Double? = 0.0,
    subTitle: String?,
): TierButton {
    val price = normalPriceAmount().formatCurrency(
        threshold = FormattingThreshold.NONE,
        currency = getCurrency(),
    )
    return TierButton(
        title = title,
        formattedPrice = "$price / $unit",
        formattedPriceAccessibilityLabel = "$price per $unit",
        subTitle = subTitle?.ifEmpty { "" },
        saveAmount = if ((compareTo ?: 0.0) > 0) {
            getDifferenceInPercent(total ?: 0.0, compareTo ?: 0.0)
        } else {
            null
        },
        timeLeft = null,
        storeProduct = this,
    )
}

fun StoreProduct.toPromoTierButton(title: String, compareTo: Double): TierButton {
    val introPrice = introductoryPriceAmount()?.formatCurrency(
        threshold = FormattingThreshold.NONE,
        currency = getCurrency(),
    ) ?: "N/A"
    val normalPrice = normalPriceAmount().formatCurrency(
        threshold = FormattingThreshold.NONE,
        currency = getCurrency(),
    )
    return TierButton(
        title = title,
        formattedPrice = "$introPrice / year",
        formattedPriceAccessibilityLabel = "$introPrice per year",
        subTitle = "For the first year, then $normalPrice per year",
        saveAmount = getDifferenceInPercent(0.0, compareTo),
        timeLeft = null,
        storeProduct = this,
    )
}

fun StoreProduct.toWelcomeTierButton(title: String, compareTo: Double) = toPromoTierButton(title, compareTo)

fun StoreProduct.monthlyToYearly(): Double = normalPriceAmount().times(12)

fun StoreProduct.yearlyToMonthly(): Double = normalPriceAmount().div(12)