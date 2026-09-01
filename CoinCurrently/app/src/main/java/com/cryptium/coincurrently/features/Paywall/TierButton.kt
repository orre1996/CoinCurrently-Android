package com.cryptium.coincurrently.features.Paywall
import com.android.billingclient.api.ProductDetails
import com.revenuecat.purchases.models.StoreProduct

data class TierButton(
    val title: String,
    val formattedPrice: String,
    val formattedPriceAccessibilityLabel: String,
    val subTitle: String? = null,
    val saveAmount: Double? = null,
    val timeLeft: String? = null,
    val product: ProductDetails? = null,
    val storeProduct: StoreProduct? = null,
) {
    val productId: String
        get() = product?.productId ?: storeProduct?.id?.substringBefore(":")
            ?: storeProduct?.id
            ?: ""

    init {
        require(product != null || storeProduct != null) {
            "TierButton requires either a Play ProductDetails or a RevenueCat StoreProduct"
        }
    }

    fun copyWithTimeLeft(timeLeft: String): TierButton {
        return this.copy(timeLeft = timeLeft)
    }
}