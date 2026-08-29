package com.cryptium.coincurrently.ui.main.MarketTab.Treasuries.Views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.invisibleToUser
import androidx.compose.ui.unit.dp
import com.cryptium.coincurrently.ui.main.Compose.onSurfaceDim
import com.cryptium.coincurrently.ui.main.Compose.surface
import com.cryptium.coincurrently.ui.main.Extensions.satoshiMedium

@Composable
fun TreasuriesHeaderView(
    modifier: Modifier = Modifier,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier
            .clearAndSetSemantics { invisibleToUser() }
            .background(MaterialTheme.colors.surface)
            .padding(
                top = 16.dp,
                start = 16.dp,
                end = 16.dp,
            )
    ) {
        Text(
            text = "Treasury",
            style = satoshiMedium(14),
            color = MaterialTheme.colors.onSurfaceDim,
            modifier = Modifier.weight(1f),
        )

        Text(
            text = "Holdings",
            style = satoshiMedium(14),
            color = MaterialTheme.colors.onSurfaceDim,
        )
    }
}
