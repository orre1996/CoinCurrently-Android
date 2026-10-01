package com.cryptium.coincurrently.features.Market.NFT.Views
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import com.cryptium.coincurrently.sharedui.theme.onSurfaceDim
import com.cryptium.coincurrently.sharedui.theme.surface
import com.cryptium.coincurrently.core.extensions.satoshiBold
import com.cryptium.coincurrently.core.localization.Localization
import com.cryptium.coincurrently.core.extensions.satoshiMedium

@Composable
fun NFTHeaderView(
    modifier: Modifier = Modifier
) {
    val isHorizontal = AccessibilityLayout.isHorizontal

    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
            .clearAndSetSemantics {
                invisibleToUser()
            }
            .background(MaterialTheme.colors.surface)
            .padding(
                PaddingValues(
                    start = 16.dp,
                    top = 16.dp,
                    bottom = 0.dp,
                    end = 16.dp
                )
            )
    ) {
        Text(
            text = Localization.string(if (isHorizontal) "Collection" else "Collection / Floor Price"),
            style = satoshiMedium(14),
            color = MaterialTheme.colors.onSurfaceDim,
            modifier = if (isHorizontal) {
                Modifier
            } else {
                Modifier.weight(1f)
            }
        )

        if (isHorizontal) {
            Text(
                text = Localization.string("Floor Price"),
                style = satoshiMedium(14),
                color = MaterialTheme.colors.onSurfaceDim,
                textAlign = TextAlign.End,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp)
            )
        }

        MinimumTextSize(
            text = "999.9%",
            font = satoshiBold(16),
            extraSize = 20.dp,
            alignment = Alignment.CenterEnd
        ) {
            Text(
                text = Localization.string("24h %"),
                style = satoshiMedium(14),
                color = MaterialTheme.colors.onSurfaceDim
            )
        }
    }
}
