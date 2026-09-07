package com.cryptium.coincurrently.core.purchases
import com.cryptium.coincurrently.core.persistence.UserPreferences
import android.app.Activity
import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.cryptium.coincurrently.BuildConfig
import com.cryptium.coincurrently.core.extensions.getActivity
import com.cryptium.coincurrently.core.extensions.getIntroductoryOffer
import com.cryptium.coincurrently.core.extensions.updateAllWidgets
import com.cryptium.coincurrently.core.analytics.SubscriptionRestoreOutcome
import com.revenuecat.purchases.CacheFetchPolicy
import com.revenuecat.purchases.CustomerInfo
import com.revenuecat.purchases.Offerings
import com.revenuecat.purchases.ProductType
import com.revenuecat.purchases.PurchaseParams
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesError
import com.revenuecat.purchases.PurchasesErrorCode
import com.revenuecat.purchases.PurchasesException
import com.revenuecat.purchases.interfaces.GetStoreProductsCallback
import com.revenuecat.purchases.interfaces.PurchaseCallback
import com.revenuecat.purchases.interfaces.ReceiveOfferingsCallback
import com.revenuecat.purchases.interfaces.ReceiveCustomerInfoCallback
import com.revenuecat.purchases.interfaces.UpdatedCustomerInfoListener
import com.revenuecat.purchases.models.GooglePurchasingData
import com.revenuecat.purchases.models.GoogleStoreProduct
import com.revenuecat.purchases.models.GoogleSubscriptionOption
import com.revenuecat.purchases.models.Period
import com.revenuecat.purchases.models.Price
import com.revenuecat.purchases.models.PricingPhase
import com.revenuecat.purchases.models.PurchaseState
import com.revenuecat.purchases.models.StoreProduct
import com.revenuecat.purchases.models.SubscriptionOption
import com.revenuecat.purchases.models.toRecurrenceMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.cryptium.coincurrently.core.networking.MainRepository
import java.util.Date

val monthlyName = "com.monthly.subscription"
val yearlyName = "com.yearly.subscription"
val promotionalName = "com.promotional.offer.subscription"
val welcomeName = "com.welcome.subscription"

/** Must match the entitlement identifier configured in the RevenueCat dashboard. */
const val revenueCatProEntitlementId = "CoinCurrently: Crypto Tracker Pro"

private val subscribedProductIds =
    setOf(monthlyName, yearlyName, promotionalName, welcomeName)

private val subscriptionProductIds = subscribedProductIds.toList()

