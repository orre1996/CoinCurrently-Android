package com.cryptium.coincurrently.features.CoinDetail.Views.About
/**
 * Data for the generic CryptoDetailAboutView.
 * Used by CryptoDetailScreen, NFTDetailScreen, and ExchangeDetailScreen.
 */
data class DetailAboutData(
    val description: String?,
    val linkSections: List<DetailAboutLinkSection>?,
    val isEmpty: Boolean,
    val emptyMessage: String,
    /** Exchange-specific info (Founded, Headquarters, Exchange Type). Only set for exchanges. */
    val exchangeInfo: ExchangeInfoData? = null
)

/**
 * Exchange-specific info shown in the "Exchange Info" section.
 */
data class ExchangeInfoData(
    val founded: String?,
    val headquarters: String?,
    val exchangeType: String?
)

data class DetailAboutLinkSection(
    val title: String,
    val links: List<String>,
    val icons: List<Int>
)
