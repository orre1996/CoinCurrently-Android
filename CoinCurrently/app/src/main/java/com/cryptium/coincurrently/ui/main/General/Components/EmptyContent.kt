package com.cryptium.coincurrently.ui.main.General.Components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cryptium.coincurrently.ui.main.Compose.AccessibilityLayout
import com.cryptium.coincurrently.ui.main.General.Components.Chart.TAB_PAGE_CONTENT_TALKBACK_TRAVERSAL_INDEX
import com.cryptium.coincurrently.ui.main.Compose.SecondaryButton
import com.cryptium.coincurrently.ui.main.Compose.onSurfaceDim
import com.cryptium.coincurrently.ui.main.Extensions.satoshiBold
import com.cryptium.coincurrently.ui.main.Extensions.satoshiRegular

@Composable
fun EmptyContent(
    title: String,
    subTitle: String,
    buttonTitle: String? = null,
    buttonAction: (() -> Unit)? = null,
    includeAccessibility: Boolean = true,
    tabPageTalkBack: Boolean = false,
    modifier: Modifier = Modifier
) {
    val talkBackEnabled = AccessibilityLayout.isTalkBackEnabled
    Column(
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
            .fillMaxWidth()
    ) {
        Column(
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (includeAccessibility) {
                        Modifier.clearAndSetSemantics {
                            contentDescription = "$title. $subTitle"
                            if (tabPageTalkBack && talkBackEnabled) {
                                traversalIndex = TAB_PAGE_CONTENT_TALKBACK_TRAVERSAL_INDEX
                            }
                        }
                    } else {
                        Modifier
                    }
                )
        ) {
        Text(
            text = title,
            style = satoshiBold(18),
            color = MaterialTheme.colors.onSurface,
            textAlign = TextAlign.Center,
        )

        Text(
            text = subTitle,
            style = satoshiRegular(14),
            color = MaterialTheme.colors.onSurfaceDim,
            textAlign = TextAlign.Center,
        )
        }

        buttonTitle?.let {
            SecondaryButton(
                text = it,
                onClick = {
                    buttonAction?.invoke()
                },
                modifier = Modifier.padding(top = 38.dp)
            )
        }
    }
}
