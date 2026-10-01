package com.cryptium.coincurrently.features.Market.Exchanges.Views
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
import androidx.compose.ui.unit.dp
import com.cryptium.coincurrently.sharedui.components.AccessibilityLayout
import com.cryptium.coincurrently.sharedui.modifiers.MinimumTextSize
import com.cryptium.coincurrently.sharedui.theme.onSurfaceDim
import com.cryptium.coincurrently.sharedui.theme.surface
import com.cryptium.coincurrently.core.extensions.satoshiBold
import com.cryptium.coincurrently.core.localization.Localization
import com.cryptium.coincurrently.core.extensions.satoshiMedium

@Composable
fun ExchangesHeaderView(
    modifier: Modifier = Modifier
) {
    val isHorizontal = AccessibilityLayout.isHorizontal

    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier
            .clearAndSetSemantics {
                invisibleToUser()
            }
            .background(MaterialTheme.colors.surface)
            .padding(
                PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 16.dp,
                    bottom = 0.dp,
                )
            )
    ) {
        Text(
            text = Localization.string(if (isHorizontal) "Exchange" else "Exchange / 24h Volume"),
            style = satoshiMedium(14),
            color = MaterialTheme.colors.onSurfaceDim,
            modifier = Modifier
                .weight(1f)
        )

        if (isHorizontal) {
            Text(
                text = Localization.string("24h Volume"),
                style = satoshiMedium(14),
                color = MaterialTheme.colors.onSurfaceDim,
            )
        }

        MinimumTextSize(
            text = "10/10",
            font = satoshiBold(16),
            alignment = Alignment.CenterEnd
        ) {
            Text(
                text = Localization.string("Trust"),
                style = satoshiMedium(14),
                color = MaterialTheme.colors.onSurfaceDim
            )
        }
    }
}
