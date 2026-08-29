package com.cryptium.coincurrently.ui.main.MarketTab.CryptoDetail.Components.About

import android.text.Html
import androidx.compose.foundation.layout.Box
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.cryptium.coincurrently.ui.main.Compose.AccessibilityLayout
import com.cryptium.coincurrently.ui.main.Compose.onSurfaceDim
import com.cryptium.coincurrently.ui.main.Extensions.satoshiRegular
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.invisibleToUser
import androidx.compose.ui.semantics.semantics

internal fun String.plainDescriptionText(): String =
    Html.fromHtml(this, Html.FROM_HTML_MODE_LEGACY).toString()
        .trimStart()
        .trimEnd()

@Composable
fun CryptoDetailInfoDescription(
    description: String?,
    isExpanded: MutableState<Boolean>,
    tabPage: Modifier = Modifier,
) {
    val talkBackEnabled = AccessibilityLayout.isTalkBackEnabled
    val plainText = remember(description) {
        description?.plainDescriptionText().orEmpty()
    }

    val text = if (isExpanded.value || talkBackEnabled) {
        plainText
    } else if (plainText.length > 500) {
        plainText.take(500) + "..."
    } else {
        plainText
    }

    Box(
        modifier = tabPage.semantics {
            contentDescription = plainText
        }
    ) {
        Text(
            text = text,
            style = satoshiRegular(16),
            color = MaterialTheme.colors.onSurfaceDim,
            modifier = if (talkBackEnabled) {
                Modifier.semantics { invisibleToUser() }
            } else {
                Modifier
            },
        )
    }
}
