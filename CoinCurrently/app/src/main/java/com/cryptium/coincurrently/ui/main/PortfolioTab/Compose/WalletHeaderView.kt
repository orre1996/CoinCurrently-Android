package com.cryptium.coincurrently.ui.main.PortfolioTab.Compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cryptium.coincurrently.ui.main.Compose.AccessibilityLayout
import com.cryptium.coincurrently.ui.main.Compose.MinimumTextSize
import com.cryptium.coincurrently.ui.main.Compose.onSurfaceDim
import com.cryptium.coincurrently.ui.main.Compose.scaledImageSize
import com.cryptium.coincurrently.ui.main.Compose.surface
import com.cryptium.coincurrently.ui.main.Extensions.satoshiBold
import com.cryptium.coincurrently.ui.main.Extensions.satoshiMedium
import com.cryptium.coincurrently.ui.main.General.Loading.ChangeSize

private val ExcludeFromAccessibility: Modifier = Modifier.clearAndSetSemantics { }

@Composable
fun WalletHeaderView(
    modifier: Modifier = Modifier,
) {
    val isHorizontal = AccessibilityLayout.isHorizontal

    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier
            .then(ExcludeFromAccessibility)
            .background(MaterialTheme.colors.surface)
            .padding(bottom = 16.dp)
    ) {
        Text(
            text = if (isHorizontal) "Coin" else "Coin / Holdings",
            style = satoshiMedium(14),
            color = MaterialTheme.colors.onSurfaceDim,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .then(ExcludeFromAccessibility),
            textAlign = TextAlign.Start,
        )

        if (isHorizontal) {
            Text(
                text = "Holdings",
                style = satoshiMedium(14),
                color = MaterialTheme.colors.onSurfaceDim,
                textAlign = TextAlign.End,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(end = 8.dp)
                    .then(ExcludeFromAccessibility),
            )
        }

        MinimumTextSize(
            text = "999.9%",
            font = satoshiBold(16),
            extraSize = scaledImageSize(ChangeSize.MEDIUM.triangleSize) + 4.dp,
            alignment = Alignment.CenterEnd,
            modifier = ExcludeFromAccessibility,
        ) {
            Text(
                text = "7d %",
                style = satoshiMedium(14),
                color = MaterialTheme.colors.onSurfaceDim,
                modifier = ExcludeFromAccessibility,
            )
        }
    }
}
