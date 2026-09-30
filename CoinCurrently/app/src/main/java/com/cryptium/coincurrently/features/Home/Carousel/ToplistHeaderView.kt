package com.cryptium.coincurrently.features.Home.Carousel
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.invisibleToUser
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cryptium.coincurrently.sharedui.components.AccessibilityLayout
import com.cryptium.coincurrently.sharedui.modifiers.MinimumTextSize
import com.cryptium.coincurrently.sharedui.modifiers.scaledImageSize
import com.cryptium.coincurrently.sharedui.loading.ChangeSize
import com.cryptium.coincurrently.sharedui.theme.onSurfaceDim
import com.cryptium.coincurrently.core.extensions.satoshiBold
import com.cryptium.coincurrently.core.localization.Localization
import com.cryptium.coincurrently.sharedui.models.PriceChange

@Composable
fun ToplistHeaderView(
    selectedChangeOption: PriceChange,
    modifier: Modifier = Modifier
) {
    val isHorizontal = AccessibilityLayout.isHorizontal

    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier
            .clearAndSetSemantics {
                invisibleToUser()
            }
            .padding(
                top = 16.dp,
                start = 16.dp,
                end = 16.dp,
                bottom = 0.dp
            )
    ) {
        MinimumTextSize(
            text = "9999",
            font = satoshiBold(16),
        ) {
            Text(
                text = Localization.string("#"),
                style = satoshiBold(14),
                color = MaterialTheme.colors.onSurfaceDim
            )
        }

        Text(
            text = Localization.string(if (isHorizontal) "Market Cap" else "Market Cap / Price"),
            style = satoshiBold(14),
            color = MaterialTheme.colors.onSurfaceDim,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            textAlign = TextAlign.Start,
        )

        if (isHorizontal) {
            Text(
                text = Localization.string("Price"),
                style = satoshiBold(14),
                color = MaterialTheme.colors.onSurfaceDim,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                textAlign = TextAlign.End,
            )
        }

        MinimumTextSize(
            text = "999.9%",
            font = satoshiBold(16),
            extraSize = scaledImageSize(ChangeSize.MEDIUM.triangleSize) + 4.dp,
            alignment = Alignment.CenterEnd,
        ) {
            Text(
                text = "${Localization.string(selectedChangeOption.rawValue)} %",
                style = satoshiBold(14),
                color = MaterialTheme.colors.onSurfaceDim
            )
        }
    }
}