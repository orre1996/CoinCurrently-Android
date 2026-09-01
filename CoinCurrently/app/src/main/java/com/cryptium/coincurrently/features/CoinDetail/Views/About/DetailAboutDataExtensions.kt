package com.cryptium.coincurrently.features.CoinDetail.Views.About
import com.cryptium.coincurrently.R
import com.cryptium.coincurrently.features.CoinDetail.CryptoDetailViewModel
import com.cryptium.coincurrently.features.ExchangeDetail.ExchangeDetailViewModel
import com.cryptium.coincurrently.features.NFTDetail.NFTDetailViewModel
import com.cryptium.coincurrently.core.networking.ResponseModels.*

fun CryptoDetailViewModel.toDetailAboutData(): DetailAboutData {
    val links = cryptoInfo?.links
    val description = cryptoInfo?.description?.en
    val hasDescription = !description.isNullOrEmpty()

    val linkSections = if (links == null) {
        null
    } else {
        buildList<DetailAboutLinkSection> {
            if (links.officialWebsites.isNotEmpty()) {
                add(
                    DetailAboutLinkSection(
                        title = "Official Websites",
                        links = links.officialWebsites,
                        icons = List(links.officialWebsites.size) { R.drawable.website }
                    )
                )
            }
            links.whitepaper?.takeIf { it.isNotEmpty() }?.let { whitepaper ->
                add(
                    DetailAboutLinkSection(
                        title = "Whitepaper",
                        links = listOf(whitepaper),
                        icons = listOf(R.drawable.whitepaper)
                    )
                )
            }
            if (links.communityLinks.isNotEmpty()) {
                add(
                    DetailAboutLinkSection(
                        title = "Communities",
                        links = links.communityLinks,
                        icons = links.communityIcons
                    )
                )
            }
            if (links.repositoryUrls.isNotEmpty()) {
                add(
                    DetailAboutLinkSection(
                        title = "Official Repositories",
                        links = links.repositoryUrls,
                        icons = List(links.repositoryUrls.size) { R.drawable.repository }
                    )
                )
            }
            if (links.relatedWebsites.isNotEmpty()) {
                add(
                    DetailAboutLinkSection(
                        title = "Related Websites",
                        links = links.relatedWebsites,
                        icons = List(links.relatedWebsites.size) { R.drawable.website }
                    )
                )
            }
        }
    }

    return DetailAboutData(
        description = description,
        linkSections = linkSections,
        isEmpty = links?.isAllLinksEmpty == true && !hasDescription,
        emptyMessage = "When information about this coin appears, it'll show up here."
    )
}

fun NFTDetailViewModel.toDetailAboutData(): DetailAboutData {
    val nftDetail = nftDetailInfo
    val description = nftDetail?.description
    val hasDescription = !description.isNullOrEmpty()

    val linkSections = if (nftDetail == null) {
        null
    } else {
        buildList<DetailAboutLinkSection> {
            nftDetail.links?.let { links ->
                if (links.officialWebsites.isNotEmpty()) {
                    add(
                        DetailAboutLinkSection(
                            title = "Official Websites",
                            links = links.officialWebsites,
                            icons = List(links.officialWebsites.size) { R.drawable.website }
                        )
                    )
                }
                if (links.socialMediaUrls.isNotEmpty()) {
                    add(
                        DetailAboutLinkSection(
                            title = "Communities",
                            links = links.socialMediaUrls,
                            icons = links.socialMediaIcons
                        )
                    )
                }
            }
            nftDetail.explorers
                ?.mapNotNull { it.link }
                ?.filter { it.isNotEmpty() }
                ?.let { explorerUrls ->
                    if (explorerUrls.isNotEmpty()) {
                        add(
                            DetailAboutLinkSection(
                                title = "Explorers",
                                links = explorerUrls,
                                icons = List(explorerUrls.size) { R.drawable.explorer },
                            )
                        )
                    }
                }
        }
    }

    return DetailAboutData(
        description = description,
        linkSections = linkSections,
        isEmpty = nftDetail?.isAllLinksEmpty == true && !hasDescription,
        emptyMessage = "When information about this NFT collection appears, it'll show up here."
    )
}

private fun String.isSteemitOrMedium(): Boolean {
    val lower = lowercase()
    return lower.contains("steemit") || lower.contains("medium")
}

private fun exchangeIconForUrl(url: String): Int = when {
    url.contains("steemit", ignoreCase = true) -> R.drawable.steemit
    url.contains("medium", ignoreCase = true) -> R.drawable.medium
    url.contains("telegram", ignoreCase = true) || url.contains("t.me", ignoreCase = true) -> R.drawable.telegram
    else -> R.drawable.website
}

fun ExchangeDetailViewModel.toDetailAboutData(): DetailAboutData {
    val exchange = exchangeInfo

    val description = exchange?.let { ex ->
        when {
            !ex.description.isNullOrBlank() -> ex.description
            !ex.public_notice.isNullOrBlank() -> ex.public_notice
            !ex.alert_notice.isNullOrBlank() -> ex.alert_notice
            else -> ex.status_updates?.firstOrNull()?.description
        }
    }

    val linkSections = if (exchange == null) {
        null
    } else {
        buildList<DetailAboutLinkSection> {
            val allWebsiteUrls = listOfNotNull(
                exchange.url,
                exchange.other_url_1,
                exchange.other_url_2
            ).filter { it.isNotEmpty() }
            val websiteUrls = allWebsiteUrls.filter { !it.isSteemitOrMedium() }
            val steemitMediumUrls = allWebsiteUrls.filter { it.isSteemitOrMedium() }


            if (websiteUrls.isNotEmpty()) {
                add(
                    DetailAboutLinkSection(
                        title = "Official Websites",
                        links = websiteUrls,
                        icons = List(websiteUrls.size) { R.drawable.website }
                    )
                )
            }

            val socialPairs = listOf(
                exchange.facebook_url to R.drawable.meta,
                exchange.reddit_url to R.drawable.reddit,
                exchange.telegram_url to R.drawable.telegram,
                exchange.slack_url to R.drawable.external
            ).filter { (url, _) -> !url.isNullOrEmpty() }
            val twitterUrl = exchange.twitter_handle?.takeIf { it.isNotEmpty() }
                ?.let { "https://twitter.com/$it" }

            val communityUrls = socialPairs.map { it.first!! } + listOfNotNull(twitterUrl) + steemitMediumUrls
            val communityIcons = socialPairs.map { it.second } + listOfNotNull(twitterUrl).map { R.drawable.x } + steemitMediumUrls.map { exchangeIconForUrl(it) }
            if (communityUrls.isNotEmpty()) {
                add(
                    DetailAboutLinkSection(
                        title = "Communities",
                        links = communityUrls,
                        icons = communityIcons
                    )
                )
            }
        }
    }

    val hasLinks = linkSections?.isNotEmpty() == true
    val hasDescription = !description.isNullOrEmpty()
    val isEmpty = exchange != null && !hasLinks && !hasDescription

    val exchangeInfo = exchange?.let { ex ->
        val founded = ex.year_established?.toString()
        val headquarters = ex.country
        val exchangeType = ex.centralized?.let { if (it) "Centralized" else "Decentralized" }
        if (founded != null || headquarters != null || exchangeType != null) {
            ExchangeInfoData(
                founded = founded,
                headquarters = headquarters,
                exchangeType = exchangeType
            )
        } else null
    }

    return DetailAboutData(
        description = description,
        linkSections = linkSections,
        isEmpty = isEmpty,
        emptyMessage = "When information about this exchange appears, it'll show up here.",
        exchangeInfo = exchangeInfo
    )
}
