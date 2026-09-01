package com.cryptium.coincurrently.core.extensions
import com.android.billingclient.api.ProductDetails

fun ProductDetails.getNormalPricePhase(): ProductDetails.PricingPhase? {
    val offerDetails = subscriptionOfferDetails
    if (offerDetails.isNullOrEmpty()) {
        return null
    }

    var mostPricedPhase: ProductDetails.PricingPhase? = null
    var highestPrice = Long.MIN_VALUE

    for (offer in offerDetails) {
        for (phase in offer.pricingPhases.pricingPhaseList) {
            if (phase.priceAmountMicros > highestPrice) {
                highestPrice = phase.priceAmountMicros
                mostPricedPhase = phase
            }
        }
    }

    return mostPricedPhase
}

fun ProductDetails.getIntroductoryOffer(): ProductDetails.PricingPhase? {
    val offerDetails = subscriptionOfferDetails
    if (offerDetails.isNullOrEmpty() || offerDetails.size <= 1) {
        return null
    }

    var leastPricedPhase: ProductDetails.PricingPhase? = null
    var lowestPrice = Long.MAX_VALUE

    for (offer in offerDetails) {
        for (phase in offer.pricingPhases.pricingPhaseList) {
            if (phase.priceAmountMicros < lowestPrice) {
                lowestPrice = phase.priceAmountMicros
                leastPricedPhase = phase
            }
        }
    }

    return leastPricedPhase
}

fun ProductDetails.PricingPhase?.pricingPhaseToDouble(): Double? {
    return this?.priceAmountMicros?.toDouble()?.div(1000000)
}