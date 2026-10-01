package com.cryptium.coincurrently.features.CoinDetail.Views.About
import com.cryptium.coincurrently.sharedui.modifiers.imageSize

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.invisibleToUser
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.cryptium.coincurrently.sharedui.theme.onSurfaceDimmer
import com.cryptium.coincurrently.sharedui.theme.outlineVariant
import com.cryptium.coincurrently.sharedui.theme.primaryBackgroundNoRipple
import com.cryptium.coincurrently.sharedui.modifiers.shimmeringModifier
import com.cryptium.coincurrently.core.extensions.satoshiBold
import com.cryptium.coincurrently.core.localization.Localization
import com.cryptium.coincurrently.core.extensions.satoshiMedium


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CryptoDetailInfoCell(
    title: String?,
    links: List<String?>,
    icons: List<Int> = listOf(),
    tabPage: Modifier = Modifier,
    callback: (String) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = title?.let { Localization.string(it) } ?: "00000000",
            style = satoshiBold(22),
            color = MaterialTheme.colors.onSurface,
            modifier = Modifier
                .shimmeringModifier(title == null)
                .then(tabPage)
                .semantics { heading() }
        )

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .primaryBackgroundNoRipple()
        ) {
            val visibleLinks = links.filter { it?.isNotEmpty() == true || title == null }
            visibleLinks.forEachIndexed { index, link ->
                val displayLabel = link?.toDisplayDomain() ?: "000000000000"
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .then(tabPage)
                        .semantics(mergeDescendants = true) {
                            contentDescription = "${index + 1} of ${visibleLinks.size}, $displayLabel"
                            onClick(label = "Open link", action = {
                                link?.let { url -> callback(url) }
                                true
                            })
                        }
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            link?.let {
                                callback(it)
                            }
                        }
                            .border(
                                BorderStroke(
                                    width = 1.dp,
                                    color = MaterialTheme.colors.outlineVariant
                                ), shape = CircleShape
                            )
                            .padding(
                                start = 12.dp,
                                top = 10.dp,
                                bottom = 10.dp,
                                end = 16.dp
                            )
                    ) {
                        (icons.getOrNull(index) ?: icons.getOrNull(0))?.let {
                            Image(
                                painter = painterResource(it),
                                contentDescription = null,
                                colorFilter = ColorFilter.tint(MaterialTheme.colors.onSurfaceDimmer),
                                modifier = Modifier
                                    .imageSize(24.dp)
                                    .semantics { invisibleToUser() },
                            )
                        }

                        Text(
                            text = displayLabel,
                            style = satoshiMedium(16),
                            color = MaterialTheme.colors.onSurface,
                            modifier = Modifier
                                .shimmeringModifier(title == null)
                                .semantics { invisibleToUser() }
                        )
                    }
            }
        }
    }
}

/**
 * Extracts a short display label from a URL for chip display (e.g. "binance.com" or "Twitter")
 */
private fun String.toDisplayDomain(): String {
    val lowercased = lowercase()
    when {
        lowercased.contains("twitter.com") || lowercased.contains("x.com") -> return "Twitter"
        lowercased.contains("reddit.com") -> return "Reddit"
        lowercased.contains("facebook.com") || lowercased.contains("fb.com") || lowercased.contains("meta") -> return "Meta"
        lowercased.contains("telegram") || lowercased.contains("t.me") -> return "Telegram"
        lowercased.contains("github.com") -> return "GitHub"
        lowercased.contains("bitbucket") -> return "Bitbucket"
        lowercased.contains("medium.com") -> return "Medium"
        lowercased.contains("steemit.com") || lowercased.contains("steemit") -> return "Steemit"
        lowercased.contains("discord") -> return "Discord"
        lowercased.contains("youtube.com") || lowercased.contains("youtu.be") -> return "YouTube"
    }
    return try {
        val url = java.net.URL(if (startsWith("http")) this else "https://$this")
        val host = url.host ?: return this
        if (host.startsWith("www.")) host.removePrefix("www.") else host
    } catch (_: Exception) {
        this
    }
}