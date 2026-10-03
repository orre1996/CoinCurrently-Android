package com.cryptium.coincurrently.features.CoinDetail.Views.About
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.invisibleToUser
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cryptium.coincurrently.sharedui.theme.*
import com.cryptium.coincurrently.sharedui.modifiers.*
import com.cryptium.coincurrently.sharedui.components.AccessibilityLayout
import com.cryptium.coincurrently.sharedui.components.tabPageTalkBackTraversal
import com.cryptium.coincurrently.core.extensions.isTablet
import com.cryptium.coincurrently.core.extensions.poppinsSemibold
import com.cryptium.coincurrently.core.extensions.satoshiBold
import com.cryptium.coincurrently.core.localization.Localization
import com.cryptium.coincurrently.core.extensions.satoshiRegular
import com.cryptium.coincurrently.core.analytics.AnalyticsManager
import com.cryptium.coincurrently.core.analytics.ScreenLabel
import com.cryptium.coincurrently.core.analytics.WebViewOpened

/**
 * Generic About view for detail screens (Crypto, NFT, Exchange).
 * Use [DetailAboutData] with the appropriate toDetailAboutData() extension.
 */
@Composable
fun CryptoDetailAboutView(
    data: DetailAboutData,
    fromScreen: ScreenLabel,
    openUrl: (String) -> Unit
) {
    val isDescriptionExpanded = remember { mutableStateOf(false) }
    val isTablet = LocalContext.current.isTablet()
    val talkBackEnabled = AccessibilityLayout.isTalkBackEnabled

    val tabPage = Modifier.tabPageTalkBackTraversal()

    Column(
        verticalArrangement = Arrangement.spacedBy(40.dp),
        modifier = Modifier
            .padding(
                start = 16.dp,
                top = 32.dp,
                end = 16.dp
            )
    ) {
        data.exchangeInfo?.let { exchangeInfo ->
            ExchangeInfoView(
                data = exchangeInfo,
                tabPage = tabPage,
            )
        }

        data.description?.let { description ->
            if (description.isNotEmpty()) {
                val plainDescription = remember(description) { description.plainDescriptionText() }

                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Text(
                        text = "Description",
                        style = poppinsSemibold(24),
                        color = MaterialTheme.colors.onSurfaceDim,
                        modifier = tabPage.semantics { heading() },
                    )

                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .primaryBackgroundNoRipple()
                    ) {
                        CryptoDetailInfoDescription(
                            description = description,
                            isExpanded = isDescriptionExpanded,
                            tabPage = tabPage,
                        )

                        if (!talkBackEnabled && plainDescription.length > 500) {
                            ViewMoreButtonView(
                                isExpanded = isDescriptionExpanded,
                                fromScreen = fromScreen
                            )
                        }
                    }
                }
            }
        }

        when {
            data.linkSections == null -> TempList(isTablet = isTablet, tabPage = tabPage)
            data.isEmpty -> EmptyView(emptyMessage = data.emptyMessage, tabPage = tabPage)
            else -> {
                data.linkSections.forEach { section ->
                    CryptoDetailInfoCell(
                        title = section.title,
                        links = section.links.map { it as String? },
                        icons = section.icons,
                        tabPage = tabPage,
                        callback = { url ->
                            AnalyticsManager.log(
                                WebViewOpened(fromScreen = fromScreen, url = url)
                            )
                            openUrl(url)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun TempList(isTablet: Boolean, tabPage: Modifier) {
    val sectionCount = if (isTablet) 6 else 3
    (0 until sectionCount).forEach { _ ->
        CryptoDetailInfoCell(
            title = null,
            links = listOf(null, null, null),
            tabPage = tabPage,
            callback = { }
        )
    }
}

@Composable
private fun EmptyView(emptyMessage: String, tabPage: Modifier) {
    val localizedTitle = Localization.string("No details yet")
    val localizedMessage = Localization.string(emptyMessage)
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = tabPage
            .padding(
                start = 16.dp,
                end = 16.dp,
                top = 40.dp,
                bottom = 40.dp
            )
            .semantics(mergeDescendants = true) {
                contentDescription = "$localizedTitle. $localizedMessage"
            }
    ) {
        Text(
            text = localizedTitle,
            style = satoshiBold(18),
            color = MaterialTheme.colors.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.semantics { invisibleToUser() },
        )

        Text(
            text = localizedMessage,
            style = satoshiRegular(14),
            color = MaterialTheme.colors.onSurfaceDim,
            textAlign = TextAlign.Center,
            modifier = Modifier.semantics { invisibleToUser() },
        )
    }
}