class BillingManager {
    companion object {
        val currentSubscription: MutableLiveData<String?> = MutableLiveData(UserPreferences.getHasBoughtPro())
        val subscriptionExpirationDate: MutableLiveData<Date?> = MutableLiveData(UserPreferences.getProExpirationDate())

        val storeProducts: MutableLiveData<List<ProductDetails>> = MutableLiveData()
        val isEligableForTrial: MutableLiveData<Boolean> = MutableLiveData(UserPreferences.getIsEligableForTrial())

        var successCallback: () -> Unit = { }
        var cancelCallback: () -> Unit = { }
        var failureCallback: (String) -> Unit = { }
        var pendingPurchaseCallback: () -> Unit = { }

        var restoreSuccessCallback: () -> Unit = { }

        private val storeProductsById = mutableMapOf<String, StoreProduct>()
        private var customerInfoListenerRegistered = false

        private fun isTestStore(): Boolean = BuildConfig.REVENUECAT_API_KEY.startsWith("test_")

        private fun notifyPurchaseFailure(
            message: String = "Something went wrong. Please try again later.",
        ) {
            GlobalScope.launch {
                withContext(Dispatchers.Main) {
                    failureCallback(message)
                }
            }
        }

        private fun purchaseFailureMessage(error: PurchasesError): String {
            return when (error.code) {
                PurchasesErrorCode.ProductNotAvailableForPurchaseError -> {
                    if (isTestStore()) {
                        "This subscription isn't available in the RevenueCat Test Store. " +
                            "Add matching products under Product catalog in the RevenueCat dashboard, " +
                            "or use a release build to test via Google Play."
                    } else {
                        "This subscription isn't available right now. Please try again later."
                    }
                }
                PurchasesErrorCode.PurchaseCancelledError ->
                    "Purchase cancelled."
                else -> error.message
            }
        }

        fun setUpBillingClient(
            context: Context,
            callback: (List<ProductDetails>, List<StoreProduct>) -> Unit = { _, _ -> },
        ) {
            storeProducts.value?.let {
                if (it.isNotEmpty()) {
                    callback.invoke(it, storeProductsById.values.toList())
                    syncSubscriptionState(context)
                    getUser()
                    return
                }
            }

            registerCustomerInfoListener(context)
            fetchProducts(context, callback)
            syncSubscriptionState(context)
            getUser()
        }

        private fun getUser() {
            GlobalScope.launch(Dispatchers.IO) {
                MainRepository.getUser()
            }
        }

        fun purchaseStoreProduct(storeProduct: StoreProduct, activity: Activity) {
            if (activity.isFinishing || activity.isDestroyed) {
                notifyPurchaseFailure()
                return
            }

            purchase(activity, PurchaseParams.Builder(activity, storeProduct).build())
        }

        fun purchaseProduct(productDetail: ProductDetails, activity: Activity) {
            if (activity.isFinishing || activity.isDestroyed) {
                notifyPurchaseFailure()
                return
            }

            val storeProduct = storeProductsById[productDetail.productId]
            if (storeProduct != null) {
                val purchaseParams = buildPurchaseParams(productDetail, storeProduct, activity)
                if (purchaseParams != null) {
                    purchase(activity, purchaseParams)
                    return
                }
            }

            if (isTestStore()) {
                notifyPurchaseFailure(
                    "This subscription isn't available in the RevenueCat Test Store. " +
                        "Add matching products under Product catalog in the RevenueCat dashboard.",
                )
                return
            }

            val subscriptionOption = subscriptionOptionFromProductDetails(productDetail)
            if (subscriptionOption != null) {
                purchase(activity, PurchaseParams.Builder(activity, subscriptionOption).build())
                return
            }

            notifyPurchaseFailure()
        }

        private fun purchase(activity: Activity, purchaseParams: PurchaseParams) {
            try {
                Purchases.sharedInstance.purchase(
                    purchaseParams,
                    object : PurchaseCallback {
                        override fun onCompleted(
                            storeTransaction: com.revenuecat.purchases.models.StoreTransaction,
                            customerInfo: CustomerInfo,
                        ) {
                            GlobalScope.launch {
                                withContext(Dispatchers.Main) {
                                    if (storeTransaction.purchaseState == PurchaseState.PENDING) {
                                        pendingPurchaseCallback()
                                        return@withContext
                                    }

                                    val hadPro = currentSubscription.value != null
                                    if (applyCustomerInfo(customerInfo)) {
                                        if (!hadPro) {
                                            isEligableForTrial.value = false
                                            UserPreferences.setIsEligableForTrial(false)
                                        }
                                        restoreSuccessCallback()
                                        successCallback()
                                    } else {
                                        notifyPurchaseFailure()
                                    }
                                }
                            }
                        }

                        override fun onError(error: PurchasesError, userCancelled: Boolean) {
                            GlobalScope.launch {
                                withContext(Dispatchers.Main) {
                                    if (userCancelled || error.code == PurchasesErrorCode.PurchaseCancelledError) {
                                        cancelCallback()
                                    } else {
                                        notifyPurchaseFailure(purchaseFailureMessage(error))
                                    }
                                }
                            }
                        }
                    },
                )
            } catch (e: PurchasesException) {
                notifyPurchaseFailure(purchaseFailureMessage(e.error))
            }
        }

        private fun buildPurchaseParams(
            productDetail: ProductDetails,
            storeProduct: StoreProduct,
            activity: Activity,
        ): PurchaseParams? {
            val googleProduct = storeProduct as? GoogleStoreProduct
            if (googleProduct == null) {
                return PurchaseParams.Builder(activity, storeProduct).build()
            }

            val subscriptionOption = subscriptionOptionForPurchase(productDetail, storeProduct)
                ?: googleProduct.defaultOption
                ?: googleProduct.subscriptionOptions?.firstOrNull()
                ?: return null

            return PurchaseParams.Builder(activity, subscriptionOption).build()
        }

        fun checkIfUserSubscribes(
            context: Context,
            success: () -> Unit = {},
            failure: () -> Unit = {},
            restoreAnalytics: ((SubscriptionRestoreOutcome) -> Unit)? = null,
        ) {
            fun handleRestoreResult(customerInfo: CustomerInfo) {
                GlobalScope.launch {
                    withContext(Dispatchers.Main) {
                        val hadPro = currentSubscription.value != null
                        val hasPro = applyCustomerInfo(customerInfo)

                        if (hasPro) {
                            if (!hadPro) {
                                context.getActivity()?.updateAllWidgets()
                            }
                            restoreAnalytics?.invoke(SubscriptionRestoreOutcome.Restored)
                            success.invoke()
                        } else {
                            if (hadPro) {
                                context.getActivity()?.updateAllWidgets()
                            }
                            restoreAnalytics?.invoke(SubscriptionRestoreOutcome.NothingToRestore)
                            failure.invoke()
                        }
                    }
                }
            }

            fun restoreFromPlayStore() {
                Purchases.sharedInstance.restorePurchases(
                    object : ReceiveCustomerInfoCallback {
                        override fun onReceived(customerInfo: CustomerInfo) {
                            handleRestoreResult(customerInfo)
                        }

                        override fun onError(error: PurchasesError) {
                            GlobalScope.launch {
                                withContext(Dispatchers.Main) {
                                    restoreAnalytics?.invoke(SubscriptionRestoreOutcome.Failed)
                                    failure.invoke()
                                }
                            }
                        }
                    },
                )
            }

            Purchases.sharedInstance.getCustomerInfo(
                CacheFetchPolicy.FETCH_CURRENT,
                object : ReceiveCustomerInfoCallback {
                    override fun onReceived(customerInfo: CustomerInfo) {
                        GlobalScope.launch {
                            withContext(Dispatchers.Main) {
                                if (applyCustomerInfo(customerInfo)) {
                                    restoreAnalytics?.invoke(SubscriptionRestoreOutcome.Restored)
                                    success.invoke()
                                } else {
                                    restoreFromPlayStore()
                                }
                            }
                        }
                    }

                    override fun onError(error: PurchasesError) {
                        restoreFromPlayStore()
                    }
                },
            )
        }

        private fun registerCustomerInfoListener(context: Context) {
            if (customerInfoListenerRegistered) return
            customerInfoListenerRegistered = true

            Purchases.sharedInstance.updatedCustomerInfoListener =
                UpdatedCustomerInfoListener { customerInfo ->
                    val previous = currentSubscription.value
                    applyCustomerInfo(customerInfo)
                    if (previous != currentSubscription.value) {
                        context.getActivity()?.updateAllWidgets()
                    }
                }
        }

        private fun syncSubscriptionState(context: Context) {
            Purchases.sharedInstance.getCustomerInfo(
                CacheFetchPolicy.FETCH_CURRENT,
                object : ReceiveCustomerInfoCallback {
                    override fun onReceived(customerInfo: CustomerInfo) {
                        GlobalScope.launch {
                            withContext(Dispatchers.Main) {
                                val previous = currentSubscription.value
                                applyCustomerInfo(customerInfo)
                                if (previous != currentSubscription.value) {
                                    context.getActivity()?.updateAllWidgets()
                                }
                            }
                        }
                    }

                    override fun onError(error: PurchasesError) {
                        // Keep cached preference state on transient errors.
                    }
                },
            )
        }

        private fun fetchProducts(
            context: Context,
            callback: (List<ProductDetails>, List<StoreProduct>) -> Unit = { _, _ -> },
        ) {
            val appContext = context.applicationContext
            var paywallPublished = false

            fun publishForPaywall(
                playProducts: List<ProductDetails>,
                revenueCatProducts: List<StoreProduct>,
            ) {
                if (paywallPublished) return
                if (playProducts.isEmpty() && revenueCatProducts.isEmpty()) return
                paywallPublished = true
                GlobalScope.launch {
                    withContext(Dispatchers.Main) {
                        publishPaywallProducts(playProducts, revenueCatProducts, callback)
                    }
                }
            }

            fun handleFetchedStoreProducts(
                fetchedStoreProducts: List<StoreProduct>,
                billingProducts: List<ProductDetails> = emptyList(),
            ) {
                val (revenueCatPlayProducts, revenueCatStoreProducts) =
                    processFetchedStoreProducts(fetchedStoreProducts)
                when {
                    revenueCatPlayProducts.isNotEmpty() ->
                        publishForPaywall(revenueCatPlayProducts, revenueCatStoreProducts)
                    revenueCatStoreProducts.isNotEmpty() ->
                        publishForPaywall(emptyList(), revenueCatStoreProducts)
                    !paywallPublished && billingProducts.isNotEmpty() && !isTestStore() ->
                        publishForPaywall(billingProducts, emptyList())
                }
            }

            // getOfferings fires its callback immediately when offerings are already cached in
            // memory, so the paywall can render instantly on repeat launches without waiting on
            // Billing/network. On a cold cache it fetches from the network in the background while
            // the Billing path below races to publish first.
            Purchases.sharedInstance.getOfferings(
                object : ReceiveOfferingsCallback {
                    override fun onReceived(offerings: Offerings) {
                        handleFetchedStoreProducts(storeProductsFromOfferings(offerings))
                    }

                    override fun onError(error: PurchasesError) {
                        // Fall through to the Billing/getProducts path below.
                    }
                },
            )

            if (isTestStore()) {
                Purchases.sharedInstance.getProducts(
                    subscriptionProductIds,
                    ProductType.SUBS,
                    object : GetStoreProductsCallback {
                        override fun onReceived(fetchedStoreProducts: List<StoreProduct>) {
                            handleFetchedStoreProducts(fetchedStoreProducts)
                        }

                        override fun onError(error: PurchasesError) {
                            // Paywall stays empty until Test Store products are configured.
                        }
                    },
                )
                return
            }

            // Billing first, then RevenueCat — avoids two BillingClients at once.
            queryPlayProductDetailsFromBillingClient(appContext) { billingProducts ->
                if (billingProducts.isNotEmpty()) {
                    publishForPaywall(billingProducts, emptyList())
                }

                Purchases.sharedInstance.getProducts(
                    subscriptionProductIds,
                    ProductType.SUBS,
                    object : GetStoreProductsCallback {
                        override fun onReceived(fetchedStoreProducts: List<StoreProduct>) {
                            handleFetchedStoreProducts(fetchedStoreProducts, billingProducts)
                        }

                        override fun onError(error: PurchasesError) {
                            if (!paywallPublished) {
                                publishForPaywall(billingProducts, emptyList())
                            }
                        }
                    },
                )
            }
        }

        /** Flattens every package across all offerings into their backing [StoreProduct]s. */
        private fun storeProductsFromOfferings(offerings: Offerings): List<StoreProduct> {
            return offerings.all.values
                .flatMap { offering -> offering.availablePackages }
                .map { it.product }
        }

        /**
         * RevenueCat returns one [StoreProduct] per base plan (e.g. 8 for 4 subscription IDs).
         * Collapse to one [ProductDetails] per Play product id for the custom paywall.
         */
        private fun processFetchedStoreProducts(
            fetchedStoreProducts: List<StoreProduct>,
        ): Pair<List<ProductDetails>, List<StoreProduct>> {
            if (fetchedStoreProducts.isEmpty()) return emptyList<ProductDetails>() to emptyList()

            val playProductDetailsById = linkedMapOf<String, ProductDetails>()
            val storeProductsForPurchase = linkedMapOf<String, StoreProduct>()

            for (storeProduct in fetchedStoreProducts) {
                val playProductId = storeProduct.playProductId() ?: continue
                if (playProductId !in subscribedProductIds) continue

                if (!storeProductsForPurchase.containsKey(playProductId)) {
                    storeProductsForPurchase[playProductId] = storeProduct
                }

                val productDetails = storeProduct.playProductDetails()
                if (productDetails != null && !playProductDetailsById.containsKey(playProductId)) {
                    playProductDetailsById[playProductId] = productDetails
                }
            }

            if (storeProductsForPurchase.isNotEmpty()) {
                storeProductsById.putAll(storeProductsForPurchase)
            }

            return playProductDetailsById.values.toList() to storeProductsForPurchase.values.toList()
        }

        private fun publishPaywallProducts(
            playProductDetails: List<ProductDetails>,
            revenueCatProducts: List<StoreProduct>,
            callback: (List<ProductDetails>, List<StoreProduct>) -> Unit,
        ) {
            val monthlyPlayProduct = playProductDetails.firstOrNull { it.productId == monthlyName }
            val monthlyStoreProduct = revenueCatProducts.firstOrNull { it.playProductId() == monthlyName }
            isEligableForTrial.value = when {
                monthlyPlayProduct != null -> monthlyPlayProduct.getIntroductoryOffer() != null
                monthlyStoreProduct != null -> monthlyStoreProduct.hasIntroductoryOffer()
                else -> false
            }
            storeProducts.value = playProductDetails
            callback.invoke(playProductDetails, revenueCatProducts)
        }

        private fun StoreProduct.hasIntroductoryOffer(): Boolean {
            val phases = defaultOption?.pricingPhases ?: return false
            return phases.size > 1 && phases.any { it.price.amountMicros == 0L }
        }

        private fun StoreProduct.playProductId(): String? {
            (this as? GoogleStoreProduct)?.productId?.let { return it }
            when (val data = purchasingData) {
                is GooglePurchasingData -> return data.productId
            }
            val idPrefix = id.substringBefore(":")
            if (idPrefix in subscribedProductIds) return idPrefix
            if (id in subscribedProductIds) return id
            return null
        }

        private fun StoreProduct.playProductDetails(): ProductDetails? {
            return (this as? GoogleStoreProduct)?.productDetails
                ?: when (val data = purchasingData) {
                    is GooglePurchasingData.InAppProduct -> data.productDetails
                    is GooglePurchasingData.Subscription -> data.productDetails
                    else -> null
                }
        }

        /** Loads [ProductDetails] from Google Play for the custom paywall UI. */
        private fun queryPlayProductDetailsFromBillingClient(
            context: Context,
            callback: (List<ProductDetails>) -> Unit,
        ) {
            val billingClient = BillingClient.newBuilder(context)
                .setListener(PurchasesUpdatedListener { _, _ -> })
                .enablePendingPurchases(
                    PendingPurchasesParams.newBuilder()
                        .enableOneTimeProducts()
                        .build(),
                )
                .build()

            val productList = subscriptionProductIds.map { productId ->
                QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(productId)
                    .setProductType(BillingClient.ProductType.SUBS)
                    .build()
            }
            val params = QueryProductDetailsParams.newBuilder()
                .setProductList(productList)
                .build()

            billingClient.startConnection(object : BillingClientStateListener {
                override fun onBillingSetupFinished(billingResult: BillingResult) {
                    if (billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
                        billingClient.endConnection()
                        callback(emptyList())
                        return
                    }

                    billingClient.queryProductDetailsAsync(params) { queryResult, productDetailsResult ->
                        val products =
                            if (queryResult.responseCode == BillingClient.BillingResponseCode.OK) {
                                productDetailsResult.productDetailsList
                            } else {
                                emptyList()
                            }
                        billingClient.endConnection()
                        callback(products)
                    }
                }

                override fun onBillingServiceDisconnected() {
                    callback(emptyList())
                }
            })
        }

        /**
         * Builds a RevenueCat [GoogleSubscriptionOption] from Play [ProductDetails] so purchases work
         * when the paywall was loaded via BillingClient.
         */
        private fun subscriptionOptionFromProductDetails(
            productDetail: ProductDetails,
        ): GoogleSubscriptionOption? {
            val offers = productDetail.subscriptionOfferDetails ?: return null
            val offerToken = offers.firstOrNull()?.offerToken
            val offer = offers.firstOrNull { it.offerToken == offerToken } ?: return null
            return offer.toGoogleSubscriptionOption(productDetail.productId, productDetail)
        }

        private fun ProductDetails.SubscriptionOfferDetails.toGoogleSubscriptionOption(
            productId: String,
            productDetails: ProductDetails,
        ): GoogleSubscriptionOption {
            val pricingPhases = pricingPhases.pricingPhaseList.map { it.toRevenueCatPricingPhase() }
            return GoogleSubscriptionOption(
                productId,
                basePlanId,
                offerId,
                pricingPhases,
                offerTags,
                productDetails,
                offerToken,
            )
        }

        private fun ProductDetails.PricingPhase.toRevenueCatPricingPhase(): PricingPhase {
            return PricingPhase(
                Period.create(billingPeriod),
                recurrenceMode.toRecurrenceMode(),
                billingCycleCount,
                Price(formattedPrice, priceAmountMicros, priceCurrencyCode),
            )
        }

        /** Matches legacy BillingClient behavior: first subscription offer on the product. */
        private fun subscriptionOptionForPurchase(
            productDetail: ProductDetails,
            storeProduct: StoreProduct,
        ): SubscriptionOption? {
            val googleProduct = storeProduct as? GoogleStoreProduct ?: return null
            val options = googleProduct.subscriptionOptions ?: return googleProduct.defaultOption
            val offerToken = productDetail.subscriptionOfferDetails?.firstOrNull()?.offerToken
                ?: return googleProduct.defaultOption

            return options.firstOrNull { option ->
                (option as? GoogleSubscriptionOption)?.offerToken == offerToken
            } ?: googleProduct.defaultOption
        }

        /** @return true when the user has an active Pro entitlement. */
        fun applyCustomerInfo(customerInfo: CustomerInfo): Boolean {
            val entitlement = customerInfo.entitlements[revenueCatProEntitlementId]
                ?: customerInfo.entitlements.active.values.firstOrNull()

            if (entitlement?.isActive == true) {
                val productId = entitlement.productIdentifier.takeIf { it.isNotEmpty() }
                    ?: customerInfo.activeSubscriptions.firstOrNull()
                    ?: promotionalName
                val expirationDate = entitlement.expirationDate
                currentSubscription.value = productId
                subscriptionExpirationDate.value = expirationDate
                UserPreferences.setHasBoughtPro(productId)
                UserPreferences.setProExpirationDate(expirationDate)
                return true
            }

            currentSubscription.value = null
            subscriptionExpirationDate.value = null
            UserPreferences.setHasBoughtPro(null)
            UserPreferences.setProExpirationDate(null)
            return false
        }
    }
}
